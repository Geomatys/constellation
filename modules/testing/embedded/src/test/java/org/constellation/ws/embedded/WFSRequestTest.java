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
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.xml.MarshallerPool;
import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.geotoolkit.ogc.xml.v110.FeatureIdType;
import org.geotoolkit.sampling.xml.v100.SamplingPointType;
import org.geotoolkit.wfs.xml.v110.FeatureCollectionType;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.InsertResultsType;
import org.geotoolkit.wfs.xml.v110.InsertedFeatureType;
import org.geotoolkit.wfs.xml.v110.QueryType;
import org.geotoolkit.wfs.xml.v110.ResultTypeType;
import org.geotoolkit.wfs.xml.v110.TransactionResponseType;
import org.geotoolkit.wfs.xml.v110.TransactionSummaryType;
import org.junit.*;
import static org.junit.Assume.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WFSRequestTest extends AbstractTestRequest {

    private static final String WFS_POST_URL = "http://localhost:9090/wfs/default?";

    private static final String WFS_GETFEATURE_URL = "http://localhost:9090/wfs/default?request=getFeature&service=WFS&version=1.1.0&"
            + "typename=sa:SamplingPoint&namespace=xmlns(sa=http://www.opengis.net/sampling/1.0)&"
            + "filter=%3Cogc:Filter%20xmlns:ogc=%22http://www.opengis.net/ogc%22%20xmlns:gml=%22http://www.opengis.net/gml%22%3E"
            + "%3Cogc:PropertyIsEqualTo%3E"
            + "%3Cogc:PropertyName%3Egml:name%3C/ogc:PropertyName%3E"
            + "%3Cogc:Literal%3E10972X0137-PONT%3C/ogc:Literal%3E"
            + "%3C/ogc:PropertyIsEqualTo%3E"
            + "%3C/ogc:Filter%3E";

     private static final String WFS_DESCRIBE_FEATURE_TYPE_URL = "http://localhost:9090/wfs/default?request=DescribeFeatureType&service=WFS&version=1.1.0&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1";

    private static String EPSG_VERSION;
    
    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initPool() throws JAXBException {
        EPSG_VERSION = CRS.getVersion("EPSG").toString();
        pool = new MarshallerPool("org.geotoolkit.wfs.xml.v110"   +
            		  ":org.geotoolkit.ogc.xml.v110"  +
            		  ":org.geotoolkit.gml.xml.v311"  +
                          ":org.geotoolkit.xsd.xml.v2001" +
                          ":org.geotoolkit.sampling.xml.v100" +
                         ":org.geotoolkit.internal.jaxb.geometry");
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
    public void testWFSGetFeaturePOST() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(WFS_POST_URL);

        final List<QueryType> queries = new ArrayList<QueryType>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        final GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, 2, queries, ResultTypeType.RESULTS, "text/xml");
        
        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof FeatureCollectionType);

    }

    /**
     */
    @Test
    public void testWFSGetFeatureGET() throws Exception {
        final URL getfeatsUrl;
        try {
            getfeatsUrl = new URL(WFS_GETFEATURE_URL);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        Object obj = unmarshallResponse(getfeatsUrl);

        assertTrue(obj instanceof FeatureCollectionType);

        FeatureCollectionType feat = (FeatureCollectionType) obj;
        assertEquals(1, feat.getFeatureMember().size());

        assertTrue("expected samplingPoint but was:" +  feat.getFeatureMember().get(0),
                feat.getFeatureMember().get(0).getAbstractFeature() instanceof SamplingPointType);
        SamplingPointType sp = (SamplingPointType) feat.getFeatureMember().get(0).getAbstractFeature();

        assertEquals("10972X0137-PONT", sp.getName());
    }

    /**
     */
    @Test
    public void testWFSDescribeFeatureGET() throws Exception {
        final URL getfeatsUrl;
        try {
            getfeatsUrl = new URL(WFS_DESCRIBE_FEATURE_TYPE_URL);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        Object obj = unmarshallResponse(getfeatsUrl);

        assertTrue(obj instanceof Schema);

        Schema schema = (Schema) obj;
        assertEquals(13, schema.getElements().size());

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
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-003"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-004"), null));
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

        xmlExpResult = xmlExpResult.replace("EPSG_VERSION", EPSG_VERSION);
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
        xmlExpResult = xmlExpResult.replace("EPSG_VERSION", EPSG_VERSION);
        
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
        xmlExpResult = xmlExpResult.replace("EPSG_VERSION", EPSG_VERSION);

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

}
