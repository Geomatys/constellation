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

import org.geotoolkit.sml.xml.AbstractSensorML;
import org.constellation.ws.CstlServiceException;

/**
 * An interface used by the SOS worker to store sensorML document into various datasource.
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface SensorWriter {

    /**
     * Store a new SensorML document into the data source.
     *
     * @param id The identifier of the sensor
     * @param sensor The sensor description.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    boolean writeSensor(String id, AbstractSensorML sensor) throws CstlServiceException;

    /**
     * Delete a SensorML document into the data source.
     *
     * @param id The identifier of the sensor
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    boolean deleteSensor(String id) throws CstlServiceException;

    /**
     * Replace a SensorML document into the data source.
     *
     * @param id The identifier of the sensor
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    int replaceSensor(String id, AbstractSensorML process) throws CstlServiceException;

    /**
     * Start a transaction on the datasource.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    void startTransaction() throws CstlServiceException;

    /**
     * Abort if there is a transaction running.
     * Restore the data like they were before the begin of the transaction.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    void abortTransaction() throws CstlServiceException;

    /**
     * End a transaction (if there is one running)
     * and store the changement made during this transaction on the datasource.
     * 
     * @throws org.constellation.ws.CstlServiceException
     */
    void endTransaction() throws CstlServiceException;

    /**
     * Create a new identifier for a sensor.
     */
    int getNewSensorId() throws CstlServiceException;

    /**
     * Return informations about the implementation class.
     */
    String getInfos();

    /**
     * Free the resources and close the connections to datasource.
     */
    void destroy();
}
