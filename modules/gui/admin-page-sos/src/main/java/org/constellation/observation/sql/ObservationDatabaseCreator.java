
/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.observation.sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.PostgisInstaller;
import org.geotoolkit.internal.sql.ScriptRunner;
import org.geotoolkit.util.logging.Logging;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.7
 */
public class ObservationDatabaseCreator {
    
    private static final Logger LOGGER = Logging.getLogger("org.mdweb.sql");
    
    /**
     * Fill a new PostgreSQL database with the O&M model.
     *
     * @param dataSource A postgreSQL dataSource.
     * 
     * @throws SQLException if an error occurs while filling the database.
     * @throws IllegalArgumentException if the dataSource is null.
     */
    public static void createObservationDatabase(final DataSource dataSource, final File postgisInstall) throws SQLException, IOException {
        if (dataSource == null) {
            throw new IllegalArgumentException("The DataSource is null");
        }
        
        final Connection con  = dataSource.getConnection();
        try {
            final PostgisInstaller pgInstaller = new PostgisInstaller(con);
            // not needed in pg 9.1
            try {
                pgInstaller.run("CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql' HANDLER plpgsql_call_handler VALIDATOR plpgsql_validator;");
            } catch (SQLException ex) {
                LOGGER.log(Level.FINER, "unable to create plpgsql lanquage", ex);
            }
            pgInstaller.run("CREATE SCHEMA postgis;");
            pgInstaller.run(postgisInstall);
            final ScriptRunner sr = new ScriptRunner(con);
            execute("org/constellation/observation/structure_observations.sql", sr);
            LOGGER.info("O&M database created");

            sr.close(false);
        } finally {
            con.close();
        }
    }
    
    public static boolean structurePresent(final DataSource source) {
        if (source != null) {
            Connection con = null;
            try  {
                con = source.getConnection();
                final Statement stmt = con.createStatement();
                ResultSet result = stmt.executeQuery("SELECT * FROM \"version\"");
                boolean exist = result.next();
                result.close();
                if (!exist) {
                    stmt.close();
                    return false;
                }
                
                result = stmt.executeQuery("SELECT * FROM \"observation\".\"phenomenons\"");
                exist = result.next();
                result.close();
                stmt.close();
                return exist;
            } catch(SQLException ex) {
                LOGGER.log(Level.FINER, "missing table in OM database", ex);
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException ex) {
                        LOGGER.log(Level.WARNING, "unable to close connection", ex);
                    }
                }
            }
        }
        return false;
    }
    
    public static boolean validConnection(final DataSource source) {
        try {
            final Connection con = source.getConnection();
            con.close();
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
    private static void execute(final String path, final ScriptRunner runner) {
        try {
            runner.run(Util.getResourceAsStream(path));
         } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IO exception while executing SQL script", ex);
        } catch (SQLException ex) {
            LOGGER.severe("SQLException creating statement: " + runner.getCurrentPosition() + " in " + path + " file.\n" + ex.getMessage());
        }
    }
}
