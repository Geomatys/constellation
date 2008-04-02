/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.observation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.SingletonTable;
import net.seagis.coverage.model.Distribution;
import net.seagis.coverage.model.DistributionEntry;
import net.seagis.coverage.model.DistributionTable;
import net.seagis.gml.AbstractTimeGeometricPrimitiveType;
import net.seagis.gml.TimeInstantType;
import net.seagis.gml.TimePeriodType;
import net.seagis.gml.TimePositionType;
import net.seagis.metadata.MetadataTable;
import net.seagis.swe.CompositePhenomenonEntry;
import net.seagis.swe.CompositePhenomenonTable;
import net.seagis.swe.PhenomenonEntry;
import net.seagis.swe.PhenomenonTable;
import org.opengis.observation.Measurement;
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
 * @see net.seagis.measervation.coverage.MeasurementTableFiller
 */
public class MeasurementTable extends SingletonTable<Measurement> {
    
    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-même une référence vers cette instance
     * de {@code measervationTable}, mais seule {@link StationEntry} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours être {@code measervationTable} d'abord, et {@code StationTable}
     * ensuite.
     */
    protected SamplingFeatureTable stations;
    
    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-même une référence vers cette instance
     * de {@code measervationTable}, mais seule {@link StationEntry} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours être {@code measervationTable} d'abord, et {@code StationTable}
     * ensuite.
     */
    protected SamplingPointTable stationPoints;
    
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
     * Connexion vers la table des {@linkplain Distribution distributions}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected DistributionTable distributions;
    
    /**
     * Connexion vers la table des méta-données. Une table par défaut (éventuellement partagée)
     * sera construite la première fois où elle sera nécessaire.
     */
    protected MetadataTable metadata;
    
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
        super(query);
        setIdentifierParameters(query.byName, null);
    }
    
    
    /**
     * Construit une mesure pour l'enregistrement courant
     */
    @Override
    public MeasurementEntry createEntry(final ResultSet result) throws SQLException, CatalogException {
        final MeasurementQuery query = (MeasurementQuery) super.query;
        if (distributions == null) {
            distributions = getDatabase().getTable(DistributionTable.class);
        }
        Distribution distrib = distributions.getEntry(result.getString(indexOf(query.distribution)));
        
        if (phenomenons == null) {
            phenomenons = getDatabase().getTable(PhenomenonTable.class);
        }
        PhenomenonEntry pheno = (PhenomenonEntry)phenomenons.getEntry(result.getString(indexOf(query.observedProperty)));
        
        if (compositePhenomenons == null) {
            compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
        }
        CompositePhenomenonEntry compoPheno = compositePhenomenons.getEntry(result.getString(indexOf(query.observedPropertyComposite)));
        
        
        if (stations == null) {
            stations = getDatabase().getTable(SamplingFeatureTable.class);
        }
        SamplingFeatureEntry station = stations.getEntry(result.getString(indexOf(query.featureOfInterest)));
        
        if (stationPoints == null) {
            stationPoints = getDatabase().getTable(SamplingPointTable.class);
        }
        SamplingPointEntry stationPoint = stationPoints.getEntry(result.getString(indexOf(query.featureOfInterestPoint)));
        
        
        if (procedures == null) {
            procedures = getDatabase().getTable(ProcessTable.class);
        }
        ProcessEntry procedure = procedures.getEntry(result.getString(indexOf(query.procedure)));
        
        if (measures == null) {
            measures = getDatabase().getTable(MeasureTable.class);
        }
        MeasureEntry resultat = measures.getEntry(result.getString(indexOf(query.result)));
        
        if(pheno == null) pheno = compoPheno;
        if(station == null) station =  stationPoint;
        
        Timestamp begin = result.getTimestamp(indexOf(query.samplingTimeBegin));
        Timestamp end = result.getTimestamp(indexOf(query.samplingTimeEnd));
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
        
        return new MeasurementEntry(result.getString(indexOf(query.name   )),
                                    result.getString(indexOf(query.description)),
                                    station,
                                    pheno,
                                    procedure,
                                    distrib,
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
    public synchronized String getIdentifier(final MeasurementEntry meas) throws SQLException, CatalogException {
        final MeasurementQuery query = (MeasurementQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (meas.getName() != null) {
                PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.name), meas.getName());
                ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return meas.getName();
                } else {
                    id = meas.getName();
                }
            } else {
                id = searchFreeIdentifier("urn:BRGM:measurement:");
            }
            PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.name),         id);
            statement.setString(indexOf(query.description),  meas.getDefinition());
            // on insere la distribution
            if (meas.getDistribution() == null) {
                meas.setDistribution(DistributionEntry.NORMAL);
            }
            if (distributions == null) {
                distributions = getDatabase().getTable(DistributionTable.class);
            }
            statement.setString(indexOf(query.distribution), distributions.getIdentifier(meas.getDistribution()));
        
            // on insere la station qui a effectué cette measervation
            if (meas.getFeatureOfInterest() instanceof SamplingPointEntry){
                SamplingPointEntry station = (SamplingPointEntry)meas.getFeatureOfInterest();
                if (stationPoints == null) {
                    stationPoints = getDatabase().getTable(SamplingPointTable.class);
                }
                statement.setString(indexOf(query.featureOfInterestPoint),stationPoints.getIdentifier(station));
                statement.setNull(indexOf(query.featureOfInterest),    java.sql.Types.VARCHAR);
       
            } else  if (meas.getFeatureOfInterest() instanceof SamplingFeatureEntry){
                SamplingFeatureEntry station = (SamplingFeatureEntry)meas.getFeatureOfInterest();
                if (stations == null) {
                    stations = getDatabase().getTable(SamplingFeatureTable.class);
                }
                statement.setString(indexOf(query.featureOfInterest),stations.getIdentifier(station));
                statement.setNull(indexOf(query.featureOfInterestPoint),    java.sql.Types.VARCHAR);
            
            } else {
                statement.setNull(indexOf(query.featureOfInterest),    java.sql.Types.VARCHAR);
                statement.setNull(indexOf(query.featureOfInterestPoint),    java.sql.Types.VARCHAR);
            }
        
            // on insere le phenomene measervé
            if(meas.getObservedProperty() instanceof CompositePhenomenonEntry){
                CompositePhenomenonEntry pheno = (CompositePhenomenonEntry)meas.getObservedProperty();
                if (compositePhenomenons == null) {
                    compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
                }
                statement.setString(indexOf(query.observedPropertyComposite), compositePhenomenons.getIdentifier(pheno));
                statement.setNull(indexOf(query.observedProperty), java.sql.Types.VARCHAR);
        
            } else if(meas.getObservedProperty() instanceof PhenomenonEntry){
                PhenomenonEntry pheno = (PhenomenonEntry)meas.getObservedProperty();
                if (phenomenons == null) {
                    phenomenons = getDatabase().getTable(PhenomenonTable.class);
                }
                statement.setString(indexOf(query.observedProperty), phenomenons.getIdentifier(pheno));
                statement.setNull(indexOf(query.observedPropertyComposite), java.sql.Types.VARCHAR);
        
            } else {
                statement.setNull(indexOf(query.observedProperty), java.sql.Types.VARCHAR);
                statement.setNull(indexOf(query.observedPropertyComposite), java.sql.Types.VARCHAR);
            }
        
            //on insere le capteur
            if (meas.getProcedure() != null) {
                ProcessEntry process = (ProcessEntry)meas.getProcedure();
                if (procedures == null) {
                    procedures = getDatabase().getTable(ProcessTable.class);
                }
                statement.setString(indexOf(query.procedure), procedures.getIdentifier(process));
            } else {
                statement.setNull(indexOf(query.procedure), java.sql.Types.VARCHAR);
            }
        
            // on insere le resultat
            if (meas.getResult() != null){
                if (measures == null) {
                    measures = getDatabase().getTable(MeasureTable.class);
                }
                statement.setString(indexOf(query.result), measures.getIdentifier((MeasureEntry)meas.getResult()));
            } else {
                statement.setNull(indexOf(query.result), java.sql.Types.VARCHAR);
            }
        
            // on insere le "samplingTime""
            if (meas.getSamplingTime() != null){
                if (meas.getSamplingTime() instanceof TimePeriodType) {
                    
                    TimePeriodType sampTime = (TimePeriodType)meas.getSamplingTime();
                    String s = sampTime.getBeginPosition().getValue();
                    Timestamp date = Timestamp.valueOf(s);
                    statement.setTimestamp(indexOf(query.samplingTimeBegin), date);
                    
                    if (sampTime.getEndPosition().getIndeterminatePosition() == null) {
                       
                        sampTime.getEndPosition().getValue();
                        date = Timestamp.valueOf(s);
                        statement.setTimestamp(indexOf(query.samplingTimeEnd),  date);
                   
                    } else {
                        statement.setNull(indexOf(query.samplingTimeEnd),   java.sql.Types.DATE);
                    }
                    
                } else if (meas.getSamplingTime() instanceof TimeInstantType) {
                    TimeInstantType sampTime = (TimeInstantType)meas.getSamplingTime();
                    String s = sampTime.getTimePosition().getValue();
                    Timestamp date = Timestamp.valueOf(s);
                    statement.setTimestamp(indexOf(query.samplingTimeBegin),  date);
                    statement.setNull(indexOf(query.samplingTimeEnd), java.sql.Types.DATE);
                    
                } else {
                    throw new IllegalArgumentException("type allowed for sampling time: TimePeriod or TimeInstant");
                }
            } else {
                statement.setNull(indexOf(query.samplingTimeBegin), java.sql.Types.DATE);
                statement.setNull(indexOf(query.samplingTimeEnd),   java.sql.Types.DATE);
            }
        
            updateSingleton(statement);
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
    
}
