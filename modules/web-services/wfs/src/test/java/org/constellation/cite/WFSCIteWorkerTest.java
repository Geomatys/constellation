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
package org.constellation.cite;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.xml.namespace.QName;

import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.provider.postgis.PostGisProvider;
import org.constellation.provider.postgis.PostGisProviderService;
import org.constellation.wfs.ws.WFSWorker;
import org.constellation.wfs.ws.DefaultWFSWorker;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.gml.xml.v311.MultiPointType;
import org.geotoolkit.gml.xml.v311.PointPropertyType;
import org.geotoolkit.gml.xml.v311.PointType;
import org.geotoolkit.ogc.xml.v110.EqualsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.wfs.xml.WFSMarshallerPool;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.QueryType;
import org.geotoolkit.wfs.xml.v110.ResultTypeType;
import org.geotoolkit.xml.MarshallerPool;

import org.junit.*;
import static org.junit.Assert.*;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WFSCIteWorkerTest {

    private static final WFSWorker worker;
    static {
        worker = new DefaultWFSWorker();
        worker.setLogLevel(Level.FINER);
    }

    private XmlFeatureWriter featureWriter;

    @BeforeClass
    public static void setUpClass() throws Exception {
        initFeatureSource();
    }



    @AfterClass
    public static void tearDownClass() throws Exception {
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
    public void getFeatureShapeFileTest() throws Exception {

        /**
         * Test 1 : query on typeName aggragateGeofeature
         */

        List<QueryType> queries = new ArrayList<QueryType>();
        List<PointPropertyType> points = new ArrayList<PointPropertyType>();
        points.add(new PointPropertyType(new PointType(null, new GeneralDirectPosition(70.83, 29.86))));
        points.add(new PointPropertyType(new PointType(null, new GeneralDirectPosition(68.87, 31.08))));
        points.add(new PointPropertyType(new PointType(null, new GeneralDirectPosition(71.96, 32.19))));
        
        EqualsType equals = new EqualsType("http://cite.opengeospatial.org/gmlsf:multiPointProperty", new MultiPointType("urn:x-ogc:def:crs:EPSG:4326", points));
        FilterType f = new FilterType(equals);
        queries.add(new QueryType(f, Arrays.asList(new QName("http://cite.opengeospatial.org/gmlsf", "AggregateGeoFeature")), "1.1.0"));
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollection);

        FeatureCollection collection = (FeatureCollection)result;

        StringWriter writer = new StringWriter();
        featureWriter.write(collection,writer);
        writer.flush();
        String xmlResult = writer.toString();
        assertEquals(1, collection.size());

        /**
         * Test 1 : query on typeName aggragateGeofeature
         

        queries = new ArrayList<QueryType>();
        BBOXType bbox = new BBOXType("http://cite.opengeospatial.org/gmlsf:pointProperty", 30, -12, 60, -6, "urn:x-ogc:def:crs:EPSG:4326");
        PropertyIsEqualToType propEqual = new PropertyIsEqualToType(new LiteralType("name-f015"), new PropertyNameType("http://www.opengis.net/gml:name"), Boolean.TRUE);
        AndType and = new AndType(bbox, propEqual);
        f = new FilterType(and);
        QueryType query = new QueryType(f, Arrays.asList(new QName("http://cite.opengeospatial.org/gmlsf", "PrimitiveGeoFeature")), "1.1.0");
        query.setSrsName("urn:x-ogc:def:crs:EPSG:6.11:32629");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollection);

        collection = (FeatureCollection)result;

        xmlResult    = featureWriter.write(collection);
        System.out.println(xmlResult);

        assertEquals(1, collection.size());
        */

        
         

    }



    private static void initFeatureSource() throws Exception {


        /****************************************
         *                                      *
         *    Defines a postgis provider       *
         *                                      *
         ****************************************/

        // Defines a PostGis data provider
        final ProviderSource sourcePostGis = new ProviderSource();
        sourcePostGis.parameters.put(PostGisProvider.KEY_DATABASE, "cite-wfs");
        sourcePostGis.parameters.put(PostGisProvider.KEY_HOST,     "db.geomatys.com");
        sourcePostGis.parameters.put(PostGisProvider.KEY_SCHEMA,   "public");
        sourcePostGis.parameters.put(PostGisProvider.KEY_USER,     "test");
        sourcePostGis.parameters.put(PostGisProvider.KEY_PASSWD,   "test");
        sourcePostGis.parameters.put(PostGisProvider.KEY_NAMESPACE,"http://cite.opengeospatial.org/gmlsf");

        final ProviderConfig configPostGis = new ProviderConfig();
        configPostGis.sources.add(sourcePostGis);
        sourcePostGis.loadAll = true;

        for (LayerProviderService service : LayerProviderProxy.getInstance().getServices()) {
            // Here we should have the postgis data provider defined previously
            if (service instanceof PostGisProviderService) {
                service.setConfiguration(configPostGis);
                if (service.getProviders().isEmpty()) {
                    return;
                }
                break;
            }
        }


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
