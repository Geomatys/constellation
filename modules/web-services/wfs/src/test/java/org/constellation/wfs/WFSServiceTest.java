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

package org.constellation.wfs;

import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.DataBusiness;
import org.constellation.admin.ProviderBusiness;
import org.constellation.admin.ServiceBusiness;
import org.constellation.admin.SpringHelper;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.configuration.LayerContext;
import org.constellation.map.configuration.LayerBusiness;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.Providers;
import org.constellation.test.utils.BasicMultiValueMap;
import org.constellation.test.utils.BasicUriInfo;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.constellation.wfs.ws.rs.FeatureCollectionWrapper;
import org.constellation.wfs.ws.rs.WFSService;
import org.constellation.ws.rs.WebService;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.provider.configuration.ProviderParameters.SOURCE_ID_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.SOURCE_LOADALL_DESCRIPTOR;
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

// JUnit dependencies

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
public class WFSServiceTest implements ApplicationContextAware {

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
    
    private static WFSService service;

    private static DefaultDataSource ds = null;

    private static DefaultDataSource ds2 = null;

    private static final BasicUriInfo info = new BasicUriInfo(null, null);

    private static final MultivaluedMap<String,String> queryParameters = new BasicMultiValueMap<>();
    private static final MultivaluedMap<String,String> pathParameters = new BasicMultiValueMap<>();

    private static boolean initialized = false;
    
    private static boolean mdweb_active = true;
    
    @PostConstruct
    public void setUpClass() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                ConfigurationEngine.setupTestEnvironement("WFSServiceTest");

                layerBusiness.removeAll();
                serviceBusiness.deleteAll();
                dataBusiness.deleteAll();
                providerBusiness.removeAll();
                
                 final ProviderFactory featfactory = DataProviders.getInstance().getFactory("feature-store");

                // Defines a PostGis data provider
                final ParameterValueGroup source = featfactory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("postgisSrc");

                final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                final ParameterValueGroup pgconfig = createGroup(choice, "PostgresParameters");
                pgconfig.parameter(DATABASE.getName().getCode()).setValue("cite-wfs");
                pgconfig.parameter(HOST.getName().getCode()).setValue("localhost");
                pgconfig.parameter(SCHEMA.getName().getCode()).setValue("public");
                pgconfig.parameter(USER.getName().getCode()).setValue("test");
                pgconfig.parameter(PASSWORD.getName().getCode()).setValue("test");
                pgconfig.parameter(NAMESPACE.getName().getCode()).setValue("http://cite.opengeospatial.org/gmlsf");
                choice.values().add(pgconfig);

                providerBusiness.createProvider("postgisSrc", null, ProviderRecord.ProviderType.LAYER, "feature-store", source);

                dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "AggregateGeoFeature"), "postgisSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "PrimitiveGeoFeature"), "postgisSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "EntitéGénérique"),     "postgisSrc", "VECTOR", false, true, null, null);
                
                final File outputDir = initDataDirectory();
                final ParameterValueGroup sourcef = featfactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourcef, "id").setValue("shapeSrc");
                getOrCreateValue(sourcef, "load_all").setValue(true);

                final ParameterValueGroup choice2 = getOrCreateGroup(sourcef, "choice");
                final ParameterValueGroup shpconfig = createGroup(choice2, "ShapefileParametersFolder");
                getOrCreateValue(shpconfig, "url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles"));
                getOrCreateValue(shpconfig, "namespace").setValue("http://www.opengis.net/gml");

                final ParameterValueGroup layer = getOrCreateGroup(sourcef, "Layer");
                getOrCreateValue(layer, "name").setValue("NamedPlaces");
                getOrCreateValue(layer, "style").setValue("cite_style_NamedPlaces");

                providerBusiness.createProvider("shapeSrc", null, ProviderRecord.ProviderType.LAYER, "feature-store", sourcef);

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
                
                final String url = "jdbc:derby:memory:TestWFSServiceOM";
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
                
                providerBusiness.createProvider("omSrc", null, ProviderRecord.ProviderType.LAYER, "feature-store", sourceOM);
                dataBusiness.create(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"), "omSrc", "VECTOR", false, true, null, null);
                
                // MDWEB store
                mdweb_active = Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql") != null;
                if (mdweb_active) {
                    final String url2 = "jdbc:derby:memory:TestWFSServiceSMl";
                    ds2 = new DefaultDataSource(url2 + ";create=true");
                    Connection con2 = ds2.getConnection();
                    DerbySqlScriptRunner sr2 = new DerbySqlScriptRunner(con2);
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql"));
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115.sql"));
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19119.sql"));
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19108.sql"));
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/data/defaultRecordSets.sql"));
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/users/creation_user.sql"));
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/SensorML.sql"));
                    sr2.run(Util.getResourceAsStream("org/constellation/sql/sml-data.sql"));
                    con.close();

                    final ParameterValueGroup sourceSML = featfactory.getProviderDescriptor().createValue();
                    getOrCreateValue(sourceSML, "id").setValue("smlSrc");
                    getOrCreateValue(sourceSML, "load_all").setValue(true);             

                    final ParameterValueGroup choiceSML = getOrCreateGroup(sourceSML, "choice");
                    final ParameterValueGroup smlconfig = createGroup(choiceSML, "SMLParameters");
                    getOrCreateValue(smlconfig, "sgbdtype").setValue("derby");
                    getOrCreateValue(smlconfig, "derbyurl").setValue(url2);

                    providerBusiness.createProvider("smlSrc", null, ProviderRecord.ProviderType.LAYER, "feature-store", sourceSML);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "System"),         "smlSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "Component"),      "smlSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "DataSourceType"), "smlSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "ProcessModel"),   "smlSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "ProcessChain"),   "smlSrc", "VECTOR", false, true, null, null);
                }
                
                        
                final LayerContext config = new LayerContext();
                config.getCustomParameters().put("shiroAccessible", "false");
                config.getCustomParameters().put("transactionSecurized", "false");
                config.getCustomParameters().put("transactionnal", "true");

                serviceBusiness.create("wfs", "default", config, null, null);
                layerBusiness.add("AggregateGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "wfs", null);
                layerBusiness.add("PrimitiveGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "wfs", null);
                layerBusiness.add("EntitéGénérique",     "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "wfs", null);
                layerBusiness.add("SamplingPoint",       "http://www.opengis.net/sampling/1.0",  "omSrc",      null, "default", "wfs", null);
                layerBusiness.add("BuildingCenters",     "http://www.opengis.net/gml",           "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("BasicPolygons",       "http://www.opengis.net/gml",           "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("Bridges",             "http://www.opengis.net/gml",           "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("Streams",             "http://www.opengis.net/gml",           "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("Lakes",               "http://www.opengis.net/gml",           "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("NamedPlaces",         "http://www.opengis.net/gml",           "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("Buildings",           "http://www.opengis.net/gml",           "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("RoadSegments",        "http://www.opengis.net/gml",           "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("DividedRoutes",       "http://www.opengis.net/gml",           "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("Forests",             "http://www.opengis.net/gml",           "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("MapNeatline",         "http://www.opengis.net/gml",           "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("Ponds",               "http://www.opengis.net/gml",           "shapeSrc",   null, "default", "wfs", null);
                if (mdweb_active) {
                    layerBusiness.add("System",              "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "default", "wfs", null);
                    layerBusiness.add("Component",           "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "default", "wfs", null);
                    layerBusiness.add("DataSourceType",      "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "default", "wfs", null);
                    layerBusiness.add("ProcessModel",        "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "default", "wfs", null);
                    layerBusiness.add("ProcessChain",        "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "default", "wfs", null);
                }

                service = new WFSService();

                Field privateStringField = WebService.class.getDeclaredField("uriContext");
                privateStringField.setAccessible(true);
                privateStringField.set(service, info);

                pathParameters.add("serviceId", "default");
                queryParameters.add("serviceId", "default");
                info.setPathParameters(pathParameters);
                info.setQueryParameters(queryParameters);
                initialized = true;
            } catch (Exception ex) {
                Logger.getLogger(WFSServiceTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ConfigurationEngine.shutdownTestEnvironement("WFSServiceTest");
        
        if (ds != null) {
            ds.shutdown();
        }
        if (ds2 != null) {
            ds2.shutdown();
        }

        if (service != null) {
            service.destroy();
        }
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void transactionInsertTest() throws Exception {

        /*
         * we verify that the number of features before insert
         */
        InputStream is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.GetFeature.xml"));
        Response result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        assertTrue(result.getEntity() instanceof FeatureCollectionWrapper);
        FeatureCollection collection = ((FeatureCollectionWrapper) result.getEntity()).getFeatureCollection();
        assertEquals(6, collection.size());

        /*
         * we insert the feature
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.InsertFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        /*
         * we verify that the features has been inserted
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.GetFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        assertTrue(result.getEntity() instanceof FeatureCollectionWrapper);
        collection = ((FeatureCollectionWrapper) result.getEntity()).getFeatureCollection();
        assertEquals(8, collection.size());

        /*
         * we delete the features
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.DeleteFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        /*
         * we verify that the features has been deleted
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.GetFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        assertTrue(result.getEntity() instanceof FeatureCollectionWrapper);
        collection = ((FeatureCollectionWrapper) result.getEntity()).getFeatureCollection();
        assertEquals(6, collection.size());

        /*
         * we insert the feature with another request
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.InsertFeature2.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        /*
         * we verify that the features has been inserted
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.GetFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        assertTrue(result.getEntity() instanceof FeatureCollectionWrapper);
        collection = ((FeatureCollectionWrapper) result.getEntity()).getFeatureCollection();
        assertEquals(8, collection.size());

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
}
