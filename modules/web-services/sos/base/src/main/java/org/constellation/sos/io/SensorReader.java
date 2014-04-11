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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.constellation.ws.CstlServiceException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface SensorReader {
    
    Map<String, List<String>> getAcceptedSensorMLFormats();
    
    /**
     * Return the specified sensor description from the specified ID.
     *
     * @param sensorID The identifier of the sensor.
     *
     * @return the specified sensor description from the specified ID.
     * @throws org.constellation.ws.CstlServiceException
     */
    AbstractSensorML getSensor(final String sensorID) throws CstlServiceException;

    /**
     * Return all sensor ID's.
     *
     * @return All sensor ID's.
     * @throws org.constellation.ws.CstlServiceException
     */
    Collection<String> getSensorNames() throws CstlServiceException;

    /**
     * Return informations about the implementation class.
     */
    String getInfos();

    int getSensorCount()throws CstlServiceException;
    
    void removeFromCache(final String sensorID);
    
    /**
     * Destroy and free the resource used by the reader.
     */
    void destroy();
}
