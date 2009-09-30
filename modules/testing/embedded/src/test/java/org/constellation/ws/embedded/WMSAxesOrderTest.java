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
import java.net.MalformedURLException;
import java.net.URL;

// Constellation dependencies
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.register.RegisterException;
import org.constellation.test.Commons;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;


/**
 * Do some tests on the {@code WMS GetMap} request, in order to ensure that the axes
 * order is well handled by Constellation, for different kinds of
 * {@linkplain CoordinateReferenceSystem CRS}.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public class WMSAxesOrderTest extends AbstractGrizzlyServer {
    /**
     * URLs which will be tested on the server.
     */
    private static final String WMS_GETMAP_111_PROJ =
            "http://localhost:9090/wms?request=GetMap&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:3395&bbox=-19000000,-19000000,19000000,19000000&" +
                                      "layers="+ LAYER_TEST +"&styles=";
    private static final String WMS_GETMAP_130_PROJ =
            "http://localhost:9090/wms?request=GetMap&service=WMS&version=1.3.0&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "crs=EPSG:3395&bbox=-19000000,-19000000,19000000,19000000&" +
                                      "layers="+ LAYER_TEST +"&styles=";
    private static final String WMS_GETMAP_111_GEO =
            "http://localhost:9090/wms?request=GetMap&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:4022&bbox=-90,-180,90,180&" +
                                      "layers="+ LAYER_TEST +"&styles=";
    private static final String WMS_GETMAP_111_EPSG_4326 =
            "http://localhost:9090/wms?request=GetMap&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:4326&bbox=-180,-90,180,90&" +
                                      "layers="+ LAYER_TEST +"&styles=";
    private static final String WMS_GETMAP_130_EPSG_4326 =
            "http://localhost:9090/wms?request=GetMap&service=WMS&version=1.3.0&" +
                                      "format=image/png&width=512&height=1024&" +
                                      "crs=EPSG:4326&bbox=-90,-180,90,180&" +
                                      "layers="+ LAYER_TEST +"&styles=";
    private static final String WMS_GETMAP_111_CRS_84 =
            "http://localhost:9090/wms?request=GetMap&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=CRS:84&bbox=-180,-90,180,90&" +
                                      "layers="+ LAYER_TEST +"&styles=";
    private static final String WMS_GETMAP_130_CRS_84 =
            "http://localhost:9090/wms?request=GetMap&service=WMS&version=1.3.0&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "crs=CRS:84&bbox=-180,-90,180,90&" +
                                      "layers="+ LAYER_TEST +"&styles=";

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initLayerList() {
        // Get the list of layers
        try {
            layers = Cstl.getRegister().getAllLayerReferences(ServiceDef.WMS_1_1_1_SLD);
        } catch (RegisterException ex) {
            layers = null;
            assumeNoException(ex);
        }
    }

    /**
     * Free some resources.
     */
    @AfterClass
    public static void finish() {
        layers = null;
    }

    /**
     * Ensures that a GetMap in version 1.1.1 on a projected
     * {@linkplain CoordinateReferenceSystem CRS} provides the same image that a GetMap
     * in version 1.3.0 on the same CRS.
     */
    @Test
    public void testGetMap111And130Projected() throws IOException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMap111Url, getMap130Url;
        try {
            getMap111Url = new URL(WMS_GETMAP_111_PROJ);
            getMap130Url = new URL(WMS_GETMAP_130_PROJ);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image111 = getImageFromURL(getMap111Url, "image/png");
        final BufferedImage image130 = getImageFromURL(getMap130Url, "image/png");

        // Test on the returned image.
        assertEquals(image111.getWidth(), 1024);
        assertEquals(image111.getHeight(), 512);
        assertEquals(Commons.checksum(image111), Commons.checksum(image130));
        // TODO: retrieve the right colors.
        //assertEquals(Commons.checksum(image130), 2274939253L);
    }

    /**
     * Test a GetMap request on a WMS version 1.1.1 for a geographical
     * {@linkplain CoordinateReferenceSystem CRS}.
     *
     * TODO: fix the implementation of the GetMap request concerning the handling of
     *       geographical CRS (not WGS84) and do this test then.
     */
    @Ignore
    public void testCRSGeographique111() throws IOException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL(WMS_GETMAP_111_GEO);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Test on the returned image.
        assertEquals(image.getWidth(), 1024);
        assertEquals(image.getHeight(), 512);
    }

    /**
     * Verify the axis order for a GetMap in version 1.1.1 for the {@code WGS84} CRS.
     */
    @Test
    public void testGetMap111Epsg4326() throws IOException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL(WMS_GETMAP_111_EPSG_4326);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Test on the returned image.
        assertEquals(image.getWidth(), 1024);
        assertEquals(image.getHeight(), 512);
        assertEquals(Commons.checksum(image), 3640849032L);
    }

    /**
     * Verify the axis order for a GetMap in version 1.3.0 for the {@code WGS84} CRS.
     *
     * TODO: fix the implementation of the GetMap request concerning the axes order,
     *       and do this test then.
     */
    @Test
    public void testGetMap130Epsg4326() throws IOException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL(WMS_GETMAP_130_EPSG_4326);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Test on the returned image.
        assertEquals(image.getWidth(), 512);
        assertEquals(image.getHeight(), 1024);
        assertTrue  (!(Commons.isImageEmpty(image)));
        // Here we have to ensure that the axis order should be lat,long in the GetMap request.
        // So with that axes order, the image should be the same than the one done in GetMap
        // version 1.1.1 with axes order long,lat.
        assertTrue  (Commons.checksum(image) != 3640849032L);
    }

    /**
     * Verify the axis order for a GetMap in version 1.1.1 for the {@code WGS84} CRS.
     */
    @Test
    public void testGetMap111Crs84() throws IOException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL(WMS_GETMAP_111_CRS_84);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Test on the returned image.
        assertEquals(image.getWidth(), 1024);
        assertEquals(image.getHeight(), 512);
        assertEquals(Commons.checksum(image), 3640849032L);
    }

    /**
     * Verify the axis order for a GetMap in version 1.3.0 for the {@code WGS84} CRS.
     *
     * TODO: fix the implementation of the GetMap request concerning the axes order,
     *       and do this test then.
     */
    @Test
    public void testGetMap130Crs84() throws IOException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL(WMS_GETMAP_130_CRS_84);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Test on the returned image.
        assertEquals(image.getWidth(), 1024);
        assertEquals(image.getHeight(), 512);
        assertTrue  (!(Commons.isImageEmpty(image)));
        // Here we have to ensure that the axis order should be lat,long in the GetMap request.
        // So with that axes order, the image should be the same than the one done in GetMap
        // version 1.1.1 with axes order long,lat.
        assertEquals(Commons.checksum(image), 3640849032L);
    }
}
