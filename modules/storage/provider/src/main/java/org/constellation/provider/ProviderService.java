/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

import java.util.Collection;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public interface ProviderService<K,V,P extends Provider<K,V>> {

    String getName();

    /**
     * Description of the complete service parameters.
     * @return 
     */
    ParameterDescriptorGroup getServiceDescriptor();
    
    /**
     * Description of the single source in the service parameters.
     * @return 
     */
    GeneralParameterDescriptor getSourceDescriptor();
    
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
     * @param config
     * @return Provider<K,V>
     */
    P createProvider(ParameterValueGroup config);

    /**
     * Special sources might provide hard coded providers.
     * If they are some, they should be returned by this method.
     * @return never null but can be empty.
     */
    Collection<? extends P> getAdditionalProviders();

}
