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

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.sml.AbstractSensorML;
import org.constellation.ws.WebServiceException;

/**
 *
 * @author Guilhem Legal
 */
public abstract class SensorReader {
    
    /**
     * use for debugging purpose
     */
    protected Logger logger = Logger.getLogger("org.constellation.sos.ws");

    public SensorReader() throws WebServiceException {
    }
    
    public abstract AbstractSensorML getSensor(String sensorId) throws WebServiceException;
    
    public abstract DirectPositionType getSensorPosition(int formID) throws WebServiceException;

    public abstract List<Integer> getNetworkIndex(int formID) throws WebServiceException;
    
    public abstract String getNetworkName(int formID, String networkName) throws WebServiceException;
    
    /**
     * Create a new identifier for an observation by searching in the O&M database.
     */
    public abstract int getNewSensorId() throws WebServiceException;

    public abstract void destroy();
}
