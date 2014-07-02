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


import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.DataBusiness;
import org.constellation.admin.ProviderBusiness;
import org.constellation.admin.ServiceBusiness;
import org.constellation.admin.SpringHelper;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.Language;
import org.constellation.configuration.Languages;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.ServiceStatus;
import org.constellation.dto.Service;
import org.constellation.dto.SimpleValue;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.map.configuration.LayerBusiness;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.Providers;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.test.utils.TestRunner;
import static org.constellation.ws.embedded.AbstractGrizzlyServer.initDataDirectory;
import static org.geotoolkit.parameter.ParametersExt.createGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateValue;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
public class AdminRequestTest extends AbstractGrizzlyServer  implements ApplicationContextAware {

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
    
    private static boolean initialized = false;
    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @PostConstruct
    public void start() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                ConfigurationEngine.setupTestEnvironement("AdminRequestTest");

                layerBusiness.removeAll();
                serviceBusiness.deleteAll();
                dataBusiness.deleteAll();
                providerBusiness.removeAll();
                
                final ProviderFactory ffactory = DataProviders.getInstance().getFactory("feature-store");
                final File outputDir = initDataDirectory();
                final ParameterValueGroup sourcef = ffactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourcef, "id").setValue("shapeSrc");
                getOrCreateValue(sourcef, "load_all").setValue(true);

                final ParameterValueGroup choice = getOrCreateGroup(sourcef, "choice");
                final ParameterValueGroup shpconfig = createGroup(choice, "ShapefileParametersFolder");
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

                final LayerContext config = new LayerContext();
                config.getCustomParameters().put("shiroAccessible", "false");

                serviceBusiness.create("wms", "default", config, null, null);
                layerBusiness.add("BuildingCenters",     "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("BasicPolygons",       "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Bridges",             "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Streams",             "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Lakes",               "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("NamedPlaces",         "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Buildings",           "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("RoadSegments",        "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("DividedRoutes",       "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Forests",             "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("MapNeatline",         "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Ponds",               "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);

                final LayerContext config2 = new LayerContext();
                config2.setSupportedLanguages(new Languages(Arrays.asList(new Language("fre"), new Language("eng", true))));
                config2.getCustomParameters().put("shiroAccessible", "false");

                serviceBusiness.create("wms", "wms1", config2, null, null);
                layerBusiness.add("Lakes", "http://www.opengis.net/gml", "shapeSrc", null, "wms1", "wms", null);



                initServer(null, null);

                // Get the list of layers
                pool = GenericDatabaseMarshallerPool.getInstance();
                initialized = true;
            } catch (Exception ex) {
                Logger.getLogger(AdminRequestTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void shutDown() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        ConfigurationEngine.shutdownTestEnvironement("AdminRequestTest");
        finish();
    }

    @Test
    @Order(order=1)
    public void testNewInstance() throws Exception {

        waitForStart();

        /*
         * we build a new instance
         */
        URL niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/OGC/wms/domain/1");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        final Service meta = new Service();
        meta.setIdentifier("wms2");
        putRequestObject(conec, meta, GenericDatabaseMarshallerPool.getInstance());
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "WMS service \"wms2\" successfully created.");
        assertEquals(expResult, obj);

        /*
         * we see the instance with a status NOT_STARTED
         */
        URL liUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/OGC/wms/all");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        final List<Instance> instances = new ArrayList<>();
        final List<String> versions = Arrays.asList("1.1.1", "1.3.0");
        instances.add(new Instance(1, "default", "OGC:WMS", "Constellation Map Server", "wms", versions, 12, ServiceStatus.STARTED));
        instances.add(new Instance(2, "wms1",    "OGC:WMS", "Constellation Map Server", "wms", versions, 1,  ServiceStatus.STARTED));
        instances.add(new Instance(3, "wms2",    "OGC:WMS", "Constellation Map Server", "wms", versions, 0,  ServiceStatus.STOPPED));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);

        /*
         * if we want to build the same new instance we receive an error
         */

        // for a POST request
        conec = niUrl.openConnection();
        putRequestObject(conec, meta, GenericDatabaseMarshallerPool.getInstance());
        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        expResult = new AcknowlegementType("Failure", "Instance already created");
        assertEquals(expResult, obj);
    }

    @Test
    @Order(order=2)
    public void testStartInstance() throws Exception {

        /*
         * we start the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/OGC/wms/wms2/start");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponsePost(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "WMS service \"wms2\" successfully started.");
        assertEquals(expResult, obj);

         /*
         * we verify tat the instance has now a status WORKING
         */
        URL liUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/OGC/wms/all");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        List<Instance> instances = new ArrayList<>();
        final List<String> versions = Arrays.asList("1.1.1", "1.3.0");
        instances.add(new Instance(1, "default", "OGC:WMS", "Constellation Map Server", "wms", versions, 12, ServiceStatus.STARTED));
        instances.add(new Instance(2, "wms1",    "OGC:WMS", "Constellation Map Server", "wms", versions, 1,  ServiceStatus.STARTED));
        instances.add(new Instance(3, "wms2",    "OGC:WMS", "Constellation Map Server", "wms", versions, 0,  ServiceStatus.STARTED));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);

    }

    @Ignore
    @Order(order=3)
    public void testConfigureInstance() throws Exception {

        /*
         * we configure the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/OGC/wms/wms2/config");


        // for a POST request
        URLConnection conec = niUrl.openConnection();
        LayerContext layerContext = new LayerContext();
        layerContext.getCustomParameters().put("shiroAccessible", "false");

        postRequestObject(conec, layerContext);
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "Service instance configuration successfully updated.");
        assertEquals(expResult, obj);

        /*
         * we restart the instance to take change in count
         */
        niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/OGC/wms/wms2/restart");


        // for a POST request
        conec = niUrl.openConnection();
        postRequestObject(conec, new SimpleValue(false), GenericDatabaseMarshallerPool.getInstance());
        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        expResult = new AcknowlegementType("Success", "Service instance successfully restarted.");
        assertEquals(expResult, obj);

        URL gcDefaultURL = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/default?request=GetCapabilities&service=WMS&version=1.1.1");
        URL gcWms2URL    = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/wms2?request=GetCapabilities&service=WMS&version=1.1.1");

        String expCapabiliites = getStringResponse(gcDefaultURL.openConnection());
        String resCapabiliites = getStringResponse(gcWms2URL.openConnection());

        resCapabiliites = resCapabiliites.replace("wms2", "default");

        assertEquals(expCapabiliites, resCapabiliites);
    }

    @Test
    @Order(order=4)
    public void testStopInstance() throws Exception {
        /*
         * we stop the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/OGC/wms/wms2/stop");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponsePost(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "WMS service \"wms2\" successfully stopped.");
        assertEquals(expResult, obj);

         /*
         * we see the instance has now a status NOT_STARTED
         */
        URL liUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/1/OGC/wms/all");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        final List<Instance> instances = new ArrayList<>();
        final List<String> versions = Arrays.asList("1.1.1", "1.3.0");
        instances.add(new Instance(1, "default", "OGC:WMS", "Constellation Map Server", "wms", versions, 12, ServiceStatus.STARTED));
        instances.add(new Instance(2, "wms1",    "OGC:WMS", "Constellation Map Server", "wms", versions, 1,  ServiceStatus.STARTED));
        instances.add(new Instance(3, "wms2",    "OGC:WMS", "Constellation Map Server", "wms", versions, 0, ServiceStatus.STOPPED));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);
    }

    @Test
    @Order(order=5)
    public void testDeleteInstance() throws Exception {
        /*
         * we stop the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/1/OGC/wms/wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponseDelete(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "WMS service \"wms2\" successfully deleted.");
        assertEquals(expResult, obj);

         /*
         * we see the instance has now a status NOT_STARTED
         */
        URL liUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/1/OGC/wms/all");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        final List<Instance> instances = new ArrayList<>();
        final List<String> versions = Arrays.asList("1.1.1", "1.3.0");
        instances.add(new Instance(1, "default", "OGC:WMS", "Constellation Map Server", "wms", versions, 12, ServiceStatus.STARTED));
        instances.add(new Instance(2, "wms1",    "OGC:WMS", "Constellation Map Server", "wms", versions, 1,  ServiceStatus.STARTED));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);
    }

}
