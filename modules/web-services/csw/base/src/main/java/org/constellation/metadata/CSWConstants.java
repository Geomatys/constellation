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
import java.util.Arrays;
import java.util.List;
import net.jcip.annotations.Immutable;

import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Service;

import org.geotoolkit.csw.xml.AbstractCapabilities;
import org.geotoolkit.csw.xml.CswXmlFactory;
import org.geotoolkit.ogc.xml.v110.ComparisonOperatorType;
import org.geotoolkit.ogc.xml.v110.ComparisonOperatorsType;
import org.geotoolkit.ogc.xml.v110.FilterCapabilities;
import org.geotoolkit.ogc.xml.v110.GeometryOperandsType;
import org.geotoolkit.ogc.xml.v110.IdCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.ScalarCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.SpatialCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.SpatialOperatorType;
import org.geotoolkit.ogc.xml.v110.SpatialOperatorsType;
import org.geotoolkit.ows.xml.AbstractContact;
import org.geotoolkit.ows.xml.AbstractDCP;
import org.geotoolkit.ows.xml.AbstractResponsiblePartySubset;
import org.geotoolkit.ows.xml.AbstractServiceIdentification;
import org.geotoolkit.ows.xml.AbstractServiceProvider;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.ows.xml.AbstractDomain;
import org.geotoolkit.ows.xml.AbstractOperation;
import org.geotoolkit.ows.xml.AbstractOperationsMetadata;

import static org.geotoolkit.gml.xml.v311.ObjectFactory.*;

import org.opengis.filter.capability.Operator;
import org.opengis.filter.capability.SpatialOperator;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.constellation.ws.MimeType;
import org.geotoolkit.ows.xml.AbstractOnlineResourceType;

/**
 * CSW constants.
 *
 * @version $Id$
 * @author Guilhem Legal (Geomatys)
 */
@Immutable
public abstract class CSWConstants {

    /**
     * Request parameters.
     */
    public static final String CSW_202_VERSION = "2.0.2";
    public static final String CSW = "CSW";
    
    public static final String OUTPUT_SCHEMA = "outputSchema";
    public static final String TYPENAMES = "TypeNames";
    public static final String FILTER_CAPABILITIES = "Filter_Capabilities";
    public static final String PARAMETERNAME = "parameterName";
    public static final String TRANSACTION_TYPE = "TransactionType";
    public static final String SOURCE = "Source";
    public static final String ALL = "All";
    public static final String NAMESPACE = "namespace";
    public static final String XML_EXT = ".xml";
    public static final String NETCDF_EXT = ".nc";
    public static final String NCML_EXT = ".ncml";

    // TODO those 3 namespace must move to geotk Namespace class
    public static final String EBRIM_25 = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5";
    public static final String EBRIM_30 = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";

    /**
     * A list of supported MIME type.
     */
    public static final List<String> ACCEPTED_OUTPUT_FORMATS;
    static {
        ACCEPTED_OUTPUT_FORMATS = new ArrayList<>();
        ACCEPTED_OUTPUT_FORMATS.add(MimeType.TEXT_XML);
        ACCEPTED_OUTPUT_FORMATS.add(MimeType.APPLICATION_XML);
        ACCEPTED_OUTPUT_FORMATS.add(MimeType.TEXT_HTML);
        ACCEPTED_OUTPUT_FORMATS.add(MimeType.TEXT_PLAIN);
    }
    
    /**
     * Error message
     */

    public static final String NOT_EXIST = " does not exist";

    public static final String MALFORMED = " is malformed";

    public static final AbstractOperationsMetadata OPERATIONS_METADATA;
    static {
        final List<AbstractDCP> getAndPost = new ArrayList<>();
        getAndPost.add(OWSXmlFactory.buildDCP("1.0.0", "somURL", "someURL"));

        final List<AbstractDCP> onlyPost = new ArrayList<>();
        onlyPost.add(OWSXmlFactory.buildDCP("1.0.0", "somURL", "someURL"));

        final List<AbstractOperation> operations = new ArrayList<>();

        final List<AbstractDomain> gcParameters = new ArrayList<>();
        gcParameters.add(OWSXmlFactory.buildDomain("1.0.0", "sections", Arrays.asList("All", "ServiceIdentification", "ServiceProvider", "OperationsMetadata", "Filter_Capabilities")));
        gcParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Version",  Arrays.asList("2.0.2")));
        gcParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Service",  Arrays.asList("CSW")));
        
        final AbstractOperation getCapabilities = OWSXmlFactory.buildOperation("1.0.0", getAndPost, gcParameters, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<AbstractDomain> grParameters = new ArrayList<>();
        grParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Version", Arrays.asList("2.0.2")));
        grParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Service", Arrays.asList("CSW")));
        grParameters.add(OWSXmlFactory.buildDomain("1.0.0", "TypeNames", Arrays.asList("gmd:MD_Metadata", "csw:Record")));
        grParameters.add(OWSXmlFactory.buildDomain("1.0.0", "outputFormat", Arrays.asList("text/xml", "application/xml")));
        grParameters.add(OWSXmlFactory.buildDomain("1.0.0", "outputSchema", Arrays.asList("http://www.opengis.net/cat/csw/2.0.2", "http://www.isotc211.org/2005/gmd")));
        grParameters.add(OWSXmlFactory.buildDomain("1.0.0", "resultType", Arrays.asList("hits", "results", "validate")));
        grParameters.add(OWSXmlFactory.buildDomain("1.0.0", "ElementSetName", Arrays.asList("brief", "summary", "full")));
        grParameters.add(OWSXmlFactory.buildDomain("1.0.0", "CONSTRAINTLANGUAGE", Arrays.asList("Filter", "CQL")));
        
        final List<AbstractDomain> grConstraints = new ArrayList<>();
        
        final List<String> supportedISOQueryable = new ArrayList<>();
        supportedISOQueryable.add("RevisionDate");
        supportedISOQueryable.add("AlternateTitle");
        supportedISOQueryable.add("CreationDate");
        supportedISOQueryable.add("PublicationDate");
        supportedISOQueryable.add("OrganisationName");
        supportedISOQueryable.add("HasSecurityConstraints");
        supportedISOQueryable.add("Language");
        supportedISOQueryable.add("ResourceIdentifier");
        supportedISOQueryable.add("ParentIdentifier");
        supportedISOQueryable.add("KeywordType");
        supportedISOQueryable.add("TopicCategory");
        supportedISOQueryable.add("ResourceLanguage");
        supportedISOQueryable.add("GeographicDescriptionCode");
        supportedISOQueryable.add("DistanceValue");
        supportedISOQueryable.add("DistanceUOM");
        supportedISOQueryable.add("TempExtent_begin");
        supportedISOQueryable.add("TempExtent_end");
        supportedISOQueryable.add("ServiceType");
        supportedISOQueryable.add("ServiceTypeVersion");
        supportedISOQueryable.add("Operation");
        supportedISOQueryable.add("CouplingType");
        supportedISOQueryable.add("OperatesOn");
        supportedISOQueryable.add("Denominator");
        supportedISOQueryable.add("OperatesOnIdentifier");
        supportedISOQueryable.add("OperatesOnWithOpName");

        grConstraints.add(OWSXmlFactory.buildDomain("1.0.0", "SupportedISOQueryables", supportedISOQueryable));
        grConstraints.add(OWSXmlFactory.buildDomain("1.0.0", "AdditionalQueryables", Arrays.asList("HierarchyLevelName")));
        
        final AbstractOperation getRecords = OWSXmlFactory.buildOperation("1.0.0", getAndPost, grParameters, grConstraints, "GetRecords");
        operations.add(getRecords);
        
        final List<AbstractDomain> grbParameters = new ArrayList<>();
        grbParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Version", Arrays.asList("2.0.2")));
        grbParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Service", Arrays.asList("CSW")));
        grbParameters.add(OWSXmlFactory.buildDomain("1.0.0", "ElementSetName", Arrays.asList("brief", "summary", "full")));
        grbParameters.add(OWSXmlFactory.buildDomain("1.0.0", "outputSchema", Arrays.asList("http://www.opengis.net/cat/csw/2.0.2", "http://www.isotc211.org/2005/gmd")));
        grbParameters.add(OWSXmlFactory.buildDomain("1.0.0", "outputFormat", Arrays.asList("text/xml", "application/xml")));
        
        final AbstractOperation getRecordById = OWSXmlFactory.buildOperation("1.0.0", getAndPost, grbParameters, null, "GetRecordById");
        operations.add(getRecordById);
        
        final List<AbstractDomain> drParameters = new ArrayList<>();
        drParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Version", Arrays.asList("2.0.2")));
        drParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Service", Arrays.asList("CSW")));
        drParameters.add(OWSXmlFactory.buildDomain("1.0.0", "TypeName", Arrays.asList("gmd:MD_Metadata", "csw:Record")));
        drParameters.add(OWSXmlFactory.buildDomain("1.0.0", "SchemaLanguage", Arrays.asList("http://www.w3.org/XML/Schema", "XMLSCHEMA")));
        drParameters.add(OWSXmlFactory.buildDomain("1.0.0", "outputFormat", Arrays.asList("text/xml", "application/xml")));
        
        final AbstractOperation describeRecord = OWSXmlFactory.buildOperation("1.0.0", getAndPost, drParameters, null, "DescribeRecord");
        operations.add(describeRecord);

        
        final List<AbstractDomain> gdParameters = new ArrayList<>();
        gdParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Version", Arrays.asList("2.0.2")));
        gdParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Service", Arrays.asList("CSW")));
        
        final AbstractOperation getDomain = OWSXmlFactory.buildOperation("1.0.0", getAndPost, gdParameters, null, "GetDomain");
        operations.add(getDomain);
        
        final List<AbstractDomain> tParameters = new ArrayList<>();
        tParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Version", Arrays.asList("2.0.2")));
        tParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Service", Arrays.asList("CSW")));
        tParameters.add(OWSXmlFactory.buildDomain("1.0.0", "ResourceType", Arrays.asList("toUpdate")));
        
        final AbstractOperation transaction = OWSXmlFactory.buildOperation("1.0.0", onlyPost, tParameters, null, "Transaction");
        operations.add(transaction);
        
        final List<AbstractDomain> hParameters = new ArrayList<>();
        hParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Version", Arrays.asList("2.0.2")));
        hParameters.add(OWSXmlFactory.buildDomain("1.0.0", "Service", Arrays.asList("CSW")));
        hParameters.add(OWSXmlFactory.buildDomain("1.0.0", "ResourceType", Arrays.asList("toUpdate")));
        
        final AbstractOperation harvest = OWSXmlFactory.buildOperation("1.0.0", onlyPost, hParameters, null, "Harvest");
        operations.add(harvest);
        
        final List<AbstractDomain> parameters = new ArrayList<>();
        parameters.add(OWSXmlFactory.buildDomain("1.0.0", "service", Arrays.asList("CSW")));
        parameters.add(OWSXmlFactory.buildDomain("1.0.0", "version", Arrays.asList("2.0.2")));

        final List<AbstractDomain> constraints = new ArrayList<>();
        constraints.add(OWSXmlFactory.buildDomain("1.0.0", "PostEncoding", Arrays.asList("XML")));

        OPERATIONS_METADATA = OWSXmlFactory.buildOperationsMetadata("1.0.0", operations, parameters, constraints, null);
    }
    
    public static final FilterCapabilities CSW_FILTER_CAPABILITIES = new FilterCapabilities();

    static {
        final GeometryOperandsType geom = new GeometryOperandsType(Arrays.asList(_Envelope_QNAME, _Point_QNAME, _LineString_QNAME, _Polygon_QNAME));
        final SpatialOperator[] spaOps = new SpatialOperator[11];
        spaOps[0]  = new SpatialOperatorType("BBOX", null);
        spaOps[1]  = new SpatialOperatorType("BEYOND", null);
        spaOps[2]  = new SpatialOperatorType("CONTAINS", null);
        spaOps[3]  = new SpatialOperatorType("CROSSES", null);
        spaOps[4]  = new SpatialOperatorType("DISJOINT", null);
        spaOps[5]  = new SpatialOperatorType("D_WITHIN", null);
        spaOps[6]  = new SpatialOperatorType("EQUALS", null);
        spaOps[7]  = new SpatialOperatorType("INTERSECTS", null);
        spaOps[8]  = new SpatialOperatorType("OVERLAPS", null);
        spaOps[9]  = new SpatialOperatorType("TOUCHES", null);
        spaOps[10] = new SpatialOperatorType("WITHIN", null);
        
        
        final SpatialOperatorsType spaOp = new SpatialOperatorsType(spaOps);
        final SpatialCapabilitiesType spatial = new SpatialCapabilitiesType(geom, spaOp);
        CSW_FILTER_CAPABILITIES.setSpatialCapabilities(spatial);

        final Operator[] compOps = new Operator[9];
        compOps[0] = ComparisonOperatorType.BETWEEN;
        compOps[1] = ComparisonOperatorType.EQUAL_TO;
        compOps[2] = ComparisonOperatorType.NOT_EQUAL_TO;
        compOps[3] = ComparisonOperatorType.LESS_THAN;
        compOps[4] = ComparisonOperatorType.LESS_THAN_EQUAL_TO;
        compOps[5] = ComparisonOperatorType.GREATER_THAN;
        compOps[6] = ComparisonOperatorType.GREATER_THAN_EQUAL_TO;
        compOps[7] = ComparisonOperatorType.LIKE;
        compOps[8] = ComparisonOperatorType.NULL_CHECK;
        final ComparisonOperatorsType compOp = new ComparisonOperatorsType(compOps);
        final ScalarCapabilitiesType scalar = new ScalarCapabilitiesType(compOp, null, true);
        
        CSW_FILTER_CAPABILITIES.setScalarCapabilities(scalar);

        final IdCapabilitiesType id = new IdCapabilitiesType(false, true);
        CSW_FILTER_CAPABILITIES.setIdCapabilities(id);
    }

    /**
     * Generates the base capabilities for a WMS from the service metadata.
     *
     * @param metadata the service metadata
     * @return the service base capabilities
     */
    public static AbstractCapabilities createCapabilities(final String version, final Service metadata) {
        ensureNonNull("metadata", metadata);
        ensureNonNull("version",  version);

        final Contact currentContact = metadata.getServiceContact();
        final AccessConstraint constraint = metadata.getServiceConstraints();

        final AbstractServiceIdentification servIdent;
        if (constraint != null) {
            servIdent = OWSXmlFactory.buildServiceIdentification("1.0.0", metadata.getName(), metadata.getDescription(),
                    metadata.getKeywords(), "CSW", metadata.getVersions(), constraint.getFees(),
                    Arrays.asList(constraint.getAccessConstraint()));
        } else {
            servIdent = OWSXmlFactory.buildServiceIdentification("1.0.0", metadata.getName(), metadata.getDescription(),
                    metadata.getKeywords(), "CSW", metadata.getVersions(), null, new ArrayList<String>());
        }

        final AbstractServiceProvider servProv;
        if (currentContact != null) {
            // Create provider part.
            final AbstractContact contact = OWSXmlFactory.buildContact("1.0.0", currentContact.getPhone(), currentContact.getFax(),
                    currentContact.getEmail(), currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                    currentContact.getZipCode(), currentContact.getCountry(), currentContact.getHoursOfService(), currentContact.getContactInstructions());

            final AbstractResponsiblePartySubset responsible = OWSXmlFactory.buildResponsiblePartySubset("1.0.0", currentContact.getFullname(), currentContact.getPosition(), contact, null);


            AbstractOnlineResourceType orgUrl = null;
            if (currentContact.getUrl() != null) {
                orgUrl = OWSXmlFactory.buildOnlineResource("1.0.0", currentContact.getUrl());
            }
            servProv = OWSXmlFactory.buildServiceProvider("1.0.0", currentContact.getOrganisation(), orgUrl, responsible);
        } else {
            servProv = OWSXmlFactory.buildServiceProvider("1.0.0", "", null, null);
        }

        // Create capabilities base.
        return CswXmlFactory.createCapabilities(version, servIdent, servProv, null, null, null);
    }

    private CSWConstants() {}

}
