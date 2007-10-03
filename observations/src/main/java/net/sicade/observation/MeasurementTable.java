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
package net.sicade.observation;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;
import net.sicade.coverage.model.DistributionEntry;
import net.sicade.coverage.model.DistributionTable;
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
 * @see net.sicade.observation.coverage.MeasurementTableFiller
 */
public class MeasurementTable extends SingletonTable<Measurement> {
    
    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-même une référence vers cette instance
     * de {@code ObservationTable}, mais seule {@link StationEntry} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours être {@code ObservationTable} d'abord, et {@code StationTable}
     * ensuite.
     */
    protected SamplingFeatureTable stations;
    
    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-même une référence vers cette instance
     * de {@code ObservationTable}, mais seule {@link StationEntry} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours être {@code ObservationTable} d'abord, et {@code StationTable}
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
     * Super constructeur qui est appelé par les classe specialisant ObservationTable.
     *
     * @param  database Connexion vers la base de données des observations.
     */
    protected MeasurementTable(MeasurementQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }
    
    
    /**
     * Construit une mesure pour l'enregistrement courant
     */
    public MeasurementEntry createEntry(final ResultSet result) throws SQLException, CatalogException {
        final MeasurementQuery query = (MeasurementQuery) super.query;
        if (distributions == null) {
            distributions = getDatabase().getTable(DistributionTable.class);
        }
        DistributionEntry distrib = distributions.getEntry(result.getString(indexOf(query.distribution)));
        
        if (phenomenons == null) {
            phenomenons = getDatabase().getTable(PhenomenonTable.class);
        }
        PhenomenonEntry pheno = (PhenomenonEntry)phenomenons.getEntry(result.getString(indexOf(query.observedProperty)));
        
        if (compositePhenomenons == null) {
            compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
        }
        CompositePhenomenonEntry compoPheno = (CompositePhenomenonEntry)compositePhenomenons.getEntry(result.getString(indexOf(query.observedPropertyComposite)));
        
        
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
        if (resultat == null ) System.out.println("NULLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
        else System.out.println("PASSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSNULLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
         if(pheno == null) pheno = compoPheno;
        if(station == null) station =  stationPoint;
        
        return new MeasurementEntry(result.getString(indexOf(query.name   )),
                result.getString(indexOf(query.description)),
                station,
                pheno,
                procedure,
                distrib,
                //manque quality
                resultat,
                new TemporalObjectEntry(result.getDate(indexOf(query.samplingTimeBegin)),
                result.getDate(indexOf(query.samplingTimeEnd))),
                result.getString(indexOf(query.resultDefinition)));
    }
    
}
