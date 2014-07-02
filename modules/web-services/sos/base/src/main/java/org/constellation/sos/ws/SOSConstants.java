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

package org.constellation.sos.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import net.jcip.annotations.Immutable;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Service;
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
import org.geotoolkit.ogc.xml.v200.ConformanceType;
import org.geotoolkit.ows.xml.v110.NoValues;
import org.geotoolkit.ows.xml.v110.ValueType;
import org.geotoolkit.ows.xml.AbstractDCP;
import org.geotoolkit.ows.xml.AbstractDomain;
import org.geotoolkit.ows.xml.AbstractOperation;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.sos.xml.v200.InsertionCapabilitiesPropertyType;
import org.geotoolkit.sos.xml.v200.InsertionCapabilitiesType;
import org.geotoolkit.sos.xml.v100.FilterCapabilities;

import static org.geotoolkit.gml.xml.v311.ObjectFactory.*;
import org.geotoolkit.ows.xml.AbstractContact;
import org.geotoolkit.ows.xml.AbstractOnlineResourceType;
import org.geotoolkit.ows.xml.AbstractOperationsMetadata;
import org.geotoolkit.ows.xml.AbstractResponsiblePartySubset;
import org.geotoolkit.ows.xml.AbstractServiceIdentification;
import org.geotoolkit.ows.xml.AbstractServiceProvider;
import org.geotoolkit.sos.xml.Capabilities;
import org.geotoolkit.sos.xml.SOSXmlFactory;


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
    public static final String OBSERVED_PROPERTY = "observedProperty";
    public static final String EVENT_TIME = "eventTime";
    public static final String PROCEDURE = "procedure";
    public static final String SRS_NAME = "srsName";
    public static final String OBSERVATION = "observation";
    public static final String FEATURE_OF_INTEREST = "featureOfInterest";
    public static final String OBSERVATION_ID = "observationId";
    public static final String OUTPUT_FORMAT = "outputFormat";
    public static final String OBSERVATION_TEMPLATE = "observationTemplate";
    public static final String PROCEDURE_DESCRIPTION_FORMAT = "procedureDescriptionFormat";
    public static final String RESPONSE_MODE = "responseMode";
    public static final String RESPONSE_FORMAT = "responseFormat";
    public static final String RESULT_MODEL = "resultModel";
    public static final String NOT_SUPPORTED = "This operation is not take in charge by the Web Service";
    public static final String SENSORML_100_FORMAT_V100 = "text/xml;subtype=\"sensorML/1.0.0\"";
    public static final String SENSORML_101_FORMAT_V100 = "text/xml;subtype=\"sensorML/1.0.1\"";
    public static final String SENSORML_100_FORMAT_V200 = "http://www.opengis.net/sensorML/1.0.0";
    public static final String SENSORML_101_FORMAT_V200 = "http://www.opengis.net/sensorML/1.0.1";
    public static final String RESPONSE_FORMAT_V100     = "text/xml; subtype=\"om/1.0.0\"";
    public static final String RESPONSE_FORMAT_V200     = "http://www.opengis.net/om/2.0";

    public static final String OM_NAMESPACE = "http://www.opengis.net/om/1.0";
    
    /**
     * The base Qname for complex observation.
     */
    public static final QName OBSERVATION_QNAME = new QName(OM_NAMESPACE, "Observation", "om");
    
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
    public static final QName MEASUREMENT_QNAME = new QName(OM_NAMESPACE, "Measurement", "om");
    
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
        
         final List<org.geotoolkit.ows.xml.v110.DomainType> constraints = new ArrayList<>();
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
        
        final org.geotoolkit.ogc.xml.v200.FilterCapabilities capa = new org.geotoolkit.ogc.xml.v200.FilterCapabilities(scalarCapabilities, spatial, temporal, null, conformance);
        SOS_FILTER_CAPABILITIES_V200.setFilterCapabilities(capa);
    }
    
    private static final List<AbstractDCP> GET_AND_POST = new ArrayList<>();
    private static final List<AbstractDCP> ONLY_POST    = new ArrayList<>();
    static {
        GET_AND_POST.add(OWSXmlFactory.buildDCP("1.1.0", "somURL", "someURL"));
        ONLY_POST.add(OWSXmlFactory.buildDCP("1.1.0", null, "someURL"));
    }
    
    private static final AbstractDomain SERVICE_PARAMETER = OWSXmlFactory.buildDomain("1.1.0", "service", Arrays.asList("SOS"));
    
    private static final AbstractOperation GET_CAPABILITIES;
    static {
        final List<AbstractDomain> gcParameters = new ArrayList<>();
        gcParameters.add(SERVICE_PARAMETER);
        gcParameters.add(OWSXmlFactory.buildDomain("1.1.0", "Acceptversions", Arrays.asList("1.0.0", "2.0.0")));
        gcParameters.add(OWSXmlFactory.buildDomain("1.1.0", "Sections", Arrays.asList("ServiceIdentification", "ServiceProvider", "OperationsMetadata", "Filter_Capabilities", "All")));
        gcParameters.add(OWSXmlFactory.buildDomain("1.1.0", "AcceptFormats", Arrays.asList("text/xml")));
        
        GET_CAPABILITIES = OWSXmlFactory.buildOperation("1.1.0", GET_AND_POST, gcParameters, null, "GetCapabilities");
    }
    
    private static final AbstractOperation GETOBSERVATION_BY_ID;
    static {
        final List<AbstractDomain> gobidParameters = new ArrayList<>();
        gobidParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0", "2.0.0")));
        gobidParameters.add(SERVICE_PARAMETER);
        gobidParameters.add(OWSXmlFactory.buildDomainAnyValue("1.1.0", "observation"));
        
        GETOBSERVATION_BY_ID = OWSXmlFactory.buildOperation("1.1.0", GET_AND_POST, gobidParameters, null, "GetObservationById");
    }
    
    public static final AbstractOperationsMetadata OPERATIONS_METADATA_100;
    static {

        final List<AbstractOperation> operations = new ArrayList<>();
        operations.add(GET_CAPABILITIES);

        final List<AbstractDomain> rsParameters = new ArrayList<>();
        rsParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0")));
        rsParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation registerSensor = OWSXmlFactory.buildOperation("1.1.0", ONLY_POST, rsParameters, null, "RegisterSensor");
        operations.add(registerSensor);
        
        final List<AbstractDomain> grParameters = new ArrayList<>();
        grParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0", "2.0.0")));
        grParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation getResult = OWSXmlFactory.buildOperation("1.1.0", GET_AND_POST, grParameters, null, "GetResult");
        operations.add(getResult);
        
        final List<AbstractDomain> goParameters = new ArrayList<>();
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0", "2.0.0")));
        goParameters.add(SERVICE_PARAMETER);
        goParameters.add(OWSXmlFactory.buildDomainAnyValue("1.1.0", "srsName"));
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "offering", Arrays.asList("offering-AllSensor")));
        goParameters.add(OWSXmlFactory.buildDomainRange("1.1.0", "eventTime", "now", "now"));
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "procedure", Arrays.asList("toUpdate")));
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "observedProperty", Arrays.asList("toUpdate")));
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "featureOfInterest", Arrays.asList("toUpdate")));
        goParameters.add(OWSXmlFactory.buildDomainAnyValue("1.1.0", "result"));
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "responseFormat", Arrays.asList("text/xml; subtype=\"om/1.0.0\"")));
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "resultModel", Arrays.asList("om:Observation")));
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "responseMode", Arrays.asList("resultTemplate","inline")));
        
        final AbstractOperation getObservation = OWSXmlFactory.buildOperation("1.1.0", GET_AND_POST, goParameters, null, "GetObservation");
        operations.add(getObservation);
        
        operations.add(GETOBSERVATION_BY_ID);
        
        final List<AbstractDomain> ioParameters = new ArrayList<>();
        ioParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0", "2.0.0")));
        ioParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation insertObservation = OWSXmlFactory.buildOperation("1.1.0", ONLY_POST, ioParameters, null, "InsertObservation");
        operations.add(insertObservation);
        
        final List<AbstractDomain> gfParameters = new ArrayList<>();
        gfParameters.add(OWSXmlFactory.buildDomain("1.1.0", "featureOfInterestId", Arrays.asList("toUpdate")));
        gfParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0", "2.0.0")));
        gfParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation getFeatureOfInterest = OWSXmlFactory.buildOperation("1.1.0", GET_AND_POST, gfParameters, null, "GetFeatureOfInterest");
        operations.add(getFeatureOfInterest);
        
        final List<AbstractDomain> gftParameters = new ArrayList<>();
        gftParameters.add(OWSXmlFactory.buildDomain("1.1.0", "featureOfInterestId", Arrays.asList("toUpdate")));
        gftParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0")));
        gftParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation getFeatureOfInterestTime = OWSXmlFactory.buildOperation("1.1.0", ONLY_POST, gftParameters, null, "GetFeatureOfInterestTime");
        operations.add(getFeatureOfInterestTime);
        
        final List<AbstractDomain> dsParameters = new ArrayList<>();
        dsParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0", "2.0.0")));
        dsParameters.add(SERVICE_PARAMETER);
        dsParameters.add(OWSXmlFactory.buildDomain("1.1.0", "outputFormat", Arrays.asList("text/xml;subtype=\"sensorML/1.0.0\"")));
        dsParameters.add(OWSXmlFactory.buildDomain("1.1.0", "procedure", Arrays.asList("toUpdate")));
        
        final AbstractOperation describeSensor = OWSXmlFactory.buildOperation("1.1.0", GET_AND_POST, dsParameters, null, "DescribeSensor");
        operations.add(describeSensor);
        
        final List<AbstractDomain> constraints = new ArrayList<>();
        constraints.add(OWSXmlFactory.buildDomain("1.1.0", "PostEncoding", Arrays.asList("XML")));
        
        OPERATIONS_METADATA_100 = OWSXmlFactory.buildOperationsMetadata("1.1.0", operations, null, constraints, null);
    }
    
    public static final AbstractOperationsMetadata OPERATIONS_METADATA_200;
    static {

        final List<AbstractOperation> operations = new ArrayList<>();
        operations.add(GET_CAPABILITIES);

        final List<AbstractDomain> rsParameters = new ArrayList<>();
        rsParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("2.0.0")));
        rsParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation insertSensor = OWSXmlFactory.buildOperation("1.1.0", ONLY_POST, rsParameters, null, "InsertSensor");
        operations.add(insertSensor);
        
        final List<AbstractDomain> irtParameters = new ArrayList<>();
        irtParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("2.0.0")));
        irtParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation insertResultTemplate = OWSXmlFactory.buildOperation("1.1.0", ONLY_POST, irtParameters, null, "InsertResultTemplate");
        operations.add(insertResultTemplate);
        
        final List<AbstractDomain> irParameters = new ArrayList<>();
        irParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("2.0.0")));
        irParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation insertResult = OWSXmlFactory.buildOperation("1.1.0", ONLY_POST, irParameters, null, "InsertResult");
        operations.add(insertResult);
        
        final List<AbstractDomain> dsParameters = new ArrayList<>();
        dsParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("2.0.0")));
        dsParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation deleteSensor = OWSXmlFactory.buildOperation("1.1.0", GET_AND_POST, dsParameters, null,  "DeleteSensor");
        operations.add(deleteSensor);
        
        final List<AbstractDomain> grtParameters = new ArrayList<>();
        grtParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("2.0.0")));
        grtParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation getResultTemplate = OWSXmlFactory.buildOperation("1.1.0", GET_AND_POST, grtParameters, null, "GetResultTemplate");
        operations.add(getResultTemplate);
        
        final List<AbstractDomain> grParameters = new ArrayList<>();
        grParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0", "2.0.0")));
        grParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation getResult = OWSXmlFactory.buildOperation("1.1.0", GET_AND_POST, grParameters, null, "GetResult");
        operations.add(getResult);
        
        final List<AbstractDomain> goParameters = new ArrayList<>();
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0", "2.0.0")));
        goParameters.add(SERVICE_PARAMETER);
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "offering", Arrays.asList("toUpdate")));
        goParameters.add(OWSXmlFactory.buildDomainRange("1.1.0", "eventTime", "now", "now"));
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "procedure", Arrays.asList("toUpdate")));
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "observedProperty", Arrays.asList("toUpdate")));
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "featureOfInterest", Arrays.asList("toUpdate")));
        goParameters.add(OWSXmlFactory.buildDomain("1.1.0", "responseFormat", Arrays.asList("http://www.opengis.net/om/2.0")));
        
        final AbstractOperation getObservation = OWSXmlFactory.buildOperation("1.1.0", GET_AND_POST, goParameters, null, "GetObservation");
        operations.add(getObservation);
        
        operations.add(GETOBSERVATION_BY_ID);
        
        final List<AbstractDomain> ioParameters = new ArrayList<>();
        ioParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0", "2.0.0")));
        ioParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation insertObservation = OWSXmlFactory.buildOperation("1.1.0", ONLY_POST, ioParameters, null, "InsertObservation");
        operations.add(insertObservation);
        
        final List<AbstractDomain> gfParameters = new ArrayList<>();
        gfParameters.add(OWSXmlFactory.buildDomain("1.1.0", "featureOfInterestId", Arrays.asList("toUpdate")));
        gfParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0", "2.0.0")));
        gfParameters.add(SERVICE_PARAMETER);
        
        final AbstractOperation getFeatureOfInterest = OWSXmlFactory.buildOperation("1.1.0", GET_AND_POST, gfParameters, null, "GetFeatureOfInterest");
        operations.add(getFeatureOfInterest);
        
        final List<AbstractDomain> desParameters = new ArrayList<>();
        desParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0", "2.0.0")));
        desParameters.add(SERVICE_PARAMETER);
        desParameters.add(OWSXmlFactory.buildDomain("1.1.0", "outputFormat", Arrays.asList("toupdate")));
        desParameters.add(OWSXmlFactory.buildDomain("1.1.0", "procedure", Arrays.asList("toUpdate")));
        
        final AbstractOperation describeSensor = OWSXmlFactory.buildOperation("1.1.0", GET_AND_POST, desParameters, null, "DescribeSensor");
        operations.add(describeSensor);
        
        final List<AbstractDomain> constraints = new ArrayList<>();
        constraints.add(OWSXmlFactory.buildDomain("1.1.0", "PostEncoding", Arrays.asList("XML")));
        
        OPERATIONS_METADATA_200 = OWSXmlFactory.buildOperationsMetadata("1.1.0", operations, null, constraints, null);
    }
    
    public static final List<String> PROFILES_V200 = new ArrayList<>();
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

    /**
     * Generates the base capabilities for a WFS from the service metadata.
     *
     * @param metadata the service metadata
     * @return the service base capabilities
     */
    public static Capabilities createCapabilities(final String version, final Service metadata) {
        ensureNonNull("metadata", metadata);
        ensureNonNull("version",  version);

        final Contact currentContact = metadata.getServiceContact();
        final AccessConstraint constraint = metadata.getServiceConstraints();

        final AbstractServiceIdentification servIdent;
        if (constraint != null) {
            servIdent = OWSXmlFactory.buildServiceIdentification("1.1.0",
                                                                 metadata.getName(),
                                                                 metadata.getDescription(),
                                                                 metadata.getKeywords(),
                                                                 "SOS",
                                                                 metadata.getVersions(),
                                                                 constraint.getFees(),
                                                                 Arrays.asList(constraint.getAccessConstraint()));
        } else {
            servIdent = OWSXmlFactory.buildServiceIdentification("1.1.0",
                                                                 metadata.getName(),
                                                                 metadata.getDescription(),
                                                                 metadata.getKeywords(),
                                                                 "SOS",
                                                                 metadata.getVersions(),
                                                                 null,
                                                                 null);
        }

        // Create provider part.
        final AbstractServiceProvider servProv;
        if (currentContact != null) {
            final AbstractContact contact = OWSXmlFactory.buildContact("1.1.0", currentContact.getPhone(), currentContact.getFax(),
                    currentContact.getEmail(), currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                    currentContact.getZipCode(), currentContact.getCountry(), currentContact.getHoursOfService(), currentContact.getContactInstructions());

            final AbstractResponsiblePartySubset responsible = OWSXmlFactory.buildResponsiblePartySubset("1.1.0", currentContact.getFullname(), currentContact.getPosition(), contact, null);


            AbstractOnlineResourceType orgUrl = null;
            if (currentContact.getUrl() != null) {
                orgUrl = OWSXmlFactory.buildOnlineResource("1.1.0", currentContact.getUrl());
            }
            servProv = OWSXmlFactory.buildServiceProvider("1.1.0", currentContact.getOrganisation(), orgUrl, responsible);
        } else {
            servProv = OWSXmlFactory.buildServiceProvider("1.1.0", null, null, null);
        }


        // Create capabilities base.
        return SOSXmlFactory.buildCapabilities(version, servIdent, servProv, null, null, null, null, null);
    }
}

