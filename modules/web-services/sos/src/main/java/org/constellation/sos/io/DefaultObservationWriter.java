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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// constellation dependencies
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.ReferenceTable;
import org.constellation.observation.MeasurementTable;
import org.constellation.observation.ObservationTable;
import org.constellation.sos.v100.ObservationOfferingEntry;
import org.constellation.sos.ObservationOfferingTable;
import org.constellation.sos.v100.OfferingPhenomenonEntry;
import org.constellation.sos.v100.OfferingProcedureEntry;
import org.constellation.sos.v100.OfferingSamplingFeatureEntry;
import org.constellation.ws.CstlServiceException;
import static org.constellation.ows.OWSExceptionCode.*;

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
public class DefaultObservationWriter extends ObservationWriter {

    /**
     * A Database object for the O&M dataBase.
     */
    private final Database OMDatabase;

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
        BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            PGSimpleDataSource dataSourceOM = new PGSimpleDataSource();
            dataSourceOM.setServerName(db.getHostName());
            dataSourceOM.setPortNumber(db.getPortNumber());
            dataSourceOM.setDatabaseName(db.getDatabaseName());
            dataSourceOM.setUser(db.getUser());
            dataSourceOM.setPassword(db.getPassword());

            OMDatabase   = new Database(dataSourceOM);
            
            //we build the database table frequently used.
            obsTable = OMDatabase.getTable(ObservationTable.class);
            offTable = OMDatabase.getTable(ObservationOfferingTable.class);
            refTable = OMDatabase.getTable(ReferenceTable.class);

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
            e.printStackTrace();
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public String writeMeasurement(Measurement measurement) throws CstlServiceException {
        try {
            MeasurementTable measTable = OMDatabase.getTable(MeasurementTable.class);
            return measTable.getIdentifier(measurement);
        } catch (CatalogException ex) {
            throw new CstlServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                                             NO_APPLICABLE_CODE);
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            Statement stmt2    = OMDatabase.getConnection().createStatement();
            final ResultSet result2;
            String request = "SELECT * FROM ";
            boolean insert = true;
            String SRSName = "4326";
            if (position.getSrsName() != null)
                SRSName = position.getSrsName();

            if (SRSName.equals("27582")) {
                request = request + " projected_localisations WHERE id='" + physicalID + "'";
                result2 = stmt2.executeQuery(request);
                if (!result2.next()) {
                    request = "INSERT INTO projected_localisations VALUES ('" + physicalID + "', GeometryFromText( 'POINT(" + position.getValue().get(0) + ' ' + position.getValue().get(1) + ")', " + position.getSrsName() + "))";
                } else {
                    insert = false;
                    logger.severe("Projected sensor location already registred for " + physicalID + " keeping old location");
                }
            } else if (SRSName.equals("4326")) {
                request = request + " geographic_localisations WHERE id='" + physicalID + "'";
                result2 = stmt2.executeQuery(request);
                if (!result2.next()) {
                    request = "INSERT INTO geographic_localisations VALUES ('" + physicalID + "', GeometryFromText( 'POINT(" + position.getValue().get(0) + ' ' + position.getValue().get(1) + ")', " + position.getSrsName() + "))";
                } else {
                    insert = false;
                    logger.severe("Geographic sensor location already registred for " + physicalID + " keeping old location");
                }
            } else {
                throw new CstlServiceException("This CRS " + SRSName + " is not supported", INVALID_PARAMETER_VALUE);
            }
            logger.info(request);
            if (insert) {
                stmt2.executeUpdate(request);
            }
            result2.close();
            stmt2.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public void destroy() {
        try {
            obsTable.clear();
            offTable.clear();
            refTable.clear();
            OMDatabase.close();
        } catch (CatalogException ex) {
            logger.severe("Catalog exception while destroying observation writer");
        } catch (SQLException ex) {
            logger.severe("SQL exception while destroying observation writer");
        }
    }

}
