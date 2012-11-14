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
import java.util.HashMap;
import java.util.Map;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.ws.WSEngine;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.parameter.Parameters.value;
import static org.constellation.process.service.RestartServiceDescriptor.*;
import org.constellation.util.ReflectionUtilities;
import org.constellation.ws.Worker;
import org.geotoolkit.process.ProcessFinder;
import org.opengis.util.NoSuchIdentifierException;

/**
 * Restart an instance for the specified WMS identifier. Or all WMS instances if identifier is not specified.
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
        File serviceDir = value(SERVICE_DIRECTORY, inputParameters);
        final Class clazz = WSEngine.getServiceWorkerClass(serviceName);

        //get config directory .constellation if null
        if (serviceDir == null) {
            final File configDirectory = ConfigDirectory.getConfigDirectory();

            if (configDirectory != null && configDirectory.isDirectory()) {

                serviceDir = new File(configDirectory, serviceName);

            } else {
                throw new ProcessException("Configuration directory can' be found.", this, null);
            }
        }

        if (serviceDir.isDirectory()) {

            if (identifier == null || "".equals(identifier)) {
                buildWorkers(serviceDir, serviceName, null, closeFirst, clazz);
            } else {
                if (WSEngine.serviceInstanceExist(serviceName, identifier)) {
                    buildWorkers(serviceDir, serviceName, identifier, closeFirst, clazz);
                } else {
                    //try to start service
                    try {
                        final ProcessDescriptor startDesc = ProcessFinder.getProcessDescriptor("constellation", StartServiceDescriptor.NAME);
                        final ParameterValueGroup input = StartServiceDescriptor.INPUT_DESC.createValue();
                        input.parameter(StartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
                        input.parameter(StartServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);

                        startDesc.createProcess(input).call(); // try to start
                    } catch (NoSuchIdentifierException ex) {
                        throw new ProcessException("There is no instance of " + identifier, this, null);
                    } catch (ProcessException ex) {
                        throw new ProcessException("There is no instance of " + identifier, this, null);
                    }
                }
            }

        } else {
            throw new ProcessException("Service directory can' be found for service name : " + serviceName, this, null);
        }
    }

    /**
     * Create new worker instance in service directory.
     *
     * @param serviceDir
     * @param identifier
     * @throws ProcessException
     */
    private void buildWorkers(final File serviceDir, final String serviceName, final String identifier, final boolean closeInstance, final Class clazz) throws ProcessException {

        /*
         * Single refresh
         */
        if (identifier != null) {
            if (closeInstance) {
                WSEngine.shutdownInstance(serviceName, identifier);
            }

            final File instanceDirectory = new File(serviceDir, identifier);

            if (instanceDirectory.isDirectory()) {
                if (!instanceDirectory.getName().startsWith(".")) {
                    try {
                        final Worker worker = (Worker) ReflectionUtilities.newInstance(clazz, instanceDirectory.getName(), instanceDirectory);

                        if (worker != null) {
                            WSEngine.addServiceInstance(serviceName, identifier, worker);
                            if (!worker.isStarted()) {
                                throw new ProcessException("Unable to start the instance " + identifier + ".", this, null);
                            }
                        } else {
                            throw new ProcessException("The instance " + identifier + " can be started, maybe there is no configuration directory with this name.", this, null);
                        }
                    } catch (IllegalArgumentException ex) {
                        throw new ProcessException(ex.getMessage(), this, ex);
                    }
                }
            } else {
                throw new ProcessException("Service instance directory can' be created. Check permissions.", this, null);
            }
        /*
         * Multiple refresh
         */
        } else {

            final Map<String, Worker> workersMap = new HashMap<String, Worker>();
            if (closeInstance) {
                WSEngine.destroyInstances(serviceName);
            }

            for (File instanceDir : serviceDir.listFiles()) {
                if (instanceDir.isDirectory()) {
                    final String instanceID = instanceDir.getName();
                    if (!instanceID.startsWith(".")) {
                        try {
                            final Worker worker = (Worker)  ReflectionUtilities.newInstance(clazz, instanceID, instanceDir);

                            if (worker != null) {
                                workersMap.put(instanceID, worker);
                            } else {
                                throw new ProcessException("The instance " + instanceID + " can be started, maybe there is no configuration directory with this name.", this, null);
                            }
                        } catch (IllegalArgumentException ex) {
                            throw new ProcessException(ex.getMessage(), this, ex);
                        }
                    }
                } else {
                    throw new ProcessException("Service instance directory can' be created. Check permissions.", this, null);
                }
            }
            WSEngine.setServiceInstances(serviceName, workersMap);
        }
    }
}
