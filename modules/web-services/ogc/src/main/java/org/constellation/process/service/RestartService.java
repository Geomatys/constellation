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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import org.constellation.ws.Worker;

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

        if (serviceDir.exists() && serviceDir.isDirectory()) {

            if (identifier == null || "".equals(identifier)) {
                buildWorkers(serviceDir, serviceName, null, closeFirst, clazz);
            } else {
                if (WSEngine.serviceInstanceExist(serviceName, identifier)) {
                    buildWorkers(serviceDir, serviceName, identifier, closeFirst, clazz);
                } else {
                    throw new ProcessException("There is no instance of" + identifier, this, null);
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

        if (identifier != null) {
            if (closeInstance) {
                WSEngine.shutdownInstance(serviceDir.getName(), identifier);
            }

            final File instanceDirectory = new File(serviceDir, identifier);

            if (instanceDirectory.exists() && serviceDir.isDirectory()) {
                if (!instanceDirectory.getName().startsWith(".")) {
                    try {
                        final Constructor constructor = clazz.getConstructor(String.class, File.class);

                        Worker worker = (Worker) constructor.newInstance(instanceDirectory.getName(), instanceDirectory);

                        if (worker != null) {
                            WSEngine.addServiceInstance(serviceDir.getName(), identifier, worker);
                            if (!worker.isStarted()) {
                                throw new ProcessException("Unable to start the instance " + identifier + ".", this, null);
                            }
                        } else {
                            throw new ProcessException("The instance " + identifier + " can be started, maybe there is no configuration directory with this name.", this, null);
                        }
                    } catch (NoSuchMethodException ex) {
                        throw new ProcessException(null, this, ex);
                    } catch (SecurityException ex) {
                        throw new ProcessException(null, this, ex);
                    } catch (InstantiationException ex) {
                        throw new ProcessException(null, this, ex);
                    } catch (IllegalAccessException ex) {
                        throw new ProcessException(null, this, ex);
                    } catch (IllegalArgumentException ex) {
                        throw new ProcessException(null, this, ex);
                    } catch (InvocationTargetException ex) {
                        throw new ProcessException(null, this, ex);
                    }
                }
            } else {
                throw new ProcessException("Service instance directory can' be created. Check permissions.", this, null);
            }

        } else {

            Map<String, Worker> oldWorkersMap = null;
            if (WSEngine.getWorkersMap(serviceName) != null ) {
                oldWorkersMap = new HashMap<String, Worker>(WSEngine.getWorkersMap(serviceName));
            }
            final Map<String, Worker> workersMap = new HashMap<String, Worker>();

            if (closeInstance) {
                WSEngine.destroyInstances(serviceDir.getName());
            }

            if (oldWorkersMap != null && !oldWorkersMap.isEmpty()) {
                for (File instanceDir : serviceDir.listFiles()) {
                    /*
                    * For each sub-directory we build a new Worker only if his already have an instance.
                    */
                    if (instanceDir.isDirectory()) {
                        if (!instanceDir.getName().startsWith(".")) {
                            if (oldWorkersMap.containsKey(instanceDir.getName())) {

                                try {
                                    final Constructor constructor = clazz.getConstructor(String.class, File.class);

                                    Worker worker = (Worker) constructor.newInstance(identifier, instanceDir);

                                    if (worker != null) {
                                        workersMap.put(instanceDir.getName(), worker);
                                    } else {
                                        throw new ProcessException("The instance " + identifier + " can be started, maybe there is no configuration directory with this name.", this, null);
                                    }
                                } catch (NoSuchMethodException ex) {
                                    throw new ProcessException(null, this, ex);
                                } catch (SecurityException ex) {
                                    throw new ProcessException(null, this, ex);
                                } catch (InstantiationException ex) {
                                    throw new ProcessException(null, this, ex);
                                } catch (IllegalAccessException ex) {
                                    throw new ProcessException(null, this, ex);
                                } catch (IllegalArgumentException ex) {
                                    throw new ProcessException(null, this, ex);
                                } catch (InvocationTargetException ex) {
                                    throw new ProcessException(null, this, ex);
                                }
                            }
                        }
                    } else {
                        throw new ProcessException("Service instance directory can' be created. Check permissions.", this, null);
                    }
                }
                WSEngine.setServiceInstances(serviceDir.getName(), workersMap);
            }
        }
    }
}
