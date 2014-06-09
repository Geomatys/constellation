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
package org.constellation.webdav;

import javax.inject.Singleton;
import java.util.logging.Level;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.constellation.ServiceDef;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;
import org.constellation.ws.rs.OGCWebService;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Path("webdav/{serviceId}")
@Singleton
public class AdminWebdavService extends OGCWebService<WebdavWorker> {

    /**
     * Build a new Restful CSW service.
     */
    public AdminWebdavService() {
        super(ServiceDef.Specification.WEBDAV);
        LOGGER.log(Level.INFO, "Webdav (Admin) REST service running ({0} instances)\n", getWorkerMapSize());
    }

    @Override
    protected Class getWorkerClass() {
        return WebdavWorker.class;
    }

    @Override
    protected Class<? extends ServiceConfigurer> getConfigurerClass() {
        return WebDavConfigurer.class;
    }

    @Override
    protected Response treatIncomingRequest(Object objectRequest, WebdavWorker worker) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    protected Response processExceptionResponse(CstlServiceException ex, ServiceDef serviceDef, Worker w) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
