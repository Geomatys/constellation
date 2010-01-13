 INSERT INTO "Schemas"."Standard"  VALUES ('Sensor Web Enablement','swe');
 INSERT INTO "Schemas"."Standard"  VALUES ('SensorML','sml');
 INSERT INTO "Schemas"."Standard"  VALUES ('XML Schema','xsi');
 INSERT INTO "Schemas"."Standard"  VALUES ('ISO 19115','gmd');
 INSERT INTO "Schemas"."Standard"  VALUES ('ISO 19103','gco');
 INSERT INTO "Schemas"."Standard"  VALUES ('ISO 19108','gml');
 INSERT INTO "Schemas"."Standard"  VALUES ('ISO 636-2', NULL);
 INSERT INTO "Schemas"."Standard"  VALUES ('ISO 19115 FRA 1.0', 'fra');
 INSERT INTO "Schemas"."Standard"  VALUES ('ISO 3166', NULL);
 INSERT INTO "Schemas"."Standard"  VALUES ('MDWEB', NULL);
 INSERT INTO "Schemas"."Standard"  VALUES ('Xlink', 'xlink');

INSERT INTO "Schemas"."Obligations" ("code", "name") VALUES ('O', 'Optionnal');
INSERT INTO "Schemas"."Obligations" ("code", "name") VALUES ('M', 'Mandatory');
INSERT INTO "Schemas"."Obligations" ("code", "name") VALUES ('C', 'Conditionnal');

INSERT INTO "Schemas"."Classes"  VALUES ('DateTime',NULL,'ISO 19108',NULL,0,NULL,NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('Date',NULL,'ISO 19103',NULL,0,NULL,NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('Integer',NULL,'ISO 19103',NULL,0,NULL,NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('Double',NULL,'ISO 19103',NULL,0,NULL,NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('CharacterString',NULL,'ISO 19103',NULL,0,NULL,NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('URI',NULL,'ISO 19103',NULL,0,NULL,NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('URL',NULL,'ISO 19103',NULL,0,NULL,NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('ID',NULL,'ISO 19103',NULL,0,NULL,NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('Boolean',NULL,'ISO 19103',NULL,0,NULL,NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('Binary', NULL, 'ISO 19103', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('Decimal', NULL, 'ISO 19103', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('Distance', NULL, 'ISO 19103', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('Real', NULL, 'ISO 19103', NULL, 0, NULL, NULL, ' ');

INSERT INTO "Schemas"."Classes"  VALUES ('UndefinedTime', NULL, 'ISO 19108', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('indeterminatePosition', NULL, 'ISO 19108', NULL, 1, 1, 'UndefinedTime', 'CharacterString', NULL, 'O', 0, 'ISO 19103', 'ISO 19108', 'P');

INSERT INTO "Schemas"."Classes"  VALUES ('TimePeriod', 'TimePeriod', 'ISO 19108', 'TimePeriod', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('beginPosition', NULL, 'ISO 19108', NULL, 1, 1, 'TimePeriod', 'Date', NULL, 'M', 0, 'ISO 19103', 'ISO 19108', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('endPosition', NULL, 'ISO 19108', NULL, 0, 1, 'TimePeriod', 'UndefinedTime', NULL, 'O', 1, 'ISO 19108', 'ISO 19108', ' ');
