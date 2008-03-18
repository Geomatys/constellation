--
-- Update the PostGIS schema.
--
-- For PostGrid installation instructions, see install.html.
--
--
-- ==========================================================
-- ============  NOTE ON APPARENT POSTGIS ERROR  ============
-- ==========================================================
-- The PostGIS "spatial_ref_sys" table seems to have an error in declaration of Lambert projections
-- used in France. The prime meridian (Paris) should be declared in gradiants, not degrees, because
-- gradiants is the unit declared in the UNIT["grad",0.01570796326794897] element. Consequently the
-- value for PRIMEM["Paris"] should be 2.5969213, not 2.33722917. This apply to EPSG:27572 and other
-- CRS close to it.
--

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;
SET search_path = postgis, pg_catalog;


--
-- Ensures that the SRID of a geometry column is known to PostGIS.
--
ALTER TABLE geometry_columns
  ADD CONSTRAINT fk_srid FOREIGN KEY (srid)
      REFERENCES spatial_ref_sys (srid) MATCH SIMPLE
      ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Some vertical CRS from the EPSG database.
--
INSERT INTO spatial_ref_sys (srid, auth_name, auth_srid, srtext) VALUES (5798, 'EPSG', 5798, 'VERT_CS["EGM84 geoid",VERT_DATUM["EGM84 geoid",2005,AUTHORITY["EPSG","5203"]],UNIT["m",1.0],AXIS["Gravity-related height",UP],AUTHORITY["EPSG","5798"]]');
INSERT INTO spatial_ref_sys (srid, auth_name, auth_srid, srtext) VALUES (5773, 'EPSG', 5773, 'VERT_CS["EGM96 geoid",VERT_DATUM["EGM96 geoid",2005,AUTHORITY["EPSG","5171"]],UNIT["m",1.0],AXIS["Gravity-related height",UP],AUTHORITY["EPSG","5773"]]');
INSERT INTO spatial_ref_sys (srid, auth_name, auth_srid, srtext) VALUES (5714, 'EPSG', 5714, 'VERT_CS["mean sea level height",VERT_DATUM["Mean Sea Level",2005,AUTHORITY["EPSG","5100"]],UNIT["m",1.0],AXIS["Gravity-related height",UP],AUTHORITY["EPSG","5714"]]');
INSERT INTO spatial_ref_sys (srid, auth_name, auth_srid, srtext) VALUES (5715, 'EPSG', 5715, 'VERT_CS["mean sea level depth",VERT_DATUM["Mean Sea Level",2005,AUTHORITY["EPSG","5100"]],UNIT["m",1.0],AXIS["Gravity-related depth",DOWN],AUTHORITY["EPSG","5715"]]');


--
-- Grants read-only access to everyone.
--
GRANT SELECT ON TABLE spatial_ref_sys TO public;
