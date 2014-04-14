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
import java.util.Set;
import java.util.logging.Level;
import javax.xml.namespace.QName;

import org.constellation.ws.CstlServiceException;

import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.ResponseModeType;

/**
 * 
 * @author Guilhem Legal (Geomatys)
 */
public interface ObservationFilter {

    /**
     * Initialize the query for a full observation request.
     *
     * @param requestMode
     * @param resultModel
     * @throws org.constellation.ws.CstlServiceException
     */
    void initFilterObservation(final ResponseModeType requestMode, final QName resultModel) throws CstlServiceException;

    /**
     * Initialize the query for a restricted to the results request.
     *
     * @param procedure
     * @param resultModel
     * @throws org.constellation.ws.CstlServiceException
     */
    void initFilterGetResult(final String procedure, final QName resultModel) throws CstlServiceException;
    
    /**
     * Initialize the query for a restricted to the results request.
     * @throws org.constellation.ws.CstlServiceException
     */
    void initFilterGetFeatureOfInterest() throws CstlServiceException;

    /**
     * Add some procedure filter to the request.
     * if the list of procedure ID is empty it add all the offering procedure.
     *
     * @param procedures
     * @param offerings
     * @throws org.constellation.ws.CstlServiceException
     */
    void setProcedure(final List<String> procedures, final List<ObservationOffering> offerings) throws CstlServiceException;

    /**
     * Add some phenomenon filter to the request.
     *
     * @param phenomenon
     */
    void setObservedProperties(final List<String> phenomenon);

    /**
     * Add some feature of interest filter to the request.
     *
     * @param fois the feature of interest identifiers.
     */
    void setFeatureOfInterest(final List<String> fois);

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
     * @throws org.constellation.ws.CstlServiceException
     */
    void setTimeLatest() throws CstlServiceException;
    
    /**
     * Add a first time filter to the current request.
     *
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
    void setBoundingBox(Envelope e) throws CstlServiceException;

    /**
     * Set the offering for the current request
     *
     * @param offerings
     * @throws org.constellation.ws.CstlServiceException
     */
    void setOfferings(final List<ObservationOffering> offerings) throws CstlServiceException;
            
    /**
     * Add a filter on the result for the specified property.
     *
     * @param propertyName a property of the result.
     * @param value a literal value.
     * @throws org.constellation.ws.CstlServiceException
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
    Set<String> filterObservation() throws CstlServiceException;
    
    
    /**
     * Execute the current query and return a list of FOI ID.
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    Set<String> filterFeatureOfInterest() throws CstlServiceException;

    /**
     * Return informations about the implementation class.
     */
    String getInfos();

    /**
     * Refresh the index if it need it.
     * @throws org.constellation.ws.CstlServiceException
     */
    void refresh() throws CstlServiceException;
    
    /**
     * Return true if each observation has a position.
     */
    boolean isBoundedObservation();

    /**
     * Return true if template are filled with a default period when there is no eventTime suplied.
     */
    boolean isDefaultTemplateTime();
    
    /**
     * Set the global level for information message.
     *
     * @param logLevel
     */
    void setLoglevel(Level logLevel);

    void destroy();
}
