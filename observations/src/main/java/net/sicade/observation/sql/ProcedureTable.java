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
 */
package net.sicade.observation.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.sicade.observation.Procedure;
import net.sicade.observation.ConfigurationKey;


/**
 * Connexion vers la table des {@linkplain Procedure procédures}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ProcedureTable extends SingletonTable<Procedure> implements Shareable {
    /**
     * Requête SQL pour obtenir une procédure.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Procedures:SELECT",
            "SELECT name, description\n" +
            "  FROM \"Procedures\"\n" +
            " WHERE name=?");

    /** Numéro de colonne. */ private static final int NAME        = 1;
    /** Numéro de colonne. */ private static final int DESCRIPTION = 2;

    /**
     * Construit une table des procédures.
     * 
     * @param  database Connexion vers la base de données.
     */
    public ProcedureTable(final Database database) {
        super(database);
    }

    /**
     * Construit une procédure pour l'enregistrement courant.
     */
    protected Procedure createEntry(final ResultSet results) throws SQLException {
        return new ProcedureEntry(results.getString(NAME),
                                  results.getString(DESCRIPTION));
    }
}
