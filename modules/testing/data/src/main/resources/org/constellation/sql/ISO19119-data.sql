/*---------------------------------------------*
 *--------------  Standard ISO 19119 ----------*
 *---------------------------------------------*/
INSERT INTO "Schemas"."Standard"  VALUES('ISO 19119','srv');

/*-------------------------------------------------*
 *--------------  Classe SV_ServiceType -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_ServiceType',NULL,'ISO 19119','Provides the abstract definition of a specific type of service but does not specify the implementation of the service.',0,NULL,NULL, ' ');


/*-------------------------------------------------*
 *--------------  CodeList SV_CouplingType --------*
 *-------------------------------------------------*/
INSERT INTO "Schemas"."CodeLists" VALUES ('SV_CouplingType', 'CouplingType', 'ISO 19119', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeListElements" VALUES ('loose', NULL, 'ISO 19119', NULL, 0, 1, 'SV_CouplingType', 'SV_CouplingType', 'SV_CouplingType', 'C', 0, 'ISO 19119', 'ISO 19119', ' ', 1);
INSERT INTO "Schemas"."CodeListElements" VALUES ('mixed', NULL, 'ISO 19119', NULL, 0, 1, 'SV_CouplingType', 'SV_CouplingType', 'SV_CouplingType', 'C', 0, 'ISO 19119', 'ISO 19119', ' ', 2);
INSERT INTO "Schemas"."CodeListElements" VALUES ('tight', NULL, 'ISO 19119', NULL, 0, 1, 'SV_CouplingType', 'SV_CouplingType', 'SV_CouplingType', 'C', 0, 'ISO 19119', 'ISO 19119', ' ', 3);

/*-------------------------------------------------*
 *--------------  CodeList DCPList -------*
 *-------------------------------------------------*/
INSERT INTO "Schemas"."CodeLists" VALUES ('DCPList', 'DCPList', 'ISO 19119', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeListElements" VALUES ('XML', NULL, 'ISO 19119', NULL, 0, 1, 'DCPList', 'DCPList', 'DCPList', 'C', 0, 'ISO 19119', 'ISO 19119', ' ', 1);
INSERT INTO "Schemas"."CodeListElements" VALUES ('CORBA', NULL, 'ISO 19119', NULL, 0, 1, 'DCPList', 'DCPList', 'DCPList', 'C', 0, 'ISO 19119', 'ISO 19119', ' ', 2);
INSERT INTO "Schemas"."CodeListElements" VALUES ('JAVA', NULL, 'ISO 19119', NULL, 0, 1, 'DCPList', 'DCPList', 'DCPList', 'C', 0, 'ISO 19119', 'ISO 19119', ' ', 3);
INSERT INTO "Schemas"."CodeListElements" VALUES ('COM', NULL, 'ISO 19119', NULL, 0, 1, 'DCPList', 'DCPList', 'DCPList', 'C', 0, 'ISO 19119', 'ISO 19119', ' ', 4);
INSERT INTO "Schemas"."CodeListElements" VALUES ('SQL', NULL, 'ISO 19119', NULL, 0, 1, 'DCPList', 'DCPList', 'DCPList', 'C', 0, 'ISO 19119', 'ISO 19119', ' ', 5);
INSERT INTO "Schemas"."CodeListElements" VALUES ('WebServices', NULL, 'ISO 19119', NULL, 0, 1, 'DCPList', 'DCPList', 'DCPList', 'C', 0, 'ISO 19119', 'ISO 19119', ' ', 6);
/*-------------------------------------------------*
 *--------------  CodeList SV_ServiceTypeCode -------*
 *-------------------------------------------------*/
INSERT INTO "Schemas"."CodeLists" VALUES ('SV_ServiceTypeCode', 'ServiceTypeCd', 'MDWEB', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeListElements" VALUES ('discovery', NULL, 'MDWEB', NULL, 0, 1, 'SV_ServiceTypeCode', 'SV_ServiceTypeCode', 'SV_ServiceTypeCode', 'C', 0, 'MDWEB', 'MDWEB', ' ', 1);
INSERT INTO "Schemas"."CodeListElements" VALUES ('view', NULL, 'MDWEB', NULL, 0, 1, 'SV_ServiceTypeCode', 'SV_ServiceTypeCode', 'SV_ServiceTypeCode', 'C', 0, 'MDWEB', 'MDWEB', ' ', 2);
INSERT INTO "Schemas"."CodeListElements" VALUES ('download', NULL, 'MDWEB', NULL, 0, 1, 'SV_ServiceTypeCode', 'SV_ServiceTypeCode', 'SV_ServiceTypeCode', 'C', 0, 'MDWEB', 'MDWEB', ' ', 3);
INSERT INTO "Schemas"."CodeListElements" VALUES ('transformation', NULL, 'MDWEB', NULL, 0, 1, 'SV_ServiceTypeCode', 'SV_ServiceTypeCode', 'SV_ServiceTypeCode', 'C', 0, 'MDWEB', 'MDWEB', ' ', 4);
INSERT INTO "Schemas"."CodeListElements" VALUES ('invoke', NULL, 'MDWEB', NULL, 0, 1, 'SV_ServiceTypeCode', 'SV_ServiceTypeCode', 'SV_ServiceTypeCode', 'C', 0, 'MDWEB', 'MDWEB', ' ', 5);
INSERT INTO "Schemas"."CodeListElements" VALUES ('other', NULL, 'MDWEB', NULL, 0, 1, 'SV_ServiceTypeCode', 'SV_ServiceTypeCode', 'SV_ServiceTypeCode', 'C', 0, 'MDWEB', 'MDWEB', ' ', 6);

/*-------------------------------------------------*
 *--------------  Classe SV_CoupledResource -------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_CoupledResource',NULL,'ISO 19119',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('operationName', NULL, 'ISO 19119', 'The name of the service operation', 1, 1,'SV_CoupledResource','CharacterString', NULL, 'M',0 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('identifier', NULL, 'ISO 19119', 'name of the identifier of a given tightly coupled dataset.', 1, 1,'SV_CoupledResource','CharacterString', NULL, 'M',1 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('scopedName', NULL, 'ISO 19119', NULL, 1, 1,'SV_CoupledResource','ScopedName', NULL, 'M',2 , 'ISO 19103','ISO 19119', ' ');

/*-------------------------------------------------*
 *--------------  Classe SV_ParameterDirection ----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_ParameterDirection',NULL,'ISO 19119',NULL,1,NULL,NULL, 'E');
 INSERT INTO "Schemas"."Properties"  VALUES('in', NULL, 'ISO 19119', 'the parameter is an input parameter to the service instance.', 0, 1,'SV_ParameterDirection','CharacterString', NULL, 'O',0 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('out', NULL, 'ISO 19119', 'the parameter is an output parameter to the service instance.', 0, 1,'SV_ParameterDirection','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('in/out', NULL, 'ISO 19119', 'the parameter is both an input and output parameter to the service instance', 0, 1,'SV_ParameterDirection','CharacterString', NULL, 'O',2 , 'ISO 19103','ISO 19119', ' ');


/*-------------------------------------------------*
 *--------------  Classe SV_Parameter -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_Parameter',NULL,'ISO 19119',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'ISO 19119', 'The name, as used by the service for this parameter. ', 1, 1,'SV_Parameter','MemberName', NULL, 'M',0 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('direction', NULL, 'ISO 19119', ' indication if the parameter is an input to the service, an output or both.', 0, 1,'SV_Parameter','SV_ParameterDirection', NULL, 'O',1 , 'ISO 19119','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('description', NULL, 'ISO 19119', ' a narrative explanation of the role of the parameter.', 0, 1,'SV_Parameter','CharacterString', NULL, 'O',2 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('optionality', NULL, 'ISO 19119', 'indication if the parameter is required.', 0, 1,'SV_Parameter','CharacterString', NULL, 'O',3 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('repeatability', NULL, 'ISO 19119', ' indication if more than one value of the parameter may be provided.', 1, 1,'SV_Parameter','Boolean', NULL, 'M',4 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('valueType', NULL, 'ISO 19119', NULL, 1, 1,'SV_Parameter','TypeName', NULL, 'M',5 , 'ISO 19103','ISO 19119', ' ');


/*-------------------------------------------------*
 *--------------  Classe SV_Operation -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_Operation',NULL,'ISO 19119',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('operationName', NULL, 'ISO 19119', 'The name of the operation.', 1, 1,'SV_Operation','MemberName', NULL, 'M',0 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('dependsOn', NULL, 'ISO 19119', 'A list of operation on witch the operation depends on. ', 0, 2147483647,'SV_Operation','SV_Operation', NULL, 'O',1 , 'ISO 19119','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('parameter', NULL, 'ISO 19119', 'The parameter of the operation.', 0, 1,'SV_Operation','SV_Parameter', NULL, 'O',2 , 'ISO 19119','ISO 19119', ' ');

/*-------------------------------------------------*
 *--------------  Classe SV_Port ------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_Port',NULL,'ISO 19119',NULL,0,NULL,NULL, ' ');


/*-------------------------------------------------*
 *--------------  Classe SV_Interface -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_Interface',NULL,'ISO 19119','Named Set of operations that characterize the behaviour of an entity.',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('typeName', NULL, 'ISO 19119', NULL, 1, 1,'SV_Interface','TypeName', NULL, 'M',0 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('theSV_Port', NULL, 'ISO 19119', NULL, 0, 2147483647,'SV_Interface','SV_Port', NULL, 'O',1 , 'ISO 19119','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('operation', NULL, 'ISO 19119', NULL, 1, 1,'SV_Interface','SV_Operation', NULL, 'M',2 , 'ISO 19119','ISO 19119', ' ');

 /*-------------- suite de SV_Port ---------------*/
 INSERT INTO "Schemas"."Properties"  VALUES('theSV_interface', NULL, 'ISO 19119', NULL, 0, 2147483647,'SV_Port','SV_Interface', NULL, 'O',0 , 'ISO 19119','ISO 19119', ' ');


/*-------------------------------------------------*
 *--------------  Classe SV_OperationChain --------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_OperationChain',NULL,'ISO 19119',NULL,0,NULL,NULL , ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'ISO 19119', 'The name of the operation.', 1, 1,'SV_OperationChain','CharacterString', NULL, 'M',0 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('description', NULL, 'ISO 19119', NULL, 0, 1,'SV_OperationChain','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('operation', NULL, 'ISO 19119', NULL, 1, 2147483647,'SV_OperationChain','SV_Operation', NULL, 'M',2 , 'ISO 19119','ISO 19119', ' ');

/*-------------------------------------------------*
 *--------------  Classe SV_OperationMetadata -----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_OperationMetadata',NULL,'ISO 19119',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('operationName', NULL, 'ISO 19119', 'a unique identifier for this operation.', 1, 1,'SV_OperationMetadata','CharacterString', NULL, 'M',0 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('DCP', NULL, 'ISO 19119', 'Distributed computing platforms on which the operation has been implemented. ', 1, 2147483647,'SV_OperationMetadata',NULL, 'DCPList', 'M',1 , 'ISO 19119','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('operationDescription', NULL, 'ISO 19119', 'Free text description of the intent of the operation and the results of the operation. ', 0, 1,'SV_OperationMetadata','CharacterString', NULL, 'O',2 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('invocationName', NULL, 'ISO 19119', 'the name used to invok this interface within the context of the DCP. The name is identical for all DCPs.', 0, 1,'SV_OperationMetadata','CharacterString', NULL, 'O',3 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('parameters', NULL, 'ISO 19119', 'The parameters that are required for this interface.', 0, 2147483647,'SV_OperationMetadata','SV_Parameter', NULL, 'O',4 , 'ISO 19119','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('connectPoint', NULL, 'ISO 19119', 'handle for accesing the service interface.', 1, 2147483647,'SV_OperationMetadata','CI_OnlineResource', NULL, 'M',5 , 'ISO 19115','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('dependsOn', NULL, 'ISO 19119', 'list of operation that must be completed immediatly before current operation is invoked, structured as a list for capturing alternate predecesor path and sets for capturing parallel predecessor paths.', 0, 2147483647,'SV_OperationMetadata','SV_OperationMetadata', NULL, 'O',6 , 'ISO 19119','ISO 19119', ' ');


/*-------------------------------------------------*
 *--------------  Classe SV_OperationChainMetadata *
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_OperationChainMetadata',NULL,'ISO 19119',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'ISO 19119', 'The name, as used by the service for this chain.', 1, 1,'SV_OperationChainMetadata','CharacterString', NULL, 'M',0 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('description', NULL, 'ISO 19119', 'a narrative explanation of the service in the chain and resulting output.', 0, 1,'SV_OperationChainMetadata','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('operation', NULL, 'ISO 19119', 'The operations composing the chain.', 0, 2147483647,'SV_OperationChainMetadata','SV_OperationMetadata', NULL, 'O',2 , 'ISO 19119','ISO 19119', ' ');


/*-------------------------------------------------*
 *--------------  Classe SV_ServiceProvider -------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_ServiceProvider',NULL,'ISO 19119','deleted in amd.1:2008  ',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('serviceContact', NULL, 'ISO 19119', NULL, 1, 2147483647,'SV_ServiceProvider','CI_ResponsibleParty', NULL, 'M',0 , 'ISO 19115','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('providerName', NULL, 'ISO 19119', 'A unique identifier for the service provider organization.', 1, 1,'SV_ServiceProvider','CharacterString', NULL, 'M',1 , 'ISO 19103','ISO 19119', ' ');



/*-------------------------------------------------*
 *--------------  Classe SV_ServiceIdentification -*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_ServiceIdentification',NULL,'ISO 19119',NULL,0,'MD_Identification','ISO 19115', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('serviceType', NULL, 'ISO 19119',' A service type name from a registry of services. For examples, the values of the namespace and name attributes of GeneralName may be "OGC" and catalogue.', 1, 1,'SV_ServiceIdentification','GenericName', NULL, 'M',0 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('serviceTypeVersion', NULL, 'ISO 19119', 'Provides for searching based on the version of serviceType. For example, we may only be interested in OGC Catalogue v1.1 services.  If version is maintained as a separate attribute users can easily search for all services of a type regardless of the version.', 0, 2147483647,'SV_ServiceIdentification','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('accessProperties', NULL, 'ISO 19119', ' information about the availability of the service, including: - fees  - planned available date and time  - ordering - turnaround', 0, 1,'SV_ServiceIdentification','MD_StandardOrderProcess', NULL, 'O',2 , 'ISO 19115','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('restrictions', NULL, 'ISO 19119', 'legal and securty constraints on accessing the service and distributing data generated by the service.', 0, 1,'SV_ServiceIdentification','MD_Constraints', NULL, 'O',3 , 'ISO 19115','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('containsOperations', NULL, 'ISO 19119', 'provides information about the operations that comprise the service. ', 1, 2147483647,'SV_ServiceIdentification','SV_OperationMetadata', NULL, 'M',4 , 'ISO 19119','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('operatesOn', NULL, 'ISO 19119', 'Provides information on the datasets that the service operates on. ', 0, 2147483647,'SV_ServiceIdentification','MD_DataIdentification', NULL, 'O',5 , 'ISO 19115','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('extent', NULL, 'ISO 19119', 'The geographic/temporal region where the service is valid including the bounding box, bounding polygon, vertical or temporal extent of the service.', 0, 2147483647,'SV_ServiceIdentification','EX_Extent', NULL, 'O',6 , 'ISO 19115','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('coupledResource', NULL, 'ISO 19119', 'further description of the data coupling in the case of tightly coupled services.', 0, 2147483647,'SV_ServiceIdentification','SV_CoupledResource', NULL, 'O',7 , 'ISO 19119','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('couplingType', NULL, 'ISO 19119', 'type of coupling between service and associated data (if exists)', 0, 1,'SV_ServiceIdentification',NULL, 'SV_CouplingType', 'O',8 , 'ISO 19119','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('provider', NULL, 'ISO 19119', 'deleted in amd.1:2008  ', 0, 2147483647,'SV_ServiceIdentification','SV_ServiceProvider', NULL, 'O',9 , 'ISO 19119','ISO 19119', ' ');


/*-------------- suite serviceProvider -----------*/
INSERT INTO "Schemas"."Properties"  VALUES('services', NULL, 'ISO 19119', NULL, 1, 2147483647,'SV_ServiceProvider','SV_ServiceIdentification', NULL, 'M',2 , 'ISO 19119','ISO 19119', ' ');

/*-------------------------------------------------*
 *--------------  Classe SV_PlatformNeutralServiceSpecification *
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_PlatformNeutralServiceSpecification',NULL,'ISO 19119','Provides the abstract definition of a specific type of service but does not specify the implementation of the service.',0,'SV_ServiceIdentification','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('serviceType', NULL, 'ISO 19119', NULL, 1, 1,'SV_PlatformNeutralServiceSpecification','SV_ServiceType', NULL, 'M',0 , 'ISO 19119','ISO 19119', ' ');


/*-------------------------------------------------*
 *--------------  Classe SV_Service ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_Service',NULL,'ISO 19119','An implementation of a service.',0,NULL,NULL, ' ');


/*-------------------------------------------------*
 *--------------  Classe SV_PlatformSpecificServiceSpecification *
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_PlatformSpecificServiceSpecification',NULL,'ISO 19119','Defines the implementation of a specific type of service.',0,'SV_PlatformNeutralServiceSpecification','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('DCP', NULL, 'ISO 19119', NULL, 1, 1,'SV_PlatformSpecificServiceSpecification',NULL, 'DCPList', 'M',0 , 'ISO 19119','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('implementation', NULL, 'ISO 19119', NULL, 1, 2147483647,'SV_PlatformSpecificServiceSpecification','SV_Service', NULL, 'M',1 , 'ISO 19119','ISO 19119', ' ');

/*-------------- suite SV_PlatformNeutralServiceSpecification -----------*/
 INSERT INTO "Schemas"."Properties"  VALUES('implSpec', NULL, 'ISO 19119', NULL, 1, 1,'SV_PlatformNeutralServiceSpecification','SV_PlatformSpecificServiceSpecification', NULL, 'M',1 , 'ISO 19119','ISO 19119', ' ');

/*-------------- suite SV_Service -----------*/
 INSERT INTO "Schemas"."Properties"  VALUES('specification', NULL, 'ISO 19119', NULL, 1, 2147483647,'SV_Service','SV_PlatformSpecificServiceSpecification', NULL, 'M',0 , 'ISO 19119','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('theSV_Port', NULL, 'ISO 19119', NULL, 1, 2147483647,'SV_Service','SV_Port', NULL, 'M',1 , 'ISO 19119','ISO 19119', ' ');



/*-------------------------------------------------*
 *--------------  Classe SV_PortSpecification -----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_PortSpecification',NULL,'ISO 19119',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('binding', NULL, 'ISO 19119', NULL, 1, 1,'SV_PortSpecification',NULL, 'DCPList', 'M',0 , 'ISO 19119','ISO 19119', ' ');



/*-------------------------------------------------*
 *--------------  Classe SV_OperationModel ----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_OperationModel',NULL,'ISO 19119',NULL,0,NULL,NULL, 'E');
 INSERT INTO "Schemas"."Properties"  VALUES('message', NULL, 'ISO 19119', '', 0, 1,'SV_OperationModel','CharacterString', NULL, 'O',0 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('object', NULL, 'ISO 19119', '.', 0, 1,'SV_OperationModel','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19119', ' ');


/*-------------------------------------------------*
 *--------------  Classe SV_ServiceSpecification --*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SV_ServiceSpecification',NULL,'ISO 19119','Defines a service without reference to the type of specification or to its implementation.',0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'ISO 19119', 'The name of the service.', 1, 1,'SV_ServiceSpecification','CharacterString', NULL, 'M',0 , 'ISO 19103','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('opModel', NULL, 'ISO 19119', 'The model of the service ("message" or "object").', 1, 1,'SV_ServiceSpecification','SV_OperationModel', NULL, 'M',1 , 'ISO 19119','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('typeSpec', NULL, 'ISO 19119', ' The type of the specification of this service.', 1, 1,'SV_ServiceSpecification','SV_PlatformNeutralServiceSpecification', NULL, 'M',2 , 'ISO 19119','ISO 19119', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('theSV_Interface', NULL, 'ISO 19119', NULL, 0, 2147483647,'SV_ServiceSpecification','SV_Interface', NULL, 'O',3 , 'ISO 19119','ISO 19119', ' ');


INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:operatesOn', 'operatesOn', 'ISO 19115', 'SV_ServiceIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:operatesOn:identifier', 'identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:operatesOn', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:operatesOn:identifier:code', 'code', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:identificationInfo:operatesOn:identifier', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:serviceType', 'serviceType', 'ISO 19115', 'SV_ServiceIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:operatesOn:spatialResolution', 'spatialResolution', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo:operatesOn', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:operatesOn:spatialResolution:equivalentScale', 'equivalentScale', 'ISO 19115', 'MD_Resolution', 'ISO 19115:MD_Metadata:identificationInfo:operatesOn:spatialResolution', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:operatesOn:spatialResolution:equivalentScale:denominator', 'denominator', 'ISO 19115', 'MD_RepresentativeFraction', 'ISO 19115:MD_Metadata:identificationInfo:operatesOn:spatialResolution:equivalentScale', 'ISO 19115');
INSERT INTO "Schemas"."Paths" VALUES ('ISO 19115:MD_Metadata:identificationInfo:serviceTypeVersion','serviceTypeVersion', 'ISO 19115', 'SV_ServiceIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19119');
INSERT INTO "Schemas"."Paths" VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations','containsOperations', 'ISO 19115', 'SV_ServiceIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19119');
INSERT INTO "Schemas"."Paths" VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint','connectPoint', 'ISO 19115', 'SV_OperationMetadata', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations', 'ISO 19119');
INSERT INTO "Schemas"."Paths" VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint', 'ISO 19115');
INSERT INTO "Schemas"."Paths" VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint', 'ISO 19115');
INSERT INTO "Schemas"."Paths" VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint', 'ISO 19115');
INSERT INTO "Schemas"."Paths" VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint', 'ISO 19115');
INSERT INTO "Schemas"."Paths" VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint', 'ISO 19115');
INSERT INTO "Schemas"."Paths" VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:connectPoint', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:operationName', 'operationName', 'ISO 19115', 'SV_OperationMetadata', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:DCP', 'DCP', 'ISO 19115', 'SV_OperationMetadata', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:operationDescription', 'operationDescription', 'ISO 19115', 'SV_OperationMetadata', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters', 'parameters', 'ISO 19115', 'SV_OperationMetadata', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:name', 'name', 'ISO 19115', 'SV_Parameter', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:name:aName', 'aName', 'ISO 19115', 'GenericName', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:name', 'ISO 19103');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:name:attributeType', 'attributeType', 'ISO 19115', 'GenericName', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:name', 'ISO 19103');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:name:attributeType:aName', 'aName', 'ISO 19115', 'TypeName', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:name:attributeType', 'ISO 19103');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:description', 'description', 'ISO 19115', 'SV_Parameter', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:optionality', 'optionality', 'ISO 19115', 'SV_Parameter', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:repeatability', 'repeatability', 'ISO 19115', 'SV_Parameter', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:valueType', 'valueType', 'ISO 19115', 'SV_Parameter', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:valueType:aName', 'aName', 'ISO 19115', 'TypeName', 'ISO 19115:MD_Metadata:identificationInfo:containsOperations:parameters:valueType', 'ISO 19103');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:couplingType', 'couplingType', 'ISO 19115', 'SV_ServiceIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:restrictions', 'restrictions', 'ISO 19115', 'SV_ServiceIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:restrictions:useLimitation', 'useLimitation', 'ISO 19115', 'MD_Constraints', 'ISO 19115:MD_Metadata:identificationInfo:restrictions', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:accessProperties', 'accessProperties', 'ISO 19115', 'SV_ServiceIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19119');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:accessProperties:fees', 'fees', 'ISO 19115', 'MD_StandardOrderProcess', 'ISO 19115:MD_Metadata:identificationInfo:accessProperties', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:accessProperties:orderingInstructions', 'orderingInstructions', 'ISO 19115', 'MD_StandardOrderProcess', 'ISO 19115:MD_Metadata:identificationInfo:accessProperties', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:accessProperties:turnaround', 'turnaround', 'ISO 19115', 'MD_StandardOrderProcess', 'ISO 19115:MD_Metadata:identificationInfo:accessProperties', 'ISO 19115');


