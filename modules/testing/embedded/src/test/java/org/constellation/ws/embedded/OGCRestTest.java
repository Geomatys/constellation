/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
import org.constellation.ServiceDef;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.service.ConstellationClient;
import org.constellation.configuration.Instance;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import static org.constellation.ws.embedded.AbstractGrizzlyServer.grizzly;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class OGCRestTest extends AbstractGrizzlyServer {

    private static ConstellationClient client;
    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initPool() throws Exception {
        final File configDirectory = ConfigurationEngine.setupTestEnvironement("OGCRestTest");
        final File dataDirectory2 = new File(configDirectory, "dataCsw2");
        dataDirectory2.mkdir();


        final Automatic config2 = new Automatic("filesystem", dataDirectory2.getPath());
        config2.putParameter("shiroAccessible", "false");
        ConfigurationEngine.storeConfiguration("CSW", "default", config2);

        initServer(null, null, "api");
        pool = GenericDatabaseMarshallerPool.getInstance();

    }

    @AfterClass
    public static void shutDown() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        ConfigurationEngine.shutdownTestEnvironement("OGCRestTest");
        finish();
    }

    @Test
    public void testGetConfiguration() throws Exception {
        waitForStart();

        client = new ConstellationClient("http://localhost:" +  grizzly.getCurrentPort() + "/");

        Instance in = client.services.getInstance(ServiceDef.Specification.CSW, "default");

        assertNotNull(in);

        //update the federated catalog in case of busy port
        URL url = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/api/1/OGC/CSW/default/config");
        URLConnection conec = url.openConnection();

        Object s = unmarshallResponse(url);

        assertTrue(s instanceof Automatic);

        Automatic auto = (Automatic) s;

        auto.setFormat("DTC");

        url = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/api/1/OGC/CSW/default/config");
        conec = url.openConnection();

        postRequestObject(conec, auto);
        unmarshallResponse(conec);

        url = new URL("http://localhost:" +  grizzly.getCurrentPort() + "/api/1/OGC/CSW/default/config");

        s = unmarshallResponse(url);

        assertTrue(s instanceof Automatic);

        auto = (Automatic) s;

        assertEquals("DTC", auto.getFormat());


    }
}
