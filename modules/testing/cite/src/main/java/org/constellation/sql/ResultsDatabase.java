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
package org.constellation.sql;

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.io.X364;
import org.geotoolkit.util.FileUtilities;

import java.io.File;
import java.io.IOException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

// Geotoolkit dependencies


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
public class ResultsDatabase {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.sql");
    /**
     * The pattern for the ouput of a date.
     */
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Database connection parameters.
     */
    private static final String DB_NAME  = "cite_resultsV2";
    private static final String PROTOCOL = "jdbc:postgresql";
    private static final String HOST     = "flupke.geomatys.com";
    private static final String USER     = "cite";
    private static final String PASS     = "WreewnUg";

    /**
     * Insertion requests.
     */
    private static final String INSERT_RESULT  = "INSERT INTO \"Results\" VALUES (?,?,?,?,?);";
    private static final String INSERT_SERVICE = "INSERT INTO \"Services\" VALUES (?,?);";
    private static final String INSERT_SUITE   = "INSERT INTO \"Suites\" VALUES (?,?,?,?);";
    private static final String INSERT_DESCRIPTION = "INSERT INTO \"TestsDescriptions\" VALUES (?,?);";

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
            "SELECT date,id,directory,groupnode FROM \"Results\" WHERE date=? AND passed=FALSE;";
    private static final String SELECT_TESTS_PASSED =
            "SELECT date,id,directory,groupnode FROM \"Results\" WHERE date=? AND passed=TRUE;";
    private static final String SELECT_TESTS_DESC =
            "SELECT assertion FROM \"TestsDescriptions\" WHERE id=?;";
    private static final String SELECT_PREVIOUS_SUITE =
            "SELECT date FROM \"Suites\" WHERE lastsuccess='TRUE' AND service=? AND version=?;";
    private static final String SELECT_TEST_RESULT_FROM_ID =
            "SELECT date,r.id,directory,passed,assertion,groupnode FROM \"Results\" r, \"TestsDescriptions\" t "
            + "WHERE date=? and r.id=? and r.id=t.id;";

    /**
     * Delete requests.
     */
    private static final String DELETE_SUITE = "DELETE FROM \"Suites\" WHERE date=?;";

    /**
     * Update requests.
     */
    private static final String UPDATE_SUITE_SUCCESS = "UPDATE \"Suites\"  set lastsuccess='TRUE' WHERE date=?;";

    private static final String UPDATE_SUITE_REPLACED = "UPDATE \"Suites\"  set lastsuccess='FALSE' WHERE service=? and version =?;";

    /**
     * The connection to the database of Cite Tests results.
     */
    protected final Connection connection;

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
     * @return {@code True} if there is no test that fails for this session and succeed
     *         for the previous one. {@code False} if there is one or more new problems.
     * @throws SQLException
     */
    public boolean compareResults(final Date date, final String service, final String version)
                                  throws SQLException
    {
        // Contains all tests that have failed for the current session.
        final List<Result> currentTestsFailed  = getTestsFailed(date);
        final List<Result> currentTestsPassed  = getTestsPassed(date);
        final int nbCurrentTest                = currentTestsFailed.size()  + currentTestsPassed.size();

        final Date previousSuite = getPreviousSuiteDate(service, version);
        // Contains all tests that have failed for the previous session.
        final List<Result> previousTestsFailed;
        // Contains all tests that have Passed for the previous session.
        final List<Result> previousTestsPassed;

        // Will contain the tests that have passed for the last session, but not for the current one.
        final List<Result> problematicTests = new ArrayList<Result>();
        // Will contain the groupNode tests that have passed for the last session, but not for the current one (not problematic).
        final List<Result> problematicGroupNodeTests = new ArrayList<Result>();
        // Will contain the tests that have passed for the current session, but not for the last one.
        final List<Result> newlyPassedTests = new ArrayList<Result>();
        // Will contain the tests that have been activated for the current session.
        final List<Result> newlyActivatedTests = new ArrayList<Result>();
        // Will contain the tests that have been deactivated for the current session.
        final List<Result> disapearTests = new ArrayList<Result>();

        final boolean missingTest;
        if (previousSuite != null) {
            previousTestsFailed = getTestsFailed(previousSuite);
            previousTestsPassed = getTestsPassed(previousSuite);
            for (Result currentTest : currentTestsFailed) {
                // add to the problematic list if a test fail and was passed in the last session
                if (!isTestPresentInList(currentTest, previousTestsFailed)) {
                    if (isTestPresentInList(currentTest, previousTestsPassed)) {
                        if (currentTest.isGroupNode()) {
                            problematicGroupNodeTests.add(currentTest);
                        } else {
                            problematicTests.add(currentTest);
                        }
                    } else {
                        newlyActivatedTests.add(currentTest);
                    }
                }
            }
            for (Result currentTest : currentTestsPassed) {
                // add to the newlyPassed list if a test pass and was failed in the last session
                if (!isTestPresentInList(currentTest, previousTestsPassed)) {
                    if (isTestPresentInList(currentTest, previousTestsFailed)) {
                        newlyPassedTests.add(currentTest);
                    } else {
                        newlyActivatedTests.add(currentTest);
                    }
                }
            }
            int nbPreviousTest = previousTestsFailed.size() + previousTestsPassed.size();
            missingTest = nbCurrentTest < nbPreviousTest;

            //if some test have been deactivated we list them
            if (missingTest) {
                previousTestsPassed.addAll(previousTestsFailed);
                for (Result previousTest: previousTestsPassed) {
                    if (!isTestPresentInList(previousTest, currentTestsFailed) && !isTestPresentInList(previousTest, currentTestsPassed)) {
                        disapearTests.add(previousTest);
                    }
                }
            }

        } else {
            missingTest = false;
        }

        displayComparisonResults(problematicTests, problematicGroupNodeTests, newlyPassedTests, newlyActivatedTests, disapearTests, date, previousSuite, service, version);
        return problematicTests.isEmpty() && !missingTest;
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
                            result.isPassed(), result.isGroupNode(), result.getDate());
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
                            final String directory, final boolean passed, final boolean groupNode, final Date date) throws SQLException
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
        ps.setBoolean(5, groupNode);

        final int result = ps.executeUpdate();
        ps.close();

        return result;
    }

    /**
     * Delete the specified suite of tests.
     *
     * @param date The date of the suite to delete.
     * @throws SQLException if an error occurs in the delete request.
     */
    @Deprecated
    public void deleteSuite(final Date date) throws SQLException {
        ensureConnectionOpened();

        final PreparedStatement ps = connection.prepareStatement(DELETE_SUITE);
        ps.setTimestamp(1, new Timestamp(date.getTime()));
        ps.execute();

        ps.close();
    }

    /**
     * Set the flag lastsuccess of the previous suite to false, and set the flag of the last suite
     * identified by the specified date to true.
     *
     * @param date The date of the suite to update.
     * @param service The service name.
     * @param version The service version.
     * @throws SQLException if an error occurs in the update request.
     */
    public void setSuiteLastSuccess(final Date date, final String service, final String version) throws SQLException {
        ensureConnectionOpened();

        PreparedStatement ps = connection.prepareStatement(UPDATE_SUITE_REPLACED);
        ps.setString(1, service);
        ps.setString(2, version);
        ps.execute();

        ps = connection.prepareStatement(UPDATE_SUITE_SUCCESS);
        ps.setTimestamp(1, new Timestamp(date.getTime()));
        ps.execute();

        ps.close();
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
     * @param problematicTests      List of problematic tests, that no more passed.
     * @param problematicGroupTests List of problematic tests, that no more passed.
     * @param newlyPassedTests    List of tests that are corrected in the current session.
     * @param newlyActivatedTests List of tests that appears in the current session.
     * @param deActivatedTests    List of tests that disapears in the current session.
     * @param date                The date of the suite of tests.
     * @param previous            The date of the previous suite of tests. Note that if it is
     *                            {@code null}, we can't compare the current session with
     *                            another one.
     * @param service             The service name.
     * @param version             The service version.
     */
    private void displayComparisonResults(final List<Result> problematicTests,
                                          final List<Result> problematicGroupTests,
                                          final List<Result> newlyPassedTests,
                                          final List<Result> newlyActivatedTests,
                                          final List<Result> deActivatedTests,
                                          final Date date, final Date previous,
                                          final String service, final String version)
    {
        final char endOfLine = '\n';
        final char tab       = '\t';
        final StringBuilder sb = new StringBuilder();
        final boolean x364 = X364.isSupported();
        if (previous == null) {
            if (x364) {sb.append(X364.FOREGROUND_BLUE.sequence());}
            if (x364) {sb.append(X364.BOLD.sequence());}
            sb.append("This is the first session of tests launched for ");
            sb.append(service).append(" ").append(version)
              .append(". We can't compare the results with a previous one.");
            if (x364) {sb.append(X364.RESET.sequence());}
            LOGGER.info(sb.toString());
            return;
        }
        if (x364) {sb.append(X364.FOREGROUND_MAGENTA.sequence());}
        if (x364) {sb.append(X364.BOLD.sequence());}
        sb.append("Results for the session ");
        sb.append(service).append(" ").append(version).append(", executed at ").append(DATE_FORMAT.format(date));
        sb.append(" compared to the one at ").append(previous.toString()).append(endOfLine);
        if (x364) {sb.append(X364.RESET.sequence());}
        if (x364) {sb.append(X364.FOREGROUND_GREEN.sequence());}
        if (newlyPassedTests.isEmpty()) {
            sb.append(tab).append("No tests have been corrected in the current session.").append(endOfLine);
        } else {
            sb.append(tab).append("Tests which have been corrected in the current session:").append(endOfLine);
            for (Result res : newlyPassedTests) {
                sb.append(tab).append(tab).append(res.toString()).append(endOfLine);
            }
        }

        if (x364) {sb.append(X364.FOREGROUND_DEFAULT.sequence());}
        if (problematicTests.isEmpty()) {
            if (x364) {sb.append(X364.FOREGROUND_GREEN.sequence());}
            sb.append(tab).append("No new tests have failed in the current session.").append(endOfLine);
            if (x364) {sb.append(X364.FOREGROUND_DEFAULT.sequence());}
        } else {
            if (x364) {sb.append(X364.FOREGROUND_RED.sequence());}
            if (x364) {sb.append(X364.BOLD.sequence());}
            sb.append(tab).append("/!\\ Some tests are now failing ! You should fix them to restore the build /!\\")
              .append(endOfLine);
            for (Result res : problematicTests) {
                sb.append(tab).append(tab).append("Id: ").append(res.getId()).append(endOfLine);
                sb.append(tab).append(tab).append(tab).append("==> Directory: ").append(res.getDirectory())
                  .append(endOfLine);
            }
        }

        if (!problematicGroupTests.isEmpty()) {
            if (x364) {sb.append(X364.FOREGROUND_RED.sequence());}
            sb.append(tab).append("Some Group tests are now failing because of new activated test failling (not an error)")
              .append(endOfLine);
            for (Result res : problematicGroupTests) {
                sb.append(tab).append(tab).append("Id: ").append(res.getId()).append(endOfLine);
                sb.append(tab).append(tab).append(tab).append("==> Directory: ").append(res.getDirectory())
                  .append(endOfLine);
            }
            if (x364) {sb.append(X364.FOREGROUND_DEFAULT.sequence());}
        }

        if (!newlyActivatedTests.isEmpty()) {
            if (x364) {sb.append(X364.FOREGROUND_GREEN.sequence());}
            sb.append(tab).append("New tests have been activated in the current session.").append(endOfLine);
            for (Result res : newlyActivatedTests) {
                sb.append(tab).append(tab).append("Id: ").append(res.getId());
                sb.append(" Result:").append(res.isPassed()).append(endOfLine);
            }
            if (x364) {sb.append(X364.FOREGROUND_DEFAULT.sequence());}
        }

        if (!deActivatedTests.isEmpty()) {
            if (x364) {sb.append(X364.FOREGROUND_RED.sequence());}
            if (x364) {sb.append(X364.BOLD.sequence());}
            sb.append(tab).append("/!\\ Some tests have been deactivated in the current session! ! You should reactivate them to restore the build /!\\")
                    .append(endOfLine);
            for (Result res : deActivatedTests) {
                sb.append(tab).append(tab).append("Id: ").append(res.getId()).append(endOfLine);
                sb.append(tab).append(tab).append(tab).append("==> Directory: ").append(res.getDirectory());
                sb.append(" Previous result:").append(res.isPassed()).append(endOfLine);
            }
            if (x364) {sb.append(X364.FOREGROUND_DEFAULT.sequence());}
        }

        if (x364) {sb.append(X364.RESET.sequence());}
        LOGGER.info(sb.toString());
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
     * @param service The service name.
     * @param version The service version.
     * @throws SQLException
     */
    private Date getPreviousSuiteDate(final String service, final String version)
                                      throws SQLException
    {
        ensureConnectionOpened();

        final PreparedStatement ps = connection.prepareStatement(SELECT_PREVIOUS_SUITE);
        ps.setString(1, service);
        ps.setString(2, version);
        final ResultSet rs = ps.executeQuery();
        final Date dateResult = (rs.next()) ? rs.getTimestamp(1) : null;
        rs.close();

        return dateResult;
    }

    /**
     * Esxtract a description of the test from a file.
     *
     * @param id The identifier of the test
     * @param directory The directory where to find the log file.
     * @return An assertion.
     */
    public String extractDescription(final String id, final String directory) {
        final File f = new File("target/logs/" + directory + "/log.xml");
        String assertion = null;
        if (f.exists()) {
            try {
                final String xml = FileUtilities.getStringFromFile(f);
                if (xml.indexOf("<assertion>") != -1) {
                    assertion =  xml.substring(xml.indexOf("<assertion>") + 11, xml.indexOf("</assertion>"));
                    final PreparedStatement ps = connection.prepareStatement(INSERT_DESCRIPTION);
                    ps.setString(1, id);
                    ps.setString(2, assertion);
                    ps.executeUpdate();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error while inserting new assertion for test:" + id,  ex);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Error while reading log file for test:" + id, ex);
            }
        }
        return assertion;
    }

    /**
     * Returns a list of {@link Result} that have failed for the session at the date specified.
     * This list can be empty, but never {@code null}.
     *
     * @param date The date of the session.
     * @throws SQLException
     */
    protected List<Result> getTestsFailed(final Date date) throws SQLException {
        ensureConnectionOpened();

        final PreparedStatement psCurrent = connection.prepareStatement(SELECT_TESTS_FAILED);
        psCurrent.setTimestamp(1, new Timestamp(date.getTime()));

        final List<Result> results = new ArrayList<Result>();
        final ResultSet rs = psCurrent.executeQuery();
        while (rs.next()) {
            final String id           = rs.getString(2);
            final String directory    = rs.getString(3);
            final boolean isGroupNode = rs.getBoolean(4);

            final PreparedStatement psDesc = connection.prepareStatement(SELECT_TESTS_DESC);
            psDesc.setString(1, id);
            final ResultSet rs2 = psDesc.executeQuery();
            final String assertion;
            if (rs2.next()) {
                assertion = rs2.getString(1);
            } else {
                assertion = extractDescription(id, directory);
            }

            results.add(new Result(rs.getTimestamp(1), id, directory, false, isGroupNode, assertion));
            rs2.close();
        }
        rs.close();

        return results;
    }

    /**
     * Returns a list of {@link Result} that have passed for the session at the date specified.
     * This list can be empty, but never {@code null}.
     *
     * @param date The date of the session.
     * @throws SQLException
     */
    private List<Result> getTestsPassed(final Date date) throws SQLException {
        ensureConnectionOpened();

        final PreparedStatement psCurrent = connection.prepareStatement(SELECT_TESTS_PASSED);
        psCurrent.setTimestamp(1, new Timestamp(date.getTime()));

        final List<Result> results = new ArrayList<Result>();
        final ResultSet rs = psCurrent.executeQuery();
        while (rs.next()) {
            final String id           = rs.getString(2);
            final String directory    = rs.getString(3);
            final boolean isGroupNode = rs.getBoolean(4);

            final PreparedStatement psDesc = connection.prepareStatement(SELECT_TESTS_DESC);
            psDesc.setString(1, id);
            final ResultSet rs2 = psDesc.executeQuery();
            final String assertion;
            if (rs2.next()) {
                assertion = rs2.getString(1);
            } else {
                assertion = extractDescription(id, directory);
            }
            results.add(new Result(rs.getTimestamp(1), id, directory, true, isGroupNode, assertion));
            rs2.close();
        }
        rs.close();

        return results;
    }

    /**
     * Returns the test for the given id and date.
     *
     * @param date The date of the session.
     * @param id The test id.
     * @return The {@linkplain Result results} for this test, or {@code null} if the test is
     *         not found in the database.
     * @throws SQLException
     */
    protected Result getTest(final Date date, final String id) throws SQLException {
        ensureConnectionOpened();

        final PreparedStatement psCurrent = connection.prepareStatement(SELECT_TEST_RESULT_FROM_ID);
        psCurrent.setTimestamp(1, new Timestamp(date.getTime()));
        psCurrent.setString(2, id);

        Result result = null;
        final ResultSet rs = psCurrent.executeQuery();
        if (rs.next()) {
            result = new Result(rs.getDate(1), rs.getString(2), rs.getString(3),
                                rs.getBoolean(4), rs.getBoolean(5), rs.getString(6));
        }
        rs.close();
        return result;
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
        ps.setBoolean(4, false);
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
            if (currentTest.getId().equals(test.getId())) {
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
