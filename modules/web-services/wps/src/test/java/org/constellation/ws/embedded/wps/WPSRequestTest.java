/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.ws.embedded.wps;

// JUnit dependencies
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.bind.JAXBException;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.geotoolkit.wps.xml.v100.WPSCapabilitiesType;
import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.9
 */
public class WPSRequestTest  extends AbstractTestRequest {

    private static final String WPS_GETCAPABILITIES ="request=GetCapabilities&service=WPS&version=1.0.0";

    private static final String WPS_GETCAPABILITIES2 ="request=GetCapabilities&service=WpS&version=1.0.0";

    @BeforeClass
    public static void initLayerList() throws JAXBException {
        pool = WPSMarshallerPool.getInstance();
    }

    @AfterClass
    public static void finish() {
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
        assertTrue(obj instanceof WPSCapabilitiesType);

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
            getCapsUrl = new URL(WPS_GETCAPABILITIES);
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

}
