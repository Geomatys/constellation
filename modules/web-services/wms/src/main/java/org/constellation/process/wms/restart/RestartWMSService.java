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
package org.constellation.process.wms.restart;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.constellation.ServiceDef;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.map.ws.DefaultWMSWorker;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.ws.WSEngine;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.parameter.Parameters.value;
import static org.constellation.process.wms.restart.RestartWMSServiceDescriptor.*;
import org.constellation.ws.Worker;

/**
 * Restart an instance for the specified WMS identifier. Or all WMS instances if identifier is not specified.
 *
 * @author Quentin Boileau (Geomatys).
 */
public final class RestartWMSService extends AbstractCstlProcess {

    public RestartWMSService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    @Override
    protected void execute() throws ProcessException {
        final String identifier = value(IDENTIFIER, inputParameters);
        final Boolean closeFirst = value(CLOSE, inputParameters);

        final File configDirectory = ConfigDirectory.getConfigDirectory();
        if (configDirectory != null && configDirectory.isDirectory()) {
            
            final String serviceName = ServiceDef.WMS_1_3_0.specification.name();
            final File serviceDir = new File(configDirectory, serviceName);
            if (serviceDir.exists() && serviceDir.isDirectory()) {

                if (identifier == null || "".equals(identifier)) {
                    buildWorkers(serviceDir, null, closeFirst);
                } else {
                    if (WSEngine.serviceInstanceExist(serviceName, identifier)) {
                        buildWorkers(serviceDir, identifier, closeFirst);
                    } else {
                        throw new ProcessException("There is no instance of" + identifier, this, null);
                    }
                }

            } else {
                throw new ProcessException("Service directory can' be found for service name : " + serviceName, this, null);
            }
        } else {
            throw new ProcessException("Configuration directory can' be found.", this, null);
        }
    }

    /**
     * Create new worker instance in service directory.
     *
     * @param serviceDir
     * @param identifier
     * @throws ProcessException
     */
    private void buildWorkers(final File serviceDir, final String identifier, final boolean closeInstance) throws ProcessException {

        if (identifier != null) {
            if (closeInstance) {
                WSEngine.shutdownInstance(serviceDir.getName(), identifier);
            }
            
            final File instanceDirectory = new File(serviceDir, identifier);

            if (instanceDirectory.exists() && serviceDir.isDirectory()) {
                final DefaultWMSWorker worker = new DefaultWMSWorker(identifier, instanceDirectory);
                if (worker != null) {
                    WSEngine.addServiceInstance(serviceDir.getName(), identifier, worker);
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
            
            final Map<String, Worker> oldWorkersMap = new HashMap<String, Worker>(WSEngine.getWorkersMap(serviceDir.getName())); 
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
                        
                        if (oldWorkersMap.containsKey(instanceDir.getName())) {
                            final DefaultWMSWorker worker = new DefaultWMSWorker(identifier, instanceDir);
                            if (worker != null) {
                                workersMap.put(instanceDir.getName(), worker);
                            } else {
                                throw new ProcessException("The instance " + identifier + " can be started, maybe there is no configuration directory with this name.", this, null);
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
