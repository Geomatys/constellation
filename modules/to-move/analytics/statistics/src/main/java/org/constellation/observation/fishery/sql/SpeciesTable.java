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
import org.constellation.catalog.SingletonTable;


/**
 * Table des espèces.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
@Deprecated
public class SpeciesTable extends SingletonTable<SpeciesEntry> {
    /**
     * Requête SQL pour obtenir un stage de développement à partir de son identifiant.
     */
    private static final ConfigurationKey SELECT = null; // new ConfigurationKey("Species:SELECT",
//            "SELECT name, english, french, latin, NULL AS remarks\n" +
//            "  FROM \"Species\"\n" +
//            " WHERE name LIKE ?");

    /** Numéro de colonne. */ private static final int  NAME    = 1;
    /** Numéro de colonne. */ private static final int  ENGLISH = 2;
    /** Numéro de colonne. */ private static final int  FRENCH  = 3;
    /** Numéro de colonne. */ private static final int  LATIN   = 4;
    /** Numéro de colonne. */ private static final int  REMARKS = 5;

    /**
     * Construit une connexion vers la table des espaces.
     *
     * @param  database Connexion vers la base de données.
     */
    public SpeciesTable(final Database database) {
        super(new org.constellation.catalog.Query(database, "species")); // TODO
    }

    /**
     * Construit une espèce pour l'enregistrement courant.
     */
    protected SpeciesEntry createEntry(final ResultSet result) throws SQLException {
        final String name    = result.getString(NAME);
        final String english = result.getString(ENGLISH);
        final String french  = result.getString(FRENCH);
        final String latin   = result.getString(LATIN);
        final String remarks = result.getString(REMARKS);
        return new SpeciesEntry(name, english, french, latin, remarks);
    }
}

