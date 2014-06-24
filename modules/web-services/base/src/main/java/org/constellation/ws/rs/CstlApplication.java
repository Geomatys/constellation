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
