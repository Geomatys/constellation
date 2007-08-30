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
package net.sicade.observation.sql;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

// Geotools dependencies
import org.geotools.resources.Utilities;
import net.sicade.catalog.ServerException;
import net.sicade.catalog.Entry;

// openGis dependencies
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.observation.Observation;
import org.opengis.observation.AnyFeature;
import org.opengis.observation.sampling.SamplingFeatureRelation;
import org.opengis.observation.sampling.SurveyProcedure;

/**
 * Implémentation d'une entrée représentant une {@link Station station}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 *
 * @todo L'implémentation actuelle n'est pas <cite>serializable</cite> du fait qu'elle nécessite
 *       une connexion à la base de données. Une version future devrait rétablir la connexion au
 *       moment de la <cite>deserialization</cite>.
 */
public class SamplingFeatureEntry extends Entry implements SamplingFeature {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 8822736167506306189L;

    /**
     * L'identifiant numérique de la station.
     */
    private final int identifier;
    
    /**
     * L'ensemble des stations. Ne sera construit que la première fois où il sera nécessaire.
     */
    private List<SamplingFeatureRelation> relatedSamplingFeature;
    
    /**
     * Les Observations
     */
    private List<Observation> relatedObservation;
    
    /**
     * Les features designé
     */
    private List<AnyFeature> sampledFeature; 
    
    /**
     * Connexion vers la table des stations.
     * Sera mis à {@code null} lorsqu'elle ne sera plus nécessaire.
     */
    private transient SamplingFeatureTable stations;
    
    /**
     * Connexion vers la table des "survey details"
     * Optionnel peut etre {@code null}
     */
    private SurveyProcedure surveyDetail;

    /**
     * Connexion vers la table des observations. Contrairement à la plupart des autres
     * entrées du paquet {@code net.sicade.observation}, les observations ne seront pas
     * conservées dans une cache car elle sont potentiellement très nombreuses. Il nous
     * faudra donc conserver la connexion en permanence.
     */
    private final ObservationTable<Observation> observations;
    

    /** 
     * Construit une entrée pour l'identifiant de station spécifié.
     *
     * @param table      La table qui a produit cette entrée.
     * @param identifier L'identifiant numérique de la station.
     * @param name       Le nom de la station.
     * @param provider   La provenance de la donnée, ou {@code null} si inconnue.
     */
    protected SamplingFeatureEntry(final SamplingFeatureTable stations,
                                   final String       name,
                                   final SurveyProcedure surveyDetail,
                                   final int identifier)
    {
        super(name);
        this.surveyDetail = surveyDetail;
        this.stations   = stations;
        this.observations   = stations.getObservationTable();
        this.identifier = identifier;
    }

    /**
     * {@inheritDoc}
     */
    public int getNumericIdentifier() {
        return identifier;
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized List<SamplingFeatureRelation> getRelatedSamplingFeatures() {
        if (relatedSamplingFeature == null) try {
            if (stations != null) {
                final List<SamplingFeatureRelation> list = null;
               
                /*synchronized (stations) {
                    assert equals(stations.getPlatform()) : this;
                    stations.setPlatform(this);
                    set = stations.getEntries();
                }*/
                relatedSamplingFeature = Collections.unmodifiableList(list);
            }
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
        return relatedSamplingFeature;
    }
  
    
    /**
     * {@inheritDoc}
     */
    public synchronized List<Observation> getRelatedObservations() {
        
        if (relatedObservation == null) try {
            if (observations != null) {
                List<Observation> list = null;
                synchronized (observations) {
                    /*assert equals(observations.) : this;
                    observations.setPlatform(this);*/
                    list = observations.getEntries();
                }
                relatedObservation = Collections.unmodifiableList(list);
            }
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
        return relatedObservation;
    }
    
     /**
     * {@inheritDoc}
     */
    public synchronized List<AnyFeature> getSampledFeatures() {
        if (sampledFeature == null) try {
            if (stations != null) {
                final List<AnyFeature> list = null;
               /* synchronized (stations) {
                    assert equals(stations.getPlatform()) : this;
                    stations.setPlatform(this);
                    set = stations.getEntries();
                }*/
                sampledFeature = Collections.unmodifiableList(list);
            }
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
        return sampledFeature;
    }

    public SurveyProcedure getSurveyDetail() {
        return this.surveyDetail;
    }
    
    

    /**
     * Retourne le code numérique identifiant cette entrée.
     */
    @Override
    public int hashCode() {
        return identifier;
    }

    /**
     * Vérifie que cette station est identique à l'objet spécifié
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final SamplingFeatureEntry that = (SamplingFeatureEntry) object;
            return                 (this.identifier       ==     that.identifier) &&
                   Utilities.equals(this.surveyDetail,           that.surveyDetail)   &&
                   Utilities.equals(this.relatedObservation,     that.relatedObservation) &&
                   Utilities.equals(this.relatedSamplingFeature, that.relatedSamplingFeature) &&
                   Utilities.equals(this.sampledFeature,         that.sampledFeature);
        }
        return false;
    }
}
