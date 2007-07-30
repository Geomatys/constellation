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
import net.sicade.catalog.CatalogException;
import net.sicade.coverage.catalog.RegionOfInterest;
import net.sicade.catalog.DatabaseTest;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests {@link CategoryTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class RegionOfInterestTableTest extends DatabaseTest {
    /**
     * The name of a ROI to be tested.
     */
    public static final String SAMPLE_NAME = "+00";

    /**
     * Tests the {@link RegionOfInterestTable#getEntry} method.
     */
    @Test
    public void testSelect() throws CatalogException, SQLException {
        final RegionOfInterestTable table   = new RegionOfInterestTable(database);
        final RegionOfInterest entry = table.getEntry(SAMPLE_NAME);
        assertEquals(0.0, entry.getNorthing(),       0.0);
        assertEquals(0.0, entry.getEasting(),        0.0);
        assertEquals(0.0, entry.getAltitudeOffset(), 0.0);
        assertEquals(0.0, entry.getDayOffset(),      0.0);

        final Set<RegionOfInterest> entries = table.getEntries();
        assertTrue(entries.contains(entry));
    }
}
