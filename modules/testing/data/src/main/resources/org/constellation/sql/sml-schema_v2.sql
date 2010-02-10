
 INSERT INTO "Schemas"."Classes"  VALUES('OnlineResource',NULL,'SensorML','based on ISO 19115',0,NULL,NULL,' ');
 INSERT INTO "Schemas"."Properties"  VALUES('href', NULL, 'Xlink', NULL, 0, 1,'OnlineResource','CharacterString', NULL, 'O',0 , 'ISO 19103','SensorML','P');


 INSERT INTO "Schemas"."Classes"  VALUES('Address',NULL,'SensorML','based on ISO 19115',0,NULL,NULL, 'N');
 INSERT INTO "Schemas"."Properties"  VALUES('deliveryPoint', NULL, 'SensorML', NULL, 0, 2147483647,'Address','CharacterString', NULL, 'O',1 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('city', NULL, 'SensorML', NULL, 0, 1,'Address','CharacterString', NULL, 'O',2 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('administrativeArea', NULL, 'SensorML', NULL, 0, 1,'Address','CharacterString', NULL, 'O',3 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('postalCode', NULL, 'SensorML', NULL, 0, 1,'Address','CharacterString', NULL, 'O',4 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('country', NULL, 'SensorML', NULL, 0, 1,'Address','CharacterString', NULL, 'O',5 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('electronicMailAddress', NULL, 'SensorML', NULL, 0, 1,'Address','CharacterString', NULL, 'O',6 , 'ISO 19103','SensorML',' ');


 INSERT INTO "Schemas"."Classes"  VALUES('Phone',NULL,'SensorML','based on ISO 19115',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('voice', NULL, 'SensorML', NULL, 0, 2147483647,'Phone','CharacterString', NULL, 'O',0 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('facsimile', NULL, 'SensorML', NULL, 0, 2147483647,'Phone','CharacterString', NULL, 'O',1 , 'ISO 19103','SensorML',' ');



 INSERT INTO "Schemas"."Classes"  VALUES('Contact',NULL,'SensorML','based on ISO 19115',0,NULL,NULL, 'N');
 INSERT INTO "Schemas"."Properties"  VALUES('phone', NULL, 'SensorML', NULL, 0, 1,'Contact','Phone', NULL, 'O',0 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('address', NULL, 'SensorML', NULL, 0, 1,'Contact','Address', NULL, 'O',1 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('onlineResource', NULL, 'SensorML', NULL, 0, 2147483647,'Contact','OnlineResource', NULL, 'O',2 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('hoursOfService', NULL, 'SensorML', NULL, 0, 1,'Contact','CharacterString', NULL, 'O',3 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('contactInstruction', NULL, 'SensorML', NULL, 0, 1,'Contact','CharacterString', NULL, 'O',4 , 'ISO 19103','SensorML',' ');



 INSERT INTO "Schemas"."Classes"  VALUES('ResponsibleParty',NULL,'SensorML','based on ISO 19115',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('individualName', NULL, 'SensorML', NULL, 0, 1,'ResponsibleParty','CharacterString', NULL, 'O',0 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('organizationName', NULL, 'SensorML', NULL, 0, 1,'ResponsibleParty','CharacterString', NULL, 'O',1 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('positionName', NULL, 'SensorML', NULL, 0, 1,'ResponsibleParty','CharacterString', NULL, 'O',2 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('contactInfo', NULL, 'SensorML', NULL, 0, 1,'ResponsibleParty','Contact', NULL, 'O',3 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('role', NULL, 'Xlink', NULL, 0, 1,'ResponsibleParty','CharacterString', NULL, 'O',4 , 'ISO 19103','SensorML','P');


/*---------------------------------------------*
 *--------------  Classe LinkRef --------------*
 *---------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('LinkRef',NULL,'SensorML',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('ref', NULL, 'SensorML', NULL, 0, 1,'LinkRef','CharacterString', NULL, 'O',0 , 'ISO 19103','SensorML','P');


/*---------------------------------------------*
 *--------------  Classe DecimalList ----------*
 *---------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('DecimalList',NULL,'Sensor Web Enablement',NULL,0,NULL,NULL, ' ');

/*---------------------------------------------*
 *--------------  Classe DecimalPair ----------*
 *---------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('DecimalPair',NULL,'Sensor Web Enablement',NULL,0,NULL,NULL, ' ');

 /*---------------------------------------------*
 *--------------  Classe TimeList ----------*
 *---------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('TimeList',NULL,'Sensor Web Enablement',NULL,0,NULL,NULL, ' ');

 /*---------------------------------------------*
 *--------------  Classe TimePair ----------*
 *---------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('TimePair',NULL,'Sensor Web Enablement',NULL,0,NULL,NULL, ' ');

/*---------------------------------------------*
 *--------------  Classe DataValue ----------*
 *---------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('DataValue',NULL,'Sensor Web Enablement',NULL,0,NULL,NULL, ' ');

 INSERT INTO "Schemas"."Classes"  VALUES('UomIdentifier',NULL,'Sensor Web Enablement',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('uomSymbol', NULL, 'Sensor Web Enablement', NULL, 0, 1,'UomIdentifier','CharacterString', NULL, 'O',0 , 'ISO 19103','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('uomURI', NULL, 'Sensor Web Enablement', NULL, 0, 1,'UomIdentifier','URI', NULL, 'O',1 , 'ISO 19103','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('code', NULL, 'Sensor Web Enablement', NULL, 0, 1,'UomIdentifier','URI', NULL, 'O',2, 'ISO 19103','Sensor Web Enablement',' ');

 /*-------------------------------------------------*
 *--------------  Classe Uom ----------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Uom',NULL,'Sensor Web Enablement','Property type that indicates unit-of-measure, either by inline definition reference  UCUM code',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('unitDefinition', NULL, 'ISO 19108', NULL, 0, 1,'Uom','CharacterString', NULL, 'O',0 , 'ISO 19103','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('code', NULL, 'Sensor Web Enablement', 'Specifies a unit by using a UCUM expression (prefered)', 0, 1,'Uom','CharacterString', NULL, 'O',1 , 'ISO 19103','Sensor Web Enablement','P');
 INSERT INTO "Schemas"."Properties"  VALUES('href', NULL, 'Xlink', NULL, 0, 1,'Uom','CharacterString', NULL, 'O',1 , 'ISO 19103','Sensor Web Enablement','P');


/*-------------------------------------------------*
 *--------------  Classe AllowedTokens ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AllowedTokens',NULL,'Sensor Web Enablement','Enumeration of allowed values',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('valueList', NULL, 'Sensor Web Enablement', 'List of allowed token values for this component', 1, 2147483647,'AllowedTokens','DecimalList', NULL, 'M',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'Sensor Web Enablement', 'List of allowed token values for this component', 1, 1,'AllowedTokens','CharacterString', NULL, 'M',1 , 'ISO 19103','Sensor Web Enablement',' ');


 /*---------------------------------------------*
  *--------------  Classe Link  ----------------*
  *---------------------------------------------*/

 INSERT INTO "Schemas"."Classes"  VALUES('Link',NULL,'SensorML',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('source', NULL, 'SensorML', NULL, 1, 1,'Link','LinkRef', NULL, 'M',0 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('destination', NULL, 'SensorML', NULL, 1, 1,'Link','LinkRef', NULL, 'M',1 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'SensorML', NULL, 0, 1,'Link','CharacterString', NULL, 'O',2 , 'ISO 19103','SensorML','P');


/*-------------------------------------------------*
 *--------------  Classe AnyData ------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AnyData',NULL,'Sensor Web Enablement',NULL,1,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'SensorML', NULL, 0, 1,'AnyData','CharacterString', NULL, 'O',0 , 'ISO 19103','Sensor Web Enablement','P');
 INSERT INTO "Schemas"."Properties"  VALUES('definition', NULL, 'Sensor Web Enablement', NULL, 0, 1,'AnyData','CharacterString', NULL, 'O',1 , 'ISO 19103','Sensor Web Enablement','C');
 INSERT INTO "Schemas"."Properties"  VALUES('role', NULL, 'Xlink', NULL, 0, 1,'AnyData','CharacterString', NULL, 'O',2 , 'ISO 19103','Sensor Web Enablement','P');


/*-------------------------------------------------*
 *----------  Classe DataComponentPropertyType ----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('DataComponentPropertyType',NULL,'Sensor Web Enablement','Base type for all data components',1,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'Sensor Web Enablement', NULL, 0, 1,'DataComponentPropertyType','CharacterString', NULL, 'O',0 , 'ISO 19103','Sensor Web Enablement','P');
 INSERT INTO "Schemas"."Properties"  VALUES('role', NULL, 'Xlink', NULL, 0, 1,'DataComponentPropertyType','CharacterString', NULL, 'O',1 , 'ISO 19103','Sensor Web Enablement','P');
 INSERT INTO "Schemas"."Properties"  VALUES('href', NULL, 'Xlink', NULL, 0, 1,'DataComponentPropertyType','CharacterString', NULL, 'O',2 , 'ISO 19103','Sensor Web Enablement','P');
 INSERT INTO "Schemas"."Properties"  VALUES('value',NULL, 'Sensor Web Enablement', NULL, 0, 2147483647,'DataComponentPropertyType','AnyData', NULL, 'O',3 , 'Sensor Web Enablement','Sensor Web Enablement',' ');

/*-------------------------------------------------*
 *--------------  Classe AbstractDataComponent ----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractDataComponent',NULL,'Sensor Web Enablement','Base type for all data components',1,'AnyData','Sensor Web Enablement', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'ISO 19108', NULL, 0, 1,'AbstractDataComponent','CharacterString', NULL, 'O',0 , 'ISO 19103','Sensor Web Enablement','C');
 INSERT INTO "Schemas"."Properties"  VALUES('definition', NULL, 'Sensor Web Enablement', NULL, 0, 1,'AbstractDataComponent','URI', NULL, 'O',1 , 'ISO 19103','Sensor Web Enablement','C');
 INSERT INTO "Schemas"."Properties"  VALUES('fixed', NULL, 'Sensor Web Enablement', NULL, 0, 1,'AbstractDataComponent','Boolean', NULL, 'O',2 , 'ISO 19103','Sensor Web Enablement',' ');


 /*-------------------------------------------------*
 *--------------  Classe ParameterList ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ParameterList',NULL,'SensorML','list of parameters',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('parameter', NULL, 'SensorML', NULL, 1, 2147483647,'ParameterList','AbstractDataComponent', NULL, 'M',0 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'SensorML', NULL, 0, 1,'ParameterList','CharacterString', NULL, 'O',1 , 'ISO 19103','SensorML', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('namerole', NULL, 'Xlink', NULL, 0, 1,'ParameterList','CharacterString', NULL, 'O',2 , 'ISO 19103','SensorML', ' ');


 /*-------------------------------------------------*
 *--------------  Classe AbstractDataRecord -------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractDataRecord',NULL,'Sensor Web Enablement',NULL,1,'AbstractDataComponent','Sensor Web Enablement', ' ');


/*-------------------------------------------------*
 *--------------  Classe DataRecord ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('DataRecord',NULL,'Sensor Web Enablement','Implementation of ISO-11404 Record datatype.',0,'AbstractDataRecord','Sensor Web Enablement', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id',NULL, 'ISO 19108', NULL, 0, 1,'DataRecord','CharacterString', NULL, 'O',0 , 'ISO 19103','Sensor Web Enablement','C');
 INSERT INTO "Schemas"."Properties"  VALUES('description',NULL, 'ISO 19108', NULL, 0, 1,'DataRecord','CharacterString', NULL, 'O',1 , 'ISO 19103','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('field',NULL, 'Sensor Web Enablement', NULL, 0, 2147483647,'DataRecord','DataComponentPropertyType', NULL, 'O',2 , 'Sensor Web Enablement','Sensor Web Enablement',' ');


/*-------------------------------------------------*
 *--------------  Classe AllowedValues ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AllowedValues',NULL,'Sensor Web Enablement','List of allowed values (There is an implicit AND between all members)',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('interval', NULL, 'Sensor Web Enablement', NULL, 0, 2147483647,'AllowedValues','DecimalPair', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('valueList', NULL, 'Sensor Web Enablement', NULL, 0, 2147483647,'AllowedValues','DecimalList', NULL, 'O',1 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'Sensor Web Enablement', 'List of allowed token values for this component', 1, 1,'AllowedValues','CharacterString', NULL, 'M',2 , 'ISO 19103','Sensor Web Enablement',' ');

/*-------------------------------------------------*
 *--------------  Classe CodeSpace ----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('CodeSpace',NULL,'Sensor Web Enablement','Property type that indicates the codespace',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('href', NULL, 'Xlink', NULL, 0, 1,'CodeSpace','CharacterString', NULL, 'O',0 , 'ISO 19103','Sensor Web Enablement','P');


/*-------------------------------------------------*
 *--------------  Classe Text ---------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Text',NULL,'Sensor Web Enablement','Free textual component',0,'AbstractDataComponent', 'Sensor Web Enablement', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'Sensor Web Enablement', 'Value is optional, to enable structure to act in a schema for values provided using other encodings', 0, 1,'Text','CharacterString', NULL, 'O',0 , 'ISO 19103','Sensor Web Enablement',' ');


 /*-------------------------------------------------*
 *--------------  Classe Category debut ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Category',NULL,'Sensor Web Enablement',NULL,0,'AbstractDataComponent','Sensor Web Enablement', ' ');

 /*-------------------------------------------------*
 *--------------  Classe Quantity debut ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Quantity',NULL,'Sensor Web Enablement','Decimal number with optional unit and constraints',0,'AbstractDataComponent', 'Sensor Web Enablement', ' ');

 /*-------------------------------------------------*
 *--------------  Classe Quality debut -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Quality',NULL,'Sensor Web Enablement','Allows for a simple quality assessment of the values carried by this component.',0,NULL,NULL, ' ');


/*-------------------------------------------------*
 *--------------  Classe Boolean ------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Boolean',NULL,'Sensor Web Enablement','Scalar component used to express truth: 1 or 0, 0 or 1',0,'AbstractDataComponent', 'Sensor Web Enablement', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('quality', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Boolean','Quality', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('definition', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Boolean','URI', NULL, 'O',1 , 'ISO 19103','Sensor Web Enablement','C');


/*-------------------------------------------------*
 *--------------  Classe QuantityRange ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('QuantityRange',NULL,'Sensor Web Enablement',NULL,0,'AbstractDataComponent','Sensor Web Enablement', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('constraint', NULL, 'Sensor Web Enablement', NULL, 0, 1,'QuantityRange','AllowedValues', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('quality', NULL, 'Sensor Web Enablement', NULL, 0, 1,'QuantityRange','Quality', NULL, 'O',1 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('uom', NULL, 'Sensor Web Enablement', NULL, 0, 1,'QuantityRange','Uom', NULL, 'O',2 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'Sensor Web Enablement', NULL, 0, 1,'QuantityRange','DecimalPair', NULL, 'O',3 , 'Sensor Web Enablement','Sensor Web Enablement','V');

/*-------------------------------------------------*
 *--------------  Classe Category fin -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Properties"  VALUES('codeSpace', NULL, 'Sensor Web Enablement', 'Provides link to dictionary or rule set to which the value belongs', 0, 1,'Category','CodeSpace', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('constraint', NULL, 'Sensor Web Enablement', 'The constraint property defines the permitted values, as an enumerated list', 0, 1,'Category','AllowedTokens', NULL, 'O',1 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('quality', NULL, 'Sensor Web Enablement', 'The quality property provides an indication of the reliability of estimates of the asociated value', 0, 1,'Category','Quality', NULL, 'O',2 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'Sensor Web Enablement', 'Value is optional, to enable structure to act in a schema for values provided using other encodings', 0, 1,'Category','CharacterString', NULL, 'O',3 , 'ISO 19103','Sensor Web Enablement',' ');


/*-------------------------------------------------*
 *--------------  Classe Quantity fin -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Properties"  VALUES('uom', NULL, 'Sensor Web Enablement', 'Unit of measure', 0, 1,'Quantity','Uom', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('constraint', NULL, 'Sensor Web Enablement', 'The constraint property defines the permitted values, as a range or enumerated list', 0, 1,'Quantity','AllowedValues', NULL, 'O',1 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('quality', NULL, 'Sensor Web Enablement', 'The quality property provides an indication of the reliability of estimates of the asociated value', 0, 2147483647,'Quantity','Quality', NULL, 'O',2 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'Sensor Web Enablement', 'Value is optional, to enable structure to act in a schema for values provided using other encodings', 0, 1,'Quantity','Double', NULL, 'O',3 , 'ISO 19103','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('axisID', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Quantity','CharacterString', NULL, 'O',4 , 'ISO 19103','Sensor Web Enablement','C');

/*-------------------------------------------------*
 *--------------  Classe Count --------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Count',NULL,'Sensor Web Enablement','Integer number used for a counting value',1,'AbstractDataComponent','Sensor Web Enablement', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('constraint', NULL, 'Sensor Web Enablement', 'The constraint property defines the permitted values, as a range or enumerated list', 0, 1,'Count','AllowedValues', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('quality', NULL, 'Sensor Web Enablement', 'The quality property provides an indication of the reliability of estimates of the asociated value', 0, 1,'Count','Quality', NULL, 'O',1 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'Sensor Web Enablement', 'Value is optional, to enable structure to act in a schema for values provided using other encodings', 0, 1,'Count','Integer', NULL, 'O',2 , 'ISO 19103','Sensor Web Enablement',' ');



/*-------------------------------------------------*
 *--------------  Classe Time debut ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Time',NULL,'Sensor Web Enablement','Either ISO 8601 (e.g. 2004-04-18T12:03:04.6Z) or time relative to a time origin',0,'AbstractDataComponent','Sensor Web Enablement', ' ');


/*-------------------------------------------------*
 *--------------  Classe AbstractVector debut -----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractVector',NULL,'Sensor Web Enablement',NULL,1,'AbstractDataRecord','Sensor Web Enablement', ' ');


/*-------------------------------------------------*
 *--------------  Classe Envelope debut-------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Envelope',NULL,'Sensor Web Enablement','Envelope described using two vectors specifying lower and upper corner points.',1,'AbstractVector','Sensor Web Enablement', ' ');

 /*-------------------------------------------------*
 *--------------  Classe AbstractSML --------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractSML',NULL,'SensorML','Main Abstract SensorML Object',1,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'ISO 19108', NULL, 0, 1,'AbstractSML','CharacterString', NULL, 'O',1 , 'ISO 19103','SensorML','C');
 INSERT INTO "Schemas"."Properties"  VALUES('description', NULL, 'ISO 19108', NULL, 0, 1,'AbstractSML','CharacterString', NULL, 'O',2 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'ISO 19108', NULL, 0, 1,'AbstractSML','CharacterString', NULL, 'O',3 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('boundedBy', NULL, 'ISO 19108', 'Specifies the possible extent of the component location', 0, 4,'AbstractSML','Envelope', NULL, 'O',2 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('role', NULL, 'Xlink', NULL, 0, 1,'AbstractSML','CharacterString', NULL, 'O',5 , 'ISO 19103','SensorML','P');

/*-------------------------------------------------*
 *--------------  Classe AbstractProcess debut -----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractProcess',NULL,'SensorML',NULL,1,'AbstractSML','SensorML', ' ');

/*-------------------------------------------------*
 *--------------  Classe TimePosition -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('TimePosition',NULL,'SensorML','Provide the ability to relate  a local time frame to a reference time frame',0,NULL,NULL, ' ');

 INSERT INTO "Schemas"."Properties"  VALUES('process', NULL, 'SensorML', NULL, 0, 1,'TimePosition','AbstractProcess', NULL, 'O',1 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'SensorML', NULL, 1, 1,'TimePosition','CharacterString', NULL, 'M',3 , 'ISO 19103','SensorML',' ');



/*-------------------------------------------------*
 *--------------  Classe AllowedTimes -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AllowedTimes',NULL,'Sensor Web Enablement','List of allowed time values (There is an implicit AND between all members)',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('min', NULL, 'Sensor Web Enablement', 'Specifies minimum allowed time value for an open interval (no max)', 0, 1,'AllowedTimes','TimePosition', NULL, 'O',0 , 'SensorML','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('max', NULL, 'Sensor Web Enablement', 'Specifies maximum allowed time value for an open interval (no min)', 0, 1,'AllowedTimes','TimePosition', NULL, 'O',1 , 'SensorML','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('interval', NULL, 'Sensor Web Enablement', 'Range of allowed time values (closed interval) for this component', 0, 2147483647,'AllowedTimes','TimePair', NULL, 'O',2 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('valueList', NULL, 'Sensor Web Enablement', 'List of allowed time values for this component', 0, 2147483647,'AllowedTimes','TimeList', NULL, 'O',3 , 'Sensor Web Enablement','Sensor Web Enablement',' ');

 /*-------------------------------------------------*
 *--------------  Classe Time fin  -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Properties"  VALUES('uom', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Time','Uom', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('constraint', NULL, 'Sensor Web Enablement', 'The constraint property defines the permitted values, as a range or enumerated list', 0, 1,'Time','AllowedTimes', NULL, 'O',1 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('quality', NULL, 'Sensor Web Enablement', 'The quality property provides an indication of the reliability of estimates of the asociated value', 0, 1,'Time','Quality', NULL, 'O',2 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'Sensor Web Enablement', 'Value is optional, to enable structure to act in a schema for values provided using other encodings', 0, 1,'Time','TimePosition', NULL, 'O',3 , 'SensorML','Sensor Web Enablement',' ');


 /*-------------------------------------------------*
 *--------------  Classe TimeRange ----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('TimeRange',NULL,'Sensor Web Enablement','Time value pair for specifying a time range (can be a decimal or ISO 8601)',0,'AbstractDataComponent','Sensor Web Enablement', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('uom', NULL, 'Sensor Web Enablement', 'Unit of measure', 0, 1,'TimeRange','UomIdentifier', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('constraint', NULL, 'Sensor Web Enablement', 'The constraint property defines the permitted values, as a range or enumerated list', 0, 1,'TimeRange','AllowedTimes', NULL, 'O',1 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('quality', NULL, 'Sensor Web Enablement', 'The quality property provides an indication of the reliability of estimates of the asociated value', 0, 1,'TimeRange','Quality', NULL, 'O',2 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'Sensor Web Enablement', 'Value is optional, to enable structure to act in a schema for values provided using other encodings', 0, 1,'TimeRange','TimePair', NULL, 'O',3 , 'Sensor Web Enablement','Sensor Web Enablement',' ');


/*-------------------------------------------------*
 *--------------  Classe AnyNumerical -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AnyNumerical',NULL,'Sensor Web Enablement','Re-usable group providing a choice of numeric data types',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'Sensor Web Enablement', NULL, 1, 1,'AnyNumerical','CharacterString', NULL, 'M',3 , 'ISO 19103','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('count', NULL, 'Sensor Web Enablement', NULL, 0, 1,'AnyNumerical','Count', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('quantity', NULL, 'Sensor Web Enablement', NULL, 0, 1,'AnyNumerical','Quantity', NULL, 'O',1 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('time', NULL, 'Sensor Web Enablement', NULL, 0, 1,'AnyNumerical','Time', NULL, 'O',2 , 'Sensor Web Enablement','Sensor Web Enablement',' ');

/*-------------------------------------------------*
 *--------------  Classe AbstractVector fin---------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Properties"  VALUES('referenceFrame', NULL, 'Sensor Web Enablement', 'Points to a spatial reference frame definition.', 0, 1,'AbstractVector','URI', NULL, 'O',0 , 'ISO 19103','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('localFrame', NULL, 'Sensor Web Enablement', 'Specifies the spatial frame which location and/or orientation is given by the enclosing vector', 0, 1,'AbstractVector','URI', NULL, 'O',1 , 'ISO 19103','Sensor Web Enablement',' ');



/*---------------------------------------------*
 *--------------  Classe Vector ---------------*
 *---------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Vector',NULL,'Sensor Web Enablement',NULL,0,'AnyData','Sensor Web Enablement', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('coordinate', NULL, 'Sensor Web Enablement', NULL, 1, 2147483647,'Vector','Quantity', NULL, 'M',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('definition', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Vector','URI', NULL, 'O',1 , 'ISO 19103','Sensor Web Enablement','P');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'ISO 19108', NULL, 0, 1,'Vector','URI', NULL, 'O',2 , 'ISO 19103','Sensor Web Enablement','C');


/*-------------------------------------------------*
 *--------------  Classe Document -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Document',NULL,'SensorML','Document record with date/time, version, author, etc.',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('description', NULL, 'ISO 19108', NULL, 0, 2147483647,'Document','CharacterString', NULL, 'O',0 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('date', NULL, 'SensorML', 'Date of creation', 0, 1,'Document','DateTime', NULL, 'O',1 , 'ISO 19108','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('contact', NULL, 'SensorML', 'Person who is responsible for the document', 0, 1,'Document','ResponsibleParty', NULL, 'O',2 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('format', NULL, 'SensorML', 'Specifies the fornat of the file pointed to by onlineResource', 0, 1,'Document','CharacterString', NULL, 'O',3 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('onlineResource', NULL, 'SensorML', 'Points to the actual document corresponding to that version', 0, 2147483647,'Document','OnlineResource', NULL, 'O',4 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('version', NULL, 'SensorML', NULL, 0, 1,'Document','CharacterString', NULL, 'O',5 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'SensorML', NULL, 0, 1,'Document','CharacterString', NULL, 'O',6 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('role', NULL, 'Xlink', NULL, 0, 1,'Document','CharacterString', NULL, 'O',7 , 'ISO 19103','SensorML','P');



/*-------------------------------------------------*
 *--------------  Classe Documentation ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Documentation',NULL,'SensorML','Relevant documentation for that object',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('role', NULL, 'Xlink', NULL, 0, 1,'Documentation','CharacterString', NULL, 'O',1 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('href', NULL, 'Xlink', NULL, 0, 1,'Documentation','CharacterString', NULL, 'O',2 , 'ISO 19103','SensorML','P');
 INSERT INTO "Schemas"."Properties"  VALUES('document', NULL, 'SensorML', NULL, 0, 2147483647,'Documentation','Document', NULL, 'O',0 , 'SensorML','SensorML',' ');

/*-------------------------------------------------*
 *--------------  Classe Rights -------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Rights',NULL,'SensorML',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('privacyAct', NULL, 'SensorML', NULL, 0, 1,'Rights','Boolean', NULL, 'O',0 , 'ISO 19103','SensorML','C');
 INSERT INTO "Schemas"."Properties"  VALUES('intellectualPropertyRights', NULL, 'SensorML', NULL, 0, 1,'Rights','Boolean', NULL, 'O',1 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('copyRights', NULL, 'SensorML', NULL, 0, 1,'Rights','Boolean', NULL, 'O',2 , 'ISO 19103','SensorML','C');
 INSERT INTO "Schemas"."Properties"  VALUES('documentation', NULL, 'SensorML', NULL, 0, 1,'Rights','Document', NULL, 'O',3 , 'SensorML','SensorML',' ');

/*-------------------------------------------------*
 *--------------  Classe DataComponent ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('DataComponent',NULL,'Sensor Web Enablement','Complex Type for all properties taking the AnyData Group',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('data', NULL, 'Sensor Web Enablement', NULL, 0, 1,'DataComponent','AnyData', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'Sensor Web Enablement', NULL, 1, 1,'DataComponent','CharacterString', NULL, 'M',1 , 'ISO 19103','Sensor Web Enablement',' ');

/*-------------------------------------------------*
 *--------------  Classe Term ---------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Term',NULL,'SensorML','A well defined token used to specify identifier and classifier values (single spaces allowed)',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('codeSpace', NULL, 'SensorML', NULL, 0, 1,'Term','CodeSpace', NULL, 'O',0 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'SensorML', NULL, 0, 1,'Term','CharacterString', NULL, 'O',1 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('definition', NULL, 'SensorML', 'Points to the term definition using a URI.', 0, 1,'Term','URI', NULL, 'O',2 , 'ISO 19103','SensorML','C');


/*-------------------------------------------------*
 *--------------  Classe Classifier     -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Classifier',NULL,'SensorML',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'SensorML', NULL, 0, 1,'Classifier','CharacterString', NULL, 'O',0 , 'ISO 19103','SensorML','P');
 INSERT INTO "Schemas"."Properties"  VALUES('term', NULL, 'SensorML', NULL, 0, 1,'Classifier','Term', NULL, 'O',1 , 'SensorML','SensorML','P');

/*-------------------------------------------------*
 *--------------  Classe Identifier     -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Identifier',NULL,'SensorML',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'SensorML', NULL, 0, 1,'Identifier','CharacterString', NULL, 'O',0 , 'ISO 19103','SensorML','P');
 INSERT INTO "Schemas"."Properties"  VALUES('term', NULL, 'SensorML', NULL, 0, 1,'Identifier','Term', NULL, 'O',1 , 'SensorML','SensorML','P');


/*-------------------------------------------------*
 *--------------  Classe KeywordList --------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('KeywordList',NULL,'SensorML',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('keyword', NULL, 'SensorML', NULL, 1, 2147483647,'KeywordList','CharacterString', NULL, 'M',0 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'SensorML', NULL, 0, 1,'KeywordList','CharacterString', NULL, 'O',1 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('codeSpace', NULL, 'SensorML', NULL, 0, 1,'KeywordList','URI', NULL, 'O',2 , 'ISO 19103','SensorML','C');


/*-------------------------------------------------*
 *--------------  Classe IdentifierList -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('IdentifierList',NULL,'SensorML','Means of providing various identity and alias values, with types such as "longName", "abbreviation", "modelNumber", "serialNumber", whose terms can be defined in a dictionary',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'SensorML', NULL, 0, 1,'IdentifierList','CharacterString', NULL, 'O',0 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('identifier', NULL, 'SensorML', NULL, 1, 2147483647,'IdentifierList','Identifier', NULL, 'M',1 , 'SensorML','SensorML',' ');


/*-------------------------------------------------*
 *--------------  Classe ClassifierList -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ClassifierList',NULL,'SensorML',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'SensorML', NULL, 0, 1,'ClassifierList','CharacterString', NULL, 'O',0 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('classifier', NULL, 'SensorML', NULL, 1, 2147483647,'ClassifierList','Classifier', NULL, 'M',1 , 'SensorML','SensorML',' ');

 /*-------------------------------------------------*
 *--------------  Classe LayerProperty ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('LayerProperty',NULL,'SensorML',NULL,1,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('dataRecord', NULL, 'SensorML', NULL, 0, 1,'LayerProperty','DataRecord', NULL, 'O',0 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('category', NULL, 'SensorML', NULL, 0, 1,'LayerProperty','Category', NULL, 'O',1 , 'Sensor Web Enablement','SensorML',' ');

/*-------------------------------------------------*
 *--------------  Classe BlockEncoding ------------*
 *-------------       a completer      ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('BlockEncoding',NULL,'Sensor Web Enablement',NULL,0,NULL,NULL, ' ');

/*-------------------------------------------------*
 *--------------  Classe DataBlockDefinition ------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('DataBlockDefinition',NULL,'Sensor Web Enablement',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('components', NULL, 'Sensor Web Enablement', NULL, 1, 1,'DataBlockDefinition','DataComponent', NULL, 'M',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('encoding', NULL, 'Sensor Web Enablement', NULL, 1, 1,'DataBlockDefinition','BlockEncoding', NULL, 'M',1 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'Sensor Web Enablement', NULL, 0, 1,'DataBlockDefinition','CharacterString', NULL, 'O',2 , 'ISO 19103','Sensor Web Enablement',' ');


 /*-------------------------------------------------*
 *--------------  Classe PresentationLayerProperty *
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PresentationLayerProperty',NULL,'SensorML',NULL,1,'LayerProperty','SensorML', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('dataBlockDefinition', NULL, 'SensorML', NULL, 0, 1,'PresentationLayerProperty','DataBlockDefinition', NULL, 'O',0 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('dataStreamDefinition', NULL, 'SensorML', NULL, 0, 1,'PresentationLayerProperty','DataBlockDefinition', NULL, 'O',1 , 'Sensor Web Enablement','SensorML',' ');



/*-------------------------------------------------*
 *--------------  Classe InterfaceDefinition ------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('InterfaceDefinition',NULL,'SensorML','Interface definition based on the OSI model. (http://en.wikipedia.org/wiki/OSI_model)',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('serviceLayer', NULL, 'SensorML', 'Layer 8 (not in OSI). Type of web service used to access the data. ', 0, 1,'InterfaceDefinition','LayerProperty', NULL, 'O',0 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('applicationLayer', NULL, 'SensorML', 'Layer 7 of the OSI model. Provides a means for the user to access information on the network through an application. ', 0, 1,'InterfaceDefinition','Category', NULL, 'O',1 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('presentationLayer', NULL, 'SensorML', 'Layer 6 of the OSI model. Transforms the data to provide a standard interface for the Application layer.)', 0, 1,'InterfaceDefinition','PresentationLayerProperty', NULL, 'O',2 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('sessionLayer', NULL, 'SensorML', 'Layer 5 of the OSI model.', 0, 1,'InterfaceDefinition','LayerProperty', NULL, 'O',3 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('transportLayer', NULL, 'SensorML', 'Layer 4 of the OSI model. Provides transparent transfer of data between end users and can control reliability of a given link.', 0, 1,'InterfaceDefinition','LayerProperty', NULL, 'O',4 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('networktLayer', NULL, 'SensorML', 'Layer 3 of the OSI model. Provides functional and procedural means of transfering data from source to destination via one or more networks while insURIng QoS.', 0, 1,'InterfaceDefinition','LayerProperty', NULL, 'O',5 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('dataLinkLayer', NULL, 'SensorML', 'Layer 2 of the OSI model. Provides functional and procedural means of transfering data between network entities and detecting/correcting errors.', 0, 1,'InterfaceDefinition','Category', NULL, 'O',6 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('physicalLayer', NULL, 'SensorML', 'Layer 1 of the OSI model. Provides all electrical and physical characteristics of the connection including pin layouts, voltages, cables specifcations,', 0, 1,'InterfaceDefinition','LayerProperty', NULL, 'O',7 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('mechanicalLayer', NULL, 'SensorML', 'Layer 0 (not is OSI). Type of connector used.', 0, 1,'InterfaceDefinition','LayerProperty', NULL, 'O',8 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'SensorML', NULL, 0, 1,'InterfaceDefinition','CharacterString', NULL, 'O',9 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'SensorML', NULL, 1, 1,'InterfaceDefinition','CharacterString', NULL, 'M',10 , 'ISO 19103','SensorML','P');




/*-------------------------------------------------*
 *--------------  Classe Interface ----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Interface',NULL,'SensorML','Interface useable to access System inputs and outputs',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('interfaceDefinition', NULL, 'SensorML', NULL, 1, 2147483647,'Interface','InterfaceDefinition', NULL, 'M',0 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'SensorML', NULL, 0, 1,'Interface','CharacterString', NULL, 'O',1 , 'ISO 19103','SensorML',' ');


/*-------------------------------------------------*
 *--------------  Classe InterfaceList ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('InterfaceList',NULL,'SensorML','List of interfaces useable to access System inputs and outputs',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('interface', NULL, 'SensorML', NULL, 1, 2147483647,'InterfaceList','Interface', NULL, 'M',0 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'SensorML', NULL, 0, 1,'InterfaceList','CharacterString', NULL, 'O',1 , 'ISO 19103','SensorML',' ');



/*-------------------------------------------------*
 *--------------  Classe AbstractDataArray --------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractDataArray',NULL,'Sensor Web Enablement','Implemetation of ISO-11404 Array datatype. This defines an array of identical data components with a elementCount. Values are given as a block and can be encoded in different ways',1,'AbstractDataComponent','Sensor Web Enablement', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('elementCount', NULL, 'Sensor Web Enablement', 'Specifies the size of the array (i.e. the number of elements of the defined type it contains)', 0, 1,'AbstractDataArray','Count', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');




/*-------------------------------------------------*
 *--------------  Classe AbstractMatrix -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractMatrix',NULL,'Sensor Web Enablement',NULL,1,'AbstractDataArray','Sensor Web Enablement', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('referenceFrame', NULL, 'Sensor Web Enablement', 'Points to a spatial reference frame definition. Coordinates of the vector will be expressed in this reference frame', 0, 1,'AbstractMatrix','URI', NULL, 'O',0 , 'ISO 19103','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('localFrame', NULL, 'Sensor Web Enablement', 'Specifies the spatial frame which location and/or orientation is given by the enclosing vector', 0, 1,'AbstractMatrix','URI', NULL, 'O',1 , 'ISO 19103','Sensor Web Enablement',' ');


/*-------------------------------------------------*
 *--------------  Classe Envelope fin -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Properties"  VALUES('time', NULL, 'Sensor Web Enablement', 'Optionally provides time range dURIng which this bounding envelope applies', 0, 1,'Envelope','TimeRange', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('lowerCorner', NULL, 'Sensor Web Enablement', NULL, 1, 1,'Envelope','Vector', NULL, 'M',1 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('upperCorner', NULL, 'Sensor Web Enablement', NULL, 1, 1,'Envelope','Vector', NULL, 'M',2 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('nil', NULL, 'XML Schema', NULL, 0, 1,'Envelope','Boolean', NULL, 'O',3 , 'ISO 19103','Sensor Web Enablement',' ');


/*-------------------------------------------------*
 *--------------  Classe SquareMatrix -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SquareMatrix',NULL,'Sensor Web Enablement','This is a square matrix (so the size is the square of one dimension) which is a DataArray of Quantities. It has a referenceFrame in which the matrix components are described',0,'AbstractMatrix','Sensor Web Enablement', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('elementType', NULL, 'Sensor Web Enablement', NULL, 1, 1,'SquareMatrix','Quantity', NULL, 'M',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('values', NULL, 'Sensor Web Enablement', 'Carries the block of values encoded as specified by the encoding element', 1, 1,'SquareMatrix','DataValue', NULL, 'M',1 , 'Sensor Web Enablement','Sensor Web Enablement',' ');



 /*-------------------------------------------------*
 *--------------  Classe StateData ----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('StateData',NULL,'Sensor Web Enablement',NULL,1,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('vector', NULL, 'Sensor Web Enablement', NULL, 0, 1,'StateData','Vector', NULL, 'O',0 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('squareMatrix', NULL, 'Sensor Web Enablement', NULL, 0, 1,'StateData','SquareMatrix', NULL, 'O',1 , 'Sensor Web Enablement','Sensor Web Enablement',' ');




/*-------------------------------------------------*
 *--------------  Classe Position -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Position',NULL,'Sensor Web Enablement','Position is given as a group of Vectors/Matrices, ',0,NULL,NULL, ' ');
INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'ISO 19108', NULL, 0, 1,'Position','CharacterString', NULL, 'O',0 , 'ISO 19103','Sensor Web Enablement','P');
INSERT INTO "Schemas"."Properties"  VALUES('fixed', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Position','Boolean', NULL, 'O',1 , 'ISO 19103','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('definition', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Position','URI', NULL, 'O',2 , 'ISO 19103','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('referenceFrame', NULL, 'Sensor Web Enablement', 'Points to a spatial reference frame definition. Coordinates of the vector will be expressed in this reference frame', 0, 1,'Position','URI', NULL, 'O',3 , 'ISO 19103','Sensor Web Enablement','C');
 INSERT INTO "Schemas"."Properties"  VALUES('localFrame', NULL, 'Sensor Web Enablement', 'Specifies the spatial frame which location and/or orientation is given by the enclosing vector', 0, 1,'Position','URI', NULL, 'O',4 , 'ISO 19103','Sensor Web Enablement','C');
 INSERT INTO "Schemas"."Properties"  VALUES('time', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Position','Time', NULL, 'O',5 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('location', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Position','Vector', NULL, 'O',6 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('orientation', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Position','StateData', NULL, 'O',7 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('velocity', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Position','Vector', NULL, 'O',8 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('angularVelocity', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Position','StateData', NULL, 'O',9 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('acceleration', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Position','Vector', NULL, 'O',10 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('accelerationVelocity', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Position','StateData', NULL, 'O',11 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('state', NULL, 'Sensor Web Enablement', NULL, 0, 1,'Position','StateData', NULL, 'O',12 , 'Sensor Web Enablement','Sensor Web Enablement',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'SensorML', NULL, 1, 1,'Position','CharacterString', NULL, 'M',13 , 'ISO 19103','Sensor Web Enablement','P');
 INSERT INTO "Schemas"."Properties"  VALUES('href', NULL, 'Xlink', NULL, 1, 1,'Position','CharacterString', NULL, 'O',14 , 'ISO 19103','Sensor Web Enablement','P');




/*-------------------------------------------------*
 *--------------  Classe PositionList -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PositionList',NULL,'SensorML','Relative positions of the System components',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('position', NULL, 'SensorML', NULL, 0, 2147483647,'PositionList','Position', NULL, 'O',0 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('timePosition', NULL, 'SensorML', NULL, 0, 1,'PositionList','TimePosition', NULL, 'O',1 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'SensorML', NULL, 0, 1,'PositionList','CharacterString', NULL, 'O',2 , 'ISO 19103','SensorML',' ');


 /*-------------------------------------------------*
 *--------------  Classe Event --------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Event',NULL,'SensorML','Event record (change to the object) including a date/time, description, identification and additional references and metadata',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('date', NULL, 'SensorML', 'Date/Time of event', 1, 1,'Event','DateTime', NULL, 'M',0 , 'ISO 19108','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('description', NULL, 'ISO 19108', NULL, 1, 1,'Event','CharacterString', NULL, 'M',1 , 'ISO 19103','SensorML',' ');



/*-------------------------------------------------*
 *--------------  Classe EventList ----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('EventList',NULL,'SensorML','List of events related to the enclosing object',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'Sensor Web Enablement', 'Value is optional, to enable structure to act in a schema for values provided using other encodings', 1, 1,'EventList','CharacterString', NULL, 'M',0 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'Sensor Web Enablement', 'Value is optional, to enable structure to act in a schema for values provided using other encodings', 1, 1,'EventList','CharacterString', NULL, 'M',1 , 'ISO 19103','SensorML',' ');


/*-------------------------------------------------*
 *--------------  Classe ObservableProperty -------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ObservableProperty',NULL,'Sensor Web Enablement','observableProperty should be used to identify (through reference only)',0,'AbstractDataComponent','Sensor Web Enablement', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'SensorML', NULL, 1, 1,'ObservableProperty','CharacterString', NULL, 'M',1 , 'ISO 19103','Sensor Web Enablement','P');
 INSERT INTO "Schemas"."Properties"  VALUES('definition', NULL, 'Sensor Web Enablement', NULL, 1, 1,'ObservableProperty','CharacterString', NULL, 'M',2 , 'ISO 19103','Sensor Web Enablement','C');


/*-------------------------------------------------*
 *--------------  Classe IoComponent --------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('IoComponent',NULL,'SensorML',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('data', NULL, 'SensorML', NULL, 0, 1,'IoComponent','AnyData', NULL, 'O',0 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('observableProperty', NULL, 'SensorML', NULL, 0, 1,'IoComponent','ObservableProperty', NULL, 'O',1 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'SensorML', NULL, 1, 1,'IoComponent','CharacterString', NULL, 'M',2 , 'ISO 19103','SensorML',' ');


/*-------------------------------------------------*
 *--------------  Classe InputList ----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('InputList',NULL,'SensorML','list of input signals',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('input', NULL, 'SensorML', NULL, 1, 2147483647,'InputList','ObservableProperty', NULL, 'M',0 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'SensorML', NULL, 0, 1,'InputList','CharacterString', NULL, 'M',1 , 'ISO 19103','SensorML',' ');


 /*-------------------------------------------------*
 *--------------  Classe OutputList ----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('OutputList',NULL,'SensorML','list of output signals',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('output', NULL, 'SensorML', NULL, 1, 2147483647,'OutputList','DataRecord', NULL, 'M',0 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'SensorML', NULL, 0, 1,'OutputList','CharacterString', NULL, 'M',1 , 'ISO 19103','SensorML',' ');



/*-------------------------------------------------*
 *--------------  Classe ArrayLink ----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ArrayLink',NULL,'SensorML','Special Link to handle accessing array elements sequentially',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('sourceArray', NULL, 'SensorML', NULL, 0, 1,'ArrayLink','LinkRef', NULL, 'O',0 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('destinationIndex', NULL, 'SensorML', NULL, 0, 2147483647,'ArrayLink','LinkRef', NULL, 'O',1 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('destinationArray', NULL, 'SensorML', NULL, 0, 1,'ArrayLink','LinkRef', NULL, 'O',2 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('sourceIndex', NULL, 'SensorML', NULL, 0, 1,'ArrayLink','LinkRef', NULL, 'O',3 , 'SensorML','SensorML',' ');


/*-------------------------------------------------*
 *--------------  Classe Connection     -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Connection',NULL,'SensorML','provides links between processes or between data sources and processes',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('link', NULL, 'SensorML', NULL, 1, 2147483647,'Connection','Link', NULL, 'M',0 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('arrayLink', NULL, 'SensorML', NULL, 1, 2147483647,'Connection','ArrayLink', NULL, 'M',0 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'SensorML', NULL, 1, 2147483647,'Connection','CharacterString', NULL, 'M',0 , 'ISO 19103','SensorML',' ');

 /*-------------------------------------------------*
 *--------------  Classe ConnectionList -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ConnectionList',NULL,'SensorML','provides links between processes or between data sources and processes',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('connection', NULL, 'SensorML', NULL, 1, 2147483647,'ConnectionList','Connection', NULL, 'M',0 , 'SensorML','SensorML',' ');


/*-------------------------------------------------*
 *--------------  Classe AbstractProcess fin ------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Properties"  VALUES('keywords', NULL, 'SensorML', NULL, 0, 1,'AbstractProcess','KeywordList', NULL, 'O',0 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('identification', NULL, 'SensorML', NULL, 0, 1,'AbstractProcess','IdentifierList', NULL, 'O',1 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('classification', NULL, 'SensorML', NULL, 0, 1,'AbstractProcess','ClassifierList', NULL, 'O',2 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('validTime', NULL, 'SensorML', NULL, 0, 1,'AbstractProcess','TimePeriod', NULL, 'O',3 , 'ISO 19108','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('legalConstraint', NULL, 'SensorML', NULL, 0, 2147483647,'AbstractProcess','Rights', NULL, 'O',5 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('characteristics', NULL, 'SensorML', NULL, 0, 2147483647,'AbstractProcess','AbstractDataRecord', NULL, 'O',6 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('capabilities', NULL, 'SensorML', NULL, 0, 2147483647,'AbstractProcess','AbstractDataRecord', NULL, 'O',7 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('contact', NULL, 'SensorML', NULL, 0, 2147483647,'AbstractProcess','ResponsibleParty', NULL, 'O',8 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('documentation', NULL, 'SensorML', NULL, 0, 2147483647,'AbstractProcess','Documentation', NULL, 'O',9 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('history', NULL, 'SensorML', NULL, 0, 2147483647,'AbstractProcess','EventList', NULL, 'O',10 , 'SensorML','SensorML',' ');



/*---------------------------------------------------*
 *--------------  Classe AbstractDerivableComponent -*
 *---------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"     VALUES('AbstractDerivableComponent',NULL, 'SensorML','Complex Type to allow creation of component profiles by extension',1,'AbstractProcess','SensorML', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('spatialReferenceFrame',     NULL, 'SensorML', NULL, 0, 1,'AbstractDerivableComponent','EngineeringCRS', NULL, 'O',1 , 'ISO 19108','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('temporalReferenceFrame',    NULL, 'SensorML', NULL, 0, 1,'AbstractDerivableComponent','TemporalCRS',    NULL, 'O',2 , 'ISO 19108','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('location',                  NULL, 'SensorML', NULL, 0, 1,'AbstractDerivableComponent','Point',          NULL, 'O',3 , 'ISO 19108','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('timePosition',              NULL, 'SensorML', NULL, 0, 1,'AbstractDerivableComponent','TimePosition',   NULL, 'O',4 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('interfaces',                NULL, 'SensorML', NULL, 0, 1,'AbstractDerivableComponent','InterfaceList',  NULL, 'O',5 , 'SensorML','SensorML',' ');

 /*-------------------------------------------------*
 *--------------  Classe AbstractComponent --------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"     VALUES('AbstractComponent',NULL,'SensorML','Complex Type for all generic components (soft typed inputs/outputs/parameters)',1,'AbstractDerivableComponent','SensorML', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('position',   NULL, 'SensorML', NULL, 0, 1,'AbstractComponent','Position',      NULL, 'O',1 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('inputs',     NULL, 'SensorML', NULL, 0, 1,'AbstractComponent','InputList',     NULL, 'O',2 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('outputs',    NULL, 'SensorML', NULL, 0, 1,'AbstractComponent','OutputList',    NULL, 'O',3 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('parameters', NULL, 'SensorML', NULL, 0, 1,'AbstractComponent','ParameterList', NULL, 'O',4 , 'SensorML','SensorML',' ');

 /*-------------------------------------------------*
 *--------------  Classe Component ----------------*
 *-------------------------------------------------*
 * duplicated propertie position to regonize type--*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"     VALUES('Component',NULL,'SensorML','Collection of subprocesses that can be chained using connections',0,'AbstractComponent','SensorML', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('position', NULL, 'SensorML', NULL, 0, 1,'Component','Position',      NULL, 'O',1 , 'Sensor Web Enablement','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('href', NULL, 'Xlink', NULL, 0, 1,'Component','CharacterString', NULL, 'O',2 , 'ISO 19103','SensorML','P');


/*-------------------------------------------------*
 *--------------  Classe ComponentPropertyType ----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"     VALUES('ComponentPropertyType',NULL,'SensorML','Collection of subprocesses that can be chained using connections',0,'AbstractComponent','SensorML', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('role', NULL, 'Xlink', NULL, 0, 1,'ComponentPropertyType','CharacterString', NULL, 'O',1 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('href', NULL, 'Xlink', NULL, 0, 1,'ComponentPropertyType','CharacterString', NULL, 'O',2 , 'ISO 19103','SensorML','P');
 INSERT INTO "Schemas"."Properties"  VALUES('process', NULL, 'SensorML', NULL, 1, 2147483647,'ComponentPropertyType','Component', NULL, 'M',0 , 'SensorML','SensorML',' ');


 /*-------------------------------------------------*
 *--------------  Classe ComponentList ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ComponentList',NULL,'SensorML',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('component', NULL, 'SensorML', NULL, 1, 2147483647,'ComponentList','ComponentPropertyType', NULL, 'M',0 , 'SensorML','SensorML',' ');

/*-------------------------------------------------*
 *--------------  Classe System -------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"     VALUES('System',NULL,'SensorML','System is a composite component containing sub-components.',0,'AbstractComponent','SensorML', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('components', NULL, 'SensorML', NULL, 0, 1,'System','ComponentList', NULL, 'O',0 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('positions', NULL, 'SensorML', NULL, 0, 1,'System','PositionList', NULL, 'O',1 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('connections', NULL, 'SensorML', NULL, 0, 1,'System','ConnectionList', NULL, 'O',2 , 'SensorML','SensorML',' ');

/*-------------------------------------------------*
 *--------------  Classe SensorML -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"     VALUES('Member',NULL,'SensorML','SensorML document root',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('role', NULL, 'Xlink', NULL, 1, 2147483647,'Member','CharacterString', NULL, 'O',0 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('href', NULL, 'Xlink', NULL, 1, 1,'Member','CharacterString', NULL, 'O',1 , 'ISO 19103','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('process', NULL, 'SensorML', NULL, 0, 1,'Member','AbstractSML', NULL, 'M',0 , 'SensorML','SensorML',' ');

/*-------------------------------------------------*
 *--------------  Classe SensorML -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"     VALUES('SensorML',NULL,'SensorML','SensorML document root',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('member', NULL, 'SensorML', NULL, 1, 2147483647,'SensorML','AbstractSML', NULL, 'M',0 , 'SensorML','SensorML',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('version', NULL, 'SensorML', NULL, 1, 1,'SensorML','CharacterString', NULL, 'M',1 , 'ISO 19103','SensorML',' ');



INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML', 'SensorML', 'SensorML', 'SensorML', NULL, 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member', 'member', 'SensorML', 'SensorML', 'SensorML:SensorML', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process', 'process', 'SensorML', 'Member', 'SensorML:SensorML:member', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:role', 'role', 'Xlink', 'Member', 'SensorML:SensorML:member', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:id', 'id', 'ISO 19108', 'AbstractSML', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:description', 'description', 'ISO 19108', 'AbstractSML', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:name', 'name', 'ISO 19108', 'AbstractSML', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:keywords', 'keywords', 'SensorML', 'AbstractProcess', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:keywords:codeSpace', 'codeSpace', 'SensorML', 'KeywordList', 'SensorML:SensorML:member:process:keywords', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:keywords:keyword', 'keyword', 'SensorML', 'KeywordList', 'SensorML:SensorML:member:process:keywords', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:identification', 'identification', 'SensorML', 'AbstractProcess', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:identification:identifier', 'identifier', 'SensorML', 'IdentifierList', 'SensorML:SensorML:member:process:identification', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:identification:identifier:name', 'name', 'SensorML', 'Identifier', 'SensorML:SensorML:member:process:identification:identifier', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:identification:identifier:term', 'term', 'SensorML', 'Identifier', 'SensorML:SensorML:member:process:identification:identifier', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:identification:identifier:term:definition', 'definition', 'SensorML', 'Term', 'SensorML:SensorML:member:process:identification:identifier:term', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:identification:identifier:term:codeSpace', 'codeSpace', 'SensorML', 'Term', 'SensorML:SensorML:member:process:identification:identifier:term', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:identification:identifier:term:codeSpace:href', 'href', 'Xlink', 'CodeSpace', 'SensorML:SensorML:member:process:identification:identifier:term:codeSpace', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:identification:identifier:term:value', 'value', 'SensorML', 'Term', 'SensorML:SensorML:member:process:identification:identifier:term', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:classification', 'classification', 'SensorML', 'AbstractProcess', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:classification:classifier', 'classifier', 'SensorML', 'ClassifierList', 'SensorML:SensorML:member:process:classification', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:classification:classifier:name', 'name', 'SensorML', 'Classifier', 'SensorML:SensorML:member:process:classification:classifier', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:classification:classifier:term', 'term', 'SensorML', 'Classifier', 'SensorML:SensorML:member:process:classification:classifier', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:classification:classifier:term:definition', 'definition', 'SensorML', 'Term', 'SensorML:SensorML:member:process:classification:classifier:term', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:classification:classifier:term:value', 'value', 'SensorML', 'Term', 'SensorML:SensorML:member:process:classification:classifier:term', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:classification:classifier:term:codeSpace', 'codeSpace', 'SensorML', 'Term', 'SensorML:SensorML:member:process:classification:classifier:term', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:classification:classifier:term:codeSpace:href', 'href', 'Xlink', 'CodeSpace', 'SensorML:SensorML:member:process:classification:classifier:term:codeSpace', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:legalConstraint', 'legalConstraint', 'SensorML', 'AbstractProcess', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:legalConstraint:copyRights', 'copyRights', 'SensorML', 'Rights', 'SensorML:SensorML:member:process:legalConstraint', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:legalConstraint:privacyAct', 'privacyAct', 'SensorML', 'Rights', 'SensorML:SensorML:member:process:legalConstraint', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:legalConstraint:documentation', 'documentation', 'SensorML', 'Rights', 'SensorML:SensorML:member:process:legalConstraint', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:legalConstraint:documentation:description', 'description', 'ISO 19108', 'Document', 'SensorML:SensorML:member:process:legalConstraint:documentation', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:characteristics', 'characteristics', 'SensorML', 'AbstractProcess', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:characteristics:definition', 'definition', 'Sensor Web Enablement', 'AbstractDataComponent', 'SensorML:SensorML:member:process:characteristics', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:characteristics:field', 'field', 'Sensor Web Enablement', 'DataRecord', 'SensorML:SensorML:member:process:characteristics', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:characteristics:field:name', 'name', 'SensorML', 'AnyData', 'SensorML:SensorML:member:process:characteristics:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:characteristics:field:field', 'field', 'Sensor Web Enablement', 'DataRecord', 'SensorML:SensorML:member:process:characteristics:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:characteristics:field:field:name', 'name', 'SensorML', 'AnyData', 'SensorML:SensorML:member:process:characteristics:field:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:characteristics:field:field:definition', 'definition', 'Sensor Web Enablement', 'AbstractDataComponent', 'SensorML:SensorML:member:process:characteristics:field:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:characteristics:field:field:uom', 'uom', 'Sensor Web Enablement', 'Quantity', 'SensorML:SensorML:member:process:characteristics:field:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:characteristics:field:field:uom:href', 'href', 'Xlink', 'Uom', 'SensorML:SensorML:member:process:characteristics:field:field:uom', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:characteristics:field:field:value', 'value', 'Sensor Web Enablement', 'Quantity', 'SensorML:SensorML:member:process:characteristics:field:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities', 'capabilities', 'SensorML', 'AbstractProcess', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:definition', 'definition', 'Sensor Web Enablement', 'AbstractDataComponent', 'SensorML:SensorML:member:process:capabilities', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:description', 'description', 'Sensor Web Enablement', 'AbstractDataComponent', 'SensorML:SensorML:member:process:capabilities', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field', 'field', 'Sensor Web Enablement', 'DataRecord', 'SensorML:SensorML:member:process:capabilities', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field:name', 'name', 'SensorML', 'AnyData', 'SensorML:SensorML:member:process:capabilities:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field:role', 'role', 'SensorML', 'AnyData', 'SensorML:SensorML:member:process:capabilities:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field:definition', 'definition', 'SensorML', 'AnyData', 'SensorML:SensorML:member:process:capabilities:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field:value', 'value', 'SensorML', 'AnyData', 'SensorML:SensorML:member:process:capabilities:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field:uom', 'uom', 'Sensor Web Enablement', 'DataRecord', 'SensorML:SensorML:member:process:capabilities:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field:uom:href', 'href', 'Xlink', 'Uom', 'SensorML:SensorML:member:process:capabilities:field:uom', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field:field', 'field', 'Sensor Web Enablement', 'DataRecord', 'SensorML:SensorML:member:process:capabilities:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field:field:name', 'name', 'SensorML', 'AnyData', 'SensorML:SensorML:member:process:capabilities:field:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field:field:definition', 'definition', 'Sensor Web Enablement', 'AbstractDataComponent', 'SensorML:SensorML:member:process:capabilities:field:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field:field:uom', 'uom', 'Sensor Web Enablement', 'Quantity', 'SensorML:SensorML:member:process:capabilities:field:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field:field:uom:href', 'href', 'Xlink', 'Uom', 'SensorML:SensorML:member:process:capabilities:field:field:uom', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:capabilities:field:field:value', 'value', 'Sensor Web Enablement', 'Quantity', 'SensorML:SensorML:member:process:capabilities:field:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:contact', 'contact', 'SensorML', 'AbstractProcess', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:contact:role', 'role', 'Xlink', 'ResponsibleParty', 'SensorML:SensorML:member:process:contact', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:contact:individualName', 'individualName', 'SensorML', 'ResponsibleParty', 'SensorML:SensorML:member:process:contact', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:contact:organizationName', 'organizationName', 'SensorML', 'ResponsibleParty', 'SensorML:SensorML:member:process:contact', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:contact:contactInfo', 'contactInfo', 'SensorML', 'ResponsibleParty', 'SensorML:SensorML:member:process:contact', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:contact:contactInfo:address', 'address', 'SensorML', 'Contact', 'SensorML:SensorML:member:process:contact:contactInfo', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:contact:contactInfo:address:deliveryPoint', 'deliveryPoint', 'SensorML', 'Address', 'SensorML:SensorML:member:process:contact:contactInfo:address', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:contact:contactInfo:address:city', 'city', 'SensorML', 'Address', 'SensorML:SensorML:member:process:contact:contactInfo:address', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:contact:contactInfo:address:electronicMailAddress', 'electronicMailAddress', 'SensorML', 'Address', 'SensorML:SensorML:member:process:contact:contactInfo:address', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:documentation', 'documentation', 'SensorML', 'AbstractProcess', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:documentation:role', 'role', 'Xlink', 'Document', 'SensorML:SensorML:member:process:documentation', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:documentation:description', 'description', 'ISO 19108', 'Document', 'SensorML:SensorML:member:process:documentation', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:documentation:format', 'format', 'SensorML', 'Document', 'SensorML:SensorML:member:process:documentation', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:documentation:onlineResource', 'onlineResource', 'SensorML', 'Document', 'SensorML:SensorML:member:process:documentation', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:documentation:onlineResource:href', 'href', 'Xlink', 'OnlineResource', 'SensorML:SensorML:member:process:documentation:onlineResource', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:spatialReferenceFrame', 'spatialReferenceFrame', 'SensorML', 'AbstractDerivableComponent', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:spatialReferenceFrame:id', 'id', 'ISO 19108', 'EngineeringCRS', 'SensorML:SensorML:member:process:spatialReferenceFrame', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:spatialReferenceFrame:srsName', 'srsName', 'ISO 19108', 'EngineeringCRS', 'SensorML:SensorML:member:process:spatialReferenceFrame', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:spatialReferenceFrame:usesCS', 'usesCS', 'ISO 19108', 'EngineeringCRS', 'SensorML:SensorML:member:process:spatialReferenceFrame', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:spatialReferenceFrame:usesCS:href', 'href', 'Xlink', 'SimpleLink', 'SensorML:SensorML:member:process:spatialReferenceFrame:usesCS', 'Xlink');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:spatialReferenceFrame:usesEngineeringDatum', 'usesEngineeringDatum', 'ISO 19108', 'EngineeringCRS', 'SensorML:SensorML:member:process:spatialReferenceFrame', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:spatialReferenceFrame:usesEngineeringDatum:id', 'id', 'ISO 19108', 'EngineeringDatum', 'SensorML:SensorML:member:process:spatialReferenceFrame:usesEngineeringDatum', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:spatialReferenceFrame:usesEngineeringDatum:datumName', 'datumName', 'ISO 19108', 'EngineeringDatum', 'SensorML:SensorML:member:process:spatialReferenceFrame:usesEngineeringDatum', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:spatialReferenceFrame:usesEngineeringDatum:anchorPoint', 'anchorPoint', 'ISO 19108', 'EngineeringDatum', 'SensorML:SensorML:member:process:spatialReferenceFrame:usesEngineeringDatum', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:temporalReferenceFrame', 'temporalReferenceFrame', 'SensorML', 'AbstractDerivableComponent', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:temporalReferenceFrame:id', 'id', 'ISO 19108', 'TemporalCRS', 'SensorML:SensorML:member:process:temporalReferenceFrame', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:temporalReferenceFrame:srsName', 'srsName', 'ISO 19108', 'TemporalCRS', 'SensorML:SensorML:member:process:temporalReferenceFrame', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:temporalReferenceFrame:usesTemporalCS', 'usesTemporalCS', 'ISO 19108', 'TemporalCRS', 'SensorML:SensorML:member:process:temporalReferenceFrame', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:temporalReferenceFrame:usesTemporalCS:href', 'href', 'Xlink', 'SimpleLink', 'SensorML:SensorML:member:process:temporalReferenceFrame:usesTemporalCS', 'Xlink');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:temporalReferenceFrame:usesTemporalDatum', 'usesTemporalDatum', 'ISO 19108', 'TemporalCRS', 'SensorML:SensorML:member:process:temporalReferenceFrame', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:temporalReferenceFrame:usesTemporalDatum:href', 'href', 'Xlink', 'SimpleLink', 'SensorML:SensorML:member:process:temporalReferenceFrame:usesTemporalDatum', 'Xlink');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:location', 'location', 'SensorML', 'AbstractDerivableComponent', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:location:id', 'id', 'ISO 19108', 'Point', 'SensorML:SensorML:member:process:location', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:location:pos', 'pos', 'ISO 19108', 'Point', 'SensorML:SensorML:member:process:location', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:location:pos:srsDimension', 'srsDimension', 'ISO 19108', 'Pos', 'SensorML:SensorML:member:process:location:pos', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:location:pos:srsName', 'srsName', 'ISO 19108', 'Pos', 'SensorML:SensorML:member:process:location:pos', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:interfaces', 'interfaces', 'SensorML', 'AbstractDerivableComponent', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:interfaces:interface', 'interface', 'SensorML', 'InterfaceList', 'SensorML:SensorML:member:process:interfaces', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:interfaces:interface:name', 'name', 'SensorML', 'Interface', 'SensorML:SensorML:member:process:interfaces:interface', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition', 'interfaceDefinition', 'SensorML', 'Interface', 'SensorML:SensorML:member:process:interfaces:interface', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition:applicationLayer', 'applicationLayer', 'SensorML', 'InterfaceDefinition', 'SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition:applicationLayer:definition', 'definition', 'Sensor Web Enablement', 'AbstractDataComponent', 'SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition:applicationLayer', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition:applicationLayer:value', 'value', 'Sensor Web Enablement', 'Category', 'SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition:applicationLayer', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition:dataLinkLayer', 'dataLinkLayer', 'SensorML', 'InterfaceDefinition', 'SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition:dataLinkLayer:definition', 'definition', 'Sensor Web Enablement', 'AbstractDataComponent', 'SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition:dataLinkLayer', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition:dataLinkLayer:value', 'value', 'Sensor Web Enablement', 'Category', 'SensorML:SensorML:member:process:interfaces:interface:interfaceDefinition:dataLinkLayer', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:inputs', 'inputs', 'SensorML', 'AbstractComponent', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:inputs:input', 'input', 'SensorML', 'InputList', 'SensorML:SensorML:member:process:inputs', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:inputs:input:name', 'name', 'SensorML', 'ObservableProperty', 'SensorML:SensorML:member:process:inputs:input', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:inputs:input:definition', 'definition', 'Sensor Web Enablement', 'ObservableProperty', 'SensorML:SensorML:member:process:inputs:input', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:outputs', 'outputs', 'SensorML', 'AbstractComponent', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:outputs:output', 'output', 'SensorML', 'OutputList', 'SensorML:SensorML:member:process:outputs', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:outputs:output:name', 'name', 'SensorML', 'AnyData', 'SensorML:SensorML:member:process:outputs:output', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:outputs:output:id', 'id', 'ISO 19108', 'DataRecord', 'SensorML:SensorML:member:process:outputs:output', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:outputs:output:field', 'field', 'Sensor Web Enablement', 'DataRecord', 'SensorML:SensorML:member:process:outputs:output', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:outputs:output:field:name', 'name', 'SensorML', 'AnyData', 'SensorML:SensorML:member:process:outputs:output:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:outputs:output:field:definition', 'definition', 'Sensor Web Enablement', 'AbstractDataComponent', 'SensorML:SensorML:member:process:outputs:output:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:outputs:output:field:uom', 'uom', 'Sensor Web Enablement', 'Time', 'SensorML:SensorML:member:process:outputs:output:field', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:outputs:output:field:uom:href', 'href', 'Xlink', 'Uom', 'SensorML:SensorML:member:process:outputs:output:field:uom', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:outputs:output:field:uom:code', 'code', 'Sensor Web Enablement', 'Uom', 'SensorML:SensorML:member:process:outputs:output:field:uom', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:components', 'components', 'SensorML', 'System', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:components:component', 'component', 'SensorML', 'ComponentList', 'SensorML:SensorML:member:process:components', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:components:component:name', 'name', 'ISO 19108', 'AbstractSML', 'SensorML:SensorML:member:process:components:component', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:components:component:href', 'href', 'Xlink', 'Component', 'SensorML:SensorML:member:process:components:component', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:components:component:role', 'role', 'Xlink', 'AbstractSML', 'SensorML:SensorML:member:process:components:component', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions', 'positions', 'SensorML', 'System', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position', 'position', 'SensorML', 'PositionList', 'SensorML:SensorML:member:process:positions', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position:name', 'name', 'SensorML', 'Position', 'SensorML:SensorML:member:process:positions:position', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position:localFrame', 'localFrame', 'Sensor Web Enablement', 'Position', 'SensorML:SensorML:member:process:positions:position', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position:referenceFrame', 'referenceFrame', 'Sensor Web Enablement', 'Position', 'SensorML:SensorML:member:process:positions:position', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position:location', 'location', 'Sensor Web Enablement', 'Position', 'SensorML:SensorML:member:process:positions:position', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position:location:definition', 'definition', 'Sensor Web Enablement', 'Vector', 'SensorML:SensorML:member:process:positions:position:location', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position:location:coordinate', 'coordinate', 'Sensor Web Enablement', 'Vector', 'SensorML:SensorML:member:process:positions:position:location', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position:location:coordinate:name', 'name', 'SensorML', 'AnyData', 'SensorML:SensorML:member:process:positions:position:location:coordinate', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position:location:coordinate:axisID', 'axisID', 'Sensor Web Enablement', 'Quantity', 'SensorML:SensorML:member:process:positions:position:location:coordinate', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position:location:coordinate:definition', 'definition', 'Sensor Web Enablement', 'AbstractDataComponent', 'SensorML:SensorML:member:process:positions:position:location:coordinate', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position:location:coordinate:uom', 'uom', 'Sensor Web Enablement', 'Quantity', 'SensorML:SensorML:member:process:positions:position:location:coordinate', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position:location:coordinate:uom:code', 'code', 'Sensor Web Enablement', 'Uom', 'SensorML:SensorML:member:process:positions:position:location:coordinate:uom', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:positions:position:location:coordinate:value', 'value', 'Sensor Web Enablement', 'Quantity', 'SensorML:SensorML:member:process:positions:position:location:coordinate', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:connections', 'connections', 'SensorML', 'System', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:connections:connection', 'connection', 'SensorML', 'ConnectionList', 'SensorML:SensorML:member:process:connections', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:connections:connection:name', 'name', 'SensorML', 'Connection', 'SensorML:SensorML:member:process:connections:connection', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:connections:connection:link', 'link', 'SensorML', 'Connection', 'SensorML:SensorML:member:process:connections:connection', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:connections:connection:link:source', 'source', 'SensorML', 'Link', 'SensorML:SensorML:member:process:connections:connection:link', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:connections:connection:link:source:ref', 'ref', 'SensorML', 'LinkRef', 'SensorML:SensorML:member:process:connections:connection:link:source', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:connections:connection:link:destination', 'destination', 'SensorML', 'Link', 'SensorML:SensorML:member:process:connections:connection:link', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:connections:connection:link:destination:ref', 'ref', 'SensorML', 'LinkRef', 'SensorML:SensorML:member:process:connections:connection:link:destination', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:validTime', 'validTime', 'SensorML', 'AbstractProcess', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:validTime:beginPosition', 'beginPosition', 'ISO 19108', 'TimePeriod', 'SensorML:SensorML:member:process:validTime', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:validTime:endPosition', 'endPosition', 'ISO 19108', 'TimePeriod', 'SensorML:SensorML:member:process:validTime', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:validTime:endPosition:indeterminatePosition', 'indeterminatePosition', 'ISO 19108', 'UndefinedTime', 'SensorML:SensorML:member:process:validTime:endPosition', 'ISO 19108');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:position', 'position', 'SensorML', 'Component', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:position:name', 'name', 'SensorML', 'Position', 'SensorML:SensorML:member:process:position', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:position:href', 'href', 'Xlink', 'Position', 'SensorML:SensorML:member:process:position', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:outputs:output:definition', 'definition', 'Sensor Web Enablement', 'ObservableProperty', 'SensorML:SensorML:member:process:outputs:output', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:parameters', 'parameters', 'SensorML', 'AbstractComponent', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:parameters:parameter', 'parameter', 'SensorML', 'ParameterList', 'SensorML:SensorML:member:process:parameters', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:parameters:parameter:name', 'name', 'SensorML', 'AnyData', 'SensorML:SensorML:member:process:parameters:parameter', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:parameters:parameter:role', 'role', 'Xlink', 'AnyData', 'SensorML:SensorML:member:process:parameters:parameter', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:parameters:parameter:definition', 'definition', 'Sensor Web Enablement', 'AbstractDataComponent', 'SensorML:SensorML:member:process:parameters:parameter', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:parameters:parameter:uom', 'uom', 'Sensor Web Enablement', 'Quantity', 'SensorML:SensorML:member:process:parameters:parameter', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:parameters:parameter:uom:href', 'href', 'Xlink', 'Uom', 'SensorML:SensorML:member:process:parameters:parameter:uom', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:parameters:parameter:value', 'value', 'Sensor Web Enablement', 'Quantity', 'SensorML:SensorML:member:process:parameters:parameter', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:parameters:parameter:uom:code', 'code', 'Sensor Web Enablement', 'Uom', 'SensorML:SensorML:member:process:parameters:parameter:uom', 'Sensor Web Enablement');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:boundedBy', 'boundedBy', 'ISO 19108', 'AbstractSML', 'SensorML:SensorML:member:process', 'SensorML');
INSERT INTO "Schemas"."Paths" ("id", "name", "standard", "owner", "parent", "owner_Standard") VALUES ('SensorML:SensorML:member:process:boundedBy:nil', 'nil', 'XML Schema', 'Envelope', 'SensorML:SensorML:member:process:boundedBy', 'Sensor Web Enablement');