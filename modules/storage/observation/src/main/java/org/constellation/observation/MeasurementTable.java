/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.observation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.constellation.sampling.SamplingCurveTable;
import org.constellation.sampling.SamplingFeatureTable;
import org.constellation.sampling.SamplingPointTable;
import org.constellation.swe.v101.CompositePhenomenonTable;
import org.constellation.swe.v101.PhenomenonTable;
import org.geotoolkit.gml.xml.v311.AbstractTimeGeometricPrimitiveType;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.observation.xml.v100.MeasureType;
import org.geotoolkit.observation.xml.v100.MeasurementType;
import org.geotoolkit.observation.xml.v100.ProcessType;
import org.geotoolkit.sampling.xml.v100.SamplingCurveType;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureType;
import org.geotoolkit.sampling.xml.v100.SamplingPointType;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonType;
import org.geotoolkit.swe.xml.v101.PhenomenonType;
import org.opengis.observation.Measurement;

/**
 * Connexion vers la table des {@linkplain Measurement mesures}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 *
 * @see MergedMeasurementTable
 * @see org.constellation.measervation.coverage.MeasurementTableFiller
 */
public class MeasurementTable extends SingletonTable<MeasurementType> implements Cloneable {
    
    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-même une référence vers cette instance
     * de {@code measervationTable}, mais seule {@link StationType} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours être {@code measervationTable} d'abord, et {@code StationTable}
     * ensuite.
     */
    protected SamplingFeatureTable stations;
    
    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-même une référence vers cette instance
     * de {@code measervationTable}, mais seule {@link StationType} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours être {@code measervationTable} d'abord, et {@code StationTable}
     * ensuite.
     */
    protected SamplingPointTable stationPoints;

    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-même une référence vers cette instance
     * de {@code measervationTable}, mais seule {@link StationType} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours être {@code measervationTable} d'abord, et {@code StationTable}
     * ensuite.
     */
    protected SamplingCurveTable stationCurves;
    
    /**
     * Connexion vers la table des {@linkplain Phenomenon phénomènes}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected PhenomenonTable phenomenons;
    
    /**
     * Connexion vers la table des {@linkplain CompositePhenomenon phénomènes composés}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected CompositePhenomenonTable compositePhenomenons;
    
    /**
     * Connexion vers la table des {@linkplain Procedure procedures}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected ProcessTable procedures;
    
    /**
     * Connexion vers la table des {@linkplain Measure measure}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected MeasureTable measures;
    
    /**
     * Construit une nouvelle connexion vers la table des mesures.
     */
    public MeasurementTable(final Database database) {
        this(new MeasurementQuery(database));
    }
    
    /**
     * Super constructeur qui est appelé par les classe specialisant measervationTable.
     *
     * @param  database Connexion vers la base de données des measervations.
     */
    protected MeasurementTable(MeasurementQuery query) {
        super(query, query.byName);
    }
    

    /**
     * Construit une nouvelle table non partagée
     */
    private MeasurementTable(final MeasurementTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected MeasurementTable clone() {
        return new MeasurementTable(this);
    }
    
    /**
     * Construit une mesure pour l'enregistrement courant
     */
    @Override
    public MeasurementType createEntry(final LocalCache lc, final ResultSet result, Comparable<?> identifier) throws SQLException, CatalogException {
        final MeasurementQuery query = (MeasurementQuery) super.query;
        
        if (phenomenons == null) {
            phenomenons = getDatabase().getTable(PhenomenonTable.class);
        }
        PhenomenonType pheno = (PhenomenonType)phenomenons.getEntry(result.getString(indexOf(query.observedProperty)));
        
        if (compositePhenomenons == null) {
            compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
        }
        final CompositePhenomenonType compoPheno = compositePhenomenons.getEntry(result.getString(indexOf(query.observedPropertyComposite)));
        
        
        if (stations == null) {
            stations = getDatabase().getTable(SamplingFeatureTable.class);
        }
        SamplingFeatureType station = stations.getEntry(result.getString(indexOf(query.featureOfInterest)));
        
        if (stationPoints == null) {
            stationPoints = getDatabase().getTable(SamplingPointTable.class);
        }
        final SamplingPointType stationPoint = stationPoints.getEntry(result.getString(indexOf(query.featureOfInterestPoint)));

        if (stationCurves == null) {
            stationCurves = getDatabase().getTable(SamplingCurveTable.class);
        }
        final SamplingCurveType stationCurve = stationCurves.getEntry(result.getString(indexOf(query.featureOfInterest)));
        
        
        if (procedures == null) {
            procedures = getDatabase().getTable(ProcessTable.class);
        }
        final ProcessType procedure = procedures.getEntry(result.getString(indexOf(query.procedure)));
        
        if (measures == null) {
            measures = getDatabase().getTable(MeasureTable.class);
        }
        final MeasureType resultat = measures.getEntry(result.getString(indexOf(query.result)));
        
        if(pheno == null) pheno = compoPheno;
        if(station == null && stationCurve == null) station =  stationPoint;
        if(station == null && stationPoint == null) station =  stationCurve;
        
        final Timestamp begin = result.getTimestamp(indexOf(query.samplingTimeBegin));
        final Timestamp end = result.getTimestamp(indexOf(query.samplingTimeEnd));
        AbstractTimeGeometricPrimitiveType samplingTime = null;
        TimePositionType beginPosition = null;
        TimePositionType endPosition   = null;
        if (begin != null) {
            beginPosition = new TimePositionType(begin.toString());
        }
        if (end != null) {
            endPosition = new TimePositionType(end.toString());
        }
        
        if (beginPosition != null && endPosition != null) {
            samplingTime = new TimePeriodType(beginPosition, endPosition);
        
        } else if (begin != null && end == null) {
            samplingTime =  new TimeInstantType(beginPosition);
        
        //this case will normally never append
        } else if (begin == null && end != null) {
            samplingTime =  new TimeInstantType(endPosition);
        } 
        
        return new MeasurementType(result.getString(indexOf(query.name   )),
                                    result.getString(indexOf(query.description)),
                                    station,
                                    pheno,
                                    procedure,
                                    //manque quality
                                    resultat,
                                    samplingTime);
    }
    
    
    /**
     * Retourne un nouvel identifier (ou l'identifier de la mesure passée en parametre si non-null)
     * et enregistre la nouvelle mesure dans la base de donnée.
     *
     * @param l'measervation a inserer dans la base de donnée.
     */
    public String getIdentifier(final Measurement meas) throws SQLException, CatalogException {
        final MeasurementQuery query = (MeasurementQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                if (meas.getName() != null) {
                    final Stmt statement = getStatement(lc, QueryType.EXISTS);
                    statement.statement.setString(indexOf(query.name), meas.getName());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc, statement);
                        return meas.getName();
                    } else {
                        id = meas.getName();
                    }
                    result.close();
                    release(lc, statement);
                } else {
                    id = searchFreeIdentifier(lc, "urn:ogc:object:measurement:");
                }
                final Stmt statement = getStatement(lc, QueryType.INSERT);
                statement.statement.setString(indexOf(query.name),         id);
                statement.statement.setString(indexOf(query.description),  meas.getDefinition());
                statement.statement.setString(indexOf(query.distribution), "normale");

                // on insere la station qui a effectué cette measervation
                if (meas.getFeatureOfInterest() instanceof SamplingPointType){
                    final SamplingPointType station = (SamplingPointType)meas.getFeatureOfInterest();
                    if (stationPoints == null) {
                        stationPoints = getDatabase().getTable(SamplingPointTable.class);
                    }
                    statement.statement.setString(indexOf(query.featureOfInterestPoint),stationPoints.getIdentifier(station));
                    statement.statement.setNull(indexOf(query.featureOfInterest),    java.sql.Types.VARCHAR);
                    statement.statement.setNull(indexOf(query.featureOfInterestCurve),    java.sql.Types.VARCHAR);

                } else  if (meas.getFeatureOfInterest() instanceof SamplingCurveType){
                    final SamplingCurveType station = (SamplingCurveType)meas.getFeatureOfInterest();
                    if (stationCurves == null) {
                        stationCurves = getDatabase().getTable(SamplingCurveTable.class);
                    }
                    statement.statement.setString(indexOf(query.featureOfInterestCurve),stationCurves.getIdentifier(station));
                    statement.statement.setNull(indexOf(query.featureOfInterestPoint),    java.sql.Types.VARCHAR);
                    statement.statement.setNull(indexOf(query.featureOfInterest),    java.sql.Types.VARCHAR);


                } else  if (meas.getFeatureOfInterest() instanceof SamplingFeatureType){
                    final SamplingFeatureType station = (SamplingFeatureType)meas.getFeatureOfInterest();
                    if (stations == null) {
                        stations = getDatabase().getTable(SamplingFeatureTable.class);
                    }
                    statement.statement.setString(indexOf(query.featureOfInterest),stations.getIdentifier(station));
                    statement.statement.setNull(indexOf(query.featureOfInterestPoint),    java.sql.Types.VARCHAR);
                    statement.statement.setNull(indexOf(query.featureOfInterestCurve),    java.sql.Types.VARCHAR);

                } else {
                    statement.statement.setNull(indexOf(query.featureOfInterest),    java.sql.Types.VARCHAR);
                    statement.statement.setNull(indexOf(query.featureOfInterestPoint),    java.sql.Types.VARCHAR);
                    statement.statement.setNull(indexOf(query.featureOfInterestCurve),    java.sql.Types.VARCHAR);
                }

                // on insere le phenomene measervé
                if(meas.getObservedProperty() instanceof CompositePhenomenonType){
                    final CompositePhenomenonType pheno = (CompositePhenomenonType)meas.getObservedProperty();
                    if (compositePhenomenons == null) {
                        compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
                    }
                    statement.statement.setString(indexOf(query.observedPropertyComposite), compositePhenomenons.getIdentifier(pheno));
                    statement.statement.setNull(indexOf(query.observedProperty), java.sql.Types.VARCHAR);

                } else if(meas.getObservedProperty() instanceof PhenomenonType){
                    final PhenomenonType pheno = (PhenomenonType)meas.getObservedProperty();
                    if (phenomenons == null) {
                        phenomenons = getDatabase().getTable(PhenomenonTable.class);
                    }
                    statement.statement.setString(indexOf(query.observedProperty), phenomenons.getIdentifier(pheno));
                    statement.statement.setNull(indexOf(query.observedPropertyComposite), java.sql.Types.VARCHAR);

                } else {
                    statement.statement.setNull(indexOf(query.observedProperty), java.sql.Types.VARCHAR);
                    statement.statement.setNull(indexOf(query.observedPropertyComposite), java.sql.Types.VARCHAR);
                }

                //on insere le capteur
                if (meas.getProcedure() != null) {
                    final ProcessType process = (ProcessType)meas.getProcedure();
                    if (procedures == null) {
                        procedures = getDatabase().getTable(ProcessTable.class);
                    }
                    statement.statement.setString(indexOf(query.procedure), procedures.getIdentifier(process));
                } else {
                    statement.statement.setNull(indexOf(query.procedure), java.sql.Types.VARCHAR);
                }

                // on insere le resultat
                if (meas.getResult() != null){
                    if (measures == null) {
                        measures = getDatabase().getTable(MeasureTable.class);
                    }
                    statement.statement.setString(indexOf(query.result), measures.getIdentifier((MeasureType)meas.getResult()));
                } else {
                    statement.statement.setNull(indexOf(query.result), java.sql.Types.VARCHAR);
                }

                // on insere le "samplingTime""
                if (meas.getSamplingTime() != null){
                    if (meas.getSamplingTime() instanceof TimePeriodType) {

                        final TimePeriodType sampTime = (TimePeriodType)meas.getSamplingTime();
                        final String s = sampTime.getBeginPosition().getValue();
                        Timestamp date = Timestamp.valueOf(s);
                        statement.statement.setTimestamp(indexOf(query.samplingTimeBegin), date);

                        if (sampTime.getEndPosition().getIndeterminatePosition() == null) {

                            sampTime.getEndPosition().getValue();
                            date = Timestamp.valueOf(s);
                            statement.statement.setTimestamp(indexOf(query.samplingTimeEnd),  date);

                        } else {
                            statement.statement.setNull(indexOf(query.samplingTimeEnd),   java.sql.Types.DATE);
                        }

                    } else if (meas.getSamplingTime() instanceof TimeInstantType) {
                        final TimeInstantType sampTime = (TimeInstantType)meas.getSamplingTime();
                        final String s = sampTime.getTimePosition().getValue();
                        final Timestamp date = Timestamp.valueOf(s);
                        statement.statement.setTimestamp(indexOf(query.samplingTimeBegin),  date);
                        statement.statement.setNull(indexOf(query.samplingTimeEnd), java.sql.Types.DATE);

                    } else {
                        throw new IllegalArgumentException("type allowed for sampling time: TimePeriod or TimeInstant");
                    }
                } else {
                    statement.statement.setNull(indexOf(query.samplingTimeBegin), java.sql.Types.DATE);
                    statement.statement.setNull(indexOf(query.samplingTimeEnd),   java.sql.Types.DATE);
                }
                statement.statement.setNull(indexOf(query.resultDefinition),   java.sql.Types.VARCHAR);

                updateSingleton(statement.statement);
                release(lc, statement);
                success = true;
            } finally {
                transactionEnd(lc, success);
            }
        }
        return id;
    }
    
}
