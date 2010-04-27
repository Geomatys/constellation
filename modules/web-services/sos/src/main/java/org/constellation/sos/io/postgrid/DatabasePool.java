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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.constellation.catalog.Database;
import org.constellation.generic.database.BDD;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.postgresql.ds.PGSimpleDataSource;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DatabasePool {

    private static final Map<BDD, Database> DATABASE_MAP = new HashMap<BDD, Database>();

    private DatabasePool(){}

    public static Database getDatabase(BDD bdd) throws IOException {
        Database db = DATABASE_MAP.get(bdd);
        if (db == null) {
            db = createDatabase(bdd);
            DATABASE_MAP.put(bdd, db);
        }
        return db;
    }

    public static Connection getDatabaseConnection(BDD bdd) throws SQLException {
        final Database db =  DATABASE_MAP.get(bdd);
        if (db != null) {
            return db.getConnection();
        }
        return null;
    }

    private static Database createDatabase(BDD bdd) throws IOException {
        final DataSource dataSource;
        if (bdd.getClassName() != null && bdd.getClassName().equals("org.postgresql.Driver")) {
            final PGSimpleDataSource pgDataSource = new PGSimpleDataSource();
            pgDataSource.setServerName(bdd.getHostName());
            pgDataSource.setPortNumber(bdd.getPortNumber());
            pgDataSource.setDatabaseName(bdd.getDatabaseName());
            pgDataSource.setUser(bdd.getUser());
            pgDataSource.setPassword(bdd.getPassword());
            dataSource = pgDataSource;
        } else {
            dataSource = new DefaultDataSource(bdd.getConnectURL());
        }

        return new Database(dataSource);
    }
}
