/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.constellation.gml.v311.FeaturePropertyType;
import org.constellation.observation.ObservationCollectionEntry;
import org.constellation.observation.ObservationEntry;
import org.constellation.sos.v100.Capabilities;
import org.constellation.sos.v100.ObservationOfferingEntry;
import org.constellation.swe.AbstractEncodingProperty;
import org.constellation.swe.DataArray;
import org.constellation.swe.DataArrayProperty;
import org.constellation.swe.DataComponentProperty;
import org.constellation.swe.v101.CompositePhenomenonEntry;
import org.constellation.swe.v101.PhenomenonPropertyType;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class Normalizer {

    private static Logger logger = Logger.getLogger("org.constellation.sos");

    /**
     * Normalize the capabilities document by replacing the double by reference
     *
     * @param capa the unnormalized document.
     *
     * @return a normalized document
     */
    public static Capabilities normalizeDocument(Capabilities capa){
        List<PhenomenonPropertyType> alreadySee = new ArrayList<PhenomenonPropertyType>();
        if (capa.getContents() != null) {
            for (ObservationOfferingEntry off: capa.getContents().getObservationOfferingList().getObservationOffering()) {
                for (PhenomenonPropertyType pheno: off.getRealObservedProperty()) {
                    if (alreadySee.contains(pheno)) {
                        pheno.setToHref();
                    } else {
                        if (pheno.getPhenomenon() instanceof CompositePhenomenonEntry) {
                            CompositePhenomenonEntry compo = (CompositePhenomenonEntry) pheno.getPhenomenon();
                            for (PhenomenonPropertyType pheno2: compo.getRealComponent()) {
                                if (alreadySee.contains(pheno2)) {
                                    pheno2.setToHref();
                                } else {
                                    alreadySee.add(pheno2);
                                }
                            }
                        }
                        alreadySee.add(pheno);
                    }
                }
            }
        }
        return capa;
    }

    /**
     * Normalize the Observation collection document by replacing the double by reference
     *
     * @param capa the unnormalized document.
     *
     * @return a normalized document
     */
    public static ObservationCollectionEntry normalizeDocument(ObservationCollectionEntry collection){
        //first if the collection is empty
        if (collection.getMember().size() == 0) {
            return new ObservationCollectionEntry("urn:ogc:def:nil:OGC:inapplicable");
        }

        List<FeaturePropertyType>          foiAlreadySee   = new ArrayList<FeaturePropertyType> ();
        List<PhenomenonPropertyType>       phenoAlreadySee = new ArrayList<PhenomenonPropertyType>();
        List<AbstractEncodingProperty>     encAlreadySee   = new ArrayList<AbstractEncodingProperty>();
        List<DataComponentProperty>        dataAlreadySee  = new ArrayList<DataComponentProperty>();
        int index = 0;
        for (ObservationEntry observation: collection.getMember()) {
            //we do this for the feature of interest
            FeaturePropertyType foi = observation.getPropertyFeatureOfInterest();
            if (foiAlreadySee.contains(foi)){
                foi.setToHref();
            } else {
                foiAlreadySee.add(foi);
            }
            //for the phenomenon
            PhenomenonPropertyType phenomenon = observation.getPropertyObservedProperty();
            if (phenoAlreadySee.contains(phenomenon)){
                phenomenon.setToHref();
            } else {
                if (phenomenon.getPhenomenon() instanceof CompositePhenomenonEntry) {
                    CompositePhenomenonEntry compo = (CompositePhenomenonEntry) phenomenon.getPhenomenon();
                    for (PhenomenonPropertyType pheno2: compo.getRealComponent()) {
                        if (phenoAlreadySee.contains(pheno2)) {
                                    pheno2.setToHref();
                        } else {
                            phenoAlreadySee.add(pheno2);
                        }
                    }
                }
                phenoAlreadySee.add(phenomenon);
            }
            //for the result : textBlock encoding and element type
            if (observation.getResult() instanceof DataArrayProperty) {
                DataArray array = ((DataArrayProperty)observation.getResult()).getDataArray();

                //element type
                DataComponentProperty elementType = array.getPropertyElementType();
                if (dataAlreadySee.contains(elementType)){
                    elementType.setToHref();
                } else {
                    dataAlreadySee.add(elementType);
                }

                //encoding
                AbstractEncodingProperty encoding = array.getPropertyEncoding();
                if (encAlreadySee.contains(encoding)){
                    encoding.setToHref();

                } else {
                    encAlreadySee.add(encoding);
                }
            } else {
                if (observation.getResult() != null)
                    logger.severe("NormalizeDocument: Class not recognized for result:" + observation.getResult().getClass().getSimpleName());
                else
                    logger.severe("NormalizeDocument: The result is null");
            }
            index++;
        }
        return collection;
    }
}
