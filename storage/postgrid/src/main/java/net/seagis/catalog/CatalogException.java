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
package net.seagis.catalog;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import net.seagis.resources.i18n.ResourceKeys;
import net.seagis.resources.i18n.Resources;
import org.geotools.resources.Classes;


/**
 * Base class for exceptions that may occur while querying the catalog.
 *
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class CatalogException extends Exception {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 3838293108990270182L;

    /**
     * The table name where a problem occured, or {@code null} if unknown.
     */
    private String table;

    /**
     * The column name where a problem occured, or {@code null} if unknown.
     */
    private String column;

    /**
     * The primary key for the record where a problem occured, or {@code null} if unknown.
     */
    private String key;

    /**
     * Creates an exception with no cause and no details message.
     */
    public CatalogException() {
        super();
    }

    /**
     * Creates an exception with the specified details message.
     *
     * @param message The detail message.
     */
    public CatalogException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with the specified cause and no details message.
     *
     * @param cause The cause for this exception.
     */
    public CatalogException(final Exception cause) {
        super(cause);
    }

    /**
     * Creates an exception with the specified details message and cause.
     *
     * @param message The detail message.
     * @param cause The cause for this exception.
     */
    public CatalogException(final String message, final Exception cause) {
        super(message, cause);
    }

    /**
     * Returns {@code true} if {@link #setMetadata} has been invoked with non-null values.
     */
    final boolean isMetadataInitialized() {
        return table!=null || column!=null || key!=null;
    }

    /**
     * Sets the table and column names from the {@linkplain ResultSetMetaData result set metadata}.
     * <strong>Note that the result set will be closed</strong>, because this exception is always
     * thrown when an error occured while reading this result set.
     *
     * @param table   The table that produced the result set, or {@code null} if unknown.
     * @param results The result set in which a problem occured, or {@code null} if none.
     * @param column  The column index where a problem occured (number starts at 1), or {@code 0} if unknow.
     * @param key     The key value for the record where a problem occured, or {@code null} if none.
     * @throws SQLException if the metadata can't be read from the result set.
     */
    public void setMetadata(final Table table, final ResultSet results, final int column, final String key)
            throws SQLException
    {
        boolean noTable=true, noColumn=true;
        if (results != null && column != 0) {
            final ResultSetMetaData metadata = results.getMetaData();
            if (metadata != null) {
                this.table  = metadata.getTableName (column);
                this.column = metadata.getColumnName(column);
                noTable  = (this.table  == null) || (this.table  = this.table .trim()).length() == 0;
                noColumn = (this.column == null) || (this.column = this.column.trim()).length() == 0;
            }
            results.close();
        }
        /*
         * We tried to use the database metadata in priority,  on the assumption that they
         * are closer to the SQL statement really executed (we could have a bug in the way
         * we created our SQL statement). But some JDBC drivers don't provide information.
         * In the later case, we fallback on the information found in our Column objects.
         */
        if ((noTable || noColumn) && table != null) {
            final Column c = table.getColumn(column);
            if (c != null) {
                if (noTable)  this.table  = c.table;
                if (noColumn) this.column = c.name;
            }
        }
        this.key = key;
    }

    /**
     * Clears the column name. Invoked when this name is not reliable.
     */
    final void clearColumnName() {
        column = null;
    }

    /**
     * Returns the table name where a problem occured, or {@code null} if unknown.
     */
    public String getTableName() {
        return table;
    }

    /**
     * Returns the column name where a problem occured, or {@code null} if unknown.
     */
    public String getColumnName() {
        return column;
    }

    /**
     * Returns the primary key for the record where a problem occured, or {@code null} if unknown.
     */
    public String getPrimaryKey() {
        return key;
    }

    /**
     * Returns a concatenation of the {@linkplain #getMessage details message} and the table
     * and column name where the error occured.
     */
    @Override
    public String getLocalizedMessage() {
        String message = super.getLocalizedMessage();
        if (message == null) {
            final Throwable cause = getCause();
            if (cause != null) {
                message = cause.getLocalizedMessage();
                if (message == null) {
                    message = Classes.getShortClassName(cause);
                }
            }
        }
        final String table  = getTableName();
        final String column = getColumnName();
        final String key    = getPrimaryKey();
        if (table != null) {
            final int localKey;
            final String[] args;
            if (column != null) {
                if (key != null) {
                    localKey = ResourceKeys.ERROR_CATALOG_$3;
                    args = new String[] {table, column, key};
                } else {
                    localKey = ResourceKeys.ERROR_CATALOG_NOKEY_$2;
                    args = new String[] {table, column};
                }
            } else {
                if (key != null) {
                    localKey = ResourceKeys.ERROR_CATALOG_$2;
                    args = new String[] {table, key};
                } else {
                    localKey = ResourceKeys.ERROR_CATALOG_NOKEY_$1;
                    args = new String[] {table};
                }
            }
            final String explain = Resources.format(localKey, args);
            if (message != null) {
                message = explain + ' ' + message;
            }
        }
        return message;
    }
}
