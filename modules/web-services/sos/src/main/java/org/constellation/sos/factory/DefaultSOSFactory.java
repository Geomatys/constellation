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
import org.constellation.generic.database.Automatic;
import org.constellation.sos.io.DataSourceType;
import org.constellation.sos.io.DefaultGenericObservationReader;
import org.constellation.sos.io.DefaultObservationFilter;
import org.constellation.sos.io.DefaultObservationReader;
import org.constellation.sos.io.DefaultObservationWriter;
import org.constellation.sos.io.FileSensorReader;
import org.constellation.sos.io.FileSensorWriter;
import org.constellation.sos.io.GenericObservationFilter;
import org.constellation.sos.io.MDWebSensorReader;
import org.constellation.sos.io.MDWebSensorWriter;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationFilterType;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.io.ObservationReaderType;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.CstlServiceException;

/**
 *
 * @author Guilhem Legal
 */
public class DefaultSOSFactory extends AbstractSOSFactory {

    public DefaultSOSFactory() {
        super(4);
    }

    @Override
    public ObservationFilter getObservationFilter(ObservationFilterType type, String observationIdBase, String observationTemplateIdBase, Properties map, Connection connection, File configDir) throws CstlServiceException {
      switch (type) {
            case DEFAULT: return new DefaultObservationFilter(observationIdBase, observationTemplateIdBase, map, connection);

            case GENERIC: return new GenericObservationFilter(observationIdBase, observationTemplateIdBase, map, connection, configDir);

            default: throw new IllegalArgumentException("Unknow observationFilter type: " + type);
        }

    }

    @Override
    public ObservationReader getObservationReader(ObservationReaderType type, DataSource dataSourceOM, String observationIdBase, Automatic configuration) throws CstlServiceException {
        switch (type) {
            case DEFAULT : return new DefaultObservationReader(dataSourceOM, observationIdBase);

            case GENERIC : return new DefaultGenericObservationReader(observationIdBase, configuration);

            default : throw new IllegalArgumentException("Unknow O&M dataSource type: " + type);
        }
    }

    @Override
    public ObservationWriter getObservationWriter(DataSource dataSourceOM) throws CstlServiceException {
        return new DefaultObservationWriter(dataSourceOM);
    }

    @Override
    public SensorReader getSensorReader(DataSourceType type, File dataDirectory, String sensorIdBase, Connection connection, Properties map) throws CstlServiceException {
        switch (type) {
            case FILE_SYSTEM: return new FileSensorReader(dataDirectory);

            case MDWEB: return new MDWebSensorReader(connection, sensorIdBase, map);

            default: throw new IllegalArgumentException("Unknow SML dataSource type: " + type);
        }
    }

    @Override
    public SensorWriter getSensorWriter(DataSourceType type,  File dataDirectory, Connection connection, String sensorIdBase) throws CstlServiceException {
        switch (type) {
            case FILE_SYSTEM: return new FileSensorWriter(dataDirectory, sensorIdBase);

            case MDWEB: return new MDWebSensorWriter(connection, sensorIdBase);

            default: throw new IllegalArgumentException("Unknow SML dataSource type: " + type);
        }
    }

}
