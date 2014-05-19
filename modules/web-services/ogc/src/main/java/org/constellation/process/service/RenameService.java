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
