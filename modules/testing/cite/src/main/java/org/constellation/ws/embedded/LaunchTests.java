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

import org.apache.maven.plugin.MojoFailureException;
import org.apache.sis.util.logging.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import org.constellation.admin.SpringHelper;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.constellation.provider.DataProviders;


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
    public static void main(String[] args) throws Exception {
        Class.forName("javax.servlet.ServletContext");
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
        
        GrizzlyServer server = new GrizzlyServer();
        applicationContext.getEnvironment().setActiveProfiles("standard","derby");
        
        applicationContext.setConfigLocation("classpath:/cstl/spring/test-derby.xml");
        applicationContext.refresh();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(server);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(DataProviders.getConfigurator());
        SpringHelper.setApplicationContext(applicationContext);
        
        
        
        
        // Launch the server.
        server.initServer();

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
