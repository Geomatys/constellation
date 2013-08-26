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
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.apache.commons.lang.StringUtils;
import org.constellation.configuration.LayerList;
import org.constellation.dto.Service;
import org.constellation.gui.service.ConstellationService;
import org.constellation.gui.service.WMSManager;
import org.constellation.ws.rs.ServiceType;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    private ConstellationService cstl;

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
     */
    @View
    @Route("edit/wms/{serviceId}")
    public Response editWMS(String serviceId) throws IOException {
        Service service = wmsManager.getServiceMetadata(serviceId, ServiceType.WMS);
        LayerList layers = wmsManager.getLayers(serviceId, "WMS");
        String capabilitiesUrl = cstl.getUrl() + "WS/wms/" + serviceId +"?REQUEST=GetCapabilities&SERVICE=WMS";
        if (service.getVersions().size() == 1) {
            capabilitiesUrl += "&VERSION=" + service.getVersions().get(0);
        }

        //use parameter map (not type safe technique) because we aren't on juzu projet => gtmpl aren't build.
        Map<String, Object> parameters = new HashMap<String, Object>(0);
        parameters.put("service", service);
        parameters.put("layers", layers);
        parameters.put("capabilitiesUrl", capabilitiesUrl);
        return serviceDescription.ok(parameters).withMimeType("text/html");
    }

    /**
     * Reloads the WMS service with the specified identifier.
     *
     * @param serviceId the service identifier
     * @return a status {@link Response}
     */
    @Ajax
    @Resource
    @Route("reload/wms")
    public Response reloadWMS(final String serviceId) {
        return wmsManager.restartService(serviceId, "WMS") ? Response.status(200) : Response.status(500);
    }

    /**
     * Stops the WMS service with the specified identifier.
     *
     * @param serviceId the service identifier
     * @return a status {@link Response}
     */
    @Ajax
    @Resource
    @Route("stop/wms")
    public Response stopWMS(final String serviceId) {
        return wmsManager.stopService(serviceId, "WMS") ? Response.status(200) : Response.status(500);
    }

    /**
     * Stars the WMS service with the specified identifier.
     *
     * @param serviceId the service identifier
     * @return a status {@link Response}
     */
    @Ajax
    @Resource
    @Route("start/wms")
    public Response startWMS(final String serviceId) {
        return wmsManager.startService(serviceId, "WMS") ? Response.status(200) : Response.status(500);
    }

    /**
     * Updates the WMS service description.
     *
     * @param name        the (new) service name
     * @param identifier  the (new) service identifier
     * @param keywords    the (new) service keywords
     * @param description the (new) service description
     * @param v111        the (new) service v111 state
     * @param v130        the (new) service v130 state
     * @return a status {@link Response}
     */
    @Ajax
    @Resource
    @Route("update/description/wms")
    public Response updateDescription(final String name,
                                      final String identifier,
                                      final String keywords,
                                      final String description,
                                      final String v111,
                                      final String v130) throws IOException {

        final Service service = wmsManager.getServiceMetadata(identifier, ServiceType.WMS);
        service.setName(name);
        service.setKeywords(Arrays.asList(keywords.split(",")));
        service.setDescription(description);
        
        final List<String> kwds = new ArrayList<>();
        if (!StringUtils.isBlank(keywords)) {
            kwds.addAll(Arrays.asList(keywords.split(",")));
        }
        service.setKeywords(kwds);
        
        final List<String> versions = new ArrayList<>();
        if (v111 != null) {
            versions.add(v111);    
        }
        if (v130 != null) {
            versions.add(v130);
        }
        service.setVersions(versions);

        return wmsManager.setServiceMetadata(service, ServiceType.WMS) ? Response.status(200) : Response.status(500);
    }
}
