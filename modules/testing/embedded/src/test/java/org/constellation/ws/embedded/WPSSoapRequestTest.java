/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.constellation.ws.embedded;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.bind.JAXBException;
import org.constellation.util.Util;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.geotoolkit.wps.xml.v100.WPSCapabilitiesType;

import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

/**
 *
 * @author guilhem
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
        
        final String result    = getStringResponse(conec);
        final String expResult = getStringFromFile("org/constellation/xml/wps/DescribeProcessResponseSOAP.xml");
        
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
        
        final String result    = getStringResponse(conec);
        final String expResult = getStringFromFile("org/constellation/xml/wps/ExecuteResponseSOAP.xml");
        
        assertEquals(expResult, result);

    }
}
