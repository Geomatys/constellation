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
package org.constellation.ws;

import org.apache.sis.util.logging.Logging;
import org.constellation.util.ReflectionUtilities;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class WSEngine {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.ws");

    /**
     * Default value, changes nothing, just using reflection to instantiate the worker.
     */
    private static WorkerFactory workerFactory = new WorkerFactory() {

        @Override
        public Worker build(Class<? extends Worker> workerClass, String identifier) {
            return (Worker) ReflectionUtilities.newInstance(workerClass, identifier);
        }


    };

    private WSEngine() {}


    /**
     * A map of service worker.
     */
    private static final Map<String, Map<String, Worker>> WORKERS_MAP = new HashMap<>();

    /**
     * A map of the registred OGC services and their endpoint protocols (SOAP, REST).
     */
    private static final Map<String, List<String>> REGISTERED_SERVICE = new HashMap<>();

    /**
     * A map of {@link Worker} class for each OGC registred service.
     */
    private static final Map<String, Class<? extends Worker>> SERVICE_WORKER_CLASS = new HashMap<>();

    /**
     * A map of {@link ServiceConfigurer} class for eache OGC resgitred service.
     */
    private static final Map<String, Class<? extends ServiceConfigurer>> SERVICE_CONFIGURER_CLASS = new HashMap<>();

    @Deprecated
    private static final List<String> TO_RESTART = new ArrayList<>();


    public static void setWorkerFactory(WorkerFactory workerFactory) {
        WSEngine.workerFactory = workerFactory;
    }

    public static Map<String, Worker> getWorkersMap(final String specification) {
        final Map<String, Worker> result = WORKERS_MAP.get(specification.toLowerCase());
        if (result == null) {
            return new HashMap<>();
        }
        return result;
    }

    @Deprecated
    public static void prepareRestart() {
        TO_RESTART.addAll(WORKERS_MAP.keySet());
    }

    /**
     * Return the number of instances for the specified OGC service.
     *
     * @param specification the OGC service type (WMS, CSW, WFS, ...)
     * @return
     */
    public static int getInstanceSize(final String specification) {
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification.toLowerCase());
        if (workersMap != null) {
            return workersMap.size();
        }
        return 0;
    }

    public static boolean serviceInstanceExist(final String specification, final String serviceID) {
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification.toLowerCase());
        if (workersMap != null) {
            return workersMap.containsKey(serviceID);
        }
        return false;
    }

    public static Set<String> getInstanceNames(final String specification) {
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification.toLowerCase());
        if (workersMap != null) {
            return workersMap.keySet();
        }
        return new HashSet<>();
    }

    public static Worker getInstance(final String specification, final String serviceID) {
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification.toLowerCase());
        if (workersMap != null) {
            return workersMap.get(serviceID);
        }
        return null;
    }

    public static void destroyInstances(final String specification) {
        if (TO_RESTART.contains(specification.toLowerCase())) {
            TO_RESTART.remove(specification.toLowerCase());
            return;
        }
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification.toLowerCase());
        if (workersMap != null) {
            for (final Worker worker : workersMap.values()) {
                worker.destroy();
            }
            workersMap.clear();
            WORKERS_MAP.put(specification.toLowerCase(), null);
        }
    }

    public static boolean isSetService(final String specification) {
        if (TO_RESTART.contains(specification)) {
            return false;
        }
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification.toLowerCase());
        return workersMap != null;
    }

    public static void setServiceInstances(final String specification, final Map<String, Worker> instances) {
        final Map<String, Worker> oldWorkersMap = WORKERS_MAP.put(specification.toLowerCase(), instances);
        if (oldWorkersMap != null && !oldWorkersMap.isEmpty()) {
            LOGGER.info("Destroying old workers");
            for (Worker oldWorker : oldWorkersMap.values()) {
                oldWorker.destroy();
            }
        }
    }

    public static void addServiceInstance(final String specification, final String serviceID, final Worker instance) {
        Map<String, Worker> workersMap = WORKERS_MAP.get(specification.toLowerCase());
        if (workersMap == null) {
            workersMap = new HashMap<>();
            WORKERS_MAP.put(specification.toLowerCase(), workersMap);
        }
        final Worker oldWorker = workersMap.put(serviceID, instance);
        if (oldWorker != null) {
            LOGGER.log(Level.INFO, "Destroying old worker: {0}({1})", new Object[]{specification.toLowerCase(), serviceID});
            oldWorker.destroy();
        }
    }

    public static Set<Entry<String, Boolean>> getEntriesStatus(final String specification) {
        final Set<Map.Entry<String, Boolean>> response = new HashSet<>();
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification.toLowerCase());
        if (workersMap != null) {
            for (Entry<String, Worker> entry : workersMap.entrySet()) {
                response.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().isStarted()));
            }
        }
        return response;
    }

    public static void shutdownInstance(final String specification, final String serviceID) {
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification.toLowerCase());
        if (workersMap != null) {
            final Worker worker = workersMap.get(serviceID);
            if (worker != null) {
                worker.destroy();
                workersMap.remove(serviceID);
            }
        }
    }

     /**
     * Add a service type to the list of registered service if it is not already registered.
     *
     * @param specification A service type (CSW, SOS, WMS, ...).
     * @param protocol
     * @param workerClass the class binding of the service worker.
     * @param configurerClass the class binding of the service configurer
     */
    public static void registerService(final String specification, final String protocol, final Class <? extends Worker> workerClass,
        final Class<? extends ServiceConfigurer> configurerClass) {
        if (REGISTERED_SERVICE.containsKey(specification.toLowerCase())) {
            final List<String> protocols = REGISTERED_SERVICE.get(specification.toLowerCase());
            if (!protocols.contains(protocol)) {
                protocols.add(protocol);
            }
            REGISTERED_SERVICE.put(specification.toLowerCase(), protocols);
        } else {
            final List<String> protocols = new ArrayList<>();
            protocols.add(protocol);
            REGISTERED_SERVICE.put(specification.toLowerCase(), protocols);
        }
        SERVICE_WORKER_CLASS.put(specification.toLowerCase(), workerClass);
        SERVICE_CONFIGURER_CLASS.put(specification.toLowerCase(), configurerClass);
    }

    public static Map<String, List<String>> getRegisteredServices() {
        return REGISTERED_SERVICE;
    }

    /**
     * Return the {@link Worker} implementation {@link Class} of a registered OGC service.
     *
     * @param specification The OGC service type (WMS, CSW, WFS, ...).
     * @return the worker class of a registered service or null if service not registered.
     */
    private static Class<? extends Worker> getServiceWorkerClass(final String specification) {
        if (SERVICE_WORKER_CLASS.containsKey(specification.toLowerCase())) {
            return SERVICE_WORKER_CLASS.get(specification.toLowerCase());
        }
        return null;
    }

    /**
     * Instanciate a new {@link Worker} for the specified OGC service.
     *
     * @param specification The OGC service type (WMS, CSW, WFS, ...).
     * @param identifier The identifier of the new {@link Worker}.
     *
     * @return The new instancied {@link Worker}.
     */
    public static Worker buildWorker(final String specification, final String identifier) {
        final Class<? extends Worker> workerClass = getServiceWorkerClass(specification.toLowerCase());
        return workerFactory.build(workerClass, identifier);
    }

    /**
     * Returns the {@link ServiceConfigurer} implementation {@link Class} for the
     * specified service specification.
     *
     * @param specification the service specification
     * @return the {@link ServiceConfigurer} implementation {@link Class} or null if service not registered
     */
    public static Class<? extends ServiceConfigurer> getServiceConfigurerClass(final String specification) {
        if (SERVICE_CONFIGURER_CLASS.containsKey(specification.toLowerCase())) {
            return SERVICE_CONFIGURER_CLASS.get(specification.toLowerCase());
        }
        return null;
    }
}
