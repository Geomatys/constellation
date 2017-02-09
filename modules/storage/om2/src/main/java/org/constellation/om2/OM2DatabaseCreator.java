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
package org.constellation.om2;

import org.apache.sis.util.logging.Logging;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.PostgisInstaller;
import org.geotoolkit.internal.sql.ScriptRunner;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.util.FileUtilities;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.9
 */
public class OM2DatabaseCreator {
    
    private static final Logger LOGGER = Logging.getLogger("org.mdweb.sql");
    
    /**
     * Fill a new PostgreSQL database with the O&M model.
     *
     * @param dataSource A postgreSQL dataSource.
     * 
     * @throws SQLException if an error occurs while filling the database.
     * @throws IllegalArgumentException if the dataSource is null.
     */
    public static void createObservationDatabase(final DataSource dataSource, final boolean isPostgres, final File postgisInstall, String schemaPrefix) throws SQLException, IOException {
        if (dataSource == null) {
            throw new IllegalArgumentException("The DataSource is null");
        }
        
        if (schemaPrefix == null) {
            schemaPrefix = "";
        }
        
        try(final Connection con  = dataSource.getConnection()) {
            if (isPostgres && postgisInstall != null) {
                final PostgisInstaller pgInstaller = new PostgisInstaller(con);
                // not needed in pg 9.1
                try {
                    pgInstaller.run("CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql' HANDLER plpgsql_call_handler VALIDATOR plpgsql_validator;");
                } catch (SQLException ex) {
                    LOGGER.log(Level.FINER, "unable to create plpgsql lanquage", ex);
                }
                pgInstaller.run("CREATE SCHEMA postgis;");
                pgInstaller.run(postgisInstall);
            }
            if (!versionTablePresent(con, schemaPrefix)) {
                try (Statement stmt = con.createStatement()) {
                    stmt.executeUpdate("CREATE TABLE \"public\".\"version\" (\"number\"   character varying(10) NOT NULL);");
                    stmt.executeUpdate("INSERT INTO \"public\".\"version\" VALUES ('1.0.3');");
                    stmt.executeUpdate("ALTER TABLE \"version\" ADD CONSTRAINT version_pk PRIMARY KEY (\"number\");");
                }
            }

            final ScriptRunner sr = new ScriptRunner(con);
            if (isPostgres) {
                execute("org/constellation/om2/structure_observations_pg.sql", sr, schemaPrefix);
            } else {
                execute("org/constellation/om2/structure_observations.sql", sr, schemaPrefix);
            }
            LOGGER.info("O&M 2 database created");

            sr.close(false);
        }
    }
    
    public static boolean structurePresent(final DataSource source, final String schemaPrefix) {
        if (source != null) {
            try (final Connection con = source.getConnection();
                 final Statement stmt = con.createStatement()) {

                boolean exist = versionTablePresent(con, schemaPrefix);
                if (!exist) {
                    return false;
                }

                try(final ResultSet resultObs = stmt.executeQuery("SELECT * FROM \"" + schemaPrefix + "om\".\"observed_properties\"")) {
                    resultObs.next();
                }
                return true;
            } catch(SQLException ex) {
                LOGGER.log(Level.FINER, "missing table in OM database", ex);
            }
        }
        return false;
    }
    
    public static boolean versionTablePresent(final Connection con, final String schemaPrefix) {
        if (con != null) {
            try (final Statement stmt = con.createStatement();
                 final ResultSet result = stmt.executeQuery("SELECT * FROM \"version\"")) {
                result.next();
                return true;
                
            } catch(SQLException ex) {
                LOGGER.log(Level.FINER, "missing table in OM database", ex);
            }
        }
        return false;
    }

    public static boolean validConnection(final DataSource source) {
        try (final Connection con = source.getConnection()) {
            return true;
        } catch (SQLException ex) {
            LOGGER.log(Level.FINER, "unable to connect", ex);
        }
        return false;
    }
    
    /**
     * Execute the SQL script pointed by the specified path.
     *
     * @param path A path in the resource files to a SQL script.
     * @param runner A SQL script runner connected to a database.
     */
    private static void execute(final String path, final ScriptRunner runner, final String schemaPrefix) {
        try {
            String sql = FileUtilities.getStringFromStream(Util.getResourceAsStream(path));
            sql = sql.replace("$SCHEMA", schemaPrefix);
            runner.run(sql);
         } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IO exception while executing SQL script", ex);
        } catch (SQLException ex) {
            LOGGER.severe("SQLException creating statement: " + runner.getCurrentPosition() + " in " + path + " file.\n" + ex.getMessage());
        }
    }
}
