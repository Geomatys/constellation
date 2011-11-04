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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

// Geotoolkit dependendies
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.util.logging.Logging;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

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
    
    public FileSensorReader(final Automatic configuration) throws MetadataIoException  {
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
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object unmarshalled = unmarshaller.unmarshal(sensorFile);
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
            } finally {
                if (unmarshaller != null) {
                    MARSHALLER_POOL.release(unmarshaller);
                }
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
        return "Constellation Filesystem Sensor Reader 0.8";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSensorNames() throws CstlServiceException {
        final List<String> result = new ArrayList<String>();
        if (dataDirectory.isDirectory()) {
            for (File sensorFile : dataDirectory.listFiles()) {
                String sensorID = sensorFile.getName();
                final int suffixPos = sensorID.indexOf(".xml");
                if (suffixPos != -1){
                    sensorID = sensorID.substring(0, suffixPos);
                    result.add(sensorID);
                }
            }
        }
        return result;
    }
}
