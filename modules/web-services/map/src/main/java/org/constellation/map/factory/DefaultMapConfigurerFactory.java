/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.map.factory;

import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.factory.AbstractConfigurerFactory;
import org.constellation.map.configuration.DefaultMapConfigurer;
import org.constellation.ws.rs.ContainerNotifierImpl;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultMapConfigurerFactory extends AbstractConfigurerFactory {

    @Override
    public AbstractConfigurer getConfigurer(ContainerNotifierImpl cn) throws ConfigurationException {
        return new DefaultMapConfigurer();
    }

    @Override
    public Class getConfigurerClass() {
        return DefaultMapConfigurer.class;
    }
    
}
