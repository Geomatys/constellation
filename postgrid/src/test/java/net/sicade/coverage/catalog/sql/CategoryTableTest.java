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
import org.geotools.coverage.Category;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.DatabaseTest;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests {@link CategoryTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CategoryTableTest extends DatabaseTest {
    /**
     * The name of the quantitative category to be tested.
     */
    public static final String SAMPLE_NAME = "Température";

    /**
     * Tests the {@link CategoryTable#getCategories} method.
     */
    @Test
    public void testSelect() throws CatalogException, SQLException {
        final CategoryTable table   = new CategoryTable(database);
        final Category[]    entries = table.getCategories(SampleDimensionTableTest.SAMPLE_NAME);
        assertEquals(2, entries.length);
        assertEquals(SAMPLE_NAME, entries[1].getName().toString());
    }
}
