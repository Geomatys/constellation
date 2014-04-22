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
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.ws.CstlServiceException;

// Geotoolkit
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.ResponseModeType;

// GeoAPI
import org.opengis.observation.Observation;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.temporal.TemporalPrimitive;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface ObservationReader {
    
    /**
     * Return the list of offering names.
     *
     * @param version SOS version of the request
     * @return A list of offering name.
     * @throws org.constellation.ws.CstlServiceException
     */
    Collection<String> getOfferingNames(final String version) throws CstlServiceException;

    /**
     * Return The offering with the specified name.
     *
     * @param offeringName The identifier of the offering
     * @param version SOS version of the request
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    ObservationOffering getObservationOffering(final String offeringName, final String version) throws CstlServiceException;
    
    /**
     * Return The offerings for the specified names.
     *
     * @param offeringNames The identifiers of the offerings
     * @param version SOS version of the request
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    List<ObservationOffering> getObservationOfferings(final List<String> offeringNames, final String version) throws CstlServiceException;

    /**
     * Return a list of all the offerings.
     * 
     * @param version SOS version of the request
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    List<ObservationOffering> getObservationOfferings(final String version) throws CstlServiceException;

    /**
     * Return a list of the sensor identifiers.
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    Collection<String> getProcedureNames() throws CstlServiceException;

    /**
     * Return a list of the phenomenon identifiers.
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    Collection<String> getPhenomenonNames() throws CstlServiceException;

    /**
     * Return a list of the sensor identifiers measuring the specified phenomenon.
     * 
     * @param observedProperty an observed phenomenon.
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    Collection<String> getProceduresForPhenomenon(final String observedProperty) throws CstlServiceException;
    
    /**
     * Return a list of the observedProperties identifiers measured by the specified procedure.
     * 
     * @param sensorID an procedure identifier.
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    Collection<String> getPhenomenonsForProcedure(String sensorID) throws CstlServiceException;
    
    /**
     * 
     * @param phenomenonName
     * @return
     * @throws org.constellation.ws.CstlServiceException 
     */
    boolean existPhenomenon(final String phenomenonName) throws CstlServiceException;
    
    /**
     * Return a list of sampling feature identifiers.
     *
     * @return A list of sampling feature identifiers.
     * @throws org.constellation.ws.CstlServiceException
     */
    Collection<String> getFeatureOfInterestNames() throws CstlServiceException;

    /**
     * Return a sampling feature for the specified sampling feature.
     *
     * @param samplingFeatureName The identifier of the feature of interest.
     * @param version SOS version of the request
     *
     * @return the corresponding feature Of interest.
     * @throws org.constellation.ws.CstlServiceException
     */
    SamplingFeature getFeatureOfInterest(final String samplingFeatureName, final String version) throws CstlServiceException;


    /**
     * Return a sampling feature for the specified sampling feature.
     *
     * @param samplingFeatureName
     * @param version SOS version of the request
     * 
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    TemporalPrimitive getFeatureOfInterestTime(final String samplingFeatureName, final String version) throws CstlServiceException;

    /**
     * Return an observation for the specified identifier.
     * 
     * @param identifier
     * @param resultModel
     * @param mode
     * @param version
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    Observation getObservation(final String identifier, final QName resultModel, final ResponseModeType mode, final String version) throws CstlServiceException;

    /**
     * Return a result for the specified identifier.
     *
     * @param identifier
     * @param resultModel
     * @param version
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    Object getResult(final String identifier, final QName resultModel, final String version) throws CstlServiceException;

    /**
     * Return a reference from the specified identifier
     * @param href
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    boolean existProcedure(final String href) throws CstlServiceException;
    
    /**
     * Create a new identifier for an observation.
     * @return 
     * @throws org.constellation.ws.CstlServiceException
     */
    String getNewObservationId() throws CstlServiceException;
    
    /**
     * Return the minimal/maximal value for the offering event Time
     * @return 
     * @throws org.constellation.ws.CstlServiceException
     */
    List<String> getEventTime() throws CstlServiceException;

    /**
     * Return the list of supported response Mode
     * @return 
     * @throws org.constellation.ws.CstlServiceException
     */
    List<ResponseModeType> getResponseModes() throws CstlServiceException;

    /**
     * Return the list of supported response Mode
     * @return 
     * @throws org.constellation.ws.CstlServiceException
     */
    List<String> getResponseFormats() throws CstlServiceException;
    
    /**
     * Return informations about the implementation class.
     * @return 
     */
    String getInfos();
    
    /**
     * free the resources and close the database connection if there is one.
     */
    void destroy();
}
