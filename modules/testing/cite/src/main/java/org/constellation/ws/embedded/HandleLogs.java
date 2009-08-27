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
import java.util.logging.Logger;
import org.constellation.sql.Result;
import org.constellation.sql.ResultsDatabase;
import org.geotoolkit.util.logging.Logging;


/**
 * Launch the {@code Cite tests} on a {@linkplain GrizzlyServer Grizzly server} that
 * embedds Constellation's web services.
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
     * This method is public as an implementation side-effect - do not use.
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
                ex.printStackTrace();
            }
        }
    }

    private static void analyseResult(final String service, final String version, final Date date) {

        ResultsDatabase resDB = null;
        try {
            resDB = new ResultsDatabase();
            resDB.compareResults(service, version, date);
        } catch (SQLException ex) {
            // May be normal if we killed the process. Prints only
            // a summary of the exception, not the full stack trace.
            System.err.println(ex);
        } finally {
            try {
                resDB.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            LOGGER.info("No argument have been given to the script. Usage log.sh [profile...]");
        }
        final Runtime rt = Runtime.getRuntime();
        for (String arg : args) {
            final Date date = new Date();
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
                ex.printStackTrace();
            }
            //analyseResult(arg, date);
        }

        System.exit(0);
    }
}
