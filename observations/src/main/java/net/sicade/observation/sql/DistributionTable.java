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

import net.sicade.observation.Distribution;
import net.sicade.observation.ConfigurationKey;


/**
 * Connexion vers la table des {@linkplain Distribution distributions}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class DistributionTable extends SingletonTable<Distribution> implements Shareable {
    /**
     * Requ�te SQL pour obtenir une distribution.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Distributions:SELECT",
            "SELECT name, scale, \"offset\", log\n" +
            "  FROM \"Distributions\"\n"            +
            " WHERE name=?");

    /** Num�ro de colonne. */ private static final int NAME   = 1;
    /** Num�ro de colonne. */ private static final int SCALE  = 2;
    /** Num�ro de colonne. */ private static final int OFFSET = 3;
    /** Num�ro de colonne. */ private static final int LOG    = 4;

    /**
     * Construit une table des distributions.
     * 
     * @param  database Connexion vers la base de donn�es.
     */
    public DistributionTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requ�te SQL � utiliser pour obtenir les distributions.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Construit une distribution pour l'enregistrement courant.
     */
    protected Distribution createEntry(final ResultSet results) throws SQLException {
        return new DistributionEntry(results.getString(NAME),
                                     results.getDouble(SCALE),
                                     results.getDouble(OFFSET),
                                     results.getBoolean(LOG));
    }
}
