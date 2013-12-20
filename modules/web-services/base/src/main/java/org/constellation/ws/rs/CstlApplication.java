/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.imageio.spi.ServiceRegistry;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * This class is here to initiate the webservice at startup instead of waiting for the first request to instanciate the service.
 * @author Guilhem Legal (Geomatys)
 */
@ApplicationPath("/")
public class CstlApplication extends Application {

    private final Set<Object> singletons = new HashSet<>();

    public CstlApplication() {
        final Iterator<WebService> ite = ServiceRegistry.lookupProviders(WebService.class);
        while (ite.hasNext()) {
            WebService ws = ite.next();
            singletons.add(ws);
        }
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }


    @PreDestroy
    public void destroy() {
        for (Object o : singletons) {
            final WebService ws = (WebService) o;
            ws.destroy();
        }
    }
}
