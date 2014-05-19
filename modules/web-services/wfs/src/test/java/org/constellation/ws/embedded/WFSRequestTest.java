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

import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.provider.DataProviders;
import org.constellation.provider.configuration.Configurator;
import org.constellation.util.Util;

import static org.constellation.provider.configuration.ProviderParameters.*;
import org.constellation.test.CstlDOMComparator;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.TestRunner;
import static org.constellation.ws.embedded.AbstractGrizzlyServer.initDataDirectory;

// Geotoolkit dependencies
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.referencing.CRS;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.Source;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.Providers;
import org.constellation.provider.configuration.AbstractConfigurator;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.ogc.xml.v110.FeatureIdType;
import org.geotoolkit.sampling.xml.v100.SamplingPointType;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.wfs.xml.v110.*;
import org.geotoolkit.wfs.xml.*;

import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.wfs.xml.v200.DescribeStoredQueriesResponseType;
import org.geotoolkit.wfs.xml.v200.DescribeStoredQueriesType;
import org.geotoolkit.wfs.xml.v200.GetPropertyValueType;
import org.geotoolkit.wfs.xml.v200.ListStoredQueriesResponseType;
import org.geotoolkit.wfs.xml.v200.ListStoredQueriesType;
import org.geotoolkit.wfs.xml.v200.MemberPropertyType;
import org.geotoolkit.wfs.xml.v200.ValueCollectionType;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assume.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

// GeoAPI dependencies
import org.opengis.parameter.ParameterValueGroup;
import static org.geotoolkit.parameter.ParametersExt.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class WFSRequestTest extends AbstractGrizzlyServer {

    private static boolean datasourceCreated = false;

    private static final String WFS_GETCAPABILITIES_URL_NO_SERV = "request=GetCapabilities&version=1.1.0";
    private static final String WFS_GETCAPABILITIES_URL_NO_SERV2 = "request=GetCapabilities&version=2.0.0";
    
    private static final String WFS_GETCAPABILITIES_URL_NO_VERS = "request=GetCapabilities&service=WFS";
    
    private static final String WFS_GETCAPABILITIES_URL = "request=GetCapabilities&version=1.1.0&service=WFS";
    
    private static final String WFS_GETCAPABILITIES_URL_AV = "request=GetCapabilities&acceptversions=10.0.0,2.0.0,1.1.0&service=WFS";
    
    private static final String WFS_GETCAPABILITIES_ERROR_URL = "request=GetCapabilities&version=1.3.0&service=WFS";

    private static final String WFS_GETFEATURE_URL = "request=getFeature&service=WFS&version=1.1.0&"
            + "typename=sa:SamplingPoint&namespace=xmlns(sa=http://www.opengis.net/sampling/1.0)&"
            + "filter=%3Cogc:Filter%20xmlns:ogc=%22http://www.opengis.net/ogc%22%20xmlns:gml=%22http://www.opengis.net/gml%22%3E"
            + "%3Cogc:PropertyIsEqualTo%3E"
            + "%3Cogc:PropertyName%3Egml:name%3C/ogc:PropertyName%3E"
            + "%3Cogc:Literal%3E10972X0137-PONT%3C/ogc:Literal%3E"
            + "%3C/ogc:PropertyIsEqualTo%3E"
            + "%3C/ogc:Filter%3E";
    
    private static final String WFS_GETFEATURE_URL_V2 = "request=getFeature&service=WFS&version=2.0.0&"
            + "typenames=sa:SamplingPoint&namespace=xmlns(sa=http://www.opengis.net/sampling/1.0)&"
            + "filter=%3Cfes:Filter%20xmlns:fes=%22http://www.opengis.net/fes/2.0%22%20xmlns:gml=%22http://www.opengis.net/gml/3.2%22%3E"
            + "%3Cfes:PropertyIsEqualTo%3E"
            + "%3Cfes:ValueReference%3Egml:name%3C/fes:ValueReference%3E"
            + "%3Cfes:Literal%3E10972X0137-PONT%3C/fes:Literal%3E"
            + "%3C/fes:PropertyIsEqualTo%3E"
            + "%3C/fes:Filter%3E";

    private static final String WFS_GETFEATURE_SQ_URL = "typeName=tns:SamplingPoint&startindex=0&count=10&request=GetFeature&service=WFS"
            +                                           "&namespaces=xmlns(xml,http://www.w3.org/XML/1998/namespace),xmlns(tns,http://www.opengis.net/sampling/1.0),xmlns(wfs,http://www.opengis.net/wfs/2.0)"
            +                                           "&storedquery_id=urn:ogc:def:storedQuery:OGC-WFS::GetFeatureByType&version=2.0.0";
    
    private static final String WFS_DESCRIBE_FEATURE_TYPE_URL = "request=DescribeFeatureType&service=WFS&version=1.1.0&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1";
    private static final String WFS_DESCRIBE_FEATURE_TYPE_URL_V2 = "request=DescribeFeatureType&service=WFS&version=2.0.0&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.2";

    private static String EPSG_VERSION;

    public static boolean hasLocalDatabase() {
        return false; // TODO
    }

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initPool() throws JAXBException {
        ConfigurationEngine.setupTestEnvironement("WFSRequestTest");

        final List<Source> sources = Arrays.asList(new Source("coverageTestSrc", true, null, null),
                                                   new Source("omSrc", true, null, null),
                                                   new Source("shapeSrc", true, null, null),
                                                   new Source("postgisSrc", true, null, null));
        final Layers layers = new Layers(sources);
        final LayerContext config = new LayerContext(layers);
        config.getCustomParameters().put("shiroAccessible", "false");
        config.getCustomParameters().put("transactionSecurized", "false");
        config.getCustomParameters().put("transactionnal", "true");

        ConfigurationEngine.storeConfiguration("WFS", "default", config);
        ConfigurationEngine.storeConfiguration("WFS", "test", config);

        final List<Source> sources2 = Arrays.asList(new Source("shapeSrc", true, null, null),
                                                   new Source("omSrc", true, null, null),
                                                   new Source("smlSrc", true, null, null));
        final Layers layers2 = new Layers(sources2);
        final LayerContext config2 = new LayerContext(layers2);
        config2.getCustomParameters().put("shiroAccessible", "false");
        config2.getCustomParameters().put("transactionSecurized", "false");
        config2.getCustomParameters().put("transactionnal", "true");

        ConfigurationEngine.storeConfiguration("WFS", "test1", config2);

        initServer(new String[] {"org.constellation.wfs.ws.rs",
            "org.constellation.configuration.ws.rs",
            "org.constellation.ws.rs.provider"}, null);

        EPSG_VERSION = CRS.getVersion("EPSG").toString();
        pool = new MarshallerPool(JAXBContext.newInstance("org.geotoolkit.wfs.xml.v110"   +
            		  ":org.geotoolkit.ogc.xml.v110"  +
                          ":org.geotoolkit.wfs.xml.v200"  +
            		  ":org.geotoolkit.gml.xml.v311"  +
                          ":org.geotoolkit.xsd.xml.v2001" +
                          ":org.geotoolkit.sampling.xml.v100" +
                         ":org.apache.sis.internal.jaxb.geometry"), null);


       final Configurator configurator = new AbstractConfigurator() {
           
            @Override
            public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
                
                 final ArrayList<Map.Entry<String, ParameterValueGroup>> lst = new ArrayList<>();
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("feature-store");
                
                try{ 
                    
                    {//OBSERVATION
                        final String url = "jdbc:derby:memory:TesWFSRequestWorker";
                        final DefaultDataSource ds = new DefaultDataSource(url + ";create=true");
                        if (!datasourceCreated) {
                            Connection con = ds.getConnection();
                            DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                            sr.run(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
                            sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));
                            con.close();
                            datasourceCreated = true;
                        }
                        ds.shutdown();

                        final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                        getOrCreateValue(source, "id").setValue("omSrc");
                        getOrCreateValue(source, "load_all").setValue(true);

                        final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                        final ParameterValueGroup omconfig = createGroup(choice, "OMParameters");
                        getOrCreateValue(omconfig, "sgbdtype").setValue("derby");
                        getOrCreateValue(omconfig, "derbyurl").setValue(url);
                        lst.add(new AbstractMap.SimpleImmutableEntry<>("omSrc",source));
                    }

                    {//SHAPEFILE
                        final File outputDir = initDataDirectory();
                        final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                        getOrCreateValue(source, "id").setValue("shapeSrc");
                        getOrCreateValue(source, "load_all").setValue(true);

                        final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                        final ParameterValueGroup shpconfig = createGroup(choice, "ShapefileParametersFolder");
                        getOrCreateValue(shpconfig, "url").setValue(new URL("file:" + outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles"));
                        getOrCreateValue(shpconfig, "namespace").setValue("http://www.opengis.net/gml");

                        final ParameterValueGroup layer = getOrCreateGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("NamedPlaces");
                        getOrCreateValue(layer, "style").setValue("cite_style_NamedPlaces");
                        
                        lst.add(new AbstractMap.SimpleImmutableEntry<>("shapeSrc",source));
                    }

                    {//POSTGIS
                        if (hasLocalDatabase()) {
                            final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                            getOrCreateValue(source, "id").setValue("postgisSrc");
                            getOrCreateValue(source, "load_all").setValue(true);

                            final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                            final ParameterValueGroup pgconfig = createGroup(choice, " PostgresParameters");
                            getOrCreateValue(pgconfig, "host").setValue("flupke.geomatys.com");
                            getOrCreateValue(pgconfig, "port").setValue(5432);
                            getOrCreateValue(pgconfig, "database").setValue("cite-wfs");
                            getOrCreateValue(pgconfig, "schema").setValue("public");
                            getOrCreateValue(pgconfig, "user").setValue("test");
                            getOrCreateValue(pgconfig, "password").setValue("test");
                            getOrCreateValue(pgconfig, "namespace").setValue("no namespace");

                            //add a custom sql query layer
                            final ParameterValueGroup layer = getOrCreateGroup(source, "Layer");
                            getOrCreateValue(layer, "name").setValue("CustomSQLQuery");
                            getOrCreateValue(layer, "language").setValue("CUSTOM-SQL");
                            getOrCreateValue(layer, "statement").setValue("SELECT name as nom, \"pointProperty\" as geom FROM \"PrimitiveGeoFeature\" ");
                            
                            lst.add(new AbstractMap.SimpleImmutableEntry<>("postgisSrc",source));                
                        }
                    }

                }catch(Exception ex){
                    throw new RuntimeException(ex.getLocalizedMessage(),ex);
                }
                
                return lst;
            }

            @Override
            public List<Configurator.ProviderInformation> getProviderInformations() throws ConfigurationException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        DataProviders.getInstance().setConfigurator(configurator);
    }

    @AfterClass
    public static void shutDown() {
        ConfigurationEngine.shutdownTestEnvironement("WFSRequestTest");
        DataProviders.getInstance().setConfigurator(Providers.DEFAULT_CONFIGURATOR);
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        finish();
    }

    @Test
    @Order(order=1)
    public void testWFSGetCapabilities() throws Exception {
        waitForStart();

        // Creates a valid GetCapabilities url.
        URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL);
        
        Object obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WFSCapabilitiesType);

        WFSCapabilitiesType responseCaps = (WFSCapabilitiesType)obj;
        String currentUrl =  responseCaps.getOperationsMetadata().getOperation("GetCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?", currentUrl);

        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/test?" + WFS_GETCAPABILITIES_URL);
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WFSCapabilitiesType);

        responseCaps = (WFSCapabilitiesType)obj;
        currentUrl =  responseCaps.getOperationsMetadata().getOperation("GetCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/test?", currentUrl);


        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL);

        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WFSCapabilitiesType);
        responseCaps = (WFSCapabilitiesType)obj;
        currentUrl =  responseCaps.getOperationsMetadata().getOperation("GetCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?", currentUrl);
        
        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_ERROR_URL);
        obj = unmarshallResponse(getCapsUrl);
        assertTrue("unexpected type:" + obj.getClass().getName(), obj instanceof ExceptionReport);
        
        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL_AV);
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof org.geotoolkit.wfs.xml.v200.WFSCapabilitiesType);
        
        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL_NO_SERV);
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof org.geotoolkit.ows.xml.v100.ExceptionReport);
        org.geotoolkit.ows.xml.v100.ExceptionReport report100 = (org.geotoolkit.ows.xml.v100.ExceptionReport) obj;
        assertEquals("1.0.0", report100.getVersion());
        
        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL_NO_SERV2);
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof ExceptionReport);
        ExceptionReport report200 = (ExceptionReport) obj;
        assertEquals("2.0.0", report200.getVersion());
        
        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL_NO_VERS);
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof org.geotoolkit.wfs.xml.v200.WFSCapabilitiesType);
    }

    /**
     */
    @Test
    @Order(order=2)
    public void testWFSGetFeaturePOST() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

        final List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        final GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, 2, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue("unexpected type: " + obj.getClass().getName() + "\n" + obj, obj instanceof FeatureCollectionType);

    }
    
    @Test
    @Order(order=3)
    public void testWFSGetFeaturePOSTV2() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

        final List<org.geotoolkit.wfs.xml.v200.QueryType> queries = new ArrayList<>();
        queries.add(new org.geotoolkit.wfs.xml.v200.QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        final org.geotoolkit.wfs.xml.v200.GetFeatureType request = new org.geotoolkit.wfs.xml.v200.GetFeatureType("WFS", "2.0.0", null, 2, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue("unexpected type: " + obj.getClass().getName() + "\n" + obj, obj instanceof org.geotoolkit.wfs.xml.v200.FeatureCollectionType);

    }

    /**
     */
    @Test
    @Order(order=4)
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

    @Test
    @Order(order=5)
    public void testWFSGetFeatureGET2() throws Exception {
        final URL getfeatsUrl;
        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETFEATURE_URL_V2);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        Object obj = unmarshallResponse(getfeatsUrl);

        assertTrue("was:" + obj, obj instanceof org.geotoolkit.wfs.xml.v200.FeatureCollectionType);

        org.geotoolkit.wfs.xml.v200.FeatureCollectionType feat = (org.geotoolkit.wfs.xml.v200.FeatureCollectionType) obj;
        assertEquals(1, feat.getMember().size());
        
        MemberPropertyType member = feat.getMember().get(0);
        
        final JAXBElement element = (JAXBElement) member.getContent().get(0);

        assertTrue("expected samplingPoint but was:" +  element.getValue(), element.getValue() instanceof SamplingPointType);
        SamplingPointType sp = (SamplingPointType) element.getValue();

        // assertEquals("10972X0137-PONT", sp.getName()); TODO name attribute is moved to namespace GML 3.2 so the java binding does not match
    }
    
    @Test
    @Order(order=6)
    public void testWFSGetFeatureGETStoredQuery() throws Exception {
        final URL getfeatsUrl;
        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETFEATURE_SQ_URL);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        final URLConnection conec = getfeatsUrl.openConnection();

        String xmlResult    = getStringResponse(conec);
        String xmlExpResult = getStringFromFile("org/constellation/wfs/xml/samplingPointCollection-3v2.xml");

        xmlExpResult = xmlExpResult.replace("EPSG_VERSION", EPSG_VERSION);
        xmlResult    = xmlResult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        domCompare(xmlExpResult, xmlResult);
    }

    
    /**
     */
    @Test
    @Order(order=7)
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
        if (hasLocalDatabase()) {
            assertEquals(17, schema.getElements().size());
        } else {
            assertEquals(13, schema.getElements().size());
        }

        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/test?" + WFS_DESCRIBE_FEATURE_TYPE_URL_V2);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        obj = unmarshallResponse(getfeatsUrl);

        assertTrue("was:" + obj, obj instanceof Schema);

        schema = (Schema) obj;
        if (hasLocalDatabase()) {
            assertEquals(17, schema.getElements().size());
        } else {
            assertEquals(13, schema.getElements().size());
        }

    }

    /**
     */
    @Test
    @Order(order=8)
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
        List<InsertedFeatureType> insertedFeatures = new ArrayList<>();
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-007"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-008"), null));
        InsertResultsType insertResult    = new InsertResultsType(insertedFeatures);
        TransactionResponseType ExpResult = new TransactionResponseType(sum, null, insertResult, "1.1.0");

        assertEquals(ExpResult, result);


        /**
         * We verify that the 2 new samplingPoint are inserted
         */
        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);
        String xmlResult    = getStringResponse(conec);
        String xmlExpResult = getStringFromFile("org/constellation/xml/samplingPointCollection-1.xml");

        xmlExpResult = xmlExpResult.replace("EPSG_VERSION", EPSG_VERSION);
        xmlResult    = xmlResult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(xmlExpResult, xmlResult);

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/Insert-SamplingPoint-2.xml");

        // Try to unmarshall something from the response returned by the server.
        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof TransactionResponseType);

        result = (TransactionResponseType) obj;

        sum              = new TransactionSummaryType(2, 0, 0);
        insertedFeatures = new ArrayList<>();
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-010"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-009"), null));
        insertResult    = new InsertResultsType(insertedFeatures);
        ExpResult = new TransactionResponseType(sum, null, insertResult, "1.1.0");

        assertEquals(ExpResult, result);

        /**
         * We verify that the 2 new samplingPoint are inserted
         */
        queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);

        // Try to unmarshall something from the response returned by the server.
        xmlResult    = getStringResponse(conec);

        xmlExpResult = getStringFromFile("org/constellation/xml/samplingPointCollection-2.xml");
        xmlExpResult = xmlExpResult.replace("EPSG_VERSION", EPSG_VERSION);
        xmlResult    = xmlResult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(xmlExpResult, xmlResult);


        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/Insert-SamplingPoint-3.xml");

        // Try to unmarshall something from the response returned by the server.
        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof TransactionResponseType);

        result = (TransactionResponseType) obj;

        sum              = new TransactionSummaryType(2, 0, 0);
        insertedFeatures = new ArrayList<>();
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-012"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-011"), null));
        insertResult    = new InsertResultsType(insertedFeatures);
        ExpResult = new TransactionResponseType(sum, null, insertResult, "1.1.0");

        assertEquals(ExpResult, result);

        /**
         * We verify that the 2 new samplingPoint are inserted
         */
        queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);

        // Try to unmarshall something from the response returned by the server.
        xmlResult    = getStringResponse(conec);
        xmlExpResult = getStringFromFile("org/constellation/xml/samplingPointCollection-3.xml");
        xmlExpResult = xmlExpResult.replace("EPSG_VERSION", EPSG_VERSION);
        xmlResult    = xmlResult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(xmlExpResult, xmlResult);

    }

    @Test
    @Order(order=9)
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
        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "NamedPlaces")), null));
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);

        // Try to unmarshall something from the response returned by the server.
        String xmlResult    = getStringResponse(conec);
        String xmlExpResult = getStringFromFile("org/constellation/xml/namedPlacesCollection-1.xml");
        xmlExpResult = xmlExpResult.replace("9090", grizzly.getCurrentPort() + "");
        xmlResult    = xmlResult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(xmlExpResult, xmlResult);
    }
    
    @Test
    @Order(order=10)
    public void testWFSListStoredQueries() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

        final ListStoredQueriesType request = new ListStoredQueriesType("WFS", "2.0.0", null);

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue("unexpected type: " + obj.getClass().getName() + "\n" + obj, obj instanceof ListStoredQueriesResponseType);

    }
    
    @Test
    @Order(order=11)
    public void testWFSDescribeStoredQueries() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

        final DescribeStoredQueriesType request = new DescribeStoredQueriesType("WFS", "2.0.0", null, Arrays.asList("urn:ogc:def:storedQuery:OGC-WFS::GetFeatureById"));

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue("unexpected type: " + obj.getClass().getName() + "\n" + obj, obj instanceof DescribeStoredQueriesResponseType);

    }
    
    @Test
    @Order(order=12)
    public void testWFSGetPropertyValue() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

         /**
         * Test 1 : query on typeName samplingPoint with HITS
         */
        org.geotoolkit.wfs.xml.v200.QueryType query = new org.geotoolkit.wfs.xml.v200.QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        String valueReference = "sampledFeature";
        GetPropertyValueType request = new GetPropertyValueType("WFS", "2.0.0", null, Integer.MAX_VALUE, query, ResultTypeType.HITS, "text/xml; subtype=gml/3.2.1",valueReference);
        request.setValueReference(valueReference);

         // for a POST request
        URLConnection conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        Object result = unmarshallResponse(conec);

        assertTrue("unexpected type: " + result.getClass().getName() + "\n" + result, result instanceof ValueCollectionType);
        
        assertTrue(result instanceof ValueCollection);
        assertEquals(12, ((ValueCollection)result).getNumberReturned());

        /**
         * Test 2 : query on typeName samplingPoint with RESULTS
         */
        request.setResultType(ResultTypeType.RESULTS);
        conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        String sresult = getStringResponse(conec);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.embedded.ValueCollectionOM1.xml"));
        domCompare(expectedResult, sresult);

        /**
         * Test 3 : query on typeName samplingPoint with RESULTS
         */
        valueReference = "position";
        request.setValueReference(valueReference);
        conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        sresult = getStringResponse(conec);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.embedded.ValueCollectionOM2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);
        domCompare(expectedResult, sresult);

    }

    public static void domCompare(final Object actual, final Object expected) throws Exception {

        final CstlDOMComparator comparator = new CstlDOMComparator(expected, actual);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
        comparator.compare();
    }
}
