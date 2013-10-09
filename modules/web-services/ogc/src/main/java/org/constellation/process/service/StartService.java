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
package org.constellation.process.service;

import java.io.File;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.ws.WSEngine;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.parameter.Parameters.value;
import static org.constellation.process.service.StartServiceDescriptor.*;
import org.constellation.util.ReflectionUtilities;
import org.constellation.ws.Worker;
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
            final Class workerClass   = WSEngine.getServiceWorkerClass(serviceType);
            final Worker worker = (Worker) ReflectionUtilities.newInstance(workerClass, identifier);

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
