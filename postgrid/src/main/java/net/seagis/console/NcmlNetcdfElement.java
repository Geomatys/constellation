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
package net.seagis.console;

import java.net.URI;


/**
 * Representation of the {@code &lt;netcdf>} tag in a NcML file having an aggregation of type
 * {@code joinExisting}.
 *
 * @source $URL$
 * @author Cédric Briançon
 */
final class NcmlNetcdfElement {
    /**
     * The location of the Netcdf file.
     */
    private final URI location;

    /**
     * The time value associated to the specified Netcdf file.
     */
    private final NcmlTimeValues timeValues;

    /**
     * Constructs an object which represents the {@code &lt;netcdf location ="...">} tag.
     *
     * @param location The URI of the NetCDF file red from the NcML file.
     * @param timeValue A representation of the {@code &lt;values>} tag that has a name of value "time".
     */
    public NcmlNetcdfElement(final URI location, final NcmlTimeValues timeValue) {
        this.location = location;
        this.timeValues = timeValue;
    }

    /**
     * Returns the URI of the NetCDF file red from the NcML file.
     */
    public URI getLocation(){
        return location;
    }

    /**
     * Returns the representation of the {@code &lt;values>} tag, for which variable is "time".
     */
    public NcmlTimeValues getTimeValues() {
        return timeValues;
    }

    /**
     * This method will be used by the {@code HashSet} to verify if two entities are equals.
     * In this case, the current value designed to be inserted will not be inserted in the
     * set, since it is already present.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof NcmlNetcdfElement)) {
            return false;
        }
        final NcmlNetcdfElement netcdfElement = (NcmlNetcdfElement)object;
        if (!netcdfElement.location.equals(location)) {
            return false;
        }
        if (!netcdfElement.timeValues.equals(timeValues)) {
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
        hash = 11 * hash + (location != null ? location.hashCode() : 0);
        hash = 11 * hash + (timeValues != null ? timeValues.hashCode() : 0);
        return hash;
    }
}
