/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.sos.ws;

import java.util.ArrayList;
import org.opengis.filter.capability.Operator;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import net.jcip.annotations.Immutable;
import org.geotoolkit.ogc.xml.v110.ComparisonOperatorType;
import org.geotoolkit.ogc.xml.v110.ComparisonOperatorsType;
import org.geotoolkit.ogc.xml.v110.GeometryOperandsType;
import org.geotoolkit.ogc.xml.v110.IdCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.ScalarCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.SpatialCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.SpatialOperatorType;
import org.geotoolkit.ogc.xml.v110.SpatialOperatorsType;
import org.geotoolkit.ogc.xml.v110.TemporalCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.TemporalOperandsType;
import org.geotoolkit.ogc.xml.v110.TemporalOperatorNameType;
import org.geotoolkit.ogc.xml.v110.TemporalOperatorType;
import org.geotoolkit.ogc.xml.v110.TemporalOperatorsType;
import org.geotoolkit.sos.xml.v100.FilterCapabilities;
import org.opengis.filter.capability.SpatialOperator;

import static org.geotoolkit.gml.xml.v311.ObjectFactory.*;
import org.geotoolkit.ows.xml.v110.AllowedValues;
import org.geotoolkit.ows.xml.v110.AnyValue;
import org.geotoolkit.ows.xml.v110.DCP;
import org.geotoolkit.ows.xml.v110.DomainType;
import org.geotoolkit.ows.xml.v110.HTTP;
import org.geotoolkit.ows.xml.v110.Operation;
import org.geotoolkit.ows.xml.v110.OperationsMetadata;
import org.geotoolkit.ows.xml.v110.RangeType;
import org.geotoolkit.ows.xml.v110.RequestMethodType;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Immutable
public final class SOSConstants {

    private SOSConstants() {}

    public static final String SOS = "SOS";
    public static final String ALL = "All";
    public static final String OFFERING = "offering";
    public static final String EVENT_TIME = "eventTime";
    public static final String PROCEDURE = "procedure";
    public static final String OUTPUT_FORMAT = "OUTPUTFORMAT";
    public static final String OBSERVATION_TEMPLATE = "observationTemplate";
    public static final String RESPONSE_MODE = "responseMode";
    public static final String RESPONSE_FORMAT = "responseFormat";
    public static final String NOT_SUPPORTED = "This operation is not take in charge by the Web Service";
    public static final String SENSORML_100_FORMAT = "text/xml;subtype=\"sensorML/1.0.0\"";
    public static final String SENSORML_101_FORMAT = "text/xml;subtype=\"sensorML/1.0.1\"";

    /**
     * The base Qname for complex observation.
     */
    public static final QName OBSERVATION_QNAME = new QName("http://www.opengis.net/om/1.0", "Observation", "om");
    
    public static final String OBSERVATION_MODEL = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";

    /**
     * The base Qname for measurement observation.
     */
    public static final QName MEASUREMENT_QNAME = new QName("http://www.opengis.net/om/1.0", "Measurement", "om");
    
    public static final FilterCapabilities SOS_FILTER_CAPABILITIES_V100 = new FilterCapabilities();
    
    static {
        final GeometryOperandsType geom = new GeometryOperandsType(Arrays.asList(_Envelope_QNAME));
        final SpatialOperator[] spaOps = new SpatialOperator[1];
        spaOps[0] = new SpatialOperatorType("BBOX", null);
        final SpatialOperatorsType spaOp = new SpatialOperatorsType(spaOps);
        final SpatialCapabilitiesType  spatial = new SpatialCapabilitiesType(geom, spaOp);
        SOS_FILTER_CAPABILITIES_V100.setSpatialCapabilities(spatial);
        
        final TemporalCapabilitiesType temporal = new TemporalCapabilitiesType();
        final TemporalOperandsType temp = new TemporalOperandsType();
        temp.getTemporalOperand().add(_TimeInstant_QNAME);
        temp.getTemporalOperand().add(_TimePeriod_QNAME);
        temporal.setTemporalOperands(temp);
        final TemporalOperatorsType tempOp = new TemporalOperatorsType();
        final TemporalOperatorType td = new TemporalOperatorType();
        td.setName(TemporalOperatorNameType.TM_DURING);
        final TemporalOperatorType te = new TemporalOperatorType();
        te.setName(TemporalOperatorNameType.TM_EQUALS);
        final TemporalOperatorType ta = new TemporalOperatorType();
        ta.setName(TemporalOperatorNameType.TM_AFTER);
        final TemporalOperatorType tb = new TemporalOperatorType();
        tb.setName(TemporalOperatorNameType.TM_BEFORE);
        
        tempOp.getTemporalOperator().add(td);
        tempOp.getTemporalOperator().add(te);
        tempOp.getTemporalOperator().add(ta);
        tempOp.getTemporalOperator().add(tb);
        
        temporal.setTemporalOperators(tempOp);
        
        SOS_FILTER_CAPABILITIES_V100.setTemporalCapabilities(temporal);
        
        final Operator[] compOps = new Operator[8];
        compOps[0] = ComparisonOperatorType.BETWEEN;
        compOps[1] = ComparisonOperatorType.EQUAL_TO;
        compOps[2] = ComparisonOperatorType.NOT_EQUAL_TO;
        compOps[3] = ComparisonOperatorType.LESS_THAN;
        compOps[4] = ComparisonOperatorType.LESS_THAN_EQUAL_TO;
        compOps[5] = ComparisonOperatorType.GREATER_THAN;
        compOps[6] = ComparisonOperatorType.GREATER_THAN_EQUAL_TO;
        compOps[7] = ComparisonOperatorType.LIKE;
        final ComparisonOperatorsType compOp = new ComparisonOperatorsType(compOps);
        final ScalarCapabilitiesType scalar = new ScalarCapabilitiesType(compOp, null, false);
        SOS_FILTER_CAPABILITIES_V100.setScalarCapabilities(scalar);
        
        final IdCapabilitiesType id = new IdCapabilitiesType(true, true);
        SOS_FILTER_CAPABILITIES_V100.setIdCapabilities(id);
    }
    
    public static final org.geotoolkit.sos.xml.v200.FilterCapabilities SOS_FILTER_CAPABILITIES_V200 = new org.geotoolkit.sos.xml.v200.FilterCapabilities();
    
    static {
                
        final org.geotoolkit.ogc.xml.v200.GeometryOperandsType geom = new org.geotoolkit.ogc.xml.v200.GeometryOperandsType(Arrays.asList(_Envelope_QNAME));
        final SpatialOperator[] spaOps = new SpatialOperator[1];
        spaOps[0] = new SpatialOperatorType("BBOX", null);
        final org.geotoolkit.ogc.xml.v200.SpatialOperatorsType spaOp = new org.geotoolkit.ogc.xml.v200.SpatialOperatorsType(spaOps);
        final org.geotoolkit.ogc.xml.v200.SpatialCapabilitiesType  spatial = new org.geotoolkit.ogc.xml.v200.SpatialCapabilitiesType(geom, spaOp);
        
        final org.geotoolkit.ogc.xml.v200.TemporalCapabilitiesType temporal = new org.geotoolkit.ogc.xml.v200.TemporalCapabilitiesType();
        final org.geotoolkit.ogc.xml.v200.TemporalOperandsType temp = new org.geotoolkit.ogc.xml.v200.TemporalOperandsType(Arrays.asList(_TimeInstant_QNAME, _TimePeriod_QNAME));
        temporal.setTemporalOperands(temp);
        final org.geotoolkit.ogc.xml.v200.TemporalOperatorsType tempOp = new org.geotoolkit.ogc.xml.v200.TemporalOperatorsType();
        final org.geotoolkit.ogc.xml.v200.TemporalOperatorType td = new org.geotoolkit.ogc.xml.v200.TemporalOperatorType();
        td.setName("TM_DURING");
        final org.geotoolkit.ogc.xml.v200.TemporalOperatorType te = new org.geotoolkit.ogc.xml.v200.TemporalOperatorType();
        te.setName("TM_EQUALS");
        final org.geotoolkit.ogc.xml.v200.TemporalOperatorType ta = new org.geotoolkit.ogc.xml.v200.TemporalOperatorType();
        ta.setName("TM_AFTER");
        final org.geotoolkit.ogc.xml.v200.TemporalOperatorType tb = new org.geotoolkit.ogc.xml.v200.TemporalOperatorType();
        tb.setName("TM_BEFORE");
        
        tempOp.getTemporalOperator().add(td);
        tempOp.getTemporalOperator().add(te);
        tempOp.getTemporalOperator().add(ta);
        tempOp.getTemporalOperator().add(tb);
        
        temporal.setTemporalOperators(tempOp);
        
        
        
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
        
        
        final org.geotoolkit.ogc.xml.v200.FilterCapabilities capa = new org.geotoolkit.ogc.xml.v200.FilterCapabilities(scalarCapabilities, spatial, temporal, null);
        SOS_FILTER_CAPABILITIES_V200.setFilterCapabilities(capa);
    }
    
    public static final OperationsMetadata OPERATIONS_METADATA;
    static {
        final List<DCP> getAndPost = new ArrayList<DCP>();
        getAndPost.add(new DCP(new HTTP(new RequestMethodType("somURL"), new RequestMethodType("someURL"))));

        final List<DCP> onlyPost = new ArrayList<DCP>();
        onlyPost.add(new DCP(new HTTP(null, new RequestMethodType("someURL"))));

        final List<Operation> operations = new ArrayList<Operation>();

        final List<DomainType> gcParameters = new ArrayList<DomainType>();
        gcParameters.add(new DomainType("service", "SOS"));
        gcParameters.add(new DomainType("Acceptversions", "1.0.0"));
        gcParameters.add(new DomainType("Sections", Arrays.asList("ServiceIdentification", "ServiceProvider", "OperationsMetadata", "Filter_Capabilities", "All")));
        gcParameters.add(new DomainType("AcceptFormats", "text/xml"));
        
        final Operation getCapabilities = new Operation(getAndPost, gcParameters, null, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<DomainType> rsParameters = new ArrayList<DomainType>();
        rsParameters.add(new DomainType("version", "1.0.0"));
        rsParameters.add(new DomainType("service", "SOS"));
        
        final Operation registerSensor = new Operation(onlyPost, rsParameters, null, null, "RegisterSensor");
        operations.add(registerSensor);
        
        final List<DomainType> grParameters = new ArrayList<DomainType>();
        grParameters.add(new DomainType("version", "1.0.0"));
        grParameters.add(new DomainType("service", "SOS"));
        
        final Operation getResult = new Operation(onlyPost, grParameters, null, null, "GetResult");
        operations.add(getResult);
        
        final List<DomainType> goParameters = new ArrayList<DomainType>();
        goParameters.add(new DomainType("version", "1.0.0"));
        goParameters.add(new DomainType("service", "SOS"));
        goParameters.add(new DomainType("srsName", new AnyValue()));
        goParameters.add(new DomainType("offering", "offering-AllSensor"));
        goParameters.add(new DomainType("eventTime", new AllowedValues(new RangeType("now", "now"))));
        goParameters.add(new DomainType("procedure", "toUpdate"));
        goParameters.add(new DomainType("observedProperty", "toUpdate"));
        goParameters.add(new DomainType("featureOfInterest", "toUpdate"));
        goParameters.add(new DomainType("result", new AnyValue()));
        goParameters.add(new DomainType("responseFormat", "text/xml; subtype=\"om/1.0.0\""));
        goParameters.add(new DomainType("resultModel", "om:Observation"));
        goParameters.add(new DomainType("responseMode", Arrays.asList("resultTemplate","inline")));
        
        final Operation getObservation = new Operation(onlyPost, goParameters, null, null, "GetObservation");
        operations.add(getObservation);
        
        final List<DomainType> ioParameters = new ArrayList<DomainType>();
        ioParameters.add(new DomainType("version", "1.0.0"));
        ioParameters.add(new DomainType("service", "SOS"));
        
        final Operation insertObservation = new Operation(onlyPost, ioParameters, null, null, "InsertObservation");
        operations.add(insertObservation);
        
        final List<DomainType> gfParameters = new ArrayList<DomainType>();
        gfParameters.add(new DomainType("featureOfInterestId", "toUpdate"));
        gfParameters.add(new DomainType("version", "1.0.0"));
        gfParameters.add(new DomainType("service", "SOS"));
        
        final Operation getFeatureOfInterest = new Operation(getAndPost, gfParameters, null, null, "GetFeatureOfInterest");
        operations.add(getFeatureOfInterest);
        
        final List<DomainType> gftParameters = new ArrayList<DomainType>();
        gftParameters.add(new DomainType("featureOfInterestId", "toUpdate"));
        gftParameters.add(new DomainType("version", "1.0.0"));
        gftParameters.add(new DomainType("service", "SOS"));
        
        final Operation getFeatureOfInterestTime = new Operation(onlyPost, gftParameters, null, null, "GetFeatureOfInterestTime");
        operations.add(getFeatureOfInterestTime);
        
        final List<DomainType> dsParameters = new ArrayList<DomainType>();
        dsParameters.add(new DomainType("version", "1.0.0"));
        dsParameters.add(new DomainType("service", "SOS"));
        dsParameters.add(new DomainType("outputFormat", "text/xml;subtype=\"sensorML/1.0.0\""));
        dsParameters.add(new DomainType("procedure", "toUpdate"));
        
        final Operation describeSensor = new Operation(getAndPost, dsParameters, null, null, "DescribeSensor");
        operations.add(describeSensor);
        
        final List<DomainType> constraints = new ArrayList<DomainType>();
        constraints.add(new DomainType("PostEncoding", "XML"));
        
        OPERATIONS_METADATA = new OperationsMetadata(operations, null, constraints, null);
    }
}

