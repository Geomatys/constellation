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
import java.util.Set;
import java.util.Date;
import java.util.HashSet;
import java.util.Calendar;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.quality.DataQuality;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.metadata.iso.citation.CitationImpl;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.Station;
import net.sicade.observation.Platform;
import net.sicade.observation.Observation;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;


/**
 * Connexion vers la table des {@linkplain Station stations}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class StationTable extends SingletonTable<Station> implements NumericAccess {
    /**
     * Requête SQL pour obtenir une station à partir de son identifiant.
     *
     * @todo L'utilisation d'une clause {@code LIKE %} ne retourne pas les lignes dont la valeur est
     *       nulle. C'est embêtant lorsque la recherche est faite sur la colonne {@code platform},
     *       ce qui ce produit dans le cas {@code LIST} de la méthode {@link #getQuery}.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Stations:SELECT",
            "SELECT identifier AS name, identifier, platform, quality, provider, \"startTime\", \"endTime\", x, y\n" +
            "  FROM \"StationsLocations\"\n" +
            " WHERE name LIKE ?\n"           +
            " ORDER BY identifier");

    /** Numéro d'argument. */ private static final int  ARGUMENT_PLATFORM = 1;

    /** Numéro de colonne. */ private static final int  NAME       = 1;
    /** Numéro de colonne. */ private static final int  IDENTIFIER = 2;
    /** Numéro de colonne. */ private static final int  PLATFORM   = 3;
    /** Numéro de colonne. */ private static final int  QUALITY    = 4;
    /** Numéro de colonne. */ private static final int  PROVIDER   = 5;
    /** Numéro de colonne. */ private static final int  START_TIME = 6;
    /** Numéro de colonne. */ private static final int  END_TIME   = 7;
    /** Numéro de colonne. */ private static final int  LONGITUDE  = 8;
    /** Numéro de colonne. */ private static final int  LATITUDE   = 9;

    /**
     * Connexion vers la table permettant d'obtenir les trajectoires des stations. Une table par
     * défaut sera construite la première fois où elle sera nécessaire.
     */
    private LocationTable locations;

    /**
     * Connexion vers la table des plateformes. Une table par défaut sera construite la première
     * fois où elle sera nécessaire.
     */
    private PlatformTable platforms;

    /**
     * Connexion vers la table des méta-données. Une table par défaut (éventuellement partagée)
     * sera construite la première fois où elle sera nécessaire.
     */
    private MetadataTable metadata;

    /**
     * Connexion vers la table des observations.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private ObservationTable<? extends Observation> observations;

    /**
     * La plateforme recherchée, ou {@code null} pour rechercher les stations de toutes les
     * plateformes.
     */
    private Platform platform;

    /**
     * Ensemble des fournisseur pour lesquels on accepte des stations, ou {@code null}
     * pour les accepter tous.
     */
    private Set<Citation> providers;

    /**
     * {@code true} si l'on autorise cette classe à construire des objets {@link StationEntry}
     * qui contiennent moins d'informations, afin de réduire le nombre de requêtes SQL. Utile
     * si l'on souhaite obtenir une {@linkplain #getEntries liste de nombreuses stations}.
     */
    private boolean abridged;

    /** 
     * Construit une nouvelle connexion vers la table des stations.
     */
    public StationTable(final Database database) {
        super(database);
    }

    /**
     * Définie la table des plateformes à utiliser. Cette méthode peut être appelée par {@link PlatformTable}
     * immédiatement après la construction de {@code StationTable} et avant toute première utilisation.
     *
     * @param  platforms Table des plateformes à utiliser.
     * @throws IllegalStateException si cette instance utilise déjà une autre table des plateformes.
     */
    protected synchronized void setPlatformTable(final PlatformTable platforms)
            throws IllegalStateException
    {
        if (this.platforms != platforms) {
            if (this.platforms != null) {
                throw new IllegalStateException();
            }
            this.platforms = platforms; // Doit être avant tout appel de setTable(this).
            platforms.setStationTable(this);
        }
    }

    /**
     * Définie la table des observations à utiliser. Cette méthode peut être appelée par
     * {@link ObservationTable} avant toute première utilisation de {@code StationTable}.
     *
     * @param  platforms Table des observations à utiliser.
     * @throws IllegalStateException si cette instance utilise déjà une autre table des observations.
     */
    protected synchronized void setObservationTable(final ObservationTable<? extends Observation> observations)
            throws IllegalStateException
    {
        if (this.observations != observations) {
            if (this.observations != null) {
                throw new IllegalStateException();
            }
            this.observations = observations; // Doit être avant tout appel de setTable(this).
            observations.setStationTable(this);
        }
    }

    /**
     * Retourne la table des positions à utiliser pour la création des objets {@link StationEntry}.
     */
    final LocationTable getLocationTable() {
        assert Thread.holdsLock(this);
        if (locations == null) {
            locations = database.getTable(LocationTable.Station.class);
        }
        return locations;
    }

    /**
     * Retourne la table des observations à utiliser pour la création des objets {@link StationEntry}.
     */
    final ObservationTable<? extends Observation> getObservationTable() {
        assert Thread.holdsLock(this);
        if (observations == null) {
            setObservationTable(database.getTable(MeasurementTable.class));
        }
        return observations;
    }

    /**
     * Retourne la {@linkplain Platform platforme} des stations désirées. La valeur {@code null}
     * signifie que cette table recherche les stations de toutes les plateformes.
     */
    public final Platform getPlatform() {
        return platform;
    }

    /**
     * Définit la {@linkplain Platform platforme} des stations désirées. Les prochains appels à la
     * méthode {@link #getEntries() getEntries()} ne retourneront que les stations de cette plateforme.
     * La valeur {@code null} retire la contrainte des plateformes (c'est-à-dire que cette table
     * recherchera les stations de toutes les plateformes).
     */
    public synchronized void setPlatform(final Platform platform) {
        if (!Utilities.equals(platform, this.platform)) {
            this.platform = platform;
            fireStateChanged("Platform");
        }
    }

    /**
     * Indique que les stations en provenance du fournisseur de données spécifié sont acceptables.
     * Toutes les stations qui ne proviennent pas de ce fournisseur ou d'un fournisseur spécifié
     * lors d'un appel précédent de cette méthode ne seront pas retenues par la méthode
     * {@link #getEntries}.
     */
    public synchronized void acceptableProvider(final Citation provider) {
        if (providers == null) {
            providers = new HashSet<Citation>();
        }
        if (providers.add(provider)) {
            fireStateChanged("Providers");
        }
    }

    /**
     * Indique que les stations en provenance du fournisseur de données spécifié sont acceptables.
     * Cette méthode est similaire à celle du même nom qui attend un objet {@link Citation} en
     * argument, excepté qu'elle tentera de déterminer le fournisseur à partir d'une chaîne de
     * caractères qui peut être une clé primaire dans la base de données.
     */
    public synchronized void acceptableProvider(final String provider) {
        if (metadata == null) {
            metadata = database.getTable(MetadataTable.class);
        }
        Citation citation;
        try {
            citation = metadata.getEntry(Citation.class, provider);
        } catch (SQLException e) {
            citation = new CitationImpl(provider);
        }
        acceptableProvider(citation);
    }

    /**
     * Indique si cette table est autorisée à construire des objets {@link Station}
     * qui contiennent moins d'informations. Cet allègement permet de réduire le nombre de
     * requêtes SQL, ce qui peut accélérer l'obtention d'une {@linkplain #getEntries liste
     * de nombreuses stations}.
     *
     * @see #setAbridged
     */
    public final boolean isAbridged() {
        return abridged;
    }

    /**
     * Spécifie si cette table est autorisée à construire des objets {@link Station}
     * qui contiennent moins d'informations. Cet allègement permet de réduire le nombre de
     * requêtes SQL, ce qui peut accélérer l'obtention d'une {@linkplain #getEntries liste
     * de nombreuses stations}.
     *
     * @see #isAbridged
     */
    public synchronized void setAbridged(final boolean abridged) {
        if (abridged != this.abridged) {
            this.abridged = abridged;
            clearCache();
            fireStateChanged("abridged");
        }
    }

    /**
     * Configure la requête SQL spécifiée en fonction de la {@linkplain #getPlatform plateforme
     * courante} de cette table. Cette méthode est appelée automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged changé d'état}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        switch (type) {
            case LIST: {
                final String name = (platform != null) ? platform.getName() : null;
                statement.setString(ARGUMENT_PLATFORM, escapeSearch(name));
                break;
            }
        }
    }

    /**
     * Retourne la requête à utiliser pour obtenir les stations.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: {
                return getProperty(SELECT);
            }
            case LIST: {
                return changeArgumentTarget(getQuery(QueryType.SELECT), 3);
            }
            default: {
                return super.getQuery(type);
            }
        }
    }

    /**
     * Construit une station pour l'enregistrement courant. L'implémentation par défaut extrait une
     * première série d'informations telles que le {@linkplain Station#getName nom de la station},
     * {@linkplain Station#getProvider son fournisseur}, <cite>etc.</cite> et appele la méthode
     * <code>{@linkplain #createEntry(int,String,Platform,DataQuality,Citation,ResultSet)
     * createEntry}(name, identifier, ...)</code> avec ces informations.
     */
    protected Station createEntry(final ResultSet result) throws CatalogException, SQLException {
        final String name    = result.getString(NAME);
        final int identifier = result.getInt(IDENTIFIER);
        final Platform owner;
        if (platform == null && !abridged) {
            if (platforms == null) {
                setPlatformTable(database.getTable(PlatformTable.class));
            }
            owner = platforms.getEntry(result.getString(PLATFORM));
        } else {
            owner = platform;
        }
        if (metadata == null) {
            metadata = database.getTable(MetadataTable.class);
        }
        final DataQuality quality = metadata.getEntry(DataQuality.class, result.getString(QUALITY ));
        final Citation   provider = metadata.getEntry(Citation.class,    result.getString(PROVIDER));
        final Calendar   calendar = getCalendar();
        Date startTime = result.getTimestamp(START_TIME, calendar);
        Date   endTime = result.getTimestamp(  END_TIME, calendar);
        double       x = result.getDouble(LONGITUDE); if (result.wasNull()) x=Double.NaN;
        double       y = result.getDouble( LATITUDE); if (result.wasNull()) y=Double.NaN;
        // Remplace le type Timestamp par Date, car DateRange exigera ce type exact.
        if (startTime != null) startTime = new Date(startTime.getTime());
        if (  endTime != null)   endTime = new Date(  endTime.getTime());
        return createEntry(identifier, name,
                (!Double.isNaN(x) || !Double.isNaN(y)) ? new Point2D.Double(x,y)           : null,
                ( startTime!=null ||  endTime!=null)   ? new DateRange(startTime, endTime) : null,
                owner, quality, provider, result);
    }

    /**
     * Construit une station à partir des informations spécifiées. Cette méthode est appelée
     * automatiquement par {@link #createEntry(ResultSet)} après avoir extrait les informations
     * communes à tous les types de stations. L'implémentation par défaut ne fait que construire
     * un objet {@link StationEntry} sans extraire davantage d'informations. Les classes dérivées
     * devraient redéfinir cette méthode si elles souhaitent construire un type de station plus
     * élaboré.
     *
     * @param table      La table qui a produit cette entrée.
     * @param identifier L'identifiant numérique de la station.
     * @param name       Le nom de la station.
     * @param coordinate Une coordonnée représentative en degrés de longitude et de latitude,
     *                   ou {@code null} si inconue.
     * @param timeRange  Plage de temps de cet élément, ou {@code null} si inconue.
     * @param platform   La plateforme (par exemple un bateau) sur laquelle a été prise cette
     *                   station, ou {@code null} si inconnue.
     * @param quality    La qualité de la donnée, ou {@code null} si inconnue.
     * @param provider   La provenance de la donnée, ou {@code null} si inconnue.
     * @param result     La ligne courante de la requête SQL. A utiliser seulement si les sous-classes
     *                   ont besoin d'extraire davantage d'informations que celles qui ont été fournies
     *                   par les arguments précédents.
     *
     * @throws SQLException si un accès à la base de données était nécessaire et a échoué.
     */
    protected Station createEntry(final int          identifier,
                                  final String       name,
                                  final Point2D      coordinate,
                                  final DateRange    timeRange,
                                  final Platform     platform,
                                  final DataQuality  quality,
                                  final Citation     provider,
                                  final ResultSet    result)
            throws SQLException
    {
        return new StationEntry(this, identifier, name, coordinate, timeRange, platform, quality, provider);
    }

    /**
     * Indique si la méthode {@link #getEntries} devrait accepter la station spécifiée.
     * L'implémentation par défaut vérifie si le {@linkplain Station#getProvider fournisseur}
     * est l'un de ceux qui ont été spécifiés à la méthode {@link #acceptableProvider(Citation)
     * acceptableProvider}. Si la station ne donne pas d'indication sur le fournisseur, alors
     * cette méthode va l'accepter comme approche conservative.
     */
    @Override
    protected boolean accept(final Station entry) throws SQLException {
        if (providers != null) {
            final Citation provider = entry.getProvider();
            if (provider != null) {
                for (final Citation acceptable : providers) {
                    if (Citations.identifierMatches(provider, acceptable)) {
                        return super.accept(entry);
                    }
                }
                return false;
            }
        }
        return super.accept(entry);
    }
}
