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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.ServiceBusiness;
import org.constellation.admin.SpringHelper;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.ws.soap.SOService;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.util.StringUtilities;

import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.runner.RunWith;
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
public class SOSSoapRequestTest extends AbstractGrizzlyServer implements ApplicationContextAware {

    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static final String SOS_DEFAULT = "http://localhost:9191/sos/default?";

    @Inject
    private ServiceBusiness serviceBusiness;
    
    private static boolean initialized = false;
    
    @PostConstruct
    public void initLayerList() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                try {
                    serviceBusiness.delete("sos", "default");
                } catch (ConfigurationException ex) {}
                
                final File configDirectory = ConfigurationEngine.setupTestEnvironement("SOSSoapRequestTest");

                final File dataDirectory = new File(configDirectory, "dataSos");
                dataDirectory.mkdir();

                writeDataFile(dataDirectory, "urn-ogc-object-sensor-SunSpot-0014.4F01.0000.261A");
                writeDataFile(dataDirectory, "urn-ogc-object-sensor-SunSpot-0014.4F01.0000.2626");
                writeDataFile(dataDirectory, "urn-ogc-object-sensor-SunSpot-2");

                final Automatic smlConfig = new Automatic(null, dataDirectory.getPath());
                final Automatic omCOnfig = new Automatic(null, new BDD("org.postgresql.Driver", "jdbc:postgresql://flupke.geomatys.com:5432/observation", "test", "test"));
                final SOSConfiguration sosconf = new SOSConfiguration(smlConfig, omCOnfig);
                sosconf.setObservationFilterType(DataSourceType.POSTGRID);
                sosconf.setObservationReaderType(DataSourceType.POSTGRID);
                sosconf.setObservationWriterType(DataSourceType.POSTGRID);
                sosconf.setSMLType(DataSourceType.FILESYSTEM);
                sosconf.setProfile("transactional");
                sosconf.setObservationIdBase("urn:ogc:object:observation:SunSpot:");
                sosconf.setSensorIdBase("urn:ogc:object:sensor:SunSpot:");
                sosconf.setPhenomenonIdBase("urn:phenomenon:");
                sosconf.setObservationTemplateIdBase("urn:ogc:object:observationTemplate:SunSpot:");
                sosconf.setVerifySynchronization(false);

                serviceBusiness.create("sos", "default", sosconf, null, null);

                final Map<String, Object> map = new HashMap<>();
                map.put("sos", new SOService());
                initServer(null, map);
                initialized = true;
            } catch (Exception ex) {
                Logger.getLogger(SOSSoapRequestTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void shutDown() {
        ConfigurationEngine.shutdownTestEnvironement("SOSSoapRequestTest");
        finish();
    }

    /**
     */
    @Test
    @Order(order=1)
    public void testSOSGetCapabilities() throws Exception {

        waitForStart();
        
        // Creates a valid GetCapabilities url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL(SOS_DEFAULT);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        URLConnection conec = getCapsUrl.openConnection();
        postRequestFile(conec, "org/constellation/xml/sos/GetCapabilitiesSOAP.xml", "application/soap+xml");

        String result    = getStringResponse(conec);
        String expResult = getStringFromFile("org/constellation/xml/sos/GetCapabilitiesResponseSOAP.xml");

        result = cleanXMlString(result);
        expResult = cleanXMlString(expResult);
        assertEquals(expResult, result);
    }

    @Test
    @Order(order=2)
    public void testSOSGetFeatureOfInterest() throws Exception {
        // Creates a valid GetObservation url.
        final URL getCapsUrl = new URL(SOS_DEFAULT);

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/sos/GetFeatureOfInterestSOAP.xml", "application/soap+xml");

        String result    = getStringResponse(conec);
        String expResult = getStringFromFile("org/constellation/xml/sos/GetFeatureOfInterestResponseSOAP.xml");

        result = cleanXMlString(result);
        expResult = cleanXMlString(expResult);

        System.out.println("result:\n" + result);

        assertEquals(expResult, result);

        conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/sos/GetFeatureOfInterestSOAP2.xml", "application/soap+xml");

        result    = getStringResponse(conec);
        expResult = getStringFromFile("org/constellation/xml/sos/GetFeatureOfInterestResponseSOAP2.xml");


        result = cleanXMlString(result);
        expResult = cleanXMlString(expResult);

        System.out.println("result:\n" + result);

        assertEquals(expResult, result);
    }

    private static String cleanXMlString(String s) {
        s = s.substring(s.indexOf('>') + 1);
        s = StringUtilities.removeXmlns(s);
        for (int i = 0; i< 17; i++) {
            s = StringUtilities.removePrefix("ns" + i);
        }

        return s;
    }

    public static void writeDataFile(File dataDirectory, String resourceName) throws IOException {

        final File dataFile;
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            final String windowsIdentifier = resourceName.replace(':', '-');
            dataFile = new File(dataDirectory, windowsIdentifier + ".xml");
        } else {
            dataFile = new File(dataDirectory, resourceName + ".xml");
        }
        FileWriter fw = new FileWriter(dataFile);
        InputStream in = Util.getResourceAsStream("org/constellation/embedded/test/" + resourceName + ".xml");

        byte[] buffer = new byte[1024];
        int size;

        while ((size = in.read(buffer, 0, 1024)) > 0) {
            fw.write(new String(buffer, 0, size));
        }
        in.close();
        fw.close();
    }
}
