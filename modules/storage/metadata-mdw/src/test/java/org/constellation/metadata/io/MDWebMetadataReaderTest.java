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

import org.apache.sis.internal.jaxb.LegacyNamespaces;
import org.apache.sis.test.XMLComparator;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.xml.XML;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.jaxb.MarshallWarnings;
import org.constellation.util.Util;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.xml.AnchoredMarshallerPool;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.TimeZone;

import static org.junit.Assert.*;

// Constellation dependencies
// Geotoolkit dependencies
// Junit dependencies

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
        sr.setEncoding("UTF-8");
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19119.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19108.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19110.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115-2.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/data/defaultRecordSets.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/users/creation_user.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/catalog_web_service.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ebrimv2.5.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ebrimv3.0.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-2.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-3.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-4.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-5.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-6.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-6.5.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-7.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-8.sql"));

        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/SensorML_v2.sql"));
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
     * Tests the getMetadata method for ISO 19139 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataDublinCoreTest() throws Exception {
        Node result = reader.getMetadata("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd", MetadataType.DUBLINCORE);

        Node expResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta8.xml"), MetadataType.DUBLINCORE);

        assertTrue(result instanceof Node);
        compare(expResult, result);
    }

    /**
     * Tests the getMetadata method for ISO 19139 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISO19115Test() throws Exception {

        Node result = reader.getMetadata("42292_5p_19900609195600", MetadataType.ISO_19115);

        Node expResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

        result = reader.getMetadata("identifier-test", MetadataType.ISO_19115);

        expResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta9.xml"), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult ,result);
    }

    /**
     * Tests the getMetadata method for ISO 19115 French profile data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISOFRATest() throws Exception {

        Node result = reader.getMetadata("MDweb-2064d5c7-80b5-4840-ba3d-4fa4e23be750", MetadataType.ISO_19115);

        Node expResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta-fra1.xml"), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

    }

    /**
     * Tests the getMetadata method for ISO 19139 data with GML geometries
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISOXlinkTest() throws Exception {
        Node result = reader.getMetadata("666-999-666", MetadataType.ISO_19115);

        Node expResult = getNodeFromString((StaticMetadata.META_20), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

        result = reader.getMetadata("999-666-999", MetadataType.ISO_19115);

        expResult = getNodeFromString((StaticMetadata.META_21), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

    }

     /**
     * Tests the getMetadata method for ISO 19139 data with GML geometries
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISOGMLTest() throws Exception {

        Node result = reader.getMetadata("IGNF_PVA_1-0__1968__C0620-0111_CDP_5569_8959.xml", MetadataType.ISO_19115);
        Node expResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta10.xml"), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

        /*
         * Point coordinates
         */
        result = reader.getMetadata("af24f70a-818c-4da1-9afb-1fc1e0058760", MetadataType.ISO_19115);

        expResult = getNodeFromString((StaticMetadata.META_11), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

        /*
         * Point pos
         */
        result = reader.getMetadata("1c7d52ac-66c5-449b-a88b-8a0feeccb5fa", MetadataType.ISO_19115);

        expResult = getNodeFromString((StaticMetadata.META_12), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

        /*
         * Curve posList
         */
        result = reader.getMetadata("4c017cc5-3e0e-49d5-9f68-549943247e7e", MetadataType.ISO_19115);

        expResult = getNodeFromString((StaticMetadata.META_13), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);


        /*
         * LineString coordinates
         */
        result = reader.getMetadata("4c017cc5-3e0e-49d5-9f68-549943247e89", MetadataType.ISO_19115);

        expResult = getNodeFromString((StaticMetadata.META_14), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

        /*
         * MultiLineString
         */
        result = reader.getMetadata("484fc4d9-8d11-48a5-a386-65c19398f7c3", MetadataType.ISO_19115);

        expResult = getNodeFromString((StaticMetadata.META_15), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

         /*
         * MultiPoint
         */
        result = reader.getMetadata("484fc4d9-8d11-48a5-a386-65c19398f7k7", MetadataType.ISO_19115);

        expResult = getNodeFromString((StaticMetadata.META_16), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);


        /*
         * Surface
         */
        result = reader.getMetadata("28644bf0-5d9d-4ebd-bef0-f2b0b2067b26", MetadataType.ISO_19115);

        expResult = getNodeFromString((StaticMetadata.META_18), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

        /*
         * Polygon
         */
        result = reader.getMetadata("937491cd-4bc4-43e4-9509-f6cc606f906e", MetadataType.ISO_19115);

        expResult = getNodeFromString((StaticMetadata.META_19), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

    }

      /**
     * Tests the getMetadata method for ISO 19119 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISOGeoRectifiedTest() throws Exception {

        Node result = reader.getMetadata("81a25c84-2bb0-4727-8f36-4a296e1e7b57", MetadataType.ISO_19115);

        Node expResult = getNodeFromString((StaticMetadata.META_17), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

    }

     /**
     * Tests the getMetadata method for ISO 19110 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISO19110Test() throws Exception {

        Node result = reader.getMetadata("cat-1", MetadataType.ISO_19115);

        Node expResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/featcatalog1.xml"), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);

    }

    /**
     * Tests the getMetadata method for ISO 19119 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISO19119Test() throws Exception {

        Node result = reader.getMetadata("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4", MetadataType.ISO_19115);

        Node expResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta-19119.xml"), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        compare(expResult, result);
    }

    /**
     * Tests the getMetadata method for ISO 19115-2 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISO191152Test() throws Exception {

        Node result = reader.getMetadata("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX", MetadataType.ISO_19115);

        Node expResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/imageMetadata.xml"), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        //pool.acquireMarshaller().marshal(expResult, System.out);
        compare(expResult, result);
    }

    /**
     * Tests the getMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataSystemSMLTest() throws Exception {

        Node absResult = reader.getMetadata("urn:ogc:object:sensor:GEOM:1", MetadataType.SENSORML);
        Node absExpResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"), MetadataType.SENSORML);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof Node);
        assertTrue(absExpResult instanceof Node);

        compare(absExpResult, absResult);

        absResult = reader.getMetadata("urn:ogc:object:sensor:GEOM:3", MetadataType.SENSORML);
        absExpResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/sml/system2.xml"), MetadataType.SENSORML);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof Node);
        assertTrue(absExpResult instanceof Node);

        compare(absExpResult, absResult);

    }

    /**
     * Tests the getMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataComponentSMLTest() throws Exception {

        Node absResult = reader.getMetadata("urn:ogc:object:sensor:GEOM:2", MetadataType.SENSORML);

        Node absExpResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/sml/component.xml"), MetadataType.SENSORML);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof Node);
        assertTrue(absExpResult instanceof Node);
        
        compare(absExpResult, absResult);
    }

    @Test
    public void getMetadataEbrimTest() throws Exception {

        Node absResult = reader.getMetadata("000068C3-3B49-C671-89CF-10A39BB1B652", MetadataType.EBRIM);

        Node expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim1.xml"), MetadataType.EBRIM);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof Node);

        compare(expResult, absResult);

        absResult = reader.getMetadata("urn:uuid:3e195454-42e8-11dd-8329-00e08157d076", MetadataType.EBRIM);
        expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim2.xml"), MetadataType.EBRIM);


        assertTrue(absResult != null);
        assertTrue(absResult instanceof Node);

         compare(expResult, absResult);
    }

    @Test
    public void getMetadataEbrim30Test() throws Exception {

        Node absResult = reader.getMetadata("urn:motiive:csw-ebrim", MetadataType.EBRIM);

        Node expResult =  getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim3.xml"), MetadataType.EBRIM);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof Node);
        
        compare(expResult, absResult);
    }
    
    /**
     * Tests the getMetadata method for ISO 19115 data with id on responsible party
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataResponsibleIDTest() throws Exception {

        Node result = reader.getMetadata("meta-id", MetadataType.ISO_19115);
        Node expResult = getNodeFromStream(Util.getResourceAsStream("org/constellation/xml/metadata/meta12.xml"), MetadataType.ISO_19115);

        assertTrue(result instanceof Node);
        
        compare(expResult, result);
    }

    private Node getNodeFromStream(final InputStream stream, final MetadataType mode) throws MetadataIoException {
        try {
            final boolean replace = mode == MetadataType.ISO_19115;
            Unmarshaller um = pool.acquireUnmarshaller();
            um.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
            um.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, replace);
            final Object obj = um.unmarshal(stream);
            pool.recycle(um);

            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.newDocument();
            Marshaller marshaller = pool.acquireMarshaller();
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
            Unmarshaller um = pool.acquireUnmarshaller();
            um.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, replace);
            um.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
            final Object obj = um.unmarshal(new StringReader(s));
            pool.recycle(um);
            
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.newDocument();
            Marshaller marshaller = pool.acquireMarshaller();
            marshaller.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, replace);
            marshaller.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
            final MarshallWarnings warnings = new MarshallWarnings();
            marshaller.setProperty(XML.CONVERTER, warnings);
            marshaller.marshal(obj, document);
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
