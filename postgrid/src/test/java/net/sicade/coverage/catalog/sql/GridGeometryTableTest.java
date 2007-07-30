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
package net.sicade.coverage.catalog.sql;

import java.util.Set;
import java.sql.SQLException;
import org.opengis.geometry.Envelope;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.DatabaseTest;
import net.sicade.catalog.CRS;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests {@link GridGeometryTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridGeometryTableTest extends DatabaseTest {
    /**
     * The name of the geometry to be tested.
     */
    public static final String SAMPLE_NAME = "Coriolis";

    /**
     * Tests some CRS definitions.
     */
    @Test
    public void testCRS() {
        assertEquals(2, CRS.XY  .getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
        assertEquals(3, CRS.XYT .getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
        assertEquals(4, CRS.XYZT.getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
    }

    /**
     * Tests the {@link GridGeometryTable#getEntry} and {@link GridGeometryTable#getEntries} methods.
     */
    @Test
    public void testSelectAndList() throws CatalogException, SQLException {
        final GridGeometryTable table = new GridGeometryTable(database);
        final GridGeometryEntry entry = table.getEntry(SAMPLE_NAME);
        final Envelope       envelope = entry.getEnvelope();
        assertEquals( 720, entry.gridRange.getLength(0));
        assertEquals( 499, entry.gridRange.getLength(1));
        assertEquals(  59, entry.gridRange.getLength(2));
        assertEquals(-180, entry.geographicEnvelope.getMinX(), 0.0);
        assertEquals(+180, entry.geographicEnvelope.getMaxX(), 0.0);
        assertEquals( -77, entry.geographicEnvelope.getMinY(), 0.5);
        assertEquals( +77, entry.geographicEnvelope.getMaxY(), 0.5);

        final double[] altitudes = entry.getVerticalOrdinates();
        assertNotNull(altitudes);
        assertEquals(10, altitudes[1], 0.0);
        assertEquals(8, entry.indexOf(100));

        assertSame(entry, table.getEntry(SAMPLE_NAME));
        final Set<GridGeometryEntry> entries = table.getEntries();
        assertTrue(entries.contains(entry));
    }
}
