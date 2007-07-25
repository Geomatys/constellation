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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.sicade.resources.XArray;


/**
 * A SQL query which may optionnaly expressed in a "spatial enabled" form. Spatial enabled
 * queries may include additional types as {@code BBOX}. They are typically defined in
 * optional extension like the PostGIS extension for PostgreSQL.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Query {
    /**
     * The SQL statement used for table join, spaces included. Some database
     * implementations require {@code "INNER JOIN"} instead of {@code "JOIN"}.
     */
    private static final String JOIN = " JOIN ";

    /**
     * A condition handled in a special way.
     */
    private static final String SPECIAL_CONDITION = "IS NULL OR";

    /**
     * An empty array of columns.
     */
    private static final Column[] EMPTY_COLUMNS = new Column[0];

    /**
     * An empty array of parameters.
     */
    private static final Parameter[] EMPTY_PARAMETERS = new Parameter[0];

    /**
     * The database for which this query is created, or {@code null} if none.
     */
    protected final Database database;

    /**
     * The catalog, or {@code null} if none.
     */
    private final String catalog;

    /**
     * The schema, or {@code null} if none.
     */
    private final String schema;

    /**
     * The columns in this query.
     */
    private Column[] columns = EMPTY_COLUMNS;

    /**
     * The parameters in this query.
     */
    private Parameter[] parameters = EMPTY_PARAMETERS;

    /**
     * SQL queries cached up to date.
     */
    private final Map<QueryType,String> cachedSQL = new HashMap<QueryType,String>();

    /**
     * Creates an initially empty query with no schema.
     *
     * @param database The database for which this query is created, or {@code null}.
     */
    public Query(final Database database) {
        this(database, null, null);
    }

    /**
     * Creates an initially empty query.
     *
     * @param database The database for which this query is created, or {@code null}.
     * @param catalog  The catalog, or {@code null} if none.
     * @param schema   The schema, or {@code null} if none.
     */
    private Query(final Database database, final String catalog, final String schema) {
        this.database = database;
        this.catalog  = catalog;
        this.schema   = schema;
    }

    /**
     * Adds the language elements, and returns the previous list of all elements.
     * The returned list will <strong>not</strong> contains the newly added element.
     * This is used by the {@link IndexedSqlElement} constructor only.
     */
    final IndexedSqlElement[] add(final IndexedSqlElement element) {
        final IndexedSqlElement[] old;
        if (element instanceof Column) {
            old = columns;
            columns = XArray.resize(columns, old.length + 1);
//          columns = Arrays.copyOf(columns, old.length + 1);  // Java 6
            columns[old.length] = (Column) element;
        } else if (element instanceof Parameter) {
            old = parameters;
            parameters = XArray.resize(parameters, old.length + 1);
//          parameters = Arrays.copyOf(parameters, old.length + 1);  // Java 6
            parameters[old.length] = (Parameter) element;
        } else {
            throw new AssertionError(element); // Should never happen.
        }
        return old;
    }

    /**
     * Creates a new column from the specified table with the specified name but no alias.
     *
     * @param table The name of the table that contains the column.
     * @param name  The column name.
     * @param types Types of the queries where the column shall appears, or {@code null}
     *              if the column is applicable to any kind of queries.
     * @return The newly added column.
     */
    protected Column addColumn(final String table, final String name, final QueryType... types) {
        return addColumn(table, name, name, types);
    }

    /**
     * Adds a new column from the specified table with the specified name and alias.
     *
     * @param table The name of the table that contains the column.
     * @param name  The column name.
     * @param alias An alias for the column.
     * @param types Types of the queries where the column shall appears, or {@code null}
     *              if the column is applicable to any kind of queries.
     * @return The newly added column.
     */
    protected Column addColumn(final String table, final String name, final String alias, final QueryType... types) {
        return new Column(this, table, name, alias, types);
        // The addition into this query is performed by the Column constructor.
    }

    /**
     * Adds a new parameter for the specified query.
     *
     * @param column The column on which the parameter is applied.
     * @param types Types of the queries where the parameter shall appears.
     * @return The newly added parameter.
     */
    protected Parameter addParameter(final Column column, final QueryType... types) {
        return new Parameter(this, column, types);
    }

    /**
     * Returns the columns for the specified type. For a statement created from the
     * <code>{@linkplain #select select}(type)</code> query, the value returned by
     * <code>{@linkplain ResultSet#getString(int) ResultSet.getString}(i)</code>
     * correponds to the {@linkplain Column column} at index <var>i</var>-1 in the list.
     *
     * @param  type The query type.
     * @return An immutable list of columns.
     *
     * @todo Current implementation do not handle correctly {@link SpatialColumn}
     *       using more than one column.
     */
    public List<Column> getColumns(final QueryType type) {
        return new IndexedSqlElementList<Column>(type, columns);
    }

    /**
     * Returns the parameters for the specified type. For a statement created from the
     * <code>{@linkplain #select select}(type)</code> query, the parameter set by
     * <code>{@linkplain PreparedStatement#setString(int,String) PreparedStatement.setString}(i, ...)</code>
     * correponds to the {@linkplain Parameter parameter} at index <var>i</var>-1 in the list.
     *
     * @param  type The query type.
     * @return An immutable list of parameters.
     *
     * @todo Current implementation do not handle correctly {@link SpatialParameter}
     *       using more than one parameter.
     */
    public List<Parameter> getParameters(final QueryType type) {
        return new IndexedSqlElementList<Parameter>(type, parameters);
    }

    /**
     * Creates the SQL statement for selecting all records.
     * No SQL parameters are expected for this statement.
     *
     * @param  buffer The buffer in which to write the SQL statement.
     * @param  type The query type.
     * @param  metadata The database metadata, used for inspection of primary and foreigner keys.
     * @param  joinParameters {@code true} if we should take parameters in account for determining
     *         the {@code JOIN ... ON} clauses.
     * @throws SQLException if an error occured while reading the database.
     */
    private void selectAll(final StringBuilder buffer, final QueryType type,
                           final DatabaseMetaData metadata, final boolean joinParameters)
            throws SQLException
    {
        /*
         * List all columns after the "SELECT" clause.
         * Keep trace of all involved tables in the process.
         */
        final String quote = metadata.getIdentifierQuoteString().trim();
        Map<String,ForeignerKey> tables = new LinkedHashMap<String,ForeignerKey>();
        String separator = "SELECT ";
        for (final Column column : columns) {
            if (column.indexOf(type) == 0) {
                continue; // Column not to be included for the requested query type.
            }
            buffer.append(separator);
            final String function = column.getFunction(type);
            if (function != null) {
                buffer.append(function).append('(');
            }
            buffer.append(quote).append(column.name).append(quote);
            if (function != null) {
                buffer.append(')');
            }
            if (!column.alias.equals(column.name)) {
                buffer.append(" AS ").append(quote).append(column.alias).append(quote);
            }
            separator = ", ";
            tables.put(column.table, null); // ForeignerKeys will be determined later.
        }
        if (joinParameters) {
            for (final Parameter parameter : parameters) {
                if (parameter.indexOf(type) != 0) {
                    tables.put(parameter.getColumnTable(), null);
                }
            }
        }
        /*
         * Optionally update the table order. First, we search for foreigner keys. We will use
         * this information later both for altering the table order and in order to construct
         * the "JOIN ... ON" clauses.
         */
        if (tables.size() >= 2) {
            for (final Map.Entry<String,ForeignerKey> entry : tables.entrySet()) {
                final String table = entry.getKey();
                final ResultSet pks = metadata.getExportedKeys(catalog, schema, table);
                while (pks.next()) {
                    assert catalog == null || catalog.equals(pks.getString("PKTABLE_CAT"  )) : catalog;
                    assert schema  == null || schema .equals(pks.getString("PKTABLE_SCHEM")) : schema;
                    assert table   == null || table  .equals(pks.getString("PKTABLE_NAME" )) : table;
                    final String pkColumn = pks.getString("PKCOLUMN_NAME");
                    // Consider only the tables from the same catalog.
                    if (catalog != null && !catalog.equals(pks.getString("FKTABLE_CAT"))) {
                        continue;
                    }
                    // Consider only the tables from the same schema.
                    if (schema != null && !schema.equals(pks.getString("FKTABLE_SCHEM"))) {
                        continue;
                    }
                    // Consider only the tables that are present in the SELECT statement.
                    final String fkTable = pks.getString("FKTABLE_NAME");
                    if (!tables.containsKey(fkTable) || table.equals(fkTable)) {
                        continue;
                    }
                    final String fkColumn = pks.getString("FKCOLUMN_NAME");
                    if (pks.getShort("KEY_SEQ") != 1) {
                        // Current implementation do not support multi-columns foreigner key.
                        pks.close();
                        throw new SQLException("Clé étrangère sur plusieurs colonnes dans la table \"" + table + "\".");
                    }
                    final Column       pk = new Column(null, table, pkColumn);
                    final ForeignerKey fk = new ForeignerKey(null, fkTable, fkColumn, pk);
                    final ForeignerKey ok = entry.setValue(fk);
                    if (ok != null && !fk.equals(ok)) {
                        // Current implementation supports only one foreigner key per table.
                        pks.close();
                        throw new SQLException("Multiple clés étrangères pour la table \"" + table + "\".");
                    }
                }
                pks.close();
            }
            /*
             * Copies the table in a new map with a potentially different order.
             * We try to move last the tables that use foreigner keys.
             */
            final Map<String,ForeignerKey> ordered = new LinkedHashMap<String,ForeignerKey>();
scan:       while (!tables.isEmpty()) {
                for (final Iterator<Map.Entry<String,ForeignerKey>> it=tables.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry<String,ForeignerKey> entry = it.next();
                    final String table = entry.getKey();
                    final ForeignerKey fk = entry.getValue();
                    if (fk == null || ordered.containsKey(fk.table)) {
                        // This table is unreferenced, or is referenced by a table already listed
                        // in the "FROM" or "JOIN" clause. Copy it to the ordered table list.
                        ordered.put(table, fk);
                        it.remove();
                        continue scan;
                    }
                }
                // None of the remaining tables can be moved.
                // Stop and copy inconditionnaly the remaining.
                break;
            }
            ordered.putAll(tables);
            tables = ordered;
        }
        /*
         * Write the "FROM" and "JOIN" clauses.
         */
        separator = " FROM ";
        for (final Map.Entry<String,ForeignerKey> entry : tables.entrySet()) {
            final String table = entry.getKey();
            buffer.append(separator).append(quote).append(table).append(quote);
            if (separator != JOIN) {
                separator = JOIN;
                assert entry.getValue() == null : entry;
                continue;
            }
            /*
             * At this point, we know that our "SELECT" clause uses more than one table.
             * Infer the "JOIN ... ON ..." statements from the primary and foreigner keys.
             */
            final ForeignerKey fk = entry.getValue();
            if (fk == null) {
                throw new SQLException("Aucune clé étrangère trouvée pour la table \"" + table + "\".");
            }
            final Column pk = fk.primaryKey;
            assert table.equals(pk.table) : table;
            buffer.append(" ON ");
            fk.qualified(buffer, quote);
            buffer.append('=');
            pk.qualified(buffer, quote);
        }
    }

    /**
     * Appends SQL parameter to the given SQL statement.
     *
     * @param  buffer The buffer in which to write the SQL statement.
     * @param  type The query type.
     * @param  metadata The database metadata.
     * @throws SQLException if an error occured while reading the database.
     */
    private void appendParameters(final StringBuilder buffer, final QueryType type,
                                  final DatabaseMetaData metadata) throws SQLException
    {
        final String quote = metadata.getIdentifierQuoteString().trim();
        String separator = " WHERE ";
        for (final Parameter p : parameters) {
            if (p.indexOf(type) != 0) {
                buffer.append(separator).append('(');
                final String variable   = p.getColumnName();
                final String function   = p.getColumnFunction(type);
                final String comparator = p.getComparator();
                final String[] comparators;
                if (comparator.startsWith(SPECIAL_CONDITION)) {
                    comparators = new String[] {
                        SPECIAL_CONDITION,
                        comparator.substring(SPECIAL_CONDITION.length()).trim(),
                    };
                } else {
                    comparators = new String[] {
                        comparator
                    };
                }
                for (int i=0; i<comparators.length; i++) {
                    if (function != null) {
                        buffer.append(function).append('(');
                    }
                    buffer.append(quote).append(variable).append(quote);
                    if (function != null) {
                        buffer.append(')');
                    }
                    buffer.append(' ').append(comparators[i]).append(' ');
                }
                final String f = p.getFunction(type);
                if (f != null) {
                    if (f.indexOf('?') >= 0) {
                        buffer.append(f);
                    } else if (f.startsWith("::")) {
                        buffer.append('?').append(f);
                    } else {
                        buffer.append(f).append("(?)");
                    }
                } else {
                    buffer.append('?');
                }
                buffer.append(')');
                separator = " AND ";
            }
        }
    }

    /**
     * Appends the {@code "ORDER BY"} clause to the given SQL statement.
     *
     * @param  buffer The buffer in which to write the SQL statement.
     * @param  type The query type.
     * @param  metadata The database metadata.
     * @throws SQLException if an error occured while reading the database.
     */
    private void appendOrdering(final StringBuilder buffer, final QueryType type,
                                final DatabaseMetaData metadata) throws SQLException
    {
        final String quote = metadata.getIdentifierQuoteString().trim();
        String separator = " ORDER BY ";
        for (final Column c : columns) {
            if (c.indexOf(type) != 0) {
                String ordering = c.getOrdering();
                if (ordering != null) {
                    buffer.append(separator).append(quote).append(c.name).append(quote);
                    if (!ordering.equals("ASC")) {
                        buffer.append(' ').append(ordering);
                    }
                    separator = ", ";
                }
            }
        }
    }

    /**
     * Creates the SQL statement for the query of the given type with no {@code WHERE} clause.
     * This is used for testing purpose.
     *
     * @param  type The query type.
     * @return The SQL statement.
     * @throws SQLException if an error occured while reading the database.
     */
    final String selectAll(final QueryType type) throws SQLException {
        final DatabaseMetaData metadata = database.getConnection().getMetaData();
        final StringBuilder buffer = new StringBuilder();
        selectAll     (buffer, type, metadata, false);
        appendOrdering(buffer, type, metadata);
        return buffer.toString();
    }

    /**
     * Creates the SQL statement for the query of the given type.
     *
     * @param  type The query type.
     * @return The SQL statement.
     * @throws SQLException if an error occured while reading the database.
     */
    public String select(final QueryType type) throws SQLException {
        String sql;
        synchronized (cachedSQL) {
            sql = cachedSQL.get(type);
            if (sql == null) {
                final DatabaseMetaData metadata = database.getConnection().getMetaData();
                final StringBuilder buffer = new StringBuilder();
                selectAll       (buffer, type, metadata, true);
                appendParameters(buffer, type, metadata);
                appendOrdering  (buffer, type, metadata);
                sql = buffer.toString();
                cachedSQL.put(type, sql);
            }
        }
        return sql;
    }

    /**
     * Returns the index of the first parameter having the specified role.
     * Parameter index start with 1. This method returns 0 if no suitable
     * parameter has been found.
     *
     * @param  type The query type on which the parameter apply.
     * @param  role The role for the parameter.
     * @return The parameter index (starting with 1), or 0 if none.
     */
    public int indexOfParameter(final QueryType type, final Role role) {
        for (final Parameter p : parameters) {
            if (p.getColumnRole() == role && p.indexOf(type) != 0) {
                return p.indexOf(type);
            }
        }
        return 0;
    }

    /**
     * Returns a string representation of this query, for debugging purpose.
     */
    @Override
    public String toString() {
        for (final QueryType type : QueryType.values()) {
            final String sql;
            try {
                sql = select(QueryType.SELECT);
            } catch (SQLException e) {
                return e.toString();
            }
            if (sql.length() != 0) {
                return sql;
            }
        }
        return "<empty>";
    }
}
