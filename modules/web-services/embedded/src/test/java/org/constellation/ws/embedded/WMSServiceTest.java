/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

// J2SE dependencies
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

// Constellation dependencies
import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.provider.LayerDetails;
import org.constellation.register.RegisterException;
import org.constellation.testing.Commons;
import org.constellation.ws.ServiceExceptionReport;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;


/**
 * A set of methods that request a Grizzly server which embeds a WMS service.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public class WMSServiceTest {
    /**
     * A list of available layers to be requested in WMS.
     */
    private static List<LayerDetails> layers;

    /**
     * A thread that contains the grizzly server.
     */
    private static GrizzlyThread grizzly;

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration
     * and launch a Grizzly server, on which WMS requests will be sent.
     */
    @BeforeClass
    public static void initServerAndLayerList() {
        // Get the list of layers
        try {
            layers = Cstl.getRegister().getAllLayerReferences(ServiceDef.WMS_1_1_1_SLD);
        } catch (RegisterException ex) {
            layers = null;
            assumeNoException(ex);
        }

        // Starting the grizzly server
        grizzly = new GrizzlyThread();
        grizzly.start();
    }

    /**
     * Ensure that a wrong value given in the request parameter for the WMS server
     * returned an error report for the user.
     */
    @Test
    public void testWrongRequest() {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));

        // Waiting for grizzly server to be completely started
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException ex) {
            assumeNoException(ex);
        }

        // Creates an intentional wrong url, regarding the WMS version 1.1.1 standard
        final URL wrongUrl;
        try {
            wrongUrl = new URL("http://localhost:9090/wms?request=SomethingElse");
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get something from the wrong url.
        final InputStream in;
        try {
            in = wrongUrl.openStream();
        } catch (IOException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a ServiceExceptionReport.
        try {
            final JAXBContext context = JAXBContext.newInstance("org.constellation.ws:" +
                                                                "org.constellation.wms.v111");
            final Object obj = context.createUnmarshaller().unmarshal(in);
            assertTrue(obj instanceof ServiceExceptionReport);
        } catch (JAXBException ex) {
            assumeNoException(ex);
            return;
        }
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    public void testGetMapReturnedImage() {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));

        // Waiting for grizzly server to be completely started
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException ex) {
            assumeNoException(ex);
        }

        final URL wrongUrl;
        try {
            wrongUrl = new URL("http://localhost:9090/wms?request=GetMap&service=WMS&version=1.1.1&" +
                                                         "format=image/png&width=1024&height=512&" +
                                                         "srs=EPSG:4326&bbox=-180,-90,180,90&" +
                                                         "layers=SST_tests&styles=");
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get something from the url.
        final BufferedImage image;
        try {
            final InputStream in = wrongUrl.openStream();
            image = ImageIO.read(in);
            in.close();
//            JFrame frame = new JFrame();
//            frame.setContentPane(new JLabel(new ImageIcon(image)));
//            frame.setSize(new Dimension(1024, 512));
//            frame.setVisible(true);
        } catch (IOException ex) {
            assumeNoException(ex);
            return;
        }

        // Test on the returned image.
        assertEquals(image.getWidth(), 1024);
        assertEquals(image.getHeight(), 512);
        assertEquals(Commons.checksum(image), 3640849032L);
    }

    /**
     * Interrupt the Grizzly server if it still alive and free some resources.
     */
    @AfterClass
    public static void finish() {
        layers = null;
        // Try to kill the grizzly server if it is still alive
        if (grizzly.isAlive()) {
            grizzly.interrupt();
        }
    }

    /**
     * Thread that launches a Grizzly server in a separate thread.
     * WMS requests will be done on this working server.
     */
    private static class GrizzlyThread extends Thread {
        /**
         * Runs a Grizzly server for a minute.
         */
        @Override
        public void run() {
            final CstlEmbeddedService cstlServer = new CstlEmbeddedService(new String[]{});
            cstlServer.duration = 1*60*1000;
            cstlServer.runREST();
        }
    }
}
