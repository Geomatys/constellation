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
import javax.xml.namespace.QName;
import org.geotoolkit.lang.Immutable;
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
import org.geotoolkit.ows.xml.v100.DCP;
import org.geotoolkit.ows.xml.v100.DomainType;
import org.geotoolkit.ows.xml.v100.HTTP;
import org.geotoolkit.ows.xml.v100.Operation;
import org.geotoolkit.ows.xml.v100.OperationsMetadata;
import org.geotoolkit.ows.xml.v100.RequestMethodType;
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

    public static final String HANDLE = "handle";
    public static final String SERVICE = "service";
    public static final String VERSION = "version";
    public static final String NAMESPACE = "namespace";
    public static final String FILTER = "filter";

    public static final String UNKNOW_TYPENAME= "The specified TypeNames does not exist:";

    public static final FilterCapabilities FILTER_CAPABILITIES;
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
        final ScalarCapabilitiesType sclalarCapabilities = new ScalarCapabilitiesType(comparisons, arithmetic, false);

        final IdCapabilitiesType idCapabilities = new IdCapabilitiesType(true, true);
        FILTER_CAPABILITIES = new FilterCapabilities(sclalarCapabilities, spatialCapabilties, idCapabilities);
        
    }

    public static final OperationsMetadata OPERATIONS_METADATA;
    static {
        final List<DCP> dcps = new ArrayList<DCP>();
        dcps.add(new DCP(new HTTP(new RequestMethodType("somURL"), new RequestMethodType("someURL"))));

        final List<DCP> dcps2 = new ArrayList<DCP>();
        dcps2.add(new DCP(new HTTP(null, new RequestMethodType("someURL"))));

        final List<Operation> operations = new ArrayList<Operation>();

        final List<DomainType> gcParameters = new ArrayList<DomainType>();
        gcParameters.add(new DomainType("AcceptVersions", Arrays.asList("1.1.0")));
        gcParameters.add(new DomainType("AcceptFormats", Arrays.asList("text/xml")));
        gcParameters.add(new DomainType("Service", Arrays.asList("WFS")));
        Operation getCapabilities = new Operation(dcps, gcParameters, null, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<DomainType> dfParameters = new ArrayList<DomainType>();
        dfParameters.add(new DomainType("outputFormat", Arrays.asList("text/xml; subtype=gml/3.1.1")));
        dfParameters.add(new DomainType("Service", Arrays.asList("WFS")));
        dfParameters.add(new DomainType("Version", Arrays.asList("1.1.0")));
        Operation describeFeatureType = new Operation(dcps, dfParameters, null, null, "DescribeFeatureType");
        operations.add(describeFeatureType);

        final List<DomainType> gfParameters = new ArrayList<DomainType>();
        gfParameters.add(new DomainType("resultType", Arrays.asList("results","hits")));
        gfParameters.add(new DomainType("outputFormat", Arrays.asList("text/xml; subtype=gml/3.1.1")));
        gfParameters.add(new DomainType("Service", Arrays.asList("WFS")));
        gfParameters.add(new DomainType("Version", Arrays.asList("1.1.0")));

        final List<DomainType> gfConstraints = new ArrayList<DomainType>();
        gfConstraints.add(new DomainType("LocalTraverseXLinkScope", Arrays.asList("2"))); // ???
        Operation getFeature = new Operation(dcps, gfParameters, gfConstraints, null, "GetFeature");
        operations.add(getFeature);

        final List<DomainType> tParameters = new ArrayList<DomainType>();
        tParameters.add(new DomainType("inputFormat", Arrays.asList("text/xml; subtype=gml/3.1.1")));
        tParameters.add(new DomainType("idgen", Arrays.asList("GenerateNew","UseExisting","ReplaceDuplicate")));
        tParameters.add(new DomainType("releaseAction", Arrays.asList("ALL", "SOME")));
        tParameters.add(new DomainType("Service", Arrays.asList("WFS")));
        tParameters.add(new DomainType("Version", Arrays.asList("1.1.0")));
        Operation Transaction = new Operation(dcps2, tParameters, null, null, "Transaction");
        operations.add(Transaction);


        OPERATIONS_METADATA = new OperationsMetadata(operations, null, null, null);
    }
}
