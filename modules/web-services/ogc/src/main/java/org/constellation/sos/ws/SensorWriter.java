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

package org.constellation.sos.ws;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import org.constellation.ws.WebServiceException;

/**
 *
 * @author Guilhem Legal
 */
public abstract class SensorWriter {

    /**
     * use for debugging purpose
     */
    protected Logger logger = Logger.getLogger("org.constellation.sos.ws");
    
    public SensorWriter() throws SQLException {
    }
    
    public abstract int writeSensor(String id, File sensorFile) throws WebServiceException;
    
    /**
     * Record the mapping between physical ID and database ID.
     * 
     * @param form The "form" containing the sensorML data.
     * @param dbId The identifier of the sensor in the O&M database.
     */
    public abstract String recordMapping(int formID, String dbId, File sicadeDirectory) throws SQLException, FileNotFoundException, IOException;
    
    
    public abstract void startTransaction() throws SQLException;
    
    public abstract void abortTransaction() throws SQLException;
    
    public abstract void endTransaction() throws SQLException;

    public abstract void destroy();
}
