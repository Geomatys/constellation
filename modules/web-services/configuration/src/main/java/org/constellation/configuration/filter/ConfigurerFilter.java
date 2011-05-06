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
package org.constellation.configuration.filter;

import java.util.ArrayList;
import java.util.List;
import javax.imageio.spi.ServiceRegistry;
import org.constellation.configuration.factory.AbstractConfigurerFactory;

/**
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class ConfigurerFilter implements ServiceRegistry.Filter{

    private final List<String> alreadyUsed = new ArrayList<String>();
            
    @Override
    public boolean filter(Object provider) {
        AbstractConfigurerFactory factory = (AbstractConfigurerFactory) provider;
        Class c = factory.getConfigurerClass();
        Class specificRoot = null;
        while (c != null && !c.getName().equals("org.constellation.configuration.AbstractConfigurer")) {
            specificRoot = c;
            c = c.getSuperclass();
        }
        if (alreadyUsed.contains(specificRoot.getName())) {
            return false;
        } else {
            alreadyUsed.add(specificRoot.getName());
            return true;
        }
    }
    
}
