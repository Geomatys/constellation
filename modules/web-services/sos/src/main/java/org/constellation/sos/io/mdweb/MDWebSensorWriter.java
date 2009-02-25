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
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sml.AbstractSensorML;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.NamespacePrefixMapperImpl;
import static org.constellation.ows.OWSExceptionCode.*;

// MDWeb dependencies
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.users.User;
import org.mdweb.sql.v20.Reader20;
import org.mdweb.sql.v20.Writer20;
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
    protected Logger logger = Logger.getLogger("org.constellation.sos");

    /**
     * A Writer to the SensorML database.
     */
    private final Writer20 sensorMLWriter;

    /**
     * the data catalog for SensorML database.
     */
    private final Catalog SMLCatalog;

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
     * A JAXB marshaller used to provide xml to the XMLReader.
     */
    private Marshaller marshaller;

    public MDWebSensorWriter(Automatic configuration, String sensorIdBase) throws CstlServiceException {
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            smlConnection  = db.getConnection();
            sensorMLWriter = new Writer20(smlConnection);
            sensorMLReader = new Reader20(Standard.SENSORML, smlConnection);
            SMLCatalog     = sensorMLReader.getCatalog("SMLC");
            mainUser       = sensorMLReader.getUser("admin");

             //we build the prepared Statement
            newSensorIdStmt    = smlConnection.prepareStatement("SELECT Count(*) FROM \"Forms\" WHERE title LIKE '%" + sensorIdBase + "%' ");

            //we initialize the unmarshaller
            JAXBContext context = JAXBContext.newInstance("org.constellation.sml.v100:org.constellation.sml.v101");
            marshaller        = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            NamespacePrefixMapperImpl prefixMapper = new NamespacePrefixMapperImpl("http://www.opengis.net/sensorML/1.0");
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);

        } catch (JAXBException ex) {
            ex.printStackTrace();
            throw new CstlServiceException("JAXBException while starting the MDweb Senor reader", NO_APPLICABLE_CODE);
        } catch (SQLException ex) {
            throw new CstlServiceException("SQLBException while starting the MDweb Senor reader: " + "\n" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }

     public void writeSensor(String id, AbstractSensorML process) throws CstlServiceException {
        try {

            //we create a new Tempory File SensorML
            File sensorFile = File.createTempFile("sml", "xml");
            marshaller.marshal(process, sensorFile);

            //we parse the temporay xmlFile
            Reader XMLReader = new Reader(sensorMLReader, sensorFile, sensorMLWriter);

            //and we write it in the sensorML Database

            Form f = XMLReader.readForm(SMLCatalog, mainUser, "source", id, Standard.SENSORML);
            sensorMLWriter.writeForm(f, false);

        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
            throw new CstlServiceException("The service has throw a ParserException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        } catch (SAXException ex) {
            ex.printStackTrace();
            throw new CstlServiceException("The service has throw a SAXException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        } catch (MalFormedDocumentException ex) {
            ex.printStackTrace();
            logger.severe("MalFormedDocumentException:" + ex.getMessage());
            throw new CstlServiceException("The SensorML Document is Malformed:" + ex.getMessage(),
                                          INVALID_PARAMETER_VALUE, "sensorDescription");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new CstlServiceException("The service cannot build the temporary file",
                                          NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CstlServiceException("the service has throw an IOException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        } catch (JAXBException ex) {
            ex.printStackTrace();
            throw new CstlServiceException("the service has throw an JAXBException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }

    public void startTransaction() throws CstlServiceException {
        try {
            smlConnection.setAutoCommit(false);
            currentSavePoint = smlConnection.setSavepoint("registerSensorTransaction");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public void abortTransaction() throws CstlServiceException {
        try {
            if (currentSavePoint != null)
                smlConnection.rollback(currentSavePoint);
            smlConnection.commit();
            smlConnection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public void endTransaction() throws CstlServiceException {
        try {
            if (currentSavePoint != null)
                smlConnection.releaseSavepoint(currentSavePoint);
            smlConnection.commit();
            smlConnection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    /**
     * Create a new identifier for a sensor.
     */
    @Override
    public int getNewSensorId() throws CstlServiceException {
        try {
            ResultSet res = newSensorIdStmt.executeQuery();
            int id = -1;
            while (res.next()) {
                id = res.getInt(1);
            }
            res.close();
            return (id + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }
    
    public void destroy() {
        try {
            newSensorIdStmt.close();
            sensorMLWriter.dispose();
        } catch (SQLException ex) {
            logger.severe("SQLException while closing SOSWorker");
        }
    }

    public String getInfos() {
        return "Constellation MDweb Sensor Writer 0.3";
    }

}
