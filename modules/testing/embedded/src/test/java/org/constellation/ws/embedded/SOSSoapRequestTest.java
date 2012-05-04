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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.bind.JAXBException;
import org.geotoolkit.sampling.xml.v100.SamplingPointType;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterest;

import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SOSSoapRequestTest extends AbstractTestSoapRequest {
    
    private static final String SOS_DEFAULT = "http://localhost:9191/sos/default?";
    
    @BeforeClass
    public static void initLayerList() throws JAXBException {
    }
      
    @AfterClass
    public static void finish() {
    }
    
    /**
     */
    @Test
    public void testSOSGetCapabilities() throws JAXBException, IOException {

        // Creates a valid GetCapabilities url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL(SOS_DEFAULT);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        
        URLConnection conec = getCapsUrl.openConnection();
        postRequestFile(conec, "org/constellation/xml/sos/GetCapabilitiesSOAP.xml");
        
        final String result    = getStringResponse(conec);
        final String expResult = getStringFromFile("org/constellation/xml/sos/GetCapabilitiesResponseSOAP.xml");
        
        assertEquals(expResult, result);
    }
    
    @Test
    public void testSOSGetFeatureOfInterest() throws Exception {
        // Creates a valid GetObservation url.
        final URL getCapsUrl = new URL(SOS_DEFAULT);

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/sos/GetFeatureOfInterestSOAP.xml");
        
        String result    = getStringResponse(conec);
        String expResult = getStringFromFile("org/constellation/xml/sos/GetFeatureOfInterestResponseSOAP.xml");
        System.out.println("result:\n" + result);
        
        assertEquals(expResult, result);
        
        conec = getCapsUrl.openConnection();
        
        postRequestFile(conec, "org/constellation/xml/sos/GetFeatureOfInterestSOAP2.xml");
        
        result    = getStringResponse(conec);
        expResult = getStringFromFile("org/constellation/xml/sos/GetFeatureOfInterestResponseSOAP2.xml");
        System.out.println("result:\n" + result);
        
        assertEquals(expResult, result);
    }
}
