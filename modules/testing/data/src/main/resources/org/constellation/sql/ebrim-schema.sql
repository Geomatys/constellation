
/*---------------------------------------------*
 *--------------  Standard Ebrim v 2.5 --------*
 *---------------------------------------------*/
INSERT INTO "Schemas"."Standard"  VALUES('Ebrim v2.5','rim25');


INSERT INTO "Schemas"."Standard"  VALUES('Web Registry Service v0.9', 'wrs09');

/*-------------------------------------------------*
 *--------------  Classe Action -------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Action',NULL,'Ebrim v2.5',NULL,1,NULL,NULL, ' ');


/*-------------------------------------------------*
 *--------------  Classe NotifyAction -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('NotifyAction',NULL,'Ebrim v2.5',NULL,1,'Action','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('notificationOption', NULL, 'Ebrim v2.5', NULL, 0, 1,'NotifyAction','CharacterString', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('endPoint', NULL, 'Ebrim v2.5', NULL, 1, 1,'NotifyAction','CharacterString', NULL, 'M',2 , 'ISO 19103','Ebrim v2.5', ' ');

/*-------------------------------------------------*
 *--------------  Classe LocalizedString ----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('LocalizedString',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('lang', NULL, 'Ebrim v2.5', NULL, 0, 1,'LocalizedString','CharacterString', NULL, 'O',0 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('charset', NULL, 'Ebrim v2.5', NULL, 0, 1,'LocalizedString','CharacterString', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'Ebrim v2.5', NULL, 1, 1,'LocalizedString','CharacterString', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');

/*-------------------------------------------------*
 *--------------  Classe InternationalString ------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('InternationalString',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('localizedString', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'InternationalString','LocalizedString', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');

/*-------------------------------------------------*
 *--------------  Classe ValueList ----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ValueList',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'ValueList','CharacterString', NULL, 'O',0 , 'ISO 19103','Ebrim v2.5', ' ');

/*-------------------------------------------------*
 *--------------  Classe Slot ---------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Slot',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('valueList', NULL, 'Ebrim v2.5', NULL, 1, 1,'Slot','ValueList', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'Ebrim v2.5', NULL, 1, 1,'Slot','CharacterString', NULL, 'M',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('slotType', NULL, 'Ebrim v2.5', NULL, 0, 1,'Slot','CharacterString', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');

/*-------------------------------------------------*
 *--------------  Classe SlotList -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SlotList',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('slot', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'SlotList','Slot', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe RegistryObject debut -----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('RegistryObject',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');


/*-------------------------------------------------*
 *--------------  Classe ExternalIdentifier -------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ExternalIdentifier',NULL,'Ebrim v2.5',NULL,0,'RegistryObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('registryObject', NULL, 'Ebrim v2.5', NULL, 0, 1,'ExternalIdentifier','CharacterString', NULL, 'O',0 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('identificationScheme', NULL, 'Ebrim v2.5', NULL, 1, 1,'ExternalIdentifier','CharacterString', NULL, 'M',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'Ebrim v2.5', NULL, 1, 1,'ExternalIdentifier','CharacterString', NULL, 'M',2 , 'ISO 19103','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe Classification -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Classification',NULL,'Ebrim v2.5',NULL,0,'RegistryObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('classificationScheme', NULL, 'Ebrim v2.5', NULL, 0, 1,'Classification','CharacterString', NULL, 'O',0 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('classifiedObject', NULL, 'Ebrim v2.5', NULL, 0, 1,'Classification','CharacterString', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('classificationNode', NULL, 'Ebrim v2.5', NULL, 0, 1,'Classification','CharacterString', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('nodeRepresentation', NULL, 'Ebrim v2.5', NULL, 0, 1,'Classification','CharacterString', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');




/*-------------------------------------------------*
 *--------------  Classe RegistryObject suite -----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'Ebrim v2.5', NULL, 0, 1,'RegistryObject','InternationalString', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('description', NULL, 'Ebrim v2.5', NULL, 0, 1,'RegistryObject','InternationalString', NULL, 'O',1 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('slot', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'RegistryObject','Slot', NULL, 'O',2 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('classification', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'RegistryObject','Classification', NULL, 'O',3 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('externalIdentifier', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'RegistryObject','ExternalIdentifier', NULL, 'O',4 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'Ebrim v2.5', NULL, 0, 1,'RegistryObject','CharacterString', NULL, 'O',5 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('home', NULL, 'Ebrim v2.5', NULL, 0, 1,'RegistryObject','CharacterString', NULL, 'O',6 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('objectType', NULL, 'Ebrim v2.5', NULL, 0, 1,'RegistryObject','CharacterString', NULL, 'O',7 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('status', NULL, 'Ebrim v2.5', NULL, 0, 1,'RegistryObject','CharacterString', NULL, 'O',8 , 'ISO 19103','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe RegistryEntry ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('RegistryEntry',NULL,'Ebrim v2.5',NULL,0,'RegistryObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('expiration', NULL, 'Ebrim v2.5', NULL, 0, 1,'RegistryEntry','Date', NULL, 'O',0 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('majorVersion', NULL, 'Ebrim v2.5', NULL, 0, 1,'RegistryEntry','Integer', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('minorVersion', NULL, 'Ebrim v2.5', NULL, 0, 1,'RegistryEntry','Integer', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('stability', NULL, 'Ebrim v2.5', NULL, 0, 1,'RegistryEntry','CharacterString', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('userVersion', NULL, 'Ebrim v2.5', NULL, 0, 1,'RegistryEntry','CharacterString', NULL, 'O',4 , 'ISO 19103','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe ObjectRef  ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ObjectRef',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'Ebrim v2.5', NULL, 1, 1,'ObjectRef','CharacterString', NULL, 'M',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('home', NULL, 'Ebrim v2.5', NULL, 0, 1,'ObjectRef','CharacterString', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('createReplica', NULL, 'Ebrim v2.5', NULL, 0, 1,'ObjectRef','Boolean', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');

/*-------------------------------------------------*
 *--------------  Classe ObjectRefList ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ObjectRefList',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('objectRef', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'ObjectRefList','ObjectRef', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe Federation ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Federation',NULL,'Ebrim v2.5',NULL,0,'RegistryEntry','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('members', NULL, 'Ebrim v2.5', NULL, 0, 1,'Federation','ObjectRefList', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('replicationSyncLatency', NULL, 'Ebrim v2.5', NULL, 0, 1,'Federation','Duration', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe ExternalLink -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ExternalLink',NULL,'Ebrim v2.5',NULL,0,'RegistryObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('externalURI', NULL, 'Ebrim v2.5', NULL, 1, 1,'ExternalLink','CharacterString', NULL, 'M',0 , 'ISO 19103','Ebrim v2.5', ' ');


/*-----------------------------------------------*
 *--------------  Classe ExtrinsicObject ----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ExtrinsicObject',NULL,'Ebrim v2.5',NULL,0,'RegistryEntry','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('mimeType', NULL, 'Ebrim v2.5', NULL, 0, 1,'ExtrinsicObject','CharacterString', NULL, 'O',0 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('isOpaque', NULL, 'Ebrim v2.5', NULL, 0, 1,'ExtrinsicObject','Boolean', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');



/*-------------------------------------------------*
 *--------------  Classe EmailAddress -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('EmailAddress',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('slot', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'EmailAddress','Slot', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('address', NULL, 'Ebrim v2.5', NULL, 0, 1,'EmailAddress','CharacterString', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('type', NULL, 'Ebrim v2.5', NULL, 0, 1,'EmailAddress','CharacterString', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');




/*-------------------------------------------------*
 *--------------  Classe AuditableEvent -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AuditableEvent',NULL,'Ebrim v2.5',NULL,0,'RegistryObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('affectedObject', NULL, 'Ebrim v2.5', NULL, 1, 1,'AuditableEvent','ObjectRefList', NULL, 'M',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('eventType', NULL, 'Ebrim v2.5', NULL, 1, 1,'AuditableEvent','CharacterString', NULL, 'M',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('timestamp', NULL, 'Ebrim v2.5', NULL, 1, 1,'AuditableEvent','Date', NULL, 'M',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('user', NULL, 'Ebrim v2.5', NULL, 1, 1,'AuditableEvent','CharacterString', NULL, 'M',3 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('requestId', NULL, 'Ebrim v2.5', NULL, 1, 1,'AuditableEvent','CharacterString', NULL, 'M',4 , 'ISO 19103','Ebrim v2.5', ' ');

/*-------------------------------------------------*
 *--------------  Classe ClassificationNode -------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ClassificationNode',NULL,'Ebrim v2.5',NULL,0,'RegistryObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('classificationNode', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'ClassificationNode','ClassificationNode', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('parent', NULL, 'Ebrim v2.5', NULL, 0, 1,'ClassificationNode','CharacterString', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('code', NULL, 'Ebrim v2.5', NULL, 0, 1,'ClassificationNode','CharacterString', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('path', NULL, 'Ebrim v2.5', NULL, 0, 1,'ClassificationNode','CharacterString', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');

/*-------------------------------------------------*
 *--------------  Classe PostalAddress ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PostalAddress',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('slot', NULL, 'Ebrim v2.5', NULL, 0, 1,'PostalAddress','Slot', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('city', NULL, 'Ebrim v2.5', NULL, 0, 1,'PostalAddress','CharacterString', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('country', NULL, 'Ebrim v2.5', NULL, 0, 1,'PostalAddress','CharacterString', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('postalCode', NULL, 'Ebrim v2.5', NULL, 0, 1,'PostalAddress','CharacterString', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('stateOrProvince', NULL, 'Ebrim v2.5', NULL, 0, 1,'PostalAddress','CharacterString', NULL, 'O',4 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('street', NULL, 'Ebrim v2.5', NULL, 0, 1,'PostalAddress','CharacterString', NULL, 'O',5 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('streetNumber', NULL, 'Ebrim v2.5', NULL, 0, 1,'PostalAddress','CharacterString', NULL, 'O',6 , 'ISO 19103','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe TelephoneNumber ----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('TelephoneNumber',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('areaCode', NULL, 'Ebrim v2.5', NULL, 0, 1,'TelephoneNumber','CharacterString', NULL, 'O',0 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('countryCode', NULL, 'Ebrim v2.5', NULL, 0, 1,'TelephoneNumber','CharacterString', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('extension', NULL, 'Ebrim v2.5', NULL, 0, 1,'TelephoneNumber','CharacterString', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('number', NULL, 'Ebrim v2.5', NULL, 0, 1,'TelephoneNumber','CharacterString', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('phoneType', NULL, 'Ebrim v2.5', NULL, 0, 1,'TelephoneNumber','CharacterString', NULL, 'O',4 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('url', NULL, 'Ebrim v2.5', NULL, 0, 1,'TelephoneNumber','CharacterString', NULL, 'O',5 , 'ISO 19103','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe Organization -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Organization',NULL,'Ebrim v2.5',NULL,0,'RegistryObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('address', NULL, 'Ebrim v2.5', NULL, 1, 2147483647,'Organization','PostalAddress', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('telephoneNumber', NULL, 'Ebrim v2.5', NULL, 1, 2147483647,'Organization','TelephoneNumber', NULL, 'M',1 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('emailAddress', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'Organization','EmailAddress', NULL, 'O',2 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('parent', NULL, 'Ebrim v2.5', NULL, 0, 1,'Organization','CharacterString', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('primaryContact', NULL, 'Ebrim v2.5', NULL, 1, 1,'Organization','CharacterString', NULL, 'M',4 , 'ISO 19103','Ebrim v2.5', ' ');



/*-------------------------------------------------*
 *--------------  Classe ClassificationScheme -----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ClassificationScheme',NULL,'Ebrim v2.5',NULL,0,'RegistryEntry','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('classificationNode', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'ClassificationScheme','ClassificationNode', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('isInternal', NULL, 'Ebrim v2.5', NULL, 1, 1,'ClassificationScheme','Boolean', NULL, 'M',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('nodeType', NULL, 'Ebrim v2.5', NULL, 1, 1,'ClassificationScheme','CharacterString', NULL, 'M',2 , 'ISO 19103','Ebrim v2.5', ' ');



/*-------------------------------------------------*
 *--------------  Classe TelephoneNumberList ------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('TelephoneNumberList',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('telephoneNumber', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'TelephoneNumberList','TelephoneNumber', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');



/*-------------------------------------------------*
 *--------------  Classe SpecificationLink --------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SpecificationLink',NULL,'Ebrim v2.5',NULL,0,'RegistryObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('usageDescription', NULL, 'Ebrim v2.5', NULL, 0, 1,'SpecificationLink','InternationalString', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('usageParameter', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'SpecificationLink','CharacterString', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('serviceBinding', NULL, 'Ebrim v2.5', NULL, 0, 1,'SpecificationLink','CharacterString', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('specificationObject', NULL, 'Ebrim v2.5', NULL, 0, 1,'SpecificationLink','CharacterString', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe ServiceBinding -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ServiceBinding',NULL,'Ebrim v2.5',NULL,0,'RegistryObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('specification', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'ServiceBinding','SpecificationLink', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('service', NULL, 'Ebrim v2.5', NULL, 0, 1,'ServiceBinding','CharacterString', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('accessURI', NULL, 'Ebrim v2.5', NULL, 0,1,'ServiceBinding','CharacterString', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('targetBinding', NULL, 'Ebrim v2.5', NULL, 0, 1,'ServiceBinding','CharacterString', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');

/*-------------------------------------------------*
 *--------------  Classe Service ------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Service',NULL,'Ebrim v2.5',NULL,0,'RegistryEntry','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('serviceBinding', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'Service','ServiceBinding', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe Registry -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Registry',NULL,'Ebrim v2.5',NULL,0,'RegistryEntry','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('operator', NULL, 'Ebrim v2.5', NULL, 1, 1,'Registry','CharacterString', NULL, 'M',0 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('specificationVersion', NULL, 'Ebrim v2.5', NULL, 1, 1,'Registry','CharacterString', NULL, 'M',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('replicationSyncLatency', NULL, 'Ebrim v2.5', NULL, 0, 1,'Registry','Duration', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('catalogingLatency', NULL, 'Ebrim v2.5', NULL, 0, 1,'Registry','Duration', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('sqlQuerySupported', NULL, 'Ebrim v2.5', NULL, 0, 1,'Registry','Boolean', NULL, 'O',4 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('eventNotificationSupported', NULL, 'Ebrim v2.5', NULL, 0, 1,'Registry','Boolean', NULL, 'O',5 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('objectReplicationSupported', NULL, 'Ebrim v2.5', NULL, 0, 1,'Registry','Boolean', NULL, 'O',6 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('objectRelocationSupported', NULL, 'Ebrim v2.5', NULL, 0, 1,'Registry','Boolean', NULL, 'O',7 , 'ISO 19103','Ebrim v2.5', ' ');

/*-------------------------------------------------*
 *--------------  Classe RegistryObjectList -------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('RegistryObjectList',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('registryObject', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'RegistryObjectList','RegistryObject', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('objectRef', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'RegistryObjectList','ObjectRef', NULL, 'O',1 , 'Ebrim v2.5','Ebrim v2.5', ' ');

/*-------------------------------------------------*
 *--------------  Classe RegistryPackage ----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('RegistryPackage',NULL,'Ebrim v2.5',NULL,0,'RegistryEntry','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('RegistryObjectList', NULL, 'Ebrim v2.5', NULL, 0, 1,'RegistryPackage','RegistryObjectList', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe PersonName ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PersonName',NULL,'Ebrim v2.5',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('slot', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'PersonName','Slot', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('firstName', NULL, 'Ebrim v2.5', NULL, 0, 1,'PersonName','CharacterString', NULL, 'O',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('middleName', NULL, 'Ebrim v2.5', NULL, 0, 1,'PersonName','CharacterString', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('lastName', NULL, 'Ebrim v2.5', NULL, 0, 1,'PersonName','CharacterString', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe UserType -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('UserType',NULL,'Ebrim v2.5',NULL,0,'RegistryObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('address', NULL, 'Ebrim v2.5', NULL, 1, 2147483647,'UserType','PostalAddress', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('personName', NULL, 'Ebrim v2.5', NULL, 1, 1,'UserType','PersonName', NULL, 'M',1 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('telephoneNumber', NULL, 'Ebrim v2.5', NULL, 1, 2147483647,'UserType','TelephoneNumber', NULL, 'M',2 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('emailAddress', NULL, 'Ebrim v2.5', NULL, 1, 2147483647,'UserType','EmailAddress', NULL, 'M',3 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('url', NULL, 'Ebrim v2.5', NULL, 0, 1,'UserType','CharacterString', NULL, 'O',4 , 'ISO 19103','Ebrim v2.5', ' ');




/*-------------------------------------------------*
 *--------------  Classe Subscription -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Subscription',NULL,'Ebrim v2.5',NULL,0,'RegistryObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('action', NULL, 'Ebrim v2.5', NULL, 0, 2147483647,'Subscription','Action', NULL, 'O',0 , 'Ebrim v2.5','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('selector', NULL, 'Ebrim v2.5', NULL, 1, 1,'Subscription','CharacterString', NULL, 'M',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('startDate', NULL, 'Ebrim v2.5', NULL, 0, 1,'Subscription','Date', NULL, 'O',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('endDate', NULL, 'Ebrim v2.5', NULL, 0, 1,'Subscription','Date', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('notificationInterval', NULL, 'Ebrim v2.5', NULL, 0, 1,'Subscription','Duration', NULL, 'O',4 , 'ISO 19103','Ebrim v2.5', ' ');




/*-------------------------------------------------*
 *--------------  Classe AdhocQueryType -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AdhocQueryType',NULL,'Ebrim v2.5',NULL,1,'RegistryObject','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe Association --------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Association',NULL,'Ebrim v2.5',NULL,0,'RegistryObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('associationType', NULL, 'Ebrim v2.5', NULL, 1, 1,'Association','CharacterString', NULL, 'O',0 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('sourceObject', NULL, 'Ebrim v2.5', NULL, 1, 1,'Association','CharacterString', NULL, 'M',1 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('targetObject', NULL, 'Ebrim v2.5', NULL, 1, 1,'Association','CharacterString', NULL, 'M',2 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('isConfirmedBySourceOwner', NULL, 'Ebrim v2.5', NULL, 0, 1,'Association','Boolean', NULL, 'O',3 , 'ISO 19103','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('isConfirmedByTargetOwner', NULL, 'Ebrim v2.5', NULL, 0, 1,'Association','Boolean', NULL, 'O',4 , 'ISO 19103','Ebrim v2.5', ' ');


/*-------------------------------------------------*
 *--------------  Classe ApplicationModule --------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ApplicationModule',NULL,'Web Registry Service v0.9',NULL,0,'RegistryPackage','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('abstractQuery', NULL, 'Web Registry Service v0.9', NULL, 0, 2147483647,'ApplicationModule','AbstractQuery', NULL, 'O',0 , 'Catalog Web Service','Web Registry Service v0.9', ' ');

/*-------------------------------------------------*
 *--------------  Classe SimpleLink ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SimpleLink',NULL,'Web Registry Service v0.9',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('type', NULL, 'Xlink', NULL, 0, 1,'SimpleLink','CharacterString', NULL, 'O',0 , 'ISO 19103','Web Registry Service v0.9', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('href', NULL, 'Xlink', NULL, 0, 1,'SimpleLink','CharacterString', NULL, 'O',1 , 'ISO 19103','Web Registry Service v0.9', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('role', NULL, 'Xlink', NULL, 0, 1,'SimpleLink','CharacterString', NULL, 'O',2 , 'ISO 19103','Web Registry Service v0.9', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('arcrole', NULL, 'Xlink', NULL, 0, 1,'SimpleLink','CharacterString', NULL, 'O',3 , 'ISO 19103','Web Registry Service v0.9', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('title', NULL, 'Xlink', NULL, 0, 1,'SimpleLink','CharacterString', NULL, 'O',4 , 'ISO 19103','Web Registry Service v0.9', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('show', NULL, 'Xlink', NULL, 0, 1,'SimpleLink','CharacterString', NULL, 'O',5 , 'ISO 19103','Web Registry Service v0.9', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('actuate', NULL, 'Xlink', NULL, 0, 1,'SimpleLink','CharacterString', NULL, 'O',6 , 'ISO 19103','Web Registry Service v0.9', ' ');

/*-------------------------------------------------*
 *--------------  Classe WRSExtrinsicObject -------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('WRSExtrinsicObject',NULL,'Web Registry Service v0.9',NULL,0,'ExtrinsicObject','Ebrim v2.5', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('content', NULL, 'Web Registry Service v0.9', NULL, 1, 1,'WRSExtrinsicObject','SimpleLink', NULL, 'M',0 , 'Web Registry Service v0.9','Web Registry Service v0.9', ' ');

/*-------------------------------------------------*
 *--------------  Classe Geometry -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Geometry',NULL,'Web Registry Service v0.9',NULL,0,'WRSExtrinsicObject','Web Registry Service v0.9', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('dimension', NULL, 'Web Registry Service v0.9', NULL, 0, 1,'Geometry','Integer', NULL, 'O',0 , 'ISO 19103','Web Registry Service v0.9', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('geometryType', NULL, 'Web Registry Service v0.9', NULL, 1, 1,'Geometry','CharacterString', NULL, 'O',1 , 'ISO 19103','Web Registry Service v0.9', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('srid', NULL, 'Web Registry Service v0.9', NULL, 0, 1,'Geometry','CharacterString', NULL, 'O',2 , 'ISO 19103','Web Registry Service v0.9', ' ');


INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject', 'ExtrinsicObject', 'Ebrim v2.5', 'ExtrinsicObject', NULL, 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:id', 'id', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:name', 'name', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:name:localizedString', 'localizedString', 'Ebrim v2.5', 'InternationalString', 'Ebrim v2.5:ExtrinsicObject:name', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:name:localizedString:lang', 'lang', 'Ebrim v2.5', 'LocalizedString', 'Ebrim v2.5:ExtrinsicObject:name:localizedString', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:name:localizedString:charset', 'charset', 'Ebrim v2.5', 'LocalizedString', 'Ebrim v2.5:ExtrinsicObject:name:localizedString', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:name:localizedString:value', 'value', 'Ebrim v2.5', 'LocalizedString', 'Ebrim v2.5:ExtrinsicObject:name:localizedString', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:description', 'description', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:description:localizedString', 'localizedString', 'Ebrim v2.5', 'InternationalString', 'Ebrim v2.5:ExtrinsicObject:description', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:description:localizedString:lang', 'lang', 'Ebrim v2.5', 'LocalizedString', 'Ebrim v2.5:ExtrinsicObject:description:localizedString', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:description:localizedString:charset', 'charset', 'Ebrim v2.5', 'LocalizedString', 'Ebrim v2.5:ExtrinsicObject:description:localizedString', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:description:localizedString:value', 'value', 'Ebrim v2.5', 'LocalizedString', 'Ebrim v2.5:ExtrinsicObject:description:localizedString', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:slot', 'slot', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:slot:valueList', 'valueList', 'Ebrim v2.5', 'Slot', 'Ebrim v2.5:ExtrinsicObject:slot', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:slot:valueList:value', 'value', 'Ebrim v2.5', 'ValueList', 'Ebrim v2.5:ExtrinsicObject:slot:valueList', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:slot:slotType', 'slotType', 'Ebrim v2.5', 'Slot', 'Ebrim v2.5:ExtrinsicObject:slot', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:slot:name', 'name', 'Ebrim v2.5', 'Slot', 'Ebrim v2.5:ExtrinsicObject:slot', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:mimeType', 'mimeType', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:isOpaque', 'isOpaque', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:minorVersion', 'minorVersion', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:majorVersion', 'majorVersion', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:stability', 'stability', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:userVersion', 'userVersion', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:objectType', 'objectType', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:status', 'status', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');
INSERT INTO "Schemas"."Paths"  VALUES ('Ebrim v2.5:ExtrinsicObject:home', 'home', 'Ebrim v2.5', 'ExtrinsicObject', 'Ebrim v2.5:ExtrinsicObject', 'Ebrim v2.5');