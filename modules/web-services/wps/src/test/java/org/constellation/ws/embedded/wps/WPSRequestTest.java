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

// JUnit dependencies

import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.*;
import org.constellation.admin.SpringHelper;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.wps.ws.soap.WPSService;
import org.constellation.ws.embedded.AbstractGrizzlyServer;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.geotoolkit.wps.xml.v100.WPSCapabilitiesType;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.sis.util.logging.Logging;
import org.constellation.webdav.AdminWebdavService;
import org.geotoolkit.wps.xml.v100.ExecuteResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;
import org.junit.BeforeClass;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.9
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-context.xml")
@ActiveProfiles({"standard"})
 public class WPSRequestTest extends AbstractGrizzlyServer implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static final String WPS_GETCAPABILITIES ="request=GetCapabilities&service=WPS&version=1.0.0";

    private static final String WPS_GETCAPABILITIES2 ="request=GetCapabilities&service=WpS&version=1.0.0";

    @Inject
    private IServiceBusiness serviceBusiness;

    private static boolean initialized = false;

    @BeforeClass
    public static void initTestDir() {
        ConfigDirectory.setupTestEnvironement("WPSRequestTest");
    }

    @PostConstruct
    public void initLayerList() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                try {
                    serviceBusiness.deleteAll();
                } catch (ConfigurationException ex) {}

                final List<ProcessFactory> process = Arrays.asList(new ProcessFactory("jts", true));
                final Processes processes = new Processes(process);
                final ProcessContext config = new ProcessContext(processes);
                config.getCustomParameters().put("shiroAccessible", "false");

                serviceBusiness.create("wps", "default", config, null);
                serviceBusiness.create("wps", "test",    config, null);

                new AdminWebdavService(); // force webdav registration
                final Map<String, Object> map = new HashMap<>();
                map.put("wps", new WPSService());
                initServer(null, map);
                pool = WPSMarshallerPool.getInstance();

                initialized = true;
            } catch (Exception ex) {
                Logging.getLogger("org.constellation.ws.embedded.wps").log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void shutDown() throws Exception {
        final IServiceBusiness service = SpringHelper.getBean(IServiceBusiness.class);
        if (service != null) {
            service.delete("wps", "default");
            service.delete("wps", "test");
        }
        ConfigDirectory.shutdownTestEnvironement("WPSRequestTest");
        finish();
    }

    /**
     * Ensures that a valid GetCapabilities request returns indeed a valid GetCapabilities
     */
    @Test
    public void testWPSGetCapabilities() throws Exception {

        waitForStart();
        // Creates a valid GetCapabilities url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?" + WPS_GETCAPABILITIES);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WPSCapabilitiesType.
        Object obj = unmarshallResponse(getCapsUrl);
        assertTrue("was " + obj, obj instanceof WPSCapabilitiesType);

        WPSCapabilitiesType responseCaps = (WPSCapabilitiesType)obj;


        String currentUrl = responseCaps.getOperationsMetadata().getOperation("getCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?", currentUrl);

        try {
            getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wps/test?" + WPS_GETCAPABILITIES2);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WPSCapabilitiesType.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WPSCapabilitiesType);

        responseCaps = (WPSCapabilitiesType)obj;

        currentUrl = responseCaps.getOperationsMetadata().getOperation("getCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wps/test?", currentUrl);


        try {
            getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?" + WPS_GETCAPABILITIES);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WPSCapabilitiesType.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WPSCapabilitiesType);

        responseCaps = (WPSCapabilitiesType)obj;

        currentUrl = responseCaps.getOperationsMetadata().getOperation("getCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?", currentUrl);
    }
    
    @Ignore // TODO FIXME when GEOTK-480 is completed
    public void testWPSExecute1() throws Exception {

        waitForStart();
        
        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?");


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/wps/xml/executeRequest.xml");
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ExecuteResponse);
    }
    
    @Test
    public void testWPSExecute2() throws Exception {

        waitForStart();
        
        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?");


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/wps/xml/executeRequest2.xml");
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ExecuteResponse);
    }
    
    @Test
    public void testWPSExecute3() throws Exception {

        waitForStart();
        
        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?");


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/wps/xml/executeRequest3.xml");
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ExecuteResponse);
    }

}
