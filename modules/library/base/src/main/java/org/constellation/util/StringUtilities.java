/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.util;

import java.awt.Color;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.referencing.crs.DefaultVerticalCRS;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;


/**
 * Utilities methods that provide conversion functionnalities between real objects and queries
 * (in both ways).
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class StringUtilities {

    /**
     * Returns a string representation of the {@code Bounding Box}. It is a comma-separated
     * list matching with this pattern: minx,miny,maxx,maxy.
     *
     * @param envelope The envelope to return the string representation.
     */
    public static String toBboxValue(final Envelope envelope) {
        final StringBuilder builder = new StringBuilder();
        final int dimEnv = envelope.getDimension();
        for (int i=0; i<dimEnv; i++) {
            builder.append(envelope.getMinimum(i)).append(',');
        }
        for (int j=0; j<dimEnv; j++) {
            if (j>0) {
                builder.append(',');
            }
            builder.append(envelope.getMaximum(j));
        }
        return builder.toString();
    }

    public static boolean toBoolean(final String strTransparent) {
        if (strTransparent == null) {
            return false;
        }
        return Boolean.parseBoolean(strTransparent.trim());
    }

    public static Color toColor(String background) throws NumberFormatException{
        Color color = null;
        if (background != null) {
            background = background.trim();
            color = Color.decode(background);
        }
        return color;
    }

    /**
     * Returns the values of the list separated by commas.
     *
     * @param list The list to extract values.
     */
    public static String toCommaSeparatedValues(final List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        final int listSize = list.size();
        final StringBuilder builder = new StringBuilder();
        for (int i=0; i<listSize; i++) {
            if (i>0) {
                builder.append(',');
            }
            builder.append(list.get(i));
        }
        return builder.toString();
    }

    /**
     * Returns the CRS code for the specified envelope, or {@code null} if not found.
     *
     * @param envelope The envelope to return the CRS code.
     */
    public static String toCrsCode(final Envelope envelope) {
        if (envelope.getCoordinateReferenceSystem().equals(DefaultGeographicCRS.WGS84)) {
            return "EPSG:4326";
        }
        final Set<ReferenceIdentifier> identifiers = envelope.getCoordinateReferenceSystem().getIdentifiers();
        if (identifiers != null && !identifiers.isEmpty()) {
            return identifiers.iterator().next().toString();
        }
        return null;
    }

    /**
     * Converts a string like "EPSG:xxxx" into a {@link CoordinateReferenceSystem}.
     *
     * @param epsg An EPSG code.
     * @return The {@link CoordinateReferenceSystem} for this code, or {@code null}
     *         if the espg parameter is {@code null}.
     * @throws FactoryException if an error occurs during the decoding of the CRS code.
     */
    public static CoordinateReferenceSystem toCRS(final String epsg) throws FactoryException {
        if (epsg == null) {
            return null;
        }
        final String epsgTrimmed = epsg.trim();
//        if (epsgTrimmed.endsWith("4326") || epsgTrimmed.endsWith("UNDEFINEDCRS")) {
//            //TODO fix this
//            //we should return the good EPSG 32662
//            return DefaultGeographicCRS.WGS84;
//        }
        return CRS.decode(epsg, true);
    }

    /**
     * Convert a string containing a date into a {@link Date}, respecting the ISO 8601 standard.
     *
     * @param strTime Date as a string.
     * @return A date parsed from a string, or {@code null} if it doesn't respect the ISO 8601.
     * @throws java.text.ParseException
     */
    public static Date toDate(final String strTime) throws ParseException {
        if (strTime == null) {
            return null;
        }
        final List<Date> dates = new ArrayList<Date>();
        TimeParser.parse(strTime, 0L, dates);
        return (dates != null && !dates.isEmpty()) ? dates.get(0) : null;
    }

    public static double toDouble(String value) throws NumberFormatException {
        if (value == null) {
            return Double.NaN;
        }
        value = value.trim();
        return Double.parseDouble(value);
    }

    /**
     * Converts a string representing the bbox coordinates into a {@link GeneralEnvelope}.
     *
     * @param bbox Coordinates of the bounding box, seperated by comas.
     * @param crs  The {@linkplain CoordinateReferenceSystem coordinate reference system} in
     *             which the envelope is expressed. Should not be {@code null}.
     * @return The enveloppe for the bounding box specified, or an
     *         {@linkplain GeneralEnvelope#setToInfinite infinite envelope}
     *         if the bbox is {@code null}.
     */
    public static Envelope toEnvelope(final String bbox, final CoordinateReferenceSystem crs)
                                                              throws IllegalArgumentException
    {
        GeneralEnvelope envelope = new GeneralEnvelope(2);
        envelope.setCoordinateReferenceSystem(crs);
        envelope.setToInfinite();
        if (bbox == null) {
            if (envelope != null) {
                envelope.setToInfinite();
            }
            return envelope;
        }
            final StringTokenizer tokens = new StringTokenizer(bbox, ",;");
        if (envelope == null) {
            envelope = new GeneralEnvelope((tokens.countTokens() + 1) >> 1);
            envelope.setCoordinateReferenceSystem(crs);
            envelope.setToInfinite();
        }
        final double[] coordinates = new double[envelope.getDimension() * 2];
            int index = 0;
            while (tokens.hasMoreTokens()) {
            final double value = toDouble(tokens.nextToken());
            if (index >= coordinates.length) {
                    throw new IllegalArgumentException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$3));
                }
            coordinates[index++] = value;
            }
        if ((index & 1) != 0) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ODD_ARRAY_LENGTH_$1));
            }
        // Fallthrough in every cases.
        switch (index) {
            default: {
                while (index >= 6) {
                    final double maximum = coordinates[--index];
                    final double minimum = coordinates[--index];
                    envelope.setRange(index >> 1, minimum, maximum);
        }
            }
            case 4: envelope.setRange(1, coordinates[1], coordinates[3]);
            case 3:
            case 2: envelope.setRange(0, coordinates[0], coordinates[2]);
            case 1:
            case 0: break;
        }
        /*
         * Checks the envelope validity. Given that the parameter order in the bounding box
         * is a little-bit counter-intuitive, it is worth to perform this check in order to
         * avoid a NonInvertibleTransformException at some later stage.
         */
        final int dimension = envelope.getDimension();
        for (index=0; index<dimension; index++) {
            final double minimum = envelope.getMinimum(index);
            final double maximum = envelope.getMaximum(index);
            if (!(minimum < maximum)) {
                throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_RANGE_$2));
            }
        }
        return envelope;
    }

    /**
     * Converts a string representing the bbox coordinates into a {@link GeneralEnvelope}.
     *
     * @param bbox Coordinates of the bounding box, seperated by comas.
     * @param crs  The {@linkplain CoordinateReferenceSystem coordinate reference system} in
     *             which the envelope is expressed. Should not be {@code null}.
     * @return The enveloppe for the bounding box specified, or an
     *         {@linkplain GeneralEnvelope#setToInfinite infinite envelope}
     *         if the bbox is {@code null}.
     */
    public static Envelope toEnvelope(final String bbox, final CoordinateReferenceSystem crs,
                    final String strElevation, final String strTime)
                    throws IllegalArgumentException, ParseException {

        final CoordinateReferenceSystem horizontalCRS = CRS.getHorizontalCRS(crs);
        final VerticalCRS               verticalCRS;
        final TemporalCRS               temporalCRS;
        final double[] dimX = new double[]{Double.NaN,Double.NaN};
        final double[] dimY = new double[]{Double.NaN,Double.NaN};
        final double[] dimZ = new double[]{Double.NaN,Double.NaN};
        final double[] dimT = new double[]{Double.NaN,Double.NaN};


        //parse bbox -----------------------------------------------------------
        if (bbox == null) {
            //set to infinity
            dimX[0] = dimY[0] = Double.NEGATIVE_INFINITY;
            dimX[1] = dimY[1] = Double.POSITIVE_INFINITY;
        } else {
            final StringTokenizer tokens = new StringTokenizer(bbox, ",;");
            final double[] values = new double[4];
            int index = 0;
            while (tokens.hasMoreTokens()) {
                    values[index] = toDouble(tokens.nextToken());
                if (index >= 4) {
                    throw new IllegalArgumentException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$3));
                }
                index++;
            }

            if(index != 5){
                throw new IllegalArgumentException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$3));
            }

            dimX[0] = values[0];
            dimX[1] = values[2];
            dimY[0] = values[1];
            dimY[1] = values[3];
        }

        //parse elevation ------------------------------------------------------
        if (strElevation != null) {
            final double elevation = toDouble(strElevation);
            dimZ[0] = dimZ[1] = elevation;

            final VerticalCRS zCRS = CRS.getVerticalCRS(crs);
            verticalCRS = (zCRS != null) ? zCRS : DefaultVerticalCRS.GEOIDAL_HEIGHT;

        } else {
            verticalCRS = null;
        }

        //parse temporal -------------------------------------------------------
        if (strTime != null) {
            final Date date = toDate(strTime);
            final TemporalCRS tCRS = CRS.getTemporalCRS(crs);
            temporalCRS = (tCRS != null) ? tCRS : DefaultTemporalCRS.MODIFIED_JULIAN;

            dimT[0] = dimT[1] = ((DefaultTemporalCRS)temporalCRS).toValue(date);

        } else {
            temporalCRS = null;
        }

        //create the 2/3/4 D BBox ----------------------------------------------
        if (verticalCRS != null && temporalCRS != null) {
            final CoordinateReferenceSystem finalCRS = new DefaultCompoundCRS("rendering bbox",
                    new CoordinateReferenceSystem[]{ horizontalCRS,
                                                     verticalCRS,
                                                     temporalCRS });
            final GeneralEnvelope envelope = new GeneralEnvelope(finalCRS);
            envelope.setRange(0, dimX[0], dimX[1]);
            envelope.setRange(1, dimY[0], dimY[1]);
            envelope.setRange(2, dimZ[0], dimZ[1]);
            envelope.setRange(3, dimT[0], dimT[1]);
            return envelope;
        } else if(verticalCRS != null) {
            final CoordinateReferenceSystem finalCRS = new DefaultCompoundCRS("rendering bbox",
                    new CoordinateReferenceSystem[]{ horizontalCRS, verticalCRS });
            final GeneralEnvelope envelope = new GeneralEnvelope(finalCRS);
            envelope.setRange(0, dimX[0], dimX[1]);
            envelope.setRange(1, dimY[0], dimY[1]);
            envelope.setRange(2, dimZ[0], dimZ[1]);
            return envelope;
        } else if(temporalCRS != null) {
            final CoordinateReferenceSystem finalCRS = new DefaultCompoundCRS("rendering bbox",
                    new CoordinateReferenceSystem[]{ horizontalCRS, temporalCRS });
            final GeneralEnvelope envelope = new GeneralEnvelope(finalCRS);
            envelope.setRange(0, dimX[0], dimX[1]);
            envelope.setRange(1, dimY[0], dimY[1]);
            envelope.setRange(2, dimT[0], dimT[1]);
            return envelope;
        } else {
            final GeneralEnvelope envelope = new GeneralEnvelope(horizontalCRS);
            envelope.setRange(0, dimX[0], dimX[1]);
            envelope.setRange(1, dimY[0], dimY[1]);
            return envelope;
        }

    }

    public static String toFormat(String format) throws IllegalArgumentException {
        if (format == null) {
            return null;
        }
        format = format.trim();
        final Set<String> formats = new HashSet<String>(Arrays.asList(ImageIO.getWriterMIMETypes()));
        formats.addAll(Arrays.asList(ImageIO.getWriterFormatNames()));
        if (!formats.contains(format)) {
            throw new IllegalArgumentException("Invalid format specified.");
        }
        return format;
    }

    public static int toInt(String value) throws NumberFormatException {
        if (value == null) {
            throw new NumberFormatException("Int value not defined.");
        }
        value = value.trim();
        return Integer.parseInt(value);
    }

    public static List<String> toStringList(String strLayers) {
        if (strLayers == null) {
            return Collections.emptyList();
        }
        strLayers = strLayers.trim();
        List<String> styles = new ArrayList<String>();
        StringTokenizer token = new StringTokenizer(strLayers,",");
        while(token.hasMoreTokens()){
            styles.add(token.nextToken());
        }
        return styles;
    }
}
