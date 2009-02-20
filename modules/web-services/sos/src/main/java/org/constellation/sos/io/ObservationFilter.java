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

import org.constellation.sos.v100.ObservationOfferingEntry;
import org.constellation.sos.v100.ResponseModeType;
import org.constellation.ws.CstlServiceException;

/**
 *
 * @author Guilhem Legal
 */
public interface ObservationFilter {

    /**
     * Initialize the query.
     */
    public void initFilterObservation(ResponseModeType requestMode);

    /**
     * Initialize the query.
     */
    public void initFilterGetResult(String procedure);

    /**
     * Add some procedure filter to the request.
     * if the list of procedure ID is empty it add all the offering procedure.
     *
     * @param procedures
     * @param off
     */
    public void setProcedure(List<String> procedures, ObservationOfferingEntry off);

    /**
     * Add some phenomenon filter to the request.
     *
     * @param phenomenon
     * @param compositePhenomenon
     */
    public void setObservedProperties(List<String> phenomenon, List<String> compositePhenomenon);

    /**
     * Add some sampling point filter to the request.
     *
     * @param phenomenon
     * @param compositePhenomenon
     */
    public void setFeatureOfInterest(List<String> fois);

    /**
     * Add a TM_Equals filter to the current request.
     * 
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    public void setTimeEquals(Object time) throws CstlServiceException;

    /**
     * Add a TM_Before filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    public void setTimeBefore(Object time) throws CstlServiceException;

    /**
     * Add a TM_After filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    public void setTimeAfter(Object time) throws CstlServiceException;

    /**
     * Add a TM_During filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    public void setTimeDuring(Object time) throws CstlServiceException;

    /**
     * Execute the current query and return a list of observation result.
     * 
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public List<ObservationResult> filterResult() throws CstlServiceException;

    /**
     * Execute the current query and return a list of observation ID.
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public List<String> filterObservation() throws CstlServiceException;

}
