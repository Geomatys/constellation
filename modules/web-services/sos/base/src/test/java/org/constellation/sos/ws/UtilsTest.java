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

import org.apache.sis.xml.MarshallerPool;
import org.constellation.util.Util;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.gml.xml.v311.PointType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.observation.ObservationStoreException;
import org.geotoolkit.observation.xml.v100.ObservationType;
import org.geotoolkit.sampling.xml.v100.SamplingPointType;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import org.geotoolkit.swe.xml.v101.PhenomenonType;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.observation.Observation;

import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class UtilsTest {

    private static MarshallerPool marshallerPool;


    @BeforeClass
    public static void setUpClass() throws Exception {
        marshallerPool = SensorMLMarshallerPool.getInstance();
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

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getPeriodDescriptionTest() throws Exception {

        assertEquals("1s 12ms", TemporalUtilities.durationToString(1012));

        assertEquals("1min 7s 12ms",TemporalUtilities.durationToString(67012));
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getPhysicalIDTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        AbstractSensorML sensor = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));
        String phyID = SOSUtils.getPhysicalID(sensor);
        assertEquals("00ARGLELES", phyID);

        sensor = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component.xml"));
        phyID  = SOSUtils.getPhysicalID(sensor);
        assertEquals("00ARGLELES_2000", phyID);

        sensor = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component2.xml"));
        phyID  = SOSUtils.getPhysicalID(sensor);
        assertEquals(null, phyID);

        marshallerPool.recycle(unmarshaller);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getNetworkNamesTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        AbstractSensorML sensor = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));
        List<String> names      = SOSUtils.getNetworkNames(sensor);
        List<String> expNames   = new ArrayList<>();
        expNames.add("600000221");
        expNames.add("600000025");
        assertEquals(expNames, names);

        sensor   = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component.xml"));
        names    = SOSUtils.getNetworkNames(sensor);
        expNames = new ArrayList<>();
        assertEquals(expNames, names);

        marshallerPool.recycle(unmarshaller);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getSensorPositionTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        AbstractSensorML sensor = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));
        AbstractGeometry result = SOSUtils.getSensorPosition(sensor);
        DirectPositionType posExpResult = new DirectPositionType("urn:ogc:crs:EPSG:27582", 2, Arrays.asList(65400.0,1731368.0));
        PointType expResult = new PointType(posExpResult);

        assertEquals(expResult, result);

        sensor    = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component.xml"));
        result    = SOSUtils.getSensorPosition(sensor);
        expResult = null;

        assertEquals(expResult, result);

        marshallerPool.recycle(unmarshaller);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getTimeValueTest() throws Exception {

        TimePositionType position = new TimePositionType("2007-05-01T07:59:00.0");
        String result             = SOSUtils.getTimeValue(position.getDate());
        String expResult          = "2007-05-01 07:59:00.0";

        assertEquals(expResult, result);

        position = new TimePositionType("2007051T07:59:00.0");

        boolean exLaunched = false;
        try {
            SOSUtils.getTimeValue(position.getDate());
        } catch (ObservationStoreException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "eventTime");
        }

        assertFalse(exLaunched);

        String t = null;
        position = new TimePositionType(t);

        exLaunched = false;
        try {
            SOSUtils.getTimeValue(position.getDate());
        } catch (ObservationStoreException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "eventTime");
        }

        assertTrue(exLaunched);

        exLaunched = false;
        try {
            SOSUtils.getTimeValue(null);
        } catch (ObservationStoreException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "eventTime");
        }

        assertTrue(exLaunched);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getLuceneTimeValueTest() throws Exception {

        TimePositionType position = new TimePositionType("2007-05-01T07:59:00.0");
        String result             = SOSUtils.getLuceneTimeValue(position.getDate());
        String expResult          = "20070501075900";

        assertEquals(expResult, result);

        position = new TimePositionType("2007051T07:59:00.0");

        boolean exLaunched = false;
        try {
            SOSUtils.getLuceneTimeValue(position.getDate());
        } catch (ObservationStoreException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "eventTime");
        }

        assertFalse(exLaunched);

        String t = null;
        position = new TimePositionType(t);

        exLaunched = false;
        try {
            SOSUtils.getLuceneTimeValue(position.getDate());
        } catch (ObservationStoreException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "eventTime");
        }

        assertTrue(exLaunched);

        exLaunched = false;
        try {
            SOSUtils.getLuceneTimeValue(null);
        } catch (ObservationStoreException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "eventTime");
        }

        assertTrue(exLaunched);
    }

     /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getCollectionBoundTest() throws Exception {

        PhenomenonType pheno = new PhenomenonType("test", "test");

        List<Observation> observations = new ArrayList<>();

        ObservationType obs1 = new ObservationType();
        ObservationType obs2 = new ObservationType();
        ObservationType obs3 = new ObservationType();

        observations.add(obs1);
        observations.add(obs2);
        observations.add(obs3);

        Envelope result = SOSUtils.getCollectionBound("1.0.0", observations, "urn:ogc:def:crs:EPSG::4326");

        EnvelopeType expResult = new EnvelopeType(null, new DirectPositionType(-180.0, -90.0), new DirectPositionType(180.0, 90.0), "urn:ogc:def:crs:EPSG::4326");
        expResult.setSrsDimension(2);
        expResult.setAxisLabels("Y X");
        assertEquals(expResult, result);


        SamplingPointType sp1 = new SamplingPointType(null, null, null, null, null);
        sp1.setBoundedBy(new EnvelopeType(null, new DirectPositionType(-10.0, -10.0), new DirectPositionType(10.0, 10.0), "urn:ogc:def:crs:EPSG::4326"));
        obs1 = new ObservationType(null, null, sp1, pheno, null, this, null);

        SamplingPointType sp2 = new SamplingPointType(null, null, null, null, null);
        sp2.setBoundedBy(new EnvelopeType(null, new DirectPositionType(-5.0, -5.0), new DirectPositionType(15.0, 15.0), "urn:ogc:def:crs:EPSG::4326"));
        obs2 = new ObservationType(null, null, sp2, pheno, null, this, null);

        SamplingPointType sp3 = new SamplingPointType(null, null, null, null, null);
        sp3.setBoundedBy(new EnvelopeType(null, new DirectPositionType(0.0, -8.0), new DirectPositionType(20.0, 10.0), "urn:ogc:def:crs:EPSG::4326"));
        obs3 = new ObservationType(null, null, sp3, pheno, null, this, null);

        observations = new ArrayList<>();
        observations.add(obs1);
        observations.add(obs2);
        observations.add(obs3);

        result = SOSUtils.getCollectionBound("1.0.0", observations, "urn:ogc:def:crs:EPSG::4326");

        expResult = new EnvelopeType(null, new DirectPositionType(-10.0, -10.0), new DirectPositionType(20.0, 15.0), "urn:ogc:def:crs:EPSG::4326");
        expResult.setSrsDimension(2);
        expResult.setAxisLabels("Y X");
        
        assertEquals(expResult, result);

    }
}
