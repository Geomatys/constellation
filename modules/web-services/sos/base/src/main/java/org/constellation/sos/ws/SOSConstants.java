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
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import net.jcip.annotations.Immutable;
import org.constellation.ws.MimeType;
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
import org.geotoolkit.ogc.xml.v200.ConformanceType;
import org.geotoolkit.ows.xml.v110.AllowedValues;
import org.geotoolkit.ows.xml.v110.AnyValue;
import org.geotoolkit.ows.xml.v110.DCP;
import org.geotoolkit.ows.xml.v110.DomainType;
import org.geotoolkit.ows.xml.v110.HTTP;
import org.geotoolkit.ows.xml.v110.NoValues;
import org.geotoolkit.ows.xml.v110.Operation;
import org.geotoolkit.ows.xml.v110.OperationsMetadata;
import org.geotoolkit.ows.xml.v110.RangeType;
import org.geotoolkit.ows.xml.v110.RequestMethodType;
import org.geotoolkit.ows.xml.v110.ValueType;
import org.geotoolkit.sos.xml.v200.InsertionCapabilitiesPropertyType;
import org.geotoolkit.sos.xml.v200.InsertionCapabilitiesType;

import static org.geotoolkit.gml.xml.v311.ObjectFactory.*;

import org.opengis.filter.capability.Operator;
import org.opengis.filter.capability.SpatialOperator;

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
    public static final String PROCEDURE_DESCRIPTION_FORMAT = "ProcedureDescriptionFormat";
    public static final String RESPONSE_MODE = "responseMode";
    public static final String RESPONSE_FORMAT = "responseFormat";
    public static final String NOT_SUPPORTED = "This operation is not take in charge by the Web Service";
    public static final String SENSORML_100_FORMAT_V100 = "text/xml;subtype=\"sensorML/1.0.0\"";
    public static final String SENSORML_101_FORMAT_V100 = "text/xml;subtype=\"sensorML/1.0.1\"";
    public static final String SENSORML_100_FORMAT_V200 = "http://www.opengis.net/sensorML/1.0.0";
    public static final String SENSORML_101_FORMAT_V200 = "http://www.opengis.net/sensorML/1.0.1";
    public static final String RESPONSE_FORMAT_V100     = "text/xml; subtype=\"om/1.0.0\"";
    public static final String RESPONSE_FORMAT_V200     = "http://www.opengis.net/om/2.0";

    /**
     * The base Qname for complex observation.
     */
    public static final QName OBSERVATION_QNAME = new QName("http://www.opengis.net/om/1.0", "Observation", "om");
    
    public static final String OBSERVATION_MODEL = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";

    /**
     * A list of supported MIME type
     */
    public static final List<String> ACCEPTED_OUTPUT_FORMATS;
    static {
        ACCEPTED_OUTPUT_FORMATS = Arrays.asList(MimeType.TEXT_XML,
                                                MimeType.APPLICATION_XML,
                                                MimeType.TEXT_PLAIN);
    }
    
    /**
     * The base Qname for measurement observation.
     */
    public static final QName MEASUREMENT_QNAME = new QName("http://www.opengis.net/om/1.0", "Measurement", "om");
    
    public static final FilterCapabilities SOS_FILTER_CAPABILITIES_V100 = new FilterCapabilities();
    
    static {
        final GeometryOperandsType geom = new GeometryOperandsType(Arrays.asList(_Envelope_QNAME));
        final SpatialOperator[] spaOps   = {new SpatialOperatorType("BBOX", null)};
        final SpatialOperatorsType spaOp = new SpatialOperatorsType(spaOps);
        final SpatialCapabilitiesType  spatial = new SpatialCapabilitiesType(geom, spaOp);
        SOS_FILTER_CAPABILITIES_V100.setSpatialCapabilities(spatial);
        
        final TemporalCapabilitiesType temporal = new TemporalCapabilitiesType();
        final TemporalOperandsType temp = new TemporalOperandsType();
        temp.getTemporalOperand().add(_TimeInstant_QNAME);
        temp.getTemporalOperand().add(_TimePeriod_QNAME);
        temporal.setTemporalOperands(temp);
        final TemporalOperatorsType tempOp = new TemporalOperatorsType();
        final TemporalOperatorType td = new TemporalOperatorType(TemporalOperatorNameType.TM_DURING);
        final TemporalOperatorType te = new TemporalOperatorType(TemporalOperatorNameType.TM_EQUALS);
        final TemporalOperatorType ta = new TemporalOperatorType(TemporalOperatorNameType.TM_AFTER);
        final TemporalOperatorType tb = new TemporalOperatorType(TemporalOperatorNameType.TM_BEFORE);
        
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
        spaOps[0] = new org.geotoolkit.ogc.xml.v200.SpatialOperatorType("BBOX", null);
        final org.geotoolkit.ogc.xml.v200.SpatialOperatorsType spaOp = new org.geotoolkit.ogc.xml.v200.SpatialOperatorsType(spaOps);
        final org.geotoolkit.ogc.xml.v200.SpatialCapabilitiesType  spatial = new org.geotoolkit.ogc.xml.v200.SpatialCapabilitiesType(geom, spaOp);
        
        final org.geotoolkit.ogc.xml.v200.TemporalCapabilitiesType temporal = new org.geotoolkit.ogc.xml.v200.TemporalCapabilitiesType();
        final org.geotoolkit.ogc.xml.v200.TemporalOperandsType temp = new org.geotoolkit.ogc.xml.v200.TemporalOperandsType(Arrays.asList(_TimeInstant_QNAME, _TimePeriod_QNAME));
        temporal.setTemporalOperands(temp);
        final org.geotoolkit.ogc.xml.v200.TemporalOperatorsType tempOp = new org.geotoolkit.ogc.xml.v200.TemporalOperatorsType();
        final org.geotoolkit.ogc.xml.v200.TemporalOperatorType td = new org.geotoolkit.ogc.xml.v200.TemporalOperatorType("During");
        final org.geotoolkit.ogc.xml.v200.TemporalOperatorType te = new org.geotoolkit.ogc.xml.v200.TemporalOperatorType("TEquals");
        final org.geotoolkit.ogc.xml.v200.TemporalOperatorType ta = new org.geotoolkit.ogc.xml.v200.TemporalOperatorType("After");
        final org.geotoolkit.ogc.xml.v200.TemporalOperatorType tb = new org.geotoolkit.ogc.xml.v200.TemporalOperatorType("Before");
        
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
        
         final List<org.geotoolkit.ows.xml.v110.DomainType> constraints = new ArrayList<org.geotoolkit.ows.xml.v110.DomainType>();
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsQuery",             new NoValues(), new ValueType("false")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsAdHocQuery",        new NoValues(), new ValueType("false")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsFunctions",         new NoValues(), new ValueType("false")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsMinStandardFilter", new NoValues(), new ValueType("false")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsStandardFilter",    new NoValues(), new ValueType("false")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsMinSpatialFilter",  new NoValues(), new ValueType("true")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsSpatialFilter",     new NoValues(), new ValueType("true")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsMinTemporalFilter", new NoValues(), new ValueType("true")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsTemporalFilter",    new NoValues(), new ValueType("true")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsVersionNav",        new NoValues(), new ValueType("false")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsSorting",           new NoValues(), new ValueType("false")));
        constraints.add(new org.geotoolkit.ows.xml.v110.DomainType("ImplementsExtendedOperators", new NoValues(), new ValueType("false")));
        final org.geotoolkit.ogc.xml.v200.ConformanceType conformance = new ConformanceType(constraints);
        
        final org.geotoolkit.ogc.xml.v200.FilterCapabilities capa = new org.geotoolkit.ogc.xml.v200.FilterCapabilities(scalarCapabilities, spatial, temporal, conformance);
        SOS_FILTER_CAPABILITIES_V200.setFilterCapabilities(capa);
    }
    
    private static final List<DCP> GET_AND_POST = new ArrayList<DCP>();
    private static final List<DCP> ONLY_POST    = new ArrayList<DCP>();
    static {
        RequestMethodType rm = new RequestMethodType("somURL");
        GET_AND_POST.add(new DCP(new HTTP(rm, rm)));
        ONLY_POST.add(new DCP(new HTTP(null, rm)));
    }
    
    private static final DomainType SERVICE_PARAMETER = new DomainType("service", "SOS");
    
    private static final Operation GET_CAPABILITIES;
    static {
        final List<DomainType> gcParameters = new ArrayList<DomainType>();
        gcParameters.add(SERVICE_PARAMETER);
        gcParameters.add(new DomainType("Acceptversions", Arrays.asList("1.0.0", "2.0.0")));
        gcParameters.add(new DomainType("Sections", Arrays.asList("ServiceIdentification", "ServiceProvider", "OperationsMetadata", "Filter_Capabilities", "All")));
        gcParameters.add(new DomainType("AcceptFormats", "text/xml"));
        
        GET_CAPABILITIES = new Operation(GET_AND_POST, gcParameters, null, null, "GetCapabilities");
    }
    
    private static final Operation GETOBSERVATION_BY_ID;
    static {
        final List<DomainType> gobidParameters = new ArrayList<DomainType>();
        gobidParameters.add(new DomainType("version", Arrays.asList("1.0.0", "2.0.0")));
        gobidParameters.add(SERVICE_PARAMETER);
        gobidParameters.add(new DomainType("observation", new AnyValue()));
        
        GETOBSERVATION_BY_ID = new Operation(ONLY_POST, gobidParameters, null, null, "GetObservationById");
    }
    
    public static final OperationsMetadata OPERATIONS_METADATA_100;
    static {

        final List<Operation> operations = new ArrayList<Operation>();
        operations.add(GET_CAPABILITIES);

        final List<DomainType> rsParameters = new ArrayList<DomainType>();
        rsParameters.add(new DomainType("version", Arrays.asList("1.0.0")));
        rsParameters.add(SERVICE_PARAMETER);
        
        final Operation registerSensor = new Operation(ONLY_POST, rsParameters, null, null, "RegisterSensor");
        operations.add(registerSensor);
        
        final List<DomainType> grParameters = new ArrayList<DomainType>();
        grParameters.add(new DomainType("version", Arrays.asList("1.0.0", "2.0.0")));
        grParameters.add(SERVICE_PARAMETER);
        
        final Operation getResult = new Operation(ONLY_POST, grParameters, null, null, "GetResult");
        operations.add(getResult);
        
        final List<DomainType> goParameters = new ArrayList<DomainType>();
        goParameters.add(new DomainType("version", Arrays.asList("1.0.0", "2.0.0")));
        goParameters.add(SERVICE_PARAMETER);
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
        
        final Operation getObservation = new Operation(ONLY_POST, goParameters, null, null, "GetObservation");
        operations.add(getObservation);
        
        operations.add(GETOBSERVATION_BY_ID);
        
        final List<DomainType> ioParameters = new ArrayList<DomainType>();
        ioParameters.add(new DomainType("version", Arrays.asList("1.0.0", "2.0.0")));
        ioParameters.add(SERVICE_PARAMETER);
        
        final Operation insertObservation = new Operation(ONLY_POST, ioParameters, null, null, "InsertObservation");
        operations.add(insertObservation);
        
        final List<DomainType> gfParameters = new ArrayList<DomainType>();
        gfParameters.add(new DomainType("featureOfInterestId", "toUpdate"));
        gfParameters.add(new DomainType("version", Arrays.asList("1.0.0", "2.0.0")));
        gfParameters.add(SERVICE_PARAMETER);
        
        final Operation getFeatureOfInterest = new Operation(GET_AND_POST, gfParameters, null, null, "GetFeatureOfInterest");
        operations.add(getFeatureOfInterest);
        
        final List<DomainType> gftParameters = new ArrayList<DomainType>();
        gftParameters.add(new DomainType("featureOfInterestId", "toUpdate"));
        gftParameters.add(new DomainType("version", Arrays.asList("1.0.0")));
        gftParameters.add(SERVICE_PARAMETER);
        
        final Operation getFeatureOfInterestTime = new Operation(ONLY_POST, gftParameters, null, null, "GetFeatureOfInterestTime");
        operations.add(getFeatureOfInterestTime);
        
        final List<DomainType> dsParameters = new ArrayList<DomainType>();
        dsParameters.add(new DomainType("version", Arrays.asList("1.0.0", "2.0.0")));
        dsParameters.add(SERVICE_PARAMETER);
        dsParameters.add(new DomainType("outputFormat", "text/xml;subtype=\"sensorML/1.0.0\""));
        dsParameters.add(new DomainType("procedure", "toUpdate"));
        
        final Operation describeSensor = new Operation(GET_AND_POST, dsParameters, null, null, "DescribeSensor");
        operations.add(describeSensor);
        
        final List<DomainType> constraints = new ArrayList<DomainType>();
        constraints.add(new DomainType("PostEncoding", "XML"));
        
        OPERATIONS_METADATA_100 = new OperationsMetadata(operations, null, constraints, null);
    }
    
    public static final OperationsMetadata OPERATIONS_METADATA_200;
    static {

        final List<Operation> operations = new ArrayList<Operation>();
        operations.add(GET_CAPABILITIES);

        final List<DomainType> rsParameters = new ArrayList<DomainType>();
        rsParameters.add(new DomainType("version", Arrays.asList("2.0.0")));
        rsParameters.add(SERVICE_PARAMETER);
        
        final Operation insertSensor = new Operation(ONLY_POST, rsParameters, null, null, "InsertSensor");
        operations.add(insertSensor);
        
        final List<DomainType> irtParameters = new ArrayList<DomainType>();
        irtParameters.add(new DomainType("version", Arrays.asList("2.0.0")));
        irtParameters.add(SERVICE_PARAMETER);
        
        final Operation insertResultTemplate = new Operation(ONLY_POST, irtParameters, null, null, "InsertResultTemplate");
        operations.add(insertResultTemplate);
        
        final List<DomainType> irParameters = new ArrayList<DomainType>();
        irParameters.add(new DomainType("version", Arrays.asList("2.0.0")));
        irParameters.add(SERVICE_PARAMETER);
        
        final Operation insertResult = new Operation(ONLY_POST, irParameters, null, null, "InsertResult");
        operations.add(insertResult);
        
        final List<DomainType> dsParameters = new ArrayList<DomainType>();
        dsParameters.add(new DomainType("version", Arrays.asList("2.0.0")));
        dsParameters.add(SERVICE_PARAMETER);
        
        final Operation deleteSensor = new Operation(ONLY_POST, dsParameters, null, null, "DeleteSensor");
        operations.add(deleteSensor);
        
        final List<DomainType> grtParameters = new ArrayList<DomainType>();
        grtParameters.add(new DomainType("version", Arrays.asList("2.0.0")));
        grtParameters.add(SERVICE_PARAMETER);
        
        final Operation getResultTemplate = new Operation(ONLY_POST, grtParameters, null, null, "GetResultTemplate");
        operations.add(getResultTemplate);
        
        final List<DomainType> grParameters = new ArrayList<DomainType>();
        grParameters.add(new DomainType("version", Arrays.asList("1.0.0", "2.0.0")));
        grParameters.add(SERVICE_PARAMETER);
        
        final Operation getResult = new Operation(ONLY_POST, grParameters, null, null, "GetResult");
        operations.add(getResult);
        
        final List<DomainType> goParameters = new ArrayList<DomainType>();
        goParameters.add(new DomainType("version", Arrays.asList("1.0.0", "2.0.0")));
        goParameters.add(SERVICE_PARAMETER);
        goParameters.add(new DomainType("offering", "toUpdate"));
        goParameters.add(new DomainType("eventTime", new AllowedValues(new RangeType("now", "now"))));
        goParameters.add(new DomainType("procedure", "toUpdate"));
        goParameters.add(new DomainType("observedProperty", "toUpdate"));
        goParameters.add(new DomainType("featureOfInterest", "toUpdate"));
        goParameters.add(new DomainType("responseFormat", "http://www.opengis.net/om/2.0"));
        
        final Operation getObservation = new Operation(ONLY_POST, goParameters, null, null, "GetObservation");
        operations.add(getObservation);
        
        operations.add(GETOBSERVATION_BY_ID);
        
        final List<DomainType> ioParameters = new ArrayList<DomainType>();
        ioParameters.add(new DomainType("version", Arrays.asList("1.0.0", "2.0.0")));
        ioParameters.add(SERVICE_PARAMETER);
        
        final Operation insertObservation = new Operation(ONLY_POST, ioParameters, null, null, "InsertObservation");
        operations.add(insertObservation);
        
        final List<DomainType> gfParameters = new ArrayList<DomainType>();
        gfParameters.add(new DomainType("featureOfInterestId", "toUpdate"));
        gfParameters.add(new DomainType("version", Arrays.asList("1.0.0", "2.0.0")));
        gfParameters.add(SERVICE_PARAMETER);
        
        final Operation getFeatureOfInterest = new Operation(GET_AND_POST, gfParameters, null, null, "GetFeatureOfInterest");
        operations.add(getFeatureOfInterest);
        
        final List<DomainType> desParameters = new ArrayList<DomainType>();
        desParameters.add(new DomainType("version", Arrays.asList("1.0.0", "2.0.0")));
        desParameters.add(SERVICE_PARAMETER);
        desParameters.add(new DomainType("outputFormat", "toupdate"));
        desParameters.add(new DomainType("procedure", "toUpdate"));
        
        final Operation describeSensor = new Operation(GET_AND_POST, desParameters, null, null, "DescribeSensor");
        operations.add(describeSensor);
        
        final List<DomainType> constraints = new ArrayList<DomainType>();
        constraints.add(new DomainType("PostEncoding", "XML"));
        
        OPERATIONS_METADATA_200 = new OperationsMetadata(operations, null, constraints, null);
    }
    
    public static final List<String> PROFILES_V200 = new ArrayList<String>();
    static {
        PROFILES_V200.add("http://www.opengis.net/spec/SOS/2.0/conf/gfoi");
        PROFILES_V200.add("http://www.opengis.net/spec/SOS/2.0/conf/obsByIdRetrieval");
        PROFILES_V200.add("http://www.opengis.net/spec/SOS/2.0/conf/sensorInsertion");
        PROFILES_V200.add("http://www.opengis.net/spec/SOS/2.0/conf/sensorDeletion");
        PROFILES_V200.add("http://www.opengis.net/spec/SOS/2.0/conf/obsInsertion");
        PROFILES_V200.add("http://www.opengis.net/spec/SOS/2.0/conf/resultInsertion");
        PROFILES_V200.add("http://www.opengis.net/spec/SOS/2.0/conf/resultRetrieval");
        PROFILES_V200.add("http://www.opengis.net/spec/SOS/2.0/conf/spatialFilteringProfile");
        PROFILES_V200.add("http://www.opengis.net/spec/SOS/2.0/conf/soap");
        //PROFILES_V200.add("http://www.opengis.net/spec/SWE/2.0/conf/uml-block-components");
        //PROFILES_V200.add("http://www.opengis.net/spec/SWE/2.0/conf/uml-record-components");
        //PROFILES_V200.add("http://www.opengis.net/spec/SWE/2.0/conf/xsd-record-components");
        //PROFILES_V200.add("http://www.opengis.net/spec/SWE/2.0/conf/xsd-block-components");
        PROFILES_V200.add("http://www.opengis.net/spec/OMXML/2.0/conf/samplingPoint");
        PROFILES_V200.add("http://www.opengis.net/spec/OMXML/2.0/conf/samplingCurve");
        PROFILES_V200.add("http://www.opengis.net/spec/OMXML/2.0/conf/observation");
    }
    
    public static final InsertionCapabilitiesPropertyType INSERTION_CAPABILITIES; 
    public static final List<String> SUPPORTED_FOI_TYPES;
    public static final List<String> SUPPORTED_OBS_TYPES;
    static {
        final List<String> procedureFormat = Arrays.asList("http://www.opengis.net/sensorML/1.0.1");
        SUPPORTED_FOI_TYPES = Arrays.asList("http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint",
                                                                 "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingCurve");
        SUPPORTED_OBS_TYPES = Arrays.asList("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation",
                                                           "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        final List<String> supportedEncoding = Arrays.asList("http://www.opengis.net/swe/2.0/TextEncoding");
        final InsertionCapabilitiesType icapa = new InsertionCapabilitiesType(procedureFormat, SUPPORTED_FOI_TYPES, SUPPORTED_OBS_TYPES, supportedEncoding);
        INSERTION_CAPABILITIES = new InsertionCapabilitiesPropertyType(icapa);
    }

}

