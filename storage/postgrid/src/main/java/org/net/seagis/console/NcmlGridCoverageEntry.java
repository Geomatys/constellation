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
package net.seagis.console;

import java.io.IOException;
import java.util.Date;
import javax.imageio.ImageReader;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.WritableGridCoverageEntry;
import org.geotools.util.DateRange;


/**
 * Adds to the {@link WritableGridCoverageEntry} some specifics parameters designed
 * for handling time specified in an NcML file.
 *
 * @see net.seagis.coverage.catalog.WritableGridCoverageEntry
 * @source $URL$
 * @author Cédric Briançon
 */
final class NcmlGridCoverageEntry extends WritableGridCoverageEntry {
    /**
     * The increment between to consecutives entries.
     */
    private final long increment;

    /**
     * The value to add to the origin time gotten from the netcdf metadata reading in order to have
     * the starting time for this entry.
     */
    private final long startTime;

    /**
     * The number of points.
     */
    private final int npts;

    /**
     * The starting time of the next item, if it exists.
     */
    private final long nextItemStart;

    /**
     * The format to use for the serie.
     */
    private final String format;

    /**
     *
     *
     * @param reader The Netcdf image reader to use. A call to javax.imageio.ImageReader#setInput(Object)
     *               must has been done for this reader previously.
     * @param imageIndex The index of the image for the reader.
     * @param startTime The starting time.
     * @param increment The increment step between two consecutive dates in the list.
     * @param npts The number of points.
     * @param nextItemStart The next item starting time. This will be used if we do not know the increment
     *                      step, in order to define the end time for the current element.
     * @throws IOException
     */
    public NcmlGridCoverageEntry(final ImageReader reader, final int imageIndex, final long startTime,
            final long increment, final int npts, final long nextItemStart, final String format)
            throws IOException
    {
        super(reader, imageIndex);
        this.startTime     = startTime;
        this.increment     = increment;
        this.npts          = npts;
        this.nextItemStart = nextItemStart;
        this.format        = format;
    }

    @Override
    public String getFormatName(final boolean upper) throws IOException {
        return (format == null) ? super.getFormatName(upper) : format;
    }

    @Override
    public DateRange[] getDateRanges() throws IOException, CatalogException {
        // Just to initialize time units for the metadata reader.
        // Time values will be recalculated from the ncml entries.
        super.getDateRanges();
        final long origin = getTimeOrigin().getTime();
        if (npts == 1 || increment == 0) {
            final long startTimeInMillis = convertInMillis(startTime) + origin;
            if (nextItemStart == 0L) {
                final Date startDate = new Date(startTimeInMillis);
                return new DateRange[] {new DateRange(startDate, startDate)};
            }
            final long nextItemStartInMillis = convertInMillis(nextItemStart) + origin;
            final long halfRange = Math.abs((nextItemStartInMillis - startTimeInMillis) / 2);
            final Date newStartTime = new Date(startTimeInMillis - halfRange);
            final Date newEndTime = new Date(startTimeInMillis + halfRange);
            return new DateRange[] {new DateRange(newStartTime, newEndTime)};
        }
        final DateRange[] ncmlDates = new DateRange[npts];
        final long incrementInMillis = convertInMillis(increment);
        for (int i = 0; i < ncmlDates.length; i++) {
            final long startTimeInMillis = (i == 0) ? 
                convertInMillis(startTime) + origin : ncmlDates[i-1].getMaxValue().getTime();
            final Date newStartTime = new Date(startTimeInMillis);
            final Date newEndTime = new Date(startTimeInMillis + incrementInMillis);
            ncmlDates[i] = new DateRange(newStartTime, newEndTime);
        }
        return ncmlDates;
    }

    /**
     * Converts a time given in a unit defined in the Netcdf file into a time in milliseconds,
     * in order to use it in a {@code Date} object.
     *
     * @param time The Java time value in {@code Long} for a {@code Date} to convert in
     *             milliseconds, according to the unit chosen in the metadata of the
     *             NetCDF file.
     * @return A time value expressed in milliseconds, depending on the unit chosen.
     */
    private long convertInMillis(final long time) {
        final String unitName = getTimeUnit().toString();
        if (unitName.equalsIgnoreCase("day")) {
            return time * 24 * 60 * 60 * 1000L;
        } else {
            // It assumes that the units is the second, if it is not the day,
            // since {@code jsr108} only allows seconds or days as a unit.
            return time * 1000L;
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + "[format=" + format + ", startTime=" + startTime +
                ", increment=" + increment + ", npts=" + npts + "]";
    }
}
