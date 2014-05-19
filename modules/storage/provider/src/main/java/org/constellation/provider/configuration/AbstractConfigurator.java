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
