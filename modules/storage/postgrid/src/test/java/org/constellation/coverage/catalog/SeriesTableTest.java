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
package org.constellation.coverage.catalog;

import org.constellation.catalog.TableTest;
import java.util.Set;
import java.sql.SQLException;
import org.constellation.catalog.CatalogException;

import org.junit.*;


/**
 * Tests {@link SeriesTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SeriesTableTest extends TableTest {
    /**
     * The name of the series to be tested.
     */
    public static final String SAMPLE_NAME = "WTH";

    /**
     * Tests the {@link SeriesTable#getEntry} and {@link SeriesTable#getEntries} methods.
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws CatalogException Should never happen in normal test execution.
     */
    @Test
    public void testSelectAndList() throws CatalogException, SQLException {
        final SeriesTable table = new SeriesTable(database);
        final Series      entry = table.getEntry(SAMPLE_NAME);
        final String      name  = entry.getName();
        assertEquals(SAMPLE_NAME, entry.getName());
        assertSame(entry, table.getEntry(SAMPLE_NAME));

        final Set<Series> entries = table.getEntries();
        assertFalse(entries.isEmpty());
        assertTrue(entries.contains(entry));

        // Ask only for series in a given layer.
        assertNull(table.getLayer());
        table.setLayer(new LayerEntry(LayerTableTest.SAMPLE_NAME, null, null, 1, null));
        assertNotSame("Should be assigned to a Layer", entry, table.getEntry(SAMPLE_NAME));
        assertEquals(name, table.getEntry(SAMPLE_NAME).getName());

        final Set<Series> filtered = table.getEntries();
        assertFalse(filtered.isEmpty());
        assertTrue(filtered.size() < entries.size());

        // Restore the full list
        assertNotNull(table.getLayer());
        table.setLayer(null);
        assertEquals(name, table.getEntry(SAMPLE_NAME).getName());
        assertEquals(entries, table.getEntries());
    }
}
