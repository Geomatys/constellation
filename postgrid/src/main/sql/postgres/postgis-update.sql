--
-- Update the PostGIS schema.
--
-- For PostGrid installation instructions, see install.html.
--

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;


--
-- Ensures that the SRID of a geometry column is known to PostGIS.
--
ALTER TABLE postgis.geometry_columns
  ADD CONSTRAINT fk_srid FOREIGN KEY (srid)
      REFERENCES postgis.spatial_ref_sys (srid) MATCH SIMPLE
      ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- PostGrid columns to declare in the "geometry_columns" table.
--
INSERT INTO postgis.geometry_columns (f_table_catalog, f_table_schema, f_table_name, f_geometry_column, coord_dimension, srid, "type") VALUES ('', 'postgrid', 'GridGeometries', 'horizontalExtent', 2, 4326, 'POLYGON');


--
-- Some vertical CRS from the EPSG database.
--
INSERT INTO postgis.spatial_ref_sys (srid, auth_name, auth_srid, srtext) VALUES (5798, 'EPSG', 5798, 'VERT_CS["EGM84 geoid",VERT_DATUM["EGM84 geoid",2005,AUTHORITY["EPSG","5203"]],UNIT["m",1.0],AXIS["Gravity-related height",UP],AUTHORITY["EPSG","5798"]]');
INSERT INTO postgis.spatial_ref_sys (srid, auth_name, auth_srid, srtext) VALUES (5773, 'EPSG', 5773, 'VERT_CS["EGM96 geoid",VERT_DATUM["EGM96 geoid",2005,AUTHORITY["EPSG","5171"]],UNIT["m",1.0],AXIS["Gravity-related height",UP],AUTHORITY["EPSG","5773"]]');
INSERT INTO postgis.spatial_ref_sys (srid, auth_name, auth_srid, srtext) VALUES (5714, 'EPSG', 5714, 'VERT_CS["mean sea level height",VERT_DATUM["Mean Sea Level",2005,AUTHORITY["EPSG","5100"]],UNIT["m",1.0],AXIS["Gravity-related height",UP],AUTHORITY["EPSG","5714"]]');
INSERT INTO postgis.spatial_ref_sys (srid, auth_name, auth_srid, srtext) VALUES (5715, 'EPSG', 5715, 'VERT_CS["mean sea level depth",VERT_DATUM["Mean Sea Level",2005,AUTHORITY["EPSG","5100"]],UNIT["m",1.0],AXIS["Gravity-related depth",DOWN],AUTHORITY["EPSG","5715"]]');


--
-- Grants read-only access to everyone.
--
GRANT SELECT ON TABLE postgis.spatial_ref_sys TO public;
