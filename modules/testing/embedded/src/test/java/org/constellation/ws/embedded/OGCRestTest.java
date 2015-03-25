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

import static org.constellation.provider.configuration.ProviderParameters.SOURCE_ID_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.SOURCE_LOADALL_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.getOrCreate;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.COVERAGESQL_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.NAMESPACE_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.PASSWORD_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.ROOT_DIRECTORY_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.SCHEMA_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.URL_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.USER_DESCRIPTOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.constellation.api.ProviderType;
import org.constellation.business.IDatasetBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Instance;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.DataProviders;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.w3c.dom.Node;

// JUnit dependencies

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
@ActiveProfiles({"standard","derby"})
public class OGCRestTest extends AbstractGrizzlyServer implements ApplicationContextAware {

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
    
    private static ConstellationClient client;
    
    private static boolean initialized = false;
    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @PostConstruct
    public void initPool() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                final File configDirectory = ConfigDirectory.setupTestEnvironement("OGCRestTest");
                final File dataDirectory2 = new File(configDirectory, "dataCsw2");
                dataDirectory2.mkdir();

                try {
                    serviceBusiness.delete("csw", "default");
                    serviceBusiness.delete("csw", "intern");
                } catch (ConfigurationException ex) {}

                final Automatic config2 = new Automatic("filesystem", dataDirectory2.getPath());
                config2.putParameter("shiroAccessible", "false");
                serviceBusiness.create("csw", "default", config2, null, null);

                writeProvider("meta1.xml",  "42292_5p_19900609195600");

                Automatic configuration = new Automatic("internal", (String)null);
                configuration.putParameter("shiroAccessible", "false");
                serviceBusiness.create("csw", "intern", configuration, null, null);

                initServer(null, null, "api");
                pool = GenericDatabaseMarshallerPool.getInstance();
                initialized = true;
            } catch (Exception ex) {
                Logger.getLogger(OGCRestTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void shutDown() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        ConfigDirectory.shutdownTestEnvironement("OGCRestTest");
        finish();
    }

    @Test
    public void testGetConfiguration() throws Exception {
        waitForStart();

        client = new ConstellationClient("http://localhost:" +  grizzly.getCurrentPort() + "/");

        Instance in = client.services.getInstance(ServiceDef.Specification.CSW, "default");

        assertNotNull(in);

        Object s = client.services.getInstanceConfiguration(ServiceDef.Specification.CSW, "default");

        assertTrue(s instanceof Automatic);

        Automatic auto = (Automatic) s;

        auto.setFormat("TEST");

        client.services.setInstanceConfiguration(ServiceDef.Specification.CSW, "default", auto);

        s = client.services.getInstanceConfiguration(ServiceDef.Specification.CSW, "default");;

        assertTrue(s instanceof Automatic);

        auto = (Automatic) s;

        assertEquals("TEST", auto.getFormat());
    }

    @Test
    public void testMetadata() throws Exception {
        waitForStart();
        
        client = new ConstellationClient("http://localhost:" +  grizzly.getCurrentPort() + "/");
        client.connectTimeout(3000000);
        client.readTimeout(3000000);
        final Node node = client.csw.getMetadata("intern", "42292_5p_19900609195600");
        assertNotNull(node);
        assertEquals("MD_Metadata", node.getLocalName());
    }

    public void writeProvider(String resourceName, String identifier) throws Exception {

        final DataProviderFactory service = DataProviders.getInstance().getFactory("coverage-sql");
        final ParameterValueGroup source = service.getProviderDescriptor().createValue();
        final ParameterValueGroup srcconfig = getOrCreate(COVERAGESQL_DESCRIPTOR,source);
        srcconfig.parameter(URL_DESCRIPTOR.getName().getCode()).setValue("jdbc:postgresql://flupke.geomatys.com/coverages-test");
        srcconfig.parameter(PASSWORD_DESCRIPTOR.getName().getCode()).setValue("test");
        final String rootDir = System.getProperty("java.io.tmpdir") + "/Constellation/images";
        srcconfig.parameter(ROOT_DIRECTORY_DESCRIPTOR.getName().getCode()).setValue(rootDir);
        srcconfig.parameter(USER_DESCRIPTOR.getName().getCode()).setValue("test");
        srcconfig.parameter(SCHEMA_DESCRIPTOR.getName().getCode()).setValue("coverages");
        srcconfig.parameter(NAMESPACE_DESCRIPTOR.getName().getCode()).setValue("no namespace");
        source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue(identifier);

        Unmarshaller u = CSWMarshallerPool.getInstance().acquireUnmarshaller();
        u.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
        DefaultMetadata meta = (DefaultMetadata) u.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/" + resourceName));
        CSWMarshallerPool.getInstance().recycle(u);

        Marshaller m = CSWMarshallerPool.getInstance().acquireMarshaller();
        m.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
        final StringWriter sw = new StringWriter();
        m.marshal(meta, sw);
        CSWMarshallerPool.getInstance().recycle(m);
        
        final Provider prov = providerBusiness.storeProvider(identifier, null, ProviderType.LAYER, service.getName(), source);
        datasetBusiness.createDataset(identifier, meta.getFileIdentifier(), sw.toString(), null);
    }

    /**
     * used for debug
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
