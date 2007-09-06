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

import java.util.Set;
import java.sql.ResultSet;
import java.sql.SQLException;


// Sicade dependencies
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.Table;
import net.sicade.coverage.model.DistributionTable;
import net.sicade.observation.ObservationEntry;
import net.sicade.observation.TemporalObjectEntry;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.observation.Observation;

// geotools dependencies
import org.geotools.resources.Utilities;

/**
 * Classe de base des connections vers la table des {@linkplain Observation observation}.
 * La requête SQL donné au constructeur doit répondre aux conditions suivantes:
 * <p>
 * <ul>
 *   <li>Les deux premiers arguments doivent être la {@linkplain Station station} et
 *       l'{@linkplain Observable observable} recherchés, dans cet ordre.</li>
 *   <li>Les deux premières colonnes retournées doivent aussi être les identifiants de la
 *       {@linkplain Station station} et de l'{@linkplain Observable observable}.</li>
 * </ul>
 * <p>
 * Exemple:
 *
 * <blockquote><pre>
 * SELECT station, observable FROM Observations WHERE (station = ?) AND (observable = ?)
 * </pre></blockquote>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 * @author Guilhem Legal
 */
public abstract class ObservationTable<EntryType extends Observation> extends Table {

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
     * Connexion vers la table des {@linkplain Phenomenon phénomènes}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected PhenomenonTable phenomenons;

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
     * La station pour laquelle on veut des observations, ou {@code null} pour récupérer les
     * observations de toutes les stations.
     */
    protected SamplingFeature featureOfInterest;
  

    /** 
     * Construit une nouvelle connexion vers la table des observations. Voyez la javadoc de
     * cette classe pour les conditions que doivent remplir la requête donnée en argument.
     * 
     * @param  database Connexion vers la base de données des observations.
     */
    public ObservationTable(final Database database) {
        super(new ObservationQuery(database)); 
    }
    
    /** 
     * Super constructeur qui est appelé par les classe specialisant ObservationTable.
     * 
     * @param  database Connexion vers la base de données des observations.
     */
    protected ObservationTable(ObservationQuery query) {
        super(query); 
    }

    
    /**
     * Définie la table des stations à utiliser. Cette méthode peut être appelée par
     * {@link StationTable} avant toute première utilisation de {@code ObservationTable}.
     *
     * @param  stations Table des stations à utiliser.
     * @throws IllegalStateException si cette instance utilise déjà une autre table des stations.
     */
    protected synchronized void setStationTable(final SamplingFeatureTable stations)
            throws IllegalStateException
    {
        if (this.stations != stations) {
            if (this.stations != null) {
                throw new IllegalStateException();
            }
            this.stations = stations; // Doit être avant tout appel de setTable(this).
            stations.setObservationTable(this);
        }
    }

    /**
     * Retourne la table des stations, en la créant si nécessaire.
     */
    private synchronized SamplingFeatureTable getStationTable() {
        if (stations == null) {
            setStationTable(getDatabase().getTable(SamplingFeatureTable.class));
        }
        return stations;
    }

    /**
     * Retourne la liste des stations qui pourrait avoir des données dans cette table.
     */
    public Set<SamplingFeature> getStations() throws CatalogException, SQLException {
        /*
         * Ne PAS synchroniser cette méthode. StationTable est déjà synchronisée, et on veut
         * éviter de garder un vérou à la fois sur ObservationTable et StationTable à cause
         * du risque de "thread lock" que cela pourrait poser.
         */
        return getStationTable().getEntries();
    }

    /**
     * Retourne la station pour laquelle on recherche des observations.
     */
    public final SamplingFeature getFeatureOfInterest() {
        return featureOfInterest;
    }

    /**
     * Définit la station pour laquelle on recherche des observations.
     * La valeur {@code null} recherche toutes les stations.
     */
    public synchronized void setStation(final SamplingFeature station) {
        if (!Utilities.equals(station, this.featureOfInterest)) {
            this.featureOfInterest = station;
            fireStateChanged("Station");
        }
    }

    /**
     * Configure la requête SQL spécifiée en fonction de la station et de l'observable recherchés
     * par cette table. Cette méthode est appelée automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged changé d'état}.
     
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        if (featureOfInterest != null) {
            statement.setInt(STATION, featureOfInterest.getNumericIdentifier());
        } else {
            throw new UnsupportedOperationException("La recherche sur toutes les stations n'est pas encore impléméntée.");
        }
        if (observable != null) {
            statement.setInt(OBSERVABLE, observable.getNumericIdentifier());
        } else {
            throw new UnsupportedOperationException("La recherche sur tous les observables n'est pas encore impléméntée.");
        }
    }
     */
    
    /**
     * Retourne {@code true} s'il existe au moins une entrée pour la station et l'observable
     * courant.
     
    public synchronized boolean exists() throws SQLException {
        final PreparedStatement statement = getStatement(getProperty(select));
        final ResultSet result = statement.executeQuery();
        final boolean exists = result.next();
        result.close();
        return exists;
    }
     
     */

    /**
     * Retourne les observations pour la station et l'observable courants. Cette méthode
     * ne retourne jamais {@code null}, mais peut retourner un ensemble vide. L'ensemble
     * retourné ne contiendra jamais plus d'un élément si une station et un observable
     * non-nuls ont été spécifiés à cette table.
     * 
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
     
    public synchronized List<EntryType> getEntries() throws CatalogException, SQLException {
        final List<EntryType> list = new ArrayList<EntryType>();
        final PreparedStatement statement = getStatement(getProperty(select));
        final ResultSet result = statement.executeQuery();
        while (result.next()) {
            list.add(createEntry(result));
        }
        result.close();
        return list;
    }*/

    /**
     * Retourne une seule observation pour la station et l'observable courants, ou {@code null}
     * s'il n'y en a pas. Cette méthode risque d'échouer si la station et l'observable n'ont pas
     * été spécifiés tous les deux à cette table avec une valeur non-nulle.
     * 
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
     
    public synchronized EntryType getEntry() throws CatalogException, SQLException {
        final PreparedStatement statement = getStatement(getProperty(select));
        final ResultSet results = statement.executeQuery();
        EntryType observation = null;
        while (results.next()) {
            final EntryType candidate = createEntry(results);
            if (observation == null) {
                observation = candidate;
            } else if (!observation.equals(candidate)) {
                throw new DuplicatedRecordException(results, 1, String.valueOf(observation));
            }
        }
        results.close();
        return observation;
    }
   */
     
    /**
     * Construit une observation pour l'enregistrement courant.
     */
    private Observation createEntry(final ResultSet result) throws CatalogException, SQLException {
      final ObservationQuery query = (ObservationQuery) super.query;
      
      return new ObservationEntry(result.getString(indexOf(query.name)),
                                    result.getString(indexOf(query.description)),
                                    stations.getEntry(result.getString(indexOf(query.featureOfInterest))),
                                    phenomenons.getEntry(result.getString(indexOf(query.observedProperty))), 
                                    procedures.getEntry(result.getString(indexOf(query.procedure))),
                                    distributions.getEntry(result.getString(indexOf(query.distribution))),
                                    //manque quality
                                    result.getString(indexOf(query.result)),
                                    new TemporalObjectEntry(result.getDate(indexOf(query.samplingTimeBegin)),
                                                            result.getDate(indexOf(query.samplingTimeEnd))),
                                    result.getString(indexOf(query.resultDefinition)));
        
        
    }

}
