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

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.parameter.Parameters;
import org.opengis.metadata.quality.ConformanceResult;
import org.opengis.parameter.ParameterValueGroup;

import java.util.logging.Logger;

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
