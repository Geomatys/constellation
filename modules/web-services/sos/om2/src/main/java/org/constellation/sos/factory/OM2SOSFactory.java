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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.DataSourceType;
import static org.constellation.configuration.DataSourceType.OM2;
import org.constellation.generic.database.Automatic;
import org.constellation.om2.OM2DatabaseCreator;
import org.constellation.sos.io.om2.OM2ObservationFilter;
import org.constellation.sos.io.om2.OM2ObservationFilterReader;
import org.constellation.sos.io.om2.OM2ObservationReader;
import org.constellation.sos.io.om2.OM2ObservationWriter;
import org.geotoolkit.observation.ObservationFilter;
import org.geotoolkit.observation.ObservationReader;
import org.geotoolkit.observation.ObservationWriter;

/**
  * A postgrid implementation of the SOS factory.
 * it provide various reader / writer / filter for observations datasource.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class OM2SOSFactory implements OMFactory {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.sos.factory");

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean factoryMatchType(DataSourceType type) {
        if (type.equals(OM2)) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationFilter getObservationFilter(final DataSourceType type, final Automatic configuration, final Map<String, Object> properties) throws DataStoreException {
       return new OM2ObservationFilterReader(configuration, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationFilter cloneObservationFilter(final ObservationFilter omFilter) throws DataStoreException {
       return new OM2ObservationFilterReader((OM2ObservationFilter)omFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationReader getObservationReader(final DataSourceType type, final Automatic configuration, final Map<String, Object> properties) throws DataStoreException {
        return new OM2ObservationReader(configuration, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationWriter getObservationWriter(final DataSourceType type, final Automatic configuration, final Map<String, Object> properties) throws DataStoreException {
        return new OM2ObservationWriter(configuration, properties);
    }

    @Override
    public boolean buildDatasource(Automatic configuration, Map<String, Object> parameters) throws DataStoreException {
        try {
            final DataSource source = configuration.getBdd().getDataSource();
            if (OM2DatabaseCreator.validConnection(source)) {
                if (!OM2DatabaseCreator.structurePresent(source)) {
                    OM2DatabaseCreator.createObservationDatabase(source, true, null);
                    return true;
                } else {
                    LOGGER.info("OM2 structure already present");
                }
                return true;
            } else {
                LOGGER.warning("unable to connect OM datasource");
            }
            return false;
        } catch (SQLException | IOException ex) {
            throw new DataStoreException("Erro while building OM2 datasource", ex);
        }
    }
}
