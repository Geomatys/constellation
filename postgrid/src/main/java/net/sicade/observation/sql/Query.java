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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A SQL query which may optionnaly expressed in a "spatial enabled" form. Spatial enabled
 * queries may include additional types as {@code BBOX}. They are typically defined in
 * optional extension like the PostGIS extension for PostgreSQL.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Query implements Serializable {
    /**
     * The SQL statement used for table join. Some database implementations
     * require {@code " INNER JOIN "} instead of {@code " JOIN "}.
     */
    private static final String JOIN = "\nJOIN ";

    /**
     * An identifier for this query, typically in the form "{@code Table:ROLE}".
     * Example: {@code "GridCoverages:LIST"}, {@code "GridCoverages:SELECT"}.
     */
    private final String identifier;

    /**
     * The columns in this query.
     */
    private final ArrayList<Column> columns = new ArrayList<Column>();

    /**
     * Creates an initially empty query with no schema.
     *
     * @param identifier An identifier for this query, typically in the form "{@code Table:ROLE}".
     *        Example: {@code "GridCoverages:LIST"}, {@code "GridCoverages:SELECT"}.
     */
    public Query(final String identifier) {
        this(identifier, null);
    }

    /**
     * Creates an initially empty query.
     *
     * @param identifier An identifier for this query, typically in the form "{@code Table:ROLE}".
     *        Example: {@code "GridCoverages:LIST"}, {@code "GridCoverages:SELECT"}.
     */
    public Query(final String identifier, final String schema) {
        this.identifier = identifier;
    }

    /**
     * Adds the specified column to this query.
     *
     * @param  column The column to add.
     * @return The index for the newly added column.
     */
    final int add(final Column column) {
        final int index = columns.size();
        columns.add(column);
        return index;
    }

    /**
     * Creates the SQL statement.
     *
     * @param metadata The database metadata, used for inspection of primary and foreigner keys.
     * @param schema The schema, or {@code null} if none.
     */
    public String createSQL(final DatabaseMetaData metadata, final String schema)
            throws SQLException
    {
        columns.trimToSize();
        final String quote = metadata.getIdentifierQuoteString().trim();
        final Set<String> tables = new LinkedHashSet<String>();
        final StringBuilder buffer = new StringBuilder();
        String separator = "SELECT ";
        for (final Column column : columns) {
            buffer.append(separator).append(quote).append(column.name).append(quote);
            if (!column.alias.equals(column.name)) {
                buffer.append(" AS ").append(quote).append(column.alias).append(quote);
            }
            separator = ", ";
            tables.add(column.table);
        }
        separator = " FROM ";
        for (final String table : tables) {
            buffer.append(separator).append(quote).append(table).append(quote);
            if (separator == JOIN) {
                String lastJoin = null;
                final ResultSet pks = metadata.getImportedKeys(null, schema, table);
                while (pks.next()) {
                    // Consider only the tables from the same schema.
                    if (schema != null && !schema.equals(pks.getString("PKTABLE_SCHEM"))) {
                        continue;
                    }
                    // Consider only the tables that are present in this SELECT statement.
                    final String pkTable = pks.getString("PKTABLE_NAME");
                    if (!tables.contains(pkTable)) {
                        continue;
                    }
                    final String pkColumn = pks.getString("PKCOLUMN_NAME");
                    assert schema==null || schema.equals(pks.getString("FKTABLE_SCHEM"));
                    assert table.equals(pks.getString("FKTABLE_NAME"));
                    final String fkColumn = pks.getString("FKCOLUMN_NAME");
                    buffer.append(pkTable.equals(lastJoin) ? " AND " : " ON ").
                            append(quote).append(fkColumn).append(quote).
                            append(quote).append(pkTable ).append(quote).append('.').
                            append(quote).append(pkColumn).append(quote);
                    lastJoin = pkTable;
                }
            }
            separator = JOIN;
        }
        return buffer.toString();
    }
}
