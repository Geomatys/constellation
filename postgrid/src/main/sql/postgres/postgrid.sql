--
-- Creates the "postgrid" schema with no data.
--
-- For PostGrid installation instructions, see install.html.
--

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- Name: postgrid; Type: SCHEMA; Schema: -; Owner: geoadmin
--

CREATE SCHEMA postgrid;


ALTER SCHEMA postgrid OWNER TO geoadmin;

--
-- Name: SCHEMA postgrid; Type: COMMENT; Schema: -; Owner: geoadmin
--

COMMENT ON SCHEMA postgrid IS 'Metadata for grid coverages';


SET search_path = postgrid, pg_catalog;

--
-- Name: ComputeDefaultExtent(); Type: FUNCTION; Schema: postgrid; Owner: geoadmin
--

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


ALTER FUNCTION postgrid."ComputeDefaultExtent"() OWNER TO geoadmin;

--
-- Name: ReplaceModelDescriptors(); Type: FUNCTION; Schema: postgrid; Owner: geoadmin
--

CREATE FUNCTION "ReplaceModelDescriptors"() RETURNS "trigger"
    AS $$
  DECLARE
    name varchar;
  BEGIN
    SELECT INTO name symbol FROM postgrid."Descriptors" WHERE identifier=NEW.source1;
    IF FOUND THEN
      NEW.source1 := name;
    END IF;
    SELECT INTO name symbol FROM postgrid."Descriptors" WHERE identifier=NEW.source2;
    IF FOUND THEN
      NEW.source2 := name;
    END IF;
    RETURN NEW;
  END;
$$
    LANGUAGE plpgsql;


ALTER FUNCTION postgrid."ReplaceModelDescriptors"() OWNER TO geoadmin;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: Categories; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "Categories" (
    name character varying NOT NULL,
    band character varying NOT NULL,
    lower integer NOT NULL,
    upper integer NOT NULL,
    c0 double precision,
    c1 double precision,
    "function" character varying,
    colors character varying DEFAULT '#000000'::character varying NOT NULL,
    CONSTRAINT "Sample_coefficients" CHECK ((((c0 IS NULL) AND (c1 IS NULL)) OR (((c0 IS NOT NULL) AND (c1 IS NOT NULL)) AND (c1 <> (0)::double precision)))),
    CONSTRAINT "Sample_range" CHECK ((lower <= upper))
);


ALTER TABLE postgrid."Categories" OWNER TO geoadmin;

--
-- Name: TABLE "Categories"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "Categories" IS 'Plage de valeurs des différents thèmes et relation entre les valeurs des pixels et leurs mesures géophysiques.';


--
-- Name: COLUMN "Categories".name; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Categories".name IS 'Nom du thème représenté par cette plage de valeurs.';


--
-- Name: COLUMN "Categories".band; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Categories".band IS 'Bande auquel s''applique cette plage de valeurs.';


--
-- Name: COLUMN "Categories".lower; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Categories".lower IS 'Valeur minimale (inclusive) des pixels de ce thème.';


--
-- Name: COLUMN "Categories".upper; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Categories".upper IS 'Valeur maximale (inclusive) des pixels de ce thème.';


--
-- Name: COLUMN "Categories".c0; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Categories".c0 IS 'Coefficient C0 de l''équation y=C0+C1*x, ou x est la valeur du pixel et y la valeur en mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';


--
-- Name: COLUMN "Categories".c1; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Categories".c1 IS 'Coefficient C1 de l''équation y=C0+C1*x, ou x est la valeur du pixel et y la valeur en mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';


--
-- Name: COLUMN "Categories"."function"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Categories"."function" IS 'Fonction appliquée sur les valeurs géophysiques. Par exemple la valeur "log" indique que les valeurs sont exprimées sous la forme log(y)=C0+C1*x.';


--
-- Name: COLUMN "Categories".colors; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Categories".colors IS 'Ce champ peut être soit un code d''une couleur, ou soit une adresse URL vers une palette de couleurs.';


--
-- Name: CONSTRAINT "Sample_coefficients" ON "Categories"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Sample_coefficients" ON "Categories" IS 'Les coefficients C0 et C1 doivent être nuls ou non-nuls en même temps.';


--
-- Name: Formats; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "Formats" (
    name character varying NOT NULL,
    mime character varying NOT NULL,
    "encoding" character varying(10) DEFAULT 'native'::character varying NOT NULL,
    CONSTRAINT "Format_type" CHECK (((("encoding")::text = 'geophysics'::text) OR (("encoding")::text = 'native'::text)))
);


ALTER TABLE postgrid."Formats" OWNER TO geoadmin;

--
-- Name: TABLE "Formats"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "Formats" IS 'Formats des images (PNG, GIF, JPEG, etc...).';


--
-- Name: COLUMN "Formats".name; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Formats".name IS 'Nom identifiant le format.';


--
-- Name: COLUMN "Formats".mime; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Formats".mime IS 'Nom MIME du format.';


--
-- Name: COLUMN "Formats"."encoding"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Formats"."encoding" IS 'Encodage des données de l''image: "geophysics" ou "native".';


--
-- Name: CONSTRAINT "Format_type" ON "Formats"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Format_type" ON "Formats" IS 'Enumération des valeurs acceptables.';


--
-- Name: SampleDimensions; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "SampleDimensions" (
    identifier character varying NOT NULL,
    format character varying NOT NULL,
    band smallint DEFAULT 1 NOT NULL,
    units character varying DEFAULT ''::character varying NOT NULL,
    CONSTRAINT "Positive_band" CHECK ((band >= 1))
);


ALTER TABLE postgrid."SampleDimensions" OWNER TO geoadmin;

--
-- Name: TABLE "SampleDimensions"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "SampleDimensions" IS 'Descriptions des bandes comprises dans chaque format d''images.';


--
-- Name: COLUMN "SampleDimensions".identifier; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "SampleDimensions".identifier IS 'Nom unique identifiant la bande.';


--
-- Name: COLUMN "SampleDimensions".format; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "SampleDimensions".format IS 'Format qui contient cette bande.';


--
-- Name: COLUMN "SampleDimensions".band; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "SampleDimensions".band IS 'Numéro de la bande (à partir de 1).';


--
-- Name: COLUMN "SampleDimensions".units; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "SampleDimensions".units IS 'Unités des mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';


--
-- Name: CONSTRAINT "Positive_band" ON "SampleDimensions"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Positive_band" ON "SampleDimensions" IS 'Le numéro de bande doit être positif.';


--
-- Name: CategoriesDetails; Type: VIEW; Schema: postgrid; Owner: geoadmin
--

CREATE VIEW "CategoriesDetails" AS
    SELECT "Formats".name AS format, "SampleDimensions".units, "Categories".name, "SampleDimensions".band, "Categories".lower, "Categories".upper, "Categories".c0, "Categories".c1, "Categories"."function", "Formats"."encoding" AS "type", "Categories".colors FROM (("Formats" JOIN "SampleDimensions" ON ((("SampleDimensions".format)::text = ("Formats".name)::text))) JOIN "Categories" ON ((("SampleDimensions".identifier)::text = ("Categories".band)::text))) ORDER BY "Formats".name, "SampleDimensions".band, "Categories".lower;


ALTER TABLE postgrid."CategoriesDetails" OWNER TO geoadmin;

--
-- Name: VIEW "CategoriesDetails"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON VIEW "CategoriesDetails" IS 'Liste des catégories et des noms de formats dans la même table.';


--
-- Name: Descriptors; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "Descriptors" (
    identifier smallint NOT NULL,
    symbol character varying NOT NULL,
    layer character varying NOT NULL,
    operation character varying DEFAULT 'Valeur'::character varying NOT NULL,
    region character varying DEFAULT '+00'::character varying NOT NULL,
    band smallint DEFAULT 1 NOT NULL,
    distribution character varying DEFAULT 'normale'::character varying NOT NULL,
    CONSTRAINT "Band_check" CHECK ((band >= 1))
);


ALTER TABLE postgrid."Descriptors" OWNER TO geoadmin;

--
-- Name: TABLE "Descriptors"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "Descriptors" IS 'Descripteurs du paysage océanique, chacun étant une combinaison d''une couche, d''une opération et d''un décalage spatio-temporel.';


--
-- Name: COLUMN "Descriptors".identifier; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Descriptors".identifier IS 'Clé primaire identifiant ce descripteur du paysage océanique.';


--
-- Name: COLUMN "Descriptors".symbol; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Descriptors".symbol IS 'Symbole unique identifiant ce descripteur, pour une lecture plus humaine que le numéro d''identifiant.';


--
-- Name: COLUMN "Descriptors".layer; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Descriptors".layer IS 'Phénomène (température, chlorophylle...) étudié par ce descripteur.';


--
-- Name: COLUMN "Descriptors".operation; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Descriptors".operation IS 'Opération appliquée sur les mesures du phénomène pour obtenir le descripteur (exemple: opérateur de gradient).';


--
-- Name: COLUMN "Descriptors".region; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Descriptors".region IS 'Décalage spatio-temporelle entre la position de l''observation et celle à laquelle sera évaluée le descripteur.';


--
-- Name: COLUMN "Descriptors".band; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Descriptors".band IS 'Numéro (à partir de 1) de la bande à prendre en compte.';


--
-- Name: COLUMN "Descriptors".distribution; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Descriptors".distribution IS 'Distribution approximative des données. La distribution "Amplitude" résulte d''une combinaison de distributions normales de la forme x²+y². Les distributions normales ne sont généralement pas indépendantes, ce qui distingue cette distribution de X².';


--
-- Name: CONSTRAINT "Band_check" ON "Descriptors"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Band_check" ON "Descriptors" IS 'Les numéros de bandes doivent être des entiers positifs non-nuls.';


--
-- Name: Distributions; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "Distributions" (
    name character varying NOT NULL,
    scale double precision DEFAULT 1 NOT NULL,
    "offset" double precision DEFAULT 0 NOT NULL,
    log boolean DEFAULT false NOT NULL
);


ALTER TABLE postgrid."Distributions" OWNER TO geoadmin;

--
-- Name: TABLE "Distributions"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "Distributions" IS 'Distributions approximatives (normale, log-normale...) des descripteurs.';


--
-- Name: COLUMN "Distributions".name; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Distributions".name IS 'Nom de la distribution.';


--
-- Name: COLUMN "Distributions".scale; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Distributions".scale IS 'Facteur par lequel multiplier les valeurs avant l''analyse statistiques. Utile surtout si le logarithme doit être calculé.';


--
-- Name: COLUMN "Distributions"."offset"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Distributions"."offset" IS 'Constantes à ajouter aux valeurs avant l''analyse statistiques. Utile surtout si le logarithme doit être calculé.';


--
-- Name: COLUMN "Distributions".log; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Distributions".log IS 'Indique si les analyses statistiques doivent se faire sur le logarithme des valeurs transformées. La transformation complète sera alors x''=log(x*scale + offset), ou "log" est le logarithme népérien.';


--
-- Name: GridGeometries; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

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


ALTER TABLE postgrid."GridGeometries" OWNER TO geoadmin;

--
-- Name: TABLE "GridGeometries"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "GridGeometries" IS 'Envelope spatiales des images ainsi que la dimension de leurs grilles. La transformation affine doit représenter le coin supérieur gauche des pixels.';


--
-- Name: COLUMN "GridGeometries".identifier; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries".identifier IS 'Identifiant unique.';


--
-- Name: COLUMN "GridGeometries".width; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries".width IS 'Nombre de pixels en largeur dans l''image.';


--
-- Name: COLUMN "GridGeometries".height; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries".height IS 'Nombre de pixels en hauteur dans l''image.';


--
-- Name: COLUMN "GridGeometries"."translateX"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries"."translateX" IS 'Élement (0,2) de la transformation affine. Il correspond habituellement à la coordonnées x du coin supérieur gauche.';


--
-- Name: COLUMN "GridGeometries"."translateY"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries"."translateY" IS 'Élement (1,2) de la transformation affine. Il correspond habituellement à la coordonnées y du coin supérieur gauche.';


--
-- Name: COLUMN "GridGeometries"."scaleX"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries"."scaleX" IS 'Élement (0,0) de la transformation affine. Il correspond habituellement à la taille selon x des pixels.';


--
-- Name: COLUMN "GridGeometries"."scaleY"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries"."scaleY" IS 'Élement (1,1) de la transformation affine. Il correspond habituellement à la taille selon y des pixels. Cette valeur est souvent négative puisque la numérotation des lignes d''une image augmente vers le bas.';


--
-- Name: COLUMN "GridGeometries"."shearX"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries"."shearX" IS 'Élement (0,1) de la transformation affine. Toujours à 0 s''il n''y a pas de rotation.';


--
-- Name: COLUMN "GridGeometries"."shearY"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries"."shearY" IS 'Élement (1,0) de la transformation affine. Toujours à 0 s''il n''y a pas de rotation.';


--
-- Name: COLUMN "GridGeometries"."horizontalSRID"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries"."horizontalSRID" IS 'Code du système de référence des coordonnées horizontales.';


--
-- Name: COLUMN "GridGeometries"."horizontalExtent"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries"."horizontalExtent" IS 'Étendue spatiale à l''horizontal.';


--
-- Name: COLUMN "GridGeometries"."verticalSRID"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries"."verticalSRID" IS 'Code du système de référence des coordonnées verticales.';


--
-- Name: COLUMN "GridGeometries"."verticalOrdinates"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridGeometries"."verticalOrdinates" IS 'Valeurs z de chacunes des couches d''une image 3D.';


--
-- Name: CONSTRAINT "GridCoverageSize" ON "GridGeometries"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "GridCoverageSize" ON "GridGeometries" IS 'Les dimensions des images doivent être positives.';


--
-- Name: CONSTRAINT "enforce_dims_horizontalExtent" ON "GridGeometries"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "enforce_dims_horizontalExtent" ON "GridGeometries" IS 'Vérifie que l''étendue horizontale est à deux dimensions.';


--
-- Name: CONSTRAINT "enforce_geotype_horizontalExtent" ON "GridGeometries"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "enforce_geotype_horizontalExtent" ON "GridGeometries" IS 'Vérifie que l''étendue horizontale est un polygone.';


--
-- Name: CONSTRAINT "enforce_srid_horizontalExtent" ON "GridGeometries"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "enforce_srid_horizontalExtent" ON "GridGeometries" IS 'Vérifie que l''étendue horizontale est exprimée selon le CRS attendu.';


--
-- Name: CONSTRAINT "enforce_srid_verticalOrdinates" ON "GridGeometries"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "enforce_srid_verticalOrdinates" ON "GridGeometries" IS 'Les coordonnées verticales et leur SRID doivent être nul ou non-nul en même temps.';


--
-- Name: BoundingBoxes; Type: VIEW; Schema: postgrid; Owner: geoadmin
--

CREATE VIEW "BoundingBoxes" AS
    SELECT "GeometryDetails".identifier, "GeometryDetails".width, "GeometryDetails".height, "GeometryDetails".crs, postgis.xmin(("GeometryDetails"."nativeBox")::postgis.box3d) AS "minX", postgis.xmax(("GeometryDetails"."nativeBox")::postgis.box3d) AS "maxX", postgis.ymin(("GeometryDetails"."nativeBox")::postgis.box3d) AS "minY", postgis.ymax(("GeometryDetails"."nativeBox")::postgis.box3d) AS "maxY", postgis.xmin(("GeometryDetails"."geographicBox")::postgis.box3d) AS west, postgis.xmax(("GeometryDetails"."geographicBox")::postgis.box3d) AS east, postgis.ymin(("GeometryDetails"."geographicBox")::postgis.box3d) AS south, postgis.ymax(("GeometryDetails"."geographicBox")::postgis.box3d) AS north FROM (SELECT "TransformedGeometries".identifier, "TransformedGeometries".width, "TransformedGeometries".height, postgis.srid("TransformedGeometries".envelope) AS crs, postgis.box2d("TransformedGeometries".envelope) AS "nativeBox", postgis.box2d(postgis.transform("TransformedGeometries".envelope, 4326)) AS "geographicBox" FROM (SELECT "GridGeometries".identifier, "GridGeometries".width, "GridGeometries".height, postgis.affine(postgis.geometryfromtext((((((((('POLYGON((0 0,0 '::text || ("GridGeometries".height)::text) || ','::text) || ("GridGeometries".width)::text) || ' '::text) || ("GridGeometries".height)::text) || ','::text) || ("GridGeometries".width)::text) || ' 0,0 0))'::text), "GridGeometries"."horizontalSRID"), "GridGeometries"."scaleX", "GridGeometries"."shearX", "GridGeometries"."shearY", "GridGeometries"."scaleY", "GridGeometries"."translateX", "GridGeometries"."translateY") AS envelope FROM "GridGeometries") "TransformedGeometries") "GeometryDetails";


ALTER TABLE postgrid."BoundingBoxes" OWNER TO geoadmin;

--
-- Name: VIEW "BoundingBoxes"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON VIEW "BoundingBoxes" IS 'Comparaison entre les enveloppes calculées et les enveloppes déclarées.';


--
-- Name: GridCoverages; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "GridCoverages" (
    series character varying NOT NULL,
    filename character varying NOT NULL,
    "index" smallint DEFAULT 1 NOT NULL,
    "startTime" timestamp without time zone,
    "endTime" timestamp without time zone,
    extent character varying NOT NULL,
    CONSTRAINT "ImageIndex_check" CHECK (("index" >= 1)),
    CONSTRAINT "TemporalExtent_range" CHECK (((("startTime" IS NULL) AND ("endTime" IS NULL)) OR ((("startTime" IS NOT NULL) AND ("endTime" IS NOT NULL)) AND ("startTime" < "endTime"))))
);


ALTER TABLE postgrid."GridCoverages" OWNER TO geoadmin;

--
-- Name: TABLE "GridCoverages"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "GridCoverages" IS 'Liste de toutes les images disponibles. Chaque enregistrement correspond à un fichier d''image.';


--
-- Name: COLUMN "GridCoverages".series; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridCoverages".series IS 'Série à laquelle appartient l''image.';


--
-- Name: COLUMN "GridCoverages".filename; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridCoverages".filename IS 'Nom du fichier contenant l''image.';


--
-- Name: COLUMN "GridCoverages"."index"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridCoverages"."index" IS 'Index de l''image dans les fichiers contenant plusieurs images. Numérotées à partir de 1.';


--
-- Name: COLUMN "GridCoverages"."startTime"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridCoverages"."startTime" IS 'Date et heure du début de l''acquisition de l''image, en heure universelle (UTC). Dans le cas des moyennes, cette date correspond au début de l''intervalle de temps ayant servit à établir la moyenne.';


--
-- Name: COLUMN "GridCoverages"."endTime"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridCoverages"."endTime" IS 'Date et heure de la fin de l''acquisition de l''image, en heure universelle (UTC). Cette date doit être supérieure à la date de début d''acquisition; une valeur égale ne suffit pas.';


--
-- Name: COLUMN "GridCoverages".extent; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "GridCoverages".extent IS 'Coordonnées de la région géographique couverte par l''image, ainsi que sa résolution approximative. ';


--
-- Name: CONSTRAINT "TemporalExtent_range" ON "GridCoverages"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "TemporalExtent_range" ON "GridCoverages" IS 'Les dates de début et de fin doivent être nulles ou non-nulles en même temps, et la date de début doit être inférieure à la date de fin.';


--
-- Name: CONSTRAINT "ImageIndex_check" ON "GridCoverages"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "ImageIndex_check" ON "GridCoverages" IS 'L''index de l''image doit être strictement positif.';


--
-- Name: Layers; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "Layers" (
    name character varying NOT NULL,
    thematic character varying NOT NULL,
    "procedure" character varying NOT NULL,
    period double precision,
    fallback character varying,
    description text
);


ALTER TABLE postgrid."Layers" OWNER TO geoadmin;

--
-- Name: TABLE "Layers"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "Layers" IS 'Ensemble de séries d''images appartenant à une même thématique.';


--
-- Name: COLUMN "Layers".name; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Layers".name IS 'Nom identifiant la couche.';


--
-- Name: COLUMN "Layers".thematic; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Layers".thematic IS 'Paramètre géophysique (ou thème) de cette couche.';


--
-- Name: COLUMN "Layers".period; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Layers".period IS 'Nombre de jours prévus entre deux image. Cette information peut être approximative ou laissée blanc si elle ne s''applique pas.';


--
-- Name: COLUMN "Layers".fallback; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Layers".fallback IS 'Couche de rechange proposée si aucune donnée n''est disponible pour la couche courante.';


--
-- Name: COLUMN "Layers".description; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Layers".description IS 'Remarques s''appliquant à la couche.';


--
-- Name: LinearModelTerms; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "LinearModelTerms" (
    target character varying NOT NULL,
    source1 character varying NOT NULL,
    source2 character varying DEFAULT '①'::character varying NOT NULL,
    coefficient double precision NOT NULL
);


ALTER TABLE postgrid."LinearModelTerms" OWNER TO geoadmin;

--
-- Name: TABLE "LinearModelTerms"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "LinearModelTerms" IS 'Poids à donner aux différentes combinaisons de descripteurs pour calculer une nouvelle image.';


--
-- Name: COLUMN "LinearModelTerms".target; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "LinearModelTerms".target IS 'Couche d''images pour laquelle ce modèle linéaire effectue ses calculs.';


--
-- Name: COLUMN "LinearModelTerms".source1; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "LinearModelTerms".source1 IS 'Premier descripteur entrant dans le terme.';


--
-- Name: COLUMN "LinearModelTerms".source2; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "LinearModelTerms".source2 IS 'Deuxième descripteur entrant dans le terme. S''il n''est pas le descripteur identité, il sera multiplié par le premier descripteur.';


--
-- Name: COLUMN "LinearModelTerms".coefficient; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "LinearModelTerms".coefficient IS 'Facteur par lequel multiplier le terme du modèle linéaire.';


--
-- Name: OperationParameters; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "OperationParameters" (
    operation character varying NOT NULL,
    parameter character varying NOT NULL,
    value character varying NOT NULL
);


ALTER TABLE postgrid."OperationParameters" OWNER TO geoadmin;

--
-- Name: TABLE "OperationParameters"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "OperationParameters" IS 'Valeur des paramètres des opérations d''images.';


--
-- Name: COLUMN "OperationParameters".operation; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "OperationParameters".operation IS 'Nom de l''opération sur lequel s''appliquera un ou plusieurs paramètres.';


--
-- Name: COLUMN "OperationParameters".parameter; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "OperationParameters".parameter IS 'Nom du paramètre, tel que déclaré dans CoverageProcessor ou JAI.';


--
-- Name: COLUMN "OperationParameters".value; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "OperationParameters".value IS 'Valeur du paramètre.';


--
-- Name: Operations; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "Operations" (
    name character varying NOT NULL,
    prefix character varying NOT NULL,
    operation character varying,
    "kernelSize" smallint DEFAULT 1,
    description text
);


ALTER TABLE postgrid."Operations" OWNER TO geoadmin;

--
-- Name: TABLE "Operations"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "Operations" IS 'Opérations mathématique ayant servit à produire les images.';


--
-- Name: COLUMN "Operations".name; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Operations".name IS 'Nom identifiant l''opération.';


--
-- Name: COLUMN "Operations".prefix; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Operations".prefix IS 'Préfix à utiliser dans les noms composites. Les noms composites seront de la forme "operation - paramètre - temps", par exemple "∇SST₋₁₅".';


--
-- Name: COLUMN "Operations".operation; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Operations".operation IS 'Nom OpenGIS ou JAI. identifiant l''opération. Ce nom sera transmis en argument à la méthode "GridCoverageProcessor.doOperation(...)".';


--
-- Name: COLUMN "Operations"."kernelSize"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Operations"."kernelSize" IS 'Nombre de pixels selon x et y nécessaire à l''application de l''opération.';


--
-- Name: COLUMN "Operations".description; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Operations".description IS 'Description de l''opération.';


--
-- Name: Procedures; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "Procedures" (
    name character varying NOT NULL,
    description text
);


ALTER TABLE postgrid."Procedures" OWNER TO geoadmin;

--
-- Name: TABLE "Procedures"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "Procedures" IS 'Procédures utilisées pour effectuer une observation.';


--
-- Name: COLUMN "Procedures".name; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Procedures".name IS 'Nom unique identifiant cette procédure.';


--
-- Name: COLUMN "Procedures".description; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Procedures".description IS 'Description de la procédure.';


--
-- Name: RegionOfInterests; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "RegionOfInterests" (
    name character varying NOT NULL,
    dx double precision NOT NULL,
    dy double precision NOT NULL,
    dz double precision NOT NULL,
    dt double precision NOT NULL
);


ALTER TABLE postgrid."RegionOfInterests" OWNER TO geoadmin;

--
-- Name: TABLE "RegionOfInterests"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "RegionOfInterests" IS 'Positions spatio-temporelles relatives à des observations.';


--
-- Name: COLUMN "RegionOfInterests".name; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "RegionOfInterests".name IS 'Nom unique identifiant cette position relative.';


--
-- Name: COLUMN "RegionOfInterests".dx; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "RegionOfInterests".dx IS 'Décalage Est-Ouest, en mètres.';


--
-- Name: COLUMN "RegionOfInterests".dy; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "RegionOfInterests".dy IS 'Décalage Nord-Sud, en mètres.';


--
-- Name: COLUMN "RegionOfInterests".dz; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "RegionOfInterests".dz IS 'Décalage vertical, en mètres.';


--
-- Name: COLUMN "RegionOfInterests".dt; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "RegionOfInterests".dt IS 'Décalage temporel, en nombre de jours.';


--
-- Name: Series; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "Series" (
    identifier character varying NOT NULL,
    layer character varying NOT NULL,
    pathname character varying NOT NULL,
    extension character varying NOT NULL,
    format character varying NOT NULL,
    visible boolean DEFAULT true NOT NULL,
    quicklook character varying
);


ALTER TABLE postgrid."Series" OWNER TO geoadmin;

--
-- Name: TABLE "Series"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "Series" IS 'Séries d''images. Chaque images appartient à une série.';


--
-- Name: COLUMN "Series".identifier; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Series".identifier IS 'Identifiant unique de la séries.';


--
-- Name: COLUMN "Series".layer; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Series".layer IS 'Couche à laquelle appartiennent les images de cette série.';


--
-- Name: COLUMN "Series".pathname; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Series".pathname IS 'Chemins relatifs des fichiers du groupe. La racine à ces chemins ne doit pas être spécifiée si elle peut varier d''une plateforme à l''autre.';


--
-- Name: COLUMN "Series".extension; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Series".extension IS 'Extension des fichiers d''images de cette série.';


--
-- Name: COLUMN "Series".format; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Series".format IS 'Format des images de ce groupe.';


--
-- Name: COLUMN "Series".visible; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Series".visible IS 'Indique si les images de ce groupe doivent apparaître dans la liste des images proposées à l''utilisateur.';


--
-- Name: COLUMN "Series".quicklook; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Series".quicklook IS 'Série dont les images sont des aperçus de cette série.';


--
-- Name: RangeOfLayers; Type: VIEW; Schema: postgrid; Owner: geoadmin
--

CREATE VIEW "RangeOfLayers" AS
    SELECT "GridCoveragesDetails".layer, count("GridCoveragesDetails".layer) AS count, min("GridCoveragesDetails"."startTime") AS "startTime", max("GridCoveragesDetails"."endTime") AS "endTime", min("GridCoveragesDetails".west) AS west, max("GridCoveragesDetails".east) AS east, min("GridCoveragesDetails".south) AS south, max("GridCoveragesDetails".north) AS north FROM (SELECT "Series".layer, "GridCoverages".filename, "GridCoverages"."startTime", "GridCoverages"."endTime", "BoundingBoxes".west, "BoundingBoxes".east, "BoundingBoxes".south, "BoundingBoxes".north FROM (("GridCoverages" JOIN "BoundingBoxes" ON ((("GridCoverages".extent)::text = ("BoundingBoxes".identifier)::text))) JOIN "Series" ON ((("GridCoverages".series)::text = ("Series".identifier)::text)))) "GridCoveragesDetails" GROUP BY "GridCoveragesDetails".layer ORDER BY "GridCoveragesDetails".layer;


ALTER TABLE postgrid."RangeOfLayers" OWNER TO geoadmin;

--
-- Name: VIEW "RangeOfLayers"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON VIEW "RangeOfLayers" IS 'Nombre d''images pour chacune des couches utilisées.';


--
-- Name: RangeOfSeries; Type: VIEW; Schema: postgrid; Owner: geoadmin
--

CREATE VIEW "RangeOfSeries" AS
    SELECT "Series".layer, "GridCoverages".series, count("GridCoverages".extent) AS count, min("GridCoverages"."startTime") AS "startTime", max("GridCoverages"."endTime") AS "endTime", min("BoundingBoxes".west) AS west, max("BoundingBoxes".east) AS east, min("BoundingBoxes".south) AS south, max("BoundingBoxes".north) AS north FROM (("GridCoverages" JOIN "Series" ON ((("GridCoverages".series)::text = ("Series".identifier)::text))) JOIN "BoundingBoxes" ON ((("GridCoverages".extent)::text = ("BoundingBoxes".identifier)::text))) GROUP BY "Series".layer, "GridCoverages".series, "GridCoverages".extent ORDER BY "Series".layer, "GridCoverages".series, count("GridCoverages".extent);


ALTER TABLE postgrid."RangeOfSeries" OWNER TO geoadmin;

--
-- Name: VIEW "RangeOfSeries"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON VIEW "RangeOfSeries" IS 'Liste des régions géographiques utilisées par chaque sous-série.';


--
-- Name: Thematics; Type: TABLE; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE TABLE "Thematics" (
    name character varying NOT NULL,
    description text NOT NULL
);


ALTER TABLE postgrid."Thematics" OWNER TO geoadmin;

--
-- Name: TABLE "Thematics"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TABLE "Thematics" IS 'Paramètres géophysiques représentés par les images (température, hauteur de l''eau...).';


--
-- Name: COLUMN "Thematics".name; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Thematics".name IS 'Nom identifiant le paramètre géophysique.';


--
-- Name: COLUMN "Thematics".description; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON COLUMN "Thematics".description IS 'Description du paramètre géophysique.';


--
-- Name: Categories_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Categories"
    ADD CONSTRAINT "Categories_pkey" PRIMARY KEY (name, band);


--
-- Name: Descriptor_uniqueness; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "Descriptor_uniqueness" UNIQUE (layer, region, band, operation);


--
-- Name: Descriptors_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "Descriptors_pkey" PRIMARY KEY (identifier);


--
-- Name: Distributions_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Distributions"
    ADD CONSTRAINT "Distributions_pkey" PRIMARY KEY (name);


--
-- Name: Formats_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Formats"
    ADD CONSTRAINT "Formats_pkey" PRIMARY KEY (name);


--
-- Name: GridCoverages_extent; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "GridCoverages_extent" UNIQUE (series, "startTime", "endTime", extent);


--
-- Name: CONSTRAINT "GridCoverages_extent" ON "GridCoverages"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "GridCoverages_extent" ON "GridCoverages" IS 'L''envelope de l''image doit être unique dans chaque série.';


--
-- Name: GridCoverages_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "GridCoverages_pkey" PRIMARY KEY (series, filename, "index");


--
-- Name: GridGeometries_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "GridGeometries"
    ADD CONSTRAINT "GridGeometries_pkey" PRIMARY KEY (identifier);


--
-- Name: Layers_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Layers"
    ADD CONSTRAINT "Layers_pkey" PRIMARY KEY (name);


--
-- Name: LinearModels_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "LinearModelTerms"
    ADD CONSTRAINT "LinearModels_pkey" PRIMARY KEY (target, source1, source2);


--
-- Name: LocationOffsets_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "RegionOfInterests"
    ADD CONSTRAINT "LocationOffsets_pkey" PRIMARY KEY (name);


--
-- Name: OperationParameters_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "OperationParameters"
    ADD CONSTRAINT "OperationParameters_pkey" PRIMARY KEY (operation, parameter);


--
-- Name: Operations_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Operations"
    ADD CONSTRAINT "Operations_pkey" PRIMARY KEY (name);


--
-- Name: Prefix_uniqueness; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Operations"
    ADD CONSTRAINT "Prefix_uniqueness" UNIQUE (prefix);


--
-- Name: Procedures_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Procedures"
    ADD CONSTRAINT "Procedures_pkey" PRIMARY KEY (name);


--
-- Name: Quicklook_uniqueness; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Quicklook_uniqueness" UNIQUE (quicklook);


--
-- Name: CONSTRAINT "Quicklook_uniqueness" ON "Series"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Quicklook_uniqueness" ON "Series" IS 'Chaque série a une seule autre série d''aperçus.';


--
-- Name: SampleDimension_uniqueness; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "SampleDimensions"
    ADD CONSTRAINT "SampleDimension_uniqueness" UNIQUE (format, band);


--
-- Name: SampleDimensions_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "SampleDimensions"
    ADD CONSTRAINT "SampleDimensions_pkey" PRIMARY KEY (identifier);


--
-- Name: Series_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Series_pkey" PRIMARY KEY (identifier);


--
-- Name: Symbol_uniqueness; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "Symbol_uniqueness" UNIQUE (symbol);


--
-- Name: Thematics_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: geoadmin; Tablespace:
--

ALTER TABLE ONLY "Thematics"
    ADD CONSTRAINT "Thematics_pkey" PRIMARY KEY (name);


--
-- Name: Band_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Band_index" ON "SampleDimensions" USING btree (band);


--
-- Name: INDEX "Band_index"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON INDEX "Band_index" IS 'Classement des bandes dans leur ordre d''apparition.';


--
-- Name: Descriptors1_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Descriptors1_index" ON "LinearModelTerms" USING btree (source1);


--
-- Name: Descriptors2_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Descriptors2_index" ON "LinearModelTerms" USING btree (source2);


--
-- Name: Distributions_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Distributions_index" ON "Descriptors" USING btree (distribution);


--
-- Name: EndTime_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "EndTime_index" ON "GridCoverages" USING btree ("endTime");


--
-- Name: INDEX "EndTime_index"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON INDEX "EndTime_index" IS 'Recherche d''images par leur date de fin d''acquisition.';


--
-- Name: Extent_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Extent_index" ON "GridCoverages" USING btree (extent);


--
-- Name: Format_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Format_index" ON "SampleDimensions" USING btree (format);


--
-- Name: HorizontalExtent_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "HorizontalExtent_index" ON "GridGeometries" USING gist ("horizontalExtent" postgis.gist_geometry_ops);


--
-- Name: Layers_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Layers_index" ON "Series" USING btree (layer);


--
-- Name: LocationOffsets_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "LocationOffsets_index" ON "Descriptors" USING btree (region);


--
-- Name: Operations_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Operations_index" ON "Descriptors" USING btree (operation);


--
-- Name: Phenomenons_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Phenomenons_index" ON "Descriptors" USING btree (operation);


--
-- Name: SampleDimension_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "SampleDimension_index" ON "Categories" USING btree (band);


--
-- Name: INDEX "SampleDimension_index"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON INDEX "SampleDimension_index" IS 'Recherche des catégories appartenant à une bande.';


--
-- Name: Series_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Series_index" ON "GridCoverages" USING btree (series);


--
-- Name: StartTime_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "StartTime_index" ON "GridCoverages" USING btree ("startTime");


--
-- Name: INDEX "StartTime_index"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON INDEX "StartTime_index" IS 'Recherche d''images par leur date de début d''acquisition.';


--
-- Name: Symbol_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Symbol_index" ON "Descriptors" USING btree (symbol);


--
-- Name: Time_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Time_index" ON "GridCoverages" USING btree ("startTime", "endTime");


--
-- Name: INDEX "Time_index"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON INDEX "Time_index" IS 'Recherche de toutes les images à l''intérieur d''une certaine plage de temps.';


--
-- Name: Visibility_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX "Visibility_index" ON "Series" USING btree (visible);


--
-- Name: dt_index; Type: INDEX; Schema: postgrid; Owner: geoadmin; Tablespace:
--

CREATE INDEX dt_index ON "RegionOfInterests" USING btree (dt);


--
-- Name: ReplaceModelDescriptors_trigger; Type: TRIGGER; Schema: postgrid; Owner: geoadmin
--

CREATE TRIGGER "ReplaceModelDescriptors_trigger"
    BEFORE INSERT OR UPDATE ON "LinearModelTerms"
    FOR EACH ROW
    EXECUTE PROCEDURE "ReplaceModelDescriptors"();


--
-- Name: TRIGGER "ReplaceModelDescriptors_trigger" ON "LinearModelTerms"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TRIGGER "ReplaceModelDescriptors_trigger" ON "LinearModelTerms" IS 'Remplace les identifieurs numériques par leurs symboles pour les colonnes "descriptors".';


--
-- Name: addDefaultExtent; Type: TRIGGER; Schema: postgrid; Owner: geoadmin
--

CREATE TRIGGER "addDefaultExtent"
    BEFORE INSERT OR UPDATE ON "GridGeometries"
    FOR EACH ROW
    EXECUTE PROCEDURE "ComputeDefaultExtent"();


--
-- Name: TRIGGER "addDefaultExtent" ON "GridGeometries"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON TRIGGER "addDefaultExtent" ON "GridGeometries" IS 'Ajoute une enveloppe par défaut si aucune n''était définie explicitement.';


--
-- Name: Descriptor1_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "LinearModelTerms"
    ADD CONSTRAINT "Descriptor1_reference" FOREIGN KEY (source1) REFERENCES "Descriptors"(symbol) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Descriptor1_reference" ON "LinearModelTerms"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Descriptor1_reference" ON "LinearModelTerms" IS 'Le premier terme doit être un des descripteurs du paysage océanique.';


--
-- Name: Descriptor2_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "LinearModelTerms"
    ADD CONSTRAINT "Descriptor2_reference" FOREIGN KEY (source2) REFERENCES "Descriptors"(symbol) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Descriptor2_reference" ON "LinearModelTerms"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Descriptor2_reference" ON "LinearModelTerms" IS 'Le second terme doit être un des descripteurs du paysage océanique.';


--
-- Name: Distribution_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "Distribution_reference" FOREIGN KEY (distribution) REFERENCES "Distributions"(name) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Distribution_reference" ON "Descriptors"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Distribution_reference" ON "Descriptors" IS 'Chaque descripteur possède des valeurs suivant une loi de distribution.';


--
-- Name: ExplainedVariable_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "LinearModelTerms"
    ADD CONSTRAINT "ExplainedVariable_reference" FOREIGN KEY (target) REFERENCES "Layers"(name) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: CONSTRAINT "ExplainedVariable_reference" ON "LinearModelTerms"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "ExplainedVariable_reference" ON "LinearModelTerms" IS 'La variable à expliquer doit être une série d''images.';


--
-- Name: Fallback_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "Layers"
    ADD CONSTRAINT "Fallback_reference" FOREIGN KEY (fallback) REFERENCES "Layers"(name) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Fallback_reference" ON "Layers"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Fallback_reference" ON "Layers" IS 'Chaque couche de second recours doit exister.';


--
-- Name: Format_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "SampleDimensions"
    ADD CONSTRAINT "Format_reference" FOREIGN KEY (format) REFERENCES "Formats"(name) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: CONSTRAINT "Format_reference" ON "SampleDimensions"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Format_reference" ON "SampleDimensions" IS 'Chaque bande fait partie de la description d''une image.';


--
-- Name: Format_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Format_reference" FOREIGN KEY (format) REFERENCES "Formats"(name) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Format_reference" ON "Series"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Format_reference" ON "Series" IS 'Toutes les images d''une même série utilisent un même séries.';


--
-- Name: GridGeometry_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "GridGeometry_reference" FOREIGN KEY (extent) REFERENCES "GridGeometries"(identifier) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "GridGeometry_reference" ON "GridCoverages"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "GridGeometry_reference" ON "GridCoverages" IS 'Chaque images doit avoir une étendue spatiale.';


--
-- Name: Layer_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "Layer_reference" FOREIGN KEY (layer) REFERENCES "Layers"(name) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: CONSTRAINT "Layer_reference" ON "Descriptors"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Layer_reference" ON "Descriptors" IS 'Chaque descripteur concerne un phénomène.';


--
-- Name: Operation_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "OperationParameters"
    ADD CONSTRAINT "Operation_reference" FOREIGN KEY (operation) REFERENCES "Operations"(name) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: Operation_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "Operation_reference" FOREIGN KEY (operation) REFERENCES "Operations"(name) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: CONSTRAINT "Operation_reference" ON "Descriptors"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Operation_reference" ON "Descriptors" IS 'Chaque descripteur est le résultat d''une certaine opération appliquée sur les données du phénomène observé.';


--
-- Name: Procedure_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "Layers"
    ADD CONSTRAINT "Procedure_reference" FOREIGN KEY ("procedure") REFERENCES "Procedures"(name) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: Quicklook_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Quicklook_reference" FOREIGN KEY (quicklook) REFERENCES "Series"(identifier) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Quicklook_reference" ON "Series"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Quicklook_reference" ON "Series" IS 'Les aperçus s''appliquent à une autre séries d''images.';


--
-- Name: ROI_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "ROI_reference" FOREIGN KEY (region) REFERENCES "RegionOfInterests"(name) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: CONSTRAINT "ROI_reference" ON "Descriptors"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "ROI_reference" ON "Descriptors" IS 'Chaque descripteur peut être évalué à une position spatio-temporelle décalée par rapport à la position de la station.';


--
-- Name: SampleDimension_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "Categories"
    ADD CONSTRAINT "SampleDimension_reference" FOREIGN KEY (band) REFERENCES "SampleDimensions"(identifier) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: CONSTRAINT "SampleDimension_reference" ON "Categories"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "SampleDimension_reference" ON "Categories" IS 'Chaque catégorie est un élément de la description d''une bande.';


--
-- Name: Series_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "Series_reference" FOREIGN KEY (series) REFERENCES "Series"(identifier) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: CONSTRAINT "Series_reference" ON "GridCoverages"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Series_reference" ON "GridCoverages" IS 'Chaque image appartient à une série.';


--
-- Name: Series_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Series_reference" FOREIGN KEY (layer) REFERENCES "Layers"(name) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: CONSTRAINT "Series_reference" ON "Series"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Series_reference" ON "Series" IS 'Chaque série appartient à une couche.';


--
-- Name: Thematic_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "Layers"
    ADD CONSTRAINT "Thematic_reference" FOREIGN KEY (thematic) REFERENCES "Thematics"(name) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: CONSTRAINT "Thematic_reference" ON "Layers"; Type: COMMENT; Schema: postgrid; Owner: geoadmin
--

COMMENT ON CONSTRAINT "Thematic_reference" ON "Layers" IS 'Chaque couche représente les données observées pour une thématique.';


--
-- Name: fk_SRID; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "GridGeometries"
    ADD CONSTRAINT "fk_SRID" FOREIGN KEY ("horizontalSRID") REFERENCES postgis.spatial_ref_sys(srid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_VERT_SRID; Type: FK CONSTRAINT; Schema: postgrid; Owner: geoadmin
--

ALTER TABLE ONLY "GridGeometries"
    ADD CONSTRAINT "fk_VERT_SRID" FOREIGN KEY ("verticalSRID") REFERENCES postgis.spatial_ref_sys(srid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: postgrid; Type: ACL; Schema: -; Owner: geoadmin
--

REVOKE ALL ON SCHEMA postgrid FROM PUBLIC;
REVOKE ALL ON SCHEMA postgrid FROM geoadmin;
GRANT ALL ON SCHEMA postgrid TO geoadmin;
GRANT USAGE ON SCHEMA postgrid TO PUBLIC;


--
-- Name: ComputeDefaultExtent(); Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON FUNCTION "ComputeDefaultExtent"() FROM PUBLIC;
REVOKE ALL ON FUNCTION "ComputeDefaultExtent"() FROM geoadmin;
GRANT ALL ON FUNCTION "ComputeDefaultExtent"() TO geoadmin;
GRANT ALL ON FUNCTION "ComputeDefaultExtent"() TO PUBLIC;


--
-- Name: ReplaceModelDescriptors(); Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON FUNCTION "ReplaceModelDescriptors"() FROM PUBLIC;
REVOKE ALL ON FUNCTION "ReplaceModelDescriptors"() FROM geoadmin;
GRANT ALL ON FUNCTION "ReplaceModelDescriptors"() TO geoadmin;
GRANT ALL ON FUNCTION "ReplaceModelDescriptors"() TO PUBLIC;


--
-- Name: Categories; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "Categories" FROM PUBLIC;
REVOKE ALL ON TABLE "Categories" FROM geoadmin;
GRANT ALL ON TABLE "Categories" TO geoadmin;
GRANT SELECT ON TABLE "Categories" TO PUBLIC;


--
-- Name: Formats; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "Formats" FROM PUBLIC;
REVOKE ALL ON TABLE "Formats" FROM geoadmin;
GRANT ALL ON TABLE "Formats" TO geoadmin;
GRANT SELECT ON TABLE "Formats" TO PUBLIC;


--
-- Name: SampleDimensions; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "SampleDimensions" FROM PUBLIC;
REVOKE ALL ON TABLE "SampleDimensions" FROM geoadmin;
GRANT ALL ON TABLE "SampleDimensions" TO geoadmin;
GRANT SELECT ON TABLE "SampleDimensions" TO PUBLIC;


--
-- Name: CategoriesDetails; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "CategoriesDetails" FROM PUBLIC;
REVOKE ALL ON TABLE "CategoriesDetails" FROM geoadmin;
GRANT ALL ON TABLE "CategoriesDetails" TO geoadmin;
GRANT SELECT ON TABLE "CategoriesDetails" TO PUBLIC;


--
-- Name: Descriptors; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "Descriptors" FROM PUBLIC;
REVOKE ALL ON TABLE "Descriptors" FROM geoadmin;
GRANT ALL ON TABLE "Descriptors" TO geoadmin;
GRANT SELECT ON TABLE "Descriptors" TO PUBLIC;


--
-- Name: Distributions; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "Distributions" FROM PUBLIC;
REVOKE ALL ON TABLE "Distributions" FROM geoadmin;
GRANT ALL ON TABLE "Distributions" TO geoadmin;
GRANT SELECT ON TABLE "Distributions" TO PUBLIC;


--
-- Name: GridGeometries; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "GridGeometries" FROM PUBLIC;
REVOKE ALL ON TABLE "GridGeometries" FROM geoadmin;
GRANT ALL ON TABLE "GridGeometries" TO geoadmin;
GRANT SELECT ON TABLE "GridGeometries" TO PUBLIC;


--
-- Name: BoundingBoxes; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "BoundingBoxes" FROM PUBLIC;
REVOKE ALL ON TABLE "BoundingBoxes" FROM geoadmin;
GRANT ALL ON TABLE "BoundingBoxes" TO geoadmin;
GRANT SELECT ON TABLE "BoundingBoxes" TO PUBLIC;


--
-- Name: GridCoverages; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "GridCoverages" FROM PUBLIC;
REVOKE ALL ON TABLE "GridCoverages" FROM geoadmin;
GRANT ALL ON TABLE "GridCoverages" TO geoadmin;
GRANT SELECT ON TABLE "GridCoverages" TO PUBLIC;


--
-- Name: Layers; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "Layers" FROM PUBLIC;
REVOKE ALL ON TABLE "Layers" FROM geoadmin;
GRANT ALL ON TABLE "Layers" TO geoadmin;
GRANT SELECT ON TABLE "Layers" TO PUBLIC;


--
-- Name: LinearModelTerms; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "LinearModelTerms" FROM PUBLIC;
REVOKE ALL ON TABLE "LinearModelTerms" FROM geoadmin;
GRANT ALL ON TABLE "LinearModelTerms" TO geoadmin;
GRANT SELECT ON TABLE "LinearModelTerms" TO PUBLIC;


--
-- Name: OperationParameters; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "OperationParameters" FROM PUBLIC;
REVOKE ALL ON TABLE "OperationParameters" FROM geoadmin;
GRANT ALL ON TABLE "OperationParameters" TO geoadmin;
GRANT SELECT ON TABLE "OperationParameters" TO PUBLIC;


--
-- Name: Operations; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "Operations" FROM PUBLIC;
REVOKE ALL ON TABLE "Operations" FROM geoadmin;
GRANT ALL ON TABLE "Operations" TO geoadmin;
GRANT SELECT ON TABLE "Operations" TO PUBLIC;


--
-- Name: RegionOfInterests; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "RegionOfInterests" FROM PUBLIC;
REVOKE ALL ON TABLE "RegionOfInterests" FROM geoadmin;
GRANT ALL ON TABLE "RegionOfInterests" TO geoadmin;
GRANT SELECT ON TABLE "RegionOfInterests" TO PUBLIC;


--
-- Name: Series; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "Series" FROM PUBLIC;
REVOKE ALL ON TABLE "Series" FROM geoadmin;
GRANT ALL ON TABLE "Series" TO geoadmin;
GRANT SELECT ON TABLE "Series" TO PUBLIC;


--
-- Name: RangeOfLayers; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "RangeOfLayers" FROM PUBLIC;
REVOKE ALL ON TABLE "RangeOfLayers" FROM geoadmin;
GRANT ALL ON TABLE "RangeOfLayers" TO geoadmin;
GRANT SELECT ON TABLE "RangeOfLayers" TO PUBLIC;


--
-- Name: RangeOfSeries; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "RangeOfSeries" FROM PUBLIC;
REVOKE ALL ON TABLE "RangeOfSeries" FROM geoadmin;
GRANT ALL ON TABLE "RangeOfSeries" TO geoadmin;
GRANT SELECT ON TABLE "RangeOfSeries" TO PUBLIC;


--
-- Name: Thematics; Type: ACL; Schema: postgrid; Owner: geoadmin
--

REVOKE ALL ON TABLE "Thematics" FROM PUBLIC;
REVOKE ALL ON TABLE "Thematics" FROM geoadmin;
GRANT ALL ON TABLE "Thematics" TO geoadmin;
GRANT SELECT ON TABLE "Thematics" TO PUBLIC;


--
-- PostgreSQL database dump complete
--

