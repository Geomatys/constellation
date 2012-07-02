/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process.wfs.start;

import java.io.File;
import org.constellation.ServiceDef;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.wfs.ws.DefaultWFSWorker;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.ws.WSEngine;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.parameter.Parameters.value;
import static org.constellation.process.wfs.start.StartWFSServiceDescriptor.*;
/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public final class StartWFSService extends AbstractCstlProcess {

    public StartWFSService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    @Override
    protected void execute() throws ProcessException {
        final String identifier = value(IDENTIFIER, inputParameters);

        if (identifier == null || identifier.isEmpty()) {
            throw new ProcessException("Service instance identifier can't be null or empty.", this, null);
        }

        final File configDirectory = ConfigDirectory.getConfigDirectory();
        if (configDirectory != null && configDirectory.isDirectory()) {

            final String serviceName = ServiceDef.WFS_2_0_0.specification.name();
            final File serviceDir = new File(configDirectory, serviceName);
            if (serviceDir.exists() && serviceDir.isDirectory()) {

                //create service instance directory
                final File instanceDirectory = new File(serviceDir, identifier);

                if (instanceDirectory.exists() && serviceDir.isDirectory()) {
                    final DefaultWFSWorker worker = new DefaultWFSWorker(identifier, instanceDirectory);
                    if (worker != null) {
                        WSEngine.addServiceInstance(serviceName, identifier, worker);
                        if (!worker.isStarted()) {
                            throw new ProcessException("Unable to start the instance " + identifier + ".", this, null);
                        }
                    } else {
                        throw new ProcessException("The instance " + identifier + " can be started, maybe there is no configuration directory with this name.", this, null);
                    }
                } else {
                    throw new ProcessException("Service instance directory can' be created. Check permissions.", this, null);
                }
            } else {
                throw new ProcessException("Service directory can' be found for service name : "+serviceName, this, null);
            }
        } else {
            throw new ProcessException("Configuration directory can' be found.", this, null);
        }
    }
}
