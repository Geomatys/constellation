/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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

package org.constellation.sos.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.sml.AbstractClassification;
import org.constellation.sml.AbstractClassifier;
import org.constellation.sml.AbstractDerivableComponent;
import org.constellation.sml.AbstractIdentification;
import org.constellation.sml.AbstractIdentifier;
import org.constellation.sml.AbstractProcess;
import org.constellation.sml.AbstractSensorML;

/**
 *
 * @author Guilhem Legal
 */
public class SensorMLUtils {

    /**
     * use for debugging purpose
     */
    private static Logger logger = Logger.getLogger("org.constellation.sos.ws");

    /**
     * Return the physical ID of a sensor.
     * This ID is found into a "Identifier" mark with the name 'supervisorCode'
     *
     * @param sensor
     * @return
     */
    public static String getPhysicalID(AbstractSensorML sensor) {
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
    public static List<String> getNetworkNames(AbstractSensorML sensor) {
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
    public static DirectPositionType getSensorPosition(AbstractSensorML sensor) {
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
}
