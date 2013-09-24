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

import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigDirectory;
import org.geotoolkit.internal.SetupService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Constellation administration database setup.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class AdminDatabaseSetup implements SetupService {

    /**
     * Logger used for debugging and event notification.
     */
    private static final Logger LOGGER = Logging.getLogger(AdminDatabaseSetup.class);

    /**
     * Administration database configuration keys.
     */
    private static final String CONFIG_KEY_PASSWORD = "admin-db-password";
    private static final String CONFIG_KEY_URL      = "admin-db-url";
    private static final String CONFIG_KEY_USERNAME = "admin-db-username";

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final Properties properties, boolean b) {
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
        try {
            final File configDirectory = ConfigDirectory.getAdminConfigDirectory();
            final File configFile      = new File(configDirectory, "admin-db.properties");

            // Read or create default configuration.
            final Properties config = new Properties();
            if (configFile.exists()) {
                config.load(new FileInputStream(configFile));
            } else {
                config.setProperty(CONFIG_KEY_URL, "jdbc:derby:" + configDirectory.getPath() + "/admin-db");
                config.store(new FileOutputStream(configFile), "Auto-generated at first Constellation startup.");
            }

            // Configure database.
            AdminDatabase.configure(config.getProperty(CONFIG_KEY_URL),
                                    config.getProperty(CONFIG_KEY_USERNAME),
                                    config.getProperty(CONFIG_KEY_PASSWORD));

        } catch (IOException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Administration database setup has failed.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        // do nothing
    }
}
