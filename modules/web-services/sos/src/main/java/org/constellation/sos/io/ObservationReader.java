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

// Constellation dependencies
import javax.xml.namespace.QName;
import org.constellation.ws.CstlServiceException;

// GeoAPI
import org.geotoolkit.gml.xml.v311.AbstractTimePrimitiveType;
import org.geotoolkit.gml.xml.v311.ReferenceType;
import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.opengis.observation.Observation;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.sampling.SamplingFeature;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface ObservationReader {
    
    /**
     * Return the list of offering names.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    Collection<String> getOfferingNames() throws CstlServiceException;

    /**
     * Return The offering with the specified name.
     *
     * @param offeringName The identifiers of the offering
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    ObservationOfferingType getObservationOffering(String offeringName) throws CstlServiceException;

    /**
     * Return a list of all the offerings.
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    List<ObservationOfferingType> getObservationOfferings() throws CstlServiceException;

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
     * Return a phenomenon with the specified identifier
     * @param phenomenonName
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    Phenomenon getPhenomenon(String phenomenonName) throws CstlServiceException;

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
     *
     * @return the correspounding feature Of interest.
     * @throws org.constellation.ws.CstlServiceException
     */
    SamplingFeature getFeatureOfInterest(String samplingFeatureName) throws CstlServiceException;


    /**
     * Return a sampling feature for the specified sampling feature.
     *
     * @param samplingFeatureName
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    AbstractTimePrimitiveType getFeatureOfInterestTime(String samplingFeatureName) throws CstlServiceException;

    /**
     * Return an observation for the specified identifier.
     * 
     * @param identifier
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    Observation getObservation(String identifier, QName resultModel) throws CstlServiceException;

    /**
     * Return a result for the specified identifier.
     *
     * @param identifier
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    Object getResult(String identifier, QName resultModel) throws CstlServiceException;

    /**
     * Return a reference from the specified identifier
     * @param href
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    ReferenceType getReference(String href) throws CstlServiceException;
    
    /**
     * Create a new identifier for an observation.
     */
    String getNewObservationId() throws CstlServiceException;
    
    /**
     * Return the minimal/maximal value for the offering event Time
     */
    List<String> getEventTime() throws CstlServiceException;

    /**
     * Return the list of supported response Mode
     */
    List<ResponseModeType> getResponseModes() throws CstlServiceException;

    /**
     * Return the list of supported response Mode
     */
    List<String> getResponseFormats() throws CstlServiceException;
    
    /**
     * Return informations about the implementation class.
     */
    String getInfos();
    
    /**
     * free the resources and close the database connection if there is one.
     */
    void destroy();
}
