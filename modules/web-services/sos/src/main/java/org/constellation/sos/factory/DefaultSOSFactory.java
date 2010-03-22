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
import org.constellation.metadata.io.MetadataIoException;
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
        super();
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
    public ObservationFilter cloneObservationFilter(ObservationFilter omFilter) throws CstlServiceException {
        if (omFilter instanceof DefaultObservationFilter) {
            return new DefaultObservationFilter((DefaultObservationFilter)omFilter);

        } else if (omFilter instanceof LuceneObservationFilter) {
            return new LuceneObservationFilter((LuceneObservationFilter) omFilter);
        
        } else if (omFilter instanceof GenericObservationFilter) {
            return new GenericObservationFilter((GenericObservationFilter) omFilter);

        } else {
            throw new IllegalArgumentException("Unknow observationFilter type: " + omFilter);
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
    public ObservationWriter getObservationWriter(ObservationWriterType type, String observationTemplateIdBase, Automatic configuration) throws CstlServiceException {
        if (type == null) {
            return null;
        }
        switch (type) {
            case DEFAULT   : return new DefaultObservationWriter(configuration);
            
            case FILESYSTEM: return new FileObservationWriter(configuration, observationTemplateIdBase);
            
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

            case MDWEB:
                try {
                    return new MDWebSensorReader(configuration, map);
                } catch (MetadataIoException ex) {
                    throw new CstlServiceException(ex);
                }

            default: throw new IllegalArgumentException("Unknow SML dataSource type: " + type);
        }
    }

    @Override
    public SensorWriter getSensorWriter(DataSourceType type,  Automatic configuration, String sensorIdBase, Properties map) throws CstlServiceException {
        if (type == null) {
            return null;
        }
        switch (type) {
            case FILE_SYSTEM: return new FileSensorWriter(configuration, sensorIdBase);

            case MDWEB:
                try {
                    return new MDWebSensorWriter(configuration, sensorIdBase, map);
                } catch (MetadataIoException ex) {
                    throw new CstlServiceException(ex);
                }

            default: throw new IllegalArgumentException("Unknow SML dataSource type: " + type);
        }
    }

}
