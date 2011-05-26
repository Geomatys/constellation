/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2011, Geomatys
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

import org.geotoolkit.ebrim.xml.v300.RegistryPackageType;
import javax.xml.bind.JAXBElement;
import java.io.StringReader;
import java.sql.Connection;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.Util;
import org.geotoolkit.csw.xml.v202.RecordType;
import static org.constellation.test.utils.MetadataUtilities.*;

// Geotoolkit dependencies
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.ebrim.xml.v250.ExtrinsicObjectType;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.feature.catalog.FeatureCatalogueImpl;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.util.ComparisonMode;
import org.geotoolkit.util.Utilities;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.xml.AnchoredMarshallerPool;

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

        pool = EBRIMMarshallerPool.getInstance();
        StaticMetadata.fillPoolAnchor((AnchoredMarshallerPool) pool);

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
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-2.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-3.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-4.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-5.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-6.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-7.sql"));
        
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/SensorML_v2.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/sml-data_v2.sql"));

        //we write the configuration file
        BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
        configuration = new Automatic("mdweb", bdd);
        configuration.setEnableThread("false");
        configuration.setEnablecache("true");
        configuration.setStoreMapping("false");
        reader = new MDWebMetadataReader(configuration);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (reader != null) {
            reader.destroy();
        }
        if (ds != null) {
            ds.shutdown();
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Tests the getMetadata method for ISO 19139 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataDublinCoreTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd", AbstractMetadataReader.DUBLINCORE);

        RecordType expResult = (RecordType) ((JAXBElement)unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta8.xml"))).getValue();

        assertTrue(result instanceof RecordType);
        assertEquals(expResult, (RecordType)result);

        pool.release(unmarshaller);
    }
    
    /**
     * Tests the getMetadata method for ISO 19139 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISO19115Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("42292_5p_19900609195600", AbstractMetadataReader.ISO_19115);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        result = reader.getMetadata("identifier-test", AbstractMetadataReader.ISO_19115);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta9.xml"));

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
        Object result = reader.getMetadata("MDweb-2064d5c7-80b5-4840-ba3d-4fa4e23be750", AbstractMetadataReader.ISO_19115);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta-fra1.xml"));

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
    public void getMetadataISOXlinkTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("666-999-666", AbstractMetadataReader.ISO_19115);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_20));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);
        
        result = reader.getMetadata("999-666-999", AbstractMetadataReader.ISO_19115);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_21));

        assertTrue(result instanceof DefaultMetadata);
        assertTrue(Utilities.deepEquals(expResult, (DefaultMetadata)result, ComparisonMode.BY_CONTRACT));
        assertFalse(Utilities.equals(expResult, (DefaultMetadata)result));
        
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
        Object result = reader.getMetadata("IGNF_PVA_1-0__1968__C0620-0111_CDP_5569_8959.xml", AbstractMetadataReader.ISO_19115);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta10.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        /*
         * Point coordinates
         */
        result = reader.getMetadata("af24f70a-818c-4da1-9afb-1fc1e0058760", AbstractMetadataReader.ISO_19115);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_11));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        /*
         * Point pos
         */
        result = reader.getMetadata("1c7d52ac-66c5-449b-a88b-8a0feeccb5fa", AbstractMetadataReader.ISO_19115);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_12));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        /*
         * Curve posList
         */
        result = reader.getMetadata("4c017cc5-3e0e-49d5-9f68-549943247e7e", AbstractMetadataReader.ISO_19115);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_13));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);


        /*
         * LineString coordinates
         */
        result = reader.getMetadata("4c017cc5-3e0e-49d5-9f68-549943247e89", AbstractMetadataReader.ISO_19115);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_14));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        /*
         * MultiLineString
         */
        result = reader.getMetadata("484fc4d9-8d11-48a5-a386-65c19398f7c3", AbstractMetadataReader.ISO_19115);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_15));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

         /*
         * MultiPoint
         */
        result = reader.getMetadata("484fc4d9-8d11-48a5-a386-65c19398f7k7", AbstractMetadataReader.ISO_19115);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_16));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);


        /*
         * Surface
         */
        result = reader.getMetadata("28644bf0-5d9d-4ebd-bef0-f2b0b2067b26", AbstractMetadataReader.ISO_19115);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_18));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        /*
         * Polygon
         */
        result = reader.getMetadata("937491cd-4bc4-43e4-9509-f6cc606f906e", AbstractMetadataReader.ISO_19115);

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
        Object result = reader.getMetadata("81a25c84-2bb0-4727-8f36-4a296e1e7b57", AbstractMetadataReader.ISO_19115);

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
        Object result = reader.getMetadata("cat-1", AbstractMetadataReader.ISO_19115);

        FeatureCatalogueImpl expResult = (FeatureCatalogueImpl) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/featcatalog1.xml"));

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
        Object result = reader.getMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4", AbstractMetadataReader.ISO_19115);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta-19119.xml"));

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
        Object result = reader.getMetadata("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX", AbstractMetadataReader.ISO_19115);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/imageMetadata.xml"));

        assertTrue(result instanceof DefaultMetadata);
        pool.acquireMarshaller().marshal(expResult, System.out);
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

        
        Object absResult = reader.getMetadata("urn:ogc:object:sensor:GEOM:1", AbstractMetadataReader.SENSORML);

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        AbstractSensorML absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) absExpResult;


        systemSMLEquals(expResult, result);


        absResult = reader.getMetadata("urn:ogc:object:sensor:GEOM:3", AbstractMetadataReader.SENSORML);

        absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system2.xml"));

        pool.release(unmarshaller);
        
        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        result = (SensorML) absResult;
        expResult = (SensorML) absExpResult;


        systemSMLEquals(expResult, result);
        
    }

    /**
     * Tests the getMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataComponentSMLTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object absResult = reader.getMetadata("urn:ogc:object:sensor:GEOM:2", AbstractMetadataReader.SENSORML);

        AbstractSensorML absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component.xml"));
        
        pool.release(unmarshaller);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) absExpResult;
        
        componentEquals(expResult, result);
    }

    @Test
    public void getMetadataEbrimTest() throws Exception {

        Object absResult = reader.getMetadata("000068C3-3B49-C671-89CF-10A39BB1B652", AbstractMetadataReader.EBRIM);

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        ExtrinsicObjectType expResult =  (ExtrinsicObjectType) ((JAXBElement)unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim1.xml"))).getValue();

        assertTrue(absResult != null);
        assertTrue(absResult instanceof ExtrinsicObjectType);
        ExtrinsicObjectType result = (ExtrinsicObjectType) absResult;


        ebrimEquals(expResult, result);


        absResult = reader.getMetadata("urn:uuid:3e195454-42e8-11dd-8329-00e08157d076", AbstractMetadataReader.EBRIM);

        expResult =  (ExtrinsicObjectType) ((JAXBElement)unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim2.xml"))).getValue();

        pool.release(unmarshaller);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof ExtrinsicObjectType);
        result = (ExtrinsicObjectType) absResult;

        ebrimEquals(expResult, result);
    }

    @Test
    public void getMetadataEbrim30Test() throws Exception {

        Object absResult = reader.getMetadata("urn:motiive:csw-ebrim", AbstractMetadataReader.EBRIM);

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        RegistryPackageType expResult =  (RegistryPackageType) ((JAXBElement)unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim3.xml"))).getValue();

        assertTrue(absResult != null);
        assertTrue(absResult instanceof RegistryPackageType);
        RegistryPackageType result = (RegistryPackageType) absResult;

        pool.release(unmarshaller);

        ebrimEquals(expResult, result);
    }
}
