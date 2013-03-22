
INSERT INTO "Storage"."Records" ("accessionNumber","recordSet", "identifier", "title", "maintainerID", "validatorID", "profile", "updateDate", "isValidated", "isPublished", "type") VALUES (35, 'CSWCat', 'meta-id','meta resp id', 'admin', NULL, NULL, '2013-03-21', 1, 1, 'NORMALRECORD');

INSERT INTO "Storage"."Values" ("form", "path", "ordinal", "type", "typeStandard", "id_value") VALUES (35, 'ISO 19115:MD_Metadata', 1, 'MD_Metadata', 'ISO 19115', 'ISO 19115:MD_Metadata.1');
INSERT INTO "Storage"."Values" ("form", "path", "ordinal", "type", "typeStandard", "id_value") VALUES (35, 'ISO 19115:MD_Metadata:contact', 1, 'CI_ResponsibleParty', 'ISO 19115', 'ISO 19115:MD_Metadata.1:contact.1');
                                      

INSERT INTO "Storage"."TextValues" ("form", "path", "ordinal", "type", "typeStandard", "id_value", "value") VALUES (35, 'ISO 19115:MD_Metadata:fileIdentifier', 1, 'CharacterString', 'ISO 19103', 'ISO 19115:MD_Metadata.1:identificationInfo.1:fileIdentifier.1', 'meta-id');
INSERT INTO "Storage"."TextValues" ("form", "path", "ordinal", "type", "typeStandard", "id_value", "value") VALUES (35, 'ISO 19115:MD_Metadata:contact:role', 1, 'CI_RoleCode', 'ISO 19115', 'ISO 19115:MD_Metadata.1:contact.1:role.1', '7');
INSERT INTO "Storage"."TextValues" ("form", "path", "ordinal", "type", "typeStandard", "id_value", "value") VALUES (35, 'ISO 19115:MD_Metadata:contact:id', 1, 'CharacterString', 'ISO 19103', 'ISO 19115:MD_Metadata.1:contact.1:id.1', 'test-id');

