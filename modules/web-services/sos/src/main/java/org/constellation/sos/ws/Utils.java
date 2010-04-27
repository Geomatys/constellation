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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.AbstractFeatureEntry;
import org.geotoolkit.gml.xml.v311.BoundingShapeEntry;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311.FeaturePropertyType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.observation.xml.v100.ObservationCollectionEntry;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.sml.xml.AbstractClassification;
import org.geotoolkit.sml.xml.AbstractClassifier;
import org.geotoolkit.sml.xml.AbstractDerivableComponent;
import org.geotoolkit.sml.xml.AbstractIdentification;
import org.geotoolkit.sml.xml.AbstractIdentifier;
import org.geotoolkit.sml.xml.AbstractProcess;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.opengis.observation.Observation;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class Utils {

    /**
     * use for debugging purpose
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.sos");

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
    public static DirectPositionType getSensorPosition(final AbstractSensorML sensor) {
        if (sensor.getMember().size() == 1) {
            if (sensor.getMember().get(0).getRealProcess() instanceof AbstractDerivableComponent) {
                final AbstractDerivableComponent component = (AbstractDerivableComponent) sensor.getMember().get(0).getRealProcess();
                if (component.getSMLLocation() != null && component.getSMLLocation().getPoint() != null &&
                    component.getSMLLocation().getPoint() != null && component.getSMLLocation().getPoint().getPos() != null)
                return component.getSMLLocation().getPoint().getPos();
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
    public static String getTimeValue(final TimePositionType time) throws CstlServiceException {
        if (time != null && time.getValue() != null) {
            String value = time.getValue();
            value = value.replace("T", " ");

            /*we delete the data after the second
            if (value.indexOf('.') != -1) {
                value = value.substring(0, value.indexOf('.'));
            }*/
             try {
                 //here t is not used but it allow to verify the syntax of the timestamp
                 final Timestamp t = Timestamp.valueOf(value);
                 return t.toString();

             } catch(IllegalArgumentException e) {
                throw new CstlServiceException("Unable to parse the value: " + value + '\n' +
                                               "Bad format of timestamp: accepted format yyyy-mm-jjThh:mm:ss.msmsms.",
                                               INVALID_PARAMETER_VALUE, "eventTime");
             }
          } else {
            String locator;
            if (time == null)
                locator = "Timeposition";
            else
                locator = "TimePosition value";
            throw new  CstlServiceException("bad format of time, " + locator + " mustn't be null",
                                              MISSING_PARAMETER_VALUE, "eventTime");
          }
    }

    /**
     * return a SQL formatted timestamp
     *
     * @param time a GML time position object.
     */
    public static String getLuceneTimeValue(final TimePositionType time) throws CstlServiceException {
        if (time != null && time.getValue() != null) {
            String value = time.getValue();
            value = value.replace("T", " ");

            // we delete the data after the second TODO remove
            if (value.indexOf('.') != -1) {
                value = value.substring(0, value.indexOf('.'));
            }
            try {
                // verify the syntax of the timestamp
                Timestamp.valueOf(value);

            } catch(IllegalArgumentException e) {
               throw new CstlServiceException("Unable to parse the value: " + value + '\n' +
                                              "Bad format of timestamp: accepted format yyyy-mm-jjThh:mm:ss.msmsms.",
                                              INVALID_PARAMETER_VALUE, "eventTime");
            }
            value = value.replace(" ", "");
            value = value.replace("-", "");
            value = value.replace(":", "");
            return value;
          } else {
            String locator;
            if (time == null)
                locator = "Timeposition";
            else
                locator = "TimePosition value";
            throw new  CstlServiceException("bad format of time, " + locator + " mustn't be null",
                                              MISSING_PARAMETER_VALUE, "eventTime");
          }
    }

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
        luceneTimeValue = luceneTimeValue.substring(2);

        return year + '-' + month + '-' + day + ' ' + hour + ':' + min + ':' + sec;
    }

    public static String getPeriodDescription(long time) {
        long temp = time / 1000;
        final long ms   = time - (temp * 1000);
        time      = time - ms;

        // we pass in second
        time      = time / 1000;

        temp      = time / 60;
        final long sec  = time - (temp * 60);
        time      = time - sec;

        if (time == 0) return sec + "s " + ms + "ms";

        //we pass in minute
        time      = time / 60;

        temp      = time / 60;
        final long min  = time - (temp * 60);
        time      = time - min;

        return min + "min " + sec + "s " + ms + "ms";
    }

    /**
     * Return an envelope containing all the Observation member of the collection.
     *
     * @param collection
     * @return
     */
    public static EnvelopeEntry getCollectionBound(final ObservationCollectionEntry collection, String srsName) {
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxx = -Double.MAX_VALUE;
        double maxy = -Double.MAX_VALUE;

        for (Observation observation: collection.getMember()) {
            final FeaturePropertyType featureProp = ((ObservationEntry)observation).getPropertyFeatureOfInterest();

            if (featureProp != null && featureProp.getAbstractFeature() != null) {
                final AbstractFeatureEntry feature = featureProp.getAbstractFeature();
                if (feature.getBoundedBy() != null) {
                    final BoundingShapeEntry bound = feature.getBoundedBy();
                    if (bound.getEnvelope() != null) {
                        if (bound.getEnvelope().getLowerCorner() != null
                            && bound.getEnvelope().getLowerCorner().getValue() != null
                            && bound.getEnvelope().getLowerCorner().getValue().size() == 2 ) {
                            final List<Double> lower = bound.getEnvelope().getLowerCorner().getValue();
                            if (lower.get(0) < minx) {
                                minx = lower.get(0);
                            }
                            if (lower.get(1) < miny) {
                                miny = lower.get(1);
                            }
                        }
                        if (bound.getEnvelope().getUpperCorner() != null
                            && bound.getEnvelope().getUpperCorner().getValue() != null
                            && bound.getEnvelope().getUpperCorner().getValue().size() == 2 ) {
                            final List<Double> upper = bound.getEnvelope().getUpperCorner().getValue();
                            if (upper.get(0) > maxx) {
                                maxx = upper.get(0);
                            }
                            if (upper.get(1) > maxy) {
                                maxy = upper.get(1);
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

        EnvelopeEntry env =  new EnvelopeEntry(null, new DirectPositionType(minx, miny), new DirectPositionType(maxx, maxy), srsName);
        env.setSrsDimension(2);
        env.setAxisLabels("Y X");
        return env;
    }
}
