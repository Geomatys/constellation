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
package org.constellation.sos.io.postgrid;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// constellation dependencies
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.gml.v311.ReferenceTable;
import org.constellation.observation.MeasurementTable;
import org.constellation.observation.ObservationTable;
import org.constellation.sos.ObservationOfferingTable;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.OfferingPhenomenonEntry;
import org.geotoolkit.sos.xml.v100.OfferingProcedureEntry;
import org.geotoolkit.sos.xml.v100.OfferingSamplingFeatureEntry;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.opengis.observation.Measurement;
import org.opengis.observation.Observation;

// Postgres dependencies
import org.postgresql.ds.PGSimpleDataSource;


/**
 * Default Observation reader for Postgrid O&M database.
 * 
 * @author Guilhem Legal
 */
public class DefaultObservationWriter implements ObservationWriter {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.sos");

    /**
     * A Database object for the O&M dataBase.
     */
    private final Database omDatabase;

    /**
     * A database table for insert and get observation
     */
    private final ObservationTable obsTable;

    /**
     * A database table for insert and get observation offerring.
     */
    private final ObservationOfferingTable offTable;

    /**
     * A database table for insert and get reference object.
     */
    private final ReferenceTable refTable;

    /**
     *
     * @param dataSourceOM
     * @param observationIdBase
     * @throws java.io.IOException
     * @throws org.constellation.catalog.NoSuchTableException
     * @throws java.sql.SQLException
     */
    public DefaultObservationWriter(Automatic configuration) throws CstlServiceException {
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            final PGSimpleDataSource dataSourceOM = new PGSimpleDataSource();
            dataSourceOM.setServerName(db.getHostName());
            dataSourceOM.setPortNumber(db.getPortNumber());
            dataSourceOM.setDatabaseName(db.getDatabaseName());
            dataSourceOM.setUser(db.getUser());
            dataSourceOM.setPassword(db.getPassword());

            omDatabase   = new Database(dataSourceOM);
            omDatabase.setProperty(ConfigurationKey.READONLY, "false");
            
            //we build the database table frequently used.
            obsTable = omDatabase.getTable(ObservationTable.class);
            offTable = omDatabase.getTable(ObservationOfferingTable.class);
            refTable = omDatabase.getTable(ReferenceTable.class);

        } catch (NoSuchTableException ex) {
            throw new CstlServiceException("NoSuchTable Exception while initalizing the O&M writer:" + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (IOException ex) {
             throw new CstlServiceException("IO Exception while initalizing the O&M writer:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }

    }

    public String writeObservation(Observation observation) throws CstlServiceException {
        try {
            if (obsTable != null) {
                return obsTable.getIdentifier(observation);
            }
            return null;
        } catch (CatalogException ex) {
            throw new CstlServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                                             NO_APPLICABLE_CODE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public String writeMeasurement(Measurement measurement) throws CstlServiceException {
        try {
            final MeasurementTable measTable = omDatabase.getTable(MeasurementTable.class);
            return measTable.getIdentifier(measurement);
        } catch (CatalogException ex) {
            throw new CstlServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                                             NO_APPLICABLE_CODE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public String writeOffering(ObservationOfferingEntry offering) throws CstlServiceException {
        try {
            return offTable.getIdentifier(offering);

        } catch (CatalogException ex) {
            throw new CstlServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                                             NO_APPLICABLE_CODE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public void updateOffering(OfferingProcedureEntry offProc, OfferingPhenomenonEntry offPheno, OfferingSamplingFeatureEntry offSF) throws CstlServiceException {
        try {
            if (offProc != null)
                offTable.getProcedures().getIdentifier(offProc);
            if (offPheno != null)
                offTable.getPhenomenons().getIdentifier(offPheno);
            if (offSF != null)
                offTable.getStations().getIdentifier(offSF);

        } catch (CatalogException ex) {
            throw new CstlServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                                             NO_APPLICABLE_CODE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public void updateOfferings() {
        offTable.flush();
    }

    public void recordProcedureLocation(String physicalID, DirectPositionType position) throws CstlServiceException {
        if (position == null || position.getValue().size() < 2)
            return;
        try {
            final Statement stmt2    = omDatabase.getConnection().createStatement();
            final ResultSet result2;
            String request = "SELECT * FROM ";
            boolean insert = true;
            String srsName = "4326";
            if (position.getSrsName() != null)
                srsName = position.getSrsName();

            if (srsName.equals("27582")) {
                request = request + " projected_localisations WHERE id='" + physicalID + "'";
                result2 = stmt2.executeQuery(request);
                if (!result2.next()) {
                    request = "INSERT INTO projected_localisations VALUES ('" + physicalID + "', GeometryFromText( 'POINT(" + position.getValue().get(0) + ' ' + position.getValue().get(1) + ")', " + position.getSrsName() + "))";
                } else {
                    insert = false;
                    LOGGER.severe("Projected sensor location already registred for " + physicalID + " keeping old location");
                }
            } else if (srsName.equals("4326")) {
                request = request + " geographic_localisations WHERE id='" + physicalID + "'";
                result2 = stmt2.executeQuery(request);
                if (!result2.next()) {
                    request = "INSERT INTO geographic_localisations VALUES ('" + physicalID + "', GeometryFromText( 'POINT(" + position.getValue().get(0) + ' ' + position.getValue().get(1) + ")', " + position.getSrsName() + "))";
                } else {
                    insert = false;
                    LOGGER.severe("Geographic sensor location already registred for " + physicalID + " keeping old location");
                }
            } else {
                throw new CstlServiceException("This CRS " + srsName + " is not supported", INVALID_PARAMETER_VALUE);
            }
            LOGGER.info(request);
            if (insert) {
                stmt2.executeUpdate(request);
            }
            result2.close();
            stmt2.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public String getInfos() {
        return "Constellation Postgrid O&M Writer 0.4";
    }

    public void destroy() {
        try {
            obsTable.clear();
            offTable.clear();
            refTable.clear();
            omDatabase.close();
        } catch (CatalogException ex) {
            LOGGER.severe("Catalog exception while destroying observation writer");
        } catch (SQLException ex) {
            LOGGER.severe("SQL exception while destroying observation writer");
        }
    }

}
