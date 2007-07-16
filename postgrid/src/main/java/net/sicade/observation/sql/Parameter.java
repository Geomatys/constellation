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


/**
 * A SQL parameter for the selection of a singleton using a {@link Query}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Parameter extends IndexedSqlElement {
    /**
     * The column on which this parameter applies.
     */
    private final Column column;

    /**
     * The comparaison operator to put in the prepared statement.
     */
    private String comparator = "=";

    /**
     * Creates a new parameter for the specified query.
     *
     * @param query  The query for which the parameter is created.
     * @param column The column on which the parameter is applied.
     * @param types  The query types for which the parameter applies.
     */
    public Parameter(final Query query, final Column column, final QueryType... types) {
        super(query, types);
        this.column = column;
    }

    /**
     * Returns the name or alias of the column on which this parameter is applied.
     */
    public String getColumnName() {
        return column.alias;
    }

    /**
     * Returns the function applied on the column, or {@code null} if none.
     */
    public String getColumnFunction(final QueryType type) {
        if (column.name.equals(column.alias)) {
            return column.getFunction(type);
        }
        return null;
    }

    /**
     * Returns the role of the column on which this parameter is applied.
     */
    public Role getColumnRole() {
        return column.getRole();
    }

    /**
     * Returns the comparaison operator to put in the prepared statement.
     * The default value is {@code "="}.
     */
    public String getComparator() {
        return comparator;
    }

    /**
     * Sets the comparaison operator to put in the prepared statement.
     */
    public void setComparator(final String comparator) {
        this.comparator = comparator;
    }

    /**
     * Returns a string representation of this parameter.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + getColumnName() + ']';
    }
}
