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

import org.constellation.admin.service.ConstellationServer;
import org.apache.sis.xml.MarshallerPool;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.constellation.sos.ws.soap.SOService;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.TestRunner;
import org.geotoolkit.util.FileUtilities;
import org.junit.*;
import org.opengis.parameter.GeneralParameterDescriptor;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

import javax.xml.bind.JAXBContext;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.generic.database.Automatic;
import static org.constellation.ws.embedded.ConfigurationRequestTest.writeDataFile;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class ConstellationServerTest extends AbstractGrizzlyServer {

    @BeforeClass
    public static void initPool() throws Exception {

        final File configDirectory = ConfigurationEngine.setupTestEnvironement("ConstellationServerTest");

        final File dataDirectory2 = new File(configDirectory, "dataCsw2");
        dataDirectory2.mkdir();

         writeDataFile(dataDirectory2, "urn-uuid-e8df05c2-d923-4a05-acce-2b20a27c0e58", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");

        final Automatic config2 = new Automatic("filesystem", dataDirectory2.getPath());
        config2.putParameter("shiroAccessible", "false");
        config2.putParameter("CSWCascading", "http://localhost:9090/csw/default");
        ConfigurationEngine.storeConfiguration("CSW", "csw2", config2);


        final File dataDirectory = new File(configDirectory, "dataCsw");
        dataDirectory.mkdir();

        writeDataFile(dataDirectory, "urn-uuid-19887a8a-f6b0-4a63-ae56-7fba0e17801f", "urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        writeDataFile(dataDirectory, "urn-uuid-1ef30a8b-876d-4828-9246-c37ab4510bbd", "urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");
        writeDataFile(dataDirectory, "urn-uuid-66ae76b7-54ba-489b-a582-0f0633d96493", "urn:uuid:66ae76b7-54ba-489b-a582-0f0633d96493");
        writeDataFile(dataDirectory, "urn-uuid-6a3de50b-fa66-4b58-a0e6-ca146fdd18d4", "urn:uuid:6a3de50b-fa66-4b58-a0e6-ca146fdd18d4");
        writeDataFile(dataDirectory, "urn-uuid-784e2afd-a9fd-44a6-9a92-a3848371c8ec", "urn:uuid:784e2afd-a9fd-44a6-9a92-a3848371c8ec");
        writeDataFile(dataDirectory, "urn-uuid-829babb0-b2f1-49e1-8cd5-7b489fe71a1e", "urn:uuid:829babb0-b2f1-49e1-8cd5-7b489fe71a1e");
        writeDataFile(dataDirectory, "urn-uuid-88247b56-4cbc-4df9-9860-db3f8042e357", "urn:uuid:88247b56-4cbc-4df9-9860-db3f8042e357");
        writeDataFile(dataDirectory, "urn-uuid-94bc9c83-97f6-4b40-9eb8-a8e8787a5c63", "urn:uuid:94bc9c83-97f6-4b40-9eb8-a8e8787a5c63");
        writeDataFile(dataDirectory, "urn-uuid-9a669547-b69b-469f-a11f-2d875366bbdc", "urn:uuid:9a669547-b69b-469f-a11f-2d875366bbdc");
        writeDataFile(dataDirectory, "urn-uuid-e9330592-0932-474b-be34-c3a3bb67c7db", "urn:uuid:e9330592-0932-474b-be34-c3a3bb67c7db");

        final File subDataDirectory = new File(dataDirectory, "sub1");
        subDataDirectory.mkdir();
        writeDataFile(subDataDirectory, "urn-uuid-ab42a8c4-95e8-4630-bf79-33e59241605a", "urn:uuid:ab42a8c4-95e8-4630-bf79-33e59241605a");

        final File subDataDirectory2 = new File(dataDirectory, "sub2");
        subDataDirectory2.mkdir();
        writeDataFile(subDataDirectory2, "urn-uuid-a06af396-3105-442d-8b40-22b57a90d2f2", "urn:uuid:a06af396-3105-442d-8b40-22b57a90d2f2");

        final Automatic config = new Automatic("filesystem", dataDirectory.getPath());
        config.putParameter("shiroAccessible", "false");
        ConfigurationEngine.storeConfiguration("CSW", "default", config);
        
        final Map<String, Object> map = new HashMap<>();
        map.put("sos", new SOService());
        initServer(null, map);
        // Get the list of layers
        pool = new MarshallerPool(JAXBContext.newInstance("org.constellation.configuration:"
                                + "org.constellation.generic.database:"
                                + "org.geotoolkit.ows.xml.v110:"
                                + "org.geotoolkit.csw.xml.v202:"
                                + "org.apache.sis.internal.jaxb.geometry:"
                                + "org.geotoolkit.ows.xml.v100"), null);
    }

    @AfterClass
    public static void shutDown() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        ConfigurationEngine.shutdownTestEnvironement("ConstellationServerTest");
        finish();
    }

    @Test
    @Order(order=1)
    public void testgetDescriptor() throws Exception {

        waitForStart();

        final ConstellationServer administrator = ConstellationServer.login("http://localhost:" + grizzly.getCurrentPort(), "", "");
        assertNotNull(administrator);
        GeneralParameterDescriptor desc = administrator.providers.getServiceDescriptor("feature-store");
        assertNotNull(desc);
    }

    @Test
    @Order(order=2)
    public void testImportFile() throws Exception {

        final ConstellationServer administrator = ConstellationServer.login("http://localhost:" + grizzly.getCurrentPort(), "", "");
        assertNotNull(administrator);
        final File f = FileUtilities.getFileFromResource("org.constellation.embedded.test.urn-uuid-e8df05c2-d923-4a05-acce-2b20a27c0e58.xml");

        final boolean inserted = administrator.csws.importFile("default", f, "urn-uuid-e8df05c2-d923-4a05-acce-2b20a27c0e58.xml");
        assertTrue(inserted);

        boolean exist = administrator.csws.metadataExist("default", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");
        assertTrue(exist);

        final boolean deleted = administrator.csws.deleteMetadata("default", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");
        assertTrue(deleted);

        exist = administrator.csws.metadataExist("default", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");
        assertFalse(exist);

    }

}
