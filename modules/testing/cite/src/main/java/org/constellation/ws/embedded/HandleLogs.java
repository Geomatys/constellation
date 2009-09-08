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
package org.constellation.ws.embedded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.sql.Result;
import org.constellation.sql.ResultsDatabase;
import org.geotoolkit.util.logging.Logging;


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
    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logging.getLogger(HandleLogs.class);

    /**
     * Prevents instanciation.
     */
    private HandleLogs() {}

    /**
     * Displays the result of the process into the standard output.
     *
     * @param in      The input stream to display.
     * @param service The service name.
     * @param version The service version.
     * @param date    The execution date of the tests suite.
     */
    private static void copyResult(final InputStream in, final String service,
                                   final String version, final Date date)
    {
        ResultsDatabase resDB = null;
        try {
            final LogParser logParser = new LogParser(date);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            resDB = new ResultsDatabase();
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("Error")) {
                    throw new IllegalArgumentException("Error the session does not exist.");
                }
                final Result result = logParser.toResult(line);
                resDB.insertResult(result, service, version);
            }
            br.close();
            resDB.close();
        } catch (IOException e) {
            // May be normal if we killed the process. Prints only
            // a summary of the exception, not the full stack trace.
            System.err.println(e);
        } catch (SQLException e) {
            // May be normal if we killed the process. Prints only
            // a summary of the exception, not the full stack trace.
            System.err.println(e);
        } finally {
            try {
                resDB.close();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    /**
     * Analyses the result gotten from this session, with the ones of the previous session.
     *
     * @param date    The execution date of the current {@code Cite tests} session.
     * @param service The service name.
     * @param version The service version.
     */
    private static void analyseResult(final Date date, final String service, final String version) {

        ResultsDatabase resDB = null;
        try {
            resDB = new ResultsDatabase();
            resDB.compareResults(date, service, version);
        } catch (SQLException ex) {
            // May be normal if we killed the process. Prints only
            // a summary of the exception, not the full stack trace.
            System.err.println(ex);
        } finally {
            try {
                resDB.close();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            LOGGER.info("No argument have been given to the script. Usage log.sh [profile...]");
        }
        final Runtime rt = Runtime.getRuntime();
        // Stores the date for each sessions in the map.
        final Map<String,Date> dateOfSessions = new HashMap<String,Date>();
        // Launches the log script, and copy the results into the database.
        for (String arg : args) {
            final Date date = new Date();
            dateOfSessions.put(arg, date);
            if (!arg.contains("-")) {
                LOGGER.severe("The session argument should respect the syntax \"service-version\".");
            }
            final String[] argValue = arg.split("-");
            final String service = argValue[0];
            final String version = argValue[1];
            final Process process = rt.exec(new String[]{"./log.sh", arg});
            copyResult(process.getInputStream(), service, version, date);
            try {
                process.waitFor();
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        // Analyses the results of the session compared to the previous one.
        for (String arg : args) {
            final String[] argValue = arg.split("-");
            final String service = argValue[0];
            final String version = argValue[1];
            analyseResult(dateOfSessions.get(arg), service, version);
        }

        System.exit(0);
    }
}
