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

package org.constellation.sos.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.test.utils.MetadataUtilities;

import static org.constellation.sos.ws.SOSConstants.*;
import org.geotoolkit.gml.xml.AbstractFeature;
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
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.samplingspatial.xml.v200.SFSpatialSamplingFeatureType;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.swe.xml.v200.DataRecordType;
import org.geotoolkit.swe.xml.v200.DataRecordType.Field;


// JUnit dependencies
import org.junit.Ignore;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Ignore
public class SOS2WorkerTest {

    protected static SOSworker worker;

    protected static MarshallerPool marshallerPool;

    protected static Capabilities capabilities;

    protected static final String URL = "http://pulsar.geomatys.fr/SOServer/SOService/";

    protected static void init() throws JAXBException {
        marshallerPool = SOSMarshallerPool.getInstance();
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        capabilities = (Capabilities) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/SOSCapabilities1.0.0.xml"));
        marshallerPool.release(unmarshaller);
    }

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
        assertTrue(result != null);

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
        assertTrue(result != null);

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
        assertTrue(result != null);

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
        assertTrue(result != null);

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
        assertTrue(result != null);

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
            assertEquals(ex.getLocator(), "outputFormat");
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
            assertEquals(ex.getLocator(), "outputFormat");
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

        SensorMLMarshallerPool.getInstance().release(unmarshaller);
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
         *  Test 2: getObservation with missinf response format
         */
        request  = new GetObservationType("2.0.0",
                                      "offering-1",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
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
        assertTrue(exLaunched);

        /**
         *  Test 6: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with wrong offering
         */
        List<TemporalOpsType> times = new ArrayList<TemporalOpsType>();
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
        times = new ArrayList<TemporalOpsType>();
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
        List<TemporalOpsType> times = new ArrayList<TemporalOpsType>();
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
        times = new ArrayList<TemporalOpsType>();
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
        assertTrue(obsR.getDataArray().getElementCount().getCount().getValue() == 15);

        /**
         *  Test 7: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TDuring
         */
        times = new ArrayList<TemporalOpsType>();
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
        assertTrue(obsR.getDataArray().getElementCount().getCount().getValue() == 10);

        /**
         *  Test 8: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         */
        times = new ArrayList<TemporalOpsType>();
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


        marshallerPool.release(unmarshaller);
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

        marshallerPool.release(unmarshaller);
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


        marshallerPool.release(unmarshaller);
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
         * Test 2 : no filter
         */
        exLaunched = false;
        request = new GetFeatureOfInterestType("2.0.0", "SOS", new ArrayList<String>());

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
        request = new GetFeatureOfInterestType("2.0.0", "SOS", bbox);


        try {
            worker.getFeatureOfInterest(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
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



        marshallerPool.release(unmarshaller);
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
            assertEquals(ex.getMessage(), "The service is not running!");
        }

        assertTrue(exLaunched);
    }
}
