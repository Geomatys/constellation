--------------------------------------------------------------------------------------------------
--  Inserts data in the "postgrid" schema     .                                                 --
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
-- Fills the "Permissions" table.                                                               --
--------------------------------------------------------------------------------------------------

INSERT INTO "Permissions" ("name", "include", "WMS", "WCS", "description")
  VALUES ('Public', NULL, TRUE, TRUE, 'Data accessible to anyone.');
INSERT INTO "Permissions" ("name", "include", "WMS", "WCS", "description")
  VALUES ('Hidden', NULL, FALSE, FALSE, 'Hidden data (e.g. data reserved for testing purpose only).');
INSERT INTO "Permissions" ("name", "include", "WMS", "WCS", "description")
  VALUES ('Restricted', 'Public', TRUE, TRUE, 'Access to public data together with restricted ones.');
INSERT INTO "Permissions" ("name", "include", "WMS", "WCS", "description")
  VALUES ('Private', NULL, TRUE, TRUE, 'Access to private data.');


--------------------------------------------------------------------------------------------------
-- Fills the "Formats" table.                                                                   --
--------------------------------------------------------------------------------------------------

INSERT INTO "Formats" ("name", "mime", "encoding")
  VALUES ('PNG', 'image/png', 'native');
INSERT INTO "Formats" ("name", "mime", "encoding")
  VALUES ('TIFF', 'image/tiff', 'native');


--------------------------------------------------------------------------------------------------
-- Fills the "Distributions" table.                                                             --
--------------------------------------------------------------------------------------------------

INSERT INTO "Distributions" ("name", "scale", "offset", "log")
  VALUES ('Normal', 1, 0, false);
INSERT INTO "Distributions" ("name", "scale", "offset", "log")
  VALUES ('Log-Normal', 1, 0, true);


--------------------------------------------------------------------------------------------------
-- Fills the "Operations" table.                                                                --
--------------------------------------------------------------------------------------------------

INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Gauss 3×3', 'ℊ₃', 'Convolve', 3, 'Moyenne des pixels sur une matrice de 3×3 pondérée par une gaussienne');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Gauss 5×5', 'ℊ₅', 'Convolve', 5, 'Moyenne des pixels sur une matrice de 5×5 pondérée par une gaussienne');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Gauss 7×7', 'ℊ₇', 'Convolve', 7, 'Moyenne des pixels sur une matrice de 7×7 pondérée par une gaussienne');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Gauss 9×9', 'ℊ₉', 'Convolve', 9, 'Moyenne des pixels sur une matrice de 9×9 pondérée par une gaussienne');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Temporal gradient', '∇⒯', NULL, 1, 'Différence entre les valeurs des pixels de deux images consécutives dans le temps.');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Gradient magnitude 3×3', '∇₃', 'GradientMagnitude', 3, 'Magnitude du gradient calculé à l''aide d''un opérateur isotropique 3×3.');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Gradient magnitude 5×5', '∇₅', 'GradientMagnitude', 5, 'Magnitude du gradient calculé à l''aide d''un opérateur isotropique modifié 5×5.');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Gradient magnitude 7×7', '∇₇', 'GradientMagnitude', 7, 'Magnitude du gradient calculé à l''aide d''un opérateur isotropique modifié 7×7.');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Gradient magnitude 9×9', '∇₉', 'GradientMagnitude', 9, 'Magnitude du gradient calculé à l''aide d''un opérateur isotropique modifié 9×9.');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Mean 3×3', '〈 〉₃', 'Convolve', 3, 'Moyenne des pixels sur une matrice de 3×3');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Mean 5×5', '〈 〉₅', 'Convolve', 5, 'Moyenne des pixels sur une matrice de 5×5');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Mean 7×7', '〈 〉₇', 'Convolve', 7, 'Moyenne des pixels sur une matrice de 7×7');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Mean 9×9', '〈 〉₉', 'Convolve', 9, 'Moyenne des pixels sur une matrice de 9×9');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Value', '', 'NodataFilter', 1, 'Valeur du pixel sans opération autre qu''un filtre sommaire des données manquantes.');
INSERT INTO "Operations" ("name", "prefix", "operation", "kernelSize", "description")
  VALUES ('Direct value', '≞', 'DirectValue', 1, 'Valeur du pixel sans interpolation ni filtre de données manquantes.');


--------------------------------------------------------------------------------------------------
-- Fills the "OperationParameters" table.                                                       --
--------------------------------------------------------------------------------------------------

INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Mean 3×3', 'kernel', 'mean(3)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Mean 5×5', 'kernel', 'mean(5)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Mean 7×7', 'kernel', 'mean(7)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Mean 9×9', 'kernel', 'mean(9)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Gradient magnitude 5×5', 'mask1', 'isotropic.x(5)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Gradient magnitude 5×5', 'mask2', 'isotropic.y(5)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Gradient magnitude 7×7', 'mask1', 'isotropic.x(7)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Gradient magnitude 7×7', 'mask2', 'isotropic.y(7)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Gradient magnitude 9×9', 'mask1', 'isotropic.x(9)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Gradient magnitude 9×9', 'mask2', 'isotropic.y(9)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Gauss 3×3', 'kernel', 'gauss(3)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Gauss 5×5', 'kernel', 'gauss(5)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Gauss 7×7', 'kernel', 'gauss(7)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Gauss 9×9', 'kernel', 'gauss(9)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Gradient magnitude 3×3', 'mask1', 'isotropic.x(3)');
INSERT INTO "OperationParameters" ("operation", "parameter", "value")
  VALUES ('Gradient magnitude 3×3', 'mask2', 'isotropic.y(3)');
