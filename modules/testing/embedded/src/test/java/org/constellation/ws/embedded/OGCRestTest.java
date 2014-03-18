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
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.TimeZone;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.sis.xml.XML;
import org.constellation.ServiceDef;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.admin.service.ConstellationClient;
import org.constellation.configuration.Instance;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.provider.DataProviders;
import org.constellation.provider.DataProviderFactory;
import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.constellation.provider.configuration.ProviderParameters.getOrCreate;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;
import org.constellation.util.Util;
import static org.constellation.ws.embedded.AbstractGrizzlyServer.grizzly;
import org.geotoolkit.csw.xml.CSWMarshallerPool;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;
import org.opengis.parameter.ParameterValueGroup;
import org.w3c.dom.Node;

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

        writeProvider("meta1.xml",  "42292_5p_19900609195600");
        
        Automatic configuration = new Automatic("internal", (String)null);
        configuration.putParameter("shiroAccessible", "false");
        ConfigurationEngine.storeConfiguration("CSW", "intern", configuration);

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

    public static void writeProvider(String resourceName, String identifier) throws Exception {

        final DataProviderFactory service = DataProviders.getInstance().getService("coverage-sql");
        final ParameterValueGroup config = service.getServiceDescriptor().createValue();
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
        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue(identifier);

        Unmarshaller u = CSWMarshallerPool.getInstance().acquireUnmarshaller();
        u.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
        Object meta = u.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/" + resourceName));
        CSWMarshallerPool.getInstance().recycle(u);

        ConfigurationEngine.writeProvider(identifier, ProviderRecord.ProviderType.LAYER, service.getName(), source);
        ConfigurationEngine.saveMetaData(meta, identifier, CSWMarshallerPool.getInstance());
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
