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

package org.constellation.sos.factory;

import java.io.File;
import java.sql.Connection;
import java.util.Properties;
import javax.sql.DataSource;
import org.constellation.sos.io.DataSourceType;
import org.constellation.sos.io.DefaultObservationFilter;
import org.constellation.sos.io.DefaultObservationReader;
import org.constellation.sos.io.DefaultObservationWriter;
import org.constellation.sos.io.FileSensorReader;
import org.constellation.sos.io.FileSensorWriter;
import org.constellation.sos.io.MDWebSensorReader;
import org.constellation.sos.io.MDWebSensorWriter;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.WebServiceException;

/**
 *
 * @author Guilhem Legal
 */
public class DefaultSOSFactory extends AbstractSOSFactory {

    public DefaultSOSFactory() {
        super(4);
    }

    @Override
    public ObservationFilter getObservationFilter(String observationIdBase, String observationTemplateIdBase, Properties map, Connection connection) throws WebServiceException {
        return new DefaultObservationFilter(observationIdBase, observationTemplateIdBase, map, connection);
    }

    @Override
    public ObservationReader getObservationReader(DataSource dataSourceOM, String observationIdBase) throws WebServiceException {
        return new DefaultObservationReader(dataSourceOM, observationIdBase);
    }

    @Override
    public ObservationWriter getObservationWriter(DataSource dataSourceOM) throws WebServiceException {
        return new DefaultObservationWriter(dataSourceOM);
    }

    @Override
    public SensorReader getSensorReader(DataSourceType type, File dataDirectory, String sensorIdBase, Connection connection, Properties map) throws WebServiceException {
        switch (type) {
            case FILE_SYSTEM: return new FileSensorReader(dataDirectory, sensorIdBase);

            case MDWEB: return new MDWebSensorReader(connection, sensorIdBase, map);

            default: throw new IllegalArgumentException("Unknow dataSource type: " + type);
        }
    }

    @Override
    public SensorWriter getSensorWriter(DataSourceType type,  File dataDirectory, Connection connection, String sensorIdBase) throws WebServiceException {
        switch (type) {
            case FILE_SYSTEM: return new FileSensorWriter(dataDirectory);

            case MDWEB: return new MDWebSensorWriter(connection, sensorIdBase);

            default: throw new IllegalArgumentException("Unknow dataSource type: " + type);
        }
    }

}
