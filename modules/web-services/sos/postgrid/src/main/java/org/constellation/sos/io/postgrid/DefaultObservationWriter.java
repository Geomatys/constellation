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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.observation.MeasurementTable;
import org.constellation.observation.ObservationTable;
import org.constellation.observation.ProcessTable;
import org.constellation.sos.ObservationOfferingTable;
import org.geotoolkit.observation.ObservationWriter;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.gml.xml.DirectPosition;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.NoSuchTableException;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.observation.xml.OMXmlFactory;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.sos.xml.v100.OfferingPhenomenonType;
import org.geotoolkit.sos.xml.v100.OfferingProcedureType;
import org.geotoolkit.sos.xml.v100.OfferingSamplingFeatureType;
import org.geotoolkit.swe.xml.v101.PhenomenonType;
import org.geotoolkit.swes.xml.ObservationTemplate;
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
     * A database table for insert and get observation offering.
     */
    private final ProcessTable procTable;

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
    public DefaultObservationWriter(final Automatic configuration) throws DataStoreException {
        if (configuration == null) {
            throw new DataStoreException("The configuration object is null");
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new DataStoreException("The configuration file does not contains a BDD object");
        }
        isPostgres = db.getClassName() != null && db.getClassName().equals("org.postgresql.Driver");
        try {
            omDatabase = DatabasePool.getDatabase(db);

            //we build the database table frequently used.
            obsTable  = omDatabase.getTable(ObservationTable.class);
            measTable = omDatabase.getTable(MeasurementTable.class);
            offTable  = omDatabase.getTable(ObservationOfferingTable.class);
            procTable = omDatabase.getTable(ProcessTable.class);

        } catch (NoSuchTableException ex) {
            throw new DataStoreException("NoSuchTable Exception while initalizing the O&M writer:" + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeObservationTemplate(final ObservationTemplate template) throws DataStoreException {
        if (template.getObservation() != null) {
            return writeObservation((AbstractObservation)template.getObservation());
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String writeObservation(final Observation observation) throws DataStoreException {
        try {
            if (observation instanceof Measurement && measTable != null) {
                return measTable.getIdentifier((Measurement) OMXmlFactory.convert("1.0.0", observation));
            } else if (obsTable != null) {
                return obsTable.getIdentifier(OMXmlFactory.convert("1.0.0", observation));
            }
            return null;
        } catch (CatalogException ex) {
            throw new DataStoreException(CAT_ERROR_MSG + ex.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new DataStoreException("the service has throw a SQL Exception:" + e.getMessage());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> writeObservations(List<Observation> observations) throws DataStoreException {
        final List<String> results = new ArrayList<>();
        for (Observation observation : observations) {
            final String oid = writeObservation(observation);
            results.add(oid);
        }
        return results;
    }
    
    @Override
    public void removeObservation(final String observationID) throws DataStoreException {
        try {
            obsTable.delete(observationID);
            measTable.delete(observationID);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeObservationForProcedure(final String procedureID) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeProcedure(final String procedureID) throws DataStoreException {
        try {
            procTable.delete(procedureID);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeOffering(final ObservationOffering offering) throws DataStoreException {
        try {
            return offTable.getIdentifier((ObservationOfferingType)offering);

        } catch (CatalogException ex) {
            throw new DataStoreException(CAT_ERROR_MSG + ex.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new DataStoreException(SQL_ERROR_MSG + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOffering(final String offeringID, final String offProc, final List<String> offPheno, final String offSF) throws DataStoreException {
        try {
            if (offProc != null) {
                final OfferingProcedureType offProcedure = new OfferingProcedureType(offeringID, offProc);
                offTable.getProcedures().getIdentifier(offProcedure);
            }
            if (offPheno != null) {
                for (String phenId : offPheno) {
                    final PhenomenonType pheno = new PhenomenonType(phenId, null);
                    final OfferingPhenomenonType offPhenomenon = new OfferingPhenomenonType(offeringID, pheno);
                    offTable.getPhenomenons().getIdentifier(offPhenomenon);
                }
            }
            if (offSF != null) {
                final OfferingSamplingFeatureType offSamp = new OfferingSamplingFeatureType(offeringID, offSF);
                offTable.getStations().getIdentifier(offSamp);
            }
        } catch (CatalogException ex) {
            throw new DataStoreException(CAT_ERROR_MSG + ex.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new DataStoreException(SQL_ERROR_MSG + e.getMessage());
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
    public void recordProcedureLocation(final String physicalID, final AbstractGeometry position) throws DataStoreException {
        if (!(position instanceof DirectPosition)) {
            throw new DataStoreException("Postgrid implementation only record directPosition procedure location");
        }
        final DirectPosition pos = (DirectPosition) position;
        if (pos == null || pos.getValue().size() < 2 || !isPostgres) {return;}
        try {
            final Connection c       = omDatabase.getDataSource(true).getConnection();
            final Statement stmt2    = c.createStatement();
            final ResultSet result2;
            String request = "SELECT * FROM ";
            boolean insert = true;
            String srsName = "4326";
            if (pos.getSrsName() != null) {
                srsName = pos.getSrsName();
            }

            if (srsName.startsWith("urn:ogc:crs:EPSG:")) {
                srsName = srsName.substring(17);
            } else if (srsName.startsWith("EPSG:")) {
                srsName = srsName.substring(5);
            }

            if ("27582".equals(srsName)) {
                request = request + " \"sos\".\"projected_localisations\" WHERE id='" + physicalID + "'";
                result2 = stmt2.executeQuery(request);
                if (!result2.next()) {
                    request = "INSERT INTO \"sos\".\"projected_localisations\" VALUES ('" + physicalID + "', GeometryFromText( 'POINT(" + pos.getValue().get(0) + ' ' + pos.getValue().get(1) + ")', " + srsName + "))";
                } else {
                    insert = false;
                    LOGGER.log(Level.INFO, "Projected sensor location already registred for {0} keeping old location", physicalID);
                }
            } else if ("4326".equals(srsName)) {
                request = request + " \"sos\".\"geographic_localisations\" WHERE id='" + physicalID + "'";
                result2 = stmt2.executeQuery(request);
                if (!result2.next()) {
                    request = "INSERT INTO \"sos\".\"geographic_localisations\" VALUES ('" + physicalID + "', GeometryFromText( 'POINT(" + pos.getValue().get(0) + ' ' + pos.getValue().get(1) + ")', " + srsName + "))";
                } else {
                    insert = false;
                    LOGGER.log(Level.INFO, "Geographic sensor location already registred for {0} keeping old location", physicalID);
                }
            } else {
                throw new DataStoreException("This CRS " + srsName + " is not supported");
            }
            LOGGER.info(request);
            if (insert) {
                stmt2.executeUpdate(request);
            }
            result2.close();
            stmt2.close();
            c.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new DataStoreException(SQL_ERROR_MSG + e.getMessage());
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
