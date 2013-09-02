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
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.template.Template;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.Instance;
import org.constellation.configuration.LayerList;
import org.constellation.dto.Service;
import org.constellation.gui.service.ConstellationService;
import org.constellation.gui.service.ServicesManager;
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
 */
public class WMSController {

    @Inject
    private ConstellationService cstl;

    @Inject
    private ServicesManager servicesManager;

    @Inject
    private WMSManager wmsManager;

    /**
     * root wms service page
     */
    @Inject
    @Path("wms_create.gtmpl")
    Template index;

    @Inject
    @Path("wms.gtmpl")
    Template serviceDescription;


    /**
     * Generate wms service main page.
     *
     * @return the view {@link Response}
     */
    @View
    @Route("/create/wms")
    public Response index() {
        return index.ok().withMimeType("text/html");
    }

    /**
     * Generate WMS service edition page.
     *
     * @param serviceId the service identifier
     * @return the view {@link Response}
     * @throws IOException on communication error with Constellation server
     */
    @View
    @Route("edit/wms/{serviceId}")
    public Response editWMS(String serviceId) throws IOException {
        final Service metadata   = servicesManager.getMetadata(serviceId, Specification.WMS);
        final Instance instance  = servicesManager.getInstance(serviceId, Specification.WMS);
        final LayerList layers   = wmsManager.getLayers(serviceId);

        // Build service capabilities URL.
        String capabilitiesUrl = cstl.getUrl() + "WS/wms/" + serviceId +"?REQUEST=GetCapabilities&SERVICE=WMS";
        if (metadata.getVersions().size() == 1) {
            capabilitiesUrl += "&VERSION=" + metadata.getVersions().get(0);
        }

        //use parameter map (not type safe technique) because we aren't on juzu project => gtmpl aren't build.
        final Map<String, Object> parameters = new HashMap<>(0);
        parameters.put("service", metadata);
        parameters.put("layers", layers);
        parameters.put("instance", instance);
        parameters.put("capabilitiesUrl", capabilitiesUrl);
        return serviceDescription.ok(parameters).withMimeType("text/html");
    }
}
