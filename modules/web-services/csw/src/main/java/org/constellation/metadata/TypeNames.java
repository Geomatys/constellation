/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
import java.util.List;
import javax.xml.namespace.QName;

/**
 *
 * @author Guilhem Legal
 */
public class TypeNames {

    private TypeNames() {}

    /**
     * a QName for csw:Record type
     */
    public static final QName RECORD_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "Record");
    
    /**
     * a QName for gmd:MD_Metadata type
     */
    public static final QName METADATA_QNAME = new QName("http://www.isotc211.org/2005/gmd", "MD_Metadata");
    
    /**
     * a QName for csw:Capabilities type
     */
    public static final QName CAPABILITIES_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "Capabilities");
    
    /**
     * some QName for ebrim 3.0 types
     */
    public    static final QName EXTRINSIC_OBJECT_QNAME      = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ExtrinsicObject");
    protected static final QName REGISTRY_PACKAGE_QNAME      = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "RegistryPackage");
    protected static final QName SPECIFICATION_LINK_QNAME    = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "SpecificationLink");
    protected static final QName REGISTRY_OBJECT_QNAME       = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "RegistryObject");
    protected static final QName ASSOCIATION_QNAME           = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Association");
    protected static final QName ADHOC_QUERY_QNAME           = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "AdhocQuery");
    protected static final QName USER_QNAME                  = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "User");
    protected static final QName CLASSIFICATION_NODE_QNAME   = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ClassificationNode");
    protected static final QName AUDITABLE_EVENT_QNAME       = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "AuditableEvent");
    protected static final QName FEDERATION_QNAME            = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Federation");
    protected static final QName SUBSCRIPTION_QNAME          = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Subscription");
    protected static final QName OBJECT_REF_LIST_QNAME       = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ObjectRefList");
    protected static final QName CLASSIFICATION_QNAME        = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Classification");
    protected static final QName PERSON_QNAME                = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Person");
    protected static final QName SERVICE_BINDING_QNAME       = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ServiceBinding");
    protected static final QName NOTIFICATION_QNAME          = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Notification");
    protected static final QName CLASSIFICATION_SCHEME_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ClassificationScheme");
    protected static final QName SERVICE_QNAME               = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Service");
    protected static final QName EXTERNAL_IDENTIFIER_QNAME   = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ExternalIdentifier");
    protected static final QName REGISTRY_QNAME              = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Registry");
    protected static final QName ORGANIZATION_QNAME          = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Organization");
    protected static final QName EXTERNAL_LINK_QNAME         = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ExternalLink");
    protected static final QName WRS_EXTRINSIC_OBJECT_QNAME  = new QName("http://www.opengis.net/cat/wrs/1.0",          "ExtrinsicObject");
    
    /**
     * some QName for ebrim 2.5 types
     */
    public    static final QName EXTRINSIC_OBJECT_25_QNAME     = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "ExtrinsicObject");
    protected static final QName FEDERATION25_QNAME            = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "Federation");
    protected static final QName EXTERNAL_LINK25_QNAME         = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "ExternalLink");
    protected static final QName CLASSIFICATION_NODE25_QNAME   = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "ClassificationNode");
    protected static final QName USER25_QNAME                  = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "User");
    protected static final QName CLASSIFICATION25_QNAME        = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "Classification");
    protected static final QName REGISTRY_PACKAGE25_QNAME      = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "RegistryPackage");
    protected static final QName REGISTRY_OBJECT25_QNAME       = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "RegistryObject");
    protected static final QName ASSOCIATION25_QNAME           = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "Association");
    protected static final QName REGISTRY_ENTRY25_QNAME        = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "RegistryEntry");
    protected static final QName CLASSIFICATION_SCHEME25_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "ClassificationScheme");
    protected static final QName ORGANIZATION25_QNAME          = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "Organization");
    protected static final QName EXTERNAL_IDENTIFIER25_QNAME   = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "ExternalIdentifier");
    protected static final QName SPECIFICATION_LINK25_QNAME    = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "SpecificationLink");
    protected static final QName REGISTRY25_QNAME              = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "Registry");
    protected static final QName SERVICE_BINDING25_QNAME       = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "ServiceBinding");
    protected static final QName SERVICE25_QNAME               = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "Service");
    protected static final QName AUDITABLE_EVENT25_QNAME       = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "AuditableEvent");
    protected static final QName SUBSCRIPTION25_QNAME          = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "Subscription");
    protected static final QName GEOMETRY09_QNAME              = new QName("http://www.opengis.net/cat/wrs",              "Geometry");
    protected static final QName APPLICATION_MODULE09_QNAME    = new QName("http://www.opengis.net/cat/wrs",              "ApplicationModule");
    protected static final QName WRS_EXTRINSIC_OBJECT09_QNAME  = new QName("http://www.opengis.net/cat/wrs",              "WRSExtrinsicObject");

    
    //iso 19115 typeNames
    public static final List<QName> ISO_TYPE_NAMES = new ArrayList<QName>();
    static {
        ISO_TYPE_NAMES.add(METADATA_QNAME);
    }
    
    //dublin core typeNames
    public static final List<QName> DC_TYPE_NAMES = new ArrayList<QName>();
    static {
        DC_TYPE_NAMES.add(RECORD_QNAME);
    }
    
    //ebrim v3.0 typeNames
    public static final List<QName> EBRIM30_TYPE_NAMES = new ArrayList<QName>();
    static {
        EBRIM30_TYPE_NAMES.add(ADHOC_QUERY_QNAME);
        EBRIM30_TYPE_NAMES.add(ASSOCIATION_QNAME);
        EBRIM30_TYPE_NAMES.add(AUDITABLE_EVENT_QNAME);
        EBRIM30_TYPE_NAMES.add(CLASSIFICATION_NODE_QNAME);
        EBRIM30_TYPE_NAMES.add(CLASSIFICATION_SCHEME_QNAME);
        EBRIM30_TYPE_NAMES.add(CLASSIFICATION_QNAME);
        EBRIM30_TYPE_NAMES.add(EXTERNAL_IDENTIFIER_QNAME);
        EBRIM30_TYPE_NAMES.add(EXTERNAL_LINK_QNAME);
        EBRIM30_TYPE_NAMES.add(EXTRINSIC_OBJECT_QNAME);
        EBRIM30_TYPE_NAMES.add(FEDERATION_QNAME);
        EBRIM30_TYPE_NAMES.add(NOTIFICATION_QNAME);
        EBRIM30_TYPE_NAMES.add(OBJECT_REF_LIST_QNAME);
        EBRIM30_TYPE_NAMES.add(PERSON_QNAME);
        EBRIM30_TYPE_NAMES.add(ORGANIZATION_QNAME);
        EBRIM30_TYPE_NAMES.add(REGISTRY_OBJECT_QNAME);
        EBRIM30_TYPE_NAMES.add(REGISTRY_PACKAGE_QNAME);
        EBRIM30_TYPE_NAMES.add(REGISTRY_QNAME);
        EBRIM30_TYPE_NAMES.add(SERVICE_BINDING_QNAME);
        EBRIM30_TYPE_NAMES.add(SERVICE_QNAME);
        EBRIM30_TYPE_NAMES.add(SPECIFICATION_LINK_QNAME);
        EBRIM30_TYPE_NAMES.add(SUBSCRIPTION_QNAME);
        EBRIM30_TYPE_NAMES.add(USER_QNAME);
        EBRIM30_TYPE_NAMES.add(WRS_EXTRINSIC_OBJECT_QNAME);
    }
    
    //ebrim v2.5 typenames
    public static final List<QName> EBRIM25_TYPE_NAMES = new ArrayList<QName>();
    static {
        EBRIM25_TYPE_NAMES.add(EXTRINSIC_OBJECT_25_QNAME);
        EBRIM25_TYPE_NAMES.add(FEDERATION25_QNAME);
        EBRIM25_TYPE_NAMES.add(EXTERNAL_LINK25_QNAME);
        EBRIM25_TYPE_NAMES.add(CLASSIFICATION_NODE25_QNAME);
        EBRIM25_TYPE_NAMES.add(USER25_QNAME);
        EBRIM25_TYPE_NAMES.add(CLASSIFICATION25_QNAME);
        EBRIM25_TYPE_NAMES.add(REGISTRY_PACKAGE25_QNAME);
        EBRIM25_TYPE_NAMES.add(REGISTRY_OBJECT25_QNAME);
        EBRIM25_TYPE_NAMES.add(ASSOCIATION25_QNAME);
        EBRIM25_TYPE_NAMES.add(REGISTRY_ENTRY25_QNAME);
        EBRIM25_TYPE_NAMES.add(CLASSIFICATION_SCHEME25_QNAME);
        EBRIM25_TYPE_NAMES.add(ORGANIZATION25_QNAME);
        EBRIM25_TYPE_NAMES.add(EXTERNAL_IDENTIFIER25_QNAME);
        EBRIM25_TYPE_NAMES.add(SPECIFICATION_LINK25_QNAME);
        EBRIM25_TYPE_NAMES.add(REGISTRY25_QNAME);
        EBRIM25_TYPE_NAMES.add(SERVICE_BINDING25_QNAME);
        EBRIM25_TYPE_NAMES.add(SERVICE25_QNAME);
        EBRIM25_TYPE_NAMES.add(AUDITABLE_EVENT25_QNAME);
        EBRIM25_TYPE_NAMES.add(SUBSCRIPTION25_QNAME);
        EBRIM25_TYPE_NAMES.add(GEOMETRY09_QNAME);
        EBRIM25_TYPE_NAMES.add(APPLICATION_MODULE09_QNAME);
        EBRIM25_TYPE_NAMES.add(WRS_EXTRINSIC_OBJECT09_QNAME);
    }

    /**
     * Return true if the specified list of QNames contains an ebrim V2.5 QName.
     *
     * @param qnames A list of QNames.
     * @return true if the list contains at least one ebrim V2.5 QName.
     */
    public static final boolean containsOneOfEbrim25(final List<QName> qnames) {

        if (qnames.contains(EXTRINSIC_OBJECT_25_QNAME)
         || qnames.contains(FEDERATION25_QNAME)
         || qnames.contains(EXTERNAL_LINK25_QNAME)
         || qnames.contains(CLASSIFICATION_NODE25_QNAME)
         || qnames.contains(USER25_QNAME)
         || qnames.contains(CLASSIFICATION25_QNAME)
         || qnames.contains(REGISTRY_PACKAGE25_QNAME)
         || qnames.contains(REGISTRY_OBJECT25_QNAME)
         || qnames.contains(ASSOCIATION25_QNAME)
         || qnames.contains(REGISTRY_ENTRY25_QNAME)
         || qnames.contains(CLASSIFICATION_SCHEME25_QNAME)
         || qnames.contains(ORGANIZATION25_QNAME)
         || qnames.contains(EXTERNAL_IDENTIFIER25_QNAME)
         || qnames.contains(SPECIFICATION_LINK25_QNAME)
         || qnames.contains(REGISTRY25_QNAME)
         || qnames.contains(SERVICE_BINDING25_QNAME)
         || qnames.contains(SERVICE25_QNAME)
         || qnames.contains(AUDITABLE_EVENT25_QNAME)
         || qnames.contains(SUBSCRIPTION25_QNAME)
         || qnames.contains(GEOMETRY09_QNAME)
         || qnames.contains(APPLICATION_MODULE09_QNAME)
         || qnames.contains(WRS_EXTRINSIC_OBJECT09_QNAME))
            return true;
        return false;
    }

    /**
     * Return true if the specified list of QNames contains an ebrim V3.0 QName.
     *
     * @param qnames A list of QNames.
     * @return true if the list contains at least one ebrim V3.0 QName.
     */
    public static final boolean containsOneOfEbrim30(final List<QName> qnames) {

        if (qnames.contains(ADHOC_QUERY_QNAME)
         || qnames.contains(ASSOCIATION_QNAME)
         || qnames.contains(AUDITABLE_EVENT_QNAME)
         || qnames.contains(CLASSIFICATION_NODE_QNAME)
         || qnames.contains(CLASSIFICATION_SCHEME_QNAME)
         || qnames.contains(CLASSIFICATION_QNAME)
         || qnames.contains(EXTERNAL_IDENTIFIER_QNAME)
         || qnames.contains(EXTERNAL_LINK_QNAME)
         || qnames.contains(EXTRINSIC_OBJECT_QNAME)
         || qnames.contains(FEDERATION_QNAME)
         || qnames.contains(NOTIFICATION_QNAME)
         || qnames.contains(OBJECT_REF_LIST_QNAME)
         || qnames.contains(PERSON_QNAME)
         || qnames.contains(ORGANIZATION_QNAME)
         || qnames.contains(REGISTRY_OBJECT_QNAME)
         || qnames.contains(REGISTRY_PACKAGE_QNAME)
         || qnames.contains(REGISTRY_QNAME)
         || qnames.contains(SERVICE_BINDING_QNAME)
         || qnames.contains(SERVICE_QNAME)
         || qnames.contains(SPECIFICATION_LINK_QNAME)
         || qnames.contains(SUBSCRIPTION_QNAME)
         || qnames.contains(USER_QNAME)
         || qnames.contains(WRS_EXTRINSIC_OBJECT_QNAME))
            return true;
        return false;
    }
}
