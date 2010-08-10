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

import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

// Junit dependencies
import javax.xml.bind.Unmarshaller;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.observation.xml.v100.ObservationCollectionEntry;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.sampling.xml.v100.SamplingPointEntry;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.swe.xml.v101.PhenomenonEntry;
import org.geotoolkit.xml.MarshallerPool;
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class UtilsTest {

    private static final Logger LOGGER = Logger.getLogger("org.constellation.sos.ws");

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

        assertEquals("1s 12ms", Utils.getPeriodDescription(1012));

        assertEquals("1min 7s 12ms", Utils.getPeriodDescription(67012));
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getPhysicalIDTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        AbstractSensorML sensor = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));
        String phyID = Utils.getPhysicalID(sensor);
        assertEquals("00ARGLELES", phyID);

        sensor = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component.xml"));
        phyID  = Utils.getPhysicalID(sensor);
        assertEquals("00ARGLELES_2000", phyID);

        sensor = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component2.xml"));
        phyID  = Utils.getPhysicalID(sensor);
        assertEquals(null, phyID);

        marshallerPool.release(unmarshaller);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getNetworkNamesTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        AbstractSensorML sensor = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));
        List<String> names      = Utils.getNetworkNames(sensor);
        List<String> expNames   = new ArrayList<String>();
        expNames.add("600000221");
        expNames.add("600000025");
        assertEquals(expNames, names);

        sensor   = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component.xml"));
        names    = Utils.getNetworkNames(sensor);
        expNames = new ArrayList<String>();
        assertEquals(expNames, names);

        marshallerPool.release(unmarshaller);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getSensorPositionTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        AbstractSensorML sensor = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));
        DirectPositionType result = Utils.getSensorPosition(sensor);
        DirectPositionType expResult = new DirectPositionType("urn:ogc:crs:EPSG:27582", 2, Arrays.asList(65400.0,1731368.0));

        assertEquals(expResult, result);

        sensor    = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component.xml"));
        result    = Utils.getSensorPosition(sensor);
        expResult = null;

        assertEquals(expResult, result);

        marshallerPool.release(unmarshaller);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getTimeValueTest() throws Exception {

        TimePositionType position = new TimePositionType("2007-05-01T07:59:00.0");
        String result             = Utils.getTimeValue(position);
        String expResult          = "2007-05-01 07:59:00.0";

        assertEquals(expResult, result);

        position = new TimePositionType("2007051T07:59:00.0");

        boolean exLaunched = false;
        try {
            Utils.getTimeValue(position);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "eventTime");
        }

        assertTrue(exLaunched);

        String t = null;
        position = new TimePositionType(t);

        exLaunched = false;
        try {
            Utils.getTimeValue(position);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "eventTime");
        }

        assertTrue(exLaunched);

        exLaunched = false;
        try {
            Utils.getTimeValue(null);
        } catch (CstlServiceException ex) {
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
        String result             = Utils.getLuceneTimeValue(position);
        String expResult          = "20070501075900";

        assertEquals(expResult, result);

        position = new TimePositionType("2007051T07:59:00.0");

        boolean exLaunched = false;
        try {
            Utils.getLuceneTimeValue(position);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "eventTime");
        }

        assertTrue(exLaunched);

        String t = null;
        position = new TimePositionType(t);

        exLaunched = false;
        try {
            Utils.getLuceneTimeValue(position);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "eventTime");
        }

        assertTrue(exLaunched);

        exLaunched = false;
        try {
            Utils.getLuceneTimeValue(null);
        } catch (CstlServiceException ex) {
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

        PhenomenonEntry pheno = new PhenomenonEntry("test", "test");

        ObservationCollectionEntry collection = new ObservationCollectionEntry();

        ObservationEntry obs1 = new ObservationEntry();
        ObservationEntry obs2 = new ObservationEntry();
        ObservationEntry obs3 = new ObservationEntry();

        collection.add(obs1);
        collection.add(obs2);
        collection.add(obs3);

        EnvelopeEntry result = Utils.getCollectionBound(collection, "urn:ogc:def:crs:EPSG::4326");

        EnvelopeEntry expResult = new EnvelopeEntry(null, new DirectPositionType(-180.0, -90.0), new DirectPositionType(180.0, 90.0), "urn:ogc:def:crs:EPSG::4326");
        expResult.setSrsDimension(2);
        expResult.setAxisLabels("Y X");
        assertEquals(expResult, result);


        SamplingPointEntry sp1 = new SamplingPointEntry(null, null, null, null, null);
        sp1.setBoundedBy(new EnvelopeEntry(null, new DirectPositionType(-10.0, -10.0), new DirectPositionType(10.0, 10.0), "urn:ogc:def:crs:EPSG::4326"));
        obs1 = new ObservationEntry(null, null, sp1, pheno, null, this, null);

        SamplingPointEntry sp2 = new SamplingPointEntry(null, null, null, null, null);
        sp2.setBoundedBy(new EnvelopeEntry(null, new DirectPositionType(-5.0, -5.0), new DirectPositionType(15.0, 15.0), "urn:ogc:def:crs:EPSG::4326"));
        obs2 = new ObservationEntry(null, null, sp2, pheno, null, this, null);

        SamplingPointEntry sp3 = new SamplingPointEntry(null, null, null, null, null);
        sp3.setBoundedBy(new EnvelopeEntry(null, new DirectPositionType(0.0, -8.0), new DirectPositionType(20.0, 10.0), "urn:ogc:def:crs:EPSG::4326"));
        obs3 = new ObservationEntry(null, null, sp3, pheno, null, this, null);

        collection = new ObservationCollectionEntry();
        collection.add(obs1);
        collection.add(obs2);
        collection.add(obs3);

        result = Utils.getCollectionBound(collection, "urn:ogc:def:crs:EPSG::4326");

        expResult = new EnvelopeEntry(null, new DirectPositionType(-10.0, -10.0), new DirectPositionType(20.0, 15.0), "urn:ogc:def:crs:EPSG::4326");
        expResult.setSrsDimension(2);
        expResult.setAxisLabels("Y X");
        
        assertEquals(expResult, result);

    }
}
