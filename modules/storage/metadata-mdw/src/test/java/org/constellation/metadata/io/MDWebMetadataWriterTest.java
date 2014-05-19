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


package org.constellation.metadata.io;


import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.TimeZone;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.sis.internal.jaxb.LegacyNamespaces;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.test.XMLComparator;
import org.apache.sis.xml.XML;

import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.jaxb.MarshallWarnings;
import org.constellation.util.Util;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.TestRunner;

import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.xml.AnchoredMarshallerPool;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;

import org.mdweb.model.storage.FullRecord;
import org.mdweb.model.storage.LinkedValue;
import org.mdweb.model.storage.Value;

import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
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
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19119.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19108.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115-2.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19110.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/data/defaultRecordSets.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/users/creation_user.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/profiles/inputLevels.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/profiles/user_profile.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/catalog_web_service.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ebrimv2.5.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ebrimv3.0.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/SensorML_v2.sql"));
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
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
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
    @Order(order=1)
    public void writeMetadataComponentSMLTest() throws Exception {

        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/sml/component2.xml"), MetadataType.SENSORML);

        writer.storeMetadata(absExpResult);

        Object absResult = reader.getMetadata("component2", MetadataType.SENSORML);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof Node);
        assertTrue(absExpResult instanceof Node);
        Node result = (Node) absResult;
        Node expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/sml/component2.xml"), MetadataType.SENSORML);

        compare(expResult, result);
    }

    /**
     * Tests the storeMetadata method for SensorML data
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order=2)
    public void writeMetadataSystemSMLTest() throws Exception {
        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"), MetadataType.SENSORML);

        writer.storeMetadata(absExpResult);

        Node result = reader.getMetadata("sensor-system", MetadataType.SENSORML);
        assertTrue(result != null);

        Node expResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"), MetadataType.SENSORML);

        compare(expResult, result);

        absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/sml/system2.xml"), MetadataType.SENSORML);

        writer.storeMetadata(absExpResult);

        absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/sml/system2.xml"), MetadataType.SENSORML);

        result = reader.getMetadata("sensor-system2", MetadataType.SENSORML);

        assertTrue(result != null);
        expResult = (Node) absExpResult;

        compare(expResult, result);
    }

    /**
     * Tests the storeMetadata method for ISO 19139 data with GML geometries
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order=3)
    public void writeMetadataISOXlinkTest() throws Exception {
        Node absExpResult = getNodeFromString(StaticMetadata.META_20, MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        Node result = reader.getMetadata("666-999-666", MetadataType.ISO_19115);
        assertTrue(result != null);
        Node expResult =  getNodeFromString(StaticMetadata.META_20, MetadataType.ISO_19115);

       /* DataIdentification expId = ((ServiceIdentificationImpl)expResult.getIdentificationInfo().iterator().next()).getOperatesOn().iterator().next();
        DataIdentification resId = ((ServiceIdentificationImpl)result.getIdentificationInfo().iterator().next()).getOperatesOn().iterator().next();
        assertEquals(new URI("http://test.com"), ((IdentifiedObject)expId).getIdentifierMap().getSpecialized(IdentifierSpace.XLINK).getHRef());
        assertEquals(new URI("http://test.com"), ((IdentifiedObject)resId).getIdentifierMap().getSpecialized(IdentifierSpace.XLINK).getHRef());*/
        compare(expResult,result);


        absExpResult = getNodeFromString(StaticMetadata.META_21, MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        result = reader.getMetadata("999-666-999", MetadataType.ISO_19115);
        assertTrue(result != null);
        expResult =  getNodeFromString(StaticMetadata.META_21, MetadataType.ISO_19115);

        /*expId = ((ServiceIdentificationImpl)expResult.getIdentificationInfo().iterator().next()).getOperatesOn().iterator().next();
        resId = ((ServiceIdentificationImpl)result.getIdentificationInfo().iterator().next()).getOperatesOn().iterator().next();
        assertEquals(new URI("http://test2.com"), ((IdentifiedObject)expId).getIdentifierMap().getSpecialized(IdentifierSpace.XLINK).getHRef());
        assertEquals(new URI("http://test2.com"), ((IdentifiedObject)resId).getIdentifierMap().getSpecialized(IdentifierSpace.XLINK).getHRef());*/

        compare(expResult, result);
    }

    /**
     * Tests the storeMetadata method for ISO 19139 data with GML geometries
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order=4)
    public void writeMetadataGMLTest() throws Exception {
       
        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta10.xml"), MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        Node result = reader.getMetadata("IGNF_PVA_1-0__1968__C0620-0111_CDP_5569_8959.xml", MetadataType.ISO_19115);
        assertTrue(result != null);
        Node expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta10.xml"), MetadataType.ISO_19115);

        compare(expResult,result);


        absExpResult = getNodeFromString(StaticMetadata.META_11, MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        result = reader.getMetadata("af24f70a-818c-4da1-9afb-1fc1e0058760", MetadataType.ISO_19115);
        assertTrue(result != null);
        expResult =  getNodeFromString(StaticMetadata.META_11, MetadataType.ISO_19115);

        compare(expResult,result);

        absExpResult = getNodeFromString(StaticMetadata.META_12, MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        result = reader.getMetadata("1c7d52ac-66c5-449b-a88b-8a0feeccb5fa", MetadataType.ISO_19115);
        assertTrue(result != null);
        expResult =  getNodeFromString(StaticMetadata.META_12, MetadataType.ISO_19115);

        compare(expResult,result);

        absExpResult = getNodeFromString(StaticMetadata.META_13, MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        result = reader.getMetadata("4c017cc5-3e0e-49d5-9f68-549943247e7e", MetadataType.ISO_19115);
        assertTrue(result != null);
        expResult =  getNodeFromString(StaticMetadata.META_13, MetadataType.ISO_19115);

        compare(expResult,result);

        absExpResult = getNodeFromString(StaticMetadata.META_14, MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        result = reader.getMetadata("4c017cc5-3e0e-49d5-9f68-549943247e89", MetadataType.ISO_19115);
        assertTrue(result != null);
        expResult =  getNodeFromString(StaticMetadata.META_14, MetadataType.ISO_19115);

        compare(expResult,result);

        absExpResult = getNodeFromString(StaticMetadata.META_15, MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        result = reader.getMetadata("484fc4d9-8d11-48a5-a386-65c19398f7c3", MetadataType.ISO_19115);
        assertTrue(result != null);
        expResult =  getNodeFromString(StaticMetadata.META_15, MetadataType.ISO_19115);

        compare(expResult,result);

        absExpResult = getNodeFromString(StaticMetadata.META_16, MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        result = reader.getMetadata("484fc4d9-8d11-48a5-a386-65c19398f7k7", MetadataType.ISO_19115);
        assertTrue(result != null);
        expResult =  getNodeFromString(StaticMetadata.META_16, MetadataType.ISO_19115);

        compare(expResult,result);

        absExpResult = getNodeFromString(StaticMetadata.META_17, MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        result = reader.getMetadata("81a25c84-2bb0-4727-8f36-4a296e1e7b57", MetadataType.ISO_19115);
        assertTrue(result != null);
        expResult =  getNodeFromString(StaticMetadata.META_17, MetadataType.ISO_19115);

        compare(expResult,result);

        absExpResult = getNodeFromString(StaticMetadata.META_18, MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        result = reader.getMetadata("28644bf0-5d9d-4ebd-bef0-f2b0b2067b26", MetadataType.ISO_19115);
        assertTrue(result != null);
        expResult =  getNodeFromString(StaticMetadata.META_18, MetadataType.ISO_19115);

        compare(expResult,result);

        absExpResult = getNodeFromString(StaticMetadata.META_19, MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        result = reader.getMetadata("937491cd-4bc4-43e4-9509-f6cc606f906e", MetadataType.ISO_19115);
        assertTrue(result != null);
        expResult =  getNodeFromString(StaticMetadata.META_19, MetadataType.ISO_19115);

        compare(expResult,result);
    }

    @Test
    @Order(order=5)
    public void writeMetadataMultiContactTest() throws Exception {

        final DefaultMetadata obj = (DefaultMetadata) getObjectFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta11.xml"), MetadataType.ISO_19115);
        assertEquals(3, obj.getContacts().size());

        FullRecord f = writer.getRecordFromObject(obj);
        int nbLinkedValue = 0;
        for (Value v : f.getValues()) {
            if (v instanceof LinkedValue) {
                nbLinkedValue++;
            }
        }
        assertEquals(2, nbLinkedValue);

        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta11.xml"), MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        Node result = reader.getMetadata("multi-contacts", MetadataType.ISO_19115);
        assertTrue(result != null);
        Node expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta11.xml"), MetadataType.ISO_19115);

        compare(expResult,result);
    }

    /**
     * Tests the storeMetadata method for ISO 19110 data
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order=5)
    public void writeMetadata19110Test() throws Exception {

        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/featcatalog1.xml"), MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);

        Node result = reader.getMetadata("cat-1", MetadataType.ISO_19115);
        assertTrue(result != null);
        
        Node expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/featcatalog1.xml"), MetadataType.ISO_19115);

        compare(expResult,result);
    }

    /**
     * Tests the storeMetadata method for Dublin Core data
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order=6)
    public void writeMetadataDCTest() throws Exception {
        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta8.xml"), MetadataType.DUBLINCORE);
        writer.storeMetadata(absExpResult);

        Node absResult = reader.getMetadata("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd", MetadataType.DUBLINCORE);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof Node);
        Node result = (Node) absResult;
        Node expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta8.xml"), MetadataType.DUBLINCORE);

        compare(expResult,result);
    }

    /**
     * Tests the storeMetadata method for ISO 19139 data
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order=7)
    public void writeMetadataISO19115Test() throws Exception {

        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta7.xml"), MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        Node result = reader.getMetadata("MDWeb_FR_SY_couche_vecteur_258", MetadataType.ISO_19115);
        assertTrue(result != null);
        Node expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta7.xml"), MetadataType.ISO_19115);

        compare(expResult,result);

        absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"), MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        result = reader.getMetadata("42292_5p_19900609195600", MetadataType.ISO_19115);
        assertTrue(result != null);
        expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"), MetadataType.ISO_19115);

        compare(expResult,result);

        absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta9.xml"), MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        result = reader.getMetadata("identifier-test", MetadataType.ISO_19115);
        assertTrue(result != null);
        expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta9.xml"), MetadataType.ISO_19115);

        compare(expResult,result);
    }

    /**
     * Tests the storeMetadata method for ISO 19115-2 data
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order=8)
    public void writeMetadata191152Test() throws Exception {

        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/imageMetadata.xml"), MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);

        Node result = reader.getMetadata("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX", MetadataType.ISO_19115);
        assertTrue(result != null);
        Node expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/imageMetadata.xml"), MetadataType.ISO_19115);

        compare(expResult,result);
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
    @Order(order=9)
    public void writeMetadata191152Again() throws Exception {
        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/imageMetadata.xml"), MetadataType.ISO_19115);

        String exceptionMsg = "";
        try {
            writer.storeMetadata(absExpResult);
        } catch (MetadataIoException ex) {
            exceptionMsg = ex.getMessage();
        }
        assertTrue(exceptionMsg.contains("is already used"));

        /*Object absResult = reader.getMetadata("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX", MetadataType.ISO_19115);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        Node result = (Node) absResult;

        // may be final String expTitle = "Sea surface temperature and history derived from an analysis of MODIS Level 3 data for the Gulf of Mexico(2)";
        final String expTitle = "Sea surface temperature and history derived from an analysis of MODIS Level 3 data for the Gulf of Mexico";
        final String title = Utils.findTitle(result);
        assertEquals(expTitle, title);*/
    }

    /**
     * Tests the storeMetadata method for ISO 19119 data
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order=10)
    public void writeMetadata19119Test() throws Exception {

        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta-19119.xml"), MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);

        Node result = reader.getMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4", MetadataType.ISO_19115);
        assertTrue(result != null);
        Node expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta-19119.xml"), MetadataType.ISO_19115);

        compare(expResult, result);
    }

    @Test
    @Order(order=11)
    public void writeMetadataEbrimTest() throws Exception {

        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim1.xml"), MetadataType.EBRIM);
        writer.storeMetadata(absExpResult);

        Node absResult = reader.getMetadata("000068C3-3B49-C671-89CF-10A39BB1B652", MetadataType.EBRIM);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof Node);
        Node result = (Node) absResult;
        Node expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim1.xml"), MetadataType.EBRIM);

        compare(expResult, result);

        absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim2.xml"), MetadataType.EBRIM);
        writer.storeMetadata(absExpResult);

        absResult = reader.getMetadata("urn:uuid:3e195454-42e8-11dd-8329-00e08157d076", MetadataType.EBRIM);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof Node);
        result = (Node) absResult;
        expResult =  (Node) getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim2.xml"), MetadataType.EBRIM);

        compare(expResult, result);

    }

    @Test
    @Order(order=12)
    public void writeMetadataEbrim30Test() throws Exception {

        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim3.xml"), MetadataType.EBRIM);
        writer.storeMetadata(absExpResult);

        Node absResult = reader.getMetadata("urn:motiive:csw-ebrim", MetadataType.EBRIM);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof Node);
        Node result = (Node) absResult;
        Node expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim3.xml"), MetadataType.EBRIM);

        compare(expResult, result);

    }

    @Test
    @Order(order=13)
    public void deleteMetadataTest() throws Exception {

        Node absResult = reader.getMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4", MetadataType.ISO_19115);

        assertTrue(absResult != null);
        Node result = (Node) absResult;
        Node expResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta-19119.xml"), MetadataType.ISO_19115);
        compare(expResult, result);

        /*
         * we delete the metadata
         */
        writer.deleteMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");

        /*
         * Metadata is still in cache.
         */
        Node cacheResult = reader.getMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4", MetadataType.ISO_19115);
        compare(result,  cacheResult);

        /*
         * removing from cache
         */
        reader.removeFromCache("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");

        assertNull(reader.getMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4", MetadataType.ISO_19115));
    }

    @Test
    @Order(order=14)
    public void writeMetadataIDTest() throws Exception {

        Node absExpResult = getNodeFromString(StaticMetadata.META_22, MetadataType.ISO_19115);
        writer.storeMetadata(absExpResult);
        Node result = reader.getMetadata("777-444-852", MetadataType.ISO_19115);
        assertTrue(result != null);
        Node expResult =  getNodeFromString(StaticMetadata.META_22, MetadataType.ISO_19115);

        compare(expResult,result);
    }

    private Object getObjectFromStream(final InputStream stream, final MetadataType mode) throws MetadataIoException {
        try {
            final boolean replace = mode == MetadataType.ISO_19115;
            Unmarshaller um = pool.acquireUnmarshaller();
            um.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, replace);
            final Object obj = um.unmarshal(stream);
            pool.recycle(um);
            return obj;
        } catch (JAXBException ex) {
            throw new MetadataIoException(ex);
        }
    }

    private Node getNodeFromStream(final InputStream stream, final MetadataType mode) throws MetadataIoException {
        try {
            final boolean replace = mode == MetadataType.ISO_19115;
            Unmarshaller um = pool.acquireUnmarshaller();
            um.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, replace);
            um.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
            final Object obj = um.unmarshal(stream);
            pool.recycle(um);
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.newDocument();
            Marshaller marshaller = EBRIMMarshallerPool.getInstance().acquireMarshaller();
            marshaller.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, replace);
            final MarshallWarnings warnings = new MarshallWarnings();
            marshaller.setProperty(XML.CONVERTER, warnings);
            marshaller.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
            marshaller.marshal(obj, document);
            pool.recycle(marshaller);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | JAXBException ex) {
            throw new MetadataIoException(ex);
        }
    }

    private Node getNodeFromString(final String s, final MetadataType mode) throws MetadataIoException {
       try {
           final boolean replace = mode == MetadataType.ISO_19115;
            Unmarshaller Unmarshaller = pool.acquireUnmarshaller();
            Unmarshaller.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, replace);
            Unmarshaller.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
            Marshaller marshaller = EBRIMMarshallerPool.getInstance().acquireMarshaller();
            marshaller.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, replace);
            marshaller.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));


            final Object obj = Unmarshaller.unmarshal(new StringReader(s));
            
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.newDocument();
            marshaller.marshal(obj, document);

            pool.recycle(Unmarshaller);
            pool.recycle(marshaller);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | JAXBException ex) {
            throw new MetadataIoException(ex);
        }
    }

    private void compare(final Node expResultNode, final Node resultNode) throws Exception {
        XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.ignoredAttributes.add("codeList");
        comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:xsi:schemaLocation");
        comparator.compare();
    }

    /**
     * used for debug
     * @param n
     * @return
     * @throws Exception
     */
    private static String getStringFromNode(final Node n) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(n), new StreamResult(writer));
        String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        return output;
    }
}
