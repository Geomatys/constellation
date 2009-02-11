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
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

// Constellation dependencies
import org.constellation.gml.v311.ReferenceEntry;
import org.constellation.observation.ObservationEntry;
import org.constellation.sampling.SamplingFeatureEntry;
import org.constellation.sos.v100.ObservationOfferingEntry;
import org.constellation.swe.AnyResult;
import org.constellation.swe.v101.PhenomenonEntry;
import org.constellation.ws.CstlServiceException;

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
     * Build a new Observation Reader
     *
     * @param observationIdBase An urn prefixing the observations id.
     */
    public ObservationReader(String observationIdBase) throws CstlServiceException {
        this.observationIdBase = observationIdBase;
    }

    /**
     * Return the list of offering names.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    public abstract Collection<String> getOfferingNames() throws CstlServiceException;

    /**
     * Return The offering with the specified name.
     *
     * @param offeringName The identifiers of the offering
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public abstract ObservationOfferingEntry getObservationOffering(String offeringName) throws CstlServiceException;

    /**
     * Return a list of all the offerings.
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public abstract List<ObservationOfferingEntry> getObservationOfferings() throws CstlServiceException;

    /**
     * Return a list of the sensor identifiers.
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public abstract Collection<String> getProcedureNames() throws CstlServiceException;

    /**
     * Return a list of the phenomenon identifiers.
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public abstract Collection<String> getPhenomenonNames() throws CstlServiceException;

    /**
     * Return a phenomenon with the specified identifier
     * @param phenomenonName
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public abstract PhenomenonEntry getPhenomenon(String phenomenonName) throws CstlServiceException;

    /**
     * Return a list of sampling feature identifiers.
     *
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public abstract Collection<String> getFeatureOfInterestNames() throws CstlServiceException;

    /**
     * Return a sampling feature for the specified sampling feature.
     *
     * @param samplingFeatureName
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public abstract SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureName) throws CstlServiceException;

    /**
     * Return an observation for the specified identifier.
     * 
     * @param identifier
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public abstract ObservationEntry getObservation(String identifier) throws CstlServiceException;

    /**
     * Return a result for the specified identifier.
     *
     * @param identifier
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public abstract AnyResult getResult(String identifier) throws CstlServiceException;

    /**
     * Return a reference from the specified identifier
     * @param href
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public abstract ReferenceEntry getReference(String href) throws CstlServiceException;
    
    /**
     * Create a new identifier for an observation.
     */
    public abstract String getNewObservationId() throws CstlServiceException;
    
    /**
     * Return the minimal value for the offering event Time
     */
    public abstract String getMinimalEventTime() throws CstlServiceException;

    /**
     * free the resources and close the database connection if there is one.
     */
    public abstract void destroy();
}
