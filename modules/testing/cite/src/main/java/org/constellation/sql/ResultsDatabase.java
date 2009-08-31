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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Geotoolkit dependencies
import org.geotoolkit.internal.sql.DefaultDataSource;


/**
 * <p>Allow to store results of a {@code Cite tests} session into a database.
 * A comparison between the current and the previous session can be performed,
 * and a summary is then displayed.</p>
 * <p>If some tests are now failing but passed in the previous session, then the
 * compilation will fail and the user will be informed about the tests that
 * are now failing. The complete error logs can be retrieved in the directory
 * specified.</p>
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.4
 */
public final class ResultsDatabase {
    /**
     * The pattern for the ouput of a date.
     */
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

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
            "SELECT date,id,directory FROM \"Results\" WHERE date=? AND passed=FALSE;";
    private static final String SELECT_PREVIOUS_SUITE =
            "SELECT date FROM \"Suites\" WHERE date < ? AND service=? AND version=? ORDER BY date DESC;";

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
     * Compare the results between the current session and the previous one.
     *
     * @param date    The execution date of the tests.
     * @param service The service name.
     * @param version The service version.
     * @throws SQLException
     */
    public void compareResults(final Date date, final String service, final String version)
                               throws SQLException
    {
        // Contains all tests that have failed for the current session.
        final List<Result> currentTestsFailed  = getTestsFailed(date);

        final Date previousSuite = getPreviousSuiteDate(date, service, version);
        // Contains all tests that have failed for the previous session.
        final List<Result> previousTestsFailed;

        // Will contain the tests that have passed for the last session, but not for the current one.
        final List<Result> problematicTests = new ArrayList<Result>();
        // Will contain the tests that have passed for the current session, but not for the last one.
        final List<Result> newlyPassedTests = new ArrayList<Result>();
        if (previousSuite != null) {
            previousTestsFailed = getTestsFailed(previousSuite);
            for (Result currentTest : currentTestsFailed) {
                if (!isTestPresentInList(currentTest, previousTestsFailed)) {
                    problematicTests.add(currentTest);
                }
            }
            for (Result previousTest : previousTestsFailed) {
                if (!isTestPresentInList(previousTest, currentTestsFailed)) {
                    newlyPassedTests.add(previousTest);
                }
            }
        }
        displayComparaisonResults(problematicTests, newlyPassedTests, date, previousSuite, service, version);
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
     * Display the results for the current session, compared to the last one.
     *
     * @param problematicTests List of problematic tests, that no more passed.
     * @param newlyPassedTests List of tests that are corrected in the current session.
     * @param date             The date of the suite of tests.
     * @param previous         The date of the previous suite of tests. Note that if it is
     *                         {@code null}, we can't compare the current session with
     *                         another one.
     * @param service          The service name.
     * @param version          The service version.
     */
    private void displayComparaisonResults(final List<Result> problematicTests,
                                           final List<Result> newlyPassedTests,
                                           final Date date, final Date previous,
                                           final String service, final String version)
    {
        final char endOfLine = '\n';
        final char tab       = '\t';
        final StringBuilder sb;
        if (previous == null) {
            sb = new StringBuilder("This is the first session of tests launched for ");
            sb.append(service).append(" ").append(version)
              .append(". We can't compare the results with a previous one.");
            System.out.println(sb.toString());
            return;
        }
        sb = new StringBuilder("Results for the session ");
        sb.append(service).append(" ").append(version).append(", executed at ").append(DATE_FORMAT.format(date));
        sb.append(" compared to the one at ").append(previous.toString()).append(endOfLine);
        if (newlyPassedTests.isEmpty()) {
            sb.append(tab).append("No tests have been corrected in the current session.").append(endOfLine);
        } else {
            sb.append(tab).append("Tests which have been corrected in the current session:").append(endOfLine);
            for (Result res : newlyPassedTests) {
                sb.append(tab).append(tab).append(res.toString()).append(endOfLine);
            }
        }

        if (problematicTests.isEmpty()) {
            sb.append(tab).append("No new tests have failed in the current session.").append(endOfLine);
        } else {
            sb.append(tab).append("/!\\ Some tests are now failing ! You should fix them to restore the build /!\\")
              .append(endOfLine);
            for (Result res : problematicTests) {
                sb.append(tab).append(tab).append("Id: ").append(res.getId()).append(endOfLine);
                sb.append(tab).append(tab).append(tab).append("==> Directory: ").append(res.getDirectory())
                  .append(endOfLine);
            }
            System.out.println(sb.toString());
            throw new RuntimeException("Some tests are now failing, but not in the previous suite. " +
                    "Please fix the service responsible of the failure of these tests !");
        }

        System.out.println(sb.toString());
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
     * Returns the date of the previous session of tests, or {@code null} if there is no other
     * test suite in the database.
     *
     * @param date The date of the current session.
     * @param service The service name.
     * @param version The service version.
     * @throws SQLException
     */
    private Date getPreviousSuiteDate(final Date date, final String service, final String version)
                                      throws SQLException
    {
        ensureConnectionOpened();

        final PreparedStatement ps = connection.prepareStatement(SELECT_PREVIOUS_SUITE);
        ps.setTimestamp(1, new Timestamp(date.getTime()));
        ps.setString(2, service);
        ps.setString(3, version);
        final ResultSet rs = ps.executeQuery();
        final Date dateResult = (rs.next()) ? rs.getTimestamp(1) : null;
        rs.close();

        return dateResult;
    }

    /**
     * Returns a list of {@link Result} that have failed for the session at the date specified.
     * This list can be empty, but never {@code null}.
     *
     * @param date The date of the session.
     * @throws SQLException
     */
    private List<Result> getTestsFailed(final Date date) throws SQLException {
        ensureConnectionOpened();

        final PreparedStatement psCurrent = connection.prepareStatement(SELECT_TESTS_FAILED);
        psCurrent.setTimestamp(1, new Timestamp(date.getTime()));

        final List<Result> results = new ArrayList<Result>();
        final ResultSet rs = psCurrent.executeQuery();
        while (rs.next()) {
            results.add(new Result(rs.getTimestamp(1), rs.getString(2), rs.getString(3), false));
        }
        rs.close();

        return results;
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
     * Returns {@code true} if the test is present in the list specified. {@code False}
     * otherwise.
     *
     * @param test  The test to verify the existence in the list.
     * @param tests The list of tests into which we search the test.
     */
    private boolean isTestPresentInList(final Result test, final List<Result> tests) {
        for (Result currentTest : tests) {
            if (currentTest.getDirectory().equals(test.getDirectory()) &&
                currentTest.getId().equals(test.getId()))
            {
                return true;
            }
        }
        return false;
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
