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
public class WPSSoapRequestTest extends AbstractGrizzlyServer {
    
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
        
        
        System.out.println(getStringResponse(conec));
        

    }

    public void postRequestFile(URLConnection conec, String filePath) throws IOException {

        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "application/soap+xml");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        final InputStream is = Util.getResourceAsStream(filePath);
        final StringWriter sw = new StringWriter();
        final BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        char[] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        wr.write(sw.toString());
        wr.flush();
        in.close();

    }
    
    public String getStringResponse(URLConnection conec) throws UnsupportedEncodingException, IOException {
        final StringWriter sw     = new StringWriter();
        final BufferedReader in   = new BufferedReader(new InputStreamReader(conec.getInputStream(), "UTF-8"));
        char [] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        String xmlResult = sw.toString();
        //xmlResult = removeXmlns(xmlResult);
        //xmlResult = xmlResult.replaceAll("xsi:schemaLocation=\"[^\"]*\" ", "");
        return xmlResult;
    }
}
