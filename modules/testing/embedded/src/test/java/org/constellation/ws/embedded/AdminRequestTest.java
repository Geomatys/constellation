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

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class AdminRequestTest extends AbstractTestRequest {

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initPool() throws JAXBException {
        // Get the list of layers
        pool = GenericDatabaseMarshallerPool.getInstance();
    }

    @AfterClass
    public static void finish() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void testNewInstance() throws Exception {

        /*
         * we build a new instance
         */
        URL niUrl = new URL("http://localhost:9090/wms/admin?request=newInstance&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);
        
        AcknowlegementType expResult = new AcknowlegementType("Success", "instance succefully created");
        assertEquals(expResult, obj);

        /*
         * we see the instance with a status NOT_STARTED
         */
        URL liUrl = new URL("http://localhost:9090/wms/admin?request=listInstance");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        List<Instance> instances = new ArrayList<Instance>();
        instances.add(new Instance("default", ServiceStatus.WORKING));
        instances.add(new Instance("wms1", ServiceStatus.WORKING));
        instances.add(new Instance("wms2", ServiceStatus.NOT_STARTED));
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
        URL niUrl = new URL("http://localhost:9090/wms/admin?request=start&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "new instance succefully started");
        assertEquals(expResult, obj);

         /*
         * we verify tat the instance has now a status WORKING
         */
        URL liUrl = new URL("http://localhost:9090/wms/admin?request=listInstance");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        List<Instance> instances = new ArrayList<Instance>();
        instances.add(new Instance("default", ServiceStatus.WORKING));
        instances.add(new Instance("wms1", ServiceStatus.WORKING));
        instances.add(new Instance("wms2", ServiceStatus.WORKING));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);

    }

    @Test
    public void testConfigureInstance() throws Exception {

        /*
         * we configure the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:9090/wms/admin?request=configure&id=wms2");


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
        niUrl = new URL("http://localhost:9090/wms/admin?request=restart&id=wms2");


        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        expResult = new AcknowlegementType("Success", "instances succefully restarted");
        assertEquals(expResult, obj);

        URL gcDefaultURL = new URL("http://localhost:9090/wms/default?request=GetCapabilities&service=WMS&version=1.1.1");
        URL gcWms2URL    = new URL("http://localhost:9090/wms/wms2?request=GetCapabilities&service=WMS&version=1.1.1");

        String expCapabiliites = getStringResponse(gcDefaultURL.openConnection());
        String resCapabiliites = getStringResponse(gcWms2URL.openConnection());

        resCapabiliites = resCapabiliites.replace("http://localhost:9090/wms/wms2", "http://localhost:9090/wms/default");

        assertEquals(expCapabiliites, resCapabiliites);
    }

    @Test
    public void testStopInstance() throws Exception {
        /*
         * we stop the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:9090/wms/admin?request=stop&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "instance succesfully stopped");
        assertEquals(expResult, obj);

         /*
         * we see the instance has now a status NOT_STARTED
         */
        URL liUrl = new URL("http://localhost:9090/wms/admin?request=listInstance");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        List<Instance> instances = new ArrayList<Instance>();
        instances.add(new Instance("default", ServiceStatus.WORKING));
        instances.add(new Instance("wms1", ServiceStatus.WORKING));
        instances.add(new Instance("wms2", ServiceStatus.NOT_STARTED));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);
    }

    @Test
    public void testDeleteInstance() throws Exception {
        /*
         * we stop the instance created at the previous test
         */
        URL niUrl = new URL("http://localhost:9090/wms/admin?request=delete&id=wms2");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);

        AcknowlegementType expResult = new AcknowlegementType("Success", "instance succesfully deleted");
        assertEquals(expResult, obj);

         /*
         * we see the instance has now a status NOT_STARTED
         */
        URL liUrl = new URL("http://localhost:9090/wms/admin?request=listInstance");


        // for a POST request
        conec = liUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof InstanceReport);

        List<Instance> instances = new ArrayList<Instance>();
        instances.add(new Instance("default", ServiceStatus.WORKING));
        instances.add(new Instance("wms1", ServiceStatus.WORKING));
        InstanceReport expResult2 = new InstanceReport(instances);
        assertEquals(expResult2, obj);
    }

}
