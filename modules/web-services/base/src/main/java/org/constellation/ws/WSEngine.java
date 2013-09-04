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
package org.constellation.ws;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ServiceConfigurer;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class WSEngine {

    private WSEngine() {}

    private static final Logger LOGGER = Logging.getLogger(WSEngine.class);

    /**
     * A map of service worker.
     */
    private static final Map<String, Map<String, Worker>> WORKERS_MAP = new HashMap<>();

    private static final Map<String, List<String>> REGISTERED_SERVICE = new HashMap<>();

    private static final Map<String, Class> SERVICE_WORKER_CLASS = new HashMap<>();

    private static final Map<String, Class<? extends ServiceConfigurer>> SERVICE_CONFIGURER_CLASS = new HashMap<>();

    private static final List<String> TO_RESTART = new ArrayList<>();

    public static Map<String, Worker> getWorkersMap(final String specification) {
        return WORKERS_MAP.get(specification);
    }

    public static void prepareRestart() {
        TO_RESTART.addAll(WORKERS_MAP.keySet());
    }

    public static int getInstanceSize(final String specification) {
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
        if (workersMap != null) {
            return workersMap.size();
        }
        return 0;
    }

    public static boolean serviceInstanceExist(final String specification, final String serviceID) {
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
        if (workersMap != null) {
            return workersMap.containsKey(serviceID);
        }
        return false;
    }

    public static Set<String> getInstanceNames(final String specification) {
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
        if (workersMap != null) {
            return workersMap.keySet();
        }
        return new HashSet<>();
    }

    public static Worker getInstance(final String specification, final String serviceID) {
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
        if (workersMap != null) {
            return workersMap.get(serviceID);
        }
        return null;
    }

    public static void destroyInstances(final String specification) {
        if (TO_RESTART.contains(specification)) {
            TO_RESTART.remove(specification);
            return;
        }
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
        if (workersMap != null) {
            for (final Worker worker : workersMap.values()) {
                worker.destroy();
            }
            workersMap.clear();
            WORKERS_MAP.put(specification, null);
        }
    }

    public static boolean isSetService(final String specification) {
        if (TO_RESTART.contains(specification)) {
            return false;
        }
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
        return workersMap != null;
    }

    public static void setServiceInstances(final String specification, final Map<String, Worker> instances) {
        final Map<String, Worker> oldWorkersMap = WORKERS_MAP.put(specification, instances);
        if (oldWorkersMap != null && !oldWorkersMap.isEmpty()) {
            LOGGER.info("Destroying old workers");
            for (Worker oldWorker : oldWorkersMap.values()) {
                oldWorker.destroy();
            }
        }
    }

    public static void addServiceInstance(final String specification, final String serviceID, final Worker instance) {
        Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
        if (workersMap == null) {
            workersMap = new HashMap<>();
            WORKERS_MAP.put(specification, workersMap);
        }
        final Worker oldWorker = workersMap.put(serviceID, instance);
        if (oldWorker != null) {
            LOGGER.info("Destroying old worker");
            oldWorker.destroy();
        }
    }

    public static Set<Entry<String, Boolean>> getEntriesStatus(final String specification) {
        final Set<Map.Entry<String, Boolean>> response = new HashSet<>();
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
        if (workersMap != null) {
            for (Entry<String, Worker> entry : workersMap.entrySet()) {
                response.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().isStarted()));
            }
        }
        return response;
    }

    public static void shutdownInstance(final String specification, final String serviceID) {
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
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
     * @param serviceName A service type (CSW, SOS, WMS, ...).
     * @param protocol
     * @param workerClass the class binding of the service worker.
      * @param workerClass the class binding of the service configurer
     */
    public static void registerService(final String serviceName, final String protocol, final Class workerClass,
        final Class<? extends ServiceConfigurer> configurerClass) {
        if (REGISTERED_SERVICE.containsKey(serviceName)) {
            final List<String> protocols = REGISTERED_SERVICE.get(serviceName);
            if (!protocols.contains(protocol)) {
                protocols.add(protocol);
            }
            REGISTERED_SERVICE.put(serviceName, protocols);
        } else {
            final List<String> protocols = new ArrayList<>();
            protocols.add(protocol);
            REGISTERED_SERVICE.put(serviceName, protocols);
        }
        SERVICE_WORKER_CLASS.put(serviceName, workerClass);
        SERVICE_CONFIGURER_CLASS.put(serviceName, configurerClass);
    }

    public static Map<String, List<String>> getRegisteredServices() {
        return REGISTERED_SERVICE;
    }

    /**
     * Return the worker class of a registered service.
     * @param serviceName
     * @return the worker class of a registered service or null if service not registered.
     */
    public static Class getServiceWorkerClass(final String serviceName) {
        if (SERVICE_WORKER_CLASS.containsKey(serviceName)) {
            return SERVICE_WORKER_CLASS.get(serviceName);
        }
        return null;
    }

    /**
     * Returns the {@link ServiceConfigurer} implementation {@link Class} for the
     * specified service specification.
     *
     * @param specification the service specification
     * @return the {@link ServiceConfigurer} implementation {@link Class} or null if service not registered
     */
    public static Class<? extends ServiceConfigurer> getServiceConfigurerClass(final String specification) {
        if (SERVICE_CONFIGURER_CLASS.containsKey(specification)) {
            return SERVICE_CONFIGURER_CLASS.get(specification);
        }
        return null;
    }
}
