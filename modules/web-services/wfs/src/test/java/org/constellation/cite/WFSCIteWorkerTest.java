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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.util.logging.Logging;
import org.constellation.business.IDataBusiness;
import org.constellation.business.ILayerBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.SpringHelper;
import org.constellation.api.ProviderType;
import org.constellation.configuration.LayerContext;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import static org.constellation.provider.configuration.ProviderParameters.SOURCE_ID_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.SOURCE_LOADALL_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.getOrCreate;
import static org.constellation.provider.featurestore.FeatureStoreProviderService.SOURCE_CONFIG_DESCRIPTOR;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.test.utils.TestDatabaseHandler;
import org.constellation.wfs.ws.DefaultWFSWorker;
import org.constellation.wfs.ws.WFSWorker;
import org.constellation.wfs.ws.rs.FeatureCollectionWrapper;
import org.geotoolkit.data.FeatureCollection;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.DATABASE;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.HOST;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.NAMESPACE;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.PASSWORD;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.SCHEMA;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.USER;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.geotoolkit.gml.xml.v311.MultiPointType;
import org.geotoolkit.gml.xml.v311.PointPropertyType;
import org.geotoolkit.gml.xml.v311.PointType;
import org.geotoolkit.ogc.xml.v110.EqualsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import static org.geotoolkit.parameter.ParametersExt.createGroup;
import org.geotoolkit.wfs.xml.ResultTypeType;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.QueryType;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
@ActiveProfiles({"standard","derby"})
public class WFSCIteWorkerTest implements ApplicationContextAware {

    private static final Logger LOGGER = Logging.getLogger(WFSCIteWorkerTest.class);
    
    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static WFSWorker worker;

    private XmlFeatureWriter featureWriter;
    
    @Inject
    protected IServiceBusiness serviceBusiness;
    
    @Inject
    protected ILayerBusiness layerBusiness;
    
    @Inject
    protected IProviderBusiness providerBusiness;
    
    @Inject
    protected IDataBusiness dataBusiness;

    private static boolean initialized = false;
    
    @PostConstruct
    public void setUpClass() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            if (!TestDatabaseHandler.hasLocalDatabase()) {
                LOGGER.warning("-- SOME TESTS WILL BE SKIPPED BECAUSE TEST DATABASE IS MISSING --");
                initialized = true;
                return;
            }
            try {
                ConfigDirectory.setupTestEnvironement("WFSCiteWorkerTest");

                layerBusiness.removeAll();
                serviceBusiness.deleteAll();
                dataBusiness.deleteAll();
                providerBusiness.removeAll();

                final ProviderFactory factory = DataProviders.getInstance().getFactory("feature-store");

                // Defines a PostGis data provider
                final ParameterValueGroup source = factory.getProviderDescriptor().createValue();;
                source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("postgisSrc");

                final ParameterValueGroup choice = getOrCreate(SOURCE_CONFIG_DESCRIPTOR,source);
                final ParameterValueGroup pgconfig = createGroup(choice, "PostgresParameters");
                pgconfig.parameter(DATABASE.getName().getCode()).setValue("cite-wfs");
                pgconfig.parameter(HOST.getName().getCode()).setValue("localhost");
                pgconfig.parameter(SCHEMA.getName().getCode()).setValue("public");
                pgconfig.parameter(USER.getName().getCode()).setValue("test");
                pgconfig.parameter(PASSWORD.getName().getCode()).setValue("test");
                pgconfig.parameter(NAMESPACE.getName().getCode()).setValue("http://cite.opengeospatial.org/gmlsf");
                choice.values().add(pgconfig);

                providerBusiness.storeProvider("postgisSrc", null, ProviderType.LAYER, "feature-store", source);

                dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "AggregateGeoFeature"), "postgisSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "PrimitiveGeoFeature"), "postgisSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "EntitéGénérique"),     "postgisSrc", "VECTOR", false, true, null, null);


                DataProviders.getInstance().reload();
                final LayerContext config = new LayerContext();
                config.getCustomParameters().put("shiroAccessible", "false");
                config.getCustomParameters().put("transactionSecurized", "false");
                config.getCustomParameters().put("transactionnal", "true");

                serviceBusiness.create("wfs", "default", config, null, null);
                layerBusiness.add("AggregateGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "wfs", null);
                layerBusiness.add("PrimitiveGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "wfs", null);
                layerBusiness.add("EntitéGénérique",     "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "wfs", null);

                worker = new DefaultWFSWorker("default");
                worker.setLogLevel(Level.FINER);
                initialized = true;
            } catch (Exception ex) {
                Logger.getLogger(WFSCIteWorkerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ConfigDirectory.shutdownTestEnvironement("WFSCiteWorkerTest");
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
        assumeTrue(TestDatabaseHandler.hasLocalDatabase());

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
    
}
