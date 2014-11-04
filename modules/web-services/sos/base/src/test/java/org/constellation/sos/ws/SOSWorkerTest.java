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

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.apache.sis.xml.MarshallerPool;
import static org.constellation.sos.ws.SOSConstants.MEASUREMENT_QNAME;
import static org.constellation.sos.ws.SOSConstants.OBSERVATION_QNAME;
import static org.constellation.sos.ws.SOSConstants.OBSERVATION_TEMPLATE;
import static org.constellation.sos.ws.SOSConstants.OFFERING;
import static org.constellation.sos.ws.SOSConstants.PROCEDURE;
import static org.constellation.sos.ws.SOSConstants.RESPONSE_FORMAT;
import static org.constellation.sos.ws.SOSConstants.RESPONSE_MODE;
import org.constellation.test.utils.MetadataUtilities;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.MimeType;
import org.geotoolkit.gml.xml.AbstractFeature;
import org.geotoolkit.gml.xml.TimeIndeterminateValueType;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.observation.xml.v100.MeasureType;
import org.geotoolkit.observation.xml.v100.MeasurementType;
import org.geotoolkit.observation.xml.v100.ObservationCollectionType;
import org.geotoolkit.observation.xml.v100.ObservationType;
import org.geotoolkit.observation.xml.v100.ProcessType;
import org.geotoolkit.ogc.xml.v110.BBOXType;
import org.geotoolkit.ogc.xml.v110.TimeAfterType;
import org.geotoolkit.ogc.xml.v110.TimeBeforeType;
import org.geotoolkit.ogc.xml.v110.TimeDuringType;
import org.geotoolkit.ogc.xml.v110.TimeEqualsType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;
import static org.geotoolkit.ows.xml.OWSExceptionCode.VERSION_NEGOTIATION_FAILED;
import org.geotoolkit.ows.xml.v110.AcceptFormatsType;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.sampling.xml.v100.SamplingCurveType;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.sos.xml.Capabilities;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.sos.xml.v100.DescribeSensor;
import org.geotoolkit.sos.xml.v100.EventTime;
import org.geotoolkit.sos.xml.v100.GetCapabilities;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterest;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterestTime;
import org.geotoolkit.sos.xml.v100.GetObservation;
import org.geotoolkit.sos.xml.v100.GetObservationById;
import org.geotoolkit.sos.xml.v100.GetResult;
import org.geotoolkit.sos.xml.v100.GetResultResponse;
import org.geotoolkit.sos.xml.v100.InsertObservation;
import org.geotoolkit.sos.xml.v100.ObservationTemplate;
import org.geotoolkit.sos.xml.v100.RegisterSensor;
import org.geotoolkit.swe.xml.v101.AnyScalarPropertyType;
import org.geotoolkit.swe.xml.v101.DataArrayPropertyType;
import org.geotoolkit.swe.xml.v101.DataArrayType;
import org.geotoolkit.swe.xml.v101.SimpleDataRecordType;
import org.geotoolkit.swe.xml.v101.TimeType;
import org.geotoolkit.swes.xml.InsertSensorResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import org.opengis.observation.sampling.SamplingPoint;
import org.opengis.temporal.TemporalPrimitive;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
@ActiveProfiles("standard")
public abstract class SOSWorkerTest implements ApplicationContextAware {

    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected static SOSworker worker;

    protected static MarshallerPool marshallerPool;

    protected static Capabilities capabilities;

    protected static final String URL = "http://pulsar.geomatys.fr/SOServer/SOService/";

    protected static void init() throws JAXBException {
        marshallerPool = SOSMarshallerPool.getInstance();
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        capabilities = (Capabilities) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/SOSCapabilities1.0.0.xml"));
        marshallerPool.recycle(unmarshaller);
    }
    
    public abstract void initWorker();

    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    public void getCapabilitiesErrorTest() throws Exception {

        /**
         *  TEST 1 : get capabilities with wrong version (waiting for an exception)
         */
        AcceptVersionsType acceptVersions = new AcceptVersionsType("3.0.0");
        SectionsType sections             = new SectionsType("All");
        AcceptFormatsType acceptFormats   = new AcceptFormatsType(MimeType.TEXT_XML);
        GetCapabilities request           = new GetCapabilities(acceptVersions, sections, acceptFormats, null, "SOS");

        boolean exLaunched = false;
        try {
            worker.getCapabilities(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), VERSION_NEGOTIATION_FAILED);
            assertEquals(ex.getLocator(), "acceptVersion");
        }

        assertTrue(exLaunched);

         /*
         *  TEST 2 : get capabilities with wrong formats (waiting for an exception)
         */
        request = new GetCapabilities("1.0.0", "ploup/xml");

        exLaunched = false;
        try {
            worker.getCapabilities(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "acceptFormats");
        }
        assertTrue(exLaunched);
    }

    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    public void getCapabilitiesTest() throws Exception {

        /*
         *  TEST 1 : minimal getCapabilities
         */
        GetCapabilities request = new GetCapabilities("1.0.0", null);
        Capabilities result = worker.getCapabilities(request);

        assertTrue(result != null);
        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() != null);

        assertTrue(result.getContents() != null);
        assertTrue(result.getContents().getOfferings() != null);
        assertEquals("nb offering!", 10, result.getContents().getOfferings().size());

        /*
         *  TEST 2 : full get capabilities
         */
        AcceptVersionsType acceptVersions = new AcceptVersionsType("1.0.0");
        SectionsType sections             = new SectionsType("All");
        AcceptFormatsType acceptFormats   = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, null, "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() != null);
        assertTrue(result.getContents() != null);
        assertTrue(result.getContents().getOfferings() != null);
        assertEquals("nb offering!", 10, result.getContents().getOfferings().size());
        assertNotNull(result);

        /*
         *  TEST 3 : get capabilities section Operation metadata
         */
        acceptVersions = new AcceptVersionsType("1.0.0");
        sections       = new SectionsType("OperationsMetadata");
        acceptFormats  = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, null, "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result.getContents() == null);
        assertNotNull(result);

        /*
         *  TEST 4 : get capabilities section Service provider
         */
        acceptVersions = new AcceptVersionsType("1.0.0");
        sections       = new SectionsType("ServiceProvider");
        acceptFormats  = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, null, "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() != null);
        assertTrue(result.getContents() == null);
        assertNotNull(result);

        /*
         *  TEST 5 : get capabilities section Service Identification
         */
        acceptVersions = new AcceptVersionsType("1.0.0");
        sections       = new SectionsType("ServiceIdentification");
        acceptFormats  = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, null, "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result.getContents() == null);
        assertNotNull(result);

        /*
         *  TEST 6 : get capabilities section Contents
         */
        acceptVersions = new AcceptVersionsType("1.0.0");
        sections       = new SectionsType("Contents");
        acceptFormats  = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, null, "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result.getContents() != null);
        assertTrue(result.getContents().getOfferings() != null);
        assertEquals("nb offering!", 10, result.getContents().getOfferings().size());
        assertNotNull(result);

    }


    /**
     * Tests the DescribeSensor method
     *
     * @throws java.lang.Exception
     */
    public void DescribeSensorErrorTest() throws Exception {

         /**
         * Test 1 bad outputFormat
         */
        boolean exLaunched = false;
        DescribeSensor request  = new DescribeSensor("1.0.0", "SOS", "urn:ogc:object:sensor:GEOM:1", "text/xml; subtype=\"SensorML/1.0.0\"");
        try {
            worker.describeSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "outputFormat");
        }
        assertTrue(exLaunched);

        /**
         * Test 2 missing outputFormat
         */
        exLaunched = false;
        request  = new DescribeSensor("1.0.0", "SOS", "urn:ogc:object:sensor:GEOM:1", null);
        try {
            worker.describeSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "outputFormat");
        }
        assertTrue(exLaunched);

        /**
         * Test 3 missing sensorID
         */
        exLaunched = false;
        request  = new DescribeSensor("1.0.0", "SOS", null, "text/xml;subtype=\"SensorML/1.0.0\"");
        try {
            worker.describeSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), PROCEDURE);
        }
        assertTrue(exLaunched);

    }

    /**
     * Tests the DescribeSensor method
     *
     * @throws java.lang.Exception
     */
    public void DescribeSensorTest() throws Exception {
        Unmarshaller unmarshaller = SensorMLMarshallerPool.getInstance().acquireUnmarshaller();


        /**
         * Test 1 system sensor
         */
        DescribeSensor request  = new DescribeSensor("1.0.0", "SOS", "urn:ogc:object:sensor:GEOM:1", "text/xml;subtype=\"SensorML/1.0.0\"");
        AbstractSensorML absResult = (AbstractSensorML) worker.describeSensor(request);

        AbstractSensorML absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));

        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) absExpResult;

        MetadataUtilities.systemSMLEquals(expResult, result);

        /**
         * Test 2 component sensor
         */
        request  = new DescribeSensor("1.0.0", "SOS", "urn:ogc:object:sensor:GEOM:2", "text/xml;subtype=\"SensorML/1.0.0\"");
        absResult = (AbstractSensorML) worker.describeSensor(request);

        absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component.xml"));

        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        result = (SensorML) absResult;
        expResult = (SensorML) absExpResult;

        MetadataUtilities.componentEquals(expResult, result);

        SensorMLMarshallerPool.getInstance().recycle(unmarshaller);
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    public void GetObservationErrorTest() throws Exception {
        /**
         *  Test 1: getObservation with bad response format
         */
        GetObservation request  = new GetObservation("1.0.0",
                                                     "offering-4",
                                                     null,
                                                     Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                                     null,
                                                     null,
                                                     null,
                                                     "text/xml;subtype=\"om/3.0.0\"",
                                                     OBSERVATION_QNAME,
                                                     ResponseModeType.INLINE,
                                                     null);
        boolean exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), RESPONSE_FORMAT);
        }
        assertTrue(exLaunched);

        /**
         *  Test 2: getObservation with bad response format
         */
        request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      null,
                                      null,
                                      null,
                                      null,
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), RESPONSE_FORMAT);
        }
        assertTrue(exLaunched);

        /**
         *  Test 3: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with unsupported Response mode
         */
        List<EventTime> times = new ArrayList<>();
        TimePeriodType period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        TimeEqualsType filter = new TimeEqualsType(null, period);
        EventTime equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-3",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.OUT_OF_BAND,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertTrue(ex.getExceptionCode().equals(NO_APPLICABLE_CODE) || ex.getExceptionCode().equals(ExceptionCode.NO_APPLICABLE_CODE));
            assertEquals(ex.getLocator(), RESPONSE_MODE);
        }
        assertTrue(exLaunched);

        /**
         *  Test 4: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with unsupported Response mode
         */
        times = new ArrayList<>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new TimeEqualsType(null, period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-3",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.ATTACHED,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), OPERATION_NOT_SUPPORTED);
            assertEquals(ex.getLocator(), RESPONSE_MODE);
        }
        assertTrue(exLaunched);

        /**
         *  Test 5: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with no offering
         */
        times = new ArrayList<>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new TimeEqualsType(null, period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      null,
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), OFFERING);
        }
        assertTrue(exLaunched);

        /**
         *  Test 6: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with wrong offering
         */
        times = new ArrayList<>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new TimeEqualsType(null, period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "inexistant-offering",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), OFFERING);
        }
        assertTrue(exLaunched);

        /**
         *  Test 7: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with wrong srsName
         */
        times = new ArrayList<>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new TimeEqualsType(null, period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-3",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      "EPSG:3333");
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "srsName");
        }
        assertTrue(exLaunched);


        /**
         *  Test 8: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with wrong resultModel
         */
        times = new ArrayList<>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new TimeEqualsType(null, period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-3",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      new QName("some_namespace", "some_localPart"),
                                      ResponseModeType.INLINE,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "resultModel");
        }
        assertTrue(exLaunched);

        /**
         *  Test 9: getObservation with unexisting procedure
         *          + Time filter TEquals
         *
         */
        times = new ArrayList<>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new TimeEqualsType(null, period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-8",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:36"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), PROCEDURE);
        }
        assertTrue(exLaunched);

        /**
         *  Test 10: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *          and with wrong observed prop
         */
        request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:hotness"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);

        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "observedProperty");
        }
        assertTrue(exLaunched);

        /**
         *  Test 11: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *          and with wrong foi
         */
        request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      new GetObservation.FeatureOfInterest(Arrays.asList("NIMP")),
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);

        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "featureOfInterest");
        }
        assertTrue(exLaunched);
    }



    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    public void GetObservationTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        /**
         *  Test 1: getObservation with procedure urn:ogc:object:sensor:GEOM:4 and no resultModel
         */
        GetObservation request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      null,
                                      ResponseModeType.INLINE,
                                      null);
        ObservationCollectionType result = (ObservationCollectionType) worker.getObservation(request);

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observation3.xml"));

        ObservationType expResult = (ObservationType)obj.getValue();

        assertEquals(result.getMember().size(), 1);

        ObservationType obsResult = (ObservationType) result.getMember().iterator().next();


        assertTrue(obsResult != null);
        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertTrue("not a dataArray. Was:" + obsResult.getResult(), obsResult.getResult() instanceof DataArrayPropertyType);
        assertTrue("not a dataArray. Was:" + obsResult.getResult(), expResult.getResult() instanceof DataArrayPropertyType);

        DataArrayPropertyType expR = (DataArrayPropertyType) expResult.getResult();
        DataArrayPropertyType obsR = (DataArrayPropertyType) obsResult.getResult();

        assertTrue(obsR.getDataArray().getElementType() instanceof SimpleDataRecordType);
        SimpleDataRecordType expSdr = (SimpleDataRecordType) expR.getDataArray().getElementType();
        SimpleDataRecordType obsSdr = (SimpleDataRecordType) obsR.getDataArray().getElementType();
        obsSdr.setBlockId(null);

        Iterator<AnyScalarPropertyType> i1 = expSdr.getField().iterator();
        Iterator<AnyScalarPropertyType> i2 = obsSdr.getField().iterator();
        TimeType expT = (TimeType) i1.next().getValue();
        TimeType obsT = (TimeType) i2.next().getValue();

        assertEquals(expT.getUom(), obsT.getUom());
        assertEquals(expT, obsT);
        assertEquals(i1.next(), i2.next());

        // do not compare datarray name (ID) because it depends on the implementation
        emptyNameAndId(expR.getDataArray(), obsR.getDataArray());
        
        assertEquals(expSdr, obsSdr);
        assertEquals(expR.getDataArray().getElementType(),     obsR.getDataArray().getElementType());
        assertEquals(expR.getDataArray().getEncoding(),        obsR.getDataArray().getEncoding());
        assertEquals(expR.getDataArray().getValues(),          obsR.getDataArray().getValues());
        assertEquals(expR.getDataArray().getId(),              obsR.getDataArray().getId());
        assertEquals(expR.getDataArray().getElementCount(),    obsR.getDataArray().getElementCount());
        assertEquals(expR.getDataArray().getName(),            obsR.getDataArray().getName());
        assertEquals(expR.getDataArray().getPropertyElementType(), obsR.getDataArray().getPropertyElementType());
        assertEquals(expR.getDataArray().getPropertyEncoding(), obsR.getDataArray().getPropertyEncoding());
        assertEquals(expR.getDataArray().getElementCount(),     obsR.getDataArray().getElementCount());
        assertEquals(expR.getDataArray().getDefinition(),       obsR.getDataArray().getDefinition());
        assertEquals(expR.getDataArray().getDescription(),      obsR.getDataArray().getDescription());
        assertEquals(expR.getDataArray().getParameterName(),    obsR.getDataArray().getParameterName());
        assertEquals(expR.getDataArray().getDescriptionReference(),                      obsR.getDataArray().getDescriptionReference());
        assertEquals(expR.getDataArray().isFixed(),                      obsR.getDataArray().isFixed());
        assertEquals(expR.getDataArray(),                      obsR.getDataArray());

        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 2: getObservation with procedure urn:ogc:object:sensor:GEOM:4 avec responseMode null
         */
        request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      null,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observation3.xml"));

        expResult = (ObservationType)obj.getValue();

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationType) result.getMember().iterator().next();

        assertTrue(obsResult != null);
        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());

        // do not compare datarray name (ID) because it depends on the implementation
        expR = (DataArrayPropertyType) expResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);


        /**
         *  Test 3: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         */
        request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observation3.xml"));

        expResult = (ObservationType)obj.getValue();

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationType) result.getMember().iterator().next();


        assertTrue(obsResult != null);
        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        
        // do not compare datarray name (ID) because it depends on the implementation
        expR = (DataArrayPropertyType) expResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 4: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         */
        request  = new GetObservation("1.0.0",
                                      "offering-3",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);
        obsResult =  (ObservationType) result.getMember().iterator().next();
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        obsR      = (DataArrayPropertyType) obsResult.getResult();
        assertEquals(obsR.getDataArray().getElementCount().getCount().getValue(), (Object)15);

        /**
         *  Test 5: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TBefore
         */
        List<EventTime> times = new ArrayList<>();
        TimeInstantType instant = new TimeInstantType(new TimePositionType("2007-05-01T03:00:00.0"));
        TimeBeforeType filter   = new TimeBeforeType(null, instant);
        EventTime before        = new EventTime(filter);
        times.add(before);
        request  = new GetObservation("1.0.0",
                                      "offering-3",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);

        assertEquals(result.getMember().iterator().next().getName().getCode(), "urn:ogc:object:observation:GEOM:304");

        /**
         *  Test 6: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TAFter
         */
        times = new ArrayList<>();
        TimeAfterType afilter   = new TimeAfterType(null, instant);
        EventTime after         = new EventTime(afilter);
        times.add(after);
        request  = new GetObservation("1.0.0",
                                      "offering-3",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);

        obsResult =  (ObservationType) result.getMember().iterator().next();
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        obsR      = (DataArrayPropertyType) obsResult.getResult();
        assertEquals((Integer)14, obsR.getDataArray().getElementCount().getCount().getValue());

        /**
         *  Test 7: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TDuring
         */
        times = new ArrayList<>();
        TimePeriodType period  = new TimePeriodType(new TimePositionType("2007-05-01T03:00:00.0"), new TimePositionType("2007-05-01T08:00:00.0"));
        TimeDuringType dfilter = new TimeDuringType(null, period);
        EventTime during       = new EventTime(dfilter);
        times.add(during);
        request  = new GetObservation("1.0.0",
                                      "offering-3",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);

        obsResult =  (ObservationType) result.getMember().iterator().next();
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        obsR      = (DataArrayPropertyType) obsResult.getResult();
        assertEquals((Integer)5, obsR.getDataArray().getElementCount().getCount().getValue());

        /**
         *  Test 8: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         */
        times = new ArrayList<>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        TimeEqualsType efilter = new TimeEqualsType(null, period);
        EventTime equals = new EventTime(efilter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-3",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);

        obsResult =  (ObservationType) result.getMember().iterator().next();
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        obsR      = (DataArrayPropertyType) obsResult.getResult();
        assertTrue(obsR.getDataArray().getElementCount().getCount().getValue() == 5);


        /**
         *  Test 9: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with resultTemplate mode
         */
        request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observationTemplate-4.xml"));

        expResult = (ObservationType)obj.getValue();

        //for template the sampling time is 1970 to now
        period = new TimePeriodType(new TimePositionType("1900-01-01T00:00:00"));
        expResult.setSamplingTime(period);

        // and we empty the result object
        DataArrayPropertyType arrayP = (DataArrayPropertyType) expResult.getResult();
        DataArrayType array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        expResult.setName("urn:ogc:object:observation:template:GEOM:4-0");

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationType) result.getMember().iterator().next();

        assertTrue(obsResult != null);
        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        
        expR = (DataArrayPropertyType) expResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 10: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with resultTemplate mode
         *           with timeFilter TEquals
         */
        times = new ArrayList<>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        efilter = new TimeEqualsType(null, period);
        equals = new EventTime(efilter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observationTemplate-4.xml"));

        expResult = (ObservationType)obj.getValue();

        //for template the sampling time is 1970 to now
        expResult.setSamplingTime(period);

        // and we empty the result object
        arrayP = (DataArrayPropertyType) expResult.getResult();
        array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        expResult.setName("urn:ogc:object:observation:template:GEOM:4-1");

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationType) result.getMember().iterator().next();


        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        
        expR = (DataArrayPropertyType) expResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 11: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with resultTemplate mode
         *           with timeFilter Tafter
         */
        times = new ArrayList<>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T17:58:00.0"));
        afilter = new TimeAfterType(null, instant);
        after = new EventTime(afilter);
        times.add(after);
        request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observationTemplate-4.xml"));

        expResult = (ObservationType)obj.getValue();

        //for template the sampling time is 1970 to now
        period = new TimePeriodType(instant.getTimePosition());
        expResult.setSamplingTime(period);

        expResult.setName("urn:ogc:object:observation:template:GEOM:4-2");

        // and we empty the result object
        arrayP = (DataArrayPropertyType) expResult.getResult();
        array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationType) result.getMember().iterator().next();


        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        
        expR = (DataArrayPropertyType) expResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 12: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with resultTemplate mode
         *           with timeFilter Tbefore
         */
        times = new ArrayList<>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T17:58:00.0"));
        TimeBeforeType bfilter = new TimeBeforeType(null, instant);
        before = new EventTime(bfilter);
        times.add(before);
        request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observationTemplate-4.xml"));

        expResult = (ObservationType)obj.getValue();

        //for template the sampling time is 1970 to now
        period = new TimePeriodType(TimeIndeterminateValueType.BEFORE, instant.getTimePosition());
        expResult.setSamplingTime(period);

        expResult.setName("urn:ogc:object:observation:template:GEOM:4-3");

        // and we empty the result object
        arrayP = (DataArrayPropertyType) expResult.getResult();
        array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationType) result.getMember().iterator().next();


        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        
        expR = (DataArrayPropertyType) expResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 13: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with observedproperties = urn:ogc:def:phenomenon:GEOM:depth
         */
        request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:depth"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observation3.xml"));

        expResult = (ObservationType)obj.getValue();
        assertEquals(result.getMember().size(), 1);


        obsResult = (ObservationType) result.getMember().iterator().next();
        assertTrue(obsResult != null);

        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());

        // do not compare datarray name (ID) because it depends on the implementation
        expR = (DataArrayPropertyType) expResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 14: getObservation with procedure urn:ogc:object:sensor:GEOM:5
         *           with observedproperties = urn:ogc:def:phenomenon:GEOM:aggreagtePhenomenon
         */
        request  = new GetObservation("1.0.0",
                                      "offering-5",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:5"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observation5.xml"));

        expResult = (ObservationType)obj.getValue();
        assertEquals(result.getMember().size(), 1);


        obsResult = (ObservationType) result.getMember().iterator().next();
        assertTrue(obsResult != null);

        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        
        // do not compare datarray name (ID) because it depends on the implementation
        expR = (DataArrayPropertyType) expResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 15: getObservation with procedure urn:ogc:object:sensor:GEOM:5
         *           with observedproperties = urn:ogc:def:phenomenon:GEOM:aggreagtePhenomenon
         *           with foi                =  10972X0137-PLOUF
         */
        request  = new GetObservation("1.0.0",
                                      "offering-5",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:5"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"),
                                      new GetObservation.FeatureOfInterest(Arrays.asList("station-002")),
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observation5.xml"));

        expResult = (ObservationType)obj.getValue();
        assertEquals(result.getMember().size(), 1);


        obsResult = (ObservationType) result.getMember().iterator().next();
        assertTrue(obsResult != null);

        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        
        // do not compare datarray name (ID) because it depends on the implementation
        expR = (DataArrayPropertyType) expResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 16: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *           with observedProperties = urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon
         *           => no error but no result
         */
        request  = new GetObservation("1.0.0",
                                      "offering-3",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"),
                                      new GetObservation.FeatureOfInterest(Arrays.asList("station-002")),
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);


        ObservationCollectionType collExpResult = new ObservationCollectionType("urn:ogc:def:nil:OGC:inapplicable");
        assertEquals(collExpResult, result);

        /**
         *  Test 17: getObservation with procedure urn:ogc:object:sensor:GEOM:4 AND BBOX Filter
         */
        request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      new GetObservation.FeatureOfInterest(new BBOXType(null, 64000.0, 1730000.0, 66000.0, 1740000.0, "urn:ogc:def:crs:EPSG:27582")),
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observation3.xml"));

        expResult = (ObservationType)obj.getValue();

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationType) result.getMember().iterator().next();


        assertTrue(obsResult != null);
        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        
        // do not compare datarray name (ID) because it depends on the implementation
        expR = (DataArrayPropertyType) expResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 18: getObservation with procedure urn:ogc:object:sensor:GEOM:4 AND BBOX Filter (no result expected)
         */
        request  = new GetObservation("1.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      new GetObservation.FeatureOfInterest(new BBOXType(null, 66000.0, 1730000.0, 67000.0, 1740000.0, "urn:ogc:def:crs:EPSG:27582")),
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        collExpResult = new ObservationCollectionType("urn:ogc:def:nil:OGC:inapplicable");
        assertEquals(collExpResult, result);


        marshallerPool.recycle(unmarshaller);
    }

     /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    public void GetObservationMeasurementTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        
        /**
         *  Test 1: getObservation with procedure urn:ogc:object:sensor:GEOM:7
         *           with resultTemplate mode
         *  => measurement type
         */
        GetObservation request  = new GetObservation("1.0.0",
                                      "offering-7",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:7"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      new GetObservation.FeatureOfInterest(Arrays.asList("station-002")),
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      MEASUREMENT_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        ObservationCollectionType result = (ObservationCollectionType) worker.getObservation(request);

        assertTrue(result.getMember().iterator().next() instanceof MeasurementType);

        MeasurementType measResult =  (MeasurementType) result.getMember().iterator().next();
        assertTrue(measResult != null);

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-7.xml"));

        ObservationType expResult = (ObservationType)obj.getValue();

        TimePeriodType period = new TimePeriodType(new TimePositionType("1900-01-01T00:00:00"));
        expResult.setSamplingTime(period);
        expResult.setName("urn:ogc:object:observation:template:GEOM:7-0");

        assertEquals(expResult.getName(), measResult.getName());

        assertTrue(measResult.getResult() instanceof MeasureType);
        MeasureType resMeas = (MeasureType) measResult.getResult();
        MeasureType expMeas = (MeasureType) expResult.getResult();
        
        assertEquals(expMeas, resMeas);
        assertEquals(expResult.getResult(), measResult.getResult());
        assertEquals(expResult, measResult);
        
        /**
         *  Test 2: getObservation with procedure urn:ogc:object:sensor:GEOM:9
         *
         *  => measurement type
         */
        request  = new GetObservation("1.0.0",
                                      "offering-9",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:9"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:depth"),
                                      new GetObservation.FeatureOfInterest(Arrays.asList("station-006")),
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      MEASUREMENT_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);

        assertTrue(result.getMember().iterator().next() instanceof MeasurementType);

        measResult =  (MeasurementType) result.getMember().iterator().next();
        assertTrue(measResult != null);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/measure1.xml"));

        expResult = (MeasurementType)obj.getValue();

        assertEquals(expResult.getName(), measResult.getName());

        assertTrue(measResult.getResult() instanceof MeasureType);
        resMeas = (MeasureType) measResult.getResult();
        expMeas = (MeasureType) expResult.getResult();
        
        assertEquals(expMeas, resMeas);
        assertEquals(expResult.getResult(), measResult.getResult());
        assertEquals(expResult, measResult);
        
        marshallerPool.recycle(unmarshaller);
    }
    
     /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    public void GetObservationSamplingCurveTest() throws Exception {

        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        /**
         *  Test 1: getObservation with procedure urn:ogc:object:sensor:GEOM:8
         *           with resultTemplate mode
         */
        GetObservation request  = new GetObservation("1.0.0",
                                      "offering-8",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:8"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        ObservationCollectionType result = (ObservationCollectionType) worker.getObservation(request);
        java.io.Reader ioReader = new InputStreamReader(Util.getResourceAsStream("org/constellation/sos/observationTemplate-8.xml"), "UTF-8");
        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(ioReader);

        ObservationType expResult = (ObservationType)obj.getValue();

        //for template the sampling time is 1970 to now
        TimePeriodType period = new TimePeriodType(new TimePositionType("1900-01-01T00:00:00"));
        expResult.setSamplingTime(period);

        // and we empty the result object
        DataArrayPropertyType arrayP = (DataArrayPropertyType) expResult.getResult();
        DataArrayType array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        expResult.setName("urn:ogc:object:observation:template:GEOM:8-0");

        assertEquals(result.getMember().size(), 1);

        ObservationType obsResult = (ObservationType) result.getMember().iterator().next();

        assertTrue(obsResult != null);
        assertEquals(expResult.getName(), obsResult.getName());
        assertTrue(obsResult.getFeatureOfInterest() instanceof SamplingCurveType);
        SamplingCurveType sampCurveResult    = (SamplingCurveType) obsResult.getFeatureOfInterest();
        SamplingCurveType sampCurveRxpResult = (SamplingCurveType) expResult.getFeatureOfInterest();
        assertEquals(sampCurveResult.getLength(), sampCurveRxpResult.getLength());
        assertEquals(sampCurveResult.getShape(), sampCurveRxpResult.getShape());
        assertEquals(sampCurveResult.getBoundedBy(), sampCurveRxpResult.getBoundedBy());
        assertEquals(sampCurveResult.getSampledFeatures(), sampCurveRxpResult.getSampledFeatures());
        assertEquals(sampCurveResult.getLocation(), sampCurveRxpResult.getLocation());
        assertEquals(sampCurveResult.getId(), sampCurveRxpResult.getId());
        assertEquals(sampCurveResult, sampCurveRxpResult);
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        DataArrayPropertyType arrayPropResult    = (DataArrayPropertyType) obsResult.getResult();
        DataArrayPropertyType arrayPropExpResult = (DataArrayPropertyType) expResult.getResult();
        
        // do not compare datarray name (ID) because it depends on the implementation
        emptyNameAndId(arrayPropResult.getDataArray(),  arrayPropExpResult.getDataArray());
        
        assertEquals(arrayPropResult.getDataArray().getEncoding(), arrayPropExpResult.getDataArray().getEncoding());
        assertEquals(arrayPropResult.getDataArray().getId(), arrayPropExpResult.getDataArray().getId());
        assertEquals(arrayPropResult.getDataArray().getElementType().getId(), arrayPropExpResult.getDataArray().getElementType().getId());
        assertEquals(arrayPropResult.getDataArray().getElementType(), arrayPropExpResult.getDataArray().getElementType());
        assertEquals(arrayPropResult.getDataArray(), arrayPropExpResult.getDataArray());
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);


        /**
         *  Test 2: getObservation with procedure urn:ogc:object:sensor:GEOM:8
         *
         */
        request  = new GetObservation("1.0.0",
                                      "offering-8",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:8"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);
        obsResult = (ObservationType) result.getMember().iterator().next();

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observation6.xml"));
        expResult = (ObservationType)obj.getValue();

        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        
        arrayPropResult    = (DataArrayPropertyType) obsResult.getResult();
        arrayPropExpResult = (DataArrayPropertyType) expResult.getResult();
        
        // do not compare datarray name (ID) because it depends on the implementation
        emptyNameAndId(arrayPropResult.getDataArray(),  arrayPropExpResult.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 3: getObservation with no procedure And FID = station-006
         *
         */
        request  = new GetObservation("1.0.0",
                                      "offering-8",
                                      null,
                                      null,
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      new GetObservation.FeatureOfInterest(Arrays.asList("station-006")),
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionType) worker.getObservation(request);
        obsResult = (ObservationType) result.getMember().iterator().next();

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observation6.xml"));
        expResult = (ObservationType)obj.getValue();

        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        
        arrayPropResult    = (DataArrayPropertyType) obsResult.getResult();
        arrayPropExpResult = (DataArrayPropertyType) expResult.getResult();
        
        // do not compare datarray name (ID) because it depends on the implementation
        emptyNameAndId(arrayPropResult.getDataArray(),  arrayPropExpResult.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        marshallerPool.recycle(unmarshaller);
    }

    
    public void GetObservationByIdTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        
        GetObservationById request = new GetObservationById("1.0.0", "urn:ogc:object:observation:GEOM:304", "text/xml; subtype=\"om/1.0.0\"", OBSERVATION_QNAME, ResponseModeType.INLINE, "EPSG:4326");
        
        ObservationCollectionType response = (ObservationCollectionType) worker.getObservationById(request);
        
        ObservationType result = (ObservationType) response.getMember().get(0);
        
        java.io.Reader ioReader = new InputStreamReader(Util.getResourceAsStream("org/constellation/sos/v100/observation1.xml"), "UTF-8");
        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(ioReader);

        ObservationType expResult = (ObservationType)obj.getValue();

        assertEquals(expResult.getFeatureOfInterest(), result.getFeatureOfInterest());
        DataArrayPropertyType expArray = (DataArrayPropertyType)expResult.getResult();
        DataArrayPropertyType resArray = (DataArrayPropertyType)result.getResult();
        assertEquals(expArray.getDataArray().getElementType(), resArray.getDataArray().getElementType());
        assertEquals(expArray.getDataArray().getEncoding(), resArray.getDataArray().getEncoding());
        assertEquals(expArray.getDataArray().getValues(), resArray.getDataArray().getValues());
        
        // do not compare datarray name (ID) because it depends on the implementation
        emptyNameAndId(expArray.getDataArray(),  resArray.getDataArray());
                
        assertEquals(expArray.getDataArray().getPropertyElementType(), resArray.getDataArray().getPropertyElementType());
        assertEquals(expArray.getDataArray().getPropertyEncoding(), resArray.getDataArray().getPropertyEncoding());
        assertEquals(expArray.getDataArray(), resArray.getDataArray());
        assertEquals(expArray, resArray);
        
        assertEquals(expResult.getObservedProperty(), result.getObservedProperty());
        assertEquals(expResult.getProcedure(), result.getProcedure());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult.getSamplingTime(), result.getSamplingTime());
        assertEquals(expResult, result);
        
        request = new GetObservationById("1.0.0", "urn:ogc:object:observation:GEOM:901", "text/xml; subtype=\"om/1.0.0\"", MEASUREMENT_QNAME, ResponseModeType.INLINE, "EPSG:4326");
        
        response = (ObservationCollectionType) worker.getObservationById(request);

        assertTrue(response.getMember().iterator().next() instanceof MeasurementType);

        MeasurementType measResult =  (MeasurementType) response.getMember().iterator().next();
        assertTrue(measResult != null);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/measure1.xml"));

        expResult = (MeasurementType)obj.getValue();

        assertEquals(expResult.getName(), measResult.getName());

        assertTrue(measResult.getResult() instanceof MeasureType);
        MeasureType resMeas = (MeasureType) measResult.getResult();
        MeasureType expMeas = (MeasureType) expResult.getResult();
        
        assertEquals(expMeas, resMeas);
        assertEquals(expResult.getResult(), measResult.getResult());
        assertEquals(expResult, measResult);
        
        marshallerPool.recycle(unmarshaller);
    }
    
    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    public void GetResultErrorTest() throws Exception {
        /**
         * Test 1: bad version number + null template ID
         */
        String templateId = null;
        GetResult request = new GetResult(templateId, null, "3.0.0");
        boolean exLaunched = false;
        try {
            worker.getResult(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

        /**
         * Test 2:  null template ID
         */
        templateId = null;
        request = new GetResult(templateId, null, "1.0.0");
        exLaunched = false;
        try {
            worker.getResult(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "ObservationTemplateId");
        }
        assertTrue(exLaunched);

        /**
         * Test 3:  bad template ID
         */
        templateId = "some id";
        request = new GetResult(templateId, null, "1.0.0");
        exLaunched = false;
        try {
            worker.getResult(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "ObservationTemplateId");
        }
        assertTrue(exLaunched);
    }

    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    public void GetResultTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();



        // we make a getObservation request in order to get a template

        /**
         *   getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with resultTemplate mode
         */
        GetObservation GOrequest  = new GetObservation("1.0.0",
                                      "offering-3",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        ObservationCollectionType obsCollResult = (ObservationCollectionType) worker.getObservation(GOrequest);

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observationTemplate-3.xml"));

        ObservationType templateExpResult = (ObservationType)obj.getValue();

        //for template the sampling time is 1970 to now
        TimePeriodType period = new TimePeriodType(new TimePositionType("1900-01-01T00:00:00"));
        templateExpResult.setSamplingTime(period);

        // and we empty the result object
        DataArrayPropertyType arrayP = (DataArrayPropertyType) templateExpResult.getResult();
        DataArrayType array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        templateExpResult.setName("urn:ogc:object:observation:template:GEOM:3-0");

        assertEquals(obsCollResult.getMember().size(), 1);

        ObservationType obsResult = (ObservationType) obsCollResult.getMember().iterator().next();

        assertNotNull(obsResult);

        DataArrayPropertyType obsR = (DataArrayPropertyType) obsResult.getResult();
        SimpleDataRecordType obsSdr = (SimpleDataRecordType) obsR.getDataArray().getElementType();
        obsSdr.setBlockId(null);

        assertNotNull(obsResult);
        assertEquals(templateExpResult.getName(), obsResult.getName());
        assertEquals(templateExpResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(templateExpResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(templateExpResult.getProcedure(), obsResult.getProcedure());
        
        // do not compare datarray name (ID) because it depends on the implementation
        emptyNameAndId(((DataArrayPropertyType)templateExpResult.getResult()).getDataArray(), ((DataArrayPropertyType)obsResult.getResult()).getDataArray());
        
        assertEquals(((DataArrayPropertyType)templateExpResult.getResult()).getDataArray().getEncoding(), ((DataArrayPropertyType)obsResult.getResult()).getDataArray().getEncoding());
        assertEquals(((DataArrayPropertyType)templateExpResult.getResult()).getDataArray(), ((DataArrayPropertyType)obsResult.getResult()).getDataArray());
        assertEquals(templateExpResult.getResult(), obsResult.getResult());
        assertEquals(templateExpResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(templateExpResult, obsResult);

        /**
         * Test 1:  getResult with no TimeFilter
         */
        String templateId = "urn:ogc:object:observation:template:GEOM:3-0";
        GetResult request = new GetResult(templateId, null, "1.0.0");
        GetResultResponse result = (GetResultResponse) worker.getResult(request);

        String value = "2007-05-01T02:59:00,6.56@@2007-05-01T03:59:00,6.56@@2007-05-01T04:59:00,6.56@@2007-05-01T05:59:00,6.56@@2007-05-01T06:59:00,6.56@@" +
                       "2007-05-01T07:59:00,6.56@@2007-05-01T08:59:00,6.56@@2007-05-01T09:59:00,6.56@@2007-05-01T10:59:00,6.56@@2007-05-01T11:59:00,6.56@@" +
                       "2007-05-01T17:59:00,6.56@@2007-05-01T18:59:00,6.55@@2007-05-01T19:59:00,6.55@@2007-05-01T20:59:00,6.55@@2007-05-01T21:59:00,6.55@@";
        GetResultResponse expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + "sos/default/" + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        /**
         *   getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *   with resultTemplate mode and time filter TBefore
         */
        List<EventTime> times = new ArrayList<>();
        TimeInstantType instant = new TimeInstantType(new TimePositionType("2007-05-01T05:00:00.0"));
        TimeBeforeType bfilter = new TimeBeforeType(null, instant);
        EventTime before = new EventTime(bfilter);
        times.add(before);
        GOrequest  = new GetObservation("1.0.0",
                                        "offering-3",
                                        times,
                                        Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                        Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                        null,
                                        null,
                                        "text/xml; subtype=\"om/1.0.0\"",
                                        OBSERVATION_QNAME,
                                        ResponseModeType.RESULT_TEMPLATE,
                                        null);
        obsCollResult = (ObservationCollectionType) worker.getObservation(GOrequest);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observationTemplate-3.xml"));

        templateExpResult = (ObservationType)obj.getValue();

        //for template the sampling time is 1970 to now
        period = new TimePeriodType(TimeIndeterminateValueType.BEFORE, new TimePositionType("2007-05-01T05:00:00.0"));
        templateExpResult.setSamplingTime(period);

        // and we empty the result object
        arrayP = (DataArrayPropertyType) templateExpResult.getResult();
        array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        templateExpResult.setName("urn:ogc:object:observation:template:GEOM:3-1");

        assertEquals(obsCollResult.getMember().size(), 1);

        obsResult = (ObservationType) obsCollResult.getMember().iterator().next();

        obsR = (DataArrayPropertyType) obsResult.getResult();
        obsSdr = (SimpleDataRecordType) obsR.getDataArray().getElementType();
        obsSdr.setBlockId(null);

        assertNotNull(obsResult);
        assertEquals(templateExpResult.getName(), obsResult.getName());
        assertEquals(templateExpResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(templateExpResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(templateExpResult.getProcedure(), obsResult.getProcedure());

        // do not compare datarray name (ID) because it depends on the implementation
        DataArrayPropertyType expR = (DataArrayPropertyType) templateExpResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(templateExpResult.getResult(), obsResult.getResult());
        assertEquals(templateExpResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(templateExpResult, obsResult);

        /**
         * Test 2:  getResult with no TimeFilter
         */
        templateId = "urn:ogc:object:observation:template:GEOM:3-1";
        request = new GetResult(templateId, null, "1.0.0");
        result = (GetResultResponse) worker.getResult(request);

        value = "2007-05-01T02:59:00,6.56@@2007-05-01T03:59:00,6.56@@2007-05-01T04:59:00,6.56@@";
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + "sos/default/" + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

         /**
         * Test 3:  getResult with Tafter
         */
        times = new ArrayList<>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T03:00:00.0"));
        TimeAfterType afilter = new TimeAfterType(null, instant);
        EventTime after = new EventTime(afilter);
        times.add(after);

        templateId = "urn:ogc:object:observation:template:GEOM:3-1";
        request = new GetResult(templateId, times, "1.0.0");
        result = (GetResultResponse) worker.getResult(request);

        value = "2007-05-01T03:59:00,6.56@@2007-05-01T04:59:00,6.56@@";
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + "sos/default/" + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        /**
         * Test 4:  getResult with Tbefore
         */
        times = new ArrayList<>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T04:00:00.0"));
        bfilter = new TimeBeforeType(null, instant);
        EventTime before2 = new EventTime(bfilter);
        times.add(before2);

        templateId = "urn:ogc:object:observation:template:GEOM:3-1";
        request = new GetResult(templateId, times, "1.0.0");
        result = (GetResultResponse) worker.getResult(request);

        value = "2007-05-01T02:59:00,6.56@@2007-05-01T03:59:00,6.56@@";
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + "sos/default/" + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        /**
         * Test 5:  getResult with TEquals
         */
        times = new ArrayList<>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T03:59:00.0"));
        TimeEqualsType efilter = new TimeEqualsType(null, instant);
        EventTime equals = new EventTime(efilter);
        times.add(equals);

        templateId = "urn:ogc:object:observation:template:GEOM:3-1";
        request = new GetResult(templateId, times, "1.0.0");
        result = (GetResultResponse) worker.getResult(request);

        value = "2007-05-01T03:59:00,6.56@@";
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + "sos/default/" + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        /**
         * Test 6:  getResult with TEquals
         */
        times = new ArrayList<>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T03:00:00.0"), new TimePositionType("2007-05-01T04:00:00.0"));
        TimeDuringType dfilter = new TimeDuringType(null, period);
        EventTime during = new EventTime(dfilter);
        times.add(during);

        templateId = "urn:ogc:object:observation:template:GEOM:3-1";
        request = new GetResult(templateId, times, "1.0.0");
        result = (GetResultResponse) worker.getResult(request);

        value = "2007-05-01T03:59:00,6.56@@";
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + "sos/default/" + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);


         /**
         *   getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *   with resultTemplate mode and time filter TAfter
         */
        times = new ArrayList<>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T19:00:00.0"));
        afilter = new TimeAfterType(null, instant);
        after = new EventTime(afilter);
        times.add(after);
        GOrequest  = new GetObservation("1.0.0",
                                        "offering-3",
                                        times,
                                        Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                        Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                        null,
                                        null,
                                        "text/xml; subtype=\"om/1.0.0\"",
                                        OBSERVATION_QNAME,
                                        ResponseModeType.RESULT_TEMPLATE,
                                        null);
        obsCollResult = (ObservationCollectionType) worker.getObservation(GOrequest);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observationTemplate-3.xml"));

        templateExpResult = (ObservationType)obj.getValue();

        //for template the sampling time is 1970 to now
        period = new TimePeriodType(new TimePositionType("2007-05-01T19:00:00.0"));
        templateExpResult.setSamplingTime(period);

        // and we empty the result object
        arrayP = (DataArrayPropertyType) templateExpResult.getResult();
        array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        templateExpResult.setName("urn:ogc:object:observation:template:GEOM:3-2");

        assertEquals(obsCollResult.getMember().size(), 1);

        obsResult = (ObservationType) obsCollResult.getMember().iterator().next();

        obsR = (DataArrayPropertyType) obsResult.getResult();
        obsSdr = (SimpleDataRecordType) obsR.getDataArray().getElementType();
        obsSdr.setBlockId(null);

        assertNotNull(obsResult);
        assertEquals(templateExpResult.getName(), obsResult.getName());
        assertEquals(templateExpResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(templateExpResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(templateExpResult.getProcedure(), obsResult.getProcedure());
        
        // do not compare datarray name (ID) because it depends on the implementation
        expR = (DataArrayPropertyType) templateExpResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(templateExpResult.getResult(), obsResult.getResult());
        assertEquals(templateExpResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(templateExpResult, obsResult);


        /**
         * Test 7:  getResult with no TimeFilter
         */
        templateId = "urn:ogc:object:observation:template:GEOM:3-2";
        request = new GetResult(templateId, null, "1.0.0");
        result = (GetResultResponse) worker.getResult(request);

        value = "2007-05-01T19:59:00,6.55@@2007-05-01T20:59:00,6.55@@2007-05-01T21:59:00,6.55@@";
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + "sos/default/" + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

         /**
         *   getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *   with resultTemplate mode and time filter TEquals
         */
        times = new ArrayList<>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T20:59:00.0"));
        efilter = new TimeEqualsType(null, instant);
        equals = new EventTime(efilter);
        times.add(equals);
        GOrequest  = new GetObservation("1.0.0",
                                        "offering-3",
                                        times,
                                        Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                        Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                        null,
                                        null,
                                        "text/xml; subtype=\"om/1.0.0\"",
                                        OBSERVATION_QNAME,
                                        ResponseModeType.RESULT_TEMPLATE,
                                        null);
        obsCollResult = (ObservationCollectionType) worker.getObservation(GOrequest);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observationTemplate-3.xml"));

        templateExpResult = (ObservationType)obj.getValue();

        instant = new TimeInstantType(new TimePositionType("2007-05-01T20:59:00.0"));
        templateExpResult.setSamplingTime(instant);

        // and we empty the result object
        arrayP = (DataArrayPropertyType) templateExpResult.getResult();
        array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        templateExpResult.setName("urn:ogc:object:observation:template:GEOM:3-3");

        assertEquals(obsCollResult.getMember().size(), 1);

        obsResult = (ObservationType) obsCollResult.getMember().iterator().next();

        obsR = (DataArrayPropertyType) obsResult.getResult();
        obsSdr = (SimpleDataRecordType) obsR.getDataArray().getElementType();
        obsSdr.setBlockId(null);

        assertNotNull(obsResult);
        assertEquals(templateExpResult.getName(), obsResult.getName());
        assertEquals(templateExpResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(templateExpResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(templateExpResult.getProcedure(), obsResult.getProcedure());
        
        // do not compare datarray name (ID) because it depends on the implementation
        expR = (DataArrayPropertyType) templateExpResult.getResult();
        obsR = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertEquals(templateExpResult.getResult(), obsResult.getResult());
        assertEquals(templateExpResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(templateExpResult, obsResult);

        /**
         * Test 8:  getResult with no TimeFilter
         */
        templateId = "urn:ogc:object:observation:template:GEOM:3-3";
        request = new GetResult(templateId, null, "1.0.0");
        result = (GetResultResponse) worker.getResult(request);

        value = "2007-05-01T20:59:00,6.55@@";
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + "sos/default/" + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        marshallerPool.recycle(unmarshaller);

    }

    /**
     * Tests the InsertObservation method
     *
     * @throws java.lang.Exception
     */
    public void insertObservationTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/observationTemplate-3.xml"));

        ObservationType template = (ObservationType)obj.getValue();

        TimePeriodType period = new TimePeriodType(new TimePositionType("2007-06-01T01:00:00.0"), new TimePositionType("2007-06-01T03:00:00.0"));
        template.setSamplingTime(period);

        // and we fill the result object
        DataArrayPropertyType arrayP = (DataArrayPropertyType) template.getResult();
        DataArrayType array = arrayP.getDataArray();
        array.setElementCount(3);
        array.setValues("2007-06-01T01:01:00,6.56@@2007-06-01T02:00:00,6.55@@2007-06-01T03:00:00,6.55@@");

        InsertObservation request = new InsertObservation("1.0.0", "urn:ogc:object:sensor:GEOM:3", template);
        worker.insertObservation(request);

         /**
         *   getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with resultTemplate mode
         */
        GetObservation GOrequest  = new GetObservation("1.0.0",
                                      "offering-3",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        ObservationCollectionType obsColl = (ObservationCollectionType) worker.getObservation(GOrequest);



        String templateId = "urn:ogc:object:observation:template:GEOM:3-0";
        GetResult GRrequest = new GetResult(templateId, null, "1.0.0");
        GetResultResponse result = (GetResultResponse) worker.getResult(GRrequest);

        String value = "2007-05-01T02:59:00,6.56@@2007-05-01T03:59:00,6.56@@2007-05-01T04:59:00,6.56@@2007-05-01T05:59:00,6.56@@2007-05-01T06:59:00,6.56@@" + 
                       "2007-05-01T07:59:00,6.56@@2007-05-01T08:59:00,6.56@@2007-05-01T09:59:00,6.56@@2007-05-01T10:59:00,6.56@@2007-05-01T11:59:00,6.56@@" + 
                       "2007-05-01T17:59:00,6.56@@2007-05-01T18:59:00,6.55@@2007-05-01T19:59:00,6.55@@2007-05-01T20:59:00,6.55@@2007-05-01T21:59:00,6.55@@" + 
                       "2007-06-01T01:01:00,6.56@@2007-06-01T02:00:00,6.55@@2007-06-01T03:00:00,6.55@@";
        GetResultResponse expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + "sos/default/" + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        marshallerPool.recycle(unmarshaller);
    }

    /**
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    public void RegisterSensorErrorTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();


        /**
         * Test 1 we register a system sensor with no Observation template
         */
        AbstractSensorML sensorDescription = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));
        RegisterSensor request = new RegisterSensor("1.0.0", sensorDescription, null);
        boolean exLaunched = false;
        try {
            worker.registerSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(),       OBSERVATION_TEMPLATE);
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
        }

        assertTrue(exLaunched);

        /**
         * Test 2 we register a system sensor with an imcomplete Observation template
         */
        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-6.xml"));
        ObservationType obsTemplate = (ObservationType)obj.getValue();

        obsTemplate.setProcedure((ProcessType)null);
        request = new RegisterSensor("1.0.0", sensorDescription, new ObservationTemplate(obsTemplate));
        exLaunched = false;
        try {
            worker.registerSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(),       OBSERVATION_TEMPLATE);
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }

        assertTrue(exLaunched);

        marshallerPool.recycle(unmarshaller);
    }

    /**
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    public void RegisterSensorTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        /**
         * Test 1 we register a system sensor
         */
        AbstractSensorML sensorDescription = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-6.xml"));

        ObservationType obsTemplate = (ObservationType)obj.getValue();

        RegisterSensor request = new RegisterSensor("1.0.0", sensorDescription, new ObservationTemplate(obsTemplate));

        InsertSensorResponse response = worker.registerSensor(request);

        assertEquals("urn:ogc:object:sensor:GEOM:6", response.getAssignedProcedure());
        
        assertNull(response.getAssignedOffering());

        /**
         * we verify that the sensor is well registered
         */
        DescribeSensor DSrequest  = new DescribeSensor("1.0.0","SOS","urn:ogc:object:sensor:GEOM:6", "text/xml;subtype=\"SensorML/1.0.0\"");
        AbstractSensorML absResult = (AbstractSensorML) worker.describeSensor(DSrequest);


        assertTrue(absResult instanceof SensorML);
        assertTrue(sensorDescription instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) sensorDescription;

        MetadataUtilities.systemSMLEquals(expResult, result);

        marshallerPool.recycle(unmarshaller);
    }

    /**
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    public void GetFeatureOfInterestErrorTest() throws Exception {

        /**
         * Test 1 : bad featureID
         */
        GetFeatureOfInterest request = new GetFeatureOfInterest("1.0.0", "SOS", "wrongFID");

        boolean exLaunched = false;
        try {
            worker.getFeatureOfInterest(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

        /**
         * Test 2 : no filter
         */
        exLaunched = false;
        request = new GetFeatureOfInterest("1.0.0", "SOS", new ArrayList<String>());

        try {
            worker.getFeatureOfInterest(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

        /**
         * Test 3 : malformed BBOX filter
         */
        exLaunched = false;
        BBOXType bbox = new BBOXType();
        request = new GetFeatureOfInterest("1.0.0", "SOS", bbox);


        try {
            worker.getFeatureOfInterest(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);
    }

    /**
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    public void GetFeatureOfInterestTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        /**
         * Test 1 : getFeatureOfInterest with featureID filter
         */
        SamplingPoint expResult = ((JAXBElement<SamplingPoint>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/feature1.xml"))).getValue();

        GetFeatureOfInterest request = new GetFeatureOfInterest("1.0.0", "SOS", "station-001");

        AbstractFeature result = worker.getFeatureOfInterest(request);

        assertTrue (result instanceof SamplingPoint);

        assertEquals(expResult, result);

        /**
         * Test 2 : getFeatureOfInterest with featureID filter (SamplingCurve)
         */
        SamplingCurveType expResultC = ((JAXBElement<SamplingCurveType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v100/feature3.xml"))).getValue();

        request = new GetFeatureOfInterest("1.0.0", "SOS", "station-006");

        result = worker.getFeatureOfInterest(request);

        assertTrue (result instanceof SamplingCurveType);

        final SamplingCurveType resultC = (SamplingCurveType) result;
        assertEquals(expResultC.getShape(),  resultC.getShape());
        assertEquals(expResultC.getLength(), resultC.getLength());
        assertEquals(expResultC, resultC);

        /**
         * Test 3 : getFeatureOfInterest with BBOX filter restore when multiple works

        request = new GetFeatureOfInterest("1.0.0", "SOS", new GetFeatureOfInterest.Location(new BBOXType(null, 64000.0, 1730000.0, 66000.0, 1740000.0, "urn:ogc:def:crs:EPSG:27582")));

        result = worker.getFeatureOfInterest(request);

        assertTrue (result instanceof SamplingPoint);

        assertEquals(expResult, result);*/



        marshallerPool.recycle(unmarshaller);
    }
    
    /**
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    public void GetFeatureOfInterestTimeTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        /**
         * Test 1 : getFeatureOfInterestTime with featureID filter
         */
        TimePeriodType expResult = new TimePeriodType(null, "2007-05-01T02:59:00", "2007-06-01T03:00:00");

        GetFeatureOfInterestTime request = new GetFeatureOfInterestTime("1.0.0", "station-001");

        TemporalPrimitive result = worker.getFeatureOfInterestTime(request);

        assertTrue (result instanceof TimePeriodType);

        assertEquals(expResult, result);

        /**
         * Test 2 : getFeatureOfInterestTime with featureID filter  (SamplingCurve)
         */
        expResult = new TimePeriodType(null, "2007-05-01T12:59:00", "2009-05-01T13:47:00");

        request = new GetFeatureOfInterestTime("1.0.0", "station-006");

        result = worker.getFeatureOfInterestTime(request);

        assertTrue (result instanceof TimePeriodType);

        assertEquals(expResult, result);

        marshallerPool.recycle(unmarshaller);
    }
    
    /**
     * Tests the destroy method
     *
     * @throws java.lang.Exception
     */
    public void destroyTest() throws Exception {
        worker.destroy();
        GetCapabilities request = new GetCapabilities();

        boolean exLaunched = false;
        try {
            worker.getCapabilities(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertTrue(ex.getMessage().contains("The service is not running"));
        }

        assertTrue(exLaunched);
        initWorker();
    }
    
    private static void emptyNameAndId(final DataArrayType resArray, final DataArrayType expArray) {
        resArray.setId(null);              
        expArray.setId(null);
        resArray.setName(null);              
        expArray.setName(null);
        resArray.getPropertyElementType().setName(null);
        expArray.getPropertyElementType().setName(null);
        
        resArray.getElementType().setId(null);
        expArray.getElementType().setId(null);
        resArray.getElementType().setName(null);
        expArray.getElementType().setName(null);
    }
}
