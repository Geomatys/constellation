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
package net.sicade.catalog;


/**
 * A parameter in a SQL {@linkplain Query query}. Parameters are created by
 * the {@link Query#addParameter addParameter(...)} method.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Parameter extends IndexedSqlElement {
    /**
     * The column on which this parameter applies.
     */
    final Column column;

    /**
     * The comparaison operator to put in the prepared statement.
     */
    private String comparator = "=";

    /**
     * Creates a new parameter for the specified query.
     *
     * @param query  The query for which the parameter is created.
     * @param column The column on which the parameter is applied.
     * @param types Types of the queries where the parameter shall appears.
     */
    Parameter(final Query query, final Column column, final QueryType... types) {
        super(query, types);
        this.column = column;
    }

    /**
     * Returns the function applied on the column, or {@code null} if none.
     */
    final String getColumnFunction(final QueryType type) {
        if (column.name.equals(column.alias)) {
            return column.getFunction(type);
        }
        return null;
    }

    /**
     * Returns the parameter index when used in a query of the given type. Valid index numbers
     * start at 1. This method returns 0 if this parameter is not applicable to a query of the
     * specified type.
     *
     * @param  type The query type.
     * @return The parameter index in the SQL prepared statment, or 0 if none.
     */
    @Override
    public int indexOf(final QueryType type) {
        return super.indexOf(type);
    }

    /**
     * Returns the function for this parameter when used in a query of the given type,
     * or {@code null} if none. The functions are typically "MIN" or "MAX".
     */
    @Override
    public String getFunction(final QueryType type) {
        return super.getFunction(type);
    }

    /**
     * Sets a function for this parameter when used in a query of the given type.
     * They are typically conversion functions like {@code "GeometryFromText(?,4326)"}.
     * The function must contains a question mark as in the above example.
     */
    @Override
    public void setFunction(final String function, final QueryType... types) {
        if (function.indexOf('?') == 0) {
            throw new IllegalArgumentException(function);
        }
        super.setFunction(function, types);
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
        return getClass().getSimpleName() + '[' + column.alias + ']';
    }
}
