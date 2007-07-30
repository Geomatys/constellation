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
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import org.geotools.resources.Utilities;
import net.sicade.observation.Station;
import net.sicade.observation.Observable;
import net.sicade.observation.Observation;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.DuplicatedRecordException;
import net.sicade.catalog.ConfigurationKey;
import net.sicade.catalog.Database;
import net.sicade.catalog.Query;
import net.sicade.catalog.QueryType;
import net.sicade.catalog.Table;


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
 */
public abstract class ObservationTable<EntryType extends Observation> extends Table {
    /** Numéro de colonne et d'argument. */ static final int STATION    = 1;
    /** Numéro de colonne et d'argument. */ static final int OBSERVABLE = 2;

    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-même une référence vers cette instance
     * de {@code ObservationTable}, mais seule {@link StationEntry} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours être {@code ObservationTable} d'abord, et {@code StationTable}
     * ensuite.
     */
    private StationTable stations;

    /**
     * Connexion vers la table des {@linkplain Observable observables}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private ObservableTable observables;

    /**
     * La station pour laquelle on veut des observations, ou {@code null} pour récupérer les
     * observations de toutes les stations.
     */
    private Station station;

    /**
     * L'observable pour lequel on veut des observations, ou {@code null} pour récupérer les
     * observations correspondant à tous les observables.
     */
    private Observable observable;

    /**
     * La clé désignant la requête à utiliser pour obtenir des valeurs.
     */
    private final ConfigurationKey select;

    /** 
     * Construit une nouvelle connexion vers la table des observations. Voyez la javadoc de
     * cette classe pour les conditions que doivent remplir la requête donnée en argument.
     * 
     * @param  database Connexion vers la base de données des observations.
     * @param  select   Clé de la requête SQL à utiliser pour obtenir des valeurs.
     */
    protected ObservationTable(final Database       database,
                               final ConfigurationKey select)
    {
        super(new Query(database)); // TODO
        this.select = select;
    }

    /** 
     * Construit une nouvelle connexion vers la table des observations pour les stations spécifiées.
     * 
     * @param  stations La table des stations à utiliser.
     * @param  select   Clé de la requête SQL à utiliser pour obtenir des valeurs.
     */
    protected ObservationTable(final StationTable   stations,
                               final ConfigurationKey select)
    {
        this(stations.getDatabase(), select);
        setStationTable(stations);
    }

    /**
     * Définie la table des stations à utiliser. Cette méthode peut être appelée par
     * {@link StationTable} avant toute première utilisation de {@code ObservationTable}.
     *
     * @param  stations Table des stations à utiliser.
     * @throws IllegalStateException si cette instance utilise déjà une autre table des stations.
     */
    protected synchronized void setStationTable(final StationTable stations)
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
    private synchronized StationTable getStationTable() {
        if (stations == null) {
            setStationTable(getDatabase().getTable(StationTable.class));
        }
        return stations;
    }

    /**
     * Retourne la liste des stations qui pourrait avoir des données dans cette table.
     */
    public Set<Station> getStations() throws CatalogException, SQLException {
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
    public final Station getStation() {
        return station;
    }

    /**
     * Définit la station pour laquelle on recherche des observations.
     * La valeur {@code null} recherche toutes les stations.
     */
    public synchronized void setStation(final Station station) {
        if (!Utilities.equals(station, this.station)) {
            this.station = station;
            fireStateChanged("Station");
        }
    }

    /**
     * Retourne l'observable pour lequel on recherche des observations.
     */
    public final Observable getObservable() {
        return observable;
    }

    /**
     * Définit l'observable pour lequel on recherche des observations.
     * La valeur {@code null} retient tous les observables.
     */
    public synchronized void setObservable(final Observable observable) {
        if (!Utilities.equals(observable, this.observable)) {
            this.observable = observable;
            fireStateChanged("Observable");
        }
    }

    /**
     * Configure la requête SQL spécifiée en fonction de la station et de l'observable recherchés
     * par cette table. Cette méthode est appelée automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged changé d'état}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        if (station != null) {
            statement.setInt(STATION, station.getNumericIdentifier());
        } else {
            throw new UnsupportedOperationException("La recherche sur toutes les stations n'est pas encore impléméntée.");
        }
        if (observable != null) {
            statement.setInt(OBSERVABLE, observable.getNumericIdentifier());
        } else {
            throw new UnsupportedOperationException("La recherche sur tous les observables n'est pas encore impléméntée.");
        }
    }

    /**
     * Retourne {@code true} s'il existe au moins une entrée pour la station et l'observable
     * courant.
     */
    public synchronized boolean exists() throws SQLException {
        final PreparedStatement statement = getStatement(getProperty(select));
        final ResultSet result = statement.executeQuery();
        final boolean exists = result.next();
        result.close();
        return exists;
    }

    /**
     * Retourne les observations pour la station et l'observable courants. Cette méthode
     * ne retourne jamais {@code null}, mais peut retourner un ensemble vide. L'ensemble
     * retourné ne contiendra jamais plus d'un élément si une station et un observable
     * non-nuls ont été spécifiés à cette table.
     * 
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
     */
    public synchronized Collection<EntryType> getEntries() throws CatalogException, SQLException {
        final List<EntryType> list = new ArrayList<EntryType>();
        final PreparedStatement statement = getStatement(getProperty(select));
        final ResultSet result = statement.executeQuery();
        while (result.next()) {
            list.add(createEntry(result));
        }
        result.close();
        return list;
    }

    /**
     * Retourne une seule observation pour la station et l'observable courants, ou {@code null}
     * s'il n'y en a pas. Cette méthode risque d'échouer si la station et l'observable n'ont pas
     * été spécifiés tous les deux à cette table avec une valeur non-nulle.
     * 
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
     */
    public synchronized EntryType getEntry() throws CatalogException, SQLException {
        final PreparedStatement statement = getStatement(getProperty(select));
        final ResultSet results = statement.executeQuery();
        EntryType observation = null;
        while (results.next()) {
            final EntryType candidate = createEntry(results);
            if (observation == null) {
                observation = candidate;
            } else if (!observation.equals(candidate)) {
                throw new DuplicatedRecordException(results, 1, String.valueOf(observable));
            }
        }
        results.close();
        return observation;
    }

    /**
     * Construit une observation pour l'enregistrement courant.
     */
    private EntryType createEntry(final ResultSet result) throws CatalogException, SQLException {
        Station station = this.station;
        if (station == null) {
            assert !Thread.holdsLock(getStationTable()); // Voir le commentaire de 'stations'.
            station = getStationTable().getEntry(result.getString(STATION));
        }
        Observable observable = this.observable;
        if (observable == null) {
            if (observables == null) {
                observables = getDatabase().getTable(ObservableTable.class);
            }
            observable = observables.getEntry(result.getString(OBSERVABLE));
        }
        return createEntry(station, observable, result);
    }

    /**
     * Construit une observation pour l'enregistrement courant. Les deux premières colonnes
     * de l'enregistrement ont déjà été extraits et donnés en argument ({@code station} et
     * {@code observable}). Les classes dérivées doivent extraires les colonnes restantes
     * et construire l'entrée appropriée.
     * 
     * @throws SQLException si une erreur est survenu lors de l'accès à la base de données.
     */
    protected abstract EntryType createEntry(final Station    station,
                                             final Observable observable,
                                             final ResultSet  result) throws SQLException;
}
