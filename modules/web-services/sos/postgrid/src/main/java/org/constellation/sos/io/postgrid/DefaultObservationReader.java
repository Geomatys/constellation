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
import java.util.Collection;
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

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.gml.xml.v311.ReferenceType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.observation.xml.OMXmlFactory;
import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonType;
import org.geotoolkit.sos.xml.ObservationOffering;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.sos.xml.SOSXmlFactory;
import org.geotoolkit.swe.xml.v101.AnyResultType;

import org.opengis.observation.Observation;
import org.opengis.observation.sampling.SamplingFeature;


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
    
    protected final String observationTemplateIdBase;

    protected final String phenomenonIdBase;
    
    protected final String sensorIdBase;
    
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
     * @param configuration
     * @param properties
     * 
     * @throws org.constellation.ws.CstlServiceException
     */
    public DefaultObservationReader(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        this.observationIdBase = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
        this.phenomenonIdBase  = (String) properties.get(OMFactory.PHENOMENON_ID_BASE);
        this.sensorIdBase      = (String) properties.get(OMFactory.SENSOR_ID_BASE);
        this.observationTemplateIdBase = (String) properties.get(OMFactory.OBSERVATION_TEMPLATE_ID_BASE);
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
    public Collection<String> getOfferingNames(final String version) throws CstlServiceException {
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
        final List<ObservationOffering> offerings = new ArrayList<>();
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
            final ObservationOfferingType off = new ObservationOfferingType(offTable.getEntry(offeringName));
            if (version.equals("2.0.0")) {
                off.setName(offeringName);
                off.setProcedures(Arrays.asList(sensorIdBase + offeringName.substring(9)));
            }
            return SOSXmlFactory.convert(version, off);
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final String version) throws CstlServiceException {
        try {
            final List<ObservationOffering> loo = new ArrayList<>();
            if (version.equals("2.0.0")) {
                final Collection<String> offeringNames = getOfferingNames(version);
                for (String offeringName : offeringNames) {
                    loo.add(getObservationOffering(offeringName, version));
                }
            } else {
                final Set<ObservationOfferingType> set  = offTable.getEntries();
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

    @Override
    public Collection<String> getProceduresForPhenomenon(String observedProperty) throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }

    @Override
    public Collection<String> getPhenomenonsForProcedure(String sensorID) throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existPhenomenon(String phenomenonName) throws CstlServiceException {
        // we remove the phenomenon id base
        if (phenomenonName.contains(phenomenonIdBase)) {
            phenomenonName = phenomenonName.replace(phenomenonIdBase, "");
        }
        try {
            final CompositePhenomenonTable compositePhenomenonTable = omDatabase.getTable(CompositePhenomenonTable.class);
            CompositePhenomenonType cphen = null;
            try {
                cphen = compositePhenomenonTable.getEntry(phenomenonName);
            } catch (NoSuchRecordException ex) {
            //we let continue to look if it is a phenomenon (simple)
            }
            if (cphen != null) {
                return true;
            }
            final PhenomenonTable phenomenonTable = omDatabase.getTable(PhenomenonTable.class);
            return phenomenonTable.getEntry(phenomenonName) != null;

        } catch (NoSuchRecordException ex) {
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
    public SamplingFeature getFeatureOfInterest(final String samplingFeatureName, final String version) throws CstlServiceException {
        //TODO remove those duplicated catch block
        try {
            final SamplingPointTable pointTable = omDatabase.getTable(SamplingPointTable.class);
            return OMXmlFactory.convert(version, pointTable.getEntry(samplingFeatureName));
        } catch (NoSuchRecordException ex) {
            try {
                final SamplingFeatureTable foiTable = omDatabase.getTable(SamplingFeatureTable.class);
                return OMXmlFactory.convert(version, foiTable.getEntry(samplingFeatureName));
            } catch (NoSuchRecordException ex2) {
                try {
                    final SamplingCurveTable curveTable = omDatabase.getTable(SamplingCurveTable.class);
                    return OMXmlFactory.convert(version, curveTable.getEntry(samplingFeatureName));
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
    public Observation getObservation(final String identifier, final QName resultModel, final ResponseModeType mode, final String version) throws CstlServiceException {
        try {
            final AbstractObservation result;
            if (resultModel.equals(MEASUREMENT_QNAME)) {
                result = OMXmlFactory.convert(version, measTable.getEntry(identifier));
            } else {
                result = OMXmlFactory.convert(version, obsTable.getEntry(identifier));
            }
            if (identifier.startsWith(observationIdBase)) {
                result.setId("obs-" + identifier.substring(observationIdBase.length()));
            } else if (identifier.startsWith(observationTemplateIdBase)) {
                result.setId("obs-" + identifier.substring(observationTemplateIdBase.length()));
            }
            return result;
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
    public Object getResult(final String identifier, final QName resultModel, final String version) throws CstlServiceException {
        try {
            if (resultModel.equals(MEASUREMENT_QNAME)) {
                final MeasureTable meaTable = omDatabase.getTable(MeasureTable.class);
                return meaTable.getEntry(identifier);
            } else {
                final AnyResultTable resTable = omDatabase.getTable(AnyResultTable.class);
                final Integer id = Integer.parseInt(identifier);
                final AnyResultType result = resTable.getEntry(id);
                return result.getPropertyArray();
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
    public AbstractTimePrimitiveType getFeatureOfInterestTime(final String samplingFeatureName, final String version) throws CstlServiceException {
        try {
            final List<Date> bounds = specialTable.getTimeForStation(samplingFeatureName);
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

    @Override
    public AbstractGeometry getSensorLocation(String sensorID, String version) throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }
}
