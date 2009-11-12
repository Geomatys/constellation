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
import java.io.InputStream;
import org.constellation.util.Util;
import org.constellation.wfs.utils.PostgisUtils;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.data.collection.FeatureIterator;
import org.geotoolkit.feature.xml.XmlFeatureReader;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPEventFeatureReader;
import org.geotoolkit.feature.xml.jaxp.JAXPEventFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureReader;
import org.junit.*;
import org.opengis.feature.simple.SimpleFeature;

import org.opengis.feature.type.FeatureType;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FeatureXmlBindingTest {

    private static FeatureCollection fcoll;

    private XmlFeatureWriter featureWriter;

    private XmlFeatureReader featureReader;

    private static FeatureType featureType;

    @BeforeClass
    public static void setUpClass() throws Exception {
        fcoll       = PostgisUtils.createShapeLayer(Util.getFileFromResource("org.constellation.wfs.shapefile.admin_commune/ADMIN_COMMUNE.SHP"));
        featureType = fcoll.getSchema();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    

    @Before
    public void setUp() throws Exception {
        featureWriter = new JAXPEventFeatureWriter();
        featureReader = new JAXPEventFeatureReader(featureType);
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
        FeatureIterator ite = fcoll.features();
        SimpleFeature feature = null;
        if (ite.hasNext()) {
            feature = (SimpleFeature) ite.next();
        }
        ite.close();

        String result = featureWriter.write(feature);

        String expresult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.adminCommune_feature.xml"));
        
        //we unformat the expected result
        expresult = expresult.replace("\n", "");
        expresult = expresult.replaceAll("> *<", "><");

        assertEquals(expresult, result);
    }

    /**
     * test the featureCollection marshall
     *
     */
    @Test
    public void featureCollectionMarshallTest() throws Exception {
        String result = featureWriter.write(fcoll);

        String expresult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.adminCommune_Collection.xml"));

        //we unformat the expected result
        expresult = expresult.replace("\n", "");
        expresult = expresult.replaceAll("> *<", "><");

        // and we replace the space for the specified data
        assertEquals(expresult, result);
    }

    /**
     * test the feature marshall
     *
     */
    @Test
    public void featureUnMarshallTest() throws Exception {
        
        FeatureIterator ite = fcoll.features();
        SimpleFeature expResult = null;
        if (ite.hasNext()) {
            expResult = (SimpleFeature) ite.next();
        }
        ite.close();

        InputStream stream = Util.getResourceAsStream("org/constellation/wfs/xml/adminCommune_feature.xml");
        SimpleFeature result = (SimpleFeature) featureReader.read(stream);

        System.out.println("result");


        assertEquals(expResult.getIdentifier(), result.getIdentifier());
        assertEquals(expResult.getID(), result.getID());


        assertEquals(expResult.getFeatureType(), result.getFeatureType());
        assertEquals(expResult.getAttributeCount(), result.getAttributeCount());
      //  assertEquals(expResult.getAttributes(), result.getAttributes());
        for (int j = 0; j < expResult.getAttributeCount(); j++) {
            if (expResult.getAttributes().get(j) instanceof Geometry) {
                assertTrue(((Geometry) expResult.getAttributes().get(j)).equals((Geometry) result.getAttributes().get(j)));
            } else {
                assertEquals(expResult.getAttributes().get(j), result.getAttributes().get(j));
            }
        }
        assertEquals(expResult, result);
    }
}
