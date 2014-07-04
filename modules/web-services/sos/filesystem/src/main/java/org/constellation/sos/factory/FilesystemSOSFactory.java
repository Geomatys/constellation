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

package org.constellation.sos.factory;

import org.constellation.configuration.DataSourceType;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.constellation.sos.io.filesystem.FileSensorReader;
import org.constellation.sos.io.filesystem.FileSensorWriter;

import java.util.Map;

import static org.constellation.configuration.DataSourceType.FILESYSTEM;

/**
  * A FileSystem implementation of the SOS SensorML factory.
 * it provide reader / writer for SensorML datasource.
 *
 * @since 0.8
 * @author Guilhem Legal (Geomatys)
 */
public class FilesystemSOSFactory implements SMLFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean factoryMatchType(DataSourceType type) {
        if (type.equals(FILESYSTEM)) {
            return true;
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SensorReader getSensorReader(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws MetadataIoException {
        return new FileSensorReader(configuration, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SensorWriter getSensorWriter(DataSourceType type,  Automatic configuration, Map<String, Object> properties) throws MetadataIoException {
        return new FileSensorWriter(configuration, properties);
    }

}
