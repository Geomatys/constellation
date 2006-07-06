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

// Sicade dependencies
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.Thematic;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.SingletonTable;
import net.sicade.observation.IllegalRecordException;


/**
 * Connexion vers la table des {@linkplain Thematic thèmes} traités par les
 * {@linkplain Series séries}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class ThematicTable extends SingletonTable<Thematic> implements Shareable {
    /**
     * Requête SQL pour obtenir un thème.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Thematics:SELECT",
            "SELECT name, description\n" +
            "  FROM \"Thematics\"\n"     +
            " WHERE name=?");

    /** Numéro de colonne. */ private static final int NAME    = 1;
    /** Numéro de colonne. */ private static final int REMARKS = 2;

    /**
     * Une instance unique de la table des sous-séries. Sera créée par {@link #getSubSeriesTable} la
     * première fois où elle sera nécessaire. <strong>Note:</strong> on évite de déclarer explicitement
     * le type {@link SubSeriesTable} afin d'éviter de charger les classes correspondantes trop tôt.
     */
    private transient Table subseries;

    /**
     * Construit une table des thèmes.
     * 
     * @param  database Connexion vers la base de données.
     */
    public ThematicTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir les thèmes.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Construit un thème pour l'enregistrement courant.
     */
    protected Thematic createEntry(final ResultSet results) throws SQLException {
        return new ThematicEntry(results.getString(NAME), results.getString(REMARKS));
    }

    /**
     * Retourne une instance unique de la table des sous-séries. Cette méthode est réservée à un
     * usage strictement interne par {@link SeriesTable}. En principe, les {@link SubSeriesTable}
     * ne sont pas {@linkplain Shareable partageable} car elle possèdent une méthode {@code set}.
     * Dans le cas particulier de {@link SeriesTable} toutefois, toutes les utilisations de
     * {@link SubSeriesTable} se font à l'intérieur d'un bloc synchronisé, de sorte qu'une
     * instance unique suffit.
     *
     * @param  type Doit obligatoirement être {@code SubSeriesTable.class}.
     * @return La table des sous-séries.
     */
    final synchronized <T extends Table> T getTable(final Class<T> type) {
        if (subseries == null) {
            subseries = database.getTable(type);
        }
        return type.cast(subseries);
    }
}
