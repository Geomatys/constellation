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
import java.util.List;

// Geotoolkit dependencies
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
    private static final String INSERT_RESULT = "INSERT INTO \"Results\" VALUES (?,?,?,?,?,?);";
    private static final String INSERT_SERVICE = "INSERT INTO \"Services\" VALUES (?,?);";

    /**
     * Count request.
     */
    private static final String COUNT_SERVICE = "SELECT COUNT(name) FROM \"Services\" " +
                                                        "WHERE name=? AND version=?;";

    /**
     * The connection to the database of Cite Tests results.
     */
    private final Connection connection;

    /**
     * List of existing services, already checked from the database as present.
     */
    private final List<Service> existingServices = new ArrayList<Service>();

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
     * Insert a result into the matching table.
     *
     * @param result A result to insert.
     * @return
     * @throws SQLException if an error occurs in the insert request.
     */
    public int insertResult(final Result result) throws SQLException {
        return insertResult(result.getName(), result.getVersion(), result.getId(),
                            result.getDirectory(), result.isPassed(), result.getDate());
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

        final PreparedStatement ps = connection.prepareStatement(INSERT_RESULT);
        ps.setString(1, service);
        ps.setString(2, version);
        ps.setString(3, id);
        ps.setString(4, directory);
        ps.setBoolean(5, passed);
        ps.setTimestamp(6, new Timestamp(date.getTime()));
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
}
