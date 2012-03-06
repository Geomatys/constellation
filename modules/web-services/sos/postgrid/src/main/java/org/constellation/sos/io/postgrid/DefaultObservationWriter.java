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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.observation.MeasurementTable;
import org.constellation.observation.ObservationTable;
import org.constellation.sos.ObservationOfferingTable;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.ws.CstlServiceException;

// Geotoolkit dependencies
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.NoSuchTableException;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.observation.xml.v100.MeasurementType;
import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.sos.xml.v100.OfferingPhenomenonType;
import org.geotoolkit.sos.xml.v100.OfferingProcedureType;
import org.geotoolkit.sos.xml.v100.OfferingSamplingFeatureType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.opengis.observation.Measurement;
import org.opengis.observation.Observation;



/**
 * Default Observation reader for Postgrid O&M database.
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultObservationWriter implements ObservationWriter {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos");

    /**
     * A Database object for the O&M dataBase.
     */
    private final Database omDatabase;

    /**
     * A database table for insert and get observation
     */
    private final ObservationTable obsTable;

    /**
     * A database table for insert and get observation
     */
    private final MeasurementTable measTable;

    /**
     * A database table for insert and get observation offering.
     */
    private final ObservationOfferingTable offTable;

    /**
     * A flag indicating if the dataSource is a postgreSQL SGBD
     */
    private final boolean isPostgres;

    private static final String SQL_ERROR_MSG = "The service has throw a SQL Exception:";

    private static final String CAT_ERROR_MSG = "The service has throw a Catalog Exception:";

    /**
     * Build a new Observation writer for postgrid dataSource.
     *
     * @param configuration
     * 
     * @throws org.constellation.ws.CstlServiceException
     */
    public DefaultObservationWriter(final Automatic configuration) throws CstlServiceException {
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        isPostgres = db.getClassName() != null && db.getClassName().equals("org.postgresql.Driver");
        try {
            omDatabase = DatabasePool.getDatabase(db);
            
            //we build the database table frequently used.
            obsTable  = omDatabase.getTable(ObservationTable.class);
            measTable = omDatabase.getTable(MeasurementTable.class);
            offTable  = omDatabase.getTable(ObservationOfferingTable.class);

        } catch (NoSuchTableException ex) {
            throw new CstlServiceException("NoSuchTable Exception while initalizing the O&M writer:" + ex.getMessage(), NO_APPLICABLE_CODE);
        } 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeObservation(final Observation observation) throws CstlServiceException {
        try {
            if (observation instanceof MeasurementType && measTable != null) {
                return measTable.getIdentifier((Measurement) observation);
            } else if (obsTable != null) {
                return obsTable.getIdentifier(observation);
            }
            return null;
        } catch (CatalogException ex) {
            throw new CstlServiceException(CAT_ERROR_MSG + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeMeasurement(final Measurement measurement) throws CstlServiceException {
        try {
            return measTable.getIdentifier(measurement);
        } catch (CatalogException ex) {
            throw new CstlServiceException(CAT_ERROR_MSG + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(), NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeOffering(final ObservationOfferingType offering) throws CstlServiceException {
        try {
            return offTable.getIdentifier(offering);

        } catch (CatalogException ex) {
            throw new CstlServiceException(CAT_ERROR_MSG + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(), NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOffering(final OfferingProcedureType offProc, final OfferingPhenomenonType offPheno, final OfferingSamplingFeatureType offSF) throws CstlServiceException {
        try {
            if (offProc != null)
                offTable.getProcedures().getIdentifier(offProc);
            if (offPheno != null)
                offTable.getPhenomenons().getIdentifier(offPheno);
            if (offSF != null)
                offTable.getStations().getIdentifier(offSF);

        } catch (CatalogException ex) {
            throw new CstlServiceException(CAT_ERROR_MSG + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(), NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOfferings() {
        //offTable.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordProcedureLocation(final String physicalID, final DirectPositionType position) throws CstlServiceException {
        if (position == null || position.getValue().size() < 2 || !isPostgres)
            return;
        try {
            final Statement stmt2    = omDatabase.getDataSource(true).getConnection().createStatement();
            final ResultSet result2;
            String request = "SELECT * FROM ";
            boolean insert = true;
            String srsName = "4326";
            if (position.getSrsName() != null)
                srsName = position.getSrsName();

            if (srsName.startsWith("urn:ogc:crs:EPSG:")) {
                srsName = srsName.substring(17);
            } else if (srsName.startsWith("EPSG:")) {
                srsName = srsName.substring(5);
            }

            if ("27582".equals(srsName)) {
                request = request + " \"sos\".\"projected_localisations\" WHERE id='" + physicalID + "'";
                result2 = stmt2.executeQuery(request);
                if (!result2.next()) {
                    request = "INSERT INTO \"sos\".\"projected_localisations\" VALUES ('" + physicalID + "', GeometryFromText( 'POINT(" + position.getValue().get(0) + ' ' + position.getValue().get(1) + ")', " + srsName + "))";
                } else {
                    insert = false;
                    LOGGER.log(Level.INFO, "Projected sensor location already registred for {0} keeping old location", physicalID);
                }
            } else if ("4326".equals(srsName)) {
                request = request + " \"sos\".\"geographic_localisations\" WHERE id='" + physicalID + "'";
                result2 = stmt2.executeQuery(request);
                if (!result2.next()) {
                    request = "INSERT INTO \"sos\".\"geographic_localisations\" VALUES ('" + physicalID + "', GeometryFromText( 'POINT(" + position.getValue().get(0) + ' ' + position.getValue().get(1) + ")', " + srsName + "))";
                } else {
                    insert = false;
                    LOGGER.log(Level.INFO, "Geographic sensor location already registred for {0} keeping old location", physicalID);
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
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(), NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Postgrid O&M Writer 0.9";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        /*try {
            omDatabase.close();
        } catch (SQLException ex) {
            LOGGER.severe("SQL exception while destroying observation writer");
        }*/
    }

}
