--------------------------------------------------------------------------------------------------
-- Optional tables for PostGrid.                                                                --
--------------------------------------------------------------------------------------------------

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;
SET default_tablespace = '';
SET default_with_oids = false;
SET search_path = postgrid, postgis, pg_catalog;




--------------------------------------------------------------------------------------------------
-- Creates the "Operations" table.                                                              --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Operations" (
    "name"        character varying NOT NULL,
    "prefix"      character varying NOT NULL,
    "operation"   character varying,
    "kernelSize"  smallint DEFAULT 1,
    "description" text
);

ALTER TABLE "Operations" OWNER TO geoadmin;
GRANT ALL ON TABLE "Operations" TO geoadmin;
GRANT SELECT ON TABLE "Operations" TO PUBLIC;

ALTER TABLE ONLY "Operations"
    ADD CONSTRAINT "Operations_pkey" PRIMARY KEY (name);
ALTER TABLE ONLY "Operations"
    ADD CONSTRAINT "Prefix_uniqueness" UNIQUE (prefix);

COMMENT ON TABLE "Operations" IS
    'Opérations mathématique ayant servit à produire les images.';
COMMENT ON COLUMN "Operations"."name" IS
    'Nom identifiant l''opération.';
COMMENT ON COLUMN "Operations"."prefix" IS
    'Préfix à utiliser dans les noms composites. Les noms composites seront de la forme "operation - paramètre - temps", par exemple "∇SST₋₁₅".';
COMMENT ON COLUMN "Operations"."operation" IS
    'Nom OpenGIS ou JAI. identifiant l''opération. Ce nom sera transmis en argument à la méthode "GridCoverageProcessor.doOperation(...)".';
COMMENT ON COLUMN "Operations"."kernelSize" IS
    'Nombre de pixels selon x et y nécessaire à l''application de l''opération.';
COMMENT ON COLUMN "Operations"."description" IS
    'Description optionnelle de l''opération.';




--------------------------------------------------------------------------------------------------
-- Creates the "OperationParameters" table.                                                     --
-- Dependencies: "Operations"                                                                   --
--------------------------------------------------------------------------------------------------

CREATE TABLE "OperationParameters" (
    "operation" character varying NOT NULL,
    "parameter" character varying NOT NULL,
    "value" character varying NOT NULL
);

ALTER TABLE "OperationParameters" OWNER TO geoadmin;
GRANT ALL ON TABLE "OperationParameters" TO geoadmin;
GRANT SELECT ON TABLE "OperationParameters" TO PUBLIC;

ALTER TABLE ONLY "OperationParameters"
    ADD CONSTRAINT "OperationParameters_pkey" PRIMARY KEY ("operation", "parameter");
ALTER TABLE ONLY "OperationParameters"
    ADD CONSTRAINT "Operation_reference" FOREIGN KEY ("operation") REFERENCES "Operations"("name")
    ON UPDATE CASCADE ON DELETE CASCADE;

COMMENT ON TABLE "OperationParameters" IS
    'Valeur des paramètres des opérations d''images.';
COMMENT ON COLUMN "OperationParameters"."operation" IS
    'Nom de l''opération sur lequel s''appliquera un ou plusieurs paramètres.';
COMMENT ON COLUMN "OperationParameters"."parameter" IS
    'Nom du paramètre, tel que déclaré dans CoverageProcessor ou JAI.';
COMMENT ON COLUMN "OperationParameters"."value" IS
    'Valeur du paramètre.';




--------------------------------------------------------------------------------------------------
-- Creates the "RegionOfInterests" table.                                                       --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "RegionOfInterests" (
    "name" character varying NOT NULL,
    "dx"   double precision  NOT NULL,
    "dy"   double precision  NOT NULL,
    "dz"   double precision  NOT NULL,
    "dt"   double precision  NOT NULL
);

ALTER TABLE "RegionOfInterests" OWNER TO geoadmin;
GRANT ALL ON TABLE "RegionOfInterests" TO geoadmin;
GRANT SELECT ON TABLE "RegionOfInterests" TO PUBLIC;

ALTER TABLE ONLY "RegionOfInterests"
    ADD CONSTRAINT "LocationOffsets_pkey" PRIMARY KEY (name);
CREATE INDEX dt_index ON "RegionOfInterests" USING btree (dt);

COMMENT ON TABLE "RegionOfInterests" IS
    'Positions spatio-temporelles relatives à des observations.';
COMMENT ON COLUMN "RegionOfInterests"."name" IS
    'Nom unique identifiant cette position relative.';
COMMENT ON COLUMN "RegionOfInterests"."dx" IS
    'Décalage Est-Ouest, en mètres.';
COMMENT ON COLUMN "RegionOfInterests"."dy" IS
    'Décalage Nord-Sud, en mètres.';
COMMENT ON COLUMN "RegionOfInterests"."dz" IS
    'Décalage vertical, en mètres.';
COMMENT ON COLUMN "RegionOfInterests"."dt" IS
    'Décalage temporel, en nombre de jours.';




--------------------------------------------------------------------------------------------------
-- Creates the "Distributions" table.                                                           --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Distributions" (
    "name"   character varying NOT NULL,
    "scale"  double precision DEFAULT 1 NOT NULL,
    "offset" double precision DEFAULT 0 NOT NULL,
    "log"    boolean DEFAULT false NOT NULL
);

ALTER TABLE "Distributions" OWNER TO geoadmin;
GRANT ALL ON TABLE "Distributions" TO geoadmin;
GRANT SELECT ON TABLE "Distributions" TO PUBLIC;

ALTER TABLE ONLY "Distributions"
    ADD CONSTRAINT "Distributions_pkey" PRIMARY KEY ("name");

COMMENT ON TABLE "Distributions" IS
    'Distributions approximatives (normale, log-normale...) des descripteurs.';
COMMENT ON COLUMN "Distributions"."name" IS
    'Nom de la distribution.';
COMMENT ON COLUMN "Distributions"."scale" IS
    'Facteur par lequel multiplier les valeurs avant l''analyse statistiques. Utile surtout si le logarithme doit être calculé.';
COMMENT ON COLUMN "Distributions"."offset" IS
    'Constantes à ajouter aux valeurs avant l''analyse statistiques. Utile surtout si le logarithme doit être calculé.';
COMMENT ON COLUMN "Distributions"."log" IS
    'Indique si les analyses statistiques doivent se faire sur le logarithme des valeurs transformées. La transformation complète sera alors x''=log(x*scale + offset), ou "log" est le logarithme népérien.';




--------------------------------------------------------------------------------------------------
-- Creates the "Descriptors" table.                                                             --
-- Dependencies: "Operations", "Distributions"                                                  --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Descriptors" (
    "identifier"   smallint NOT NULL,
    "symbol"       character varying NOT NULL,
    "layer"        character varying NOT NULL,
    "operation"    character varying DEFAULT 'Valeur' NOT NULL,
    "region"       character varying DEFAULT '+00' NOT NULL,
    "band"         smallint DEFAULT 1 NOT NULL,
    "distribution" character varying DEFAULT 'normale' NOT NULL,
    CONSTRAINT "Band_check" CHECK (band >= 1)
);

ALTER TABLE "Descriptors" OWNER TO geoadmin;
GRANT ALL ON TABLE "Descriptors" TO geoadmin;
GRANT SELECT ON TABLE "Descriptors" TO PUBLIC;

ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "Descriptors_pkey" PRIMARY KEY ("identifier");
ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "Symbol_uniqueness" UNIQUE ("symbol");
ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "Descriptor_uniqueness" UNIQUE ("layer", "region", "band", "operation");
ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "Layer_reference" FOREIGN KEY ("layer") REFERENCES "Layers"("name")
    ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "Operation_reference" FOREIGN KEY ("operation") REFERENCES "Operations"("name")
    ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "ROI_reference" FOREIGN KEY ("region") REFERENCES "RegionOfInterests"("name")
    ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY "Descriptors"
    ADD CONSTRAINT "Distribution_reference" FOREIGN KEY (distribution) REFERENCES "Distributions"("name")
    ON UPDATE CASCADE ON DELETE RESTRICT;

CREATE INDEX "Symbol_index"          ON "Descriptors" ("symbol");
CREATE INDEX "Phenomenons_index"     ON "Descriptors" ("layer");
CREATE INDEX "Operations_index"      ON "Descriptors" ("operation");
CREATE INDEX "LocationOffsets_index" ON "Descriptors" ("region");
CREATE INDEX "Distributions_index"   ON "Descriptors" ("distribution");

COMMENT ON TABLE "Descriptors" IS
    'Descripteurs du paysage océanique, chacun étant une combinaison d''une couche, d''une opération et d''un décalage spatio-temporel.';
COMMENT ON COLUMN "Descriptors"."identifier" IS
    'Clé primaire identifiant ce descripteur du paysage océanique.';
COMMENT ON COLUMN "Descriptors"."symbol" IS
    'Symbole unique identifiant ce descripteur, pour une lecture plus humaine que le numéro d''identifiant.';
COMMENT ON COLUMN "Descriptors"."layer" IS
    'Phénomène (température, chlorophylle...) étudié par ce descripteur.';
COMMENT ON COLUMN "Descriptors"."operation" IS
    'Opération appliquée sur les mesures du phénomène pour obtenir le descripteur (exemple: opérateur de gradient).';
COMMENT ON COLUMN "Descriptors"."region" IS
    'Décalage spatio-temporelle entre la position de l''observation et celle à laquelle sera évaluée le descripteur.';
COMMENT ON COLUMN "Descriptors"."band" IS
    'Numéro (à partir de 1) de la bande à prendre en compte.';
COMMENT ON COLUMN "Descriptors"."distribution" IS
    'Distribution approximative des données. La distribution "Amplitude" résulte d''une combinaison de distributions normales de la forme x²+y². Les distributions normales ne sont généralement pas indépendantes, ce qui distingue cette distribution de X².';
COMMENT ON CONSTRAINT "Band_check" ON "Descriptors" IS
    'Les numéros de bandes doivent être des entiers positifs non-nuls.';
COMMENT ON CONSTRAINT "Layer_reference" ON "Descriptors" IS
    'Chaque descripteur concerne un phénomène.';
COMMENT ON CONSTRAINT "Operation_reference" ON "Descriptors" IS
    'Chaque descripteur est le résultat d''une certaine opération appliquée sur les données du phénomène observé.';
COMMENT ON CONSTRAINT "ROI_reference" ON "Descriptors" IS
    'Chaque descripteur peut être évalué à une position spatio-temporelle décalée par rapport à la position de la station.';
COMMENT ON CONSTRAINT "Distribution_reference" ON "Descriptors" IS
    'Chaque descripteur possède des valeurs suivant une loi de distribution.';




--------------------------------------------------------------------------------------------------
-- Creates the "LinearModelTerms" table.                                                        --
-- Dependencies: "Descriptors"                                                                  --
--------------------------------------------------------------------------------------------------

CREATE TABLE "LinearModelTerms" (
    "target"      character varying NOT NULL,
    "source1"     character varying NOT NULL,
    "source2"     character varying DEFAULT '①' NOT NULL,
    "coefficient" double precision NOT NULL
);

ALTER TABLE "LinearModelTerms" OWNER TO geoadmin;
GRANT ALL ON TABLE "LinearModelTerms" TO geoadmin;
GRANT SELECT ON TABLE "LinearModelTerms" TO PUBLIC;

ALTER TABLE ONLY "LinearModelTerms"
    ADD CONSTRAINT "LinearModels_pkey" PRIMARY KEY ("target", "source1", "source2");
ALTER TABLE ONLY "LinearModelTerms"
    ADD CONSTRAINT "ExplainedVariable_reference" FOREIGN KEY (target) REFERENCES "Layers"("name")
    ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY "LinearModelTerms"
    ADD CONSTRAINT "Descriptor1_reference" FOREIGN KEY ("source1") REFERENCES "Descriptors"("symbol")
    ON UPDATE CASCADE ON DELETE RESTRICT;
ALTER TABLE ONLY "LinearModelTerms"
    ADD CONSTRAINT "Descriptor2_reference" FOREIGN KEY ("source2") REFERENCES "Descriptors"("symbol")
    ON UPDATE CASCADE ON DELETE RESTRICT;

CREATE INDEX "Descriptors1_index" ON "LinearModelTerms" ("source1");
CREATE INDEX "Descriptors2_index" ON "LinearModelTerms" ("source2");

COMMENT ON TABLE "LinearModelTerms" IS
    'Poids à donner aux différentes combinaisons de descripteurs pour calculer une nouvelle image.';
COMMENT ON COLUMN "LinearModelTerms"."target" IS
    'Couche d''images pour laquelle ce modèle linéaire effectue ses calculs.';
COMMENT ON COLUMN "LinearModelTerms"."source1" IS
    'Premier descripteur entrant dans le terme.';
COMMENT ON COLUMN "LinearModelTerms"."source2" IS
    'Deuxième descripteur entrant dans le terme. S''il n''est pas le descripteur identité, il sera multiplié par le premier descripteur.';
COMMENT ON COLUMN "LinearModelTerms"."coefficient" IS
    'Facteur par lequel multiplier le terme du modèle linéaire.';
COMMENT ON CONSTRAINT "ExplainedVariable_reference" ON "LinearModelTerms" IS
    'La variable à expliquer doit être une série d''images.';
COMMENT ON CONSTRAINT "Descriptor1_reference" ON "LinearModelTerms" IS
    'Le premier terme doit être un des descripteurs du paysage océanique.';
COMMENT ON CONSTRAINT "Descriptor2_reference" ON "LinearModelTerms" IS
    'Le second terme doit être un des descripteurs du paysage océanique.';




--------------------------------------------------------------------------------------------------
-- Function to be applied on new records in the "LinearModelTerms" table.                       --
--------------------------------------------------------------------------------------------------

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

ALTER FUNCTION "ReplaceModelDescriptors"() OWNER TO geoadmin;
GRANT ALL ON FUNCTION "ReplaceModelDescriptors"() TO geoadmin;
GRANT EXECUTE ON FUNCTION "ReplaceModelDescriptors"() TO PUBLIC;

CREATE TRIGGER "ReplaceModelDescriptors_trigger"
    BEFORE INSERT OR UPDATE ON "LinearModelTerms"
    FOR EACH ROW
    EXECUTE PROCEDURE "ReplaceModelDescriptors"();

COMMENT ON TRIGGER "ReplaceModelDescriptors_trigger" ON "LinearModelTerms" IS
    'Remplace les identifieurs numériques par leurs symboles pour les colonnes "descriptors".';
