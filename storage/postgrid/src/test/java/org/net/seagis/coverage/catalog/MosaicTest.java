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
package net.seagis.coverage.catalog;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;
import java.util.Set;
import java.io.File;
import org.junit.*;

import org.geotools.coverage.grid.ViewType;
import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.metadata.extent.GeographicBoundingBox;

import net.seagis.catalog.DatabaseTest;


/**
 * Tests a mosaic using Blue Marble.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MosaicTest extends DatabaseTest {
    /**
     * The layer name in the database.
     */
    private static final String BLUEMARBLE = "BlueMarble", ORTHO2000 = "Ortho2000";

    /**
     * Small number for floating point comparaisons.
     */
    private static final double EPS = 1E-6;

    /**
     * Tests the BlueMarble layer. We try first with a query on the full extent (no subarea, no
     * subsampling) without trying to load the image since it would be too big. Next we test the
     * global area with a 1°&times;1° pixel size. The image should be 360&times;180 pixel width.
     *
     * @throws Exception If an I/O, SQL or catalog exception occured.
     */
    @Test
    public void testBlueMarble() throws Exception {
        LayerTable table = database.getTable(LayerTable.class);
        Layer layer = table.getEntry(BLUEMARBLE);
        assertNotNull(layer);

        Set<CoverageReference> entries = layer.getCoverageReferences();
        assertEquals(1, entries.size());

        CoverageReference entry = entries.iterator().next();
        CoverageReference other = layer.getCoverageReferences().iterator().next();
        assertEquals("Should be cached", entry, other);
        assertSame  ("Should be cached", entry, other);
        assertEquals(BLUEMARBLE, entry.getSeries().getName());

        GeographicBoundingBox box = entry.getGeographicBoundingBox();
        assertEquals(-180, box.getWestBoundLongitude(), EPS);
        assertEquals(+180, box.getEastBoundLongitude(), EPS);
        assertEquals( -90, box.getSouthBoundLatitude(), EPS);
        assertEquals( +90, box.getNorthBoundLatitude(), EPS);

        Rectangle range = entry.getGridGeometry().getGridRange2D();
        assertEquals(4*21600, range.width);
        assertEquals(2*21600, range.height);
        /*
         * Applies subsampling.
         */
        table = new LayerTable(table);
        table.setPreferredResolution(new Dimension(1,1));
        layer = table.getEntry(BLUEMARBLE);
        assertNotNull(layer);

        entries = layer.getCoverageReferences();
        assertEquals(1, entries.size());

        entry = layer.getCoverageReference();
        other = layer.getCoverageReference();
        assertEquals("Should be cached", entry, other);
        assertSame  ("Should be cached", entry, other);
        assertEquals(BLUEMARBLE, entry.getSeries().getName());
        assertTrue  (entries.contains(entry));

        box = entry.getGeographicBoundingBox();
        assertEquals(-180, box.getWestBoundLongitude(), EPS);
        assertEquals(+180, box.getEastBoundLongitude(), EPS);
        assertEquals( -90, box.getSouthBoundLatitude(), EPS);
        assertEquals( +90, box.getNorthBoundLatitude(), EPS);

        range = entry.getGridGeometry().getGridRange2D();
        assertEquals(360, range.width);
        assertEquals(180, range.height);

        GridCoverage2D coverage = entry.getCoverage(null);
        RenderedImage image = coverage.view(ViewType.RENDERED).getRenderedImage();
        // The image size is different than the expected one because ImageMosaicImageReader
        // selected a different subsampling than the requested one for performance reasons.
        assertEquals(480, image.getWidth());
        assertEquals(240, image.getHeight());
        if (false) {
            ImageIO.write(image, "png", new File("Test-BlueMarble.png"));
        }
    }

    /**
     * Tests loading a mosaic from Ortho Littorale 2000.
     *
     * @throws Exception If an I/O, SQL or catalog exception occured.
     */
    @Test
    public void testOrtho2000() throws Exception {
        final LayerTable table = new LayerTable(database.getTable(LayerTable.class));
        Layer layer = table.getEntry(ORTHO2000);
        assertNotNull(layer);
        CoverageReference entry = layer.getCoverageReference();
        Rectangle range = entry.getGridGeometry().getGridRange2D();
        assertEquals(10000, range.width);
        assertEquals(20000, range.height);

        table.setPreferredResolution(new Dimension(1,1));
        layer = table.getEntry(ORTHO2000);
        entry = layer.getCoverageReference();
        GridCoverage2D coverage = entry.getCoverage(null);
        RenderedImage image = coverage.view(ViewType.RENDERED).getRenderedImage();
        if (false) {
            ImageIO.write(image, "png", new File("Test-Ortho2000.png"));
        }
    }
}
