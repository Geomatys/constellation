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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.constellation.generic.database.Automatic;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileSensorWriter implements SensorWriter {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.sos");

    /**
     * A JAXB unmarshaller used to unmarshall the xml generated by the XMLWriter.
     */
    private MarshallerPool marshallerPool;

    private File dataDirectory;

    private List<File> uncommittedFiles;

    private String sensorIdBase;

    public FileSensorWriter(Automatic configuration, String sensorIdBase) throws CstlServiceException {
        if (configuration == null) {
            throw new CstlServiceException("The sensor configuration object is null", NO_APPLICABLE_CODE);
        }
        this.sensorIdBase = sensorIdBase;
        uncommittedFiles = new ArrayList<File>();
        if (configuration.getDataDirectory() == null) {
            throw new CstlServiceException("The sensor data directory is null", NO_APPLICABLE_CODE);
        }
        this.dataDirectory = configuration.getDataDirectory();
        try {
            marshallerPool = new MarshallerPool("org.geotoolkit.sml.xml.v100:org.geotoolkit.sml.xml.v101");
        } catch (JAXBException ex) {
            throw new CstlServiceException("Unable to initialize the fileSensorWriter JAXB context", ex, NO_APPLICABLE_CODE);
        }

    }

    @Override
    public void writeSensor(String id, AbstractSensorML sensor) throws CstlServiceException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            id = id.replace(":", "-");
            final File currentFile = new File(dataDirectory, id + ".xml");
            final boolean create  = currentFile.createNewFile();
            if (!create) {
                throw new CstlServiceException("the service was unable to create a new file:" + currentFile.getName(), NO_APPLICABLE_CODE);
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
    }

    @Override
    public void startTransaction() throws CstlServiceException {
        uncommittedFiles = new ArrayList<File>();
    }

    @Override
    public void abortTransaction() throws CstlServiceException {
        for (File f: uncommittedFiles) {
            final boolean delete = f.delete();
            if (!delete) {
                LOGGER.severe("unable to delete the file:" + f.getName());
            }
        }
        uncommittedFiles = new ArrayList<File>();
    }

    @Override
    public void endTransaction() throws CstlServiceException {
        uncommittedFiles = new ArrayList<File>();
    }

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

    @Override
    public void destroy() {
        if (uncommittedFiles != null)
            uncommittedFiles.clear();
    }

    @Override
    public String getInfos() {
        return "Constellation Filesystem Sensor Writer 0.5";
    }

}
