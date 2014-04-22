/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.sos.ws;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.constellation.util.ReflectionUtilities;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.AbstractFeature;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.gml.xml.BoundingShape;
import org.geotoolkit.gml.xml.Envelope;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.sml.xml.AbstractClassification;
import org.geotoolkit.sml.xml.AbstractClassifier;
import org.geotoolkit.sml.xml.AbstractDerivableComponent;
import org.geotoolkit.sml.xml.AbstractIdentification;
import org.geotoolkit.sml.xml.AbstractIdentifier;
import org.geotoolkit.sml.xml.AbstractProcess;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sos.xml.SOSXmlFactory;
import org.geotoolkit.swe.xml.AbstractEncoding;
import org.geotoolkit.swe.xml.TextBlock;
import org.geotoolkit.temporal.object.ISODateParser;
import org.opengis.geometry.primitive.Point;
import org.opengis.observation.Observation;
import org.opengis.temporal.Period;
import org.opengis.temporal.Position;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class Utils {

    /**
     * use for debugging purpose
     */
    private static final Logger LOGGER = Logging.getLogger("org.constellation.sos");

    private Utils() {}
    
    /**
     * Return the physical ID of a sensor.
     * This ID is found into a "Identifier" mark with the name 'supervisorCode'
     *
     * @param sensor
     * @return
     */
    public static String getPhysicalID(final AbstractSensorML sensor) {
        if (sensor != null && sensor.getMember().size() > 0) {
            final AbstractProcess process = sensor.getMember().get(0).getRealProcess();
            final List<? extends AbstractIdentification> idents = process.getIdentification();

            for(AbstractIdentification ident : idents) {
                if (ident.getIdentifierList() != null) {
                    for (AbstractIdentifier identifier: ident.getIdentifierList().getIdentifier()) {
                        if ("supervisorCode".equals(identifier.getName()) && identifier.getTerm() != null) {
                            return identifier.getTerm().getValue();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Return the networks names binded to this sensor.
     *
     * * Those names are found into "Classifier" marks with the name 'network'
     * @param sensor
     * @return
     */
    @Deprecated
    public static List<String> getNetworkNames(final AbstractSensorML sensor) {
        final List<String> results = new ArrayList<String>();
        if (sensor != null && sensor.getMember().size() == 1) {
            final AbstractProcess component = sensor.getMember().get(0).getRealProcess();
            if (component != null) {
                for (AbstractClassification cl : component.getClassification()) {
                    if (cl.getClassifierList() != null) {
                        for (AbstractClassifier classifier : cl.getClassifierList().getClassifier()) {
                            if (classifier.getName().equals("network") && classifier.getTerm() != null) {
                                results.add(classifier.getTerm().getValue());
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Return the position of a sensor.
     * 
     * @param sensor
     * @return
     */
    public static AbstractGeometry getSensorPosition(final AbstractSensorML sensor) {
        if (sensor.getMember().size() == 1) {
            if (sensor.getMember().get(0).getRealProcess() instanceof AbstractDerivableComponent) {
                final AbstractDerivableComponent component = (AbstractDerivableComponent) sensor.getMember().get(0).getRealProcess();
                if (component.getSMLLocation() != null && component.getSMLLocation().getGeometry()!= null) {
                    return component.getSMLLocation().getGeometry();
                }
            }
        }
        LOGGER.severe("there is no piezo location");
        return null;
    }

    /**
     * return a SQL formatted timestamp
     *
     * @param time a GML time position object.
     */
    public static String getTimeValue(final Position time) throws CstlServiceException {
        if (time != null && time.getDateTime() != null) {
             try {
                 final String value = time.getDateTime().toString();
                 //here t is not used but it allow to verify the syntax of the timestamp
                 final ISODateParser parser = new ISODateParser();
                 final Date d = parser.parseToDate(value);
                 final Timestamp t = new Timestamp(d.getTime());
                 return t.toString();

             } catch(IllegalArgumentException e) {
                throw new CstlServiceException("Unable to parse the value: " + time.toString() + '\n' +
                                               "Bad format of timestamp:\n" + e.getMessage(),
                                               INVALID_PARAMETER_VALUE, "eventTime");
             } 
          } else {
            String locator;
            if (time == null) {
                locator = "Timeposition";
            } else {
                locator = "TimePosition value";
            }
            throw new  CstlServiceException("bad format of time, " + locator + " mustn't be null",
                                              MISSING_PARAMETER_VALUE, "eventTime");
          }
    }
    
    public static Timestamp getTimestampValue(final Position time) throws CstlServiceException {
        return Timestamp.valueOf(getTimeValue(time));
    }

    /**
     * return a SQL formatted timestamp
     *
     * @param time a GML time position object.
     * @throws org.constellation.ws.CstlServiceException
     */
    public static String getLuceneTimeValue(final Position time) throws CstlServiceException {
        if (time != null && time.getDateTime() != null) {
            String value = time.getDateTime().toString();

            // we delete the data after the second TODO remove
            if (value.indexOf('.') != -1) {
                value = value.substring(0, value.indexOf('.'));
            }
            try {
                // verify the syntax of the timestamp
                //here t is not used but it allow to verify the syntax of the timestamp
                 final ISODateParser parser = new ISODateParser();
                 final Date d = parser.parseToDate(value);

            } catch(IllegalArgumentException e) {
               throw new CstlServiceException("Unable to parse the value: " + value + '\n' +
                                              "Bad format of timestamp:\n" + e.getMessage(),
                                              INVALID_PARAMETER_VALUE, "eventTime");
            }
            value = value.replace(" ", "");
            value = value.replace("-", "");
            value = value.replace(":", "");
            value = value.replace("T", "");
            return value;
          } else {
            String locator;
            if (time == null) {
                locator = "Timeposition";
            } else {
                locator = "TimePosition value";
            }
            throw new  CstlServiceException("bad format of time, " + locator + " mustn't be null",
                                              MISSING_PARAMETER_VALUE, "eventTime");
          }
    }

    /**
     * Transform a Lucene Date syntax string into a yyyy-MM-dd hh:mm:ss Date format String.
     *
     * @param luceneTimeValue A String on Lucene date format
     * @return A String on yyy-MM-dd hh:mm:ss Date format
     */
    public static String unLuceneTimeValue(String luceneTimeValue) {
        final String year     = luceneTimeValue.substring(0, 4);
        luceneTimeValue = luceneTimeValue.substring(4);
        final String month    = luceneTimeValue.substring(0, 2);
        luceneTimeValue = luceneTimeValue.substring(2);
        final String day      = luceneTimeValue.substring(0, 2);
        luceneTimeValue = luceneTimeValue.substring(2);
        final String hour     = luceneTimeValue.substring(0, 2);
        luceneTimeValue = luceneTimeValue.substring(2);
        final String min      = luceneTimeValue.substring(0, 2);
        luceneTimeValue = luceneTimeValue.substring(2);
        final String sec      = luceneTimeValue.substring(0, 2);

        return year + '-' + month + '-' + day + ' ' + hour + ':' + min + ':' + sec;
    }

    /**
     * Return an envelope containing all the Observation member of the collection.
     *
     * @param observations
     * @return
     */
    public static Envelope getCollectionBound(final String version, final List<Observation> observations, final String srsName) {
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxx = -Double.MAX_VALUE;
        double maxy = -Double.MAX_VALUE;

        for (Observation observation: observations) {
            final AbstractFeature feature = (AbstractFeature) observation.getFeatureOfInterest();
            if (feature != null) {
                if (feature.getBoundedBy() != null) {
                    final BoundingShape bound = feature.getBoundedBy();
                    if (bound.getEnvelope() != null) {
                        if (bound.getEnvelope().getLowerCorner() != null
                            && bound.getEnvelope().getLowerCorner().getCoordinate() != null
                            && bound.getEnvelope().getLowerCorner().getCoordinate().length == 2 ) {
                            final double[] lower = bound.getEnvelope().getLowerCorner().getCoordinate();
                            if (lower[0] < minx) {
                                minx = lower[0];
                            }
                            if (lower[1] < miny) {
                                miny = lower[1];
                            }
                        }
                        if (bound.getEnvelope().getUpperCorner() != null
                            && bound.getEnvelope().getUpperCorner().getCoordinate() != null
                            && bound.getEnvelope().getUpperCorner().getCoordinate().length == 2 ) {
                            final double[] upper = bound.getEnvelope().getUpperCorner().getCoordinate();
                            if (upper[0] > maxx) {
                                maxx = upper[0];
                            }
                            if (upper[1] > maxy) {
                                maxy = upper[1];
                            }
                        }
                    }
                }
            }
        }

        if (minx == Double.MAX_VALUE) {
            minx = -180.0;
        }
        if (miny == Double.MAX_VALUE) {
            miny = -90.0;
        }
        if (maxx == (-Double.MAX_VALUE)) {
            maxx = 180.0;
        }
        if (maxy == (-Double.MAX_VALUE)) {
            maxy = 90.0;
        }

        final Envelope env = SOSXmlFactory.buildEnvelope(version, null, minx, miny, maxx, maxy, srsName);
        env.setSrsDimension(2);
        env.setAxisLabels(Arrays.asList("Y X"));
        return env;
    }

    /**
     * Used for CSV encoding, while iterating on a resultSet.
     * 
     * if the round on the current date is over, and some field data are not present,
     * we have to add empty token before to start the next date round.
     *
     * example : we are iterating on some date with temperature an salinity
     *
     * date       |  phenomenon | value
     * 2010-01-01    TEMP          1
     * 2010-01-01    SAL           202
     * 2010-01-02    TEMP          3
     * 2010-01-02    SAL           201
     * 2010-01-03    TEMP          4
     * 2010-01-04    TEMP          2
     * 2010-01-04    SAL           210
     *
     * CSV encoding will be : @@2010-01-01,1,202@@2010-01-02,3,201@@2010-01-03,4,@@2010-01-04,2,210
     *
     * @param value the datablock builder.
     * @param currentIndex the current object index.
     */
    public static void fillEndingDataHoles(final Appendable value, int currentIndex, final List<String> fieldList, final TextBlock encoding, final int nbBlockByHole) throws IOException {
        while (currentIndex < fieldList.size()) {
            if (value != null) {
                for (int i = 0; i < nbBlockByHole; i++) {
                    value.append(encoding.getTokenSeparator());
                }
            }
            currentIndex++;
        }
    }

    /**
     * Used for CSV encoding, while iterating on a resultSet.
     * 
     * if some field data are not present in the middle of a date round,
     * we have to add empty token until we got the next phenomenon data.
     *
     * @param value the datablock builder.
     * @param phenomenonIndex the current phenomenon index.
     * @param phenomenonName the name of the current phenomenon.
     *
     * @return the updated phenomenon index.
     */
    public static int fillDataHoles(final Appendable value, int currentIndex, final String searchedField, final List<String> fieldList, final TextBlock encoding, final int nbBlockByHole) throws IOException {
        while (currentIndex < fieldList.size() && !fieldList.get(currentIndex).equals(searchedField)) {
            if (value != null) {
                for (int i = 0; i < nbBlockByHole; i++) {
                    value.append(encoding.getTokenSeparator());
                }
            }
            currentIndex++;
        }
        return currentIndex;
    }
    
    public static String getIDFromObject(final Object obj) {
        if (obj != null) {
            final Method idGetter = ReflectionUtilities.getGetterFromName("id", obj.getClass());
            if (idGetter != null) {
                return (String) ReflectionUtilities.invokeMethod(obj, idGetter);
            }
        }
        return null;
    }
    
    public static Period extractTimeBounds(final String version, final String brutValues, final AbstractEncoding abstractEncoding) {
        final String[] result = new String[2];
        if (abstractEncoding instanceof TextBlock) {
            final TextBlock encoding        = (TextBlock) abstractEncoding;
            final StringTokenizer tokenizer = new StringTokenizer(brutValues, encoding.getBlockSeparator());
            boolean first = true;
            while (tokenizer.hasMoreTokens()) {
                final String block = tokenizer.nextToken();
                final int tokenEnd = block.indexOf(encoding.getTokenSeparator());
                String samplingTimeValue;
                if (tokenEnd != -1) {
                    samplingTimeValue = block.substring(0, block.indexOf(encoding.getTokenSeparator()));
                // only one field
                } else {
                    samplingTimeValue = block;
                }
                if (first) {
                    result[0] = samplingTimeValue;
                    first = false;
                } else if (!tokenizer.hasMoreTokens()) {
                    result[1] = samplingTimeValue;
                }
            }
        } else {
            LOGGER.warning("unable to parse datablock unknown encoding");
        }
        return SOSXmlFactory.buildTimePeriod(version, null, result[0], result[1]);
    }
    
    /**
     * Return true if the samplingPoint entry is strictly inside the specified envelope.
     *
     * @param sp A sampling point (2D) station.
     * @param e An envelope (2D).
     * @return True if the sampling point is strictly inside the specified envelope.
     */
    public static boolean samplingPointMatchEnvelope(final Point sp, final Envelope e) {
        if (sp.getDirectPosition() != null) {

            final double stationX = sp.getDirectPosition().getOrdinate(0);
            final double stationY = sp.getDirectPosition().getOrdinate(1);
            final double minx     = e.getLowerCorner().getOrdinate(0);
            final double maxx     = e.getUpperCorner().getOrdinate(0);
            final double miny     = e.getLowerCorner().getOrdinate(1);
            final double maxy     = e.getUpperCorner().getOrdinate(1);

            // we look if the station if contained in the BBOX
            return stationX < maxx && stationX > minx && stationY < maxy && stationY > miny;
        }
        LOGGER.log(Level.WARNING, " the feature of interest does not have proper position");
        return false;
    }
    
    public static boolean BoundMatchEnvelope(final AbstractFeature sc, final Envelope e) {
         if (sc.getBoundedBy() != null && 
            sc.getBoundedBy().getEnvelope() != null &&
            sc.getBoundedBy().getEnvelope().getLowerCorner() != null && 
            sc.getBoundedBy().getEnvelope().getUpperCorner() != null &&
            sc.getBoundedBy().getEnvelope().getLowerCorner().getCoordinate().length > 1 && 
            sc.getBoundedBy().getEnvelope().getUpperCorner().getCoordinate().length > 1) {

            final double stationMinX  = sc.getBoundedBy().getEnvelope().getLowerCorner().getOrdinate(0);
            final double stationMaxX  = sc.getBoundedBy().getEnvelope().getUpperCorner().getOrdinate(0);
            final double stationMinY  = sc.getBoundedBy().getEnvelope().getLowerCorner().getOrdinate(1);
            final double stationMaxY  = sc.getBoundedBy().getEnvelope().getUpperCorner().getOrdinate(1);
            final double minx         = e.getLowerCorner().getOrdinate(0);
            final double maxx         = e.getUpperCorner().getOrdinate(0);
            final double miny         = e.getLowerCorner().getOrdinate(1);
            final double maxy         = e.getUpperCorner().getOrdinate(1);

            // we look if the station if contained in the BBOX
            if (stationMaxX < maxx && stationMinX > minx &&
                stationMaxY < maxy && stationMinY > miny) {
                return true;
            } else {
                LOGGER.log(Level.FINER, " the feature of interest {0} is not in the BBOX", sc.getId());
            }
        } else {
            LOGGER.log(Level.WARNING, " the feature of interest (samplingCurve){0} does not have proper bounds", sc.getId());
        }
        return false;
    }
}
