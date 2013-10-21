/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.process.service;

import java.util.List;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.ws.WSEngine;
import static org.constellation.process.service.RenameServiceDescriptor.*;
import org.constellation.ws.Worker;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import static org.geotoolkit.parameter.Parameters.*;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class RenameService extends AbstractProcess {

    public RenameService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    @Override
    protected void execute() throws ProcessException {
        final String serviceType      = value(SERVICE_TYPE, inputParameters);
        final String identifier = value(IDENTIFIER, inputParameters);
        final String newName    = value(NEW_NAME, inputParameters);
        
        final List<String> existingService = ConfigurationEngine.getServiceConfigurationIds(serviceType);
        if (existingService.contains(identifier)) {
            if (!existingService.contains(newName)) {
                if (ConfigurationEngine.renameConfiguration(serviceType, identifier, newName)) {
                    // we stop the current worker
                    WSEngine.shutdownInstance(serviceType, identifier);

                    // start the new one
                    final Worker newWorker = WSEngine.buildWorker(serviceType, newName);
                    if (newWorker == null) {
                        throw new ProcessException("The instance " + newName + " can be started, maybe there is no configuration directory with this name.", this, null);
                    } else {
                        WSEngine.addServiceInstance(serviceType, newName, newWorker);
                        if (!newWorker.isStarted()) {
                            throw new ProcessException("unable to start the renamed instance", this, null);
                        }
                    }
                } else {
                    throw new ProcessException("Unable to rename the directory", this, null);
                }
            } else {
                throw new ProcessException("already existing instance:" + newName, this, null);
            }
        } else {
            throw new ProcessException("no existing instance:" + identifier, this, null);
        }
    }
}
