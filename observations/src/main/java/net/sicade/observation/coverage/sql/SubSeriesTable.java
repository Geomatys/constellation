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
 * Connexion vers la table des sous-s�ries. Cette connexion est utilis�e en interne par le
 * {@linkplain SeriesTable table des s�ries}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Use(FormatTable.class)
@UsedBy(SeriesTable.class)
public class SubSeriesTable extends SingletonTable<SubSeries> {
    /**
     * Requ�te SQL utilis�e pour obtenir une sous-s�ries par son nom.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("SubSeries:SELECT",
        "SELECT identifier, layer, format, NULL as remarks\n"  +
        "  FROM \"Series\"\n"                                  +
        " WHERE identifier=?");

    /**
     * Requ�te SQL utilis�e pour obtenir une liste de sous-s�ries.
     */
    private static final ConfigurationKey LIST = new ConfigurationKey("SubSeries:LIST",
        "SELECT identifier, layer, format, NULL as remarks\n"  +
        "  FROM \"Series\"\n"                                  +
        " WHERE layer LIKE ?\n"                                +
        " ORDER BY identifier");

    /** Num�ro de colonne. */ private static final int NAME    = 1;
    /** Num�ro de colonne. */ private static final int SERIES  = 2;
    /** Num�ro de colonne. */ private static final int FORMAT  = 3;
    /** Num�ro de colonne. */ private static final int REMARKS = 4;

    /**
     * Connexion vers la table des formats.
     * Une connexion (potentiellement partag�e) sera �tablie la premi�re fois o� elle sera n�cessaire.
     */
    private FormatTable formats;

    /**
     * S�rie dont on veut les sous-s�ries, ou {@code null} pour les prendre tous.
     */
    private Series series;

    /**
     * Construit une table qui interrogera la base de donn�es sp�cifi�e.
     *
     * @param database  Connexion vers la base de donn�es d'observations.
     */
    public SubSeriesTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la s�rie d'images dont on veut les sous-s�ries, ou {@code null} si toutes les
     * sous-s�ries sont retenues.
     */
    public Series getSeries() {
        return series;
    }

    /**
     * D�finit la s�rie d'images dont on veut les sous-s�ries. Les prochains appels de la m�tohdes
     * {@link #getEntries() getEntries()} ne retourneront que les sous-s�ries de cette s�rie. La
     * valeur {@code null} fera retourner toutes les sous-s�ries.
     */
    public synchronized void setSeries(final Series series) {
        if (!Utilities.equals(series, this.series)) {
            this.series = series;
            fireStateChanged("Series");
        }
    }

    /**
     * Retourne la requ�te SQL � utiliser pour obtenir les sous-s�ries.
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
     * Configure la requ�te SQL sp�cifi�e en fonction de la {@linkplain #getSeries s�ries recherch�e}
     * par cette table. Cette m�thode est appel�e automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged chang� d'�tat}.
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
     * Construit une sous-s�rie pour l'enregistrement courant.
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
