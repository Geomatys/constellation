/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
import java.util.Arrays;
import java.util.List;
import net.jcip.annotations.Immutable;
import org.geotoolkit.ogc.xml.v110.ComparisonOperatorType;
import org.geotoolkit.ogc.xml.v110.ComparisonOperatorsType;
import org.geotoolkit.ogc.xml.v110.FilterCapabilities;
import org.geotoolkit.ogc.xml.v110.GeometryOperandsType;
import org.geotoolkit.ogc.xml.v110.IdCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.ScalarCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.SpatialCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.SpatialOperatorType;
import org.geotoolkit.ogc.xml.v110.SpatialOperatorsType;
import org.geotoolkit.ows.xml.v100.DCP;
import org.geotoolkit.ows.xml.v100.DomainType;
import org.geotoolkit.ows.xml.v100.HTTP;
import org.geotoolkit.ows.xml.v100.Operation;
import org.geotoolkit.ows.xml.v100.OperationsMetadata;
import org.geotoolkit.ows.xml.v100.RequestMethodType;
import org.opengis.filter.capability.Operator;
import org.opengis.filter.capability.SpatialOperator;

import static org.geotoolkit.gml.xml.v311.ObjectFactory.*;


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
     * Error message
     */

    public static final String NOT_EXIST = " does not exist";

    public static final String MALFORMED = " is malformed";

    public static final OperationsMetadata OPERATIONS_METADATA;
    static {
        final List<DCP> getAndPost = new ArrayList<DCP>();
        getAndPost.add(new DCP(new HTTP(new RequestMethodType("somURL"), new RequestMethodType("someURL"))));

        final List<DCP> onlyPost = new ArrayList<DCP>();
        onlyPost.add(new DCP(new HTTP(null, new RequestMethodType("someURL"))));

        final List<Operation> operations = new ArrayList<Operation>();

        final List<DomainType> gcParameters = new ArrayList<DomainType>();
        gcParameters.add(new DomainType("sections", Arrays.asList("All", "ServiceIdentification", "ServiceProvider", "OperationsMetadata", "Filter_Capabilities")));
        gcParameters.add(new DomainType("Version", "2.0.2"));
        gcParameters.add(new DomainType("Service", "CSW"));
        
        final List<DomainType> gcConstraints = new ArrayList<DomainType>();
        gcConstraints.add(new DomainType("PostEncoding", "XML"));
        
        final Operation getCapabilities = new Operation(getAndPost, gcParameters, gcConstraints, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<DomainType> grParameters = new ArrayList<DomainType>();
        grParameters.add(new DomainType("Version", "2.0.2"));
        grParameters.add(new DomainType("Service", "CSW"));
        grParameters.add(new DomainType("TypeNames", Arrays.asList("gmd:MD_Metadata", "csw:Record")));
        grParameters.add(new DomainType("outputFormat", Arrays.asList("text/xml", "application/xml")));
        grParameters.add(new DomainType("outputSchema", Arrays.asList("http://www.opengis.net/cat/csw/2.0.2", "http://www.isotc211.org/2005/gmd")));
        grParameters.add(new DomainType("resultType", Arrays.asList("hits", "results", "validate")));
        grParameters.add(new DomainType("ElementSetName", Arrays.asList("brief", "summary", "full")));
        grParameters.add(new DomainType("CONSTRAINTLANGUAGE", Arrays.asList("Filter", "CQL")));
        
        final List<DomainType> grConstraints = new ArrayList<DomainType>();
        
        final List<String> supportedISOQueryable = new ArrayList<String>();
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
        
        grConstraints.add(new DomainType("SupportedISOQueryables", supportedISOQueryable));
        grConstraints.add(new DomainType("AdditionalQueryables", "HierarchyLevelName"));
        grConstraints.add(new DomainType("PostEncoding", "XML"));
        
        
        final Operation getRecords = new Operation(getAndPost, grParameters, grConstraints, null, "GetRecords");
        operations.add(getRecords);
        
        final List<DomainType> grbParameters = new ArrayList<DomainType>();
        grbParameters.add(new DomainType("Version", "2.0.2"));
        grbParameters.add(new DomainType("Service", "CSW"));
        grbParameters.add(new DomainType("ElementSetName", Arrays.asList("brief", "summary", "full")));
        grbParameters.add(new DomainType("outputSchema", Arrays.asList("http://www.opengis.net/cat/csw/2.0.2", "http://www.isotc211.org/2005/gmd")));
        grbParameters.add(new DomainType("outputFormat", Arrays.asList("text/xml", "application/xml")));
        
        final List<DomainType> grbConstraints = new ArrayList<DomainType>();
        grbConstraints.add(new DomainType("PostEncoding", "XML"));
        
        final Operation getRecordById = new Operation(getAndPost, grbParameters, grbConstraints, null, "GetRecordById");
        operations.add(getRecordById);
        
        final List<DomainType> drParameters = new ArrayList<DomainType>();
        drParameters.add(new DomainType("Version", "2.0.2"));
        drParameters.add(new DomainType("Service", "CSW"));
        drParameters.add(new DomainType("TypeName", Arrays.asList("gmd:MD_Metadata", "csw:Record")));
        drParameters.add(new DomainType("SchemaLanguage", Arrays.asList("http://www.w3.org/XML/Schema", "XMLSCHEMA")));
        drParameters.add(new DomainType("outputFormat", Arrays.asList("text/xml", "application/xml")));
        
        final List<DomainType> drConstraints = new ArrayList<DomainType>();
        drConstraints.add(new DomainType("PostEncoding", "XML"));
        
        final Operation describeRecord = new Operation(getAndPost, drParameters, drConstraints, null, "DescribeRecord");
        operations.add(describeRecord);

        
        final List<DomainType> gdParameters = new ArrayList<DomainType>();
        gdParameters.add(new DomainType("Version", "2.0.2"));
        gdParameters.add(new DomainType("Service", "CSW"));
        
        final List<DomainType> gdConstraints = new ArrayList<DomainType>();
        gdConstraints.add(new DomainType("PostEncoding", "XML"));
        
        final Operation getDomain = new Operation(getAndPost, gdParameters, gdConstraints, null, "GetDomain");
        operations.add(getDomain);
        
        final List<DomainType> tParameters = new ArrayList<DomainType>();
        tParameters.add(new DomainType("Version", "2.0.2"));
        tParameters.add(new DomainType("Service", "CSW"));
        tParameters.add(new DomainType("ResourceType", "toUpdate"));
        
        final List<DomainType> tConstraints = new ArrayList<DomainType>();
        tConstraints.add(new DomainType("PostEncoding", "XML"));
        
        final Operation transaction = new Operation(onlyPost, tParameters, tConstraints, null, "Transaction");
        operations.add(transaction);
        
        final List<DomainType> hParameters = new ArrayList<DomainType>();
        hParameters.add(new DomainType("Version", "2.0.2"));
        hParameters.add(new DomainType("Service", "CSW"));
        hParameters.add(new DomainType("ResourceType", "toUpdate"));
        
        final List<DomainType> hConstraints = new ArrayList<DomainType>();
        hConstraints.add(new DomainType("PostEncoding", "XML"));
        
        final Operation harvest = new Operation(onlyPost, hParameters, hConstraints, null, "Harvest");
        operations.add(harvest);
        
        final List<DomainType> parameters = new ArrayList<DomainType>();
        parameters.add(new DomainType("service", "http://www.opengis.net/cat/csw/2.0.2"));
        parameters.add(new DomainType("version", "2.0.2"));

        OPERATIONS_METADATA = new OperationsMetadata(operations, parameters, null, null);
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

    private CSWConstants() {}

}
