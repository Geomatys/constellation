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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class WSEngine {
    
    private WSEngine() {}
    
    /**
     * A map of service worker.
     */
    private static final Map<String, Map<String, Worker>> WORKERS_MAP = new HashMap<String, Map<String, Worker>>();
 
    public static Map<String, Worker> getWorkersMap(final String specification) {
        return WORKERS_MAP.get(specification);
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
        return new HashSet<String>();
    }
    
    public static Worker getInstance(final String specification, final String serviceID) {
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
        if (workersMap != null) {
            return workersMap.get(serviceID);
        }
        return null;
    }
    
    public static void destroyInstances(final String specification) {
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
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
        return workersMap != null;
    }
    
    public static void setServiceInstances(final String specification, final Map<String, Worker> instances) {
        WORKERS_MAP.put(specification, instances);
    }
    
    public static void addServiceInstance(final String specification, final String serviceID, final Worker instance) {
        Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
        if (workersMap != null) {
            workersMap = new HashMap<String, Worker>();
        }
        workersMap.put(serviceID, instance);
    }

    public static Set<Map.Entry<String, Worker>> getEntries(final String specification) {
        final Map<String, Worker> workersMap = WORKERS_MAP.get(specification);
        if (workersMap != null) {
            return workersMap.entrySet();
        }
        return new HashSet<Map.Entry<String, Worker>>();
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
}
