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

package org.constellation.sos.io.mdweb;

import java.util.Arrays;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

// JAXB dependencies
import java.util.logging.Level;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.io.AbstractMetadataReader;
import org.constellation.metadata.io.MDWebMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.factory.OMFactory;
import org.constellation.sos.io.SensorReader;
import org.constellation.ws.CstlServiceException;
import org.mdweb.io.MD_IOException;

import org.geotoolkit.sml.xml.AbstractSensorML;
import org.mdweb.io.sql.AbstractReader;
import org.mdweb.model.storage.RecordSet;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebSensorReader extends MDWebMetadataReader implements SensorReader {

    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */
    private final Properties map;
    
    /**
     * Build a new Sensor reader for a MDweb database.
     * 
     * @param configuration
     * @param map
     * @throws org.constellation.ws.CstlServiceException
     */
    public MDWebSensorReader(final Automatic configuration, final Map<String, Object> properties) throws MetadataIoException  {
        super(configuration);
        if (configuration == null) {
            throw new MetadataIoException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new MetadataIoException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        // pre warm up
        try {
            ((AbstractReader)mdReader).getAllClasses();
        } catch (MD_IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new MetadataIoException("the service has throw a MD_IO Exception:" + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
        }
        this.map = (Properties) properties.get(OMFactory.IDENTIFIER_MAPPING);
    }

    /**
     * Return the specified sensor description from the specified ID.
     *
     * @param sensorID The identifier of the sensor.
     *
     * @return the specified sensor description from the specified ID.
     * 
     * @throws CstlServiceException If the sensor is not registered in the database,
     *                              if the specified record in the database is not a sensorML object,
     *                              or if an IO Exception occurs.
     */
    @Override
    public AbstractSensorML getSensor(final String sensorId) throws CstlServiceException {
        try {
            String dbId = map.getProperty(sensorId);
            if (dbId == null) {
                dbId = sensorId;
            }
            final Object metadata   = getMetadata(dbId, AbstractMetadataReader.SENSORML);
            
            if (metadata instanceof AbstractSensorML) {
               return (AbstractSensorML) metadata;
            
            } else if (metadata == null) {
                throw new CstlServiceException("this sensor is not registered in the database (id:" + sensorId + ")!",
                        INVALID_PARAMETER_VALUE, "procedure");
            } else {
                throw new CstlServiceException("The form metadata is not a sensor", NO_APPLICABLE_CODE);
            }

        } catch (MetadataIoException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("the service has throw a Metadata IO Exception:" + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation MDweb Sensor Reader 0.8";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSensorNames() throws CstlServiceException {
        try {
            final RecordSet smlCat = mdReader.getRecordSet("SMLC");
            if (smlCat != null) {
                return mdReader.getAllIdentifiers(Arrays.asList(smlCat), false);
            }
            return new ArrayList<String>();
        } catch (MD_IOException ex) {
            throw new CstlServiceException(ex);
        }
    }
}
