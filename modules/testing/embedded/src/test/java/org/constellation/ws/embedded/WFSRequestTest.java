/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.constellation.util.Util;
import org.geotoolkit.ogc.xml.v110.FeatureIdType;
import org.geotoolkit.wfs.xml.WFSMarshallerPool;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.InsertResultsType;
import org.geotoolkit.wfs.xml.v110.InsertedFeatureType;
import org.geotoolkit.wfs.xml.v110.QueryType;
import org.geotoolkit.wfs.xml.v110.ResultTypeType;
import org.geotoolkit.wfs.xml.v110.TransactionResponseType;
import org.geotoolkit.wfs.xml.v110.TransactionSummaryType;
import org.geotoolkit.xml.MarshallerPool;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WFSRequestTest {

    private static MarshallerPool pool;
    
    private static final String WFS_POST_URL = "http://localhost:9090/wfs?";

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initLayerList() throws JAXBException {
        // Get the list of layers
        pool = WFSMarshallerPool.getInstance();
    }

    @AfterClass
    public static void finish() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
    }
    /**
     */
    @Test
    public void testWFSTransactionInsert() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(WFS_POST_URL);
        

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/Insert-SamplingPoint-1.xml");
        Object obj = unmarshallResponse(conec);
        
        assertTrue(obj instanceof TransactionResponseType);

        TransactionResponseType result = (TransactionResponseType) obj;

        TransactionSummaryType sum        = new TransactionSummaryType(2, 0, 0);
        List<InsertedFeatureType> insertedFeatures = new ArrayList<InsertedFeatureType>();
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("sampling-point-3"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("sampling-point-4"), null));
        InsertResultsType insertResult    = new InsertResultsType(insertedFeatures);
        TransactionResponseType ExpResult = new TransactionResponseType(sum, null, insertResult, "1.1.0");

        assertEquals(ExpResult, result);


        /**
         * We verify that the 2 new samplingPoint are inserted
         */
        List<QueryType> queries = new ArrayList<QueryType>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);
        String xmlResult    = getStringResponse(conec);
        String xmlExpResult = getStringFromFile("org/constellation/xml/samplingPointCollection-1.xml");

        assertEquals(xmlExpResult, xmlResult);

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/Insert-SamplingPoint-2.xml");

        // Try to unmarshall something from the response returned by the server.
        obj = unmarshallResponse(conec);
        
        assertTrue(obj instanceof TransactionResponseType);

        result = (TransactionResponseType) obj;

        sum              = new TransactionSummaryType(2, 0, 0);
        insertedFeatures = new ArrayList<InsertedFeatureType>();
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-005"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-006"), null));
        insertResult    = new InsertResultsType(insertedFeatures);
        ExpResult = new TransactionResponseType(sum, null, insertResult, "1.1.0");

        assertEquals(ExpResult, result);

        /**
         * We verify that the 2 new samplingPoint are inserted
         */
        queries = new ArrayList<QueryType>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);

        // Try to unmarshall something from the response returned by the server.
        xmlResult    = getStringResponse(conec);
        
        xmlExpResult = getStringFromFile("org/constellation/xml/samplingPointCollection-2.xml");

        assertEquals(xmlExpResult, xmlResult);


        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/Insert-SamplingPoint-3.xml");
        
        // Try to unmarshall something from the response returned by the server.
        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof TransactionResponseType);

        result = (TransactionResponseType) obj;

        sum              = new TransactionSummaryType(2, 0, 0);
        insertedFeatures = new ArrayList<InsertedFeatureType>();
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-007"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-008"), null));
        insertResult    = new InsertResultsType(insertedFeatures);
        ExpResult = new TransactionResponseType(sum, null, insertResult, "1.1.0");

        assertEquals(ExpResult, result);

        /**
         * We verify that the 2 new samplingPoint are inserted
         */
        queries = new ArrayList<QueryType>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);
        
        // Try to unmarshall something from the response returned by the server.
        xmlResult    = getStringResponse(conec);

        xmlExpResult = getStringFromFile("org/constellation/xml/samplingPointCollection-3.xml");

        assertEquals(xmlExpResult, xmlResult);

    }

    @Test
    public void testWFSTransactionUpdate() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(WFS_POST_URL);
        
        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/Update-NamedPlaces-1.xml");
        
        // Try to unmarshall something from the response returned by the server.
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof TransactionResponseType);

        TransactionResponseType result = (TransactionResponseType) obj;

        TransactionSummaryType sum              = new TransactionSummaryType(0, 1, 0);
        TransactionResponseType ExpResult = new TransactionResponseType(sum, null, null, "1.1.0");

        assertEquals(ExpResult, result);


        /**
         * We verify that the namedPlaces have been changed
         */
        List<QueryType> queries = new ArrayList<QueryType>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "NamedPlaces")), null));
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);

        // Try to unmarshall something from the response returned by the server.
        String xmlResult    = getStringResponse(conec);
        String xmlExpResult = getStringFromFile("org/constellation/xml/namedPlacesCollection-1.xml");

        assertEquals(xmlExpResult, xmlResult);
    }

    public String removeXmlns(String xml) {
        String s = xml;
        s = s.replaceAll("xmlns=\"[^\"]*\" ", "");
        s = s.replaceAll("xmlns=\"[^\"]*\"", "");
        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\" ", "");
        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\"", "");
        return s;
    }

    public void postRequestFile(URLConnection conec, String filePath) throws IOException {

        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "text/xml");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        final InputStream is = Util.getResourceAsStream(filePath);
        final StringWriter sw = new StringWriter();
        final BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        char[] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        wr.write(sw.toString());
        wr.flush();
        in.close();

    }

    public void postRequestObject(URLConnection conec, Object request) throws IOException, JAXBException {
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "text/xml");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        final StringWriter sw = new StringWriter();
        Marshaller marshaller = pool.acquireMarshaller();
        marshaller.marshal(request, sw);

        wr.write(sw.toString());
        wr.flush();
    }

    public Object unmarshallResponse(URLConnection conec) throws JAXBException, IOException {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object obj = unmarshaller.unmarshal(conec.getInputStream());

        pool.release(unmarshaller);

        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        return obj;
    }

    public String getStringResponse(URLConnection conec) throws UnsupportedEncodingException, IOException {
        // Try to unmarshall something from the response returned by the server.
        final StringWriter sw     = new StringWriter();
        final BufferedReader in   = new BufferedReader(new InputStreamReader(conec.getInputStream(), "UTF-8"));
        char [] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        String xmlResult = sw.toString();
        xmlResult = removeXmlns(xmlResult);
        xmlResult = xmlResult.replaceAll("xsi:schemaLocation=\"[^\"]*\" ", "");
        return xmlResult;
    }

    public String getStringFromFile(String filePath) throws UnsupportedEncodingException, IOException {
        final StringWriter sw     = new StringWriter();
        final BufferedReader in   = new BufferedReader(new InputStreamReader(Util.getResourceAsStream(filePath), "UTF-8"));
        char [] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        String xmlExpResult = sw.toString();

        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");
        xmlExpResult = removeXmlns(xmlExpResult);

        return xmlExpResult;
    }
}
