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
package net.sicade.catalog;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.geotools.resources.Arguments;


/**
 * Copies the content of a table from a database to an other database. This class is used when
 * there is two copies of a database (typically an experimental copy and an operational copy)
 * and we want to copy the content of the experimental database to the operational database.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Synchronizer {
    /**
     * The connection to the source database.
     */
    private final Database source;

    /**
     * The connection to the target database.
     */
    private final Database target;

    /**
     * The metadata for the target database. Will be fetch when first needed.
     */
    private transient DatabaseMetaData metadata;

    /**
     * Creates a synchronizer reading the configuration from the
     * {@code "config-source.xml"} and {@code "config-target.xml"} files.
     * Those files are read from the current directory if present, or from
     * the user directory as specified in {@link Database} javadoc otherwise.
     */
    private Synchronizer() throws IOException {
        this("config-source.xml", "config-target.xml");
    }

    /**
     * Creates a synchronizer reading the configuration from the specified files.
     * Those files are read from the current directory if present, or from the user
     * directory as specified in {@link Database} javadoc otherwise.
     *
     * @param source The source configuration file. Default to {@code "config-source.xml"}.
     * @param target The target configuration file. Default to {@code "config-target.xml"}.
     */
    private Synchronizer(final String source, final String target) throws IOException {
        this.source = new Database(null, source);
        this.target = new Database(null, target);
    }

    /**
     * Deletes the content of the specified table in the target database.
     *
     * @param  table The name of the table in which to replace the records.
     * @param  condition A SQL condition (to be put after a {@code WHERE} clause) for
     *         the records to be deleted, or {@code null} for deleting the whole table.
     * @throws SQLException if a reading or writing operation failed.
     */
    private void delete(final String table, final String condition) throws SQLException {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("DELETE FROM \"").append(table).append('"');
        if (condition != null) {
            buffer.append(" WHERE ").append(condition);
        }
        final String sql = buffer.toString();
        final Statement targetStmt = target.getConnection().createStatement();
        final int count = targetStmt.executeUpdate(sql);
        log(LoggingLevel.DELETE, "delete", sql + '\n' + count + " lignes supprimées.");
        targetStmt.close();
    }

    /**
     * Copies the content of the specified table from source to the target database.
     *
     * @param  table The name of the table to copy.
     * @param  condition A SQL condition (to be put after a {@code WHERE} clause) for
     *         the records to be copied, or {@code null} for copying the whole table.
     * @throws SQLException if a reading or writing operation failed.
     */
    private void insert(final String table, final String condition) throws SQLException {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("SELECT * FROM \"").append(table).append('"');
        if (condition != null) {
            buffer.append(" WHERE ").append(condition);
        }
        String sql = buffer.toString();
        final Statement sourceStmt = source.getConnection().createStatement();
        final Statement targetStmt = target.getConnection().createStatement();
        final ResultSet          sources = sourceStmt.executeQuery(sql);
        final ResultSetMetaData metadata = sources.getMetaData();
        final String[]           columns = new String[metadata.getColumnCount()];
        for (int i=0; i<columns.length;) {
            columns[i] = metadata.getColumnName(++i);
        }
        log(LoggingLevel.SELECT, "insert", sql);
        buffer.setLength(0);
        buffer.append("INSERT INTO \"").append(table).append("\" (");
        for (int i=0; i<columns.length; i++) {
            if (i != 0) {
                buffer.append(',');
            }
            buffer.append('"').append(columns[i]).append('"');
        }
        buffer.append(") VALUES (");
        final int valuesStart = buffer.length();
        while (sources.next()) {
            buffer.setLength(valuesStart);
            for (int i=0; i<columns.length;) {
                if (i != 0) {
                    buffer.append(',');
                }
                buffer.append('\'').append(sources.getString(++i)).append('\'');
            }
            buffer.append(')');
            sql = buffer.toString();
            final int count = targetStmt.executeUpdate(sql);
            if (count == 1) {
                log(LoggingLevel.INSERT, "insert", sql);
            } else {
                log(Level.WARNING, "insert", String.valueOf(count) + " enregistrements ajoutés.");
            }
        }
        sources.close();
        sourceStmt.close();
        targetStmt.close();
    }

    /**
     * Replaces the content of the specified table. The {@linkplain Map#keySet map keys} shall
     * contains the set of every tables to take in account; table not listed in this set will
     * be untouched. The associated values are the SLQ conditions to put in the {@code WHERE}
     * clauses.
     * <p>
     * This method process {@code table} as well as dependencies found in {@code tables}.
     */
    private void replace(final String table, final Map<String,String> tables) throws SQLException {
        final String condition = tables.remove(table);
        delete(table, condition);
        /*
         * Before to insert any new records, check if this table has some foreigner keys
         * toward other table. If such tables are found, they will be processed them before
         * we add any record to the current table.
         */
        final String catalog = target.catalog;
        final String schema  = target.schema;
        if (metadata == null) {
            metadata = target.getConnection().getMetaData();
        }
        final ResultSet dependencies = metadata.getImportedKeys(catalog, schema, table);
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
                replace(dependency, tables);
            }
        }
        dependencies.close();

        insert(table, condition);
    }

    /**
     * Replaces the content of the specified table in the target database. The content of the target
     * table will be cleared before the new content is added. If {@code condition} is {@code null},
     * then the whole table will be replaced. If {@code condition} is not null, then only the records
     * matching the specified condition will be replaced.
     *
     * @param  table The name of the table in which to replace the records.
     * @param  condition A SQL condition (to be put after a {@code WHERE} clause) for the
     *         records to be replaced, or {@code null} for replacing the whole table.
     * @throws SQLException if a reading or writing operation failed.
     */
    private void replace(String table, String condition) throws SQLException {
        /*
         * Arguments check.
         */
        table = table.trim();
        if (condition != null) {
            condition = condition.trim();
            if (condition.length() == 0) {
                condition = null;
            }
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append("DELETE FROM \"").append(table).append('"');
        if (condition != null) {
            buffer.append(" WHERE ").append(condition);
        }
        String sql = buffer.toString();
        /*
         * Gets the connections to the databases.
         */
        final Connection source = this.source.getConnection();
        final Connection target = this.target.getConnection();
        source.setReadOnly(true);
        target.setAutoCommit(false);
        final Statement sourceStmt = source.createStatement();
        final Statement targetStmt = target.createStatement();
        boolean success = false;
        try {
            /*
             * Delete the old records from the target table.
             */
            int count = targetStmt.executeUpdate(sql);
            log(LoggingLevel.DELETE, "replace", sql + '\n' + count + " lignes supprimées.");
            /*
             * Fetch the new records from the source table, together with the name of all columns.
             */
            buffer.setLength(0);
            buffer.append("SELECT * FROM \"").append(table).append('"');
            if (condition != null) {
                buffer.append(" WHERE ").append(condition);
            }
            sql = buffer.toString();
            final ResultSet          sources = sourceStmt.executeQuery(sql);
            final ResultSetMetaData metadata = sources.getMetaData();
            final String[]           columns = new String[metadata.getColumnCount()];
            for (int i=0; i<columns.length;) {
                columns[i] = metadata.getColumnName(++i);
            }
            log(LoggingLevel.SELECT, "replace", sql);
            /*
             * Copies the records in the target table.
             */
            buffer.setLength(0);
            buffer.append("INSERT INTO \"").append(table).append("\" (");
            for (int i=0; i<columns.length; i++) {
                if (i != 0) {
                    buffer.append(',');
                }
                buffer.append('"').append(columns[i]).append('"');
            }
            buffer.append(") VALUES (");
            final int valuesStart = buffer.length();
            while (sources.next()) {
                buffer.setLength(valuesStart);
                for (int i=0; i<columns.length;) {
                    if (i != 0) {
                        buffer.append(',');
                    }
                    buffer.append('\'').append(sources.getString(++i)).append('\'');
                }
                buffer.append(')');
                sql = buffer.toString();
                count = targetStmt.executeUpdate(sql);
                if (count != 1) {
                    break;
                }
                log(LoggingLevel.INSERT, "replace", sql);
            }
            sources.close();
            success = true;
        } finally {
            if (success) {
                target.commit();
            } else {
                target.rollback();
            }
        }
        sourceStmt.close();
        targetStmt.close();
        if (!success) {
            throw new SQLException("Certains enregistrements n'ont pas pu être ajoutés.");
        }
    }

    /**
     * Closes the database connections.
     */
    private void close() throws SQLException, IOException {
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
     * Synchronizes the content of the given table.
     */
    public static void main(String[] args) throws IOException, SQLException {
        final Arguments arguments = new Arguments(args);
        final String table     = arguments.getRequiredString("-table");
        final String condition = arguments.getOptionalString("-condition");
        args = arguments.getRemainingArguments(0);
        final Synchronizer synchronizer = new Synchronizer();
        synchronizer.replace(table, condition);
        synchronizer.close();
    }
}
