/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
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

import java.io.Serializable;


/**
 * A column in a SQL {@linkplain Query query}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Column implements Serializable {
    /**
     * The table name in which this column appears.
     */
    final String table;

    /**
     * The column name.
     */
    final String name;

    /**
     * The alias, or {@code name} if none.
     */
    final String alias;

    /**
     * The column index in the query.
     */
    private final int index;

    /**
     * Creates a column from the specified table with the specified name with no alias.
     *
     * @param table The table name in which this column appears.
     * @param name  The column name.
     */
    public Column(final Query query, final String table, final String name) {
        this(query, table, name, name);
    }

    /**
     * Creates a column from the specified table with the specified name with the specified alias.
     *
     * @param table The table name in which this column appears.
     * @param name  The column name.
     * @param alias The alias.
     */
    public Column(final Query query, final String table, final String name, final String alias) {
        this.table = table.trim();
        this.name  = name .trim();
        this.alias = alias.trim();
        index = query.add(this);
    }
}
