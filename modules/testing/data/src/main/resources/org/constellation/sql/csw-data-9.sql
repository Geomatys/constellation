INSERT INTO "Storage"."Records" ("accessionNumber","recordSet", "identifier", "title", "maintainerID", "validatorID", "profile", "updateDate", "isValidated", "isPublished", "type") VALUES (36, 'CSWCat', 'urn:uuid:1ef30a8b-876d-4828-9246-dcbbyyiioo', 'urn:uuid:1ef30a8b-876d-4828-9246-dcbbyyiioo', 'admin', NULL, NULL, '2009-10-14', 1, 1, 'NORMALRECORD');

INSERT INTO "Storage"."Values" ("form", "path", "ordinal", "type", "typeStandard", "id_value") VALUES (36, 'Catalog Web Service:Record', 1, 'Record', 'Catalog Web Service', 'Catalog Web Service:Record.1'),
                                      (36, 'Catalog Web Service:Record:subject', 1, 'SimpleLiteral', 'DublinCore', 'Catalog Web Service:Record.1:subject.1'),
                                      (36, 'Catalog Web Service:Record:abstract', 1, 'SimpleLiteral', 'DublinCore', 'Catalog Web Service:Record.1:abstract.1'),
                                      (36, 'Catalog Web Service:Record:identifier', 1, 'SimpleLiteral', 'DublinCore', 'Catalog Web Service:Record.1:identifier.1'),
                                      (36, 'Catalog Web Service:Record:type', 1, 'SimpleLiteral', 'DublinCore', 'Catalog Web Service:Record.1:type.1'),
                                      (36, 'Catalog Web Service:Record:BoundingBox', 1, 'BoundingBox', 'OGC Web Service', 'Catalog Web Service:Record.1:BoundingBox.1');


INSERT INTO "Storage"."TextValues" ("form", "path", "ordinal", "type", "typeStandard", "id_value", "value") VALUES (36, 'Catalog Web Service:Record:subject:content', 1, 'CharacterString', 'ISO 19103', 'Catalog Web Service:Record.1:subject.1:content.1', 'Transmittance and attenuance of the water column'),
                                          (36, 'Catalog Web Service:Record:abstract:content', 1, 'CharacterString', 'ISO 19103', 'Catalog Web Service:Record.1:abstract.1:content.1', 'Proin sit amet justo. In justo. Aenean adipiscing nulla id tellus.'),
                                          (36, 'Catalog Web Service:Record:identifier:content', 1, 'CharacterString', 'ISO 19103', 'Catalog Web Service:Record.1:identifier.1:content.1', 'urn:uuid:1ef30a8b-876d-4828-9246-dcbbyyiioo'),
                                          (36, 'Catalog Web Service:Record:type:content', 1, 'CharacterString', 'ISO 19103', 'Catalog Web Service:Record.1:type.1:content.1', 'http://purl.org/dc/dcmitype/Service'),
                                          (36, 'Catalog Web Service:Record:BoundingBox:crs', 1, 'CharacterString', 'ISO 19103', 'Catalog Web Service:Record.1:BoundingBox.1:crs.1', 'EPSG:4326'),
                                          (36, 'Catalog Web Service:Record:BoundingBox:LowerCorner', 1, 'Real', 'ISO 19103', 'Catalog Web Service:Record.1:BoundingBox.1:LowerCorner.1', '60.042'),
                                          (36, 'Catalog Web Service:Record:BoundingBox:LowerCorner', 2, 'Real', 'ISO 19103', 'Catalog Web Service:Record.1:BoundingBox.1:LowerCorner.2', '13.754'),
                                          (36, 'Catalog Web Service:Record:BoundingBox:UpperCorner', 1, 'Real', 'ISO 19103', 'Catalog Web Service:Record.1:BoundingBox.1:UpperCorner.1', '68.41'),
                                          (36, 'Catalog Web Service:Record:BoundingBox:UpperCorner', 2, 'Real', 'ISO 19103', 'Catalog Web Service:Record.1:BoundingBox.1:UpperCorner.2', '17.92');
