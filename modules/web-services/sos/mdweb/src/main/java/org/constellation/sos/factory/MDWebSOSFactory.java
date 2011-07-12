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
import org.constellation.configuration.DataSourceType;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.io.mdweb.MDWebSensorReader;
import org.constellation.sos.io.mdweb.MDWebSensorWriter;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;

import static org.constellation.configuration.DataSourceType.*;

/**
 * A MDweb implementation of the SOS SensorML factory.
 * it provide reader / writer for SensorML MDWeb datasource.
 *
 * @since 0.8
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebSOSFactory implements SMLFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean factoryMatchType(Object type) {
        if (type instanceof DataSourceType && ((DataSourceType)type).equals(MDWEB)) {
            return true;
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SensorReader getSensorReader(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws MetadataIoException {
        return new MDWebSensorReader(configuration, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SensorWriter getSensorWriter(DataSourceType type,  Automatic configuration, Map<String, Object> properties) throws MetadataIoException {
        return new MDWebSensorWriter(configuration, properties);
    }

}
