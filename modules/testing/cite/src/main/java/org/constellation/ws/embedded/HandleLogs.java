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
package org.constellation.ws.embedded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.MojoFailureException;

import org.constellation.sql.Result;
import org.constellation.sql.ResultsDatabase;
import org.apache.sis.util.logging.Logging;


/**
 * Perform actions on the logs gotten from the execution of {@code Cite tests}.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.4
 * @see GrizzlyServer
 */
public final class HandleLogs {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.ws.embedded");
    /**
     * Prevents instantiation.
     */
    private HandleLogs() {}

    /**
     * Inserts the result of the process into the database.
     *
     * @param in      The input stream to display.
     * @param service The service name.
     * @param version The service version.
     * @param date    The execution date of the tests suite.
     */
    private static void insertResult(final InputStream in, final String service,
                                   final String version, final Date date)
    {
        ResultsDatabase resDB = null;
        try {
            final LogParser logParser = new LogParser(date);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            Result precedentResult = null;
            final List<Result> results = new ArrayList<Result>();
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("Error")) {
                    throw new IllegalArgumentException("Error the session does not exist.");
                }
                final Result result = logParser.toResult(line);
                if (precedentResult == null) {
                    result.setGroupNode(true);
                } else if (isChildOf(precedentResult, result)) {
                    precedentResult.setGroupNode(true);
                }
                precedentResult = result;
                results.add(result);
            }
            br.close();

            resDB = new ResultsDatabase();
            for (Result result : results) {
                resDB.insertResult(result, service, version);
            }
            resDB.close();
        } catch (IOException e) {
            // May be normal if we killed the process. Prints only
            // a summary of the exception, not the full stack trace.
            System.err.println(e);
        } catch (SQLException e) {
            // May be normal if we killed the process. Prints only
            // a summary of the exception, not the full stack trace.
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
        } finally {
            try {
                if (resDB != null) {
                    resDB.close();
                }
            } catch (SQLException ex) {
                System.err.println(ex);
            }
        }
    }

    /**
     * return true if the result is a child of the specified precedent result.
     * A result id considered as a child if his directory contains the precedent directory.
     * 
     * @param oldResult The precedent result.
     * @param result The current result.
     *
     * @return True if the directory of the current result contain the directory of the precedent result.
     */
    private static boolean isChildOf(final Result oldResult, final Result result) {
        final String oldDirectory   = oldResult.getDirectory();
        final String childDirectory = result.getDirectory();
        return childDirectory.startsWith(oldDirectory);
    }

    /**
     * Analyzes the result gotten from this session, with the ones of the previous session.
     *
     * @param date    The execution date of the current {@code Cite tests} session.
     * @param service The service name.
     * @param version The service version.
     * @return {@code True} if there is no test that fails for this session and succeed
     *         for the previous one. {@code False} if there is one or more new problems.
     */
    private static boolean analyseResult(final Date date, final String service, final String version) {

        ResultsDatabase resDB = null;
        try {
            resDB = new ResultsDatabase();
            return resDB.compareResults(date, service, version);
        } catch (SQLException ex) {
            // May be normal if we killed the process. Prints only
            // a summary of the exception, not the full stack trace.
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return true;
        } finally {
            try {
                if (resDB != null) {
                    resDB.close();
                }
            } catch (SQLException ex) {
                System.err.println(ex);
            }
        }
    }

    /**
     * Deletes from the database a specific session that contains new failures
     * from the previous one.
     *
     * @param date The date of the session that will be deleted.
     */
    @Deprecated
    private static void deleteSessionWithNewFailures(final Date date) {
        ResultsDatabase resDB = null;
        try {
            resDB = new ResultsDatabase();
            resDB.deleteSuite(date);
        } catch (SQLException ex) {
            // May be normal if we killed the process. Prints only
            // a summary of the exception, not the full stack trace.
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        } finally {
            try {
                if (resDB != null) {
                    resDB.close();
                }
            } catch (SQLException ex) {
                System.err.println(ex);
            }
        }
    }

    /**
     * Update the last session by tagging it as the last succeed suite.
     *
     * @param date the date of the current session which have succeed
     * @param service The service name.
     * @param version The service version.
     */
    private static void updateSessionSuite(final Date date, final String service, final String version) {
        ResultsDatabase resDB = null;
        try {
            resDB = new ResultsDatabase();
            resDB.setSuiteLastSuccess(date, service, version);
        } catch (SQLException ex) {
            // May be normal if we killed the process. Prints only
            // a summary of the exception, not the full stack trace.
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        } finally {
            try {
                if (resDB != null) {
                    resDB.close();
                }
            } catch (SQLException ex) {
                System.err.println(ex);
            }
        }
    }

    /**
     * From the logs of a Cite Tests session, extracts various information and
     * store them into a database. Those data will be used to compare the rate
     * of success between the current session and the previous one.
     *
     * @param args The session to execute. Each parameters should respect the
     *             syntax "service-version".
     * @throws IOException if the execution of the script fails.
     * @throws MojoFailureException if a regression is detected.
     */
    public static void main(String[] args) throws IOException, MojoFailureException {
        if (args.length == 0) {
            System.err.println("No argument have been given to the script. Usage log.sh [profile...]");
            return;
        }
        final Runtime rt = Runtime.getRuntime();
        // Stores the date for each sessions in the map.
        final Map<String,Date> dateOfSessions = new HashMap<String,Date>();
        // Launches the log script, and copy the results into the database.
        for (String arg : args) {
            if (!arg.contains("-")) {
                System.err.println("The session argument should respect the syntax \"service-version\".");
                continue;
            }
            final Date date = new Date();
            dateOfSessions.put(arg, date);
            final String[] argValue = arg.split("-");
            final String service = argValue[0];
            final String version = argValue[1];
            final Process process = rt.exec(new String[]{"../cite/log.sh", arg});
            insertResult(process.getInputStream(), service, version, date);
            try {
                process.waitFor();
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }

        /*
         * Analyses the results of the session compared to the previous one. If one session
         * contains new errors, then an exception will be thrown in the end.
         */
        boolean successResults = true;
        for (String arg : args) {
            final String[] argValue = arg.split("-");
            final String service = argValue[0];
            final String version = argValue[1];
            final Date currentSessionDate = dateOfSessions.get(arg);
            if (analyseResult(currentSessionDate, service, version) == false) {
                /* The session has already been writen in the database for comparison purpose,
                 * but in fact we do not want to keep it there because there are new failures.
                 * We do not want the next tests session to be compared to that one, this way
                 * it is compulsory to correct newly-failing tests to fix the build.
                 */
                //deleteSessionWithNewFailures(currentSessionDate);
                successResults = false;
            } else {
                updateSessionSuite(currentSessionDate, service, version);
            }
        }

        if (successResults == false) {
            /*
             * A regression is detected, a mojo exception is thrown to make the build fail.
             */
            throw new MojoFailureException("Some tests are now failing, but not in the previous suite.\n" +
                          "Please fix service(s) responsible for that building failure.");
        }
        System.exit(0);
    }
}
