/*
 * (C) 2008, Geomatys
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.seagis.ncml;

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
public class NcmlGridCoverageEntry extends WritableGridCoverageEntry {
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
            final long increment, final int npts, final long nextItemStart) throws IOException
    {
        super(reader, imageIndex);
        this.startTime     = startTime;
        this.increment     = increment;
        this.npts          = npts;
        this.nextItemStart = nextItemStart;
    }

    @Override
    public DateRange[] getDateRanges() throws IOException, CatalogException {
        final DateRange[] originalDates = super.getDateRanges();
        final int size = originalDates.length;
        if (size == 0) {
            return null;
        }
        final long origin = getTimeOrigin().getTime();
        final long startTimeInMillis = convertInMillis(startTime);
        final long incrementInMillis = convertInMillis(increment);
        for (int i = 0; i < size; i++) {
            // If the starting time for the next has been given, then one uses it in order to
            // calculate the range between two consecutive dates.
            // Otherwise one uses the increment variable found in the NcML file.
            if (nextItemStart != 0L) {
                final long nextItemStartInMillis = convertInMillis(nextItemStart);
                // If the starting time of the next item is lower than the starting time for the current
                // item, it signifies that we have reached the end of the list.
                // In this case, because there is no other element, we take the previous element as
                // the nextOne, and we apply an absolute calculation in order to have a valid range.
                final long halfRange = Math.abs((nextItemStartInMillis - startTimeInMillis) / 2);
                final Date newStartTime = new Date(origin + startTimeInMillis - halfRange);
                final Date newEndTime = new Date(origin + startTimeInMillis + halfRange);
                originalDates[i] = new DateRange(newStartTime, newEndTime);
            } else {
                final Date newStartTime = new Date(origin + startTimeInMillis);
                final Date newEndTime = new Date(origin + startTimeInMillis + incrementInMillis * npts);
                originalDates[i] = new DateRange(newStartTime, newEndTime);
            }
        }
        return originalDates;
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
            return time * 60 * 60 * 1000L;
        }
    }
}
