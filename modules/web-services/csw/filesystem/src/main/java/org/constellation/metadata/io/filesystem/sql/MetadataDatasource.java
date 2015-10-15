/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.metadata.io.filesystem.sql;

import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MetadataDatasource {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.metadata.io.filesystem.sql");

    /**
     * CSW database {@link DefaultDataSource} instance.
     */
    private static final Map<String, DataSource> DATA_SOURCE = new HashMap<>();

    /**
     * Obtains a csw database {@link org.constellation.metadata.io.filesystem.sql.Session} instance.
     *
     * @param serviceID
     * @return a {@link org.constellation.admin.dao.Session} instance
     * @throws  SQLException if a database access error occurs
     */
    public static Session createSession(final String serviceID) throws SQLException {
        final DataSource source = getOrCreateDataSource(serviceID);
        return new Session(source.getConnection());
    }

    private static DataSource getOrCreateDataSource(final String serviceID) throws SQLException {
        final DataSource source = DATA_SOURCE.get(serviceID);
        synchronized(MetadataDatasource.class) {
            if (source == null) {
                return setup(serviceID);
            }
        }
        return source;
    }

    /**
     * Sets static connection variables and check if the csw schema named
     * {@code "csw"} exists on the current {@link DataSource}.
     * <p />
     * If the schema is missing create it executing the {@code create-csw-db.sql} resource file.
     *
     * @throws SQLException if an error occurred while connecting to database or executing a SQL statement
     */
    private static DataSource setup(final String serviceID) throws SQLException {
        // Force loading driver because some container like tomcat 7.0.21+ disable drivers at startup.
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }


        final File configDirectory = ConfigDirectory.getInstanceDirectory("CSW", serviceID);

        // Read or create default configuration.
        if (!configDirectory.exists()) {
            configDirectory.mkdir();
        }

        // Initialize data source.
        final String dbUrl = "jdbc:derby:" + configDirectory.getPath() + "/csw-db;create=true;";
        LOGGER.log(Level.INFO, "connection to {0}", dbUrl);
        final DataSource source = new DefaultDataSource(dbUrl);

        // Establish connection and create schema if does not exist.
        Connection con = null;
        try {
            con = source.getConnection();

            if (!schemaExists(con, "csw")) {
                // Load database schema SQL stream.
                final InputStream stream = Util.getResourceAsStream("org/constellation/csw/filesystem/sql/v1/create-csw-db.sql");

                // Create schema.
                final DerbySqlScriptRunner runner = new DerbySqlScriptRunner(con);
                runner.run(stream);
                runner.close(false);
            }
        } catch (IOException unexpected) {
            throw new IllegalStateException("Unexpected error occurred while trying to create csw database schema.", unexpected);
        } finally {
            if (con != null) {
                con.close();
            }
        }
        DATA_SOURCE.put(serviceID, source);
        return source;
    }

    private static boolean schemaExists(final Connection connect, final String schemaName) throws SQLException {
        ensureNonNull("schemaName", schemaName);
        final ResultSet schemas = connect.getMetaData().getSchemas();
        while (schemas.next()) {
            if (schemaName.equals(schemas.getString(1))) {
                return true;
            }
        }
        return false;
    }

    public static void close(final String serviceID) {
        final DefaultDataSource source = (DefaultDataSource) DATA_SOURCE.get(serviceID);
        if (source != null) {
            source.shutdown();
            DATA_SOURCE.remove(serviceID);
        }
    }

}
