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
package net.sicade.observation.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.sicade.observation.Phenomenon;
import net.sicade.observation.ConfigurationKey;


/**
 * Connexion vers la table des {@linkplain Phenomenon phénomènes}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PhenomenonTable extends SingletonTable<Phenomenon> implements Shareable {
    /**
     * Requête SQL pour obtenir un phénomène.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Phenomenons:SELECT",
            "SELECT name, NULL as description\n" +
            "  FROM \"Phenomenons\"\n"           +
            " WHERE name=?");

    /** Numéro de colonne. */ private static final int NAME    = 1;
    /** Numéro de colonne. */ private static final int REMARKS = 2;

    /**
     * Construit une table des phénomènes.
     * 
     * @param  database Connexion vers la base de données.
     */
    public PhenomenonTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir les procédures.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Construit un phénomène pour l'enregistrement courant.
     */
    protected Phenomenon createEntry(final ResultSet results) throws SQLException {
        return new PhenomenonEntry(results.getString(NAME), results.getString(REMARKS));
    }
}
