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
package org.constellation.coverage.model;

import java.sql.SQLException;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.TableTest;
import org.constellation.coverage.catalog.LayerTableTest;

import org.junit.*;


/**
 * Tests {@link DescriptorTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DescriptorTableTest extends TableTest {
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
