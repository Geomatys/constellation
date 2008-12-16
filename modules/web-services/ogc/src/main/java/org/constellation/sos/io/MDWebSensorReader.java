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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.sos.ws.SensorReader;
import org.constellation.ws.WebServiceException;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.sql.v20.Reader20;
import org.mdweb.xml.Writer;
import org.postgresql.ds.PGSimpleDataSource;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public class MDWebSensorReader extends SensorReader {

     /**
     * A simple Connection to the SensorML database.
     */
    private final Connection sensorMLConnection;

    /**
     * A Reader to the SensorML database.
     */
    private final Reader20 sensorMLReader;

    /**
     * the data catalog for SensorML database.
     */
    private final Catalog SMLCatalog;

    /**
     * An SQL satetement finding the last sensor ID recorded
     */
    private final PreparedStatement newSensorIdStmt;

    /**
     * An SQL statement get a sensorML value in the MDWeb database
     */
    private final PreparedStatement getValueStmt;

    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */
    private final Properties map;
    
    /**
     *
     */
    private final Writer XMLWriter;

    public MDWebSensorReader(PGSimpleDataSource dataSourceSML, String sensorIdBase, Properties map) throws IOException, NoSuchTableException, SQLException {
        super();
        sensorMLConnection = dataSourceSML.getConnection();
        sensorMLReader     = new Reader20(Standard.SENSORML, sensorMLConnection);
        SMLCatalog         = sensorMLReader.getCatalog("SMLC");
        XMLWriter          = new Writer(sensorMLReader);
        this.map           = map;

        //we build the prepared Statement
        newSensorIdStmt    = sensorMLConnection.prepareStatement("SELECT Count(*) FROM \"Forms\" WHERE title LIKE '%" + sensorIdBase + "%' ");
        getValueStmt       = sensorMLConnection.prepareStatement(" SELECT value FROM \"TextValues\" WHERE id_value=? AND form=?");
    }

    public String getSensor(String sensorId) throws WebServiceException {
        try {
            String dbId = map.getProperty(sensorId);
            if (dbId == null) {
                dbId = sensorId;
            }
            // we find the form id describing the sensor.
            int id = sensorMLReader.getIdFromTitleForm(dbId);
            logger.info("describesensor id: " + dbId);
            logger.info("describesensor mdweb id: " + id);
            // we get the form
            Form f = sensorMLReader.getForm(SMLCatalog, id);

            if (f == null) {
                throw new WebServiceException("this sensor is not registered in the database!",
                        INVALID_PARAMETER_VALUE, "procedure");
            }
            //we transform the form into an XML string
           return XMLWriter.writeForm(f);

       } catch (SQLException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
       }
    }

    public String getSRSName(int formID) throws WebServiceException {
        try {
            String SRS      = "";
            //we get the srs name
            getValueStmt.setString(1, "SensorML:SensorML.1:member.1:location.1:pos.1:srsName.1");
            getValueStmt.setInt(2, formID);
            ResultSet result = getValueStmt.executeQuery();
            if (result.next()) {
                SRS = result.getString(1);
                if (SRS.indexOf(':') != -1) {
                    SRS = SRS.substring(SRS.lastIndexOf(':') + 1);
                }
                logger.info("srsName:" + SRS);
            } else {
                logger.severe("there is no srsName for the piezo location");
                return "";
            }
            result.close();
            return SRS;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public String getSensorCoordinates(int formID) throws WebServiceException {
        try {
            String coordinates = "";

            // we get the coordinates
            getValueStmt.setString(1, "SensorML:SensorML.1:member.1:location.1:pos.1");
            getValueStmt.setInt(2, formID);
            ResultSet result = getValueStmt.executeQuery();
            if (result.next()) {

                coordinates = result.getString(1);
                logger.info(coordinates);
            } else {
                logger.severe("there is no coordinates for the piezo location");
                return "";
            }
            result.close();
            return coordinates;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public List<Integer> getNetworkIndex(int formID) throws WebServiceException {
        try {
            int i = 1;
            List<Integer> networksIndex = new ArrayList<Integer>();
            boolean moreClassifier = true;
            while (moreClassifier) {

                getValueStmt.setString(1, "SensorML:SensorML.1:member.1:classification.1:classifier." + i + ":name.1");
                getValueStmt.setInt(2,    formID);
                ResultSet result = getValueStmt.executeQuery();
                moreClassifier   = result.next();
                if (moreClassifier) {
                    String value = result.getString(1);
                    if (value.equals("network")){
                        networksIndex.add(i);

                    }
                }
                result.close();
                i++;
            }
            return networksIndex;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public String getNetworkName(int formID, String networkName) throws WebServiceException {
        try {
            getValueStmt.setString(1, "SensorML:SensorML.1:member.1:classification.1:classifier." + networkName + ":value.1");
            getValueStmt.setInt(2,    formID);
            ResultSet result = getValueStmt.executeQuery();
            String network = null;
            if (result.next()) {
                network = result.getString(1);
            }
            result.close();
            return network;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    /**
     * Create a new identifier for an observation by searching in the O&M database.
     */
    public int getNewSensorId() throws SQLException {
        ResultSet res = newSensorIdStmt.executeQuery();int id = -1;
        while (res.next()) {
            id = res.getInt(1);
        }
        res.close();
        return (id + 1);
    }

    public void destroy() {
        try {
            newSensorIdStmt.close();
            sensorMLConnection.close();
            sensorMLReader.dispose();

        } catch (SQLException ex) {
            logger.severe("SQLException while closing SOSWorker");
        }
    }

}
