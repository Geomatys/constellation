/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// constellation dependencies
import org.constellation.ws.MimeType;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.observation.xml.v100.ObservationCollectionEntry;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.ogc.xml.v110.BinaryTemporalOpType;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.sos.xml.v100.EventTime;
import org.geotoolkit.sos.xml.v100.GetCapabilities;
import org.geotoolkit.sos.xml.v100.GetObservation;
import org.geotoolkit.sos.xml.v100.GetResult;
import org.geotoolkit.sos.xml.v100.GetResultResponse;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.geotoolkit.swe.xml.v101.DataArrayEntry;
import org.geotoolkit.swe.xml.v101.DataArrayPropertyType;
import org.geotoolkit.swe.xml.v101.SimpleDataRecordEntry;
import org.geotoolkit.xml.MarshallerPool;
import static org.constellation.sos.ws.SOSworker.*;

// Junit dependencies
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal
 */
public class SosIOTest {

    private Logger logger = Logger.getLogger("org.constellation.sos.ws");


    private SOSworker defaultWorker;

    private SOSworker genericWorker;

    private MarshallerPool marshallerPool;

    private Marshaller marshaller;
    private final boolean configFilesExist;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        marshallerPool = new MarshallerPool("org.geotoolkit.sos.xml.v100:org.geotoolkit.ows.xml.v110");
        marshaller = marshallerPool.acquireMarshaller();
    }

    @After
    public void tearDown() throws Exception {
        if (marshaller != null) {
            marshallerPool.release(marshaller);
        }
    }

    public SosIOTest() throws Exception {
        File capabilitiesFile = new File("sosDefaultConfig/sos_configuration/SOSCapabilities1.0.0.xml");
        Capabilities staticCapabilities = null;
        if (capabilitiesFile.exists()) {
            Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

            Object obj = unmarshaller.unmarshal(capabilitiesFile);
            if (obj instanceof Capabilities) {
                staticCapabilities = (Capabilities) obj;
            }
            marshallerPool.release(unmarshaller);
        } else {
            configFilesExist = false;
            return;
        }
        File configDirectory1 = new File("sosDefaultConfig");
        if (configDirectory1.exists()) {
            defaultWorker = new SOSworker(configDirectory1);
            defaultWorker.setSkeletonCapabilities(staticCapabilities);
        } else {
            configFilesExist = false;
            return;
        }

        File configDirectory2 = new File("sosGenericConfig");
        if (configDirectory2.exists()) {
            genericWorker = new SOSworker(configDirectory2);
            genericWorker.setSkeletonCapabilities(staticCapabilities);
            configFilesExist = true;
        } else {
            configFilesExist = false;
        }
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void GetCapabilitiesTest() throws Exception {
        if (configFilesExist) {
            
            GetCapabilities request = new GetCapabilities("1.0.0", MimeType.APP_XML);
            Capabilities expResult  = defaultWorker.getCapabilities(request);
            Capabilities result     = genericWorker.getCapabilities(request);

            marshaller.marshal(expResult, new File("expectedCapabilities.xml"));
            marshaller.marshal(result, new File("resultCapabilities.xml"));

            List<ObservationOfferingEntry> expOfferingList = expResult.getContents().getObservationOfferingList().getObservationOffering();
            List<ObservationOfferingEntry> resOfferingList = result.getContents().getObservationOfferingList().getObservationOffering();
            assertEquals(expOfferingList.size(), resOfferingList.size());
            for (int i = 0; i <expOfferingList.size(); i++) {
                ObservationOfferingEntry expOffering = expOfferingList.get(i);
                ObservationOfferingEntry resOffering = resOfferingList.get(i);

                assertEquals(expOffering.getObservedProperty().size(),  resOffering.getObservedProperty().size());
                assertEquals(expOffering.getObservedProperty(),  resOffering.getObservedProperty());
                assertEquals(expOffering.getFeatureOfInterest(), resOffering.getFeatureOfInterest());
                assertEquals(expOffering.getProcedure(),         resOffering.getProcedure());
                assertEquals(expOffering.getTime(),         resOffering.getTime());
                assertEquals(expOffering, resOffering);
            }
            assertEquals(expOfferingList, resOfferingList);

            assertEquals(expResult.getContents().getObservationOfferingList(), result.getContents().getObservationOfferingList());
            assertEquals(expResult.getContents(), result.getContents());
            assertEquals(expResult.getFilterCapabilities(), result.getFilterCapabilities());
            assertEquals(expResult.getOperationsMetadata(), result.getOperationsMetadata());
            assertEquals(expResult.getServiceIdentification(), result.getServiceIdentification());
            assertEquals(expResult.getServiceProvider(), result.getServiceProvider());
            assertEquals(expResult, result);
        
        } else {
            logger.info("configuration files missing skipping test");
        }

    }
    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void GetObservationTest() throws Exception {

        if (configFilesExist) {
            
            TimePositionType beginPosition = new TimePositionType("2007-02-11T00:00:00.000-06:00");
            TimePositionType endPosition = new TimePositionType("2007-02-12T00:00:00.000-06:00");
            TimePeriodType period = new TimePeriodType(beginPosition, endPosition);
            BinaryTemporalOpType tduring = new BinaryTemporalOpType(period);
            EventTime timeFilter = new EventTime(null, null, tduring);

            GetObservation request = new GetObservation("1.0.0", "offering-allSensor", Arrays.asList(timeFilter), Arrays.asList("urn:ogc:object:sensor:BRGM:3"), null, null, null, "text/xml; subtype=\"om/1.0.0\"", OBSERVATION_QNAME, ResponseModeType.RESULT_TEMPLATE, null);

            ObservationCollectionEntry expResult = (ObservationCollectionEntry) defaultWorker.getObservation(request);
            ObservationCollectionEntry result    = (ObservationCollectionEntry) genericWorker.getObservation(request);

            assertEquals(expResult.getMember().size(), result.getMember().size());
            ObservationEntry expObs = (ObservationEntry) expResult.getMember().iterator().next();
            ObservationEntry obs    = (ObservationEntry) result.getMember().iterator().next();
            assertEquals(expObs.getDefinition(), obs.getDefinition());
            assertEquals(expObs.getFeatureOfInterest(), obs.getFeatureOfInterest());
            assertEquals(expObs.getName(), obs.getName());
            assertEquals(expObs.getObservedProperty(), obs.getObservedProperty());
            assertEquals(expObs.getProcedure(), obs.getProcedure());
            assertEquals(expObs.getProcedureParameter(), obs.getProcedureParameter());
            assertEquals(expObs.getProcedureTime(), obs.getProcedureTime());
            assertEquals(expObs.getPropertyFeatureOfInterest(), obs.getPropertyFeatureOfInterest());
            assertEquals(expObs.getPropertyObservedProperty(), obs.getPropertyObservedProperty());
            assertEquals(expObs.getQuality(), obs.getQuality());
            DataArrayEntry expRes = ((DataArrayPropertyType)expObs.getResult()).getDataArray();
            DataArrayEntry res    = ((DataArrayPropertyType)obs.getResult()).getDataArray();
            assertEquals(expRes.getEncoding(), res.getEncoding());
            assertEquals(expRes.getValues(), res.getValues());

            assertTrue(expRes.getElementType() instanceof SimpleDataRecordEntry);
            assertTrue(res.getElementType()    instanceof SimpleDataRecordEntry);
            SimpleDataRecordEntry expElementType = (SimpleDataRecordEntry) expRes.getElementType();
            SimpleDataRecordEntry resElementType = (SimpleDataRecordEntry) res.getElementType();

            assertEquals(expElementType.getBlockId(), resElementType.getBlockId());
            assertEquals(expElementType.getDefinition(), resElementType.getDefinition());
            assertEquals(expElementType.getId(), resElementType.getId());
            assertEquals(expElementType.getField().size(), resElementType.getField().size());

            assertEquals(expElementType.getField(), resElementType.getField());
            assertEquals(expElementType, resElementType);

            assertEquals(expRes.getElementType(), res.getElementType());

            assertEquals(expRes.getElementCount(), res.getElementCount());

            assertEquals(expRes, res);
            assertEquals(expObs.getResult(), obs.getResult());
            assertEquals(expObs, obs);
            assertEquals(expResult.getMember(), result.getMember());
            assertEquals(expResult, result);

            GetObservation request2 = new GetObservation("1.0.0", "offering-allSensor", Arrays.asList(timeFilter), Arrays.asList("urn:ogc:object:sensor:BRGM:3"), Arrays.asList("depth"), null, null, "text/xml; subtype=\"om/1.0.0\"", OBSERVATION_QNAME, ResponseModeType.RESULT_TEMPLATE, null);

            ObservationCollectionEntry expResult2 = (ObservationCollectionEntry) defaultWorker.getObservation(request2);
            ObservationCollectionEntry result2    = (ObservationCollectionEntry) genericWorker.getObservation(request2);

            assertEquals(expResult2, result2);

            GetObservation request3 = new GetObservation("1.0.0", "offering-allSensor", Arrays.asList(timeFilter), Arrays.asList("urn:ogc:object:sensor:BRGM:3"), Arrays.asList("depth"), null, null, "text/xml; subtype=\"om/1.0.0\"", OBSERVATION_QNAME, ResponseModeType.INLINE, null);

            ObservationCollectionEntry expResult3 = (ObservationCollectionEntry) defaultWorker.getObservation(request3);
            ObservationCollectionEntry result3    = (ObservationCollectionEntry) genericWorker.getObservation(request3);

            assertEquals(expResult3, result3);



            TimePositionType position = new TimePositionType("2007-02-12T00:00:00.000-06:00");
            TimeInstantType instant   = new TimeInstantType(position);
            BinaryTemporalOpType tafter = new BinaryTemporalOpType(instant);
            timeFilter = new EventTime(tafter, null, null);
            GetObservation request4 = new GetObservation("1.0.0", "offering-allSensor", Arrays.asList(timeFilter), Arrays.asList("urn:ogc:object:sensor:BRGM:3", "urn:ogc:object:sensor:BRGM:4"), Arrays.asList("depth"), null, null, "text/xml; subtype=\"om/1.0.0\"", OBSERVATION_QNAME, ResponseModeType.INLINE, null);

            ObservationCollectionEntry expResult4 = (ObservationCollectionEntry) defaultWorker.getObservation(request4);
            ObservationCollectionEntry result4    = (ObservationCollectionEntry) genericWorker.getObservation(request4);

            assertEquals(expResult4, result4);

        } else {
            logger.info("configuration files missing skipping test");
        }
    }

    /**
     * Test simple lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void GetResultTest() throws Exception {

        if (configFilesExist) {
            TimePositionType beginPosition = new TimePositionType("2007-02-11T00:00:00.000-06:00");
            TimePositionType endPosition   = new TimePositionType("2007-02-12T00:00:00.000-06:00");
            TimePeriodType period          = new TimePeriodType(beginPosition, endPosition);
            BinaryTemporalOpType tduring   = new BinaryTemporalOpType(period);
            EventTime timeFilter           = new EventTime(null, null, tduring);

            GetObservation request = new GetObservation("1.0.0", "offering-allSensor", Arrays.asList(timeFilter), Arrays.asList("urn:ogc:object:sensor:BRGM:3"), null, null, null, "text/xml; subtype=\"om/1.0.0\"", OBSERVATION_QNAME, ResponseModeType.RESULT_TEMPLATE, null);

            ObservationCollectionEntry expResult1 = (ObservationCollectionEntry) defaultWorker.getObservation(request);
            ObservationCollectionEntry result1    = (ObservationCollectionEntry) genericWorker.getObservation(request);

            assertEquals(expResult1, result1);
            System.out.println("default template ID:" + expResult1.getMember().iterator().next().getName());
            System.out.println("generic template ID:" + result1.getMember().iterator().next().getName());

            TimePositionType position   = new TimePositionType("2007-02-11T00:00:00.000-06:00");
            TimeInstantType instant     = new TimeInstantType(position);
            BinaryTemporalOpType tafter = new BinaryTemporalOpType(instant);
            EventTime timeFilter2       = new EventTime(tafter, null, null);

            List<EventTime> eventTime   = new ArrayList<EventTime>();
            eventTime.add(timeFilter2);
            GetResult GRrequest = new GetResult("urn:ogc:object:observationTemplate:BRGM:3-0", eventTime);

            GetResultResponse expResult2 = defaultWorker.getResult(GRrequest);
            GetResultResponse result2    = genericWorker.getResult(GRrequest);

            System.out.println("RESULT:"  + expResult2.getResult().getValue());
            assertEquals(expResult2, result2);

        } else {
            logger.info("configuration files missing skipping test");
        }
    }
}
