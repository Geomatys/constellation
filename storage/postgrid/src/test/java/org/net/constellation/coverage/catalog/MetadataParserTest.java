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
package net.seagis.coverage.catalog;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import net.seagis.catalog.CatalogException;
import org.junit.Test;

import org.geotools.util.DateRange;

import net.seagis.catalog.Element;
import net.seagis.catalog.DatabaseTest;


/**
 * Tests {@link MetadataParser}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MetadataParserTest extends DatabaseTest {
    /**
     * The file to test for inclusion.
     *
     * @todo Needs a smaller test file so we can commit it on SVN in a relative directory.
     */
    private static final String TEST_FILE = "/home/desruisseaux/Données/PostGRID/Monde/SST/Coriolis/OA_RTQCGL01_20070606_FLD_TEMP.nc";

    /**
     * Tests on a NetCDF file.
     *
     * @throws IOException      If the test file can not be read.
     * @throws CatalogException Should never happen in normal test execution.
     */
    @Test
    public void testCoriolis() throws IOException, CatalogException {
        final File file = new File(TEST_FILE);
        if (!file.isFile()) {
            Element.LOGGER.warning("Test file \"" + file + "\" not found.");
            return;
        }
        final Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("NetCDF");
        assertTrue("A NetCDF reader must be available.", readers.hasNext());
        final ImageReader reader = readers.next();
        reader.setInput(file);

        final MetadataParser metadata = new MetadataParser(database, reader, 0);
        final DateRange[] dates = metadata.getDateRanges();
        assertNotNull(dates);
        assertEquals(1, dates.length);

        final DateRange dateRange = dates[0];
        assertNotNull(dateRange);
        assertTrue  (dateRange.isMinIncluded());
        assertTrue  (dateRange.isMaxIncluded());
        assertEquals(dateRange.getMinValue(), dateRange.getMaxValue());
        assertEquals(1181088000000L, dateRange.getMinValue().getTime()); // June 6, 2007.

        reader.dispose();
    }
}
