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
import org.geotoolkit.gml.xml.v311.FeaturePropertyType;
import org.geotoolkit.observation.xml.v100.ObservationCollectionEntry;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.swe.xml.AbstractEncodingProperty;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.geotoolkit.swe.xml.DataComponentProperty;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonEntry;
import org.geotoolkit.swe.xml.v101.PhenomenonPropertyType;
import org.opengis.observation.Observation;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class Normalizer {

    private static final Logger logger = Logger.getLogger("org.constellation.sos");

    private Normalizer() {}
    
    /**
     * Normalize the capabilities document by replacing the double by reference
     *
     * @param capa the unnormalized document.
     *
     * @return a normalized document
     */
    public static Capabilities normalizeDocument(Capabilities capa){
        final List<PhenomenonPropertyType> alreadySee = new ArrayList<PhenomenonPropertyType>();
        if (capa.getContents() != null) {
            for (ObservationOfferingEntry off: capa.getContents().getObservationOfferingList().getObservationOffering()) {
                for (PhenomenonPropertyType pheno: off.getRealObservedProperty()) {
                    if (alreadySee.contains(pheno)) {
                        pheno.setToHref();
                    } else {
                        if (pheno.getPhenomenon() instanceof CompositePhenomenonEntry) {
                            final CompositePhenomenonEntry compo = (CompositePhenomenonEntry) pheno.getPhenomenon();
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

        final List<FeaturePropertyType>      foiAlreadySee   = new ArrayList<FeaturePropertyType> ();
        final List<PhenomenonPropertyType>   phenoAlreadySee = new ArrayList<PhenomenonPropertyType>();
        final List<AbstractEncodingProperty> encAlreadySee   = new ArrayList<AbstractEncodingProperty>();
        final List<DataComponentProperty>    dataAlreadySee  = new ArrayList<DataComponentProperty>();
        for (Observation observation: collection.getMember()) {
            //we do this for the feature of interest
            final FeaturePropertyType foi = ((ObservationEntry)observation).getPropertyFeatureOfInterest();
            if (foiAlreadySee.contains(foi)){
                foi.setToHref();
            } else {
                foiAlreadySee.add(foi);
            }
            //for the phenomenon
            final PhenomenonPropertyType phenomenon = ((ObservationEntry)observation).getPropertyObservedProperty();
            if (phenoAlreadySee.contains(phenomenon)){
                phenomenon.setToHref();
            } else {
                if (phenomenon.getPhenomenon() instanceof CompositePhenomenonEntry) {
                    final CompositePhenomenonEntry compo = (CompositePhenomenonEntry) phenomenon.getPhenomenon();
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
                final DataArray array = ((DataArrayProperty)observation.getResult()).getDataArray();

                //element type
                final DataComponentProperty elementType = array.getPropertyElementType();
                if (dataAlreadySee.contains(elementType)){
                    elementType.setToHref();
                } else {
                    dataAlreadySee.add(elementType);
                }

                //encoding
                final AbstractEncodingProperty encoding = array.getPropertyEncoding();
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
        }
        return collection;
    }
}
