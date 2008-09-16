/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
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
public class CSWQueryable {
    
    /**
     * The queryable element from ISO 19115 and their path id.
     */
    protected static Map<String, List<String>> ISO_QUERYABLE;
    static {
        ISO_QUERYABLE      = new HashMap<String, List<String>>();
        List<String> paths;
        
        /*
         * The core queryable of ISO 19115
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:topicCategory");
        ISO_QUERYABLE.put("Subject", paths);
        
        //MANDATORY
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:title");
        ISO_QUERYABLE.put("Title", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:abstract");
        ISO_QUERYABLE.put("Abstract", paths);
        
        //MANDATORY TODO tout les valeur
        paths = new ArrayList<String>();
        paths.add("*");
        ISO_QUERYABLE.put("AnyText", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributionFormat:name");
        ISO_QUERYABLE.put("Format", paths);
        
        //MANDATORY
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:fileIdentifier");
        ISO_QUERYABLE.put("Identifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:dateStamp");
        ISO_QUERYABLE.put("Modified", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:hierarchyLevel");
        ISO_QUERYABLE.put("Type", paths);
        
        /*
         * Bounding box
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude");
        ISO_QUERYABLE.put("WestBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude");
        ISO_QUERYABLE.put("EastBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude");
        ISO_QUERYABLE.put("NorthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude");
        ISO_QUERYABLE.put("SouthBoundLatitude",     paths);
        
        /*
         * CRS 
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:codeSpace");
        ISO_QUERYABLE.put("Authority",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:code");
        ISO_QUERYABLE.put("ID",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:code");
        ISO_QUERYABLE.put("Version",     paths);
        
        /*
         * Additional queryable Element
         */ 
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:alternateTitle");
        ISO_QUERYABLE.put("AlternateTitle",   paths);
        
        //TODO verify codelist  CI_DateTypeCode=revision
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:date:date");
        ISO_QUERYABLE.put("RevisionDate",  paths);
        
        //TODO verify codelist  CI_DateTypeCode=creation
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:date:date");
        ISO_QUERYABLE.put("CreationDate",  paths);
        
        //TODO verify codelist  CI_DateTypeCode=publication
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:date:date");
        ISO_QUERYABLE.put("PublicationDate",  paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:contact:organisationName");
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:organisationName");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:organisationName");
        ISO_QUERYABLE.put("OrganisationName", paths);
        
        //TODO If an instance of the class MD_SecurityConstraint exists for a resource, the “HasSecurityConstraints” is “true”, otherwise “false”
        paths = new ArrayList<String>();
        ISO_QUERYABLE.put("HasSecurityConstraints", paths);
        
        //TODO MD_FeatureCatalogueDescription
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:language");
        ISO_QUERYABLE.put("Language", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:identifier:code");
        ISO_QUERYABLE.put("ResourceIdentifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:parentIdentifier");
        ISO_QUERYABLE.put("ParentIdentifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:Type");
        ISO_QUERYABLE.put("KeywordType", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:topicCategory");
        ISO_QUERYABLE.put("TopicCategory", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:language");
        ISO_QUERYABLE.put("ResourceLanguage", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:code");
        ISO_QUERYABLE.put("GeographicDescriptionCode", paths);
        
        /*
         * spatial resolution
         */
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:spatialResolution:equivalentScale:denominator");
        ISO_QUERYABLE.put("Denominator", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:spatialResolution:distance");
        ISO_QUERYABLE.put("DistanceValue", paths);
        
        //TODO not existing path in MDWeb or geotools (Distance is treated as a primitive type)
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:spatialResolution:distance:uom");
        ISO_QUERYABLE.put("DistanceUOM", paths);
        
        /*
         * Temporal Extent
         */ 
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:beginPosition");
        ISO_QUERYABLE.put("TempExtent_begin", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:endPosition");
        ISO_QUERYABLE.put("TempExtent_end", paths);
        
       
        
        // the following element are described in Service part of ISO 19139 not yet used in MDWeb 
        paths = new ArrayList<String>();
        ISO_QUERYABLE.put("ServiceType", paths);
        ISO_QUERYABLE.put("ServiceTypeVersion", paths);
        ISO_QUERYABLE.put("Operation", paths);
        ISO_QUERYABLE.put("CouplingType", paths);
        ISO_QUERYABLE.put("OperatesOn", paths);
        ISO_QUERYABLE.put("OperatesOnIdentifier", paths);
        ISO_QUERYABLE.put("OperatesOnWithOpName", paths);
    }
    
    
    
    /**
     * The queryable element from DublinCore and their path id.
     */
    protected static Map<String, List<String>> DUBLIN_CORE_QUERYABLE;
    static {
        DUBLIN_CORE_QUERYABLE = new HashMap<String, List<String>>();
        List<String> paths;
        
        /*
         * The core queryable of DublinCore
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:title");
        paths.add("Catalog Web Service:Record:title:content");
        paths.add("Ebrim v3.0:RegistryObject:name:localizedString:value");
        paths.add("Ebrim v3.0:RegistryPackage:name:localizedString:value");
        DUBLIN_CORE_QUERYABLE.put("title", paths);
        
        //TODO verify codelist=originator
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName");
        paths.add("Catalog Web Service:Record:creator:content");
        DUBLIN_CORE_QUERYABLE.put("creator", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:topicCategory");
        paths.add("Catalog Web Service:Record:subject:content");
        //TODO @name = “http://purl.org/dc/elements/1.1/subject”
        paths.add("Ebrim v3.0:RegistryObject:slot:valueList:value");
        paths.add("Ebrim v3.0:RegistryPackage:slot:valueList:value");
        DUBLIN_CORE_QUERYABLE.put("description", paths);
        DUBLIN_CORE_QUERYABLE.put("subject", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:abstract");
        paths.add("Catalog Web Service:Record:abstract:content");
        paths.add("Ebrim v3.0:RegistryObject:description:localizedString:value");
        paths.add("Ebrim v3.0:RegistryPackage:description:localizedString:value");
        DUBLIN_CORE_QUERYABLE.put("abstract", paths);
        
        //TODO verify codelist=publisher
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName");
        paths.add("Catalog Web Service:Record:publisher:content");
        DUBLIN_CORE_QUERYABLE.put("publisher", paths);
        
        //TODO verify codelist=contributor
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName");
        paths.add("Catalog Web Service:Record:contributor:content");
        DUBLIN_CORE_QUERYABLE.put("contributor", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:dateStamp");
        paths.add("Catalog Web Service:Record:date:content");
        DUBLIN_CORE_QUERYABLE.put("date", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:hierarchyLevel");
        paths.add("Catalog Web Service:Record:type:content");
        paths.add("Ebrim v3.0:RegistryObject:objectType");
        paths.add("Ebrim v3.0:RegistryPackage:objectType");
        DUBLIN_CORE_QUERYABLE.put("type", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributionFormat:name");
        paths.add("Catalog Web Service:Record:format:content");
        paths.add("Ebrim v3.0:ExtrinsicObject:mimeType");
        DUBLIN_CORE_QUERYABLE.put("format", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:fileIdentifier");
        paths.add("Catalog Web Service:Record:identifier:content");
        paths.add("ISO 19110:FC_FeatureCatalogue:id");
        paths.add("Ebrim v3.0:RegistryObject:id");
        paths.add("Ebrim v3.0:RegistryPackage:id");
        DUBLIN_CORE_QUERYABLE.put("identifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("Catalog Web Service:Record:source");
        DUBLIN_CORE_QUERYABLE.put("source", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:language");
        paths.add("Catalog Web Service:Record:language:content");
        DUBLIN_CORE_QUERYABLE.put("language", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:aggregationInfo");
        paths.add("Catalog Web Service:Record:relation:content");
        DUBLIN_CORE_QUERYABLE.put("relation", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:accessConstraints");
        paths.add("Catalog Web Service:Record:rights:content");
        DUBLIN_CORE_QUERYABLE.put("rights", paths);
        
        /*
         * Bounding box
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude");
        paths.add("Catalog Web Service:Record:BoundingBox:LowerCorner");
        DUBLIN_CORE_QUERYABLE.put("WestBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude");
        paths.add("Catalog Web Service:Record:BoundingBox:UpperCorner");
        DUBLIN_CORE_QUERYABLE.put("EastBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude");
        paths.add("Catalog Web Service:Record:BoundingBox:UpperCorner");
        DUBLIN_CORE_QUERYABLE.put("NorthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude");
        paths.add("Catalog Web Service:Record:BoundingBox:LowerCorner");
        DUBLIN_CORE_QUERYABLE.put("SouthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("Catalog Web Service:Record:BoundingBox:crs");
        DUBLIN_CORE_QUERYABLE.put("CRS",     paths);
    }
    
    /**
     * The queryable element from DublinCore and their path id.
     */
    protected static Map<String, List<String>> EBRIM_QUERYABLE;
    static {
        EBRIM_QUERYABLE = new HashMap<String, List<String>>();
        List<String> paths;
        
        /*
         * The core queryable of DublinCore
         */
        paths = new ArrayList<String>();
        paths.add("Ebrim v3.0:RegistryObject:name:localizedString:value");
        paths.add("Ebrim v3.0:RegistryPackage:name:localizedString:value");
        EBRIM_QUERYABLE.put("", paths);
        
        //TODO verify codelist=originator
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("creator", paths);
        
        paths = new ArrayList<String>();
        //TODO @name = “http://purl.org/dc/elements/1.1/subject”
        paths.add("Ebrim v3.0:RegistryObject:slot:valueList:value");
        paths.add("Ebrim v3.0:RegistryPackage:slot:valueList:value");
        EBRIM_QUERYABLE.put("description", paths);
        EBRIM_QUERYABLE.put("subject", paths);
        
        paths = new ArrayList<String>();
        paths.add("Ebrim v3.0:RegistryObject:description:localizedString:value");
        paths.add("Ebrim v3.0:RegistryPackage:description:localizedString:value");
        EBRIM_QUERYABLE.put("abstract", paths);
        
        //TODO verify codelist=publisher
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("publisher", paths);
        
        //TODO verify codelist=contributor
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("contributor", paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("date", paths);
        
        paths = new ArrayList<String>();
        paths.add("Ebrim v3.0:RegistryObject:objectType");
        paths.add("Ebrim v3.0:RegistryPackage:objectType");
        EBRIM_QUERYABLE.put("type", paths);
        
        paths = new ArrayList<String>();
        paths.add("Ebrim v3.0:ExtrinsicObject:mimeType");
        EBRIM_QUERYABLE.put("format", paths);
        
        paths = new ArrayList<String>();
        paths.add("Ebrim v3.0:RegistryObject:id");
        paths.add("Ebrim v3.0:RegistryPackage:id");
        EBRIM_QUERYABLE.put("identifier", paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("source", paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("language", paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("relation", paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("rigths", paths);
        
        /*
         * Bounding box
         */
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("WestBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("EastBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("NorthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("SouthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("CRS",     paths);
    }
    
    /**
     * a QName for csw:Record type
     */
    protected final static QName _Record_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "Record");
    
    /**
     * a QName for gmd:MD_Metadata type
     */
    protected final static QName _Metadata_QNAME = new QName("http://www.isotc211.org/2005/gmd", "MD_Metadata");
    
    /**
     * a QName for csw:Capabilities type
     */
    protected final static QName _Capabilities_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "Capabilities");
    
    /**
     * some QName for ebrim types
     */
    protected final static QName _ExtrinsicObject_QNAME      = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ExtrinsicObject");
    protected final static QName _RegistryPackage_QNAME      = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "RegistryPackage");
    protected final static QName _SpecificationLink_QNAME    = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "SpecificationLink");
    protected final static QName _RegistryObject_QNAME       = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "RegistryObject");
    protected final static QName _Association_QNAME          = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Association");
    protected final static QName _AdhocQuery_QNAME           = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "AdhocQuery");
    protected final static QName _User_QNAME                 = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "User");
    protected final static QName _ClassificationNode_QNAME   = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ClassificationNode");
    protected final static QName _AuditableEvent_QNAME       = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "AuditableEvent");
    protected final static QName _Federation_QNAME           = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Federation");
    protected final static QName _Subscription_QNAME         = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Subscription");
    protected final static QName _ObjectRefList_QNAME        = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ObjectRefList");
    protected final static QName _Classification_QNAME       = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Classification");
    protected final static QName _Person_QNAME               = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Person");
    protected final static QName _ServiceBinding_QNAME       = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ServiceBinding");
    protected final static QName _Notification_QNAME         = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Notification");
    protected final static QName _ClassificationScheme_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ClassificationScheme");
    protected final static QName _Service_QNAME              = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Service");
    protected final static QName _ExternalIdentifier_QNAME   = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ExternalIdentifier");
    protected final static QName _Registry_QNAME             = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Registry");
    protected final static QName _Organization_QNAME         = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Organization");
    protected final static QName _ExternalLink_QNAME         = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ExternalLink");
    
}
