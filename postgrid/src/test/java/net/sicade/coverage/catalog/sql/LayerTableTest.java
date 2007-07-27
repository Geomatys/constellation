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
import net.sicade.coverage.catalog.Layer;
import net.sicade.catalog.DatabaseTest;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests {@link LayerTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class LayerTableTest extends DatabaseTest {
    /**
     * The name of the layer to be tested.
     */
    public static final String SAMPLE_NAME = "SST (Monde - hebdomadaires)";

    /**
     * Tests the {@link LayerTableTest#getEntry} and @link LayerTableTest#getEntries} methods.
     */
    @Test
    public void testSelectAndList() throws CatalogException, SQLException {
        final LayerTable table = new LayerTable(database);
        final Layer      entry = table.getEntry(SAMPLE_NAME);
        assertEquals(SAMPLE_NAME, entry.getName());
        assertSame(entry, table.getEntry(SAMPLE_NAME));

        final Set<Layer> entries = table.getEntries();
        assertFalse(entries.isEmpty());
        assertTrue(entries.contains(entry));
    }
}
