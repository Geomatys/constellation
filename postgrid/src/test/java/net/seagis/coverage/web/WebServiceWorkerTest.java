/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.web;

import java.util.Set;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Transparency;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.IndexColorModel;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.referencing.crs.DefaultVerticalCRS;
import org.geotools.coverage.grid.ViewType;
import org.geotools.coverage.grid.GridCoverage2D;

import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.LayerTableTest;
import net.seagis.catalog.DatabaseTest;

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
    private static final boolean DISABLED = false;

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

        GridCoverage2D coverage = worker.getGridCoverage2D(false);
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
}
