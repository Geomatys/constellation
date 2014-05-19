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
package org.constellation.plugin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.constellation.ws.embedded.LaunchTests;
import org.apache.sis.util.logging.Logging;


/**
 * A mojo plugin whose goal is to execute {@code Cite Tests} on different
 * web services, and compare the results with the previous session.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.4
 *
 * @phase test
 * @goal cite
 */
public class CiteTests extends AbstractMojo {
    /**
     * The default logger of exceptions.
     */
    private static final Logger LOGGER = Logging.getLogger(CiteTests.class);

    /**
     * The different services that will be tested.
     *
     * @parameter
     */
    private String[] services;

    /**
     * Launch the Cite Tests on the various Constellation services. An analysis
     * phase will be done in the end, and the build will fail if a regression
     * is found.
     *
     * @throws MojoExecutionException 
     * @throws MojoFailureException if some new tests are failing.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            LaunchTests.main(services);
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
    }

}
