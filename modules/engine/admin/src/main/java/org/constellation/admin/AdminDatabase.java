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
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.UserRecord;
import org.constellation.util.DatabaseUtilities;
import org.geotoolkit.internal.sql.DefaultDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
     * Logger used for debugging and event notification.
     */
    private static final Logger LOGGER = Logging.getLogger(AdminDatabase.class);

    /**
     * Administration database configuration keys.
     */
    private static final String CONFIG_KEY_PASSWORD = "admin-db-password";
    private static final String CONFIG_KEY_URL      = "admin-db-url";
    private static final String CONFIG_KEY_USERNAME = "admin-db-username";

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
     *
     * TODO: implement real cache system with a maximum size to reduce memory impact
     */
    private static final Map<String, UserRecord> USER_CACHE = new ConcurrentHashMap<>();


    /**
     * Obtains a administration database {@link AdminSession} instance.
     *
     * @return a {@link AdminSession} instance
     * @throws SQLException if a database access error occurs
     */
    public static AdminSession createSession() throws SQLException {
        if (DATA_SOURCE == null) {
            synchronized(AdminDatabase.class) {
                if (DATA_SOURCE == null) {
                    setup();
                }
            }
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
     * TODO: implement multiple dialects support (only derby is supported actually)
     *
     * @throws SQLException if an error occurred while connecting to database or executing a SQL statement
     */
    private static void setup() throws SQLException {
        // Force loading driver because some container like tomcat 7.0.21+ disable drivers at startup.
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        /*
         * Browses Constellation configuration folder to obtain the admin database
         * configuration. The configuration is stored into the admin-db.properties
         * file from .constellation/admin folder. If this file is missing a default
         * configuration file will be created during Constellation startup.
         */
        final Properties config = new Properties();
        try {
            final File configDirectory = ConfigDirectory.getAdminConfigDirectory();
            final File configFile      = new File(configDirectory, "admin-db.properties");

            // Read or create default configuration.
            if (configFile.exists()) {
                config.load(new FileInputStream(configFile));
            } else {
                config.setProperty(CONFIG_KEY_URL, "jdbc:derby:" + configDirectory.getPath() + "/admin-db");
                config.store(new FileOutputStream(configFile), "Auto-generated at first Constellation startup.");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Unexpected exception while reading/writing administration database property file.", ex);
        }

        // Set connection variables.
        DATA_SOURCE = new DefaultDataSource(config.getProperty(CONFIG_KEY_URL).replace('\\','/') + ";create=true;");
        USERNAME    = config.getProperty(CONFIG_KEY_USERNAME);
        PASSWORD    = config.getProperty(CONFIG_KEY_PASSWORD);

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
