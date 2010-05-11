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

import java.util.Properties;
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
 *
 * @author Guilhem Legal
 */
public abstract class AbstractSOSFactory extends Factory {

    public abstract ObservationFilter getObservationFilter(ObservationFilterType type, String observationIdBase, String observationTemplateIdBase, Properties map, Automatic configuration) throws CstlServiceException;

    public abstract ObservationFilter cloneObservationFilter(ObservationFilter omFilter) throws CstlServiceException;
    
    public abstract ObservationReader getObservationReader(ObservationReaderType type, Automatic configuration, String observationIdBase) throws CstlServiceException;

    public abstract ObservationWriter getObservationWriter(ObservationWriterType type, String observationTemplateIdBase, Automatic configuration) throws CstlServiceException;

    public abstract SensorReader getSensorReader(DataSourceType type, Automatic configuration, String sensorIdBase, Properties map) throws MetadataIoException;

    public abstract SensorWriter getSensorWriter(DataSourceType type, Automatic configuration, String sensorIdBase, Properties map) throws MetadataIoException;

}
