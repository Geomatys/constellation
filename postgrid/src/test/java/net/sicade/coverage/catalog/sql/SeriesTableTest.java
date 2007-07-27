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
import net.sicade.coverage.catalog.CatalogException;
import net.sicade.coverage.catalog.Series;
import net.sicade.catalog.DatabaseTest;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests {@link SeriesTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SeriesTableTest extends DatabaseTest {
    /**
     * The name of the series to be tested.
     */
    public static final String SAMPLE_NAME = "WTH";

    /**
     * Tests the {@link SeriesTable#getEntry} and {@link SeriesTable#getEntries} methods.
     */
    @Test
    public void testSelectAndList() throws CatalogException, SQLException {
        final SeriesTable table = new SeriesTable(database);
        final Series      entry = table.getEntry(SAMPLE_NAME);
        assertEquals(SAMPLE_NAME, entry.getName());
        assertSame(entry, table.getEntry(SAMPLE_NAME));

        final Set<Series> entries = table.getEntries();
        assertFalse(entries.isEmpty());
        assertTrue(entries.contains(entry));

        // Ask only for series in a given layer.
        assertNull(table.getLayer());
        table.setLayer(new LayerEntry(LayerTableTest.SAMPLE_NAME, null, null, 1, null));
        assertSame(entry, table.getEntry(SAMPLE_NAME));

        final Set<Series> filtered = table.getEntries();
        assertFalse(filtered.isEmpty());
        assertTrue(filtered.contains(entry));
        assertTrue(entries.containsAll(filtered));
        assertTrue(filtered.size() < entries.size());

        // Restore the full list
        assertNotNull(table.getLayer());
        table.setLayer(null);
        assertSame(entry, table.getEntry(SAMPLE_NAME));
        assertEquals(entries, table.getEntries());
    }
}
