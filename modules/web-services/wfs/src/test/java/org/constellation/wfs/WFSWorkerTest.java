/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.wfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.constellation.provider.DataProviders;
import org.constellation.provider.configuration.Configurator;
import static org.constellation.provider.configuration.ProviderParameters.*;
import org.constellation.test.CstlDOMComparator;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.TestRunner;
import org.constellation.util.Util;
import org.constellation.wfs.ws.DefaultWFSWorker;
import org.constellation.wfs.ws.WFSWorker;
import org.constellation.wfs.ws.rs.FeatureCollectionWrapper;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.data.FeatureCollection;
import static org.geotoolkit.parameter.ParametersExt.*;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.ogc.xml.v110.BBOXType;
import org.geotoolkit.ogc.xml.v110.ComparisonOpsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.LiteralType;
import org.geotoolkit.ogc.xml.v110.PropertyIsEqualToType;
import org.geotoolkit.ogc.xml.v110.PropertyNameType;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.geotoolkit.ogc.xml.v110.SortOrderType;
import org.geotoolkit.ogc.xml.v110.SortPropertyType;
import org.geotoolkit.ogc.xml.v110.SpatialOpsType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.ows.xml.v100.AcceptVersionsType;
import org.geotoolkit.ows.xml.v100.SectionsType;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.wfs.xml.AllSomeType;
import org.geotoolkit.wfs.xml.ResultTypeType;
import org.geotoolkit.wfs.xml.TransactionResponse;
import org.geotoolkit.wfs.xml.WFSCapabilities;
import org.geotoolkit.wfs.xml.WFSMarshallerPool;
import org.geotoolkit.wfs.xml.v110.DeleteElementType;
import org.geotoolkit.wfs.xml.v110.DescribeFeatureTypeType;
import org.geotoolkit.wfs.xml.v110.FeatureCollectionType;
import org.geotoolkit.wfs.xml.v110.GetCapabilitiesType;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.InsertElementType;
import org.geotoolkit.wfs.xml.v110.PropertyType;
import org.geotoolkit.wfs.xml.v110.QueryType;
import org.geotoolkit.wfs.xml.v110.TransactionResponseType;
import org.geotoolkit.wfs.xml.v110.TransactionSummaryType;
import org.geotoolkit.wfs.xml.v110.TransactionType;
import org.geotoolkit.wfs.xml.v110.UpdateElementType;
import org.geotoolkit.wfs.xml.v110.ValueType;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.Source;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.configuration.AbstractConfigurator;
import org.geotoolkit.wfs.xml.StoredQueries;
import org.geotoolkit.wfs.xml.StoredQueryDescription;
import org.geotoolkit.wfs.xml.v200.ObjectFactory;
import org.geotoolkit.wfs.xml.v200.ParameterExpressionType;
import org.geotoolkit.wfs.xml.v200.QueryExpressionTextType;
import org.geotoolkit.wfs.xml.v200.StoredQueryDescriptionType;
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.xsd.xml.v2001.TopLevelComplexType;
import org.geotoolkit.xsd.xml.v2001.TopLevelElement;
import org.geotoolkit.xsd.xml.v2001.XSDMarshallerPool;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class WFSWorkerTest {

    private static MarshallerPool pool;
    private static WFSWorker worker ;

    private static DefaultDataSource ds = null;

    private static DefaultDataSource ds2 = null;

    private XmlFeatureWriter featureWriter;

    private static String EPSG_VERSION;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigurationEngine.setupTestEnvironement("WFSWorkerTest");

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

        pool = WFSMarshallerPool.getInstance();

        final List<StoredQueryDescription> descriptions = new ArrayList<>();
        final ParameterExpressionType param = new ParameterExpressionType("name", "name Parameter", "A parameter on the name of the feature", new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
        final List<QName> types = Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"));
        final org.geotoolkit.ogc.xml.v200.PropertyIsEqualToType pis = new org.geotoolkit.ogc.xml.v200.PropertyIsEqualToType(new org.geotoolkit.ogc.xml.v200.LiteralType("$name"), "name", true);
        final org.geotoolkit.ogc.xml.v200.FilterType filter = new org.geotoolkit.ogc.xml.v200.FilterType(pis);
        final org.geotoolkit.wfs.xml.v200.QueryType query = new org.geotoolkit.wfs.xml.v200.QueryType(filter, types, "2.0.0");
        final QueryExpressionTextType queryEx = new QueryExpressionTextType("urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression", null, types);
        final ObjectFactory factory = new ObjectFactory();
        queryEx.getContent().add(factory.createQuery(query));
        final StoredQueryDescriptionType des1 = new StoredQueryDescriptionType("nameQuery", "Name query" , "filter on name for samplingPoint", param, queryEx);
        descriptions.add(des1);
        final StoredQueries queries = new StoredQueries(descriptions);
        ConfigurationEngine.storeConfiguration("WFS", "test1", "StoredQueries.xml", queries, pool);

        EPSG_VERSION = CRS.getVersion("EPSG").toString();
        initFeatureSource();
        worker = new DefaultWFSWorker("test1");
        worker.setLogLevel(Level.FINER);
        worker.setServiceUrl("http://geomatys.com/constellation/WS/");
        worker.setShiroAccessible(false);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ConfigurationEngine.shutdownTestEnvironement("WFSWorkerTest");
        if (ds != null) {
            ds.shutdown();
        }
        if (ds2 != null) {
            ds2.shutdown();
        }

        if (worker != null) {
            worker.destroy();
        }

        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
    }

    @Before
    public void setUp() throws Exception {
        featureWriter = new JAXPStreamFeatureWriter();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * test the feature marshall
     *
     */
    @Test
    @Order(order=1)
    public void getCapabilitiesTest() throws Exception {
        final Marshaller marshaller = pool.acquireMarshaller();

        AcceptVersionsType acceptVersion = new AcceptVersionsType("1.1.0");
        SectionsType sections       = new SectionsType("featureTypeList");
        GetCapabilitiesType request = new GetCapabilitiesType(acceptVersion, sections, null, null, "WFS");

        WFSCapabilities result = worker.getCapabilities(request);


        StringWriter sw = new StringWriter();
        marshaller.marshal(result, sw);
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities1-1-0-ftl.xml"),
                sw.toString());

        
        request = new GetCapabilitiesType("WFS");
        result = worker.getCapabilities(request);

        sw = new StringWriter();
        marshaller.marshal(result, sw);
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities1-1-0.xml"),
                sw.toString());

        acceptVersion = new AcceptVersionsType("2.3.0");
        request = new GetCapabilitiesType(acceptVersion, null, null, null, "WFS");

        try {
            worker.getCapabilities(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), VERSION_NEGOTIATION_FAILED);
            assertEquals(ex.getLocator(), "version");
        }

        request = new GetCapabilitiesType(acceptVersion, null, null, null, "WPS");

        try {
            worker.getCapabilities(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "service");
        }

        request = new GetCapabilitiesType(null);

        try {
            worker.getCapabilities(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "service");
        }

        acceptVersion = new AcceptVersionsType("1.1.0");
        sections      = new SectionsType("operationsMetadata");
        request       = new GetCapabilitiesType(acceptVersion, sections, null, null, "WFS");

        result = worker.getCapabilities(request);


        sw = new StringWriter();
        marshaller.marshal(result, sw);
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities1-1-0-om.xml"),
                sw.toString());

        acceptVersion = new AcceptVersionsType("1.1.0");
        sections      = new SectionsType("serviceIdentification");
        request       = new GetCapabilitiesType(acceptVersion, sections, null, null, "WFS");

        result = worker.getCapabilities(request);


        sw = new StringWriter();
        marshaller.marshal(result, sw);
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities1-1-0-si.xml"),
                sw.toString());

        acceptVersion = new AcceptVersionsType("1.1.0");
        sections      = new SectionsType("serviceProvider");
        request       = new GetCapabilitiesType(acceptVersion, sections, null, null, "WFS");

        result = worker.getCapabilities(request);


        sw = new StringWriter();
        marshaller.marshal(result, sw);
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities1-1-0-sp.xml"),
                sw.toString());

        pool.recycle(marshaller);
    }

    /**
     * test the Getfeature operations with bad parameter causing exception return
     *
     */
    @Test
    @Order(order=2)
    public void getFeatureErrorTest() throws Exception {
        /**
         * Test 1 : empty query => error
         */
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, null, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        Object result = null;
        try {
            result = worker.getFeature(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            //ok
        }

        /**
         * Test 2 : bad version => error
         */
        request = new GetFeatureType("WFS", "1.2.0", null, Integer.MAX_VALUE, null, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        try {
            result = worker.getFeature(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "version");
        }
    }

     /**
     * test the feature marshall
     *
     */
    @Test
    @Order(order=3)
    public void getFeatureOMTest() throws Exception {

        /**
         * Test 1 : query on typeName samplingPoint
         */
        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-3.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);
        
        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);

        /**
         * Test 2 : query on typeName samplingPoint whith HITS result type
         */
        queries = new ArrayList<>();
        QueryType query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.HITS, "text/gml; subtype=gml/3.1.1");

        FeatureCollectionType resultHits = (FeatureCollectionType) worker.getFeature(request);

        assertTrue(resultHits.getNumberOfFeatures() == 6);


        /**
         * Test 3 : query on typeName samplingPoint with propertyName = {gml:name}
         */
        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        query.getPropertyNameOrXlinkPropertyNameOrFunction().add("gml:name");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-5.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);


        /**
         * Test 4 : query on typeName samplingPoint whith a filter name = 10972X0137-PONT
         */
        queries = new ArrayList<>();
        ComparisonOpsType pe = new PropertyIsEqualToType(new LiteralType("10972X0137-PONT"), new PropertyNameType("name"), Boolean.TRUE);
        FilterType filter = new FilterType(pe);
        queries.add(new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-4.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);

        /**
         * Test 5 : query on typeName samplingPoint whith a filter xpath //gml:name = 10972X0137-PONT
         */
        queries = new ArrayList<>();
        pe = new PropertyIsEqualToType(new LiteralType("10972X0137-PONT"), new PropertyNameType("//{http://www.opengis.net/gml}name"), Boolean.TRUE);
        filter = new FilterType(pe);
        queries.add(new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-4.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);

        /**
         * Test 6 : query on typeName samplingPoint whith a spatial filter BBOX
         */
        queries = new ArrayList<>();
        SpatialOpsType bbox = new BBOXType("{http://www.opengis.net/sampling/1.0}position", 65300.0, 1731360.0, 65500.0, 1731400.0, "urn:ogc:def:crs:epsg:7.6:27582");
        filter = new FilterType(bbox);
        queries.add(new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

       result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-8.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);

        /**
         * Test 7 : query on typeName samplingPoint whith a spatial filter BBOX () with no namespace
         */
        queries = new ArrayList<>();
        bbox = new BBOXType("position", 65300.0, 1731360.0, 65500.0, 1731400.0, "urn:ogc:def:crs:epsg:7.6:27582");
        filter = new FilterType(bbox);
        queries.add(new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-8.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);


        /**
         * Test 8 : query on typeName samplingPoint with sort on gml:name
         */

        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        query.setSortBy(new SortByType(Arrays.asList(new SortPropertyType("http://www.opengis.net/gml:name", SortOrderType.ASC))));
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-6.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);


        /**
         * Test 9 : query on typeName samplingPoint with sort on gml:name
         */
        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        query.setSortBy(new SortByType(Arrays.asList(new SortPropertyType("http://www.opengis.net/gml:name", SortOrderType.DESC))));
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-7.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);
        
        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 10 : query on typeName samplingPoint whith HITS result type
         */
        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.HITS, "text/gml; subtype=gml/3.1.1");

        resultHits = (FeatureCollectionType) worker.getFeature(request);

        assertTrue(resultHits.getNumberOfFeatures() == 6);


        /**
         * Test 11 : query on typeName samplingPoint whith a filter with unexpected property
         */

        queries = new ArrayList<>();
        pe = new PropertyIsEqualToType(new LiteralType("whatever"), new PropertyNameType("wrongProperty"), Boolean.TRUE);
        filter = new FilterType(pe);
        queries.add(new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        try {
            worker.getFeature(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            //ok
        }

        /**
         * Test 12 : query on typeName samplingPoint whith a an unexpected property in propertyNames
         */

        queries = new ArrayList<>();
        query = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        query.getPropertyNameOrXlinkPropertyNameOrFunction().add("wrongProperty");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        try {
            worker.getFeature(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * test the feature marshall
     *
     */
    @Test
    @Order(order=4)
    public void getFeatureSMLTest() throws Exception {

        /**
         * Test 1 : query on typeName sml:System
         */

        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null));
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-3.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);

        /**
         * Test 2 : query on typeName sml:System avec srsName = EPSG:4326
         */

        queries = new ArrayList<>();
        QueryType query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);
        query.setSrsName("urn:ogc:def:crs:epsg:EPSG:27582");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-1.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);
        
        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);
        /**
         * Test 3 : query on typeName sml:System with propertyName = {sml:keywords, sml:phenomenons}
         */

        queries = new ArrayList<>();
        query   = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);
        query.getPropertyNameOrXlinkPropertyNameOrFunction().add("sml:keywords");
        query.getPropertyNameOrXlinkPropertyNameOrFunction().add("sml:phenomenons");
        queries.add(query);

        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);
        
        /**
         * Test 4 : query on typeName sml:System filter on name = 'Piezometer Test'
         */
        ComparisonOpsType pe1     = new PropertyIsEqualToType(new LiteralType("Piezometer Test"), new PropertyNameType("name"), Boolean.TRUE);
        FilterType filter         = new FilterType(pe1);
        queries = new ArrayList<>();
        query   = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);

        queries.add(query);

        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.1.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-4.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);
        
        /**
         * Test 5 : same test xpath style
         */
        pe1     = new PropertyIsEqualToType(new LiteralType("Piezometer Test"), new PropertyNameType("/name"), Boolean.TRUE);
        filter         = new FilterType(pe1);
        queries = new ArrayList<>();
        query   = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);

        queries.add(query);

        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.1.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-4.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);
        
        /**
         * Test 6 : query on typeName sml:System filter on input name = 'height' (xpath style)
         */
        pe1     = new PropertyIsEqualToType(new LiteralType("height"), new PropertyNameType("/inputs/input/name"), Boolean.TRUE);
        filter         = new FilterType(pe1);
        queries = new ArrayList<>();
        query   = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);

        queries.add(query);

        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.1.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-5.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(expectedResult, sresult);
        
        /**
         * Test 7 : query on typeName sml:System with bad xpath NOT WORKING
        
        pe1     = new PropertyIsEqualToType(new LiteralType("height"), new PropertyNameType("/inputs/inputation/namein"), Boolean.TRUE);
        filter         = new FilterType(pe1);
        queries = new ArrayList<>();
        query   = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);

        queries.add(query);

        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.1.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-5.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        domCompare(expectedResult, writer.toString());
         */
        
        /**
         * Test 8 : query on typeName sml:System filter on input name = 'height' (xpath style) prefixed with featureType name
         */
        pe1     = new PropertyIsEqualToType(new LiteralType("height"), new PropertyNameType("{http://www.opengis.net/sml/1.0}System/inputs/input/name"), Boolean.TRUE);
        filter         = new FilterType(pe1);
        queries = new ArrayList<>();
        query   = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);

        queries.add(query);

        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.1.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-5.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);
        
        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);
    }

    /**
     * test the feature marshall
     *
     */
    @Test
    @Order(order=5)
    public void getFeatureShapeFileTest() throws Exception {

        /**
         * Test 1 : query on typeName bridges
         */

        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "Bridges")), null));
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.bridgeCollection.xml"),
                sresult);

        /**
         * Test 2 : query on typeName bridges with propertyName = {FID}
         */
        queries = new ArrayList<>();
        QueryType query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "Bridges")), null);
        query.getPropertyNameOrXlinkPropertyNameOrFunction().add("FID");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.bridgeCollection-2.xml"),
                sresult);

        /**
         * Test 3 : query on typeName NamedPlaces
         */

        queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "NamedPlaces")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-1.xml"),
                sresult);

        /**
         * Test 4 : query on typeName NamedPlaces with resultType = HITS
         */

        queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "NamedPlaces")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.HITS, "text/gml; subtype=gml/3.1.1");

        FeatureCollectionType resultHits = (FeatureCollectionType) worker.getFeature(request);worker.getFeature(request);

        assertTrue(resultHits.getNumberOfFeatures() == 2);

        /**
         * Test 5 : query on typeName NamedPlaces with srsName = EPSG:27582
         */

        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "NamedPlaces")), null);
        query.setSrsName("EPSG:27582");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-1_reproj.xml"),
                sresult);

        /**
         * Test 6 : query on typeName NamedPlaces with DESC sortBy on NAME property (not supported)
         */

        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "NamedPlaces")), null);
        query.setSortBy(new SortByType(Arrays.asList(new SortPropertyType("NAME", SortOrderType.DESC))));
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);
        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);
        
        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-5.xml"),
                sresult);
        

        /**
         * Test 7 : query on typeName NamedPlaces with ASC sortBy on NAME property (not supported)
         */
        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "NamedPlaces")), null);
        query.setSortBy(new SortByType(Arrays.asList(new SortPropertyType("NAME", SortOrderType.ASC))));
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);
        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);
        
        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-1.xml"),
                sresult);
        

    }

    /**
     *
     *
     */
    @Test
    @Order(order=6)
    public void DescribeFeatureTest() throws Exception {
        Unmarshaller unmarshaller = XSDMarshallerPool.getInstance().acquireUnmarshaller();

        /**
         * Test 1 : describe Feature type bridges
         */
        List<QName> typeNames = new ArrayList<>();
        typeNames.add(new QName("http://www.opengis.net/gml", "Bridges"));
        DescribeFeatureTypeType request = new DescribeFeatureTypeType("WFS", "1.1.0", null, typeNames, "text/gml; subtype=gml/3.1.1");

        Schema result = worker.describeFeatureType(request);

        Schema ExpResult = (Schema) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/wfs/xsd/bridge.xsd"));

        assertEquals(ExpResult, result);

        /**
         * Test 2 : describe Feature type Sampling point
         */
        typeNames = new ArrayList<>();
        typeNames.add(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"));
        request = new DescribeFeatureTypeType("WFS", "1.1.0", null, typeNames, "text/gml; subtype=gml/3.1.1");

        result = worker.describeFeatureType(request);

        ExpResult = (Schema) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/wfs/xsd/sampling.xsd"));

        assertEquals(ExpResult, result);

        /**
         * Test 3 : describe Feature type System
         */
        typeNames = new ArrayList<>();
        typeNames.add(new QName("http://www.opengis.net/sml/1.0", "System"));
        request = new DescribeFeatureTypeType("WFS", "1.1.0", null, typeNames, "text/gml; subtype=gml/3.1.1");

        result = worker.describeFeatureType(request);

        ExpResult = (Schema) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/wfs/xsd/system.xsd"));

        assertEquals(ExpResult.getElements().size(), result.getElements().size());
        for (int i = 0; i < ExpResult.getElements().size(); i++) {
            TopLevelElement expElem = ExpResult.getElements().get(i);
            TopLevelElement resElem = result.getElements().get(i);
            assertEquals(expElem, resElem);
        }
        assertEquals(ExpResult.getComplexTypes().size(), result.getComplexTypes().size());
        for (int i = 0; i < ExpResult.getComplexTypes().size(); i++) {
            TopLevelComplexType expElem = ExpResult.getComplexTypes().get(i);
            TopLevelComplexType resElem = result.getComplexTypes().get(i);
            assertEquals(expElem, resElem);
        }

        assertEquals(ExpResult, result);

        XSDMarshallerPool.getInstance().recycle(unmarshaller);
    }

    /**
     *
     *
     */
    @Test
    @Order(order=7)
    public void TransactionUpdateTest() throws Exception {

        /**
         * Test 1 : transaction update for Feature type bridges with a bad inputFormat
         */

        QName typeName = new QName("http://www.opengis.net/gml", "Bridges");
        List<PropertyType> properties = new ArrayList<>();
        UpdateElementType update = new UpdateElementType(properties, null, typeName, null);
        update.setInputFormat("bad inputFormat");
        TransactionType request = new TransactionType("WFS", "1.1.0", null, AllSomeType.ALL, update);


        try {
            worker.transaction(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "inputFormat");
        }


        /**
         * Test 2 : transaction update for Feature type bridges with a bad property
         */

        typeName = new QName("http://www.opengis.net/gml", "Bridges");
        properties = new ArrayList<>();
        properties.add(new PropertyType(new QName("whatever"), new ValueType("someValue")));
        request = new TransactionType("WFS", "1.1.0", null, AllSomeType.ALL, new UpdateElementType(properties, null, typeName, null));


        try {
            worker.transaction(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_VALUE);
            assertEquals(ex.getMessage(), "The feature Type {http://www.opengis.net/gml}Bridges does not has such a property: whatever");
        }


        /**
         * Test 3 : transaction update for Feature type bridges with a bad property in filter
         */

        typeName = new QName("http://www.opengis.net/gml", "Bridges");
        properties = new ArrayList<>();
        properties.add(new PropertyType(new QName("NAME"), new ValueType("someValue")));
        ComparisonOpsType pe     = new PropertyIsEqualToType(new LiteralType("10972X0137-PONT"), new PropertyNameType("bad"), Boolean.TRUE);
        FilterType filter        = new FilterType(pe);
        request = new TransactionType("WFS", "1.1.0", null, AllSomeType.ALL, new UpdateElementType(properties, filter, typeName, null));


        try {
            worker.transaction(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getMessage(), "The feature Type {http://www.opengis.net/gml}Bridges does not has such a property: bad");
        }

        /**
         * Test 4 : transaction update for Feature type NamedPlaces with a property in filter
         */

        typeName = new QName("http://www.opengis.net/gml", "NamedPlaces");
        properties = new ArrayList<>();
        properties.add(new PropertyType(new QName("FID"), new ValueType("999")));
        pe     = new PropertyIsEqualToType(new LiteralType("Ashton"), new PropertyNameType("NAME"), Boolean.TRUE);
        filter = new FilterType(pe);
        request = new TransactionType("WFS", "1.1.0", null, AllSomeType.ALL, new UpdateElementType(properties, filter, typeName, null));


        TransactionResponse result = worker.transaction(request);

        TransactionSummaryType sum = new TransactionSummaryType(0, 1, 0);
        TransactionResponseType ExpResult = new TransactionResponseType(sum, null, null, "1.1.0");

        assertEquals(ExpResult, result);

        /**
         * we verify that the feature have been updated
         */
         List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "NamedPlaces")), null));
        GetFeatureType requestGF = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        Object resultGF = worker.getFeature(requestGF);

        assertTrue(resultGF instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) resultGF;
        resultGF = wrapper.getFeatureCollection();

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)resultGF,writer);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-3.xml"),
                sresult);

    }

    @Test
    @Order(order=8)
    public void TransactionDeleteTest() throws Exception {

        /**
         * Test 1 : transaction delete for Feature type bridges with a bad property in filter
         */
        QName typeName           = new QName("http://www.opengis.net/gml", "Bridges");
        ComparisonOpsType pe     = new PropertyIsEqualToType(new LiteralType("10972X0137-PONT"), new PropertyNameType("bad"), Boolean.TRUE);
        FilterType filter        = new FilterType(pe);
        DeleteElementType delete = new DeleteElementType(filter, null, typeName);
        TransactionType request  = new TransactionType("WFS", "1.1.0", null, AllSomeType.ALL, delete);

        try {
            worker.transaction(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getMessage(), "The feature Type {http://www.opengis.net/gml}Bridges does not has such a property: bad");
        }


        /**
         * Test 2 : transaction delete for Feature type NamedPlaces with a property in filter
         */
        typeName = new QName("http://www.opengis.net/gml", "NamedPlaces");
        pe       = new PropertyIsEqualToType(new LiteralType("Ashton"), new PropertyNameType("NAME"), Boolean.TRUE);
        filter   = new FilterType(pe);
        delete   = new DeleteElementType(filter, null, typeName);
        request  = new TransactionType("WFS", "1.1.0", null, AllSomeType.ALL, delete);

        TransactionResponse result = worker.transaction(request);

        TransactionSummaryType sum = new TransactionSummaryType(0, 0, 1);
        TransactionResponseType expresult = new TransactionResponseType(sum, null, null, "1.1.0");

        assertEquals(expresult, result);

        /**
         * we verify that the feature have been deleted
         */
        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "NamedPlaces")), null));
        GetFeatureType requestGF = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        Object resultGF = worker.getFeature(requestGF);

        assertTrue(resultGF instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) resultGF;
        resultGF = wrapper.getFeatureCollection();

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)resultGF,writer);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-2.xml"),
                sresult);
    }
    /**
     *
     *
     */
    @Test
    @Order(order=9)
    public void TransactionInsertTest() throws Exception {

        /**
         * Test 1 : transaction insert for Feature type bridges with a bad inputFormat
         */

        final QName typeName = new QName("http://www.opengis.net/gml", "Bridges");
        final InsertElementType insert = new InsertElementType();
        insert.setInputFormat("bad inputFormat");
        final TransactionType request = new TransactionType("WFS", "1.1.0", null, AllSomeType.ALL, insert);

        try {
            worker.transaction(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "inputFormat");
        }
    }
    
    @Test
    @Order(order=10)
    public void schemaLocationTest() throws Exception {
        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "NamedPlaces")), null));
        GetFeatureType requestGF = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        Object resultGF = worker.getFeature(requestGF);

        assertTrue(resultGF instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) resultGF;
        
        final Map<String, String> expResult = new HashMap<>();
        expResult.put("http://www.opengis.net/gml", "http://geomatys.com/constellation/WS/wfs/test1?request=DescribeFeatureType&version=1.1.0&service=WFS&namespace=xmlns(ns1=http://www.opengis.net/gml)&typename=ns1:NamedPlaces");
        assertEquals(wrapper.getSchemaLocations(), expResult);
        
    }

    private static void initFeatureSource() throws Exception {
         final File outputDir = initDataDirectory();

         final Configurator config = new AbstractConfigurator() {

             @Override
             public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
                 
                 final ArrayList<Map.Entry<String, ParameterValueGroup>> lst = new ArrayList<>();
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("feature-store");
                
                try{ 
                    
                    {//OBSERVATION
                        final String url = "jdbc:derby:memory:TestWFSWorkerOM";
                        ds = new DefaultDataSource(url + ";create=true");
                        Connection con = ds.getConnection();
                        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                        sr.run(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
                        sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));
                        con.close();
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
                        getOrCreateValue(shpconfig, "url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles"));
                        getOrCreateValue(shpconfig, "namespace").setValue("http://www.opengis.net/gml");        
                        
                        ParameterValueGroup layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("BasicPolygons");
                        getOrCreateValue(layer, "style").setValue("cite_style_BasicPolygons");     
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("Bridges");
                        getOrCreateValue(layer, "style").setValue("cite_style_Bridges");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("BuildingCenters");
                        getOrCreateValue(layer, "style").setValue("cite_style_BuildingCenters");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("Buildings");
                        getOrCreateValue(layer, "style").setValue("cite_style_Buildings");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("DividedRoutes");
                        getOrCreateValue(layer, "style").setValue("cite_style_DividedRoutes");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("Forests");
                        getOrCreateValue(layer, "style").setValue("cite_style_Forests");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("Lakes");
                        getOrCreateValue(layer, "style").setValue("cite_style_Lakes");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("MapNeatline");
                        getOrCreateValue(layer, "style").setValue("cite_style_MapNeatLine");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("NamedPlaces");
                        getOrCreateValue(layer, "style").setValue("cite_style_NamedPlaces");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("Ponds");
                        getOrCreateValue(layer, "style").setValue("cite_style_Ponds");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("RoadSegments");
                        getOrCreateValue(layer, "style").setValue("cite_style_RoadSegments");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("Streams");
                        getOrCreateValue(layer, "style").setValue("cite_style_Streams"); 
                        
                        lst.add(new AbstractMap.SimpleImmutableEntry<>("shapeSrc",source));
                    }

                    {//SENSORML
                        final String url2 = "jdbc:derby:memory:TestWFSWorkerSMl";
                        ds2 = new DefaultDataSource(url2 + ";create=true");
                        Connection con = ds2.getConnection();
                        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19119.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19108.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/data/defaultRecordSets.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/users/creation_user.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/SensorML.sql"));
                        sr.run(Util.getResourceAsStream("org/constellation/sql/sml-data.sql"));
                        con.close();
                            
                        final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                        getOrCreateValue(source, "id").setValue("smlSrc");
                        getOrCreateValue(source, "load_all").setValue(true);             
                        
                        final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                        final ParameterValueGroup omconfig = createGroup(choice, "SMLParameters");
                        getOrCreateValue(omconfig, "sgbdtype").setValue("derby");
                        getOrCreateValue(omconfig, "derbyurl").setValue(url2);                      
                        
                        lst.add(new AbstractMap.SimpleImmutableEntry<>("smlSrc",source));                   
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

        DataProviders.getInstance().setConfigurator(config);
    }
    /**
     * Initialises the data directory in unzipping the jar containing the resources
     * into a temporary directory.
     *
     * @return The root output directory where the data are unzipped.
     * @throws IOException
     */
    private static File initDataDirectory() throws IOException {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final String stylePath = "org/constellation/ws/embedded/wms111/styles";
        String styleResource = classloader.getResource(stylePath).getFile();

        if (styleResource.indexOf('!') != -1) {
            styleResource = styleResource.substring(0, styleResource.indexOf('!'));
        }
        if (styleResource.startsWith("file:")) {
            styleResource = styleResource.substring(5);
        }

        File styleJar = new File(styleResource);
        if (!styleJar.exists()) {
            throw new IOException("Unable to find the style folder: "+ styleJar);
        }
        if (styleJar.isDirectory()) {
            styleJar = new File(styleJar.getPath().replaceAll(stylePath, ""));
            return styleJar;
        }
        final InputStream in = new FileInputStream(styleJar);
        final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        final File outputDir = new File(tmpDir, "Constellation");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        IOUtilities.unzip(in, outputDir);
        in.close();
        return outputDir;
    }


    public static void domCompare(final Object actual, final Object expected) throws Exception {

        final CstlDOMComparator comparator = new CstlDOMComparator(expected, actual);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }
}
