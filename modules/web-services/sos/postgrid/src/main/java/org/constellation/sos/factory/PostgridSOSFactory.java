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

import java.util.Map;
import org.apache.sis.storage.DataStoreException;
import org.constellation.configuration.DataSourceType;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.io.postgrid.DefaultObservationFilter;
import org.constellation.sos.io.postgrid.DefaultObservationReader;
import org.constellation.sos.io.postgrid.DefaultObservationWriter;
import org.constellation.sos.io.ObservationFilter;
import org.geotoolkit.observation.ObservationReader;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.ws.CstlServiceException;

import static org.constellation.configuration.DataSourceType.*;

/**
  * A postgrid implementation of the SOS factory.
 * it provide various reader / writer / filter for observations datasource.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class PostgridSOSFactory implements OMFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean factoryMatchType(DataSourceType type) {
        if (type.equals(POSTGRID)) {
            return true;
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationFilter getObservationFilter(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws CstlServiceException {
       return new DefaultObservationFilter(configuration, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationFilter cloneObservationFilter(ObservationFilter omFilter) throws CstlServiceException {
       return new DefaultObservationFilter((DefaultObservationFilter)omFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationReader getObservationReader(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws DataStoreException {
        return new DefaultObservationReader(configuration, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationWriter getObservationWriter(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws CstlServiceException {
        return new DefaultObservationWriter(configuration);
    }

}
