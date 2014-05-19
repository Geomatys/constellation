/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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


package org.constellation.provider.configuration;

import java.util.List;
import java.util.Map;
import org.constellation.configuration.ConfigurationException;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Abstract configurator.
 * Falls back on getProviderConfigurations and is not writable
 * 
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractConfigurator implements Configurator {

    @Override
    public ParameterValueGroup getProviderConfiguration(String providerId) throws ConfigurationException {
        List<Map.Entry<String, ParameterValueGroup>> all = getProviderConfigurations();
        for(Map.Entry<String, ParameterValueGroup> entry : all){
            if(entry.getKey().equals(providerId)){
                return entry.getValue();
            }
        }
        throw new ConfigurationException("Provider for id : "+providerId+" does not exist");
    }

    @Override
    public void addProviderConfiguration(String providerId, ParameterValueGroup config) throws ConfigurationException {
        throw new ConfigurationException("Configurator does not support edition");
    }

    @Override
    public void updateProviderConfiguration(String providerId, ParameterValueGroup config) throws ConfigurationException {
        throw new ConfigurationException("Configurator does not support edition");
    }

    @Override
    public void removeProviderConfiguration(String providerId) throws ConfigurationException {
        throw new ConfigurationException("Configurator does not support edition");
    }
    
}
