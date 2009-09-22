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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;

// JAXB dependencies
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// MDWeb dependencies
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.users.User;
import org.mdweb.io.sql.v20.Reader20;
import org.mdweb.io.sql.v20.Writer20;
import org.mdweb.xml.MalFormedDocumentException;
import org.mdweb.xml.Reader;

/**
 *
 * @author Guilhem Legal (geomatys)
 */
public class MDWebSensorWriter implements SensorWriter {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.sos");

    private static final String SQL_ERROR_MSG = "The service has throw a SQL Exception:";
    /**
     * A Writer to the SensorML database.
     */
    private final Writer20 sensorMLWriter;

    /**
     * the data catalog for SensorML database.
     */
    private final Catalog sensorMLCatalog;

    /**
     * The user who owe the form.
     */
    private final User mainUser;
    /**
     * A Reader to the SensorML database.
     */
    private final Reader20 sensorMLReader;

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
     * A JAXB marshaller pool used to provide xml to the XMLReader.
     */
    private MarshallerPool marshallerPool;

    public MDWebSensorWriter(final Automatic configuration, final String sensorIdBase) throws CstlServiceException {
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            smlConnection   = db.getConnection();
            final boolean isPostgres = db.getClassName().equals("org.postgresql.Driver");
            sensorMLWriter  = new Writer20(smlConnection, isPostgres);
            sensorMLReader  = new Reader20(Standard.SENSORML, smlConnection, isPostgres);
            sensorMLCatalog = sensorMLReader.getCatalog("SMLC");
            mainUser        = sensorMLReader.getUser("admin");

             //we build the prepared Statement
            newSensorIdStmt    = smlConnection.prepareStatement("SELECT Count(*) FROM \"Storage\".\"Forms\" WHERE \"title\" LIKE '%" + sensorIdBase + "%' ");

            //we initialize the marshaller
            marshallerPool = new MarshallerPool("http://www.opengis.net/sensorML/1.0", "org.geotoolkit.sml.xml.v100:org.geotoolkit.sml.xml.v101");

        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("JAXBException while starting the MDweb Sensor writer", NO_APPLICABLE_CODE);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("SQ1LException while starting the MDweb Sensor writer: " + "\n" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }

    @Override
     public void writeSensor(String id, AbstractSensorML process) throws CstlServiceException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();

            //we create a new Tempory File SensorML
            final File sensorFile = File.createTempFile("sml", "xml");
            marshaller.marshal(process, sensorFile);

            //we parse the temporay xmlFile
            final Reader xmlReader = new Reader(sensorMLReader, sensorFile, sensorMLWriter);

            //and we write it in the sensorML Database

            final Form f = xmlReader.readForm(sensorMLCatalog, mainUser, "source", id, Standard.SENSORML);
            sensorMLWriter.writeForm(f, false, true);

        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("The service has throw a ParserException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        } catch (SAXException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("The service has throw a SAXException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        } catch (MalFormedDocumentException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            LOGGER.severe("MalFormedDocumentException:" + ex.getMessage());
            throw new CstlServiceException("The SensorML Document is Malformed:" + ex.getMessage(),
                                          INVALID_PARAMETER_VALUE, "sensorDescription");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("The service cannot build the temporary file",
                                          NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("the service has throw an IOException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("the service has throw an JAXBException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
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
        try {
            newSensorIdStmt.close();
            sensorMLWriter.dispose();
        } catch (SQLException ex) {
            LOGGER.severe("SQLException while closing SOSWorker");
        }
    }

    @Override
    public String getInfos() {
        return "Constellation MDweb Sensor Writer 0.4";
    }

}
