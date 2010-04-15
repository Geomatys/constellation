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

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;

// JAXB dependencies
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.io.MDWebMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.CstlServiceException;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.sql.v20.Writer20;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// MDWeb dependencies
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.RecordSet.EXPOSURE;

/**
 *
 * @author Guilhem Legal (geomatys)
 */
public class MDWebSensorWriter extends MDWebMetadataWriter implements SensorWriter {

    private static final String SQL_ERROR_MSG = "The service has throw a SQL Exception:";

    /**
     * A connection to the MDWeb database.
     */
    private final Connection smlConnection;

    private Savepoint currentSavePoint;

    /**
     * An SQL satetement finding the last sensor ID recorded
     */
    private final PreparedStatement newSensorIdStmt;

    private static String currentSensorID;

    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */
    private final Properties map;
    
    public MDWebSensorWriter(final Automatic configuration, final String sensorIdBase, final Properties map) throws MetadataIoException {
        super(configuration);

        final BDD db = configuration.getBdd();
        try {
            smlConnection   = db.getConnection();
            this.map        = map;
             //we build the prepared Statement
            newSensorIdStmt    = smlConnection.prepareStatement("SELECT Count(*) FROM \"Storage\".\"Forms\" WHERE \"title\" LIKE '%" + sensorIdBase + "%' ");

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new MetadataIoException("SQLException while starting the MDweb Sensor writer: " + "\n" + ex.getMessage(), NO_APPLICABLE_CODE);
        } 
    }

    @Override
    public RecordSet getRecordSet(String recordSet) throws MD_IOException {
        RecordSet cat = mdWriter.getRecordSet("SMLC");
        if (cat == null) {
            cat = new RecordSet("SMLC", "SensorML RecordSet", null, null, EXPOSURE.EXTERNAL, 0, new Date(System.currentTimeMillis()), false);
            mdWriter.writeRecordSet(cat);
        }
        return cat;
    }

    @Override
    public boolean writeSensor(String id, AbstractSensorML process) throws CstlServiceException {
       
        try {
            currentSensorID = id;
            return super.storeMetadata(process);

        } catch (MetadataIoException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    @Override
    public int replaceSensor(String sensorid, AbstractSensorML process) throws CstlServiceException {
        boolean deleted = deleteSensor(sensorid);
        int result;
        if (deleted) {
            result = REPLACED;
        } else {
            result = INSERTED;
        }
        writeSensor(sensorid, process);
        return result;
    }

    @Override
    public boolean deleteSensor(String sensorId) throws CstlServiceException {
        try {
            String dbId = map.getProperty(sensorId);
            if (dbId == null) {
                dbId = sensorId;
            }
            // we find the form id describing the sensor.
            final int id = ((Writer20)mdWriter).getIdFromTitleForm(dbId);
            LOGGER.finer("describesensor id: " + dbId);
            LOGGER.finer("describesensor mdweb id: " + id);

            String identifier = id + ":SMLC";
            return super.deleteMetadata(identifier);

        } catch (MD_IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("the service has throw a MD_IO Exception:" + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
        } catch (MetadataIoException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("the service has throw a Metadata IO Exception:" + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
        }
        
    }

    /**
     * This method should be called. It is used in a subProject in order to clear the database.
     *
     * @return
     * @throws CstlServiceException
     */
    public void deleteAllSensor() throws CstlServiceException {
        try {
            
            RecordSet smlCat      = mdWriter.getRecordSet("SMLC");
            List<String> allTitle = mdWriter.getFormsTitle(smlCat);

            for (String title : allTitle) {
                // we find the form id describing the sensor.
                final int id = ((Writer20)mdWriter).getIdFromTitleForm(title);
                LOGGER.finer("describesensor id: " + title);
                LOGGER.finer("describesensor mdweb id: " + id);

                String identifier = id + ":SMLC";
                super.deleteMetadata(identifier);
            }

        } catch (MD_IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("the service has throw a MD_IO Exception:" + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
        } catch (MetadataIoException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("the service has throw a Metadata IO Exception:" + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
        }

    }

    @Override
    protected String findTitle(Object obj) {
        return currentSensorID;
    }

    @Override
    public void startTransaction() throws CstlServiceException {
        try {
            smlConnection.setAutoCommit(false);
            currentSavePoint = smlConnection.setSavepoint("registerSensorTransaction");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    @Override
    public void abortTransaction() throws CstlServiceException {
        try {
            if (currentSavePoint != null)
                smlConnection.rollback(currentSavePoint);
            smlConnection.commit();
            smlConnection.setAutoCommit(true);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    @Override
    public void endTransaction() throws CstlServiceException {
        try {
            if (currentSavePoint != null)
                smlConnection.releaseSavepoint(currentSavePoint);
            smlConnection.commit();
            smlConnection.setAutoCommit(true);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    /**
     * Create a new identifier for a sensor.
     */
    @Override
    public int getNewSensorId() throws CstlServiceException {
        try {
            final ResultSet res = newSensorIdStmt.executeQuery();
            int id = -1;
            while (res.next()) {
                id = res.getInt(1);
            }
            res.close();
            return id + 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        try {
            newSensorIdStmt.close();
        } catch (SQLException ex) {
            LOGGER.severe("SQLException while closing SOSWorker");
        }
    }

    @Override
    public String getInfos() {
        return "Constellation MDweb Sensor Writer 0.5";
    }

}
