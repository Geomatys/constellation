/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.metadata.configuration;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

// Constellation dependencies
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.ws.rs.ContainerNotifierImpl;

/**
 * Default implementation of CSW Configurer
 *
 * @author Guilhem Legal
 */
public class DefaultCSWConfigurer extends AbstractCSWConfigurer {

    public DefaultCSWConfigurer(ContainerNotifierImpl cn) throws ConfigurationException {
        super(cn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File getCswInstanceDirectory(String instanceId) {
        final File configDir = ConfigDirectory.getConfigDirectory();

        if (configDir != null && configDir.exists()) {
            final File cswDir = new File(configDir, "CSW");
            if (configDir.exists() && configDir.isDirectory()) {
                File  instanceDir = new File(cswDir, instanceId);
                if (instanceDir.exists() && instanceDir.isDirectory()) {
                    return instanceDir;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<File> getAllCswInstanceDirectory() {
        final File configDir     = ConfigDirectory.getConfigDirectory();
        final List<File> results = new ArrayList<File>();
        if (configDir != null && configDir.exists()) {
            final File cswDir = new File(configDir, "CSW");
            if (cswDir.exists() && cswDir.isDirectory()) {
                for (File instanceDir : cswDir.listFiles()) {
                    if (instanceDir.isDirectory()) {
                        results.add(instanceDir);
                    }
                }
            }
        }
        return results;
    }
}
