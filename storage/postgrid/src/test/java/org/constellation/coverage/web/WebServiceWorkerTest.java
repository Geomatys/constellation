/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.coverage.web;

import java.util.Set;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Transparency;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.IndexColorModel;
import javax.imageio.IIOException;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.referencing.crs.DefaultVerticalCRS;
import org.geotools.coverage.grid.ViewType;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.util.logging.Logging;

import org.constellation.catalog.DatabaseTest;
import org.constellation.catalog.ConfigurationKey;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.LayerTableTest;

import org.junit.*;


/**
 * Tests {@link WebServiceWorkerTest}.
 *
 * @author Martin Desruisseaux
 */
public class WebServiceWorkerTest extends DatabaseTest {
    /**
     * {@code true} for disabling tests. Useful for disabling every tests except one
     * during debugging.
     */
    private static final boolean DISABLED = true;

    /**
     * Tests with the default test layer.
     *
     * @throws WebServiceException If a WMS parameter is illegal.
     * @throws IOException If an error occured while reading an image.
     */
    @Test
    public void testSST() throws WebServiceException, IOException {
        if (DISABLED) return;
        final WebServiceWorker worker = new WebServiceWorker(database, false);
        worker.setService("WMS", "1.0");
        worker.setLayer(LayerTableTest.SAMPLE_NAME);
        worker.setCoordinateReferenceSystem("EPSG:4326");
        worker.setTime(LayerTableTest.SAMPLE_TIME_AS_TEXT);

        Layer layer = worker.getLayer();
        assertEquals(layer.getName(), LayerTableTest.SAMPLE_NAME);
        assertSame("The layer should be cached.", layer, worker.getLayer());

        GridCoverage2D coverage = worker.getGridCoverage2D(false);
        assertSame("The coverage should be cached.", coverage, worker.getGridCoverage2D(false));
        assertSame("Expected no ressampling.",       coverage, worker.getGridCoverage2D(true ));

        Set<ViewType> types = coverage.getViewTypes();
        assertFalse(types.contains(ViewType.SAME));
        assertTrue (types.contains(ViewType.GEOPHYSICS));
        assertFalse(types.contains(ViewType.PACKED));
        assertFalse(types.contains(ViewType.NATIVE));

        String format = worker.getMimeType();
        assertEquals("image/png", format);

        File file = worker.getImageFile();
        assertTrue(file.getName().endsWith(".png"));
        assertTrue(file.isFile());
        assertEquals("image/png", format); // Previous value was a default one. Now it has been computed.

        RenderedImage image = ImageIO.read(file);
        assertEquals(4096, image.getWidth());
        assertEquals(2048, image.getHeight());
        assertEquals(IndexColorModel.class, image.getColorModel().getClass());
        IndexColorModel model = (IndexColorModel) image.getColorModel();
        assertEquals(256, model.getMapSize());
        /*
         * Forces a crop.
         */
        int width  = 342; // Expected width
        int height = 341; // Expected height
        worker.setBoundingBox("-10,20,20,50");
        assertEquals(layer.getName(), LayerTableTest.SAMPLE_NAME);
        assertNotSame("A new layer should be created.", layer, worker.getLayer());
        assertNotSame("A new coverage should be created.", coverage, worker.getGridCoverage2D(false));
        layer    = worker.getLayer();
        coverage = worker.getGridCoverage2D(false);
        worker.setBoundingBox("-10,20,20,50");  // Same value should not flush the cache.
        assertSame("The layer should be cached.", layer, worker.getLayer());
        assertSame("The coverage should be cached.", coverage, worker.getGridCoverage2D(false));

        image = worker.getRenderedImage();
        assertEquals(width,  image.getWidth());
        assertEquals(height, image.getHeight());
        if (false) {
            SampleModel sm = image.getSampleModel();
            assertEquals(width,  image.getTileWidth());
            assertEquals(height, image.getTileHeight());
            assertEquals(1,      image.getNumXTiles());
            assertEquals(1,      image.getNumYTiles());
            assertEquals(width,  sm.getWidth());
            assertEquals(height, sm.getHeight());
        }
        file = worker.getImageFile();
        assertTrue(file.getName().endsWith(".png"));
        assertTrue(file.isFile());
        assertSame("The same file should be reused", file, worker.getImageFile());

        image = ImageIO.read(file);
        assertEquals(width,  image.getWidth());
        assertEquals(height, image.getHeight());
        assertEquals(IndexColorModel.class, image.getColorModel().getClass());
        model = (IndexColorModel) image.getColorModel();
        assertEquals(256, model.getMapSize());
        /*
         * Adds a size.
         */
        width  = 150; // Expected width
        height = 150; // Expected height
        worker.setDimension("150","150", null);
        assertEquals(layer.getName(), LayerTableTest.SAMPLE_NAME);
        assertNotSame("A new layer should be created.", layer, worker.getLayer());
        assertNotSame("A new coverage should be created.", coverage, worker.getGridCoverage2D(false));
        layer    = worker.getLayer();
        coverage = worker.getGridCoverage2D(false);
        worker.setDimension("150","150", null);  // Same value should not flush the cache.
        assertSame("The layer should be cached.", layer, worker.getLayer());
        assertSame("The coverage should be cached.", coverage, worker.getGridCoverage2D(false));

        image = worker.getRenderedImage();
        assertEquals(width,  image.getWidth());
        assertEquals(height, image.getHeight());
        assertFalse("A new file should be created.", file.equals(worker.getImageFile()));
        file = worker.getImageFile();
        assertTrue(file.getName().endsWith(".png"));
        assertTrue(file.isFile());

        image = ImageIO.read(file);
        assertEquals(width,  image.getWidth());
        assertEquals(height, image.getHeight());
        assertEquals(IndexColorModel.class, image.getColorModel().getClass());
        model = (IndexColorModel) image.getColorModel();
        assertEquals(256, model.getMapSize());
        /*
         * Changes only a little bit the envelope.
         */
        worker.setBoundingBox("-10,20,20,50");  // Same value should not flush the cache.
        assertSame("The layer should be cached.", layer, worker.getLayer());

        worker.setBoundingBox("-9,20,21,49");  // Value not different enough
        assertSame("The layer should be cached.", layer, worker.getLayer());

        worker.setBoundingBox("-10,20,140,50");  // Different value should flush the cache.
        assertNotSame("A new layer should be created.", layer, worker.getLayer());
    }

    /**
     * Tests with the NetCDF test layer.
     *
     * @throws WebServiceException If a WMS parameter is illegal.
     * @throws IOException If an error occured while reading an image.
     */
    @Test
    public void testNetCDF() throws WebServiceException, IOException {
        if (DISABLED) return;
        final WebServiceWorker worker = new WebServiceWorker(database, false);
        worker.setInterpolation("bilinear");
        worker.setService("WMS", "1.0");
        worker.setLayer(LayerTableTest.NETCDF_NAME);
        worker.setCoordinateReferenceSystem("EPSG:3395");

        Layer layer = worker.getLayer();
        assertEquals(layer.getName(), LayerTableTest.NETCDF_NAME);
        assertSame("The layer should be cached.", layer, worker.getLayer());
        worker.setTime(LayerTableTest.NETCDF_TIME_AS_TEXT);
        assertSame("The layer should be cached.", layer, worker.getLayer());

        GridCoverage2D coverage;
        try {
            coverage = worker.getGridCoverage2D(false);
        } catch (WMSWebServiceException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IIOException) {
                final String message = cause.getMessage();
                if (message.contains("image/x-netcdf-ifremer")) {
                    Logging.getLogger(WebServiceWorkerTest.class).warning("Skipping IFREMER-specific test.");
                    return;
                }
            }
            throw e;
        }
        assertEquals(4, coverage.getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
        assertSame("The coverage should be cached.", coverage, worker.getGridCoverage2D(false));
        assertEquals(2, worker.getGridCoverage2D(true).getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
        RenderedImage image = worker.getRenderedImage();
        assertEquals(Transparency.BITMASK, image.getColorModel().getTransparency());
        if (false) try {
            org.geotools.gui.swing.image.OperationTreeBrowser.show(image);
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            // Ignore and go back to work.
        }
        String format = worker.getMimeType();
        assertEquals("image/png", format);

        File file = worker.getImageFile();
        assertTrue(file.getName().endsWith(".png"));
        assertTrue(file.isFile());
        assertEquals("image/png", format); // Previous value was a default one. Now it has been computed.

        image = ImageIO.read(file);
        assertEquals(720, image.getWidth());
        assertEquals(499, image.getHeight());
        assertEquals(Transparency.BITMASK, image.getColorModel().getTransparency());
        /*
         * Sets an envelope intentionnaly bigger. We want to test the background color.
         */
        worker.setBoundingBox("-2.5E7,-1.5E7,2.5E7,1.5E7");
        if (false) try {
            org.geotools.gui.swing.image.OperationTreeBrowser.show(worker.getRenderedImage());
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            // Ignore and go back to work.
        }
        file = worker.getImageFile();
        image = ImageIO.read(file);

        worker.setBoundingBox("-2.5E7,-1.5E7,2E7,1E7"); // Will force a clip.
        file = worker.getImageFile();
        image = ImageIO.read(file);
    }

    /**
     * A test required for the proper working of {@link #testLambert}.
     *
     * @throws Exception If factory or transform exception occured.
     *
     * @see http://jira.codehaus.org/browse/GEOT-1783
     */
    @Test(expected=OperationNotFoundException.class)
    public void testProjected4D() throws Exception {
        CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:3395");
        CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:27572");
        sourceCRS = new DefaultCompoundCRS("3D", sourceCRS, DefaultVerticalCRS.ELLIPSOIDAL_HEIGHT);
        sourceCRS = new DefaultCompoundCRS("4D", sourceCRS, DefaultTemporalCRS.JULIAN);
        try {
            CRS.findMathTransform(sourceCRS, targetCRS);
            fail();
        } catch (OperationNotFoundException e) {
            // Expected exception until GEOT-1783 is fixed.
        }
    }

    /**
     * Tests a request in Lambert projection.
     *
     * @throws WebServiceException If a WMS parameter is illegal.
     * @throws IOException If an error occured while reading an image.
     */
    @Test
    @Ignore
    public void testLambert() throws WebServiceException, IOException {
        if (true) return; // @Ignore above is not enough...
        if (DISABLED) return;
        final WebServiceWorker worker = new WebServiceWorker(database, false);
        worker.setService("WMS", "1.1.1");
        worker.setLayer(LayerTableTest.NETCDF_NAME);
        worker.setCoordinateReferenceSystem("EPSG:27572");
        worker.setTime(LayerTableTest.NETCDF_TIME_AS_TEXT);
        worker.setElevation("5.0");
        worker.setBoundingBox("-742914.357143, 1634430.060549, 855611.357143, 2756573.939451");
        worker.setDimension("604", "424", null);
        worker.setColormapRange("-3.0, 40.0");
        worker.setFormat("image/png");
        RenderedImage image = worker.getRenderedImage();
        assertEquals(Transparency.BITMASK, image.getColorModel().getTransparency());
        if (true) try {
            org.geotools.gui.swing.image.OperationTreeBrowser.show(image);
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            // Ignore and go back to work.
        }
        File file = worker.getImageFile();
        assertTrue(file.getName().endsWith(".png"));
        assertTrue(file.isFile());
        image = ImageIO.read(file);
        assertEquals(604, image.getWidth());
        assertEquals(424, image.getHeight());
        assertEquals(Transparency.BITMASK, image.getColorModel().getTransparency());
        if (false) try {
            org.geotools.gui.swing.image.OperationTreeBrowser.show(worker.getRenderedImage());
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            // Ignore and go back to work.
        }
    }

    /**
     * Tests with BlueMarble layer.
     *
     * @throws WebServiceException If a WMS parameter is illegal.
     * @throws IOException If an error occured while reading an image.
     */
    @Test
    public void testBlueMarble() throws WebServiceException, IOException {
        if (DISABLED) return;
        final WebServiceWorker worker = new WebServiceWorker(database, false);
        worker.setService("WMS", "1.1");
        worker.setLayer("BlueMarble");
        worker.setCoordinateReferenceSystem("EPSG:4326");
        worker.setBoundingBox("-180,-90,180,90");
        worker.setDimension("360", "180", null);

        Layer layer = worker.getLayer();
        assertEquals(layer.getName(), "BlueMarble");
        assertSame("The layer should be cached.", layer, worker.getLayer());

        GridCoverage2D coverage = worker.getGridCoverage2D(false);
        assertSame("The coverage should be cached.", coverage, worker.getGridCoverage2D(false));
        RenderedImage image = coverage.getRenderedImage();
        assertEquals(480, image.getWidth());
        assertEquals(240, image.getHeight());

        File file = worker.getImageFile();
        assertTrue(file.getName().endsWith(".png"));

        image = ImageIO.read(file);
        assertEquals(360, image.getWidth());
        assertEquals(180, image.getHeight());

        worker.setService("WMS", "1.1.1");
        worker.setBoundingBox("-28.5375, 26.5875, -6.0375, 42.3375");
        worker.setDimension("600", "420", null);
        file = worker.getImageFile();
        image = ImageIO.read(file);
        assertEquals(600, image.getWidth());
        assertEquals(420, image.getHeight());

        worker.setCoordinateReferenceSystem("EPSG:3395");
        worker.setBoundingBox("3085245.489437, 696668.336325, 8094622.574437, 4213184.700629");
        worker.setDimension("604", "424", null);

        file = worker.getImageFile();
        image = ImageIO.read(file);
        assertEquals(604, image.getWidth());
        assertEquals(424, image.getHeight());
    }

    /**
     * Tests with the Ortho2000 layer.
     *
     * @throws WebServiceException If a WMS parameter is illegal.
     * @throws IOException If an error occured while reading an image.
     */
    @Test
    public void testOrtho2000() throws WebServiceException, IOException {
        //if (DISABLED) return;
        final WebServiceWorker worker = new WebServiceWorker(database, false);
        worker.setService("WMS", "1.1.1");
        worker.setLayer("Ortho2000");
        worker.setCoordinateReferenceSystem("EPSG:27572");
        worker.setDimension("604", "424", null);
        /*
         * Global view.
         */
        worker.setBoundingBox("-215869.691647, 1632837.614174, 1387192.992499, 2758166.385826");
        RenderedImage image = ImageIO.read(worker.getImageFile());
        assertEquals(604, image.getWidth());
        assertEquals(424, image.getHeight());
        /*
         * Following was used to throw a NullPointerException.
         */
        worker.setBoundingBox("203806.280092, 2174932.90099, 303997.697852, 2245265.949218");
        image = ImageIO.read(worker.getImageFile());
        assertEquals(604, image.getWidth());
        assertEquals(424, image.getHeight());
        /*
         * Following was used to miss a tile (need visual check - look for black area).
         * BBOX demandée: 49°18'28,0"N, 01°02'43,1"W - 49°18'09,2"N, 01°02'03,3"W
         * BBOX utilisée: 49°18'29,7"N, 01°02'43,2"W - 49°18'08,4"N, 01°01'57,4"W
         * BBOX obtenue:  49°18'31,3"N, 01°02'41,5"W - 49°18'07,5"N, 01°01'52,9"W
         */
        worker.setBoundingBox("353875.689453, 2483613.235058, 354658.434904, 2484162.711997");
        image = ImageIO.read(worker.getImageFile());
        assertEquals(604, image.getWidth());
        assertEquals(424, image.getHeight());
    }

    /**
     * Tests the permission.
     *
     * @throws WebServiceException If a WMS parameter is illegal.
     * @throws IOException If an error occured while reading an image.
     *
     * @todo Disabled for now.
     */
    @Test
    public void testPermissions() throws WebServiceException, IOException {
        if (true) return;

        if (DISABLED) return;

        // First Case we test with an "Anonymous" user.
        database.setProperty(ConfigurationKey.PERMISSION, "Anonymous");

        //Blue Marble is Public so we can see it in WCS and WMS

        // for a request WS/wms?bbox=-33.52649,25.033113,-25.927152,31.142384&format=image/png&service=wms&version=1.1.1&request=GetMap&layers=AO_Coriolis_(Sal)&time=2007-06-20T12:00:00Z&srs=EPSG:4326&width=608&height=428&styles=dd
        final WebServiceWorker worker = new WebServiceWorker(database, false);
        worker.setService("WMS", "1.1");
        worker.setLayer("AO_Coriolis_(Sal)");
        worker.setCoordinateReferenceSystem("EPSG:4326");
        worker.setBoundingBox("-33.52649,25.033113,-25.927152,31.142384");
        worker.setDimension("608", "428", null);
        worker.setFormat("image/png");
        worker.setTime("2007-06-20T12:00:00Z");

        Layer layer = worker.getLayer();
        assertEquals(layer.getName(), "AO_Coriolis_(Sal)");
        assertSame("The layer should be cached.", layer, worker.getLayer());

        GridCoverage2D coverage = worker.getGridCoverage2D(false);
        assertSame("The coverage should be cached.", coverage, worker.getGridCoverage2D(false));

        File file = worker.getImageFile();
        assertTrue(file.getName().endsWith(".png"));

        RenderedImage image = ImageIO.read(file);
        assertEquals(608, image.getWidth());
        assertEquals(428, image.getHeight());

        //we test with the WCS
        // corresponding to the following request
        ///WS/wcs?bbox=-33.52649,25.033113,-25.927152,31.142384,5.0,5.0&format=image/png&service=wcs&version=1.0.0&request=GetCoverage&coverage=AO_Coriolis_(Sal)&time=2007-06-20T12:00:00Z&crs=EPSG:4326&width=608&height=428

        worker.setService("WCS", "1.0.0");
        worker.setBoundingBox("-33.52649,25.033113,-25.927152,31.142384");
        worker.setLayer("AO_Coriolis_(Sal)");
        worker.setCoordinateReferenceSystem("EPSG:4326");
        worker.setDimension("608", "428", null);
        worker.setFormat("image/png");
        worker.setTime("2007-06-20T12:00:00Z");
        file = worker.getImageFile();
        assertTrue(file.getName().endsWith(".png"));
        image = ImageIO.read(file);
        assertEquals(608, image.getWidth());
        assertEquals(428, image.getHeight());


       // Mars3D Ligure (XE) is Download so only WCS for anonymous user.

        worker.setService("WCS", "1.0.0");
        worker.setBoundingBox("-33.52649,25.033113,-25.927152,31.142384");
        worker.setLayer("AO_Coriolis_(Temp)");
        worker.setCoordinateReferenceSystem("EPSG:4326");
        worker.setDimension("608", "428", null);
        worker.setFormat("image/png");
        worker.setTime("2007-06-20T12:00:00Z");
        file = worker.getImageFile();
        assertTrue(file.getName().endsWith(".png"));
        image = ImageIO.read(file);
        assertEquals(608, image.getWidth());
        assertEquals(428, image.getHeight());

        //WMS must not work
        worker.setService("WMS", "1.1");
        worker.setLayer("AO_Coriolis_(Temp)");
        worker.setCoordinateReferenceSystem("EPSG:4326");
        worker.setBoundingBox("-33.52649,25.033113,-25.927152,31.142384");
        worker.setDimension("608", "428", null);
        worker.setFormat("image/png");
        worker.setTime("2007-06-20T12:00:00Z");

        layer = worker.getLayer();
        assertEquals(layer.getName(), "AO_Coriolis_(Temp)");
        assertSame("The layer should be cached.", layer, worker.getLayer());

        coverage = worker.getGridCoverage2D(false);
        assertSame("The coverage should be cached.", coverage, worker.getGridCoverage2D(false));

        file = worker.getImageFile();
        assertTrue(file.getName().endsWith(".png"));

        image = ImageIO.read(file);
        assertEquals(608, image.getWidth());
        assertEquals(428, image.getHeight());
    }
}
