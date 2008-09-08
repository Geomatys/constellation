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


/**
 * Represents the {@code &lt;value>} tag for the {@code time} variable red in a
 * {@code &lt;netcdf location="" ...> tag. It contains a piece of information about the time series.
 *
 * @source $URL$
 * @author Cédric Briançon
 */
final class NcmlTimeValues {
    /**
     * The starting time of this netcdf file.
     */
    private final long startTime;

    /**
     * The increment time between two consecutive elements.
     */
    private final long increment;

    /**
     * The number of points taken.
     */
    private final int npts;

    /**
     * Represents the {@code &lt;value>} tag in a {@code NcML} file.
     *
     * @param startTime The starting time expressed in a long value since the origin defined in
     *                  the Netcdf file's header.
     * @param increment The increment step between two data red from the {@code NcML} file.
     * @param npts
     */
    public NcmlTimeValues(final long startTime, final long increment, final int npts) {
        this.startTime = startTime;
        this.increment = increment;
        this.npts = npts;
    }

    /**
     * Returns the starting time of this element.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the increment step of time between two consecutive elements defined in the
     * NcML file.
     */
    public long getIncrement() {
        return increment;
    }

    /**
     * Returns the number of points chosen in the NcML file.
     */
    public int getNpts() {
        return npts;
    }

    /**
     * This method will be used by the {@code HashSet} to verify if two entities are equals.
     * In this case, the current value designed to be inserted will not be inserted in the
     * set, since it is already present.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof NcmlTimeValues)) {
            return false;
        }
        final NcmlTimeValues timeValue = (NcmlTimeValues)object;
        if (startTime != timeValue.startTime) {
            return false;
        }
        if (increment != timeValue.increment) {
            return false;
        }
        if (npts != timeValue.npts) {
            return false;
        }
        return true;
    }

    /**
     * Used by the {@code HashSet} each time a new element is trying to be inserted, in order
     * to know if there is no other element already present in the set that has the same hashCode.
     * If it is the case, the equals(Object) method will be tested, in order to ensure that they
     * really are equals.
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (int) (this.startTime ^ (this.startTime >>> 32));
        hash = 67 * hash + (int) (this.increment ^ (this.increment >>> 32));
        hash = 67 * hash + this.npts;
        return hash;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[startTime=" + startTime + ", increment=" + increment +
                ", npts=" + npts + "]";
    }
}
