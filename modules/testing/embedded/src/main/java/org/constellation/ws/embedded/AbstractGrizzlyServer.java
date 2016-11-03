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

import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.util.Util;
import org.geotoolkit.image.io.XImageIO;
import org.geotoolkit.internal.io.IOUtilities;

import javax.imageio.ImageReader;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.geotoolkit.util.FileUtilities;

/**
 * Launches a Grizzly server in a thread at the beginning of the testing process
 * and kill it when it is done.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public abstract class AbstractGrizzlyServer {

    protected static final Logger LOGGER = Logging.getLogger("org.constellation.ws.embedded");

    /**
     * The grizzly server that will received some HTTP requests.
     */
    protected static GrizzlyThread grizzly = null;

    protected static MarshallerPool pool;

    public static void initServer(final String[] resourcePackages, final Map<String, Object> soapServices) {
        initServer(resourcePackages, soapServices, null);
    }
    /**
     * Initialize the Grizzly server, on which WCS and WMS requests will be sent,
     * and defines a PostGrid data provider.
     */
    public static void initServer(final String[] resourcePackages, final Map<String, Object> soapServices, final String uriSuffix) {
        // Protective test in order not to launch a new instance of the grizzly server for
        // each sub classes.
        if (grizzly != null) {
            LOGGER.info("GRIZZLY is not shutDown");
            return;
        }

        /* Instanciates the Grizzly server, but not start it at this moment.
         * The implementation waits for the data provider to be defined for
         * starting the server.
         */
        grizzly = new GrizzlyThread(resourcePackages, soapServices, uriSuffix);

        // Starting the grizzly server
        grizzly.start();

    }

    /**
     * Stop the grizzly server, if it is still alive.
     */
    public static void finish() {
        if (grizzly != null) {
            if (grizzly.isAlive()) {
                grizzly.cstlServer.shutdown();
                grizzly.interrupt();
            }
            File f = new File("derby.log");
            if (f.exists()) {
                f.delete();
            }
            grizzly = null;
        }
    }

    public void waitForStart() throws Exception {
        boolean ex = true;
        int cpt = 0;
        while (ex) {
            Thread.sleep(1 * 2000);
            final URL u;
            final String suffix;
            if (grizzly.getUriSuffix() != null) {
                suffix = "/" + grizzly.getUriSuffix();
            } else {
                suffix = "";
            }
            if (grizzly != null && grizzly.getCurrentPort() != null) {
                u = new URL("http://localhost:" + grizzly.getCurrentPort() + suffix + "/1/OGC/spec/list");
            } else {
                u = new URL("http://localhost:9090//1/OGC/spec/list");
            }
            ex = false;
            URLConnection conec = u.openConnection();
            try {
                conec.getInputStream();
            } catch (IOException e) {
                ex = true;
            }
            if (cpt == 100) {
                throw new Exception("The grizzly server never start");
            }
            cpt++;
        }
    }

    public void waitForSoapStart(String instance) throws Exception {
        waitForSoapStart(instance, 100);
    }

    public void waitForSoapStart(String instance, int max) throws Exception {
        boolean ex = true;
        int cpt = 0;
        while (ex) {
            Thread.sleep(1 * 2000);
            final URL u;
            final String suffix;
            if (grizzly.getUriSuffix() != null) {
                suffix = "/" + grizzly.getUriSuffix();
            } else {
                suffix = "";
            }
            if (grizzly != null && grizzly.getCurrentPortSoap()!= null) {
                u = new URL("http://localhost:" + grizzly.getCurrentPortSoap() + suffix + "/" + instance + "?wsdl");
            } else {
                u = new URL("http://localhost:9090/1/user/access");
            }
            ex = false;
            URLConnection conec = u.openConnection();
            try {
                conec.getInputStream();
            } catch (IOException e) {
                ex = true;
            }
            if (cpt == max) {
                throw new Exception("The grizzly server never start");
            }
            cpt++;
        }
    }

    /**
     * Thread that launches a Grizzly server in a separate thread.
     * Requests will be done on this working server.
     */
    protected static class GrizzlyThread extends Thread {
        private final CstlEmbeddedService cstlServer;
        private final Map<String, Object> soapServices;

        public GrizzlyThread(final String[] resourcePackages, final Map<String, Object> soapServices) {
            this(resourcePackages, soapServices, null);
        }
        public GrizzlyThread(final String[] resourcePackages, final Map<String, Object> soapServices, final String uriSuffix) {
            this.soapServices = soapServices;
            if (resourcePackages != null) {
                cstlServer = new CstlEmbeddedService(new String[]{}, resourcePackages, uriSuffix);
            } else {
                cstlServer = new CstlEmbeddedService(new String[]{}, uriSuffix);
            }
        }

        public Integer getCurrentPort() {
            return cstlServer.currentPort;
        }

        public Integer getCurrentPortSoap() {
            return cstlServer.portsoap;
        }

        public String getUriSuffix() {
            return cstlServer.uriSuffix;
        }

        /**
         * Runs a Grizzly server for five minutes.
         */
        @Override
        public void run() {
            cstlServer.duration = 5*60*1000;
            cstlServer.findAvailablePort = true;
            if (soapServices != null) {
                cstlServer.serviceInstanceSOAP.putAll(soapServices);
            }
            cstlServer.runAll();
        }
    }

    protected static String getStringResponse(final URLConnection conec) throws UnsupportedEncodingException, IOException {
        InputStream is;
        if (((HttpURLConnection)conec).getResponseCode() == 200) {
            is = conec.getInputStream();
        } else {
            is = ((HttpURLConnection)conec).getErrorStream();
        }
        final StringWriter sw     = new StringWriter();
        final BufferedReader in   = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        char [] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        String xmlResult = sw.toString();
        return xmlResult;
    }

    /**
     * Already in FileUtilities ???
     */
    protected static String getStringFromFile(String filePath) throws UnsupportedEncodingException, IOException {
        final StringWriter sw     = new StringWriter();
        final BufferedReader in   = new BufferedReader(new InputStreamReader(Util.getResourceAsStream(filePath), "UTF-8"));
        char [] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        String xmlExpResult = sw.toString();

        //we unformat the expected result
        xmlExpResult = xmlExpResult.replace("\n", "");

        return xmlExpResult;
    }

    protected static void postRequestFile(URLConnection conec, String filePath, String contentType) throws IOException {
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", contentType);
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

    protected static void putRequestFile(URLConnection conec, String filePath, String contentType) throws IOException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("PUT");
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", contentType);
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

    protected static void postRequestFile(URLConnection conec, String filePath) throws IOException {
        postRequestFile(conec, filePath, "text/xml");
    }

    protected static void putRequestFile(URLConnection conec, String filePath) throws IOException {
        putRequestFile(conec, filePath, "text/xml");
    }

    protected static void postRequestPlain(URLConnection conec, String request) throws IOException {
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "text/plain");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        wr.write(request);
        wr.flush();
    }

    /**
     * Returned the {@link BufferedImage} from an URL requesting an image.
     *
     * @param url  The url of a request of an image.
     * @param mime The mime type of the image to return.
     *
     * @return The {@link BufferedImage} or {@code null} if an error occurs.
     * @throws IOException
     */
    protected static BufferedImage getImageFromURL(final URL url, final String mime) throws IOException {
        // Try to get the image from the url.
        final InputStream in = url.openStream();
        final ImageReader reader = XImageIO.getReaderByMIMEType(mime, in, true, true);
        final BufferedImage image = reader.read(0);
        XImageIO.close(reader);
        reader.dispose();
        // For debugging, uncomment the JFrame creation and the Thread.sleep further,
        // in order to see the image in a popup.
//        javax.swing.JFrame frame = new javax.swing.JFrame();
//        frame.setContentPane(new javax.swing.JLabel(new javax.swing.ImageIcon(image)));
//        frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//        try {
//            Thread.sleep(5 * 1000);
//            frame.dispose();
//        } catch (InterruptedException ex) {
//            assumeNoException(ex);
//        }
        return image;
    }

    protected static BufferedImage getImageFromPostKvp(final URL url, final Map<String, String> parameters, final String mime) throws IOException {
        final URLConnection conec = url.openConnection();
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        final StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : parameters.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.deleteCharAt(sb.length() -1);
        wr.write(sb.toString());
        wr.flush();

        // Try to get the image from the url.
        final InputStream in = conec.getInputStream();
        final ImageReader reader = XImageIO.getReaderByMIMEType(mime, in, true, true);
        final BufferedImage image = reader.read(0);
        XImageIO.close(reader);
        reader.dispose();
        // For debugging, uncomment the JFrame creation and the Thread.sleep further,
        // in order to see the image in a popup.
//        javax.swing.JFrame frame = new javax.swing.JFrame();
//        frame.setContentPane(new javax.swing.JLabel(new javax.swing.ImageIcon(image)));
//        frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//        try {
//            Thread.sleep(5 * 1000);
//            frame.dispose();
//        } catch (InterruptedException ex) {
//            assumeNoException(ex);
//        }
        return image;
    }

    /**
     * Initializes the data directory in unzipping the jar containing the resources
     * into a temporary directory.
     *
     * @return The root output directory where the data are unzipped.
     * @throws IOException
     */
    public static File initDataDirectory() throws IOException {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String styleResource = classloader.getResource("org/constellation/ws/embedded/wms111/styles").getFile();
        if (styleResource.indexOf('!') != -1) {
            styleResource = styleResource.substring(0, styleResource.indexOf('!'));
        }
        if (styleResource.startsWith("file:")) {
            styleResource = styleResource.substring(5);
        }
        final File styleJar = new File(styleResource);
        if (!styleJar.exists()) {
            throw new IOException("Unable to find the style folder: "+ styleJar);
        }

        final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File outputDir = new File(tmpDir, "Constellation");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        if (styleJar.isDirectory()) {
            FileUtilities.copy(styleJar, outputDir);
        } else {
            final InputStream in = new FileInputStream(styleJar);
            IOUtilities.unzip(in, outputDir);
            in.close();
        }
        return outputDir;
    }

    protected static void postRequestObject(URLConnection conec, Object request) throws IOException, JAXBException {
        postRequestObject(conec, request, pool);
    }

    protected static void postRequestObject(URLConnection conec, Object request, MarshallerPool pool) throws IOException, JAXBException {
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "application/xml");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        final StringWriter sw = new StringWriter();
        Marshaller marshaller = pool.acquireMarshaller();
        marshaller.marshal(request, sw);

        wr.write(sw.toString());
        wr.flush();
    }

    protected static void putRequestObject(URLConnection conec, Object request, MarshallerPool pool) throws IOException, JAXBException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("PUT");
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "application/xml");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        final StringWriter sw = new StringWriter();
        Marshaller marshaller = pool.acquireMarshaller();
        marshaller.marshal(request, sw);

        wr.write(sw.toString());
        wr.flush();
    }

    protected static Object unmarshallResponsePut(final URLConnection conec) throws JAXBException, IOException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("PUT");
        return unmarshallStream(conec.getInputStream());
    }

    protected static Object unmarshallResponsePost(final URLConnection conec) throws JAXBException, IOException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("POST");
        return unmarshallStream(conec.getInputStream());
    }

    protected static Object unmarshallResponseDelete(final URLConnection conec) throws JAXBException, IOException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("DELETE");
        return unmarshallStream(conec.getInputStream());
    }

    protected static Object unmarshallResponse(final URLConnection conec) throws JAXBException, IOException {
        return unmarshallStream(conec.getInputStream());
    }

    protected static Object unmarshallResponse(final URL url) throws JAXBException, IOException {
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream is;
        if (conn.getResponseCode() == 200) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
        }
        return unmarshallStream(is);
    }

    private static Object unmarshallStream(final InputStream is) throws IOException, JAXBException {

        //JDK-8 : ensure xml reader are allowed to access external DTD files when needed
        System.setProperty("javax.xml.accessExternalSchema", "all");
        System.setProperty("javax.xml.accessExternalDTD", "all");
        
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");

        Object obj;
        try {
            obj = unmarshaller.unmarshal(new StringReader(writer.toString()));
            pool.recycle(unmarshaller);
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "JAXB Error received while trying to read:{0}", writer.toString());
            throw ex;
        }

        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        return obj;
    }

    protected static String removeUpdateSequence(final String xml) {
        String s = xml;
        s = s.replaceAll("updateSequence=\"[^\"]*\" ", "");
        return s;
    }

    /**
      FOR SOAP TODO see if well need it
      public void waitForStart() throws Exception {
        final URL u = new URL("http://localhost:9191/wps/wsdl?");
        boolean ex = true;

        while (ex) {
            Thread.sleep(1 * 1000);
            ex = false;
            URLConnection conec = u.openConnection();
            try {
                conec.getInputStream();
            } catch (ConnectException e) {
                ex = true;
            }
        }
    }
     */
}
