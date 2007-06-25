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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

// Geotools dependencies
import org.geotools.resources.Utilities;

// Sicade dependencies
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.Format;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.SubSeries;
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.SingletonTable;


/**
 * Connexion vers la table des sous-séries. Cette connexion est utilisée en interne par le
 * {@linkplain SeriesTable table des séries}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Use(FormatTable.class)
@UsedBy(SeriesTable.class)
public class SubSeriesTable extends SingletonTable<SubSeries> {
    /**
     * Requête SQL utilisée pour obtenir une sous-séries par son nom.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("SubSeries:SELECT",
        "SELECT identifier, layer, format, NULL as remarks\n"  +
        "  FROM \"Series\"\n"                                  +
        " WHERE identifier=?");

    /**
     * Requête SQL utilisée pour obtenir une liste de sous-séries.
     */
    private static final ConfigurationKey LIST = new ConfigurationKey("SubSeries:LIST",
        "SELECT identifier, layer, format, NULL as remarks\n"  +
        "  FROM \"Series\"\n"                                  +
        " WHERE layer LIKE ?\n"                                +
        " ORDER BY identifier");

    /** Numéro de colonne. */ private static final int NAME    = 1;
    /** Numéro de colonne. */ private static final int SERIES  = 2;
    /** Numéro de colonne. */ private static final int FORMAT  = 3;
    /** Numéro de colonne. */ private static final int REMARKS = 4;

    /**
     * Connexion vers la table des formats.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private FormatTable formats;

    /**
     * Série dont on veut les sous-séries, ou {@code null} pour les prendre tous.
     */
    private Series series;

    /**
     * Construit une table qui interrogera la base de données spécifiée.
     *
     * @param database  Connexion vers la base de données d'observations.
     */
    public SubSeriesTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la série d'images dont on veut les sous-séries, ou {@code null} si toutes les
     * sous-séries sont retenues.
     */
    public Series getSeries() {
        return series;
    }

    /**
     * Définit la série d'images dont on veut les sous-séries. Les prochains appels de la métohdes
     * {@link #getEntries() getEntries()} ne retourneront que les sous-séries de cette série. La
     * valeur {@code null} fera retourner toutes les sous-séries.
     */
    public synchronized void setSeries(final Series series) {
        if (!Utilities.equals(series, this.series)) {
            this.series = series;
            fireStateChanged("Series");
        }
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir les sous-séries.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            case LIST:   return getProperty(LIST);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Configure la requête SQL spécifiée en fonction de la {@linkplain #getSeries séries recherchée}
     * par cette table. Cette méthode est appelée automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged changé d'état}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        switch (type) {
            case LIST: {
                statement.setString(1, escapeSearch(series!=null ? series.getName() : null));
            }
        }
    }

    /**
     * Construit une sous-série pour l'enregistrement courant.
     */
    protected SubSeries createEntry(final ResultSet results) throws CatalogException, SQLException {
        final String name    = results.getString(NAME);
        final String remarks = results.getString(REMARKS);
        if (formats == null) {
            formats = database.getTable(FormatTable.class);
        }
        final Format format = formats.getEntry(results.getString(FORMAT));
        return new SubSeriesEntry(name, format, remarks);
    }
}
