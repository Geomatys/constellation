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

package org.constellation.sos.io;

// J2SE dependencies
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

// Constellation dependencies
import org.constellation.gml.v311.ReferenceEntry;
import org.constellation.observation.ObservationEntry;
import org.constellation.sampling.SamplingFeatureEntry;
import org.constellation.sos.ObservationOfferingEntry;
import org.constellation.swe.v101.AnyResultEntry;
import org.constellation.swe.v101.PhenomenonEntry;
import org.constellation.ws.WebServiceException;

/**
 *
 * @author Guilhem Legal
 */
public abstract class ObservationReader {
    
    /**
     * use for debugging purpose
     */
    protected Logger logger = Logger.getLogger("org.constellation.sos.io");
    
    /**
     * The base for observation id.
     */ 
    protected final String observationIdBase;
    
    /**
     * 
     * @param dataSourceOM
     * @param observationIdBase
     */
    public ObservationReader(String observationIdBase) throws WebServiceException {
        this.observationIdBase = observationIdBase;
    }
    
    public abstract Set<String> getOfferingNames() throws WebServiceException;
    
    public abstract ObservationOfferingEntry getObservationOffering(String offeringName) throws WebServiceException;

    public abstract List<ObservationOfferingEntry> getObservationOfferings() throws WebServiceException;
    
    public abstract Set<String> getProcedureNames() throws WebServiceException;
    
    public abstract Set<String> getPhenomenonNames() throws WebServiceException;
    
    public abstract PhenomenonEntry getPhenomenon(String phenomenonName) throws WebServiceException;
    
    public abstract Set<String> getFeatureOfInterestNames() throws WebServiceException;
    
    public abstract SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureName) throws WebServiceException;
    
    public abstract ObservationEntry getObservation(String identifier) throws WebServiceException;

    public abstract AnyResultEntry getResult(String identifier) throws WebServiceException;
    
    public abstract Set<ReferenceEntry> getReferences() throws WebServiceException;
    
    /**
     * Create a new identifier for an observation by searching in the O&M database.
     */
    public abstract String getNewObservationId() throws WebServiceException;
    
    /**
     * Return the minimal value for the offering event Time
     */
    public abstract String getMinimalEventTime() throws WebServiceException;

    public abstract void destroy();
}
