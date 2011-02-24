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
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.io.MDWebMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.factory.AbstractSOSFactory;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.sml.xml.AbstractSensorML;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// MDWeb dependencies
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.RecordSet.EXPOSURE;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.sql.AbstractReader;
import org.mdweb.io.sql.v20.Writer20;

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

    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */
    private final Properties map;
    
    public MDWebSensorWriter(final Automatic configuration, final Map<String, Object> properties) throws MetadataIoException {
        super(configuration);
        final String sensorIdBase = (String) properties.get(AbstractSOSFactory.SENSOR_ID_BASE);
        final BDD db = configuration.getBdd();
        try {
            smlConnection   = db.getConnection();
            this.map        = (Properties) properties.get(AbstractSOSFactory.IDENTIFIER_MAPPING);
             //we build the prepared Statement
            final String version = ((AbstractReader)mdWriter).getVersion();
            if ("2.0".equals(version)) {
                newSensorIdStmt    = smlConnection.prepareStatement("SELECT Count(*) FROM \"Storage\".\"Forms\" WHERE \"title\" LIKE '%" + sensorIdBase + "%' ");
            } else if (version.startsWith("2.1") || version.startsWith("2.2")) {
                newSensorIdStmt    = smlConnection.prepareStatement("SELECT Count(*) FROM \"Storage\".\"Records\" WHERE \"title\" LIKE '%" + sensorIdBase + "%' ");
            } else {
                throw new IllegalArgumentException("unexpected MDWeb database version:" + version);
            }

            // we enbale the fast storage mode
            mdWriter.setProperty("fastStorage", true);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new MetadataIoException("SQLException while starting the MDweb Sensor writer: " + "\n" + ex.getMessage(), NO_APPLICABLE_CODE);
        } 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecordSet getRecordSet(String recordSet) throws MD_IOException {
        RecordSet cat = mdWriter.getRecordSet("SMLC");
        if (cat == null) {
            cat = new RecordSet("SMLC", "SensorML RecordSet", null, null, EXPOSURE.EXTERNAL, 0, new Date(System.currentTimeMillis()), true);
            mdWriter.writeRecordSet(cat);
        }
        return cat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeSensor(String id, AbstractSensorML process) throws CstlServiceException {
       
        try {
            return super.storeMetadata(process, id);

        } catch (MetadataIoException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int replaceSensor(String sensorid, AbstractSensorML process) throws CstlServiceException {
        final boolean deleted = deleteSensor(sensorid);
        int result;
        if (deleted) {
            result = REPLACED;
        } else {
            result = INSERTED;
        }
        writeSensor(sensorid, process);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteSensor(String sensorId) throws CstlServiceException {
        try {
            String dbId = map.getProperty(sensorId);
            if (dbId == null) {
                dbId = sensorId;
            }
            // we find the form id describing the sensor.
            final int id = ((Writer20)mdWriter).getIdFromTitleForm(dbId);
            LOGGER.log(Level.FINER, "describesensor id: {0}", dbId);
            LOGGER.log(Level.FINER, "describesensor mdweb id: {0}", id);

            return super.deleteMetadata(id + ":SMLC");

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
     * This method shouldn't be called. It is used in a subProject in order to clear the database.
     *
     * @return
     * @throws CstlServiceException
     */
    public void deleteAllSensor() throws CstlServiceException {
        try {
            
            final RecordSet smlCat      = mdWriter.getRecordSet("SMLC");
            final List<String> allTitle = mdWriter.getFormsTitle(smlCat);

            for (String title : allTitle) {
                // we find the form id describing the sensor.
                final int id = ((Writer20)mdWriter).getIdFromTitleForm(title);
                LOGGER.log(Level.FINER, "describesensor id: {0}", title);
                LOGGER.log(Level.FINER, "describesensor mdweb id: {0}", id);

                super.deleteMetadata(id + ":SMLC");
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();
        try {
            newSensorIdStmt.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while closing MDW sensor Writer:{0}", ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation MDweb Sensor Writer 0.7";
    }

}
