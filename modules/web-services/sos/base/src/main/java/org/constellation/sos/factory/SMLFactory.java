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
import org.constellation.configuration.DataSourceType;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;

/**
 * Factory used to load various implementation of sensor metadata reader/writer.
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface SMLFactory {

    public static final String OBSERVATION_ID_BASE = "observationIdBase";

    public static final String OBSERVATION_TEMPLATE_ID_BASE = "observationTemplateIdBase";

    public static final String SENSOR_ID_BASE = "sensorIdBase";

    public static final String PHENOMENON_ID_BASE = "phenomenonIdBase";

    public static final String IDENTIFIER_MAPPING = "identifierMapping";

    /**
     * Return true if the factory can return an implementation for the specified type.
     */
    boolean factoryMatchType(DataSourceType type);

    /**
     *  Return a Sensor metadata reader for the specified datasource.
     *
     * @param type The type of the reader requested.
     * @param configuration A configuration object containing all the information to use the datasource.
     * @param properties The associated parameters (observation base identifier, template base identifiers,....)
     *
     * @return a Sensor metadata reader for the specified datasource.
     * @throws MetadataIoException
     */
    SensorReader getSensorReader(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws MetadataIoException;

    /**
     * Return a Sensor metadata writer for the specified datasource.
     *
     * @param type  The type of the writer requested.
     * @param configuration A configuration object containing all the information to use the datasource.
     * @param properties The associated parameters (observation base identifier, template base identifiers,....)
     *
     * @return a Sensor metadata writer for the specified datasource.
     * @throws MetadataIoException
     */
    SensorWriter getSensorWriter(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws MetadataIoException;

}
