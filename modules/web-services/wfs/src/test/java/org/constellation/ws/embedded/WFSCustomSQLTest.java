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


import org.apache.sis.test.XMLComparator;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.DataBusiness;
import org.constellation.admin.ProviderBusiness;
import org.constellation.admin.ServiceBusiness;
import org.constellation.admin.SpringHelper;
import org.constellation.api.ProviderType;
import org.constellation.configuration.LayerContext;
import org.constellation.map.configuration.LayerBusiness;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.Providers;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.provider.configuration.ProviderParameters.SOURCE_ID_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.SOURCE_LOADALL_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.getOrCreate;
import static org.constellation.provider.featurestore.FeatureStoreProviderService.SOURCE_CONFIG_DESCRIPTOR;
import org.constellation.test.utils.TestDatabaseHandler;
import static org.geotoolkit.data.AbstractFeatureStoreFactory.NAMESPACE;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.DATABASE;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.HOST;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.PASSWORD;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.SCHEMA;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.USER;
import static org.geotoolkit.parameter.ParametersExt.createGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeTrue;

// JUnit dependencies

/**
 * Ensure extended datastores properly work.
 *
 * @author Johann Sorel (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
public class WFSCustomSQLTest extends AbstractGrizzlyServer implements ApplicationContextAware {

    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Inject
    private ServiceBusiness serviceBusiness;
    
    @Inject
    protected LayerBusiness layerBusiness;
    
    @Inject
    protected ProviderBusiness providerBusiness;
    
    @Inject
    protected DataBusiness dataBusiness;
    
     private static final String WFS_DESCRIBE_FEATURE_TYPE_URL =
               "request=DescribeFeatureType"
             + "&service=WFS"
             + "&version=1.1.0"
             + "&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1"
             + "&TypeName=CustomSQLQuery";


    private static boolean localdb_active = true;

    private static boolean initialized = false;
    
    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @PostConstruct
    public void initPool() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                ConfigDirectory.setupTestEnvironement("WFSCustomSQLTest");

                layerBusiness.removeAll();
                serviceBusiness.deleteAll();
                dataBusiness.deleteAll();
                providerBusiness.removeAll();
                
                 final ProviderFactory featfactory = DataProviders.getInstance().getFactory("feature-store");

                // Defines a PostGis data provider
                localdb_active = TestDatabaseHandler.hasLocalDatabase();
                if (localdb_active) {
                    final ParameterValueGroup source = featfactory.getProviderDescriptor().createValue();;
                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("postgisSrc");

                    final ParameterValueGroup choice = getOrCreate(SOURCE_CONFIG_DESCRIPTOR,source);
                    final ParameterValueGroup pgconfig = createGroup(choice, "PostgresParameters");
                    pgconfig.parameter(DATABASE .getName().getCode()).setValue(TestDatabaseHandler.testProperties.getProperty("feature_db_name"));
                    pgconfig.parameter(HOST     .getName().getCode()).setValue(TestDatabaseHandler.testProperties.getProperty("feature_db_host"));
                    pgconfig.parameter(SCHEMA   .getName().getCode()).setValue(TestDatabaseHandler.testProperties.getProperty("feature_db_schema"));
                    pgconfig.parameter(USER     .getName().getCode()).setValue(TestDatabaseHandler.testProperties.getProperty("feature_db_user"));
                    pgconfig.parameter(PASSWORD .getName().getCode()).setValue(TestDatabaseHandler.testProperties.getProperty("feature_db_pass"));
                    pgconfig.parameter(NAMESPACE.getName().getCode()).setValue("http://cite.opengeospatial.org/gmlsf");

                    //add a custom sql query layer
                    final ParameterValueGroup layer = getOrCreateGroup(source, "Layer");
                    getOrCreateValue(layer, "name").setValue("CustomSQLQuery");
                    getOrCreateValue(layer, "language").setValue("CUSTOM-SQL");
                    getOrCreateValue(layer, "statement").setValue("SELECT name as nom, \"pointProperty\" as geom FROM \"PrimitiveGeoFeature\" ");

                    choice.values().add(pgconfig);

                    providerBusiness.createProvider("postgisSrc", null, ProviderType.LAYER, "feature-store", source);

                    dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "AggregateGeoFeature"), "postgisSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "PrimitiveGeoFeature"), "postgisSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "EntitéGénérique"),     "postgisSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "CustomSQLQuery"),      "postgisSrc", "VECTOR", false, true, null, null);
                }
                
                final File outputDir = initDataDirectory();
                final ParameterValueGroup sourcef = featfactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourcef, "id").setValue("shapeSrc");
                getOrCreateValue(sourcef, "load_all").setValue(true);

                final ParameterValueGroup choice2 = getOrCreateGroup(sourcef, "choice");
                final ParameterValueGroup shpconfig = createGroup(choice2, "ShapefileParametersFolder");
                getOrCreateValue(shpconfig, "url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles"));
                getOrCreateValue(shpconfig, "namespace").setValue("http://www.opengis.net/gml");

                final ParameterValueGroup layer2 = getOrCreateGroup(sourcef, "Layer");
                getOrCreateValue(layer2, "name").setValue("NamedPlaces");
                getOrCreateValue(layer2, "style").setValue("cite_style_NamedPlaces");
                
                providerBusiness.createProvider("shapeSrc", null, ProviderType.LAYER, "feature-store", sourcef);

                dataBusiness.create(new QName("http://www.opengis.net/gml", "BuildingCenters"), "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "BasicPolygons"),   "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Bridges"),         "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Streams"),         "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Lakes"),           "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "NamedPlaces"),     "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Buildings"),       "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "RoadSegments"),    "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "DividedRoutes"),   "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Forests"),         "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "MapNeatline"),     "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Ponds"),           "shapeSrc", "VECTOR", false, true, null, null);

                final String url = "jdbc:derby:memory:TestWFSCustomOM";
                final DefaultDataSource ds = new DefaultDataSource(url + ";create=true");
                Connection con = ds.getConnection();
                DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                sr.run(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
                sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));
                con.close();
                ds.shutdown();

                final ParameterValueGroup sourceOM = featfactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourceOM, "id").setValue("omSrc");
                getOrCreateValue(sourceOM, "load_all").setValue(true);    

                final ParameterValueGroup choiceOM = getOrCreateGroup(sourceOM, "choice");
                final ParameterValueGroup omconfig = createGroup(choiceOM, " SOSDBParameters");
                getOrCreateValue(omconfig, "sgbdtype").setValue("derby");
                getOrCreateValue(omconfig, "derbyurl").setValue(url);
                
                providerBusiness.createProvider("omSrc", null, ProviderType.LAYER, "feature-store", sourceOM);
                dataBusiness.create(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"), "omSrc", "VECTOR", false, true, null, null);
                
                DataProviders.getInstance().reload();
                
                final LayerContext config = new LayerContext();
                config.getCustomParameters().put("shiroAccessible", "false");
                config.getCustomParameters().put("transactionSecurized", "false");
                config.getCustomParameters().put("transactionnal", "true");

               serviceBusiness.create("WFS", "default", config, null, null);
               if (localdb_active) {
                   layerBusiness.add("AggregateGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "WFS", null);
                   layerBusiness.add("PrimitiveGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "WFS", null);
                   layerBusiness.add("EntitéGénérique",     "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "WFS", null);
                   layerBusiness.add("CustomSQLQuery",      "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "WFS", null);
               }
               layerBusiness.add("SamplingPoint",       "http://www.opengis.net/sampling/1.0",  "omSrc",      null, "default", "WFS", null);
               layerBusiness.add("BuildingCenters",     "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "WFS", null);
               layerBusiness.add("BasicPolygons",       "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "WFS", null);
               layerBusiness.add("Bridges",             "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "WFS", null);
               layerBusiness.add("Streams",             "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "WFS", null);
               layerBusiness.add("Lakes",               "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "WFS", null);
               layerBusiness.add("NamedPlaces",         "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "WFS", null);
               layerBusiness.add("Buildings",           "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "WFS", null);
               layerBusiness.add("RoadSegments",        "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "WFS", null);
               layerBusiness.add("DividedRoutes",       "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "WFS", null);
               layerBusiness.add("Forests",             "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "WFS", null);
               layerBusiness.add("MapNeatline",         "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "WFS", null);
               layerBusiness.add("Ponds",               "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "WFS", null);

                initServer(null, null);

                pool = new MarshallerPool(JAXBContext.newInstance("org.geotoolkit.wfs.xml.v110"   +
                        ":org.geotoolkit.ogc.xml.v110"  +
                        ":org.geotoolkit.gml.xml.v311"  +
                        ":org.geotoolkit.xsd.xml.v2001" +
                        ":org.geotoolkit.sampling.xml.v100" +
                        ":org.apache.sis.internal.jaxb.geometry"), null);
                initialized = true;
            } catch (Exception ex) {
                Logger.getLogger(WFSCustomSQLTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void shutDown() {
        ConfigDirectory.shutdownTestEnvironement("WFSCustomSQLTest");
        DataProviders.getInstance().setConfigurator(Providers.DEFAULT_CONFIGURATOR);
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        finish();
    }

    @Test
    public void testWFSDescribeFeatureGET() throws Exception {
        waitForStart();
        assumeTrue(localdb_active);
        
        final URL getfeatsUrl;
        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" +WFS_DESCRIBE_FEATURE_TYPE_URL);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        Object obj = unmarshallResponse(getfeatsUrl);
        assertTrue(obj instanceof Schema);
        final Schema schema = (Schema) obj;
        final List elements = schema.getElements();
        assertEquals(1, elements.size());

        final XMLComparator comparator = new XMLComparator(WFSCustomSQLTest.class.getResource("/expected/customsqlquery.xsd"), getfeatsUrl);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();

    }

}
