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

import org.geotoolkit.sml.xml.AbstractSensorML;
import org.constellation.ws.CstlServiceException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface SensorReader {
    
    /**
     * Return the specified sensor description from the specified ID.
     *
     * @param sensorID The identifier of the sensor.
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public AbstractSensorML getSensor(String sensorID) throws CstlServiceException;

    /**
     * Return informations about the implementation class.
     */
    public String getInfos();

    /**
     * Destroy and free the resource used by the reader.
     */
    public abstract void destroy();
}
