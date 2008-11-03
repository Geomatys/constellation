/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.observation.fishery.sql;

// SQL dependencies
import java.sql.ResultSet;
import java.sql.SQLException;

// Constellation dependencies
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.catalog.Query;
import org.constellation.catalog.SingletonTable;


/**
 * Interroge la base de données pour obtenir la liste des stades de 
 * développement des espèces observées.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
@Deprecated
public class StageTable extends SingletonTable<StageEntry> {
    /**
     * Requête SQL pour obtenir un stage de développement à partir de son identifiant.
     */
    private static final ConfigurationKey SELECT = null; // new ConfigurationKey("Stages:SELECT",
//            "SELECT name, NULL AS remarks\n" +
//            "  FROM \"Stages\"\n" +
//            " WHERE name LIKE ?");

    /** Numéro de colonne. */ private static final int  NAME    = 1;
    /** Numéro de colonne. */ private static final int  REMARKS = 2;

    /**
     * Construit une connexion vers la table des stages.
     *
     * @param  database Connexion vers la base de données.
     */
    public StageTable(final Database database) {
        super(new Query(database, "stage")); // TODO
    }

    /**
     * Construit un stage de développement pour l'enregistrement courant.
     */
    protected StageEntry createEntry(final ResultSet result) throws SQLException {
        final String name    = result.getString(NAME);
        final String remarks = result.getString(REMARKS);
        return new StageEntry(name, remarks);
    }
}
