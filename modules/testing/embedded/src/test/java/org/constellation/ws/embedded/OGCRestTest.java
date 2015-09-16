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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.xml.XML;
import org.constellation.ServiceDef;
import org.constellation.admin.SpringHelper;
import org.constellation.admin.service.ConstellationClient;
import org.constellation.business.IDatasetBusiness;
import org.constellation.business.IMetadataBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.Instance;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.w3c.dom.Node;

// JUnit dependencies

/**
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-context.xml")
@ActiveProfiles({"standard"})
public class OGCRestTest extends AbstractGrizzlyServer implements ApplicationContextAware {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("org.constellation.ws.embedded");

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Inject
    private IServiceBusiness serviceBusiness;

    @Inject
    private IProviderBusiness providerBusiness;

    @Inject
    private IDatasetBusiness datasetBusiness;

    @Inject
    private IMetadataBusiness metadataBusiness;

    private static ConstellationClient client;

    private static boolean initialized = false;

    private static File configDirectory;

    @BeforeClass
    public static void initTestDir() {
        configDirectory = ConfigDirectory.setupTestEnvironement("OGCRestTest");
    }

    @AfterClass
    public static void teardown() {
        try {
            final IDatasetBusiness dataset = SpringHelper.getBean(IDatasetBusiness.class);
            dataset.removeAllDatasets();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        //clean providers
        try {
            final IProviderBusiness provider = SpringHelper.getBean(IProviderBusiness.class);
            provider.removeAll();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        //clean services
        try {
            final IServiceBusiness service = SpringHelper.getBean(IServiceBusiness.class);
            if (service != null) {
                service.deleteAll();
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        ConfigDirectory.shutdownTestEnvironement("OGCRestTest");
        finish();
    }

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @PostConstruct
    public void initPool() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                //clean datasets
                try {
                    datasetBusiness.removeAllDatasets();
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }

                //clean providers
                try {
                    providerBusiness.removeAll();
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }

                //clean services
                try {
                    serviceBusiness.deleteAll();
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }

                final File dataDirectory2 = new File(configDirectory, "dataCsw2");
                dataDirectory2.mkdir();

                final Automatic config2 = new Automatic("filesystem", dataDirectory2.getPath());
                config2.putParameter("shiroAccessible", "false");
                serviceBusiness.create("csw", "default", config2, null);

                createDataset("meta1.xml", "42292_5p_19900609195600");

                Automatic configuration = new Automatic("internal", (String) null);
                configuration.putParameter("shiroAccessible", "false");
                serviceBusiness.create("csw", "intern", configuration, null);

                initServer(null, null, "api");
                pool = GenericDatabaseMarshallerPool.getInstance();
                initialized = true;
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    @Test
    public void testGetConfiguration() throws Exception {
        waitForStart();

        client = new ConstellationClient("http://localhost:" + grizzly.getCurrentPort() + "/");

        Instance in = client.services.getInstance(ServiceDef.Specification.CSW, "default");

        assertNotNull(in);

        Object s = client.services.getInstanceConfiguration(ServiceDef.Specification.CSW, "default");

        assertTrue(s instanceof Automatic);

        Automatic auto = (Automatic) s;

        auto.setFormat("TEST");

        client.services.setInstanceConfiguration(ServiceDef.Specification.CSW, "default", auto);

        s = client.services.getInstanceConfiguration(ServiceDef.Specification.CSW, "default");

        assertTrue(s instanceof Automatic);

        auto = (Automatic) s;

        assertEquals("TEST", auto.getFormat());
    }

    @Test
    public void testMetadata() throws Exception {
        waitForStart();

        client = new ConstellationClient("http://localhost:" + grizzly.getCurrentPort() + "/");
        client.connectTimeout(3000000);
        client.readTimeout(3000000);
        final Node node = client.csw.getMetadata("intern", "42292_5p_19900609195600");
        assertNotNull(node);
        assertEquals("MD_Metadata", node.getLocalName());
    }

    public void createDataset(String resourceName, String identifier) throws Exception {

        Unmarshaller u = CSWMarshallerPool.getInstance().acquireUnmarshaller();
        u.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
        DefaultMetadata meta = (DefaultMetadata) u.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/" + resourceName));
        CSWMarshallerPool.getInstance().recycle(u);

        Marshaller m = CSWMarshallerPool.getInstance().acquireMarshaller();
        m.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
        final StringWriter sw = new StringWriter();
        m.marshal(meta, sw);
        CSWMarshallerPool.getInstance().recycle(m);

        datasetBusiness.createDataset(identifier, sw.toString(), null);
    }

    /**
     * used for debug
     *
     * @param n
     * @return
     * @throws Exception
     */
    private static String getStringFromNode(final Node n) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(n), new StreamResult(writer));
        String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        return output;
    }
}
