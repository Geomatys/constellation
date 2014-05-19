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

import org.constellation.process.AbstractCstlProcess;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;

import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.parameter.Parameters.value;
import static org.constellation.process.service.StartServiceDescriptor.*;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public final class StartService extends AbstractCstlProcess {

    public StartService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    @Override
    protected void execute() throws ProcessException {
        final String identifier = value(IDENTIFIER, inputParameters);
        final String serviceType = value(SERVICE_TYPE, inputParameters);

        if (identifier == null || identifier.isEmpty()) {
            throw new ProcessException("Service instance identifier can't be null or empty.", this, null);
        }
        
        try {
            final Worker worker = WSEngine.buildWorker(serviceType, identifier);
            if (worker != null) {
                WSEngine.addServiceInstance(serviceType, identifier, worker);
                if (!worker.isStarted()) {
                    throw new ProcessException("Unable to start the instance " + identifier + ".", this, null);
                }
            } else {
                throw new ProcessException("The instance " + identifier + " can not be instanciated.", this, null);
            }
        } catch (IllegalArgumentException ex) {
            throw new ProcessException(ex.getMessage(), this, ex);
        }
    }
}
