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


import org.constellation.configuration.Source;
import org.constellation.configuration.Layers;
import org.constellation.configuration.Instance;
import java.util.List;
import java.util.ArrayList;
import java.net.URLConnection;
import java.net.URL;
import java.io.File;
import javax.xml.bind.JAXBException;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.ServiceStatus;

import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.Configurator;
import org.constellation.provider.shapefile.ShapeFileProviderService;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;
import static org.constellation.provider.configuration.ProviderParameters.*;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class AdminRequestTest extends AbstractGrizzlyServer {

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void start() throws JAXBException {

        initServer(new String[] {
            "org.constellation.map.ws.rs",
            "org.constellation.configuration.ws.rs",
            "org.constellation.ws.rs.provider"
        }, null);

        // Get the list of layers
        pool = GenericDatabaseMarshallerPool.getInstance();

        final Configurator config = new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(String serviceName, ParameterDescriptorGroup desc) {

                final ParameterValueGroup config = desc.createValue();

                if("coverage-sql".equals(serviceName)){
                    // Defines a PostGrid data provider
                    final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    final ParameterValueGroup srcconfig = getOrCreate(COVERAGESQL_DESCRIPTOR,source);
                    srcconfig.parameter(URL_DESCRIPTOR.getName().getCode()).setValue("jdbc:postgresql://flupke.geomatys.com/coverages-test");
                    srcconfig.parameter(PASSWORD_DESCRIPTOR.getName().getCode()).setValue("test");
                    final String rootDir = System.getProperty("java.io.tmpdir") + "/Constellation/images";
                    srcconfig.parameter(ROOT_DIRECTORY_DESCRIPTOR.getName().getCode()).setValue(rootDir);
                    srcconfig.parameter(USER_DESCRIPTOR.getName().getCode()).setValue("test");
                    srcconfig.parameter(SCHEMA_DESCRIPTOR.getName().getCode()).setValue("coverages");
                    srcconfig.parameter(NAMESPACE_DESCRIPTOR.getName().getCode()).setValue("no namespace");
                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("coverageTestSrc");

                }else if("shapefile".equals(serviceName)){
                    try{
                        final File outputDir = initDataDirectory();

                        final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                        final ParameterValueGroup srcconfig = getOrCreate(ShapeFileProviderService.SOURCE_CONFIG_DESCRIPTOR,source);
                        source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("shapeSrc");
                        srcconfig.parameter(ShapeFileProviderService.FOLDER_DESCRIPTOR.getName().getCode())
                                .setValue(outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles");
                        srcconfig.parameter(ShapeFileProviderService.NAMESPACE_DESCRIPTOR.getName().getCode())
                                .setValue("http://www.opengis.net/gml");

                        ParameterValueGroup layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                        layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("NamedPlaces");
                        layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_NamedPlaces");

                    }catch(Exception ex){
                        throw new RuntimeException(ex.getLocalizedMessage(),ex);
                    }
                }
                //empty configuration for others
                return config;
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        LayerProviderProxy.getInstance().setConfigurator(config);
    }

    @AfterClass
    public static void shutDown() {
        LayerProviderProxy.getInstance().setConfigurator(Configurator.DEFAULT);
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        //finish();
    }

    @Test
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

        AcknowlegementType expResult = new AcknowlegementType("Success", "instance succefully created");
        assertEquals(expResult, obj);

        /*
         * we see the instance with a status NOT_STARTED
         */
        URL liUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=listInstance");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        List<Instance> instances = new ArrayList<Instance>();
        instances.add(new Instance("default", "WMS", ServiceStatus.WORKING));
        instances.add(new Instance("wms1", "WMS", ServiceStatus.WORKING));
        instances.add(new Instance("wms2", "WMS", ServiceStatus.NOT_STARTED));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);

        /*
         * if we want to build the same new instance we receive an error
         */

        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        expResult = new AcknowlegementType("Error", "unable to create an instance");
        assertEquals(expResult, obj);
    }

    @Test
    public void testStartInstance() throws Exception {

        /*
         * we start the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=start&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "new instance succefully started");
        assertEquals(expResult, obj);

         /*
         * we verify tat the instance has now a status WORKING
         */
        URL liUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=listInstance");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        List<Instance> instances = new ArrayList<Instance>();
        instances.add(new Instance("default", "WMS", ServiceStatus.WORKING));
        instances.add(new Instance("wms1", "WMS", ServiceStatus.WORKING));
        instances.add(new Instance("wms2", "WMS", ServiceStatus.WORKING));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);

    }

    @Test
    public void testConfigureInstance() throws Exception {

        /*
         * we configure the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=configure&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();
        List<Source> sources = new ArrayList<Source>();
        sources.add(new Source("coverageTestSrc", true, null, null));
        sources.add(new Source("shapeSrc", true, null, null));
        Layers layerObj = new Layers(sources);
        LayerContext layerContext = new LayerContext(layerObj);

        postRequestObject(conec, layerContext);
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "Instance correctly configured");
        assertEquals(expResult, obj);

        /*
         * we restart the instance to take change in count
         */
        niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=restart&id=wms2");


        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        expResult = new AcknowlegementType("Success", "instances succefully restarted");
        assertEquals(expResult, obj);

        URL gcDefaultURL = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/default?request=GetCapabilities&service=WMS&version=1.1.1");
        URL gcWms2URL    = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/wms2?request=GetCapabilities&service=WMS&version=1.1.1");

        String expCapabiliites = getStringResponse(gcDefaultURL.openConnection());
        String resCapabiliites = getStringResponse(gcWms2URL.openConnection());

        resCapabiliites = resCapabiliites.replace("http://localhost:" +  grizzly.getCurrentPort() + "/wms/wms2", "http://localhost:" +  grizzly.getCurrentPort() + "/wms/default");

        assertEquals(expCapabiliites, resCapabiliites);
    }

    @Test
    public void testStopInstance() throws Exception {
        /*
         * we stop the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=stop&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "instance succesfully stopped");
        assertEquals(expResult, obj);

         /*
         * we see the instance has now a status NOT_STARTED
         */
        URL liUrl = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/wms/admin?request=listInstance");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        List<Instance> instances = new ArrayList<Instance>();
        instances.add(new Instance("default", "WMS", ServiceStatus.WORKING));
        instances.add(new Instance("wms1", "WMS", ServiceStatus.WORKING));
        instances.add(new Instance("wms2", "WMS", ServiceStatus.NOT_STARTED));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);
    }

    @Test
    public void testDeleteInstance() throws Exception {
        /*
         * we stop the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/admin?request=delete&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "instance succesfully deleted");
        assertEquals(expResult, obj);

         /*
         * we see the instance has now a status NOT_STARTED
         */
        URL liUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/admin?request=listInstance");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        List<Instance> instances = new ArrayList<Instance>();
        instances.add(new Instance("default", "WMS", ServiceStatus.WORKING));
        instances.add(new Instance("wms1", "WMS", ServiceStatus.WORKING));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);
    }

}
