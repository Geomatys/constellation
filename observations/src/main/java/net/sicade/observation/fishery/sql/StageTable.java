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
package net.sicade.observation.fishery.sql;

// SQL dependencies
import java.sql.ResultSet;
import java.sql.SQLException;

// Sicade dependencies
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.fishery.Stage;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.SingletonTable;


/**
 * Interroge la base de donn�es pour obtenir la liste des stades de 
 * d�veloppement des esp�ces observ�es.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class StageTable extends SingletonTable<Stage> implements Shareable {
    /**
     * Requ�te SQL pour obtenir un stage de d�veloppement � partir de son identifiant.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Stages:SELECT",
            "SELECT name, NULL AS remarks\n" +
            "  FROM \"Stages\"\n" +
            " WHERE name LIKE ?");

    /** Num�ro de colonne. */ private static final int  NAME    = 1;
    /** Num�ro de colonne. */ private static final int  REMARKS = 2;

    /**
     * Construit une connexion vers la table des stages.
     *
     * @param  database Connexion vers la base de donn�es.
     */
    public StageTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requ�te � utiliser pour obtenir un stage de d�veloppement.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default: return super.getQuery(type);
        }
    }

    /**
     * Construit un stage de d�veloppement pour l'enregistrement courant.
     */
    protected Stage createEntry(final ResultSet result) throws SQLException {
        final String name    = result.getString(NAME);
        final String remarks = result.getString(REMARKS);
        return new StageEntry(name, remarks);
    }
}
