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

import org.constellation.admin.ConfigurationEngine;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.ws.WSEngine;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.process.service.DeleteServiceDescriptor.*;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class DeleteService extends AbstractCstlProcess {

    public DeleteService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Delete an instance and configuration for a specified service and instance name.
     *
     * @throws ProcessException in cases :
     * - if identifier doesn't exist or is null/empty.
     * - if error during file erasing phase.
     */
    @Override
    protected void execute() throws ProcessException {
        final String serviceType = value(SERVICE_TYPE, inputParameters);
        final String identifier = value(IDENTIFIER, inputParameters);

        if (identifier == null || identifier.isEmpty()) {
            throw new ProcessException("Service instance identifier can't be null or empty.", this, null);
        }

        //unregister the service instance if exist
        if (WSEngine.serviceInstanceExist(serviceType, identifier)) {
            WSEngine.shutdownInstance(serviceType, identifier);
        }

        //delete folder
        if (!ConfigurationEngine.deleteConfiguration(serviceType, identifier)) {
            throw new ProcessException("Service instance directory " + identifier + " can't be deleted.", this, null);
        }
    }
}
