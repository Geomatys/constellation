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
import org.constellation.configuration.ObservationFilterType;
import org.constellation.configuration.ObservationReaderType;
import org.constellation.configuration.ObservationWriterType;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.factory.Factory;

/**
 * Factory used to load various implementation of observation reader/writer/filter, and sensor metadata reader/writer.
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractSOSFactory extends Factory {

    public static final String OBSERVATION_ID_BASE = "observationIdBase";

    public static final String OBSERVATION_TEMPLATE_ID_BASE = "observationTemplateIdBase";

    public static final String SENSOR_ID_BASE = "sensorIdBase";

    public static final String PHENOMENON_ID_BASE = "phenomenonIdBase";

    public static final String IDENTIFIER_MAPPING = "identifierMapping";

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
    public abstract ObservationFilter getObservationFilter(ObservationFilterType type, Automatic configuration, Map<String, Object> properties) throws CstlServiceException;

    /**
     * Clone an observation filter.
     *
     * @param omFilter The observation filter to clone.
     * 
     * @return a clone of the specified Observation filter.
     * @throws CstlServiceException
     */
    public abstract ObservationFilter cloneObservationFilter(ObservationFilter omFilter) throws CstlServiceException;

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
    public abstract ObservationReader getObservationReader(ObservationReaderType type, Automatic configuration, Map<String, Object> properties) throws CstlServiceException;

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
    public abstract ObservationWriter getObservationWriter(ObservationWriterType type,  Automatic configuration, Map<String, Object> properties) throws CstlServiceException;

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
    public abstract SensorReader getSensorReader(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws MetadataIoException;

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
    public abstract SensorWriter getSensorWriter(DataSourceType type, Automatic configuration, Map<String, Object> properties) throws MetadataIoException;

}
