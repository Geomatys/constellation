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

package org.constellation.sos.io.filesystem;

// J2SE dependencies
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.io.SensorReader;
import org.constellation.ws.CstlServiceException;

import static org.constellation.sos.ws.SOSConstants.*;

// Geotoolkit dependendies
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.util.logging.Logging;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.util.StringUtilities;

/**
 *
 * @author Guilhem Legal (geomatys)
 */
public class FileSensorReader implements SensorReader {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.sos.io.filesystem");
    
    /**
     * A JAXB unmarshaller used to unmarshall the xml files.
     */
    private static final MarshallerPool MARSHALLER_POOL = SensorMLMarshallerPool.getInstance();

    /**
     * The directory where the data file are stored
     */
    private final File dataDirectory;
    
    private final Map<String, List<String>> acceptedSensorMLFormats = new HashMap<>();
    
    public FileSensorReader(final Automatic configuration, final Map<String, Object> properties) throws MetadataIoException  {
        //we initialize the unmarshaller
        dataDirectory  = configuration.getDataDirectory();
        if (dataDirectory == null) {
            throw new MetadataIoException("The sensor data directory is null", NO_APPLICABLE_CODE);
        } else if (!dataDirectory.exists()) {
            boolean sucess = dataDirectory.mkdir();
            if (!sucess) {
                throw new MetadataIoException("unable to build the directory:" + dataDirectory.getPath(), NO_APPLICABLE_CODE);
            }
        }
        final String smlFormats100 = (String) properties.get("smlFormats100");
        if (smlFormats100 != null) {
            acceptedSensorMLFormats.put("1.0.0", StringUtilities.toStringList(smlFormats100));
        } else {
            acceptedSensorMLFormats.put("1.0.0", Arrays.asList(SENSORML_100_FORMAT_V100,
                                                               SENSORML_101_FORMAT_V100));
        }
        
        final String smlFormats200 = (String) properties.get("smlFormats200");
        if (smlFormats200 != null) {
            acceptedSensorMLFormats.put("2.0.0", StringUtilities.toStringList(smlFormats200));
        } else {
            acceptedSensorMLFormats.put("2.0.0", Arrays.asList(SENSORML_100_FORMAT_V200,
                                                               SENSORML_101_FORMAT_V200));
        }
    }
    
    @Override
    public Map<String, List<String>> getAcceptedSensorMLFormats() {
        return acceptedSensorMLFormats;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractSensorML getSensor(final String sensorId) throws CstlServiceException {
        File sensorFile = new File(dataDirectory, sensorId + ".xml");
        if (!sensorFile.exists()) {
            final String sensorIdTmp = sensorId.replace(":", "-");
            sensorFile = new File(dataDirectory, sensorIdTmp + ".xml");
        }
        if (sensorFile.exists()){
            try {
                final Unmarshaller unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object unmarshalled = unmarshaller.unmarshal(sensorFile);
                MARSHALLER_POOL.recycle(unmarshaller);
                if (unmarshalled instanceof JAXBElement) {
                    unmarshalled = ((JAXBElement) unmarshalled).getValue();
                }
                if (unmarshalled instanceof AbstractSensorML) {
                    return (AbstractSensorML) unmarshalled;
                } else {
                    throw new CstlServiceException("The form unmarshalled is not a sensor", NO_APPLICABLE_CODE);
                }
            } catch (JAXBException ex) {
                throw new CstlServiceException("JAXBException while unmarshalling the sensor", ex, NO_APPLICABLE_CODE);
            }
        } else {
            LOGGER.log(Level.INFO, "the file: {0} does not exist", sensorFile.getPath());
            throw new CstlServiceException("this sensor is not registered in the database:" + sensorId,
                        INVALID_PARAMETER_VALUE, "procedure");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Filesystem Sensor Reader 0.9";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSensorNames() throws CstlServiceException {
        final List<String> result = new ArrayList<>();
        if (dataDirectory.isDirectory()) {
            for (File sensorFile : dataDirectory.listFiles()) {
                String sensorID = sensorFile.getName();
                if (!sensorID.endsWith("~")) {
                    final int suffixPos = sensorID.indexOf(".xml");
                    if (suffixPos != -1){
                        sensorID = sensorID.substring(0, suffixPos);
                        result.add(sensorID);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void removeFromCache(String sensorID) {
        // do nothing no cache
    }

    @Override
    public int getSensorCount() throws CstlServiceException {
        return getSensorNames().size();
    }
}
