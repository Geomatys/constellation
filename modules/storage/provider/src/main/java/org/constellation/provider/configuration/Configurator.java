/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010-2014, Geomatys
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
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigurationException;
import org.opengis.parameter.ParameterValueGroup;

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
