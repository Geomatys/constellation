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
 *
 * @author Guilhem Legal (Geomatys)
 */
public class GenericMetadataUtilities {

    /**
     * A date Formater.
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
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.metadata.ioc");

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
     * @param date
     * @return
     */
    public static CitationDate createRevisionDate(String date) {
        final DefaultCitationDate revisionDate = new DefaultCitationDate();
        revisionDate.setDateType(DateType.REVISION);
        final Date d = parseDate(date);
        if (d != null)
            revisionDate.setDate(d);
        else LOGGER.finer("revision date null: " + date);
        return revisionDate;
    }

    /**
     * Parse the specified date and return a CitationDate with the dateType code PUBLICATION.
     *
     * @param date
     * @return
     */
    public static CitationDate createPublicationDate(String date) {
        final DefaultCitationDate revisionDate = new DefaultCitationDate();
        revisionDate.setDateType(DateType.PUBLICATION);
        final Date d = parseDate(date);
        if (d != null)
            revisionDate.setDate(d);
        else LOGGER.finer("publication date null: " + date);
        return revisionDate;
    }

    /**
     *
     */
    public static Date parseDate(String date) {
        if (date == null)
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
        LOGGER.severe("unable to parse the date: " + date);
        return null;
    }

    /**
     *
     * @param westVar
     * @param eastVar
     * @param southVar
     * @param northVar
     * @return
     */
    public static List<GeographicExtent> createGeographicExtent(String westVar, String eastVar, String southVar, String northVar, Values values) {
        final List<GeographicExtent> result = new ArrayList<GeographicExtent>();

        final List<String> w = values.getVariables(westVar);
        final List<String> e = values.getVariables(eastVar);
        final List<String> s = values.getVariables(southVar);
        final List<String> n = values.getVariables(northVar);
        if (w == null || e == null || s == null || n == null) {
            LOGGER.severe("One or more extent coordinates are null");
            return result;
        }
        if (!(w.size() == e.size() &&  e.size() == s.size() && s.size() == n.size())) {
            LOGGER.severe("There is not the same number of geographic extent coordinates");
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
            } catch (NumberFormatException ex) {
                LOGGER.severe("Number format exception while parsing boundingBox: " + '\n' +
                        "current box: " + westValue + ',' + eastValue + ',' + southValue + ',' + northValue);
            }
            final GeographicExtent geo = new DefaultGeographicBoundingBox(west, east, south, north);
            result.add(geo);
        }
        return result;
    }

    /**
     *
     * @param westVar
     * @param eastVar
     * @param southVar
     * @param northVar
     * @return
     */
    public static List<BoundingBoxType> createBoundingBoxes(String westVar, String eastVar, String southVar, String northVar, Values values) {
        final List<BoundingBoxType> result = new ArrayList<BoundingBoxType>();

        final List<String> w = values.getVariables(westVar);
        final List<String> e = values.getVariables(eastVar);
        final List<String> s = values.getVariables(southVar);
        final List<String> n = values.getVariables(northVar);
        if (w == null || e == null || s == null || n == null) {
            LOGGER.severe("One or more BBOX coordinates are null");
            return result;
        }
        if (!(w.size() == e.size() &&  e.size() == s.size() && s.size() == n.size())) {
            LOGGER.severe("There is not the same number of geographic BBOX coordinates");
            return result;
        }
        final int size = w.size();
        for (int i = 0; i < size; i++) {
            double west = 0; double east = 0; double south = 0; double north = 0;
            try {
                if (w.get(i) != null) {
                    west = Double.parseDouble(w.get(i));
                }
                if (e.get(i) != null) {
                    east = Double.parseDouble(e.get(i));
                }
                if (s.get(i) != null) {
                    south = Double.parseDouble(s.get(i));
                }
                if (n.get(i) != null) {
                    north = Double.parseDouble(n.get(i));
                }

                // for point BBOX we replace the westValue equals to 0 by the eastValue (respectively for  north/south)
                if (east == 0) {
                    east = west;
                }
                if (north == 0) {
                    north = south;
                }
            } catch (NumberFormatException ex) {
                LOGGER.severe("Number format exception while parsing boundingBox: " + '\n' +
                        "current box: " + w.get(i) + ',' + e.get(i) + ',' + s.get(i) + ',' + n.get(i));
            }
            //TODO CRS
            final BoundingBoxType bbox = new BoundingBoxType("EPSG:4326", west, south, east, north);
            result.add(bbox);
        }
        return result;
    }

}
