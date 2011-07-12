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

import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.CstlServiceException;
import org.constellation.sos.factory.SMLFactory;

import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 * A sensorML Writer working on a fileSystem.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileSensorWriter implements SensorWriter {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos");

    /**
     * A JAXB unmarshaller used to unmarshall the xml generated by the XMLWriter.
     */
    private final MarshallerPool marshallerPool;

    /**
     * The directory where the data file are stored
     */
    private final File dataDirectory;

    /**
     * Contains the files written during a transaction.
     * If the transaction is aborted, all these files will be deleted.
     */
    private List<File> uncommittedFiles;

    /**
     * The base identifier of all the sensor.
     */
    private final String sensorIdBase;

    public FileSensorWriter(final Automatic configuration,  final Map<String, Object> properties) throws MetadataIoException {
        if (configuration == null) {
            throw new MetadataIoException("The sensor configuration object is null", NO_APPLICABLE_CODE);
        }
        this.sensorIdBase = (String) properties.get(SMLFactory.SENSOR_ID_BASE);
        uncommittedFiles = new ArrayList<File>();
        if (configuration.getDataDirectory() == null) {
            throw new MetadataIoException("The sensor data directory is null", NO_APPLICABLE_CODE);
        }
        this.dataDirectory  = configuration.getDataDirectory();
        this.marshallerPool =  SensorMLMarshallerPool.getInstance();
        if (marshallerPool == null) {
            throw new MetadataIoException("Unable to initialize the fileSensorWriter JAXB context", NO_APPLICABLE_CODE);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeSensor(String id, final AbstractSensorML sensor) throws CstlServiceException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            id = id.replace(":", "-");
            final File currentFile = new File(dataDirectory, id + ".xml");
            if (!currentFile.exists()) {
                final boolean create  = currentFile.createNewFile();
                if (!create) {
                    throw new CstlServiceException("the service was unable to create a new file:" + currentFile.getName(), NO_APPLICABLE_CODE);
                }
            } else {
                LOGGER.log(Level.WARNING, "we overwrite the file: {0}", currentFile.getPath());
            }
            marshaller.marshal(sensor, currentFile);
        } catch (JAXBException ex) {
            String msg = ex.getMessage();
            if (msg == null && ex.getCause() != null) {
                msg = ex.getCause().getMessage();
            }
            throw new CstlServiceException("the service has throw a JAXB Exception:" + msg,
                                           ex, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            String msg = ex.getMessage();
            if (msg == null && ex.getCause() != null) {
                msg = ex.getCause().getMessage();
            }
            throw new CstlServiceException("the service has throw a IO Exception:" + msg,
                                           ex, NO_APPLICABLE_CODE);
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteSensor(String id) throws CstlServiceException {

        id = id.replace(":", "-");
        final File currentFile = new File(dataDirectory, id + ".xml");
        boolean delete = false;
        if (currentFile.exists()) {
            delete = currentFile.delete();
        }
        if (!delete) {
            throw new CstlServiceException("the service was unable to delete the file:" + currentFile.getName(), NO_APPLICABLE_CODE);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int replaceSensor(String id, final AbstractSensorML sensor) throws CstlServiceException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            id = id.replace(":", "-");
            final File currentFile = new File(dataDirectory, id + ".xml");
            marshaller.marshal(sensor, currentFile);
            return 1;//AbstractMetadataWriter.REPLACED;
        } catch (JAXBException ex) {
            String msg = ex.getMessage();
            if (msg == null && ex.getCause() != null) {
                msg = ex.getCause().getMessage();
            }
            throw new CstlServiceException("the service has throw a JAXB Exception:" + msg,
                                           ex, NO_APPLICABLE_CODE);
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startTransaction() throws CstlServiceException {
        uncommittedFiles = new ArrayList<File>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abortTransaction() throws CstlServiceException {
        for (File f: uncommittedFiles) {
            final boolean delete = f.delete();
            if (!delete) {
                LOGGER.log(Level.WARNING, "unable to delete the file:{0}", f.getName());
            }
        }
        uncommittedFiles = new ArrayList<File>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endTransaction() throws CstlServiceException {
        uncommittedFiles = new ArrayList<File>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNewSensorId() throws CstlServiceException {
        int maxID = 0;
        if (dataDirectory != null) {
            for (File f : dataDirectory.listFiles()) {
                String id = f.getName();
                id = id.substring(0, id.indexOf(".xml"));
                id = id.substring(id.indexOf(sensorIdBase) + sensorIdBase.length());
                try {
                    final int curentID = Integer.parseInt(id);
                    if (curentID > maxID) {
                        maxID = curentID;
                    }
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException("unable to parse the identifier:" + id, ex, NO_APPLICABLE_CODE);
                }
            }
        }
        return maxID + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        if (uncommittedFiles != null)
            uncommittedFiles.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Filesystem Sensor Writer 0.7";
    }
}
