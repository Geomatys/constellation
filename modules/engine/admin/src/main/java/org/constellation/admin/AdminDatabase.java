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

package org.constellation.admin;

import org.apache.sis.util.Static;
import org.constellation.util.DatabaseUtilities;
import org.geotoolkit.internal.sql.DefaultDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * Constellation administration database utility class.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class AdminDatabase extends Static {

    /**
     * Administration database {@link DefaultDataSource} instance.
     */
    private static DefaultDataSource DATA_SOURCE;

    /**
     * Administration database username.
     */
    private static String USERNAME;

    /**
     * Administration database password.
     */
    private static String PASSWORD;

    /**
     * User cache for improved authentication performance.
     */
    private static final Map<String, UserRecord> USER_CACHE = new ConcurrentHashMap<>();


    /**
     * Obtains a administration database {@link AdminSession} instance.
     *
     * @return a {@link AdminSession} instance
     * @throws  SQLException if a database access error occurs
     */
    public static AdminSession createSession() throws SQLException {
        if (DATA_SOURCE == null) {
            throw new IllegalStateException("Constellation administration database not configured.");
        }
        return new AdminSession(DATA_SOURCE.getConnection(USERNAME, PASSWORD), USER_CACHE);
    }

    /**
     * Tries to find a {@link UserRecord} with the specified {@code login} from cache.
     *
     * @param login the user login
     * @return a {@link UserRecord} instance or {@code null}
     */
    public static UserRecord getCachedUser(final String login) {
        return USER_CACHE.get(login);
    }

    /**
     * Sets static connection variables and check if the administration schema named
     * {@code "CstlAdmin"} exists on the current {@link DataSource}.
     * <p />
     * If the schema is missing create it executing the {@code admin-db.sql} resource file.
     *
     * @throws SQLException if an error occurred while connecting to database or executing a SQL statement
     */
    static synchronized void configure(final String dbUrl, final String username, final String password) throws SQLException {
        ensureNonNull("dbUrl", dbUrl);

        // Update connection variables.
        DATA_SOURCE = new DefaultDataSource(dbUrl.replace('\\','/') + ";create=true;");
        USERNAME    = username;
        PASSWORD    = password;

        // Establish connection and create schema if does not exist.
        AdminSession session = null;
        try {
            session = createSession();
            if (!DatabaseUtilities.schemaExists(session.getConnection(), "CstlAdmin")) {
                // Create schema.
                DatabaseUtilities.runSqlScript(session.getConnection(), "org/constellation/sql/v1/create-admin-db.sql");

                // Create default admin user.
                session.writeUser("admin", "admin", "Default Constellation Administrator", Arrays.asList("cstl-admin"));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unexpected error occurred while trying to create admin database schema.", ex);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
