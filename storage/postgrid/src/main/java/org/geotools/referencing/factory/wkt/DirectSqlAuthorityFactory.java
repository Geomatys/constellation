/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
package org.geotools.referencing.factory.wkt;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.geotools.factory.Hints;

import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;

import org.geotools.io.TableWriter;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.factory.DirectAuthorityFactory;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * Abstract class for authority factories backed by a connection to a SQL database.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo EPSG authority factory should extends this class.
 */
public abstract class DirectSqlAuthorityFactory extends DirectAuthorityFactory {
    /**
     * Connection to the database, or {@code null} if none.
     */
    private Connection connection;

    /**
     * Creates a factory fetching CRS in the standard {@value #CRS_TABLE} table.
     *
     * @param hints The hints, or {@code null} if none.
     * @param connection The connection to the database.
     */
    public DirectSqlAuthorityFactory(final Hints hints, final Connection connection) {
        super(hints, NORMAL_PRIORITY);
        this.connection = connection;
    }

    /**
     * Returns the connection to the database.
     *
     * @throws SQLException if the connection can not be etablished.
     */
    protected Connection getConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException(Errors.format(ErrorKeys.DISPOSED_FACTORY));
        }
        return connection;
    }

    /**
     * Returns a description of the underlying backing store.
     */
    @Override
    public String getBackingStoreDescription() throws FactoryException {
        final Citation   authority = getAuthority();
        final TableWriter    table = new TableWriter(null, " ");
        final Vocabulary resources = Vocabulary.getResources(null);
        CharSequence cs;
        if ((cs=authority.getEdition()) != null) {
            final String identifier = Citations.getIdentifier(authority);
            table.write(resources.getString(VocabularyKeys.VERSION_OF_$1, identifier));
            table.write(':');
            table.nextColumn();
            table.write(cs.toString());
            table.nextLine();
        }
        try {
            String s;
            final DatabaseMetaData metadata = getConnection().getMetaData();
            if ((s=metadata.getDatabaseProductName()) != null) {
                table.write(resources.getLabel(VocabularyKeys.DATABASE_ENGINE));
                table.nextColumn();
                table.write(s);
                if ((s=metadata.getDatabaseProductVersion()) != null) {
                    table.write(' ');
                    table.write(resources.getString(VocabularyKeys.VERSION_$1, s));
                }
                table.nextLine();
            }
            if ((s=metadata.getURL()) != null) {
                table.write(resources.getLabel(VocabularyKeys.DATABASE_URL));
                table.nextColumn();
                table.write(s);
                table.nextLine();
            }
        } catch (SQLException exception) {
            throw databaseFailure(null, null, exception);
        }
        return table.toString();
    }

    /**
     * Releases resources immediately instead of waiting for the garbage collector.
     *
     * @throws FactoryException if an error occured while disposing the factory.
     */
    @Override
    public synchronized void dispose() throws FactoryException {
        connection = null; // Don't close the connection, since we may not own it.
        super.dispose();
    }

    /**
     * Wraps a {@link SQLException} into a {@link FactoryException}.
     *
     * @param  type The type of the object being created, or {@code null} if unknown.
     * @param  code The code of the object being created, or {@code null} if unknown.
     * @param  SQLException The SQL exception that occured while querying the database.
     * @return A factory exception wrapping the SQL exception.
     */
    protected FactoryException databaseFailure(final Class<?> type, final String code,
                                               final SQLException exception)
    {
        String message = exception.getLocalizedMessage();
        if (code != null) {
            String typeName;
            if (type != null) {
                typeName = type.getSimpleName();
            } else {
                typeName = Vocabulary.format(VocabularyKeys.UNKNOW);
            }
            message = Errors.format(ErrorKeys.DATABASE_FAILURE_$2, typeName, code) + ": " + message;
        }
        return new FactoryException(message, exception);
    }
}
