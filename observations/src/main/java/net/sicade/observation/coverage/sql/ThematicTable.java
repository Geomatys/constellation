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
 * Connexion vers la table des {@linkplain Thematic th�mes} trait�s par les
 * {@linkplain Series s�ries}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class ThematicTable extends SingletonTable<Thematic> implements Shareable {
    /**
     * Requ�te SQL pour obtenir un th�me.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Thematics:SELECT",
            "SELECT name, description\n" +
            "  FROM \"Thematics\"\n"     +
            " WHERE name=?");

    /** Num�ro de colonne. */ private static final int NAME    = 1;
    /** Num�ro de colonne. */ private static final int REMARKS = 2;

    /**
     * Une instance unique de la table des sous-s�ries. Sera cr��e par {@link #getSubSeriesTable} la
     * premi�re fois o� elle sera n�cessaire. <strong>Note:</strong> on �vite de d�clarer explicitement
     * le type {@link SubSeriesTable} afin d'�viter de charger les classes correspondantes trop t�t.
     */
    private transient Table subseries;

    /**
     * Construit une table des th�mes.
     * 
     * @param  database Connexion vers la base de donn�es.
     */
    public ThematicTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requ�te SQL � utiliser pour obtenir les th�mes.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Construit un th�me pour l'enregistrement courant.
     */
    protected Thematic createEntry(final ResultSet results) throws SQLException {
        return new ThematicEntry(results.getString(NAME), results.getString(REMARKS));
    }

    /**
     * Retourne une instance unique de la table des sous-s�ries. Cette m�thode est r�serv�e � un
     * usage strictement interne par {@link SeriesTable}. En principe, les {@link SubSeriesTable}
     * ne sont pas {@linkplain Shareable partageable} car elle poss�dent une m�thode {@code set}.
     * Dans le cas particulier de {@link SeriesTable} toutefois, toutes les utilisations de
     * {@link SubSeriesTable} se font � l'int�rieur d'un bloc synchronis�, de sorte qu'une
     * instance unique suffit.
     *
     * @param  type Doit obligatoirement �tre {@code SubSeriesTable.class}.
     * @return La table des sous-s�ries.
     */
    final synchronized <T extends Table> T getTable(final Class<T> type) {
        if (subseries == null) {
            subseries = database.getTable(type);
        }
        return type.cast(subseries);
    }
}
