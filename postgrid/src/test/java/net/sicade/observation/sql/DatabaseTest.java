/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.sicade.observation.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.*;
import junit.framework.TestCase;


/**
 * The base class for tests that require a {@link Database}.
 *
 * @version $Id$
 * @author Marton Desruisseaux
 */
public class DatabaseTest extends TestCase {
    /**
     * The connection to the database.
     */
    protected static Database database;

    /**
     * For JUnit 3 compatibility. We rely on the shutdown hook for tear down.
     */
    @Override
    protected void setUp() throws Exception {
        if (database == null) {
            openDatabase();
        }
    }

    /**
     * Opens the connection to the database.
     *
     * @throws IOException  if an I/O error occured.
     */
    @BeforeClass
    public static void openDatabase() throws IOException {
        database = new Database();
    }

    /**
     * Closes the connection to the database.
     *
     * @throws IOException  if an I/O error occured.
     * @throws SQLException if an SQL error occured.
     */
    @AfterClass
    public static void closeDatabase() throws IOException, SQLException {
        database.close();
    }

    /**
     * Tests the database connection.
     *
     * @throws SQLException if an SQL error occured.
     */
    @Test
    public void testConnection() throws SQLException {
        final Connection connection = database.getConnection();
        assertNotNull(connection);
        assertFalse(connection.isClosed());
    }

    /**
     * Tries to executes the specified query statement.
     *
     * @param  query the statement to test.
     * @throws SQLException if an query error occured.
     */
    protected static void tryStatement(final String query) throws SQLException {
        final Statement s = database.getConnection().createStatement();
        final ResultSet r = s.executeQuery(query);
        assertTrue(r.next());
        r.close();
        s.close();
    }

    /**
     * Tries the {@link Query#selectAll} method on the specified table.
     */
    protected static void trySelectAll(final Table table) throws SQLException {
        final String query = table.query.select(QueryType.SELECT);
        assertNotNull(query);
        tryStatement(query);
    }
}
