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
import java.util.Map;
import java.util.Date;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.sos.factory.OMFactory;
import org.geotoolkit.gml.xml.v311.AbstractTimePrimitiveType;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.NoSuchRecordException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.gml.v311.ReferenceTable;
import org.constellation.observation.MeasureTable;
import org.constellation.observation.MeasurementTable;
import org.constellation.observation.ObservationTable;
import org.constellation.observation.ProcessTable;
import org.constellation.observation.SpecialOperationsTable;
import org.constellation.sampling.SamplingCurveTable;
import org.constellation.sampling.SamplingFeatureTable;
import org.constellation.sampling.SamplingPointTable;
import org.constellation.sos.ObservationOfferingTable;
import org.constellation.sos.io.ObservationReader;
import org.constellation.swe.v101.AnyResultTable;
import org.constellation.swe.v101.CompositePhenomenonTable;
import org.constellation.swe.v101.PhenomenonTable;
import org.constellation.ws.CstlServiceException;
import static org.constellation.sos.ws.SOSConstants.*;

import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.gml.xml.v311.ReferenceType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.gml.xml.v321.EnvelopeType;
import org.geotoolkit.observation.xml.v100.MeasurementType;
import org.geotoolkit.observation.xml.v100.ObservationType;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureType;
import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonType;
import org.geotoolkit.swe.xml.v101.PhenomenonType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.sos.xml.ObservationOffering;


/**
 * Default Observation reader for Postgrid O&M database.
 *
 * @author Guilhem Legal
 */
public class DefaultObservationReader implements ObservationReader {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos");

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
     * A database table for insert and get observation offering.
     */
    private final ObservationOfferingTable offTable;

    /**
     * A database table for insert and get reference object.
     */
    private final ReferenceTable refTable;

    /**
     * An SQL statement verifying if the specified observation already exist.
     */
    private final SpecialOperationsTable specialTable;


    private static final String SQL_ERROR_MSG = "The service has throw a SQL Exception:";

    private static final String CAT_ERROR_MSG = "The service has throw a Catalog Exception:";


    /**
     *
     * @param dataSourceOM
     * @param observationIdBase
     */
    public DefaultObservationReader(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        this.observationIdBase = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object (DefaultObservationReader)", NO_APPLICABLE_CODE);
        }
        try {
            omDatabase = DatabasePool.getDatabase(db);
            //verify the validity of the connection
            final Connection c = omDatabase.getDataSource(true).getConnection();
            c.close();
            //omDatabase.setProperty(ConfigurationKey.READONLY, "false");

            //we build the database table frequently used.
            obsTable     = omDatabase.getTable(ObservationTable.class);
            measTable    = omDatabase.getTable(MeasurementTable.class);
            offTable     = omDatabase.getTable(ObservationOfferingTable.class);
            refTable     = omDatabase.getTable(ReferenceTable.class);
            specialTable = omDatabase.getTable(SpecialOperationsTable.class);

        } catch (SQLException ex) {
            throw new CstlServiceException("SQL Exception while initalizing the O&M reader:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getOfferingNames(final String version) throws CstlServiceException {
        try {
            return offTable.getIdentifiers();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final List<String> offeringNames, final String version) throws CstlServiceException {
        final List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
        for (String offeringName : offeringNames) {
            offerings.add(getObservationOffering(offeringName, version));
        }
        return offerings;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationOffering getObservationOffering(final String offeringName, final String version) throws CstlServiceException {
        try {
            final ObservationOfferingType off =  offTable.getEntry(offeringName);
            if (version.equals("2.0.0")) {
                return offeringToV200(version, off);
            } else {
                return off;
            }
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
    
    private ObservationOffering offeringToV200(final String version, final ObservationOfferingType off) {
        final EnvelopeType env;
                if (off.getBoundedBy() != null && off.getBoundedBy().getEnvelope() != null) {
                    env = new EnvelopeType(off.getBoundedBy().getEnvelope());
                } else {
                    env = null;
                }
                final org.geotoolkit.gml.xml.v321.TimePeriodType period;
                if (off.getTime() != null) {
                    final TimePeriodType pv100 = (TimePeriodType) off.getTime();
                    period = new org.geotoolkit.gml.xml.v321.TimePeriodType(pv100.getBeginPosition().getValue(), pv100.getEndPosition().getValue());
                } else {
                    period = null;
                }
                final String singleProcedure;
                if (version.equals("2.0.0") && off.getProcedures().size() > 1) {
                    LOGGER.warning("multiple procedure unsuported in V2.0.0");
                    singleProcedure = off.getProcedures().get(0);
                } else if (version.equals("2.0.0") && off.getProcedures().size() == 1) {
                    singleProcedure = off.getProcedures().get(0);
                } else {
                    singleProcedure = null;
                }
                return new org.geotoolkit.sos.xml.v200.ObservationOfferingType(
                                                   off.getId(),
                                                   off.getName(),
                                                   off.getDescription(),
                                                   env,
                                                   period,
                                                   singleProcedure,
                                                   off.getObservedProperties(),
                                                   off.getFeatureOfInterestIds(),
                                                   off.getResponseFormat(),
                                                   Arrays.asList("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final String version) throws CstlServiceException {
        try {
            final List<ObservationOffering> loo = new ArrayList<ObservationOffering>();
            final Set<ObservationOfferingType> set  = offTable.getEntries();
            if (version.equals("2.0.0")) {
                for (ObservationOfferingType off : set) {
                    loo.add(offeringToV200(version, off));
                }
            } else {
                
                loo.addAll(set);
            }
            return loo;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        }  catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException("The service has throw a Runtime Exception:\n" +
                                           "Type:"      + e.getClass().getSimpleName() +
                                           "\nMessage:" + e.getMessage() +
                                           "Cause:" + e.getCause() ,
                    NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getProcedureNames() throws CstlServiceException {
        try {
            final ProcessTable procTable = omDatabase.getTable(ProcessTable.class);
            return procTable.getIdentifiers();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
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

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PhenomenonType getPhenomenon(final String phenomenonName) throws CstlServiceException {
        try {
            final CompositePhenomenonTable compositePhenomenonTable = omDatabase.getTable(CompositePhenomenonTable.class);
            CompositePhenomenonType cphen = null;
            try {
                cphen = compositePhenomenonTable.getEntry(phenomenonName);
            } catch (NoSuchRecordException ex) {
            //we let continue to look if it is a phenomenon (simple)
            }
            if (cphen != null) {
                return cphen;
            }
            final PhenomenonTable phenomenonTable = omDatabase.getTable(PhenomenonTable.class);
            return (PhenomenonType) phenomenonTable.getEntry(phenomenonName);

        } catch (NoSuchRecordException ex) {
            return null;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getFeatureOfInterestNames() throws CstlServiceException {
        try {
            final SamplingFeatureTable featureTable = omDatabase.getTable(SamplingFeatureTable.class);
            final Set<String> featureNames          = featureTable.getIdentifiers();
            final SamplingPointTable pointTable     = omDatabase.getTable(SamplingPointTable.class);
            final Set<String> pointNames            = pointTable.getIdentifiers();
            featureNames.addAll(pointNames);
            final SamplingCurveTable curveTable     = omDatabase.getTable(SamplingCurveTable.class);
            final Set<String> curveNames            = curveTable.getIdentifiers();
            featureNames.addAll(curveNames);
            return featureNames;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SamplingFeatureType getFeatureOfInterest(final String samplingFeatureName) throws CstlServiceException {
        //TODO remove those duplicated catch block
        try {
            final SamplingPointTable pointTable = omDatabase.getTable(SamplingPointTable.class);
            return pointTable.getEntry(samplingFeatureName);
        } catch (NoSuchRecordException ex) {
            try {
                final SamplingFeatureTable foiTable = omDatabase.getTable(SamplingFeatureTable.class);
                return foiTable.getEntry(samplingFeatureName);
            } catch (NoSuchRecordException ex2) {
                try {
                    final SamplingCurveTable curveTable = omDatabase.getTable(SamplingCurveTable.class);
                    return curveTable.getEntry(samplingFeatureName);
                } catch (NoSuchRecordException ex3) {
                    return null;
                }  catch (CatalogException ex3) {
                    LOGGER.log(Level.SEVERE, ex3.getMessage(), ex3);
                    throw new CstlServiceException("Catalog exception while getting the feature of interest",
                        NO_APPLICABLE_CODE, "featureOfInterest");
                } catch (SQLException ex3) {
                    LOGGER.log(Level.SEVERE, ex3.getMessage(), ex3);
                    throw new CstlServiceException(SQL_ERROR_MSG + ex3.getMessage(),
                        NO_APPLICABLE_CODE);
                }
            } catch (CatalogException ex2) {
                LOGGER.log(Level.SEVERE, ex2.getMessage(), ex2);
                throw new CstlServiceException("Catalog exception while getting the feature of interest",
                    NO_APPLICABLE_CODE, "featureOfInterest");
            } catch (SQLException ex2) {
                LOGGER.log(Level.SEVERE, ex2.getMessage(), ex2);
                throw new CstlServiceException(SQL_ERROR_MSG + ex2.getMessage(),
                    NO_APPLICABLE_CODE);
            }
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationType getObservation(final String identifier, final QName resultModel) throws CstlServiceException {
        try {
            if (resultModel.equals(MEASUREMENT_QNAME)) {
                return (MeasurementType) measTable.getEntry(identifier);
            } else {
                return (ObservationType) obsTable.getEntry(identifier);
            }
        } catch (CatalogException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            String msg = " null";
            if ( ex.getMessage() != null) {
                msg =  ex.getMessage();
            } else if (ex.getCause() != null) {
                msg = ex.getCause().getMessage();
            }
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            throw new CstlServiceException("Catalog exception while getting the observation with identifier: " + identifier +  " of type " + resultModel.getLocalPart() + " " + msg,
                    NO_APPLICABLE_CODE, "getObservation");
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getResult(final String identifier, final QName resultModel) throws CstlServiceException {
        try {
            if (resultModel.equals(MEASUREMENT_QNAME)) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existProcedure(final String href) throws CstlServiceException {
        try {
            final Set<ReferenceType> references = refTable.getEntries();
            if (references != null) {
                final Iterator<ReferenceType> it = references.iterator();
                while (it.hasNext()) {
                    final ReferenceType ref = it.next();
                    if (ref != null && ref.getHref() != null && ref.getHref().equals(href)) {
                        return true;
                    }
                }
            }
            return false;

        } catch (NoSuchRecordException ex) {
            LOGGER.info("NoSuchRecordException in getReferences");
            return false;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewObservationId() throws CstlServiceException {
        try {
            final int nbMeas = specialTable.measureCount(observationIdBase);
            final int nbObs  = specialTable.observationCount(observationIdBase);
            int id = nbMeas + nbObs;

            //there is a possibility that someone delete some observation manually.
            // so we must verify that this id is not already assigned. if it is we must find a free identifier
            boolean again;
            do {
                id ++;
                again = specialTable.observationExists(observationIdBase + id);
            } while (again);
            return observationIdBase + id;
        } catch( SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException("The service has throw a SQLException:" + e.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractTimePrimitiveType getFeatureOfInterestTime(final String samplingFeatureName) throws CstlServiceException {
        try {
            List<Date> bounds = specialTable.getTimeForStation(samplingFeatureName);
            return new TimePeriodType(new TimePositionType(bounds.get(0)), new TimePositionType(bounds.get(1)));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CstlServiceException("The service has throw a SQLException:" + e.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEventTime() throws CstlServiceException {
        String ret = null;
        try {

            final Timestamp t = specialTable.getMinTimeOffering();
            if (t != null) {
                ret = t.toString();
            }
        } catch (SQLException ex) {
           LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
           throw new CstlServiceException(SQL_ERROR_MSG + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
        }
        if  (ret != null) {
            return Arrays.asList(ret);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Postgrid O&M Reader 0.9";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResponseModeType> getResponseModes() throws CstlServiceException {
        return Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getResponseFormats() throws CstlServiceException {
        return Arrays.asList("text/xml; subtype=\"om/1.0.0\"");
    }
}
