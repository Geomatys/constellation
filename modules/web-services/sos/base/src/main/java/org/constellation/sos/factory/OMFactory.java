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
import org.geotoolkit.observation.ObservationFilter;
import org.geotoolkit.observation.ObservationReader;
import org.geotoolkit.observation.ObservationWriter;

import java.util.Map;

/**
 * Factory used to load various implementation of observation reader/writer/filter.
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface OMFactory {

    public static final String OBSERVATION_ID_BASE = "observationIdBase";

    public static final String OBSERVATION_TEMPLATE_ID_BASE = "observationTemplateIdBase";

    public static final String SENSOR_ID_BASE = "sensorIdBase";

    public static final String PHENOMENON_ID_BASE = "phenomenonIdBase";

    public static final String ALWAYS_FEATURE_COLLECTION = "alwaysFeatureCollection";

    /**
     * Return true if the factory can return an implementation for the specified type.
     * @param type
     */
    boolean factoryMatchType(DataSourceType type);
    
    /**
     * Return an observation filter for  the specified datasource.
     *
     * @param type The type of the filter requested.
     * @param configuration A configuration object containing all the information to use the datasource.
     * @param properties The associated parameters (observation base identifier, template base identifiers,....)
     *
     * @return An observation filter for  the specified datasource.
     * @throws DataStoreException
     */
    ObservationFilter getObservationFilter(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws DataStoreException;

    /**
     * Clone an observation filter.
     *
     * @param omFilter The observation filter to clone.
     * 
     * @return a clone of the specified Observation filter.
     * @throws DataStoreException
     */
    ObservationFilter cloneObservationFilter(ObservationFilter omFilter) throws DataStoreException;

    /**
     * Return an Observation reader for the specified datasource.
     * 
     * @param type  The type of the reader requested.
     * @param configuration A configuration object containing all the information to use the datasource.
     * @param properties The associated parameters (observation base identifier, template base identifiers,....)
     *
     * @return An Observation reader for the specified datasource.
     * @throws DataStoreException
     */
    ObservationReader getObservationReader(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws DataStoreException;

    /**
     * Return an Observation writer for the specified datasource.
     *
     * @param type The type of the writer requested.
     * @param configuration A configuration object containing all the information to use the datasource.
     * @param properties The associated parameters (observation base identifier, template base identifiers,....)
     * 
     * @return an Observation writer for the specified datasource.
     * @throws DataStoreException
     */
    ObservationWriter getObservationWriter(DataSourceType type,  Automatic configuration, Map<String, Object> properties) throws DataStoreException;

}
