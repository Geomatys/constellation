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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;
import org.constellation.metadata.MetadataTable;
import org.constellation.sampling.SamplingFeatureTable;
import org.constellation.sampling.SamplingPointTable;
import org.constellation.swe.v101.CompositePhenomenonTable;
import org.constellation.swe.v101.PhenomenonTable;
import org.geotoolkit.gml.xml.v311.AbstractTimeGeometricPrimitiveType;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.observation.xml.v100.MeasureEntry;
import org.geotoolkit.observation.xml.v100.MeasurementEntry;
import org.geotoolkit.observation.xml.v100.ProcessEntry;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureEntry;
import org.geotoolkit.sampling.xml.v100.SamplingPointEntry;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonEntry;
import org.geotoolkit.swe.xml.v101.PhenomenonEntry;
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
        
        if (phenomenons == null) {
            phenomenons = getDatabase().getTable(PhenomenonTable.class);
        }
        PhenomenonEntry pheno = (PhenomenonEntry)phenomenons.getEntry(result.getString(indexOf(query.observedProperty)));
        
        if (compositePhenomenons == null) {
            compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
        }
        final CompositePhenomenonEntry compoPheno = compositePhenomenons.getEntry(result.getString(indexOf(query.observedPropertyComposite)));
        
        
        if (stations == null) {
            stations = getDatabase().getTable(SamplingFeatureTable.class);
        }
        SamplingFeatureEntry station = stations.getEntry(result.getString(indexOf(query.featureOfInterest)));
        
        if (stationPoints == null) {
            stationPoints = getDatabase().getTable(SamplingPointTable.class);
        }
        final SamplingPointEntry stationPoint = stationPoints.getEntry(result.getString(indexOf(query.featureOfInterestPoint)));
        
        
        if (procedures == null) {
            procedures = getDatabase().getTable(ProcessTable.class);
        }
        final ProcessEntry procedure = procedures.getEntry(result.getString(indexOf(query.procedure)));
        
        if (measures == null) {
            measures = getDatabase().getTable(MeasureTable.class);
        }
        final MeasureEntry resultat = measures.getEntry(result.getString(indexOf(query.result)));
        
        if(pheno == null) pheno = compoPheno;
        if(station == null) station =  stationPoint;
        
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
        
        return new MeasurementEntry(result.getString(indexOf(query.name   )),
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
    public synchronized String getIdentifier(final Measurement meas) throws SQLException, CatalogException {
        final MeasurementQuery query = (MeasurementQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (meas.getName() != null) {
                final PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.name), meas.getName());
                final ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return meas.getName();
                } else {
                    id = meas.getName();
                }
                result.close();
            } else {
                id = searchFreeIdentifier("urn:BRGM:measurement:");
            }
            final PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.name),         id);
            statement.setString(indexOf(query.description),  meas.getDefinition());
            statement.setString(indexOf(query.distribution), "normale");
        
            // on insere la station qui a effectué cette measervation
            if (meas.getFeatureOfInterest() instanceof SamplingPointEntry){
                final SamplingPointEntry station = (SamplingPointEntry)meas.getFeatureOfInterest();
                if (stationPoints == null) {
                    stationPoints = getDatabase().getTable(SamplingPointTable.class);
                }
                statement.setString(indexOf(query.featureOfInterestPoint),stationPoints.getIdentifier(station));
                statement.setNull(indexOf(query.featureOfInterest),    java.sql.Types.VARCHAR);
       
            } else  if (meas.getFeatureOfInterest() instanceof SamplingFeatureEntry){
                final SamplingFeatureEntry station = (SamplingFeatureEntry)meas.getFeatureOfInterest();
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
                final CompositePhenomenonEntry pheno = (CompositePhenomenonEntry)meas.getObservedProperty();
                if (compositePhenomenons == null) {
                    compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
                }
                statement.setString(indexOf(query.observedPropertyComposite), compositePhenomenons.getIdentifier(pheno));
                statement.setNull(indexOf(query.observedProperty), java.sql.Types.VARCHAR);
        
            } else if(meas.getObservedProperty() instanceof PhenomenonEntry){
                final PhenomenonEntry pheno = (PhenomenonEntry)meas.getObservedProperty();
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
                final ProcessEntry process = (ProcessEntry)meas.getProcedure();
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
                    
                    final TimePeriodType sampTime = (TimePeriodType)meas.getSamplingTime();
                    final String s = sampTime.getBeginPosition().getValue();
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
                    final TimeInstantType sampTime = (TimeInstantType)meas.getSamplingTime();
                    final String s = sampTime.getTimePosition().getValue();
                    final Timestamp date = Timestamp.valueOf(s);
                    statement.setTimestamp(indexOf(query.samplingTimeBegin),  date);
                    statement.setNull(indexOf(query.samplingTimeEnd), java.sql.Types.DATE);
                    
                } else {
                    throw new IllegalArgumentException("type allowed for sampling time: TimePeriod or TimeInstant");
                }
            } else {
                statement.setNull(indexOf(query.samplingTimeBegin), java.sql.Types.DATE);
                statement.setNull(indexOf(query.samplingTimeEnd),   java.sql.Types.DATE);
            }
            statement.setNull(indexOf(query.resultDefinition),   java.sql.Types.VARCHAR);

            updateSingleton(statement);
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
    
}
