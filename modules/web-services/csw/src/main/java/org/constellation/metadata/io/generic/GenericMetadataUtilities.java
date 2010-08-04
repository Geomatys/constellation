/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.metadata.io.generic;

// J2SE dependencies
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Constellation dependencies
import org.constellation.generic.Values;

// Geotoolkit dependencies
import org.geotoolkit.metadata.iso.citation.DefaultCitationDate;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.ows.xml.v100.BoundingBoxType;
import org.geotoolkit.util.SimpleInternationalString;

// GeoAPI dependencies
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.util.InternationalString;

/**
 * A set of static utility methods generally used in sub-implementations of the Generic reader.
 * It allows to create more easily geotk ISO object.
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class GenericMetadataUtilities {

    /**
     * Forbidden constructor.
     */
    private GenericMetadataUtilities() {}
    
    /**
     * A List of date Formater.
     */
    public static final List<DateFormat> DATE_FORMATS;
    static {
        DATE_FORMATS = new ArrayList<DateFormat>();
        DATE_FORMATS.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        DATE_FORMATS.add(new SimpleDateFormat("yyyy-MM-dd"));
        DATE_FORMATS.add(new SimpleDateFormat("yyyy"));
    }

    /**
     * A debugging logger
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.metadata.io.generic");

    /**
     * Avoid the IllegalArgumentException when the variable value is null.
     *
     */
    public static InternationalString getInternationalStringVariable(String value) {
        if (value != null){
            return new SimpleInternationalString(value);
        }
        return null;
    }

    /**
     * Parse the specified date and return a CitationDate with the dateType code REVISION.
     *
     * @param date The date to parse.
     *
     * @return A CitationDate of type revision.
     */
    public static CitationDate createRevisionDate(String date) {
        return createCitationDate(date, DateType.REVISION);
    }

    /**
     * Parse the specified date and return a CitationDate with the dateType code PUBLICATION.
     *
     * @param date The date to parse.
     *
     * @return A CitationDate of type revision.
     */
    public static CitationDate createPublicationDate(String date) {
        return createCitationDate(date, DateType.PUBLICATION);
    }

    /**
     * Parse the specified date and return a CitationDate with the dateType code CREATION.
     *
      * @param date The date to parse.
     *
     * @return A CitationDate of type revision.
     */
    public static CitationDate createCreationDate(String date) {
        return createCitationDate(date, DateType.CREATION);
    }

    /**
     * Parse the specified date and return a CitationDate with the specified dateType.
     *
     * @param date The date to parse.
     *
     * @return A CitationDate of type revision.
     */
    public static CitationDate createCitationDate(String date, DateType type) {
        final DefaultCitationDate ciDate = new DefaultCitationDate();
        ciDate.setDateType(type);
        final Date d = parseDate(date);
        if (d != null)
            ciDate.setDate(d);
        else LOGGER.log(Level.FINER, "citation date null: {0}", date);
        return ciDate;
    }

    /**
     * Parse a date from a String
     * @param date The date to parse.
     */
    public static Date parseDate(String date) {
        if (date == null || date.isEmpty())
            return null;
        int i = 0;
        while (i < DATE_FORMATS.size()) {
            final DateFormat dateFormat = DATE_FORMATS.get(i);
            try {
                Date d;
                synchronized (dateFormat) {
                    d = dateFormat.parse(date);
                }
                return d;
            } catch (ParseException ex) {
                i++;
            }
        }
        LOGGER.log(Level.WARNING, "unable to parse the date: [{0}]", date);
        return null;
    }


    /**
     * Extract A list of w-e-s-n coordinates from the values object,
     * and return it a many array of 4 double.
     *
     * @param westVar The name of the west coordinates variable.
     * @param eastVar The name of the east coordinates variable.
     * @param southVar The name of the south coordinates variable.
     * @param northVar The name of the north coordinates variable.
     * @param values A set of variables and their correspounding values.
     *
     * @return
     */
    private static List<Double[]> getCoordinateList(String westVar, String eastVar, String southVar, String northVar, Values values) {
        final List<Double[]> result = new ArrayList<Double[]>();

        final List<String> w = values.getVariables(westVar);
        final List<String> e = values.getVariables(eastVar);
        final List<String> s = values.getVariables(southVar);
        final List<String> n = values.getVariables(northVar);
        if (w == null || e == null || s == null || n == null) {
            LOGGER.severe("One or more extent/BBOX coordinates are null");
            return result;
        }
        if (!(w.size() == e.size() &&  e.size() == s.size() && s.size() == n.size())) {
            LOGGER.severe("There is not the same number of geographic extent/BBOX coordinates");
            return result;
        }
        final int size = w.size();
        for (int i = 0; i < size; i++) {
            double west = 0; double east = 0; double south = 0; double north = 0;
            String westValue  = null; String eastValue  = null;
            String southValue = null; String northValue = null;
            try {
                westValue = w.get(i);
                if (westValue != null) {
                    if (westValue.indexOf(',') != -1) {
                        westValue = westValue.substring(0, westValue.indexOf(','));
                    }
                    west = Double.parseDouble(westValue);
                }
                eastValue = e.get(i);
                if (eastValue != null) {
                    if (eastValue.indexOf(',') != -1) {
                        eastValue = eastValue.substring(0, eastValue.indexOf(','));
                    }
                    east = Double.parseDouble(eastValue);
                }
                southValue = s.get(i);
                if (southValue != null) {
                    if (southValue.indexOf(',') != -1) {
                        southValue = southValue.substring(0, southValue.indexOf(','));
                    }
                    south = Double.parseDouble(southValue);
                }
                northValue = n.get(i);
                if (northValue != null) {
                    north = Double.parseDouble(northValue);
                }

                // for point BBOX we replace the westValue equals to 0 by the eastValue (respectively for  north/south)
                if (east == 0) {
                    east = west;
                }
                if (north == 0) {
                    north = south;
                }
                final Double[] coordinate = new Double[4];
                coordinate[0] = west;
                coordinate[1] = east;
                coordinate[2] = south;
                coordinate[3] = north;
                result.add(coordinate);
            } catch (NumberFormatException ex) {
                LOGGER.severe("Number format exception while parsing boundingBox:\ncurrent box: " +
                        westValue + ',' + eastValue + ',' + southValue + ',' + northValue);
            }
        }
        return result;
    }

    /**
     * Extract one or more geographic extent from the values object. (ISO 19139 object)
     *
     * @param westVar The name of the west coordinates variable.
     * @param eastVar The name of the east coordinates variable.
     * @param southVar The name of the south coordinates variable.
     * @param northVar The name of the north coordinates variable.
     * @param values A set of variables and their correspounding values.
     *
     * @return A list of geographic extent.
     */
    public static List<GeographicExtent> createGeographicExtent(String westVar, String eastVar, String southVar, String northVar, Values values) {
        final List<GeographicExtent> result = new ArrayList<GeographicExtent>();
        final List<Double[]> coordinates = getCoordinateList(westVar, eastVar, southVar, northVar, values);
        
        final int size = coordinates.size();
        for (int i = 0; i < size; i++) {
            final Double[] coordinate = coordinates.get(i);
            final GeographicExtent geo = new DefaultGeographicBoundingBox(coordinate[0], coordinate[1], coordinate[2], coordinate[3]);
            result.add(geo);
        }
        return result;
    }

    /**
     * Extract one or more bounding box from the values object. (Dublin-core object)
     *
     * @param westVar The name of the west coordinates variable.
     * @param eastVar The name of the east coordinates variable.
     * @param southVar The name of the south coordinates variable.
     * @param northVar The name of the north coordinates variable.
     * @param values A set of variables and their correspounding values.
     *
     * @return A list of BoundingBox.
     */
    public static List<BoundingBoxType> createBoundingBoxes(String westVar, String eastVar, String southVar, String northVar, Values values) {
        final List<BoundingBoxType> result = new ArrayList<BoundingBoxType>();
        final List<Double[]> coordinates = getCoordinateList(westVar, eastVar, southVar, northVar, values);
        
        final int size = coordinates.size();
        for (int i = 0; i < size; i++) {
            final Double[] coordinate = coordinates.get(i);
            //TODO CRS
            final BoundingBoxType bbox = new BoundingBoxType("EPSG:4326", coordinate[0], coordinate[2], coordinate[1], coordinate[3]);
            result.add(bbox);
        }
        return result;
    }

}
