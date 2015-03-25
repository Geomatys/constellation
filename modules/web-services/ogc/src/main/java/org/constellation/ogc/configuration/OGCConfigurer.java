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
import java.util.List;
import java.util.Map;

import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Instance;
import org.constellation.configuration.ServiceStatus;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.Details;
import org.constellation.engine.register.jooq.tables.pojos.Service;
import org.constellation.ws.ServiceConfigurer;
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
    protected IServiceBusiness serviceBusiness;
    
    
    /**
     * Find and returns a service {@link Instance}.
     *
     * @param serviceType The type of the service.
     * @param identifier the service identifier
     * @throws TargetNotFoundException if the service with specified identifier does not exist
     * @return an {@link Instance} instance
     */
    public Instance getInstance(final String serviceType, final String identifier) throws ConfigurationException {
        Service service = serviceBusiness.getServiceByIdentifierAndType(serviceType, identifier);
        final Instance instance = new Instance(service.getId(), identifier, serviceType, ServiceStatus.valueOf(service.getStatus()));
        Details details = null;
        try {
            details = serviceBusiness.getInstanceDetails(serviceType, identifier, null);
        } catch (ConfigurationException ignore) {
            // Do nothing.
        }
        if (details != null) {
            instance.setName(details.getName());
            instance.set_abstract(details.getDescription());
            instance.setVersions(details.getVersions());
        } else {
            instance.set_abstract("");
            instance.setVersions(new ArrayList<String>());
        }
        return instance;
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
        final Map<String, ServiceStatus> statusMap = serviceBusiness.getStatus(spec);
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
