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

import java.util.Set;
import java.sql.SQLException;
import org.geotools.coverage.GridSampleDimension;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.TableTest;

import org.junit.*;


/**
 * Tests {@link FormatTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FormatTableTest extends TableTest {
    /**
     * The name of the format to be tested.
     */
    public static final String SAMPLE_NAME = "PNG Température [-3 .. 35,25 °C]";

    /**
     * The name of a format with two bands.
     */
    public static final String SAMPLE_NAME_2 = "Mars (u,v)";

    /**
     * Tests the {@link FormatTable#getEntry} and {@link FormatTable#getEntries} methods.
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws CatalogException Should never happen in normal test execution.
     */
    @Test
    public void testSelectAndList() throws CatalogException, SQLException {
        final FormatTable table = new FormatTable(database);
        final Format entry = table.getEntry(SAMPLE_NAME);
        assertEquals(SAMPLE_NAME, entry.getName());
        assertSame(entry, table.getEntry(SAMPLE_NAME));
        assertEquals("image/png", entry.getImageFormat());
        final GridSampleDimension[] bands = entry.getSampleDimensions();
        assertEquals(1, bands.length);

        final Set<Format> entries = table.getEntries();
        assertFalse(entries.isEmpty());
        assertTrue(entries.contains(entry));
    }

    /**
     * Tests a for an entry having two bands
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws CatalogException Should never happen in normal test execution.
     */
    @Test
    public void testTwoBands() throws CatalogException, SQLException {
        final FormatTable table = new FormatTable(database);
        final Format entry = table.getEntry(SAMPLE_NAME_2);
        assertEquals(SAMPLE_NAME_2, entry.getName());
        assertSame(entry, table.getEntry(SAMPLE_NAME_2));
        final GridSampleDimension[] bands = entry.getSampleDimensions();
        assertEquals(2, bands.length);
        assertFalse(bands[0].equals(bands[1]));

        final Set<Format> entries = table.getEntries();
        assertFalse(entries.isEmpty());
        assertTrue(entries.contains(entry));
    }
}
