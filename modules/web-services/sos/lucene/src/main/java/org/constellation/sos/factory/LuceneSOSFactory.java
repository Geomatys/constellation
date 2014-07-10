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

import org.apache.sis.storage.DataStoreException;
import org.constellation.configuration.DataSourceType;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.io.filesystem.FileObservationReader;
import org.constellation.sos.io.filesystem.FileObservationWriter;
import org.constellation.sos.io.lucene.LuceneObservationFilter;
import org.geotoolkit.observation.ObservationFilter;
import org.geotoolkit.observation.ObservationReader;
import org.geotoolkit.observation.ObservationWriter;

import java.util.Map;

import static org.constellation.configuration.DataSourceType.FILESYSTEM;
import static org.constellation.configuration.DataSourceType.LUCENE;

/**
  * A Lucene implementation of the SOS factory.
 * it provide reader / writer / filter for observations datasource.
 *
 * @since 0.8
 * @author Guilhem Legal (Geomatys)
 */
public class LuceneSOSFactory implements OMFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean factoryMatchType(DataSourceType type) {
        if (type.equals(LUCENE) ||
            type.equals(FILESYSTEM)) {
            return true;
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationFilter getObservationFilter(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws DataStoreException {
        return new LuceneObservationFilter(configuration, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationFilter cloneObservationFilter(ObservationFilter omFilter) throws DataStoreException {
        return new LuceneObservationFilter((LuceneObservationFilter) omFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationReader getObservationReader(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws DataStoreException {
        return new FileObservationReader(configuration, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationWriter getObservationWriter(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws DataStoreException {
        return new FileObservationWriter(configuration, properties);
    }
    
    @Override
    public boolean buildDatasource(Automatic configuration, Map<String, Object> parameters) {
        // do nothing
        return true;
    }
}
