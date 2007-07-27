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

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import net.sicade.coverage.catalog.CatalogException;
import net.sicade.coverage.catalog.CoverageReference;
import net.sicade.sql.DatabaseTest;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests {@link GridCoverageTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridCoverageTableTest extends DatabaseTest {
    /**
     * The name of the coverage to be tested.
     */
    public static final String SAMPLE_NAME = "198602";

    /**
     * The format for the date used in this test.
     */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Tests the {@link GridCoverageTable#getEntry} and {@link GridCoverageTable#getEntries} methods.
     */
    @Test
    public void testSelectAndList() throws CatalogException, SQLException, ParseException {
        final GridCoverageTable table = new GridCoverageTable(database);
        table.setLayer(LayerTableTest.SAMPLE_NAME);
        final CoverageReference entry = table.getEntry(SAMPLE_NAME);
        assertEquals(SeriesTableTest.SAMPLE_NAME + ':' + SAMPLE_NAME, entry.getName());
        assertSame(entry, table.getEntry(SAMPLE_NAME));

        table.setTimeRange(date("1986-01-05"), date("1986-01-20"));
        final Set<CoverageReference> entries = table.getEntries();
        assertEquals(3, entries.size());
        assertTrue(entries.contains(entry));
        assertSame(entry, table.getEntry());

        final SortedMap<Date, SortedSet<Number>> centroids = table.getAvailableCentroids();
        assertEquals(entries.size(), centroids.size());
        final Set<Number> depths = centroids.get(date("1986-01-13"));
        assertNotNull(depths);
        assertTrue(depths.isEmpty());

        final Set<Date> availableTimes = table.getAvailableTimes();
        assertEquals(centroids.keySet(), availableTimes);

        final Set<Number> altitudes = table.getAvailableAltitudes();
        assertTrue(altitudes.isEmpty());
    }

    /**
     * Returns the specified date.
     */
    private static Date date(final String asText) throws ParseException {
        return DATE_FORMAT.parse(asText);
    }
}
