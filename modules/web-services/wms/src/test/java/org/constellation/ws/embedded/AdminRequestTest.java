/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.ws.embedded;


import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.Language;
import org.constellation.configuration.Languages;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.ServiceStatus;
import org.constellation.configuration.Source;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.Providers;
import org.constellation.provider.configuration.AbstractConfigurator;
import org.constellation.provider.configuration.Configurator;
import static org.constellation.provider.configuration.ProviderParameters.*;
import org.constellation.test.utils.Order;
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

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class AdminRequestTest extends AbstractGrizzlyServer {

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void start() throws JAXBException {
        ConfigurationEngine.setupTestEnvironement("AdminRequestTest");

        final List<Source> sources = Arrays.asList(new Source("shapeSrc", true, null, null));
        final Layers layers = new Layers(sources);
        final LayerContext config = new LayerContext(layers);
        config.getCustomParameters().put("shiroAccessible", "false");

        ConfigurationEngine.storeConfiguration("WMS", "default", config);

        final List<Source> sources2 = Arrays.asList(new Source("shapeSrc", false, Arrays.asList(new Layer(new QName("http://www.opengis.net/gml","Lakes"))), null));
        final Layers layers2 = new Layers(sources2);
        final LayerContext config2 = new LayerContext(layers2);
        config2.setSupportedLanguages(new Languages(Arrays.asList(new Language("fre"), new Language("eng", true))));
        config2.getCustomParameters().put("shiroAccessible", "false");

        ConfigurationEngine.storeConfiguration("WMS", "wms1", config2);


        initServer(new String[] {
            "org.constellation.map.ws.rs",
            "org.constellation.configuration.ws.rs",
            "org.constellation.ws.rs.provider"
        }, null);

        // Get the list of layers
        pool = GenericDatabaseMarshallerPool.getInstance();

        final Configurator configurator = new AbstractConfigurator() {
            @Override
            public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {

                final ArrayList<Map.Entry<String, ParameterValueGroup>> lst = new ArrayList<>();
                final ProviderFactory factory = DataProviders.getInstance().getFactory("feature-store");
                
                try{ 
                    {//SHAPEFILE
                        final File outputDir = initDataDirectory();
                        final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                        getOrCreateValue(source, "id").setValue("shapeSrc");
                        getOrCreateValue(source, "load_all").setValue(true);

                        final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                        final ParameterValueGroup shpconfig = createGroup(choice, "ShapefileParametersFolder");
                        getOrCreateValue(shpconfig, "url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles"));
                        getOrCreateValue(shpconfig, "namespace").setValue("http://www.opengis.net/gml");

                        final ParameterValueGroup layer = getOrCreateGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("NamedPlaces");
                        getOrCreateValue(layer, "style").setValue("cite_style_NamedPlaces");
                        lst.add(new AbstractMap.SimpleImmutableEntry<>("shapeSrc",source));
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
        DataProviders.getInstance().setConfigurator(Providers.DEFAULT_CONFIGURATOR);
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
        URL niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=newInstance&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "Service instance successfully created.");
        assertEquals(expResult, obj);

        /*
         * we see the instance with a status NOT_STARTED
         */
        URL liUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=listInstance");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        final List<Instance> instances = new ArrayList<>();
        final List<String> versions = Arrays.asList("1.1.1", "1.3.0");
        instances.add(new Instance("default", "OGC:WMS", "Constellation Map Server", "WMS", versions, 12, ServiceStatus.WORKING));
        instances.add(new Instance("wms1",    "OGC:WMS", "Constellation Map Server", "WMS", versions, 1,  ServiceStatus.WORKING));
        instances.add(new Instance("wms2",    "OGC:WMS", "Constellation Map Server", "WMS", versions, 0,  ServiceStatus.NOT_STARTED));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);

        /*
         * if we want to build the same new instance we receive an error
         */

        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        expResult = new AcknowlegementType("Error", "Unable to create an instance.");
        assertEquals(expResult, obj);
    }

    @Test
    @Order(order=2)
    public void testStartInstance() throws Exception {

        /*
         * we start the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=start&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "Service instance successfully started.");
        assertEquals(expResult, obj);

         /*
         * we verify tat the instance has now a status WORKING
         */
        URL liUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=listInstance");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        List<Instance> instances = new ArrayList<>();
        final List<String> versions = Arrays.asList("1.1.1", "1.3.0");
        instances.add(new Instance("default", "OGC:WMS", "Constellation Map Server", "WMS", versions, 12, ServiceStatus.WORKING));
        instances.add(new Instance("wms1",    "OGC:WMS", "Constellation Map Server", "WMS", versions, 1,  ServiceStatus.WORKING));
        instances.add(new Instance("wms2",    "OGC:WMS", "Constellation Map Server", "WMS", versions, 0,  ServiceStatus.WORKING));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);

    }

    @Test
    @Order(order=3)
    public void testConfigureInstance() throws Exception {

        /*
         * we configure the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=configure&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();
        List<Source> sources = new ArrayList<>();
        sources.add(new Source("coverageTestSrc", true, null, null));
        sources.add(new Source("shapeSrc", true, null, null));
        Layers layerObj = new Layers(sources);
        LayerContext layerContext = new LayerContext(layerObj);
        layerContext.getCustomParameters().put("shiroAccessible", "false");

        postRequestObject(conec, layerContext);
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "Service instance configuration successfully updated.");
        assertEquals(expResult, obj);

        /*
         * we restart the instance to take change in count
         */
        niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=restart&id=wms2");


        // for a POST request
        conec = niUrl.openConnection();

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
        URL niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=stop&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "Service instance successfully stopped.");
        assertEquals(expResult, obj);

         /*
         * we see the instance has now a status NOT_STARTED
         */
        URL liUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=listInstance");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        final List<Instance> instances = new ArrayList<>();
        final List<String> versions = Arrays.asList("1.1.1", "1.3.0");
        instances.add(new Instance("default", "OGC:WMS", "Constellation Map Server", "WMS", versions, 12, ServiceStatus.WORKING));
        instances.add(new Instance("wms1",    "OGC:WMS", "Constellation Map Server", "WMS", versions, 1,  ServiceStatus.WORKING));
        instances.add(new Instance("wms2",    "OGC:WMS", "Constellation Map Server", "WMS", versions, 12, ServiceStatus.NOT_STARTED));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);
    }

    @Test
    @Order(order=5)
    public void testDeleteInstance() throws Exception {
        /*
         * we stop the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/admin?request=delete&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "Service instance successfully deleted.");
        assertEquals(expResult, obj);

         /*
         * we see the instance has now a status NOT_STARTED
         */
        URL liUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/admin?request=listInstance");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        final List<Instance> instances = new ArrayList<>();
        final List<String> versions = Arrays.asList("1.1.1", "1.3.0");
        instances.add(new Instance("default", "OGC:WMS", "Constellation Map Server", "WMS", versions, 12, ServiceStatus.WORKING));
        instances.add(new Instance("wms1",    "OGC:WMS", "Constellation Map Server", "WMS", versions, 1,  ServiceStatus.WORKING));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);
    }

}
