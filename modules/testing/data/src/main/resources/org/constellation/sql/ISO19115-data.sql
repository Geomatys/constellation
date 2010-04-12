
INSERT INTO "Schemas"."Classes"  VALUES ('Measure', NULL, 'ISO 19103', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('UomLength', NULL, 'ISO 19103', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('Anchor', NULL, 'MDWEB', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('TM_PeriodDuration', NULL, 'MDWEB', NULL, 0, NULL, NULL, ' ');

/*-------------------------------------------------*
 *--------------  Classe GenericName --------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('GenericName',NULL,'ISO 19103','Base class for  ScopedName  and  LocalName structure for type and attribute  name in the context of name spaces.',1,NULL,NULL, ' ');

/*-------------------------------------------------*
 *--------------  Classe LocalName ----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('LocalName',NULL,'ISO 19103','Identifier within a name space for a local object.',1,'GenericName','ISO 19103', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'ISO 19103', NULL, 0, 1,'LocalName','CharacterString', NULL, 'O',0 , 'ISO 19103','ISO 19103', ' ');


/*-------------------------------------------------*
 *--------------  Classe ScopedName ---------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('ScopedName',NULL,'ISO 19103','Fully qualified identifier for an object.',1,'GenericName','ISO 19103', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('scope', NULL, 'ISO 19103', 'The scope of this variable (also know as the "tail").', 0, 1,'ScopedName','GenericName', NULL, 'O',0 , 'ISO 19103','ISO 19103', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('separator', NULL, 'ISO 19103', 'The separator character.', 0, 1,'ScopedName','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19103', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'ISO 19103', 'The head as a local name.', 0, 1,'ScopedName','LocalName', NULL, 'O',2 , 'ISO 19103','ISO 19103', ' ');

/*---------------------------------------------*
 *--------------  (ISO 19115) -----------------*
 *---------------------------------------------*/

INSERT INTO "Schemas"."Classes"  VALUES ('CodeList', 'CodeList', 'ISO 19115', NULL, 1, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_SpatialRepresentation', 'SpatialRepresentation', 'ISO 19115', NULL, 1, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Identification', 'Identification', 'ISO 19115', NULL, 1, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_ContentInformation', 'ContentInformation', 'ISO 19115', NULL, 1, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_Element', 'Element', 'ISO 19115', NULL, 1, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_Completeness', 'Completeness', 'ISO 19115', NULL, 1, 'DQ_Element', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_LogicalConsistency', 'LogicalConsistency', 'ISO 19115', NULL, 1, 'DQ_Element', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_PositionalAccuracy', 'PositionalAccuracy', 'ISO 19115', NULL, 1, 'DQ_Element', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_ThematicAccuracy', 'ThematicAccuracy', 'ISO 19115', NULL, 1, 'DQ_Element', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_TemporalAccuracy', 'TemporalAccuracy', 'ISO 19115', NULL, 1, 'DQ_Element', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('CI_Citation', 'Citation', 'ISO 19115', 'CI_Citation', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('CI_Date', 'Date', 'ISO 19115', 'CI_Date', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('CI_Series', 'DatasetSeries', 'ISO 19115', 'CI_Series', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_AbsoluteExternalPositionalAccuracy', 'DQAbsExtPosAcc', 'ISO 19115', 'DQ_AbsoluteExternalPositionalAccuracy', 0, 'DQ_PositionalAccuracy', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Identifier', 'MdIdent', 'ISO 19115', 'MD_Identifier', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('rubrique', NULL, 'MDWEB', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_AccuracyOfATimeMeasurement', 'DQAccTimeMeas', 'ISO 19115', 'DQ_AccuracyOfATimeMeasurement', 0, 'DQ_TemporalAccuracy', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_CompletenessCommission', 'DQCompComm', 'ISO 19115', 'DQ_CompletenessCommission', 0, 'DQ_Completeness', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_CompletenessOmission', 'DQCompOm', 'ISO 19115', 'DQ_CompletenessOmission', 0, 'DQ_Completeness', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_ConceptualConsistency', 'DQConcConsis', 'ISO 19115', 'DQ_ConceptualConsistency', 0, 'DQ_LogicalConsistency', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_DataQuality', 'DataQual', 'ISO 19115', 'DQ_DataQuality', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_Scope', 'DQScope', 'ISO 19115', 'DQ_Scope', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('EX_Extent', 'Extent', 'ISO 19115', 'EX_Extent', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('EX_GeographicExtent', null, 'ISO 19115', 'EX_GeographicExtent', 1, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('EX_GeographicBoundingBox', 'GeoBndBox', 'ISO 19115', 'EX_GeographicBoundingBox', 0, 'EX_GeographicExtent', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('EX_GeographicDescription', 'GeoDesc', 'ISO 19115', 'EX_GeographicDescription', 0, 'EX_GeographicExtent', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('EX_BoundingPolygon', 'BoundPoly', 'ISO 19115', 'EX_BoundingPolygon', 0, 'EX_GeographicExtent', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('EX_TemporalExtent', 'TempExtent', 'ISO 19115', 'EX_TemporalExtent', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('EX_VerticalExtent', 'VertExtent', 'ISO 19115', 'EX_VerticalExtent', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_ScopeDescription', 'ScpDesc', 'ISO 19115', 'MD_ScopeDescription', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('LI_Lineage', 'Lineage', 'ISO 19115', 'LI_Lineage', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('LI_ProcessStep', 'PrcessStep', 'ISO 19115', 'LI_ProcessStep', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('LI_Source', 'Source', 'ISO 19115', 'LI_Source', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('text', NULL, 'ISO 19103', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_RepresentativeFraction', 'RepFract', 'ISO 19115', 'MD_RepresentativeFraction', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_ReferenceSystem', 'RefSystem', 'ISO 19115', 'MD_ReferenceSystem', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('RS_Identifier', 'RsIdent', 'ISO 19115', 'RS_Identifier', 0, 'MD_Identifier', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_DomainConsistency', 'DQDomConsis', 'ISO 19115', 'DQ_DomainConsistency', 0, 'DQ_LogicalConsistency', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_FormatConsistency', 'DQFormConsis', 'ISO 19115', 'DQ_FormatConsistency', 0, 'DQ_LogicalConsistency', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_GriddedDataPositionalAccuracy', 'DQGridDataPosAcc', 'ISO 19115', 'DQ_GriddedDataPositionalAccuracy', 0, 'DQ_PositionalAccuracy', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_NonQuantitativeAttributeAccuracy', 'DQNonQuantAttAcc', 'ISO 19115', 'DQ_NonQuantitativeAttributeAccuracy', 0, 'DQ_ThematicAccuracy', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_QuantitativeAttributeAccuracy', 'DQQuanAttAcc', 'ISO 19115', 'DQ_QuantitativeAttributeAccuracy', 0, 'DQ_ThematicAccuracy', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_RelativeInternalPositionalAccuracy', 'DQRelIntPosAcc', 'ISO 19115', 'DQ_RelativeInternalPositionalAccuracy', 0, 'DQ_PositionalAccuracy', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_TemporalConsistency', 'DQTempConsis', 'ISO 19115', 'DQ_TemporalConsistency', 0, 'DQ_TemporalAccuracy', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_TemporalValidity', 'DQTempValid', 'ISO 19115', 'DQ_TemporalValidity', 0, 'DQ_TemporalAccuracy', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_ThematicClassificationCorrectness', 'DQThemClassCor', 'ISO 19115', 'DQ_ThematicClassificationCorrectness', 0, 'DQ_ThematicAccuracy', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('DQ_TopologicalConsistency', 'DQTopConsis', 'ISO 19115', 'DQ_TopologicalConsistency', 0, 'DQ_LogicalConsistency', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_AggregateInformation', 'AggregateInfo', 'ISO 19115', 'MD_AggregateInformation', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_ApplicationSchemaInformation', 'AppSchInfo', 'ISO 19115', 'MD_ApplicationSchemaInformation', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_RangeDimension', 'RangeDim', 'ISO 19115', 'MD_RangeDimension', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MemberName', 'MemberName', 'ISO 19103', 'MemberName class', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('TypeName', 'TypeName', 'ISO 19103', 'Type name of attribute', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Band', 'Band', 'ISO 19115', 'MD_Band', 0, 'MD_RangeDimension', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_BrowseGraphic', 'BrowGraph', 'ISO 19115', 'MD_BrowseGraphic', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Constraints', 'Conts', 'ISO 19115', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('FRA_Constraints', 'Conts', 'ISO 19115 FRA 1.0', 'MD_Constraints', 0, 'MD_Constraints', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_CoverageDescription', 'CovDesc', 'ISO 19115', 'MD_CoverageDescription', 0, 'MD_ContentInformation', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_CRS', 'MdCoRefSys', 'ISO 19115', 'MD_CRS', 0, 'MD_ReferenceSystem', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_EllipsoidParameters', 'EllParas', 'ISO 19115', 'MD_EllipsoidParameters', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_ProjectionParameters', 'ProjParas', 'ISO 19115', 'MD_ProjectionParameters', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_DataIdentification', 'DataIdent', 'ISO 19115', NULL, 0, 'MD_Identification', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_MaintenanceInformation', 'MaintInfo', 'ISO 19115', 'MD_MaintenanceInformation', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Format', 'Format', 'ISO 19115', 'MD_Format', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Keywords', 'Keywords', 'ISO 19115', 'MD_Keywords', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Usage', 'Usage', 'ISO 19115', 'MD_Usage', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Resolution', 'Resol', 'ISO 19115', 'MD_Resolution', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('FRA_DataIdentification', 'DataIdent', 'ISO 19115 FRA 1.0', 'MD_DataIdentification', 0, 'MD_DataIdentification', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_DigitalTransferOptions', 'DigTranOps', 'ISO 19115', 'MD_DigitalTransferOptions', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Medium', 'Medium', 'ISO 19115', 'MD_Medium', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Dimension', 'Dimen', 'ISO 19115', 'MD_Dimension', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Distribution', 'Distrib', 'ISO 19115', 'MD_Distribution', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Distributor', 'Distributor', 'ISO 19115', 'MD_Distributor', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_StandardOrderProcess', 'StanOrdProc', 'ISO 19115', 'MD_StandardOrderProcess', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_ExtendedElementInformation', 'ExtEleInfo', 'ISO 19115', 'MD_ExtendedElementInformation', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_FeatureCatalogueDescription', 'FetCatDesc', 'ISO 19115', 'MD_FeatureCatalogueDescription', 0, 'MD_ContentInformation', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_GeometricObjects', 'GeometObjs', 'ISO 19115', 'MD_GeometricObjects', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_GridSpatialRepresentation', 'GridSpatRep', 'ISO 19115', 'MD_GridSpatialRepresentation', 0, 'MD_SpatialRepresentation', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Georectified', 'Georect', 'ISO 19115', 'MD_Georectified', 0, 'MD_GridSpatialRepresentation', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('float', NULL, 'ISO 19103', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Georeferenceable', 'Georef', 'ISO 19115', 'MD_Georeferenceable', 0, 'MD_GridSpatialRepresentation', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_ImageDescription', 'ImgDesc', 'ISO 19115', 'MD_ImageDescription', 0, 'MD_CoverageDescription', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_Metadata', 'metadata', 'ISO 19115', 'MD_Metadata', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('PT_Locale', 'PT_Locale', 'ISO 19115', 'Class PT_Locale', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_MetadataExtensionInformation', 'MdExtInfo', 'ISO 19115', 'MD_MetadataExtensionInformation', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_PortrayalCatalogueReference', 'PortCatRef', 'ISO 19115', 'MD_PortrayalCatalogueReference', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('obliqueLineAzimuthParameter', 'obLnAziPars', 'ISO 19115', 'Oblique line azimut parameter', 0, 'MD_ProjectionParameters', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_ObliqueLineAzimuth', 'ObLineAzi', 'ISO 19115', 'MD_ObliqueLineAzimuth', 0, 'obliqueLineAzimuthParameter', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('obliqueLinePointParameter', 'obLnPtPars', 'ISO 19115', 'Oblique line azimuth parameter', 0, 'MD_ProjectionParameters', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_ObliqueLinePoint', 'ObLinePt', 'ISO 19115', 'MD_ObliqueLinePoint', 0, 'obliqueLinePointParameter', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('FRA_DirectReferenceSystem', 'RefSystem', 'ISO 19115 FRA 1.0', 'MD_ReferenceSystem', 0, 'MD_ReferenceSystem', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_SecurityConstraints', 'SecConsts', 'ISO 19115', NULL, 0, 'MD_Constraints', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('FRA_SecurityConstraints', 'SecConsts', 'ISO 19115 FRA 1.0', 'MD_SecurityConstraints', 0, 'MD_SecurityConstraints', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_ServiceIdentification', 'SerIdent', 'ISO 19115', 'MD_ServiceIdentification', 0, 'MD_Identification', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_VectorSpatialRepresentation', 'VectSpatRep', 'ISO 19115', 'MD_VectorSpatialRepresentation', 0, 'MD_SpatialRepresentation', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('FRA_IndirectReferenceSystem', 'RefSystem', 'ISO 19115 FRA 1.0', 'MD_ReferenceSystem', 0, 'MD_ReferenceSystem', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('QE_Usability', 'Usability', 'ISO 19115 FRA 1.0', 'QE_Usability', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('MD_LegalConstraints', 'LegConsts', 'ISO 19115', NULL, 0, 'MD_Constraints', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Classes"  VALUES ('FRA_LegalConstraints', 'LegConsts', 'ISO 19115 FRA 1.0', 'MD_LegalConstraints', 0, 'MD_LegalConstraints', 'ISO 19115', ' ');

INSERT INTO "Schemas"."CodeLists"  VALUES ('AxisDirection', 'Axisdir', 'ISO 19108', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeListElements"  VALUES ('UP', NULL, 'ISO 19108', NULL, 0, 1, 'AxisDirection', 'AxisDirection', 'AxisDirection', 'C', 0, 'ISO 19108', 'ISO 19108', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('DOWN', NULL, 'ISO 19108', NULL, 0, 1, 'AxisDirection', 'AxisDirection', 'AxisDirection', 'C', 0, 'ISO 19108', 'ISO 19108', ' ', 2);


INSERT INTO "Schemas"."CodeLists"  VALUES ('MeasureType', 'MeasType', 'ISO 19103', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeListElements"  VALUES ('angle', NULL, 'ISO 19103', NULL, 0, 1, 'MeasureType', 'MeasureType', 'MeasureType', 'C', 0, 'ISO 19103', 'ISO 19103', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('area', NULL, 'ISO 19103', NULL, 0, 1, 'MeasureType', 'MeasureType', 'MeasureType', 'C', 0, 'ISO 19103', 'ISO 19103', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('length', NULL, 'ISO 19103', NULL, 0, 1, 'MeasureType', 'MeasureType', 'MeasureType', 'C', 0, 'ISO 19103', 'ISO 19103', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('scale', NULL, 'ISO 19103', NULL, 0, 1, 'MeasureType', 'MeasureType', 'MeasureType', 'C', 0, 'ISO 19103', 'ISO 19103', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('time', NULL, 'ISO 19103', NULL, 0, 1, 'MeasureType', 'MeasureType', 'MeasureType', 'C', 0, 'ISO 19103', 'ISO 19103', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('velocity', NULL, 'ISO 19103', NULL, 0, 1, 'MeasureType', 'MeasureType', 'MeasureType', 'C', 0, 'ISO 19103', 'ISO 19103', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('volume', NULL, 'ISO 19103', NULL, 0, 1, 'MeasureType', 'MeasureType', 'MeasureType', 'C', 0, 'ISO 19103', 'ISO 19103', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('weight', NULL, 'ISO 19103', NULL, 0, 1, 'MeasureType', 'MeasureType', 'MeasureType', 'C', 0, 'ISO 19103', 'ISO 19103', ' ', 8);


INSERT INTO "Schemas"."Classes" VALUES ('UnitOfMeasure', null, 'ISO 19103', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('formula', null, 'ISO 19103', null, 0, 1, 'UnitOfMeasure', 'CharacterString', NULL, 'O', 1, 'ISO 19103', 'ISO 19103', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('measureType', null, 'ISO 19103', null, 1, 1, 'UnitOfMeasure', NULL, 'MeasureType', 'O', 2, 'ISO 19103', 'ISO 19103', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('nameStandardUnit', null, 'ISO 19103', null, 0, 1, 'UnitOfMeasure', 'CharacterString', NULL, 'O', 3, 'ISO 19103', 'ISO 19103', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('offsetToStandardUnit', null, 'ISO 19103', null, 0, 1, 'UnitOfMeasure', 'Real', NULL, 'O', 4, 'ISO 19103', 'ISO 19103', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('scaleToStandardUnit', null, 'ISO 19103', null, 0, 1, 'UnitOfMeasure', 'Real', NULL, 'O', 5, 'ISO 19103', 'ISO 19103', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('uomName', null, 'ISO 19103', null, 1, 1, 'UnitOfMeasure', 'CharacterString', NULL, 'O', 6, 'ISO 19103', 'ISO 19103', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('uomSymbol', null, 'ISO 19103', null, 1, 1, 'UnitOfMeasure', 'CharacterString', NULL, 'O', 7, 'ISO 19103', 'ISO 19103', ' ');

INSERT INTO "Schemas"."Classes" VALUES ('Type', null, 'ISO 19103', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('typeName', null, 'ISO 19103', null, 1, 1, 'Type', 'TypeName', NULL, 'M', 1, 'ISO 19103', 'ISO 19103', ' ');

INSERT INTO "Schemas"."Classes" VALUES ('RecordType', null, 'ISO 19103', NULL, 0, 'Type', 'ISO 19103', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('attributes', null, 'ISO 19103', null, 0, 2147483647, 'RecordType', 'Type', NULL, 'O', 1, 'ISO 19103', 'ISO 19103', ' ');

INSERT INTO "Schemas"."Classes" VALUES ('Record', null, 'ISO 19103', NULL, 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('recordType', null, 'ISO 19103', null, 0, 1, 'Record', 'RecordType', NULL, 'M', 1, 'ISO 19103', 'ISO 19103', ' ');

INSERT INTO "Schemas"."Classes" VALUES ('DQ_Result', 'Result', 'ISO 19115', 'generalisation of more specific result classes', 1, NULL, NULL, ' ');
INSERT INTO "Schemas"."Classes" VALUES ('DQ_ConformanceResult', 'ConResult', 'ISO 19115', 'information about the outcome of evaluating the obtained value (or set of values) against a specified acceptable conformance quality level', 0, 'DQ_Result', 'ISO 19115', ' ');


INSERT INTO "Schemas"."Classes" VALUES ('DQ_QuantitativeResult', 'QuanResult', 'ISO 19115', 'the values or information about the value(s) (or set of values) obtained from applying a data quality measure', 0, 'DQ_Result', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('valueType', null, 'ISO 19115', null, 0, 1, 'DQ_QuantitativeResult', 'RecordType', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('valueUnit', null, 'ISO 19115', null, 1, 1, 'DQ_QuantitativeResult', 'UnitOfMeasure', NULL, 'M', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('errorStatistic', null, 'ISO 19115', null, 1, 1, 'DQ_QuantitativeResult', 'CharacterString', NULL, 'M', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('value', null, 'ISO 19115', null, 1, 2147483647, 'DQ_QuantitativeResult', 'Record', NULL, 'M', 4, 'ISO 19103', 'ISO 19115', ' ');


/*-------------------------------------------------*
 *--------------  Classe CoordinateSystemAxis -----*
 *-------------------------------------------------*/
INSERT INTO "Schemas"."Classes"  VALUES ('CoordinateSystemAxis', NULL, 'ISO 19108', '', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Properties"  VALUES('identifier', NULL, 'ISO 19108', NULL, 0, 1,'CoordinateSystemAxis','RS_Identifier', NULL, 'O',0 , 'ISO 19115','ISO 19108',' ');
INSERT INTO "Schemas"."Properties"  VALUES('axisAbbrev', NULL, 'ISO 19108', NULL, 0, 1,'CoordinateSystemAxis','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19108',' ');
INSERT INTO "Schemas"."Properties"  VALUES('axisDirection', NULL, 'ISO 19108', NULL, 0, 1,'CoordinateSystemAxis',NULL, 'AxisDirection', 'O',2 , 'ISO 19108','ISO 19108',' ');
INSERT INTO "Schemas"."Properties"  VALUES('uom', NULL, 'ISO 19108', NULL, 0, 1,'CoordinateSystemAxis','CharacterString', NULL, 'O',3 , 'ISO 19103','ISO 19108',' ');

/*-------------------------------------------------*
 *--------------  Classe VerticalCS --------------*
 *-------------------------------------------------*/
INSERT INTO "Schemas"."Classes"  VALUES ('VerticalCS', NULL, 'ISO 19108', '', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Properties"  VALUES('identifier', NULL, 'ISO 19108', NULL, 0, 1,'VerticalCS','RS_Identifier', NULL, 'O',0 , 'ISO 19115','ISO 19108',' ');
INSERT INTO "Schemas"."Properties"  VALUES('axis', NULL, 'ISO 19108', NULL, 0, 1,'VerticalCS','CoordinateSystemAxis', NULL, 'O',0 , 'ISO 19108','ISO 19108',' ');

/*-------------------------------------------------*
 *--------------  Classe VerticalDatum --------------*
 *-------------------------------------------------*/
INSERT INTO "Schemas"."Classes"  VALUES ('VerticalDatum', NULL, 'ISO 19108', '', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Properties"  VALUES('identifier', NULL, 'ISO 19108', NULL, 0, 1,'VerticalDatum','RS_Identifier', NULL, 'O',0 , 'ISO 19115','ISO 19108',' ');

/*-------------------------------------------------*
 *--------------  Classe VerticalCRS --------------*
 *-------------------------------------------------*/
INSERT INTO "Schemas"."Classes"  VALUES ('VerticalCRS', NULL, 'ISO 19108', '', 0, NULL, NULL, ' ');
INSERT INTO "Schemas"."Properties"  VALUES('identifier', NULL, 'ISO 19108', NULL, 0, 1,'VerticalCRS','RS_Identifier', NULL, 'O',0 , 'ISO 19115','ISO 19108','C');
INSERT INTO "Schemas"."Properties"  VALUES('verticalCSProperty', NULL, 'ISO 19108', NULL, 0, 1,'VerticalCRS','VerticalCS', NULL, 'O',0 , 'ISO 19108','ISO 19108',' ');
INSERT INTO "Schemas"."Properties"  VALUES('verticalDatumProperty', NULL, 'ISO 19108', NULL, 0, 1,'VerticalCRS','VerticalDatum', NULL, 'O',0 , 'ISO 19108','ISO 19108',' ');

/*-------------------------------------------------*
 *--------------  Classe LineString----------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('LineString',NULL,'ISO 19108',NULL,0, NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('identifier', NULL, 'ISO 19108', NULL, 0, 1,'LineString','CharacterString', NULL, 'O',0 , 'ISO 19103','ISO 19108',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name', NULL, 'ISO 19108', NULL, 0, 1,'LineString','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19108',' ');
 INSERT INTO "Schemas"."Properties"  VALUES('coordinates', NULL, 'ISO 19108', NULL, 0, 1,'LineString','CharacterString', NULL, 'O',2 , 'ISO 19103','ISO 19108',' ');

INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_RestrictionCode', 'RestrictCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('DS_AssociationTypeCode', 'AscTypeCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MW_LengthUnitCode', 'LUnitCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_CellGeometryCode', 'CellGeoCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_CharacterSetCode', 'CharSetCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_ClassificationCode', 'ClassificationCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('RO_SystRefCode', 'RefSysCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_CoverageContentTypeCode', 'ContentTypCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_DatatypeCode', 'DatatypeCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('CI_DateTypeCode', 'DateTypeCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_DimensionNameTypeCode', 'DimNameTypCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('DQ_EvaluationMethodTypeCode', 'EvalMethTypeCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_GeometricObjectTypeCode', 'GeoObjTypCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_ScopeCode', 'ScopeCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_ImagingConditionCode', 'ImgCondCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('DS_InitiativeTypeCode', 'InitTypeCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('LanguageCode', 'LangCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_MaintenanceFrequencyCode', 'MaintFreqCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_MediumFormatCode', 'MedFormCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_MediumNameCode', 'MedNameCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('CI_PresentationFormCode', 'PresFormCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_SpatialRepresentationTypeCode', 'SpatRepTypCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_ProgressCode', 'ProgCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_TopologyLevelCode', 'TopoLevCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_KeywordTypeCode', 'KeyTypCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');




INSERT INTO "Schemas"."CodeListElements"  VALUES ('copyright', NULL, 'ISO 19115', NULL, 0, 1, 'MD_RestrictionCode', 'MD_RestrictionCode', 'MD_RestrictionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('patent', NULL, 'ISO 19115', NULL, 0, 1, 'MD_RestrictionCode', 'MD_RestrictionCode', 'MD_RestrictionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('patentPending', NULL, 'ISO 19115', NULL, 0, 1, 'MD_RestrictionCode', 'MD_RestrictionCode', 'MD_RestrictionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('trademark', NULL, 'ISO 19115', NULL, 0, 1, 'MD_RestrictionCode', 'MD_RestrictionCode', 'MD_RestrictionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('license', NULL, 'ISO 19115', NULL, 0, 1, 'MD_RestrictionCode', 'MD_RestrictionCode', 'MD_RestrictionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('intellectualPropertyRight', NULL, 'ISO 19115', NULL, 0, 1, 'MD_RestrictionCode', 'MD_RestrictionCode', 'MD_RestrictionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('restricted', NULL, 'ISO 19115', NULL, 0, 1, 'MD_RestrictionCode', 'MD_RestrictionCode', 'MD_RestrictionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('otherRestrictions', NULL, 'ISO 19115', NULL, 0, 1, 'MD_RestrictionCode', 'MD_RestrictionCode', 'MD_RestrictionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 8);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('No access restriction', NULL, 'ISO 19115', NULL, 0, 1, 'MD_RestrictionCode', 'MD_RestrictionCode', 'MD_RestrictionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 9);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('crossReference', NULL, 'ISO 19115', NULL, 0, 1, 'DS_AssociationTypeCode', 'DS_AssociationTypeCode', 'DS_AssociationTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('largerWorkCitation', NULL, 'ISO 19115', NULL, 0, 1, 'DS_AssociationTypeCode', 'DS_AssociationTypeCode', 'DS_AssociationTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('partOfSeamlessDatabase', NULL, 'ISO 19115', NULL, 0, 1, 'DS_AssociationTypeCode', 'DS_AssociationTypeCode', 'DS_AssociationTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('source', NULL, 'ISO 19115', NULL, 0, 1, 'DS_AssociationTypeCode', 'DS_AssociationTypeCode', 'DS_AssociationTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('stereoMate', NULL, 'ISO 19115', NULL, 0, 1, 'DS_AssociationTypeCode', 'DS_AssociationTypeCode', 'DS_AssociationTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('point', NULL, 'ISO 19115', NULL, 0, 1, 'MD_CellGeometryCode', 'MD_CellGeometryCode', 'MD_CellGeometryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('area', NULL, 'ISO 19115', NULL, 0, 1, 'MD_CellGeometryCode', 'MD_CellGeometryCode', 'MD_CellGeometryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('8859part6', NULL, 'ISO 19115', NULL, 0, 1, 'MD_CharacterSetCode', 'MD_CharacterSetCode', 'MD_CharacterSetCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 11);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('usAscii', NULL, 'ISO 19115', NULL, 0, 1, 'MD_CharacterSetCode', 'MD_CharacterSetCode', 'MD_CharacterSetCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 21);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('utf8', NULL, 'ISO 19115', NULL, 0, 1, 'MD_CharacterSetCode', 'MD_CharacterSetCode', 'MD_CharacterSetCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('utf16', NULL, 'ISO 19115', NULL, 0, 1, 'MD_CharacterSetCode', 'MD_CharacterSetCode', 'MD_CharacterSetCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('8859part1', NULL, 'ISO 19115', NULL, 0, 1, 'MD_CharacterSetCode', 'MD_CharacterSetCode', 'MD_CharacterSetCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('unclassified', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ClassificationCode', 'MD_ClassificationCode', 'MD_ClassificationCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('restricted', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ClassificationCode', 'MD_ClassificationCode', 'MD_ClassificationCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('confidential', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ClassificationCode', 'MD_ClassificationCode', 'MD_ClassificationCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('secret', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ClassificationCode', 'MD_ClassificationCode', 'MD_ClassificationCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('topSecret', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ClassificationCode', 'MD_ClassificationCode', 'MD_ClassificationCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102022', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102022);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102030', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102030);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102001', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102001);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102002', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102002);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('54027', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 54027);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('54002', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 54002);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102013', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102013);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102031', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102031);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102014', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102014);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('23028', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 23028);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('23029', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 23029);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102023', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102023);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('23030', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 23030);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('23031', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 23031);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('23032', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 23032);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('23033', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 23033);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('23034', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 23034);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('23035', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 23035);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('23036', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 23036);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('23037', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 23037);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('23038', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 23038);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102007', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102007);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102024', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102024);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('54004', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 54004);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102008', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102008);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102010', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102010);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102009', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102009);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('2154', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2154);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('27581', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 27581);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('27582', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 27582);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('27593', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 27593);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('27583', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 27583);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('27584', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 27584);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102006', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102006);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('27571', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 27571);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('27572', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 27572);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('27573', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 27573);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('27574', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 27574);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102033', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102033);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102032', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102032);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102015', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102015);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102003', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102003);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102005', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102005);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102004', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102004);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102025', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102025);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('32628', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 32628);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('32629', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 32629);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('32630', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 32630);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('32631', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 32631);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('32632', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 32632);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('32633', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 32633);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('32634', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 32634);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('32635', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 32635);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('32636', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 32636);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('32637', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 32637);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102026', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102026);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('32638', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 32638);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('unknown', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 0);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102027', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102027);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102028', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102028);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('102029', NULL, 'ISO 19115', NULL, 0, 1, 'RO_SystRefCode', 'RO_SystRefCode', 'RO_SystRefCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 102029);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('image', NULL, 'ISO 19115', NULL, 0, 1, 'MD_CoverageContentTypeCode', 'MD_CoverageContentTypeCode', 'MD_CoverageContentTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('thematicClassification', NULL, 'ISO 19115', NULL, 0, 1, 'MD_CoverageContentTypeCode', 'MD_CoverageContentTypeCode', 'MD_CoverageContentTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('physicalMeasurement', NULL, 'ISO 19115', NULL, 0, 1, 'MD_CoverageContentTypeCode', 'MD_CoverageContentTypeCode', 'MD_CoverageContentTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('class', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('unionClass', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 10);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('metaClass', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 11);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('typeClass', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 12);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('CharacterString', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 13);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('integer', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 14);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('association', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 15);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('codelist', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('enumeration', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('codelistElement', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('abstractClass', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('aggregateClass', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('specifiedClass', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('datatypeClass', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 8);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('interfaceClass', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DatatypeCode', 'MD_DatatypeCode', 'MD_DatatypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 9);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('creation', NULL, 'ISO 19115', NULL, 0, 1, 'CI_DateTypeCode', 'CI_DateTypeCode', 'CI_DateTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('publication', NULL, 'ISO 19115', NULL, 0, 1, 'CI_DateTypeCode', 'CI_DateTypeCode', 'CI_DateTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('revision', NULL, 'ISO 19115', NULL, 0, 1, 'CI_DateTypeCode', 'CI_DateTypeCode', 'CI_DateTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('row', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('column', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('vertical', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('track', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('crossTrack', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('line', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('sample', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('time', NULL, 'ISO 19115', NULL, 0, 1, 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'MD_DimensionNameTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 8);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('directInternal', NULL, 'ISO 19115', NULL, 0, 1, 'DQ_EvaluationMethodTypeCode', 'DQ_EvaluationMethodTypeCode', 'DQ_EvaluationMethodTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('directExternal', NULL, 'ISO 19115', NULL, 0, 1, 'DQ_EvaluationMethodTypeCode', 'DQ_EvaluationMethodTypeCode', 'DQ_EvaluationMethodTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('indirect', NULL, 'ISO 19115', NULL, 0, 1, 'DQ_EvaluationMethodTypeCode', 'DQ_EvaluationMethodTypeCode', 'DQ_EvaluationMethodTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('complex', NULL, 'ISO 19115', NULL, 0, 1, 'MD_GeometricObjectTypeCode', 'MD_GeometricObjectTypeCode', 'MD_GeometricObjectTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('composite', NULL, 'ISO 19115', NULL, 0, 1, 'MD_GeometricObjectTypeCode', 'MD_GeometricObjectTypeCode', 'MD_GeometricObjectTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('curve', NULL, 'ISO 19115', NULL, 0, 1, 'MD_GeometricObjectTypeCode', 'MD_GeometricObjectTypeCode', 'MD_GeometricObjectTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('point', NULL, 'ISO 19115', NULL, 0, 1, 'MD_GeometricObjectTypeCode', 'MD_GeometricObjectTypeCode', 'MD_GeometricObjectTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('solid', NULL, 'ISO 19115', NULL, 0, 1, 'MD_GeometricObjectTypeCode', 'MD_GeometricObjectTypeCode', 'MD_GeometricObjectTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('surface', NULL, 'ISO 19115', NULL, 0, 1, 'MD_GeometricObjectTypeCode', 'MD_GeometricObjectTypeCode', 'MD_GeometricObjectTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('dataset', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('series', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('blurredImage', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('snow', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 10);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('terrainMasking', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 11);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('cloud', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('degradingObliquity', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('fog', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('heavySmokeOrDust', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('night', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('rain', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('semiDarkness', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 8);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('shadow', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'MD_ImagingConditionCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 9);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('campaign', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('process', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 10);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('program', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 11);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('project', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 12);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('study', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 13);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('task', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 14);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('trial', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 15);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('collection', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('exercise', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('experiment', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('investigation', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('mission', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('sensor', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('operation', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 8);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('platform', NULL, 'ISO 19115', NULL, 0, 1, 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'DS_InitiativeTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 9);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('continual', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('irregular', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 10);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('notPlanned', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 11);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('unknown', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 12);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('triennal', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 13);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('four-year', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 14);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('daily', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('weekly', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('fornightly', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('monthly', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('quarterly', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('biannually', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('annually', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 8);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('asNeeded', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'MD_MaintenanceFrequencyCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 9);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('cpio', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('tar', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('highSierra', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('iso9660', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('iso9660RockRidge', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('iso9660AppleHFS', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('none', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'MD_MediumFormatCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('cdRom', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('3580Cartridge', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 10);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('4mmCartridgetape', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 11);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('8mmCartridgeTape', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 12);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('1quarterInchCartridgeTape', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 13);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('digitalLinearTape', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 14);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('onLine', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 15);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('satelite', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 16);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('telephoneLink', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 17);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('hardcopy', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 18);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('none', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 19);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('dvd', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('dvdRom', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('3halfInchFloppy', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('5quarterInchFloppy', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('7trackTape', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('9trackTape', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('3480Cartridge', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 8);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('3490Cartridge', NULL, 'ISO 19115', NULL, 0, 1, 'MD_MediumNameCode', 'MD_MediumNameCode', 'MD_MediumNameCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 9);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('documentDigital', NULL, 'ISO 19115', NULL, 0, 1, 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('tableDigital', NULL, 'ISO 19115', NULL, 0, 1, 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 11);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('tableHardcopy', NULL, 'ISO 19115', NULL, 0, 1, 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 12);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('geoDatabase', NULL, 'ISO 19115', NULL, 0, 1, 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 15);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('database', NULL, 'ISO 19115', NULL, 0, 1, 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 16);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('vector theme', NULL, 'ISO 19115', NULL, 0, 1, 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 17);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('raster layer', NULL, 'ISO 19115', NULL, 0, 1, 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 18);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('documentHardcopy', NULL, 'ISO 19115', NULL, 0, 1, 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('imageDigital', NULL, 'ISO 19115', NULL, 0, 1, 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('imageHardcopy', NULL, 'ISO 19115', NULL, 0, 1, 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('mapDigital', NULL, 'ISO 19115', NULL, 0, 1, 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('mapHardcopy', NULL, 'ISO 19115', NULL, 0, 1, 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'CI_PresentationFormCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('vector', NULL, 'ISO 19115', NULL, 0, 1, 'MD_SpatialRepresentationTypeCode', 'MD_SpatialRepresentationTypeCode', 'MD_SpatialRepresentationTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('grid', NULL, 'ISO 19115', NULL, 0, 1, 'MD_SpatialRepresentationTypeCode', 'MD_SpatialRepresentationTypeCode', 'MD_SpatialRepresentationTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('textTable', NULL, 'ISO 19115', NULL, 0, 1, 'MD_SpatialRepresentationTypeCode', 'MD_SpatialRepresentationTypeCode', 'MD_SpatialRepresentationTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('tin', NULL, 'ISO 19115', NULL, 0, 1, 'MD_SpatialRepresentationTypeCode', 'MD_SpatialRepresentationTypeCode', 'MD_SpatialRepresentationTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('stereoModel', NULL, 'ISO 19115', NULL, 0, 1, 'MD_SpatialRepresentationTypeCode', 'MD_SpatialRepresentationTypeCode', 'MD_SpatialRepresentationTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('video', NULL, 'ISO 19115', NULL, 0, 1, 'MD_SpatialRepresentationTypeCode', 'MD_SpatialRepresentationTypeCode', 'MD_SpatialRepresentationTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('completed', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ProgressCode', 'MD_ProgressCode', 'MD_ProgressCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('historicalArchive', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ProgressCode', 'MD_ProgressCode', 'MD_ProgressCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('obsolete', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ProgressCode', 'MD_ProgressCode', 'MD_ProgressCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('onGoing', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ProgressCode', 'MD_ProgressCode', 'MD_ProgressCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('planned', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ProgressCode', 'MD_ProgressCode', 'MD_ProgressCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('required', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ProgressCode', 'MD_ProgressCode', 'MD_ProgressCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('underDevelopment', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ProgressCode', 'MD_ProgressCode', 'MD_ProgressCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('geometryOnly', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('topology1D', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('planarGraph', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('fullPlanarGraph', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('surfaceGraph', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('fullSurfaceGraph', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('topology3D', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('fullTopology3D', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 8);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('abstract', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'MD_TopologyLevelCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 9);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('discipline', NULL, 'ISO 19115', NULL, 0, 1, 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('place', NULL, 'ISO 19115', NULL, 0, 1, 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('stratum', NULL, 'ISO 19115', NULL, 0, 1, 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('temporal', NULL, 'ISO 19115', NULL, 0, 1, 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('theme', NULL, 'ISO 19115', NULL, 0, 1, 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('parameter', NULL, 'ISO 19115', NULL, 0, 1, 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('instrument', NULL, 'ISO 19115', NULL, 0, 1, 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('platform_class', NULL, 'ISO 19115', NULL, 0, 1, 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 8);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('projects', NULL, 'ISO 19115', NULL, 0, 1, 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'MD_KeywordTypeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 9);




INSERT INTO "Schemas"."Locales"  VALUES ('meter', 'm', 'ISO 19115', NULL, 0, 1, 'MW_LengthUnitCode', 'MW_LengthUnitCode', 'MW_LengthUnitCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 0);
INSERT INTO "Schemas"."Locales"  VALUES ('centimeter', 'cm', 'ISO 19115', NULL, 0, 1, 'MW_LengthUnitCode', 'MW_LengthUnitCode', 'MW_LengthUnitCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 0);
INSERT INTO "Schemas"."Locales"  VALUES ('kilometer', 'km', 'ISO 19115', NULL, 0, 1, 'MW_LengthUnitCode', 'MW_LengthUnitCode', 'MW_LengthUnitCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 0);
INSERT INTO "Schemas"."Locales"  VALUES ('mile', 'mi', 'ISO 19115', NULL, 0, 1, 'MW_LengthUnitCode', 'MW_LengthUnitCode', 'MW_LengthUnitCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 0);
INSERT INTO "Schemas"."Locales"  VALUES ('fra', 'fra', 'ISO 636-2', NULL, 0, 1, 'LanguageCode', 'LanguageCode', 'LanguageCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 0);
INSERT INTO "Schemas"."Locales"  VALUES ('spa', 'spa', 'ISO 636-2', NULL, 0, 1, 'LanguageCode', 'LanguageCode', 'LanguageCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 0);
INSERT INTO "Schemas"."Locales"  VALUES ('eng', 'eng', 'ISO 636-2', NULL, 0, 1, 'LanguageCode', 'LanguageCode', 'LanguageCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 0);
INSERT INTO "Schemas"."Locales"  VALUES ('por', 'por', 'ISO 636-2', NULL, 0, 1, 'LanguageCode', 'LanguageCode', 'LanguageCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 0);
INSERT INTO "Schemas"."Locales"  VALUES ('ara', 'ara', 'ISO 636-2', NULL, 0, 1, 'LanguageCode', 'LanguageCode', 'LanguageCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 0);
INSERT INTO "Schemas"."Locales"  VALUES ('ger', 'ger', 'ISO 636-2', NULL, 0, 1, 'LanguageCode', 'LanguageCode', 'LanguageCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 0);
INSERT INTO "Schemas"."Locales"  VALUES ('rus', 'rus', 'ISO 636-2', NULL, 0, 1, 'LanguageCode', 'LanguageCode', 'LanguageCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 0);


INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata', 'MD_Metadata', 'ISO 19115', 'MD_Metadata', NULL, 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:fileIdentifier', 'fileIdentifier', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:language', 'language', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:characterSet', 'characterSet', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataStandardName', 'metadataStandardName', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataStandardVersion', 'metadataStandardVersion', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dateStamp', 'dateStamp', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataSetURI', 'dataSetURI', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact', 'contact', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:organisationName', 'organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:contact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:role', 'role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:contact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo', 'contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:contact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo:address', 'address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:contact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo:address:city', 'city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:contact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo:address:deliveryPoint', 'deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:contact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo:address:postalCode', 'postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:contact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo:address:country', 'country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:contact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo:address:electronicMailAddress', 'electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:contact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo:onlineResource', 'onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:contact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo:onlineResource:linkage', 'linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:contact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo', 'identificationInfo', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:abstract', 'abstract', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:credit', 'credit', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:status', 'status', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:language', 'language', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:characterSet', 'characterSet', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:topicCategory', 'topicCategory', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords', 'descriptiveKeywords', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword', 'keyword', 'ISO 19115', 'MD_Keywords', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation', 'citation', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:title', 'title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:date', 'date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:date:date', 'date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:identificationInfo:citation:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:date:dateType', 'dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:identificationInfo:citation:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:presentationForm', 'presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty', 'citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:organisationName', 'organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:role', 'role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo', 'contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address', 'address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address:deliveryPoint', 'deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address:city', 'city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address:postalCode', 'postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address:country', 'country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address:electronicMailAddress', 'electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource', 'onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource:linkage', 'linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:spatialRepresentationType', 'spatialRepresentationType', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:spatialResolution', 'spatialResolution', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:spatialResolution:equivalentScale', 'equivalentScale', 'ISO 19115', 'MD_Resolution', 'ISO 19115:MD_Metadata:identificationInfo:spatialResolution', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:spatialResolution:equivalentScale:denominator', 'denominator', 'ISO 19115', 'MD_RepresentativeFraction', 'ISO 19115:MD_Metadata:identificationInfo:spatialResolution:equivalentScale', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent', 'extent', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2', 'geographicElement2', 'ISO 19115', 'EX_Extent', 'ISO 19115:MD_Metadata:identificationInfo:extent', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude', 'westBoundLongitude', 'ISO 19115', 'EX_GeographicBoundingBox', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude', 'eastBoundLongitude', 'ISO 19115', 'EX_GeographicBoundingBox', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude', 'northBoundLatitude', 'ISO 19115', 'EX_GeographicBoundingBox', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude', 'southBoundLatitude', 'ISO 19115', 'EX_GeographicBoundingBox', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement', 'temporalElement', 'ISO 19115', 'EX_Extent', 'ISO 19115:MD_Metadata:identificationInfo:extent', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent', 'extent', 'ISO 19115', 'EX_TemporalExtent', 'ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:beginPosition', 'beginPosition', 'ISO 19108', 'TimePeriod', 'ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:endPosition', 'endPosition', 'ISO 19108', 'TimePeriod', 'ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3', 'geographicElement3', 'ISO 19115', 'EX_Extent', 'ISO 19115:MD_Metadata:identificationInfo:extent', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier', 'geographicIdentifier', 'ISO 19115', 'EX_GeographicDescription', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:code', 'code', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:resourceFormat', 'resourceFormat', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:resourceFormat:name', 'name', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:identificationInfo:resourceFormat', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:resourceFormat:version', 'version', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:identificationInfo:resourceFormat', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance', 'resourceMaintenance', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:maintenanceAndUpdateFrequency', 'maintenanceAndUpdateFrequency', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints', 'resourceConstraints', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:useLimitation', 'useLimitation', 'ISO 19115', 'MD_LegalConstraints', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:accessConstraints', 'accessConstraints', 'ISO 19115', 'MD_LegalConstraints', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:useConstraints', 'useConstraints', 'ISO 19115', 'MD_LegalConstraints', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:otherConstraints', 'otherConstraints', 'ISO 19115', 'MD_LegalConstraints', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:referenceSystemInfo', 'referenceSystemInfo', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier', 'referenceSystemIdentifier', 'ISO 19115', 'MD_ReferenceSystem', 'ISO 19115:MD_Metadata:referenceSystemInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:code', 'code', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo', 'dataQualityInfo', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope', 'scope', 'ISO 19115', 'DQ_DataQuality', 'ISO 19115:MD_Metadata:dataQualityInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:level', 'level', 'ISO 19115', 'DQ_Scope', 'ISO 19115:MD_Metadata:dataQualityInfo:scope', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:lineage', 'lineage', 'ISO 19115', 'DQ_DataQuality', 'ISO 19115:MD_Metadata:dataQualityInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:statement', 'statement', 'ISO 19115', 'LI_Lineage', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo', 'distributionInfo', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributionFormat', 'distributionFormat', 'ISO 19115', 'MD_Distribution', 'ISO 19115:MD_Metadata:distributionInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributionFormat:name', 'name', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:distributionInfo:distributionFormat', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributionFormat:version', 'version', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:distributionInfo:distributionFormat', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor', 'distributor', 'ISO 19115', 'MD_Distribution', 'ISO 19115:MD_Metadata:distributionInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact', 'distributorContact', 'ISO 19115', 'MD_Distributor', 'ISO 19115:MD_Metadata:distributionInfo:distributor', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:organisationName', 'organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:role', 'role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo', 'contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address', 'address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address:deliveryPoint', 'deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address:city', 'city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address:postalCode', 'postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address:country', 'country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address:electronicMailAddress', 'electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource', 'onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource:linkage', 'linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:transferOptions', 'transferOptions', 'ISO 19115', 'MD_Distribution', 'ISO 19115:MD_Metadata:distributionInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:transferSize', 'transferSize', 'ISO 19115', 'MD_DigitalTransferOptions', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine', 'onLine', 'ISO 19115', 'MD_DigitalTransferOptions', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine:linkage', 'linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:offLine', 'offLine', 'ISO 19115', 'MD_DigitalTransferOptions', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:offLine:name', 'name', 'ISO 19115', 'MD_Medium', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions:offLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:offLine:density', 'density', 'ISO 19115', 'MD_Medium', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions:offLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:offLine:volumes', 'volumes', 'ISO 19115', 'MD_Medium', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions:offLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:offLine:mediumFormat', 'mediumFormat', 'ISO 19115', 'MD_Medium', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions:offLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:parentIdentifier', 'parentIdentifier', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:graphicOverview', 'graphicOverview', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:graphicOverview:fileName', 'fileName', 'ISO 19115', 'MD_BrowseGraphic', 'ISO 19115:MD_Metadata:identificationInfo:graphicOverview', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:graphicOverview:fileType', 'fileType', 'ISO 19115', 'MD_BrowseGraphic', 'ISO 19115:MD_Metadata:identificationInfo:graphicOverview', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:graphicOverview:fileDescription', 'fileDescription', 'ISO 19115', 'MD_BrowseGraphic', 'ISO 19115:MD_Metadata:identificationInfo:graphicOverview', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:spatialRepresentationInfo', 'spatialRepresentationInfo', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:spatialRepresentationInfo:topologyLevel', 'topologyLevel', 'ISO 19115', 'MD_VectorSpatialRepresentation', 'ISO 19115:MD_Metadata:spatialRepresentationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:spatialRepresentationInfo:geometricObjects', 'geometricObjects', 'ISO 19115', 'MD_VectorSpatialRepresentation', 'ISO 19115:MD_Metadata:spatialRepresentationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:spatialRepresentationInfo:geometricObjects:geometricObjectType', 'geometricObjectType', 'ISO 19115', 'MD_GeometricObjects', 'ISO 19115:MD_Metadata:spatialRepresentationInfo:geometricObjects', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:spatialRepresentationInfo:geometricObjects:geometricObjectCount', 'geometricObjectCount', 'ISO 19115', 'MD_GeometricObjects', 'ISO 19115:MD_Metadata:spatialRepresentationInfo:geometricObjects', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:series', 'series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:series:name', 'name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:citation:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:ISBN', 'ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:ISSN', 'ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:spatialResolution:distance', 'distance', 'ISO 19115', 'MD_Resolution', 'ISO 19115:MD_Metadata:identificationInfo:spatialResolution', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:spatialRepresentationInfo:numberOfDimensions', 'numberOfDimensions', 'ISO 19115', 'MD_GridSpatialRepresentation', 'ISO 19115:MD_Metadata:spatialRepresentationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:spatialRepresentationInfo:axisDimensionsProperties', 'axisDimensionsProperties', 'ISO 19115', 'MD_GridSpatialRepresentation', 'ISO 19115:MD_Metadata:spatialRepresentationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:spatialRepresentationInfo:axisDimensionsProperties:dimensionName', 'dimensionName', 'ISO 19115', 'MD_Dimension', 'ISO 19115:MD_Metadata:spatialRepresentationInfo:axisDimensionsProperties', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:spatialRepresentationInfo:axisDimensionsProperties:dimensionSize', 'dimensionSize', 'ISO 19115', 'MD_Dimension', 'ISO 19115:MD_Metadata:spatialRepresentationInfo:axisDimensionsProperties', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:spatialRepresentationInfo:axisDimensionsProperties:resolution', 'resolution', 'ISO 19115', 'MD_Dimension', 'ISO 19115:MD_Metadata:spatialRepresentationInfo:axisDimensionsProperties', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:spatialRepresentationInfo:cellGeometry', 'cellGeometry', 'ISO 19115', 'MD_GridSpatialRepresentation', 'ISO 19115:MD_Metadata:spatialRepresentationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:spatialRepresentationInfo:transformationParameterAvailability', 'transformationParameterAvailability', 'ISO 19115', 'MD_GridSpatialRepresentation', 'ISO 19115:MD_Metadata:spatialRepresentationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:edition', 'edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:series:page', 'page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:citation:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:collectiveTitle', 'collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:hierarchyLevel', 'hierarchyLevel', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:hierarchyLevelName', 'hierarchyLevelName', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:locale', 'locale', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataExtensionInfo', 'metadataExtensionInfo', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contentInfo', 'contentInfo', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:portrayalCatalogueInfo', 'portrayalCatalogueInfo', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:applicationSchemaInfo', 'applicationSchemaInfo', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataMaintenance', 'metadataMaintenance', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataConstraints', 'metadataConstraints', 'ISO 19115', 'MD_Metadata', 'ISO 19115:MD_Metadata', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact', 'pointOfContact', 'ISO 19115', 'FRA_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115 FRA 1.0');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName', 'organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo', 'contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address', 'address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address:electronicMailAddress', 'electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:role', 'role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:relatedCitation', 'relatedCitation', 'ISO 19115 FRA 1.0', 'FRA_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115 FRA 1.0');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:alternateTitle', 'alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:identifier', 'identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage', 'resourceSpecificUsage', 'ISO 19115', 'MD_Identification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement', 'verticalElement', 'ISO 19115', 'EX_Extent', 'ISO 19115:MD_Metadata:identificationInfo:extent', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorFormat', 'distributorFormat', 'ISO 19115', 'MD_Distributor', 'ISO 19115:MD_Metadata:distributionInfo:distributor', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions', 'distributorTransferOptions', 'ISO 19115', 'MD_Distributor', 'ISO 19115:MD_Metadata:distributionInfo:distributor', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription', 'levelDescription', 'ISO 19115', 'DQ_Scope', 'ISO 19115:MD_Metadata:dataQualityInfo:scope', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report', 'report', 'ISO 19115', 'DQ_DataQuality', 'ISO 19115:MD_Metadata:dataQualityInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep', 'processStep', 'ISO 19115', 'LI_Lineage', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:source', 'source', 'ISO 19115', 'LI_Lineage', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo:phone', 'phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:contact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo:phone:voice', 'voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:contact:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo:phone:facsimile', 'facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:contact:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:contact:contactInfo:onlineResource:protocol', 'protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:contact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'extendedElementInformation', 'ISO 19115', 'MD_MetadataExtensionInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:name', 'name', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:definition', 'definition', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:dataType', 'dataType', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:parentEntity', 'parentEntity', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:rationale', 'rationale', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source', 'source', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:phone', 'phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:phone:voice', 'voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:phone:facsimile', 'facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource:protocol', 'protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:Type', 'Type', 'ISO 19115', 'MD_Keywords', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ThesaurusName', 'ISO 19115', 'MD_Keywords', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:title', 'title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:alternateTitle', 'alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:date', 'date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:date:date', 'date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:date:dateType', 'dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:edition', 'edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:identifier', 'identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:identifier:code', 'code', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:identifier', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty', 'citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:presentationForm', 'presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:extentTypeCode', 'extentTypeCode', 'ISO 19115', 'EX_GeographicBoundingBox', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine:protocol', 'protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine:description', 'description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine:function', 'function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:measureDescription', 'measureDescription', 'ISO 19115', 'DQ_Element', 'ISO 19115:MD_Metadata:dataQualityInfo:report', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:evaluationMethodType', 'evaluationMethodType', 'ISO 19115', 'DQ_Element', 'ISO 19115:MD_Metadata:dataQualityInfo:report', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:evaluationMethodDescription', 'evaluationMethodDescription', 'ISO 19115', 'DQ_Element', 'ISO 19115:MD_Metadata:dataQualityInfo:report', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:dateTime', 'dateTime', 'ISO 19115', 'DQ_Element', 'ISO 19115:MD_Metadata:dataQualityInfo:report', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:nameOfMeasure', 'nameOfMeasure', 'ISO 19115', 'DQ_Element', 'ISO 19115:MD_Metadata:dataQualityInfo:report', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:measureIdentification', 'measureIdentification', 'ISO 19115', 'DQ_Element', 'ISO 19115:MD_Metadata:dataQualityInfo:report', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:measureIdentification:code', 'code', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:report:measureIdentification', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:measureIdentification:codeSpace', 'codeSpace', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:report:measureIdentification', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result', 'result', 'ISO 19115', 'DQ_Element', 'ISO 19115:MD_Metadata:dataQualityInfo:report', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification', 'specification', 'ISO 19115', 'DQ_Result', 'ISO 19115:MD_Metadata:dataQualityInfo:report:result', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result:explanation', 'explanation', 'ISO 19115', 'DQ_Result', 'ISO 19115:MD_Metadata:dataQualityInfo:report:result', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result:pass', 'pass', 'ISO 19115', 'DQ_Result', 'ISO 19115:MD_Metadata:dataQualityInfo:report:result', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:title', 'title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:date', 'date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:date:date', 'date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:date:dateType', 'dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:citation:identifier:code', 'code', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:identificationInfo:citation:identifier', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contact:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:contact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contact:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:contact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contact:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:contact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contact:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:contact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contact:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:contact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contact:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:contact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contact:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:contact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contact:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:contact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contact:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:contact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:locale:languageCode','languageCode', 'ISO 19115', 'PT_Locale', 'ISO 19115:MD_Metadata:locale', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:locale:characterEncoding','characterEncoding', 'ISO 19115', 'PT_Locale', 'ISO 19115:MD_Metadata:locale', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:locale:country','country', 'ISO 19115', 'PT_Locale', 'ISO 19115:MD_Metadata:locale', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority','authority', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:identifier','identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:identifier:code','code', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:authority:identifier', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:codeSpace','codeSpace', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:version','version', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection','projection', 'ISO 19115', 'MD_CRS', 'ISO 19115:MD_Metadata:referenceSystemInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:code','code', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:version','version', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:codeSpace','codeSpace', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority','authority', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid','ellipsoid', 'ISO 19115', 'MD_CRS', 'ISO 19115:MD_Metadata:referenceSystemInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:code','code', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:version','version', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:codeSpace','codeSpace', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority','authority', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum','datum', 'ISO 19115', 'MD_CRS', 'ISO 19115:MD_Metadata:referenceSystemInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:code','code', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:version','version', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:codeSpace','codeSpace', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority','authority', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:referenceSystemInfo:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:citation:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:citation:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:citation:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:citation:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:citation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:purpose','purpose', 'ISO 19115', 'MD_Identification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:dateOfNextUpdate','dateOfNextUpdate', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:userDefinedMaintenanceFrequency','userDefinedMaintenanceFrequency', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScope','updateScope', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription','updateScopeDescription', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription:attributes','attributes', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription:features','features', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription:featureInstances','featureInstances', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription:attributeInstances','attributeInstances', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription:dataset','dataset', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription:other','other', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:updateScopeDescription', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:maintenanceNote','maintenanceNote', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceFormat:amendmentNumber','amendmentNumber', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:identificationInfo:resourceFormat', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceFormat:specification','specification', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:identificationInfo:resourceFormat', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceFormat:fileDecompressionTechnique','fileDecompressionTechnique', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:identificationInfo:resourceFormat', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation','citation', 'ISO 19115', 'FRA_Constraints', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints', 'ISO 19115 FRA 1.0');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:phone', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:citation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:userNote','userNote', 'ISO 19115', 'MD_SecurityConstraints', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:classificationSystem','classificationSystem', 'ISO 19115', 'MD_SecurityConstraints', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:handlingDescription','handlingDescription', 'ISO 19115', 'MD_SecurityConstraints', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:environnementDescription','environnementDescription', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:description','description', 'ISO 19115', 'EX_Extent', 'ISO 19115:MD_Metadata:identificationInfo:extent', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:extentTypeCode','extentTypeCode', 'ISO 19115', 'EX_GeographicExtent', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority','authority', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4','geographicElement4', 'ISO 19115', 'EX_Extent', 'ISO 19115:MD_Metadata:identificationInfo:extent', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:extentTypeCode','extentTypeCode', 'ISO 19115', 'EX_GeographicExtent', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon','polygon', 'ISO 19115', 'EX_BoundingPolygon', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:minimumValue','minimumValue', 'ISO 19115', 'EX_VerticalExtent', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:maximumValue','maximumValue', 'ISO 19115', 'EX_VerticalExtent', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:unitOfMeasure','unitOfMeasure', 'ISO 19115', 'EX_VerticalExtent', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS','verticalCRS', 'ISO 19115', 'EX_VerticalExtent', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:identifier','identifier', 'ISO 19115', 'VerticalCRS', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS', 'ISO 19108');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:identifier:code','code', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:identifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty','verticalCSProperty', 'ISO 19115', 'VerticalCRS', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS', 'ISO 19108');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:identifier','identifier', 'ISO 19115', 'VerticalCS', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty', 'ISO 19108');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:identifier:code','code', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:identifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis','axis', 'ISO 19115', 'VerticalCS', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty', 'ISO 19108');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis:identifier','identifier', 'ISO 19115', 'CoordinateSystemAxis', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis', 'ISO 19108');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis:identifier:code','code', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis:identifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis:identifier:codespace','codespace', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis:identifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis:axisAbbrev','axisAbbrev', 'ISO 19115', 'CoordinateSystemAxis', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis', 'ISO 19108');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis:axisDirection','axisDirection', 'ISO 19115', 'CoordinateSystemAxis', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis', 'ISO 19108');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis:uom','uom', 'ISO 19115', 'CoordinateSystemAxis', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalCSProperty:axis', 'ISO 19108');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalDatumProperty','verticalDatumProperty', 'ISO 19115', 'VerticalCRS', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS', 'ISO 19108');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalDatumProperty:identifier','identifier', 'ISO 19115', 'VerticalDatum', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalDatumProperty', 'ISO 19108');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalDatumProperty:identifier:code','code', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:identificationInfo:extent:verticalElement:verticalCRS:verticalDatumProperty:identifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:supplementalInformation','supplementalInformation', 'ISO 19115', 'MD_DataIdentification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributionFormat:amendmentNumber','amendmentNumber', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:distributionInfo:distributionFormat', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributionFormat:specification','specification', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:distributionInfo:distributionFormat', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributionFormat:fileDecompressionTechnique','fileDecompressionTechnique', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:distributionInfo:distributionFormat', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorFormat:name','name', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorFormat', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorFormat:version','version', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorFormat', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorFormat:amendmentNumber','amendmentNumber', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorFormat', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorFormat:specification','specification', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorFormat', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorFormat:fileDecompressionTechnique','fileDecompressionTechnique', 'ISO 19115', 'MD_Format', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorFormat', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:unitsOfDistribution','unitsOfDistribution', 'ISO 19115', 'MD_DigitalTransferOptions', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:transferSize','transferSize', 'ISO 19115', 'MD_DigitalTransferOptions', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine','onLine', 'ISO 19115', 'MD_DigitalTransferOptions', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine','offLine', 'ISO 19115', 'MD_DigitalTransferOptions', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:onLine', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine:name','name', 'ISO 19115', 'MD_Medium', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine:density','density', 'ISO 19115', 'MD_Medium', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine:densityUnits','densityUnits', 'ISO 19115', 'MD_Medium', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine:volumes','volumes', 'ISO 19115', 'MD_Medium', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine:mediumFormat','mediumFormat', 'ISO 19115', 'MD_Medium', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine:mediumName','mediumName', 'ISO 19115', 'MD_Medium', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributorTransferOptions:offLine', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributionOrderProcess','distributionOrderProcess', 'ISO 19115', 'MD_Distributor', 'ISO 19115:MD_Metadata:distributionInfo:distributor', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributionOrderProcess:fees','fees', 'ISO 19115', 'MD_StandardOrderProcess', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributionOrderProcess', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributionOrderProcess:plannedAvailableDateTime','plannedAvailableDateTime', 'ISO 19115', 'MD_StandardOrderProcess', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributionOrderProcess', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributionOrderProcess:orderingInstructions','orderingInstructions', 'ISO 19115', 'MD_StandardOrderProcess', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributionOrderProcess', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:distributor:distributionOrderProcess:turnaround','turnaround', 'ISO 19115', 'MD_StandardOrderProcess', 'ISO 19115:MD_Metadata:distributionInfo:distributor:distributionOrderProcess', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:unitsOfDistribution','unitsOfDistribution', 'ISO 19115', 'MD_DigitalTransferOptions', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions:onLine', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:distributionInfo:transferOptions:offLine:densityUnits','densityUnits', 'ISO 19115', 'MD_Medium', 'ISO 19115:MD_Metadata:distributionInfo:transferOptions:offLine', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoidParameters','ellipsoidParameters', 'ISO 19115', 'MD_CRS', 'ISO 19115:MD_Metadata:referenceSystemInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoidParameters:semiMajorAxis','semiMajorAxis', 'ISO 19115', 'MD_EllipsoidParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoidParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoidParameters:axisUnits','axisUnits', 'ISO 19115', 'MD_EllipsoidParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoidParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoidParameters:denominatorOfFlatetteningRatio','denominatorOfFlatetteningRatio', 'ISO 19115', 'MD_EllipsoidParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:ellipsoidParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters','projectionParameters', 'ISO 19115', 'MD_CRS', 'ISO 19115:MD_Metadata:referenceSystemInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:zone','zone', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:standardParallel','standardParallel', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:longitudeOfCentralMeridian','longitudeOfCentralMeridian', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:latitudeOfProjectionOrigin','latitudeOfProjectionOrigin', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:0Easting','0Easting', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:0Northing','0Northing', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:0EastingNorthingUnits','0EastingNorthingUnits', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:scaleFactorAtEquator','scaleFactorAtEquator', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:heightOfProspectivePointAboveSurface','heightOfProspectivePointAboveSurface', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:longitudeOfProjectionCenter','longitudeOfProjectionCenter', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:latitudeOfProjectionCenter','latitudeOfProjectionCenter', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:scaleFactorAtCenterLine','scaleFactorAtCenterLine', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:straightVerticalLongitudeFromPole','straightVerticalLongitudeFromPole', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters:scaleFactorAtProjectionOrigin','scaleFactorAtProjectionOrigin', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:referenceSystemInfo:projectionParameters', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation','featureCatalogueCitation', 'ISO 19115', 'MD_FeatureCatalogueDescription', 'ISO 19115:MD_Metadata:contentInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact','contact', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceMaintenance:contact:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource','extensionOnLineResource', 'ISO 19115', 'MD_MetadataExtensionInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extensionOnLineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:shortName','shortName', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:domainCode','domainCode', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:obligation','obligation', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:condition','condition', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:maximumOccurrence','maximumOccurrence', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:domainValue','domainValue', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:parentIdentity','parentIdentity', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:rule','rule', 'ISO 19115', 'MD_ExtendedElementInformation', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:phone', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataExtensionInfo:extendedElementInformation:source', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo','aggregationInfo', 'ISO 19115', 'MD_Identification', 'ISO 19115:MD_Metadata:identificationInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:associationType','associationType', 'ISO 19115', 'MD_AggregateInformation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:initiativeType','initiativeType', 'ISO 19115', 'MD_AggregateInformation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName','aggregateDataSetName', 'ISO 19115', 'MD_AggregateInformation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:phone', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:identifier','identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier','aggregateDataSetIdentifier', 'ISO 19115', 'MD_AggregateInformation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority','authority', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:identifier','identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier:code','code', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetIdentifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:phone', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:codeSpace','codeSpace', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:ThesaurusName', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:specificUsage','specificUsage', 'ISO 19115', 'MD_Usage', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:usageDateTime','usageDateTime', 'ISO 19115', 'MD_Usage', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userDeterminedLimitations','userDeterminedLimitations', 'ISO 19115', 'MD_Usage', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo','userContactInfo', 'ISO 19115', 'MD_Usage', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:phone', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:identificationInfo:resourceSpecificUsage:userContactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:complianceCode','complianceCode', 'ISO 19115', 'MD_FeatureCatalogueDescription', 'ISO 19115:MD_Metadata:contentInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:language','language', 'ISO 19115', 'MD_FeatureCatalogueDescription', 'ISO 19115:MD_Metadata:contentInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:includedWithDataset','includedWithDataset', 'ISO 19115', 'MD_FeatureCatalogueDescription', 'ISO 19115:MD_Metadata:contentInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureTypes','featureTypes', 'ISO 19115', 'MD_FeatureCatalogueDescription', 'ISO 19115:MD_Metadata:contentInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:identifier','identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:contentInfo:featureCatalogueCitation', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:attributeDescription','attributeDescription', 'ISO 19115', 'MD_CoverageDescription', 'ISO 19115:MD_Metadata:contentInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:contentType','contentType', 'ISO 19115', 'MD_CoverageDescription', 'ISO 19115:MD_Metadata:contentInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension','dimension', 'ISO 19115', 'MD_CoverageDescription', 'ISO 19115:MD_Metadata:contentInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:sequenceIdentifier','sequenceIdentifier', 'ISO 19115', 'MD_RangeDimension', 'ISO 19115:MD_Metadata:contentInfo:dimension', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:sequenceIdentifier:attributeType','attributeType', 'ISO 19115', 'MemberName', 'ISO 19115:MD_Metadata:contentInfo:dimension:sequenceIdentifier', 'ISO 19103');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:sequenceIdentifier:attributeType:aName','aName', 'ISO 19115', 'GenericName', 'ISO 19115:MD_Metadata:contentInfo:dimension:sequenceIdentifier:attributeType', 'ISO 19103');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:sequenceIdentifier:aName','aName', 'ISO 19115', 'GenericName', 'ISO 19115:MD_Metadata:contentInfo:dimension:sequenceIdentifier', 'ISO 19103');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:descriptor','descriptor', 'ISO 19115', 'MD_RangeDimension', 'ISO 19115:MD_Metadata:contentInfo:dimension', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:maxValue','maxValue', 'ISO 19115', 'MD_Band', 'ISO 19115:MD_Metadata:contentInfo:dimension', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:minValue','minValue', 'ISO 19115', 'MD_Band', 'ISO 19115:MD_Metadata:contentInfo:dimension', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:units','units', 'ISO 19115', 'MD_Band', 'ISO 19115:MD_Metadata:contentInfo:dimension', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:peakResponse','peakResponse', 'ISO 19115', 'MD_Band', 'ISO 19115:MD_Metadata:contentInfo:dimension', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:bitsPerValue','bitsPerValue', 'ISO 19115', 'MD_Band', 'ISO 19115:MD_Metadata:contentInfo:dimension', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:toneGradation','toneGradation', 'ISO 19115', 'MD_Band', 'ISO 19115:MD_Metadata:contentInfo:dimension', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:scaleFactor','scaleFactor', 'ISO 19115', 'MD_Band', 'ISO 19115:MD_Metadata:contentInfo:dimension', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:contentInfo:dimension:offset','offset', 'ISO 19115', 'MD_Band', 'ISO 19115:MD_Metadata:contentInfo:dimension', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent','extent', 'ISO 19115', 'EX_Extent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:description','description', 'ISO 19115', 'DQ_Scope', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement2', 'geographicElement2', 'ISO 19115', 'EX_Extent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement2:extentTypeCode','extentTypeCode', 'ISO 19115', 'EX_GeographicExtent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement2', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement2:westBoundLongitude', 'westBoundLongitude', 'ISO 19115', 'EX_GeographicBoundingBox', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement2', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement2:eastBoundLongitude', 'eastBoundLongitude', 'ISO 19115', 'EX_GeographicBoundingBox', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement2', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement2:northBoundLatitude', 'northBoundLatitude', 'ISO 19115', 'EX_GeographicBoundingBox', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement2', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement2:southBoundLatitude', 'southBoundLatitude', 'ISO 19115', 'EX_GeographicBoundingBox', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement2', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3', 'geographicElement3', 'ISO 19115', 'EX_Extent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:extentTypeCode','extentTypeCode', 'ISO 19115', 'EX_GeographicExtent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier','geographicIdentifier', 'ISO 19115', 'EX_GeographicDescription', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:code','code', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority','authority', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:identifier','identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement3:geographicIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement4','geographicElement4', 'ISO 19115', 'EX_Extent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement4:extentTypeCode','extentTypeCode', 'ISO 19115', 'EX_GeographicExtent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement4', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement4:polygon','polygon', 'ISO 19115', 'EX_BoundingPolygon', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:geographicElement4', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:temporalElement', 'temporalElement', 'ISO 19115', 'EX_Extent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:temporalElement:extent', 'extent', 'ISO 19115', 'EX_TemporalExtent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:temporalElement', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:temporalElement:extent:beginPosition', 'beginPosition', 'ISO 19108', 'TimePeriod', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:temporalElement:extent', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:temporalElement:extent:endPosition', 'endPosition', 'ISO 19108', 'TimePeriod', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:temporalElement:extent', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:verticalElement:minimumValue', 'minimumValue', 'ISO 19115', 'EX_VerticalExtent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:temporalElement:extent', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:verticalElement:maximumValue', 'maximumValue', 'ISO 19115', 'EX_VerticalExtent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:temporalElement:extent', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:verticalElement:unitOfMeasure', 'unitOfMeasure', 'ISO 19115', 'EX_VerticalExtent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:temporalElement:extent', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:verticalElement:verticalDatum', 'verticalDatum', 'ISO 19115', 'EX_VerticalExtent', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:extent:temporalElement:extent', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription:attributes','attributes', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription:features','features', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription:featureInstances','featureInstances', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription:attributeInstances','attributeInstances', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription:dataset','dataset', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription:other','other', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:dataQualityInfo:scope:levelDescription', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:description','description', 'ISO 19115', 'LI_ProcessStep', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:rationale','rationale', 'ISO 19115', 'LI_ProcessStep', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:dateTime','dateTime', 'ISO 19115', 'LI_ProcessStep', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor','processor', 'ISO 19115', 'LI_ProcessStep', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:processor', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source','source', 'ISO 19115', 'LI_ProcessStep', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem','sourceReferenceSystem', 'ISO 19115', 'LI_Source', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:description','description', 'ISO 19115', 'LI_Source', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:scaleDenominator','scaleDenominator', 'ISO 19115', 'LI_Source', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:scaleDenominator:denominator','denominator', 'ISO 19115', 'MD_RepresentativeFraction', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:scaleDenominator', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier','referenceSystemIdentifier', 'ISO 19115', 'MD_ReferenceSystem', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:code','code', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:codeSpace','codeSpace', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:version','version', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority','authority', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:identifier','identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:referenceSystemIdentifier:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection','projection', 'ISO 19115', 'MD_CRS', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid','ellipsoid', 'ISO 19115', 'MD_CRS', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum','datum', 'ISO 19115', 'MD_CRS', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address:city', 'city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address:deliveryPoint', 'deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address:postalCode', 'postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address:country', 'country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource:linkage', 'linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:identificationInfo:pointOfContact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority','authority', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:code','code', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:version','version', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:identifier','identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:codeSpace','codeSpace', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projection:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority','authority', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:code','code', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:version','version', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:identifier','identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:codeSpace','codeSpace', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoid:authority', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority','authority', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:code','code', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:version','version', 'ISO 19115', 'RS_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:identifier','identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:date', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:phone', 'ISO 19115');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:address', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:contactInfo', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:citedResponsibleParty', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:series', 'ISO 19115');

INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:codeSpace','codeSpace', 'ISO 19115', 'MD_Identifier', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:datum:authority', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoidParameters','ellipsoidParameters', 'ISO 19115', 'MD_CRS', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoidParameters:semiMajorAxis','semiMajorAxis', 'ISO 19115', 'MD_EllipsoidParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoidParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoidParameters:axisUnits','axisUnits', 'ISO 19115', 'MD_EllipsoidParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoidParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoidParameters:denominatorOfFlatetteningRatio','denominatorOfFlatetteningRatio', 'ISO 19115', 'MD_EllipsoidParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:ellipsoidParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters','projectionParameters', 'ISO 19115', 'MD_CRS', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:zone','zone', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:standardParallel','standardParallel', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:longitudeOfCentralMeridian','longitudeOfCentralMeridian', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:latitudeOfProjectionOrigin','latitudeOfProjectionOrigin', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:0Easting','0Easting', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:0Northing','0Northing', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:0EastingNorthingUnits','0EastingNorthingUnits', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:scaleFactorAtEquator','scaleFactorAtEquator', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:heightOfProspectivePointAboveSurface','heightOfProspectivePointAboveSurface', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:longitudeOfProjectionCenter','longitudeOfProjectionCenter', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:latitudeOfProjectionCenter','latitudeOfProjectionCenter', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:scaleFactorAtCenterLine','scaleFactorAtCenterLine', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:straightVerticalLongitudeFromPole','straightVerticalLongitudeFromPole', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters:scaleFactorAtProjectionOrigin','scaleFactorAtProjectionOrigin', 'ISO 19115', 'MD_ProjectionParameters', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceReferenceSystem:projectionParameters', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation','sourceCitation', 'ISO 19115', 'LI_Source', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:identifier','identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:lineage:processStep:source:sourceCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation','citation', 'ISO 19115', 'MD_Constraints', 'ISO 19115:MD_Metadata:metadataConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:title','title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:identifier','identifier', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:alternateTitle','alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:date','date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:date:date','date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:metadataConstraints:citation:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:date:dateType','dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:metadataConstraints:citation:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:edition','edition', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:editionDate','editionDate', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty','citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataConstraints:citation:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:presentationForm','presentationForm', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:series','series', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:series:name','name', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:metadataConstraints:citation:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:series:issueIdentification','issueIdentification', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:metadataConstraints:citation:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:series:page','page', 'ISO 19115', 'CI_Series', 'ISO 19115:MD_Metadata:metadataConstraints:citation:series', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:otherCitationDetails','otherCitationDetails', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:collectiveTitle','collectiveTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:ISBN','ISBN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:citation:ISSN','ISSN', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:metadataConstraints:citation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:accessConstraints','accessConstraints', 'ISO 19115', 'MD_LegalConstraints', 'ISO 19115:MD_Metadata:metadataConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:useConstraints','useConstraints', 'ISO 19115', 'MD_LegalConstraints', 'ISO 19115:MD_Metadata:metadataConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:otherConstraints','otherConstraints', 'ISO 19115', 'MD_LegalConstraints', 'ISO 19115:MD_Metadata:metadataConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:classification','classification', 'ISO 19115', 'MD_SecurityConstraints', 'ISO 19115:MD_Metadata:metadataConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:userNote','userNote', 'ISO 19115', 'MD_SecurityConstraints', 'ISO 19115:MD_Metadata:metadataConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:classificationSystem','classificationSystem', 'ISO 19115', 'MD_SecurityConstraints', 'ISO 19115:MD_Metadata:metadataConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:handlingDescription','handlingDescription', 'ISO 19115', 'MD_SecurityConstraints', 'ISO 19115:MD_Metadata:metadataConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataConstraints:useLimitation','useLimitation', 'ISO 19115', 'MD_Constraints', 'ISO 19115:MD_Metadata:metadataConstraints', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:dateOfNextUpdate','dateOfNextUpdate', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:metadataMaintenance', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:userDefinedMaintenanceFrequency','userDefinedMaintenanceFrequency', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:metadataMaintenance', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:updateScope','updateScope', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:metadataMaintenance', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription','updateScopeDescription', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:metadataMaintenance', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription:attributes','attributes', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription:features','features', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription:featureInstances','featureInstances', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription:attributeInstances','attributeInstances', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription:dataset','dataset', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription:other','other', 'ISO 19115', 'MD_ScopeDescription', 'ISO 19115:MD_Metadata:metadataMaintenance:updateScopeDescription', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:maintenanceNote','maintenanceNote', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:metadataMaintenance', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact','contact', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:metadataMaintenance', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:individualName','individualName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataMaintenance:contact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:positionName','positionName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataMaintenance:contact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:organisationName','organisationName', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataMaintenance:contact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:role','role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataMaintenance:contact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo','contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:metadataMaintenance:contact', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:phone','phone', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:phone:voice','voice', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:phone:facsimile','facsimile', 'ISO 19115', 'CI_Telephone', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:phone', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address','address', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address:deliveryPoint','deliveryPoint', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address:city','city', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address:postalCode','postalCode', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address:electronicMailAddress','electronicMailAddress', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address:administrativeArea','administrativeArea', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address:country','country', 'ISO 19115', 'CI_Address', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:address', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:hoursOfService','hoursOfService', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:contactInstructions','contactInstructions', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource','onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource:linkage','linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource:protocol','protocol', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource:function','function', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource:applicationProfile','applicationProfile', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource:name','name', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource:description','description', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:metadataMaintenance:contact:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:metadataMaintenance:maintenanceAndUpdateFrequency', 'maintenanceAndUpdateFrequency', 'ISO 19115', 'MD_MaintenanceInformation', 'ISO 19115:MD_Metadata:metadataMaintenance', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:identifier', 'identifier', 'ISO 19115', 'LineString', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:name', 'name', 'ISO 19115', 'LineString', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:coordinates', 'coordinates', 'ISO 19115', 'LineString', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:citedResponsibleParty', 'citedResponsibleParty', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:citedResponsibleParty:contactInfo', 'contactInfo', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:citedResponsibleParty:contactInfo:onlineResource', 'onlineResource', 'ISO 19115', 'CI_Contact', 'ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:citedResponsibleParty:contactInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:citedResponsibleParty:contactInfo:onlineResource:linkage', 'linkage', 'ISO 19115', 'CI_OnlineResource', 'ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:citedResponsibleParty:contactInfo:onlineResource', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:citedResponsibleParty:role', 'role', 'ISO 19115', 'CI_ResponsibleParty', 'ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:citedResponsibleParty', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:portrayalCatalogueInfo:portrayalCatalogueCitation', 'portrayalCatalogueCitation', 'ISO 19115', 'MD_PortrayalCatalogueReference', 'ISO 19115:MD_Metadata:portrayalCatalogueInfo', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:portrayalCatalogueInfo:portrayalCatalogueCitation:title', 'title', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:portrayalCatalogueInfo:portrayalCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:portrayalCatalogueInfo:portrayalCatalogueCitation:alternateTitle', 'alternateTitle', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:portrayalCatalogueInfo:portrayalCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:portrayalCatalogueInfo:portrayalCatalogueCitation:date', 'date', 'ISO 19115', 'CI_Citation', 'ISO 19115:MD_Metadata:portrayalCatalogueInfo:portrayalCatalogueCitation', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:portrayalCatalogueInfo:portrayalCatalogueCitation:date:date', 'date', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:portrayalCatalogueInfo:portrayalCatalogueCitation:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:portrayalCatalogueInfo:portrayalCatalogueCitation:date:dateType', 'dateType', 'ISO 19115', 'CI_Date', 'ISO 19115:MD_Metadata:portrayalCatalogueInfo:portrayalCatalogueCitation:date', 'ISO 19115');
INSERT INTO "Schemas"."Paths"  VALUES ('ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:classification', 'classification', 'ISO 19115', 'MD_SecurityConstraints', 'ISO 19115:MD_Metadata:identificationInfo:resourceConstraints', 'ISO 19115');




INSERT INTO "Schemas"."Properties"  VALUES ('title', 'resTitle', 'ISO 19115', 'Title', 1, 1, 'CI_Citation', 'CharacterString', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('alternateTitle', 'resAltTitle', 'ISO 19115', 'Alternate title of the resource', 0, 2147483647, 'CI_Citation', 'CharacterString', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('date', 'refDate', 'ISO 19115', 'Date of dataset creation', 1, 1, 'CI_Date', 'Date', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('dateType', 'refDateType', 'ISO 19115', 'Event used for reference date', 1, 1, 'CI_Date', NULL, 'CI_DateTypeCode', 'M', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('date', 'resRefDate', 'ISO 19115', 'Reference date', 1, 2147483647, 'CI_Citation', 'CI_Date', NULL, 'M', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('edition', 'resEd', 'ISO 19115', 'Dataset Version', 0, 1, 'CI_Citation', 'CharacterString', NULL, 'O', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('editionDate', 'resEdDate', 'ISO 19115', 'Date of edition', 0, 1, 'CI_Citation', 'Date', NULL, 'O', 5, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('identifier', 'citId', 'ISO 19115', 'Identifier of the cited resource', 0, 2147483647, 'CI_Citation', 'MD_Identifier', NULL, 'O', 6, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('citedResponsibleParty', 'citRespParty', 'ISO 19115', 'Cited responsible party for the dataset', 0, 2147483647, 'CI_Citation', 'CI_ResponsibleParty', NULL, 'O', 7, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('presentationForm', 'presForm', 'ISO 19115', 'Presentation form of the dataset', 0, 2147483647, 'CI_Citation', NULL, 'CI_PresentationFormCode', 'O', 8, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('name', 'seriesName', 'ISO 19115', 'Series name', 0, 1, 'CI_Series', 'CharacterString', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('issueIdentification', 'issId', 'ISO 19115', 'Issue identification', 0, 1, 'CI_Series', 'CharacterString', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('page', 'artPage', 'ISO 19115', 'Number of page', 0, 1, 'CI_Series', 'CharacterString', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('series', 'datasetSeries', 'ISO 19115', 'Series information', 0, 1, 'CI_Citation', 'CI_Series', NULL, 'O', 9, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('otherCitationDetails', 'otherCitDet', 'ISO 19115', 'Other citation details', 0, 1, 'CI_Citation', 'CharacterString', NULL, 'O', 10, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('collectiveTitle', 'collTitle', 'ISO 19115', 'Collective title of the cited resource', 0, 1, 'CI_Citation', 'CharacterString', NULL, 'O', 11, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('ISBN', 'isbn', 'ISO 19115', 'ISBN', 0, 1, 'CI_Citation', 'CharacterString', NULL, 'O', 12, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('ISSN', 'issn', 'ISO 19115', 'ISSN', 0, 1, 'CI_Citation', 'CharacterString', NULL, 'O', 13, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('authority', 'identAuth', 'ISO 19115', 'Identifier authority', 0, 1, 'MD_Identifier', 'CI_Citation', NULL, 'O', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('code', 'identCode', 'ISO 19115', 'Geographic Identifier', 1, 1, 'MD_Identifier', 'CharacterString', NULL, 'M', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('nameOfMeasure', 'measName', 'ISO 19115', 'Name of measure', 0, 2147483647, 'DQ_Element', 'CharacterString', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('measureIdentification', 'measId', 'ISO 19115', 'Measure identification', 0, 1, 'DQ_Element', 'MD_Identifier', NULL, 'O', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('measureDescription', 'measDesc', 'ISO 19115', 'Measure description', 0, 1, 'DQ_Element', 'CharacterString', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('evaluationMethodType', 'evalMethType', 'ISO 19115', 'Evaluation method type', 0, 1, 'DQ_Element', NULL, 'DQ_EvaluationMethodTypeCode', 'O', 4, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('evaluationMethodDescription', 'evalMethDesc', 'ISO 19115', 'Evaluation method description', 0, 1, 'DQ_Element', 'CharacterString', NULL, 'O', 5, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('evaluationProcedure', 'evalProc', 'ISO 19115', 'Evaluation procedure', 0, 1, 'DQ_Element', 'CI_Citation', NULL, 'O', 6, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('dateTime', 'measDateTm', 'ISO 19115', 'Date time of data quality measure', 0, 2147483647, 'DQ_Element', 'DateTime', NULL, 'O', 7, 'ISO 19108', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('result', 'measResult', 'ISO 19115', 'Result of quality measure', 1, 2, 'DQ_Element', 'DQ_Result', NULL, 'M', 8, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('level', 'scpLvl', 'ISO 19115', 'Scope level of data', 1, 1, 'DQ_Scope', NULL, 'MD_ScopeCode', 'M', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('description', 'exDesc', 'ISO 19115', 'Description of the extension', 0, 1, 'EX_Extent', 'CharacterString', NULL, 'C', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('extentTypeCode', 'exTypeCode', 'ISO 19115', 'Extent type code', 0, 1, 'EX_GeographicBoundingBox', 'Boolean', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('westBoundLongitude', 'westBL', 'ISO 19115', 'West bound longitude', 1, 1, 'EX_GeographicBoundingBox', 'Decimal', NULL, 'M', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('eastBoundLongitude', 'eastBL', 'ISO 19115', 'East bound longitude', 1, 1, 'EX_GeographicBoundingBox', 'Decimal', NULL, 'M', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('southBoundLatitude', 'southBL', 'ISO 19115', 'South bound latitude', 1, 1, 'EX_GeographicBoundingBox', 'Decimal', NULL, 'M', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('northBoundLatitude', 'northBL', 'ISO 19115', 'North bound latitude', 1, 1, 'EX_GeographicBoundingBox', 'Decimal', NULL, 'M', 5, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('geographicElement2', 'geoEle', 'ISO 19115', 'Geographic extent of dataset', 0, 2147483647, 'EX_Extent', 'EX_GeographicBoundingBox', NULL, 'C', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('extentTypeCode', 'exTypeCode', 'ISO 19115', 'Extent type code', 0, 1, 'EX_GeographicDescription', 'Boolean', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('geographicIdentifier', 'geoId', 'ISO 19115', 'Geographic identifier', 1, 1, 'EX_GeographicDescription', 'MD_Identifier', NULL, 'M', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('geographicElement3', 'geoEle', 'ISO 19115', 'Geographic extent of dataset', 0, 2147483647, 'EX_Extent', 'EX_GeographicDescription', NULL, 'C', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('extentTypeCode', 'exTypeCode', 'ISO 19115', 'Extent type code', 0, 1, 'EX_BoundingPolygon', 'Boolean', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('polygon', 'polygon', 'ISO 19115', 'Bouding polygon', 1, 2147483647, 'EX_BoundingPolygon', 'rubrique', NULL, 'M', 2, 'MDWEB', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('geographicElement4', 'geoEle', 'ISO 19115', 'Geographic extent of dataset', 0, 2147483647, 'EX_Extent', 'EX_BoundingPolygon', NULL, 'C', 4, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('extent', 'exTemp', 'ISO 19115', 'Temporal extent of the dataset', 1, 1, 'EX_TemporalExtent', 'TimePeriod', NULL, 'M', 1, 'ISO 19108', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('temporalElement', 'tempEle', 'ISO 19115', 'Temporal  extent of dataset', 0, 2147483647, 'EX_Extent', 'EX_TemporalExtent', NULL, 'C', 5, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('minimumValue', 'vertMinVal', 'ISO 19115', 'Vertical minimum value', 1, 1, 'EX_VerticalExtent', 'Real', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('maximumValue', 'vertMaxVal', 'ISO 19115', 'Vertical minimum value', 1, 1, 'EX_VerticalExtent', 'Real', NULL, 'M', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('unitOfMeasure', 'vertUoM', 'ISO 19115', 'Vertical units of measure', 1, 1, 'EX_VerticalExtent', 'UomLength', NULL, 'M', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('verticalDatum', 'vertDatum', 'ISO 19115', 'Vertical datum information', 1, 1, 'EX_VerticalExtent', 'rubrique', NULL, 'M', 4, 'MDWEB', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('verticalCRS', 'vertCRS', 'ISO 19115', 'Vertical CRS information', 1, 1, 'EX_VerticalExtent', 'VerticalCRS', NULL, 'M', 4, 'ISO 19108', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('verticalElement', 'vertEle', 'ISO 19115', 'Vertical component of extent', 0, 2147483647, 'EX_Extent', 'EX_VerticalExtent', NULL, 'C', 6, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('extent', 'scpExt', 'ISO 19115', 'Scope extent', 0, 1, 'DQ_Scope', 'EX_Extent', NULL, 'O', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('attributes', 'attribSet', 'ISO 19115', 'Attributes', 0, 1, 'MD_ScopeDescription', 'rubrique', NULL, 'O', 1, 'MDWEB', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('features', 'featSet', 'ISO 19115', 'Features', 0, 1, 'MD_ScopeDescription', 'rubrique', NULL, 'O', 2, 'MDWEB', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('featureInstances', 'featIntSet', 'ISO 19115', 'Feature instances', 0, 1, 'MD_ScopeDescription', 'rubrique', NULL, 'O', 3, 'MDWEB', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('attributeInstances', 'attribIntSet', 'ISO 19115', 'Attribute instances', 0, 1, 'MD_ScopeDescription', 'rubrique', NULL, 'O', 4, 'MDWEB', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('dataset', 'datasetSet', 'ISO 19115', 'Dataset', 0, 1, 'MD_ScopeDescription', 'CharacterString', NULL, 'O', 5, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('other', 'other', 'ISO 19115', 'Other', 0, 1, 'MD_ScopeDescription', 'CharacterString', NULL, 'O', 6, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('levelDescription', 'scpLvlDesc', 'ISO 19115', 'Scope level description', 0, 2147483647, 'DQ_Scope', 'MD_ScopeDescription', NULL, 'C', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('scope', 'dqScope', 'ISO 19115', 'Data Scope', 1, 1, 'DQ_DataQuality', 'DQ_Scope', NULL, 'M', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('report', 'dqReport', 'ISO 19115', 'Report', 0, 2147483647, 'DQ_DataQuality', 'DQ_Element', NULL, 'C', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('statement', 'statement', 'ISO 19115', 'Statement of dataset production', 0, 1, 'LI_Lineage', 'CharacterString', NULL, 'C', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('description', 'stepDesc', 'ISO 19115', 'Description of the process step', 1, 1, 'LI_ProcessStep', 'CharacterString', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('rationale', 'stepRat', 'ISO 19115', 'Requirement or purpose for the process step', 0, 1, 'LI_ProcessStep', 'CharacterString', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('dateTime', 'stepDateTm', 'ISO 19115', 'Data and time of the process step occurred', 0, 1, 'LI_ProcessStep', 'DateTime', NULL, 'O', 3, 'ISO 19108', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('processor', 'stepProc', 'ISO 19115', 'Identification of person(s) and organisation(s)', 0, 2147483647, 'LI_ProcessStep', 'CI_ResponsibleParty', NULL, 'O', 4, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('description', 'srcDesc', 'ISO 19115', 'Description of the source data', 0, 1, 'LI_Source', 'text', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('denominator', 'rfDenom', 'ISO 19115', 'Scale of the dataset', 1, 1, 'MD_RepresentativeFraction', 'Integer', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('scaleDenominator', 'srcScale', 'ISO 19115', 'Scale denomintator of the source data', 0, 1, 'LI_Source', 'MD_RepresentativeFraction', NULL, 'O', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('codeSpace', 'identCodeSpace', 'ISO 19115', 'Name or identifier of the organisation responsible for namespace', 0, 1, 'RS_Identifier', 'CharacterString', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('version', 'identVrsn', 'ISO 19115', 'Version identifier for the namespace', 0, 1, 'RS_Identifier', 'CharacterString', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('referenceSystemIdentifier', 'refSysId', 'ISO 19115', 'Name of reference system', 0, 1, 'MD_ReferenceSystem', 'RS_Identifier', NULL, 'C', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('sourceReferenceSystem', 'srcRefSys', 'ISO 19115', 'Reference system of the source data', 0, 1, 'LI_Source', 'MD_ReferenceSystem', NULL, 'O', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('sourceCitation', 'srcCitatn', 'ISO 19115', 'Citation for the source data', 0, 1, 'LI_Source', 'CI_Citation', NULL, 'O', 4, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('sourceExtent', 'srcExt', 'ISO 19115', 'Extent of the source data', 0, 1, 'LI_Source', 'EX_Extent', NULL, 'O', 5, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('sourceStep', 'srcStep', 'ISO 19115', 'Process step of the source data', 0, 1, 'LI_Source', 'LI_ProcessStep', NULL, 'O', 6, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('source', 'stepSrc', 'ISO 19115', 'Information about the source data used', 0, 2147483647, 'LI_ProcessStep', 'LI_Source', NULL, 'O', 5, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('processStep', 'prcStep', 'ISO 19115', 'Process step', 0, 2147483647, 'LI_Lineage', 'LI_ProcessStep', NULL, 'C', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('source', 'dataSource', 'ISO 19115', 'Source data', 0, 2147483647, 'LI_Lineage', 'LI_Source', NULL, 'C', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('lineage', 'dataLineage', 'ISO 19115', 'Data lineage', 0, 1, 'DQ_DataQuality', 'LI_Lineage', NULL, 'C', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('aggregateDataSetName', 'aggrDSName', 'ISO 19115', 'Citation information about aggregate dataset', 0, 1, 'MD_AggregateInformation', 'CI_Citation', NULL, 'O', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('aggregateDataSetIdentifier', 'aggrDSIdent', 'ISO 19115', 'Identification information about aggregate datatset', 0, 1, 'MD_AggregateInformation', 'MD_Identifier', NULL, 'O', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('associationType', 'assocType', 'ISO 19115', 'Association type of the aggregate dataset', 1, 1, 'MD_AggregateInformation', NULL, 'DS_AssociationTypeCode', 'M', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('initiativeType', 'initType', 'ISO 19115', 'Type of initiative under which the aggregate dataset was produced', 0, 1, 'MD_AggregateInformation', NULL, 'DS_InitiativeTypeCode', 'O', 4, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('name', 'asName', 'ISO 19115', 'Name of the application schema', 1, 1, 'MD_ApplicationSchemaInformation', 'CI_Citation', NULL, 'M', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('schemaLanguage', 'asSchLang', 'ISO 19115', 'Identification of the schema language', 1, 1, 'MD_ApplicationSchemaInformation', 'CharacterString', NULL, 'M', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('constraintLanguage', 'asCstLang', 'ISO 19115', 'Formal language in application schema', 1, 1, 'MD_ApplicationSchemaInformation', 'CharacterString', NULL, 'M', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('schemaAscii', 'asAscii', 'ISO 19115', 'Application schema (ASCII file)', 0, 1, 'MD_ApplicationSchemaInformation', 'CharacterString', NULL, 'O', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('graphicsFile', 'asGraFile', 'ISO 19115', 'Application schema (graphic file)', 0, 1, 'MD_ApplicationSchemaInformation', 'Binary', NULL, 'O', 5, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('softwareDevelopmentFile', 'asSwDevFile', 'ISO 19115', 'Application schema (software development file)', 0, 1, 'MD_ApplicationSchemaInformation', 'Binary', NULL, 'O', 6, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('softwareDevelopmentFileFormat', 'asSwDevFiFt', 'ISO 19115', 'Software development file format', 0, 1, 'MD_ApplicationSchemaInformation', 'CharacterString', NULL, 'O', 7, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('aName', 'aName', 'ISO 19103', 'Name of attribute', 1, 1, 'MemberName', 'CharacterString', NULL, 'M', 1, 'ISO 19103', 'ISO 19103', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('aName', 'aName', 'ISO 19103', 'Name of type', 1, 1, 'TypeName', 'CharacterString', NULL, 'M', 1, 'ISO 19103', 'ISO 19103', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('attributeType', 'attributeType', 'ISO 19103', 'Attribute type', 0, 1, 'MemberName', 'TypeName', NULL, 'O', 2, 'ISO 19103', 'ISO 19103', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('sequenceIdentifier', 'seqID', 'ISO 19115', 'Sequence identifier', 0, 1, 'MD_RangeDimension', 'MemberName', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('descriptor', 'dimDescrp', 'ISO 19115', 'Descriptor', 0, 1, 'MD_RangeDimension', 'CharacterString', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('maxValue', 'maxVal', 'ISO 19115', 'Maximum value', 0, 1, 'MD_Band', 'Real', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('minValue', 'minVal', 'ISO 19115', 'Minimum value', 0, 1, 'MD_Band', 'Real', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('units', 'valUnit', 'ISO 19115', 'Units', 0, 1, 'MD_Band', NULL, 'MW_LengthUnitCode', 'O', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('peakResponse', 'pkResp', 'ISO 19115', 'Peak response', 0, 1, 'MD_Band', 'Real', NULL, 'O', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('bitsPerValue', 'bitsPerVal', 'ISO 19115', 'Bits per value', 0, 1, 'MD_Band', 'Integer', NULL, 'O', 5, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('toneGradation', 'toneGrad', 'ISO 19115', 'Tone gradation', 0, 1, 'MD_Band', 'Integer', NULL, 'O', 6, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('scaleFactor', 'sclFac', 'ISO 19115', 'Scale factor', 0, 1, 'MD_Band', 'Real', NULL, 'O', 7, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('offset', 'offset', 'ISO 19115', 'Offset', 0, 1, 'MD_Band', 'Real', NULL, 'O', 8, 'ISO 19103', 'ISO 19115', ' ');

--------------------------------------------------------------------------------------------
------------------- correction adding missing properties FileDescription
INSERT INTO "Schemas"."Properties"  VALUES ('fileName', 'bgFileName', 'ISO 19115', 'Quicklook name', 1, 1, 'MD_BrowseGraphic', 'CharacterString', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('fileDescription', NULL, 'ISO 19115', NULL, 0, 1, 'MD_BrowseGraphic', 'CharacterString', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('fileType', 'bgFileType', 'ISO 19115', 'File type', 0, 1, 'MD_BrowseGraphic', 'CharacterString', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');


INSERT INTO "Schemas"."Properties"  VALUES ('useLimitation', 'useLimit', 'ISO 19115', 'Limitation use of metadata', 0, 2147483647, 'MD_Constraints', 'CharacterString', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('citation', 'CstCit', 'ISO 19115 FRA 1.0', 'Citation of documents or information source', 0, 2147483647, 'FRA_Constraints', 'CI_Citation', NULL, 'O', 2, 'ISO 19115', 'ISO 19115 FRA 1.0', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('attributeDescription', 'attDesc', 'ISO 19115', 'Attribute description', 1, 1, 'MD_CoverageDescription', 'RecordType', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('contentType', 'contentTyp', 'ISO 19115', 'Content type', 1, 1, 'MD_CoverageDescription', NULL, 'MD_CoverageContentTypeCode', 'M', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('dimension', 'covDim', 'ISO 19115', 'Dimension of the cell', 0, 2147483647, 'MD_CoverageDescription', 'MD_RangeDimension', NULL, 'O', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('referenceSystemIdentifier', 'refSysId', 'ISO 19115', 'Name of reference system', 0, 1, 'MD_CRS', 'RS_Identifier', NULL, 'C', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('projection', 'projection', 'ISO 19115', 'Projection', 0, 1, 'MD_CRS', 'RS_Identifier', NULL, 'O', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('ellipsoid', 'ellipsoid', 'ISO 19115', 'Ellipsoid', 0, 1, 'MD_CRS', 'RS_Identifier', NULL, 'O', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('datum', 'datum', 'ISO 19115', 'Datum', 0, 1, 'MD_CRS', 'RS_Identifier', NULL, 'O', 4, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('semiMajorAxis', 'semiMajAx', 'ISO 19115', 'Radius of the equatorial axis of the ellipsoid', 1, 1, 'MD_EllipsoidParameters', 'Real', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('axisUnits', 'axisUnits', 'ISO 19115', 'Units of the semi-major axis', 1, 1, 'MD_EllipsoidParameters', NULL, 'MW_LengthUnitCode', 'M', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('denominatorOfFlatetteningRatio', 'denFlatRat', 'ISO 19115', 'Denominator of Flattening ratio', 0, 1, 'MD_EllipsoidParameters', 'Real', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('ellipsoidParameters', 'ellParas', 'ISO 19115', 'Ellipsoid Parameters', 0, 1, 'MD_CRS', 'MD_EllipsoidParameters', NULL, 'O', 5, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('zone', 'zone', 'ISO 19115', 'Projection zone', 0, 1, 'MD_ProjectionParameters', 'Integer', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('standardParallel', 'stanParal', 'ISO 19115', 'Standard parallel', 0, 2, 'MD_ProjectionParameters', 'Real', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('longitudeOfCentralMeridian', 'longCntMer', 'ISO 19115', 'Longitude of Central meridian', 0, 1, 'MD_ProjectionParameters', 'Real', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('latitudeOfProjectionOrigin', 'latProjOri', 'ISO 19115', 'Latitude of projection origin', 0, 1, 'MD_ProjectionParameters', 'Real', NULL, 'O', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('0Easting', 'falEasting', 'ISO 19115', '0 Easting', 0, 1, 'MD_ProjectionParameters', 'Real', NULL, 'O', 5, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('0Northing', 'falNorthing', 'ISO 19115', '0 Northing', 0, 1, 'MD_ProjectionParameters', 'Real', NULL, 'O', 6, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('0EastingNorthingUnits', 'falENUnits', 'ISO 19115', '0 Easting and Northing units', 0, 1, 'MD_ProjectionParameters', NULL, 'MW_LengthUnitCode', 'O', 7, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('scaleFactorAtEquator', 'sclFacEqu', 'ISO 19115', 'Scale factor at Equator', 0, 1, 'MD_ProjectionParameters', 'Real', NULL, 'O', 8, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('heightOfProspectivePointAboveSurface', 'hgtProsPt', 'ISO 19115', 'Height of prospective point above surface', 0, 1, 'MD_ProjectionParameters', 'Real', NULL, 'O', 9, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('longitudeOfProjectionCenter', 'longProjCnt', 'ISO 19115', 'Longitude of projection center', 0, 1, 'MD_ProjectionParameters', 'Real', NULL, 'O', 10, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('latitudeOfProjectionCenter', 'latProjCnt', 'ISO 19115', 'Latitude of projection center', 0, 1, 'MD_ProjectionParameters', 'Real', NULL, 'O', 11, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('scaleFactorAtCenterLine', 'sclFacCnt', 'ISO 19115', 'Scale factor at center line', 0, 1, 'MD_ProjectionParameters', 'Real', NULL, 'O', 12, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('straightVerticalLongitudeFromPole', 'stVrLongPI', 'ISO 19115', 'Straight vertical longitude from pole', 0, 1, 'MD_ProjectionParameters', 'Real', NULL, 'O', 13, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('scaleFactorAtProjectionOrigin', 'sclFacPrOr', 'ISO 19115', 'Scale factor at projection origin', 0, 1, 'MD_ProjectionParameters', 'Real', NULL, 'O', 14, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('projectionParameters', 'projParas', 'ISO 19115', 'Projection parameters', 0, 1, 'MD_CRS', 'MD_ProjectionParameters', NULL, 'O', 6, 'ISO 19115', 'ISO 19115', ' ');

-------------------------------------------------------------------------------------------------------------
------------- correction this properties belong to MD_Identification not MD_DataIdentification --------------
-------------------------------------------------------------------------------------------------------------
INSERT INTO "Schemas"."Properties"  VALUES ('citation', 'idCitation', 'ISO 19115', 'Citation of documents or information source', 1, 1, 'MD_Identification', 'CI_Citation', NULL, 'M', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('abstract', 'idAbs', 'ISO 19115', 'Summary', 1, 1, 'MD_Identification', 'CharacterString', NULL, 'M', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('purpose', 'idPurp', 'ISO 19115', 'Purpose of the resource(s)', 0, 1, 'MD_Identification', 'CharacterString', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('credit', 'idCredit', 'ISO 19115', 'Author(s)', 0, 2147483647, 'MD_Identification', 'CharacterString', NULL, 'O', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('status', 'idStatus', 'ISO 19115', 'Status of dataset', 0, 2147483647, 'MD_Identification', NULL, 'MD_ProgressCode', 'O', 5, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('pointOfContact', 'idPoC', 'ISO 19115', 'Point of contact', 0, 2147483647, 'MD_Identification', 'CI_ResponsibleParty', NULL, 'O', 6, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('resourceMaintenance', 'resMaint', 'ISO 19115', 'Informations about the maintenance of dataset', 0, 2147483647, 'MD_Identification', 'MD_MaintenanceInformation', NULL, 'O', 7, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('graphicOverview', 'graphOver', 'ISO 19115', 'Quicklook', 0, 2147483647, 'MD_Identification', 'MD_BrowseGraphic', NULL, 'O', 8, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('resourceFormat', 'dsFormat', 'ISO 19115', 'Informations about the dataset format', 0, 2147483647, 'MD_Identification', 'MD_Format', NULL, 'O', 9, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('descriptiveKeywords', 'descKeys', 'ISO 19115', 'Keywords', 0, 2147483647, 'MD_Identification', 'MD_Keywords', NULL, 'O', 10, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('resourceSpecificUsage', 'idSpecUse', 'ISO 19115', 'Informations about the specific usage of the resource(s)', 0, 2147483647, 'MD_Identification', 'MD_Usage', NULL, 'O', 11, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('resourceConstraints', 'resConst', 'ISO 19115', 'Informations about the dataset constraints', 0, 2147483647, 'MD_Identification', 'MD_Constraints', NULL, 'O', 12, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('aggregationInfo', 'aggrInfo', 'ISO 19115', 'Informations about aggregate dataset', 0, 2147483647, 'MD_Identification', 'MD_AggregateInformation', NULL, 'O', 13, 'ISO 19115', 'ISO 19115', ' ');
-------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------

INSERT INTO "Schemas"."Properties"  VALUES ('maintenanceAndUpdateFrequency', 'maintFreq', 'ISO 19115', 'Maintenance and update frequency of metadata', 1, 1, 'MD_MaintenanceInformation', NULL, 'MD_MaintenanceFrequencyCode', 'M', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('dateOfNextUpdate', 'dateNext', 'ISO 19115', 'Date of the next update of metadata', 0, 1, 'MD_MaintenanceInformation', 'Date', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('userDefinedMaintenanceFrequency', 'usrDefFreq', 'ISO 19115', 'Frequency of metadata update by user', 0, 1, 'MD_MaintenanceInformation', 'TM_PeriodDuration', NULL, 'O', 3, 'MDWEB', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('updateScope', 'maintScp', 'ISO 19115', 'Maintenance scope', 0, 2147483647, 'MD_MaintenanceInformation', NULL, 'MD_ScopeCode', 'O', 4, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('updateScopeDescription', 'upScpDesc', 'ISO 19115', 'Additionnal information about of the maintenance', 0, 2147483647, 'MD_MaintenanceInformation', 'MD_ScopeDescription', NULL, 'O', 5, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('maintenanceNote', 'maintNote', 'ISO 19115', 'Maintenance note', 0, 2147483647, 'MD_MaintenanceInformation', 'CharacterString', NULL, 'O', 6, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('contact', 'maintCont', 'ISO 19115', 'Informations about maintenance contact', 0, 2147483647, 'MD_MaintenanceInformation', 'CI_ResponsibleParty', NULL, 'O', 7, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('name', 'formatName', 'ISO 19115', 'Dataset format', 1, 1, 'MD_Format', 'CharacterString', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('version', 'formatVer', 'ISO 19115', 'Version of the format', 1, 1, 'MD_Format', 'CharacterString', NULL, 'M', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('amendmentNumber', 'formatAmdNum', 'ISO 19115', 'Amendment number of the format version', 0, 1, 'MD_Format', 'CharacterString', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('specification', 'formatSpec', 'ISO 19115', 'Specification of format', 0, 1, 'MD_Format', 'CharacterString', NULL, 'O', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('fileDecompressionTechnique', 'fileDecmTech', 'ISO 19115', 'File decompression technique', 0, 1, 'MD_Format', 'CharacterString', NULL, 'O', 5, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('keyword', 'keyword', 'ISO 19115', 'Keyword', 1, 2147483647, 'MD_Keywords', 'CharacterString', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('Type', 'keyTyp', 'ISO 19115', 'Type of keyword', 0, 1, 'MD_Keywords', NULL, 'MD_KeywordTypeCode', 'O', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('ThesaurusName', 'thesaName', 'ISO 19115', 'Thesaurus name', 0, 1, 'MD_Keywords', 'CI_Citation', NULL, 'O', 4, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('specificUsage', 'specUsage', 'ISO 19115', 'Description of resource usage', 1, 1, 'MD_Usage', 'CharacterString', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('usageDateTime', 'usageDate', 'ISO 19115', 'Date of the first usage of the resource', 0, 1, 'MD_Usage', 'DateTime', NULL, 'O', 2, 'ISO 19108', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('userDeterminedLimitations', 'usrDetLim', 'ISO 19115', 'Limitations of the resource use', 0, 1, 'MD_Usage', 'CharacterString', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('userContactInfo', 'usrCntInfo', 'ISO 19115', 'Identification of organisation using the resource', 0, 2147483647, 'MD_Usage', 'CI_ResponsibleParty', NULL, 'O', 4, 'ISO 19115', 'ISO 19115', ' ');

-----------------------------------------------------------------
--- adding missing codelist MD_TopicCategoryCode ----------------
-----------------------------------------------------------------

INSERT INTO "Schemas"."CodeLists"  VALUES ('MD_TopicCategoryCode', 'TopicCatCd', 'ISO 19115', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeListElements"  VALUES ('farming', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('biota', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('boundaries', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('climatologyMeteorologyAtmosphere', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('economy', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 5);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('elevation', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 6);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('environment', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('geoscientificInformation', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 8);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('health', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 9);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('imageryBaseMapsEarthCover', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 10);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('intelligenceMilitary', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 11);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('inlandWaters', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 12);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('location', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 13);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('oceans', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 14);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('planningCadastre', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 15);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('society', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 16);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('structure', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 17);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('transportation', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 18);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('utilitiesCommunication', NULL, 'ISO 19115', NULL, 0, 1, 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'MD_TopicCategoryCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 19);


--------------------------------------------------------------
---------------- MD_DataIdentification properties ------------
--------------------------------------------------------------
INSERT INTO "Schemas"."Properties"  VALUES ('spatialRepresentationType', 'spatRpType', 'ISO 19115', 'Spatial representation type of the resource(s)', 0, 2147483647, 'MD_DataIdentification', NULL, 'MD_SpatialRepresentationTypeCode', 'O', 14, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('spatialResolution', 'dataScale', 'ISO 19115', 'Spatial resolution of the dataset', 0, 2147483647, 'MD_DataIdentification', 'MD_Resolution', NULL, 'O', 15, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('language', 'dataLang', 'ISO 19115', 'Dataset language', 1, 2147483647, 'MD_DataIdentification', NULL, 'LanguageCode', 'M', 16, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('characterSet', 'dataChar', 'ISO 19115', 'Character set used for the dataset', 0, 1, 'MD_DataIdentification', NULL, 'MD_CharacterSetCode', 'C', 17, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('topicCategory', 'tpCat', 'ISO 19115', 'Topic category', 1, 2147483647, 'MD_DataIdentification', NULL, 'MD_TopicCategoryCode', 'M', 18, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('environmentDescription', 'envirDesc', 'ISO 19115', 'Information about the producing environment of the resource(s)', 0, 1, 'MD_DataIdentification', 'CharacterString', NULL, 'O', 19, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('extent', 'dataExt', 'ISO 19115', 'Informations about the spatial and temporal extent of the datatset', 0, 2147483647, 'MD_DataIdentification', 'EX_Extent', NULL, 'O', 20, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('supplementalInformation', 'suppInfo', 'ISO 19115', 'Other descriptive informations about the resource(s)', 0, 1, 'MD_DataIdentification', 'CharacterString', NULL, 'O', 21, 'ISO 19103', 'ISO 19115', ' ');
--------------------------------------------------------------
--------------------------------------------------------------
--------------------------------------------------------------
INSERT INTO "Schemas"."Properties"  VALUES ('relatedCitation', 'relatedCitation', 'ISO 19115 FRA 1.0', 'Citation of documents or information source', 0, 2147483647, 'FRA_DataIdentification', 'CI_Citation', NULL, 'O', 22, 'ISO 19115', 'ISO 19115 FRA 1.0', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('equivalentScale', 'equScale', 'ISO 19115', 'Equivalent scale', 0, 1, 'MD_Resolution', 'MD_RepresentativeFraction', NULL, 'C', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('distance', 'scaleDist', 'ISO 19115', 'Pixel size', 0, 1, 'MD_Resolution', 'Distance', NULL, 'C', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('unitsOfDistribution', 'unitsODist', 'ISO 19115', 'Units of distribution of the resource', 0, 1, 'MD_DigitalTransferOptions', 'CharacterString', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('transferSize', 'transSize', 'ISO 19115', 'Transfer size of the dataset (Mb)', 0, 1, 'MD_DigitalTransferOptions', 'Real', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('onLine', 'onLineSrc', 'ISO 19115', 'Informations about the online sources', 0, 2147483647, 'MD_DigitalTransferOptions', 'CI_OnlineResource', NULL, 'O', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('name', 'medName', 'ISO 19115', 'Name of medium', 0, 1, 'MD_Medium', NULL, 'MD_MediumNameCode', 'O', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('density', 'medDensity', 'ISO 19115', 'Density of the media', 0, 2147483647, 'MD_Medium', 'Real', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('densityUnits', 'medDensityUnits', 'ISO 19115', 'Density unit of resource record', 0, 1, 'MD_Medium', 'CharacterString', NULL, 'C', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('volumes', 'medVol', 'ISO 19115', 'Numbers of items', 0, 1, 'MD_Medium', 'Integer', NULL, 'O', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('mediumFormat', 'medFormat', 'ISO 19115', 'Media format', 0, 2147483647, 'MD_Medium', NULL, 'MD_MediumFormatCode', 'O', 5, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('offLine', 'offLineMed', 'ISO 19115', 'Informations about the  offline media', 0, 1, 'MD_DigitalTransferOptions', 'MD_Medium', NULL, 'O', 4, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('dimensionName', 'dimName', 'ISO 19115', 'Name of axis', 1, 1, 'MD_Dimension', NULL, 'MD_DimensionNameTypeCode', 'M', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('dimensionSize', 'dimSize', 'ISO 19115', 'Number of element along axis', 1, 1, 'MD_Dimension', 'Integer', NULL, 'M', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('resolution', 'dimResol', 'ISO 19115', 'Pixel size', 0, 1, 'MD_Dimension', 'Measure', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('distributionFormat', 'distFormat', 'ISO 19115', 'Informations about distribution format', 0, 2147483647, 'MD_Distribution', 'MD_Format', NULL, 'C', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('distributorContact', 'distorCont', 'ISO 19115', 'Informations tabout  the distributor of the dataset', 1, 1, 'MD_Distributor', 'CI_ResponsibleParty', NULL, 'M', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('fees', 'resfees', 'ISO 19115', 'Fees for the order process', 0, 1, 'MD_StandardOrderProcess', 'CharacterString', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('plannedAvailableDateTime', 'planAvDtTm', 'ISO 19115', 'Availability of the resource (date and time)', 0, 1, 'MD_StandardOrderProcess', 'DateTime', NULL, 'O', 2, 'ISO 19108', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('orderingInstructions', 'ordInstr', 'ISO 19115', 'Generals instructions to provide the resource', 0, 1, 'MD_StandardOrderProcess', 'CharacterString', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('turnaround', 'ordTurn', 'ISO 19115', 'Turnaround time to fill an order', 0, 1, 'MD_StandardOrderProcess', 'CharacterString', NULL, 'O', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('distributionOrderProcess', 'distorOrdPrc', 'ISO 19115', 'Distribution order process', 0, 2147483647, 'MD_Distributor', 'MD_StandardOrderProcess', NULL, 'O', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('distributorFormat', 'distorFormat', 'ISO 19115', 'Distributor format', 0, 2147483647, 'MD_Distributor', 'MD_Format', NULL, 'C', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('distributorTransferOptions', 'distorTran', 'ISO 19115', 'Informations about the distributor transfer options', 0, 2147483647, 'MD_Distributor', 'MD_DigitalTransferOptions', NULL, 'O', 4, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('distributor', 'distributor', 'ISO 19115', 'Informations about the distributor of the dataset', 0, 2147483647, 'MD_Distribution', 'MD_Distributor', NULL, 'O', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('transferOptions', 'distTranOps', 'ISO 19115', 'Transfer options', 0, 2147483647, 'MD_Distribution', 'MD_DigitalTransferOptions', NULL, 'O', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('name', 'extEleName', 'ISO 19115', 'Element name', 1, 1, 'MD_ExtendedElementInformation', 'CharacterString', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('shortName', 'extEleShortName', 'ISO 19115', 'Element short name', 0, 1, 'MD_ExtendedElementInformation', 'CharacterString', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('domainCode', 'extDomCode', 'ISO 19115', 'Domain code of element', 0, 1, 'MD_ExtendedElementInformation', 'Integer', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('definition', 'extEleDef', 'ISO 19115', 'Definition of element', 1, 1, 'MD_ExtendedElementInformation', 'CharacterString', NULL, 'M', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('obligation', 'extEleOb', 'ISO 19115', 'Obligation of element', 0, 1, 'MD_ExtendedElementInformation', 'CharacterString', NULL, 'O', 5, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('condition', 'extEleCond', 'ISO 19115', 'Condition of obligation', 0, 1, 'MD_ExtendedElementInformation', 'CharacterString', NULL, 'O', 6, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('dataType', 'eleDataType', 'ISO 19115', 'Data type code', 1, 1, 'MD_ExtendedElementInformation', NULL, 'MD_DatatypeCode', 'M', 7, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('maximumOccurrence', 'extEleMxOc', 'ISO 19115', 'Maximum occurrence of element', 0, 1, 'MD_ExtendedElementInformation', 'Integer', NULL, 'O', 8, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('domainValue', 'extEleDomVal', 'ISO 19115', 'Domain of  value of element', 0, 1, 'MD_ExtendedElementInformation', 'CharacterString', NULL, 'O', 9, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('parentEntity', 'extEleParEnt', 'ISO 19115', 'Parent entity of element', 1, 2147483647, 'MD_ExtendedElementInformation', 'CharacterString', NULL, 'M', 10, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('rule', 'extEleRule', 'ISO 19115', 'Rule of use element with others', 1, 1, 'MD_ExtendedElementInformation', 'CharacterString', NULL, 'M', 11, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('rationale', 'extEleRat', 'ISO 19115', 'Reason of creating the element', 0, 2147483647, 'MD_ExtendedElementInformation', 'CharacterString', NULL, 'O', 12, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('source', 'extEleSrc', 'ISO 19115', 'Informations of responsible party associated to creation of extended element', 1, 2147483647, 'MD_ExtendedElementInformation', 'CI_ResponsibleParty', NULL, 'M', 13, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('complianceCode', 'compCode', 'ISO 19115', 'Compliance code with ISO 19110', 0, 1, 'MD_FeatureCatalogueDescription', 'Boolean', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('language', 'catLang', 'ISO 19115', 'Language', 0, 2147483647, 'MD_FeatureCatalogueDescription', NULL, 'LanguageCode', 'O', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('includedWithDataset', 'incWithDS', 'ISO 19115', 'Included with dataset', 1, 1, 'MD_FeatureCatalogueDescription', 'Boolean', NULL, 'M', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('featureTypes', 'catFetTyps', 'ISO 19115', 'Subset of feature types', 0, 2147483647, 'MD_FeatureCatalogueDescription', 'LocalName', NULL, 'O', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('featureCatalogueCitation', 'catCitation', 'ISO 19115', 'Feature catalogue citation', 1, 2147483647, 'MD_FeatureCatalogueDescription', 'CI_Citation', NULL, 'M', 5, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('geometricObjectType', 'geoObjTyp', 'ISO 19115', 'Vector object type', 1, 1, 'MD_GeometricObjects', NULL, 'MD_GeometricObjectTypeCode', 'M', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('geometricObjectCount', 'geoObjCnt', 'ISO 19115', 'Geomtric object count', 0, 1, 'MD_GeometricObjects', 'Integer', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('numberOfDimensions', 'numDims', 'ISO 19115', 'Number of dimension (axis)', 1, 1, 'MD_GridSpatialRepresentation', 'Integer', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('axisDimensionsProperties', 'axDimProps', 'ISO 19115', 'Informations about spatial-temporal axis properties', 1, 1, 'MD_GridSpatialRepresentation', 'MD_Dimension', NULL, 'M', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('cellGeometry', 'cellGeo', 'ISO 19115', 'Identification of a grid data', 1, 1, 'MD_GridSpatialRepresentation', NULL, 'MD_CellGeometryCode', 'M', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('transformationParameterAvailability', 'tranParaAv', 'ISO 19115', 'Availability of transformation parameter', 1, 1, 'MD_GridSpatialRepresentation', 'Boolean', NULL, 'M', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('checkPointAvailibility', 'chkPtAv', 'ISO 19115', 'Availability of check point', 1, 1, 'MD_Georectified', 'Boolean', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('checkPointDescription', 'chkPtDesc', 'ISO 19115', 'Check point description', 0, 1, 'MD_Georectified', 'CharacterString', NULL, 'C', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('cornerPoints', 'cornerPts', 'ISO 19115', 'Corner points', 1, 1, 'MD_Georectified', 'rubrique', NULL, 'M', 3, 'MDWEB', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('centerPoint', 'centerPt', 'ISO 19115', 'Center point', 0, 1, 'MD_Georectified', 'float', NULL, 'O', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('pointInPixel', 'ptInPixel', 'ISO 19115', 'Point in a pixel corresponding to the location of the pixel', 1, 1, 'MD_Georectified', 'CharacterString', NULL, 'M', 5, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('transformationDimensionDescription', 'tranDimDesc', 'ISO 19115', 'General description of the transformation', 0, 1, 'MD_Georectified', 'CharacterString', NULL, 'O', 6, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('transformationDimensionMapping', 'tranDimMap', 'ISO 19115', 'Information about the saptial axes', 0, 2, 'MD_Georectified', 'CharacterString', NULL, 'O', 7, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('controlPointAvailability', 'ctrlPtAv', 'ISO 19115', 'Availability of check point', 1, 1, 'MD_Georeferenceable', 'Boolean', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('orientationParameterAvailability', 'orieParaAv', 'ISO 19115', 'Orientation parameter avalaibility', 1, 1, 'MD_Georeferenceable', 'Boolean', NULL, 'M', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('orientationParameterDescription', 'orieParaDs', 'ISO 19115', 'Orientation parameter description', 0, 1, 'MD_Georeferenceable', 'CharacterString', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('georeferencedParameters', 'georefPars', 'ISO 19115', 'Georeferenced parameters', 1, 1, 'MD_Georeferenceable', 'Integer', NULL, 'M', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('parameterCitation', 'paraCit', 'ISO 19115', 'Parameter citation', 0, 2147483647, 'MD_Georeferenceable', 'CI_Citation', NULL, 'O', 5, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('illuminationElevationAngle', 'illElevAng', 'ISO 19115', 'Illumination elevation angle', 0, 1, 'MD_ImageDescription', 'Real', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('illuminationAzimuthAngle', 'illAziAng', 'ISO 19115', 'Illumination azimuth angle', 0, 1, 'MD_ImageDescription', 'Real', NULL, 'O', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('imagingCondition', 'imagCond', 'ISO 19115', 'Imaging condition', 0, 1, 'MD_ImageDescription', NULL, 'MD_ImagingConditionCode', 'O', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES (' imageQualityCode', 'imagQuCode', 'ISO 19115', 'Image quality code', 0, 1, 'MD_ImageDescription', 'MD_Identifier', NULL, 'O', 4, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('cloudCoverPercentage', 'cloudCovPer', 'ISO 19115', 'Cloud cover percentage', 0, 1, 'MD_ImageDescription', 'Real', NULL, 'O', 5, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('processingLevelCode', 'prcTypCode', 'ISO 19115', 'Processing level code', 0, 1, 'MD_ImageDescription', 'MD_Identifier', NULL, 'O', 6, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('compressionGenerationQuantity', 'cmpGenQuan', 'ISO 19115', 'Compression generation quantity', 0, 1, 'MD_ImageDescription', 'Integer', NULL, 'O', 7, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('triangulationIndicator', 'trianInd', 'ISO 19115', 'Triangulation indicator', 0, 1, 'MD_ImageDescription', 'Boolean', NULL, 'O', 8, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('radiometricCalibrationDataAvailability', 'radCalDatAv', 'ISO 19115', 'Radiometric calibration data availability', 0, 1, 'MD_ImageDescription', 'Boolean', NULL, 'O', 9, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('cameraCalibrationInformationAvailability', 'camCalInAv', 'ISO 19115', 'Camera calibration information availability', 0, 1, 'MD_ImageDescription', 'Boolean', NULL, 'O', 10, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('filmDistortionInformationAvailability', 'filmDistInAv', 'ISO 19115', 'Film distortion information availability', 0, 1, 'MD_ImageDescription', 'Boolean', NULL, 'O', 11, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('lensDistortionInformationAvailability', 'lensDistInAv', 'ISO 19115', 'Lens distortion information availability', 0, 1, 'MD_ImageDescription', 'Boolean', NULL, 'O', 12, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('fileIdentifier', 'mdFileID', 'ISO 19115', 'File identifier', 0, 1, 'MD_Metadata', 'CharacterString', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('language', 'mdLang', 'ISO 19115', 'Metadata file language', 0, 1, 'MD_Metadata', NULL, 'LanguageCode', 'C', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('characterSet', 'mdChar', 'ISO 19115', 'Metadata character set', 0, 1, 'MD_Metadata', NULL, 'MD_CharacterSetCode', 'C', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('parentIdentifier', 'mdParentID', 'ISO 19115', 'Metadata file parent identifier', 0, 1, 'MD_Metadata', 'CharacterString', NULL, 'O', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('hierarchyLevel', 'mdHrLv', 'ISO 19115', 'Hierarchy level of resource documented', 0, 1, 'MD_Metadata', NULL, 'MD_ScopeCode', 'C', 5, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('hierarchyLevelName', 'mdHrLvName', 'ISO 19115', 'Hierarchy level name', 0, 1, 'MD_Metadata', 'CharacterString', NULL, 'O', 6, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('contact', 'mdContact', 'ISO 19115', 'Informations about responsible party', 1, 1, 'MD_Metadata', 'CI_ResponsibleParty', NULL, 'M', 7, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('dateStamp', 'mdDateSt', 'ISO 19115', 'Date stamp', 1, 1, 'MD_Metadata', 'Date', NULL, 'M', 8, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('metadataStandardName', 'mdStanName', 'ISO 19115', 'Metadata standard name', 0, 1, 'MD_Metadata', 'CharacterString', NULL, 'O', 9, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('metadataStandardVersion', 'mdStanVer', 'ISO 19115', 'Version of metadata standard', 0, 1, 'MD_Metadata', 'CharacterString', NULL, 'O', 10, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('dataSetURI', 'dataSetURI', 'ISO 19115', 'Identifier address (URI) of referenced dataset', 0, 1, 'MD_Metadata', 'CharacterString', NULL, 'O', 11, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('languageCode', 'language', 'ISO 19115', 'Language of metadata', 1, 1, 'PT_Locale', NULL, 'LanguageCode', 'M', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('country', 'country', 'ISO 19115', 'Country', 0, 1, 'PT_Locale', NULL, 'CountryCode', 'O', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('characterEncoding', 'characterEncoding', 'ISO 19115', 'Character set of metadata', 1, 1, 'PT_Locale', NULL, 'MD_CharacterSetCode', 'M', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('locale', 'locale', 'ISO 19115', 'Locale', 0, 2147483647, 'MD_Metadata', 'PT_Locale', NULL, 'O', 12, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('spatialRepresentationInfo', 'spatRepInfo', 'ISO 19115', 'Dataset Spatial representation', 0, 2147483647, 'MD_Metadata', 'MD_SpatialRepresentation', NULL, 'O', 13, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('referenceSystemInfo', 'refSysInfo', 'ISO 19115', 'Spatial reference system', 0, 2147483647, 'MD_Metadata', 'MD_ReferenceSystem', NULL, 'O', 14, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('extensionOnLineResource', 'extOnRes', 'ISO 19115', 'On line resource of metadata extension', 0, 1, 'MD_MetadataExtensionInformation', 'CI_OnlineResource', NULL, 'O', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('extendedElementInformation', 'extEleInfo', 'ISO 19115', 'Extended element Informations', 0, 2147483647, 'MD_MetadataExtensionInformation', 'MD_ExtendedElementInformation', NULL, 'O', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('metadataExtensionInfo', 'mdExtInfo', 'ISO 19115', 'Informations about metadata extension', 0, 2147483647, 'MD_Metadata', 'MD_MetadataExtensionInformation', NULL, 'O', 15, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('identificationInfo', 'dataIdInfo', 'ISO 19115', 'Dataset identification', 1, 2147483647, 'MD_Metadata', 'MD_Identification', NULL, 'M', 16, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('contentInfo', 'contInfo', 'ISO 19115', 'Informations about resource content', 0, 2147483647, 'MD_Metadata', 'MD_ContentInformation', NULL, 'O', 17, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('distributionInfo', 'distInfo', 'ISO 19115', 'Dataset distribution', 0, 1, 'MD_Metadata', 'MD_Distribution', NULL, 'O', 18, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('dataQualityInfo', 'dqInfo', 'ISO 19115', 'Dataset quality', 0, 2147483647, 'MD_Metadata', 'DQ_DataQuality', NULL, 'O', 19, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('portrayalCatalogueCitation', 'portCatCit', 'ISO 19115', 'Portrayal catalogue citation', 1, 2147483647, 'MD_PortrayalCatalogueReference', 'CI_Citation', NULL, 'M', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('portrayalCatalogueInfo', 'porCatInfo', 'ISO 19115', 'Informations about the portrayal catalogue', 0, 2147483647, 'MD_Metadata', 'MD_PortrayalCatalogueReference', NULL, 'O', 20, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('applicationSchemaInfo', 'appSchInfo', 'ISO 19115', 'Nformation about the conceptual schema of the dataset', 0, 2147483647, 'MD_Metadata', 'MD_ApplicationSchemaInformation', NULL, 'O', 21, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('metadataMaintenance', 'mdMaint', 'ISO 19115', 'Informations about the maintenance of metadata', 0, 1, 'MD_Metadata', 'MD_MaintenanceInformation', NULL, 'O', 22, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('metadataConstraints', 'mdConst', 'ISO 19115', 'Metadata constraints', 0, 2147483647, 'MD_Metadata', 'MD_Constraints', NULL, 'O', 23, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('azimuthAngle', 'aziAngle', 'ISO 19115', 'Azimuth angle', 1, 1, 'MD_ObliqueLineAzimuth', 'Real', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('azimuthMeasurePointLongitude', 'aziPtLong', 'ISO 19115', 'Azimuth measure point longitude', 1, 1, 'MD_ObliqueLineAzimuth', 'Real', NULL, 'M', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('obliqueLineLatitude', 'obLineLat', 'ISO 19115', 'Oblique line latitude', 1, 1, 'MD_ObliqueLinePoint', 'Real', NULL, 'M', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('obliqueLineLongitude', 'obLineLong', 'ISO 19115', 'Oblique line longitude', 1, 1, 'MD_ObliqueLinePoint', 'Real', NULL, 'M', 2, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('useLimitation', 'useLimit', 'ISO 19115', 'Limitation use of metadata', 0, 1, 'MD_SecurityConstraints', 'CharacterString', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('classification', 'class', 'ISO 19115', 'Handling restrictions of the metadata', 1, 1, 'MD_SecurityConstraints', NULL, 'MD_ClassificationCode', 'M', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('userNote', 'userNote', 'ISO 19115', 'Explanation of the application of the constraints', 0, 1, 'MD_SecurityConstraints', 'CharacterString', NULL, 'O', 3, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('classificationSystem', 'classSys', 'ISO 19115', 'Name of the classification of restrictions', 0, 1, 'MD_SecurityConstraints', 'CharacterString', NULL, 'O', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('handlingDescription', 'handDesc', 'ISO 19115', 'Handling description', 0, 1, 'MD_SecurityConstraints', 'CharacterString', NULL, 'O', 5, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('citation', 'SecCit', 'ISO 19115 FRA 1.0', 'Citation of documents or information source', 0, 2147483647, 'FRA_SecurityConstraints', 'CI_Citation', NULL, 'O', 6, 'ISO 19115', 'ISO 19115 FRA 1.0', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('topologyLevel', 'topLvl', 'ISO 19115', 'Topology level of dataset', 0, 1, 'MD_VectorSpatialRepresentation', NULL, 'MD_TopologyLevelCode', 'O', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('geometricObjects', 'geometObjs', 'ISO 19115', 'Informations about the geometric objects', 0, 2147483647, 'MD_VectorSpatialRepresentation', 'MD_GeometricObjects', NULL, 'O', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('useLimitation', 'useLimit', 'ISO 19115', 'Limitation use of dataset', 0, 1, 'MD_LegalConstraints', 'CharacterString', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('accessConstraints', 'accessConsts', 'ISO 19115', 'Access constraints of the dataset', 0, 2147483647, 'MD_LegalConstraints', NULL, 'MD_RestrictionCode', 'O', 2, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('useConstraints', 'useConsts', 'ISO 19115', 'Use constraints of the dataset', 0, 2147483647, 'MD_LegalConstraints', NULL, 'MD_RestrictionCode', 'O', 3, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('otherConstraints', 'othConsts', 'ISO 19115', 'Others constraints of the dataset', 0, 2147483647, 'MD_LegalConstraints', 'CharacterString', NULL, 'C', 4, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('citation', 'LegCit', 'ISO 19115 FRA 1.0', 'Citation of documents or information source', 0, 2147483647, 'FRA_LegalConstraints', 'CI_Citation', NULL, 'O', 5, 'ISO 19115', 'ISO 19115 FRA 1.0', ' ');


/*INSERT INTO "Schemas"."Classes"  VALUES ('TimePosition', 'TimePosition', 'ISO 19108', 'TimePosition', 0, NULL, NULL, ' ')
INSERT INTO "Schemas"."Properties"  VALUES ('position', NULL, 'ISO 19108', NULL, 1, 1, 'TimePosition', 'Date', NULL, 'M', 0, 'ISO 19103', 'ISO 19108', ' ')*/

--
-- Properties
--

INSERT INTO "Schemas"."Properties"  VALUES ('specification', 'conSpec', 'ISO 19115', 'citation of product specification or user requirement against which data is being evaluated', 1, 1, 'DQ_ConformanceResult', 'CI_Citation', NULL, 'O', 1, 'ISO 19115', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('explanation', 'conExpl', 'ISO 19115', 'explanation of meaning of conformance for this result', 1, 1, 'DQ_ConformanceResult', 'CharacterString', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');
INSERT INTO "Schemas"."Properties"  VALUES ('pass', 'conPass', 'ISO 19115', 'indication of conformance result where 0 = fail and 1 = pass', 1, 1, 'DQ_ConformanceResult', 'Boolean', NULL, 'O', 1, 'ISO 19103', 'ISO 19115', ' ');

-----------------------------------------------------------------
--- adding missing codelist element for MD_ScopeCode ------------
-----------------------------------------------------------------
INSERT INTO "Schemas"."CodeListElements"  VALUES ('attribute', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 1);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('attributeType', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 2);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('collectionHardware', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 3);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('collectionSession', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 4);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('nonGeographicDataset', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 7);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('dimensionGroup', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 8);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('feature', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 9);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('featureType', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 10);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('propertyType', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 11);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('fieldSession', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 12);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('software', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 13);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('service', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 14);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('model', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 15);
INSERT INTO "Schemas"."CodeListElements"  VALUES ('tile', NULL, 'ISO 19115', NULL, 0, 1, 'MD_ScopeCode', 'MD_ScopeCode', 'MD_ScopeCode', 'C', 0, 'ISO 19115', 'ISO 19115', ' ', 16);

