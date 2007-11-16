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
package net.seagis.catalog;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.geotools.resources.Utilities;

import net.seagis.resources.XArray;
import net.seagis.resources.i18n.Resources;
import net.seagis.resources.i18n.ResourceKeys;


/**
 * A SQL query build from {@linkplain Column columns} and {@linkplain Parameter parameters}.
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
     * The name of the main table.
     */
    protected final String table;

    /**
     * The columns in this query.
     */
    private Column[] columns = EMPTY_COLUMNS;

    /**
     * The parameters in this query.
     */
    private Parameter[] parameters = EMPTY_PARAMETERS;

    /**
     * The ordering for each column. We stores this information in the query rather than
     * in the column because the column order is significant.
     * <p>
     * Values shall be {@code "ASC"} or {@code "DESC"}.
     */
    final Map<Column,String> ordering = new LinkedHashMap<Column,String>();

    /**
     * SQL queries cached up to date.
     */
    private final Map<QueryType,String> cachedSQL = new HashMap<QueryType,String>();

    /**
     * Creates an initially empty query with no schema.
     *
     * @param database The database for which this query is created, or {@code null}.
     * @param table    The main table name.
     */
    public Query(final Database database, final String table) {
        this.database = database;
        this.table    = table;
    }

    /**
     * Adds the language elements, and returns the previous list of all elements.
     * The returned list will <strong>not</strong> contains the newly added element.
     * This is used by the {@link IndexedSqlElement} constructor only.
     */
    final IndexedSqlElement[] add(final IndexedSqlElement element) {
        cachedSQL.clear();
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
     * Creates a new mandatory column with the specified name.
     *
     * @param name  The column name.
     * @param types Types of the queries where the column shall appears, or {@code null}
     *              if the column is applicable to any kind of queries.
     * @return The newly added column.
     */
    protected final Column addColumn(final String name, final QueryType... types) {
        return addForeignerColumn(table, name, types);
    }

    /**
     * Creates a new optional column with the specified name and default value.
     *
     * @param name  The column name.
     * @param defaultValue The default value if the column is not present in the database.
     *              Should be a {@link Number}, a {@link String} or {@code null}.
     * @param types Types of the queries where the column shall appears, or {@code null}
     *              if the column is applicable to any kind of queries.
     * @return The newly added column.
     */
    protected final Column addColumn(final String name, final Object defaultValue, final QueryType... types) {
        return addForeignerColumn(table, name, defaultValue, types);
    }

    /**
     * Creates a new mandatory column from the specified table with the specified name.
     *
     * @param table The name of the table that contains the column.
     * @param name  The column name.
     * @param types Types of the queries where the column shall appears, or {@code null}
     *              if the column is applicable to any kind of queries.
     * @return The newly added column.
     */
    protected Column addForeignerColumn(final String table, final String name, final QueryType... types) {
        return new Column(this, table, name, name, Column.MANDATORY, types);
        // The addition into this query is performed by the Column constructor.
    }

    /**
     * Creates a new optional column from the specified table with the specified name and default
     * value.
     *
     * @param table The name of the table that contains the column.
     * @param name  The column name.
     * @param defaultValue The default value if the column is not present in the database.
     *              Should be a {@link Number}, a {@link String} or {@code null}.
     * @param types Types of the queries where the column shall appears, or {@code null}
     *              if the column is applicable to any kind of queries.
     * @return The newly added column.
     */
    protected Column addForeignerColumn(final String table, final String name, final Object defaultValue, final QueryType... types) {
        return new Column(this, table, name, name, defaultValue, types);
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
     */
    public List<Parameter> getParameters(final QueryType type) {
        return new IndexedSqlElementList<Parameter>(type, parameters);
    }

    /**
     * Returns the column names for the specified table.
     *
     * @param  metadata The database metadata.
     * @param  table The table name.
     * @return The columns in the specified table.
     * @throws SQLException if an error occured while reading the database.
     */
    private Set<String> getColumnNames(final DatabaseMetaData metadata, final String table)
            throws SQLException
    {
        final Set<String> columns = new HashSet<String>();
        ResultSet results = metadata.getColumns(database.catalog, database.schema, table, null);
        while (results.next()) {
            columns.add(results.getString("COLUMN_NAME"));
        }
        results.close();
        return columns;
    }

    /**
     * Returns {@code true} if this query contains at least one column or parameter
     * for the given type.
     */
    private boolean useQueryType(final QueryType type) {
        for (final IndexedSqlElement element : columns) {
            if (element.indexOf(type) != 0) {
                return true;
            }
        }
        for (final IndexedSqlElement element : parameters) {
            if (element.indexOf(type) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the SQL statement for selecting all records.
     * No SQL parameters are expected for this statement.
     *
     * @param  buffer     The buffer in which to write the SQL statement.
     * @param  type       The query type.
     * @param  maxColumns The maximum number of columns to put in the query.
     * @param  metadata   The database metadata, used for inspection of primary and foreigner keys.
     * @param  joinParameters {@code true} if we should take parameters in account for determining
     *         the {@code JOIN ... ON} clauses.
     * @throws SQLException if an error occured while reading the database.
     */
    private void selectAll(final StringBuilder buffer, final QueryType type, int maxColumns,
                           final DatabaseMetaData metadata, final boolean joinParameters)
            throws SQLException
    {
        /*
         * Lists all columns after the "SELECT" clause.
         * Keep trace of all involved tables in the process.
         */
        final String quote = metadata.getIdentifierQuoteString().trim();
        Map<String,CrossReference> tables = new LinkedHashMap<String,CrossReference>();
        Map<String,Set<String>> columnNames = null;
        String separator = "SELECT ";
        for (final Column column : columns) {
            if (column.indexOf(type) == 0) {
                // Column not to be included for the requested query type.
                continue;
            }
            if (--maxColumns < 0) {
                // Reached the maximal amount of columns to accept.
                continue;
            }
            final String table = column.table; // Because often requested.
            /*
             * Checks if the column exists in the table. This check is performed only if the column
             * is optional. For mandatory columns, we will inconditionnaly insert the column in the
             * SELECT clause and lets the SQL driver throws the appropriate exception later.
             */
            final boolean columnExists;
            if (column.defaultValue == Column.MANDATORY) {
                columnExists = true;
            } else {
                if (columnNames == null) {
                    columnNames = new HashMap<String,Set<String>>();
                }
                Set<String> columns = columnNames.get(table);
                if (columns == null) {
                    columns = getColumnNames(metadata, table);
                    columnNames.put(table, columns);
                }
                columnExists = columns.contains(column.name);
                if (!columnExists) {
                    final LogRecord record = new LogRecord(Level.CONFIG, Resources.format(
                            ResourceKeys.COLUMN_NOT_FOUND_$3, column.name, table, column.defaultValue));
                    record.setSourceClassName(Utilities.getShortClassName(this));
                    record.setSourceMethodName("select");
                    Element.LOGGER.log(record);
                }
            }
            /*
             * Appends the column name in the SELECT clause, or the default value if the column
             * doesn't exist in the current database.
             */
            buffer.append(separator);
            if (columnExists) {
                final String function = column.getFunction(type);
                appendFunctionPrefix(buffer, function);
                buffer.append(quote).append(column.name).append(quote);
                appendFunctionSuffix(buffer, function);
            } else {
                // Don't put quote for number, boolean and null values.
                final boolean needQuotes = (column.defaultValue instanceof CharSequence);
                String defaultValue = String.valueOf(column.defaultValue); // May be "null"
                if (needQuotes) {
                    buffer.append(quote);
                } else {
                    defaultValue = defaultValue.toUpperCase(Locale.ENGLISH);
                }
                buffer.append(defaultValue);
                if (needQuotes) {
                    buffer.append(quote);
                }
            }
            /*
             * Declares the alias if needed. This part is mandatory if the
             * column doesn't exist and has been replaced by a default value.
             */
            if (!columnExists || !column.alias.equals(column.name)) {
                buffer.append(" AS ").append(quote).append(column.alias).append(quote);
            }
            separator = ", ";
            tables.put(table, null); // ForeignerKeys will be determined later.
        }
        if (joinParameters) {
            for (final Parameter parameter : parameters) {
                if (parameter.indexOf(type) != 0) {
                    tables.put(parameter.column.table, null);
                }
            }
        }
        /*
         * Optionally update the table order. First, we search for foreigner keys. We will use
         * this information later both for altering the table order and in order to construct
         * the "JOIN ... ON" clauses.
         */
        final String catalog = database.catalog;
        final String schema  = database.schema;
        if (tables.size() >= 2) {
            for (final Map.Entry<String,CrossReference> entry : tables.entrySet()) {
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
                    final Column pk = new Column(null,   table, pkColumn);
                    final Column fk = new Column(null, fkTable, fkColumn);
                    final CrossReference ref = new CrossReference(fk, pk);
                    final CrossReference old = entry.setValue(ref);
                    if (old != null && !ref.equals(old)) {
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
            final Map<String,CrossReference> ordered = new LinkedHashMap<String,CrossReference>();
scan:       while (!tables.isEmpty()) {
                for (final Iterator<Map.Entry<String,CrossReference>> it=tables.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry<String,CrossReference> entry = it.next();
                    final String table = entry.getKey();
                    final CrossReference ref = entry.getValue();
                    if (ref == null || ordered.containsKey(ref.foreignerKey.table)) {
                        // This table is unreferenced, or is referenced by a table already listed
                        // in the "FROM" or "JOIN" clause. Copy it to the ordered table list.
                        ordered.put(table, ref);
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
        for (final Map.Entry<String,CrossReference> entry : tables.entrySet()) {
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
            final CrossReference ref = entry.getValue();
            if (ref == null) {
                throw new SQLException("Aucune clé étrangère trouvée pour la table \"" + table + "\".");
            }
            assert table.equals(ref.primaryKey.table) : table;
            buffer.append(" ON ");
            ref.foreignerKey.qualified(buffer, quote);
            buffer.append('=');
            ref.primaryKey.qualified(buffer, quote);
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
                final String variable   = p.column.alias;
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
                    // Reminder: aggregate functions are not allowed in a WHERE clause.
                    buffer.append(quote).append(variable).append(quote)
                          .append(' ').append(comparators[i]).append(' ');
                }
                final String function = p.getFunction(type);
                if (function != null) {
                    buffer.append(function);
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
        for (final Column c : ordering.keySet()) {
            String ordering = c.getOrdering(type);
            if (ordering != null) {
                buffer.append(separator).append(quote).append(c.name).append(quote);
                if (!ordering.equals("ASC")) {
                    buffer.append(' ').append(ordering);
                }
                separator = ", ";
            }
        }
    }

    /**
     * Creates the SQL statement for the query of the given type with no {@code WHERE} clause.
     * This is mostly used for debugging purpose.
     *
     * @param  type The query type.
     * @return The SQL statement.
     * @throws SQLException if an error occured while reading the database.
     */
    final String selectAll(final QueryType type) throws SQLException {
        final DatabaseMetaData metadata = database.getConnection().getMetaData();
        final StringBuilder buffer = new StringBuilder();
        selectAll(buffer, type, Integer.MAX_VALUE, metadata, false);
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
                QueryType buildType   = type;
                int       maxColumns  = Integer.MAX_VALUE;
                boolean   sortEntries = true;
                /*
                 * If the type is not described at all in this query, then tries
                 * to fallback on some default depending on the query type.
                 */
                if (!useQueryType(type)) switch (type) {
                    case EXISTS: {
                        /*
                         * The user asked for a query of type EXISTS but didn't provided any explicit
                         * definition for it. We will fallback on a default (and often suffisient) behavior:
                         * handle EXISTS in the same way than SELECT, except that we will fetch only the first
                         * column (usually the identifier) instead of all of them. Since we only want to see
                         * if at least one row exists, this is usually suffisient.
                         */
                        buildType   = QueryType.SELECT;
                        maxColumns  = 1;
                        sortEntries = false;
                    }
                }
                final DatabaseMetaData metadata = database.getConnection().getMetaData();
                final StringBuilder buffer = new StringBuilder();
                selectAll(buffer, buildType, maxColumns, metadata, true);
                appendParameters(buffer, buildType, metadata);
                if (sortEntries) {
                    appendOrdering(buffer, buildType, metadata);
                }
                sql = buffer.toString();
                cachedSQL.put(type, sql);
            }
        }
        return sql;
    }

    /**
     * Creates the SQL statement for inserting elements in the table that contains the given
     * column. This method should be invoked only for queries of type {@link QueryType#INSERT}.
     *
     * @param  table The name of the table in which to insert a statement.
     * @return The SQL statement, or {@code null} if there is no column in the query.
     * @throws SQLException if an error occured while reading the database.
     */
    final String insert(final String table) throws SQLException {
        final DatabaseMetaData metadata = database.getConnection().getMetaData();
        final String quote = metadata.getIdentifierQuoteString().trim();
        final Set<String> columnNames = getColumnNames(metadata, table);
        final StringBuilder buffer = new StringBuilder("INSERT INTO ");
        if (database.catalog != null) {
            buffer.append(quote).append(database.catalog).append(quote).append('.');
        }
        if (database.schema != null) {
            buffer.append(quote).append(database.schema).append(quote).append('.');
        }
        buffer.append(quote).append(table).append(quote);
        String separator = " (";
        int count = 0;
        final String[] functions = new String[columns.length];
        for (final Column column : columns) {
            if (!table.equals(column.table) || !columnNames.contains(column.name)) {
                // Column not to be included for an insert statement.
                continue;
            }
            final int index = column.indexOf(QueryType.INSERT);
            if (index == 0) {
                /*
                 * We require the column to be explicitly declared as to be included in an INSERT
                 * statement. This is in order to reduce the risk of unintentional write into the
                 * database, and also because some columns are expected to be left to their default
                 * value (sometime computed by trigger, e.g. GridGeometries.horizontalExtent).
                 */
                continue;
            }
            functions[count] = column.getFunction(QueryType.INSERT);
            if (++count != index) {
                // Safety check.
                throw new IllegalStateException(String.valueOf(column));
            }
            buffer.append(separator).append(quote).append(column.name).append(quote);
            separator = ", ";
        }
        if (count == 0) {
            return null;
        }
        buffer.append(") VALUES");
        separator = " (";
        for (int i=0; i<count; i++) {
            final String function = functions[i];
            appendFunctionPrefix(buffer, function);
            buffer.append(separator).append('?');
            appendFunctionSuffix(buffer, function);
            separator = ", ";
        }
        return buffer.append(')').toString();
    }

    /**
     * Appends the specified function before its operands.
     */
    private static void appendFunctionPrefix(final StringBuilder buffer, final String function) {
        if (function != null) {
            if (!function.startsWith("::")) {
                buffer.append(function).append('(');
            }
        }
    }

    /**
     * Appends the specified function after its operands.
     */
    private static void appendFunctionSuffix(final StringBuilder buffer, final String function) {
        if (function != null) {
            if (function.startsWith("::")) {
                buffer.append(function);
            } else {
                buffer.append(')');
            }
        }
    }

    /**
     * Returns a string representation of this query, for debugging purpose.
     */
    @Override
    public String toString() {
        for (final QueryType type : QueryType.values()) {
            final String sql;
            try {
                sql = select(type);
            } catch (SQLException e) {
                return e.toString();
            }
            if (sql!=null && sql.length() != 0) {
                return sql;
            }
        }
        return "<empty>";
    }
}
