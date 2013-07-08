/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.gui;

import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.template.Template;
import org.constellation.dto.Service;
import org.constellation.gui.service.WMSManager;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * WMS service controller. To manage all specific service operations
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
public class WMSController {

    @Inject
    private WMSManager wmsManager;

    /**
     * root wms service page
     */
    @Inject
    @Path("wmscreate.gtmpl")
    Template index;

    @Inject
    @Path("servicedescription.gtmpl")
    Template serviceDescription;

    /**
     * Generate wms service main page
     * @return a {@link Response} with right mime type
     * @throws IOException
     */
    @View
    @Route("/create/wms")
    public Response index() throws IOException {
        return index.ok().withMimeType("text/html");
    }

    /**
     * generate wms service editon page
     * @param serviceName service name
     * @return a {@link Response} with right mime type
     * @throws IOException
     */
    @View
    @Route("edit/wms/{serviceName}")
    public Response editWMS(String serviceName) throws IOException{
        Service service = wmsManager.getServiceMetadata(serviceName, "WMS");

        //use parameter map (not type safe technique) because we aren't on juzu projet => gtmpl aren't build.
        Map<String, Object> parameters = new HashMap<String, Object>(0);
        parameters.put("service", service);
        return serviceDescription.ok(parameters).withMimeType("text/html");
    }

}
