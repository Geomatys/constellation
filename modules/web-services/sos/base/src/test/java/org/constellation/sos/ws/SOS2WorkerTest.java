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
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.test.utils.MetadataUtilities;

import static org.constellation.sos.ws.SOSConstants.*;
import org.geotoolkit.gml.xml.AbstractFeature;
import org.geotoolkit.gml.xml.v321.FeatureCollectionType;
import org.geotoolkit.gml.xml.v321.TimeInstantType;
import org.geotoolkit.gml.xml.v321.TimePeriodType;
import org.geotoolkit.gml.xml.v321.TimePositionType;
import org.geotoolkit.observation.xml.v200.OMObservationType;
import org.geotoolkit.ogc.xml.v200.BBOXType;
import org.geotoolkit.ogc.xml.v200.TemporalOpsType;
import org.geotoolkit.ogc.xml.v200.TimeAfterType;
import org.geotoolkit.ogc.xml.v200.TimeBeforeType;
import org.geotoolkit.ogc.xml.v200.TimeDuringType;
import org.geotoolkit.ogc.xml.v200.TimeEqualsType;

import org.geotoolkit.ows.xml.v110.AcceptFormatsType;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.sos.xml.Capabilities;
import org.geotoolkit.sos.xml.v200.GetCapabilitiesType;
import org.geotoolkit.sos.xml.v200.GetFeatureOfInterestType;
import org.geotoolkit.sos.xml.v200.GetObservationResponseType;
import org.geotoolkit.sos.xml.v200.GetObservationType;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.swe.xml.v200.DataArrayPropertyType;
import org.geotoolkit.swe.xml.v200.TimeType;
import org.geotoolkit.swes.xml.InsertSensorResponse;
import org.geotoolkit.swes.xml.v200.InsertSensorType;
import org.geotoolkit.swes.xml.v200.DescribeSensorType;
import org.geotoolkit.swes.xml.v200.InsertSensorResponseType;
import org.apache.sis.xml.MarshallerPool;
import org.geotoolkit.samplingspatial.xml.v200.SFSpatialSamplingFeatureType;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.sos.xml.InsertResultResponse;
import org.geotoolkit.sos.xml.v200.GetObservationByIdResponseType;
import org.geotoolkit.sos.xml.v200.GetObservationByIdType;
import org.geotoolkit.sos.xml.v200.GetResultResponseType;
import org.geotoolkit.sos.xml.v200.GetResultTemplateResponseType;
import org.geotoolkit.sos.xml.v200.GetResultTemplateType;
import org.geotoolkit.sos.xml.v200.GetResultType;
import org.geotoolkit.sos.xml.v200.InsertObservationType;
import org.geotoolkit.sos.xml.v200.InsertResultTemplateResponseType;
import org.geotoolkit.sos.xml.v200.InsertResultTemplateType;
import org.geotoolkit.sos.xml.v200.InsertResultType;
import org.geotoolkit.swe.xml.v200.AbstractDataComponentType;
import org.geotoolkit.swe.xml.v200.AbstractEncodingType;
import org.geotoolkit.swe.xml.v200.DataArrayType;
import org.geotoolkit.swe.xml.v200.DataRecordType;
import org.geotoolkit.swe.xml.v200.Field;
import org.geotoolkit.swes.xml.v200.DeleteSensorResponseType;
import org.geotoolkit.swes.xml.v200.DeleteSensorType;


// JUnit dependencies
import org.junit.Ignore;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class SOS2WorkerTest {

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
        GetCapabilitiesType request       = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, null, "SOS");

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
        request = new GetCapabilitiesType("2.0.0", "ploup/xml");

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
        GetCapabilitiesType request = new GetCapabilitiesType("2.0.0", null);
        Capabilities result = worker.getCapabilities(request);

        assertTrue(result != null);
        assertTrue(result.getVersion().equals("2.0.0"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() != null);

        assertTrue(result.getContents() != null);
        assertTrue(result.getContents().getOfferings() != null);
        assertEquals(result.getContents().getOfferings().size(), 10);

        /*
         *  TEST 2 : full get capabilities
         */
        AcceptVersionsType acceptVersions = new AcceptVersionsType("2.0.0");
        SectionsType sections             = new SectionsType("All");
        AcceptFormatsType acceptFormats   = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, null, "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("2.0.0"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() != null);
        assertTrue(result.getContents() != null);
        assertTrue(result.getContents().getOfferings() != null);
        assertEquals(result.getContents().getOfferings().size(), 10);
        assertNotNull(result);

        /*
         *  TEST 3 : get capabilities section Operation metadata
         */
        acceptVersions = new AcceptVersionsType("2.0.0");
        sections       = new SectionsType("OperationsMetadata");
        acceptFormats  = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, null, "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("2.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result.getContents() == null);
        assertNotNull(result);

        /*
         *  TEST 4 : get capabilities section Service provider
         */
        acceptVersions = new AcceptVersionsType("2.0.0");
        sections       = new SectionsType("ServiceProvider");
        acceptFormats  = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, null, "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("2.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() != null);
        assertTrue(result.getContents() == null);
        assertNotNull(result);

        /*
         *  TEST 5 : get capabilities section Service Identification
         */
        acceptVersions = new AcceptVersionsType("2.0.0");
        sections       = new SectionsType("ServiceIdentification");
        acceptFormats  = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, null, "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("2.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result.getContents() == null);
        assertNotNull(result);

        /*
         *  TEST 6 : get capabilities section Contents
         */
        acceptVersions = new AcceptVersionsType("2.0.0");
        sections       = new SectionsType("Contents");
        acceptFormats  = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, null, "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("2.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result.getContents() != null);
        assertTrue(result.getContents().getOfferings() != null);
        assertEquals(result.getContents().getOfferings().size(),  10);
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
        DescribeSensorType request  = new DescribeSensorType("2.0.0", "SOS", "urn:ogc:object:sensor:GEOM:1", "http://www.flipouse.net/sensorml/1.0.1");
        try {
            worker.describeSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "procedureDescriptionFormat");
        }
        assertTrue(exLaunched);

        /**
         * Test 2 missing outputFormat
         */
        exLaunched = false;
        request  = new DescribeSensorType("2.0.0", "SOS", "urn:ogc:object:sensor:GEOM:1", null);
        try {
            worker.describeSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "procedureDescriptionFormat");
        }
        assertTrue(exLaunched);

        /**
         * Test 3 missing sensorID
         */
        exLaunched = false;
        request  = new DescribeSensorType("2.0.0", "SOS", null, "http://www.opengis.net/sensorml/1.0.1");
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
        DescribeSensorType request  = new DescribeSensorType("2.0.0", "SOS", "urn:ogc:object:sensor:GEOM:1", "http://www.opengis.net/sensorml/1.0.0");
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
        request  = new DescribeSensorType("2.0.0", "SOS", "urn:ogc:object:sensor:GEOM:2", "http://www.opengis.net/sensorml/1.0.0");
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
        final List<String> nullList = null;
        /**
         *  Test 1: getObservation with bad response format
         */
        GetObservationType request  = new GetObservationType("2.0.0",
                                                     "offering-1",
                                                     null,
                                                     Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                                     null,
                                                     nullList,
                                                     "text/xml;subtype=\"om/3.0.0\"");
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
         *  Test 2: getObservation with missing response format => optionnal so no error
         */
        request  = new GetObservationType("2.0.0",
                                      "offering-1",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:1"),
                                      null,
                                      nullList,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), RESPONSE_FORMAT);
        }
        assertTrue(!exLaunched);

        /**
         *  Test 6: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with wrong offering
         */
        List<TemporalOpsType> times = new ArrayList<>();
        TimePeriodType period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        TimeEqualsType equals = new TimeEqualsType(null, period);
        times.add(equals);
        request  = new GetObservationType("2.0.0",
                                      "inexistant-offering",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
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
         *  Test 9: getObservation with unexisting procedure
         *          + Time filter TEquals
         *
         */
        times = new ArrayList<>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        equals = new TimeEqualsType(null, period);
        times.add(equals);
        request  = new GetObservationType("2.0.0",
                                      "offering-1",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:36"),
                                      null,
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
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
         *  Test 10: getObservation with procedure urn:ogc:object:sensor:GEOM:1
         *          and with wrong observed prop
         */
        request  = new GetObservationType("2.0.0",
                                      "offering-1",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:1"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:hotness"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");

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
         *  Test 11: getObservation with procedure urn:ogc:object:sensor:GEOM:5
         *          and with wrong foi
         */
        request  = new GetObservationType("2.0.0",
                                      "offering-1",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:1"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      Arrays.asList("NIMP"),
                                      "text/xml; subtype=\"om/1.0.0\"");

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

        final List<String> nullList = null;
        
        /**
         *  Test 1: getObservation with procedure urn:ogc:object:sensor:GEOM:4 and no resultModel
         */
        GetObservationType request  = new GetObservationType("2.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        GetObservationResponseType result = (GetObservationResponseType) worker.getObservation(request);

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observation3.xml"));

        OMObservationType expResult = (OMObservationType)obj.getValue();

        assertEquals(result.getMember().size(), 1);

        OMObservationType obsResult = (OMObservationType) result.getMember().iterator().next();


        assertTrue(obsResult != null);
        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        assertTrue(expResult.getResult() instanceof DataArrayPropertyType);

        DataArrayPropertyType expR = (DataArrayPropertyType) expResult.getResult();
        DataArrayPropertyType obsR = (DataArrayPropertyType) obsResult.getResult();

        // do not compare datarray name (ID) because it depends on the implementation
        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());
        
        assertTrue(obsR.getDataArray().getElementType().getAbstractRecord() instanceof DataRecordType);
        DataRecordType expSdr = (DataRecordType) expR.getDataArray().getElementType().getAbstractRecord();
        DataRecordType obsSdr = (DataRecordType) obsR.getDataArray().getElementType().getAbstractRecord();

        Iterator<Field> i1 = expSdr.getField().iterator();
        Iterator<Field> i2 = obsSdr.getField().iterator();
        TimeType expT = (TimeType) i1.next().getTime();
        TimeType obsT = (TimeType) i2.next().getTime();

        assertEquals(expT.getUom(), obsT.getUom());
        assertEquals(expT, obsT);
        assertEquals(i1.next(), i2.next());

        assertEquals(expSdr, obsSdr);

        assertEquals(expR.getDataArray().getElementType().getAbstractArray(),     obsR.getDataArray().getElementType().getAbstractArray());
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
        request  = new GetObservationType("2.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observation3.xml"));

        expResult = (OMObservationType)obj.getValue();

        assertEquals(result.getMember().size(), 1);

        obsResult = (OMObservationType) result.getMember().iterator().next();

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
        request  = new GetObservationType("2.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observation3.xml"));

        expResult = (OMObservationType)obj.getValue();

        assertEquals(result.getMember().size(), 1);

        obsResult = (OMObservationType) result.getMember().iterator().next();


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
        request  = new GetObservationType("2.0.0",
                                      "offering-3",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);
        obsResult =  (OMObservationType) result.getMember().iterator().next();
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        obsR      = (DataArrayPropertyType) obsResult.getResult();
        assertTrue(obsR.getDataArray().getElementCount().getCount().getValue() == 15.0);

        /**
         *  Test 5: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TBefore
         */
        List<TemporalOpsType> times = new ArrayList<>();
        TimeInstantType instant = new TimeInstantType(new TimePositionType("2007-05-01T03:00:00.0"));
        TimeBeforeType before   = new TimeBeforeType(null, instant);
        times.add(before);
        request  = new GetObservationType("2.0.0",
                                      "offering-3",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);

        assertEquals(result.getMember().iterator().next().getName(), "urn:ogc:object:observation:GEOM:304");

        /**
         *  Test 6: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TAFter
         */
        times = new ArrayList<>();
        TimeAfterType after   = new TimeAfterType(null, instant);
        times.add(after);
        request  = new GetObservationType("2.0.0",
                                      "offering-3",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);

        obsResult =  (OMObservationType) result.getMember().iterator().next();
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        obsR      = (DataArrayPropertyType) obsResult.getResult();
        assertEquals((Integer)14, obsR.getDataArray().getElementCount().getCount().getValue());

        /**
         *  Test 7: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TDuring
         */
        times = new ArrayList<>();
        TimePeriodType period  = new TimePeriodType(new TimePositionType("2007-05-01T03:00:00.0"), new TimePositionType("2007-05-01T08:00:00.0"));
        TimeDuringType during = new TimeDuringType(null, period);
        times.add(during);
        request  = new GetObservationType("2.0.0",
                                      "offering-3",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);

        obsResult =  (OMObservationType) result.getMember().iterator().next();
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        obsR      = (DataArrayPropertyType) obsResult.getResult();
        assertEquals((Integer)5, obsR.getDataArray().getElementCount().getCount().getValue());

        /**
         *  Test 8: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         */
        times = new ArrayList<>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        TimeEqualsType equals = new TimeEqualsType(null, period);
        times.add(equals);
        request  = new GetObservationType("2.0.0",
                                      "offering-3",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);

        obsResult =  (OMObservationType) result.getMember().iterator().next();
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        obsR      = (DataArrayPropertyType) obsResult.getResult();
        assertTrue(obsR.getDataArray().getElementCount().getCount().getValue() == 5);


        /**
         *  Test 13: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with observedproperties = urn:ogc:def:phenomenon:GEOM:depth
         */
        request  = new GetObservationType("2.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:depth"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observation3.xml"));

        expResult = (OMObservationType)obj.getValue();
        assertEquals(result.getMember().size(), 1);


        obsResult = (OMObservationType) result.getMember().iterator().next();
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
        request  = new GetObservationType("2.0.0",
                                      "offering-5",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:5"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observation5.xml"));

        expResult = (OMObservationType)obj.getValue();
        assertEquals(result.getMember().size(), 1);


        obsResult = (OMObservationType) result.getMember().iterator().next();
        assertTrue(obsResult != null);

        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        final DataArrayPropertyType expArray = (DataArrayPropertyType) expResult.getResult();
        final DataArrayPropertyType resArray = (DataArrayPropertyType) obsResult.getResult();
        
        // do not compare datarray name (ID) because it depends on the implementation
        emptyNameAndId(expArray.getDataArray(),  resArray.getDataArray());
        
        assertEquals(expArray.getDataArray().getElementCount(), resArray.getDataArray().getElementCount());
        assertEquals(expArray.getDataArray().getElementType(),  resArray.getDataArray().getElementType());
        assertEquals(expArray.getDataArray().getEncoding(),  resArray.getDataArray().getEncoding());
        assertEquals(expArray.getDataArray(), resArray.getDataArray());
        assertEquals(expArray, resArray);
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 15: getObservation with procedure urn:ogc:object:sensor:GEOM:5
         *           with observedproperties = urn:ogc:def:phenomenon:GEOM:aggreagtePhenomenon
         *           with foi                =  10972X0137-PLOUF
         */
        request  = new GetObservationType("2.0.0",
                                      "offering-5",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:5"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"),
                                      Arrays.asList("station-002"),
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observation5.xml"));

        expResult = (OMObservationType)obj.getValue();
        assertEquals(result.getMember().size(), 1);


        obsResult = (OMObservationType) result.getMember().iterator().next();
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
        request  = new GetObservationType("2.0.0",
                                      "offering-3",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"),
                                      Arrays.asList("station-002"),
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);


        GetObservationResponseType collExpResult = new GetObservationResponseType();
        assertEquals(collExpResult, result);



        /**
         *  Test 18: getObservation with procedure urn:ogc:object:sensor:GEOM:4 AND BBOX Filter
         */
        request  = new GetObservationType("2.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      new BBOXType(null, 64000.0, 1730000.0, 66000.0, 1740000.0, "urn:ogc:def:crs:EPSG:27582"),
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observation3.xml"));

        expResult = (OMObservationType)obj.getValue();

        assertEquals(result.getMember().size(), 1);

        obsResult = (OMObservationType) result.getMember().iterator().next();


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
         *  Test 19: getObservation with procedure urn:ogc:object:sensor:GEOM:4 AND BBOX Filter (no result expected)
         */
        request  = new GetObservationType("2.0.0",
                                      "offering-4",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      new BBOXType(null, 66000.0, 1730000.0, 67000.0, 1740000.0, "urn:ogc:def:crs:EPSG:27582"),
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);

        collExpResult = new GetObservationResponseType();
        assertEquals(collExpResult, result);


        marshallerPool.recycle(unmarshaller);
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    public void GetObservationSamplingCurveTest() throws Exception {

        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        final List<String> nullList = null;
        
        /**
         *  Test 2: getObservation with procedure urn:ogc:object:sensor:GEOM:8
         *
         */
        GetObservationType request  = new GetObservationType("2.0.0",
                                      "offering-8",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:8"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
        GetObservationResponseType result = (GetObservationResponseType) worker.getObservation(request);
        OMObservationType obsResult = (OMObservationType) result.getMember().iterator().next();

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observation6.xml"));
        OMObservationType expResult = (OMObservationType)obj.getValue();

        assertTrue(obsResult.getFeatureOfInterest() instanceof SFSpatialSamplingFeatureType);
        final SFSpatialSamplingFeatureType expectedFOI = (SFSpatialSamplingFeatureType)expResult.getFeatureOfInterest();
        final SFSpatialSamplingFeatureType resultFOI   = (SFSpatialSamplingFeatureType)obsResult.getFeatureOfInterest();
        assertEquals(expectedFOI.getShape(),     resultFOI.getShape());
        assertEquals(expectedFOI.getBoundedBy(), resultFOI.getBoundedBy());
        assertEquals(expectedFOI, resultFOI);
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());

        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        DataArrayPropertyType expArray = (DataArrayPropertyType) expResult.getResult();
        DataArrayPropertyType resArray = (DataArrayPropertyType) obsResult.getResult();
        
         // do not compare datarray name (ID) because it depends on the implementation
        emptyNameAndId(expArray.getDataArray(),  resArray.getDataArray());
        
        assertEquals(expArray.getDataArray().getElementCount(), resArray.getDataArray().getElementCount());
        assertEquals(expArray.getDataArray().getElementType(),  resArray.getDataArray().getElementType());
        assertEquals(expArray.getDataArray().getEncoding(),  resArray.getDataArray().getEncoding());
        assertEquals(expArray.getDataArray(), resArray.getDataArray());
        assertEquals(expArray, resArray);
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 3: getObservation with no procedure And FID = station-006
         *
         */
        request  = new GetObservationType("2.0.0",
                                      null,
                                      null,
                                      null,
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      Arrays.asList("station-006"),
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        result = (GetObservationResponseType) worker.getObservation(request);
        assertEquals(1, result.getMember().size());
        obsResult = (OMObservationType) result.getMember().iterator().next();

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observation6.xml"));
        expResult = (OMObservationType)obj.getValue();

        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        
         // do not compare datarray name (ID) because it depends on the implementation
        expArray = (DataArrayPropertyType) expResult.getResult();
        resArray = (DataArrayPropertyType) obsResult.getResult();
        emptyNameAndId(expArray.getDataArray(),  resArray.getDataArray());
        
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        marshallerPool.recycle(unmarshaller);
    }

    public void GetObservationByIdTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        
        GetObservationByIdType request = new GetObservationByIdType("2.0.0", Arrays.asList("urn:ogc:object:observation:GEOM:304"));
        
        final GetObservationByIdResponseType response = (GetObservationByIdResponseType) worker.getObservationById(request);
        
        final OMObservationType result = (OMObservationType) response.getMember().get(0);
        
        java.io.Reader ioReader = new InputStreamReader(Util.getResourceAsStream("org/constellation/sos/v200/observation1.xml"), "UTF-8");
        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(ioReader);

        OMObservationType expResult = (OMObservationType)obj.getValue();

        assertEquals(expResult.getFeatureOfInterest(), result.getFeatureOfInterest());
        
         // do not compare datarray name (ID) because it depends on the implementation
        DataArrayPropertyType expArray = (DataArrayPropertyType) expResult.getResult();
        DataArrayPropertyType resArray = (DataArrayPropertyType) result.getResult();
        emptyNameAndId(expArray.getDataArray(),  resArray.getDataArray());
        
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
         * Test 1 we register a system sensor with no decription format
         */
        AbstractSensorML sensorDescription = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));
        InsertSensorType request = new InsertSensorType("2.0.0", sensorDescription,  null);
        boolean exLaunched = false;
        try {
            worker.registerSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(),       PROCEDURE_DESCRIPTION_FORMAT);
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
        }

        assertTrue(exLaunched);

        /**
         * Test 2 we register a system sensor with an invalid decription format
         */
        request = new InsertSensorType("2.0.0", sensorDescription, "something");
        exLaunched = false;
        try {
            worker.registerSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(),       PROCEDURE_DESCRIPTION_FORMAT);
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

        /*JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-6.xml"));

        OMObservationType obsTemplate = (OMObservationType)obj.getValue();*/

        InsertSensorType request = new InsertSensorType("2.0.0", sensorDescription, "http://www.opengis.net/sensorML/1.0.0");

        InsertSensorResponse response = worker.registerSensor(request);
        
        assertTrue(response instanceof InsertSensorResponseType);

        assertEquals("urn:ogc:object:sensor:GEOM:4", response.getAssignedProcedure());

        /**
         * we verify that the sensor is well registered
         */
        DescribeSensorType DSrequest  = new DescribeSensorType("2.0.0","SOS","urn:ogc:object:sensor:GEOM:4", "http://www.opengis.net/sensorML/1.0.0");
        AbstractSensorML absResult = (AbstractSensorML) worker.describeSensor(DSrequest);


        assertTrue(absResult instanceof SensorML);
        assertTrue(sensorDescription instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) sensorDescription;

        MetadataUtilities.systemSMLEquals(expResult, result);


        marshallerPool.recycle(unmarshaller);
    }
    
    public void DeleteSensorTest() throws Exception {
        
        final DeleteSensorType request = new DeleteSensorType("2.0.0","urn:ogc:object:sensor:GEOM:4");
        final DeleteSensorResponseType result = (DeleteSensorResponseType) worker.deleteSensor(request);
        final DeleteSensorResponseType expResult = new DeleteSensorResponseType("urn:ogc:object:sensor:GEOM:4");
        
        assertEquals(expResult, result);
        
        /**
         * Test 1 system sensor
         */
        DescribeSensorType requestds  = new DescribeSensorType("2.0.0", "SOS", "urn:ogc:object:sensor:GEOM:4", "http://www.opengis.net/sensorml/1.0.0");
        boolean exLaunched = false;
        try {
            worker.describeSensor(requestds);
        } catch (CstlServiceException ex) {
           exLaunched = true;
           assertTrue(ex.getMessage().contains("this sensor is not registered"));
        }
        assertTrue(exLaunched);
        
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
        GetFeatureOfInterestType request = new GetFeatureOfInterestType("2.0.0", "SOS", "wrongFID");

        boolean exLaunched = false;
        try {
            worker.getFeatureOfInterest(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

        /**
         * Test 2 : no filter => not an error in v2
         */
        exLaunched = false;
        request = new GetFeatureOfInterestType("2.0.0", "SOS", new ArrayList<String>());

        try {
            worker.getFeatureOfInterest(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
        }
        assertTrue(!exLaunched);

        /**
         * Test 3 : malformed BBOX filter
         */
        exLaunched = false;
        BBOXType bbox = new BBOXType();
        request = new GetFeatureOfInterestType("2.0.0", "SOS", bbox);

        try {
            worker.getFeatureOfInterest(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);
    }

    /**
     * Tests the GetFeatureOfInterest method
     *
     * @throws java.lang.Exception
     */
    public void GetFeatureOfInterestTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        /**
         * Test 1 : getFeatureOfInterest with featureID filter
         */
        SFSpatialSamplingFeatureType expResult = ((JAXBElement<SFSpatialSamplingFeatureType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/feature1.xml"))).getValue();

        GetFeatureOfInterestType request = new GetFeatureOfInterestType("2.0.0", "SOS", "station-001");

        AbstractFeature result = worker.getFeatureOfInterest(request);

        assertTrue ("was" + result, result instanceof SFSpatialSamplingFeatureType);

        assertEquals(expResult, result);

        /**
         * Test 2 : getFeatureOfInterest with featureID filter (SamplingCurve)
         */
        SFSpatialSamplingFeatureType expResultC = ((JAXBElement<SFSpatialSamplingFeatureType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/feature3.xml"))).getValue();

        request = new GetFeatureOfInterestType("2.0.0", "SOS", "station-006");

        result = worker.getFeatureOfInterest(request);

        assertTrue (result instanceof SFSpatialSamplingFeatureType);
        
        final SFSpatialSamplingFeatureType resultC = (SFSpatialSamplingFeatureType) result;

        assertEquals(expResultC.getShape(), resultC.getShape());
        
        assertEquals(expResultC.getBoundedBy(), resultC.getBoundedBy());
        
        assertEquals(expResultC, resultC);

        /**
         * Test 3 : getFeatureOfInterest with BBOX filter restore when multiple works

        request = new GetFeatureOfInterest("2.0.0", "SOS", new GetFeatureOfInterest.Location(new BBOXType(null, 64000.0, 1730000.0, 66000.0, 1740000.0, "urn:ogc:def:crs:EPSG:27582")));

        result = worker.getFeatureOfInterest(request);

        assertTrue (result instanceof SamplingPoint);

        assertEquals(expResult, result);*/



        marshallerPool.recycle(unmarshaller);
    }
    
    public void GetFeatureOfInterestObservedPropertiesTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        
        GetFeatureOfInterestType request = new GetFeatureOfInterestType("2.0.0", "SOS", Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"), null, null, null);
        
        AbstractFeature result = worker.getFeatureOfInterest(request);
        
        assertTrue(result instanceof FeatureCollectionType);
        
        FeatureCollectionType collection = (FeatureCollectionType)result;
        assertEquals(2, collection.getFeatureMember().size());
        
        assertTrue(collection.getFeatureMember().get(0).getAbstractFeature() instanceof SFSpatialSamplingFeatureType);
        
        SFSpatialSamplingFeatureType sf1 = (SFSpatialSamplingFeatureType) collection.getFeatureMember().get(0).getAbstractFeature();
        assertEquals("station-002", sf1.getId());
        SFSpatialSamplingFeatureType sf2 = (SFSpatialSamplingFeatureType) collection.getFeatureMember().get(1).getAbstractFeature();
        assertEquals("station-006", sf2.getId());
        
        request = new GetFeatureOfInterestType("2.0.0", "SOS", Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"), Arrays.asList("urn:ogc:object:sensor:GEOM:8"), null, null);
        
        result = worker.getFeatureOfInterest(request);
        
        assertTrue(result instanceof FeatureCollectionType);
        
        collection = (FeatureCollectionType)result;
        assertEquals(1, collection.getFeatureMember().size());
        
        assertTrue(collection.getFeatureMember().get(0).getAbstractFeature() instanceof SFSpatialSamplingFeatureType);
        
        sf1 = (SFSpatialSamplingFeatureType) collection.getFeatureMember().get(0).getAbstractFeature();
        assertEquals("station-006", sf1.getId());
        
        marshallerPool.recycle(unmarshaller);
    }

    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    public void GetResultErrorTest() throws Exception {
        /**
         * Test 1: bad version number + null offering ID
         */
        String offeringId = null;
        String observedProperty = "urn:ogc:def:phenomenon:GEOM:depth";
        GetResultType request = new GetResultType("3.0.0", "SOS", offeringId, observedProperty, null, null, null);
        boolean exLaunched = false;
        try {
            worker.getResult(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

        /**
         * Test 2:  null offering ID
         */
        offeringId = null;
        request = new GetResultType("2.0.0", "SOS", offeringId, observedProperty, null, null, null);
        exLaunched = false;
        try {
            worker.getResult(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "offering");
        }
        assertTrue(exLaunched);

        /**
         * Test 3:  bad offering ID
         */
        offeringId = "some id";
        request = new GetResultType("2.0.0", "SOS", offeringId, observedProperty, null, null, null);
        exLaunched = false;
        try {
            worker.getResult(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "offering");
        }
        assertTrue(exLaunched);
    }

    public void GetResultTemplateTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        /** 
         *   getResultTemplate with procedure urn:ogc:object:sensor:GEOM:4
         */
        GetResultTemplateType GOrequest  = new GetResultTemplateType("2.0.0",
                                      "offering-4",
                                      "urn:ogc:def:phenomenon:GEOM:depth");
        GetResultTemplateResponseType obsCollResult = (GetResultTemplateResponseType) worker.getResultTemplate(GOrequest);

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observationTemplate-4.xml"));

        OMObservationType templateExpResult = (OMObservationType)obj.getValue();

        DataArrayPropertyType arrayP = (DataArrayPropertyType) templateExpResult.getResult();
        DataArrayType array = arrayP.getDataArray();
        
        // empty id 
        array.getElementType().getValue().setId(null);
        obsCollResult.getResultStructure().setId(null);

        assertEquals(array.getEncoding(), obsCollResult.getResultEncoding());
        assertEquals(array.getElementType().getValue(), obsCollResult.getResultStructure());

    }
    
    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    public void GetResultTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        /**
         * Test 1:  getResult with no TimeFilter
         */
        String offeringId = "offering-3";
        String observedProperty = "urn:ogc:def:phenomenon:GEOM:depth";
        GetResultType request = new GetResultType("2.0.0", "SOS", offeringId, observedProperty, null, null, null);
        GetResultResponseType result = (GetResultResponseType) worker.getResult(request);

        String value = "2007-05-01T02:59:00,6.56@@2007-05-01T03:59:00,6.56@@2007-05-01T04:59:00,6.56@@2007-05-01T05:59:00,6.56@@2007-05-01T06:59:00,6.56@@" + 
                       "2007-05-01T07:59:00,6.56@@2007-05-01T08:59:00,6.56@@2007-05-01T09:59:00,6.56@@2007-05-01T10:59:00,6.56@@2007-05-01T11:59:00,6.56@@" + 
                       "2007-05-01T17:59:00,6.56@@2007-05-01T18:59:00,6.55@@2007-05-01T19:59:00,6.55@@2007-05-01T20:59:00,6.55@@2007-05-01T21:59:00,6.55@@";
        GetResultResponseType expResult = new GetResultResponseType(value);

        assertEquals(expResult.getResultValues(), result.getResultValues());
        assertEquals(expResult, result);

        /**
         * Test 2:  getResult with no TimeFilter
         */
        offeringId = "offering-3";
        request = new GetResultType("2.0.0", "SOS", offeringId, observedProperty, null, null, null);
        
        // template filter
        TimeInstantType instant = new TimeInstantType(new TimePositionType("2007-05-01T05:00:00.0"));
        TemporalOpsType templatefilter = new TimeBeforeType(null, instant);
        request.addTemporalFilter(templatefilter);
        
        result = (GetResultResponseType) worker.getResult(request);

        value = "2007-05-01T02:59:00,6.56@@2007-05-01T03:59:00,6.56@@2007-05-01T04:59:00,6.56@@";
        expResult = new GetResultResponseType(value);

        assertEquals(expResult.getResultValues(), result.getResultValues());
        assertEquals(expResult, result);

         /**
         * Test 3:  getResult with Tafter
         */
        instant = new TimeInstantType(new TimePositionType("2007-05-01T03:00:00.0"));
        TimeAfterType afilter = new TimeAfterType(null, instant);
        request.addTemporalFilter(afilter);
        result = (GetResultResponseType) worker.getResult(request);

        value = "2007-05-01T03:59:00,6.56@@2007-05-01T04:59:00,6.56@@";
        expResult = new GetResultResponseType(value);

        assertEquals(expResult.getResultValues(), result.getResultValues());
        assertEquals(expResult, result);

        /**
         * Test 4:  getResult with Tbefore
         */
        instant = new TimeInstantType(new TimePositionType("2007-05-01T04:00:00.0"));
        TimeBeforeType bfilter = new TimeBeforeType(null, instant);
        List<TemporalOpsType> filters = new ArrayList<>();
        filters.add(templatefilter);
        filters.add(bfilter);        
        request = new GetResultType("2.0.0", "SOS", offeringId, observedProperty, filters, null, null);
        result = (GetResultResponseType) worker.getResult(request);

        value = "2007-05-01T02:59:00,6.56@@2007-05-01T03:59:00,6.56@@";
        expResult = new GetResultResponseType(value);

        assertEquals(expResult.getResultValues(), result.getResultValues());
        assertEquals(expResult, result);

        /**
         * Test 5:  getResult with TEquals
         */
        instant = new TimeInstantType(new TimePositionType("2007-05-01T03:59:00.0"));
        TimeEqualsType efilter = new TimeEqualsType(null, instant);
        filters = new ArrayList<>();
        filters.add(templatefilter);
        filters.add(efilter);  

        request = new GetResultType("2.0.0", "SOS", offeringId, observedProperty, filters, null, null);
        result = (GetResultResponseType) worker.getResult(request);

        value = "2007-05-01T03:59:00,6.56@@";
        expResult = new GetResultResponseType(value);

        assertEquals(expResult.getResultValues(), result.getResultValues());
        assertEquals(expResult, result);

        /**
         * Test 6:  getResult with TEquals
         */
        TimePeriodType period = new TimePeriodType(new TimePositionType("2007-05-01T03:00:00.0"), new TimePositionType("2007-05-01T04:00:00.0"));
        TimeDuringType dfilter = new TimeDuringType(null, period);
        filters = new ArrayList<>();
        filters.add(templatefilter);
        filters.add(dfilter);  
        
        request = new GetResultType("2.0.0", "SOS", offeringId, observedProperty, filters, null, null);
        result = (GetResultResponseType) worker.getResult(request);

        value = "2007-05-01T03:59:00,6.56@@";
        expResult = new GetResultResponseType(value);

        assertEquals(expResult.getResultValues(), result.getResultValues());
        assertEquals(expResult, result);


        /**
         * Test 7:  getResult with no TimeFilter
         */
        request = new GetResultType("2.0.0", "SOS", offeringId, observedProperty, null, null, null);
        instant = new TimeInstantType(new TimePositionType("2007-05-01T19:00:00.0"));
        templatefilter = new TimeAfterType(null, instant);
        request.addTemporalFilter(templatefilter);
                
        result = (GetResultResponseType) worker.getResult(request);

        value = "2007-05-01T19:59:00,6.55@@2007-05-01T20:59:00,6.55@@2007-05-01T21:59:00,6.55@@";
        expResult = new GetResultResponseType(value);

        assertEquals(expResult.getResultValues(), result.getResultValues());
        assertEquals(expResult, result);

        /**
         * Test 8:  getResult with no TimeFilter
         */
        instant = new TimeInstantType(new TimePositionType("2007-05-01T20:59:00.0"));
        templatefilter = new TimeEqualsType(null, instant);
        request = new GetResultType("2.0.0", "SOS", offeringId, observedProperty, null, null, null);
        request.addTemporalFilter(templatefilter);
        result = (GetResultResponseType) worker.getResult(request);

        value = "2007-05-01T20:59:00,6.55@@";
        expResult = new GetResultResponseType(value);

        assertEquals(expResult.getResultValues(), result.getResultValues());
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
        
        String observedProperty = "urn:ogc:def:phenomenon:GEOM:depth";
        
        GetResultType GRrequest = new GetResultType("2.0.0", "SOS", "offering-3", observedProperty, null, null, null);
        GetResultResponseType result = (GetResultResponseType) worker.getResult(GRrequest);

        String value = "2007-05-01T02:59:00,6.56@@2007-05-01T03:59:00,6.56@@2007-05-01T04:59:00,6.56@@2007-05-01T05:59:00,6.56@@2007-05-01T06:59:00,6.56@@" + 
                       "2007-05-01T07:59:00,6.56@@2007-05-01T08:59:00,6.56@@2007-05-01T09:59:00,6.56@@2007-05-01T10:59:00,6.56@@2007-05-01T11:59:00,6.56@@" + 
                       "2007-05-01T17:59:00,6.56@@2007-05-01T18:59:00,6.55@@2007-05-01T19:59:00,6.55@@2007-05-01T20:59:00,6.55@@2007-05-01T21:59:00,6.55@@";
        
        GetResultResponseType expResult = new GetResultResponseType(value);

        assertEquals(expResult.getResultValues(), result.getResultValues());
        assertEquals(expResult, result);

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observationTemplate-3.xml"));

        OMObservationType template = (OMObservationType)obj.getValue();

        TimePeriodType period = new TimePeriodType(new TimePositionType("2007-06-01T01:00:00.0"), new TimePositionType("2007-06-01T03:00:00.0"));
        template.setPhenomenonTime(period);

        // and we fill the result object
        DataArrayPropertyType arrayP = (DataArrayPropertyType) template.getResult();
        DataArrayType array = arrayP.getDataArray();
        array.setElementCount(3);
        array.setValues("2007-06-01T01:01:00,6.56@@2007-06-01T02:00:00,6.55@@2007-06-01T03:00:00,6.55@@");

        InsertObservationType request = new InsertObservationType("2.0.0", Arrays.asList("offering-3"), Arrays.asList(template));
        worker.insertObservation(request);

        GRrequest = new GetResultType("2.0.0", "SOS", "offering-3", observedProperty, null, null, null);
        result = (GetResultResponseType) worker.getResult(GRrequest);

        value = "2007-05-01T02:59:00,6.56@@2007-05-01T03:59:00,6.56@@2007-05-01T04:59:00,6.56@@2007-05-01T05:59:00,6.56@@2007-05-01T06:59:00,6.56@@" +
                "2007-05-01T07:59:00,6.56@@2007-05-01T08:59:00,6.56@@2007-05-01T09:59:00,6.56@@2007-05-01T10:59:00,6.56@@2007-05-01T11:59:00,6.56@@" +
                "2007-05-01T17:59:00,6.56@@2007-05-01T18:59:00,6.55@@2007-05-01T19:59:00,6.55@@2007-05-01T20:59:00,6.55@@2007-05-01T21:59:00,6.55@@" +
                "2007-06-01T01:01:00,6.56@@2007-06-01T02:00:00,6.55@@2007-06-01T03:00:00,6.55@@";
        expResult = new GetResultResponseType(value);

        assertEquals(expResult.getResultValues(), result.getResultValues());
        assertEquals(expResult, result);

        marshallerPool.recycle(unmarshaller);
    }
    
    /**
     * Tests the InsertObservation method
     *
     * @throws java.lang.Exception
     */
    public void insertResultTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observationTemplate-3.xml"));

        OMObservationType template = (OMObservationType)obj.getValue();


        // and we fill the result object
        DataArrayPropertyType arrayP = (DataArrayPropertyType) template.getResult();
        DataArrayType array = arrayP.getDataArray();
        final AbstractDataComponentType record = array.getElementType().getValue();
        final AbstractEncodingType encoding = array.getEncoding();
        template .setResult(null);

        InsertResultTemplateType request = new InsertResultTemplateType("2.0.0", "offering-3", template, record, encoding);
        InsertResultTemplateResponseType result = (InsertResultTemplateResponseType) worker.insertResultTemplate(request);

        final String templateID = result.getAcceptedTemplate();
        assertTrue(templateID.startsWith("urn:ogc:object:observation:template:GEOM:"));
        
        String value = "2012-01-01T00:01:00,12.1@@2012-01-01T00:02:00,13.1@@";
        InsertResultType requestIR = new InsertResultType("2.0.0", templateID, value);
        final InsertResultResponse response = worker.insertResult(requestIR);
        assertNotNull(response);
        

         final List<String> nullList = null;
        
        /**
         *  Test 1: getObservation with procedure urn:ogc:object:sensor:GEOM:4 and no resultModel
         */
        GetObservationType requestGO  = new GetObservationType("2.0.0",
                                      "offering-3",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:ALL"),
                                      nullList,
                                      "text/xml; subtype=\"om/1.0.0\"");
        
        GetObservationResponseType resultGO = (GetObservationResponseType) worker.getObservation(requestGO);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/v200/observation4.xml"));

        OMObservationType expResult = (OMObservationType)obj.getValue();

        assertEquals(resultGO.getMember().size(), 1);

        OMObservationType obsResult = (OMObservationType) resultGO.getMember().iterator().next();

        Marshaller marshaller = marshallerPool.acquireMarshaller();
        marshaller.marshal(obsResult, System.out);
        marshallerPool.recycle(marshaller);
        
        assertTrue(obsResult != null);
        obsResult.setName(null);
        expResult.setName(null);
        
        obsResult.setId(null);
        expResult.setId(null);
        
        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        assertTrue(expResult.getResult() instanceof DataArrayPropertyType);

        DataArrayPropertyType expR = (DataArrayPropertyType) expResult.getResult();
        DataArrayPropertyType obsR = (DataArrayPropertyType) obsResult.getResult();

        assertTrue(obsR.getDataArray().getElementType().getAbstractRecord() instanceof DataRecordType);
        DataRecordType expSdr = (DataRecordType) expR.getDataArray().getElementType().getAbstractRecord();
        DataRecordType obsSdr = (DataRecordType) obsR.getDataArray().getElementType().getAbstractRecord();

        emptyNameAndId(expR.getDataArray(),  obsR.getDataArray());        
        
        Iterator<Field> i1 = expSdr.getField().iterator();
        Iterator<Field> i2 = obsSdr.getField().iterator();
        TimeType expT = (TimeType) i1.next().getTime();
        TimeType obsT = (TimeType) i2.next().getTime();

        assertEquals(expT.getUom(), obsT.getUom());
        assertEquals(expT, obsT);
        assertEquals(i1.next(), i2.next());

        assertEquals(expSdr, obsSdr);
        
        assertEquals(expR.getDataArray().getElementType().getName(),     obsR.getDataArray().getElementType().getName());
        assertEquals(expR.getDataArray().getElementType().getAbstractArray(),     obsR.getDataArray().getElementType().getAbstractArray());
        assertEquals(expR.getDataArray().getElementType(),     obsR.getDataArray().getElementType());
        assertEquals(expR.getDataArray().getEncoding(),        obsR.getDataArray().getEncoding());
        
        
        String v = "2007-05-01T02:59:00,6.56@@2007-05-01T03:59:00,6.56@@2007-05-01T04:59:00,6.56@@2007-05-01T05:59:00,6.56@@2007-05-01T06:59:00,6.56@@2007-05-01T07:59:00,6.56@@2007-05-01T08:59:00,6.56@@2007-05-01T09:59:00,6.56@@2007-05-01T10:59:00,6.56@@2007-05-01T11:59:00,6.56@@2007-05-01T17:59:00,6.56@@2007-05-01T18:59:00,6.55@@2007-05-01T19:59:00,6.55@@2007-05-01T20:59:00,6.55@@2007-05-01T21:59:00,6.55@@2007-06-01T01:01:00,6.56@@2007-06-01T02:00:00,6.55@@2007-06-01T03:00:00,6.55@@2012-01-01T00:01:00,12.1@@2012-01-01T00:02:00,13.1@@";
        expR.getDataArray().setValues(v);
        
        assertEquals(expR.getDataArray().getValues(),          obsR.getDataArray().getValues());
        assertEquals(expR.getDataArray().getId(),              obsR.getDataArray().getId());
        
        expR.getDataArray().setElementCount(20);
        
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
        
        ((TimePeriodType)expResult.getSamplingTime()).setBeginPosition(new TimePositionType("2007-05-01T02:59:00.0"));
        ((TimePeriodType)expResult.getSamplingTime()).setEndPosition(new TimePositionType("2012-01-01T00:02:00"));

                    
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        
        /**
         * Try to insert bad strcutured data
         */
        boolean exLaunched = false;
        requestIR = new InsertResultType("2.0.0", templateID, "1234567890");
        try {
            worker.insertResult(requestIR);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "resultValues");
        }
        assertTrue(exLaunched);
        
        marshallerPool.recycle(unmarshaller);
    }

    /**
     * Tests the destroy method
     *
     * @throws java.lang.Exception
     */
    public void destroyTest() throws Exception {
        worker.destroy();
        GetCapabilitiesType request = new GetCapabilitiesType();

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
        resArray.getPropertyElementType().getValue().setId(null);
        expArray.getPropertyElementType().getValue().setId(null);
        
    }
}
