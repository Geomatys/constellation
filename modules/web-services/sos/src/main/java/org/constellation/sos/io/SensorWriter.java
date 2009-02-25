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

import org.constellation.sml.AbstractSensorML;
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
    public void writeSensor(String id, AbstractSensorML sensor) throws CstlServiceException;

    /**
     * Start a transaction on the datasource.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    public void startTransaction() throws CstlServiceException;

    /**
     * Abort if there is a transaction running.
     * Restore the data like they were before the begin of the transaction.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    public void abortTransaction() throws CstlServiceException;

    /**
     * End a transaction (if there is one running)
     * and store the changement made during this transaction on the datasource.
     * 
     * @throws org.constellation.ws.CstlServiceException
     */
    public void endTransaction() throws CstlServiceException;

    /**
     * Create a new identifier for a sensor.
     */
    public int getNewSensorId() throws CstlServiceException;

    /**
     * Return informations about the implementation class.
     */
    public String getInfos();

    /**
     * Free the resources and close the connections to datasource.
     */
    public void destroy();
}
