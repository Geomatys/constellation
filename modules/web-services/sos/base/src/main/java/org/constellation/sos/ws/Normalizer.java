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
import org.geotoolkit.gml.xml.FeatureProperty;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.observation.xml.Process;
import org.geotoolkit.observation.xml.v100.MeasureType;
import org.geotoolkit.observation.xml.v100.MeasurementType;
import org.geotoolkit.sos.xml.Capabilities;
import org.geotoolkit.sos.xml.SOSXmlFactory;
import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.swe.xml.AbstractEncodingProperty;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.geotoolkit.swe.xml.DataComponentProperty;
import org.geotoolkit.swe.xml.PhenomenonProperty;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonType;
import org.geotoolkit.util.logging.Logging;
import org.opengis.observation.Observation;
import org.opengis.observation.ObservationCollection;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;

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
        final List<PhenomenonProperty> alreadySee = new ArrayList<PhenomenonProperty>();
        if (capa.getContents() != null) {
            for (ObservationOfferingType off: capa.getContents().getObservationOfferingList().getObservationOffering()) {
                for (PhenomenonProperty pheno: off.getRealObservedProperty()) {
                    if (alreadySee.contains(pheno)) {
                        pheno.setToHref();
                    } else {
                        if (pheno.getPhenomenon() instanceof CompositePhenomenonType) {
                            final CompositePhenomenonType compo = (CompositePhenomenonType) pheno.getPhenomenon();
                            for (PhenomenonProperty pheno2: compo.getRealComponent()) {
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
        final Map<String, Observation> merged = new HashMap<String, Observation>();
        for (Observation obs : members) {
            final Process process = (Process) obs.getProcedure();
            if (merged.containsKey(process.getHref())) {
                final AbstractObservation uniqueObs = (AbstractObservation) merged.get(process.getHref());
                if (uniqueObs.getResult() instanceof DataArrayProperty) {
                    final DataArrayProperty mergedArrayP = (DataArrayProperty) uniqueObs.getResult();
                    final DataArray mergedArray          = mergedArrayP.getDataArray();

                    if (obs.getResult() instanceof DataArrayProperty) {
                        final DataArrayProperty arrayP = (DataArrayProperty) obs.getResult();
                        final DataArray array          = arrayP.getDataArray();

                        //we merge this observation with the map one
                        mergedArray.setElementCount(mergedArray.getElementCount().getCount().getValue() + array.getElementCount().getCount().getValue());
                        mergedArray.setValues(mergedArray.getValues() + array.getValues());
                    } 
                }
                // merge the samplingTime
                if (uniqueObs.getSamplingTime() instanceof Period) {
                    final Period totalPeriod = (Period)uniqueObs.getSamplingTime();
                    if (obs.getSamplingTime() instanceof Instant) {
                        final Instant instant = (Instant)obs.getSamplingTime();
                        if (totalPeriod.getBeginning().getPosition().getDate().getTime() > instant.getPosition().getDate().getTime()) {
                            final Period newPeriod = SOSXmlFactory.buildTimePeriod(version,  instant.getPosition(), totalPeriod.getEnding().getPosition());
                            uniqueObs.setSamplingTimePeriod(newPeriod);
                        }
                        if (totalPeriod.getEnding().getPosition().getDate().getTime() < instant.getPosition().getDate().getTime()) {
                            final Period newPeriod = SOSXmlFactory.buildTimePeriod(version,  totalPeriod.getBeginning().getPosition(), instant.getPosition());
                            uniqueObs.setSamplingTimePeriod(newPeriod);
                        } 
                    } else if (obs.getSamplingTime() instanceof Period) {
                        final Period period = (Period)obs.getSamplingTime();
                        if (totalPeriod.getBeginning().getPosition().getDate().getTime() > period.getBeginning().getPosition().getDate().getTime()) {
                            final Period newPeriod = SOSXmlFactory.buildTimePeriod(version,  period.getBeginning().getPosition(), totalPeriod.getEnding().getPosition());
                            uniqueObs.setSamplingTimePeriod(newPeriod);
                        }
                        if (totalPeriod.getEnding().getPosition().getDate().getTime() < period.getEnding().getPosition().getDate().getTime()) {
                            final Period newPeriod = SOSXmlFactory.buildTimePeriod(version,  totalPeriod.getBeginning().getPosition(), period.getEnding().getPosition());
                            uniqueObs.setSamplingTimePeriod(newPeriod);
                        }
                    }
                }
            } else {
                final Observation clone;
                if (obs instanceof MeasurementType) {
                    clone = (MeasurementType) obs;
                } else {
                    clone = SOSXmlFactory.cloneObservation(version, obs);
                }
                merged.put(process.getHref(), clone);
            }
        }

        final List<Observation> obervations = new ArrayList<Observation>();
        for (Observation entry: merged.values()) {
            obervations.add(entry);
        }
        return SOSXmlFactory.buildGetObservationResponse(version, "collection-1", bounds, obervations);
    }

    /**
     * Normalize the Observation collection document by replacing the double by reference
     *
     * @param collection the unnormalized document.
     *
     * @return a normalized document
     */
    public static ObservationCollection normalizeDocument(final String version, final ObservationCollection collection) {
        //first if the collection is empty
        if (collection.getMember().isEmpty()) {
            return SOSXmlFactory.buildObservationCollection(version, "urn:ogc:def:nil:OGC:inapplicable");
        }

        final List<FeatureProperty>          foiAlreadySee   = new ArrayList<FeatureProperty> ();
        final List<PhenomenonProperty>       phenoAlreadySee = new ArrayList<PhenomenonProperty>();
        final List<AbstractEncodingProperty> encAlreadySee   = new ArrayList<AbstractEncodingProperty>();
        final List<DataComponentProperty>    dataAlreadySee  = new ArrayList<DataComponentProperty>();
        for (Observation observation: collection.getMember()) {
            //we do this for the feature of interest
            final FeatureProperty foi =  getPropertyFeatureOfInterest(observation);
            if (foi != null) {
                if (foiAlreadySee.contains(foi)){
                    foi.setToHref();
                } else {
                    foiAlreadySee.add(foi);
                }
            }
            //for the phenomenon
            final PhenomenonProperty phenomenon = getPhenomenonProperty(observation);
            if (phenomenon != null) {
                if (phenoAlreadySee.contains(phenomenon)){
                    phenomenon.setToHref();
                } else {
                    if (phenomenon.getPhenomenon() instanceof CompositePhenomenonType) {
                        final CompositePhenomenonType compo = (CompositePhenomenonType) phenomenon.getPhenomenon();
                        for (PhenomenonProperty pheno2: compo.getRealComponent()) {
                            if (phenoAlreadySee.contains(pheno2)) {
                                pheno2.setToHref();
                            } else {
                                phenoAlreadySee.add(pheno2);
                            }
                        }
                    }
                    phenoAlreadySee.add(phenomenon);
                }
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
    
    private static FeatureProperty getPropertyFeatureOfInterest(final Observation obs) {
        if (obs instanceof org.geotoolkit.observation.xml.v100.ObservationType) {
            return ((org.geotoolkit.observation.xml.v100.ObservationType)obs).getPropertyFeatureOfInterest();
        } else if (obs instanceof org.geotoolkit.observation.xml.v200.OMObservationType) {
            return ((org.geotoolkit.observation.xml.v200.OMObservationType)obs).getFeatureOfInterestProperty();
        }
        return null;
    }
    
    private static PhenomenonProperty getPhenomenonProperty(final Observation obs) {
        if (obs instanceof org.geotoolkit.observation.xml.v100.ObservationType) {
            return ((org.geotoolkit.observation.xml.v100.ObservationType)obs).getPropertyObservedProperty();
        }
        return null;
    }
}
