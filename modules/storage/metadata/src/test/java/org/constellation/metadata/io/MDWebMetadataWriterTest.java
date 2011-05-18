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


package org.constellation.metadata.io;


import org.geotoolkit.xml.IdentifiedObject;
import org.opengis.metadata.identification.DataIdentification;
import java.net.URI;
import org.geotoolkit.service.ServiceIdentificationImpl;
import org.geotoolkit.csw.xml.v202.RecordType;
import javax.xml.bind.JAXBElement;
import org.geotoolkit.ebrim.xml.v250.ExtrinsicObjectType;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.xml.AnchoredMarshallerPool;
import org.geotoolkit.feature.catalog.FeatureCatalogueImpl;
import java.io.StringReader;
import java.sql.Connection;
import javax.xml.bind.Unmarshaller;

import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.Util;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.ebrim.xml.v300.RegistryPackageType;
import static org.constellation.test.utils.MetadataUtilities.*;

import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.metadata.iso.identification.DefaultDataIdentification;
import org.geotoolkit.util.ComparisonMode;
import org.geotoolkit.util.Utilities;

import org.opengis.feature.catalog.FeatureCatalogue;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebMetadataWriterTest {


    private static Automatic configuration;

    private static MDWebMetadataReader reader;

    private static MDWebMetadataWriter writer;

    private static MarshallerPool pool;

    private static DefaultDataSource ds;

    @BeforeClass
    public static void setUpClass() throws Exception {
        
        Setup.initialize(null);
        pool = EBRIMMarshallerPool.getInstance();
        StaticMetadata.fillPoolAnchor((AnchoredMarshallerPool) pool);

        final String url = "jdbc:derby:memory:MMWTest;create=true";
        ds               = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/model/mdw_schema_2.1(derby).sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19115.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19119.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19108.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19115-2.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19110.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/data/defaultRecordSets.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/users/creation_user.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/profiles/inputLevels.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/catalog_web_service.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ebrimv2.5.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ebrimv3.0.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/SensorML_v2.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/contact.sql"));

        //we write the configuration file
        BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
        configuration = new Automatic("mdweb", bdd);
        
        reader = new MDWebMetadataReader(configuration);
        writer = new MDWebMetadataWriter(configuration);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (reader != null) {
            reader.destroy();
        }
        if (writer != null) {
            writer.destroy();
        }
        if (ds != null) {
            ds.shutdown();
        }
        Setup.shutdown();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Tests the storeMetadata method for SensorML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataComponentSMLTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        SensorML absExpResult = (SensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component2.xml"));

        writer.storeMetadata(absExpResult);

        Object absResult = reader.getMetadata("component2", AbstractMetadataReader.SENSORML);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult =  (SensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component2.xml"));

        pool.release(unmarshaller);

        componentEquals(expResult, result);
    }
    
    /**
     * Tests the storeMetadata method for SensorML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataSystemSMLTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        AbstractSensorML absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));

        writer.storeMetadata(absExpResult);

        AbstractSensorML absResult = (AbstractSensorML) reader.getMetadata("sensor-system", AbstractMetadataReader.SENSORML);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));

        systemSMLEquals(expResult, result);

        absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system2.xml"));

        writer.storeMetadata(absExpResult);

        absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system2.xml"));

        absResult = (AbstractSensorML) reader.getMetadata("sensor-system2", AbstractMetadataReader.SENSORML);

        pool.release(unmarshaller);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        result = (SensorML) absResult;
        expResult = (SensorML) absExpResult;

        systemSMLEquals(expResult, result);
        
    }
    
    /**
     * Tests the storeMetadata method for ISO 19139 data with GML geometries
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataISOXlinkTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        DefaultMetadata absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_20));
        writer.storeMetadata(absExpResult);
        Object absResult = reader.getMetadata("666-999-666", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) absResult;
        DefaultMetadata expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_20));

        DataIdentification expId = ((ServiceIdentificationImpl)expResult.getIdentificationInfo().iterator().next()).getOperatesOn().iterator().next();
        DataIdentification resId = ((ServiceIdentificationImpl)result.getIdentificationInfo().iterator().next()).getOperatesOn().iterator().next();
        assertEquals(new URI("http://test.com"), ((IdentifiedObject)expId).getXLink().getHRef());
        assertEquals(new URI("http://test.com"), ((IdentifiedObject)resId).getXLink().getHRef());
        metadataEquals(expResult,result);
        
        
        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_21));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("999-666-999", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_21));

        expId = ((ServiceIdentificationImpl)expResult.getIdentificationInfo().iterator().next()).getOperatesOn().iterator().next();
        resId = ((ServiceIdentificationImpl)result.getIdentificationInfo().iterator().next()).getOperatesOn().iterator().next();
        assertEquals(new URI("http://test2.com"), ((IdentifiedObject)expId).getXLink().getHRef());
        assertEquals(new URI("http://test2.com"), ((IdentifiedObject)resId).getXLink().getHRef());
        
        // TODO metadataEquals(expResult, result, ComparisonMode.BY_CONTRACT);
        
        pool.release(unmarshaller);
    }

    /**
     * Tests the storeMetadata method for ISO 19139 data with GML geometries
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataGMLTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        DefaultMetadata absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta10.xml"));
        writer.storeMetadata(absExpResult);
        Object absResult = reader.getMetadata("IGNF_PVA_1-0__1968__C0620-0111_CDP_5569_8959.xml", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) absResult;
        DefaultMetadata expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta10.xml"));

        metadataEquals(expResult,result);
        
        
        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_11));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("af24f70a-818c-4da1-9afb-1fc1e0058760", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_11));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_12));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("1c7d52ac-66c5-449b-a88b-8a0feeccb5fa", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_12));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_13));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("4c017cc5-3e0e-49d5-9f68-549943247e7e", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_13));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_14));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("4c017cc5-3e0e-49d5-9f68-549943247e89", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_14));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_15));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("484fc4d9-8d11-48a5-a386-65c19398f7c3", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_15));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_16));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("484fc4d9-8d11-48a5-a386-65c19398f7k7", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_16));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_17));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("81a25c84-2bb0-4727-8f36-4a296e1e7b57", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_17));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_18));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("28644bf0-5d9d-4ebd-bef0-f2b0b2067b26", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_18));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_19));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("937491cd-4bc4-43e4-9509-f6cc606f906e", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_19));

        metadataEquals(expResult,result);

        pool.release(unmarshaller);

    }

    /**
     * Tests the storeMetadata method for ISO 19110 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadata19110Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        FeatureCatalogue absExpResult = (FeatureCatalogue) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/featcatalog1.xml"));
        writer.storeMetadata(absExpResult);

        Object absResult = reader.getMetadata("cat-1", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof FeatureCatalogueImpl);
        FeatureCatalogueImpl result = (FeatureCatalogueImpl) absResult;
        FeatureCatalogueImpl expResult =  (FeatureCatalogueImpl) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/featcatalog1.xml"));

        pool.release(unmarshaller);
        catalogueEquals(expResult,result);
    }

    /**
     * Tests the storeMetadata method for Dublin Core data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataDCTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        RecordType absExpResult = (RecordType) ((JAXBElement)unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta8.xml"))).getValue();
        writer.storeMetadata(absExpResult);

        Object absResult = reader.getMetadata("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd", AbstractMetadataReader.DUBLINCORE);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof RecordType);
        RecordType result = (RecordType) absResult;
        RecordType expResult =  (RecordType) ((JAXBElement)unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta8.xml"))).getValue();

        pool.release(unmarshaller);
        assertEquals(expResult,result);
    }
    
    /**
     * Tests the storeMetadata method for ISO 19139 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataISO19115Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        
        DefaultMetadata absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta7.xml"));
        writer.storeMetadata(absExpResult);
        Object absResult = reader.getMetadata("MDWeb_FR_SY_couche_vecteur_258", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) absResult;
        DefaultMetadata expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta7.xml"));

        metadataEquals(expResult,result);
        
        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("42292_5p_19900609195600", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta9.xml"));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("identifier-test", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta9.xml"));

        metadataEquals(expResult,result);

        pool.release(unmarshaller);
    }

    /**
     * Tests the storeMetadata method for ISO 19115-2 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadata191152Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        DefaultMetadata absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/imageMetadata.xml"));
        writer.storeMetadata(absExpResult);

        Object absResult = reader.getMetadata("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) absResult;
        DefaultMetadata expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/imageMetadata.xml"));

        pool.release(unmarshaller);
        metadataEquals(expResult,result);
    }

    /**
     * Tests the storeMetadata method for ISO 19115-2 data
     * The purpose of  this test is to write a second time the same file.
     *
     * @todo the result is another metadata stored with title(2) in the Records table but not in the metadata.
     * @todo what about the identifier ?
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadata191152Again() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        DefaultMetadata absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/imageMetadata.xml"));

        String exceptionMsg = "";
        try {
            writer.storeMetadata(absExpResult);
        } catch (MetadataIoException ex) {
            exceptionMsg = ex.getMessage();
        }
        assertTrue(exceptionMsg.contains("is already used"));
        
        /*Object absResult = reader.getMetadata("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) absResult;

        // may be final String expTitle = "Sea surface temperature and history derived from an analysis of MODIS Level 3 data for the Gulf of Mexico(2)";
        final String expTitle = "Sea surface temperature and history derived from an analysis of MODIS Level 3 data for the Gulf of Mexico";
        final String title = Utils.findTitle(result);
        assertEquals(expTitle, title);*/
        pool.release(unmarshaller);
    }

    /**
     * Tests the storeMetadata method for ISO 19119 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadata19119Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        DefaultMetadata absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta-19119.xml"));
        writer.storeMetadata(absExpResult);

        Object absResult = reader.getMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4", AbstractMetadataReader.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) absResult;
        DefaultMetadata expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta-19119.xml"));

        pool.release(unmarshaller);
        metadataEquals(expResult, result);
    }

    @Test
    public void writeMetadataEbrimTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object absExpResult = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim1.xml"));
        writer.storeMetadata(absExpResult);

        Object absResult = reader.getMetadata("000068C3-3B49-C671-89CF-10A39BB1B652", AbstractMetadataReader.EBRIM);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof ExtrinsicObjectType);
        ExtrinsicObjectType result = (ExtrinsicObjectType) absResult;
        ExtrinsicObjectType expResult =  (ExtrinsicObjectType) ((JAXBElement)unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim1.xml"))).getValue();

        ebrimEquals(expResult, result);

        absExpResult = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim2.xml"));
        writer.storeMetadata(absExpResult);

        absResult = reader.getMetadata("urn:uuid:3e195454-42e8-11dd-8329-00e08157d076", AbstractMetadataReader.EBRIM);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof ExtrinsicObjectType);
        result = (ExtrinsicObjectType) absResult;
        expResult =  (ExtrinsicObjectType) ((JAXBElement)unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim2.xml"))).getValue();

        ebrimEquals(expResult, result);

        pool.release(unmarshaller);
    }

    @Test
    public void writeMetadataEbrim30Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object absExpResult = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim3.xml"));
        writer.storeMetadata(absExpResult);

        Object absResult = reader.getMetadata("urn:motiive:csw-ebrim", AbstractMetadataReader.EBRIM);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof RegistryPackageType);
        RegistryPackageType result = (RegistryPackageType) absResult;
        RegistryPackageType expResult =  (RegistryPackageType) ((JAXBElement)unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim3.xml"))).getValue();

        ebrimEquals(expResult, result);

        pool.release(unmarshaller);
    }

    @Test
    public void deleteMetadataTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();

        Object absResult = reader.getMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4", AbstractMetadataReader.ISO_19115);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) absResult;
        DefaultMetadata expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta-19119.xml"));
        pool.release(unmarshaller);
        metadataEquals(expResult, result);

        /*
         * we delete the metadata
         */
        writer.deleteMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");

        /*
         * Metadata is still in cache.
         */
        Object cacheResult = reader.getMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4", AbstractMetadataReader.ISO_19115);
        metadataEquals(result, (DefaultMetadata)cacheResult);

        /*
         * removing from cache
         */
        reader.removeFromCache("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");

        assertNull(reader.getMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4", AbstractMetadataReader.ISO_19115));
    }
}
