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
package net.seagis.coverage.catalog;

import java.io.File;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import junit.framework.TestCase;
import org.junit.Test;

import org.geotools.util.MeasurementRange;
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
     */
    @Test
    public void testCoriolis() throws Exception {
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
