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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.bind.JAXBException;
import org.geotoolkit.util.StringUtilities;

import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WPSSoapRequestTest extends AbstractTestSoapRequest {
    
    private static final String WPS_DEFAULT = "http://localhost:9191/wps/default?";
    
    private static final String WPS_TEST = "http://localhost:9191/wps/test?";
    
    
    @BeforeClass
    public static void initLayerList() throws JAXBException {
    }
      
    @AfterClass
    public static void finish() {
    }
    
    /**
     */
    @Test
    public void testWPSGetCapabilities() throws JAXBException, IOException {

        // Creates a valid GetCapabilities url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL(WPS_DEFAULT);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        
        URLConnection conec = getCapsUrl.openConnection();
        postRequestFile(conec, "org/constellation/xml/wps/GetCapabilitiesSOAP.xml");
        
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
            getCapsUrl = new URL(WPS_DEFAULT);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        
        URLConnection conec = getCapsUrl.openConnection();
        postRequestFile(conec, "org/constellation/xml/wps/DescribeProcessSOAP.xml");
        
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
            getCapsUrl = new URL(WPS_DEFAULT);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        
        URLConnection conec = getCapsUrl.openConnection();
        postRequestFile(conec, "org/constellation/xml/wps/ExecuteSOAP.xml");
        
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
