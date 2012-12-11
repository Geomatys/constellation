/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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

package org.constellation.wfs.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import net.jcip.annotations.Immutable;
import org.geotoolkit.ogc.xml.v110.ArithmeticOperatorsType;
import org.geotoolkit.ogc.xml.v110.ComparisonOperatorType;
import org.geotoolkit.ogc.xml.v110.ComparisonOperatorsType;
import org.geotoolkit.ogc.xml.v110.FilterCapabilities;
import org.geotoolkit.ogc.xml.v110.GeometryOperandsType;
import org.geotoolkit.ogc.xml.v110.IdCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.ScalarCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.SpatialCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.SpatialOperatorType;
import org.geotoolkit.ogc.xml.v110.SpatialOperatorsType;
import org.geotoolkit.ogc.xml.v200.ConformanceType;
import org.geotoolkit.ogc.xml.v200.FilterType;
import org.geotoolkit.ogc.xml.v200.LiteralType;
import org.geotoolkit.ogc.xml.v200.PropertyIsEqualToType;
import org.geotoolkit.ogc.xml.v200.ResourceIdentifierType;
import org.geotoolkit.ows.xml.AbstractDomain;
import org.geotoolkit.ows.xml.AbstractOperation;
import org.geotoolkit.ows.xml.AbstractOperationsMetadata;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.ows.xml.v100.DCP;
import org.geotoolkit.ows.xml.v100.DomainType;
import org.geotoolkit.ows.xml.v100.HTTP;
import org.geotoolkit.ows.xml.v100.Operation;
import org.geotoolkit.ows.xml.v100.RequestMethodType;
import org.geotoolkit.ows.xml.v110.AllowedValues;
import org.geotoolkit.ows.xml.v110.NoValues;
import org.geotoolkit.ows.xml.v110.ValueType;
import org.geotoolkit.wfs.xml.v200.ObjectFactory;
import org.geotoolkit.wfs.xml.v200.ParameterExpressionType;
import org.geotoolkit.wfs.xml.v200.QueryExpressionTextType;
import org.geotoolkit.wfs.xml.v200.QueryType;
import org.geotoolkit.wfs.xml.v200.StoredQueryDescriptionType;
import org.opengis.filter.capability.Operator;
import org.opengis.filter.capability.SpatialOperator;

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
        final List<QName> operandList = new ArrayList<QName>();
        operandList.add(new QName("http://www.opengis.net/gml", "Envelope"));
        operandList.add(new QName("http://www.opengis.net/gml", "Point"));
        operandList.add(new QName("http://www.opengis.net/gml", "LineString"));
        operandList.add(new QName("http://www.opengis.net/gml", "Polygon"));
        final GeometryOperandsType operands = new GeometryOperandsType(operandList);
        final SpatialOperator[] operatorList = new SpatialOperator[10];
        operatorList[0] = new SpatialOperatorType("DISJOINT", null);
        operatorList[1] = new SpatialOperatorType("EQUALS", null);
        operatorList[2] = new SpatialOperatorType("D_WITHIN", null);
        operatorList[3] = new SpatialOperatorType("BEYOND", null);
        operatorList[4] = new SpatialOperatorType("INTERSECTS", null);
        operatorList[5] = new SpatialOperatorType("TOUCHES", null);
        operatorList[6] = new SpatialOperatorType("CROSSES", null);
        operatorList[7] = new SpatialOperatorType("CONTAINS", null);
        operatorList[8] = new SpatialOperatorType("OVERLAPS", null);
        operatorList[9] = new SpatialOperatorType("BBOX", null);

        final SpatialOperatorsType spatialOperators = new SpatialOperatorsType(operatorList);
        final SpatialCapabilitiesType spatialCapabilties = new SpatialCapabilitiesType(operands, spatialOperators);

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

        final ComparisonOperatorsType comparisons = new ComparisonOperatorsType(compaOperatorList);
        final ScalarCapabilitiesType scalarCapabilities = new ScalarCapabilitiesType(comparisons, arithmetic, true);


        final IdCapabilitiesType idCapabilities = new IdCapabilitiesType(true, true);
        FILTER_CAPABILITIES_V110 = new FilterCapabilities(scalarCapabilities, spatialCapabilties, idCapabilities);

    }

    public static final org.geotoolkit.ogc.xml.v200.FilterCapabilities FILTER_CAPABILITIES_V200;
    static {
        final List<QName> operandList = new ArrayList<QName>();
        operandList.add(new QName("http://www.opengis.net/gml/3.2", "Envelope"));
        operandList.add(new QName("http://www.opengis.net/gml/3.2", "Point"));
        operandList.add(new QName("http://www.opengis.net/gml/3.2", "LineString"));
        operandList.add(new QName("http://www.opengis.net/gml/3.2", "Polygon"));
        final org.geotoolkit.ogc.xml.v200.GeometryOperandsType operands = new org.geotoolkit.ogc.xml.v200.GeometryOperandsType(operandList);
        final SpatialOperator[] operatorList = new SpatialOperator[10];
        operatorList[0] = new org.geotoolkit.ogc.xml.v200.SpatialOperatorType("Disjoint", null);
        operatorList[1] = new org.geotoolkit.ogc.xml.v200.SpatialOperatorType("Equals", null);
        operatorList[2] = new org.geotoolkit.ogc.xml.v200.SpatialOperatorType("DWithin", null);
        operatorList[3] = new org.geotoolkit.ogc.xml.v200.SpatialOperatorType("Beyond", null);
        operatorList[4] = new org.geotoolkit.ogc.xml.v200.SpatialOperatorType("Intersects", null);
        operatorList[5] = new org.geotoolkit.ogc.xml.v200.SpatialOperatorType("Touches", null);
        operatorList[6] = new org.geotoolkit.ogc.xml.v200.SpatialOperatorType("Crosses", null);
        operatorList[7] = new org.geotoolkit.ogc.xml.v200.SpatialOperatorType("Contains", null);
        operatorList[8] = new org.geotoolkit.ogc.xml.v200.SpatialOperatorType("Overlaps", null);
        operatorList[9] = new org.geotoolkit.ogc.xml.v200.SpatialOperatorType("BBOX", null);

        final org.geotoolkit.ogc.xml.v200.SpatialOperatorsType spatialOperators = new org.geotoolkit.ogc.xml.v200.SpatialOperatorsType(operatorList);
        final org.geotoolkit.ogc.xml.v200.SpatialCapabilitiesType spatialCapabilties = new org.geotoolkit.ogc.xml.v200.SpatialCapabilitiesType(operands, spatialOperators);

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

        final org.geotoolkit.ogc.xml.v200.ComparisonOperatorsType comparisons = new org.geotoolkit.ogc.xml.v200.ComparisonOperatorsType(compaOperatorList);
        final org.geotoolkit.ogc.xml.v200.ScalarCapabilitiesType scalarCapabilities = new org.geotoolkit.ogc.xml.v200.ScalarCapabilitiesType(comparisons, true);

        final ResourceIdentifierType iden = new ResourceIdentifierType(new QName("http://www.opengis.net/fes/2.0", "ResourceId"));
        final org.geotoolkit.ogc.xml.v200.IdCapabilitiesType idCapabilities = new org.geotoolkit.ogc.xml.v200.IdCapabilitiesType(iden);
        
        final List<org.geotoolkit.ows.xml.v110.DomainType> constraints = new ArrayList<org.geotoolkit.ows.xml.v110.DomainType>();
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsQuery",             new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsAdHocQuery",        new NoValues(), new ValueType("FALSE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsFunctions",         new NoValues(), new ValueType("FALSE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsMinStandardFilter", new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsStandardFilter",    new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsMinSpatialFilter",  new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsSpatialFilter",     new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsMinTemporalFilter", new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsTemporalFilter",    new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsVersionNav",        new NoValues(), new ValueType("FALSE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsSorting",           new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsExtendedOperators", new NoValues(), new ValueType("FALSE")));
        final org.geotoolkit.ogc.xml.v200.ConformanceType conformance = new ConformanceType(constraints);
        FILTER_CAPABILITIES_V200 = new org.geotoolkit.ogc.xml.v200.FilterCapabilities(scalarCapabilities, spatialCapabilties, idCapabilities, conformance);
    }

    public static final AbstractOperationsMetadata OPERATIONS_METADATA_V110;
    static {
        final List<DCP> dcps = new ArrayList<DCP>();
        dcps.add(new DCP(new HTTP(new RequestMethodType("somURL"), new RequestMethodType("someURL"))));

        final List<DCP> dcps2 = new ArrayList<DCP>();
        dcps2.add(new DCP(new HTTP(null, new RequestMethodType("someURL"))));

        final List<AbstractOperation> operations = new ArrayList<AbstractOperation>();

        final List<DomainType> gcParameters = new ArrayList<DomainType>();
        gcParameters.add(new DomainType("AcceptVersions", "1.1.0"));
        gcParameters.add(new DomainType("AcceptFormats", "text/xml"));
        gcParameters.add(new DomainType("Service", "WFS"));
        Operation getCapabilities = new Operation(dcps, gcParameters, null, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<DomainType> dfParameters = new ArrayList<DomainType>();
        dfParameters.add(new DomainType("outputFormat", "text/xml; subtype=gml/3.1.1"));
        dfParameters.add(new DomainType("Service", "WFS"));
        dfParameters.add(new DomainType("Version", "1.1.0"));
        Operation describeFeatureType = new Operation(dcps, dfParameters, null, null, "DescribeFeatureType");
        operations.add(describeFeatureType);

        final List<DomainType> gfParameters = new ArrayList<DomainType>();
        gfParameters.add(new DomainType("resultType", Arrays.asList("results","hits")));
        gfParameters.add(new DomainType("outputFormat", "text/xml; subtype=gml/3.1.1"));
        gfParameters.add(new DomainType("Service", "WFS"));
        gfParameters.add(new DomainType("Version", "1.1.0"));

        final List<DomainType> gfConstraints = new ArrayList<DomainType>();
        gfConstraints.add(new DomainType("LocalTraverseXLinkScope", "2")); // ???
        Operation getFeature = new Operation(dcps, gfParameters, gfConstraints, null, "GetFeature");
        operations.add(getFeature);

        final List<DomainType> tParameters = new ArrayList<DomainType>();
        tParameters.add(new DomainType("inputFormat", "text/xml; subtype=gml/3.1.1"));
        tParameters.add(new DomainType("idgen", Arrays.asList("GenerateNew","UseExisting","ReplaceDuplicate")));
        tParameters.add(new DomainType("releaseAction", Arrays.asList("ALL", "SOME")));
        tParameters.add(new DomainType("Service", "WFS"));
        tParameters.add(new DomainType("Version", "1.1.0"));
        Operation Transaction = new Operation(dcps2, tParameters, null, null, "Transaction");
        operations.add(Transaction);


        OPERATIONS_METADATA_V110 = OWSXmlFactory.buildOperationsMetadata("1.0.0", operations, null, null, null);
    }

    public static final AbstractOperationsMetadata OPERATIONS_METADATA_V200;
    static {
        final List<org.geotoolkit.ows.xml.v110.DCP> dcps = new ArrayList<org.geotoolkit.ows.xml.v110.DCP>();
        dcps.add(new org.geotoolkit.ows.xml.v110.DCP(new org.geotoolkit.ows.xml.v110.HTTP(new org.geotoolkit.ows.xml.v110.RequestMethodType("somURL"),
                                                                                          new org.geotoolkit.ows.xml.v110.RequestMethodType("someURL"))));

        final List<org.geotoolkit.ows.xml.v110.DCP> dcps2 = new ArrayList<org.geotoolkit.ows.xml.v110.DCP>();
        dcps2.add(new org.geotoolkit.ows.xml.v110.DCP(new org.geotoolkit.ows.xml.v110.HTTP(null, new org.geotoolkit.ows.xml.v110.RequestMethodType("someURL"))));

        final List<AbstractOperation> operations = new ArrayList<AbstractOperation>();

        final org.geotoolkit.ows.xml.v110.DomainType serviceDomain = new org.geotoolkit.ows.xml.v110.DomainType("Service", "WFS");
        final org.geotoolkit.ows.xml.v110.DomainType versionDomain = new org.geotoolkit.ows.xml.v110.DomainType("Version", "2.0.0");
        
        final List<org.geotoolkit.ows.xml.v110.DomainType> gcParameters = new ArrayList<org.geotoolkit.ows.xml.v110.DomainType>();
        gcParameters.add(new org.geotoolkit.ows.xml.v110.DomainType("AcceptVersions", new AllowedValues(Arrays.asList("2.0.0", "1.1.0"))));
        gcParameters.add(new org.geotoolkit.ows.xml.v110.DomainType("AcceptFormats", "text/xml"));
        gcParameters.add(serviceDomain);
        org.geotoolkit.ows.xml.v110.Operation getCapabilities = new org.geotoolkit.ows.xml.v110.Operation(dcps, gcParameters, null, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<org.geotoolkit.ows.xml.v110.DomainType> dfParameters = new ArrayList<org.geotoolkit.ows.xml.v110.DomainType>();
        dfParameters.add(new org.geotoolkit.ows.xml.v110.DomainType("outputFormat", "text/xml; subtype=gml/3.1.1"));
        dfParameters.add(serviceDomain);
        dfParameters.add(versionDomain);
        org.geotoolkit.ows.xml.v110.Operation describeFeatureType = new org.geotoolkit.ows.xml.v110.Operation(dcps, dfParameters, null, null, "DescribeFeatureType");
        operations.add(describeFeatureType);

        final List<org.geotoolkit.ows.xml.v110.DomainType> gfParameters = new ArrayList<org.geotoolkit.ows.xml.v110.DomainType>();
        gfParameters.add(new org.geotoolkit.ows.xml.v110.DomainType("resultType", new AllowedValues(Arrays.asList("results","hits"))));
        gfParameters.add(new org.geotoolkit.ows.xml.v110.DomainType("outputFormat", "text/xml; subtype=gml/3.1.1"));
        gfParameters.add(serviceDomain);
        gfParameters.add(versionDomain);

        final List<org.geotoolkit.ows.xml.v110.DomainType> gfConstraints = new ArrayList<org.geotoolkit.ows.xml.v110.DomainType>();
        gfConstraints.add(new org.geotoolkit.ows.xml.v110.DomainType("LocalTraverseXLinkScope", new AllowedValues(Arrays.asList("2")))); // ???
        org.geotoolkit.ows.xml.v110.Operation getFeature = new org.geotoolkit.ows.xml.v110.Operation(dcps, gfParameters, gfConstraints, null, "GetFeature");
        operations.add(getFeature);

        final List<org.geotoolkit.ows.xml.v110.DomainType> tParameters = new ArrayList<org.geotoolkit.ows.xml.v110.DomainType>();
        tParameters.add(new org.geotoolkit.ows.xml.v110.DomainType("inputFormat", "text/xml; subtype=gml/3.1.1"));
        tParameters.add(new org.geotoolkit.ows.xml.v110.DomainType("idgen", new AllowedValues(Arrays.asList("GenerateNew","UseExisting","ReplaceDuplicate"))));
        tParameters.add(new org.geotoolkit.ows.xml.v110.DomainType("releaseAction", new AllowedValues(Arrays.asList("ALL", "SOME"))));
        tParameters.add(serviceDomain);
        tParameters.add(versionDomain);
        org.geotoolkit.ows.xml.v110.Operation Transaction = new org.geotoolkit.ows.xml.v110.Operation(dcps2, tParameters, null, null, "Transaction");
        operations.add(Transaction);
        
        final List<org.geotoolkit.ows.xml.v110.DomainType> lsqParameters = new ArrayList<org.geotoolkit.ows.xml.v110.DomainType>();
        lsqParameters.add(serviceDomain);
        lsqParameters.add(versionDomain);
        org.geotoolkit.ows.xml.v110.Operation listStoredQueries = new org.geotoolkit.ows.xml.v110.Operation(dcps, lsqParameters, null, null, "ListStoredQueries");
        operations.add(listStoredQueries);
        
        final List<org.geotoolkit.ows.xml.v110.DomainType> dsqParameters = new ArrayList<org.geotoolkit.ows.xml.v110.DomainType>();
        dsqParameters.add(serviceDomain);
        dsqParameters.add(versionDomain);
        org.geotoolkit.ows.xml.v110.Operation describeStoredQueries = new org.geotoolkit.ows.xml.v110.Operation(dcps, dsqParameters, null, null, "DescribeStoredQueries");
        operations.add(describeStoredQueries);
        
        final List<org.geotoolkit.ows.xml.v110.DomainType> gpvParameters = new ArrayList<org.geotoolkit.ows.xml.v110.DomainType>();
        gpvParameters.add(serviceDomain);
        gpvParameters.add(versionDomain);
        org.geotoolkit.ows.xml.v110.Operation getPropertyValue = new org.geotoolkit.ows.xml.v110.Operation(dcps, gpvParameters, null, null, "GetPropertyValue");
        operations.add(getPropertyValue);
        
        final List<org.geotoolkit.ows.xml.v110.DomainType> csqParameters = new ArrayList<org.geotoolkit.ows.xml.v110.DomainType>();
        csqParameters.add(serviceDomain);
        csqParameters.add(versionDomain);
        org.geotoolkit.ows.xml.v110.Operation createStoredQuery = new org.geotoolkit.ows.xml.v110.Operation(dcps, csqParameters, null, null, "CreateStoredQuery");
        operations.add(createStoredQuery);
        
        final List<org.geotoolkit.ows.xml.v110.DomainType> dsqParameters2 = new ArrayList<org.geotoolkit.ows.xml.v110.DomainType>();
        dsqParameters2.add(serviceDomain);
        dsqParameters2.add(versionDomain);
        org.geotoolkit.ows.xml.v110.Operation dropStoredQuery = new org.geotoolkit.ows.xml.v110.Operation(dcps, dsqParameters2, null, null, "DropStoredQuery");
        operations.add(dropStoredQuery);

        final List<AbstractDomain> parameters = new ArrayList<AbstractDomain>();
        parameters.add(new org.geotoolkit.ows.xml.v110.DomainType("version", new AllowedValues(Arrays.asList("2.0.0", "1.1.0"))));
        
        final List<AbstractDomain> constraints = new ArrayList<AbstractDomain>();
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsSimpleWFS",         new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsBasicWFS",          new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsTransactionalWFS",  new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsLockingWFS",        new NoValues(), new ValueType("FALSE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("KVPEncoding",                 new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("XMLEncoding",                 new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("SOAPEncoding",                new NoValues(), new ValueType("FALSE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsInheritance",       new NoValues(), new ValueType("FALSE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsRemoteResolve",     new NoValues(), new ValueType("FALSE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsResultPaging",      new NoValues(), new ValueType("TRUE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsStandardJoins",     new NoValues(), new ValueType("FALSE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsSpatialJoins",      new NoValues(), new ValueType("FALSE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsTemporalJoins",     new NoValues(), new ValueType("FALSE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsFeatureVersioning", new NoValues(), new ValueType("FALSE")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ManageStoredQueries",         new NoValues(), new ValueType("TRUE")));

        OPERATIONS_METADATA_V200 = OWSXmlFactory.buildOperationsMetadata("1.1.0", operations, parameters, constraints, null);
    }

    /**
     * this static member is not yet useable because of clone issues for the featureType names.
     * build your own {@linkplain StoredQueryDescriptionType} with IDENTIFIER_FILTER
     */
    public static final StoredQueryDescriptionType IDENTIFIER_STORED_QUERY;
    public static final FilterType IDENTIFIER_FILTER;
    public static final ParameterExpressionType IDENTIFIER_PARAM;
    static {
        IDENTIFIER_PARAM = new ParameterExpressionType("id", "id Parameter", "A parameter on the id of the feature", new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
        final PropertyIsEqualToType pis = new PropertyIsEqualToType(new LiteralType("$id"), "@id", true);
        IDENTIFIER_FILTER = new FilterType(pis);
        final QueryType query = new QueryType(IDENTIFIER_FILTER, null, "2.0.0");
        final QueryExpressionTextType queryEx = new QueryExpressionTextType("urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression", null, null);
        final ObjectFactory factory = new ObjectFactory();
        queryEx.getContent().add(factory.createQuery(query));
        IDENTIFIER_STORED_QUERY = new StoredQueryDescriptionType("urn:ogc:def:storedQuery:OGC-WFS::GetFeatureById", "Identifier query" , "filter on feature identifier", IDENTIFIER_PARAM, queryEx);
    }
}
