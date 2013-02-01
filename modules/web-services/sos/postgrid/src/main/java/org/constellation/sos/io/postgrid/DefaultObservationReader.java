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

import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.gml.xml.v311.ReferenceType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.gml.xml.v321.AbstractTimeObjectType;
import org.geotoolkit.gml.xml.v321.DirectPositionType;
import org.geotoolkit.gml.xml.v321.EnvelopeType;
import org.geotoolkit.gml.xml.v321.FeaturePropertyType;
import org.geotoolkit.gml.xml.v321.LineStringType;
import org.geotoolkit.gml.xml.v321.PointType;
import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonType;
import org.geotoolkit.swe.xml.v101.PhenomenonType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.sampling.xml.v100.SamplingCurveType;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureType;
import org.geotoolkit.sampling.xml.v100.SamplingPointType;
import org.geotoolkit.sampling.xml.v200.SFSamplingFeatureType;
import org.geotoolkit.samplingspatial.xml.v200.SFSpatialSamplingFeatureType;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.SOSXmlFactory;
import org.geotoolkit.swe.xml.v101.AnyScalarPropertyType;
import org.geotoolkit.swe.xml.v101.DataArrayPropertyType;
import org.geotoolkit.swe.xml.v101.SimpleDataRecordType;
import org.geotoolkit.swe.xml.v101.TextBlockType;
import org.geotoolkit.swe.xml.v200.AbstractDataComponentType;
import org.geotoolkit.swe.xml.v200.BooleanType;
import org.geotoolkit.swe.xml.v200.CategoryType;
import org.geotoolkit.swe.xml.v200.CountRangeType;
import org.geotoolkit.swe.xml.v200.CountType;
import org.geotoolkit.swe.xml.v200.DataArrayType;
import org.geotoolkit.swe.xml.v200.DataArrayType.ElementType;
import org.geotoolkit.swe.xml.v200.DataArrayType.Encoding;
import org.geotoolkit.swe.xml.v200.DataRecordType;
import org.geotoolkit.swe.xml.v200.DataRecordType.Field;
import org.geotoolkit.swe.xml.v200.QuantityRangeType;
import org.geotoolkit.swe.xml.v200.QuantityType;
import org.geotoolkit.swe.xml.v200.TextType;
import org.geotoolkit.swe.xml.v200.TimeRangeType;
import org.geotoolkit.swe.xml.v200.TimeType;
import org.geotoolkit.swe.xml.v200.VectorType;
import org.opengis.observation.Measurement;
import org.opengis.observation.Observation;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;


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
     * @param dataSourceOM
     * @param observationIdBase
     */
    public DefaultObservationReader(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        this.observationIdBase = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
        this.phenomenonIdBase  = (String) properties.get(OMFactory.PHENOMENON_ID_BASE);
        this.sensorIdBase      = (String) properties.get(OMFactory.SENSOR_ID_BASE);
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
            if (version.equals("1.0.0")) {
                return offTable.getIdentifiers();
                
            // for 2.0 we adapt the offering with one by procedure   
            } else if (version.equals("2.0.0")) {
                final ProcessTable procTable = omDatabase.getTable(ProcessTable.class);
                final Set<String> procedures = procTable.getIdentifiers();
                final List<String> result = new ArrayList<String>();
                for (String procedure : procedures) {
                    if (procedure.startsWith(sensorIdBase)) {
                        procedure = procedure.replace(sensorIdBase, "");
                    }
                    result.add("offering-" + procedure);
                }
                return result;
            } else {
                throw new IllegalArgumentException("unexpected SOS version:" + version);
            }
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
            final String offeringNameVar;
            if (version.equals("2.0.0")) {
                final String procedureName = sensorIdBase + offeringName.substring(9);
                if (!getProcedureNames().contains(procedureName)) {
                    return null;
                }
                offeringNameVar = "offering-allSensor";
            } else {
                offeringNameVar = offeringName;
            }
            final ObservationOfferingType off =  offTable.getEntry(offeringNameVar);
            return convert(version, off, offeringName);
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
    
    private ObservationOffering convert(final String version, final ObservationOfferingType off, final String offeringName) {
        if (version.equals("2.0.0")) {
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
            if (version.equals("2.0.0")) {
                singleProcedure = sensorIdBase + offeringName.substring(9);
            } else {
                singleProcedure = null;
            }
            return new org.geotoolkit.sos.xml.v200.ObservationOfferingType(
                                               off.getId(),
                                               offeringName,
                                               off.getDescription(),
                                               env,
                                               period,
                                               singleProcedure,
                                               off.getObservedProperties(),
                                               off.getFeatureOfInterestIds(),
                                               off.getResponseFormat(),
                                               Arrays.asList("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation"));
        } else {
            return off;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final String version) throws CstlServiceException {
        try {
            final List<ObservationOffering> loo = new ArrayList<ObservationOffering>();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public PhenomenonType getPhenomenon(String phenomenonName) throws CstlServiceException {
        // we remove the phenomenon id base
        if (phenomenonName.indexOf(phenomenonIdBase) != -1) {
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
    public SamplingFeature getFeatureOfInterest(final String samplingFeatureName, final String version) throws CstlServiceException {
        //TODO remove those duplicated catch block
        try {
            final SamplingPointTable pointTable = omDatabase.getTable(SamplingPointTable.class);
            return convert(version, pointTable.getEntry(samplingFeatureName));
        } catch (NoSuchRecordException ex) {
            try {
                final SamplingFeatureTable foiTable = omDatabase.getTable(SamplingFeatureTable.class);
                return convert(version, foiTable.getEntry(samplingFeatureName));
            } catch (NoSuchRecordException ex2) {
                try {
                    final SamplingCurveTable curveTable = omDatabase.getTable(SamplingCurveTable.class);
                    return convert(version, curveTable.getEntry(samplingFeatureName));
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
    
    private SamplingFeature convert(final String version, final SamplingFeature feature) {
        if (version.equals("2.0.0")) {
            if (feature instanceof SamplingPointType) {
                final SamplingPointType sp = (SamplingPointType) feature;
                final FeaturePropertyType fp;
                if (sp.getSampledFeatures() != null && !sp.getSampledFeatures().isEmpty()) {
                    fp = new FeaturePropertyType(sp.getSampledFeatures().iterator().next().getHref());
                } else {
                    fp = null;
                }
                final PointType pt;
                if (sp.getPosition() != null) {
                    final DirectPositionType dp = new DirectPositionType(sp.getPosition().getPos());
                    pt = new PointType(sp.getPosition().getId(), dp);
                } else {
                    pt = null;
                }
                return new SFSpatialSamplingFeatureType(sp.getId(), sp.getName(), sp.getDescription(),
                        "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint", fp, pt, null);
            } else if (feature instanceof SamplingCurveType) {
                final SamplingCurveType sp = (SamplingCurveType) feature;
                final FeaturePropertyType fp;
                if (sp.getSampledFeatures() != null && !sp.getSampledFeatures().isEmpty()) {
                    fp = new FeaturePropertyType(sp.getSampledFeatures().iterator().next().getHref());
                } else {
                    fp = null;
                }
                final LineStringType pt;
                if (sp.getShape() != null && sp.getShape().getAbstractCurve() instanceof org.geotoolkit.gml.xml.v311.LineStringType) {
                    final org.geotoolkit.gml.xml.v311.LineStringType line311 = (org.geotoolkit.gml.xml.v311.LineStringType)sp.getShape().getAbstractCurve();
                    final List<org.geotoolkit.gml.xml.v321.DirectPositionType> positions = new ArrayList<org.geotoolkit.gml.xml.v321.DirectPositionType>();
                    for (org.geotoolkit.gml.xml.v311.DirectPositionType pos : line311.getPos()) {
                        positions.add(new DirectPositionType(pos.getValue()));
                    }
                    pt = new LineStringType(line311.getId(), positions);
                    
                } else {
                    pt = null;
                }
                final EnvelopeType env = new EnvelopeType(sp.getBoundedBy().getEnvelope());
                return new SFSpatialSamplingFeatureType(sp.getId(), sp.getName(), sp.getDescription(), 
                        "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingCurve", fp, pt, env);
            } else if (feature instanceof SamplingFeatureType) {
                final SamplingFeatureType sp = (SamplingFeatureType) feature;
                final FeaturePropertyType fp;
                if (sp.getSampledFeatures() != null && !sp.getSampledFeatures().isEmpty()) {
                    fp = new FeaturePropertyType(sp.getSampledFeatures().iterator().next().getHref());
                } else {
                    fp = null;
                }
                return new SFSamplingFeatureType(sp.getId(), sp.getName(), sp.getDescription(), 
                        "http://www.opengis.net/def/samplingFeatureType/OGC-OM/SF_SamplingFeature", fp, null);
            } else {
                throw new IllegalArgumentException("unexpected feature type.");
            }
        } else {
            return feature;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observation getObservation(final String identifier, final QName resultModel, final String version) throws CstlServiceException {
        try {
            if (resultModel.equals(MEASUREMENT_QNAME)) {
                return convert(version, measTable.getEntry(identifier));
            } else {
                return convert(version, obsTable.getEntry(identifier));
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
    
    private Observation convert(final String version, final Observation observation) {
       if (version.equals("2.0.0")) {
           final String name = observation.getName();
           final String type;
           if (observation instanceof Measurement) {
               type = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement";
           } else {
               type = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation";
           }
           final AbstractTimeObjectType time;
           if (observation.getSamplingTime() instanceof Period) {
               final Period p = (Period) observation.getSamplingTime();
               String dateBegin = null;
               if (p.getBeginning() != null && p.getBeginning().getPosition() != null) {
                   dateBegin = p.getBeginning().getPosition().getDateTime().toString();
               }
               String dateEnd = null;
               if (p.getEnding() != null && p.getEnding().getPosition() != null) {
                   dateEnd = p.getEnding().getPosition().getDateTime().toString();
               }
               time = (AbstractTimeObjectType) SOSXmlFactory.buildTimePeriod(version, dateBegin, dateEnd);
           } else if (observation.getSamplingTime() instanceof Instant) {
               final Instant p = (Instant) observation.getSamplingTime();
               String date = null;
               if (p.getPosition() != null) {
                   date = p.getPosition().getDateTime().toString();
               }
               time = (AbstractTimeObjectType) SOSXmlFactory.buildTimeInstant(version, date);
           } else if (observation.getSamplingTime() != null) {
               throw new IllegalArgumentException("Unexpected samplingTime type:" + observation.getSamplingTime().getClass().getName());
           } else {
               time = null;
           }
           final String procedure            = ((org.geotoolkit.observation.xml.Process)observation.getProcedure()).getHref();
           final String observedProperty     = ((org.geotoolkit.swe.xml.Phenomenon)observation.getObservedProperty()).getName();
           final SamplingFeature sf          = convert(version, (SamplingFeature)observation.getFeatureOfInterest());
           final FeaturePropertyType feature = (FeaturePropertyType) SOSXmlFactory.buildFeatureProperty(version, sf);
           final Object result;
           if (observation.getResult() instanceof DataArrayPropertyType) {
               final DataArrayPropertyType resultv100 = (DataArrayPropertyType) observation.getResult();
               final TextBlockType encodingV100 = (TextBlockType) resultv100.getDataArray().getEncoding();
               
               final int count = resultv100.getDataArray().getElementCount().getCount().getValue();
               final String id = resultv100.getDataArray().getId();
               final Encoding enc = new Encoding(encodingV100.getId(), encodingV100.getDecimalSeparator(), encodingV100.getTokenSeparator(), encodingV100.getBlockSeparator());
               final String values = resultv100.getDataArray().getValues();
               final SimpleDataRecordType recordv100 =  (SimpleDataRecordType) resultv100.getDataArray().getElementType();
               final List<Field> fields = new ArrayList<Field>();
               for (AnyScalarPropertyType scalar : recordv100.getField()) {
                   final AbstractDataComponentType component = convert(scalar.getValue());
                   fields.add(new Field(scalar.getName(), component));
               }
               final DataRecordType record = new DataRecordType(fields);
               final ElementType elem = new ElementType(resultv100.getDataArray().getName(), record);
               final org.geotoolkit.swe.xml.v200.DataArrayType array = new DataArrayType(id, count, enc, values, elem);
               final org.geotoolkit.swe.xml.v200.DataArrayPropertyType resultv200 = new org.geotoolkit.swe.xml.v200.DataArrayPropertyType(array);
               result = resultv200;
           } else {
               result = observation.getResult();
           }
           return new org.geotoolkit.observation.xml.v200.OMObservationType(name, type, time, procedure, observedProperty, feature, result);
       } else {
           return observation;
       }
    }
    
    private AbstractDataComponentType convert(org.geotoolkit.swe.xml.v101.AbstractDataComponentType data) {
        if (data instanceof org.geotoolkit.swe.xml.v101.BooleanType) {
            final org.geotoolkit.swe.xml.v101.BooleanType old = (org.geotoolkit.swe.xml.v101.BooleanType)data;
            return new BooleanType(old.isValue(), old.getDefinition());
        } else if (data instanceof org.geotoolkit.swe.xml.v101.VectorType) {
            final org.geotoolkit.swe.xml.v101.VectorType old = (org.geotoolkit.swe.xml.v101.VectorType)data;
            return new VectorType(); // TODO
        } else if (data instanceof org.geotoolkit.swe.xml.v101.TimeType) {
            final org.geotoolkit.swe.xml.v101.TimeType old = (org.geotoolkit.swe.xml.v101.TimeType)data;
            return new TimeType(old.getDefinition());
        } else if (data instanceof org.geotoolkit.swe.xml.v101.TimeRange) {
            final org.geotoolkit.swe.xml.v101.TimeRange old = (org.geotoolkit.swe.xml.v101.TimeRange)data;
            return new TimeRangeType(old.getDefinition(), old.getValue());
        } else if (data instanceof org.geotoolkit.swe.xml.v101.Category) {
            final org.geotoolkit.swe.xml.v101.Category old = (org.geotoolkit.swe.xml.v101.Category)data;
            return new CategoryType(old.getDefinition(), old.getValue());
        } else if (data instanceof org.geotoolkit.swe.xml.v101.QuantityRange) {
            final org.geotoolkit.swe.xml.v101.QuantityRange old = (org.geotoolkit.swe.xml.v101.QuantityRange)data;
            return new QuantityRangeType(old.getDefinition(), old.getValue());
        } else if (data instanceof org.geotoolkit.swe.xml.v101.CountRange) {
            final org.geotoolkit.swe.xml.v101.CountRange old = (org.geotoolkit.swe.xml.v101.CountRange)data;
            return new CountRangeType(old.getDefinition(), old.getValue());
        } else if (data instanceof org.geotoolkit.swe.xml.v101.QuantityType) {
            final org.geotoolkit.swe.xml.v101.QuantityType old = (org.geotoolkit.swe.xml.v101.QuantityType)data;
            String uomCode = null;
            if (old.getUom() != null) {
                uomCode = old.getUom().getCode();
            }
            return new QuantityType(old.getDefinition(), uomCode, old.getValue());
        } else if (data instanceof org.geotoolkit.swe.xml.v101.Text) {
            final org.geotoolkit.swe.xml.v101.Text old = (org.geotoolkit.swe.xml.v101.Text)data;
            return new TextType(old.getDefinition(), old.getValue());
        } else if (data instanceof org.geotoolkit.swe.xml.v101.Count) {
            final org.geotoolkit.swe.xml.v101.Count old = (org.geotoolkit.swe.xml.v101.Count)data;
            return new CountType(old.getDefinition(), old.getValue());
        } else {
            throw new IllegalArgumentException("Unexpected data component type:" + data);
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
