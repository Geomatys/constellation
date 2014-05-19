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
package org.constellation.provider;

import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Creates new providers of a specific type.
 * 
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public interface ProviderFactory<K,V,P extends Provider<K,V>> {

    /**
     * Factory format name.
     * @return String , never null
     */
    String getName();
    
    /**
     * Description of a provider parameters.
     * @return 
     */
    ParameterDescriptorGroup getProviderDescriptor();
    
    /**
     * Description of the datasource in the provider parameters.
     * @return ParameterDescriptorGroup , never null
     */
    GeneralParameterDescriptor getStoreDescriptor();
    
    /**
     * Check if the given parameter can be handle
     * 
     * @param params
     * @return true if the given parameter can be processed
     */
    boolean canProcess(final ParameterValueGroup params);
    
    /**
     * Create a provider for the given configuration.
     *
     * @param id
     * @param config
     * @return Provider<K,V>
     */
    P createProvider(String id, ParameterValueGroup config);

}
