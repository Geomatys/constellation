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
     * Requ�te SQL pour obtenir une station � partir de son identifiant.
     *
     * @todo L'utilisation d'une clause {@code LIKE %} ne retourne pas les lignes dont la valeur est
     *       nulle. C'est emb�tant lorsque la recherche est faite sur la colonne {@code provider},
     *       ce qui ce produit dans le cas {@code LIST} de la m�thode {@link #getQuery}.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Stations:SELECT",
            "SELECT identifier AS name, identifier, platform, quality, provider, \"startTime\", \"endTime\", x, y\n" +
            "  FROM \"StationsLocations\"\n" +
            " WHERE name LIKE ?\n"           +
            " ORDER BY identifier");

    /** Num�ro d'argument. */ private static final int  ARGUMENT_PLATFORM = 1;

    /** Num�ro de colonne. */ private static final int  NAME       = 1;
    /** Num�ro de colonne. */ private static final int  IDENTIFIER = 2;
    /** Num�ro de colonne. */ private static final int  PLATFORM   = 3;
    /** Num�ro de colonne. */ private static final int  QUALITY    = 4;
    /** Num�ro de colonne. */ private static final int  PROVIDER   = 5;
    /** Num�ro de colonne. */ private static final int  START_TIME = 6;
    /** Num�ro de colonne. */ private static final int  END_TIME   = 7;
    /** Num�ro de colonne. */ private static final int  LONGITUDE  = 8;
    /** Num�ro de colonne. */ private static final int  LATITUDE   = 9;

    /**
     * Connexion vers la table permettant d'obtenir les trajectoires des stations. Une table par
     * d�faut sera construite la premi�re fois o� elle sera n�cessaire.
     */
    private LocationTable locations;

    /**
     * Connexion vers la table des plateformes. Une table par d�faut sera construite la premi�re
     * fois o� elle sera n�cessaire.
     */
    private PlatformTable platforms;

    /**
     * Connexion vers la table des m�ta-donn�es. Une table par d�faut (�ventuellement partag�e)
     * sera construite la premi�re fois o� elle sera n�cessaire.
     */
    private MetadataTable metadata;

    /**
     * Connexion vers la table des observations.
     * Une connexion (potentiellement partag�e) sera �tablie la premi�re fois o� elle sera n�cessaire.
     */
    private ObservationTable<? extends Observation> observations;

    /**
     * La plateforme recherch�e, ou {@code null} pour rechercher les stations de toutes les
     * plateformes.
     */
    private Platform platform;

    /**
     * Ensemble des fournisseur pour lesquels on accepte des stations, ou {@code null}
     * pour les accepter tous.
     */
    private Set<Citation> providers;

    /**
     * {@code true} si l'on autorise cette classe � construire des objets {@link StationEntry}
     * qui contiennent moins d'informations, afin de r�duire le nombre de requ�tes SQL. Utile
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
     * D�finie la table des plateformes � utiliser. Cette m�thode peut �tre appel�e par {@link PlatformTable}
     * imm�diatement apr�s la construction de {@code StationTable} et avant toute premi�re utilisation.
     *
     * @param  platforms Table des plateformes � utiliser.
     * @throws IllegalStateException si cette instance utilise d�j� une autre table des plateformes.
     */
    protected synchronized void setPlatformTable(final PlatformTable platforms)
            throws IllegalStateException
    {
        if (this.platforms != platforms) {
            if (this.platforms != null) {
                throw new IllegalStateException();
            }
            this.platforms = platforms; // Doit �tre avant tout appel de setTable(this).
            platforms.setStationTable(this);
        }
    }

    /**
     * D�finie la table des observations � utiliser. Cette m�thode peut �tre appel�e par
     * {@link ObservationTable} avant toute premi�re utilisation de {@code StationTable}.
     *
     * @param  platforms Table des observations � utiliser.
     * @throws IllegalStateException si cette instance utilise d�j� une autre table des observations.
     */
    protected synchronized void setObservationTable(final ObservationTable<? extends Observation> observations)
            throws IllegalStateException
    {
        if (this.observations != observations) {
            if (this.observations != null) {
                throw new IllegalStateException();
            }
            this.observations = observations; // Doit �tre avant tout appel de setTable(this).
            observations.setStationTable(this);
        }
    }

    /**
     * Retourne la table des positions � utiliser pour la cr�ation des objets {@link StationEntry}.
     */
    final LocationTable getLocationTable() {
        assert Thread.holdsLock(this);
        if (locations == null) {
            locations = database.getTable(LocationTable.Station.class);
        }
        return locations;
    }

    /**
     * Retourne la table des observations � utiliser pour la cr�ation des objets {@link StationEntry}.
     */
    final ObservationTable<? extends Observation> getObservationTable() {
        assert Thread.holdsLock(this);
        if (observations == null) {
            setObservationTable(database.getTable(MeasurementTable.class));
        }
        return observations;
    }

    /**
     * Retourne la {@linkplain Platform platforme} des stations d�sir�es. La valeur {@code null}
     * signifie que cette table recherche les stations de toutes les plateformes.
     */
    public final Platform getPlatform() {
        return platform;
    }

    /**
     * D�finit la {@linkplain Platform platforme} des stations d�sir�es. Les prochains appels � la
     * m�thode {@link #getEntries() getEntries()} ne retourneront que les stations de cette plateforme.
     * La valeur {@code null} retire la contrainte des plateformes (c'est-�-dire que cette table
     * recherchera les stations de toutes les plateformes).
     */
    public synchronized void setPlatform(final Platform platform) {
        if (!Utilities.equals(platform, this.platform)) {
            this.platform = platform;
            fireStateChanged("Platform");
        }
    }

    /**
     * Indique que les stations en provenance du fournisseur de donn�es sp�cifi� sont acceptables.
     * Toutes les stations qui ne proviennent pas de ce fournisseur ou d'un fournisseur sp�cifi�
     * lors d'un appel pr�c�dent de cette m�thode ne seront pas retenues par la m�thode
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
     * Indique que les stations en provenance du fournisseur de donn�es sp�cifi� sont acceptables.
     * Cette m�thode est similaire � celle du m�me nom qui attend un objet {@link Citation} en
     * argument, except� qu'elle tentera de d�terminer le fournisseur � partir d'une cha�ne de
     * caract�res qui peut �tre une cl� primaire dans la base de donn�es.
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
     * Indique si cette table est autoris�e � construire des objets {@link Station}
     * qui contiennent moins d'informations. Cet all�gement permet de r�duire le nombre de
     * requ�tes SQL, ce qui peut acc�l�rer l'obtention d'une {@linkplain #getEntries liste
     * de nombreuses stations}.
     *
     * @see #setAbridged
     */
    public final boolean isAbridged() {
        return abridged;
    }

    /**
     * Sp�cifie si cette table est autoris�e � construire des objets {@link Station}
     * qui contiennent moins d'informations. Cet all�gement permet de r�duire le nombre de
     * requ�tes SQL, ce qui peut acc�l�rer l'obtention d'une {@linkplain #getEntries liste
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
     * Configure la requ�te SQL sp�cifi�e en fonction de la {@linkplain #getPlatform plateforme
     * courante} de cette table. Cette m�thode est appel�e automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged chang� d'�tat}.
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
     * Retourne la requ�te � utiliser pour obtenir les stations.
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
     * Construit une station pour l'enregistrement courant. L'impl�mentation par extrait une
     * premi�re s�rie d'informations telles que le {@linkplain Station#getName nom de la station},
     * {@linkplain Station#getProvider son fournisseur}, <cite>etc.</cite> et appele la m�thode
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
     * Construit une station � partir des informations sp�cifi�es. Cette m�thode est appel�e
     * automatiquement par {@link #createEntry(ResultSet)} apr�s avoir extrait les informations
     * communes � tous les types de stations. L'impl�mentation par d�faut ne fait que construire
     * un objet {@link StationEntry} sans extraire davantage d'informations. Les classes d�riv�es
     * devraient red�finir cette m�thode si elles souhaitent construire un type de station plus
     * �labor�.
     *
     * @param table      La table qui a produit cette entr�e.
     * @param identifier L'identifiant num�rique de la station.
     * @param name       Le nom de la station.
     * @param coordinate Une coordonn�e repr�sentative en degr�s de longitude et de latitude,
     *                   ou {@code null} si inconue.
     * @param timeRange  Plage de temps de cet �l�ment, ou {@code null} si inconue.
     * @param platform   La plateforme (par exemple un bateau) sur laquelle a �t� prise cette
     *                   station, ou {@code null} si inconnue.
     * @param quality    La qualit� de la donn�e, ou {@code null} si inconnue.
     * @param provider   La provenance de la donn�e, ou {@code null} si inconnue.
     * @param result     La ligne courante de la requ�te SQL. A utiliser seulement si les sous-classes
     *                   ont besoin d'extraire davantage d'informations que celles qui ont �t� fournies
     *                   par les arguments pr�c�dents.
     *
     * @throws SQLException si un acc�s � la base de donn�es �tait n�cessaire et a �chou�.
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
     * Indique si la m�thode {@link #getEntries} devrait accepter la station sp�cifi�e.
     * L'impl�mentation par d�faut v�rifie si le {@linkplain Station#getProvider fournisseur}
     * est l'un de ceux qui ont �t� sp�cifi�s � la m�thode {@link #acceptableProvider(Citation)
     * acceptableProvider}. Si la station ne donne pas d'indication sur le fournisseur, alors
     * cette m�thode va l'accepter comme approche conservative.
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
