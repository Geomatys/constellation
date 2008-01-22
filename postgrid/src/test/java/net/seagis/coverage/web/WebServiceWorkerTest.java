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

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Transparency;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.IndexColorModel;

import org.geotools.util.logging.Logging;
import org.geotools.coverage.grid.GridCoverage2D;

import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.LayerTableTest;
import net.seagis.catalog.DatabaseTest;

import org.junit.Test;


/**
 * Tests {@link WebServiceWorkerTest}.
 *
 * @author Martin Desruisseaux
 */
public class WebServiceWorkerTest extends DatabaseTest {
    /**
     * Tests with the default test layer.
     */
    @Test
    public void testSST() throws WebServiceException, IOException {
        final WebServiceWorker worker = new WebServiceWorker(database);
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
     */
    @Test
    public void testNetCDF() throws WebServiceException, IOException {
        final WebServiceWorker worker = new WebServiceWorker(database);
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
        } catch (WebServiceException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof IOException) {
                // The test datafile is not present on every systems.
                Logging.recoverableException(WebServiceWorkerTest.class, "testNetCDF", cause);
                return;
            }
            throw exception;
        }
        assertEquals(4, coverage.getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
        assertSame("The coverage should be cached.", coverage, worker.getGridCoverage2D(false));
        assertEquals(2, worker.getGridCoverage2D(true).getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
        RenderedImage image = worker.getRenderedImage();
        assertEquals(Transparency.BITMASK, image.getColorModel().getTransparency());
        if (false) try {
            // TODO: Need more debugging. We want to avoid creating two consecutive "SampleTranscoder" operations.
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
    }

    /**
     * Tests with BlueMarble layer.
     */
    @Test
    public void testBlueMarble() throws WebServiceException, IOException {
        if (true) {
            return; // TODO
        }
        final WebServiceWorker worker = new WebServiceWorker(database);
        worker.setService("WMS", "1.1.1");
        worker.setLayer("BlueMarble");
        worker.setCoordinateReferenceSystem("EPSG:4326");
        worker.setBoundingBox("-180,-90,180,90");
        worker.setDimension("360", "180", null);

        Layer layer = worker.getLayer();
        assertEquals(layer.getName(), "BlueMarble");
        assertSame("The layer should be cached.", layer, worker.getLayer());

        GridCoverage2D coverage = worker.getGridCoverage2D(false);
        assertSame("The coverage should be cached.", coverage, worker.getGridCoverage2D(false));

        File file = worker.getImageFile();
        assertTrue(file.getName().endsWith(".png"));

        RenderedImage image = ImageIO.read(file);
        assertEquals(360, image.getWidth());
        assertEquals(180, image.getHeight());
    }
}
