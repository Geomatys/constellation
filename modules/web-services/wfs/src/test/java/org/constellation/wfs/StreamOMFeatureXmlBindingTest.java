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

package org.constellation.wfs;

import com.vividsolutions.jts.geom.Geometry;
import java.io.File;
import java.io.InputStream;

import java.sql.Connection;
import org.constellation.util.Util;
import org.constellation.wfs.utils.PostgisUtils;

import org.geotoolkit.data.DataUtilities;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.xml.XmlFeatureReader;
import org.geotoolkit.feature.xml.XmlFeatureTypeReader;
import org.geotoolkit.feature.xml.XmlFeatureTypeWriter;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeReader;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureReader;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.FileUtilities;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class StreamOMFeatureXmlBindingTest {

    private static DefaultDataSource ds = null;
    
    private static FeatureCollection fcoll;

    private XmlFeatureWriter featureWriter;

    private XmlFeatureReader featureReader;

    private XmlFeatureTypeReader featureTypeReader;

    private XmlFeatureTypeWriter featureTypeWriter;

    private static FeatureType featureType;

    @BeforeClass
    public static void setUpClass() throws Exception {
        final String url = "jdbc:derby:memory:TestStream;create=true";
        ds = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        Util.executeSQLScript("org/constellation/sql/structure-observations.sql", con);
        Util.executeSQLScript("org/constellation/sql/sos-data.sql", con);

        FeatureReader fr = PostgisUtils.createEmbeddedOMLayer(url, new DefaultName("http://www.opengis.net/sampling/1.0","SamplingPoint"));
        featureType      = fr.getFeatureType();
        fcoll            = DataUtilities.collection("collection-1", featureType);
        while (fr.hasNext()) {
            fcoll.add(fr.next());
        }

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (ds != null) {
            ds.shutdown();
        }
        File dlog = new File("derby.log");
        if (dlog.exists()) {
            dlog.delete();
        }
    }



    @Before
    public void setUp() throws Exception {
        featureWriter     = new JAXPStreamFeatureWriter();
        featureReader     = new JAXPStreamFeatureReader(featureType);
        featureTypeReader = new JAXBFeatureTypeReader();
        featureTypeWriter = new JAXBFeatureTypeWriter();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * test the feature marshall
     *
     */
    @Test
    public void featureMarshallTest() throws Exception {
        FeatureIterator ite = fcoll.iterator();
        SimpleFeature feature = null;
        if (ite.hasNext()) {
            feature = (SimpleFeature) ite.next();
        }
        ite.close();

        String result = featureWriter.write(feature);

        String expresult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPoint-1.xml"));

        //we unformat the expected result
        expresult = expresult.replace("\n", "");
        expresult = expresult.replaceAll("> *<", "><");
        expresult = expresult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");

        expresult = removeXmlns(expresult);
        result    = removeXmlns(result);
        
        assertEquals(expresult, result);
    }

    /**
     * test the featureCollection marshall
     *
     */
    @Test
    public void featureCollectionMarshallTest() throws Exception {
        String result = featureWriter.write(fcoll);

        String expresult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection.xml"));

        //we unformat the expected result
        expresult = expresult.replace("\n", "");
        expresult = expresult.replaceAll("> *<", "><");
        expresult = expresult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");

        expresult = removeXmlns(expresult);
        result    = removeXmlns(result);
        
        // and we replace the space for the specified data
        assertEquals(expresult, result);
    }

    /**
     * test the feature unmarshall
     *
     */
    @Test
    public void featureUnMarshallTest() throws Exception {

        FeatureIterator ite = fcoll.iterator();
        SimpleFeature expResult = null;
        if (ite.hasNext()) {
            expResult = (SimpleFeature) ite.next();
        }
        ite.close();

        InputStream stream = Util.getResourceAsStream("org/constellation/wfs/xml/samplingPoint-1.xml");
        SimpleFeature result = (SimpleFeature) featureReader.read(stream);
        assertTrue(result != null);
        
        featureEquals(expResult, result);
    }

    /**
     * test the feature marshall
     *
     */
    @Test
    public void featureCollectionUnMarshallTest() throws Exception {

        InputStream stream = Util.getResourceAsStream("org/constellation/wfs/xml/samplingPointCollection.xml");
        FeatureCollection result = (FeatureCollection) featureReader.read(stream);
        assertTrue(result != null);

        assertEquals(fcoll.getID(), result.getID());
        assertEquals(fcoll.size(), result.size());
        // TODO assertTrue(fcoll.getBounds().equals(result.getBounds()));
        assertEquals(fcoll.getFeatureType(), result.getFeatureType());

        FeatureIterator expIterator = fcoll.iterator();
        FeatureIterator resIterator = result.iterator();
        SimpleFeature temp          = null;
        while (expIterator.hasNext()) {
            SimpleFeature expFeature = (SimpleFeature)expIterator.next();
            SimpleFeature resFeature = (SimpleFeature)resIterator.next();

            featureEquals(expFeature, resFeature);
        }
        expIterator.close();
        resIterator.close();

    }

    /**
     * test the feature unmarshall
     *
     */
    @Test
    public void featuretypeUnMarshallTest() throws Exception {

        InputStream stream = Util.getResourceAsStream("org/constellation/wfs/xsd/sampling.xsd");
        FeatureType result  = featureTypeReader.read(stream, "SamplingPoint");

//        assertEquals(featureType, result);

    }

     /**
     * test the feature unmarshall
     *
     */
    @Test
    public void featuretypeMarshallTest() throws Exception {
        String expResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org/constellation/wfs/xsd/sampling.xsd"));
        String result    = featureTypeWriter.write(featureType);

        expResult = removeXmlns(expResult);
        result    = removeXmlns(result);
        assertEquals(expResult, result);
    }


    public void featureEquals(SimpleFeature expResult, SimpleFeature result) {
        assertEquals(expResult.getIdentifier(), result.getIdentifier());
        assertEquals(expResult.getID(), result.getID());


        assertEquals(expResult.getFeatureType(), result.getFeatureType());
        assertEquals(expResult.getAttributeCount(), result.getAttributeCount());

        for (int j = 0; j < expResult.getAttributeCount(); j++) {
            if (expResult.getAttributes().get(j) instanceof Geometry) {
                assertTrue(((Geometry) expResult.getAttributes().get(j)).equals((Geometry) result.getAttributes().get(j)));
            } else {
                assertEquals(expResult.getAttributes().get(j), result.getAttributes().get(j));
            }
        }
        assertEquals(expResult, result);
    }

    public String removeXmlns(String xml) {

        String s = xml;
        s = s.replaceAll("xmlns=\"[^\"]*\" ", "");

        s = s.replaceAll("xmlns=\"[^\"]*\"", "");

        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\" ", "");

        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\"", "");


        return s;
    }
}
