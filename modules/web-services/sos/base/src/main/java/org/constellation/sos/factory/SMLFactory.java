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

import java.util.Map;

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
