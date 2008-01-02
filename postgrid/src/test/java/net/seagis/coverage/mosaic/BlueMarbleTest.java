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
package net.seagis.coverage.mosaic;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;
import java.util.Set;
import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.LayerTable;
import net.seagis.coverage.catalog.CoverageReference;
import net.seagis.catalog.DatabaseTest;


/**
 * Tests a mosaic using Blue Marble.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class BlueMarbleTest extends DatabaseTest {
    /**
     * The layer name in the database.
     */
    private static final String LAYER = "BlueMarble";

    /**
     * Tests a query on the full extent (no subarea, no subsamlping).
     * We will not try to load this image, since it would be too big.
     */
    @Test
    public void testGlobal() throws Exception {
        final Layer layer = database.getTable(LayerTable.class).getEntry(LAYER);
        assertNotNull(layer);

        Set<CoverageReference> entries = layer.getCoverageReferences();
        assertEquals(1, entries.size());

        CoverageReference entry = entries.iterator().next();
        assertSame("Should be cached", entry, layer.getCoverageReferences().iterator().next());
        assertEquals("BlueMarble", entry.getSeries().getName());
        assertNull(entry.getFile());
        assertNull(entry.getURI());
        assertEquals(GeographicBoundingBoxImpl.WORLD, entry.getGeographicBoundingBox());

        Rectangle range = entry.getGridGeometry().getGridRange2D();
        assertEquals(4*21600, range.width);
        assertEquals(2*21600, range.height);
    }

    /**
     * Tests the global area with a 1°&times;1° pixel size.
     * The image should be 360&times;180 pixel width.
     */
    @Test
    public void testSubsampling() throws Exception {
        final LayerTable table = new LayerTable(database.getTable(LayerTable.class));
        table.setPreferredResolution(new Dimension(1,1));
        final Layer layer = table.getEntry(LAYER);
        assertNotNull(layer);

        Set<CoverageReference> entries = layer.getCoverageReferences();
        assertEquals(1, entries.size());

        CoverageReference entry = entries.iterator().next();
        assertSame("Should be cached", entry, layer.getCoverageReferences().iterator().next());
        assertEquals("BlueMarble", entry.getSeries().getName());
        assertNull(entry.getFile());
        assertNull(entry.getURI());
        assertEquals(GeographicBoundingBoxImpl.WORLD, entry.getGeographicBoundingBox());

        Rectangle range = entry.getGridGeometry().getGridRange2D();
        assertEquals(360, range.width);
        assertEquals(180, range.height);

        GridCoverage2D coverage = entry.getCoverage(null);
        RenderedImage image = coverage.geophysics(false).getRenderedImage();
        assertEquals(360, image.getWidth());
        assertEquals(180, image.getHeight());
        ImageIO.write(image, "png", new File("/home/desruisseaux/Test.png"));
    }

    /**
     * Tests a mosaic without pyramid.
     */
    @Test
    public void testMosaic() throws Exception {

    }
}
