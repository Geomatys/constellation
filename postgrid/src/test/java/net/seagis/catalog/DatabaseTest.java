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
package net.seagis.catalog;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
     * {@code true} if we should keep the database connection opened.
     */
    static boolean keepOpen = false;

    /**
     * For JUnit 3 compatibility.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openDatabase();
    }

    /**
     * For JUnit 3 compatibility.
     */
    @Override
    protected void tearDown() throws Exception {
        closeDatabase();
        super.tearDown();
    }

    /**
     * Opens the connection to the database.
     *
     * @throws IOException if an I/O error occured.
     */
    @BeforeClass
    public static void openDatabase() throws IOException {
        if (database == null) {
            database = new Database();
        }
    }

    /**
     * Closes the connection to the database.
     *
     * @throws IOException  if an I/O error occured.
     * @throws SQLException if an SQL error occured.
     */
    @AfterClass
    public static void closeDatabase() throws IOException, SQLException {
        if (database != null && !keepOpen) {
            database.close();
            database = null;
        }
    }

    /**
     * Puts this class as the first element in a test suite in order to keep the database
     * connection open for the full suite. This is an optimization in order to avoid to
     * open and close the connection for every test class.
     */
    public static final class Open extends DatabaseTest {
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            openOnce();
        }

        @BeforeClass
        public static void openOnce() {
            keepOpen = true;
        }
    }

    /**
     * Puts this class as the last element in a test suite after {@link DatabaseTest#Open}.
     */
    public static final class Close extends DatabaseTest {
        @Override
        protected void tearDown() throws Exception {
            closeOnce();
            super.tearDown();
        }

        @AfterClass
        public static void closeOnce() {
            keepOpen = false;
        }
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
     * Tries to executes the specified query statement and to read one row.
     *
     * @param  query the statement to test.
     * @throws SQLException if an query error occured.
     */
    protected static void tryStatement(final String query) throws SQLException {
        final Statement s = database.getConnection().createStatement();
        final ResultSet r = s.executeQuery(query);
        if (r.next()) {
            final ResultSetMetaData metadata = r.getMetaData();
            final int num = metadata.getColumnCount();
            for (int i=1; i<=num; i++) {
                final String value = r.getString(i);
                if (metadata.isNullable(i) == ResultSetMetaData.columnNoNulls) {
                    assertNotNull(value);
                }
            }
        }
        r.close();
        s.close();
    }
}
