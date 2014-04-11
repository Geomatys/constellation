/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.configuration;

import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef.Specification;
import org.constellation.util.ReflectionUtilities;
import org.constellation.ws.WSEngine;

/**
 * @author Bernard Fabien (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public abstract class ServiceConfigurer {

    protected static final Logger LOGGER = Logging.getLogger(ServiceConfigurer.class);

    /**
     * Gets the {@link ServiceConfigurer} implementation from the service {@link Specification}.
     *
     * @param specification the service specification
     * @return the {@link ServiceConfigurer} instance
     * @throws NotRunningServiceException if the service is not registered or if the configuration
     * directory is missing
     */
    public static ServiceConfigurer newInstance(final Specification specification) throws NotRunningServiceException {
        if (WSEngine.getRegisteredServices().get(specification.name()) == null) {
            throw new NotRunningServiceException(specification);
        }
        final Class c = WSEngine.getServiceConfigurerClass(specification.name());
        return (ServiceConfigurer) ReflectionUtilities.newInstance(c);
    }


    /**
     * Service to configure specification.
     */
    protected final Specification specification;

    /**
     * Configuration object class.
     */
    protected final Class configClass;

    /**
     * Create a new {@link ServiceConfigurer} instance.
     *
     * @param specification  the target service specification
     * @param configClass    the target service config class
     * @param configFileName the target service config file name
     */
    protected ServiceConfigurer(final Specification specification, final Class configClass, final String configFileName) {
        this.specification  = specification;
        this.configClass    = configClass;
    }
}
