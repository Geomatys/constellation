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

// J2SE dependencies
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;

// Constellation dependencies
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchRecordException;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.gml.v311.ReferenceEntry;
import org.constellation.gml.v311.ReferenceTable;
import org.constellation.observation.ObservationEntry;
import org.constellation.observation.ObservationTable;
import org.constellation.observation.ProcessTable;
import org.constellation.sampling.SamplingFeatureEntry;
import org.constellation.sampling.SamplingFeatureTable;
import org.constellation.sampling.SamplingPointTable;
import org.constellation.sos.ObservationOfferingEntry;
import org.constellation.sos.ObservationOfferingTable;
import org.constellation.swe.v101.AnyResultEntry;
import org.constellation.swe.v101.AnyResultTable;
import org.constellation.swe.v101.CompositePhenomenonEntry;
import org.constellation.swe.v101.CompositePhenomenonTable;
import org.constellation.swe.v101.PhenomenonEntry;
import org.constellation.swe.v101.PhenomenonTable;
import org.constellation.ws.WebServiceException;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 * Default Observation reader for Postgrid O&M database.
 *
 * @author Guilhem Legal
 */
public class DefaultObservationReader extends ObservationReader {

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
     * An SQL statement finding the last Observation ID recorded
     */
    private final PreparedStatement newObservationIDStmt;

    /**
     * An SQL statement verying if the specified observation already exist.
     */
    private final PreparedStatement observationExistStmt;

    /**
     * An SQL statement get the minimal eventime for the observation offering
     */
    private final PreparedStatement getMinEventTimeOffering;

    /**
     *
     * @param dataSourceOM
     * @param observationIdBase
     */
    public DefaultObservationReader(DataSource dataSourceOM, String observationIdBase) throws WebServiceException {
        super(observationIdBase);
        try {
            OMDatabase = new Database(dataSourceOM);
            //we build the database table frequently used.
            obsTable = OMDatabase.getTable(ObservationTable.class);
            offTable = OMDatabase.getTable(ObservationOfferingTable.class);
            refTable = OMDatabase.getTable(ReferenceTable.class);
            //we build the prepared Statement
            newObservationIDStmt = OMDatabase.getConnection().prepareStatement("SELECT Count(*) FROM \"observations\" WHERE name LIKE '%" + observationIdBase + "%' ");
            observationExistStmt = OMDatabase.getConnection().prepareStatement("SELECT name FROM \"observations\" WHERE name=?");
            getMinEventTimeOffering = OMDatabase.getConnection().prepareStatement("select MIN(event_time_begin) from observation_offerings");
        } catch (SQLException ex) {
            throw new WebServiceException("SQL Exception while initalizing the O&M reader:" + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (NoSuchTableException ex) {
            throw new WebServiceException("NoSuchTable Exception while initalizing the O&M reader:" + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (IOException ex) {
             throw new WebServiceException("IO Exception while initalizing the O&M reader:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }


    }

    public Set<String> getOfferingNames() throws WebServiceException {
        try {
            return offTable.getIdentifiers();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public ObservationOfferingEntry getObservationOffering(String offeringName) throws WebServiceException {
        try {
            return offTable.getEntry(offeringName);
        } catch (NoSuchRecordException ex) {
            return null;
        } catch (CatalogException ex) {
            throw new WebServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                                             NO_APPLICABLE_CODE);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    public List<ObservationOfferingEntry> getObservationOfferings() throws WebServiceException {
        try {
            List<ObservationOfferingEntry> loo = new ArrayList<ObservationOfferingEntry>();
            Set<ObservationOfferingEntry> set = offTable.getEntries();
            loo.addAll(set);
            return loo;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public Set<String> getProcedureNames() throws WebServiceException {
        try {
            ProcessTable procTable = OMDatabase.getTable(ProcessTable.class);
            return procTable.getIdentifiers();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public Set<String> getPhenomenonNames() throws WebServiceException {
        try {
            PhenomenonTable phenoTable = OMDatabase.getTable(PhenomenonTable.class);
            Set<String> phenoNames = phenoTable.getIdentifiers();
            CompositePhenomenonTable compoPhenoTable = OMDatabase.getTable(CompositePhenomenonTable.class);
            Set<String> compoPhenoNames = compoPhenoTable.getIdentifiers();
            phenoNames.addAll(compoPhenoNames);
            phenoNames.remove("");
            return phenoNames;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public PhenomenonEntry getPhenomenon(String phenomenonName) throws WebServiceException {
        try {
            CompositePhenomenonTable compositePhenomenonTable = OMDatabase.getTable(CompositePhenomenonTable.class);
            CompositePhenomenonEntry cphen = null;
            try {
                cphen = compositePhenomenonTable.getEntry(phenomenonName);
            } catch (NoSuchRecordException ex) {
            //we let continue to look if it is a phenomenon (simple)
            }
            PhenomenonTable phenomenonTable = OMDatabase.getTable(PhenomenonTable.class);
            return (PhenomenonEntry) phenomenonTable.getEntry(phenomenonName);

        } catch (NoSuchRecordException ex) {
            return null;

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public Set<String> getFeatureOfInterestNames() throws WebServiceException {
        try {
            SamplingFeatureTable featureTable = OMDatabase.getTable(SamplingFeatureTable.class);
            Set<String> featureNames = featureTable.getIdentifiers();
            SamplingPointTable pointTable = OMDatabase.getTable(SamplingPointTable.class);
            Set<String> pointNames = pointTable.getIdentifiers();
            featureNames.addAll(pointNames);
            return featureNames;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureName) throws WebServiceException {
        try {
            SamplingPointTable foiTable = OMDatabase.getTable(SamplingPointTable.class);
            return foiTable.getEntry(samplingFeatureName);
        } catch (NoSuchRecordException ex) {
            return null;
        } catch (CatalogException ex) {
            throw new WebServiceException("Catalog exception while getting the feature of interest",
                    NO_APPLICABLE_CODE, "featureOfInterest");
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public ObservationEntry getObservation(String identifier) throws WebServiceException {
        try {
            return (ObservationEntry) obsTable.getEntry(identifier);
        } catch (CatalogException ex) {
            throw new WebServiceException("Catalog exception while getting the observations: " + ex.getMessage(),
                    NO_APPLICABLE_CODE, "getObservation");
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public AnyResultEntry getResult(String identifier) throws WebServiceException {
        try {
            AnyResultTable resTable = OMDatabase.getTable(AnyResultTable.class);
            return resTable.getEntry(identifier);
        } catch (CatalogException ex) {
            throw new WebServiceException("Catalog exception while getting the observations: " + ex.getMessage(),
                    NO_APPLICABLE_CODE, "getResult");
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    public Set<ReferenceEntry> getReferences() throws WebServiceException {
        try {
            return refTable.getEntries();

        } catch (NoSuchRecordException ex) {
            logger.info("NoSuchRecordException in getReferences");
            return null;

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                NO_APPLICABLE_CODE);

        } catch (CatalogException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
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
            OMDatabase.close();
        } catch (CatalogException ex) {
            logger.severe("Catalog Exception while destroy observation reader");
        } catch (SQLException ex) {
            logger.severe("SQL Exception while destroy observation reader");
        }
    }

    /**
     * Create a new identifier for an observation by searching in the O&M database.
     */
    public String getNewObservationId() throws WebServiceException {
        try {
            ResultSet res = newObservationIDStmt.executeQuery();
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
            e.printStackTrace();
            throw new WebServiceException("The service has throw a SQLException:" + e.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }

    /**
     * Return the minimal value for the offering event Time
     */
    public String getMinimalEventTime() throws WebServiceException {
        String ret = null;
        try {
            ResultSet res = getMinEventTimeOffering.executeQuery();
            Timestamp t = null;
            while (res.next()) {
                t = res.getTimestamp(1);
            }

            if (t != null) {
                ret = t.toString();
            }
            res.close();

        } catch (SQLException ex) {
           ex.printStackTrace();
           throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
        }
        return ret;
    }

}
