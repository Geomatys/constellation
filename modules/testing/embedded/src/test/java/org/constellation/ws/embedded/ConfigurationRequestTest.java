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
package org.constellation.ws.embedded;

// JUnit dependencies

import org.apache.sis.xml.MarshallerPool;
import org.constellation.business.IDatasetBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.SpringHelper;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.ServiceReport;
import org.constellation.configuration.StringList;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.metadata.io.filesystem.sql.MetadataDatasource;
import org.constellation.metadata.io.filesystem.sql.Session;
import org.constellation.sos.ws.soap.SOService;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-context.xml")
@ActiveProfiles({"standard"})
public class ConfigurationRequestTest extends AbstractGrizzlyServer implements ApplicationContextAware {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("org.constellation.ws.embedded");

    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Inject
    private IServiceBusiness serviceBusiness;
    
    private static boolean initialized = false;
    
    private static File configDirectory;
    
    @BeforeClass
    public static void initTestDir() {
        configDirectory = ConfigDirectory.setupTestEnvironement("ConfigurationRequestTest");
    }
    
    @PostConstruct
    public void initPool() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                //clean services
                try {
                    serviceBusiness.deleteAll();
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }

                final File dataDirectory2 = new File(configDirectory, "dataCsw2");
                dataDirectory2.mkdir();

                writeDataFile(dataDirectory2, "urn-uuid-e8df05c2-d923-4a05-acce-2b20a27c0e58", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");

                final Automatic config2 = new Automatic("filesystem", dataDirectory2.getPath());
                config2.putParameter("shiroAccessible", "false");
                config2.putParameter("CSWCascading", "http://localhost:9090/csw/default");
                serviceBusiness.create("csw", "csw2", config2, null);


                final File dataDirectory = new File(configDirectory, "dataCsw");
                dataDirectory.mkdir();

                writeDataFile(dataDirectory, "urn-uuid-19887a8a-f6b0-4a63-ae56-7fba0e17801f", "urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
                writeDataFile(dataDirectory, "urn-uuid-1ef30a8b-876d-4828-9246-c37ab4510bbd", "urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");
                writeDataFile(dataDirectory, "urn-uuid-66ae76b7-54ba-489b-a582-0f0633d96493", "urn:uuid:66ae76b7-54ba-489b-a582-0f0633d96493");
                writeDataFile(dataDirectory, "urn-uuid-6a3de50b-fa66-4b58-a0e6-ca146fdd18d4", "urn:uuid:6a3de50b-fa66-4b58-a0e6-ca146fdd18d4");
                writeDataFile(dataDirectory, "urn-uuid-784e2afd-a9fd-44a6-9a92-a3848371c8ec", "urn:uuid:784e2afd-a9fd-44a6-9a92-a3848371c8ec");
                writeDataFile(dataDirectory, "urn-uuid-829babb0-b2f1-49e1-8cd5-7b489fe71a1e", "urn:uuid:829babb0-b2f1-49e1-8cd5-7b489fe71a1e");
                writeDataFile(dataDirectory, "urn-uuid-88247b56-4cbc-4df9-9860-db3f8042e357", "urn:uuid:88247b56-4cbc-4df9-9860-db3f8042e357");
                writeDataFile(dataDirectory, "urn-uuid-94bc9c83-97f6-4b40-9eb8-a8e8787a5c63", "urn:uuid:94bc9c83-97f6-4b40-9eb8-a8e8787a5c63");
                writeDataFile(dataDirectory, "urn-uuid-9a669547-b69b-469f-a11f-2d875366bbdc", "urn:uuid:9a669547-b69b-469f-a11f-2d875366bbdc");
                writeDataFile(dataDirectory, "urn-uuid-e9330592-0932-474b-be34-c3a3bb67c7db", "urn:uuid:e9330592-0932-474b-be34-c3a3bb67c7db");

                final File subDataDirectory = new File(dataDirectory, "sub1");
                subDataDirectory.mkdir();
                writeDataFile(subDataDirectory, "urn-uuid-ab42a8c4-95e8-4630-bf79-33e59241605a", "urn:uuid:ab42a8c4-95e8-4630-bf79-33e59241605a");

                final File subDataDirectory2 = new File(dataDirectory, "sub2");
                subDataDirectory2.mkdir();
                writeDataFile(subDataDirectory2, "urn-uuid-a06af396-3105-442d-8b40-22b57a90d2f2", "urn:uuid:a06af396-3105-442d-8b40-22b57a90d2f2");

                final Automatic config = new Automatic("filesystem", dataDirectory.getPath());
                config.putParameter("shiroAccessible", "false");
                serviceBusiness.create("csw", "default", config, null);

                final Map<String, Object> map = new HashMap<>();
                map.put("sos", new SOService());
                initServer(null, map);
                // Get the list of layers
                pool = new MarshallerPool(JAXBContext.newInstance("org.constellation.configuration:"
                        + "org.constellation.generic.database:"
                        + "org.geotoolkit.ows.xml.v110:"
                        + "org.geotoolkit.csw.xml.v202:"
                        + "org.apache.sis.internal.jaxb.geometry:"
                        + "org.geotoolkit.ows.xml.v100"), null);
                initialized = true;
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    @AfterClass
    public static void shutDown() {
        try {
            final IServiceBusiness service = SpringHelper.getBean(IServiceBusiness.class);
            service.deleteAll();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        ConfigDirectory.shutdownTestEnvironement("ConfigurationRequestTest");
        finish();
    }

    private static String getCswURL() {
        return "http://localhost:" +  grizzly.getCurrentPort() + "/csw/default?";
    }

    @Test
    @Order(order=1)
    public void testRestart() throws Exception {

        waitForStart();
        //update the federated catalog in case of busy port
        URL fedCatURL = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/CSW/csw2/federatedCatalog");
        URLConnection conec = fedCatURL.openConnection();

        postRequestObject(conec, new StringList(Arrays.asList(getCswURL())),  GenericDatabaseMarshallerPool.getInstance());
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);


        URL niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/OGC/csw/csw2/restart");

        // for a POST request
        conec = niUrl.openConnection();

        postRequestObject(conec, new SimpleValue(false), GenericDatabaseMarshallerPool.getInstance());
        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);
        AcknowlegementType expResult = new AcknowlegementType("Success",  "CSW service \"csw2\" successfully restarted.");
        assertEquals(expResult, obj);
    }

    @Test
    @Order(order=3)
    public void testCSWRefreshIndex() throws Exception {
        waitForStart();

        // first we make a getRecords request to count the number of record
        URL niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        GetRecordsResponseType response = (GetRecordsResponseType) obj;

        assertEquals(12, response.getSearchResults().getNumberOfRecordsMatched());

        // build 2 new metadata file
        RecordType record = new RecordType();
        record.setIdentifier(new SimpleLiteral("urn_test00"));
        File f = new File(configDirectory, "dataCsw/urn_test00.xml");
        RecordType record2 = new RecordType();
        record2.setIdentifier(new SimpleLiteral("urn_test01"));
        File f2 = new File(configDirectory, "dataCsw/urn_test01.xml");


        Marshaller m = pool.acquireMarshaller();
        m.marshal(record, f);
        m.marshal(record2, f2);
        pool.recycle(m);


        niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/CSW/default/index/refresh");

        // for a POST request
        conec = niUrl.openConnection();

        Map<String, String> params = new HashMap<>();
        params.put("ASYNCHRONE", "false");
        params.put("FORCED", "false");
        
        postRequestObject(conec, new ParameterValues(params), GenericDatabaseMarshallerPool.getInstance());
        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);
        AcknowlegementType expResult = new AcknowlegementType("Success",  "CSW index succefully recreated");
        assertEquals(expResult, obj);

        niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        response = (GetRecordsResponseType) obj;

        assertEquals(14, response.getSearchResults().getNumberOfRecordsMatched());
        
        // remove data
        f.delete();
        f2.delete();
        
        niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/CSW/default/index/refresh");

        conec = niUrl.openConnection();
        postRequestObject(conec, new ParameterValues(params), GenericDatabaseMarshallerPool.getInstance());

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);
        expResult = new AcknowlegementType("Success",  "CSW index succefully recreated");
        assertEquals(expResult, obj);

        niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        response = (GetRecordsResponseType) obj;

        assertEquals(12, response.getSearchResults().getNumberOfRecordsMatched());
    }

    @Test
    @Order(order=4)
    public void testCSWAddToIndex() throws Exception {
        waitForStart();
        
        // first we make a getRecords request to count the number of record
        URL niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        GetRecordsResponseType response = (GetRecordsResponseType) obj;

        assertEquals(12, response.getSearchResults().getNumberOfRecordsMatched());

        // build a new metadata file
        RecordType record = new RecordType();
        record.setIdentifier(new SimpleLiteral("urn_test"));
        File f = new File(configDirectory, "dataCsw/urn_test.xml");

        Session session = null;
        try {
            session = MetadataDatasource.createSession("default");
            session.putRecord("urn_test", f.getPath());

        } finally {
            if (session != null) {
                session.close();
            }
        }
        Marshaller m = pool.acquireMarshaller();
        m.marshal(record, f);
        pool.recycle(m);

        // add a metadata to the index
        niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/CSW/default/index/urn_test");

        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponsePut(conec);

        assertTrue(obj instanceof AcknowlegementType);
        AcknowlegementType expResult = new AcknowlegementType("Success",  "The specified record have been added to the CSW index");
        assertEquals(expResult, obj);


        //clear the csw cache
        niUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/1/CSW/default/clearCache");
        conec = niUrl.openConnection();
        obj = unmarshallResponse(conec);


         // verify that the number of record have increased
        niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        response = (GetRecordsResponseType) obj;

        assertEquals(13, response.getSearchResults().getNumberOfRecordsMatched());
        
        // restore previous context
        f.delete();
        niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/CSW/default/index/refresh");

        conec = niUrl.openConnection();
        
        // for a PUT request
        Map<String, String> params = new HashMap<>();
        params.put("ASYNCHRONE", "false");
        params.put("FORCED", "false");
        postRequestObject(conec, new ParameterValues(params), GenericDatabaseMarshallerPool.getInstance());
        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);
        expResult = new AcknowlegementType("Success",  "CSW index succefully recreated");
        assertEquals(expResult, obj);

        niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        response = (GetRecordsResponseType) obj;

        assertEquals(12, response.getSearchResults().getNumberOfRecordsMatched());
    }
    
    @Test
    @Order(order=5)
    public void testCSWRemoveFromIndex() throws Exception {
        waitForStart();
        
        // first we make a getRecords request to count the number of record
        URL niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        GetRecordsResponseType response = (GetRecordsResponseType) obj;

        assertEquals(12, response.getSearchResults().getNumberOfRecordsMatched());

        // remove metadata from the index
        niUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/1/CSW/default/index/urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");

        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponseDelete(conec);

        assertTrue(obj instanceof AcknowlegementType);
        AcknowlegementType expResult = new AcknowlegementType("Success",  "The specified record have been remove from the CSW index");
        assertEquals(expResult, obj);


        //clear the csw cache
        niUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/1/CSW/default/clearCache");
        conec = niUrl.openConnection();
        obj = unmarshallResponse(conec);


         // verify that the number of record have increased
        niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        response = (GetRecordsResponseType) obj;

        assertEquals(11, response.getSearchResults().getNumberOfRecordsMatched());
        
        // restore previous context
        niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/CSW/default/index/refresh");

        // for a PUT request
        conec = niUrl.openConnection();
        Map<String, String> params = new HashMap<>();
        params.put("ASYNCHRONE", "false");
        params.put("FORCED", "false");
        postRequestObject(conec, new ParameterValues(params), GenericDatabaseMarshallerPool.getInstance());
        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);
        expResult = new AcknowlegementType("Success",  "CSW index succefully recreated");
        assertEquals(expResult, obj);

        niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        response = (GetRecordsResponseType) obj;

        assertEquals(12, response.getSearchResults().getNumberOfRecordsMatched());
    }

    @Test
    @Order(order=6)
    public void testCSWRemoveAll() throws Exception {
        waitForStart();
        
        // first we make a getRecords request to count the number of record
        URL niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        GetRecordsResponseType response = (GetRecordsResponseType) obj;

        assertEquals(12, response.getSearchResults().getNumberOfRecordsMatched());

         // remove  all metadata from the index
        niUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/1/CSW/default/records");

        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponseDelete(conec);

        assertTrue(obj instanceof AcknowlegementType);
        AcknowlegementType expResult = new AcknowlegementType("Success",  "All records have been deleted from the CSW");
        assertEquals(expResult, obj);


        //clear the csw cache
        niUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/1/CSW/default/clearCache");
        conec = niUrl.openConnection();
        obj = unmarshallResponse(conec);


         // verify that the number of record have decreased
        niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        response = (GetRecordsResponseType) obj;

        assertEquals(0, response.getSearchResults().getNumberOfRecordsMatched());
    }

    @Test
    @Order(order=7)
    public void testListAvailableService() throws Exception {
        waitForStart();

        URL niUrl = new URL("http://localhost:" + grizzly.getCurrentPort() +  "/1/OGC/CSW/list");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ServiceReport);
        final ServiceReport result = (ServiceReport) obj;
        assertTrue(result.getAvailableServices().containsKey("sos"));
        assertTrue(result.getAvailableServices().containsKey("csw"));

        assertEquals(result.getAvailableServices().toString(), 3, result.getAvailableServices().size());


    }

    public static void writeDataFile(File dataDirectory, String resourceName, String identifier) throws IOException {

        final File dataFile;
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            final String windowsIdentifier = identifier.replace(':', '-');
            dataFile = new File(dataDirectory, windowsIdentifier + ".xml");
        } else {
            dataFile = new File(dataDirectory, identifier + ".xml");
        }
        FileWriter fw = new FileWriter(dataFile);
        InputStream in = Util.getResourceAsStream("org/constellation/embedded/test/" + resourceName + ".xml");

        byte[] buffer = new byte[1024];
        int size;

        while ((size = in.read(buffer, 0, 1024)) > 0) {
            fw.write(new String(buffer, 0, size));
        }
        in.close();
        fw.close();
    }
}
