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

package org.constellation.sos.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.constellation.ws.WebServiceException;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.users.User;
import org.mdweb.sql.v20.Reader20;
import org.mdweb.sql.v20.Writer20;
import org.mdweb.xml.MalFormedDocumentException;
import org.mdweb.xml.Reader;
import org.postgresql.ds.PGSimpleDataSource;
import org.xml.sax.SAXException;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author guilhem
 */
public class MDWebSensorWriter extends SensorWriter {

    /**
     * A Writer to the SensorML database.
     */
    private final Writer20 sensorMLWriter;

    /**
     * An SQL statement get a sensorML value in the MDWeb database
     */
    private final PreparedStatement getValueStmt;

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
     * The properties file allowing to store the id mapping between physical and database ID.
     */
    private final Properties map;

    public MDWebSensorWriter(PGSimpleDataSource dataSourceSML, String sensorIdBase, Properties map) throws SQLException {
        smlConnection  = dataSourceSML.getConnection();
        sensorMLWriter = new Writer20(smlConnection);
        sensorMLReader = new Reader20(Standard.SENSORML, smlConnection);
        SMLCatalog     = sensorMLReader.getCatalog("SMLC");
        mainUser       = sensorMLReader.getUser("admin");
        this.map       = map;

        //we build the prepared Statement
        getValueStmt       = smlConnection.prepareStatement(" SELECT value FROM \"TextValues\" WHERE id_value=? AND form=?");
    }

     public int writeSensor(String id, File sensorFile) throws WebServiceException {
        try {
            //we parse the temporay xmlFile
            Reader XMLReader = new Reader(sensorMLReader, sensorFile, sensorMLWriter);

            //and we write it in the sensorML Database

            Form f = XMLReader.readForm(SMLCatalog, mainUser, "source", id, Standard.SENSORML);
            sensorMLWriter.writeForm(f, false);
            return f.getId();

        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
            throw new WebServiceException("The service has throw a ParserException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        } catch (SAXException ex) {
            ex.printStackTrace();
            throw new WebServiceException("The service has throw a SAXException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        } catch (MalFormedDocumentException ex) {
            ex.printStackTrace();
            logger.severe("MalFormedDocumentException:" + ex.getMessage());
            throw new WebServiceException("The SensorML Document is Malformed",
                                          INVALID_PARAMETER_VALUE, "sensorDescription");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new WebServiceException("The service cannot build the temporary file",
                                          NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw an IOException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }

    /**
     * Record the mapping between physical ID and database ID.
     *
     * @param form The "form" containing the sensorML data.
     * @param dbId The identifier of the sensor in the O&M database.
     */
    public String recordMapping(String dbId, File sicadeDirectory) throws WebServiceException {
        try {
            //we search which identifier is the supervisor code
            int formID             = sensorMLReader.getIdFromTitleForm(dbId);
            int i                  = 1;
            boolean found          = false;
            boolean moreIdentifier = true;
            while (moreIdentifier && !found) {
                getValueStmt.setString(1, "SensorML:SensorML.1:member.1:identification.1:identifier." + i + ":name.1");
                getValueStmt.setInt(2, formID);
                ResultSet result = getValueStmt.executeQuery();
                moreIdentifier   = result.next();
                if (moreIdentifier) {
                    String value = result.getString(1);
                    if (value.equals("supervisorCode")){
                        found = true;
                    }
                }
                result.close();
                i++;
            }

            if (!found) {
                logger.severe("There is no supervisor code in that SensorML file");
                return "";
            } else {
                getValueStmt.setString(1, "SensorML:SensorML.1:member.1:identification.1:identifier." + (i - 1) + ":value.1");
                getValueStmt.setInt(2,    formID);
                ResultSet result = getValueStmt.executeQuery();
                String value = "";
                if (result.next()) {
                    value = result.getString(1);
                    logger.info("PhysicalId:" + value);
                    map.setProperty(value, dbId);
                    File mappingFile = new File(sicadeDirectory, "/sos_configuration/mapping.properties");
                    FileOutputStream out = new FileOutputStream(mappingFile);
                    map.store(out, "");
                    out.close();
                } else {
                    logger.severe("no value for supervisorcode identifier numero " + (i - 1));
                }
                result.close();
                return value;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                             NO_APPLICABLE_CODE);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new WebServiceException("The service cannot build the temporary file",
                                          NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw an IOException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }

    public void startTransaction() throws WebServiceException {
        try {
            smlConnection.setAutoCommit(false);
            currentSavePoint = smlConnection.setSavepoint("registerSensorTransaction");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public void abortTransaction() throws WebServiceException {
        try {
            if (currentSavePoint != null)
                smlConnection.rollback(currentSavePoint);
            smlConnection.commit();
            smlConnection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public void endTransaction() throws WebServiceException {
        try {
            if (currentSavePoint != null)
                smlConnection.releaseSavepoint(currentSavePoint);
            smlConnection.commit();
            smlConnection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }
    public void destroy() {
        try {
            getValueStmt.close();

            sensorMLWriter.dispose();

        } catch (SQLException ex) {
            logger.severe("SQLException while closing SOSWorker");
        }
    }

}
