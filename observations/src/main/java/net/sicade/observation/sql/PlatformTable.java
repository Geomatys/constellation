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

// J2SE dependencies
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

// Geotools dependencies
import org.geotools.resources.Utilities;

// Sicade dependencies
import net.sicade.observation.Station;
import net.sicade.observation.Platform;
import net.sicade.observation.ConfigurationKey;


/**
 * Connexion vers la table des {@linkplain Platform plateformes}
 * (bateaux, campagnes d'échantillonages...).
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PlatformTable extends BoundedSingletonTable<Platform> {
    /**
     * Requête SQL pour obtenir les limites géographiques des plateformes dans une région.
     */
    private static final ConfigurationKey BOUNDING_BOX = new ConfigurationKey("Platforms:BBOX",
            "SELECT MIN(date), MAX(date), MIN(x), MAX(x), MIN(y), MAX(y)\n"          +
            "  FROM \"Locations\"\n"                                                 +
            "  JOIN \"Stations\" ON station=identifier\n"                            +
            " WHERE (date>=? AND date<=?) AND (x>=? AND x<=?) AND (y>=? AND y<=?)\n" +
            "   AND (provider LIKE ?)");

    /**
     * Requête SQL pour obtenir la liste des plateformes dans une région.
     */
    private static final ConfigurationKey LIST = new ConfigurationKey("Platforms:LIST",
            "SELECT DISTINCT platform\n"                                             +
            "  FROM \"Stations\"\n"                                                  +
            "  JOIN \"Locations\" ON station=identifier\n"                           +
            " WHERE (date>=? AND date<=?) AND (x>=? AND x<=?) AND (y>=? AND y<=?)\n" +
            "   AND (provider LIKE ?)\n" +
            " ORDER BY date");

    /**
     * Requête SQL pour obtenir des informations sur une plateforme.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Platforms:SELECT",
            "SELECT DISTINCT platform\n" +
            "  FROM \"Stations\"\n"      +
            " WHERE platform=?");

    /** Numéro d'argument. */ private static final int ARGUMENT_PROVIDER = 7;
    /** Numéro de colonne. */ private static final int NAME              = 1;

    /**
     * Connexion vers la table permettant d'obtenir les trajectoires des plateformes. Une table
     * par défaut sera construite la première fois où elle sera nécessaire.
     */
    private LocationTable locations;

    /**
     * Connexion vers la table des {@linkplain Station stations}. Une table par défaut sera
     * construite la première fois où elle sera nécessaire.
     * <p>
     * <strong>IMPORTANT:</strong> La table des stations peut faire elle-même appel à cette
     * table {@code PlatformTable}.  En conséquence, toutes méthodes qui peut faire appel à
     * la table des stations doit se synchroniser sur {@code stations} <em>avant</em> de se
     * synchroniser sur {@code this}, afin d'éviter des situations de <cite>thread lock</cite>.
     */
    private StationTable stations;

    /**
     * Le fournisseur des stations recherchées, ou {@code null} si on accepte tous les fournisseurs.
     */
    private String provider;

    /**
     * Construit une connexion vers la table des plateformes qui utilisera la base de données
     * spécifiée.
     */
    public PlatformTable(final Database database) {
        super(database, CRS.XYZT);
    }

    /**
     * Définie la table des stations à utiliser. Cette méthode peut être appelée par
     * {@link StationTable} avant toute première utilisation de {@code PlatformTable}.
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
            stations.setPlatformTable(this);
        }
    }

    /**
     * Retourne la table des stations à utiliser pour la création des objets {@link StationEntry}.
     */
    final StationTable getStationTable() {
        assert Thread.holdsLock(this);
        if (stations == null) {
            setStationTable(database.getTable(StationTable.class));
        }
        return stations;
    }

    /**
     * Retourne la table des positions à utiliser pour la création des objets {@link StationEntry}.
     */
    final LocationTable getLocationTable() {
        assert Thread.holdsLock(this);
        if (locations == null) {
            locations = database.getTable(LocationTable.Platform.class);
        }
        return locations;
    }

    /**
     * Retourne le fournisseur des plateformes désirées, ou {@code null} pour obtenir toutes
     * les plateformes.
     */
    public final String getProvider() {
        return provider;
    }

    /**
     * Définit le fournisseur des plateformes désirées. Les prochains appels à la méthode
     * {@link #getEntries() getEntries()} ne retourneront que les plateformes de ce fournisseur.
     * La valeur {@code null} sélectionne toutes les plateformes.
     */
    public synchronized void setProvider(final String provider) {
        if (!Utilities.equals(provider, this.provider)) {
            this.provider = provider;
            fireStateChanged("Provider");
        }
    }

    /**
     * Configure la requête SQL spécifiée en fonction du {@linkplain #getProvider provider}
     * des données de cette table. Cette méthode est appelée automatiquement lorsque cette
     * table a {@linkplain #fireStateChanged changé d'état}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        switch (type) {
            case LIST: // Fall through
            case BOUNDING_BOX: {
                statement.setString(ARGUMENT_PROVIDER, escapeSearch(provider));
                break;
            }
        }
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir les plateformes.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        final ConfigurationKey key;
        switch (type) {
            case LIST:         key=LIST;         break;
            case SELECT:       key=SELECT;       break;
            case BOUNDING_BOX: key=BOUNDING_BOX; break;
            default:           return super.getQuery(type);
        }
        return getProperty(key);
    }

    /**
     * Construit une plateforme pour l'enregistrement courant.
     */
    protected Platform createEntry(final ResultSet results) throws SQLException {
        final String name = results.getString(NAME);
        return new PlatformEntry(this, name);
    }
}
