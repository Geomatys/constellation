/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

import java.util.List;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.opengis.observation.Observation;

/**
 * 
 * @author Guilhem Legal (Geomatys)
 */
public interface ObservationFilter {

    /**
     * Initialize the query for a full observation request.
     */
    void initFilterObservation(ResponseModeType requestMode, QName resultModel);

    /**
     * Initialize the query for a restricted to the results request.
     */
    void initFilterGetResult(Observation procedure, QName resultModel);

    /**
     * Add some procedure filter to the request.
     * if the list of procedure ID is empty it add all the offering procedure.
     *
     * @param procedures
     * @param off
     */
    void setProcedure(List<String> procedures, ObservationOfferingType off);

    /**
     * Add some phenomenon filter to the request.
     *
     * @param phenomenon
     * @param compositePhenomenon
     */
    void setObservedProperties(List<String> phenomenon, List<String> compositePhenomenon);

    /**
     * Add some sampling point filter to the request.
     *
     * @param phenomenon
     * @param compositePhenomenon
     */
    void setFeatureOfInterest(List<String> fois);

    /**
     * Add a TM_Equals filter to the current request.
     * 
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
     void setTimeEquals(Object time) throws CstlServiceException;

    /**
     * Add a TM_Before filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    void setTimeBefore(Object time) throws CstlServiceException;

    /**
     * Add a TM_After filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    void setTimeAfter(Object time) throws CstlServiceException;

    /**
     * Add a TM_During filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    void setTimeDuring(Object time) throws CstlServiceException;
    
    /**
     * Add a latest time filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    void setTimeLatest() throws CstlServiceException;
    
    /**
     * Add a first time filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    void setTimeFirst() throws CstlServiceException;

    /**
     * Add a BBOX filter to the current request.
     * ( this method is implemented only if isBoundedObservation() return true)
     *
     * @param e
     * @throws org.constellation.ws.CstlServiceException
     */
    void setBoundingBox(EnvelopeType e) throws CstlServiceException;

    /**
     * Set the offering for the current request
     *
     * @param offering
     * @throws org.constellation.ws.CstlServiceException
     */
    void setOffering(final ObservationOfferingType offering) throws CstlServiceException;
            
    /**
     * Add a filter on the result for the specified property.
     *
     * @param propertyName a property of the result.
     * @param value a literal value.
     */
    void setResultEquals(String propertyName, String value) throws CstlServiceException;

    /**
     * Return the list of properties that can be applied on the result.
     * 
     * @return  the list of properties that can be applied on the result.
     */
    List<String> supportedQueryableResultProperties();

    /**
     * Execute the current query and return a list of observation result.
     * 
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    List<ObservationResult> filterResult() throws CstlServiceException;

    /**
     * Execute the current query and return a list of observation ID.
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    List<String> filterObservation() throws CstlServiceException;

    /**
     * Return informations about the implementation class.
     */
    String getInfos();

    /**
     * Refresh the index if it need it.
     */
    void refresh() throws CstlServiceException;
    
    /**
     * Return true if each observation has a position.
     */
    boolean isBoundedObservation();

    /**
     * Set the global level for information message.
     *
     * @param logLevel
     */
    void setLoglevel(Level logLevel);
}
