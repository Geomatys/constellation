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

package org.constellation.sos.io.filesystem;

// J2SE dependencies

import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.io.SensorReader;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import org.geotoolkit.util.StringUtilities;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.sos.ws.SOSConstants.SENSORML_100_FORMAT_V100;
import static org.constellation.sos.ws.SOSConstants.SENSORML_100_FORMAT_V200;
import static org.constellation.sos.ws.SOSConstants.SENSORML_101_FORMAT_V100;
import static org.constellation.sos.ws.SOSConstants.SENSORML_101_FORMAT_V200;
import org.constellation.sos.ws.SOSUtils;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import org.geotoolkit.sml.xml.SensorMLUtilities;

// Constellation dependencies
// Geotoolkit dependendies

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
    public Collection<String> getSensorNames(String sensorTypeFilter) throws CstlServiceException {
        List<String> names = getSensorNames();
        if (sensorTypeFilter == null || sensorTypeFilter.isEmpty()) {
            return names;
        }
        List<String> results = new ArrayList<>();
        for (String name : names) {
            AbstractSensorML sml = getSensor(name);
            String type = SensorMLUtilities.getSensorMLType(sml);
            if (type.equals(sensorTypeFilter)) {
                results.add(name);
            }
        }
        return results;
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
