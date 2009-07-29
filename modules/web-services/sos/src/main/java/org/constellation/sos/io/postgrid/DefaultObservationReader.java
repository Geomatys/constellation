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

// J2SE dependencies
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// Constellation dependencies
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchRecordException;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.gml.v311.ReferenceTable;
import org.constellation.observation.ObservationTable;
import org.constellation.observation.ProcessTable;
import org.constellation.sampling.SamplingFeatureTable;
import org.constellation.sampling.SamplingPointTable;
import org.constellation.sos.ObservationOfferingTable;
import org.constellation.sos.io.ObservationReader;
import org.constellation.swe.v101.AnyResultTable;
import org.constellation.swe.v101.CompositePhenomenonTable;
import org.constellation.swe.v101.PhenomenonTable;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.ReferenceEntry;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureEntry;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.geotoolkit.swe.xml.v101.AnyResultEntry;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonEntry;
import org.geotoolkit.swe.xml.v101.PhenomenonEntry;
import org.postgresql.ds.PGSimpleDataSource;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 * Default Observation reader for Postgrid O&M database.
 *
 * @author Guilhem Legal
 */
public class DefaultObservationReader implements ObservationReader {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.sos");

    /**
     * The base for observation id.
     */
    protected final String observationIdBase;

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
     * An SQL statement finding the last Observation ID recorded
     */
    private final PreparedStatement newObservationIDStmt;

    /**
     * An SQL statement finding the last Measurement ID recorded
     */
    private final PreparedStatement newMeasurementIDStmt;

    /**
     * An SQL statement verying if the specified observation already exist.
     */
    private final PreparedStatement observationExistStmt;

    /**
     * An SQL statement get the minimal eventime for the observation offering
     */
    private final PreparedStatement getMinEventTimeOffering;

    private static final String SQL_ERROR_MSG = "The service has throw a SQL Exception:";

    private static final String CAT_ERROR_MSG = "The service has throw a Catalog Exception:";
    /**
     *
     * @param dataSourceOM
     * @param observationIdBase
     */
    public DefaultObservationReader(Automatic configuration, String observationIdBase) throws CstlServiceException {
        this.observationIdBase = observationIdBase;
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object (DefaultObservationReader)", NO_APPLICABLE_CODE);
        }
        try {
            final PGSimpleDataSource dataSourceOM = new PGSimpleDataSource();
            dataSourceOM.setServerName(db.getHostName());
            dataSourceOM.setPortNumber(db.getPortNumber());
            dataSourceOM.setDatabaseName(db.getDatabaseName());
            dataSourceOM.setUser(db.getUser());
            dataSourceOM.setPassword(db.getPassword());

            omDatabase = new Database(dataSourceOM);
            omDatabase.setProperty(ConfigurationKey.READONLY, "false");
            
            //we build the database table frequently used.
            obsTable = omDatabase.getTable(ObservationTable.class);
            offTable = omDatabase.getTable(ObservationOfferingTable.class);
            refTable = omDatabase.getTable(ReferenceTable.class);
            //we build the prepared Statement
            newObservationIDStmt    = omDatabase.getConnection().prepareStatement("SELECT Count(*) FROM \"observations\" WHERE name LIKE '%" + observationIdBase + "%' ");
            newMeasurementIDStmt    = omDatabase.getConnection().prepareStatement("SELECT Count(*) FROM \"measurements\" WHERE name LIKE '%" + observationIdBase + "%' ");
            observationExistStmt    = omDatabase.getConnection().prepareStatement("SELECT name FROM \"observations\" WHERE name=?");
            getMinEventTimeOffering = omDatabase.getConnection().prepareStatement("select MIN(event_time_begin) from observation_offerings");
        } catch (SQLException ex) {
            throw new CstlServiceException("SQL Exception while initalizing the O&M reader:" + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (NoSuchTableException ex) {
            throw new CstlServiceException("NoSuchTable Exception while initalizing the O&M reader:" + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (IOException ex) {
             throw new CstlServiceException("IO Exception while initalizing the O&M reader:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }


    }

    public Set<String> getOfferingNames() throws CstlServiceException {
        try {
            return offTable.getIdentifiers();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(CAT_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public ObservationOfferingEntry getObservationOffering(String offeringName) throws CstlServiceException {
        try {
            return offTable.getEntry(offeringName);
        } catch (NoSuchRecordException ex) {
            return null;
        } catch (CatalogException ex) {
            throw new CstlServiceException(CAT_ERROR_MSG + ex.getMessage(),
                                             NO_APPLICABLE_CODE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException(SQL_ERROR_MSG + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public List<ObservationOfferingEntry> getObservationOfferings() throws CstlServiceException {
        try {
            final List<ObservationOfferingEntry> loo = new ArrayList<ObservationOfferingEntry>();
            final Set<ObservationOfferingEntry> set  = offTable.getEntries();
            loo.addAll(set);
            return loo;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(CAT_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException("the service has throw a Runtime Exception:" + e.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public Set<String> getProcedureNames() throws CstlServiceException {
        try {
            final ProcessTable procTable = omDatabase.getTable(ProcessTable.class);
            return procTable.getIdentifiers();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(CAT_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public Set<String> getPhenomenonNames() throws CstlServiceException {
        try {
            final PhenomenonTable phenoTable               = omDatabase.getTable(PhenomenonTable.class);
            final Set<String> phenoNames                   = phenoTable.getIdentifiers();
            final CompositePhenomenonTable compoPhenoTable = omDatabase.getTable(CompositePhenomenonTable.class);
            final Set<String> compoPhenoNames              = compoPhenoTable.getIdentifiers();
            phenoNames.addAll(compoPhenoNames);
            phenoNames.remove("");
            return phenoNames;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(CAT_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public PhenomenonEntry getPhenomenon(String phenomenonName) throws CstlServiceException {
        try {
            final CompositePhenomenonTable compositePhenomenonTable = omDatabase.getTable(CompositePhenomenonTable.class);
            CompositePhenomenonEntry cphen = null;
            try {
                cphen = compositePhenomenonTable.getEntry(phenomenonName);
            } catch (NoSuchRecordException ex) {
            //we let continue to look if it is a phenomenon (simple)
            }
            if (cphen != null)
                return cphen;
            
            final PhenomenonTable phenomenonTable = omDatabase.getTable(PhenomenonTable.class);
            return (PhenomenonEntry) phenomenonTable.getEntry(phenomenonName);

        } catch (NoSuchRecordException ex) {
            return null;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(CAT_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public Set<String> getFeatureOfInterestNames() throws CstlServiceException {
        try {
            final SamplingFeatureTable featureTable = omDatabase.getTable(SamplingFeatureTable.class);
            final Set<String> featureNames          = featureTable.getIdentifiers();
            final SamplingPointTable pointTable     = omDatabase.getTable(SamplingPointTable.class);
            final Set<String> pointNames            = pointTable.getIdentifiers();
            featureNames.addAll(pointNames);
            return featureNames;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(CAT_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureName) throws CstlServiceException {
        try {
            final SamplingPointTable foiTable = omDatabase.getTable(SamplingPointTable.class);
            return foiTable.getEntry(samplingFeatureName);
        } catch (NoSuchRecordException ex) {
            return null;
        } catch (CatalogException ex) {
            throw new CstlServiceException("Catalog exception while getting the feature of interest",
                    NO_APPLICABLE_CODE, "featureOfInterest");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public ObservationEntry getObservation(String identifier) throws CstlServiceException {
        try {
            return (ObservationEntry) obsTable.getEntry(identifier);
        } catch (CatalogException ex) {
            throw new CstlServiceException("Catalog exception while getting the observations: " + ex.getMessage(),
                    NO_APPLICABLE_CODE, "getObservation");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public AnyResultEntry getResult(String identifier) throws CstlServiceException {
        try {
            final AnyResultTable resTable = omDatabase.getTable(AnyResultTable.class);
            final Integer id = Integer.parseInt(identifier);
            return resTable.getEntry(id);
        } catch (CatalogException ex) {
            throw new CstlServiceException("Catalog exception while getting the results: " + ex.getMessage(),
                    NO_APPLICABLE_CODE, "getResult");
        } catch (NumberFormatException ex) {
            throw new CstlServiceException("Number format exception while getting the results: " + ex.getMessage(),
                    NO_APPLICABLE_CODE, "getResult");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public ReferenceEntry getReference(String href) throws CstlServiceException {
        try {
            final Set<ReferenceEntry> references = refTable.getEntries();
            if (references != null) {
                final Iterator<ReferenceEntry> it = references.iterator();
                while (it.hasNext()) {
                    final ReferenceEntry ref = it.next();
                    if (ref != null && ref.getHref() != null && ref.getHref().equals(href)) {
                        return ref;
                    }
                }
            }
            return null;

        } catch (NoSuchRecordException ex) {
            LOGGER.info("NoSuchRecordException in getReferences");
            return null;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(CAT_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public void destroy() {
        try {
            newObservationIDStmt.close();
            observationExistStmt.close();
            getMinEventTimeOffering.close();
            obsTable.clear();
            offTable.clear();
            refTable.clear();
            omDatabase.close();
        } catch (CatalogException ex) {
            LOGGER.severe("Catalog Exception while destroy observation reader");
        } catch (SQLException ex) {
            LOGGER.severe("SQL Exception while destroy observation reader");
        }
    }

    /**
     * Create a new identifier for an observation by searching in the O&M database.
     */
    public String getNewObservationId(String type) throws CstlServiceException {
        try {
            ResultSet res;
            if (type != null && type.equals("measurement")) {
                res = newMeasurementIDStmt.executeQuery();
            } else {
                res = newObservationIDStmt.executeQuery();
            }
            int id = -1;
            while (res.next()) {
                id = res.getInt(1);
            }
            res.close();
            //there is a possibility that someone delete some observation manually.
            // so we must verify that this id is not already assigned. if it is we must find a free identifier
            do {
                id ++;
                observationExistStmt.setString(1, observationIdBase + id);
                res = observationExistStmt.executeQuery();
            } while (res.next());
            res.close();
            return observationIdBase + id;
        } catch( SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException("The service has throw a SQLException:" + e.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }

    /**
     * Return the minimal value for the offering event Time
     */
    public List<String> getEventTime() throws CstlServiceException {
        String ret = null;
        try {
            final ResultSet res = getMinEventTimeOffering.executeQuery();
            Timestamp t = null;
            while (res.next()) {
                t = res.getTimestamp(1);
            }

            if (t != null) {
                ret = t.toString();
            }
            res.close();

        } catch (SQLException ex) {
           LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
           throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
        }
        if  (ret != null)
            return Arrays.asList(ret);
        return null;
    }

    public String getInfos() {
        return "Constellation Postgrid O&M Reader 0.4";
    }

    public List<ResponseModeType> getResponseModes() throws CstlServiceException {
        return Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
    }

    public List<String> getResponseFormats() throws CstlServiceException {
        return Arrays.asList("text/xml; subtype=\"om/1.0.0\"");
    }
}
