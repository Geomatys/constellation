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
    'Formats des images (PNG, GIF, JPEG, etc...).';
COMMENT ON COLUMN "Formats"."name" IS
    'Nom identifiant le format.';
COMMENT ON COLUMN "Formats"."mime" IS
    'Nom MIME du format.';
COMMENT ON COLUMN "Formats"."encoding" IS
    'Encodage des données de l''image: "geophysics" ou "native".';
COMMENT ON CONSTRAINT "Formats_encoding_check" ON "Formats" IS
    'Énumération des valeurs acceptables.';




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
    'Descriptions des bandes comprises dans chaque format d''images.';
COMMENT ON COLUMN "SampleDimensions"."identifier" IS
    'Nom unique identifiant la bande.';
COMMENT ON COLUMN "SampleDimensions"."format" IS
    'Format qui contient cette bande.';
COMMENT ON COLUMN "SampleDimensions"."band" IS
    'Numéro de la bande (à partir de 1).';
COMMENT ON COLUMN "SampleDimensions"."units" IS
    'Unités des mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';
COMMENT ON CONSTRAINT "SampleDimensions_format_fkey" ON "SampleDimensions" IS
    'Chaque bande fait partie de la description d''une image.';
COMMENT ON CONSTRAINT "SampleDimensions_band_check" ON "SampleDimensions" IS
    'Le numéro de bande doit être positif.';
COMMENT ON INDEX "SampleDimensions_index" IS
    'Classement des bandes dans leur ordre d''apparition.';




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
    "colors"   character varying NOT NULL DEFAULT '#000000',
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
    'Plage de valeurs des différents thèmes et relation entre les valeurs des pixels et leurs mesures géophysiques.';
COMMENT ON COLUMN "Categories"."name" IS
    'Nom du thème représenté par cette plage de valeurs.';
COMMENT ON COLUMN "Categories"."band" IS
    'Bande auquel s''applique cette plage de valeurs.';
COMMENT ON COLUMN "Categories"."lower" IS
    'Valeur minimale (inclusive) des pixels de ce thème.';
COMMENT ON COLUMN "Categories"."upper" IS
    'Valeur maximale (inclusive) des pixels de ce thème.';
COMMENT ON COLUMN "Categories"."c0" IS
    'Coefficient C0 de l''équation y=C0+C1*x, ou x est la valeur du pixel et y la valeur en mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';
COMMENT ON COLUMN "Categories"."c1" IS
    'Coefficient C1 de l''équation y=C0+C1*x, ou x est la valeur du pixel et y la valeur en mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';
COMMENT ON COLUMN "Categories"."function" IS
    'Fonction appliquée sur les valeurs géophysiques. Par exemple la valeur "log" indique que les valeurs sont exprimées sous la forme log(y)=C0+C1*x.';
COMMENT ON COLUMN "Categories"."colors" IS
    'Ce champ peut être soit un code d''une couleur, ou soit une adresse URL vers une palette de couleurs.';
COMMENT ON CONSTRAINT "Categories_band_fkey" ON "Categories" IS
    'Chaque catégorie est un élément de la description d''une bande.';
COMMENT ON CONSTRAINT "Categories_coefficients" ON "Categories" IS
    'Les coefficients C0 et C1 doivent être nuls ou non-nuls en même temps.';
COMMENT ON INDEX "Categories_index" IS
    'Recherche des catégories appartenant à une bande.';




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
    'Plage des valeurs de chaque format d''images.';




--------------------------------------------------------------------------------------------------
-- Creates the "Layers" table.                                                                  --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Layers" (
    "name"      character varying NOT NULL PRIMARY KEY,
    "thematic"  character varying,
    "procedure" character varying,
    "period"    double precision,
    "fallback"  character varying REFERENCES "Layers" ON UPDATE CASCADE ON DELETE RESTRICT
);

ALTER TABLE "Layers" OWNER TO geoadmin;
GRANT ALL ON TABLE "Layers" TO geoadmin;
GRANT SELECT ON TABLE "Layers" TO PUBLIC;

CREATE INDEX "Layers_index" ON "Layers" ("thematic", "procedure");

COMMENT ON TABLE "Layers" IS
    'Ensemble de séries d''images appartenant à une même thématique.';
COMMENT ON COLUMN "Layers"."name" IS
    'Nom identifiant la couche.';
COMMENT ON COLUMN "Layers"."thematic" IS
    'Paramètre géophysique (ou thème) de cette couche.';
COMMENT ON COLUMN "Layers"."procedure" IS
    'Procédure appliquée pour produire les images.';
COMMENT ON COLUMN "Layers"."period" IS
    'Nombre de jours prévus entre deux image. Cette information peut être approximative ou laissée blanc si elle ne s''applique pas.';
COMMENT ON COLUMN "Layers"."fallback" IS
    'Couche de rechange proposée si aucune donnée n''est disponible pour la couche courante.';
COMMENT ON CONSTRAINT "Layers_fallback_fkey" ON "Layers" IS
    'Chaque couche de second recours doit exister.';
COMMENT ON INDEX "Layers_index" IS
    'Recherche des couches appartenant à une thématique.';




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
    "visible"    boolean           NOT NULL DEFAULT true,
    "quicklook"  character varying UNIQUE   REFERENCES "Series"  ON UPDATE CASCADE ON DELETE RESTRICT
);

ALTER TABLE "Series" OWNER TO geoadmin;
GRANT ALL ON TABLE "Series" TO geoadmin;
GRANT SELECT ON TABLE "Series" TO PUBLIC;

CREATE INDEX "Series_index" ON "Series" ("layer", "visible");

COMMENT ON TABLE "Series" IS
    'Séries d''images. Chaque images appartient à une série.';
COMMENT ON COLUMN "Series"."identifier" IS
    'Identifiant unique de la séries.';
COMMENT ON COLUMN "Series"."layer" IS
    'Couche à laquelle appartiennent les images de cette série.';
COMMENT ON COLUMN "Series"."pathname" IS
    'Chemins relatifs des fichiers du groupe. La racine à ces chemins ne doit pas être spécifiée si elle peut varier d''une plateforme à l''autre.';
COMMENT ON COLUMN "Series"."extension" IS
    'Extension des fichiers d''images de cette série.';
COMMENT ON COLUMN "Series"."format" IS
    'Format des images de ce groupe.';
COMMENT ON COLUMN "Series"."visible" IS
    'Indique si les images de ce groupe doivent apparaître dans la liste des images proposées à l''utilisateur.';
COMMENT ON COLUMN "Series"."quicklook" IS
    'Série dont les images sont des aperçus de cette série.';
COMMENT ON CONSTRAINT "Series_quicklook_key" ON "Series" IS
    'Chaque série a une seule autre série d''aperçus.';
COMMENT ON CONSTRAINT "Series_layer_fkey" ON "Series" IS
    'Chaque série appartient à une couche.';
COMMENT ON CONSTRAINT "Series_format_fkey" ON "Series" IS
    'Toutes les images d''une même série utilisent un même séries.';
COMMENT ON CONSTRAINT "Series_quicklook_fkey" ON "Series" IS
    'Les aperçus s''appliquent à une autre séries d''images.';
COMMENT ON INDEX "Series_index" IS
    'Recherche des séries appartenant à une couche.';




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
    'Recherche des geométries interceptant une région géographique.';

COMMENT ON TABLE "GridGeometries" IS
    'Envelope spatiales des images ainsi que la dimension de leurs grilles. La transformation affine doit représenter le coin supérieur gauche des pixels.';
COMMENT ON COLUMN "GridGeometries"."identifier" IS
    'Identifiant unique.';
COMMENT ON COLUMN "GridGeometries"."width" IS
    'Nombre de pixels en largeur dans l''image.';
COMMENT ON COLUMN "GridGeometries"."height" IS
    'Nombre de pixels en hauteur dans l''image.';
COMMENT ON COLUMN "GridGeometries"."translateX" IS
    'Élement (0,2) de la transformation affine. Il correspond habituellement à la coordonnées x du coin supérieur gauche.';
COMMENT ON COLUMN "GridGeometries"."translateY" IS
    'Élement (1,2) de la transformation affine. Il correspond habituellement à la coordonnées y du coin supérieur gauche.';
COMMENT ON COLUMN "GridGeometries"."scaleX" IS
    'Élement (0,0) de la transformation affine. Il correspond habituellement à la taille selon x des pixels.';
COMMENT ON COLUMN "GridGeometries"."scaleY" IS
    'Élement (1,1) de la transformation affine. Il correspond habituellement à la taille selon y des pixels. Cette valeur est souvent négative puisque la numérotation des lignes d''une image augmente vers le bas.';
COMMENT ON COLUMN "GridGeometries"."shearX" IS
    'Élement (0,1) de la transformation affine. Toujours à 0 s''il n''y a pas de rotation.';
COMMENT ON COLUMN "GridGeometries"."shearY" IS
    'Élement (1,0) de la transformation affine. Toujours à 0 s''il n''y a pas de rotation.';
COMMENT ON COLUMN "GridGeometries"."horizontalSRID" IS
    'Code du système de référence des coordonnées horizontales.';
COMMENT ON COLUMN "GridGeometries"."horizontalExtent" IS
    'Étendue spatiale à l''horizontal.';
COMMENT ON COLUMN "GridGeometries"."verticalSRID" IS
    'Code du système de référence des coordonnées verticales.';
COMMENT ON COLUMN "GridGeometries"."verticalOrdinates" IS
    'Valeurs z de chacunes des couches d''une image 3D.';
COMMENT ON CONSTRAINT "GridGeometries_size" ON "GridGeometries" IS
    'Les dimensions des images doivent être positives.';
COMMENT ON CONSTRAINT "enforce_dims_horizontalExtent" ON "GridGeometries" IS
    'Vérifie que l''étendue horizontale est à deux dimensions.';
COMMENT ON CONSTRAINT "enforce_geotype_horizontalExtent" ON "GridGeometries" IS
    'Vérifie que l''étendue horizontale est un polygone.';
COMMENT ON CONSTRAINT "enforce_srid_horizontalExtent" ON "GridGeometries" IS
    'Vérifie que l''étendue horizontale est exprimée selon le CRS attendu.';
COMMENT ON CONSTRAINT "enforce_srid_verticalOrdinates" ON "GridGeometries" IS
    'Les coordonnées verticales et leur SRID doivent être nul ou non-nul en même temps.';




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
    'Ajoute une enveloppe par défaut si aucune n''était définie explicitement.';




--------------------------------------------------------------------------------------------------
-- Creates the "BoundingBoxes" view.                                                            --
-- Dependencies: "GridGeometries"                                                               --
--------------------------------------------------------------------------------------------------

CREATE VIEW "BoundingBoxes" AS
    SELECT "identifier",
           "width",
           "height",
           "crs",
           xmin("nativeBox")     AS "minX",
           xmax("nativeBox")     AS "maxX",
           ymin("nativeBox")     AS "minY",
           ymax("nativeBox")     AS "maxY",
           xmin("geographicBox") AS "west",
           xmax("geographicBox") AS "east",
           ymin("geographicBox") AS "south",
           ymax("geographicBox") AS "north"
      FROM
   (SELECT "identifier",
           "width",
           "height",
           srid("TransformedGeometries".envelope) AS crs,
           box2d("TransformedGeometries".envelope) AS "nativeBox",
           box2d(transform("TransformedGeometries".envelope, 4326)) AS "geographicBox"
      FROM
   (SELECT "identifier",
           "width",
           "height",
           affine(geometryfromtext('POLYGON((0 0,0 ' || "height" || ',' || "width" || ' ' || "height" || ',' || "width" || ' 0,0 0))', "horizontalSRID"),
           "scaleX", "shearX", "shearY", "scaleY", "translateX", "translateY") AS envelope
      FROM "GridGeometries") AS "TransformedGeometries") AS "GeometryDetails";

ALTER TABLE "BoundingBoxes" OWNER TO geoadmin;
GRANT ALL ON TABLE "BoundingBoxes" TO geoadmin;
GRANT SELECT ON TABLE "BoundingBoxes" TO PUBLIC;

COMMENT ON VIEW "BoundingBoxes" IS
    'Comparaison entre les enveloppes calculées et les enveloppes déclarées.';




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
    UNIQUE ("series", "startTime", "endTime", "extent"),
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
    'Liste de toutes les images disponibles. Chaque enregistrement correspond à un fichier d''image.';
COMMENT ON COLUMN "GridCoverages"."series" IS
    'Série à laquelle appartient l''image.';
COMMENT ON COLUMN "GridCoverages"."filename" IS
    'Nom du fichier contenant l''image.';
COMMENT ON COLUMN "GridCoverages"."index" IS
    'Index de l''image dans les fichiers contenant plusieurs images. Numérotées à partir de 1.';
COMMENT ON COLUMN "GridCoverages"."startTime" IS
    'Date et heure du début de l''acquisition de l''image, en heure universelle (UTC). Dans le cas des moyennes, cette date correspond au début de l''intervalle de temps ayant servit à établir la moyenne.';
COMMENT ON COLUMN "GridCoverages"."endTime" IS
    'Date et heure de la fin de l''acquisition de l''image, en heure universelle (UTC). Cette date doit être supérieure à la date de début d''acquisition; une valeur égale ne suffit pas.';
COMMENT ON COLUMN "GridCoverages"."extent" IS
    'Coordonnées de la région géographique couverte par l''image, ainsi que sa résolution approximative. ';
COMMENT ON CONSTRAINT "GridCoverages_series_key" ON "GridCoverages" IS
    'L''envelope de l''image doit être unique dans chaque série.';
COMMENT ON CONSTRAINT "GridCoverages_series_fkey" ON "GridCoverages" IS
    'Chaque image appartient à une série.';
COMMENT ON CONSTRAINT "GridCoverages_extent_fkey" ON "GridCoverages" IS
    'Chaque images doit avoir une étendue spatiale.';
COMMENT ON CONSTRAINT "GridCoverages_check" ON "GridCoverages" IS
    'Les dates de début et de fin doivent être nulles ou non-nulles en même temps, et la date de début doit être inférieure ou égale à la date de fin.';
COMMENT ON CONSTRAINT "GridCoverages_index_check" ON "GridCoverages" IS
    'L''index de l''image doit être strictement positif.';
COMMENT ON INDEX "GridCoverages_index" IS
    'Recherche de toutes les images à l''intérieur d''une certaine plage de temps.';
COMMENT ON INDEX "GridCoverages_extent_index" IS
    'Recherche de toutes les images dans une région géographique.';




--------------------------------------------------------------------------------------------------
-- Creates the "RangeOfSeries" view.                                                            --
-- Dependencies: "GridCoverages", "BoundingBoxes"                                               --
--------------------------------------------------------------------------------------------------

CREATE VIEW "RangeOfSeries" AS
    SELECT "TimeRanges"."series", "count", "startTime", "endTime",
           "west", "east", "south", "north", "XResolution", "YResolution"
      FROM
   (SELECT "series",
           count("extent")  AS "count",
           min("startTime") AS "startTime",
           max("endTime")   AS "endTime"
      FROM "GridCoverages" GROUP BY "series") AS "TimeRanges"
      JOIN
   (SELECT "series",
           min("west")  AS "west",
           max("east")  AS "east",
           min("south") AS "south",
           max("north") AS "north",
           avg(("east"  - "west" ) / "width" ) AS "XResolution",
           avg(("north" - "south") / "height") AS "YResolution"
      FROM (SELECT DISTINCT "series", "extent" FROM "GridCoverages") AS "Extents"
 LEFT JOIN "BoundingBoxes" ON "Extents"."extent" = "BoundingBoxes"."identifier"
  GROUP BY "series") AS "BoundingBoxRanges" ON "TimeRanges".series = "BoundingBoxRanges".series
  ORDER BY "series";

ALTER TABLE "RangeOfSeries" OWNER TO geoadmin;
GRANT ALL ON TABLE "RangeOfSeries" TO geoadmin;
GRANT SELECT ON TABLE "RangeOfSeries" TO PUBLIC;

COMMENT ON VIEW "RangeOfSeries" IS
    'Liste des régions géographiques utilisées par chaque sous-série.';




--------------------------------------------------------------------------------------------------
-- Creates the "RangeOfLayers" view.                                                            --
-- Dependencies: "RangeOfSeries", "Series"                                                      --
--------------------------------------------------------------------------------------------------

CREATE VIEW "RangeOfLayers" AS
 SELECT "layer",
        sum("count")     AS "count",
        min("startTime") AS "startTime",
        max("endTime")   AS "endTime",
        min("west")      AS "west",
        max("east")      AS "east",
        min("south")     AS "south",
        max("north")     AS "north",
        sum("XResolution" * "count") / sum("count") AS "XResolution",
        sum("YResolution" * "count") / sum("count") AS "YResolution"
   FROM "RangeOfSeries"
   JOIN "Series" ON "RangeOfSeries"."series" = "Series"."identifier"
  GROUP BY "layer"
  ORDER BY "layer";

ALTER TABLE "RangeOfLayers" OWNER TO geoadmin;
GRANT ALL ON TABLE "RangeOfLayers" TO geoadmin;
GRANT SELECT ON TABLE "RangeOfLayers" TO PUBLIC;

COMMENT ON VIEW "RangeOfLayers" IS
    'Nombre d''images et région géographique pour chacune des couches utilisées.';
