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
package net.sicade.observation.coverage.sql;

import java.util.Set;
import java.sql.SQLException;
import net.sicade.observation.CatalogException;
import net.sicade.observation.Distribution;
import net.sicade.observation.sql.DatabaseTest;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests {@link DistributionTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DistributionTableTest extends DatabaseTest {
    /**
     * The name of the thematic to be tested.
     */
    public static final String SAMPLE_NAME = "amplitude ∇SST";

    /**
     * Tests the {@link DistributionTable#getEntry} and {@link DistributionTable#getEntries} methods.
     */
    @Test
    public void testSelectAndList() throws CatalogException, SQLException {
        final DistributionTable table = new DistributionTable(database);
        final Distribution      entry = table.getEntry(SAMPLE_NAME);
        assertEquals(SAMPLE_NAME, entry.getName());
        assertSame(entry, table.getEntry(SAMPLE_NAME));

        final Set<Distribution> entries = table.getEntries();
        assertFalse(entries.isEmpty());
        assertTrue(entries.contains(entry));
    }
}
