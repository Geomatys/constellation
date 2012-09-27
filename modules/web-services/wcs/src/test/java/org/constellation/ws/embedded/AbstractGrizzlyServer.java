/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2010, Geomatys
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

// JAI dependencies

// J2SE dependencies
import java.io.File;
import java.util.logging.Logger;

// geotoolkit dependencies
import org.geotoolkit.util.logging.Logging;

// JUnit dependencies
import org.junit.*;


/**
 * Launches a Grizzly server in a thread at the beginning of the testing process
 * and kill it when it is done.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public abstract class AbstractGrizzlyServer {//extends CoverageSQLTestCase {
    /**
     * The grizzly server that will received some HTTP requests.
     */
    protected static GrizzlyThread grizzly = null;

    private static final Logger LOGGER = Logging.getLogger("org.constellation.ws.embedded");

    /**
     * Initialize the Grizzly server, on which WCS and WMS requests will be sent,
     * and defines a PostGrid data provider.
     */
    public static void initServer(final String[] resourcePackages) {
        // Protective test in order not to launch a new instance of the grizzly server for
        // each sub classes.
        if (grizzly != null) {
            return;
        }

        /* Instanciates the Grizzly server, but not start it at this moment.
         * The implementation waits for the data provider to be defined for
         * starting the server.
         */
        grizzly = new GrizzlyThread(resourcePackages);

        // Starting the grizzly server
        grizzly.start();

    }

    /**
     * Stop the grizzly server, if it is still alive.
     */
    @AfterClass
    public static void finish() {
        if (grizzly.isAlive()) {
            grizzly.interrupt();
        }
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * Thread that launches a Grizzly server in a separate thread.
     * Requests will be done on this working server.
     */
    protected static class GrizzlyThread extends Thread {
        private final CstlEmbeddedService cstlServer;

        public GrizzlyThread(final String[] resourcePackages) {
            cstlServer = new CstlEmbeddedService(new String[]{}, resourcePackages);
        }

        public Integer getCurrentPort() {
            return cstlServer.currentPort;
        }

        /**
         * Runs a Grizzly server for five minutes.
         */
        @Override
        public void run() {
            cstlServer.duration = 5*60*1000;
            cstlServer.findAvailablePort = true;
            cstlServer.runAll();
        }
    }
}
