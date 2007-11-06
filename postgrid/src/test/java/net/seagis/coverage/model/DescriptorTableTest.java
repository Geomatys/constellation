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
package net.seagis.coverage.model;

import java.sql.SQLException;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.DatabaseTest;
import net.seagis.coverage.catalog.LayerTableTest;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests {@link DescriptorTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DescriptorTableTest extends DatabaseTest {
    /**
     * The name of the descriptor to be tested.
     */
    public static final String SAMPLE_NAME = "∇₃SST";

    /**
     * Tests the {@link DescriptorTable#getEntry} method.
     */
    @Test
    public void testSelectAndList() throws CatalogException, SQLException {
        final DescriptorTable table = new DescriptorTable(database);
        final Descriptor      entry = table.getEntry(SAMPLE_NAME);
        assertEquals(SAMPLE_NAME, entry.getName());
        assertSame(entry, table.getEntry(SAMPLE_NAME));
        assertEquals(LayerTableTest.SAMPLE_NAME, entry.getLayer().getName());
        assertEquals(OperationTableTest.SAMPLE_NAME, entry.getOperation().getName());
    }
}
