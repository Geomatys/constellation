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
package net.sicade.coverage.catalog.sql;

// J2SE dependencies
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sicade.coverage.catalog.CatalogException;
import net.sicade.coverage.catalog.Thematic;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;


/**
 * Connection to a table of {@linkplain Thematic thematic} represented by {@linkplain Layer layers}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class ThematicTable extends SingletonTable<Thematic> {
    /**
     * Creates a thematic table.
     * 
     * @param database Connection to the database.
     */
    public ThematicTable(final Database database) {
        super(new ThematicQuery(database));
        setIdentifierParameters(((ThematicQuery) query).byName, null);
    }

    /**
     * Creates a thematic entry from the current row in the specified result set.
     *
     * @throws SQLException if an error occured while reading the database.
     */
    protected Thematic createEntry(final ResultSet results) throws CatalogException, SQLException {
        final ThematicQuery query = (ThematicQuery) super.query;
        return new ThematicEntry(results.getString(indexOf(query.name   )),
                                 results.getString(indexOf(query.remarks)));
    }
}
