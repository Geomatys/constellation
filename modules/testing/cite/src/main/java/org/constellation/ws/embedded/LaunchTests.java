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
import java.util.logging.Level;
import java.util.logging.Logger;
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
public final class LaunchTests {
    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logging.getLogger(LaunchTests.class);

    /**
     * Prevents instanciation.
     */
    private LaunchTests() {}

    /**
     * Display the result of a process into the standard output.
     *
     * @param in Stream returned by the execution of the tests.
     * @throws IOException
     */
    private static void displayResult(final InputStream in) throws IOException {
        final InputStreamReader isr = new InputStreamReader(in);
        final BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }

    public static void main(String[] args) throws IOException {
        // Launch the server.
        GrizzlyServer.initServer();

        // Launch the test suite.
        final Runtime runtime = Runtime.getRuntime();
        if (args.length == 0) {
            LOGGER.info("No argument have been given to the script. Usage run.sh [profile...]");
        }
        for (String arg : args) {
            final Process process = runtime.exec(new String[]{"./run.sh", arg});
            displayResult(process.getInputStream());
            try {
                // Makes sure the test suite has been totally executed.
                process.waitFor();
            } catch (InterruptedException ex) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }

        // Then we can kill the server.
        GrizzlyServer.finish();
    }
}
