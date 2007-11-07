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

SET search_path = postgrid, pg_catalog;




--------------------------------------------------------------------------------------------------
-- Creates the "Formats" table.                                                                 --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Formats" (
    name character varying NOT NULL,
    mime character varying NOT NULL,
    "encoding" character varying(10) DEFAULT 'native'::character varying NOT NULL,
    CONSTRAINT "Format_type" CHECK (((("encoding")::text = 'geophysics'::text) OR (("encoding")::text = 'native'::text)))
);

ALTER TABLE "Formats" OWNER TO geoadmin;
GRANT ALL ON TABLE "Formats" TO geoadmin;
GRANT SELECT ON TABLE "Formats" TO PUBLIC;

ALTER TABLE ONLY "Formats"
    ADD CONSTRAINT "Formats_pkey" PRIMARY KEY (name);

COMMENT ON TABLE "Formats" IS
    'Formats des images (PNG, GIF, JPEG, etc...).';
COMMENT ON COLUMN "Formats".name IS
    'Nom identifiant le format.';
COMMENT ON COLUMN "Formats".mime IS
    'Nom MIME du format.';
COMMENT ON COLUMN "Formats"."encoding" IS
    'Encodage des données de l''image: "geophysics" ou "native".';
COMMENT ON CONSTRAINT "Format_type" ON "Formats" IS
    'Énumération des valeurs acceptables.';




--------------------------------------------------------------------------------------------------
-- Creates the "SampleDimensions" table.                                                        --
-- Dependencies: Formats                                                                        --
--------------------------------------------------------------------------------------------------

CREATE TABLE "SampleDimensions" (
    identifier character varying NOT NULL,
    format character varying NOT NULL,
    band smallint DEFAULT 1 NOT NULL,
    units character varying DEFAULT ''::character varying NOT NULL,
    CONSTRAINT "Positive_band" CHECK ((band >= 1))
);

ALTER TABLE "SampleDimensions" OWNER TO geoadmin;
GRANT ALL ON TABLE "SampleDimensions" TO geoadmin;
GRANT SELECT ON TABLE "SampleDimensions" TO PUBLIC;

ALTER TABLE ONLY "SampleDimensions"
    ADD CONSTRAINT "SampleDimensions_pkey" PRIMARY KEY (identifier);
ALTER TABLE ONLY "SampleDimensions"
    ADD CONSTRAINT "SampleDimension_uniqueness" UNIQUE (format, band);
ALTER TABLE ONLY "SampleDimensions"
    ADD CONSTRAINT "Format_reference" FOREIGN KEY (format) REFERENCES "Formats"(name)
    ON UPDATE CASCADE ON DELETE CASCADE;

CREATE INDEX "Band_index" ON "SampleDimensions" USING btree (band);
CREATE INDEX "Format_index" ON "SampleDimensions" USING btree (format);

COMMENT ON TABLE "SampleDimensions" IS
    'Descriptions des bandes comprises dans chaque format d''images.';
COMMENT ON COLUMN "SampleDimensions".identifier IS
    'Nom unique identifiant la bande.';
COMMENT ON COLUMN "SampleDimensions".format IS
    'Format qui contient cette bande.';
COMMENT ON COLUMN "SampleDimensions".band IS
    'Numéro de la bande (à partir de 1).';
COMMENT ON COLUMN "SampleDimensions".units IS
    'Unités des mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';
COMMENT ON CONSTRAINT "Positive_band" ON "SampleDimensions" IS
    'Le numéro de bande doit être positif.';
COMMENT ON CONSTRAINT "Format_reference" ON "SampleDimensions" IS
    'Chaque bande fait partie de la description d''une image.';
COMMENT ON INDEX "Band_index" IS
    'Classement des bandes dans leur ordre d''apparition.';




--------------------------------------------------------------------------------------------------
-- Creates the "Categories" table.                                                              --
-- Dependencies: "SampleDimensions"                                                             --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Categories" (
    name character varying NOT NULL,
    band character varying NOT NULL,
    lower integer NOT NULL,
    upper integer NOT NULL,
    c0 double precision,
    c1 double precision,
    "function" character varying,
    colors character varying DEFAULT '#000000'::character varying NOT NULL,
    CONSTRAINT "Sample_coefficients" CHECK ((((c0 IS NULL) AND (c1 IS NULL)) OR
        (((c0 IS NOT NULL) AND (c1 IS NOT NULL)) AND (c1 <> (0)::double precision)))),
    CONSTRAINT "Sample_range" CHECK ((lower <= upper))
);

ALTER TABLE "Categories" OWNER TO geoadmin;
GRANT ALL ON TABLE "Categories" TO geoadmin;
GRANT SELECT ON TABLE "Categories" TO PUBLIC;

ALTER TABLE ONLY "Categories"
    ADD CONSTRAINT "Categories_pkey" PRIMARY KEY (name, band);
ALTER TABLE ONLY "Categories"
    ADD CONSTRAINT "SampleDimension_reference" FOREIGN KEY (band) REFERENCES "SampleDimensions"(identifier)
    ON UPDATE CASCADE ON DELETE CASCADE;

CREATE INDEX "SampleDimension_index" ON "Categories" USING btree (band);

COMMENT ON TABLE "Categories" IS
    'Plage de valeurs des différents thèmes et relation entre les valeurs des pixels et leurs mesures géophysiques.';
COMMENT ON COLUMN "Categories".name IS
    'Nom du thème représenté par cette plage de valeurs.';
COMMENT ON COLUMN "Categories".band IS
    'Bande auquel s''applique cette plage de valeurs.';
COMMENT ON COLUMN "Categories".lower IS
    'Valeur minimale (inclusive) des pixels de ce thème.';
COMMENT ON COLUMN "Categories".upper IS
    'Valeur maximale (inclusive) des pixels de ce thème.';
COMMENT ON COLUMN "Categories".c0 IS
    'Coefficient C0 de l''équation y=C0+C1*x, ou x est la valeur du pixel et y la valeur en mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';
COMMENT ON COLUMN "Categories".c1 IS
    'Coefficient C1 de l''équation y=C0+C1*x, ou x est la valeur du pixel et y la valeur en mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';
COMMENT ON COLUMN "Categories"."function" IS
    'Fonction appliquée sur les valeurs géophysiques. Par exemple la valeur "log" indique que les valeurs sont exprimées sous la forme log(y)=C0+C1*x.';
COMMENT ON COLUMN "Categories".colors IS
    'Ce champ peut être soit un code d''une couleur, ou soit une adresse URL vers une palette de couleurs.';
COMMENT ON CONSTRAINT "Sample_coefficients" ON "Categories" IS
    'Les coefficients C0 et C1 doivent être nuls ou non-nuls en même temps.';
COMMENT ON CONSTRAINT "SampleDimension_reference" ON "Categories" IS
    'Chaque catégorie est un élément de la description d''une bande.';
COMMENT ON INDEX "SampleDimension_index" IS
    'Recherche des catégories appartenant à une bande.';




--------------------------------------------------------------------------------------------------
-- Creates the "CategoriesDetails" view.                                                        --
-- Dependencies: "Categories", "SampleDimensions", "Format"                                     --
--------------------------------------------------------------------------------------------------

CREATE VIEW "CategoriesDetails" AS
    SELECT "Formats".name AS format, "SampleDimensions".units, "Categories".name, "SampleDimensions".band,
           "Categories".lower, "Categories".upper, "Categories".c0, "Categories".c1, "Categories"."function",
           "Formats"."encoding" AS "type", "Categories".colors
      FROM (("Formats" JOIN "SampleDimensions" ON ((("SampleDimensions".format)::text = ("Formats".name)::text)))
      JOIN "Categories" ON ((("SampleDimensions".identifier)::text = ("Categories".band)::text)))
  ORDER BY "Formats".name, "SampleDimensions".band, "Categories".lower;

ALTER TABLE "CategoriesDetails" OWNER TO geoadmin;
GRANT ALL ON TABLE "CategoriesDetails" TO geoadmin;
GRANT SELECT ON TABLE "CategoriesDetails" TO PUBLIC;

COMMENT ON VIEW "CategoriesDetails" IS
    'Liste des catégories et des noms de formats dans la même table.';




--------------------------------------------------------------------------------------------------
-- Creates the "Layers" table.                                                                  --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Layers" (
    name character varying NOT NULL,
    thematic character varying NOT NULL,
    "procedure" character varying NOT NULL,
    period double precision,
    fallback character varying,
    description text
);

ALTER TABLE "Layers" OWNER TO geoadmin;
GRANT ALL ON TABLE "Layers" TO geoadmin;
GRANT SELECT ON TABLE "Layers" TO PUBLIC;

ALTER TABLE ONLY "Layers"
    ADD CONSTRAINT "Layers_pkey" PRIMARY KEY (name);
ALTER TABLE ONLY "Layers"
    ADD CONSTRAINT "Fallback_reference" FOREIGN KEY (fallback) REFERENCES "Layers"(name)
    ON UPDATE CASCADE ON DELETE RESTRICT;

COMMENT ON TABLE "Layers" IS
    'Ensemble de séries d''images appartenant à une même thématique.';
COMMENT ON COLUMN "Layers".name IS
    'Nom identifiant la couche.';
COMMENT ON COLUMN "Layers".thematic IS
    'Paramètre géophysique (ou thème) de cette couche.';
COMMENT ON COLUMN "Layers"."procedure" IS
    'Procédure appliquée pour produire les images.';
COMMENT ON COLUMN "Layers".period IS
    'Nombre de jours prévus entre deux image. Cette information peut être approximative ou laissée blanc si elle ne s''applique pas.';
COMMENT ON COLUMN "Layers".fallback IS
    'Couche de rechange proposée si aucune donnée n''est disponible pour la couche courante.';
COMMENT ON COLUMN "Layers".description IS
    'Remarques s''appliquant à la couche.';
COMMENT ON CONSTRAINT "Fallback_reference" ON "Layers" IS
    'Chaque couche de second recours doit exister.';




--------------------------------------------------------------------------------------------------
-- Creates the "Series" table.                                                                  --
-- Dependencies: "Layers", "Formats"                                                            --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Series" (
    identifier character varying NOT NULL,
    layer character varying NOT NULL,
    pathname character varying NOT NULL,
    extension character varying NOT NULL,
    format character varying NOT NULL,
    visible boolean DEFAULT true NOT NULL,
    quicklook character varying
);

ALTER TABLE "Series" OWNER TO geoadmin;
GRANT ALL ON TABLE "Series" TO geoadmin;
GRANT SELECT ON TABLE "Series" TO PUBLIC;

ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Series_pkey" PRIMARY KEY (identifier);
ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Series_reference" FOREIGN KEY (layer) REFERENCES "Layers"(name)
    ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Quicklook_uniqueness" UNIQUE (quicklook);
ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Format_reference" FOREIGN KEY (format) REFERENCES "Formats"(name)
    ON UPDATE CASCADE ON DELETE RESTRICT;
ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Quicklook_reference" FOREIGN KEY (quicklook) REFERENCES "Series"(identifier)
    ON UPDATE CASCADE ON DELETE RESTRICT;

CREATE INDEX "Layers_index" ON "Series" USING btree (layer);
CREATE INDEX "Visibility_index" ON "Series" USING btree (visible);

COMMENT ON TABLE "Series" IS
    'Séries d''images. Chaque images appartient à une série.';
COMMENT ON COLUMN "Series".identifier IS
    'Identifiant unique de la séries.';
COMMENT ON COLUMN "Series".layer IS
    'Couche à laquelle appartiennent les images de cette série.';
COMMENT ON COLUMN "Series".pathname IS
    'Chemins relatifs des fichiers du groupe. La racine à ces chemins ne doit pas être spécifiée si elle peut varier d''une plateforme à l''autre.';
COMMENT ON COLUMN "Series".extension IS
    'Extension des fichiers d''images de cette série.';
COMMENT ON COLUMN "Series".format IS
    'Format des images de ce groupe.';
COMMENT ON COLUMN "Series".visible IS
    'Indique si les images de ce groupe doivent apparaître dans la liste des images proposées à l''utilisateur.';
COMMENT ON COLUMN "Series".quicklook IS
    'Série dont les images sont des aperçus de cette série.';
COMMENT ON CONSTRAINT "Series_reference" ON "Series" IS
    'Chaque série appartient à une couche.';
COMMENT ON CONSTRAINT "Quicklook_uniqueness" ON "Series" IS
    'Chaque série a une seule autre série d''aperçus.';
COMMENT ON CONSTRAINT "Format_reference" ON "Series" IS
    'Toutes les images d''une même série utilisent un même séries.';
COMMENT ON CONSTRAINT "Quicklook_reference" ON "Series" IS
    'Les aperçus s''appliquent à une autre séries d''images.';




--------------------------------------------------------------------------------------------------
-- Creates the "GridGeometries" table.                                                          --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "GridGeometries" (
    identifier character varying NOT NULL,
    width integer NOT NULL,
    height integer NOT NULL,
    "translateX" double precision DEFAULT 0 NOT NULL,
    "translateY" double precision DEFAULT 0 NOT NULL,
    "scaleX" double precision DEFAULT 1 NOT NULL,
    "scaleY" double precision DEFAULT 1 NOT NULL,
    "shearX" double precision DEFAULT 0 NOT NULL,
    "shearY" double precision DEFAULT 0 NOT NULL,
    "horizontalSRID" integer DEFAULT 4326 NOT NULL,
    "horizontalExtent" postgis.geometry NOT NULL,
    "verticalSRID" integer,
    "verticalOrdinates" double precision[],
    CONSTRAINT "GridCoverageSize" CHECK (((width > 0) AND (height > 0))),
    CONSTRAINT "enforce_dims_horizontalExtent" CHECK ((postgis.ndims("horizontalExtent") = 2)),
    CONSTRAINT "enforce_geotype_horizontalExtent" CHECK (((postgis.geometrytype("horizontalExtent") = 'POLYGON'::text) OR ("horizontalExtent" IS NULL))),
    CONSTRAINT "enforce_srid_horizontalExtent" CHECK ((postgis.srid("horizontalExtent") = 4326)),
    CONSTRAINT "enforce_srid_verticalOrdinates" CHECK (((("verticalSRID" IS NULL) AND ("verticalOrdinates" IS NULL)) OR (("verticalSRID" IS NOT NULL) AND ("verticalOrdinates" IS NOT NULL))))
);

ALTER TABLE "GridGeometries" OWNER TO geoadmin;
GRANT ALL ON TABLE "GridGeometries" TO geoadmin;
GRANT SELECT ON TABLE "GridGeometries" TO PUBLIC;

ALTER TABLE ONLY "GridGeometries"
    ADD CONSTRAINT "GridGeometries_pkey" PRIMARY KEY (identifier);
ALTER TABLE ONLY "GridGeometries"
    ADD CONSTRAINT "fk_SRID" FOREIGN KEY ("horizontalSRID") REFERENCES postgis.spatial_ref_sys(srid)
    ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE ONLY "GridGeometries"
    ADD CONSTRAINT "fk_VERT_SRID" FOREIGN KEY ("verticalSRID") REFERENCES postgis.spatial_ref_sys(srid)
    ON UPDATE RESTRICT ON DELETE RESTRICT;

CREATE INDEX "HorizontalExtent_index" ON "GridGeometries" USING gist ("horizontalExtent" postgis.gist_geometry_ops);

COMMENT ON TABLE "GridGeometries" IS
    'Envelope spatiales des images ainsi que la dimension de leurs grilles. La transformation affine doit représenter le coin supérieur gauche des pixels.';
COMMENT ON COLUMN "GridGeometries".identifier IS
    'Identifiant unique.';
COMMENT ON COLUMN "GridGeometries".width IS
    'Nombre de pixels en largeur dans l''image.';
COMMENT ON COLUMN "GridGeometries".height IS
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
COMMENT ON CONSTRAINT "GridCoverageSize" ON "GridGeometries" IS
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
    SELECT "GeometryDetails".identifier, "GeometryDetails".width, "GeometryDetails".height, "GeometryDetails".crs, postgis.xmin(("GeometryDetails"."nativeBox")::postgis.box3d) AS "minX", postgis.xmax(("GeometryDetails"."nativeBox")::postgis.box3d) AS "maxX", postgis.ymin(("GeometryDetails"."nativeBox")::postgis.box3d) AS "minY", postgis.ymax(("GeometryDetails"."nativeBox")::postgis.box3d) AS "maxY", postgis.xmin(("GeometryDetails"."geographicBox")::postgis.box3d) AS west, postgis.xmax(("GeometryDetails"."geographicBox")::postgis.box3d) AS east, postgis.ymin(("GeometryDetails"."geographicBox")::postgis.box3d) AS south, postgis.ymax(("GeometryDetails"."geographicBox")::postgis.box3d) AS north FROM (SELECT "TransformedGeometries".identifier, "TransformedGeometries".width, "TransformedGeometries".height, postgis.srid("TransformedGeometries".envelope) AS crs, postgis.box2d("TransformedGeometries".envelope) AS "nativeBox", postgis.box2d(postgis.transform("TransformedGeometries".envelope, 4326)) AS "geographicBox" FROM (SELECT "GridGeometries".identifier, "GridGeometries".width, "GridGeometries".height, postgis.affine(postgis.geometryfromtext((((((((('POLYGON((0 0,0 '::text || ("GridGeometries".height)::text) || ','::text) || ("GridGeometries".width)::text) || ' '::text) || ("GridGeometries".height)::text) || ','::text) || ("GridGeometries".width)::text) || ' 0,0 0))'::text), "GridGeometries"."horizontalSRID"), "GridGeometries"."scaleX", "GridGeometries"."shearX", "GridGeometries"."shearY", "GridGeometries"."scaleY", "GridGeometries"."translateX", "GridGeometries"."translateY") AS envelope FROM "GridGeometries") "TransformedGeometries") "GeometryDetails";

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
    series character varying NOT NULL,
    filename character varying NOT NULL,
    "index" smallint DEFAULT 1 NOT NULL,
    "startTime" timestamp without time zone,
    "endTime" timestamp without time zone,
    extent character varying NOT NULL,
    CONSTRAINT "ImageIndex_check" CHECK (("index" >= 1)),
    CONSTRAINT "TemporalExtent_range" CHECK (((("startTime" IS NULL) AND ("endTime" IS NULL)) OR
        ((("startTime" IS NOT NULL) AND ("endTime" IS NOT NULL)) AND ("startTime" <= "endTime"))))
);

ALTER TABLE "GridCoverages" OWNER TO geoadmin;
GRANT ALL ON TABLE "GridCoverages" TO geoadmin;
GRANT SELECT ON TABLE "GridCoverages" TO PUBLIC;

ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "GridCoverages_pkey" PRIMARY KEY (series, filename, "index");
ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "Series_reference" FOREIGN KEY (series) REFERENCES "Series"(identifier)
    ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "GridCoverages_extent" UNIQUE (series, "startTime", "endTime", extent);
ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "GridGeometry_reference" FOREIGN KEY (extent) REFERENCES "GridGeometries"(identifier)
    ON UPDATE CASCADE ON DELETE RESTRICT;

CREATE INDEX "Series_index" ON "GridCoverages" USING btree (series);
CREATE INDEX "StartTime_index" ON "GridCoverages" USING btree ("startTime");
CREATE INDEX "EndTime_index" ON "GridCoverages" USING btree ("endTime");
CREATE INDEX "Time_index" ON "GridCoverages" USING btree ("startTime", "endTime");
CREATE INDEX "Extent_index" ON "GridCoverages" USING btree (extent);

COMMENT ON TABLE "GridCoverages" IS
    'Liste de toutes les images disponibles. Chaque enregistrement correspond à un fichier d''image.';
COMMENT ON COLUMN "GridCoverages".series IS
    'Série à laquelle appartient l''image.';
COMMENT ON COLUMN "GridCoverages".filename IS
    'Nom du fichier contenant l''image.';
COMMENT ON COLUMN "GridCoverages"."index" IS
    'Index de l''image dans les fichiers contenant plusieurs images. Numérotées à partir de 1.';
COMMENT ON COLUMN "GridCoverages"."startTime" IS
    'Date et heure du début de l''acquisition de l''image, en heure universelle (UTC). Dans le cas des moyennes, cette date correspond au début de l''intervalle de temps ayant servit à établir la moyenne.';
COMMENT ON COLUMN "GridCoverages"."endTime" IS
    'Date et heure de la fin de l''acquisition de l''image, en heure universelle (UTC). Cette date doit être supérieure à la date de début d''acquisition; une valeur égale ne suffit pas.';
COMMENT ON COLUMN "GridCoverages".extent IS
    'Coordonnées de la région géographique couverte par l''image, ainsi que sa résolution approximative. ';
COMMENT ON CONSTRAINT "Series_reference" ON "GridCoverages" IS
    'Chaque image appartient à une série.';
COMMENT ON CONSTRAINT "TemporalExtent_range" ON "GridCoverages" IS
    'Les dates de début et de fin doivent être nulles ou non-nulles en même temps, et la date de début doit être inférieure ou égale à la date de fin.';
COMMENT ON CONSTRAINT "ImageIndex_check" ON "GridCoverages" IS
    'L''index de l''image doit être strictement positif.';
COMMENT ON CONSTRAINT "GridCoverages_extent" ON "GridCoverages" IS
    'L''envelope de l''image doit être unique dans chaque série.';
COMMENT ON CONSTRAINT "GridGeometry_reference" ON "GridCoverages" IS
    'Chaque images doit avoir une étendue spatiale.';
COMMENT ON INDEX "StartTime_index" IS
    'Recherche d''images par leur date de début d''acquisition.';
COMMENT ON INDEX "EndTime_index" IS
    'Recherche d''images par leur date de fin d''acquisition.';
COMMENT ON INDEX "Time_index" IS
    'Recherche de toutes les images à l''intérieur d''une certaine plage de temps.';




--------------------------------------------------------------------------------------------------
-- Creates the "RangeOfLayers" view.                                                            --
-- Dependencies: "GridCoverages", "BoundingBoxes", "Series"                                     --
--------------------------------------------------------------------------------------------------

CREATE VIEW "RangeOfLayers" AS
    SELECT "GridCoveragesDetails".layer, count("GridCoveragesDetails".layer) AS count, min("GridCoveragesDetails"."startTime") AS "startTime", max("GridCoveragesDetails"."endTime") AS "endTime", min("GridCoveragesDetails".west) AS west, max("GridCoveragesDetails".east) AS east, min("GridCoveragesDetails".south) AS south, max("GridCoveragesDetails".north) AS north FROM (SELECT "Series".layer, "GridCoverages".filename, "GridCoverages"."startTime", "GridCoverages"."endTime", "BoundingBoxes".west, "BoundingBoxes".east, "BoundingBoxes".south, "BoundingBoxes".north FROM (("GridCoverages" JOIN "BoundingBoxes" ON ((("GridCoverages".extent)::text = ("BoundingBoxes".identifier)::text))) JOIN "Series" ON ((("GridCoverages".series)::text = ("Series".identifier)::text)))) "GridCoveragesDetails" GROUP BY "GridCoveragesDetails".layer ORDER BY "GridCoveragesDetails".layer;

ALTER TABLE "RangeOfLayers" OWNER TO geoadmin;
GRANT ALL ON TABLE "RangeOfLayers" TO geoadmin;
GRANT SELECT ON TABLE "RangeOfLayers" TO PUBLIC;

COMMENT ON VIEW "RangeOfLayers" IS
    'Nombre d''images pour chacune des couches utilisées.';




--------------------------------------------------------------------------------------------------
-- Creates the "RangeOfSeries" view.                                                            --
-- Dependencies: "GridCoverages", "BoundingBoxes", "Series"                                     --
--------------------------------------------------------------------------------------------------

CREATE VIEW "RangeOfSeries" AS
    SELECT "Series".layer, "GridCoverages".series, count("GridCoverages".extent) AS count, min("GridCoverages"."startTime") AS "startTime", max("GridCoverages"."endTime") AS "endTime", min("BoundingBoxes".west) AS west, max("BoundingBoxes".east) AS east, min("BoundingBoxes".south) AS south, max("BoundingBoxes".north) AS north FROM (("GridCoverages" JOIN "Series" ON ((("GridCoverages".series)::text = ("Series".identifier)::text))) JOIN "BoundingBoxes" ON ((("GridCoverages".extent)::text = ("BoundingBoxes".identifier)::text))) GROUP BY "Series".layer, "GridCoverages".series, "GridCoverages".extent ORDER BY "Series".layer, "GridCoverages".series, count("GridCoverages".extent);

ALTER TABLE "RangeOfSeries" OWNER TO geoadmin;
GRANT ALL ON TABLE "RangeOfSeries" TO geoadmin;
GRANT SELECT ON TABLE "RangeOfSeries" TO PUBLIC;

COMMENT ON VIEW "RangeOfSeries" IS
    'Liste des régions géographiques utilisées par chaque sous-série.';
