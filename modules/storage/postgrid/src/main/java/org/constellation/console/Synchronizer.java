/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.console;

import java.sql.*;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import org.geotoolkit.io.TableWriter;
import org.geotoolkit.util.Utilities;
import org.geotools.resources.Arguments;

import org.constellation.catalog.Database;
import org.constellation.catalog.Element;
import org.constellation.catalog.LoggingLevel;


/**
 * Copies the content of a table from a database to an other database. This class is used when
 * there is two copies of a database (typically an experimental copy and an operational copy)
 * and we want to copy the content of the experimental database to the operational database.
 * <p>
 * <b>Mandatory arguments</b>
 * <ul>
 *   <li><p>{@code -config} <var>file</var><br>
 *     Configuration file containing source and target databases, and the list of tables
 *     to synchronize. See "{@code postgrid-sync.properties}" for an example.</p></li>
 * </ul>
 * <p>
 * <b>Optional arguments</b>
 * <ul>
 *   <li><p>{@code -delete-before-insert}<br>
 *     Empties the tables before to re-insert all entries. Used when table content need to
 *     be replaced, not just updated with new entries.</p></li>
 *   <li><p>{@code -pretend}<br>
 *     Prints the SQL statements to standard output without executing them.</p></li>
 * </ul>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Cédric Briançon
 */
public class Synchronizer {
    /**
     * The kind of synchronisation.
     */
    public static enum Policy {
        INSERT_ONLY, INSERT_OR_UPDATE, DELETE_BEFORE_INSERT
    };

    /**
     * The connection to the source database.
     */
    private final Database source;

    /**
     * The connection to the target database.
     */
    private final Database target;

    /**
     * The metadata for the source database. Will be fetch when first needed.
     */
    private transient DatabaseMetaData sourceMetadata;

    /**
     * The metadata for the target database. Will be fetch when first needed.
     */
    private transient DatabaseMetaData targetMetadata;

    /**
     * Where to print reports.
     */
    private final Writer out;

    /**
     * If {@code true}, no changes will be applied to the database. The {@code DELETE} and
     * {@code INSERT} statements will not be executed. This is useful for testing purpose,
     * or for getting the reports or log record without performing the action.
     */
    private boolean pretend;

    /**
     * Creates a synchronizer reading the configuration from the specified files.
     * Those files are read from the current directory if present, or from the user
     * directory as specified in {@link Database} javadoc otherwise.
     *
     * @param source The source configuration file.
     * @param target The target configuration file.
     * @param out Where to print reports.
     */
    private Synchronizer(final String source, final String target, final Writer out) throws IOException {
        this.source = new Database(null, source);
        this.target = new Database(null, target);
        this.out = out;
    }

    /**
     * Creates a synchronizer using the configuration files specified by
     * {@code "config-source"} and {@code "config-target"} properties.
     *
     * @param properties The properties where to look for {@code "config-source"}
     *        and {@code "config-target"} filenames.
     * @param out Where to print reports.
     */
    private Synchronizer(final Properties properties, final Writer out) throws IOException {
        this(properties.getProperty("config-source", "config-source.xml"),
             properties.getProperty("config-target", "config-target.xml"), out);
    }

    /**
     * Creates a synchronizer between two SQL connections.
     *
     * @param srcConnec A connection to the source database.
     * @param dstConnec A connection to the destination database.
     * @param out Where to print reports.
     *
     * @throws SQLException if a connection to the database fails.
     */
    public Synchronizer(final Connection srcConnec, final Connection dstConnec, final Writer out)
                        throws SQLException
    {
        try {
            source = new Database(srcConnec, null);
            final Properties props = new Properties();
            props.setProperty("ReadOnly", "false");
            target = new Database(dstConnec, props);
            this.out = out;
        } catch (IOException io) {
            /*
             * Should not occurs, because this exception is thrown only when an error occurs
             * in reading an XML configuration file, and here we directly have our connections.
             */
            throw new AssertionError(io);
        }
    }

    /**
     * Appends a table name to the given buffer using the given quote character.
     */
    private static void appendTableName(final StringBuilder buffer, final Database database,
                                        final String table, final String quote)
    {
        buffer.append(quote);
        if (database.schema != null) {
            buffer.append(database.schema).append(quote).append('.').append(quote);
        }
        buffer.append(table).append(quote);
    }

    /**
     * Returns {@code true} if the given array contains the given value. This is usually
     * an unefficient way to make this checks when invoked in a loop. But for this class,
     * the given array will be very short (often only one element, usually not more than
     * three), so it should be suffisient.
     *
     * @param array The array where to check for a value. Elements doesn't need to be
     *              sorted (and often they are not).
     * @param value The value to search in the given array.
     */
    private static boolean contains(final int[] array, final int value) {
        for (int i=0; i<array.length; i++) {
            if (array[i] == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the primary keys in the target database for the given table. If the primary keys
     * span over more than one column, then the columns are returned in sequence order. If the
     * table has no primary key, then this method returns an array of length 0.
     */
    private String[] getPrimaryKeys(final String table) throws SQLException {
        final String catalog = target.catalog;
        final String schema  = target.schema;
        final ResultSet results = targetMetadata.getPrimaryKeys(catalog, schema, table);
        String[] columns = new String[0];
        while (results.next()) {
            if (catalog!=null && !catalog.equals(results.getString("TABLE_CAT"))) {
                continue;
            }
            if (schema!=null && !schema.equals(results.getString("TABLE_SCHEM"))) {
                continue;
            }
            if (!table.equals(results.getString("TABLE_NAME"))) {
                continue;
            }
            final String column = results.getString("COLUMN_NAME");
            final int index = results.getShort("KEY_SEQ");
            if (index > columns.length) {
                columns = Arrays.copyOf(columns, index);
            }
            columns[index - 1] = column;
        }
        results.close();
        return columns;
    }

    /**
     * Returns the index of the specified column, or 0 if not found.
     *
     * @param  metadata The metadata to search into.
     * @param  name The column to search for.
     * @return The index of the specified column.
     */
    private static int getColumnIndex(final ResultSetMetaData metadata, final String column)
            throws SQLException
    {
        final int count = metadata.getColumnCount();
        for (int i=1; i<=count; i++) {
            if (column.equals(metadata.getColumnName(i))) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Returns the index of the specified columns. If a column is not found, its corresponding
     * index will be left to 0.
     */
    private static int[] getColumnIndex(final ResultSetMetaData metadata, final String[] columns)
            throws SQLException
    {
        final int[] index = new int[columns.length];
        for (int i=0; i<columns.length; i++) {
            index[i] = getColumnIndex(metadata, columns[i]);
        }
        return index;
    }

    /**
     * Deletes the content of the specified table in the target database.
     *
     * @param  table The name of the table in which to delete the records.
     * @param  condition A SQL condition (to be put after a {@code WHERE} clause) for
     *         the records to be deleted, or {@code null} for deleting the whole table.
     *
     * @throws SQLException if a reading or writing operation failed.
     */
    private void delete(final String table, final String condition) throws SQLException {
        final String quote = targetMetadata.getIdentifierQuoteString();
        final StringBuilder buffer = new StringBuilder("DELETE FROM ");
        appendTableName(buffer, target, table, quote);
        if (condition != null) {
            buffer.append(" WHERE ").append(condition);
        }
        final String sql = buffer.toString();
        final Statement targetStatement = target.getConnection().createStatement();
        final int count = pretend ? 0 : targetStatement.executeUpdate(sql);
        log(LoggingLevel.UPDATE, "delete", sql + '\n' + count + " lignes supprimées.");
        targetStatement.close();
    }

    /**
     * Copies the content of the specified table from source to the target database.
     * If a record already exists for the same primary key, the action will be determined
     * by the given policy.
     *
     * @param  table The name of the table to copy.
     * @param  condition A SQL condition (to be put after a {@code WHERE} clause) for
     *         the records to be copied, or {@code null} for copying the whole table.
     * @param  onExisting What to do with existing entries before to write new ones.
     * @throws SQLException if a reading or writing operation failed.
     * @throws IOException if an error occured while writting reports on this operation.
     */
    private void insert(final String table, final String condition, final Policy onExisting)
            throws SQLException, IOException
    {
        /*
         * Creates the SQL statement for the SELECT query, opens the source ResultSet and
         * gets the metadata (especially the column names). This ResultSet will stay open
         * until the end of this method. We will use the column names later in order to
         * build the INSERT statement for the target database.
         */
        final String quoteSource = sourceMetadata.getIdentifierQuoteString();
        final StringBuilder buffer = new StringBuilder("SELECT * FROM ");
        appendTableName(buffer, source, table, quoteSource);
        if (condition != null) {
            buffer.append(" WHERE ").append(condition);
        }
        String sql = buffer.toString();
        final Statement  sourceStatement = source.getConnection().createStatement();
        final ResultSet  sourceResultSet = sourceStatement.executeQuery(sql);
        final ResultSetMetaData metadata = sourceResultSet.getMetaData();
        final String[] sourceColumns = new String[metadata.getColumnCount()];
        for (int i=0; i<sourceColumns.length;) {
            sourceColumns[i] = metadata.getColumnName(++i);
        }
        log(LoggingLevel.SELECT, "insert", sql);
        /*
         * Gets the primary keys of the target table. We don't check for primary keys in the
         * source table since it may be a view. Then gets the index (counting from 1) in the
         * source table for those primary keys.
         */
        final String[] pkColumns = getPrimaryKeys(table);
        final int[] pkSourceIndex = new int[pkColumns.length];
        for (int i=0; i<pkColumns.length; i++) {
            final String name = pkColumns[i];
            if ((pkSourceIndex[i] = getColumnIndex(metadata, name)) == 0) {
                throw new SQLException("Primary key \"" + name + "\" defined in the target \"" +
                        table + "\" table is not found in the source table.");
            }
        }
        final int[] nonpkSourceIndex = new int[sourceColumns.length - pkSourceIndex.length];
        for (int i=0,j=0; i<sourceColumns.length;) {
            if (!contains(pkSourceIndex, ++i)) {
                nonpkSourceIndex[j++] = i;
            }
        }
        assert !contains(nonpkSourceIndex, 0);
        /*
         * Creates the SQL statement for the SELECT or UPDATE query in the target database.
         * This is used in order to search for existing entries before to insert a new one.
         * This operation can be performed only if the target table contains at least one
         * primary key column.
         */
        final PreparedStatement existing;
        final boolean update = nonpkSourceIndex.length != 0 && onExisting.equals(Policy.INSERT_OR_UPDATE);
        final String quoteTarget = targetMetadata.getIdentifierQuoteString();
        if (pkColumns.length == 0 || onExisting.equals(Policy.DELETE_BEFORE_INSERT)) {
            existing = null;
        } else {
            buffer.setLength(0);
            appendTableName(buffer.append(update ? "UPDATE " : "SELECT * FROM "), target, table, quoteTarget);
            if (update) {
                buffer.append(" SET ");
                boolean afterFirst = false;
                for (int i=0; i<nonpkSourceIndex.length; i++) {
                    if (afterFirst) buffer.append(',');
                    else afterFirst = true;
                    final String name = sourceColumns[nonpkSourceIndex[i] - 1];
                    buffer.append(quoteTarget).append(name).append(quoteTarget).append("=?");
                }
            }
            String separator = " WHERE ";
            for (int i=0; i<pkColumns.length; i++) {
                final String name = pkColumns[i];
                buffer.append(separator).append(quoteTarget).append(name).append(quoteTarget).append("=?");
                separator = " AND ";
            }
            sql = buffer.toString();
            existing = target.getConnection().prepareStatement(sql);
        }
        /*
         * Creates the target prepared statement for the INSERT queries. The parameters will
         * need to be filled in the same order than the column from the source SELECT query.
         */
        buffer.setLength(0);
        appendTableName(buffer.append("INSERT INTO "), target, table, quoteTarget);
        buffer.append(" (");
        for (int i=0; i<sourceColumns.length; i++) {
            if (i != 0) buffer.append(',');
            buffer.append(quoteTarget).append(sourceColumns[i]).append(quoteTarget);
        }
        buffer.append(") VALUES (");
        for (int i=0; i<sourceColumns.length; i++) {
            if (i != 0) buffer.append(',');
            buffer.append('?');
        }
        sql = buffer.append(')').toString();
        final PreparedStatement insertStatement = target.getConnection().prepareStatement(sql);
        /*
         * Reads all records from the source table and check if a corresponding records exists
         * in the target table. If such record exists and have identical content, then nothing
         * is done. If the content is not identical, then a warning is printed.
         */
        int[] sourceToTarget = null;
        TableWriter mismatchs = null;
        final Object[] primaryKeyValues = new Object[pkColumns.length];
        while (sourceResultSet.next()) {
            if (existing != null) {
                int param = 0;
                if (update) {
                    for (int i=0; i<nonpkSourceIndex.length; i++) {
                        final Object value = sourceResultSet.getObject(nonpkSourceIndex[i]);
                        existing.setObject(++param, value);
                    }
                }
                for (int i=0; i<pkSourceIndex.length; i++) {
                    final Object value = sourceResultSet.getObject(pkSourceIndex[i]);
                    existing.setObject(++param, value);
                    primaryKeyValues[i] = value;
                }
                int count = 0;
                if (update) {
                    count = existing.executeUpdate();
                } else {
                    final ResultSet targetResultSet = existing.executeQuery();
                    if (sourceToTarget == null) {
                        sourceToTarget = getColumnIndex(targetResultSet.getMetaData(), sourceColumns);
                    }
                    while (targetResultSet.next()) {
                        for (int i=0; i<sourceToTarget.length; i++) {
                            final int index = sourceToTarget[i];
                            if (index == 0) {
                                // Compares only the columns present in both tables.
                                continue;
                            }
                            final String source = sourceResultSet.getString(i+1);
                            final String target = targetResultSet.getString(index);
                            if (!Utilities.equals(source, target)) {
                                if (mismatchs == null) {
                                    mismatchs = createMismatchTable(table, pkColumns);
                                } else {
                                    mismatchs.nextLine();
                                }
                                for (int j=0; j<primaryKeyValues.length; j++) {
                                    mismatchs.write(String.valueOf(primaryKeyValues[j]));
                                    mismatchs.nextColumn();
                                }
                                mismatchs.write(sourceColumns[i]); mismatchs.nextColumn();
                                mismatchs.write(source);           mismatchs.nextColumn();
                                mismatchs.write(target);           mismatchs.nextLine();
                            }
                        }
                        count++;
                    }
                    targetResultSet.close();
                }
                if (count != 0) {
                    continue;
                }
            }
            /*
             * At this point, we know that we have a new element.
             * Now insert the new record in the target table.
             */
            for (int i=1; i<=sourceColumns.length; i++) {
                insertStatement.setObject(i, sourceResultSet.getObject(i));
            }
            final int count = pretend ? 1 : insertStatement.executeUpdate();
            if (count == 1) {
                log(LoggingLevel.UPDATE, "insert", target.format(insertStatement, sql));
            } else {
                log(Level.WARNING, "insert", String.valueOf(count) + " enregistrements ajoutés.");
            }
        }
        /*
         * Disposes all resources used by this method.
         */
        sourceResultSet.close();
        sourceStatement.close();
        insertStatement.close();
        if (existing != null) {
            existing.close();
        }
        if (mismatchs != null) {
            mismatchs.nextLine(TableWriter.SINGLE_HORIZONTAL_LINE);
            mismatchs.flush();
        }
    }

    /**
     * Creates an initially empty (except for the header) table of mismatchs.
     *
     * @param  table     The table name.
     * @param  pkColumns The column names.
     * @return A new table of mismatchs.
     * @throws IOException if an error occured while writing to the output stream.
     */
    private TableWriter createMismatchTable(final String table, final String[] pkColumns)
            throws IOException
    {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        out.write(lineSeparator);
        out.write(table);
        out.write(lineSeparator);
        final TableWriter mismatchs = new TableWriter(out, TableWriter.SINGLE_VERTICAL_LINE);
        mismatchs.nextLine(TableWriter.SINGLE_HORIZONTAL_LINE);
        for (int j=0; j<pkColumns.length; j++) {
            mismatchs.write(pkColumns[j]);
            mismatchs.nextColumn();
        }
        mismatchs.write("Colonne");
        mismatchs.nextColumn();
        mismatchs.write("Valeur à copier");
        mismatchs.nextColumn();
        mismatchs.write("Valeur existante");
        mismatchs.nextLine();
        mismatchs.nextLine(TableWriter.SINGLE_HORIZONTAL_LINE);
        return mismatchs;
    }

    /**
     * Copies or replaces the content of the specified table. The {@linkplain Map#keySet map keys}
     * shall contains the set of every tables to take in account; table not listed in this set will
     * be untouched. The associated values are the SLQ conditions to put in the {@code WHERE} clauses.
     * <p>
     * This method process {@code table} as well as dependencies found in {@code tables}.
     * Processed dependencies are removed from the {@code tables} map.
     *
     * @param table   The table to process.
     * @param tables  The (<var>table</var>, <var>condition</var>) mapping. This map will be modified.
     * @param onExisting What to do with existing entries before to write new ones.
     *
     * @throws SQLException if an error occured while reading or writting in the database.
     * @throws IOException if an error occured while writting reports on this operation.
     */
    private void copy(final String table, final Map<String,String> tables, final Policy onExisting)
            throws SQLException, IOException
    {
        String condition = tables.remove(table);
        if (condition != null) {
            condition = condition.trim();
            if (condition.length() == 0) {
                condition = null;
            }
        }
        if (onExisting.equals(Policy.DELETE_BEFORE_INSERT)) {
            delete(table, condition);
        }
        /*
         * Before to insert any new records, check if this table has some foreigner keys
         * toward other table.  If such tables are found, we will process them before to
         * add any record to the current table.
         */
        final String catalog = target.catalog;
        final String schema  = target.schema;
        final ResultSet dependencies = targetMetadata.getImportedKeys(catalog, schema, table);
        while (dependencies.next()) {
            String dependency = dependencies.getString("PKTABLE_CAT");
            if (catalog!=null && !catalog.equals(dependency)) {
                continue;
            }
            dependency = dependencies.getString("PKTABLE_SCHEM");
            if (schema!=null && !schema.equals(dependency)) {
                continue;
            }
            dependency = dependencies.getString("PKTABLE_NAME");
            if (tables.containsKey(dependency)) {
                copy(dependency, tables, onExisting);
            }
        }
        dependencies.close();
        insert(table, condition, onExisting);
    }

    /**
     * Copies or replaces the content of the specified tables. The {@linkplain Map#keySet map keys}
     * shall contains the set of every tables to take in account; table not listed in this set will
     * be untouched. The associated values are the SLQ conditions to put in the {@code WHERE} clauses.
     *
     * @param  tables The (<var>table</var>, <var>condition</var>) mapping. This map will be modified.
     * @param  onExisting What to do with existing entries before to write new ones.
     * @throws SQLException if an error occured while reading or writting in the database.
     * @throws IOException if an error occured while writting reports on this operation.
     */
    public void copy(final Map<String,String> tables, final Policy onExisting)
            throws SQLException, IOException
    {
        final String catalog = target.catalog;
        final String schema  = target.schema;
        sourceMetadata = source.getConnection().getMetaData();
        targetMetadata = target.getConnection().getMetaData();
search: while (!tables.isEmpty()) {
nextTable: for (final String table : tables.keySet()) {
                // Skips all tables that have dependencies.
                final ResultSet dependents = targetMetadata.getExportedKeys(catalog, schema, table);
                while (dependents.next()) {
                    if ((catalog==null || catalog.equals(dependents.getString("FKTABLE_CAT"))) &&
                        (schema ==null || schema .equals(dependents.getString("FKTABLE_SCHEM"))))
                    {
                        final String dependent = dependents.getString("FKTABLE_NAME");
                        if (tables.containsKey(dependent)) {
                            dependents.close();
                            continue nextTable;
                        }
                    }
                }
                dependents.close();
                // We have found a table which have no dependencies (a leaf).
                copy(table, tables, onExisting);
                continue search;
            }
            // We have been unable to find any leaf. Take a chance: process the first table.
            // An exception is likely to be throw, but we will have tried.
            for (final String table : tables.keySet()) {
                copy(table, tables, onExisting);
                continue search;
            }
        }
    }

    /**
     * Closes the database connections.
     */
    private void close() throws SQLException, IOException {
        sourceMetadata = null;
        targetMetadata = null;
        target.close();
        source.close();
    }

    /**
     * Writes an event to the logger.
     */
    private static void log(final Level level, final String method, final String message) {
        final LogRecord record = new LogRecord(level, message);
        record.setSourceClassName(Synchronizer.class.getName());
        record.setSourceMethodName(method);
        Element.LOGGER.log(record);
    }

    /**
     * Returns a copy of the specified properties as a map. The copy is performed mostly
     * because {@link Properties} parameterized type is {@code <Object,Object>} instead
     * of {@code <String,String>}.
     */
    private static Map<String,String> asMap(final Properties properties) {
        final Map<String,String> map = new HashMap<String,String>();
        for (final Map.Entry<Object,Object> entry : properties.entrySet()) {
            final String key = (String) entry.getKey();
            if (!key.equalsIgnoreCase("config-source") && !key.equalsIgnoreCase("config-target")) {
                map.put(key, (String) entry.getValue());
            }
        }
        return map;
    }

    /**
     * Runs from the command line.
     *
     * @param  args The command-line arguments.
     * @throws IOException  If an error occured while writing to the output stream.
     * @throws SQLException If an error occured while reading or writing the database.
     */
    public static void main(String[] args) throws IOException, SQLException {
        final Arguments arguments = new Arguments(args);
        final String config = arguments.getRequiredString("-config");
        final boolean deleteBeforeInsert = arguments.getFlag("-delete-before-insert");
        final boolean pretend = arguments.getFlag("-pretend");
        final Policy onExisting = deleteBeforeInsert ? Policy.DELETE_BEFORE_INSERT : Policy.INSERT_ONLY;
        args = arguments.getRemainingArguments(0);
        final Properties properties = new Properties();
        final InputStream in = new FileInputStream(config);
        properties.load(in);
        in.close();

        final Synchronizer synchronizer = new Synchronizer(properties, arguments.out);
        synchronizer.pretend = pretend;
        final Connection source = synchronizer.source.getConnection();
        final Connection target = synchronizer.target.getConnection();
        source.setReadOnly(true);
        target.setAutoCommit(false);
        boolean success = false;
        try {
            synchronizer.copy(asMap(properties), onExisting);
            success = true;
        } finally {
            if (success) {
                target.commit();
            } else {
                target.rollback();
            }
            synchronizer.close();
        }
    }
}
