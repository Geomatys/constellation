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
import org.geotoolkit.gml.xml.v311modified.AbstractFeatureEntry;
import org.geotoolkit.gml.xml.v311modified.BoundingShapeEntry;
import org.geotoolkit.gml.xml.v311modified.DirectPositionType;
import org.geotoolkit.gml.xml.v311modified.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311modified.TimePositionType;
import org.geotoolkit.observation.xml.v100.ObservationCollectionEntry;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.sml.xml.AbstractClassification;
import org.geotoolkit.sml.xml.AbstractClassifier;
import org.geotoolkit.sml.xml.AbstractDerivableComponent;
import org.geotoolkit.sml.xml.AbstractIdentification;
import org.geotoolkit.sml.xml.AbstractIdentifier;
import org.geotoolkit.sml.xml.AbstractProcess;
import org.geotoolkit.sml.xml.AbstractSensorML;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class Utils {

    /**
     * use for debugging purpose
     */
    private static Logger logger = Logger.getLogger("org.constellation.sos");

    /**
     * Return the physical ID of a sensor.
     * This ID is found into a "Identifier" mark with the name 'supervisorCode'
     *
     * @param sensor
     * @return
     */
    public static String getPhysicalID(final AbstractSensorML sensor) {
        if (sensor != null && sensor.getMember().size() > 0) {
            AbstractProcess process = sensor.getMember().get(0).getRealProcess();
            List<? extends AbstractIdentification> idents = process.getIdentification();

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
        List<String> results = new ArrayList<String>();
        if (sensor.getMember().size() == 1) {
            if (sensor.getMember().get(0) instanceof AbstractProcess) {
                AbstractProcess component = (AbstractProcess) sensor.getMember().get(0);
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
            if (sensor.getMember().get(0) instanceof AbstractDerivableComponent) {
                AbstractDerivableComponent component = (AbstractDerivableComponent) sensor.getMember().get(0);
                if (component.getSMLLocation() != null && component.getSMLLocation().getPoint() != null &&
                    component.getSMLLocation().getPoint() != null && component.getSMLLocation().getPoint().getPos() != null)
                return component.getSMLLocation().getPoint().getPos();
            }
        }
        logger.severe("there is no piezo location");
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

            //we delete the data after the second
            if (value.indexOf('.') != -1) {
                value = value.substring(0, value.indexOf('.'));
            }
             try {
                 //here t is not used but it allow to verify the syntax of the timestamp
                 Timestamp t = Timestamp.valueOf(value);
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
     * Return an envelope containing all the Observation member of the collection.
     *
     * @param collection
     * @return
     */
    public static EnvelopeEntry getCollectionBound(final ObservationCollectionEntry collection) {
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxx = (-Double.MAX_VALUE);
        double maxy = (-Double.MAX_VALUE);

        for (ObservationEntry observation: collection.getMember()) {
            if (observation.getFeatureOfInterest() instanceof AbstractFeatureEntry) {
                AbstractFeatureEntry feature = (AbstractFeatureEntry) observation.getFeatureOfInterest();
                if (feature.getBoundedBy() != null) {
                    BoundingShapeEntry bound = feature.getBoundedBy();
                    if (bound.getEnvelope() != null) {
                        if (bound.getEnvelope().getLowerCorner() != null
                            && bound.getEnvelope().getLowerCorner().getValue() != null
                            && bound.getEnvelope().getLowerCorner().getValue().size() == 2 ) {
                            List<Double> lower = bound.getEnvelope().getLowerCorner().getValue();
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
                            List<Double> Upper = bound.getEnvelope().getUpperCorner().getValue();
                            if (Upper.get(0) > maxx) {
                                maxx = Upper.get(0);
                            }
                            if (Upper.get(1) > maxy) {
                                maxy = Upper.get(1);
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

        return new EnvelopeEntry(null, new DirectPositionType(minx, miny), new DirectPositionType(maxx, maxy), "urn:ogc:crs:espg:4326");
    }
}
