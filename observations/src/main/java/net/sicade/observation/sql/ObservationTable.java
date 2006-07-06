/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.sql;

// J2SE dependencies
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

// Geotools dependencies
import org.geotools.resources.Utilities;

// Sicade dependencies
import net.sicade.observation.Station;
import net.sicade.observation.Observable;
import net.sicade.observation.Observation;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;
import net.sicade.observation.IllegalRecordException;
import net.sicade.resources.seagis.ResourceKeys;
import net.sicade.resources.seagis.Resources;


/**
 * Classe de base des connections vers la table des {@linkplain Observation observation}.
 * La requ�te SQL donn� au constructeur doit r�pondre aux conditions suivantes:
 * <p>
 * <ul>
 *   <li>Les deux premiers arguments doivent �tre la {@linkplain Station station} et
 *       l'{@linkplain Observable observable} recherch�s, dans cet ordre.</li>
 *   <li>L'op�rateur de comparaison pour les deux arguments du point pr�c�dent
 *       doit �tre {@code LIKE}.</li>
 *   <li>Les deux premi�res colonnes retourn�es doivent aussi �tre les identifiants de la
 *       {@linkplain Station station} et de l'{@linkplain Observable observable}.</li>
 * </ul>
 * <p>
 * Exemple:
 *
 * <blockquote><pre>
 * SELECT station, observable FROM Observations WHERE (station LIKE ?) AND (observable LIKE ?)
 * </pre></blockquote>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public abstract class ObservationTable<EntryType extends Observation> extends Table {
    /** Num�ro de colonne et d'argument. */ static final int STATION    = 1;
    /** Num�ro de colonne et d'argument. */ static final int OBSERVABLE = 2;

    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-m�me une r�f�rence vers cette instance
     * de {@code ObservationTable}, mais seule {@link StationEntry} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours �tre {@code ObservationTable} d'abord, et {@code StationTable}
     * ensuite.
     */
    private StationTable stations;

    /**
     * Connexion vers la table des {@linkplain Observable observables}.
     * Une connexion (potentiellement partag�e) sera �tablie la premi�re fois o� elle sera n�cessaire.
     */
    private ObservableTable observables;

    /**
     * La station pour laquelle on veut des observations, ou {@code null} pour r�cup�rer les
     * observations de toutes les stations.
     */
    private Station station;

    /**
     * L'observable pour lequel on veut des observations, ou {@code null} pour r�cup�rer les
     * observations correspondant � tous les observables.
     */
    private Observable observable;

    /**
     * La cl� d�signant la requ�te � utiliser pour obtenir des valeurs.
     */
    private final ConfigurationKey select;

    /** 
     * Construit une nouvelle connexion vers la table des observations. Voyez la javadoc de
     * cette classe pour les conditions que doivent remplir la requ�te donn�e en argument.
     * 
     * @param  database Connexion vers la base de donn�es des observations.
     * @param  select   Cl� de la requ�te SQL � utiliser pour obtenir des valeurs.
     */
    protected ObservationTable(final Database       database,
                               final ConfigurationKey select)
    {
        super(database);
        this.select = select;
    }

    /** 
     * Construit une nouvelle connexion vers la table des observations pour les stations sp�cifi�es.
     * 
     * @param  stations La table des stations � utiliser.
     * @param  select   Cl� de la requ�te SQL � utiliser pour obtenir des valeurs.
     */
    protected ObservationTable(final StationTable   stations,
                               final ConfigurationKey select)
    {
        this(stations.database, select);
        setStationTable(stations);
    }

    /**
     * D�finie la table des stations � utiliser. Cette m�thode peut �tre appel�e par
     * {@link StationTable} avant toute premi�re utilisation de {@code ObservationTable}.
     *
     * @param  stations Table des stations � utiliser.
     * @throws IllegalStateException si cette instance utilise d�j� une autre table des stations.
     */
    protected synchronized void setStationTable(final StationTable stations)
            throws IllegalStateException
    {
        if (this.stations != stations) {
            if (this.stations != null) {
                throw new IllegalStateException();
            }
            this.stations = stations; // Doit �tre avant tout appel de setTable(this).
            stations.setObservationTable(this);
        }
    }

    /**
     * Retourne la table des stations, en la cr�ant si n�cessaire.
     */
    private synchronized StationTable getStationTable() {
        if (stations == null) {
            setStationTable(database.getTable(StationTable.class));
        }
        return stations;
    }

    /**
     * Retourne la liste des stations qui pourrait avoir des donn�es dans cette table.
     */
    public Set<Station> getStations() throws CatalogException, SQLException {
        /*
         * Ne PAS synchroniser cette m�thode. StationTable est d�j� synchronis�e, et on veut
         * �viter de garder un v�rou � la fois sur ObservationTable et StationTable � cause
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
     * D�finit la station pour laquelle on recherche des observations.
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
     * D�finit l'observable pour lequel on recherche des observations.
     * La valeur {@code null} retient tous les observables.
     */
    public synchronized void setObservable(final Observable observable) {
        if (!Utilities.equals(observable, this.observable)) {
            this.observable = observable;
            fireStateChanged("Observable");
        }
    }

    /**
     * Configure la requ�te SQL sp�cifi�e en fonction de la station et de l'observable recherch�s
     * par cette table. Cette m�thode est appel�e automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged chang� d'�tat}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        if (station != null) {
            statement.setInt(STATION, station.getNumericIdentifier());
        } else {
            statement.setString(STATION, "%");
        }
        if (observable != null) {
            statement.setInt(OBSERVABLE, observable.getNumericIdentifier());
        } else {
            statement.setString(OBSERVABLE, "%");
        }
    }

    /**
     * Retourne {@code true} s'il existe au moins une entr�e pour la station et l'observable
     * courant.
     */
    public synchronized boolean exists() throws SQLException {
        final PreparedStatement statement = getStatement(select);
        final ResultSet result = statement.executeQuery();
        final boolean exists = result.next();
        result.close();
        return exists;
    }

    /**
     * Retourne les observations pour la station et l'observable courants. Cette m�thode
     * ne retourne jamais {@code null}, mais peut retourner un ensemble vide. L'ensemble
     * retourn� ne contiendra jamais plus d'un �l�ment si une station et un observable
     * non-nuls ont �t� sp�cifi�s � cette table.
     * 
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de donn�es a �chou� pour une autre raison.
     */
    public synchronized Collection<EntryType> getEntries() throws CatalogException, SQLException {
        final List<EntryType> list = new ArrayList<EntryType>();
        final PreparedStatement statement = getStatement(select);
        final ResultSet result = statement.executeQuery();
        while (result.next()) {
            list.add(createEntry(result));
        }
        result.close();
        return list;
    }

    /**
     * Retourne une seule observation pour la station et l'observable courants, ou {@code null}
     * s'il n'y en a pas. Cette m�thode risque d'�chouer si la station et l'observable n'ont pas
     * �t� sp�cifi�s tous les deux � cette table avec une valeur non-nulle.
     * 
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de donn�es a �chou� pour une autre raison.
     */
    public synchronized EntryType getEntry() throws CatalogException, SQLException {
        final PreparedStatement statement = getStatement(select);
        final ResultSet result = statement.executeQuery();
        EntryType observation = null;
        while (result.next()) {
            final EntryType candidate = createEntry(result);
            if (observation == null) {
                observation = candidate;
            } else if (!observation.equals(candidate)) {
                final String table = result.getMetaData().getTableName(1);
                result.close();
                throw new IllegalRecordException(table, Resources.format(
                          ResourceKeys.ERROR_DUPLICATED_RECORD_$1, observable));
            }
        }
        result.close();
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
                observables = database.getTable(ObservableTable.class);
            }
            observable = observables.getEntry(result.getString(OBSERVABLE));
        }
        return createEntry(station, observable, result);
    }

    /**
     * Construit une observation pour l'enregistrement courant. Les deux premi�res colonnes
     * de l'enregistrement ont d�j� �t� extraits et donn�s en argument ({@code station} et
     * {@code observable}). Les classes d�riv�es doivent extraires les colonnes restantes
     * et construire l'entr�e appropri�e.
     * 
     * @throws SQLException si une erreur est survenu lors de l'acc�s � la base de donn�es.
     */
    protected abstract EntryType createEntry(final Station    station,
                                             final Observable observable,
                                             final ResultSet  result) throws SQLException;
}
