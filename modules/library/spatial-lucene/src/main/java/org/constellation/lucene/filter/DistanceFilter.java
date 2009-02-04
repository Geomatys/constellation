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

package org.constellation.lucene.filter;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.Utilities;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/**
 *
 * @author Guilhem Legal
 */
public abstract class DistanceFilter extends SpatialFilter {

    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -8080362347861156199L;

    private final static List<String> SUPPORTED_UNITS;
    static {
        SUPPORTED_UNITS = new ArrayList<String>();
        SUPPORTED_UNITS.add("kilometers");
        SUPPORTED_UNITS.add("km");
        SUPPORTED_UNITS.add("meters");
        SUPPORTED_UNITS.add("m");
        SUPPORTED_UNITS.add("centimeters");
        SUPPORTED_UNITS.add("cm");
        SUPPORTED_UNITS.add("milimeters");
        SUPPORTED_UNITS.add("mm");
        SUPPORTED_UNITS.add("miles");
        SUPPORTED_UNITS.add("mi");
    }

    /**
     * The distance used in a Dwithin or Beyond spatial filter.
     */
    private Double distance;

    /**
     * The unit of measure for the distance.
     */
    private String distanceUnit;

    /**
     * initialize the filter with the specified geometry and filterType.
     *
     * @param geometry   A geometry object, supported types are: GeneralEnvelope, GeneralDirectPosition, Line2D.
     * @param filterType A flag representing the type of spatial filter to apply restricted to Beyond and Dwithin.
     * @param distance   The distance to applies to this filter.
     * @param units      The unit of measure of the distance.
     */
    public DistanceFilter(Object geometry, String crsName, Double distance, String units) throws NoSuchAuthorityCodeException, FactoryException  {
       super(geometry, crsName);
       this.distance = distance;
       this.distanceUnit = units;

       if (!SUPPORTED_UNITS.contains(units)) {
           String msg = "Unsupported distance units. supported ones are: ";
           for (String s: SUPPORTED_UNITS) {
               msg = msg + s + ',';
           }
           msg = msg.substring(0, msg.length() - 1);
           throw new IllegalArgumentException(msg);
       }
    }

    /**
     * Return the distance units (in case of a Distance Spatial filter).
     */
    public String getDistanceUnit() {
        return this.distanceUnit;
    }

    /**
     * Return the distance (in case of a Distance Spatial filter).
     */
    public Double getDistance() {
        return this.distance;
    }

    /**
     * Return the orthodromic distance between two geometric object on the earth.
     *
     * @param geometry a geometric object.
     */
    protected double getDistance(final Object geometry) {
        if (geometry instanceof GeneralDirectPosition) {

            GeneralDirectPosition tempPoint = (GeneralDirectPosition) geometry;
            if (point != null) {
                return GeometricUtilities.getOrthodromicDistance(tempPoint.getOrdinate(0), tempPoint.getOrdinate(1),
                                                                     point.getOrdinate(0),     point.getOrdinate(1), distanceUnit);

            } else if (boundingBox != null) {
                return GeometricUtilities.BBoxToPointDistance(boundingBox, tempPoint, distanceUnit);

            } else if (line != null) {
                return GeometricUtilities.lineToPointDistance(line, tempPoint, distanceUnit);
            } else {
                return 0;
            }

        } else if (geometry instanceof GeneralEnvelope) {

            GeneralEnvelope tempBox = (GeneralEnvelope) geometry;
            if (point != null) {
                return GeometricUtilities.BBoxToPointDistance(tempBox, point, distanceUnit);

            } else if (line != null) {
                return GeometricUtilities.lineToBBoxDistance(line, tempBox, distanceUnit);

            } else if (boundingBox != null) {
                return GeometricUtilities.BBoxToBBoxDistance(tempBox, boundingBox, distanceUnit);

            } else {
                return 0;
            }

        } else if (geometry instanceof Line2D) {

            Line2D tempLine = (Line2D) geometry;
            if (point != null) {
                return GeometricUtilities.lineToPointDistance(tempLine, point, distanceUnit);

            } else if (line != null) {
                return GeometricUtilities.lineTolineDistance(tempLine, line, distanceUnit);

            } else if (boundingBox != null) {
                return GeometricUtilities.lineToBBoxDistance(tempLine, boundingBox, distanceUnit);

            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof DistanceFilter && super.equals(object)) {
            final DistanceFilter that = (DistanceFilter) object;

            return Utilities.equals(this.distance,        that.distance)  &&
                   Utilities.equals(this.distanceUnit,    that.distanceUnit);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 23 * hash + (this.distance != null ? this.distance.hashCode() : 0);
        hash = 23 * hash + (this.distanceUnit != null ? this.distanceUnit.hashCode() : 0);
        return hash;
    }

    /**
     * Return a String description of the filter
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString()).append('\n');
        if (distance != null)
            s.append("Distance: ").append(distance).append(" ").append(distanceUnit).append('\n');
        return s.toString();
    }
}
