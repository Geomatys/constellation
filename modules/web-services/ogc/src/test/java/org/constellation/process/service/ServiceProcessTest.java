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
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.process.AbstractProcessTest;
import org.constellation.util.ReflectionUtilities;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.junit.AfterClass;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class ServiceProcessTest extends AbstractProcessTest {

    protected static File configDirectory;
    protected static String serviceName;
    private static Class workerClass;

    public ServiceProcessTest(final String str, final String serviceName, final Class workerClass) {
        super(str);
        ServiceProcessTest.serviceName     = serviceName;
        ServiceProcessTest.workerClass     = workerClass;
        configDirectory.mkdir();
        ConfigDirectory.setConfigDirectory(configDirectory);
        
        WSEngine.registerService(serviceName, "REST", workerClass, null);
    }

    @AfterClass
    public static void destroyFolder() {
        WSEngine.destroyInstances(serviceName);
        ConfigurationEngine.clearDatabase();
    }

    /**
     * Create a default instance of service.
     * @param identifier
     */
    protected abstract void createInstance(String identifier);

    /**
     * Check if an service instance exist.
     * @param identifier
     * @return
     */
    protected abstract boolean checkInstanceExist(final String identifier);

    protected static void deleteInstance(String identifier) {
        ConfigurationEngine.deleteConfiguration(serviceName, identifier);
        if (WSEngine.getWorkersMap(serviceName) != null) {
            WSEngine.getWorkersMap(serviceName).remove(identifier);
        }
    }

    protected static void startInstance(String identifier) {
        try {
            final Worker worker = (Worker) ReflectionUtilities.newInstance(workerClass, identifier);
            if (worker != null) {
                WSEngine.addServiceInstance(serviceName, identifier, worker);
            }
        } catch (Exception ex) {

        }
    }

    public static void setServiceName(String serviceName) {
        ServiceProcessTest.serviceName = serviceName;
    }
}
