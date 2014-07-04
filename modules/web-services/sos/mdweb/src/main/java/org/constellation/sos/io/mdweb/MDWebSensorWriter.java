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

package org.constellation.sos.io.mdweb;

import org.apache.sis.internal.jaxb.LegacyNamespaces;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.io.MDWebMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.factory.OMFactory;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.sql.AbstractReader;
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.RecordSet.EXPOSURE;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// JAXB dependencies
// Constellation dependencies
// MDWeb dependencies

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
     * An SQL statement finding the last sensor ID recorded
     */
    private final PreparedStatement newSensorIdStmt;

    public MDWebSensorWriter(final Automatic configuration, final Map<String, Object> properties) throws MetadataIoException {
        super(configuration);
        final String sensorIdBase = (String) properties.get(OMFactory.SENSOR_ID_BASE);
        final BDD db = configuration.getBdd();
        try {
            final DataSource ds = db.getPooledDataSource();
            smlConnection       = ds.getConnection();
             //we build the prepared Statement
            final String version = ((AbstractReader)mdWriter).getVersion();
            if ("2.0".equals(version)) {
                newSensorIdStmt    = smlConnection.prepareStatement("SELECT Count(*) FROM \"Storage\".\"Forms\" WHERE \"title\" LIKE '%" + sensorIdBase + "%' ");
            } else if (version.startsWith("2.1") || version.startsWith("2.2") || version.startsWith("2.3") || version.startsWith("2.4")) {
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
    public RecordSet getRecordSet(final String recordSet) throws MD_IOException {
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
    public boolean writeSensor(final String id, final AbstractSensorML process) throws CstlServiceException {

        try {
            final Node n = marshallProcess(process);
            return super.storeMetadata(n, id);

        } catch (MetadataIoException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    private Node marshallProcess(final AbstractSensorML process) throws MetadataIoException {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.newDocument();
            Marshaller marshaller = SOSMarshallerPool.getInstance().acquireMarshaller();
            marshaller.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, false);
            marshaller.marshal(process, document);
            SOSMarshallerPool.getInstance().recycle(marshaller);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | JAXBException ex) {
            throw new MetadataIoException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int replaceSensor(final String sensorid, final AbstractSensorML process) throws CstlServiceException {
        final boolean deleted = deleteSensor(sensorid);
        int result;
        if (deleted) {
            result = 1;//REPLACED;
        } else {
            result = 0; //INSERTED;
        }
        writeSensor(sensorid, process);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteSensor(final String sensorId) throws CstlServiceException {
        try {
            return super.deleteMetadata(sensorId);
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
            final Collection<String> allIdentifier = mdWriter.getAllIdentifiers(Arrays.asList(smlCat), false);

            for (String identifier : allIdentifier) {
                LOGGER.log(Level.FINER, "sensor id: {0}", identifier);
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
            if (currentSavePoint != null) {
                smlConnection.rollback(currentSavePoint);
            }
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
            if (currentSavePoint != null) {
                smlConnection.releaseSavepoint(currentSavePoint);
            }
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
            smlConnection.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while closing MDW sensor Writer:{0}", ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation MDweb Sensor Writer 0.9";
    }

}
