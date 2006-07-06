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
 * (bateaux, campagnes d'�chantillonages...).
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PlatformTable extends BoundedSingletonTable<Platform> {
    /**
     * Requ�te SQL pour obtenir les limites g�ographiques des plateformes dans une r�gion.
     */
    private static final ConfigurationKey BOUNDING_BOX = new ConfigurationKey("Platforms:BBOX",
            "SELECT MIN(date), MAX(date), MIN(x), MAX(x), MIN(y), MAX(y)\n"          +
            "  FROM \"Locations\"\n"                                                 +
            "  JOIN \"Stations\" ON station=identifier\n"                            +
            " WHERE (date>=? AND date<=?) AND (x>=? AND x<=?) AND (y>=? AND y<=?)\n" +
            "   AND (provider LIKE ?)");

    /**
     * Requ�te SQL pour obtenir la liste des plateformes dans une r�gion.
     */
    private static final ConfigurationKey LIST = new ConfigurationKey("Platforms:LIST",
            "SELECT DISTINCT platform\n"                                             +
            "  FROM \"Stations\"\n"                                                  +
            "  JOIN \"Locations\" ON station=identifier\n"                           +
            " WHERE (date>=? AND date<=?) AND (x>=? AND x<=?) AND (y>=? AND y<=?)\n" +
            "   AND (provider LIKE ?)\n" +
            " ORDER BY date");

    /**
     * Requ�te SQL pour obtenir des informations sur une plateforme.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Platforms:SELECT",
            "SELECT DISTINCT platform\n" +
            "  FROM \"Stations\"\n"      +
            " WHERE platform=?");

    /** Num�ro d'argument. */ private static final int ARGUMENT_PROVIDER = 7;
    /** Num�ro de colonne. */ private static final int NAME              = 1;

    /**
     * Connexion vers la table permettant d'obtenir les trajectoires des plateformes. Une table
     * par d�faut sera construite la premi�re fois o� elle sera n�cessaire.
     */
    private LocationTable locations;

    /**
     * Connexion vers la table des {@linkplain Station stations}. Une table par d�faut sera
     * construite la premi�re fois o� elle sera n�cessaire.
     * <p>
     * <strong>IMPORTANT:</strong> La table des stations peut faire elle-m�me appel � cette
     * table {@code PlatformTable}.  En cons�quence, toutes m�thodes qui peut faire appel �
     * la table des stations doit se synchroniser sur {@code stations} <em>avant</em> de se
     * synchroniser sur {@code this}, afin d'�viter des situations de <cite>thread lock</cite>.
     */
    private StationTable stations;

    /**
     * Le fournisseur des stations recherch�es, ou {@code null} si on accepte tous les fournisseurs.
     */
    private String provider;

    /**
     * Construit une connexion vers la table des plateformes qui utilisera la base de donn�es
     * sp�cifi�e.
     */
    public PlatformTable(final Database database) {
        super(database, CRS.XYZT);
    }

    /**
     * D�finie la table des stations � utiliser. Cette m�thode peut �tre appel�e par
     * {@link StationTable} avant toute premi�re utilisation de {@code PlatformTable}.
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
            stations.setPlatformTable(this);
        }
    }

    /**
     * Retourne la table des stations � utiliser pour la cr�ation des objets {@link StationEntry}.
     */
    final StationTable getStationTable() {
        assert Thread.holdsLock(this);
        if (stations == null) {
            setStationTable(database.getTable(StationTable.class));
        }
        return stations;
    }

    /**
     * Retourne la table des positions � utiliser pour la cr�ation des objets {@link StationEntry}.
     */
    final LocationTable getLocationTable() {
        assert Thread.holdsLock(this);
        if (locations == null) {
            locations = database.getTable(LocationTable.Platform.class);
        }
        return locations;
    }

    /**
     * Retourne le fournisseur des plateformes d�sir�es, ou {@code null} pour obtenir toutes
     * les plateformes.
     */
    public final String getProvider() {
        return provider;
    }

    /**
     * D�finit le fournisseur des plateformes d�sir�es. Les prochains appels � la m�thode
     * {@link #getEntries() getEntries()} ne retourneront que les plateformes de ce fournisseur.
     * La valeur {@code null} s�lectionne toutes les plateformes.
     */
    public synchronized void setProvider(final String provider) {
        if (!Utilities.equals(provider, this.provider)) {
            this.provider = provider;
            fireStateChanged("Provider");
        }
    }

    /**
     * Configure la requ�te SQL sp�cifi�e en fonction du {@linkplain #getProvider provider}
     * des donn�es de cette table. Cette m�thode est appel�e automatiquement lorsque cette
     * table a {@linkplain #fireStateChanged chang� d'�tat}.
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
     * Retourne la requ�te SQL � utiliser pour obtenir les plateformes.
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
