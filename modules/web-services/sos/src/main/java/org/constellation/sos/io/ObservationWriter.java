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
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.sos.v100.ObservationOfferingEntry;
import org.constellation.sos.v100.OfferingPhenomenonEntry;
import org.constellation.sos.v100.OfferingProcedureEntry;
import org.constellation.sos.v100.OfferingSamplingFeatureEntry;
import org.constellation.ws.CstlServiceException;

// GeoAPI dependencies
import org.opengis.observation.Measurement;
import org.opengis.observation.Observation;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface ObservationWriter {
    
    public String writeObservation(Observation observation) throws CstlServiceException;
    
    public String writeMeasurement(Measurement measurement) throws CstlServiceException;
    
    public String writeOffering(ObservationOfferingEntry offering) throws CstlServiceException;
    
    public void updateOffering(OfferingProcedureEntry offProc, OfferingPhenomenonEntry offPheno,
            OfferingSamplingFeatureEntry offSF) throws CstlServiceException;
    
    public void updateOfferings();
    
    public void recordProcedureLocation(String physicalID, DirectPositionType position) throws CstlServiceException;

    /**
     * Return informations about the implementation class.
     */
    public String getInfos();
    
    public void destroy();
}
