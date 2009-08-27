/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

// Geotoolkit dependencies
import java.util.Map;
import org.geotoolkit.internal.jdbc.DefaultDataSource;


/**
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.4
 */
public final class ResultsDatabase {
    /**
     * Database connection parameters.
     */
    private static final String DB_NAME  = "cite_results";
    private static final String PROTOCOL = "jdbc:postgresql";
    private static final String HOST     = "hyperion.geomatys.com";
    private static final String USER     = "cite";
    private static final String PASS     = "cite";

    /**
     * Insertion requests.
     */
    private static final String INSERT_RESULT  = "INSERT INTO \"Results\" VALUES (?,?,?,?);";
    private static final String INSERT_SERVICE = "INSERT INTO \"Services\" VALUES (?,?);";
    private static final String INSERT_SUITE   = "INSERT INTO \"Suites\" VALUES (?,?,?);";

    /**
     * Count requests.
     */
    private static final String COUNT_SERVICE = "SELECT COUNT(name) FROM \"Services\" "+
                                                        "WHERE name=? AND version=?;";
    private static final String COUNT_SUITE   = "SELECT COUNT(date) FROM \"Suites\" "+
                                                        "WHERE date=?;";

    /**
     * Select requests.
     */
    private static final String SELECT_TESTS_FAILED =
            "SELECT * FROM \"TestsFailed\" WHERE service=? AND version=? AND date=?;";

    /**
     * The connection to the database of Cite Tests results.
     */
    private final Connection connection;

    /**
     * List of existing services, already checked from the database as present.
     */
    private final List<Service> existingServices = new ArrayList<Service>();

    /**
     * List of existing suites, already checked from the database as present.
     */
    private final List<Suite> existingSuites = new ArrayList<Suite>();

    /**
     * Stores the ids for a session, and their number of occurrences.
     */
    private final Map<String,Integer> ids = new HashMap<String,Integer>();

    /**
     * Initialize the connection to the database.
     *
     * @throws SQLException if an exception occurs while trying to connect to the database.
     */
    public ResultsDatabase() throws SQLException {
        final DefaultDataSource ds = new DefaultDataSource(PROTOCOL +"://"+ HOST +"/"+ DB_NAME);
        connection = ds.getConnection(USER, PASS);
    }

    /**
     *
     * @param service
     * @param version
     * @param date
     * @throws SQLException
     */
    public void compareResults(final String service, final String version, final Date date)
                               throws SQLException
    {
        ensureConnectionOpened();

        final PreparedStatement ps = connection.prepareStatement(SELECT_TESTS_FAILED);
        ps.setString(1, service);
        ps.setString(2, version);
        ps.setTimestamp(3, new Timestamp(date.getTime()));

        final ResultSet rs = ps.executeQuery();
    }

    /**
     * Insert a result into the matching table.
     *
     * @param result A result to insert.
     * @param service The service name.
     * @param version The service version.
     * @return
     * @throws SQLException if an error occurs in the insert request.
     */
    public int insertResult(final Result result, final String service, final String version)
                            throws SQLException
    {
        return insertResult(service, version, result.getId(), result.getDirectory(),
                            result.isPassed(), result.getDate());
    }

    /**
     * Insert a result into the matching table.
     *
     * @param service   The service name.
     * @param version   The service version.
     * @param id        The identifier of the test.
     * @param directory The directory where the logs are stored.
     * @param passed    Defined whether the test has passed or not.
     * @param date      The date when the test has been executed.
     * @return
     * @throws SQLException if an error occurs in the insert request.
     */
    public int insertResult(final String service, final String version, final String id,
                            final String directory, final boolean passed, final Date date) throws SQLException
    {
        ensureConnectionOpened();

        // try to add the service.
        addService(service, version);

        // add the test suite.
        addSuite(date, service, version);

        // Verify that the name is not of the test id is not already defined.
        // If already defined, we use the id name concatenated with the index number,
        // and we increment the count of this id into the hashmap.
        final String finalId;
        final Integer numberId = ids.get(id);
        if (numberId == null) {
            ids.put(id, 0);
            finalId = id;
        } else {
            ids.put(id, numberId + 1);
            finalId = id + (numberId + 1);
        }

        final PreparedStatement ps = connection.prepareStatement(INSERT_RESULT);
        ps.setTimestamp(1, new Timestamp(date.getTime()));
        ps.setString(2, finalId);
        ps.setBoolean(3, passed);
        ps.setString(4, directory);

        final int result = ps.executeUpdate();
        ps.close();

        return result;
    }

    /**
     * Close the connection to the database
     *
     * @throws SQLException if an error occurs when closing the connection.
     */
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Add a service if it is not already present into the database.
     *
     * @param service The service name.
     * @param version The service version.
     * @throws SQLException
     */
    private void addService(final String service, final String version) throws SQLException {
        if (!serviceExists(service, version)) {
            insertService(service, version);
        }
    }

    /**
     * Add a suite if it is not already present into the database.
     *
     * @param date    The date of the suite.
     * @param service The service name.
     * @param version The service version.
     * @throws SQLException
     */
    private void addSuite(final Date date, final String service, final String version) throws SQLException {
        if (!suiteExists(date, service, version)) {
            insertSuite(date, service, version);
        }
    }

    /**
     * Throws an {@link SQLException} if the connection to the database is closed.
     * This method should be invoked before trying to perform an action on the
     * database.
     */
    private void ensureConnectionOpened() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("The connection to the database is already closed. " +
                                   "Unable to perform the wished action.");
        }
    }

    /**
     * Inserts a new service in the database.
     *
     * @param service The service name.
     * @param version The service version.
     *
     * @throws SQLException if an error occurs while inserting the new service.
     */
    private void insertService(final String service, final String version) throws SQLException {
        ensureConnectionOpened();

        final PreparedStatement ps = connection.prepareStatement(INSERT_SERVICE);
        ps.setString(1, service);
        ps.setString(2, version);
        ps.execute();
        ps.close();

        existingServices.add(new Service(service, version));
    }

    /**
     * Inserts a new suite in the database.
     *
     * @param date    The date of the suite.
     * @param service The service name.
     * @param version The service version.
     * @throws SQLException
     */
    private void insertSuite(final Date date, final String service, final String version) throws SQLException {
        ensureConnectionOpened();

        final PreparedStatement ps = connection.prepareStatement(INSERT_SUITE);
        ps.setTimestamp(1, new Timestamp(date.getTime()));
        ps.setString(2, service);
        ps.setString(3, version);
        ps.execute();
        ps.close();

        existingSuites.add(new Suite(date, service, version));
    }

    /**
     * Verify if the service already exists in {@link #existingServices} and if not in the
     * database.
     *
     * @param service The service name.
     * @param version The service version.
     * @return {@code True} if the service exists, false otherwise.
     *
     * @throws SQLException if an error occurs while trying to get the services contained in the database.
     */
    private boolean serviceExists(final String service, final String version) throws SQLException {
        // Verify that the service is not already present in the list.
        if (!existingServices.isEmpty() && existingServices.contains(new Service(service, version))) {
            return true;
        }

        ensureConnectionOpened();

        final PreparedStatement ps = connection.prepareStatement(COUNT_SERVICE);
        ps.setString(1, service);
        ps.setString(2, version);
        final ResultSet rs = ps.executeQuery();
        int result = 0;
        if (rs.next()) {
            result = rs.getInt(1);
        }
        rs.close();
        ps.close();

        if (result == 0) {
            return false;
        }
        return true;
    }

    /**
     * Verify if the suite already exists in {@link #existingSuites} and if not in the
     * database.
     *
     * @param date    The date of the suite.
     * @param service The service name.
     * @param version The service version.
     * @return {@code True} if the service exists, false otherwise.
     * @throws SQLException if an error occurs while trying to get the suites contained in the database.
     */
    private boolean suiteExists(final Date date, final String service, final String version) throws SQLException {
        // Verify that the service is not already present in the list.
        if (!existingSuites.isEmpty() && existingSuites.contains(new Suite(date, service, version))) {
            return true;
        }

        ensureConnectionOpened();

        final PreparedStatement ps = connection.prepareStatement(COUNT_SUITE);
        ps.setTimestamp(1, new Timestamp(date.getTime()));
        final ResultSet rs = ps.executeQuery();
        int result = 0;
        if (rs.next()) {
            result = rs.getInt(1);
        }
        rs.close();
        ps.close();

        if (result == 0) {
            return false;
        }
        return true;
    }
}
