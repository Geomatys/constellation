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

import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.util.ReflectionUtilities;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author Bernard Fabien (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public abstract class ServiceConfigurer {

    protected static final Logger LOGGER = Logging.getLogger(ServiceConfigurer.class);

    private static AutowireCapableBeanFactory autowirer;

    /**
     * Gets the {@link ServiceConfigurer} implementation from the service {@link Specification}.
     *
     * @param specification the service specification
     * @return the {@link ServiceConfigurer} instance
     * @throws NotRunningServiceException if the service is not registered or if the configuration
     * directory is missing
     */
    public static ServiceConfigurer newInstance(final Specification specification) throws NotRunningServiceException {
        final String serviceType = specification.name().toLowerCase();
        if (WSEngine.getRegisteredServices().get(serviceType) == null) {
            throw new NotRunningServiceException(specification);
        }
        final Class c = WSEngine.getServiceConfigurerClass(serviceType);
        ServiceConfigurer instance =  (ServiceConfigurer) ReflectionUtilities.newInstance(c);
        autowirer.autowireBean(instance);
        return instance;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        ServiceConfigurer.autowirer = applicationContext.getAutowireCapableBeanFactory();
        
    }
}
