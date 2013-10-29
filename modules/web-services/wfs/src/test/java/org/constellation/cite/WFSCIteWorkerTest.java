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
import org.constellation.provider.configuration.Configurator;
import org.constellation.wfs.ws.WFSWorker;
import org.constellation.wfs.ws.DefaultWFSWorker;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.apache.sis.geometry.GeneralDirectPosition;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.Source;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderService;
import org.geotoolkit.gml.xml.v311.MultiPointType;
import org.geotoolkit.gml.xml.v311.PointPropertyType;
import org.geotoolkit.gml.xml.v311.PointType;
import org.geotoolkit.ogc.xml.v110.EqualsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.QueryType;
import org.geotoolkit.wfs.xml.ResultTypeType;

import org.junit.*;
import org.opengis.parameter.ParameterValueGroup;

import static org.junit.Assert.*;
import static org.constellation.provider.featurestore.FeatureStoreProviderService.*;
import static org.constellation.provider.configuration.ProviderParameters.*;
import org.constellation.wfs.ws.rs.FeatureCollectionWrapper;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.*;
import org.geotoolkit.db.postgres.PostgresFeatureStoreFactory;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WFSCIteWorkerTest {

    private static WFSWorker worker;

    private XmlFeatureWriter featureWriter;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigurationEngine.setupTestEnvironement("WFSCIteWorkerTest");

        final List<Source> sources = Arrays.asList(new Source("coverageTestSrc", true, null, null),
                                                   new Source("omSrc", true, null, null),
                                                   new Source("shapeSrc", true, null, null),
                                                   new Source("postgisSrc", true, null, null));
        final Layers layers = new Layers(sources);
        final LayerContext config = new LayerContext(layers);
        config.getCustomParameters().put("shiroAccessible", "false");
        config.getCustomParameters().put("transactionSecurized", "false");

        ConfigurationEngine.storeConfiguration("WFS", "default", config);

        initFeatureSource();
        worker = new DefaultWFSWorker("default");
        worker.setLogLevel(Level.FINER);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        LayerProviderProxy.getInstance().setConfigurator(Configurator.DEFAULT);
        ConfigurationEngine.shutdownTestEnvironement("WFSCIteWorkerTest");
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

        List<QueryType> queries = new ArrayList<>();
        List<PointPropertyType> points = new ArrayList<>();
        points.add(new PointPropertyType(new PointType(null, new GeneralDirectPosition(29.86, 70.83))));
        points.add(new PointPropertyType(new PointType(null, new GeneralDirectPosition(31.08, 68.87))));
        points.add(new PointPropertyType(new PointType(null, new GeneralDirectPosition(32.19, 71.96))));

        EqualsType equals = new EqualsType("http://cite.opengeospatial.org/gmlsf:multiPointProperty", new MultiPointType("urn:x-ogc:def:crs:EPSG:4326", points));
        FilterType f = new FilterType(equals);
        queries.add(new QueryType(f, Arrays.asList(new QName("http://cite.opengeospatial.org/gmlsf", "AggregateGeoFeature")), "1.1.0"));
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);

        FeatureCollection collection = ((FeatureCollectionWrapper)result).getFeatureCollection();

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

        final Configurator config = new Configurator() {

            @Override
            public ParameterValueGroup getConfiguration(final ProviderService service) {
                final ParameterValueGroup config = service.getServiceDescriptor().createValue();

                if("feature-store".equals(service.getName())){
                    // Defines a PostGis data provider
                    final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("postgisSrc");
                    
                    final ParameterValueGroup choice = getOrCreate(SOURCE_CONFIG_DESCRIPTOR,source);                    
                    final ParameterValueGroup pgconfig = getOrCreate(PostgresFeatureStoreFactory.PARAMETERS_DESCRIPTOR,source);
                    pgconfig.parameter(DATABASE.getName().getCode()).setValue("cite-wfs");
                    pgconfig.parameter(HOST.getName().getCode()).setValue("flupke.geomatys.com");
                    pgconfig.parameter(SCHEMA.getName().getCode()).setValue("public");
                    pgconfig.parameter(USER.getName().getCode()).setValue("test");
                    pgconfig.parameter(PASSWORD.getName().getCode()).setValue("test");
                    pgconfig.parameter(NAMESPACE.getName().getCode()).setValue("http://cite.opengeospatial.org/gmlsf");
                    choice.values().add(pgconfig);
                }

                return config;
            }

            @Override
            public void saveConfiguration(ProviderService service, List<Provider> providers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        LayerProviderProxy.getInstance().setConfigurator(config);

    }
}
