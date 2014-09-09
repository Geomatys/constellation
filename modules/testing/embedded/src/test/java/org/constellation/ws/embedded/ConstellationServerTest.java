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

import org.apache.sis.xml.MarshallerPool;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.SpringHelper;
import org.constellation.admin.service.ConstellationClient;
import org.constellation.configuration.ConfigurationException;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.ws.soap.SOService;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.geotoolkit.util.FileUtilities;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.ws.embedded.ConfigurationRequestTest.writeDataFile;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Ignore //issue with Spring security filter and grizzly
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
public class ConstellationServerTest extends AbstractGrizzlyServer implements ApplicationContextAware {

    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Inject
    private IServiceBusiness serviceBusiness;
    
    private static boolean initialized = false;
    
    @PostConstruct
    public void initPool() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                try {
                    serviceBusiness.delete("csw", "default");
                    serviceBusiness.delete("csw", "csw2");
                } catch (ConfigurationException ex) {}
                
                final File configDirectory = ConfigDirectory.setupTestEnvironement("ConstellationServerTest");

                final File dataDirectory2 = new File(configDirectory, "dataCsw2");
                dataDirectory2.mkdir();

                writeDataFile(dataDirectory2, "urn-uuid-e8df05c2-d923-4a05-acce-2b20a27c0e58", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");

                final Automatic config2 = new Automatic("filesystem", dataDirectory2.getPath());
                config2.putParameter("shiroAccessible", "false");
                config2.putParameter("CSWCascading", "http://localhost:9090/csw/default");
                serviceBusiness.create("csw", "csw2", config2, null, null);


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
                serviceBusiness.create("csw", "default", config, null, null);

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
                initialized = true;
            } catch (Exception ex) {
                Logger.getLogger(ConstellationServerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void shutDown() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        ConfigDirectory.shutdownTestEnvironement("ConstellationServerTest");
        finish();
    }

    @Test
    @Order(order=1)
    public void testgetDescriptor() throws Exception {

        waitForStart();

        final ConstellationClient client = new ConstellationClient("http://localhost:" + grizzly.getCurrentPort());
        final ConstellationClient administrator = client.auth("", "");
        assertNotNull(administrator);
        GeneralParameterDescriptor desc = administrator.providers.getServiceDescriptor("feature-store");
        assertNotNull(desc);
    }

    @Test
    @Order(order=2)
    public void testImportFile() throws Exception {

        final ConstellationClient client = new ConstellationClient("http://localhost:" + grizzly.getCurrentPort());
        final ConstellationClient administrator = client.auth("", "");
        assertNotNull(administrator);
        final File f = FileUtilities.getFileFromResource("org.constellation.embedded.test.urn-uuid-e8df05c2-d923-4a05-acce-2b20a27c0e58.xml");

        final boolean inserted = administrator.csw.importMetadata("default", f);
        assertTrue(inserted);

        boolean exist = administrator.csw.metadataExist("default", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");
        assertTrue(exist);

        final boolean deleted = administrator.csw.deleteMetadata("default", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");
        assertTrue(deleted);

        exist = administrator.csw.metadataExist("default", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");
        assertFalse(exist);

    }

}
