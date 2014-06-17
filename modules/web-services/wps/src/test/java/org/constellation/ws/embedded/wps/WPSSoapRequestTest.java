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
package org.constellation.ws.embedded.wps;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.ServiceBusiness;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.ProcessFactory;
import org.constellation.configuration.Processes;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.wps.ws.soap.WPSService;
import org.constellation.ws.embedded.AbstractGrizzlyServer;
import org.geotoolkit.util.StringUtilities;

import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class WPSSoapRequestTest extends AbstractGrizzlyServer {

    @Inject
    private ServiceBusiness serviceBusiness;
    
    @PostConstruct
    public void initLayerList() {
        try {
            ConfigurationEngine.setupTestEnvironement("WPSSoapRequestTest");
            
            final List<ProcessFactory> process = Arrays.asList(new ProcessFactory("jts", true));
            final Processes processes = new Processes(process);
            final ProcessContext config = new ProcessContext(processes);
            config.getCustomParameters().put("shiroAccessible", "false");
            
            serviceBusiness.create("WPS", "default", config, null);
            serviceBusiness.create("WPS", "test",    config, null);
            
            final Map<String, Object> map = new HashMap<>();
            map.put("wps", new WPSService());
            initServer(new String[] {
                "org.constellation.wps.ws.rs",
                "org.constellation.configuration.ws.rs",
                "org.constellation.ws.rs.provider"}, map);
        } catch (Exception ex) {
            Logger.getLogger(WPSSoapRequestTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void shutdown() {
        ConfigurationEngine.shutdownTestEnvironement("WPSSoapRequestTest");
        finish();
    }

    /**
     */
    @Test
    public void testWPSGetCapabilities() throws Exception {

        waitForStart();
        // Creates a valid GetCapabilities url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?");
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        URLConnection conec = getCapsUrl.openConnection();
        postRequestFile(conec, "org/constellation/xml/wps/GetCapabilitiesSOAP.xml", "application/soap+xml");

        final String result    = cleanXMlString(getStringResponse(conec));
        final String expResult = cleanXMlString(getStringFromFile("org/constellation/xml/wps/GetCapabilitiesResponseSOAP.xml"));

        assertEquals(expResult, result);
    }

    /**
     */
    @Test
    public void testWPSDescribeProcess() throws JAXBException, IOException {

        // Creates a valid GetCapabilities url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?");
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        URLConnection conec = getCapsUrl.openConnection();
        postRequestFile(conec, "org/constellation/xml/wps/DescribeProcessSOAP.xml", "application/soap+xml");

        final String result    = cleanXMlString(getStringResponse(conec));
        final String expResult = cleanXMlString(getStringFromFile("org/constellation/xml/wps/DescribeProcessResponseSOAP.xml"));

        assertEquals(expResult, result);

    }

    /**
     */
    @Test
    public void testWPSExecute() throws JAXBException, IOException {

        // Creates a valid GetCapabilities url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?");
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        URLConnection conec = getCapsUrl.openConnection();
        postRequestFile(conec, "org/constellation/xml/wps/ExecuteSOAP.xml", "application/soap+xml");

        final String result    = cleanXMlString(getStringResponse(conec));
        final String expResult = cleanXMlString(getStringFromFile("org/constellation/xml/wps/ExecuteResponseSOAP.xml"));

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
}
