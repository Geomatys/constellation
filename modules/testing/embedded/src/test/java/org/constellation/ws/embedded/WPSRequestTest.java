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
package org.constellation.ws.embedded;

// JUnit dependencies
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.bind.JAXBException;
import org.geotoolkit.wcs.xml.WCSMarshallerPool;
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
    
    private static final String WPS_DEFAULT = "http://localhost:9090/wps/default?";
    
    private static final String WPS_TEST = "http://localhost:9090/wps/test?";
    
    private static final String WPS_GETCAPABILITIES =
            "http://localhost:9090/wps/default?request=GetCapabilities&service=WPS&version=1.0.0";

    private static final String WPS_GETCAPABILITIES2 =
            "http://localhost:9090/wps/test?request=GetCapabilities&service=WpS&version=1.0.0";
    
    @BeforeClass
    public static void initLayerList() throws JAXBException {
        pool = WPSMarshallerPool.getInstance();
    }
      
    @AfterClass
    public static void finish() {
        layers = null;
    }
    
    /**
     * Ensures that a valid GetCapabilities request returns indeed a valid GetCapabilities
     * document representing the server capabilities in the WCS version 1.0.0 standard.
     */
    @Test
    public void testWPSGetCapabilities() throws JAXBException, IOException {

        // Creates a valid GetCapabilities url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL(WPS_GETCAPABILITIES);
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
        assertEquals(WPS_DEFAULT, currentUrl);
        
        try {
            getCapsUrl = new URL(WPS_GETCAPABILITIES2);
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
        assertEquals(WPS_TEST, currentUrl);
        
        
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
        assertEquals(WPS_DEFAULT, currentUrl);
    }

}
