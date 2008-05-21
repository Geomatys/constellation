/*
 * Ecocast - NASA Ames Research Center
 * (C) 2008, Ecocast
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
package net.seagis.coverage.metadata;

import net.seagis.coverage.metadata.PointOfContactTable;
import net.seagis.coverage.metadata.PointOfContact;
import java.util.Set;
import java.util.Date;
import java.sql.SQLException;

import org.geotools.util.NumberRange;
import net.seagis.catalog.TableTest;
import net.seagis.catalog.CatalogException;

import org.junit.*;


/**
 * Tests {@link PointOfContactTable}.
 *
 * @version $Id: PointOfContactTableTest.java  $
 * @author Sam Hiatt
 */
public class PointOfContactTableTest extends TableTest {
    /**
     * The name of the layer metadata set to be tested.
     */
    public static final String id = "123";
    /**
     * Tests the {@link LayerTableTest#getIdentifiers} method.
     */
    @Test
    public void testIdentifiers() throws CatalogException, SQLException {
        final PointOfContactTable table = new PointOfContactTable(database);
        final Set<String> identifiers = table.getIdentifiers();
        assertTrue(identifiers.contains(id));
    }

    /**
     * Tests the {@link LayerTableTest#getEntry} and @link LayerTableTest#getEntries} methods.
     */
    @Test
    public void testSelectAndList() throws CatalogException, SQLException {
        final PointOfContactTable table = new PointOfContactTable(database);
        final PointOfContact entry = table.getEntry(id);
        //assertEquals(id, entry.getName());
        assertSame("Should be cached.", entry, table.getEntry(id));

        final Set<PointOfContact> entries = table.getEntries();
        assertFalse(entries.isEmpty());
        assertTrue(entries.contains(entry));
        
        System.out.println(entry.getMetadata());

    }

}
