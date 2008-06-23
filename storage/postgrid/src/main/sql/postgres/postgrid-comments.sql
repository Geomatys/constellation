--------------------------------------------------------------------------------------------------
-- Optional columns and tables for PostGrid.                                                    --
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
-- Additional columns to the existing tables.                                                   --
--------------------------------------------------------------------------------------------------

ALTER TABLE "Layers" ADD COLUMN description text;
COMMENT ON COLUMN "Layers"."description" IS
    'Description optionnelle de la couche.';




--------------------------------------------------------------------------------------------------
-- Creates the "Thematics" table.                                                               --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Thematics" (
    "name" character varying NOT NULL PRIMARY KEY,
    "description" text
);

ALTER TABLE "Thematics" OWNER TO geoadmin;
GRANT ALL ON TABLE "Thematics" TO geoadmin;
GRANT SELECT ON TABLE "Thematics" TO PUBLIC;

COMMENT ON TABLE "Thematics" IS
    'Paramètres géophysiques représentés par les images (température, hauteur de l''eau...).';
COMMENT ON COLUMN "Thematics"."name" IS
    'Nom identifiant le paramètre géophysique.';
COMMENT ON COLUMN "Thematics"."description" IS
    'Description du paramètre géophysique.';

ALTER TABLE ONLY "Layers"
    ADD CONSTRAINT "Layers_thematic_fkey" FOREIGN KEY ("thematic") REFERENCES "Thematics"
    ON UPDATE CASCADE ON DELETE CASCADE;

COMMENT ON CONSTRAINT "Layers_thematic_fkey" ON "Layers" IS
    'Chaque couche représente les données observées pour une thématique.';




--------------------------------------------------------------------------------------------------
-- Creates the "Procedures" table.                                                              --
-- Dependencies: (none)                                                                         --
--------------------------------------------------------------------------------------------------

CREATE TABLE "Procedures" (
    "name" character varying NOT NULL PRIMARY KEY,
    "description" text
);

ALTER TABLE "Procedures" OWNER TO geoadmin;
GRANT ALL ON TABLE "Procedures" TO geoadmin;
GRANT SELECT ON TABLE "Procedures" TO PUBLIC;

COMMENT ON TABLE "Procedures" IS
    'Procédures utilisées pour effectuer une observation.';
COMMENT ON COLUMN "Procedures".name IS
    'Nom unique identifiant cette procédure.';
COMMENT ON COLUMN "Procedures".description IS
    'Description de la procédure.';

ALTER TABLE ONLY "Layers"
    ADD CONSTRAINT "Layers_procedure_fkey" FOREIGN KEY ("procedure") REFERENCES "Procedures"
    ON UPDATE CASCADE ON DELETE CASCADE;
