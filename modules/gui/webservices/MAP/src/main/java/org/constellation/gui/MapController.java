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
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.Instance;
import org.constellation.configuration.LayerList;
import org.constellation.dto.Service;
import org.constellation.gui.service.ConstellationService;
import org.constellation.gui.service.MapManager;
import org.constellation.gui.service.ServicesManager;

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
public class MapController {

    @Inject
    private ConstellationService cstl;

    @Inject
    private ServicesManager servicesManager;

    @Inject
    private MapManager mapManager;

    /**
     * root wms service page
     */
    @Inject
    @Path("create_map_service.gtmpl")
    Template create;

    @Inject
    @Path("map_service.gtmpl")
    Template serviceDescription;


    /**
     * Generate wms service main page.
     *
     * @return the view {@link juzu.Response}
     */
    @View
    @Route("/create/{serviceType}")
    public Response index(String serviceType) {
        final Map<String, Object> parameters = new HashMap<>(0);
        parameters.put("serviceType", serviceType);
        return create.ok(parameters).withMimeType("text/html");
    }

    /**
     * Generate WMS service edition page.
     *
     * @param serviceId the service identifier
     * @return the view {@link juzu.Response}
     * @throws java.io.IOException on communication error with Constellation server
     */
    @View
    @Route("edit/{serviceType}/{serviceId}")
    public Response editMapService(String serviceId, String serviceType) throws IOException {
        final Service metadata   = servicesManager.getMetadata(serviceId, Specification.fromShortName(serviceType));
        final Instance instance  = servicesManager.getInstance(serviceId, Specification.fromShortName(serviceType));
        final LayerList layers   = mapManager.getLayers(serviceId);

        // Build service capabilities URL.
        String capabilitiesUrl = cstl.getUrl() + "WS/"+serviceType+"/" + serviceId +"?REQUEST=GetCapabilities&SERVICE="+Specification.fromShortName(serviceType).name();
        if (metadata.getVersions()!=null && metadata.getVersions().size() == 1) {
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
