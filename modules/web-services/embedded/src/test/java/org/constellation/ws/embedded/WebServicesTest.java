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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.ows.v110.ExceptionReport;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.provider.postgrid.PostGridProvider;
import org.constellation.provider.postgrid.PostGridProviderService;
import org.constellation.register.RegisterException;
import org.constellation.test.Commons;
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
        // Defines a PostGrid data provider
        final ProviderSource source = new ProviderSource();
        source.parameters.put(PostGridProvider.KEY_DATABASE, "jdbc:postgresql://test.geomatys.com/coverages-test");
        source.parameters.put(PostGridProvider.KEY_DRIVER,   "org.postgresql.Driver");
        source.parameters.put(PostGridProvider.KEY_PASSWORD, "g3ouser");
        source.parameters.put(PostGridProvider.KEY_READONLY, "true");
        // assume that a NFS mount has been done on the Geomatys server that host the data.
        // TODO: put the image somewhere accessible from outside of Geomatys network.
        source.parameters.put(PostGridProvider.KEY_ROOT_DIRECTORY, "/media/geomatys/Données/PostGRID");
        source.parameters.put(PostGridProvider.KEY_USER,     "geouser");

        final ProviderConfig config = new ProviderConfig();
        config.sources.add(source);

        for (LayerProviderService service : LayerProviderProxy.getInstance().getServices()) {
            // Here we should have the postgrid data provider defined previously
            if (service instanceof PostGridProviderService) {
                service.init(config);
                assumeTrue(!(service.getProviders().isEmpty()));
                if (service.getProviders().isEmpty()) {
                    return;
                }
                break;
            }
        }

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
            Thread.sleep(3 * 1000);
        } catch (InterruptedException ex) {
            assumeNoException(ex);
        }
    }

    /**
     * Ensure that a wrong value given in the request parameter for the WMS server
     * returned an error report for the user.
     */
    @Test
    public void testWMSWrongRequest() {
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
    public void testWCSWrongRequest() {
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
    public void testWMSGetMap() {
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

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image;
        try {
            image = getImageFromURL(getMapUrl, "image/png");
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
    public void testWCSGetCoverage() {
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

        // Try to get the coverage from the url.
        final BufferedImage image;
        try {
            image = getImageFromURL(getCoverageUrl, "image/png");
        } catch (IOException ex) {
            assumeNoException(ex);
            return;
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
     * Ensures a GetCoverage request with the output format matrix works fine.
     *
     * For now, this format is not well handled by the current Geotools. There are some
     * errors in the reading of this format, and they will be corrected in the next version
     * of Geotools.
     *
     * @TODO: do this test when moving of Geotools' version
     */
    @Test
    @Ignore
    public void testWCSGetCoverageMatrixFormat() {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetCoverage url.
        final URL getCovMatrixUrl;
        try {
            getCovMatrixUrl = new URL("http://localhost:9090/wcs?request=GetCoverage&service=WCS&version=1.0.0&" +
                                                                "format=matrix&width=1024&height=512&" +
                                                                "crs=EPSG:4326&bbox=-180,-90,180,90&" +
                                                                "coverage=SST_tests");
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        final BufferedImage image;
        try {
            image = getImageFromURL(getCovMatrixUrl, "application/matrix");
        } catch (IOException ex) {
            assumeNoException(ex);
            return;
        }
        //assertEquals(Commons.checksum(image), ...);
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

        // Creates a valid GetCapabilities url.
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

        // Creates a valid GetCapabilities url.
        final InputStream in;
        try {
            in = getCapsUrl.openStream();
        } catch (IOException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WCSCapabilitiesType.
        Object obj;
        try {
            final JAXBContext context = JAXBContext.newInstance("org.constellation.ws:" +
                                                                "org.constellation.wcs.v100:" +
                                                                "org.constellation.ows.v100:" +
                                                                "org.constellation.gml.v311");
            obj = context.createUnmarshaller().unmarshal(in);
            if (obj instanceof JAXBElement) {
                obj = ((JAXBElement) obj).getValue();
            }
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
            for (JAXBElement<String> elem : coverage.getRest()) {
                if (elem.getValue().equals("SST_tests")) {
                    layerTestFound = true;
                    final LonLatEnvelopeType env = coverage.getLonLatEnvelope();
                    assertTrue(env.getPos().get(0).getValue().get(0) == -180d);
                    assertTrue(env.getPos().get(0).getValue().get(1) ==  -90d);
                    assertTrue(env.getPos().get(1).getValue().get(0) ==  180d);
                    assertTrue(env.getPos().get(1).getValue().get(1) ==   90d);
                }
            }
        }
        if (layerTestFound == false) {
            throw new AssertionError("The layer \"SST_tests\" was not found in the returned GetCapabilities.");
        }
    }

    /**
     * Ensures that the {@code WMS GetFeatureInfo} request on a particular point of the
     * testing layer produces the whished result.
     */
    @Test
    public void testWMSGetFeatureInfo() {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetFeatureInfo url.
        final URL gfi;
        try {
            gfi = new URL("http://localhost:9090/wms?request=GetFeatureInfo&service=WMS&version=1.1.1&" +
                                                    "format=image/png&width=1024&height=512&" +
                                                    "srs=EPSG:4326&bbox=-180,-90,180,90&" +
                                                    "layers=SST_tests&styles=&query_layers=SST_tests&" +
                                                    "info_format=text/plain&X=300&Y=200");
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        String value = null;
        try {
            final InputStream inGfi = gfi.openStream();
            final InputStreamReader isr = new InputStreamReader(inGfi);
            final BufferedReader reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                // Verify that the line starts with a number, only the one with the value
                // should begin like this.
                if (line.matches("[0-9]+.*")) {
                    // keep the line with the value
                    value = line;
                }
            }
            reader.close();
        } catch (IOException ex) {
            assumeNoException(ex);
        }

        // Tests on the returned value
        assertNotNull(value);
        assertTrue   (value.startsWith("28.35"));
    }

    /**
     * Ensures that a valid GetLegendGraphic request returns indeed a {@link BufferedImage}.
     */
    @Test
    public void testWMSGetLegendGraphic() {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetLegendGraphic url.
        final URL getLegendUrl;
        try {
            getLegendUrl = new URL("http://localhost:9090/wms?request=GetLegendGraphic&service=wms&width=200&height=40&" +
                                                          "layer=SST_tests&format=image/png&version=1.1.0");
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image;
        try {
            image = getImageFromURL(getLegendUrl, "image/png");
        } catch (IOException ex) {
            assumeNoException(ex);
            return;
        }

        // Test on the returned image.
        assertEquals(image.getWidth(), 200);
        assertEquals(image.getHeight(), 40);
        assertEquals(Commons.checksum(image), 1522814217L);
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
     * Returned the {@link BufferedImage} from an URL requesting an image.
     *
     * @param url  The url of a request of an image.
     * @param mime The mime type of the image to return.
     *
     * @return The {@link BufferedImage} or {@code null} if an error occurs.
     * @throws IOException
     */
    private BufferedImage getImageFromURL(final URL url, final String mime) throws IOException {
        // Try to get the image from the url.
        final InputStream in = url.openStream();
        final ImageInputStream iis = ImageIO.createImageInputStream(in);
        final Iterator<ImageReader> irs = ImageIO.getImageReadersByMIMEType(mime);
        if (!irs.hasNext()) {
            return null;
        }
        final ImageReader ir = irs.next();
        ir.setInput(iis, true, true);
        final BufferedImage image = ir.read(0);
        ir.dispose();
        iis.close();
        // For debugging, uncomment the JFrame creation and the Thread.sleep further,
        // in order to see the image in a popup.
//        JFrame frame = new JFrame();
//        frame.setContentPane(new JLabel(new ImageIcon(image)));
//        frame.setSize(new Dimension(1024, 512));
//        frame.setVisible(true);
//        try {
//            Thread.sleep(3 * 1000);
//        } catch (InterruptedException ex) {
//            assumeNoException(ex);
//        }
        return image;
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
