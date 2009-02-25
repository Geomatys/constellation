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

import java.util.Properties;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.ObservationFilterType;
import org.constellation.configuration.ObservationReaderType;
import org.constellation.configuration.ObservationWriterType;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.io.generic.DefaultGenericObservationReader;
import org.constellation.sos.io.postgrid.DefaultObservationFilter;
import org.constellation.sos.io.postgrid.DefaultObservationReader;
import org.constellation.sos.io.postgrid.DefaultObservationWriter;
import org.constellation.sos.io.filesystem.FileObservationReader;
import org.constellation.sos.io.filesystem.FileObservationWriter;
import org.constellation.sos.io.filesystem.FileSensorReader;
import org.constellation.sos.io.filesystem.FileSensorWriter;
import org.constellation.sos.io.generic.GenericObservationFilter;
import org.constellation.sos.io.lucene.LuceneObservationFilter;
import org.constellation.sos.io.mdweb.MDWebSensorReader;
import org.constellation.sos.io.mdweb.MDWebSensorWriter;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationReader;
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
    public ObservationFilter getObservationFilter(ObservationFilterType type, String observationIdBase, String observationTemplateIdBase, Properties map, Automatic configuration) throws CstlServiceException {
        if (type == null) {
            return null;
        }
        switch (type) {
            case DEFAULT: return new DefaultObservationFilter(observationIdBase, observationTemplateIdBase, map, configuration);

            case GENERIC: return new GenericObservationFilter(observationIdBase, observationTemplateIdBase, map, configuration);

            case LUCENE : return new LuceneObservationFilter(observationIdBase, observationTemplateIdBase, map, configuration);

            default: throw new IllegalArgumentException("Unknow observationFilter type: " + type);
        }

    }

    @Override
    public ObservationReader getObservationReader(ObservationReaderType type, Automatic configuration, String observationIdBase) throws CstlServiceException {
        if (type == null) {
            return null;
        }
        switch (type) {
            case DEFAULT   : return new DefaultObservationReader(configuration, observationIdBase);

            case GENERIC   : return new DefaultGenericObservationReader(observationIdBase, configuration);

            case FILESYSTEM: return new FileObservationReader(observationIdBase, configuration);

            default : throw new IllegalArgumentException("Unknow O&M dataSource type: " + type);
        }
    }

    @Override
    public ObservationWriter getObservationWriter(ObservationWriterType type, Automatic configuration) throws CstlServiceException {
        if (type == null) {
            return null;
        }
        switch (type) {
            case DEFAULT   : return new DefaultObservationWriter(configuration);
            
            case FILESYSTEM: return new FileObservationWriter(configuration);
            
            default : throw new IllegalArgumentException("Unknow O&M dataSource type: " + type);
        }
    }

    @Override
    public SensorReader getSensorReader(DataSourceType type, Automatic configuration, String sensorIdBase, Properties map) throws CstlServiceException {
        if (type == null) {
            return null;
        }
        switch (type) {
            case FILE_SYSTEM: return new FileSensorReader(configuration);

            case MDWEB: return new MDWebSensorReader(configuration, sensorIdBase, map);

            default: throw new IllegalArgumentException("Unknow SML dataSource type: " + type);
        }
    }

    @Override
    public SensorWriter getSensorWriter(DataSourceType type,  Automatic configuration, String sensorIdBase) throws CstlServiceException {
        if (type == null) {
            return null;
        }
        switch (type) {
            case FILE_SYSTEM: return new FileSensorWriter(configuration, sensorIdBase);

            case MDWEB: return new MDWebSensorWriter(configuration, sensorIdBase);

            default: throw new IllegalArgumentException("Unknow SML dataSource type: " + type);
        }
    }

}
