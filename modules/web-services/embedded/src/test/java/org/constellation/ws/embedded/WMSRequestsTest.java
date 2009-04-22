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
import java.util.List;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import javax.xml.bind.Unmarshaller;
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.provider.LayerDetails;
import org.constellation.register.RegisterException;
import org.constellation.test.Commons;
import org.constellation.wms.v111.LatLonBoundingBox;
import org.constellation.wms.v111.Layer;
import org.constellation.wms.v111.WMT_MS_Capabilities;
import org.constellation.ws.ServiceExceptionReport;

// Geotools dependencies
import org.geotoolkit.internal.jaxb.v110.sld.DescribeLayerResponseType;
import org.geotoolkit.internal.jaxb.v110.sld.LayerDescriptionType;
import org.geotoolkit.internal.jaxb.v110.sld.TypeNameType;

// JUnit dependencies
import org.geotoolkit.xml.MarshallerPool;
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
public class WMSRequestsTest extends AbstractGrizzlyServer {
    /**
     * A list of available layers to be requested in WMS.
     */
    private static List<LayerDetails> layers;

    private static MarshallerPool pool;

    /**
     * URLs which will be tested on the server.
     */
    private static final String WMS_GETCAPABILITIES =
            "http://localhost:9090/wms?request=GetCapabilities&service=WMS&version=1.1.1";

    private static final String WMS_FALSE_REQUEST =
            "http://localhost:9090/wms?request=SomethingElse";

    private static final String WMS_GETMAP =
            "http://localhost:9090/wms?request=GetMap&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:4326&bbox=-180,-90,180,90&" +
                                      "layers="+ LAYER_TEST +"&styles=";

    private static final String WMS_GETFEATUREINFO =
            "http://localhost:9090/wms?request=GetFeatureInfo&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:4326&bbox=-180,-90,180,90&" +
                                      "layers="+ LAYER_TEST +"&styles=&" +
                                      "query_layers="+ LAYER_TEST +"&" + "info_format=text/plain&" +
                                      "X=300&Y=200";

    private static final String WMS_GETLEGENDGRAPHIC =
            "http://localhost:9090/wms?request=GetLegendGraphic&service=wms&" +
            "width=200&height=40&layer="+ LAYER_TEST +"&format=image/png&version=1.1.0";

    private static final String WMS_DESCRIBELAYER =
            "http://localhost:9090/wms?request=DescribeLayer&service=WMS&" +
            "version=1.1.1&layers="+ LAYER_TEST;

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initLayerList() throws JAXBException {
        // Get the list of layers
        try {
            layers = Cstl.getRegister().getAllLayerReferences(ServiceDef.WMS_1_1_1_SLD);
        } catch (RegisterException ex) {
            layers = null;
            assumeNoException(ex);
        }
        pool = new MarshallerPool("org.constellation.ws:" +
                                  "org.constellation.wms.v111:" +
                                  "org.geotoolkit.internal.jaxb.v110.sld");
    }

    /**
     * Ensure that a wrong value given in the request parameter for the WMS server
     * returned an error report for the user.
     */
    @Test
    public void testWMSWrongRequest() throws JAXBException {
        // Creates an intentional wrong url, regarding the WMS version 1.1.1 standard
        final URL wrongUrl;
        try {
            wrongUrl = new URL(WMS_FALSE_REQUEST);
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
        final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        final Object obj = unmarshaller.unmarshal(in);
        pool.release(unmarshaller);
        assertTrue(obj instanceof ServiceExceptionReport);
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
            getMapUrl = new URL(WMS_GETMAP);
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
     * Ensures that a valid GetCapabilities request returns indeed a valid GetCapabilities
     * document representing the server capabilities in the WMS version 1.1.1 standard.
     */
    @Test
    public void testWMSGetCapabilities() throws JAXBException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getCapsUrl;
        try {
            getCapsUrl = new URL(WMS_GETCAPABILITIES);
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
        final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        final Object obj = unmarshaller.unmarshal(in);
        pool.release(unmarshaller);
        assertTrue(obj instanceof WMT_MS_Capabilities);

        final WMT_MS_Capabilities responseCaps = (WMT_MS_Capabilities)obj;
        final Layer layer = (Layer) responseCaps.getLayerFromName(LAYER_TEST);

        assertNotNull(layer);
        assertEquals(layer.getSRS().get(0), "EPSG:4326");
        final LatLonBoundingBox bboxGeo = (LatLonBoundingBox) layer.getLatLonBoundingBox();
        assertTrue(bboxGeo.getWestBoundLongitude() == -180d);
        assertTrue(bboxGeo.getSouthBoundLatitude() ==  -90d);
        assertTrue(bboxGeo.getEastBoundLongitude() ==  180d);
        assertTrue(bboxGeo.getNorthBoundLatitude() ==   90d);
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
            gfi = new URL(WMS_GETFEATUREINFO);
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
            getLegendUrl = new URL(WMS_GETLEGENDGRAPHIC);
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
        assertEquals(Commons.isImageEmpty(image), false);
    }

    /**
     * Ensures that a valid DescribeLayer request produces a valid document.
     */
    @Test
    public void testWMSDescribeLayer() throws JAXBException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid DescribeLayer url.
        final URL describeUrl;
        try {
            describeUrl = new URL(WMS_DESCRIBELAYER);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        final InputStream in;
        try {
            in = describeUrl.openStream();
        } catch (IOException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        final Object obj = unmarshaller.unmarshal(in);
        pool.release(unmarshaller);
        assertTrue(obj instanceof DescribeLayerResponseType);

        // Tests on the response
        final DescribeLayerResponseType desc = (DescribeLayerResponseType)obj;
        final List<LayerDescriptionType> layerDescs = desc.getLayerDescription();
        assertFalse(layerDescs.isEmpty());
        final List<TypeNameType> typeNames = layerDescs.get(0).getTypeName();
        assertFalse(typeNames.isEmpty());
        assertEquals(typeNames.get(0).getCoverageName(), LAYER_TEST);
    }

    /**
     * Free some resources.
     */
    @AfterClass
    public static void finish() {
        layers = null;
    }

    /**
     * Returns {@code true} if the {@code SST_tests} layer is found in the list of
     * available layers. It means the postgrid database, pointed by the postgrid.xml
     * file in the configuration directory, contains this layer and can then be requested
     * in WMS.
     */
    private static boolean containsTestLayer() {
        for (LayerDetails layer : layers) {
            if (layer.getName().equals(LAYER_TEST)) {
                return true;
            }
        }
        return false;
    }
}
