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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import javax.xml.bind.Unmarshaller;
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.ows.v110.ExceptionReport;
import org.constellation.provider.LayerDetails;
import org.constellation.register.RegisterException;
import org.constellation.test.Commons;
import org.constellation.wcs.v100.CoverageDescription;
import org.constellation.wcs.v100.CoverageOfferingBriefType;
import org.constellation.wcs.v100.CoverageOfferingType;
import org.constellation.wcs.v100.LonLatEnvelopeType;
import org.constellation.wcs.v100.WCSCapabilitiesType;

// JUnit dependencies
import org.geotoolkit.xml.MarshallerPool;
import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;


/**
 * A set of methods that request a Grizzly server which embeds a WCS service.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public class WCSRequestsTest extends AbstractGrizzlyServer {
    /**
     * A list of available layers to be requested in WCS.
     */
    private static List<LayerDetails> layers;

    private static MarshallerPool pool;

    /**
     * URLs which will be tested on the server.
     */
    private static final String WCS_FALSE_REQUEST =
            "http://localhost:9090/wcs?request=SomethingElse";

    private static final String WCS_GETCOVERAGE =
            "http://localhost:9090/wcs?request=GetCoverage&service=WCS&version=1.0.0&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "crs=EPSG:4326&bbox=-180,-90,180,90&" +
                                      "coverage="+ LAYER_TEST;

    private static final String WCS_GETCOVERAGE_MATRIX =
            "http://localhost:9090/wcs?request=GetCoverage&service=WCS&version=1.0.0&" +
                                      "format=matrix&width=1024&height=512&" +
                                      "crs=EPSG:4326&bbox=-180,-90,180,90&" +
                                      "coverage="+ LAYER_TEST;

    private static final String WCS_GETCAPABILITIES =
            "http://localhost:9090/wcs?request=GetCapabilities&service=WCS&version=1.0.0";

    private static final String WCS_DESCRIBECOVERAGE =
            "http://localhost:9090/wcs?request=DescribeCoverage&coverage=SST_tests&service=wcs&version=1.0.0";

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initLayerList() throws JAXBException {
        // Get the list of layers
        try {
            layers = Cstl.getRegister().getAllLayerReferences(ServiceDef.WCS_1_0_0);
        } catch (RegisterException ex) {
            layers = null;
            assumeNoException(ex);
        }
        pool = new MarshallerPool("org.constellation.ws:" +
                                  "org.constellation.wcs.v100:" +
                                  "org.constellation.ows.v100:" +
                                  "org.constellation.gml.v311");
    }

    /**
     * Ensure that a wrong value given in the request parameter for the WCS server
     * returned an error report for the user.
     */
    @Test
    public void testWCSWrongRequest() throws JAXBException {
        // Creates an intentional wrong url, regarding the WCS version 1.0.0 standard
        final URL wrongUrl;
        try {
            wrongUrl = new URL(WCS_FALSE_REQUEST);
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
        assertTrue(obj instanceof ExceptionReport);
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
            getCoverageUrl = new URL(WCS_GETCOVERAGE);
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
            getCovMatrixUrl = new URL(WCS_GETCOVERAGE_MATRIX);
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
     * document representing the server capabilities in the WCS version 1.0.0 standard.
     */
    @Test
    public void testWCSGetCapabilities() throws JAXBException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl;
        try {
            getCapsUrl = new URL(WCS_GETCAPABILITIES);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get the result of this request from the url.
        final InputStream in;
        try {
            in = getCapsUrl.openStream();
        } catch (IOException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WCSCapabilitiesType.
        final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object obj = unmarshaller.unmarshal(in);
        pool.release(unmarshaller);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        assertTrue(obj instanceof WCSCapabilitiesType);

        final WCSCapabilitiesType responseCaps = (WCSCapabilitiesType)obj;
        final List<CoverageOfferingBriefType> coverages = responseCaps.getContentMetadata().getCoverageOfferingBrief();

        assertNotNull(coverages);
        assertFalse(coverages.isEmpty());
        boolean layerTestFound = false;
        for (CoverageOfferingBriefType coverage : coverages) {
            for (JAXBElement<String> elem : coverage.getRest()) {
                if (elem.getValue().equals(LAYER_TEST)) {
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
            throw new AssertionError("The layer \""+ LAYER_TEST +"\" was not found in the returned GetCapabilities.");
        }
    }

    /**
     * Ensures that a valid DescribeCoverage request returns indeed a valid document.
     */
    @Test
    public void testWCSDescribeCoverage() throws JAXBException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid DescribeCoverage url.
        final URL getCapsUrl;
        try {
            getCapsUrl = new URL(WCS_DESCRIBECOVERAGE);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get the result of the DescribeCoverage request from the url.
        final InputStream in;
        try {
            in = getCapsUrl.openStream();
        } catch (IOException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WCSCapabilitiesType.
        final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object obj = unmarshaller.unmarshal(in);
        pool.release(unmarshaller);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        assertTrue(obj instanceof CoverageDescription);

        final CoverageDescription responseDesc = (CoverageDescription)obj;
        assertNotNull(responseDesc);
        final List<CoverageOfferingType> coverageOffs = responseDesc.getCoverageOffering();
        assertFalse(coverageOffs.isEmpty());
        assertEquals(coverageOffs.get(0).getRest().get(1).getValue(), LAYER_TEST);
        // TODO: add more tests on returned XML doc
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
     * in WCS.
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
