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
package org.constellation.provider;

import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.constellation.provider.configuration.Configurator;

/**
 * Common informations for providers.
 * 
 * @author Johann Sorel (Geomatys)
 */
public abstract class Providers {
    
    /**
     * Logger used by all providers.
     */
    public static final Logger LOGGER = Logging.getLogger("org.constellation.provider");

    protected static Configurator CONFIGURATOR = Configurator.DEFAULT;

    public static Configurator getConfigurator() {
        return CONFIGURATOR;
    }

    public static synchronized void setConfigurator(Configurator configurator) {
        CONFIGURATOR = configurator;
        //clear caches
        DataProviders.getInstance().dispose();
        StyleProviders.getInstance().dispose();
    }
        
    
}
