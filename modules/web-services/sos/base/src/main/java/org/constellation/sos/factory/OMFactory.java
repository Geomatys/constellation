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
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.ws.CstlServiceException;

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

    public static final String IDENTIFIER_MAPPING = "identifierMapping";
    
    public static final String ALWAYS_FEATURE_COLLECTION = "alwaysFeatureCollection";

    /**
     * Return true if the factory can return an implementation for the specified type.
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
     * @throws CstlServiceException
     */
    ObservationFilter getObservationFilter(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws CstlServiceException;

    /**
     * Clone an observation filter.
     *
     * @param omFilter The observation filter to clone.
     * 
     * @return a clone of the specified Observation filter.
     * @throws CstlServiceException
     */
    ObservationFilter cloneObservationFilter(ObservationFilter omFilter) throws CstlServiceException;

    /**
     * Return an Observation reader for the specified datasource.
     * 
     * @param type  The type of the reader requested.
     * @param configuration A configuration object containing all the information to use the datasource.
     * @param properties The associated parameters (observation base identifier, template base identifiers,....)
     *
     * @return An Observation reader for the specified datasource.
     * @throws CstlServiceException
     */
    ObservationReader getObservationReader(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws CstlServiceException;

    /**
     * Return an Observation writer for the specified datasource.
     *
     * @param type The type of the writer requested.
     * @param configuration A configuration object containing all the information to use the datasource.
     * @param properties The associated parameters (observation base identifier, template base identifiers,....)
     * 
     * @return an Observation writer for the specified datasource.
     * @throws CstlServiceException
     */
    ObservationWriter getObservationWriter(DataSourceType type,  Automatic configuration, Map<String, Object> properties) throws CstlServiceException;

}
