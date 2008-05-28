--------------------------------------------------------------------------------------------------
--  Creates the "postgrid" schema with no data.                                                 --
--------------------------------------------------------------------------------------------------

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;
SET default_tablespace = '';
SET default_with_oids = false;

CREATE SCHEMA postgrid;
ALTER SCHEMA postgrid OWNER TO geoadmin;
GRANT ALL ON SCHEMA postgrid TO geoadmin;
GRANT USAGE ON SCHEMA postgrid TO PUBLIC;

COMMENT ON SCHEMA postgrid IS 'Metadata for grid coverages';

SET search_path = postgrid, postgis, pg_catalog;




--------------------------------------------------------------------------------------------------
-- Creates the "Permissions" table.                                                             --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Permissions" (
    "name"        character varying NOT NULL DEFAULT 'Public',
    "user"        character varying NOT NULL DEFAULT 'Anonymous',
    "WMS"         boolean NOT NULL DEFAULT TRUE,
    "WCS"         boolean NOT NULL DEFAULT TRUE,
    "description" character varying,
    PRIMARY KEY ("name", "user")
);

ALTER TABLE "Permissions" OWNER TO geoadmin;
GRANT ALL ON TABLE "Permissions" TO geoadmin;
GRANT SELECT ON TABLE "Permissions" TO PUBLIC;

COMMENT ON TABLE "Permissions" IS
    'Permissions to view and obtain coverage data.';
COMMENT ON COLUMN "Permissions"."name" IS
    'Permission name.';
COMMENT ON COLUMN "Permissions"."user" IS
    'User for who the permission is applied';
COMMENT ON COLUMN "Permissions"."WMS" IS
    'Whatever permission allows Web Map Server (WMS) access.';
COMMENT ON COLUMN "Permissions"."WCS" IS
    'Whatever permission allows Web Coverage Server (WCS) access.';
COMMENT ON COLUMN "Permissions"."description" IS
    'A description of this permission (scope, etc.).';




--------------------------------------------------------------------------------------------------
-- Creates the "Formats" table.                                                                 --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Formats" (
    "name"     character varying NOT NULL PRIMARY KEY,
    "mime"     character varying NOT NULL,
    "encoding" character varying NOT NULL DEFAULT 'native'
               CHECK ("encoding"='geophysics' OR "encoding"='native')
);

ALTER TABLE "Formats" OWNER TO geoadmin;
GRANT ALL ON TABLE "Formats" TO geoadmin;
GRANT SELECT ON TABLE "Formats" TO PUBLIC;

COMMENT ON TABLE "Formats" IS
    'Image formats (PNG, GIF, JPEG, etc...).';
COMMENT ON COLUMN "Formats"."name" IS
    'Name of the format.';
COMMENT ON COLUMN "Formats"."mime" IS
    'Format name or MIME type.';
COMMENT ON COLUMN "Formats"."encoding" IS
    'Encoding of image data: either "geophysics" or "native".';
COMMENT ON CONSTRAINT "Formats_encoding_check" ON "Formats" IS
    'Enumeration of acceptable values.';




--------------------------------------------------------------------------------------------------
-- Creates the "SampleDimensions" table.                                                        --
-- Dependencies: Formats                                                                        --
--------------------------------------------------------------------------------------------------

CREATE TABLE "SampleDimensions" (
    "identifier" character varying NOT NULL PRIMARY KEY,
    "format"     character varying NOT NULL REFERENCES "Formats" ON UPDATE CASCADE ON DELETE CASCADE,
    "band"       smallint          NOT NULL DEFAULT 1 CHECK (band >= 1),
    "units"      character varying NOT NULL DEFAULT '',
    UNIQUE ("format", "band")
);

ALTER TABLE "SampleDimensions" OWNER TO geoadmin;
GRANT ALL ON TABLE "SampleDimensions" TO geoadmin;
GRANT SELECT ON TABLE "SampleDimensions" TO PUBLIC;

CREATE INDEX "SampleDimensions_index" ON "SampleDimensions" ("format", "band");

COMMENT ON TABLE "SampleDimensions" IS
    'Descriptions of the bands included in each image format.';
COMMENT ON COLUMN "SampleDimensions"."identifier" IS
    'Unique name identifying the band.';
COMMENT ON COLUMN "SampleDimensions"."format" IS
    'Format containing this band.';
COMMENT ON COLUMN "SampleDimensions"."band" IS
    'Band number (starting at 1).';
COMMENT ON COLUMN "SampleDimensions"."units" IS
    'Geophysical measurement units.  May be left blank if not applicable.';
COMMENT ON CONSTRAINT "SampleDimensions_format_fkey" ON "SampleDimensions" IS
    'Each band forms part of the description of the image.';
COMMENT ON CONSTRAINT "SampleDimensions_band_check" ON "SampleDimensions" IS
    'The band number must be positive.';
COMMENT ON INDEX "SampleDimensions_index" IS
    'Index of the bands in order of appearance.';




--------------------------------------------------------------------------------------------------
-- Creates the "Categories" table.                                                              --
-- Dependencies: "SampleDimensions"                                                             --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Categories" (
    "name"     character varying NOT NULL,
    "band"     character varying NOT NULL REFERENCES "SampleDimensions" ON UPDATE CASCADE ON DELETE CASCADE,
    "lower"    integer           NOT NULL,
    "upper"    integer           NOT NULL,
    "c0"       double precision,
    "c1"       double precision,
    "function" character varying,
    "colors"   character varying,
    PRIMARY KEY ("name", "band"),
    CONSTRAINT "Categories_range" CHECK ("lower" <= "upper"),
    CONSTRAINT "Categories_coefficients" CHECK
                    ((("c0" IS     NULL) AND ("c1" IS     NULL)) OR
                     (("c0" IS NOT NULL) AND ("c1" IS NOT NULL) AND ("c1" <> 0)))
);

ALTER TABLE "Categories" OWNER TO geoadmin;
GRANT ALL ON TABLE "Categories" TO geoadmin;
GRANT SELECT ON TABLE "Categories" TO PUBLIC;

CREATE INDEX "Categories_index" ON "Categories" ("band", "lower");

COMMENT ON TABLE "Categories" IS
    'Categories classify the ranges of values and scaling information for interpreting geophysical measurements from pixel values and for rendering (coloring) the image.';
COMMENT ON COLUMN "Categories"."name" IS
    'Name of the category represented by this range of values.';
COMMENT ON COLUMN "Categories"."band" IS
    'The band to which this range of values applies.';
COMMENT ON COLUMN "Categories"."lower" IS
    'Minimum pixel value (inclusive) for this category.';
COMMENT ON COLUMN "Categories"."upper" IS
    'Maximum pixel value (inclusive) for this category.';
COMMENT ON COLUMN "Categories"."c0" IS
    'Coefficient C0 of the equation y=C0+C1*x, where x is the pixel value and y is the value of the geophysical measurement.  May be left blank if not applicable.';
COMMENT ON COLUMN "Categories"."c1" IS
    'Coefficient C1 of the equation y=C0+C1*x, where x is the pixel value and y is the value of the geophysical measurement.  May be left blank if not applicable.';
COMMENT ON COLUMN "Categories"."function" IS
    'Function applied to the geophysical values.  For example, the value "log" indicates that the values are expressed in the form log(y)=C0+C1*x.';
COMMENT ON COLUMN "Categories"."colors" IS
    'This field can be either a color code or the name of a color pallet.';
COMMENT ON CONSTRAINT "Categories_band_fkey" ON "Categories" IS
    'Each category is an element of the band description.';
COMMENT ON CONSTRAINT "Categories_coefficients" ON "Categories" IS
    'Both coefficients C0 and C1 must be either null or non-null.';
COMMENT ON INDEX "Categories_index" IS
    'Index of categories belonging to a band.';




--------------------------------------------------------------------------------------------------
-- Creates the "RangeOfFormats" view.                                                        --
-- Dependencies: "Categories", "SampleDimensions", "Format"                                     --
--------------------------------------------------------------------------------------------------
CREATE VIEW "RangeOfFormats" AS
 SELECT "SampleDimensions"       ."format",
        "SampleDimensions"       ."identifier" AS "band",
        "RangeOfSampleDimensions"."fillValue",
        "RangeOfSampleDimensions"."lower",
        "RangeOfSampleDimensions"."upper",
        "RangeOfSampleDimensions"."minimum",
        "RangeOfSampleDimensions"."maximum",
        "SampleDimensions"       ."units",
        "Formats"                ."encoding"
   FROM "SampleDimensions" JOIN (
 SELECT "band", count("band") AS "numCategories",
        min("lower") AS "lower",
        max("upper") AS "upper",
        min(CASE WHEN "c1" IS NULL THEN "lower" ELSE NULL END) AS "fillValue",
        min((CASE WHEN "c1" < 0 THEN "upper" ELSE "lower" END) * "c1" + "c0") AS "minimum",
        max((CASE WHEN "c1" < 0 THEN "lower" ELSE "upper" END) * "c1" + "c0") AS "maximum"
   FROM "Categories" GROUP BY "band") AS "RangeOfSampleDimensions"
     ON "SampleDimensions"."identifier" = "RangeOfSampleDimensions"."band"
   JOIN "Formats" ON "SampleDimensions"."format" = "Formats"."name"
  ORDER BY "SampleDimensions"."format", "SampleDimensions"."band";

ALTER TABLE "RangeOfFormats" OWNER TO geoadmin;
GRANT ALL ON TABLE "RangeOfFormats" TO geoadmin;
GRANT SELECT ON TABLE "RangeOfFormats" TO PUBLIC;
COMMENT ON VIEW "RangeOfFormats" IS
    'Value range of each image format.';




--------------------------------------------------------------------------------------------------
-- Creates the "Layers" table.                                                                  --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Layers" (
    "name"      character varying NOT NULL PRIMARY KEY,
    "title"     character varying,
    "thematic"  character varying,
    "procedure" character varying,
    "period"    double precision,
    "minScale"  double precision,
    "maxScale"  double precision,
    "fallback"  character varying REFERENCES "Layers" ON UPDATE CASCADE ON DELETE RESTRICT
);

ALTER TABLE "Layers" OWNER TO geoadmin;
GRANT ALL ON TABLE "Layers" TO geoadmin;
GRANT SELECT ON TABLE "Layers" TO PUBLIC;

CREATE INDEX "Layers_index" ON "Layers" ("thematic", "procedure");

COMMENT ON TABLE "Layers" IS
    'Set of a series of images belonging to the same category.';
COMMENT ON COLUMN "Layers"."name" IS
    'Name of the layer.';
COMMENT ON COLUMN "Layers"."title" IS
    'Title of the layer.';
COMMENT ON COLUMN "Layers"."thematic" IS
    'Geophysical parameter (or category) of this layer.';
COMMENT ON COLUMN "Layers"."procedure" IS
    'Procedure applied to produce the images.';
COMMENT ON COLUMN "Layers"."period" IS
    'Number of days between images.  Can be approximate or left blank if not applicable.';
COMMENT ON COLUMN "Layers"."minScale" IS
    'Minimum scale to request this Layer in WMS. Should contain only values greater than 1.';
COMMENT ON COLUMN "Layers"."maxScale" IS
    'Maximum scale to request this Layer in WMS. Should contain only values greater than 1.';
COMMENT ON COLUMN "Layers"."fallback" IS
    'Fallback layer which is suggested if no data is available for the current layer.';
COMMENT ON CONSTRAINT "Layers_fallback_fkey" ON "Layers" IS
    'Each fallback layer must exist.';
COMMENT ON INDEX "Layers_index" IS
    'Index of layers belonging to a category.';




--------------------------------------------------------------------------------------------------
-- Creates the "Series" table.                                                                  --
-- Dependencies: "Layers", "Formats"                                                            --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Series" (
    "identifier" character varying NOT NULL PRIMARY KEY,
    "layer"      character varying NOT NULL REFERENCES "Layers"  ON UPDATE CASCADE ON DELETE CASCADE,
    "pathname"   character varying NOT NULL,
    "extension"  character varying, -- Accepts NULL since some files has no extension
    "format"     character varying NOT NULL REFERENCES "Formats" ON UPDATE CASCADE ON DELETE RESTRICT,
    "quicklook"  character varying UNIQUE   REFERENCES "Series"  ON UPDATE CASCADE ON DELETE RESTRICT,
    "permission" character varying NOT NULL DEFAULT 'Public'
);

ALTER TABLE "Series" OWNER TO geoadmin;
GRANT ALL ON TABLE "Series" TO geoadmin;
GRANT SELECT ON TABLE "Series" TO PUBLIC;

CREATE INDEX "Series_index" ON "Series" ("layer", "permission");

COMMENT ON TABLE "Series" IS
    'Series of images.  Each image belongs to a series.';
COMMENT ON COLUMN "Series"."identifier" IS
    'Unique identifier.';
COMMENT ON COLUMN "Series"."layer" IS
    'The layer to which the images in the series belong.';
COMMENT ON COLUMN "Series"."pathname" IS
    'Relative path to the files in the group.  The root path should not be specified if it varies from platform to platform.';
COMMENT ON COLUMN "Series"."extension" IS
    'File extention of the images in the series.';
COMMENT ON COLUMN "Series"."format" IS
    'Format of the images in the series.';
COMMENT ON COLUMN "Series"."quicklook" IS
    'Series of overview images.';
COMMENT ON COLUMN "Series"."permission" IS
    'Permissions of images in the series (public, restricted, etc.)';
COMMENT ON CONSTRAINT "Series_quicklook_key" ON "Series" IS
    'Each series has only one overview series.';
COMMENT ON CONSTRAINT "Series_layer_fkey" ON "Series" IS
    'Each series belongs to a layer.';
COMMENT ON CONSTRAINT "Series_format_fkey" ON "Series" IS
    'All the images of a series use the same series.';
COMMENT ON CONSTRAINT "Series_quicklook_fkey" ON "Series" IS
    'The overviews apply to another series of images.';
COMMENT ON INDEX "Series_index" IS
    'Index of series belonging to a layer.';




--------------------------------------------------------------------------------------------------
-- Creates the "GridGeometries" table.                                                          --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "GridGeometries" (
    "identifier"        character varying NOT NULL PRIMARY KEY,
    "width"             integer           NOT NULL,
    "height"            integer           NOT NULL,
    "translateX"        double precision  NOT NULL DEFAULT 0,
    "translateY"        double precision  NOT NULL DEFAULT 0,
    "scaleX"            double precision  NOT NULL DEFAULT 1,
    "scaleY"            double precision  NOT NULL DEFAULT 1,
    "shearX"            double precision  NOT NULL DEFAULT 0,
    "shearY"            double precision  NOT NULL DEFAULT 0,
    "horizontalSRID"    integer           NOT NULL DEFAULT 4326,
    CONSTRAINT "GridGeometries_size" CHECK (width > 0 AND height > 0)
);

SELECT AddGeometryColumn('GridGeometries', 'horizontalExtent', 4326, 'POLYGON', 2);
ALTER TABLE "GridGeometries" ALTER COLUMN "horizontalExtent" SET NOT NULL;
ALTER TABLE "GridGeometries" ADD COLUMN "verticalSRID" integer;
ALTER TABLE "GridGeometries" ADD COLUMN "verticalOrdinates" double precision[];
ALTER TABLE "GridGeometries"
  ADD CONSTRAINT "enforce_srid_verticalOrdinates" CHECK
            (((("verticalSRID" IS     NULL) AND ("verticalOrdinates" IS     NULL)) OR
              (("verticalSRID" IS NOT NULL) AND ("verticalOrdinates" IS NOT NULL))));

ALTER TABLE "GridGeometries" OWNER TO geoadmin;
GRANT ALL ON TABLE "GridGeometries" TO geoadmin;
GRANT SELECT ON TABLE "GridGeometries" TO PUBLIC;

ALTER TABLE ONLY "GridGeometries"
    ADD CONSTRAINT "fk_SRID" FOREIGN KEY ("horizontalSRID") REFERENCES spatial_ref_sys(srid)
    ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE ONLY "GridGeometries"
    ADD CONSTRAINT "fk_VERT_SRID" FOREIGN KEY ("verticalSRID") REFERENCES spatial_ref_sys(srid)
    ON UPDATE RESTRICT ON DELETE RESTRICT;

CREATE INDEX "HorizontalExtent_index" ON "GridGeometries" USING gist ("horizontalExtent" gist_geometry_ops);
COMMENT ON INDEX "HorizontalExtent_index" IS
    'Index of geometries intersecting a geographical area.';

COMMENT ON TABLE "GridGeometries" IS
    'Spatial referencing parameters for a Grid Coverage.  Defines the spatial envelope of the images, as well as their grid dimensions.';
COMMENT ON COLUMN "GridGeometries"."identifier" IS
    'Unique identifier.';
COMMENT ON COLUMN "GridGeometries"."width" IS
    'Number of pixels wide.';
COMMENT ON COLUMN "GridGeometries"."height" IS
    'Number of pixels high.';
COMMENT ON COLUMN "GridGeometries"."translateX" IS
    'Element (0,2) of the affine transform.  Usually corresponds to the x-coordinate of the top left corner.';
COMMENT ON COLUMN "GridGeometries"."translateY" IS
    'Element (1,2) of the affine transform.  Usually corresponds to the y-coordinate of the top left corner.';
COMMENT ON COLUMN "GridGeometries"."scaleX" IS
    'Element (0,0) of the affine transform.  Usually corresponds to the x size of the pixels.';
COMMENT ON COLUMN "GridGeometries"."scaleY" IS
    'Element (1,1) of the affine transform.  Usually corresponds to the y size of the pixels.  This value is often negative since the numbering of the lines of an image increases downwards.';
COMMENT ON COLUMN "GridGeometries"."shearX" IS
    'Element (0,1) of the affine transform.  Always 0 if there is no rotation.';
COMMENT ON COLUMN "GridGeometries"."shearY" IS
    'Element (1,0) of the affine transform.  Always 0 if there is no rotation.';
COMMENT ON COLUMN "GridGeometries"."horizontalSRID" IS
    'Horizontal coordinate system code.';
COMMENT ON COLUMN "GridGeometries"."horizontalExtent" IS
    'Horizontal spatial extent. (Computed automatically if none is explicitly defined).';
COMMENT ON COLUMN "GridGeometries"."verticalSRID" IS
    'Vertical coordinate system code.';
COMMENT ON COLUMN "GridGeometries"."verticalOrdinates" IS
    'Z values of each of the layers of a 3D image.';
COMMENT ON CONSTRAINT "GridGeometries_size" ON "GridGeometries" IS
    'The dimensions of the images must be positive.';
COMMENT ON CONSTRAINT "enforce_dims_horizontalExtent" ON "GridGeometries" IS
    'Verify that the horizontal extent has two dimensions.';
COMMENT ON CONSTRAINT "enforce_geotype_horizontalExtent" ON "GridGeometries" IS
    'Verify that the horizontal extent is a polygon.';
COMMENT ON CONSTRAINT "enforce_srid_horizontalExtent" ON "GridGeometries" IS
    'Verify that the horizontal extent is expressed according to the expected CRS.';
COMMENT ON CONSTRAINT "enforce_srid_verticalOrdinates" ON "GridGeometries" IS
    'The vertical coordinates and their SRID must both either be null or non-null.';




--------------------------------------------------------------------------------------------------
-- Function to be applied on new records in the "GridGeometries" table.                         --
--------------------------------------------------------------------------------------------------

CREATE FUNCTION "ComputeDefaultExtent"() RETURNS "trigger"
    AS $$
  BEGIN
    IF NEW."horizontalExtent" IS NULL THEN
      NEW."horizontalExtent" := Transform(Affine(GeometryFromText(
        'POLYGON((0 0,0 ' || NEW."height" || ',' || NEW."width" || ' ' || NEW."height" || ',' || NEW."width" || ' 0,0 0))',
        NEW."horizontalSRID"), NEW."scaleX", NEW."shearX", NEW."shearY", NEW."scaleY", NEW."translateX", NEW."translateY"), 4326);
    END IF;
    RETURN NEW;
  END;
$$
    LANGUAGE plpgsql;

ALTER FUNCTION "ComputeDefaultExtent"() OWNER TO geoadmin;
GRANT ALL ON FUNCTION "ComputeDefaultExtent"() TO geoadmin;
GRANT EXECUTE ON FUNCTION "ComputeDefaultExtent"() TO PUBLIC;

CREATE TRIGGER "addDefaultExtent"
    BEFORE INSERT OR UPDATE ON "GridGeometries"
    FOR EACH ROW
    EXECUTE PROCEDURE "ComputeDefaultExtent"();

COMMENT ON TRIGGER "addDefaultExtent" ON "GridGeometries" IS
    'Add an envelope by default if none is explicitly defined.';




--------------------------------------------------------------------------------------------------
-- Creates the "BoundingBoxes" view.                                                            --
-- Dependencies:  "GridGeometries"                                                              --
-- Inner queries: "Corners" contains 4 (x,y) corners as 4 rows for each grid geometries.        --
--                "NativeBoxes" contains the minimum and maximum values of "Corners".           --
--------------------------------------------------------------------------------------------------

CREATE VIEW "BoundingBoxes" AS
    SELECT "GridGeometries"."identifier", "width", "height",
           "horizontalSRID" AS "crs", "minX", "maxX", "minY", "maxY",
           xmin("horizontalExtent") AS "west",
           xmax("horizontalExtent") AS "east",
           ymin("horizontalExtent") AS "south",
           ymax("horizontalExtent") AS "north"
      FROM "GridGeometries"
 LEFT JOIN (SELECT "identifier",
       min("x") AS "minX",
       max("x") AS "maxX",
       min("y") AS "minY",
       max("y") AS "maxY"
FROM (SELECT "identifier",
             "translateX" AS "x",
             "translateY" AS "y" FROM "GridGeometries"
UNION SELECT "identifier",
             "width"*"scaleX" + "translateX" AS "x",
             "width"*"shearY" + "translateY" AS "y" FROM "GridGeometries"
UNION SELECT "identifier",
             "height"*"shearX" + "translateX" AS "x",
             "height"*"scaleY" + "translateY" AS "y" FROM "GridGeometries"
UNION SELECT "identifier",
             "width"*"scaleX" + "height"*"shearX" + "translateX" AS "x",
             "width"*"shearY" + "height"*"scaleY" + "translateY" AS "y" FROM "GridGeometries") AS "Corners"
    GROUP BY "identifier") AS "NativeBoxes"
          ON "GridGeometries"."identifier" = "NativeBoxes"."identifier"
    ORDER BY "identifier";

ALTER TABLE "BoundingBoxes" OWNER TO geoadmin;
GRANT ALL ON TABLE "BoundingBoxes" TO geoadmin;
GRANT SELECT ON TABLE "BoundingBoxes" TO PUBLIC;

COMMENT ON VIEW "BoundingBoxes" IS
    'Comparison between the calculated envelopes and the declared envelopes.';




--------------------------------------------------------------------------------------------------
-- Creates the "GridCoverages" table.                                                           --
-- Dependencies: "Series", "GridGeometries"                                                     --
--------------------------------------------------------------------------------------------------

CREATE TABLE "GridCoverages" (
    "series"    character varying NOT NULL REFERENCES "Series" ON UPDATE CASCADE ON DELETE CASCADE,
    "filename"  character varying NOT NULL,
    "index"     smallint          NOT NULL DEFAULT 1 CHECK ("index" >= 1),
    "startTime" timestamp without time zone,
    "endTime"   timestamp without time zone,
    "extent"    character varying NOT NULL REFERENCES "GridGeometries" ON UPDATE CASCADE ON DELETE RESTRICT,
    PRIMARY KEY ("series", "filename", "index"),
    UNIQUE ("series", "endTime", "startTime"),  -- Same order than the index.
    CHECK ((("startTime" IS     NULL) AND ("endTime" IS     NULL)) OR
           (("startTime" IS NOT NULL) AND ("endTime" IS NOT NULL) AND ("startTime" <= "endTime")))
);

ALTER TABLE "GridCoverages" OWNER TO geoadmin;
GRANT ALL ON TABLE "GridCoverages" TO geoadmin;
GRANT SELECT ON TABLE "GridCoverages" TO PUBLIC;

-- Index "endTime" before "startTime" because we most frequently sort by
-- end time, since we are often interrested in the latest image available.
CREATE INDEX "GridCoverages_index"        ON "GridCoverages" ("series", "endTime", "startTime");
CREATE INDEX "GridCoverages_extent_index" ON "GridCoverages" ("series", "extent");

COMMENT ON TABLE "GridCoverages" IS
    'List of all the images available.  Each listing corresponds to an image file.';
COMMENT ON COLUMN "GridCoverages"."series" IS
    'Series to which the image belongs.';
COMMENT ON COLUMN "GridCoverages"."filename" IS
    'File name of the image.';
COMMENT ON COLUMN "GridCoverages"."index" IS
    'Index of the image in the file (for files containing multipal images).  Numbered from 1.';
COMMENT ON COLUMN "GridCoverages"."startTime" IS
    'Date and time of the image acquisition start, in UTC.  In the case of averages, the time corresponds to the beginning of the interval used to calculate the average.';
COMMENT ON COLUMN "GridCoverages"."endTime" IS
    'Date and time of the image acquisition end, in UTC.  This time must be greater than or equal to the acquisition start time.';
COMMENT ON COLUMN "GridCoverages"."extent" IS
    'Grid Geomerty ID that defines the spatial footprint of this coverage.';
COMMENT ON CONSTRAINT "GridCoverages_series_key" ON "GridCoverages" IS
    'The time range of the image must be unique in each series.';
COMMENT ON CONSTRAINT "GridCoverages_series_fkey" ON "GridCoverages" IS
    'Each image belongs to a series.';
COMMENT ON CONSTRAINT "GridCoverages_extent_fkey" ON "GridCoverages" IS
    'Each image must have a spatial extent.';
COMMENT ON CONSTRAINT "GridCoverages_check" ON "GridCoverages" IS
    'The start and end times must be both null or both non-null, and the end time must be greater than or equal to the start time.';
COMMENT ON CONSTRAINT "GridCoverages_index_check" ON "GridCoverages" IS
    'The image index must be positive.';
COMMENT ON INDEX "GridCoverages_index" IS
    'Index of all the images within a certain time range.';
COMMENT ON INDEX "GridCoverages_extent_index" IS
    'Index of all the images in a geographic region.';




--------------------------------------------------------------------------------------------------
-- Creates the "Tiles" table.                                                                   --
-- Dependencies: "Series", "GridGeometries", "GridCoverages"                                    --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Tiles" (
  "dx" INTEGER NOT NULL DEFAULT 0,
  "dy" INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY ("series", "filename", "index"),
  FOREIGN KEY ("extent") REFERENCES "GridGeometries" ON UPDATE CASCADE ON DELETE RESTRICT,
  FOREIGN KEY ("series") REFERENCES "Series" ON UPDATE CASCADE ON DELETE CASCADE
) INHERITS ("GridCoverages");

ALTER TABLE "Tiles" OWNER TO geoadmin;
GRANT ALL ON TABLE "Tiles" TO geoadmin;
GRANT SELECT ON TABLE "Tiles" TO PUBLIC;

CREATE INDEX "Tiles_index" ON "Tiles" ("series", "endTime", "startTime");

COMMENT ON COLUMN "Tiles"."dx" IS 'Amount of pixels to translate along the x axis before to apply the affine transform.';
COMMENT ON COLUMN "Tiles"."dy" IS 'Amount of pixels to translate along the y axis before to apply the affine transform.';



--------------------------------------------------------------------------------------------------
-- Creates the "Tiling" view.                                                                   --
-- Dependencies: "Tiles", "GridGeometries", "Series"                                            --
--------------------------------------------------------------------------------------------------

CREATE VIEW "Tiling" AS
 SELECT "series",
        "startTime",
        "endTime",
        count("filename") AS "numTiles",
        max("width") = min("width") AND max("height") = min("height") AS "uniformSize",
        min("width") AS "width",
        min("height") AS "height",
        sqrt("scaleX" * "scaleX" + "shearX" * "shearX") AS "scaleX",
        sqrt("scaleY" * "scaleY" + "shearY" * "shearY") AS "scaleY",
        "horizontalSRID"
   FROM "Tiles"
   JOIN "GridGeometries" ON "Tiles"."extent" = "GridGeometries".identifier
  GROUP BY "series", "endTime", "startTime", "horizontalSRID", "scaleX", "scaleY", "shearX", "shearY"
  ORDER BY "series", "endTime", "scaleX"*"scaleX" + "shearX"*"shearX" + "scaleY"*"scaleY" + "shearY"*"shearY" DESC;

ALTER TABLE "Tiling" OWNER TO geoadmin;
GRANT ALL ON TABLE "Tiling" TO geoadmin;
GRANT SELECT ON TABLE "Tiling" TO PUBLIC;

COMMENT ON VIEW "Tiling" IS
    'Summary of tiling by series inferred from the tiles table content.';




--------------------------------------------------------------------------------------------------
-- Creates the "DomainOfSeries" view.                                                           --
-- Dependencies: "GridCoverages", "BoundingBoxes"                                               --
--------------------------------------------------------------------------------------------------

CREATE VIEW "DomainOfSeries" AS
    SELECT "TimeRanges"."series", "count", "startTime", "endTime",
           "west", "east", "south", "north", "xResolution", "yResolution"
      FROM
   (SELECT "series",
           count("extent")  AS "count",
           min("startTime") AS "startTime",
           max("endTime")   AS "endTime"
      FROM ONLY "GridCoverages" GROUP BY "series") AS "TimeRanges"
      JOIN
   (SELECT "series",
           min("west")  AS "west",
           max("east")  AS "east",
           min("south") AS "south",
           max("north") AS "north",
           min(("east"  - "west" ) / "width" ) AS "xResolution",
           min(("north" - "south") / "height") AS "yResolution"
      FROM (SELECT DISTINCT "series", "extent" FROM ONLY "GridCoverages") AS "Extents"
 LEFT JOIN "BoundingBoxes" ON "Extents"."extent" = "BoundingBoxes"."identifier"
  GROUP BY "series") AS "BoundingBoxRanges" ON "TimeRanges".series = "BoundingBoxRanges".series
  ORDER BY "series";

ALTER TABLE "DomainOfSeries" OWNER TO geoadmin;
GRANT ALL ON TABLE "DomainOfSeries" TO geoadmin;
GRANT SELECT ON TABLE "DomainOfSeries" TO PUBLIC;

COMMENT ON VIEW "DomainOfSeries" IS
    'List of geographical areas used by each sub-series.';




--------------------------------------------------------------------------------------------------
-- Creates the "DomainOfLayers" view.                                                           --
-- Dependencies: "DomainOfSeries", "Series"                                                     --
--------------------------------------------------------------------------------------------------

CREATE VIEW "DomainOfLayers" AS
 SELECT "layer",
        sum("count")       AS "count",
        min("startTime")   AS "startTime",
        max("endTime")     AS "endTime",
        min("west")        AS "west",
        max("east")        AS "east",
        min("south")       AS "south",
        max("north")       AS "north",
        min("xResolution") AS "xResolution",
        min("yResolution") AS "yResolution"
   FROM "DomainOfSeries"
   JOIN "Series" ON "DomainOfSeries"."series" = "Series"."identifier"
  GROUP BY "layer"
  ORDER BY "layer";

ALTER TABLE "DomainOfLayers" OWNER TO geoadmin;
GRANT ALL ON TABLE "DomainOfLayers" TO geoadmin;
GRANT SELECT ON TABLE "DomainOfLayers" TO PUBLIC;

COMMENT ON VIEW "DomainOfLayers" IS
    'Number of images and geographical area for each layer used.';
