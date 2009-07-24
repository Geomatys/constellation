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

// constellation dependencies
import org.constellation.ws.CstlServiceException;

// GeoAPI dependencies
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.OfferingPhenomenonEntry;
import org.geotoolkit.sos.xml.v100.OfferingProcedureEntry;
import org.geotoolkit.sos.xml.v100.OfferingSamplingFeatureEntry;
import org.opengis.observation.Measurement;
import org.opengis.observation.Observation;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface ObservationWriter {

    /**
     * Write a new Observation into the database
     *
     * @param observation An O&M observation
     *
     * @return The new identifiers of the observation
     *
     * @throws CstlServiceException
     */
    String writeObservation(Observation observation) throws CstlServiceException;

    /**
     * Write a new Measurement into the database
     *
     * @param measurement An O&M measurement
     *
     * @return The new identifiers of the observation
     *
     * @throws CstlServiceException
     */
    String writeMeasurement(Measurement measurement) throws CstlServiceException;

    /**
     * Write a new Observation offering into the database
     *
     * @param offering
     * @return
     * @throws CstlServiceException
     */
    String writeOffering(ObservationOfferingEntry offering) throws CstlServiceException;
    
    void updateOffering(OfferingProcedureEntry offProc, OfferingPhenomenonEntry offPheno,
            OfferingSamplingFeatureEntry offSF) throws CstlServiceException;
    
    void updateOfferings();
    
    void recordProcedureLocation(String physicalID, DirectPositionType position) throws CstlServiceException;

    /**
     * Return informations about the implementation class.
     */
    String getInfos();
    
    void destroy();
}
