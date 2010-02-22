INSERT INTO "Schemas"."Standard"  VALUES ('Catalog Web Service', 'csw');
INSERT INTO "Schemas"."Standard"  VALUES ('DublinCore', 'dc');
INSERT INTO "Schemas"."Standard"  VALUES ('DublinCore-terms', 'dct');
INSERT INTO "Schemas"."Standard"  VALUES ('OGC Web Service', 'ows');
INSERT INTO "Schemas"."Standard"  VALUES ('OGC Filter','ogc');

-- Dublin Core Simple Literal class --

INSERT INTO "Schemas"."Classes"  VALUES ('SimpleLiteral', NULL, 'DublinCore', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('scheme', NULL, 'DublinCore', NULL, 0, 1, 'SimpleLiteral', 'CharacterString', NULL, 'O', 1, 'ISO 19103', 'DublinCore', 'P');
INSERT INTO "Schemas"."Properties"  VALUES ('content', NULL, 'DublinCore', NULL, 1, 1, 'SimpleLiteral', 'CharacterString', NULL, 'M', 2, 'ISO 19103', 'DublinCore', 'V');


-- OWS BoundingBox class --


INSERT INTO "Schemas"."Classes"  VALUES ('BoundingBox', NULL, 'OGC Web Service', NULL, 0, NULL, NULL, 'N');
INSERT INTO "Schemas"."Properties"  VALUES ('crs', NULL, 'OGC Web Service', NULL, 1, 1, 'BoundingBox', 'CharacterString', NULL, 'M', 1, 'ISO 19103', 'OGC Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('LowerCorner', NULL, 'OGC Web Service', NULL, 2, 2147483647, 'BoundingBox', 'Real', NULL, 'O', 2, 'ISO 19103', 'OGC Web Service', 'V');
INSERT INTO "Schemas"."Properties"  VALUES ('UpperCorner', NULL, 'OGC Web Service', NULL, 2, 2147483647, 'BoundingBox', 'Real', NULL, 'O', 3, 'ISO 19103', 'OGC Web Service', 'V');
INSERT INTO "Schemas"."Classes"  VALUES ('WGS84BoundingBox', NULL, 'OGC Web Service', NULL, 0, NULL, NULL, 'N');
INSERT INTO "Schemas"."Properties"  VALUES ('LowerCorner', NULL, 'OGC Web Service', NULL, 2, 2147483647, 'WGS84BoundingBox', 'Real', NULL, 'O', 2, 'ISO 19103', 'OGC Web Service', 'V');
INSERT INTO "Schemas"."Properties"  VALUES ('UpperCorner', NULL, 'OGC Web Service', NULL, 2, 2147483647, 'WGS84BoundingBox', 'Real', NULL, 'O', 3, 'ISO 19103', 'OGC Web Service', 'V');


-- CSW Record class --

INSERT INTO "Schemas"."Classes"  VALUES ('Record', NULL, 'Catalog Web Service', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('creator', NULL, 'DublinCore', 'Name of the person or company who create the data', 0, 2147483647, 'Record', 'CharacterString', NULL, 'M', 0, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('contributor', NULL, 'DublinCore', 'Name of the person or company who contribute to the data creation', 0, 2147483647, 'Record', 'CharacterString', NULL, 'M', 1, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('publisher', NULL, 'DublinCore', 'Name of the person or company who publishe the data', 0, 2147483647, 'Record', 'CharacterString', NULL, 'M', 2, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('subject', NULL, 'DublinCore', 'keywords, summary', 0, 2147483647, 'Record', 'CharacterString', NULL, 'M', 3, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('abstract', NULL, 'DublinCore-terms', 'Description of the data', 0, 2147483647, 'Record', 'CharacterString', NULL, 'M', 4, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('identifier', NULL, 'DublinCore', 'identifier of the data', 1, 1, 'Record', 'CharacterString', NULL, 'M', 5, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('relation', NULL, 'DublinCore', 'Link with other resources', 0, 2147483647, 'Record', 'CharacterString', NULL, 'M', 6, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('source', NULL, 'DublinCore', '', 0, 1, 'Record', 'CharacterString', NULL, 'M', 7, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('rights', NULL, 'DublinCore', 'copyrights', 0, 1, 'Record', 'CharacterString', NULL, 'M', 8, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('format', NULL, 'DublinCore', 'MIME or physical type od the data', 0, 1, 'Record', 'CharacterString', NULL, 'M', 9, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('type', NULL, 'DublinCore', 'kind of the content', 0, 1, 'Record', 'CharacterString', NULL, 'M', 10, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('modified', NULL, 'DublinCore-terms', 'last date of update', 0, 1, 'Record', 'CharacterString', NULL, 'M', 11, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('coverage', NULL, 'DublinCore', '', 0, 1, 'Record', 'CharacterString', NULL, 'M', 12, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('date', NULL, 'DublinCore', 'date of an event in the lifecycle of the data', 0, 1, 'Record', 'CharacterString', NULL, 'M', 13, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('language', NULL, 'DublinCore', '', 0, 1, 'Record', 'CharacterString', NULL, 'M', 14, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('description', NULL, 'DublinCore', '', 0, 1, 'Record', 'CharacterString', NULL, 'M', 15, 'ISO 19103', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('title', NULL, 'DublinCore', 'main title of the data', 1, 1, 'Record', 'SimpleLiteral', NULL, 'M', 16, 'DublinCore', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('BoundingBox', NULL, 'OGC Web Service', 'The spatial localisation of the data', 0, 2147483647, 'Record', 'BoundingBox', NULL, 'O', 17, 'OGC Web Service', 'Catalog Web Service', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('spatial', NULL, 'DublinCore-terms', NULL, 0, 1, 'Record', 'SimpleLiteral', NULL, 'M', 18, 'DublinCore', 'Catalog Web Service', ' ');

/*-------------------------------------------------*
 *--------------  Classe Object --------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Object',NULL,'MDWEB',NULL,1,NULL,NULL, ' ');

/*-------------------------------------------------*
 *--------------  Classe QName --------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('QName',NULL,'MDWEB',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('namespaceURI', NULL, 'OGC Filter', NULL, 0, 1,'QName','CharacterString', NULL, 'O',0 , 'ISO 19103','MDWEB', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('localPart', NULL, 'OGC Filter', NULL, 0, 1,'QName','CharacterString', NULL, 'O',1 , 'ISO 19103','MDWEB', ' ');

/*-------------------------------------------------*
 *--------------  Classe PropertyName -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PropertyName',NULL,'OGC Filter',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('content', NULL, 'OGC Filter', NULL, 0, 1,'PropertyName','CharacterString', NULL, 'O',0 , 'ISO 19103','OGC Filter', ' ');


/*-------------------------------------------------*
 *--------------  Classe SpatialOps ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SpatialOps',NULL,'OGC Filter',NULL,1,NULL,NULL, ' ');

/*-------------------------------------------------*
 *--------------  Classe AbstractGML --------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractGML',NULL,'ISO 19108',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('description', NULL, 'ISO 19108', NULL, 0, 1,'AbstractGML','CharacterString', NULL, 'O',0 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'ISO 19108', NULL, 0, 1,'AbstractGML','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'ISO 19108', NULL, 0, 1,'AbstractGML','CharacterString', NULL, 'O',2 , 'ISO 19103','ISO 19108', ' ');


/*-------------------------------------------------*
 *--------------  Classe AbstractGeometry ---------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractGeometry',NULL,'ISO 19108',NULL,0,'AbstractGML','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('srsDimension', NULL, 'ISO 19108', NULL, 0, 1,'AbstractGeometry','Integer', NULL, 'O',0 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('srsName', NULL, 'ISO 19108', NULL, 0, 1,'AbstractGeometry','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('axisLabels', NULL, 'ISO 19108', NULL, 0, 2147483647,'AbstractGeometry','CharacterString', NULL, 'O',2 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('uomLabels', NULL, 'ISO 19108', NULL, 0, 2147483647,'AbstractGeometry','CharacterString', NULL, 'O',3 , 'ISO 19103','ISO 19108', ' ');


/*-------------------------------------------------*
 *--------------  Classe BinarySpatialOp ----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('BinarySpatialOp',NULL,'OGC Filter',NULL,0,'SpatialOps','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('abstractGeometry', NULL, 'OGC Filter', NULL, 0, 1,'BinarySpatialOp','AbstractGeometry', NULL, 'O',0 , 'ISO 19108','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('propertyName', NULL, 'OGC Filter', NULL, 0, 1,'BinarySpatialOp','PropertyName', NULL, 'O',1 , 'OGC Filter','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('envelope', NULL, 'OGC Filter', NULL, 0, 1,'BinarySpatialOp','Envelope', NULL, 'O',2 , 'ISO 19108','OGC Filter', ' ');


/*-------------------------------------------------*
 *--------------  Classe BBOX ---------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('BBOX',NULL,'OGC Filter',NULL,0,'SpatialOps','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('propertyName', NULL, 'OGC Filter', NULL, 0, 1,'BBOX','CharacterString', NULL, 'O',0 , 'ISO 19103','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('envelope', NULL, 'OGC Filter', NULL, 0, 1,'BBOX','Envelope', NULL, 'O',1 , 'ISO 19108','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('envelopeWithTimePeriod', NULL, 'OGC Filter', NULL, 0, 1,'BBOX','EnvelopeWithTimePeriod', NULL, 'O',2 , 'ISO 19108','OGC Filter', ' ');

/*-------------------------------------------------*
 *--------------  Classe Intersects ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Intersects',NULL,'OGC Filter',NULL,0,'BinarySpatialOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe Overlaps -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Overlaps',NULL,'OGC Filter',NULL,0,'BinarySpatialOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe Contains -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Contains',NULL,'OGC Filter',NULL,0,'BinarySpatialOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe Within -------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Within',NULL,'OGC Filter',NULL,0,'BinarySpatialOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe Crosses ------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Crosses',NULL,'OGC Filter',NULL,0,'BinarySpatialOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe Disjoint -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Disjoint',NULL,'OGC Filter',NULL,0,'BinarySpatialOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe Equals -------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Equals',NULL,'OGC Filter',NULL,0,'BinarySpatialOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe Touches ------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Touches',NULL,'OGC Filter',NULL,0,'BinarySpatialOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe Distance -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Distance',NULL,'OGC Filter',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'ISO 19108', NULL, 0, 1,'Distance','Double', NULL, 'O',0 , 'ISO 19103','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('units', NULL, 'ISO 19108', NULL, 0, 1,'Distance','CharacterString', NULL, 'O',1 , 'ISO 19103','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe DistanceBuffer -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('DistanceBuffer',NULL,'OGC Filter',NULL,0,'SpatialOps','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('propertyName', NULL, 'OGC Filter', NULL, 1, 1,'DistanceBuffer','PropertyName', NULL, 'M',0 , 'OGC Filter','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('abstractGeometry', NULL, 'OGC Filter', NULL, 0, 1,'DistanceBuffer','AbstractGeometry', NULL, 'O',1 , 'ISO 19108','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('distance', NULL, 'OGC Filter', NULL, 0, 1,'DistanceBuffer','Distance', NULL, 'O',2 , 'OGC Filter','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe DWithin ------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('DWithin',NULL,'OGC Filter',NULL,0,'DistanceBuffer','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe Beyond -------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Beyond',NULL,'OGC Filter',NULL,0,'DistanceBuffer','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe ComparisonOps ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ComparisonOps',NULL,'OGC Filter',NULL,1,NULL,NULL, ' ');
/*-------------------------------------------------*
 *--------------  Classe Literal ------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Literal',NULL,'OGC Filter',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('content', NULL, 'OGC Filter', NULL, 0, 2147483647,'Literal','CharacterString', NULL, 'O',0 , 'ISO 19103','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe Expression ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Expression',NULL,'OGC Filter',NULL,1,NULL,NULL, ' ');
/*-------------------------------------------------*
 *--------------  Classe BinaryComparisonOp ------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('BinaryComparisonOp',NULL,'OGC Filter',NULL,0,'ComparisonOps','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('literal', NULL, 'OGC Filter', NULL, 0, 1,'BinaryComparisonOp','Literal', NULL, 'O',0 , 'OGC Filter','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('expression', NULL, 'OGC Filter', NULL, 0, 1,'BinaryComparisonOp','Expression', NULL, 'O',1 , 'OGC Filter','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('propertyName', NULL, 'OGC Filter', NULL, 0, 1,'BinaryComparisonOp','CharacterString', NULL, 'O',2 , 'ISO 19103','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('matchCase', NULL, 'OGC Filter', NULL, 0, 1,'BinaryComparisonOp','Boolean', NULL, 'O',3 , 'ISO 19103','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe PropertyIsNotEqualTo -----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PropertyIsNotEqualTo',NULL,'OGC Filter',NULL,0,'BinaryComparisonOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe PropertyIsEqualTo --------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PropertyIsEqualTo',NULL,'OGC Filter',NULL,0,'BinaryComparisonOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe PropertyIsLessThan -------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PropertyIsLessThan',NULL,'OGC Filter',NULL,0,'BinaryComparisonOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe PropertyIsLessThanOrEqualTo *
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PropertyIsLessThanOrEqualTo',NULL,'OGC Filter',NULL,0,'BinaryComparisonOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe PropertyIsLike -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PropertyIsLike',NULL,'OGC Filter',NULL,0,'ComparisonOps','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe PropertyIsGreaterThanOrEqualTo *
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PropertyIsGreaterThanOrEqualTo',NULL,'OGC Filter',NULL,0,'BinaryComparisonOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe PropertyIsNull -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PropertyIsNull',NULL,'OGC Filter',NULL,0,'ComparisonOps','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe PropertyIsBetween --------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PropertyIsBetween',NULL,'OGC Filter',NULL,0,'ComparisonOps','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe PropertyIsGreaterThan ----*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PropertyIsGreaterThan',NULL,'OGC Filter',NULL,0,'BinaryComparisonOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe LogicOps -----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('LogicOps',NULL,'OGC Filter',NULL,1,NULL,NULL, ' ');
/*-------------------------------------------------*
 *--------------  Classe UnaryLogicOp -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('UnaryLogicOp',NULL,'OGC Filter',NULL,1,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('comparisonOps', NULL, 'OGC Filter', NULL, 0, 1,'UnaryLogicOp','ComparisonOps', NULL, 'O',0 , 'OGC Filter','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('spatialOps', NULL, 'OGC Filter', NULL, 0, 1,'UnaryLogicOp','SpatialOps', NULL, 'O',1 , 'OGC Filter','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('logicOps', NULL, 'OGC Filter', NULL, 0, 1,'UnaryLogicOp','LogicOps', NULL, 'O',2 , 'OGC Filter','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe BinaryLogicOp ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('BinaryLogicOp',NULL,'OGC Filter',NULL,0,'LogicOps','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('logicOps', NULL, 'OGC Filter', NULL, 0, 1,'BinaryLogicOp','LogicOps', NULL, 'O',0 , 'OGC Filter','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('comparisonOps', NULL, 'OGC Filter', NULL, 0, 1,'BinaryLogicOp','ComparisonOps', NULL, 'O',1 , 'OGC Filter','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('spatialOps', NULL, 'OGC Filter', NULL, 0, 1,'BinaryLogicOp','SpatialOps', NULL, 'O',2 , 'OGC Filter','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe And ----------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('And',NULL,'OGC Filter',NULL,0,'BinaryLogicOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe Or -----------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Or',NULL,'OGC Filter',NULL,0,'BinaryLogicOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe Not ----------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Not',NULL,'OGC Filter',NULL,0,'UnaryLogicOp','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe AbstractId ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractId',NULL,'OGC Filter',NULL,1,NULL,NULL, ' ');
/*-------------------------------------------------*
 *--------------  Classe Filter -------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Filter',NULL,'OGC Filter',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('spatialOps', NULL, 'OGC Filter', NULL, 0, 1,'Filter','SpatialOps', NULL, 'O',0 , 'OGC Filter','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('comparisonOps', NULL, 'OGC Filter', NULL, 0, 1,'Filter','ComparisonOps', NULL, 'O',1 , 'OGC Filter','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('logicOps', NULL, 'OGC Filter', NULL, 0, 1,'Filter','LogicOps', NULL, 'O',2 , 'OGC Filter','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id', NULL, 'OGC Filter', NULL, 0, 2147483647,'Filter','AbstractId', NULL, 'O',3 , 'OGC Filter','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  CodeList SortOrder   ------------*
 *-------------------------------------------------*/
INSERT INTO "Schemas"."CodeLists" VALUES ('SortOrder', NULL, 'OGC Filter', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeListElements" VALUES ('ASC', NULL, 'ISO 19108', NULL, 0, 1, 'SortOrder', 'SortOrder', 'SortOrder', 'C', 0, 'OGC Filter', 'OGC Filter', ' ', 1);
INSERT INTO "Schemas"."CodeListElements" VALUES ('DESC', NULL, 'ISO 19108', NULL, 0, 1, 'SortOrder', 'SortOrder', 'SortOrder', 'C', 0, 'OGC Filter', 'OGC Filter', ' ', 2);
/*-------------------------------------------------*
 *--------------  Classe SortProperty -------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SortProperty',NULL,'OGC Filter',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('propertyName', NULL, 'OGC Filter', NULL, 1, 1,'SortProperty','PropertyName', NULL, 'M',0 , 'OGC Filter','OGC Filter', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('sortOrder', NULL, 'OGC Filter', NULL, 0, 1,'SortProperty',NULL, 'SortOrder', 'O',1 , 'OGC Filter','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe SortBy -------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SortBy',NULL,'OGC Filter',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('sortProperty', NULL, 'OGC Filter', NULL, 0, 2147483647,'SortBy','SortProperty', NULL, 'O',0 , 'OGC Filter','OGC Filter', ' ');
/*-------------------------------------------------*
 *--------------  Classe RequestBase --------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('RequestBase',NULL,'Catalog Web Service',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('service', NULL, 'Catalog Web Service', NULL, 1, 1,'RequestBase','CharacterString', NULL, 'M',0 , 'ISO 19103','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('version', NULL, 'Catalog Web Service', NULL, 1, 1,'RequestBase','CharacterString', NULL, 'M',1 , 'ISO 19103','Catalog Web Service', ' ');
/*-------------------------------------------------*
 *--------------  Classe DistributedSearch --------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('DistributedSearch',NULL,'Catalog Web Service',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('hopCount', NULL, 'Catalog Web Service', NULL, 0, 1,'DistributedSearch','Integer', NULL, 'O',0 , 'ISO 19103','Catalog Web Service', ' ');
/*-------------------------------------------------*
 *--------------  CodeList ElementSet  ------------*
 *-------------------------------------------------*/
INSERT INTO "Schemas"."CodeLists" VALUES ('ElementSet', NULL, 'Catalog Web Service', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeListElements" VALUES ('brief', NULL, 'Catalog Web Service', NULL, 0, 1, 'ElementSet', 'ElementSet', 'ElementSet', 'C', 0, 'Catalog Web Service', 'Catalog Web Service', ' ', 1);
INSERT INTO "Schemas"."CodeListElements" VALUES ('summary', NULL, 'Catalog Web Service', NULL, 0, 1, 'ElementSet', 'ElementSet', 'ElementSet', 'C', 0, 'Catalog Web Service', 'Catalog Web Service', ' ', 2);
INSERT INTO "Schemas"."CodeListElements" VALUES ('full', NULL, 'Catalog Web Service', NULL, 0, 1, 'ElementSet', 'ElementSet', 'ElementSet', 'C', 0, 'Catalog Web Service', 'Catalog Web Service', ' ', 3);
/*-------------------------------------------------*
 *--------------  Classe ElementSetName -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ElementSetName',NULL,'Catalog Web Service',NULL,1,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value', NULL, 'Catalog Web Service', NULL, 0, 1,'ElementSetName',NULL, 'ElementSet', 'O',2 , 'Catalog Web Service','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('typeNames', NULL, 'Catalog Web Service', NULL, 0, 2147483647,'ElementSetName','QName', NULL, 'O',1 , 'MDWEB','Catalog Web Service', ' ');
/*-------------------------------------------------*
 *--------------  Classe QueryConstraint -----------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('QueryConstraint',NULL,'Catalog Web Service',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('filter', NULL, 'Catalog Web Service', NULL, 0, 1,'QueryConstraint','Filter', NULL, 'O',0 , 'OGC Filter','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('cqlText', NULL, 'Catalog Web Service', NULL, 0, 1,'QueryConstraint','CharacterString', NULL, 'O',1 , 'ISO 19103','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('version', NULL, 'Catalog Web Service', NULL, 1, 1,'QueryConstraint','CharacterString', NULL, 'M',2 , 'ISO 19103','Catalog Web Service', ' ');
/*-------------------------------------------------*
 *--------------  Classe AbstractQuery ------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractQuery',NULL,'Catalog Web Service',NULL,1,NULL,NULL, ' ');
/*-------------------------------------------------*
 *--------------  Classe Query --------------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Query',NULL,'Catalog Web Service',NULL,0,'AbstractQuery','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('elementSetName', NULL, 'Catalog Web Service', NULL, 0, 1,'Query','ElementSetName', NULL, 'O',0 , 'Catalog Web Service','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('elementName', NULL, 'Catalog Web Service', NULL, 0, 2147483647,'Query','QName', NULL, 'O',1 , 'MDWEB','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('constraint', NULL, 'Catalog Web Service', NULL, 0, 1,'Query','QueryConstraint', NULL, 'O',2 , 'Catalog Web Service','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('sortBy', NULL, 'OGC Filter', NULL, 0, 1,'Query','SortBy', NULL, 'O',3 , 'OGC Filter','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('typeNames', NULL, 'Catalog Web Service', NULL, 1, 2147483647,'Query','QName', NULL, 'M',4 , 'MDWEB','Catalog Web Service', ' ');
/*-------------------------------------------------*
 *--------------  CodeList Result   ------------*
 *-------------------------------------------------*/
INSERT INTO "Schemas"."CodeLists" VALUES ('Result', NULL, 'Catalog Web Service', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeListElements" VALUES ('results', NULL, 'Catalog Web Service', NULL, 0, 1, 'Result', 'Result', 'Result', 'C', 0, 'Catalog Web Service', 'Catalog Web Service', ' ', 1);
INSERT INTO "Schemas"."CodeListElements" VALUES ('hits', NULL, 'Catalog Web Service', NULL, 0, 1, 'Result', 'Result', 'Result', 'C', 0, 'Catalog Web Service', 'Catalog Web Service', ' ', 2);
INSERT INTO "Schemas"."CodeListElements" VALUES ('validate', NULL, 'Catalog Web Service', NULL, 0, 1, 'Result', 'Result', 'Result', 'C', 0, 'Catalog Web Service', 'Catalog Web Service', ' ', 3);
/*-------------------------------------------------*
 *--------------  Classe GetRecords ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('GetRecords',NULL,'Catalog Web Service',NULL,0,'RequestBase','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('distributedSearch', NULL, 'Catalog Web Service', NULL, 0, 1,'GetRecords','DistributedSearch', NULL, 'O',0 , 'Catalog Web Service','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('responseHandler', NULL, 'Catalog Web Service', NULL, 0, 2147483647,'GetRecords','CharacterString', NULL, 'O',1 , 'ISO 19103','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('abstractQuery', NULL, 'Catalog Web Service', NULL, 0, 1,'GetRecords','AbstractQuery', NULL, 'O',2 , 'Catalog Web Service','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('any', NULL, 'Catalog Web Service', NULL, 0, 1,'GetRecords','Object', NULL, 'O',3 , 'MDWEB','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('requestId', NULL, 'Catalog Web Service', NULL, 0, 1,'GetRecords','CharacterString', NULL, 'O',4 , 'ISO 19103','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('resultType', NULL, 'Catalog Web Service', NULL, 0, 1,'GetRecords',NULL, 'Result', 'O',5 , 'Catalog Web Service','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('outputFormat', NULL, 'Catalog Web Service', NULL, 0, 1,'GetRecords','CharacterString', NULL, 'O',6 , 'ISO 19103','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('outputSchema', NULL, 'Catalog Web Service', NULL, 0, 1,'GetRecords','CharacterString', NULL, 'O',7 , 'ISO 19103','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('startPosition', NULL, 'Catalog Web Service', NULL, 0, 1,'GetRecords','Integer', NULL, 'O',8 , 'ISO 19103','Catalog Web Service', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('maxRecords', NULL, 'Catalog Web Service', NULL, 0, 1,'GetRecords','Integer', NULL, 'O',9 , 'ISO 19103','Catalog Web Service', ' ');




INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record', 'Record', 'Catalog Web Service', 'Record', NULL, 'Catalog Web Service');
INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record:subject', 'subject', 'DublinCore', 'Record', 'Catalog Web Service:Record', 'Catalog Web Service');
INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record:subject:content', 'content', 'DublinCore', 'SimpleLiteral', 'Catalog Web Service:Record:subject', 'DublinCore');
INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record:abstract', 'abstract', 'DublinCore-terms', 'Record', 'Catalog Web Service:Record', 'Catalog Web Service');
INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record:abstract:content', 'content', 'DublinCore', 'SimpleLiteral', 'Catalog Web Service:Record:abstract', 'DublinCore');
INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record:identifier', 'identifier', 'DublinCore', 'Record', 'Catalog Web Service:Record', 'Catalog Web Service');
INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record:identifier:content', 'content', 'DublinCore', 'SimpleLiteral', 'Catalog Web Service:Record:identifier', 'DublinCore');
INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record:type', 'type', 'DublinCore', 'Record', 'Catalog Web Service:Record', 'Catalog Web Service');
INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record:type:content', 'content', 'DublinCore', 'SimpleLiteral', 'Catalog Web Service:Record:type', 'DublinCore');
INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record:BoundingBox', 'BoundingBox', 'OGC Web Service', 'Record', 'Catalog Web Service:Record', 'Catalog Web Service');
INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record:BoundingBox:crs', 'crs', 'OGC Web Service', 'BoundingBox', 'Catalog Web Service:Record:BoundingBox', 'OGC Web Service');
INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record:BoundingBox:LowerCorner', 'LowerCorner', 'OGC Web Service', 'BoundingBox', 'Catalog Web Service:Record:BoundingBox', 'OGC Web Service');
INSERT INTO "Schemas"."Paths" VALUES ('Catalog Web Service:Record:BoundingBox:UpperCorner', 'UpperCorner', 'OGC Web Service', 'BoundingBox', 'Catalog Web Service:Record:BoundingBox', 'OGC Web Service');