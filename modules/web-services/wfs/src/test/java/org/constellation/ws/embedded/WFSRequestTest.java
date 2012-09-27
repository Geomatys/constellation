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

import java.net.MalformedURLException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.Configurator;
import org.constellation.provider.shapefile.ShapeFileProviderService;
import org.constellation.util.Util;

import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;
import static org.constellation.provider.configuration.ProviderParameters.*;

// Geotoolkit dependencies
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.data.om.OMDataStoreFactory;
import org.geotoolkit.data.postgis.PostgisNGDataStoreFactory;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.ogc.xml.v110.FeatureIdType;
import org.geotoolkit.sampling.xml.v100.SamplingPointType;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.wfs.xml.v110.*;
import org.geotoolkit.wfs.xml.*;

import static org.geotoolkit.data.postgis.PostgisNGDataStoreFactory.*;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assume.*;
import static org.junit.Assert.*;

// GeoAPI dependencies
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WFSRequestTest extends AbstractGrizzlyServer {

    private static boolean datasourceCreated = false;

    private static final String WFS_GETCAPABILITIES_URL = "request=GetCapabilities&version=1.1.0&service=WFS";
    private static final String WFS_GETCAPABILITIES_URL2 = "request=GetCapabilities&version=1.1.0&service=WFS";

    private static final String WFS_GETFEATURE_URL = "request=getFeature&service=WFS&version=1.1.0&"
            + "typename=sa:SamplingPoint&namespace=xmlns(sa=http://www.opengis.net/sampling/1.0)&"
            + "filter=%3Cogc:Filter%20xmlns:ogc=%22http://www.opengis.net/ogc%22%20xmlns:gml=%22http://www.opengis.net/gml%22%3E"
            + "%3Cogc:PropertyIsEqualTo%3E"
            + "%3Cogc:PropertyName%3Egml:name%3C/ogc:PropertyName%3E"
            + "%3Cogc:Literal%3E10972X0137-PONT%3C/ogc:Literal%3E"
            + "%3C/ogc:PropertyIsEqualTo%3E"
            + "%3C/ogc:Filter%3E";

     private static final String WFS_DESCRIBE_FEATURE_TYPE_URL = "request=DescribeFeatureType&service=WFS&version=1.1.0&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1";
     private static final String WFS_DESCRIBE_FEATURE_TYPE_URL_V2 = "request=DescribeFeatureType&service=WFS&version=2.0.0&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.2";

    private static String EPSG_VERSION;

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initPool() throws JAXBException {
        initServer(new String[] {"org.constellation.wfs.ws.rs",
            "org.constellation.configuration.ws.rs",
            "org.constellation.ws.rs.provider"}, null);

        EPSG_VERSION = CRS.getVersion("EPSG").toString();
        pool = new MarshallerPool("org.geotoolkit.wfs.xml.v110"   +
            		  ":org.geotoolkit.ogc.xml.v110"  +
            		  ":org.geotoolkit.gml.xml.v311"  +
                          ":org.geotoolkit.xsd.xml.v2001" +
                          ":org.geotoolkit.sampling.xml.v100" +
                         ":org.geotoolkit.internal.jaxb.geometry");


       final Configurator config = new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(String serviceName, ParameterDescriptorGroup desc) {

                final ParameterValueGroup config = desc.createValue();

                if("observation".equals(serviceName)){
                    try{
                        final String url = "jdbc:derby:memory:TestEmbeddedWFSWorker";
                        final DefaultDataSource ds = new DefaultDataSource(url + ";create=true");
                        if (!datasourceCreated) {
                            Connection con = ds.getConnection();
                            DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                            sr.run(Util.getResourceAsStream("org/constellation/observation/structure_observations.sql"));
                            sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data.sql"));
                            con.close();
                            datasourceCreated = true;
                        }
                        ds.shutdown();

                        final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                        final ParameterValueGroup srcconfig = getOrCreate(OMDataStoreFactory.PARAMETERS_DESCRIPTOR,source);
                        srcconfig.parameter(OMDataStoreFactory.SGBDTYPE.getName().getCode()).setValue("derby");
                        srcconfig.parameter(OMDataStoreFactory.DERBYURL.getName().getCode()).setValue(url);
                        source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("omSrc");
                    }catch(Exception ex){
                        throw new RuntimeException(ex.getLocalizedMessage(),ex);
                    }
                }else if("shapefile".equals(serviceName)){
                    try{
                        final File outputDir = initDataDirectory();

                        final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                        final ParameterValueGroup srcconfig = getOrCreate(ShapeFileProviderService.SOURCE_CONFIG_DESCRIPTOR,source);
                        source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("shapeSrc");
                        srcconfig.parameter(ShapeFileProviderService.FOLDER_DESCRIPTOR.getName().getCode())
                                .setValue(outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles");
                        srcconfig.parameter(ShapeFileProviderService.NAMESPACE_DESCRIPTOR.getName().getCode())
                                .setValue("http://www.opengis.net/gml");

                        ParameterValueGroup layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                        layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("NamedPlaces");
                        layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_NamedPlaces");

                    }catch(Exception ex){
                        throw new RuntimeException(ex.getLocalizedMessage(),ex);
                    }
                }else if("postgis".equals(serviceName)){
                    // Defines a PostGis data provider
                    final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    final ParameterValueGroup srcconfig = getOrCreate(PostgisNGDataStoreFactory.PARAMETERS_DESCRIPTOR,source);

                    srcconfig.parameter(HOST.getName().getCode()).setValue("flupke.geomatys.com");
                    srcconfig.parameter(PORT.getName().getCode()).setValue(5432);
                    srcconfig.parameter(DATABASE.getName().getCode()).setValue("cite-wfs");
                    srcconfig.parameter(SCHEMA.getName().getCode()).setValue("public");
                    srcconfig.parameter(USER.getName().getCode()).setValue("test");
                    srcconfig.parameter(PASSWD.getName().getCode()).setValue("test");
                    srcconfig.parameter(NAMESPACE_DESCRIPTOR.getName().getCode()).setValue("no namespace");

                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("postgisSrc");

                    //add a custom sql query layer
                    ParameterValueGroup layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("CustomSQLQuery");
                    layer.parameter(LAYER_QUERY_LANGUAGE.getName().getCode()).setValue("CUSTOM-SQL");
                    layer.parameter(LAYER_QUERY_STATEMENT.getName().getCode()).setValue(
                            "SELECT name as nom, \"pointProperty\" as geom FROM \"PrimitiveGeoFeature\" ");
                }

                //empty configuration for others
                return config;
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        LayerProviderProxy.getInstance().setConfigurator(config);
    }

    @AfterClass
    public static void shutDown() {
        LayerProviderProxy.getInstance().setConfigurator(Configurator.DEFAULT);
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        //finish();
    }

    @Test
    public void testWFSGetCapabilities() throws JAXBException, IOException {

        // Creates a valid GetCapabilities url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WCSCapabilitiesType.
        Object obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WFSCapabilitiesType);

        WFSCapabilitiesType responseCaps = (WFSCapabilitiesType)obj;


        String currentUrl =  responseCaps.getOperationsMetadata().getOperation("GetCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?", currentUrl);

        try {
            getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/test?" + WFS_GETCAPABILITIES_URL2);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WFSCapabilitiesType.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WFSCapabilitiesType);

        responseCaps = (WFSCapabilitiesType)obj;

        currentUrl =  responseCaps.getOperationsMetadata().getOperation("GetCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/test?", currentUrl);


        try {
            getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WCSCapabilitiesType.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WFSCapabilitiesType);

        responseCaps = (WFSCapabilitiesType)obj;

        currentUrl =  responseCaps.getOperationsMetadata().getOperation("GetCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?", currentUrl);
    }

    /**
     */
    @Test
    public void testWFSGetFeaturePOST() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

        final List<QueryType> queries = new ArrayList<QueryType>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        final GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, 2, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue("unexpected type: " + obj.getClass().getName() + "\n" + obj, obj instanceof FeatureCollectionType);

    }

    /**
     */
    @Test
    public void testWFSGetFeatureGET() throws Exception {
        final URL getfeatsUrl;
        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETFEATURE_URL);
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
        URL getfeatsUrl;
        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_DESCRIBE_FEATURE_TYPE_URL);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        Object obj = unmarshallResponse(getfeatsUrl);

        assertTrue(obj instanceof Schema);

        Schema schema = (Schema) obj;
        assertEquals(17, schema.getElements().size());

        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/test?" + WFS_DESCRIBE_FEATURE_TYPE_URL_V2);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        obj = unmarshallResponse(getfeatsUrl);

        assertTrue(obj instanceof Schema);

        schema = (Schema) obj;
        assertEquals(17, schema.getElements().size());

    }

    /**
     */
    @Test
    public void testWFSTransactionInsert() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/Insert-SamplingPoint-1.xml");
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof TransactionResponseType);

        TransactionResponseType result = (TransactionResponseType) obj;

        TransactionSummaryType sum        = new TransactionSummaryType(2, 0, 0);
        List<InsertedFeatureType> insertedFeatures = new ArrayList<InsertedFeatureType>();
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-006"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-007"), null));
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
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-008"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-009"), null));
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
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-010"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-011"), null));
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
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

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
        xmlExpResult = xmlExpResult.replace("9090", grizzly.getCurrentPort() + "");

        assertEquals(xmlExpResult, xmlResult);
    }

}
