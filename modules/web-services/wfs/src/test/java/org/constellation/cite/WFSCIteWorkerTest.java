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
package org.constellation.cite;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import org.apache.sis.geometry.GeneralDirectPosition;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.ServiceBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.map.configuration.LayerBusiness;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.Providers;
import org.constellation.provider.configuration.AbstractConfigurator;
import org.constellation.provider.configuration.Configurator;
import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.constellation.provider.featurestore.FeatureStoreProviderService.*;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.wfs.ws.DefaultWFSWorker;
import org.constellation.wfs.ws.WFSWorker;
import org.constellation.wfs.ws.rs.FeatureCollectionWrapper;
import org.geotoolkit.data.FeatureCollection;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.*;
import org.geotoolkit.db.postgres.PostgresFeatureStoreFactory;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.geotoolkit.gml.xml.v311.MultiPointType;
import org.geotoolkit.gml.xml.v311.PointPropertyType;
import org.geotoolkit.gml.xml.v311.PointType;
import org.geotoolkit.ogc.xml.v110.EqualsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.wfs.xml.ResultTypeType;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.QueryType;
import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class WFSCIteWorkerTest {

    private static WFSWorker worker;

    private XmlFeatureWriter featureWriter;
    
    @Inject
    protected ServiceBusiness serviceBusiness;
    
    @Inject
    protected LayerBusiness layerBusiness;

    public static boolean hasLocalDatabase() {
        return false; // TODO
    }
    
    @PostConstruct
    public void setUpClass() {
    
        try {
            ConfigurationEngine.setupTestEnvironement("WFSCiteWorkerTest");
            
            final LayerContext config = new LayerContext();
            config.getCustomParameters().put("shiroAccessible", "false");
            config.getCustomParameters().put("transactionSecurized", "false");
            config.getCustomParameters().put("transactionnal", "true");
            
            serviceBusiness.create("WFS", "default", config, null);
            layerBusiness.add("AggregateGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "WFS", null);
            layerBusiness.add("PrimitiveGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "WFS", null);
            layerBusiness.add("EntitéGénérique",     "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "WFS", null);
            
            initFeatureSource();
            worker = new DefaultWFSWorker("default");
            worker.setLogLevel(Level.FINER);
            
        } catch (Exception ex) {
            Logger.getLogger(WFSCIteWorkerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        DataProviders.getInstance().setConfigurator(Providers.DEFAULT_CONFIGURATOR);
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
        assumeTrue(hasLocalDatabase());

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

        final Configurator config = new AbstractConfigurator() {

            @Override
            public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
                final ArrayList<Map.Entry<String, ParameterValueGroup>> lst = new ArrayList<>();
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("feature-store");
                
                if (hasLocalDatabase()) {
                    // Defines a PostGis data provider
                    final ParameterValueGroup source = factory.getProviderDescriptor().createValue();;
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
                return lst;
            }
            
            @Override
            public List<Configurator.ProviderInformation> getProviderInformations() throws ConfigurationException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        DataProviders.getInstance().setConfigurator(config);

    }
}
