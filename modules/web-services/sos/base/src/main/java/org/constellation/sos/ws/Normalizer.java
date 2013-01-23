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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.gml.xml.v311.FeaturePropertyType;
import org.geotoolkit.observation.xml.v100.MeasureType;
import org.geotoolkit.observation.xml.v100.MeasurementType;
import org.geotoolkit.observation.xml.v100.ObservationCollectionType;
import org.geotoolkit.observation.xml.v100.ObservationType;
import org.geotoolkit.observation.xml.v100.ProcessType;
import org.geotoolkit.sos.xml.Capabilities;
import org.geotoolkit.sos.xml.SOSXmlFactory;
import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.swe.xml.AbstractEncodingProperty;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.geotoolkit.swe.xml.DataComponentProperty;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonType;
import org.geotoolkit.swe.xml.v101.DataArrayType;
import org.geotoolkit.swe.xml.v101.DataArrayPropertyType;
import org.geotoolkit.swe.xml.v101.PhenomenonPropertyType;
import org.geotoolkit.util.logging.Logging;
import org.opengis.observation.Observation;
import org.opengis.observation.ObservationCollection;

/**
 * Static methods use to create valid XML file, by setting object into referenceMode.
 * The goal is to avoid to declare the same block many times in a XML file.
 * 
 * @author Guilhem Legal (Geomatys)
 */
public final class Normalizer {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.sos");

    private Normalizer() {}
    
    public static Capabilities normalizeDocument(final Capabilities capa){
        if (capa instanceof org.geotoolkit.sos.xml.v100.Capabilities) {
            return normalizeDocumentv100((org.geotoolkit.sos.xml.v100.Capabilities)capa);
        } else {
            return capa; // no necessary in SOS 2
        }
    }
    
    /**
     * Normalize the capabilities document by replacing the double by reference
     *
     * @param capa the unnormalized document.
     *
     * @return a normalized document
     */
    private static Capabilities normalizeDocumentv100(final org.geotoolkit.sos.xml.v100.Capabilities capa){
        final List<PhenomenonPropertyType> alreadySee = new ArrayList<PhenomenonPropertyType>();
        if (capa.getContents() != null) {
            for (ObservationOfferingType off: capa.getContents().getObservationOfferingList().getObservationOffering()) {
                for (PhenomenonPropertyType pheno: off.getRealObservedProperty()) {
                    if (alreadySee.contains(pheno)) {
                        pheno.setToHref();
                    } else {
                        if (pheno.getPhenomenon() instanceof CompositePhenomenonType) {
                            final CompositePhenomenonType compo = (CompositePhenomenonType) pheno.getPhenomenon();
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
     * Regroup the different Observation by sensor.
     *
     * @param collection
     *
     * @return a collection
     */
    public static ObservationCollection regroupObservation(final String version, final Envelope bounds, final ObservationCollection collection){
        final List<Observation> members = collection.getMember();
        final Map<String, ObservationType> merged = new HashMap<String, ObservationType>();
        for (Observation obs : members) {
            final ProcessType process = (ProcessType) obs.getProcedure();
            if (merged.containsKey(process.getHref())) {
                final ObservationType uniqueObs         = merged.get(process.getHref());
                if (uniqueObs.getResult() instanceof DataArrayPropertyType) {
                    final DataArrayPropertyType mergedArrayP = (DataArrayPropertyType) uniqueObs.getResult();
                    final DataArrayType mergedArray         = mergedArrayP.getDataArray();

                    if (obs.getResult() instanceof DataArrayPropertyType) {
                        final DataArrayPropertyType arrayP = (DataArrayPropertyType) obs.getResult();
                        final DataArrayType array         = arrayP.getDataArray();

                        //we merge this observation with the map one
                        mergedArray.setElementCount(mergedArray.getElementCount().getCount().getValue() + array.getElementCount().getCount().getValue());
                        mergedArray.setValues(mergedArray.getValues() + array.getValues());
                    } 
                }
            } else {
                final ObservationType clone;
                if (obs instanceof MeasurementType) {
                    clone = (MeasurementType) obs;
                } else {
                    clone = new ObservationType((ObservationType) obs);
                }
                merged.put(process.getHref(), clone);
            }
        }

        final List<Observation> obervations = new ArrayList<Observation>();
        for (ObservationType entry: merged.values()) {
            obervations.add(entry);
        }
        return SOSXmlFactory.buildObservationCollection(version, "collection-1", bounds, obervations);
    }

    /**
     * Normalize the Observation collection document by replacing the double by reference
     *
     * @param collection the unnormalized document.
     *
     * @return a normalized document
     */
    public static ObservationCollection normalizeDocument(final ObservationCollection collection) {
        //first if the collection is empty
        if (collection.getMember().isEmpty()) {
            return new ObservationCollectionType("urn:ogc:def:nil:OGC:inapplicable");
        }

        final List<FeaturePropertyType>      foiAlreadySee   = new ArrayList<FeaturePropertyType> ();
        final List<PhenomenonPropertyType>   phenoAlreadySee = new ArrayList<PhenomenonPropertyType>();
        final List<AbstractEncodingProperty> encAlreadySee   = new ArrayList<AbstractEncodingProperty>();
        final List<DataComponentProperty>    dataAlreadySee  = new ArrayList<DataComponentProperty>();
        for (Observation observation: collection.getMember()) {
            //we do this for the feature of interest
            final FeaturePropertyType foi = ((ObservationType)observation).getPropertyFeatureOfInterest();
            if (foiAlreadySee.contains(foi)){
                foi.setToHref();
            } else {
                foiAlreadySee.add(foi);
            }
            //for the phenomenon
            final PhenomenonPropertyType phenomenon = ((ObservationType)observation).getPropertyObservedProperty();
            if (phenoAlreadySee.contains(phenomenon)){
                phenomenon.setToHref();
            } else {
                if (phenomenon.getPhenomenon() instanceof CompositePhenomenonType) {
                    final CompositePhenomenonType compo = (CompositePhenomenonType) phenomenon.getPhenomenon();
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
            } else if (observation.getResult() instanceof MeasureType) {
                // do nothing
            } else {
                if (observation.getResult() != null) {
                    LOGGER.log(Level.WARNING, "NormalizeDocument: Class not recognized for result:{0}", observation.getResult().getClass().getSimpleName());
                } else {
                    LOGGER.warning("NormalizeDocument: The result is null");
                }
            }
        }
        return collection;
    }
}
