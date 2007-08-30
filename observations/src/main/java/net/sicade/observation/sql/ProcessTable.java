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
import net.sicade.observation.ProcessEntry;

import org.opengis.observation.Process;
import net.sicade.catalog.ConfigurationKey;
import net.sicade.catalog.Database;
import net.sicade.catalog.Query;
import net.sicade.catalog.SingletonTable;


/**
 * Connexion vers la table des {@linkplain Procedure procédures}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Deprecated
public class ProcessTable extends SingletonTable<Process> {
    /**
     * Requête SQL pour obtenir une procédure.
     */
    private static final ConfigurationKey SELECT = null; // new ConfigurationKey("Procedures:SELECT",
//            "SELECT name, description\n" +
//            "  FROM \"Procedures\"\n" +
//            " WHERE name=?");

    /** Numéro de colonne. */ private static final int NAME        = 1;
    /** Numéro de colonne. */ private static final int DESCRIPTION = 2;

    /**
     * Construit une table des procédures.
     * 
     * @param  database Connexion vers la base de données.
     */
    public ProcessTable(final Database database) {
        super(new Query(database)); // TODO
    }

    /**
     * Construit une procédure pour l'enregistrement courant.
     */
    protected Process createEntry(final ResultSet results) throws SQLException {
        return new ProcessEntry(results.getString(NAME),
                                  results.getString(DESCRIPTION));
    }
}
