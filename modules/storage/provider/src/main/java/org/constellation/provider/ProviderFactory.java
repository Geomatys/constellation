/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2014, Geomatys
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
