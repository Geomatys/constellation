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


package org.constellation.metadata.io;

import java.util.UUID;
import org.geotoolkit.xml.AnchoredMarshallerPool;
import java.io.StringReader;
import java.sql.Connection;
import javax.xml.bind.Unmarshaller;

// cstl dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.CSWworkerTest;
import org.constellation.util.Util;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import static org.constellation.metadata.CSWTestUtils.*;

// Geotoolkit dependencies
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.feature.catalog.FeatureCatalogueImpl;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.xml.MarshallerPool;

// Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebMetadataReaderTest {


    private static Automatic configuration;

    private static MDWebMetadataReader reader;

    private static MarshallerPool pool;

    private static DefaultDataSource ds;

    @BeforeClass
    public static void setUpClass() throws Exception {

        System.out.println(UUID.randomUUID());
        pool = CSWMarshallerPool.getInstance();
        CSWworkerTest.fillPoolAnchor((AnchoredMarshallerPool) pool);

        final String url = "jdbc:derby:memory:MMRTest;create=true";
        ds               = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/model/mdw_schema_2.1(derby).sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19115.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19119.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19108.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19110.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19115-2.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/data/defaultRecordSets.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/users/creation_user.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/catalog_web_service.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ebrimv2.5.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ebrimv3.0.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/metadata/sql/csw-data.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/metadata/sql/csw-data-3.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/metadata/sql/csw-data-5.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/metadata/sql/csw-data-6.sql"));
        
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/SensorML_v2.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/sml-data_v2.sql"));

        //we write the configuration file
        BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
        configuration = new Automatic("mdweb", bdd);
        
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        
        if (ds != null) {
            ds.shutdown();
        }
    }

    /*
     * TODO LOOK out for cache of CLasses in mdw meta reader
     *
     * SensorML timePeriod != iso timePeriod
     *
     */

    @Before
    public void setUp() throws Exception {
        reader = new MDWebMetadataReader(configuration);
    }

    @After
    public void tearDown() throws Exception {
        if (reader != null) {
            reader.destroy();
        }
    }

    /**
     * Tests the getMetadata method for ISO 19139 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISOTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("42292_5p_19900609195600", AbstractMetadataReader.ISO_19115, null);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        result = reader.getMetadata("identifier-test", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta9.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        pool.release(unmarshaller);
    }

    /**
     * Tests the getMetadata method for ISO 19115 French profile data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISOFRATest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("MDweb-2064d5c7-80b5-4840-ba3d-4fa4e23be750", AbstractMetadataReader.ISO_19115, null);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta-fra1.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);


        pool.release(unmarshaller);
    }

     /**
     * Tests the getMetadata method for ISO 19139 data with GML geometries
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISOGMLTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("IGNF_PVA_1-0__1968__C0620-0111_CDP_5569_8959.xml", AbstractMetadataReader.ISO_19115, null);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta10.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        /*
         * Point coordinates
         */
        result = reader.getMetadata("af24f70a-818c-4da1-9afb-1fc1e0058760", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_11));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        /*
         * Point pos
         */
        result = reader.getMetadata("1c7d52ac-66c5-449b-a88b-8a0feeccb5fa", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_12));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        /*
         * Curve posList
         */
        result = reader.getMetadata("4c017cc5-3e0e-49d5-9f68-549943247e7e", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_13));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);


        /*
         * LineString coordinates
         */
        result = reader.getMetadata("4c017cc5-3e0e-49d5-9f68-549943247e89", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_14));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        /*
         * MultiLineString
         */
        result = reader.getMetadata("484fc4d9-8d11-48a5-a386-65c19398f7c3", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_15));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

         /*
         * MultiPoint
         */
        result = reader.getMetadata("484fc4d9-8d11-48a5-a386-65c19398f7k7", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_16));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);


        /*
         * Surface
         */
        result = reader.getMetadata("28644bf0-5d9d-4ebd-bef0-f2b0b2067b26", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_18));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        /*
         * Polygon
         */
        result = reader.getMetadata("937491cd-4bc4-43e4-9509-f6cc606f906e", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_19));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        pool.release(unmarshaller);
    }

      /**
     * Tests the getMetadata method for ISO 19119 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISOGeoRectifiedTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("81a25c84-2bb0-4727-8f36-4a296e1e7b57", AbstractMetadataReader.ISO_19115, null);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_17));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        pool.release(unmarshaller);
    }

     /**
     * Tests the getMetadata method for ISO 19110 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISO19110Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("cat-1", AbstractMetadataReader.ISO_19115, null);

        FeatureCatalogueImpl expResult = (FeatureCatalogueImpl) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/featcatalog1.xml"));

        assertTrue(result instanceof FeatureCatalogueImpl);
        catalogueEquals(expResult, (FeatureCatalogueImpl)result);

        pool.release(unmarshaller);
    }

    /**
     * Tests the getMetadata method for ISO 19119 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISO19119Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4", AbstractMetadataReader.ISO_19115, null);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta-19119.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        pool.release(unmarshaller);
    }

    /**
     * Tests the getMetadata method for ISO 19115-2 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISO191152Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX", AbstractMetadataReader.ISO_19115, null);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/imageMetadata.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        pool.release(unmarshaller);
    }

    /**
     * Tests the getMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataSystemSMLTest() throws Exception {

        
        Object absResult = reader.getMetadata("urn:ogc:object:sensor:GEOM:1", AbstractMetadataReader.SENSORML, null);

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        AbstractSensorML absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) absExpResult;


        SystemSMLEquals(expResult, result);


        absResult = reader.getMetadata("urn:ogc:object:sensor:GEOM:3", AbstractMetadataReader.SENSORML, null);

        absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system2.xml"));

        pool.release(unmarshaller);
        
        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        result = (SensorML) absResult;
        expResult = (SensorML) absExpResult;


        SystemSMLEquals(expResult, result);
        
    }

    /**
     * Tests the getMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataComponentSMLTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object absResult = reader.getMetadata("urn:ogc:object:sensor:GEOM:2", AbstractMetadataReader.SENSORML, null);

        AbstractSensorML absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component.xml"));
        
        pool.release(unmarshaller);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) absExpResult;
        
        ComponentEquals(expResult, result);
    }
}
