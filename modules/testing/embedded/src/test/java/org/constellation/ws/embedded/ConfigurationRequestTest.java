/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.ws.embedded;

// JUnit dependencies
import org.constellation.configuration.ExceptionReport;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.util.StringUtilities;
import org.apache.sis.xml.MarshallerPool;
import java.net.URLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ServiceReport;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.ws.soap.SOService;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.TestRunner;
import org.constellation.util.Util;
import org.constellation.ws.ExceptionCode;
import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class ConfigurationRequestTest extends AbstractGrizzlyServer {

    private static File configDirectory;

    @BeforeClass
    public static void initPool() throws Exception {
        configDirectory = ConfigurationEngine.setupTestEnvironement("ConfigurationRequestTest");

        final File dataDirectory2 = new File(configDirectory, "dataCsw2");
        dataDirectory2.mkdir();

        writeDataFile(dataDirectory2, "urn-uuid-e8df05c2-d923-4a05-acce-2b20a27c0e58", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");

        final Automatic config2 = new Automatic("filesystem", dataDirectory2.getPath());
        config2.putParameter("shiroAccessible", "false");
        config2.putParameter("CSWCascading", "http://localhost:9090/csw/default");
        ConfigurationEngine.storeConfiguration("CSW", "csw2", config2);


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
        ConfigurationEngine.storeConfiguration("CSW", "default", config);
        
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
    }

    @AfterClass
    public static void shutDown() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        ConfigurationEngine.shutdownTestEnvironement("ConfigurationRequestTest");
        finish();
    }

    private static String getConfigurationURL() {
        return "http://localhost:" +  grizzly.getCurrentPort() + "/configuration?";
    }

    private static String getCswURL() {
        return "http://localhost:" +  grizzly.getCurrentPort() + "/csw/default?";
    }

    @Test
    @Order(order=1)
    public void testRestart() throws Exception {

        waitForStart();
        //update the federated catalog in case of busy port
        URL fedCatURL = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/csw/admin?request=setFederatedCatalog&id=csw2&servers=" + getCswURL());
        URLConnection conec = fedCatURL.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);


        URL niUrl = new URL(getConfigurationURL() + "request=restart");

        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);
        AcknowlegementType expResult = new AcknowlegementType("Success",  "services successfully restarted");
        assertEquals(expResult, obj);
    }

    @Test
    @Order(order=2)
    public void testDownloadFile() throws Exception {

        URL niUrl = new URL(getConfigurationURL() + "request=download");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ExceptionReport);
        ExceptionReport expResult = new ExceptionReport("Download operation not implemented",
                                                         StringUtilities.transformCodeName(ExceptionCode.OPERATION_NOT_SUPPORTED.name()));
        assertEquals(expResult, obj);
    }

    @Test
    @Order(order=3)
    public void testCSWRefreshIndex() throws Exception {

        /*
         * try to get a missing parameter error
         */
        URL niUrl = new URL(getConfigurationURL() + "request=refreshIndex");

        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ExceptionReport);
        ExceptionReport exception = new ExceptionReport("The parameter ID must be specified",
                                                         StringUtilities.transformCodeName(ExceptionCode.MISSING_PARAMETER_VALUE.name()));
        assertEquals(exception, obj);


        // first we make a getRecords request to count the number of record
        niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

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


        niUrl = new URL(getConfigurationURL() + "request=refreshIndex&id=default");

        // for a POST request
        conec = niUrl.openConnection();

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
        
        niUrl = new URL(getConfigurationURL() + "request=refreshIndex&id=default");

        // for a POST request
        conec = niUrl.openConnection();

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

        Marshaller m = pool.acquireMarshaller();
        m.marshal(record, f);
        pool.recycle(m);

        // add a metadata to the index
        niUrl = new URL(getConfigurationURL() + "request=addToIndex&id=default&identifiers=urn_test");

        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);
        AcknowlegementType expResult = new AcknowlegementType("Success",  "The specified record have been added to the CSW index");
        assertEquals(expResult, obj);


        //clear the csw cache
        niUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/csw/admin?request=clearCache&id=default");
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
        niUrl = new URL(getConfigurationURL() + "request=refreshIndex&id=default");

        // for a POST request
        conec = niUrl.openConnection();

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

        // first we make a getRecords request to count the number of record
        URL niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        GetRecordsResponseType response = (GetRecordsResponseType) obj;

        assertEquals(12, response.getSearchResults().getNumberOfRecordsMatched());

        // remove metadata from the index
        niUrl = new URL(getConfigurationURL() + "request=removeFromIndex&id=default&identifiers=urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");

        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);
        AcknowlegementType expResult = new AcknowlegementType("Success",  "The specified record have been remove from the CSW index");
        assertEquals(expResult, obj);


        //clear the csw cache
        niUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/csw/admin?request=clearCache&id=default");
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
        niUrl = new URL(getConfigurationURL() + "request=refreshIndex&id=default");

        // for a POST request
        conec = niUrl.openConnection();

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

        // first we make a getRecords request to count the number of record
        URL niUrl = new URL(getCswURL() + "request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");

        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof GetRecordsResponseType);
        GetRecordsResponseType response = (GetRecordsResponseType) obj;

        assertEquals(12, response.getSearchResults().getNumberOfRecordsMatched());

         // remove  all metadata from the index
        niUrl = new URL(getConfigurationURL() + "request=deleteAllRecords&id=default");

        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);
        AcknowlegementType expResult = new AcknowlegementType("Success",  "All records have been deleted from the CSW");
        assertEquals(expResult, obj);


        //clear the csw cache
        niUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/csw/admin?request=clearCache&id=default");
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
        URL niUrl = new URL(getConfigurationURL() + "request=ListAvailableService");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ServiceReport);
        final ServiceReport result = (ServiceReport) obj;
        assertTrue(result.getAvailableServices().containsKey("SOS"));
        assertTrue(result.getAvailableServices().containsKey("CSW"));

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
