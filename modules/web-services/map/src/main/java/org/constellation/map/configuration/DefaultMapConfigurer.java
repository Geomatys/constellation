/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.map.configuration;

import javax.ws.rs.core.MultivaluedMap;
import org.constellation.configuration.AbstractConfigurer;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.ws.CstlServiceException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultMapConfigurer extends AbstractConfigurer {

    @Override
    public Object treatRequest(String request, MultivaluedMap<String, String> parameters) throws CstlServiceException {
        return null;
    }
    
    @Override
    public void beforeRestart() {
        StyleProviderProxy.getInstance().dispose();
        LayerProviderProxy.getInstance().dispose();
    }
    
}
