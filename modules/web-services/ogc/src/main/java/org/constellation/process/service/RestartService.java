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

import java.util.HashMap;
import java.util.Map;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.ws.WSEngine;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.parameter.Parameters.value;
import static org.constellation.process.service.RestartServiceDescriptor.*;
import org.constellation.ws.Worker;
import org.geotoolkit.process.ProcessFinder;
import org.opengis.util.NoSuchIdentifierException;

/**
 * Restart an instance for the specified WMS identifier. Or all instances if identifier is not specified.
 *
 * @author Quentin Boileau (Geomatys).
 */
public final class RestartService extends AbstractCstlProcess {

    public RestartService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    @Override
    protected void execute() throws ProcessException {
        final String serviceName = value(SERVICE_TYPE, inputParameters);
        final String identifier = value(IDENTIFIER, inputParameters);
        final Boolean closeFirst = value(CLOSE, inputParameters);

        if (identifier == null || "".equals(identifier)) {
            buildWorkers(serviceName, null, closeFirst);
        } else {
            if (WSEngine.serviceInstanceExist(serviceName, identifier)) {
                buildWorkers(serviceName, identifier, closeFirst);
            } else {
                //try to start service
                try {
                    final ProcessDescriptor startDesc = ProcessFinder.getProcessDescriptor("constellation", StartServiceDescriptor.NAME);
                    final ParameterValueGroup input = StartServiceDescriptor.INPUT_DESC.createValue();
                    input.parameter(StartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
                    input.parameter(StartServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);

                    startDesc.createProcess(input).call(); // try to start
                } catch (NoSuchIdentifierException | ProcessException ex) {
                    throw new ProcessException("There is no instance of " + identifier, this, null);
                }
            }
        }
    }

    /**
     * Create new worker instance in service directory.
     *
     * @param serviceDir
     * @param identifier
     * @throws ProcessException
     */
    private void buildWorkers(final String serviceType, final String identifier, final boolean closeInstance) throws ProcessException {

        /*
         * Single refresh
         */
        if (identifier != null) {
            if (closeInstance) {
                WSEngine.shutdownInstance(serviceType, identifier);
            }
            try {
                final Worker worker = WSEngine.buildWorker(serviceType, identifier);
                if (worker != null) {
                    WSEngine.addServiceInstance(serviceType, identifier, worker);
                    if (!worker.isStarted()) {
                        throw new ProcessException("Unable to start the instance " + identifier + ".", this, null);
                    }
                } else {
                    throw new ProcessException("The instance " + identifier + " can't be started, maybe there is no configuration directory with this name.", this, null);
                }
            } catch (IllegalArgumentException ex) {
                throw new ProcessException(ex.getMessage(), this, ex);
            }
            
        /*
         * Multiple refresh
         */
        } else {

            final Map<String, Worker> workersMap = new HashMap<>();
            if (closeInstance) {
                WSEngine.destroyInstances(serviceType);
            }

            for (String instanceID : ConfigurationEngine.getServiceConfigurationIds(serviceType)) {
                try {
                    final Worker worker = WSEngine.buildWorker(serviceType, instanceID);
                    if (worker != null) {
                        workersMap.put(instanceID, worker);
                    } else {
                        throw new ProcessException("The instance " + instanceID + " can be started, maybe there is no configuration directory with this name.", this, null);
                    }
                } catch (IllegalArgumentException ex) {
                    throw new ProcessException(ex.getMessage(), this, ex);
                }
            }
            WSEngine.setServiceInstances(serviceType, workersMap);
        }
    }
}
