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

import java.util.logging.Logger;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.parameter.Parameters;
import org.apache.sis.util.logging.Logging;
import org.opengis.metadata.quality.ConformanceResult;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractProviderFactory<K,V,P extends Provider<K,V>> implements ProviderFactory<K,V,P> {
   
    private static final Logger LOGGER = Logging.getLogger("org.constellation.provider");

    private final String name;

    protected AbstractProviderFactory(String name){
        this.name = name;
    }

    protected static Logger getLogger() {
        return LOGGER;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean canProcess(final ParameterValueGroup params) {
        final ConformanceResult result = Parameters.isValid(params, getProviderDescriptor());
        return (result != null) && Boolean.TRUE.equals(result.pass());
    }
    
}
