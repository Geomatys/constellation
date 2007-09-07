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
package net.sicade.swe;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;
import net.sicade.swe.ReferenceQuery;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class ReferenceTable extends SingletonTable<Reference>{
    
    /**
     * Construit une table des reference.
     *
     * @param  database Connexion vers la base de données.
     */
    public ReferenceTable(final Database database) {
        this(new ReferenceQuery(database));
    }

    /**
     * Initialise l'identifiant de la table.
     */
    private ReferenceTable(final ReferenceQuery query) {
        super(query);
        setIdentifierParameters(query.byIdReference, null);
    }

    /**
     * Construit une reference pour l'enregistrement courant.
     */
    protected Reference createEntry(final ResultSet results) throws CatalogException, SQLException {
         final ReferenceQuery query = (ReferenceQuery) super.query;
         return new ReferenceEntry(results.getString(indexOf(query.idReference)));
    }
    
}
