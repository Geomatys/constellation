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
     * Prevents instanciation.
     */
    private LaunchTests() {}

    public static void main(String[] args) throws IOException {
        // Launch the server.
        GrizzlyServer.initServer();

        // Launch the test suite.
        final Runtime runtime = Runtime.getRuntime();
        final String argSerie = (args.length > 0) ? args[0] : null;
        final Process process = runtime.exec(new String[]{"./run.sh", argSerie});

        // Display the result.
        final InputStreamReader isr = new InputStreamReader(process.getInputStream());
        final BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();

        // Then we can kill the server.
        GrizzlyServer.finish();
    }
}
