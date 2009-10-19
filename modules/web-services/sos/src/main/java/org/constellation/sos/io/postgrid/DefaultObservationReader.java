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
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchRecordException;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.gml.v311.ReferenceTable;
import org.constellation.observation.MeasureTable;
import org.constellation.observation.MeasurementTable;
import org.constellation.observation.ObservationTable;
import org.constellation.observation.ProcessTable;
import org.constellation.sampling.SamplingFeatureTable;
import org.constellation.sampling.SamplingPointTable;
import org.constellation.sos.ObservationOfferingTable;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.ws.Parameters;
import org.constellation.swe.v101.AnyResultTable;
import org.constellation.swe.v101.CompositePhenomenonTable;
import org.constellation.swe.v101.PhenomenonTable;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.ReferenceEntry;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.observation.xml.v100.MeasurementEntry;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureEntry;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
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
     * A database table for insert and get observation
     */
    private final MeasurementTable measTable;

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
            final DataSource dataSourceOM;
            if (db.getClassName() != null && db.getClassName().equals("org.postgresql.Driver")) {
                final PGSimpleDataSource PGdataSourceOM = new PGSimpleDataSource();
                PGdataSourceOM.setServerName(db.getHostName());
                PGdataSourceOM.setPortNumber(db.getPortNumber());
                PGdataSourceOM.setDatabaseName(db.getDatabaseName());
                PGdataSourceOM.setUser(db.getUser());
                PGdataSourceOM.setPassword(db.getPassword());
                dataSourceOM = PGdataSourceOM;
            } else {
                dataSourceOM = new DefaultDataSource(db.getConnectURL());
            }

            omDatabase = new Database(dataSourceOM);
            omDatabase.setProperty(ConfigurationKey.READONLY, "false");
            
            //we build the database table frequently used.
            obsTable  = omDatabase.getTable(ObservationTable.class);
            measTable = omDatabase.getTable(MeasurementTable.class);
            offTable  = omDatabase.getTable(ObservationOfferingTable.class);
            refTable  = omDatabase.getTable(ReferenceTable.class);
            //we build the prepared Statement
            newObservationIDStmt    = omDatabase.getConnection().prepareStatement("SELECT Count(*) FROM \"observation\".\"observations\" WHERE \"name\" LIKE '%" + observationIdBase + "%' ");
            newMeasurementIDStmt    = omDatabase.getConnection().prepareStatement("SELECT Count(*) FROM \"observation\".\"measurements\" WHERE \"name\" LIKE '%" + observationIdBase + "%' ");
            observationExistStmt    = omDatabase.getConnection().prepareStatement("SELECT \"name\" FROM \"observation\".\"observations\" WHERE \"name\"=? UNION SELECT \"name\" FROM \"observation\".\"measurements\" WHERE \"name\"=?");
            getMinEventTimeOffering = omDatabase.getConnection().prepareStatement("select MIN(\"event_time_begin\") from \"sos\".\"observation_offerings\"");
        } catch (SQLException ex) {
            throw new CstlServiceException("SQL Exception while initalizing the O&M reader:" + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (NoSuchTableException ex) {
            throw new CstlServiceException("NoSuchTable Exception while initalizing the O&M reader:" + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (IOException ex) {
             throw new CstlServiceException("IO Exception while initalizing the O&M reader:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }


    }

    @Override
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

    @Override
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

    @Override
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
            throw new CstlServiceException("The service has throw a Runtime Exception:\n" +
                                           "Type:"      + e.getClass().getSimpleName() +
                                           "\nMessage:" + e.getMessage() +
                                           "Cause:" + e.getCause() ,
                    NO_APPLICABLE_CODE);
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureName) throws CstlServiceException {
        try {
            final SamplingPointTable foiTable = omDatabase.getTable(SamplingPointTable.class);
            return foiTable.getEntry(samplingFeatureName);
        } catch (NoSuchRecordException ex) {
            return null;
        } catch (CatalogException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("Catalog exception while getting the feature of interest",
                    NO_APPLICABLE_CODE, "featureOfInterest");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    @Override
    public ObservationEntry getObservation(String identifier, QName resultModel) throws CstlServiceException {
        try {
            if (resultModel.equals(Parameters.MEASUREMENT_QNAME)) {
                return (MeasurementEntry) measTable.getEntry(identifier);
            } else {
                return (ObservationEntry) obsTable.getEntry(identifier);
            }
        } catch (CatalogException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            String msg = " null";
            if ( ex.getMessage() != null) {
                msg =  ex.getMessage();
            } else if (ex.getCause() != null) {
                msg = ex.getCause().getMessage();
            }
            throw new CstlServiceException("Catalog exception while getting the observation with identifier: " + identifier +  " of type " + resultModel.getLocalPart() + msg,
                    NO_APPLICABLE_CODE, "getObservation");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    @Override
    public Object getResult(String identifier, QName resultModel) throws CstlServiceException {
        try {
            if (resultModel.equals(Parameters.MEASUREMENT_QNAME)) {
                final MeasureTable meaTable = omDatabase.getTable(MeasureTable.class);
                return meaTable.getEntry(identifier);
            } else {
                final AnyResultTable resTable = omDatabase.getTable(AnyResultTable.class);
                final Integer id = Integer.parseInt(identifier);
                return resTable.getEntry(id);
            }
            
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

    @Override
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

    @Override
    public void destroy() {
        try {
            newObservationIDStmt.close();
            observationExistStmt.close();
            getMinEventTimeOffering.close();
            omDatabase.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQL Exception while destroy observation reader");
        }
    }

    /**
     * Create a new identifier for an observation by searching in the O&M database.
     */
    @Override
    public String getNewObservationId() throws CstlServiceException {
        try {
            ResultSet res = newMeasurementIDStmt.executeQuery();
            int nbMeas = 0;
            if (res.next()) {
                nbMeas = res.getInt(1);
            }
            res.close();
            
            int nbObs = 0;
            res = newObservationIDStmt.executeQuery();
            if (res.next()) {
                nbObs = res.getInt(1);
            }
            res.close();


            int id = nbMeas + nbObs;

            //there is a possibility that someone delete some observation manually.
            // so we must verify that this id is not already assigned. if it is we must find a free identifier
            do {
                id ++;
                observationExistStmt.setString(1, observationIdBase + id);
                observationExistStmt.setString(2, observationIdBase + id);
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
    @Override
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

    @Override
    public String getInfos() {
        return "Constellation Postgrid O&M Reader 0.5";
    }

    @Override
    public List<ResponseModeType> getResponseModes() throws CstlServiceException {
        return Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
    }

    @Override
    public List<String> getResponseFormats() throws CstlServiceException {
        return Arrays.asList("text/xml; subtype=\"om/1.0.0\"");
    }
}
