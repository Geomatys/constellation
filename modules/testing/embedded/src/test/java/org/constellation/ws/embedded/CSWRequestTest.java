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

import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.SpringHelper;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.StringList;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.sos.ws.soap.SOService;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.ResultType;
import org.geotoolkit.csw.xml.TypeNames;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.csw.xml.v202.DescribeRecordResponseType;
import org.geotoolkit.csw.xml.v202.DistributedSearchType;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ElementSetNameType;
import org.geotoolkit.csw.xml.v202.GetCapabilitiesType;
import org.geotoolkit.csw.xml.v202.GetDomainResponseType;
import org.geotoolkit.csw.xml.v202.GetDomainType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdType;
import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordsType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.csw.xml.v202.ObjectFactory;
import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.geotoolkit.csw.xml.v202.QueryType;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.ows.xml.v100.ExceptionReport;
import org.geotoolkit.ows.xml.v100.Operation;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.springframework.test.context.ActiveProfiles;

// JUnit dependencies

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
@ActiveProfiles({"standard","derby"})
public class CSWRequestTest extends AbstractGrizzlyServer implements ApplicationContextAware {

    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Inject
    private IServiceBusiness serviceBusiness;
    
    private static boolean initialized = false;
    
    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @PostConstruct
    public void initPool() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                
                try {
                    serviceBusiness.delete("csw", "default");
                    serviceBusiness.delete("csw", "csw2");
                } catch (ConfigurationException ex) {}
                
                final File configDirectory = ConfigDirectory.setupTestEnvironement("CSWRequestTest");

                final File dataDirectory2 = new File(configDirectory, "dataCsw2");
                dataDirectory2.mkdir();

                writeDataFile(dataDirectory2, "urn-uuid-e8df05c2-d923-4a05-acce-2b20a27c0e58");

                final Automatic config2 = new Automatic("filesystem", dataDirectory2.getPath());
                config2.putParameter("shiroAccessible", "false");
                config2.putParameter("CSWCascading", "http://localhost:9090/csw/default");
                serviceBusiness.create("csw", "csw2", config2, null, null);


                final File dataDirectory = new File(configDirectory, "dataCsw");
                dataDirectory.mkdir();

                writeDataFile(dataDirectory, "urn-uuid-19887a8a-f6b0-4a63-ae56-7fba0e17801f");
                writeDataFile(dataDirectory, "urn-uuid-1ef30a8b-876d-4828-9246-c37ab4510bbd");
                writeDataFile(dataDirectory, "urn-uuid-66ae76b7-54ba-489b-a582-0f0633d96493");
                writeDataFile(dataDirectory, "urn-uuid-6a3de50b-fa66-4b58-a0e6-ca146fdd18d4");
                writeDataFile(dataDirectory, "urn-uuid-784e2afd-a9fd-44a6-9a92-a3848371c8ec");
                writeDataFile(dataDirectory, "urn-uuid-829babb0-b2f1-49e1-8cd5-7b489fe71a1e");
                writeDataFile(dataDirectory, "urn-uuid-88247b56-4cbc-4df9-9860-db3f8042e357");
                writeDataFile(dataDirectory, "urn-uuid-94bc9c83-97f6-4b40-9eb8-a8e8787a5c63");
                writeDataFile(dataDirectory, "urn-uuid-9a669547-b69b-469f-a11f-2d875366bbdc");
                writeDataFile(dataDirectory, "urn-uuid-e9330592-0932-474b-be34-c3a3bb67c7db");

                final File subDataDirectory = new File(dataDirectory, "sub1");
                subDataDirectory.mkdir();
                writeDataFile(subDataDirectory, "urn-uuid-ab42a8c4-95e8-4630-bf79-33e59241605a");

                final File subDataDirectory2 = new File(dataDirectory, "sub2");
                subDataDirectory2.mkdir();
                writeDataFile(subDataDirectory2, "urn-uuid-a06af396-3105-442d-8b40-22b57a90d2f2");

                final Automatic config = new Automatic("filesystem", dataDirectory.getPath());
                config.putParameter("shiroAccessible", "false");
                serviceBusiness.create("csw", "default", config, null, null);

                final Map<String, Object> map = new HashMap<>();
                map.put("sos", new SOService());
                initServer(null, map);
                // Get the list of layers
                pool = EBRIMMarshallerPool.getInstance();
                initialized = true;
            } catch (Exception ex) {
                Logger.getLogger(CSWRequestTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void shutDown() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        ConfigDirectory.shutdownTestEnvironement("CSWRequestTest");
        finish();
    }

    private static String getDefaultURL() {
        return "http://localhost:" +  grizzly.getCurrentPort() + "/csw/default?";
    }

    private static String getCsw2URL() {
        return "http://localhost:" +  grizzly.getCurrentPort() + "/csw/csw2?";
    }

    @Test
    @Order(order=1)
    public void testCSWGetCapabilities() throws Exception {

        waitForStart();

        //update the federated catalog in case of busy port
        URL fedCatURL = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/CSW/csw2/federatedCatalog");
        URLConnection conec = fedCatURL.openConnection();

        postRequestObject(conec, new StringList(Arrays.asList(getDefaultURL())),  GenericDatabaseMarshallerPool.getInstance());

        // Creates a valid GetCapabilities url.
        URL getCapsUrl = new URL(getDefaultURL());

        // for a POST request
        conec = getCapsUrl.openConnection();

        final GetCapabilitiesType request = new GetCapabilitiesType("CSW");

        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof Capabilities);

        Capabilities c = (Capabilities) obj;

        assertTrue(c.getOperationsMetadata() != null);

        Operation op = c.getOperationsMetadata().getOperation("GetRecords");

        assertTrue(op != null);
        assertTrue(op.getDCP().size() > 0);

        assertEquals(op.getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref(), getDefaultURL());

        // Creates a valid GetCapabilties url.
        getCapsUrl = new URL(getDefaultURL() + "request=GetCapabilities&service=CSW&version=2.0.2");


        // Try to marshall something from the response returned by the server.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof Capabilities);

        Capabilities capa = (Capabilities) obj;

        String currentURL = capa.getOperationsMetadata().getOperation("getRecords").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();

        assertEquals(getDefaultURL(), currentURL);


         // Creates a valid GetCapabilties url.
        getCapsUrl = new URL(getCsw2URL() + "request=GetCapabilities&service=CSW&version=2.0.2");

        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof Capabilities);

        capa = (Capabilities) obj;

        currentURL = capa.getOperationsMetadata().getOperation("getRecords").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();

        assertEquals(getCsw2URL(), currentURL);


         // Creates a valid GetCapabilties url.
        getCapsUrl = new URL(getDefaultURL() + "request=GetCapabilities&service=CSW&version=2.0.2");

        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof Capabilities);

        capa = (Capabilities) obj;

        currentURL = capa.getOperationsMetadata().getOperation("getRecords").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();

        assertEquals(getDefaultURL(), currentURL);
    }

    @Test
    @Order(order=2)
    public void testCSWError() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(getDefaultURL());


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        final GetCapabilitiesType request = new GetCapabilitiesType("SOS");

        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ExceptionReport);

        ExceptionReport result = (ExceptionReport) obj;

        assertEquals("InvalidParameterValue", result.getException().get(0).getExceptionCode());

    }

    @Test
    @Order(order=3)
    public void testCSWGetDomain() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(getDefaultURL());


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        GetDomainType request = new GetDomainType("CSW", "2.0.2", null, "GetCapabilities.sections");

        postRequestObject(conec, request);
        Object result = unmarshallResponse(conec);

        assertTrue(result instanceof GetDomainResponseType);

        List<DomainValues> values = new ArrayList<>();
        ListOfValuesType list = new ListOfValuesType(Arrays.asList("All", "ServiceIdentification", "ServiceProvider", "OperationsMetadata","Filter_Capabilities"));
        values.add(new DomainValuesType("GetCapabilities.sections", null, list, new QName("http://www.opengis.net/cat/csw/2.0.2", "Capabilities")));
        GetDomainResponseType expResult = new GetDomainResponseType(values);

        assertEquals(expResult, result);

        request = new GetDomainType("CSW", "2.0.2", "title", null);

        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);
        result = unmarshallResponse(conec);

        assertTrue(result instanceof GetDomainResponseType);

        values = new ArrayList<>();
        list = new ListOfValuesType(Arrays.asList("Aliquam fermentum purus quis arcu","Fuscé vitae ligulä","Lorem ipsum","Lorem ipsum dolor sit amet",
                "Maecenas enim","Mauris sed neque","Ut facilisis justo ut lacus","Vestibulum massa purus","Ñunç elementum"));
        values.add(new DomainValuesType(null, "title", list, new QName("http://www.isotc211.org/2005/gmd", "MD_Metadata")));
        expResult = new GetDomainResponseType(values);

        assertEquals(expResult, result);
    }

    @Test
    @Order(order=4)
    public void testCSWGetRecordByID() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(getDefaultURL());


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        GetRecordByIdType request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "text/xml", null, Arrays.asList("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f"));

        final ObjectFactory factory = new ObjectFactory();
        postRequestObject(conec, factory.createGetRecordById(request));
        Object result = unmarshallResponse(conec);

        assertTrue(result instanceof GetRecordByIdResponseType);

        GetRecordByIdResponseType grResult = (GetRecordByIdResponseType) result;
        assertEquals(1, grResult.getAny().size());


        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "text/xml", null, Arrays.asList("urn:uuid:ab42a8c4-95e8-4630-bf79-33e59241605a"));

        conec = getCapsUrl.openConnection();

        postRequestObject(conec, factory.createGetRecordById(request));
        result = unmarshallResponse(conec);

        assertTrue(result instanceof GetRecordByIdResponseType);

        grResult = (GetRecordByIdResponseType) result;
        assertEquals(1, grResult.getAny().size());

    }

    @Test
    @Order(order=5)
    public void testCSWGetRecords() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(getDefaultURL());


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        QueryConstraintType constraint = new QueryConstraintType("identifier='urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f'", "1.1.0");
        QueryType query = new QueryType(Arrays.asList(TypeNames.RECORD_QNAME), new ElementSetNameType(ElementSetType.FULL), null, constraint);
        GetRecordsType request = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, null, null, 1, 10, query, null);

        postRequestObject(conec, request);
        Object result = unmarshallResponse(conec);

        assertTrue(result instanceof GetRecordsResponseType);

        GetRecordsResponseType grResult = (GetRecordsResponseType) result;
        assertEquals(1, grResult.getSearchResults().getAny().size());

        /**
         * get all the records
         */
        conec = getCapsUrl.openConnection();

        constraint = new QueryConstraintType("identifier like '%%'", "1.1.0");
        query = new QueryType(Arrays.asList(TypeNames.RECORD_QNAME), new ElementSetNameType(ElementSetType.FULL), null, constraint);
        request = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, null, null, 1, 20, query, null);

        postRequestObject(conec, request);
        result = unmarshallResponse(conec);

        assertTrue(result instanceof GetRecordsResponseType);

        grResult = (GetRecordsResponseType) result;

        assertEquals(12, grResult.getSearchResults().getAny().size());

    }

    @Test
    @Order(order=6)
    public void testDistributedCSWGetRecords() throws Exception {

        System.out.println("\n\n DISTIBUTED SEARCH \n\n");
        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(getCsw2URL());


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        QueryConstraintType constraint = new QueryConstraintType("identifier like '%%'", "1.1.0");
        QueryType query = new QueryType(Arrays.asList(TypeNames.RECORD_QNAME), new ElementSetNameType(ElementSetType.FULL), null, constraint);
        DistributedSearchType dist = new DistributedSearchType(1);
        GetRecordsType request = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, null, null, 1, 20, query, dist);

        postRequestObject(conec, request);
        Object result = unmarshallResponse(conec);

        assertTrue(result instanceof GetRecordsResponseType);

        GetRecordsResponseType grResult = (GetRecordsResponseType) result;
        assertEquals(13, grResult.getSearchResults().getAny().size());



        // no distribution
        conec = getCapsUrl.openConnection();

        constraint = new QueryConstraintType("identifier like '%%'", "1.1.0");
        query = new QueryType(Arrays.asList(TypeNames.RECORD_QNAME), new ElementSetNameType(ElementSetType.FULL), null, constraint);
        request = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, null, null, 1, 20, query, null);

        postRequestObject(conec, request);
        result = unmarshallResponse(conec);

        assertTrue(result instanceof GetRecordsResponseType);

        grResult = (GetRecordsResponseType) result;

        assertEquals(1, grResult.getSearchResults().getAny().size());

        // distribution with 0 hopcount
        conec = getCapsUrl.openConnection();

        constraint = new QueryConstraintType("identifier like '%%'", "1.1.0");
        query = new QueryType(Arrays.asList(TypeNames.RECORD_QNAME), new ElementSetNameType(ElementSetType.FULL), null, constraint);
        dist = new DistributedSearchType(0);
        request = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, null, null, 1, 20, query, dist);

        postRequestObject(conec, request);
        result = unmarshallResponse(conec);

        assertTrue(result instanceof GetRecordsResponseType);

        grResult = (GetRecordsResponseType) result;

        assertEquals(1, grResult.getSearchResults().getAny().size());
     }


    @Test
    @Order(order=7)
    public void testDescribeRecords() throws Exception {


        /**
         * Dublin core
         */
        URL getCapsUrl = new URL(getDefaultURL() + "service=CSW&request=DescribeRecord&version=2.0.2&typename=csw:Record");

        // Try to marshall something from the response returned by the server.
        Object obj = unmarshallResponse(getCapsUrl);
        assertTrue("was:" + obj.getClass(), obj instanceof DescribeRecordResponseType);
        DescribeRecordResponseType result = (DescribeRecordResponseType) obj;
        assertEquals(result.getSchemaComponent().size(), 1);
        assertEquals(result.getSchemaComponent().get(0).getTargetNamespace(), "http://www.opengis.net/cat/csw/2.0.2");


        getCapsUrl = new URL(getDefaultURL() + "service=CSW&request=DescribeRecord&version=2.0.2&typename=csw:Record&namespace=xmlns(csw=http://www.opengis.net/cat/csw/2.0.2)");

        // Try to marshall something from the response returned by the server.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue("was:" + obj.getClass(), obj instanceof DescribeRecordResponseType);
        result = (DescribeRecordResponseType) obj;
        assertEquals(result.getSchemaComponent().size(), 1);
        assertEquals(result.getSchemaComponent().get(0).getTargetNamespace(), "http://www.opengis.net/cat/csw/2.0.2");

        getCapsUrl = new URL(getDefaultURL() + "service=CSW&request=DescribeRecord&version=2.0.2&typename=csw:Record&namespace=xmlns(csw=http://www.opengis.net/cat/csw/3.8)");

        // Try to marshall something from the response returned by the server.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue("was:" + obj.getClass(), obj instanceof DescribeRecordResponseType);
        result = (DescribeRecordResponseType) obj;
        assertEquals(result.getSchemaComponent().size(), 0);


        /**
         * GMD
         */
        getCapsUrl = new URL(getDefaultURL() + "service=CSW&request=DescribeRecord&version=2.0.2&typename=gmd:MD_Metadata");

        // Try to marshall something from the response returned by the server.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue("was:" + obj.getClass(), obj instanceof DescribeRecordResponseType);
        result = (DescribeRecordResponseType) obj;
        assertEquals(result.getSchemaComponent().size(), 1);
        assertEquals(result.getSchemaComponent().get(0).getTargetNamespace(), "http://www.isotc211.org/2005/gmd");

        getCapsUrl = new URL(getDefaultURL() + "service=CSW&request=DescribeRecord&version=2.0.2&typename=gmd:MD_Metadata&namespace=xmlns(gmd=http://www.isotc211.org/2005/gmd)");

        // Try to marshall something from the response returned by the server.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue("was:" + obj.getClass(), obj instanceof DescribeRecordResponseType);
        result = (DescribeRecordResponseType) obj;
        assertEquals(result.getSchemaComponent().size(), 1);
        assertEquals(result.getSchemaComponent().get(0).getTargetNamespace(), "http://www.isotc211.org/2005/gmd");

        getCapsUrl = new URL(getDefaultURL() + "service=CSW&request=DescribeRecord&version=2.0.2&typename=gmd:MD_Metadata&namespace=xmlns(gmd=http://www.isotc211.org/2005/wrong)");

        // Try to marshall something from the response returned by the server.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue("was:" + obj.getClass(), obj instanceof DescribeRecordResponseType);
        result = (DescribeRecordResponseType) obj;
        assertEquals(result.getSchemaComponent().size(), 0);


        /**
         * ALL
         */
        getCapsUrl = new URL(getDefaultURL() + "service=CSW&request=DescribeRecord&version=2.0.2");

        // Try to marshall something from the response returned by the server.
        obj = unmarshallResponse(getCapsUrl);
        result = (DescribeRecordResponseType) obj;
        assertEquals(result.getSchemaComponent().size(), 4);
        assertEquals(result.getSchemaComponent().get(0).getTargetNamespace(), "http://www.opengis.net/cat/csw/2.0.2");
        assertEquals(result.getSchemaComponent().get(1).getTargetNamespace(), "http://www.isotc211.org/2005/gmd");
        assertEquals(result.getSchemaComponent().get(2).getTargetNamespace(), "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
        assertEquals(result.getSchemaComponent().get(3).getTargetNamespace(), "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5");

    }

    public static void writeDataFile(File dataDirectory, String resourceName) throws IOException {

        final File dataFile;
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            final String windowsIdentifier = resourceName.replace(':', '-');
            dataFile = new File(dataDirectory, windowsIdentifier + ".xml");
        } else {
            dataFile = new File(dataDirectory, resourceName + ".xml");
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
