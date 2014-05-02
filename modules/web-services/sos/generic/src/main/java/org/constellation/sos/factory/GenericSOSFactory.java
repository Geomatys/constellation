/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2011, Geomatys
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

import java.util.Map;
import org.apache.sis.storage.DataStoreException;
import org.constellation.configuration.DataSourceType;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.io.generic.DefaultGenericObservationReader;
import org.constellation.sos.io.generic.GenericObservationFilter;
import org.geotoolkit.observation.ObservationFilter;
import org.geotoolkit.observation.ObservationReader;
import org.geotoolkit.observation.ObservationWriter;

import static org.constellation.configuration.DataSourceType.*;

/**
  * A Generic implementation of the SOS O&M factory.
 * it provide reader / writer / filter for observations datasource.
 *
 * @since 0.8
 * @author Guilhem Legal (Geomatys)
 */
public class GenericSOSFactory implements OMFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean factoryMatchType(DataSourceType type) {
        if (type.equals(GENERIC)) {
            return true;
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationFilter getObservationFilter(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws DataStoreException {
        return new GenericObservationFilter(configuration, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationFilter cloneObservationFilter(ObservationFilter omFilter) throws DataStoreException {
        if (omFilter instanceof GenericObservationFilter) {
            return new GenericObservationFilter((GenericObservationFilter) omFilter);
        } else {
            throw new IllegalArgumentException("Unknow observationFilter type: " + omFilter);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationReader getObservationReader(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws DataStoreException {
        try {
            return new DefaultGenericObservationReader(configuration, properties);
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationWriter getObservationWriter(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws DataStoreException {
        return null;
    }

}
