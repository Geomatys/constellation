/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.sos.io;

import org.constellation.ws.CstlServiceException;
import org.geotoolkit.sml.xml.AbstractSensorML;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
     * Return all sensor ID's.
     *
     * @param sensorTypeFilter
     * @return All sensor ID's.
     * @throws org.constellation.ws.CstlServiceException
     */
    Collection<String> getSensorNames(String sensorTypeFilter) throws CstlServiceException;

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
