/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
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
package org.geotools.geometry;

import org.geotools.resources.Classes;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Immutable representation of an {@code Envelope}.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class DefinedEnvelope implements Envelope {
    /**
     * The coordinate reference system for this envelope.
     */
    private final CoordinateReferenceSystem crs;

    /**
     * The minimum X coordinate.
     */
    private final double minx;

    /**
     * The minimum Y coordinate.
     */
    private final double miny;

    /**
     * The maximum X coordinate.
     */
    private final double maxx;

    /**
     * The maximum Y coordinate.
     */
    private final double maxy;

    /**
     * The minimum Z coordinate. It can be {@code null} in case of a 2D request.
     */
    private final Double minz;

    /**
     * The maximum Y coordinate. It can be {@code null} in case of a 2D request.
     */
    private final Double maxz;

    /**
     * Builds an envelope with a coordinate reference system and its bounds.
     */
    public DefinedEnvelope(final CoordinateReferenceSystem crs,  final double minx,
                           final double miny, final double maxx, final double maxy)
    {
        this (crs, minx, miny, maxx, maxy, null, null);
    }

    public DefinedEnvelope(final CoordinateReferenceSystem crs,  final double minx,
                           final double miny, final double maxx, final double maxy,
                           final Double minz, final Double maxz)
    {
        this.crs = crs;
        this.minx = minx; this.miny = miny;
        this.maxx = maxx; this.maxy = maxy;
        this.minz = minz; this.maxz = maxz;
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    @Override
    public int getDimension() {
        return (minz == null) ? 2 : 3;
    }

    @Override
    public DirectPosition getLowerCorner() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DirectPosition getUpperCorner() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getMinimum(int dimension) throws IndexOutOfBoundsException {
        if (dimension == 0) {
            return minx;
        }
        if (dimension == 1) {
            return miny;
        }
        if (dimension == 2 && minz != null) {
            return minz;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public double getMaximum(int dimension) throws IndexOutOfBoundsException {
        if (dimension == 0) {
            return maxx;
        }
        if (dimension == 1) {
            return maxy;
        }
        if (dimension == 2 && maxz != null) {
            return maxz;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public double getCenter(int dimension) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getMedian(int dimension) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getLength(int dimension) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getSpan(int dimension) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns a string representation of this envelope. The default implementation returns a
     * string containing {@linkplain #getLowerCorner lower corner} coordinates first, followed
     * by {@linkplain #getUpperCorner upper corner} coordinates. Other informations like the
     * CRS or class name may or may not be presents at implementor choice.
     * <p>
     * This string is okay for occasional formatting (for example for debugging purpose). But
     * if there is a lot of envelopes to format, users will get more control by using their own
     * instance of {@link org.geotools.measure.CoordinateFormat}.
     */
    @Override
    public String toString() {
        return toString(this);
    }

    /**
     * Formats the specified envelope. The returned string will contain the
     * {@linkplain #getLowerCorner lower corner} coordinates first, followed by
     * {@linkplain #getUpperCorner upper corner} coordinates.
     */
    static String toString(final Envelope envelope) {
        final StringBuilder buffer = new StringBuilder(Classes.getShortClassName(envelope));
        final int dimension = envelope.getDimension();
        if (dimension != 0) {
            String separator = "[(";
            for (int i=0; i<dimension; i++) {
                buffer.append(separator).append(envelope.getMinimum(i));
                separator = ", ";
            }
            separator = "), (";
            for (int i=0; i<dimension; i++) {
                buffer.append(separator).append(envelope.getMaximum(i));
                separator = ", ";
            }
            buffer.append(")]");
        }
        return buffer.toString();
    }
}