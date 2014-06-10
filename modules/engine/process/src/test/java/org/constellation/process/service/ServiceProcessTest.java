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

import java.util.UUID;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.process.AbstractProcessTest;
import org.constellation.util.ReflectionUtilities;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class ServiceProcessTest extends AbstractProcessTest {

    private static String configName;
    protected static String serviceName;
    private static Class workerClass;

    public ServiceProcessTest(final String str, final String serviceName, final Class workerClass) {
        super(str);
        ServiceProcessTest.serviceName     = serviceName;
        ServiceProcessTest.workerClass     = workerClass;
        WSEngine.registerService(serviceName, "REST", workerClass, null);
    }

    @BeforeClass
    public static void setEnvironement() {
        configName = UUID.randomUUID().toString();
        ConfigurationEngine.setupTestEnvironement(configName);
    }

    @AfterClass
    public static void destroyFolder() {
        WSEngine.destroyInstances(serviceName);
        ConfigurationEngine.shutdownTestEnvironement(configName);
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
