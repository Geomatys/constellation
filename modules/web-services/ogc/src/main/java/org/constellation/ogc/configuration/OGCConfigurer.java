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

package org.constellation.ogc.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.ServiceBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Instance;
import org.constellation.configuration.ServiceStatus;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.Service;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.WSEngine;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Describe methods which need to be specify by an implementation to manage
 * service (create, set configuration, etc...).
 *
 * @author Benjamin Garcia (Geomatys).
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public abstract class OGCConfigurer extends ServiceConfigurer {

    @Autowired
    protected ServiceBusiness serviceBusiness;
    
    
    /**
     * Find and returns a service {@link Instance}.
     *
     * @param serviceType The type of the service.
     * @param identifier the service identifier
     * @throws TargetNotFoundException if the service with specified identifier does not exist
     * @return an {@link Instance} instance
     */
    public Instance getInstance(final String serviceType, final String identifier) throws ConfigurationException {
        org.constellation.engine.register.Service service = serviceBusiness.getServiceByIdentifierAndType(serviceType, identifier);
        final Instance instance = new Instance(service.getId(), identifier, serviceType, getInstanceStatus(serviceType, identifier));
        Service metadata = null;
        try {
            metadata = serviceBusiness.getInstanceMetadata(serviceType, identifier);
        } catch (ConfigurationException ignore) {
            // Do nothing.
        }
        instance.setIdentifier(identifier);
        if (metadata != null) {
            instance.setName(metadata.getName());
            instance.set_abstract(metadata.getDescription());
            instance.setVersions(metadata.getVersions());
        } else {
            instance.set_abstract("");
            instance.setVersions(new ArrayList<String>());
        }
        return instance;
    }

    /**
     * Returns a service instance status.
     *
     * @param serviceType
     * @param identifier the service identifier
     * @return a {@link ServiceStatus} status
     * @throws TargetNotFoundException if the service with specified identifier does not exist
     */
    public ServiceStatus getInstanceStatus(final String serviceType, final String identifier) throws TargetNotFoundException {
        serviceBusiness.ensureExistingInstance(serviceType, identifier);
        for (Map.Entry<String, Boolean> entry : WSEngine.getEntriesStatus(serviceType)) {
            if (entry.getKey().equals(identifier)) {
                return entry.getValue() ? ServiceStatus.STARTED : ServiceStatus.ERROR;
            }
        }
        return ServiceStatus.STOPPED;
    }

    /**
     * Returns all service instances (for current specification) status.
     *
     * @param spec
     * @return a {@link Map} of {@link ServiceStatus} status
     */
    public Map<String, ServiceStatus> getInstancesStatus(final String spec) {
        final Map<String, ServiceStatus> status = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : WSEngine.getEntriesStatus(spec)) {
            status.put(entry.getKey(), entry.getValue() ? ServiceStatus.STARTED : ServiceStatus.ERROR);
        }
        final List<String> serviceIDs = ConfigurationEngine.getServiceConfigurationIds(spec);
        for (String serviceID : serviceIDs) {
            if (!WSEngine.serviceInstanceExist(spec, serviceID)) {
                status.put(serviceID, ServiceStatus.STOPPED);
            }
        }
        return status;
    }

    /**
     * Returns list of service {@link Instance}(s) related to the {@link OGCConfigurer}
     * implementation.
     *
     * @param spec
     * @return the {@link Instance} list
     */
    public List<Instance> getInstances(final String spec) {
        final List<Instance> instances = new ArrayList<>();
        final Map<String, ServiceStatus> statusMap = getInstancesStatus(spec);
        for (final String key : statusMap.keySet()) {
            try {
                instances.add(getInstance(spec, key));
            } catch (ConfigurationException ignore) {
                // Do nothing.
            }
        }
        return instances;
    }
}
