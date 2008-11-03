/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.junit.Test;

import org.constellation.catalog.Element;
import org.constellation.catalog.TableTest;
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.CatalogException;


/**
 * Tests the addition of new entries in the database.
 *
 * @version $Id$
 * @author Cédric Briançon
 * @author Martin Desruisseaux
 */
public class WritableGridCoverageTableTest extends TableTest {
    /**
     * The file to test for inclusion, relative to the data root directory.
     */
    private static final String TEST_FILE = "Monde/SST/Coriolis/OA_RTQCGL01_20070606_FLD_TEMP.nc";

    /**
     * Tests a {@code INSERT} statement (but do not really performs the insert).
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws IOException      Should never happen in normal test execution.
     * @throws CatalogException Should never happen in normal test execution.
     */
    @Test
    public void testPseudoInserts() throws SQLException, CatalogException, IOException {
        final StringWriter insertStatements = new StringWriter();
        database.setUpdateSimulator(new PrintWriter(insertStatements));

        final String root = database.getProperty(ConfigurationKey.ROOT_DIRECTORY);
        assertNotNull("The ROOT_DIRECTORY property must be defined.", root);
        final File file = new File(root, TEST_FILE);
        if (!file.isFile()) {
            Element.LOGGER.warning("Test file \"" + file + "\" not found.");
            return;
        }
        final LayerTable layers = database.getTable(LayerTable.class);
        final Layer layer = layers.getEntry(LayerTableTest.NETCDF_NAME);
        final Set<Series> series = layer.getSeries();
        assertEquals("Expected only one series in the layer.", 1, series.size());
        final Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("NetCDF");
        assertTrue("A NetCDF reader must be available.", readers.hasNext());
        final ImageReader reader = readers.next();
        reader.setInput(file);

        WritableGridCoverageTable table = database.getTable(WritableGridCoverageTable.class);
        try {
            table.addEntry(reader);
            fail("Should not accept to add an entry without layer.");
        } catch (CatalogException exception) {
            // This is the expected exception since no layer has been specified.
        }
        try {
            table.setLayer(layer);
            fail("Should not accept to modify a shared table.");
        } catch (IllegalStateException exception) {
            // This is the expected exception since the table has not been cloned.
        }
        table = new WritableGridCoverageTable(table);
        table.setLayer(layer);
        table.addEntry(reader);
        reader.dispose();
    }
}
