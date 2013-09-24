/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.util;

import org.apache.sis.util.Static;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * Utility class for database operations/management.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class DatabaseUtilities extends Static {

    /**
     * Checks if a schema exists on a database.
     *
     * @param connect    the database {@link Connection} instance
     * @param schemaName the schema name to find
     * @return {@code true} if the schema exists, otherwise {@code false}
     * @throws SQLException if an error occurred while executing a SQL statement
     */
    public static boolean schemaExists(final Connection connect, final String schemaName) throws SQLException {
        ensureNonNull("connect",    connect);
        ensureNonNull("schemaName", schemaName);

        final ResultSet schemas = connect.getMetaData().getSchemas();
        while (schemas.next()) {
            if (schemaName.equals(schemas.getString(1))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Runs an {@code .sql} script file.
     *
     * @param connect   the database {@link Connection} instance
     * @param classPath the resource file classpath
     * @throws java.io.IOException if an error occurred while reading the input
     * @throws java.sql.SQLException if an error occurred while executing a SQL statement
     */
    public static void runSqlScript(final Connection connect, final String classPath) throws IOException, SQLException {
        ensureNonNull("connect",    connect);
        ensureNonNull("schemaName", classPath);

        final ClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
        final InputStream sqlStream = loader.getResourceAsStream(classPath);
        new DerbySqlScriptRunner(connect).run(sqlStream);
    }
}
