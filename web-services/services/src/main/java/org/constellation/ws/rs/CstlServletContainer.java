/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.ws.rs;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import javax.servlet.ServletConfig;
import org.constellation.configuration.ws.ConfigurationService;

/**
 *
 * @author Guilhem Legal
 */
public class CstlServletContainer extends ServletContainer {

    public static boolean reload = false;
    
    @Override
    protected void configure(final ServletConfig sc, ResourceConfig rc, WebApplication wa) {
        super.configure(sc, rc, wa);
        ConfigurationService configService = new ConfigurationService();
        
        if (!reload) {
            rc.getSingletons().add(configService);
        } 
        reload = true;
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_NOTIFIER, configService);
        
        
    }
}
