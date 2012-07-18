/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

import org.geotoolkit.ows.xml.v100.Operation;
import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.csw.xml.TypeNames;
import org.geotoolkit.csw.xml.ResultType;
import org.geotoolkit.csw.xml.DomainValues;
import java.util.Arrays;
import java.util.List;
import org.geotoolkit.csw.xml.v202.GetDomainType;
import java.net.URLConnection;
import java.net.URL;
import java.io.File;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.constellation.configuration.AcknowlegementType;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.v202.*;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.ows.xml.v100.ExceptionReport;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CSWRequestTest extends AbstractTestRequest {

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initPool() throws JAXBException {
        // Get the list of layers
        pool = EBRIMMarshallerPool.getInstance();
    }

    @AfterClass
    public static void finish() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
    }

    private static String getDefaultURL() {
        return "http://localhost:" +  grizzly.getCurrentPort() + "/csw/default?";
    }

    private static String getCsw2URL() {
        return "http://localhost:" +  grizzly.getCurrentPort() + "/csw/csw2?";
    }

    @Test
    public void testCSWGetCapabilities() throws Exception {

        waitForStart();

        //update the federated catalog in case of busy port
        URL fedCatURL = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/csw/admin?request=setFederatedCatalog&id=csw2&servers=" + getDefaultURL());
        URLConnection conec = fedCatURL.openConnection();

        Object obj = getStringResponse(conec);

        // Creates a valid GetCapabilities url.
        URL getCapsUrl = new URL(getDefaultURL());


        // for a POST request
        conec = getCapsUrl.openConnection();

        final GetCapabilitiesType request = new GetCapabilitiesType("CSW");

        postRequestObject(conec, request);
        obj = unmarshallResponse(conec);

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
    public void testCSWGetDomain() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(getDefaultURL());


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        GetDomainType request = new GetDomainType("CSW", "2.0.2", null, "GetCapabilities.sections");

        postRequestObject(conec, request);
        Object result = unmarshallResponse(conec);

        assertTrue(result instanceof GetDomainResponseType);

        List<DomainValues> values = new ArrayList<DomainValues>();
        ListOfValuesType list = new ListOfValuesType(Arrays.asList("All", "ServiceIdentification", "ServiceProvider", "OperationsMetadata","Filter_Capabilities"));
        values.add(new DomainValuesType("GetCapabilities.sections", null, list, new QName("http://www.opengis.net/cat/csw/2.0.2", "Capabilities")));
        GetDomainResponseType expResult = new GetDomainResponseType(values);

        assertEquals(expResult, result);

        request = new GetDomainType("CSW", "2.0.2", "title", null);

        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);
        result = unmarshallResponse(conec);

        assertTrue(result instanceof GetDomainResponseType);

        values = new ArrayList<DomainValues>();
        list = new ListOfValuesType(Arrays.asList("Aliquam fermentum purus quis arcu","Fuscé vitae ligulä","Lorem ipsum","Lorem ipsum dolor sit amet",
                "Maecenas enim","Mauris sed neque","Ut facilisis justo ut lacus","Vestibulum massa purus","Ñunç elementum"));
        values.add(new DomainValuesType(null, "title", list, new QName("http://www.isotc211.org/2005/gmd", "MD_Metadata")));
        expResult = new GetDomainResponseType(values);

        assertEquals(expResult, result);
    }

    @Test
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
        assertEquals(1, grResult.getAbstractRecord().size());


        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "text/xml", null, Arrays.asList("urn:uuid:ab42a8c4-95e8-4630-bf79-33e59241605a"));

        conec = getCapsUrl.openConnection();

        postRequestObject(conec, factory.createGetRecordById(request));
        result = unmarshallResponse(conec);

        assertTrue(result instanceof GetRecordByIdResponseType);

        grResult = (GetRecordByIdResponseType) result;
        assertEquals(1, grResult.getAbstractRecord().size());

    }

    @Test
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
        assertEquals(1, grResult.getSearchResults().getAbstractRecord().size());

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

        assertEquals(12, grResult.getSearchResults().getAbstractRecord().size());

    }

    @Test
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
        assertEquals(13, grResult.getSearchResults().getAbstractRecord().size());



        // no distribution
        conec = getCapsUrl.openConnection();

        constraint = new QueryConstraintType("identifier like '%%'", "1.1.0");
        query = new QueryType(Arrays.asList(TypeNames.RECORD_QNAME), new ElementSetNameType(ElementSetType.FULL), null, constraint);
        request = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, null, null, 1, 20, query, null);

        postRequestObject(conec, request);
        result = unmarshallResponse(conec);

        assertTrue(result instanceof GetRecordsResponseType);

        grResult = (GetRecordsResponseType) result;

        assertEquals(1, grResult.getSearchResults().getAbstractRecord().size());

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

        assertEquals(1, grResult.getSearchResults().getAbstractRecord().size());
     }
}
