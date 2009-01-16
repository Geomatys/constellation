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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.gml.v311.TimeInstantType;
import org.constellation.gml.v311.TimePeriodType;
import org.constellation.gml.v311.TimePositionType;
import org.constellation.observation.ObservationCollectionEntry;
import org.constellation.observation.ObservationEntry;
import org.constellation.ogc.BinaryTemporalOpType;
import org.constellation.sos.Capabilities;
import org.constellation.sos.EventTime;
import org.constellation.sos.GetCapabilities;
import org.constellation.sos.GetObservation;
import org.constellation.sos.GetResult;
import org.constellation.sos.GetResultResponse;
import org.constellation.sos.ObservationOfferingEntry;
import org.constellation.sos.ResponseModeType;
import org.constellation.swe.v101.DataArrayEntry;
import org.constellation.swe.v101.DataArrayPropertyType;
import org.constellation.swe.v101.SimpleDataRecordEntry;
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.constellation.sos.ws.SOSworker.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal
 */
public class SosIOTest {

    private Logger logger = Logger.getLogger("org.constellation.sos.ws");


    private SOSworker defaultWorker;

    private SOSworker genericWorker;

    private Marshaller marshaller;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    public SosIOTest() throws Exception {
        File capabilitiesFile = new File("/home/guilhem/netbeans project/Constellation/sosDefaultConfig/sos_configuration/SOSCapabilities1.0.0.xml");
        JAXBContext context = JAXBContext.newInstance("org.constellation.sos:org.constellation.ows.v110");
        Unmarshaller unmarshaller = context.createUnmarshaller();
        marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        Object obj = unmarshaller.unmarshal(capabilitiesFile);
        Capabilities staticCapabilities = null;
        if (obj instanceof Capabilities) {
            staticCapabilities = (Capabilities) obj;
        }

        File configDirectory1 = new File("sosDefaultConfig");
        defaultWorker = new SOSworker(DISCOVERY, configDirectory1);
        defaultWorker.setStaticCapabilities(staticCapabilities);

        File configDirectory2 = new File("sosGenericConfig");
        genericWorker = new SOSworker(DISCOVERY, configDirectory2);
        genericWorker.setStaticCapabilities(staticCapabilities);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void GetCapabilitiesTest() throws Exception {
        GetCapabilities request = new GetCapabilities("1.0.0", "application/xml");
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

    }
    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void GetObservationTest() throws Exception {

        TimePositionType beginPosition = new TimePositionType("2007-02-11T00:00:00.000-06:00");
        TimePositionType endPosition = new TimePositionType("2007-02-12T00:00:00.000-06:00");
        TimePeriodType period = new TimePeriodType(beginPosition, endPosition);
        BinaryTemporalOpType tduring = new BinaryTemporalOpType(period);
        EventTime timeFilter = new EventTime(null, null, tduring);

        GetObservation request = new GetObservation("1.0.0", "offering-allSensor", Arrays.asList(timeFilter), Arrays.asList("urn:ogc:object:sensor:BRGM:3"), null, null, null, "text/xml; subtype=\"om/1.0.0\"", observation_QNAME, ResponseModeType.RESULT_TEMPLATE, null);

        ObservationCollectionEntry expResult = defaultWorker.getObservation(request);
        ObservationCollectionEntry result    = genericWorker.getObservation(request);

        assertEquals(expResult.getMember().size(), result.getMember().size());
        ObservationEntry expObs = expResult.getMember().iterator().next();
        ObservationEntry obs    = result.getMember().iterator().next();
        assertEquals(expObs.getDefinition(), obs.getDefinition());
        assertEquals(expObs.getDistribution(), obs.getDistribution());
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

        GetObservation request2 = new GetObservation("1.0.0", "offering-allSensor", Arrays.asList(timeFilter), Arrays.asList("urn:ogc:object:sensor:BRGM:3"), Arrays.asList("depth"), null, null, "text/xml; subtype=\"om/1.0.0\"", observation_QNAME, ResponseModeType.RESULT_TEMPLATE, null);

        ObservationCollectionEntry expResult2 = defaultWorker.getObservation(request2);
        ObservationCollectionEntry result2    = genericWorker.getObservation(request2);

        assertEquals(expResult2, result2);

        GetObservation request3 = new GetObservation("1.0.0", "offering-allSensor", Arrays.asList(timeFilter), Arrays.asList("urn:ogc:object:sensor:BRGM:3"), Arrays.asList("depth"), null, null, "text/xml; subtype=\"om/1.0.0\"", observation_QNAME, ResponseModeType.INLINE, null);

        ObservationCollectionEntry expResult3 = defaultWorker.getObservation(request3);
        ObservationCollectionEntry result3    = genericWorker.getObservation(request3);

        assertEquals(expResult3, result3);
    }

    /**
     * Test simple lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void GetResultTest() throws Exception {

        TimePositionType beginPosition = new TimePositionType("2007-02-11T00:00:00.000-06:00");
        TimePositionType endPosition   = new TimePositionType("2007-02-12T00:00:00.000-06:00");
        TimePeriodType period          = new TimePeriodType(beginPosition, endPosition);
        BinaryTemporalOpType tduring   = new BinaryTemporalOpType(period);
        EventTime timeFilter           = new EventTime(null, null, tduring);

        GetObservation request = new GetObservation("1.0.0", "offering-allSensor", Arrays.asList(timeFilter), Arrays.asList("urn:ogc:object:sensor:BRGM:3"), null, null, null, "text/xml; subtype=\"om/1.0.0\"", observation_QNAME, ResponseModeType.RESULT_TEMPLATE, null);

        ObservationCollectionEntry expResult1 = defaultWorker.getObservation(request);
        ObservationCollectionEntry result1    = genericWorker.getObservation(request);

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

    }
}
