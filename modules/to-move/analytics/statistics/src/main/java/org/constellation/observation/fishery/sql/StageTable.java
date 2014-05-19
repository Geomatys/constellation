/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
