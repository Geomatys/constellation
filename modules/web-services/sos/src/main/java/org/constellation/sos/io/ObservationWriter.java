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

import java.util.logging.Logger;

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
 * @author Guilhem Legal
 */
public abstract class ObservationWriter {
    
    /**
     * use for debugging purpose
     */
    protected Logger logger = Logger.getLogger("org.constellation.sos.io");
    
    public ObservationWriter() throws CstlServiceException {
    }
   
    public abstract String writeObservation(Observation observation) throws CstlServiceException;
    
    public abstract String writeMeasurement(Measurement measurement) throws CstlServiceException;
    
    public abstract String writeOffering(ObservationOfferingEntry offering) throws CstlServiceException;
    
    public abstract void updateOffering(OfferingProcedureEntry offProc, OfferingPhenomenonEntry offPheno,
            OfferingSamplingFeatureEntry offSF) throws CstlServiceException;
    
    public abstract void updateOfferings();
    
    public abstract void recordProcedureLocation(String physicalID, DirectPositionType position) throws CstlServiceException;
    
    public abstract void destroy();
}
