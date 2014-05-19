/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;


/**
 * A container for list of queryable elements in different schemas used in CSW.
 *
 * @author Guilhem Legal
 */
public final class CSWQueryable {

     public static final String INSPIRE  = "http://www.inspire.org";
     public static final String INSPIRE_PREFIX  = "ins";

     public static final QName DEGREE_QNAME                               = new QName(INSPIRE, "Degree",                          INSPIRE_PREFIX);
     public static final QName ACCESS_CONSTRAINTS_QNAME                   = new QName(INSPIRE, "AccessConstraints",               INSPIRE_PREFIX);
     public static final QName OTHER_CONSTRAINTS_QNAME                    = new QName(INSPIRE, "OtherConstraints",                INSPIRE_PREFIX);
     public static final QName INS_CLASSIFICATION_QNAME                   = new QName(INSPIRE, "Classification",                  INSPIRE_PREFIX);
     public static final QName CONDITION_APPLYING_TO_ACCESS_AND_USE_QNAME = new QName(INSPIRE, "ConditionApplyingToAccessAndUse", INSPIRE_PREFIX);
     public static final QName METADATA_POINT_OF_CONTACT_QNAME            = new QName(INSPIRE, "MetadataPointOfContact",          INSPIRE_PREFIX);
     public static final QName LINEAGE_QNAME                              = new QName(INSPIRE, "Lineage",                         INSPIRE_PREFIX);
     public static final QName SPECIFICATION_TITLE_QNAME                  = new QName(INSPIRE, "SpecificationTitle",              INSPIRE_PREFIX);
     public static final QName SPECIFICATION_DATE_QNAME                   = new QName(INSPIRE, "SpecificationDate",               INSPIRE_PREFIX);
     public static final QName SPECIFICATION_DATETYPE_QNAME               = new QName(INSPIRE, "SpecificationDateType",           INSPIRE_PREFIX);

     private CSWQueryable() {}

     /**
     * The queryable element from ISO 19110 and their path id.
     */
    public static final Map<String, List<String>> ISO_FC_QUERYABLE = new HashMap<>();
    static {
        List<String> paths;

        /*
         * The core queryable of ISO 19115
         */
        paths = new ArrayList<>();
        paths.add("/gfc:FC_FeatureCatalogue/gfc:featureType/gfc:FC_FeatureType/gfc:carrierOfCharacteristics/gfc:FC_FeatureAttribute/gfc:memberName/gco:LocalName");
        ISO_FC_QUERYABLE.put("attributeName", paths);
    }
    
    /**
     * The queryable element from ISO 19115 and their path id.
     */
    public static final Map<String, List<String>> ISO_QUERYABLE = new HashMap<>();
    static {
        List<String> paths;

        /*
         * The core queryable of ISO 19115
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gmx:Anchor");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode");
        ISO_QUERYABLE.put("Subject", paths);

        //MANDATORY
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gmx:Anchor");
        ISO_QUERYABLE.put("Title", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:abstract/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:abstract/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:abstract/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:abstract/gmx:Anchor");
        ISO_QUERYABLE.put("Abstract", paths);

        /*MANDATORY
        paths = new ArrayList<>();
        ISO_QUERYABLE.put("AnyText", paths);*/

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gmx:Anchor");
        ISO_QUERYABLE.put("Format", paths);

        //MANDATORY
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:fileIdentifier/gco:CharacterString");
        ISO_QUERYABLE.put("Identifier", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:dateStamp/gco:DateTime");
        paths.add("/gmd:MD_Metadata/gmd:dateStamp/gco:Date");
        paths.add("/gmi:MI_Metadata/gmd:dateStamp/gco:DateTime");
        paths.add("/gmi:MI_Metadata/gmd:dateStamp/gco:Date");
        ISO_QUERYABLE.put("Modified", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue");
        paths.add("/gmi:MI_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue");
        ISO_QUERYABLE.put("Type", paths);

        /*
         * Bounding box
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal");
        ISO_QUERYABLE.put("WestBoundLongitude",     paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal");
        ISO_QUERYABLE.put("EastBoundLongitude",     paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal");
        ISO_QUERYABLE.put("NorthBoundLatitude",     paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal");
        ISO_QUERYABLE.put("SouthBoundLatitude",     paths);

        /*
         * CRS
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString");
        ISO_QUERYABLE.put("Authority",     paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString");
        ISO_QUERYABLE.put("ID",     paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:version/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:version/gco:CharacterString");
        ISO_QUERYABLE.put("Version",     paths);

        /*
         * Additional queryable Element
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gmx:Anchor");
        ISO_QUERYABLE.put("AlternateTitle",   paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date#gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=revision/gmd:date/gco:Date");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date#gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=revision/gmd:date/gco:DateTime");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date#gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=revision/gmd:date/gco:Date");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date#gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=revision/gmd:date/gco:DateTime");
        ISO_QUERYABLE.put("RevisionDate",  paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date#gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=creation/gmd:date/gco:Date");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date#gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=creation/gmd:date/gco:DateTime");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date#gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=creation/gmd:date/gco:Date");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date#gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=creation/gmd:date/gco:DateTime");
        ISO_QUERYABLE.put("CreationDate",  paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date#gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=publication/gmd:date/gco:Date");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date#gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=publication/gmd:date/gco:DateTime");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date#gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=publication/gmd:date/gco:Date");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date#gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=publication/gmd:date/gco:DateTime");
        ISO_QUERYABLE.put("PublicationDate",  paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
        // TODO remove the following path are not normalized
        paths.add("/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
        paths.add("/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");

        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
        // TODO remove the following path are not normalized
        paths.add("/gmi:MI_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
        ISO_QUERYABLE.put("OrganisationName", paths);

        //TODO If an instance of the class MD_SecurityConstraint exists for a resource, the “HasSecurityConstraints” is “true”, otherwise “false”
        //paths = new ArrayList<>();
        //ISO_QUERYABLE.put("HasSecurityConstraints", paths);

        //TODO MD_FeatureCatalogueDescription
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue");
        paths.add("/gmi:MI_Metadata/gmd:language/gmd:LanguageCode/@codeListValue");
        ISO_QUERYABLE.put("Language", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString");
        ISO_QUERYABLE.put("ResourceIdentifier", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:parentIdentifier/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:parentIdentifier/gco:CharacterString");
        ISO_QUERYABLE.put("ParentIdentifier", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode");
        ISO_QUERYABLE.put("KeywordType", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode");
        ISO_QUERYABLE.put("TopicCategory", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:language/gmd:LanguageCode");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:language/gmd:LanguageCode");
        ISO_QUERYABLE.put("ResourceLanguage", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString");
        ISO_QUERYABLE.put("GeographicDescriptionCode", paths);

        /*
         * spatial resolution
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer");
        ISO_QUERYABLE.put("Denominator", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance");

        ISO_QUERYABLE.put("DistanceValue", paths);

        //TODO not existing path in MDWeb or geotoolkit (Distance is treated as a primitive type)
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance@uom");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance@uom");
        ISO_QUERYABLE.put("DistanceUOM", paths);

        /*
         * Temporal Extent
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition/gmx:Anchor");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimeInstant/gml:position");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimeInstant/gml:position");
        ISO_QUERYABLE.put("TempExtent_begin", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition/gmx:Anchor");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimeInstant/gml:position");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimeInstant/gml:position");
        ISO_QUERYABLE.put("TempExtent_end", paths);

        /*
         *  cloud cover percentage
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:contentInfo/gmd:MD_ImageDescription/gmd:cloudCoverPercentage/gco:Real");
        paths.add("/gmi:MI_Metadata/gmd:contentInfo/gmd:MD_ImageDescription/gmd:cloudCoverPercentage/gco:Real");
        ISO_QUERYABLE.put("CloudCover", paths);

        /*
         *  illuminationElevationAngle
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:contentInfo/gmd:MD_ImageDescription/gmd:illuminationElevationAngle/gco:Real");
        paths.add("/gmi:MI_Metadata/gmd:contentInfo/gmd:MD_ImageDescription/gmd:illuminationElevationAngle/gco:Real");
        ISO_QUERYABLE.put("IlluminationElevation", paths);

        /*
         *  processing level
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:contentInfo/gmd:MD_ImageDescription/gmd:processingLevelCode/gmd:RS_Identifier/gmd:code/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:contentInfo/gmd:MD_ImageDescription/gmd:processingLevelCode/gmd:RS_Identifier/gmd:code/gco:CharacterString");
        ISO_QUERYABLE.put("ProcessingLevel", paths);


        /**
         * ISO 19119 specific queryable
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName");
        ISO_QUERYABLE.put("ServiceType", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:couplingType/srv:SV_CouplingType");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:couplingType/srv:SV_CouplingType");
        ISO_QUERYABLE.put("CouplingType", paths);

        //TODO  the following element are described in Service part of ISO 19139 not yet used.
        paths = new ArrayList<>();
        ISO_QUERYABLE.put("ServiceTypeVersion", paths);
        ISO_QUERYABLE.put("OperatesOn", paths);
        ISO_QUERYABLE.put("OperatesOnIdentifier", paths);
        ISO_QUERYABLE.put("OperatesOnWithOpName", paths);

        /**
         * ISO 19115-2 specific queryable
         */
        paths = new ArrayList<>();
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:instrument/gmi:MI_Instrument/gmi:mountedOn/gmi:MI_Platform/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:operation/gmi:MI_Operation/gmi:platform/gmi:MI_Platform/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:operation/gmi:MI_Operation/gmi:parentOperation/gmi:MI_Operation/gmi:platform/gmi:MI_Platform/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:operation/gmi:MI_Operation/gmi:childOperation/gmi:MI_Operation/gmi:platform/gmi:MI_Platform/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        ISO_QUERYABLE.put("Platform", paths);

        paths = new ArrayList<>();
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:instrument/gmi:MI_Instrument/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform/gmi:instrument/gmi:MI_Instrument/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:operation/gmi:MI_Operation/gmi:platform/gmi:MI_Platform/gmi:instrument/gmi:MI_Instrument/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:operation/gmi:MI_Operation/gmi:parentOperation/gmi:MI_Operation/gmi:platform/gmi:MI_Platform/gmi:instrument/gmi:MI_Instrument/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:operation/gmi:MI_Operation/gmi:childOperation/gmi:MI_Operation/gmi:platform/gmi:MI_Platform/gmi:instrument/gmi:MI_Instrument/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        ISO_QUERYABLE.put("Instrument", paths);

        paths = new ArrayList<>();
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:operation/gmi:MI_Operation/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:operation/gmi:MI_Operation/gmi:childOperation/gmi:MI_Operation/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:operation/gmi:MI_Operation/gmi:parentOperation/gmi:MI_Operation/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        ISO_QUERYABLE.put("Operation", paths);
    }



    /**
     * The queryable element from DublinCore and their path id.
     */
    public static final Map<String, List<String>> DUBLIN_CORE_QUERYABLE = new HashMap<>();
    static {
        List<String> paths;

        /*
         * The core queryable of DublinCore
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gmx:Anchor");
        paths.add("/csw:Record/dc:title");
        paths.add("/eb3:*/eb3:Name/eb3:LocalizedString/@value");
        paths.add("/eb2:*/eb2:Name/eb2:LocalizedString/@value");
        DUBLIN_CORE_QUERYABLE.put("title", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty#gmd:role/gmd:CI_RoleCode/@codeListValue=originator/gmd:organisationName/gmx:Anchor");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty#gmd:role/gmd:CI_RoleCode/@codeListValue=originator/gmd:organisationName/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty#gmd:role/gmd:CI_RoleCode/@codeListValue=originator/gmd:organisationName/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty#gmd:role/gmd:CI_RoleCode/@codeListValue=originator/gmd:organisationName/gco:CharacterString");
        paths.add("/csw:Record/dc:creator");
        DUBLIN_CORE_QUERYABLE.put("creator", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gmx:Anchor");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode");
        paths.add("/csw:Record/dc:subject");
        //TODO @name = “http://purl.org/dc/elements/1.1/subject”
        paths.add("/eb3:*/eb3:slot/eb3:valueList/eb3:Value");
        paths.add("/eb2:*/eb2:slot/eb2:valueList/eb2:Value");
        DUBLIN_CORE_QUERYABLE.put("description", paths);
        DUBLIN_CORE_QUERYABLE.put("subject", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:abstract/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:abstract/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:abstract/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:abstract/gmx:Anchor");
        paths.add("/csw:Record/gmd:abstract");
        paths.add("/eb3:*/eb3:Description/eb3:LocalizedString/@value");
        paths.add("/eb2:*/eb2:Description/eb2:LocalizedString/@value");
        DUBLIN_CORE_QUERYABLE.put("abstract", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty#gmd:role/gmd:CI_RoleCode/@codeListValue=publisher/gmd:organisationName/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty#gmd:role/gmd:CI_RoleCode/@codeListValue=publisher/gmd:organisationName/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty#gmd:role/gmd:CI_RoleCode/@codeListValue=publisher/gmd:organisationName/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty#gmd:role/gmd:CI_RoleCode/@codeListValue=publisher/gmd:organisationName/gmx:Anchor");
        paths.add("/csw:Record/dc:publisher");
        DUBLIN_CORE_QUERYABLE.put("publisher", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty#gmd:role/gmd:CI_RoleCode/@codeListValue=author/gmd:organisationName/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty#gmd:role/gmd:CI_RoleCode/@codeListValue=author/gmd:organisationName/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty#gmd:role/gmd:CI_RoleCode/@codeListValue=author/gmd:organisationName/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty#gmd:role/gmd:CI_RoleCode/@codeListValue=author/gmd:organisationName/gmx:Anchor");
        paths.add("/csw:Record/dc:contributor");
        DUBLIN_CORE_QUERYABLE.put("contributor", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:dateStamp/gco:DateTime");
        paths.add("/gmd:MD_Metadata/gmd:dateStamp/gco:Date");
        paths.add("/gmi:MI_Metadata/gmd:dateStamp/gco:DateTime");
        paths.add("/gmi:MI_Metadata/gmd:dateStamp/gco:Date");
        paths.add("/csw:Record/dc:date");
        DUBLIN_CORE_QUERYABLE.put("date", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode");
        paths.add("/gmi:MI_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode");
        paths.add("/csw:Record/dc:type");
        paths.add("/eb3:*/@objectType");
        paths.add("/eb2:*/@objectType");
        DUBLIN_CORE_QUERYABLE.put("type", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gmx:Anchor");
        paths.add("/csw:Record/dc:format");
        paths.add("/eb3:*/@mimeType");
        paths.add("/eb2:*/@mimeType");
        DUBLIN_CORE_QUERYABLE.put("format", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:fileIdentifier/gco:CharacterString");

        paths.add("/csw:Record/dc:identifier");

        paths.add("/gfc:FC_FeatureCatalogue/@id");

        paths.add("/eb3:*/@id");
        paths.add("/wrs:ExtrinsicObject/@id");

        paths.add("/eb2:*/@id");
        paths.add("/wr:*/@id");
        DUBLIN_CORE_QUERYABLE.put("identifier", paths);

        paths = new ArrayList<>();
        paths.add("/csw:Record/dc:source");
        DUBLIN_CORE_QUERYABLE.put("source", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:language/gmd:LanguageCode");
        paths.add("/gmi:MI_Metadata/gmd:language/gmd:LanguageCode");
        paths.add("/csw:Record/dc:language");
        DUBLIN_CORE_QUERYABLE.put("language", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:aggregationInfo/gmd:MD_AggregateInformation/gmd:aggregateDataSetName/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:aggregationInfo/gmd:MD_AggregateInformation/gmd:aggregateDataSetName/gmd:CI_Citation/gmd:title/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:aggregationInfo/gmd:MD_AggregateInformation/gmd:aggregateDataSetName/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:aggregationInfo/gmd:MD_AggregateInformation/gmd:aggregateDataSetName/gmd:CI_Citation/gmd:title/gmx:Anchor");
        paths.add("/csw:Record/dc:relation");
        DUBLIN_CORE_QUERYABLE.put("relation", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode");
        paths.add("/csw:Record/dc:rights");
        DUBLIN_CORE_QUERYABLE.put("rights", paths);

        /*
         * Bounding box
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal");
        paths.add("/csw:Record/ows:BoundingBox/ows:LowerCorner[0]");
        DUBLIN_CORE_QUERYABLE.put("WestBoundLongitude",     paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal");
        paths.add("/csw:Record/ows:BoundingBox/ows:UpperCorner[0]");
        DUBLIN_CORE_QUERYABLE.put("EastBoundLongitude",     paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal");
        paths.add("/csw:Record/ows:BoundingBox/ows:UpperCorner[1]");
        DUBLIN_CORE_QUERYABLE.put("NorthBoundLatitude",     paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal");
        paths.add("/csw:Record/ows:BoundingBox/ows:LowerCorner[1]");
        DUBLIN_CORE_QUERYABLE.put("SouthBoundLatitude",     paths);

        paths = new ArrayList<>();
        paths.add("/csw:Record/ows:BoundingBox/@crs");
        DUBLIN_CORE_QUERYABLE.put("CRS",     paths);
    }

    /**
     * The queryable element from ebrim and their path id.
     * @deprecated
     */
    @Deprecated
    public static final Map<String, List<String>> EBRIM_QUERYABLE = new HashMap<>();
    static {
        List<String> paths;

        /*
         * The core queryable of DublinCore
         */
        paths = new ArrayList<>();
        paths.add("/eb3:RegistryObject/eb3:Name/eb3:LocalizedString/@value");
        paths.add("/eb3:RegistryPackage/eb3:Name/eb3:LocalizedString/@value");
        EBRIM_QUERYABLE.put("name", paths);

        //TODO verify codelist=originator
        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("creator", paths);

        paths = new ArrayList<>();
        //TODO @name = “http://purl.org/dc/elements/1.1/subject”
        paths.add("/eb3:RegistryObject/eb3:slot/eb3:valueList/eb3:Value");
        paths.add("/eb3:RegistryPackage/eb3:slot/eb3:valueList/eb3:Value");
        EBRIM_QUERYABLE.put("description", paths);
        EBRIM_QUERYABLE.put("subject", paths);

        paths = new ArrayList<>();
        paths.add("/eb3:RegistryObject/eb3:Description/eb3:LocalizedString/@value");
        paths.add("/eb3:RegistryPackage/eb3:Description/eb3:LocalizedString/@value");
        EBRIM_QUERYABLE.put("abstract", paths);

        //TODO verify codelist=publisher
        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("publisher", paths);

        //TODO verify codelist=contributor
        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("contributor", paths);

        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("date", paths);

        paths = new ArrayList<>();
        paths.add("/eb3:RegistryObject/@objectType");
        paths.add("/eb3:RegistryPackage/@objectType");
        EBRIM_QUERYABLE.put("type", paths);

        paths = new ArrayList<>();
        paths.add("/eb3:ExtrinsicObject/@mimeType");
        EBRIM_QUERYABLE.put("format", paths);

        paths = new ArrayList<>();
        paths.add("/eb3:RegistryObject/@id");
        paths.add("/eb3:RegistryPackage/@id");
        EBRIM_QUERYABLE.put("identifier", paths);

        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("source", paths);

        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("language", paths);

        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("relation", paths);

        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("rigths", paths);

        /*
         * Bounding box
         */
        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("WestBoundLongitude",     paths);

        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("EastBoundLongitude",     paths);

        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("NorthBoundLatitude",     paths);

        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("SouthBoundLatitude",     paths);

        paths = new ArrayList<>();
        EBRIM_QUERYABLE.put("CRS",     paths);
    }

     /**
     * The queryable element from DublinCore and their path id.
     */
    public static final Map<String, List<String>> INSPIRE_QUERYABLE = new HashMap<>();
    static {
        List<String> paths;

        /*
         * The core queryable of DublinCore
         */
        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result/*/gmd:pass/gco:Boolean");
        paths.add("/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result/*/gmd:pass/gco:Boolean");
        INSPIRE_QUERYABLE.put("Degree", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode");
        INSPIRE_QUERYABLE.put("AccessConstraints", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:otherConstraints/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:otherConstraints/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:otherConstraints/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:otherConstraints/gmx:Anchor");
        INSPIRE_QUERYABLE.put("OtherConstraints", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:classification/gmd:MD_ClassificationCode");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:classification/gmd:MD_ClassificationCode");
        INSPIRE_QUERYABLE.put("Classification", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:useLimitation/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:useLimitation/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:useLimitation/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:useLimitation/gmx:Anchor");
        INSPIRE_QUERYABLE.put("ConditionApplyingToAccessAndUse", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
        INSPIRE_QUERYABLE.put("MetadataPointOfContact", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/*/gmd:statement/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/*/gmd:statement/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/*/gmd:statement/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/*/gmd:statement/gmx:Anchor");
        INSPIRE_QUERYABLE.put("Lineage", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gmx:Anchor");
        paths.add("/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString");
        paths.add("/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gmx:Anchor");
        INSPIRE_QUERYABLE.put("SpecificationTitle", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date");
        paths.add("/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:DateTime");
        paths.add("/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date");
        paths.add("/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:DateTime");
        INSPIRE_QUERYABLE.put("SpecificationDate", paths);

        paths = new ArrayList<>();
        paths.add("/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode");
        paths.add("/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode");
        INSPIRE_QUERYABLE.put("SpecificationDateType", paths);
    }
}
