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

import java.sql.ResultSet;
import java.sql.SQLException;

import net.sicade.observation.Phenomenon;
import net.sicade.observation.ConfigurationKey;


/**
 * Connexion vers la table des {@linkplain Phenomenon ph�nom�nes}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PhenomenonTable extends SingletonTable<Phenomenon> implements Shareable {
    /**
     * Requ�te SQL pour obtenir un ph�nom�ne.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Phenomenons:SELECT",
            "SELECT name, NULL as description\n" +
            "  FROM \"Phenomenons\"\n"           +
            " WHERE name=?");

    /** Num�ro de colonne. */ private static final int NAME    = 1;
    /** Num�ro de colonne. */ private static final int REMARKS = 2;

    /**
     * Construit une table des ph�nom�nes.
     * 
     * @param  database Connexion vers la base de donn�es.
     */
    public PhenomenonTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requ�te SQL � utiliser pour obtenir les proc�dures.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Construit un ph�nom�ne pour l'enregistrement courant.
     */
    protected Phenomenon createEntry(final ResultSet results) throws SQLException {
        return new PhenomenonEntry(results.getString(NAME), results.getString(REMARKS));
    }
}
