/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

import javax.xml.bind.JAXBException;
import org.constellation.wfs.ws.WFSWorker;
import org.constellation.wfs.ws.DefaultWFSWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.provider.om.OMProvider;
import org.constellation.provider.om.OMProviderService;
import org.constellation.provider.shapefile.ShapeFileProvider;
import org.constellation.provider.shapefile.ShapeFileProviderService;
import org.constellation.provider.sml.SMLProvider;
import org.constellation.provider.sml.SMLProviderService;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.ogc.xml.v110.ComparisonOpsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.LiteralType;
import org.geotoolkit.ogc.xml.v110.PropertyIsEqualToType;
import org.geotoolkit.ogc.xml.v110.PropertyNameType;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.geotoolkit.ogc.xml.v110.SortOrderType;
import org.geotoolkit.ogc.xml.v110.SortPropertyType;
import org.geotoolkit.ows.xml.v100.AcceptVersionsType;
import org.geotoolkit.ows.xml.v100.SectionsType;
import org.geotoolkit.wfs.xml.v110.DescribeFeatureTypeType;
import org.geotoolkit.wfs.xml.v110.GetCapabilitiesType;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.QueryType;
import org.geotoolkit.wfs.xml.v110.ResultTypeType;
import org.geotoolkit.wfs.xml.v110.WFSCapabilitiesType;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.geotoolkit.xsd.xml.v2001.Schema;
import org.junit.*;
import org.opengis.filter.sort.SortOrder;
import static org.junit.Assert.*;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WFSWorkerTest {

    private static MarshallerPool pool;
    static {
        try {
            pool = new MarshallerPool("org.geotoolkit.wfs.xml.v110" +
            		  ":org.geotoolkit.ogc.xml.v110" +
            		  ":org.geotoolkit.gml.xml.v311" +
                          ":org.geotoolkit.xsd.xml.v2001");
        } catch (Exception ex) {
            
        }

    }
    private static WFSWorker worker ;
    static {
        try {

            worker = new DefaultWFSWorker(pool);
            worker.setLogLevel(Level.FINER);
        } catch (Exception ex) {
            
        }
    }

    

    private static DefaultDataSource ds = null;

    private static DefaultDataSource ds2 = null;
    
    private XmlFeatureWriter featureWriter;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        initFeatureSource();
    }

    

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (ds != null) {
            ds.shutdown();
        }
        if (ds2 != null) {
            ds2.shutdown();
        }
    }

    @Before
    public void setUp() throws Exception {
        featureWriter     = new JAXPStreamFeatureWriter();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * test the feature marshall
     *
     */
    @Test
    public void getCapabilitiesTest() throws JAXBException, CstlServiceException, IOException {
        Marshaller marshaller = pool.acquireMarshaller();

        GetCapabilitiesType request = new GetCapabilitiesType("WFS");
        WFSCapabilitiesType result = worker.getCapabilities(request);

        StringWriter sw = new StringWriter();
        marshaller.marshal(result, sw);
        String xmlResult    = removeXmlns(sw.toString());
        String xmlExpResult = removeXmlns(Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities1-1-0.xml")));

        assertEquals(xmlExpResult, xmlResult);

        AcceptVersionsType acceptVersion = new AcceptVersionsType("2.3.0");
        request = new GetCapabilitiesType(acceptVersion, null, null, null, "WFS");

        boolean exlaunched = false;
        try {
            result = worker.getCapabilities(request);
        } catch (CstlServiceException ex) {
            exlaunched = true;
            assertEquals(ex.getExceptionCode(), VERSION_NEGOTIATION_FAILED);
            assertEquals(ex.getLocator(), "version");

        }
        assertTrue(exlaunched);

        request = new GetCapabilitiesType(acceptVersion, null, null, null, "WPS");

        exlaunched = false;
        try {
            result = worker.getCapabilities(request);
        } catch (CstlServiceException ex) {
            exlaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "service");

        }
        assertTrue(exlaunched);

        request = new GetCapabilitiesType(null);

        exlaunched = false;
        try {
            result = worker.getCapabilities(request);
        } catch (CstlServiceException ex) {
            exlaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "service");

        }
        assertTrue(exlaunched);


        acceptVersion = new AcceptVersionsType("1.1.0");
        SectionsType sections = new SectionsType("featureTypeList");
        request       = new GetCapabilitiesType(acceptVersion, sections, null, null, "WFS");

        result = worker.getCapabilities(request);


        sw = new StringWriter();
        marshaller.marshal(result, sw);
        xmlResult    = removeXmlns(sw.toString());
        xmlExpResult = removeXmlns(Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities1-1-0-ftl.xml")));

        assertEquals(xmlExpResult, xmlResult);

        acceptVersion = new AcceptVersionsType("1.1.0");
        sections      = new SectionsType("operationsMetadata");
        request       = new GetCapabilitiesType(acceptVersion, sections, null, null, "WFS");

        result = worker.getCapabilities(request);


        sw = new StringWriter();
        marshaller.marshal(result, sw);
        xmlResult    = removeXmlns(sw.toString());
        xmlExpResult = removeXmlns(Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities1-1-0-om.xml")));

        assertEquals(xmlExpResult, xmlResult);

        acceptVersion = new AcceptVersionsType("1.1.0");
        sections      = new SectionsType("serviceIdentification");
        request       = new GetCapabilitiesType(acceptVersion, sections, null, null, "WFS");

        result = worker.getCapabilities(request);


        sw = new StringWriter();
        marshaller.marshal(result, sw);
        xmlResult    = removeXmlns(sw.toString());
        xmlExpResult = removeXmlns(Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities1-1-0-si.xml")));

        assertEquals(xmlExpResult, xmlResult);

        acceptVersion = new AcceptVersionsType("1.1.0");
        sections      = new SectionsType("serviceProvider");
        request       = new GetCapabilitiesType(acceptVersion, sections, null, null, "WFS");

        result = worker.getCapabilities(request);


        sw = new StringWriter();
        marshaller.marshal(result, sw);
        xmlResult    = removeXmlns(sw.toString());
        xmlExpResult = removeXmlns(Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities1-1-0-sp.xml")));

        assertEquals(xmlExpResult, xmlResult);
    }

    /**
     * test the feature marshall
     *
     */
    @Test
    public void getFeatureTest() throws Exception {

        /**
         * Test 2 : empty query => error
         */
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, null, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        boolean exLaunched = false;
        Object result = null;
        try {
            result = worker.getFeature(request);

        } catch (CstlServiceException ex) {
            exLaunched = true;
        }

        assertTrue(exLaunched);

        request = new GetFeatureType("WFS", "1.2.0", null, Integer.MAX_VALUE, null, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        exLaunched = false;
        try {
            result = worker.getFeature(request);

        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "version");
        }

        assertTrue(exLaunched);

        
        /**
         * Test 2 : query on typeName bridges
         */

        List<QueryType> queries = new ArrayList<QueryType>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://geotoolkit.org", "Bridges")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        String xmlResult    = featureWriter.write((FeatureCollection)result);
        String xmlExpResult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.bridgeCollection.xml"));
        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");

        assertEquals(xmlExpResult, xmlResult);

         /**
         * Test 3 : query on typeName bridges with propertyName = {FID}
         */

        queries = new ArrayList<QueryType>();
        QueryType query = new QueryType(null, Arrays.asList(new QName("http://geotoolkit.org", "Bridges")), null);
        query.getPropertyNameOrXlinkPropertyNameOrFunction().add("FID");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        xmlResult    = featureWriter.write((FeatureCollection)result);
        xmlExpResult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.bridgeCollection-2.xml"));

        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");

        assertEquals(xmlExpResult, xmlResult);

        /**
         * Test 4 : query on typeName NamedPlaces
         */

        queries = new ArrayList<QueryType>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://geotoolkit.org", "NamedPlaces")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        xmlResult    = featureWriter.write((FeatureCollection)result);
        xmlExpResult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-1.xml"));
        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");

        assertEquals(xmlExpResult, xmlResult);

        /**
         * Test 5 : query on typeName NamedPlaces with srsName = EPSG:27582
         */

        queries = new ArrayList<QueryType>();
        query = new QueryType(null, Arrays.asList(new QName("http://geotoolkit.org", "NamedPlaces")), null);
        query.setSrsName("EPSG:27582");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        xmlResult    = featureWriter.write((FeatureCollection)result);
        xmlExpResult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-1.xml"));
        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");

        assertEquals(xmlExpResult, xmlResult);

        /**
         * Test 5 : query on typeName NamedPlaces with DESC sortBy on NAME property
         

        queries = new ArrayList<QueryType>();
        query = new QueryType(null, Arrays.asList(new QName("http://geotoolkit.org", "NamedPlaces")), null);
        query.setSortBy(new SortByType(Arrays.asList(new SortPropertyType("NAME", SortOrderType.DESC))));
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        xmlResult    = featureWriter.write((FeatureCollection)result);
        xmlExpResult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-1.xml"));
        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");

        assertEquals(xmlExpResult, xmlResult);
       */

        /**
         * Test 6 : query on typeName samplingPoint
         */

        queries = new ArrayList<QueryType>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        xmlResult    = featureWriter.write((FeatureCollection)result);
        xmlExpResult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-3.xml"));
        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");

        assertEquals(xmlExpResult, xmlResult);

        /**
         * Test 7 : query on typeName samplingPoint with propertyName = {gml:name}
         */

        queries = new ArrayList<QueryType>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        query.getPropertyNameOrXlinkPropertyNameOrFunction().add("gml:name");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        xmlResult    = featureWriter.write((FeatureCollection)result);
        xmlExpResult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-5.xml"));
        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");

        assertEquals(xmlExpResult, xmlResult);

        /**
         * Test 8 : query on typeName sml:System
         */

        queries = new ArrayList<QueryType>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        xmlResult    = featureWriter.write((FeatureCollection)result);
        xmlExpResult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.systemCollection-1.xml"));

        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");

        // to see
        xmlExpResult = xmlExpResult.replaceAll("srsName=\"\"", "srsName=\"urn:ogc:def:crs:epsg:7.4:27582\"");

        assertEquals(xmlExpResult, xmlResult);

        /**
         * Test 9 : query on typeName sml:System avec srsName = EPSG:4326
         */

        queries = new ArrayList<QueryType>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);
        query.setSrsName("EPSG:4326");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        xmlResult    = featureWriter.write((FeatureCollection)result);
        xmlExpResult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.systemCollection-1.xml"));

        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");

        // to see
        xmlExpResult = xmlExpResult.replaceAll("srsName=\"\"", "srsName=\"urn:ogc:def:crs:epsg:7.4:27582\"");

        assertEquals(xmlExpResult, xmlResult);

        /**
         * Test 10 : query on typeName sml:System with propertyName = {sml:keywords, sml:phenomenons}
         */

        queries = new ArrayList<QueryType>();
        query   = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);
        query.getPropertyNameOrXlinkPropertyNameOrFunction().add("sml:keywords");
        query.getPropertyNameOrXlinkPropertyNameOrFunction().add("sml:phenomenons");
        queries.add(query);

        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        xmlResult    = featureWriter.write((FeatureCollection)result);
        xmlExpResult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.systemCollection-2.xml"));

        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");

        // to see
        xmlExpResult = xmlExpResult.replaceAll("srsName=\"\"", "srsName=\"urn:ogc:def:crs:epsg:7.4:27582\"");

        assertEquals(xmlExpResult, xmlResult);

        /**
         * Test 11 : query on typeName samplingPoint whith a filter name = 10972X0137-PONT
         */

        queries = new ArrayList<QueryType>();
        ComparisonOpsType pe = new PropertyIsEqualToType(new LiteralType("10972X0137-PONT"), new PropertyNameType("name"), Boolean.TRUE);
        FilterType filter = new FilterType(pe);
        queries.add(new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        xmlResult    = featureWriter.write((FeatureCollection)result);
        xmlExpResult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-4.xml"));
        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");

        assertEquals(xmlExpResult, xmlResult);

        /**
         * Test 12 : query on typeName samplingPoint with sort on gml:name
         

        queries = new ArrayList<QueryType>();
        query = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        query.setSortBy(new SortByType(Arrays.asList(new SortPropertyType("gml:name", SortOrderType.ASC))));
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        xmlResult    = featureWriter.write((FeatureCollection)result);
        xmlExpResult = Util.stringFromFile(Util.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection.xml"));
        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");
        xmlExpResult = xmlExpResult.replace("<?xml version='1.0'?>", "<?xml version='1.0' encoding='UTF-8'?>");
        xmlExpResult = xmlExpResult.replaceAll("> *<", "><");

        assertEquals(xmlExpResult, xmlResult);
        */
        
        /**
         * Test 13 : query on typeName samplingPoint whith a filter with unexpected property
         */

        queries = new ArrayList<QueryType>();
        pe = new PropertyIsEqualToType(new LiteralType("whatever"), new PropertyNameType("wrongProperty"), Boolean.TRUE);
        filter = new FilterType(pe);
        queries.add(new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        exLaunched = false;
        try {
            result = worker.getFeature(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
        }

        assertTrue(exLaunched);

        /**
         * Test 13 : query on typeName samplingPoint whith a an unexpected property in propertyNames
         */

        queries = new ArrayList<QueryType>();
        query = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        query.getPropertyNameOrXlinkPropertyNameOrFunction().add("wrongProperty");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        exLaunched = false;
        try {
            result = worker.getFeature(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }

        assertTrue(exLaunched);
        

    }

    /**
     * 
     *
     */
    @Test
    public void DescribeFeatureTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();

        /**
         * Test 1 : describe Feature type bridges
         */
        List<QName> typeNames = new ArrayList<QName>();
        typeNames.add(new QName("http://geotoolkit.org", "Bridges"));
        DescribeFeatureTypeType request = new DescribeFeatureTypeType("WFS", "1.1.0", null, typeNames, "text/gml; subtype=gml/3.1.1");

        Schema result = worker.describeFeatureType(request);

        Schema ExpResult = (Schema) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/wfs/xsd/bridge.xsd"));

        assertEquals(ExpResult, result);

        /**
         * Test 2 : describe Feature type Sampling point
         */
        typeNames = new ArrayList<QName>();
        typeNames.add(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"));
        request = new DescribeFeatureTypeType("WFS", "1.1.0", null, typeNames, "text/gml; subtype=gml/3.1.1");

        result = worker.describeFeatureType(request);

        ExpResult = (Schema) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/wfs/xsd/sampling.xsd"));

        assertEquals(ExpResult, result);

        /**
         * Test 3 : describe Feature type System
         */
        typeNames = new ArrayList<QName>();
        typeNames.add(new QName("http://www.opengis.net/sml/1.0", "System"));
        request = new DescribeFeatureTypeType("WFS", "1.1.0", null, typeNames, "text/gml; subtype=gml/3.1.1");

        result = worker.describeFeatureType(request);

        ExpResult = (Schema) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/wfs/xsd/system.xsd"));

        assertEquals(ExpResult, result);
    }


    private static void initFeatureSource() throws Exception {

         final File outputDir = initDataDirectory();

        /****************************************
         *                                      *
         * Defines a ShapeFile data provider    *
         *                                      *
         ****************************************/
        final ProviderSource sourceShape = new ProviderSource();
        sourceShape.loadAll = true;
        sourceShape.parameters.put(ShapeFileProvider.KEY_FOLDER_PATH, outputDir.getAbsolutePath() +
                "/org/constellation/ws/embedded/wms111/shapefiles");
        sourceShape.layers.add(new ProviderLayer("BasicPolygons", Collections.singletonList("cite_style_BasicPolygons"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("Bridges", Collections.singletonList("cite_style_Bridges"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("BuildingCenters", Collections.singletonList("cite_style_BuildingCenters"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("Buildings", Collections.singletonList("cite_style_Buildings"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("DividedRoutes", Collections.singletonList("cite_style_DividedRoutes"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("Forests", Collections.singletonList("cite_style_Forests"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("Lakes", Collections.singletonList("cite_style_Lakes"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("MapNeatline", Collections.singletonList("cite_style_MapNeatLine"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("NamedPlaces", Collections.singletonList("cite_style_NamedPlaces"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("Ponds", Collections.singletonList("cite_style_Ponds"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("RoadSegments", Collections.singletonList("cite_style_RoadSegments"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("Streams", Collections.singletonList("cite_style_Streams"),
                               null, null, null, null, false, null));


        final ProviderConfig configShape = new ProviderConfig();
        configShape.sources.add(sourceShape);

        for (LayerProviderService service : LayerProviderProxy.getInstance().getServices()) {
            // Here we should have the shapefile data provider defined previously
            if (service instanceof ShapeFileProviderService) {
                service.setConfiguration(configShape);
                if (service.getProviders().isEmpty()) {
                    return;
                }
                break;
            }
        }

        /****************************************
         *                                      *
         *    Defines a O&M data provider       *
         *                                      *
         ****************************************/

        final String url = "jdbc:derby:memory:TestWFSWorker";
        ds = new DefaultDataSource(url + ";create=true");

        Connection con = ds.getConnection();

        Util.executeSQLScript("org/constellation/sql/structure-observations.sql", con);
        Util.executeSQLScript("org/constellation/sql/sos-data.sql", con);

        con.close();
        
        final ProviderSource sourceOM = new ProviderSource();
        sourceOM.loadAll = true;
        sourceOM.parameters.put(OMProvider.KEY_SGBDTYPE, "derby");
        sourceOM.parameters.put(OMProvider.KEY_DERBYURL, url);

        final ProviderConfig configOM = new ProviderConfig();
        configOM.sources.add(sourceOM);

        for (LayerProviderService service : LayerProviderProxy.getInstance().getServices()) {
            // Here we should have the shapefile data provider defined previously
            if (service instanceof OMProviderService) {
                service.setConfiguration(configOM);
                if (service.getProviders().isEmpty()) {
                    return;
                }
                break;
            }
        }

        /****************************************
         *                                      *
         *    Defines a SML data provider       *
         *                                      *
         ****************************************/

        final String url2 = "jdbc:derby:memory:TestWFSWorkerSMl";
        ds2 = new DefaultDataSource(url2 + ";create=true");

        con = ds2.getConnection();

        Util.executeSQLScript("org/constellation/sql/structure-mdweb.sql", con);
        Util.executeSQLScript("org/constellation/sql/sml-schema.sql", con);
        Util.executeSQLScript("org/constellation/sql/sml-data.sql", con);


        con.close();

        final ProviderSource sourceSML = new ProviderSource();
        sourceSML.loadAll = true;
        sourceSML.parameters.put(SMLProvider.KEY_SGBDTYPE, "derby");
        sourceSML.parameters.put(SMLProvider.KEY_DERBYURL, url2);

        final ProviderConfig configSML = new ProviderConfig();
        configSML.sources.add(sourceSML);

        for (LayerProviderService service : LayerProviderProxy.getInstance().getServices()) {
            // Here we should have the shapefile data provider defined previously
            if (service instanceof SMLProviderService) {
                service.setConfiguration(configSML);
                if (service.getProviders().isEmpty()) {
                    return;
                }
                break;
            }
        }
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
        String styleResource = classloader.getResource("org/constellation/ws/embedded/wms111/styles").getFile();
        if (styleResource.indexOf('!') != -1) {
            styleResource = styleResource.substring(0, styleResource.indexOf('!'));
        }
        if (styleResource.startsWith("file:")) {
            styleResource = styleResource.substring(5);
        }
        final File styleJar = new File(styleResource);
        if (styleJar == null || !styleJar.exists()) {
            throw new IOException("Unable to find the style folder: "+ styleJar);
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

    public String removeXmlns(String xml) {

        String s = xml;
        s = s.replaceAll("xmlns=\"[^\"]*\" ", "");

        s = s.replaceAll("xmlns=\"[^\"]*\"", "");

        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\" ", "");

        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\"", "");


        return s;
    }
}
