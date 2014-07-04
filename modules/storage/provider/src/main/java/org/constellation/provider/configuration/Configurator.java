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

import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigurationException;
import org.opengis.parameter.ParameterValueGroup;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * @author Johann Sorel (Geomatys)
 */
public interface Configurator {

    public static final Logger LOGGER = Logging.getLogger(Configurator.class);

    /**
     * Get a list of all provider configurations.
     * @return List of entry<providerId,parameters>
     * @throws org.constellation.configuration.ConfigurationException
     * 
     *  @deprecated use getProviderInformations()
     */
    @Deprecated 
    List<Entry<String,ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException;
    
    /**
     * Get a list of all provider configurations.
     * @return List of entry<providerId,parameters>
     * @throws org.constellation.configuration.ConfigurationException
     */
    List<ProviderInformation> getProviderInformations() throws ConfigurationException;
    
    /**
     * Get configuration for one provider.
     * @param providerId 
     * @return Configuration or null
     * @throws org.constellation.configuration.ConfigurationException
     */
    ParameterValueGroup getProviderConfiguration(String providerId) throws ConfigurationException;
    
    /**
     * Store a new provider configuration.
     * @param providerId
     * @param config
     * @throws org.constellation.configuration.ConfigurationException
     */
    void addProviderConfiguration(String providerId, ParameterValueGroup config) throws ConfigurationException;
    
    /**
     * Save an existing provider updated configuration.
     * @param providerId
     * @param config
     * @throws ConfigurationException 
     */
    void updateProviderConfiguration(String providerId, ParameterValueGroup config) throws ConfigurationException;
    
    /**
     * Remove a provider configuration.
     * @param providerId
     * @throws ConfigurationException 
     */
    void removeProviderConfiguration(String providerId) throws ConfigurationException;
    
    public static class ProviderInformation {
        
        public String id;
        
        public String impl;
        
        public ParameterValueGroup config;
        
        public ProviderInformation(final String id, final String impl, final ParameterValueGroup config) {
            this.config = config;
            this.id     = id;
            this.impl   = impl;
        }
    }
}
