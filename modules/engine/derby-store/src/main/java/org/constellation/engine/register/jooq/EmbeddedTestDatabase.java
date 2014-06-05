package org.constellation.engine.register.jooq;

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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.sis.util.Static;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;

/**
 * Constellation embedded administration database class.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class EmbeddedTestDatabase extends Static {

    /**
     * Logger used for debugging and event notification.
     */
    private static final Logger LOGGER = Logging.getLogger(EmbeddedTestDatabase.class);

    /**
     * Administration database configuration keys.
     */
    private static final String CONFIG_KEY_URL = "admin-db-url";

    /**
     * Administration database {@link DefaultDataSource} instance.
     */
    private static DefaultDataSource DATA_SOURCE;

    /**
     * Obtains a administration database
     * {@link org.constellation.admin.dao.Session} instance.
     *
     * @return a {@link org.constellation.admin.dao.Session} instance
     * @throws SQLException
     *             if a database access error occurs
    

    /**
     * Exposes dataSource (Spring artifact).
     * 
     * @return
     * @throws SQLException
     * @throws IOException 
     */
    public static DataSource getOrCreateDataSource() throws SQLException, IOException {
        if (DATA_SOURCE == null) {
            synchronized (EmbeddedTestDatabase.class) {
                if (DATA_SOURCE == null) {
                    setup();
                }
            }
        }
        return DATA_SOURCE;
    }

    
    /**
     * Sets static connection variables and check if the administration schema
     * named {@code "CstlAdmin"} exists on the current {@link DataSource}.
     * <p />
     * If the schema is missing create it executing the {@code admin-db.sql}
     * resource file.
     *
     * @throws SQLException
     *             if an error occurred while connecting to database or
     *             executing a SQL statement
     * @throws IOException 
     */
    private static void setup() throws SQLException, IOException {
        // Force loading driver because some container like tomcat 7.0.21+
        // disable drivers at startup.
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        String dbFolder = "target/test/admin-db";

        /*
         * Browses Constellation configuration folder to obtain the admin
         * database configuration. The configuration is stored into the
         * admin-db.properties file from .constellation/admin folder. If this
         * file is missing a default configuration file will be created during
         * Constellation startup.
         */
        final Properties config = new Properties();
        // Read or create default configuration.
        config.setProperty(CONFIG_KEY_URL, "jdbc:derby:" + dbFolder);

        // Initialize data source.
        final String dbUrl = config.getProperty(CONFIG_KEY_URL);
        if (dbUrl == null) {
            throw new IllegalStateException("Embedded database configuration property \"" + CONFIG_KEY_URL
                    + "\" is missing.");
        }
        DATA_SOURCE = new DefaultDataSource(dbUrl.replace('\\', '/') + ";create=true;");

        try (Connection conn = DATA_SOURCE.getConnection()) {

            DerbySqlScriptRunner derbySqlScriptRunner = new DerbySqlScriptRunner(conn);
            derbySqlScriptRunner.run(EmbeddedTestDatabase.class
                    .getResourceAsStream("org/constellation/sql/v1/create-admin-db.sql"));
        }
    }

    public static void clear() {
        if (DATA_SOURCE != null) {
            DATA_SOURCE.shutdown();
        }
        DATA_SOURCE = null;
    }
}
