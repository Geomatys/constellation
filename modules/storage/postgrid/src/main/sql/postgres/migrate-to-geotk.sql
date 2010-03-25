---
--- Migrate a database in the legacy PostGris schema
--- to the new schema defined in Geotoolkit.org.
---

--- Formats ----------------------------------------------------------------------------------------
INSERT INTO coverages."Formats" ("name", "plugin", "packMode", "comments")
SELECT "name", "mime", "encoding"::coverages."PackMode", "comment" FROM postgrid."Formats" WHERE "name" <> 'PNG' AND "name" <> 'TIFF';

--- SampleDimensions -------------------------------------------------------------------------------
INSERT INTO coverages."SampleDimensions" ("format", "band", "name", "units")
SELECT "format", "band", "identifier", "units" FROM postgrid."SampleDimensions";

--- Categories -------------------------------------------------------------------------------------
INSERT INTO coverages."Categories" ("format", "band", "name", "lower", "upper", "c0", "c1", "function", "colors")
(SELECT "format", "SampleDimensions"."band", "name", "lower", "upper", "c0", "c1", "function"::coverages."Function", "colors" FROM postgrid."Categories"
JOIN postgrid."SampleDimensions" ON "Categories"."band" = "SampleDimensions"."identifier");

--- Layers -----------------------------------------------------------------------------------------
INSERT INTO coverages."Layers" ("name", "period", "minScale", "maxScale", "fallback", "comments")
SELECT "name", "period", "minScale", "maxScale", "fallback", "description" FROM postgrid."Layers";

--- Series -----------------------------------------------------------------------------------------
CREATE TABLE postgrid."SeriesID" (
    "name" character varying NOT NULL PRIMARY KEY,
    "identifier" serial NOT NULL UNIQUE
);

INSERT INTO postgrid."SeriesID" ("name") SELECT "identifier" FROM postgrid."Series";

INSERT INTO coverages."Series" ("identifier", "layer", "pathname", "extension", "format", "quicklook")
SELECT main."identifier", "layer", "pathname", "extension", "format", fb."identifier" FROM postgrid."Series"
JOIN postgrid."SeriesID" AS main ON "Series"."identifier" = main."name"
LEFT JOIN postgrid."SeriesID" AS fb ON "quicklook" = fb."name";

--- GridGeometries ---------------------------------------------------------------------------------
CREATE TABLE postgrid."GridGeometriesID" (
    "name" character varying NOT NULL PRIMARY KEY,
    "identifier" serial NOT NULL UNIQUE
);

INSERT INTO postgrid."GridGeometriesID" ("name") SELECT "identifier" FROM postgrid."GridGeometries";

INSERT INTO coverages."GridGeometries" ("identifier", "width", "height", "scaleX", "shearY", "shearX", "scaleY", "translateX", "translateY", "horizontalSRID", "verticalSRID", "verticalOrdinates")
SELECT "GridGeometriesID"."identifier", "width", "height", "scaleX", "shearY", "shearX", "scaleY", "translateX", "translateY", "horizontalSRID", "verticalSRID", "verticalOrdinates"
FROM postgrid."GridGeometries" JOIN postgrid."GridGeometriesID" ON "GridGeometries"."identifier" = "GridGeometriesID"."name";

--- GridCoverages ----------------------------------------------------------------------------------
INSERT INTO coverages."GridCoverages" ("series", "filename", "index", "startTime", "endTime", "extent")
SELECT "SeriesID"."identifier", "filename", "index", "startTime", "endTime", "GridGeometriesID"."identifier" FROM ONLY postgrid."GridCoverages"
JOIN postgrid."SeriesID" ON "GridCoverages"."series" = "SeriesID"."name"
JOIN postgrid."GridGeometriesID" ON "GridCoverages"."extent" = "GridGeometriesID"."name";
