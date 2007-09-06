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

import java.util.Locale;
import org.geotools.resources.Utilities;


/**
 * A column in a SQL {@linkplain Query query}. Columns are created by
 * the {@link Query#addColumn addColumn(...)} method.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Column extends IndexedSqlElement {
    /**
     * A flag for column without {@linkplain #defaultValue default value}.
     */
    static final Object MANDATORY = new Object();

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
     * The default value, or {@link #MANDATORY} if the value is mandatory.
     * Should be a {@link Number}, a {@link String} or {@code null}.
     */
    final Object defaultValue;

    /**
     * The ordering: {@code "ASC"}, {@code "DESC"} or {@code null} if none.
     */
    private String ordering;

    /**
     * Creates a column from the specified table with the specified name but no alias.
     *
     * @param query The query for which the column is created.
     * @param table The table name in which this column appears.
     * @param name  The column name.
     */
    Column(final Query query, final String table, final String name) {
        this(query, table, name, name, MANDATORY, (QueryType[]) null);
    }

    /**
     * Creates a column from the specified table with the specified name and alias.
     * The new column is automatically added to the specified query.
     *
     * @param query The query for which the column is created.
     * @param table The name of the table that contains the column.
     * @param name  The column name.
     * @param alias An alias for the column.
     * @param defaultValue The default value if the column is not present in the database.
     *              Should be a {@link Number}, a {@link String} or {@code null}.
     * @param types Types of the queries where the column shall appears, or {@code null}
     *              if the column is applicable to any kind of queries.
     */
    Column(final Query query, final String table, final String name, final String alias,
           final Object defaultValue, final QueryType... types)
    {
        super(query, types);
        this.table = table.trim();
        this.name  = name .trim();
        this.alias = alias.trim();
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the column index when used in a query of the given type. Valid index numbers
     * start at 1. This method returns 0 if this column is not applicable to a query of the
     * specified type.
     *
     * @param  type The query type.
     * @return The column index in the SQL prepared statment, or 0 if none.
     */
    @Override
    public int indexOf(final QueryType type) {
        return super.indexOf(type);
    }

    /**
     * Returns the function for this column when used in a query of the given type,
     * or {@code null} if none.
     */
    @Override
    public String getFunction(final QueryType type) {
        return super.getFunction(type);
    }

    /**
     * Sets a function for this column when used in a query of the given type.
     * They are typically aggregate functions like {@code "MIN"} or {@code "MAX"}.
     */
    @Override
    public void setFunction(final String function, final QueryType... types) {
        super.setFunction(function, types);
    }

    /**
     * Returns the ordering: {@code "ASC"}, {@code "DESC"} or {@code null} if none.
     */
    public String getOrdering() {
        return ordering;
    }

    /**
     * Sets the ordering to the specified value.
     *
     * @todo Need to preserve the order in which this method has been invoked.
     */
    public void setOrdering(String ordering) {
        if (ordering != null) {
            ordering = ordering.trim().toUpperCase(Locale.ENGLISH);
        }
        this.ordering = ordering;
    }

    /**
     * Formats this column as a fully qualified name.
     *
     * @param buffer The buffer in which to write the name.
     * @param quote The database-dependent identifier quote.
     */
    final void qualified(final StringBuilder buffer, final String quote) {
        buffer.append(quote).append(table).append(quote).append('.')
              .append(quote).append(name).append(quote);
    }

    /**
     * Returns a hash code value for this column.
     */
    @Override
    public int hashCode() {
        return name.hashCode() + super.hashCode();
    }

    /**
     * Compares this column with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final Column that = (Column) object;
            return Utilities.equals(this.table,        that.table) &&
                   Utilities.equals(this.name,         that.name ) &&
                   Utilities.equals(this.alias,        that.alias) &&
                   Utilities.equals(this.defaultValue, that.defaultValue) &&
                   Utilities.equals(this.ordering,     that.ordering);
        }
        return false;
    }

    /**
     * Returns a string representation of this column.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + table + '.' + name + ']';
    }
}
