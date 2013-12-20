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
package org.constellation.webdav;

import javax.inject.Singleton;
import java.util.logging.Level;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.constellation.ServiceDef;
import org.constellation.configuration.ServiceConfigurer;
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
