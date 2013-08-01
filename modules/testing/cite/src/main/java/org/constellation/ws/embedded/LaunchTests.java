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
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.apache.maven.plugin.MojoFailureException;

import org.apache.sis.util.logging.Logging;


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
public final class LaunchTests implements Runnable {
    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logging.getLogger(LaunchTests.class);

    /**
     * The running process.
     */
    private final Process process;

    private boolean hasCompleted = false;
    
    /**
     * Creates a new monitor for the given process.
     */
    private LaunchTests(final Process process) {
        this.process = process;
    }

    /**
     * Displays the result of the process into the standard output.
     * This method is public as an implementation side-effect - do not use.
     */
    @Override
    public void run() {
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
        } catch (IOException e) {
            // May be normal if we killed the process. Prints only
            // a summary of the exception, not the full stack trace.
            System.err.println(e);
        }
        hasCompleted = true;
    }

    /**
     * Launch the Cite Tests, and the analysis phase.
     *
     * @param args The session to execute. Each parameters should respect the
     *             syntax "service-version".
     * @throws IOException if the execution of the script fails.
     * @throws MojoFailureException if a regression is detected.
     */
    public static void main(String[] args) throws IOException, MojoFailureException {
        // Launch the server.
        GrizzlyServer.initServer();

        // Launch the test suite.
        if (args.length == 0) {
            LOGGER.info("No argument have been given to the script. Usage run.sh [profile...]");
            return;
        }
        final Runtime rt = Runtime.getRuntime();
        for (String arg : args) {
            final Process process = rt.exec(new String[]{"../cite/run.sh", arg});
            final LaunchTests lt = new LaunchTests(process);
            final Thread t = new Thread(lt);
            t.setDaemon(true);
            t.start();
            try {
                t.join(30*60*1000L);
            } catch (InterruptedException e) {
                // Ignore. We will kill the process.
            }
            if (!lt.hasCompleted) {
               LOGGER.severe("Shutting down process after timeout");
            }
            process.destroy();
        }
        
        // Then we can kill the server.
        GrizzlyServer.finish();

        // Launch the process that will analyse the tests results.
        HandleLogs.main(args);

        System.exit(0);
    }
}
