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
package net.seagis.observation.fishery.sql;

// SQL dependencies
import java.sql.ResultSet;
import java.sql.SQLException;

// Sicade dependencies
import net.seagis.catalog.ConfigurationKey;
import net.seagis.catalog.Database;
import net.seagis.catalog.SingletonTable;


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
        super(new net.seagis.catalog.Query(database, "species")); // TODO
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

