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

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;

import net.seagis.catalog.CatalogException;
import net.seagis.catalog.TableTest;

import org.junit.*;
import static org.geotools.referencing.CRS.getHorizontalCRS;


/**
 * Tests {@link GridCoverageTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridCoverageTableTest extends TableTest {
    /**
     * The name of the coverage to be tested.
     */
    public static final String SAMPLE_NAME = "198602";

    /**
     * Tests the {@link GridCoverageTable#getEntry} and {@link GridCoverageTable#getEntries} methods.
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws ParseException   Should never happen in normal test execution.
     * @throws CatalogException Should never happen in normal test execution.
     */
    @Test
    public void testSelectAndList() throws CatalogException, SQLException, ParseException {
        final GridCoverageTable table = new GridCoverageTable(database);
        table.setLayer(LayerTableTest.SAMPLE_NAME);
        final Set<Date> allTimes = table.getAvailableTimes();
        final CoverageReference entry = table.getEntry(SAMPLE_NAME);
        assertEquals(SeriesTableTest.SAMPLE_NAME + ':' + SAMPLE_NAME + ":1", entry.getName());
        assertSame(entry, table.getEntry(SAMPLE_NAME));
        assertEquals(1, entry.getSampleDimensions().length);
        /*
         * Tests the envelope of a single entry.
         */
        final Envelope envelope = entry.getEnvelope();
        assertTrue(getHorizontalCRS(envelope.getCoordinateReferenceSystem()) instanceof GeographicCRS);
        assertEquals(-180, envelope.getMinimum(0), 0.0);
        assertEquals(+180, envelope.getMaximum(0), 0.0);
        assertEquals( -90, envelope.getMinimum(1), 0.0);
        assertEquals( +90, envelope.getMaximum(1), 0.0);
        /*
         * Gets the set of entries in the layer.
         */
        table.setTimeRange(LayerTableTest.START_TIME, LayerTableTest.END_TIME);
        final Set<CoverageReference> entries = table.getEntries();
        assertEquals(3, entries.size());
        assertTrue(entries.contains(entry));
        assertSame(entry, table.getEntry());
        /*
         * Tests available centroids.
         */
        final SortedMap<Date, SortedSet<Number>> centroids = table.getAvailableCentroids();
        assertEquals(entries.size(), centroids.size());
        final Set<Number> depths = centroids.get(LayerTableTest.SAMPLE_TIME);
        assertNotNull(depths);
        assertTrue(depths.isEmpty());
        /*
         * Tests available times.
         */
        final Set<Date> availableTimes = table.getAvailableTimes();
        assertEquals(centroids.keySet(), availableTimes);
        assertTrue(allTimes.containsAll(availableTimes));
        assertFalse(availableTimes.containsAll(allTimes));
        /*
         * Tests available elevations.
         */
        final Set<Number> elevations = table.getAvailableElevations();
        assertTrue(elevations.isEmpty());
    }

    /**
     * Tests the table for NetCDF images. They use a Mercator projection.
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws ParseException   Should never happen in normal test execution.
     * @throws CatalogException Should never happen in normal test execution.
     * @throws IOException      If the test image can't be read.
     */
    @Test
    public void testNetCDF() throws CatalogException, SQLException, ParseException, IOException {
        final GridCoverageTable table = new GridCoverageTable(database);
        table.setLayer(LayerTableTest.NETCDF_NAME);
        final Set<Date> availableTimes = table.getAvailableTimes();
        assertEquals(3, availableTimes.size());
        /*
         * Tests a single entry.
         */
        final CoverageReference entry = table.getEntry();
        assertEquals(1, entry.getSampleDimensions().length);
        /*
         * Tests the envelope, which may be projected.
         */
        final Envelope envelope = entry.getEnvelope();
        assertTrue(getHorizontalCRS(envelope.getCoordinateReferenceSystem()) instanceof ProjectedCRS);
        assertEquals(-2.00375E7, envelope.getMinimum(0), 100.0);
        assertEquals( 2.00375E7, envelope.getMaximum(0), 100.0);
        assertEquals(-1.38176E7, envelope.getMinimum(1), 100.0);
        assertEquals( 1.38176E7, envelope.getMaximum(1), 100.0);
        /*
         * Tests the geographic envelope, which must be geographic.
         */
        final GeographicBoundingBox bbox = entry.getGeographicBoundingBox();
        assertEquals(-180, bbox.getWestBoundLongitude(), 0.0);
        assertEquals(+180, bbox.getEastBoundLongitude(), 0.0);
        assertEquals( -77, bbox.getSouthBoundLatitude(), 0.1);
        assertEquals( +77, bbox.getNorthBoundLatitude(), 0.1);
        /*
         * Tries to read the image.
         */
        final GridCoverage2D coverage = entry.getCoverage(null);
        assertEquals(1, coverage.getSampleDimensions().length);
    }

    /**
     * Tests the table for a NetCDF image with two bands.
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws ParseException   Should never happen in normal test execution.
     * @throws CatalogException Should never happen in normal test execution.
     * @throws IOException      If the test image can't be read.
     */
    @Test
    public void testTwoBands() throws CatalogException, SQLException, ParseException, IOException {
        final GridCoverageTable table = new GridCoverageTable(database);
        table.setLayer(LayerTableTest.TWO_BANDS_NAME);
        final CoverageReference entry = table.getEntry();
        assertEquals(2, entry.getSampleDimensions().length);
        final GridCoverage2D coverage = entry.getCoverage(null);
        assertEquals(2, coverage.getSampleDimensions().length);
    }

    /**
     * Tests the request for the bounding box.
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws CatalogException Should never happen in normal test execution.
     */
    @Test
    public void testBoundingBox() throws CatalogException, SQLException {
        final GridCoverageTable table = new GridCoverageTable(database);
        table.setLayer(LayerTableTest.SAMPLE_NAME);
        final GeneralEnvelope search = new GeneralEnvelope(table.getCoordinateReferenceSystem());
        search.setRange(0, -200, 200);
        search.setRange(1, -100, 100);
        search.setRange(2, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        table.setEnvelope(search);

        Envelope envelope = table.getEnvelope();
        assertEquals(-200, envelope.getMinimum(0), 0.0);
        assertEquals(+200, envelope.getMaximum(0), 0.0);
        assertEquals( -90, envelope.getMinimum(1), 0.0);
        assertEquals( +90, envelope.getMaximum(1), 0.0);

        table.trimEnvelope();
        envelope = table.getEnvelope();
        assertEquals(-180, envelope.getMinimum(0), 0.0);
        assertEquals(+180, envelope.getMaximum(0), 0.0);
        assertEquals( -90, envelope.getMinimum(1), 0.0);
        assertEquals( +90, envelope.getMaximum(1), 0.0);
    }
}
