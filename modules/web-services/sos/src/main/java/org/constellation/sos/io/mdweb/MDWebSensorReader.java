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

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

// JAXB dependencies
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.constellation.sos.io.SensorReader;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// MDWeb dependencies
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.io.sql.v20.Reader20;
import org.mdweb.io.xml.Writer;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebSensorReader implements SensorReader {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.sos");

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
    private final Catalog sensorMLCatalog;

    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */
    private final Properties map;
    
    /**
     * An mdweb xml writer.
     */
    private final Writer xmlWriter;

    /**
     * A JAXB unmarshaller used to unmarshall the xml generated by the XMLWriter.
     */
    private MarshallerPool marshallerPool;

    /**
     *
     * @param dataSourceSML
     * @param sensorIdBase
     * @param map
     * @throws java.io.IOException
     * @throws org.constellation.catalog.NoSuchTableException
     * @throws java.sql.SQLException
     */
    public MDWebSensorReader(Automatic configuration, Properties map) throws CstlServiceException  {
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            sensorMLConnection = db.getConnection();
            final boolean isPostgres = db.getClassName().equals("org.postgresql.Driver");
            sensorMLReader     = new Reader20(Standard.SENSORML, sensorMLConnection, isPostgres);
            sensorMLCatalog    = sensorMLReader.getCatalog("SMLC");
            xmlWriter          = new Writer(sensorMLReader);
            this.map           = map;

            //we initialize the unmarshaller
            marshallerPool = new MarshallerPool("org.geotoolkit.sml.xml.v100:org.geotoolkit.sml.xml.v101");

        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("JAXBException while starting the MDweb Sensor reader", NO_APPLICABLE_CODE);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("SQLException while starting the MDweb Sensor reader: " + "\n" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }

    @Override
    public AbstractSensorML getSensor(String sensorId) throws CstlServiceException {
        Unmarshaller unmarshaller = null;
        try {
            String dbId = map.getProperty(sensorId);
            if (dbId == null) {
                dbId = sensorId;
            }
            // we find the form id describing the sensor.
            final int id = sensorMLReader.getIdFromTitleForm(dbId);
            LOGGER.info("describesensor id: " + dbId);
            LOGGER.info("describesensor mdweb id: " + id);
            // we get the form
            final Form f = sensorMLReader.getForm(sensorMLCatalog, id);

            if (f == null) {
                throw new CstlServiceException("this sensor is not registered in the database!",
                        INVALID_PARAMETER_VALUE, "procedure");
            }
            //we transform the form into an XML string
            final String xml      = xmlWriter.writeForm(f);
            final StringReader sr = new StringReader(xml);
            unmarshaller          = marshallerPool.acquireUnmarshaller();
           
            Object unmarshalled = unmarshaller.unmarshal(sr);
            if (unmarshalled instanceof JAXBElement) {
                unmarshalled = ((JAXBElement)unmarshalled).getValue();
            }
            if (unmarshalled instanceof AbstractSensorML)
               return (AbstractSensorML) unmarshalled;
            else
              throw new CstlServiceException("The form unmarshalled is not a sensor", NO_APPLICABLE_CODE);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("JAXBException while unmarshalling the sensor:" + ex.getMessage(), NO_APPLICABLE_CODE);
        } finally {
            if (unmarshaller != null) {
                marshallerPool.release(unmarshaller);
            }
        }
    }

    @Override
    public String getInfos() {
        return "Constellation MDweb Sensor Reader 0.4";
    }

    @Override
    public void destroy() {
        try {
            sensorMLConnection.close();
            sensorMLReader.dispose();

        } catch (SQLException ex) {
            LOGGER.severe("SQLException while closing SOSWorker");
        }
    }
}
