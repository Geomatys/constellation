/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.sos.io.postgrid;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.geotoolkit.internal.sql.table.Database;
import org.constellation.generic.database.BDD;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.geotoolkit.jdbc.WrappedDataSource;
import org.geotoolkit.util.logging.Logging;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class DatabasePool {

    /**
     * A map of datasource informations / postgrid {@link Database}.
     */
    private static final Map<BDD, Database> DATABASE_MAP = new HashMap<BDD, Database>();

    private static final Logger LOGGER = Logging.getLogger("org.constellation.sos.io.postgrid");

    private DatabasePool(){}

    /**
     * Return a {@link Database} from the pool or create a new one if its not present in the pool.
     *
     * @param bdd Some database informations.
     * @return A postgrid {@link Database}.
     */
    public static Database getDatabase(final BDD bdd) {
        Database db = DATABASE_MAP.get(bdd);
        if (db == null) {
            db = createDatabase(bdd);
            DATABASE_MAP.put(bdd, db);
        }
        return db;
    }

    /**
     * Return a connection from a postgrid {@link Database} if there is one already pooled.
     * otherwise its return {@code null}.
     *
     * @param bdd Some database informations.
     *
     * @return A connection to a postgrid {@link Database} or {@code null}.
     * @throws SQLException
     */
    public static Connection getDatabaseConnection(final BDD bdd) throws SQLException {
        final Database db =  DATABASE_MAP.get(bdd);
        if (db != null) {
            return db.getDataSource(true).getConnection();
        }
        return null;
    }

    /**
     * Build a new postgrid {@link Database}.
     * first it try to read informations from JNDI context, in this case the parameter bdd will be ignored.
     * if there is no JNDI context or if the ressource "java:/comp/env/jdbc/observation" does not exist,
     * it will use the database informations specified to instanciate a postgrid {@link Database}.
     *
     * @param bdd Some database informations.
     * @return A new postgrid {@link Database}
     */
    private static Database createDatabase(final BDD bdd) {

    final DataSource dataSource;
    DataSource ds =null;
        try {
            final InitialContext cxt = new InitialContext();
            if (cxt == null) {
                LOGGER.warning("no initialContext found!");
            } else {
                ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/observation");
                if (ds == null) {
                    LOGGER.warning("Data source not found for O&M in JNDI context!");
                }
            }
        } catch (NamingException ex) {
            LOGGER.log(Level.FINER, "Naming exception while try to get database informations", ex);
        }
        if (ds != null) {
            dataSource = ds;
            LOGGER.info("using tomcat database pool");
            
        } else if (bdd.getClassName() != null && bdd.getClassName().equals("org.postgresql.Driver")) {
            final PGConnectionPoolDataSource pgDataSource = new PGConnectionPoolDataSource();
            pgDataSource.setServerName(bdd.getHostName());
            pgDataSource.setPortNumber(bdd.getPortNumber());
            pgDataSource.setDatabaseName(bdd.getDatabaseName());
            pgDataSource.setUser(bdd.getUser());
            pgDataSource.setPassword(bdd.getPassword());
            dataSource = new WrappedDataSource(pgDataSource);
        } else {
            dataSource = new DefaultDataSource(bdd.getConnectURL());
        }

        return new Database(dataSource, null);
    }
}
