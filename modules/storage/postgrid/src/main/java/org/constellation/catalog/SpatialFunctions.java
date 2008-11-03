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
package org.constellation.catalog;

import java.util.Arrays;
import org.opengis.geometry.Envelope;
import org.geotools.geometry.GeneralEnvelope;


/**
 * Utility methods for spatial database.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class SpatialFunctions {
    /**
     * The maximal dimension allowed by this simple parser.
     */
    private static final int MAX_DIMENSION = 8;

    /**
     * Any of those characters can appear as point separator in our lousy WKT parser.
     */
    private static final String POINT_SEPARATORS = ",()";

    /**
     * Enumeration of the 4 corners in an envelope, with repetition of the first point.
     * The values are (x,y) pairs with {@code false} meaning "minimal value" and {@code true}
     * meaning "maximal value".
     */
    private static final boolean[] CORNERS = {
        false, false,
        false, true,
        true,  true,
        true,  false,
        false, false
    };

    /**
     * Do not allow instantiation of this class.
     */
    private SpatialFunctions() {
    }

    /**
     * Returns the dimension of the specified envelope, ignoring the dimensions
     * with infinite values.
     */
    private static int getDimension(final Envelope envelope) {
        int dimension = envelope.getDimension();
        while (dimension != 0) {
            final double length = envelope.getLength(dimension - 1);
            if (!Double.isNaN(length) && !Double.isInfinite(length)) {
                break;
            }
            dimension--;
        }
        return dimension;
    }

    /**
     * Formats a {@code BOX2D} or {@code BOX3D} element from an envelope.
     *
     * @param  envelope The envelope to format.
     * @return The envelope as a {@code BOX2D} or {@code BOX3D} in WKT format.
     */
    public static String formatBox(final Envelope envelope) {
        final int dimension = getDimension(envelope);
        final StringBuilder buffer = new StringBuilder("BOX").append(dimension).append('D');
        char separator = '(';
        for (int i=0; i<dimension; i++) {
            buffer.append(separator).append(envelope.getMinimum(i));
            separator = ' ';
        }
        separator = ',';
        for (int i=0; i<dimension; i++) {
            buffer.append(separator).append(envelope.getMaximum(i));
            separator = ' ';
        }
        return buffer.append(')').toString();
    }

    /**
     * Formats a {@code POLYGON} element from an envelope.
     *
     * @param  envelope The envelope to format.
     * @return The envelope as a {@code POLYGON} in WKT format.
     */
    public static String formatPolygon(final Envelope envelope) {
        final int dimension = getDimension(envelope);
        final StringBuilder buffer = new StringBuilder("POLYGON(");
        char separator = '(';
        for (int corner=0; corner<CORNERS.length; corner+=2) {
            for (int i=0; i<dimension; i++) {
                final double value;
                switch (i) {
                    case  0: // Fall through
                    case  1: value = CORNERS[corner+i] ? envelope.getMaximum(i) : envelope.getMinimum(i); break;
                    default: value = envelope.getCenter(i); break;
                }
                buffer.append(separator).append(value);
                separator = ' ';
            }
            separator = ',';
        }
        return buffer.append("))").toString();
    }

    /**
     * Parses a {@code BOX} or {@code POLYGON} element as an envelope.
     *
     * @param  bbox The {@code BOX} or {@code POLYGON} element to parse.
     * @return The parsed envelope.
     * @throws NumberFormatException if a number can not be parsed.
     */
    public static GeneralEnvelope parse(final String bbox) throws NumberFormatException {
        final int       length = bbox.length();
        final double[] minimum = new double[MAX_DIMENSION];
        final double[] maximum = new double[MAX_DIMENSION];
        Arrays.fill(minimum, Double.POSITIVE_INFINITY);
        Arrays.fill(maximum, Double.NEGATIVE_INFINITY);
        int dimension = 0;
scan:   for (int i=0; i<length; i++) {
            char c = bbox.charAt(i);
            if (Character.isJavaIdentifierStart(c)) {
                do if (++i >= length) break scan;
                while (Character.isJavaIdentifierPart(c = bbox.charAt(i)));
            }
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (POINT_SEPARATORS.indexOf(c) >= 0) {
                dimension = 0;
                continue;
            }
            final int start = i;
            boolean flush = false;
            while (++i < length) {
                c = bbox.charAt(i);
                if (Character.isWhitespace(c)) {
                    break;
                }
                if (POINT_SEPARATORS.indexOf(c) >= 0) {
                    flush = true;
                    break;
                }
            }
            final double value = Double.parseDouble(bbox.substring(start, i));
            if (value < minimum[dimension]) minimum[dimension] = value;
            if (value > maximum[dimension]) maximum[dimension] = value;
            if (flush) {
                dimension = 0;
            } else {
                dimension++;
            }
        }
        for (dimension=0; dimension<minimum.length; dimension++) {
            if (!(minimum[dimension] <= maximum[dimension])) {
                break;
            }
        }
        final GeneralEnvelope envelope = new GeneralEnvelope(dimension);
        for (int i=0; i<dimension; i++) {
            envelope.setRange(i, minimum[i], maximum[i]);
        }
        return envelope;
    }
}
