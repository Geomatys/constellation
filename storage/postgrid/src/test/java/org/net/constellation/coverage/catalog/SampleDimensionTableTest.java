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

import java.sql.SQLException;
import org.geotools.coverage.GridSampleDimension;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.TableTest;

import org.junit.*;


/**
 * Tests {@link SampleDimensionTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SampleDimensionTableTest extends TableTest {
    /**
     * The name of the band to be tested.
     */
    public static final String SAMPLE_NAME = "SST [-3 .. 35,25°C]";

    /**
     * Tests the {@link SampleDimensionTable#getEntry} method.
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws CatalogException Should never happen in normal test execution.
     */
    @Test
    public void testSelect() throws CatalogException, SQLException {
        final SampleDimensionTable  table   = new SampleDimensionTable(database);
        final GridSampleDimension[] entries = table.getSampleDimensions(FormatTableTest.SAMPLE_NAME);
        assertEquals(1, entries.length);
        assertEquals(SAMPLE_NAME, entries[0].getDescription().toString());
    }
}
