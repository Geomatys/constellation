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
import org.constellation.ows.v110.ExceptionReport;
import org.constellation.provider.LayerDetails;
import org.constellation.register.RegisterException;
import org.constellation.testing.Commons;
import org.constellation.wcs.v100.CoverageOfferingBriefType;
import org.constellation.wcs.v100.LonLatEnvelopeType;
import org.constellation.wcs.v100.WCSCapabilitiesType;
import org.constellation.wms.v111.LatLonBoundingBox;
import org.constellation.wms.v111.Layer;
import org.constellation.wms.v111.WMT_MS_Capabilities;
import org.constellation.ws.ServiceExceptionReport;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;


/**
 * A set of methods that request a Grizzly server which embeds a WMS and WCS service.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public class WebServicesTest {
    /**
     * A list of available layers to be requested in WMS and WCS.
     */
    private static List<LayerDetails> layers;

    /**
     * A thread that contains the grizzly server.
     */
    private static GrizzlyThread grizzly;

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration
     * and launch a Grizzly server, on which WCS and WMS requests will be sent.
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

        // Waiting for grizzly server to be completely started
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException ex) {
            assumeNoException(ex);
        }
    }

    /**
     * Ensure that a wrong value given in the request parameter for the WMS server
     * returned an error report for the user.
     */
    @Test
    public void testWrongWMSRequest() {
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
     * Ensure that a wrong value given in the request parameter for the WCS server
     * returned an error report for the user.
     */
    @Test
    public void testWrongWCSRequest() {
        // Creates an intentional wrong url, regarding the WCS version 1.0.0 standard
        final URL wrongUrl;
        try {
            wrongUrl = new URL("http://localhost:9090/wcs?request=SomethingElse");
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
                                                                "org.constellation.wcs.v100");
            final Object obj = context.createUnmarshaller().unmarshal(in);
            assertTrue(obj instanceof ExceptionReport);
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
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:9090/wms?request=GetMap&service=WMS&version=1.1.1&" +
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
            final InputStream in = getMapUrl.openStream();
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
     * Ensures that a valid GetCoverage request returns indeed a {@link BufferedImage}.
     */
    @Test
    public void testGetCoverageReturnedImage() {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetCoverage url.
        final URL getCoverageUrl;
        try {
            getCoverageUrl = new URL("http://localhost:9090/wcs?request=GetCoverage&service=WCS&version=1.0.0&" +
                                                          "format=image/png&width=1024&height=512&" +
                                                          "crs=EPSG:4326&bbox=-180,-90,180,90&" +
                                                          "coverage=SST_tests");
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get something from the url.
        final BufferedImage image;
        try {
            final InputStream in = getCoverageUrl.openStream();
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

        // Waiting for grizzly server to be completely started
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException ex) {
            assumeNoException(ex);
        }
        // Test on the returned image.
        assertEquals(image.getWidth(), 1024);
        assertEquals(image.getHeight(), 512);
        // The checksum value should be the same as the checksum on the same image produced by
        // a getMap request. It is strange but they are slightly different, even if for the user
        // both images are identical.
        assertEquals(Commons.checksum(image), 3183786073L);
    }

    /**
     * Ensures that a valid GetCapabilities request returns indeed a valid GetCapabilities
     * document representing the server capabilities in the WMS version 1.1.1 standard.
     */
    @Test
    public void testWMSGetCapabilities() {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getCapsUrl;
        try {
            getCapsUrl = new URL("http://localhost:9090/wms?request=GetCapabilities&service=WMS&version=1.1.1");
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get something from the wrong url.
        final InputStream in;
        try {
            in = getCapsUrl.openStream();
        } catch (IOException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        final Object obj;
        try {
            final JAXBContext context = JAXBContext.newInstance("org.constellation.ws:" +
                                                                "org.constellation.wms.v111:" +
                                                                "org.geotools.internal.jaxb.v110.sld");
            obj = context.createUnmarshaller().unmarshal(in);
            assertTrue(obj instanceof WMT_MS_Capabilities);
        } catch (JAXBException ex) {
            assumeNoException(ex);
            return;
        }

        final WMT_MS_Capabilities responseCaps = (WMT_MS_Capabilities)obj;
        final Layer layer = (Layer) responseCaps.getLayerFromName("SST_tests");

        assertNotNull(layer);
        assertEquals(layer.getSRS().get(0), "EPSG:4326");
        final LatLonBoundingBox bboxGeo = (LatLonBoundingBox) layer.getLatLonBoundingBox();
        assertTrue(bboxGeo.getWestBoundLongitude() == -180d);
        assertTrue(bboxGeo.getSouthBoundLatitude() ==  -90d);
        assertTrue(bboxGeo.getEastBoundLongitude() ==  180d);
        assertTrue(bboxGeo.getNorthBoundLatitude() ==   90d);
    }

    /**
     * Ensures that a valid GetCapabilities request returns indeed a valid GetCapabilities
     * document representing the server capabilities in the WCS version 1.0.0 standard.
     */
    @Test
    public void testWCSGetCapabilities() {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getCapsUrl;
        try {
            getCapsUrl = new URL("http://localhost:9090/wcs?request=GetCapabilities&service=WCS&version=1.0.0");
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get something from the wrong url.
        final InputStream in;
        try {
            in = getCapsUrl.openStream();
        } catch (IOException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        final Object obj;
        try {
            final JAXBContext context = JAXBContext.newInstance("org.constellation.ws:" +
                                                                "org.constellation.wms.v111:" +
                                                                "org.constellation.ows.v100");
            obj = context.createUnmarshaller().unmarshal(in);
            assertTrue(obj instanceof WCSCapabilitiesType);
        } catch (JAXBException ex) {
            assumeNoException(ex);
            return;
        }

        final WCSCapabilitiesType responseCaps = (WCSCapabilitiesType)obj;
        final List<CoverageOfferingBriefType> coverages = responseCaps.getContentMetadata().getCoverageOfferingBrief();

        assertNotNull(coverages);
        assertFalse(coverages.isEmpty());
        boolean layerTestFound = false;
        for (CoverageOfferingBriefType coverage : coverages) {
            if (coverage.getName().equals("SST_tests")) {
                layerTestFound = true;
                final LonLatEnvelopeType env = coverage.getLonLatEnvelope();
                assertTrue(env.getPos().get(0).getValue().get(0) == -180d);
                assertTrue(env.getPos().get(0).getValue().get(1) ==  180d);
                assertTrue(env.getPos().get(1).getValue().get(0) ==  -90d);
                assertTrue(env.getPos().get(1).getValue().get(1) ==   90d);
            }
        }
        if (layerTestFound == false) {
            throw new AssertionError("The layer \"SST_tests\" was not found in the returned GetCapabilities.");
        }
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
     * Returns {@code true} if the {@code SST_tests} layer is found in the list of
     * available layers. It means the postgrid database, pointed by the postgrid.xml
     * file in the configuration directory, contains this layer and can then be requested
     * in WMS.
     */
    private static boolean containsTestLayer() {
        for (LayerDetails layer : layers) {
            if (layer.getName().equals("SST_tests")) {
                return true;
            }
        }
        return false;
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
