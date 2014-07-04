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

package org.constellation.wfs.ws;

import net.jcip.annotations.Immutable;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Details;
import org.geotoolkit.ogc.xml.Conformance;
import org.geotoolkit.ogc.xml.FilterXmlFactory;
import org.geotoolkit.ogc.xml.v110.ArithmeticOperatorsType;
import org.geotoolkit.ogc.xml.v110.ComparisonOperatorType;
import org.geotoolkit.ogc.xml.v110.IdCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.ScalarCapabilitiesType;
import org.geotoolkit.ogc.xml.v200.FilterType;
import org.geotoolkit.ogc.xml.v200.LiteralType;
import org.geotoolkit.ogc.xml.v200.PropertyIsEqualToType;
import org.geotoolkit.ogc.xml.v200.ResourceIdentifierType;
import org.geotoolkit.ows.xml.AbstractContact;
import org.geotoolkit.ows.xml.AbstractDCP;
import org.geotoolkit.ows.xml.AbstractDomain;
import org.geotoolkit.ows.xml.AbstractOnlineResourceType;
import org.geotoolkit.ows.xml.AbstractOperation;
import org.geotoolkit.ows.xml.AbstractOperationsMetadata;
import org.geotoolkit.ows.xml.AbstractResponsiblePartySubset;
import org.geotoolkit.ows.xml.AbstractServiceIdentification;
import org.geotoolkit.ows.xml.AbstractServiceProvider;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.wfs.xml.WFSCapabilities;
import org.geotoolkit.wfs.xml.WFSXmlFactory;
import org.geotoolkit.wfs.xml.v200.ObjectFactory;
import org.geotoolkit.wfs.xml.v200.ParameterExpressionType;
import org.geotoolkit.wfs.xml.v200.QueryExpressionTextType;
import org.geotoolkit.wfs.xml.v200.QueryType;
import org.geotoolkit.wfs.xml.v200.StoredQueryDescriptionType;
import org.opengis.filter.capability.ComparisonOperators;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.filter.capability.GeometryOperand;
import org.opengis.filter.capability.Operator;
import org.opengis.filter.capability.SpatialCapabilities;
import org.opengis.filter.capability.SpatialOperator;
import org.opengis.filter.capability.SpatialOperators;

import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 *  WFS Constants
 *
 * @author Guilhem Legal (Geomatys)
 */
@Immutable
public final class WFSConstants {

    private WFSConstants() {}

    public static final String HANDLE    = "handle";
    public static final String NAMESPACE = "namespace";
    public static final String FILTER    = "filter";

    public static final String STR_GETCAPABILITIES         = "GetCapabilities";
    public static final String STR_DESCRIBEFEATURETYPE     = "DescribeFeatureType";
    public static final String STR_GETFEATURE              = "GetFeature";
    public static final String STR_GETGMLOBJECT            = "getGMLObject";
    public static final String STR_LOCKFEATURE             = "lockFeature";
    public static final String STR_TRANSACTION             = "Transaction";
    public static final String STR_DESCRIBE_STORED_QUERIES = "DescribeStoredQueries";
    public static final String STR_LIST_STORED_QUERIES     = "ListStoredQueries";
    public static final String STR_GET_PROPERTY_VALUE      = "GetPropertyValue";
    public static final String STR_CREATE_STORED_QUERY     = "CreateStoredQuery";
    public static final String STR_DROP_STORED_QUERY       = "DropStoredQuery";

    public static final String UNKNOW_TYPENAME= "The specified TypeNames does not exist:";

    /**
     * The Mime type for describe feature GML 3.1.1
     */
    public final static MediaType GML_3_1_1 = new MediaType("text", "xml; subtype=gml/3.1.1");

    public final static MediaType GML_3_2_1 = new MediaType("application", "gml+xml; version=3.2");

    public static final FilterCapabilities FILTER_CAPABILITIES_V110;
    static {
        final GeometryOperand[] geometryOperands = new GeometryOperand[4];
        geometryOperands[0] = GeometryOperand.Envelope;
        geometryOperands[1] = GeometryOperand.Point;
        geometryOperands[2] = GeometryOperand.LineString;
        geometryOperands[3] = GeometryOperand.Polygon;

        final SpatialOperator[] operatorList = new SpatialOperator[10];
        operatorList[0] = FilterXmlFactory.buildSpatialOperator("1.1.0", "DISJOINT", null);
        operatorList[1] = FilterXmlFactory.buildSpatialOperator("1.1.0", "EQUALS", null);
        operatorList[2] = FilterXmlFactory.buildSpatialOperator("1.1.0", "D_WITHIN", null);
        operatorList[3] = FilterXmlFactory.buildSpatialOperator("1.1.0", "BEYOND", null);
        operatorList[4] = FilterXmlFactory.buildSpatialOperator("1.1.0", "INTERSECTS", null);
        operatorList[5] = FilterXmlFactory.buildSpatialOperator("1.1.0", "TOUCHES", null);
        operatorList[6] = FilterXmlFactory.buildSpatialOperator("1.1.0", "CROSSES", null);
        operatorList[7] = FilterXmlFactory.buildSpatialOperator("1.1.0", "CONTAINS", null);
        operatorList[8] = FilterXmlFactory.buildSpatialOperator("1.1.0", "OVERLAPS", null);
        operatorList[9] = FilterXmlFactory.buildSpatialOperator("1.1.0", "BBOX", null);

        final SpatialOperators spatialOperators = FilterXmlFactory.buildSpatialOperators("1.1.0", operatorList);
        final SpatialCapabilities spatialCapabilties = FilterXmlFactory.buildSpatialCapabilities("1.1.0", geometryOperands, spatialOperators);

        final ArithmeticOperatorsType arithmetic = new ArithmeticOperatorsType(true, null);
        final Operator[] compaOperatorList = new Operator[9];
        compaOperatorList[0] = ComparisonOperatorType.BETWEEN;
        compaOperatorList[1] = ComparisonOperatorType.EQUAL_TO;
        compaOperatorList[2] = ComparisonOperatorType.GREATER_THAN;
        compaOperatorList[3] = ComparisonOperatorType.GREATER_THAN_EQUAL_TO;
        compaOperatorList[4] = ComparisonOperatorType.LESS_THAN;
        compaOperatorList[5] = ComparisonOperatorType.LESS_THAN_EQUAL_TO;
        compaOperatorList[6] = ComparisonOperatorType.LIKE;
        compaOperatorList[7] = ComparisonOperatorType.NOT_EQUAL_TO;
        compaOperatorList[8] = ComparisonOperatorType.NULL_CHECK;

        final ComparisonOperators comparisons = FilterXmlFactory.buildComparisonOperators("1.1.0", compaOperatorList);
        final ScalarCapabilitiesType scalarCapabilities = new ScalarCapabilitiesType(comparisons, arithmetic, true);


        final IdCapabilitiesType idCapabilities = new IdCapabilitiesType(true, true);
        FILTER_CAPABILITIES_V110 = FilterXmlFactory.buildFilterCapabilities("1.1.0", scalarCapabilities, spatialCapabilties, idCapabilities, null, null);

    }

    public static final FilterCapabilities FILTER_CAPABILITIES_V200;
    static {
        final GeometryOperand[] geometryOperands = new GeometryOperand[4];
        geometryOperands[0] = GeometryOperand.Envelope;
        geometryOperands[1] = GeometryOperand.Point;
        geometryOperands[2] = GeometryOperand.LineString;
        geometryOperands[3] = GeometryOperand.Polygon;

        final SpatialOperator[] operatorList = new SpatialOperator[10];
        operatorList[0] = FilterXmlFactory.buildSpatialOperator("2.0.0", "Disjoint", null);
        operatorList[1] = FilterXmlFactory.buildSpatialOperator("2.0.0", "Equals", null);
        operatorList[2] = FilterXmlFactory.buildSpatialOperator("2.0.0", "DWithin", null);
        operatorList[3] = FilterXmlFactory.buildSpatialOperator("2.0.0", "Beyond", null);
        operatorList[4] = FilterXmlFactory.buildSpatialOperator("2.0.0", "Intersects", null);
        operatorList[5] = FilterXmlFactory.buildSpatialOperator("2.0.0", "Touches", null);
        operatorList[6] = FilterXmlFactory.buildSpatialOperator("2.0.0", "Crosses", null);
        operatorList[7] = FilterXmlFactory.buildSpatialOperator("2.0.0", "Contains", null);
        operatorList[8] = FilterXmlFactory.buildSpatialOperator("2.0.0", "Overlaps", null);
        operatorList[9] = FilterXmlFactory.buildSpatialOperator("2.0.0", "BBOX", null);

        final SpatialOperators spatialOperators = FilterXmlFactory.buildSpatialOperators("2.0.0", operatorList);
        final SpatialCapabilities spatialCapabilties = FilterXmlFactory.buildSpatialCapabilities("2.0.0", geometryOperands, spatialOperators);

        final Operator[] compaOperatorList = new Operator[9];
        compaOperatorList[0] = new org.geotoolkit.ogc.xml.v200.ComparisonOperatorType("PropertyIsBetween");
        compaOperatorList[1] = new org.geotoolkit.ogc.xml.v200.ComparisonOperatorType("PropertyIsEqualTo");
        compaOperatorList[2] = new org.geotoolkit.ogc.xml.v200.ComparisonOperatorType("PropertyIsGreaterThan");
        compaOperatorList[3] = new org.geotoolkit.ogc.xml.v200.ComparisonOperatorType("PropertyIsGreaterThanOrEqualTo");
        compaOperatorList[4] = new org.geotoolkit.ogc.xml.v200.ComparisonOperatorType("PropertyIsLessThan");
        compaOperatorList[5] = new org.geotoolkit.ogc.xml.v200.ComparisonOperatorType("PropertyIsLessThanOrEqualTo");
        compaOperatorList[6] = new org.geotoolkit.ogc.xml.v200.ComparisonOperatorType("PropertyIsLike");
        compaOperatorList[7] = new org.geotoolkit.ogc.xml.v200.ComparisonOperatorType("PropertyIsNotEqualTo");
        compaOperatorList[8] = new org.geotoolkit.ogc.xml.v200.ComparisonOperatorType("PropertyIsNull");

        final ComparisonOperators comparisons = FilterXmlFactory.buildComparisonOperators("2.0.0", compaOperatorList);
        final org.geotoolkit.ogc.xml.v200.ScalarCapabilitiesType scalarCapabilities = new org.geotoolkit.ogc.xml.v200.ScalarCapabilitiesType(comparisons, true);

        final ResourceIdentifierType iden = new ResourceIdentifierType(new QName("http://www.opengis.net/fes/2.0", "ResourceId"));
        final org.geotoolkit.ogc.xml.v200.IdCapabilitiesType idCapabilities = new org.geotoolkit.ogc.xml.v200.IdCapabilitiesType(iden);

        final List<AbstractDomain> constraints = new ArrayList<>();
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsQuery", "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsAdHocQuery",         "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsFunctions",          "FALSE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsMinStandardFilter",  "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsStandardFilter",     "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsMinSpatialFilter",   "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsSpatialFilter",      "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsMinTemporalFilter",  "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsTemporalFilter",     "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsVersionNav",         "FALSE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsSorting",            "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsExtendedOperators",  "FALSE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsResourceId",         "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsMinimumXPath",       "TRUE"));
        final Conformance conformance = FilterXmlFactory.buildConformance("2.0.0", constraints);
        FILTER_CAPABILITIES_V200 = FilterXmlFactory.buildFilterCapabilities("2.0.0", scalarCapabilities, spatialCapabilties, idCapabilities, null, conformance);
    }

    public static final AbstractOperationsMetadata OPERATIONS_METADATA_V110;
    static {
        final List<AbstractDCP> dcps = new ArrayList<>();
        dcps.add(WFSXmlFactory.buildDCP("1.1.0", "someURL", "someURL"));

        final List<AbstractDCP> dcps2 = new ArrayList<>();
        dcps2.add(WFSXmlFactory.buildDCP("1.1.0", null, "someURL"));

        final List<AbstractOperation> operations = new ArrayList<>();

        final List<AbstractDomain> gcParameters = new ArrayList<>();
        gcParameters.add(WFSXmlFactory.buildDomain("1.1.0", "AcceptVersions", Arrays.asList("1.1.0")));
        gcParameters.add(WFSXmlFactory.buildDomain("1.1.0", "AcceptFormats",  Arrays.asList("text/xml")));
        gcParameters.add(WFSXmlFactory.buildDomain("1.1.0", "Service",        Arrays.asList("WFS")));
        AbstractOperation getCapabilities = WFSXmlFactory.buildOperation("1.1.0", dcps, gcParameters, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<AbstractDomain> dfParameters = new ArrayList<>();
        dfParameters.add(WFSXmlFactory.buildDomain("1.1.0", "outputFormat", Arrays.asList("text/xml; subtype=gml/3.1.1")));
        dfParameters.add(WFSXmlFactory.buildDomain("1.1.0", "Service",      Arrays.asList("WFS")));
        dfParameters.add(WFSXmlFactory.buildDomain("1.1.0", "Version",      Arrays.asList("1.1.0")));
        AbstractOperation describeFeatureType = WFSXmlFactory.buildOperation("1.1.0", dcps, dfParameters, null, "DescribeFeatureType");
        operations.add(describeFeatureType);

        final List<AbstractDomain> gfParameters = new ArrayList<>();
        gfParameters.add(WFSXmlFactory.buildDomain("1.1.0", "resultType",   Arrays.asList("results","hits")));
        gfParameters.add(WFSXmlFactory.buildDomain("1.1.0", "outputFormat", Arrays.asList("text/xml; subtype=gml/3.1.1")));
        gfParameters.add(WFSXmlFactory.buildDomain("1.1.0", "Service",      Arrays.asList("WFS")));
        gfParameters.add(WFSXmlFactory.buildDomain("1.1.0", "Version",      Arrays.asList("1.1.0")));

        final List<AbstractDomain> gfConstraints = new ArrayList<>();
        gfConstraints.add(WFSXmlFactory.buildDomain("1.1.0", "LocalTraverseXLinkScope", Arrays.asList("2"))); // ???
        AbstractOperation getFeature = WFSXmlFactory.buildOperation("1.1.0", dcps, gfParameters, gfConstraints, "GetFeature");
        operations.add(getFeature);

        final List<AbstractDomain> tParameters = new ArrayList<>();
        tParameters.add(WFSXmlFactory.buildDomain("1.1.0", "inputFormat",   Arrays.asList("text/xml; subtype=gml/3.1.1")));
        tParameters.add(WFSXmlFactory.buildDomain("1.1.0", "idgen",         Arrays.asList("GenerateNew","UseExisting","ReplaceDuplicate")));
        tParameters.add(WFSXmlFactory.buildDomain("1.1.0", "releaseAction", Arrays.asList("ALL", "SOME")));
        tParameters.add(WFSXmlFactory.buildDomain("1.1.0", "Service",       Arrays.asList("WFS")));
        tParameters.add(WFSXmlFactory.buildDomain("1.1.0", "Version",       Arrays.asList("1.1.0")));
        AbstractOperation Transaction = WFSXmlFactory.buildOperation("1.1.0", dcps2, tParameters, null, "Transaction");
        operations.add(Transaction);


        OPERATIONS_METADATA_V110 = OWSXmlFactory.buildOperationsMetadata("1.0.0", operations, null, null, null);
    }

    public static final AbstractOperationsMetadata OPERATIONS_METADATA_V200;
    static {
        final List<AbstractDCP> dcps = new ArrayList<>();
        dcps.add(WFSXmlFactory.buildDCP("2.0.0", "somURL", "someURL"));

        final List<AbstractDCP> dcps2 = new ArrayList<>();
        dcps2.add((WFSXmlFactory.buildDCP("2.0.0", null, "someURL")));

        final List<AbstractOperation> operations = new ArrayList<>();

        final AbstractDomain serviceDomain = WFSXmlFactory.buildDomain("2.0.0", "Service", Arrays.asList("WFS"));
        final AbstractDomain versionDomain = WFSXmlFactory.buildDomain("2.0.0", "Version", Arrays.asList("2.0.0"));

        final List<AbstractDomain> gcParameters = new ArrayList<>();
        gcParameters.add(WFSXmlFactory.buildDomain("2.0.0", "AcceptVersions", Arrays.asList("2.0.0", "1.1.0")));
        gcParameters.add(WFSXmlFactory.buildDomain("2.0.0", "AcceptFormats",  Arrays.asList("text/xml")));
        gcParameters.add(serviceDomain);
        AbstractOperation getCapabilities = WFSXmlFactory.buildOperation("2.0.0", dcps, gcParameters, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<AbstractDomain> dfParameters = new ArrayList<>();
        dfParameters.add(WFSXmlFactory.buildDomain("2.0.0", "outputFormat", Arrays.asList("application/gml+xml; version=3.2")));
        dfParameters.add(serviceDomain);
        dfParameters.add(versionDomain);
        AbstractOperation describeFeatureType = WFSXmlFactory.buildOperation("2.0.0", dcps, dfParameters, null, "DescribeFeatureType");
        operations.add(describeFeatureType);

        final List<AbstractDomain> gfParameters = new ArrayList<>();
        gfParameters.add(WFSXmlFactory.buildDomain("2.0.0", "resultType",   Arrays.asList("results","hits")));
        gfParameters.add(WFSXmlFactory.buildDomain("2.0.0", "outputFormat", Arrays.asList("application/gml+xml; version=3.2")));
        gfParameters.add(serviceDomain);
        gfParameters.add(versionDomain);

        final List<AbstractDomain> gfConstraints = new ArrayList<>();
        gfConstraints.add((WFSXmlFactory.buildDomain("2.0.0", "LocalTraverseXLinkScope", Arrays.asList("2")))); // ???
        AbstractOperation getFeature =  WFSXmlFactory.buildOperation("2.0.0", dcps, gfParameters, gfConstraints, "GetFeature");
        operations.add(getFeature);

        final List<AbstractDomain> tParameters = new ArrayList<>();
        tParameters.add(WFSXmlFactory.buildDomain("2.0.0", "inputFormat",   Arrays.asList("application/gml+xml; version=3.2")));
        tParameters.add(WFSXmlFactory.buildDomain("2.0.0", "idgen",         Arrays.asList("GenerateNew","UseExisting","ReplaceDuplicate")));
        tParameters.add(WFSXmlFactory.buildDomain("2.0.0", "releaseAction", Arrays.asList("ALL", "SOME")));
        tParameters.add(serviceDomain);
        tParameters.add(versionDomain);
        AbstractOperation Transaction =  WFSXmlFactory.buildOperation("2.0.0", dcps2, tParameters, null, "Transaction");
        operations.add(Transaction);

        final List<AbstractDomain> lsqParameters = new ArrayList<>();
        lsqParameters.add(serviceDomain);
        lsqParameters.add(versionDomain);
        AbstractOperation listStoredQueries =  WFSXmlFactory.buildOperation("2.0.0", dcps, lsqParameters, null, "ListStoredQueries");
        operations.add(listStoredQueries);

        final List<AbstractDomain> dsqParameters = new ArrayList<>();
        dsqParameters.add(serviceDomain);
        dsqParameters.add(versionDomain);
        AbstractOperation describeStoredQueries =  WFSXmlFactory.buildOperation("2.0.0", dcps, dsqParameters, null, "DescribeStoredQueries");
        operations.add(describeStoredQueries);

        final List<AbstractDomain> gpvParameters = new ArrayList<>();
        gpvParameters.add(serviceDomain);
        gpvParameters.add(versionDomain);
        AbstractOperation getPropertyValue =  WFSXmlFactory.buildOperation("2.0.0", dcps, gpvParameters, null, "GetPropertyValue");
        operations.add(getPropertyValue);

        final List<AbstractDomain> csqParameters = new ArrayList<>();
        csqParameters.add(serviceDomain);
        csqParameters.add(versionDomain);
        AbstractOperation createStoredQuery =  WFSXmlFactory.buildOperation("2.0.0", dcps, csqParameters, null, "CreateStoredQuery");
        operations.add(createStoredQuery);

        final List<AbstractDomain> dsqParameters2 = new ArrayList<>();
        dsqParameters2.add(serviceDomain);
        dsqParameters2.add(versionDomain);
        AbstractOperation dropStoredQuery =  WFSXmlFactory.buildOperation("2.0.0", dcps, dsqParameters2, null, "DropStoredQuery");
        operations.add(dropStoredQuery);

        final List<AbstractDomain> parameters = new ArrayList<>();
        parameters.add(WFSXmlFactory.buildDomain("2.0.0", "version", Arrays.asList("2.0.0")));

        final List<AbstractDomain> constraints = new ArrayList<>();
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsSimpleWFS",          "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsBasicWFS",           "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsTransactionalWFS",   "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsLockingWFS",         "FALSE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "KVPEncoding",                  "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "XMLEncoding",                  "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "SOAPEncoding",                 "FALSE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsInheritance",        "FALSE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsRemoteResolve",      "FALSE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsResultPaging",       "TRUE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsStandardJoins",      "FALSE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsSpatialJoins",       "FALSE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsTemporalJoins",      "FALSE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ImplementsFeatureVersioning",  "FALSE"));
        constraints.add(WFSXmlFactory.buildDomainNoValues("2.0.0", "ManageStoredQueries",          "TRUE"));

        OPERATIONS_METADATA_V200 = OWSXmlFactory.buildOperationsMetadata("1.1.0", operations, parameters, constraints, null);
    }

    /**
     * this static member is not yet useable because of clone issues for the featureType names.
     * build your own {@linkplain StoredQueryDescriptionType} with IDENTIFIER_FILTER
     */
    public static final StoredQueryDescriptionType IDENTIFIER_STORED_QUERY;
    public static final FilterType IDENTIFIER_FILTER;
    public static final ParameterExpressionType IDENTIFIER_PARAM;
    public static final ParameterExpressionType TYPE_PARAM;
    static {
        TYPE_PARAM = new ParameterExpressionType("typeName", "type Parameter", "A parameter on the identifier of the featureType", new QName("http://www.w3.org/2001/XMLSchema", "QName", "xs"));
        IDENTIFIER_PARAM = new ParameterExpressionType("id", "id Parameter", "A parameter on the id of the feature", new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
        final PropertyIsEqualToType pis = new PropertyIsEqualToType(new LiteralType("$id"), "@id", true);
        IDENTIFIER_FILTER = new FilterType(pis);
        final QueryType query = new QueryType(IDENTIFIER_FILTER, null, "2.0.0");
        final QueryExpressionTextType queryEx = new QueryExpressionTextType("urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression", null, null);
        final ObjectFactory factory = new ObjectFactory();
        queryEx.getContent().add(factory.createQuery(query));
        IDENTIFIER_STORED_QUERY = new StoredQueryDescriptionType("urn:ogc:def:storedQuery:OGC-WFS::GetFeatureById", "Identifier query" , "filter on feature identifier", IDENTIFIER_PARAM, queryEx);
    }

    /**
     * Generates the base capabilities for a WFS from the service metadata.
     *
     * @param metadata the service metadata
     * @return the service base capabilities
     */
    public static WFSCapabilities createCapabilities(final String version, final Details metadata) {
        ensureNonNull("metadata", metadata);
        ensureNonNull("version",  version);

        final Contact currentContact = metadata.getServiceContact();
        final AccessConstraint constraint = metadata.getServiceConstraints();

        final AbstractServiceIdentification servIdent;
        if (constraint != null) {
            servIdent = WFSXmlFactory.buildServiceIdentification(version, metadata.getName(), metadata.getDescription(),
                                                                 metadata.getKeywords(), "WFS", metadata.getVersions(),
                                                                 constraint.getFees(), Arrays.asList(constraint.getAccessConstraint()));
        } else {
            servIdent = WFSXmlFactory.buildServiceIdentification(version, metadata.getName(), metadata.getDescription(),
                                                                 metadata.getKeywords(), "WFS", metadata.getVersions(),
                                                                 null, new ArrayList<String>());
        }

        // Create provider part.
        final AbstractServiceProvider servProv;
        if (currentContact != null) {
            final AbstractContact contact = WFSXmlFactory.buildContact(version, currentContact.getPhone(), currentContact.getFax(),
                    currentContact.getEmail(), currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                    currentContact.getZipCode(), currentContact.getCountry(), currentContact.getHoursOfService(), currentContact.getContactInstructions());

            final AbstractResponsiblePartySubset responsible = WFSXmlFactory.buildResponsiblePartySubset(version, currentContact.getFullname(), currentContact.getPosition(), contact, null);


            AbstractOnlineResourceType orgUrl = null;
            if (currentContact.getUrl() != null) {
                orgUrl = WFSXmlFactory.buildOnlineResource(version, currentContact.getUrl());
            }

            servProv = WFSXmlFactory.buildServiceProvider(version, currentContact.getOrganisation(), orgUrl, responsible);
        } else {
            servProv = WFSXmlFactory.buildServiceProvider(version, "", null, null);
        }

        // Create capabilities base.
        return WFSXmlFactory.buildWFSCapabilities(version, servIdent, servProv, null, null, null);
    }
}
