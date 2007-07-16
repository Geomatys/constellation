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
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.Thematic;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Role;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.SingletonTable;
import static net.sicade.observation.sql.QueryType.*;


/**
 * Connection to a table of {@linkplain Thematic thematic} represented by {@linkplain Layer layers}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class ThematicTable extends SingletonTable<Thematic> implements Shareable {
    /**
     * Column name declared in the {@linkplain #query query}.
     */
    private final Column name, remarks;

    /**
     * Parameter declared in the {@linkplain #query query}.
     */
    private final Parameter byName;

    /**
     * Creates a thematic table.
     * 
     * @param database Connection to the database.
     */
    public ThematicTable(final Database database) {
        super(database);
        final QueryType[] usage = {SELECT, LIST};
        name    = new Column   (query, "Thematics", "name",        usage);
        remarks = new Column   (query, "Thematics", "description", usage);
        byName  = new Parameter(query, name, SELECT);
        name.setRole(Role.NAME);
        name.setOrdering("ASC");
    }

    /**
     * Creates a thematic entry for the current row in the specified result set.
     *
     * @throws SQLException if an error occured while reading the database.
     */
    protected Thematic createEntry(final ResultSet results) throws CatalogException, SQLException {
        return new ThematicEntry(results.getString(indexOf(name   )),
                                 results.getString(indexOf(remarks)));
    }
}
