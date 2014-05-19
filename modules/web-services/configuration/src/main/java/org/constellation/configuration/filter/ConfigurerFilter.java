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
