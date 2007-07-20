--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- Name: postgrid; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA postgrid;


--
-- Name: SCHEMA postgrid; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA postgrid IS 'Metadata for grid coverages';


SET search_path = postgrid, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: Categories; Type: TABLE; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE TABLE "Categories" (
    name character varying(50) NOT NULL,
    band character varying(20) NOT NULL,
    lower integer NOT NULL,
    upper integer NOT NULL,
    c0 double precision,
    c1 double precision,
    "function" character varying(5),
    colors character varying(40) DEFAULT '#000000'::character varying NOT NULL,
    CONSTRAINT "Sample_coefficients" CHECK ((((c0 IS NULL) AND (c1 IS NULL)) OR (((c0 IS NOT NULL) AND (c1 IS NOT NULL)) AND (c1 <> (0)::double precision)))),
    CONSTRAINT "Sample_range" CHECK ((lower <= upper))
);


--
-- Name: TABLE "Categories"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON TABLE "Categories" IS 'Plage de valeurs des différents thèmes et relation entre les valeurs des pixels et leurs mesures géophysiques.';


--
-- Name: COLUMN "Categories".name; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Categories".name IS 'Nom du thème représenté par cette plage de valeurs.';


--
-- Name: COLUMN "Categories".band; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Categories".band IS 'Bande auquel s''applique cette plage de valeurs.';


--
-- Name: COLUMN "Categories".lower; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Categories".lower IS 'Valeur minimale (inclusive) des pixels de ce thème.';


--
-- Name: COLUMN "Categories".upper; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Categories".upper IS 'Valeur maximale (inclusive) des pixels de ce thème.';


--
-- Name: COLUMN "Categories".c0; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Categories".c0 IS 'Coefficient C0 de l''équation y=C0+C1*x, ou x est la valeur du pixel et y la valeur en mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';


--
-- Name: COLUMN "Categories".c1; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Categories".c1 IS 'Coefficient C1 de l''équation y=C0+C1*x, ou x est la valeur du pixel et y la valeur en mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';


--
-- Name: COLUMN "Categories"."function"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Categories"."function" IS 'Fonction appliquée sur les valeurs géophysiques. Par exemple la valeur "log" indique que les valeurs sont exprimées sous la forme log(y)=C0+C1*x.';


--
-- Name: COLUMN "Categories".colors; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Categories".colors IS 'Ce champ peut être soit un code d''une couleur, ou soit une adresse URL vers une palette de couleurs.';


--
-- Name: CONSTRAINT "Sample_coefficients" ON "Categories"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "Sample_coefficients" ON "Categories" IS 'Les coefficients C0 et C1 doivent être nuls ou non-nuls en même temps.';


--
-- Name: Formats; Type: TABLE; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE TABLE "Formats" (
    name character varying(60) NOT NULL,
    mime character varying(30) NOT NULL,
    "encoding" character varying(10) DEFAULT 'native'::character varying NOT NULL,
    CONSTRAINT "Format_type" CHECK (((("encoding")::text = 'geophysics'::text) OR (("encoding")::text = 'native'::text)))
);


--
-- Name: TABLE "Formats"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON TABLE "Formats" IS 'Formats des images (PNG, GIF, JPEG, etc...).';


--
-- Name: COLUMN "Formats".name; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Formats".name IS 'Nom identifiant le format.';


--
-- Name: COLUMN "Formats".mime; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Formats".mime IS 'Nom MIME du format.';


--
-- Name: COLUMN "Formats"."encoding"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Formats"."encoding" IS 'Encodage des données de l''image: "geophysics" ou "native".';


--
-- Name: CONSTRAINT "Format_type" ON "Formats"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "Format_type" ON "Formats" IS 'Enumération des valeurs acceptables.';


--
-- Name: SampleDimensions; Type: TABLE; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE TABLE "SampleDimensions" (
    identifier character varying(20) NOT NULL,
    format character varying(60) NOT NULL,
    band smallint DEFAULT 1 NOT NULL,
    units character varying(20) DEFAULT ''::character varying NOT NULL,
    CONSTRAINT "Positive_band" CHECK ((band >= 1))
);


--
-- Name: TABLE "SampleDimensions"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON TABLE "SampleDimensions" IS 'Descriptions des bandes comprises dans chaque format d''images.';


--
-- Name: COLUMN "SampleDimensions".identifier; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "SampleDimensions".identifier IS 'Nom unique identifiant la bande.';


--
-- Name: COLUMN "SampleDimensions".format; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "SampleDimensions".format IS 'Format qui contient cette bande.';


--
-- Name: COLUMN "SampleDimensions".band; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "SampleDimensions".band IS 'Numéro de la bande (à partir de 1).';


--
-- Name: COLUMN "SampleDimensions".units; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "SampleDimensions".units IS 'Unités des mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';


--
-- Name: CONSTRAINT "Positive_band" ON "SampleDimensions"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "Positive_band" ON "SampleDimensions" IS 'Le numéro de bande doit être positif.';


--
-- Name: CategoriesDetails; Type: VIEW; Schema: postgrid; Owner: -
--

CREATE VIEW "CategoriesDetails" AS
    SELECT "Formats".name AS format, "SampleDimensions".units, "Categories".name, "SampleDimensions".band, "Categories".lower, "Categories".upper, "Categories".c0, "Categories".c1, "Categories"."function", "Formats"."encoding" AS "type", "Categories".colors FROM (("Formats" JOIN "SampleDimensions" ON ((("SampleDimensions".format)::text = ("Formats".name)::text))) JOIN "Categories" ON ((("SampleDimensions".identifier)::text = ("Categories".band)::text))) ORDER BY "Formats".name, "SampleDimensions".band, "Categories".lower;


--
-- Name: VIEW "CategoriesDetails"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON VIEW "CategoriesDetails" IS 'Liste des catégories et des noms de formats dans la même table.';


--
-- Name: GridCoverages; Type: TABLE; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE TABLE "GridCoverages" (
    series character(4) NOT NULL,
    filename character varying(30) NOT NULL,
    "startTime" timestamp without time zone,
    "endTime" timestamp without time zone,
    extent character varying(8) NOT NULL,
    CONSTRAINT "TemporalExtent_range" CHECK (((("startTime" IS NULL) AND ("endTime" IS NULL)) OR ((("startTime" IS NOT NULL) AND ("endTime" IS NOT NULL)) AND ("startTime" < "endTime"))))
);
ALTER TABLE ONLY "GridCoverages" ALTER COLUMN series SET STORAGE PLAIN;


--
-- Name: TABLE "GridCoverages"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON TABLE "GridCoverages" IS 'Liste de toutes les images disponibles. Chaque enregistrement correspond à un fichier d''image.';


--
-- Name: COLUMN "GridCoverages".series; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "GridCoverages".series IS 'Série à laquelle appartient l''image.';


--
-- Name: COLUMN "GridCoverages".filename; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "GridCoverages".filename IS 'Nom du fichier contenant l''image.';


--
-- Name: COLUMN "GridCoverages"."startTime"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "GridCoverages"."startTime" IS 'Date et heure du début de l''acquisition de l''image, en heure universelle (UTC). Dans le cas des moyennes, cette date correspond au début de l''intervalle de temps ayant servit à établir la moyenne.';


--
-- Name: COLUMN "GridCoverages"."endTime"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "GridCoverages"."endTime" IS 'Date et heure de la fin de l''acquisition de l''image, en heure universelle (UTC). Cette date doit être supérieure à la date de début d''acquisition; une valeur égale ne suffit pas.';


--
-- Name: COLUMN "GridCoverages".extent; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "GridCoverages".extent IS 'Coordonnées de la région géographique couverte par l''image, ainsi que sa résolution approximative. ';


--
-- Name: CONSTRAINT "TemporalExtent_range" ON "GridCoverages"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "TemporalExtent_range" ON "GridCoverages" IS 'Les dates de début et de fin doivent être nulles ou non-nulles en même temps, et la date de début doit être inférieure à la date de fin.';


--
-- Name: GridGeometries; Type: TABLE; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE TABLE "GridGeometries" (
    id character varying(8) NOT NULL,
    "spatialExtent" box3d NOT NULL,
    "CRS" character varying DEFAULT 'CRS:84'::character varying NOT NULL,
    width integer NOT NULL,
    height integer NOT NULL,
    depth integer DEFAULT 1 NOT NULL,
    altitudes double precision[],
    CONSTRAINT "GridCoverageAltitudes" CHECK (((altitudes IS NULL) OR (((array_upper(altitudes, 1) - array_lower(altitudes, 1)) + 1) = depth))),
    CONSTRAINT "GridCoverageSize" CHECK (((width > 0) AND (height > 0)))
);


--
-- Name: TABLE "GridGeometries"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON TABLE "GridGeometries" IS 'Envelope spatiales des images ainsi que la dimension de leurs grilles.';


--
-- Name: COLUMN "GridGeometries".id; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "GridGeometries".id IS 'Identifiant unique.';


--
-- Name: COLUMN "GridGeometries"."spatialExtent"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "GridGeometries"."spatialExtent" IS 'Étendue spatiale à 3 dimensions.';


--
-- Name: COLUMN "GridGeometries"."CRS"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "GridGeometries"."CRS" IS 'Identifiant du système de référence des coordonnées. Le CRS peut avoir jusqu''à 4 dimensions (x,y,z,t).';


--
-- Name: COLUMN "GridGeometries".width; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "GridGeometries".width IS 'Nombre de pixels en largeur dans l''image.';


--
-- Name: COLUMN "GridGeometries".height; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "GridGeometries".height IS 'Nombre de pixels en hauteur dans l''image.';


--
-- Name: COLUMN "GridGeometries".depth; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "GridGeometries".depth IS 'Nombre de pixels en profondeur dans l''image, si elle est à trois dimensions.';


--
-- Name: COLUMN "GridGeometries".altitudes; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "GridGeometries".altitudes IS 'Valeurs z de chacunes des couches d''une image 3D.';


--
-- Name: CONSTRAINT "GridCoverageAltitudes" ON "GridGeometries"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "GridCoverageAltitudes" ON "GridGeometries" IS 'La longueur du tableau d''altitudes doit correspondre à la valeur ''depth'' déclarée.';


--
-- Name: CONSTRAINT "GridCoverageSize" ON "GridGeometries"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "GridCoverageSize" ON "GridGeometries" IS 'Les dimensions des images doivent être positives.';


--
-- Name: Layers; Type: TABLE; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE TABLE "Layers" (
    name character varying(60) NOT NULL,
    thematic character varying(50) NOT NULL,
    period double precision,
    fallback character varying(60),
    description text
);


--
-- Name: TABLE "Layers"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON TABLE "Layers" IS 'Ensemble de séries d''images appartenant à une même thématique.';


--
-- Name: COLUMN "Layers".name; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Layers".name IS 'Nom identifiant la couche.';


--
-- Name: COLUMN "Layers".thematic; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Layers".thematic IS 'Paramètre géophysique (ou thème) de cette couche.';


--
-- Name: COLUMN "Layers".period; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Layers".period IS 'Nombre de jours prévus entre deux image. Cette information peut être approximative ou laissée blanc si elle ne s''applique pas.';


--
-- Name: COLUMN "Layers".fallback; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Layers".fallback IS 'Couche de rechange proposée si aucune donnée n''est disponible pour la couche courante.';


--
-- Name: COLUMN "Layers".description; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Layers".description IS 'Remarques s''appliquant à la couche.';


--
-- Name: OperationParameters; Type: TABLE; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE TABLE "OperationParameters" (
    operation character varying NOT NULL,
    parameter character varying NOT NULL,
    value character varying NOT NULL
);


--
-- Name: TABLE "OperationParameters"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON TABLE "OperationParameters" IS 'Valeur des paramètres des opérations d''images.';


--
-- Name: COLUMN "OperationParameters".operation; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "OperationParameters".operation IS 'Nom de l''opération sur lequel s''appliquera un ou plusieurs paramètres.';


--
-- Name: COLUMN "OperationParameters".parameter; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "OperationParameters".parameter IS 'Nom du paramètre, tel que déclaré dans CoverageProcessor ou JAI.';


--
-- Name: COLUMN "OperationParameters".value; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "OperationParameters".value IS 'Valeur du paramètre.';


--
-- Name: Operations; Type: TABLE; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE TABLE "Operations" (
    name character varying(50) NOT NULL,
    description text,
    prefix character(4) NOT NULL,
    operation character varying(20),
    "kernelSize" smallint DEFAULT 1
);
ALTER TABLE ONLY "Operations" ALTER COLUMN prefix SET STORAGE PLAIN;


--
-- Name: TABLE "Operations"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON TABLE "Operations" IS 'Opérations mathématique ayant servit à produire les images.';


--
-- Name: COLUMN "Operations".name; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Operations".name IS 'Nom identifiant l''opération.';


--
-- Name: COLUMN "Operations".description; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Operations".description IS 'Description de l''opération.';


--
-- Name: COLUMN "Operations".prefix; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Operations".prefix IS 'Préfix à utiliser dans les noms composites. Les noms composites seront de la forme "operation - paramètre - temps", par exemple "∇SST₋₁₅".';


--
-- Name: COLUMN "Operations".operation; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Operations".operation IS 'Nom OpenGIS ou JAI. identifiant l''opération. Ce nom sera transmis en argument à la méthode "GridCoverageProcessor.doOperation(...)".';


--
-- Name: COLUMN "Operations"."kernelSize"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Operations"."kernelSize" IS 'Nombre de pixels selon x et y nécessaire à l''application de l''opération.';


--
-- Name: Series; Type: TABLE; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE TABLE "Series" (
    identifier character(4) NOT NULL,
    layer character varying(60) NOT NULL,
    pathname character varying(75) NOT NULL,
    extension character varying(16) NOT NULL,
    format character varying(60) NOT NULL,
    visible boolean DEFAULT true NOT NULL,
    quicklook character(4)
);
ALTER TABLE ONLY "Series" ALTER COLUMN identifier SET STORAGE PLAIN;
ALTER TABLE ONLY "Series" ALTER COLUMN quicklook SET STORAGE PLAIN;


--
-- Name: TABLE "Series"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON TABLE "Series" IS 'Séries d''images. Chaque images appartient à une série.';


--
-- Name: COLUMN "Series".identifier; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Series".identifier IS 'Identifiant unique de la séries.';


--
-- Name: COLUMN "Series".layer; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Series".layer IS 'Couche à laquelle appartiennent les images de cette série.';


--
-- Name: COLUMN "Series".pathname; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Series".pathname IS 'Chemins relatifs des fichiers du groupe. La racine à ces chemins ne doit pas être spécifiée si elle peut varier d''une plateforme à l''autre.';


--
-- Name: COLUMN "Series".extension; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Series".extension IS 'Extension des fichiers d''images de cette série.';


--
-- Name: COLUMN "Series".format; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Series".format IS 'Format des images de ce groupe.';


--
-- Name: COLUMN "Series".visible; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Series".visible IS 'Indique si les images de ce groupe doivent apparaître dans la liste des images proposées à l''utilisateur.';


--
-- Name: COLUMN "Series".quicklook; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Series".quicklook IS 'Série dont les images sont des aperçus de cette série.';


--
-- Name: Thematics; Type: TABLE; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE TABLE "Thematics" (
    name character varying(40) NOT NULL,
    description text NOT NULL
);


--
-- Name: TABLE "Thematics"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON TABLE "Thematics" IS 'Paramètres géophysiques représentés par les images (température, hauteur de l''eau...).';


--
-- Name: COLUMN "Thematics".name; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Thematics".name IS 'Nom identifiant le paramètre géophysique.';


--
-- Name: COLUMN "Thematics".description; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON COLUMN "Thematics".description IS 'Description du paramètre géophysique.';


--
-- Name: Categories_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "Categories"
    ADD CONSTRAINT "Categories_pkey" PRIMARY KEY (name, band);


--
-- Name: Formats_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "Formats"
    ADD CONSTRAINT "Formats_pkey" PRIMARY KEY (name);


--
-- Name: GridCoverages_extent; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "GridCoverages_extent" UNIQUE (series, "startTime", "endTime", extent);


--
-- Name: CONSTRAINT "GridCoverages_extent" ON "GridCoverages"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "GridCoverages_extent" ON "GridCoverages" IS 'L''envelope de l''image doit être unique dans chaque série.';


--
-- Name: GridCoverages_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "GridCoverages_pkey" PRIMARY KEY (series, filename);


--
-- Name: GridCoverages_uniqueness; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "GridCoverages_uniqueness" UNIQUE (series, filename);


--
-- Name: CONSTRAINT "GridCoverages_uniqueness" ON "GridCoverages"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "GridCoverages_uniqueness" ON "GridCoverages" IS 'Le nom du fichier doit être unique pour chaque série.';


--
-- Name: GridGeometries_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "GridGeometries"
    ADD CONSTRAINT "GridGeometries_pkey" PRIMARY KEY (id);


--
-- Name: Layers_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "Layers"
    ADD CONSTRAINT "Layers_pkey" PRIMARY KEY (name);


--
-- Name: OperationParameters_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "OperationParameters"
    ADD CONSTRAINT "OperationParameters_pkey" PRIMARY KEY (operation, parameter);


--
-- Name: Operations_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "Operations"
    ADD CONSTRAINT "Operations_pkey" PRIMARY KEY (name);


--
-- Name: Prefix_uniqueness; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "Operations"
    ADD CONSTRAINT "Prefix_uniqueness" UNIQUE (prefix);


--
-- Name: Quicklook_uniqueness; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Quicklook_uniqueness" UNIQUE (quicklook);


--
-- Name: CONSTRAINT "Quicklook_uniqueness" ON "Series"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "Quicklook_uniqueness" ON "Series" IS 'Chaque série a une seule autre série d''aperçus.';


--
-- Name: SampleDimension_uniqueness; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "SampleDimensions"
    ADD CONSTRAINT "SampleDimension_uniqueness" UNIQUE (format, band);


--
-- Name: SampleDimensions_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "SampleDimensions"
    ADD CONSTRAINT "SampleDimensions_pkey" PRIMARY KEY (identifier);


--
-- Name: Series_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Series_pkey" PRIMARY KEY (identifier);


--
-- Name: Thematics_pkey; Type: CONSTRAINT; Schema: postgrid; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "Thematics"
    ADD CONSTRAINT "Thematics_pkey" PRIMARY KEY (name);


--
-- Name: Band_index; Type: INDEX; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE INDEX "Band_index" ON "SampleDimensions" USING btree (band);


--
-- Name: INDEX "Band_index"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON INDEX "Band_index" IS 'Classement des bandes dans leur ordre d''apparition.';


--
-- Name: EndTime_index; Type: INDEX; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE INDEX "EndTime_index" ON "GridCoverages" USING btree ("endTime");


--
-- Name: INDEX "EndTime_index"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON INDEX "EndTime_index" IS 'Recherche d''images par leur date de fin d''acquisition.';


--
-- Name: Extent_index; Type: INDEX; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE INDEX "Extent_index" ON "GridCoverages" USING btree (extent);


--
-- Name: Format_index; Type: INDEX; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE INDEX "Format_index" ON "SampleDimensions" USING btree (format);


--
-- Name: Layers_index; Type: INDEX; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE INDEX "Layers_index" ON "Series" USING btree (layer);


--
-- Name: SampleDimension_index; Type: INDEX; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE INDEX "SampleDimension_index" ON "Categories" USING btree (band);


--
-- Name: INDEX "SampleDimension_index"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON INDEX "SampleDimension_index" IS 'Recherche des catégories appartenant à une bande.';


--
-- Name: Series_index; Type: INDEX; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE INDEX "Series_index" ON "GridCoverages" USING btree (series);


--
-- Name: StartTime_index; Type: INDEX; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE INDEX "StartTime_index" ON "GridCoverages" USING btree ("startTime");


--
-- Name: INDEX "StartTime_index"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON INDEX "StartTime_index" IS 'Recherche d''images par leur date de début d''acquisition.';


--
-- Name: Time_index; Type: INDEX; Schema: postgrid; Owner: -; Tablespace: 
--

CREATE INDEX "Time_index" ON "GridCoverages" USING btree ("startTime", "endTime");


--
-- Name: INDEX "Time_index"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON INDEX "Time_index" IS 'Recherche de toutes les images à l''intérieur d''une certaine plage de temps.';


--
-- Name: Fallback_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: -
--

ALTER TABLE ONLY "Layers"
    ADD CONSTRAINT "Fallback_reference" FOREIGN KEY (fallback) REFERENCES "Layers"(name) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Fallback_reference" ON "Layers"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "Fallback_reference" ON "Layers" IS 'Chaque couche de second recours doit exister.';


--
-- Name: Format_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: -
--

ALTER TABLE ONLY "SampleDimensions"
    ADD CONSTRAINT "Format_reference" FOREIGN KEY (format) REFERENCES "Formats"(name) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Format_reference" ON "SampleDimensions"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "Format_reference" ON "SampleDimensions" IS 'Chaque bande fait partie de la description d''une image.';


--
-- Name: Format_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: -
--

ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Format_reference" FOREIGN KEY (format) REFERENCES "Formats"(name) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Format_reference" ON "Series"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "Format_reference" ON "Series" IS 'Toutes les images d''une même série utilisent un même séries.';


--
-- Name: GridGeometry_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: -
--

ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "GridGeometry_reference" FOREIGN KEY (extent) REFERENCES "GridGeometries"(id);


--
-- Name: CONSTRAINT "GridGeometry_reference" ON "GridCoverages"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "GridGeometry_reference" ON "GridCoverages" IS 'Chaque images doit avoir une étendue spatiale.';


--
-- Name: Operation_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: -
--

ALTER TABLE ONLY "OperationParameters"
    ADD CONSTRAINT "Operation_reference" FOREIGN KEY (operation) REFERENCES "Operations"(name) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: Phenomenon_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: -
--

ALTER TABLE ONLY "Layers"
    ADD CONSTRAINT "Phenomenon_reference" FOREIGN KEY (thematic) REFERENCES "Thematics"(name) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Phenomenon_reference" ON "Layers"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "Phenomenon_reference" ON "Layers" IS 'Chaque couche représente les données observées pour un phénomène (ou thème).';


--
-- Name: Quicklook_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: -
--

ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Quicklook_reference" FOREIGN KEY (quicklook) REFERENCES "Series"(identifier) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Quicklook_reference" ON "Series"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "Quicklook_reference" ON "Series" IS 'Les aperçus s''appliquent à une autre séries d''images.';


--
-- Name: SampleDimension_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: -
--

ALTER TABLE ONLY "Categories"
    ADD CONSTRAINT "SampleDimension_reference" FOREIGN KEY (band) REFERENCES "SampleDimensions"(identifier) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "SampleDimension_reference" ON "Categories"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "SampleDimension_reference" ON "Categories" IS 'Chaque catégorie est un élément de la description d''une bande.';


--
-- Name: Series_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: -
--

ALTER TABLE ONLY "Series"
    ADD CONSTRAINT "Series_reference" FOREIGN KEY (layer) REFERENCES "Layers"(name) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Series_reference" ON "Series"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "Series_reference" ON "Series" IS 'Chaque série appartient à une couche.';


--
-- Name: Series_reference; Type: FK CONSTRAINT; Schema: postgrid; Owner: -
--

ALTER TABLE ONLY "GridCoverages"
    ADD CONSTRAINT "Series_reference" FOREIGN KEY (series) REFERENCES "Series"(identifier) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: CONSTRAINT "Series_reference" ON "GridCoverages"; Type: COMMENT; Schema: postgrid; Owner: -
--

COMMENT ON CONSTRAINT "Series_reference" ON "GridCoverages" IS 'Chaque image appartient à une série.';


--
-- PostgreSQL database dump complete
--

