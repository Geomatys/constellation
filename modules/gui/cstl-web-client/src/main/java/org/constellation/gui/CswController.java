/*
 * Constellation - An open source and standard compliant SDI
 *      http://www.constellation-sdi.org
 *   (C) 2014, Geomatys
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details..
 */

package org.constellation.gui;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.template.Template;
import org.constellation.ServiceDef;
import org.constellation.configuration.Instance;
import org.constellation.dto.Service;
import org.constellation.generic.database.Automatic;
import org.constellation.gui.service.ConstellationService;
import org.constellation.gui.service.ServicesManager;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for specific CSW views
 *
 * @author Benjamin Garcia (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
public class CswController {

    @Inject
    private ConstellationService cstl;

    @Inject
    @Path("csw_choose_source.gtmpl")
    protected org.constellation.gui.templates.csw_choose_source csw_choose_source;

    /**
     * Manager used to call constellation server side.
     */
    @Inject
    protected ServicesManager servicesManager;

    @Inject
    @Path("csw_service.gtmpl")
    Template serviceDescription;

    /**
     * View after service creation
     *
     * @param name
     * @param description
     * @param identifier
     * @param type           service type
     * @param versionList    service version available
     * @return a {@link juzu.Response} to create view
     */
    @View
    @Route("csw/choosesource")
    public Response chooseSource(final String name, final String description, final String identifier, final String type, final List<String> versionList) {
        return csw_choose_source.with().name(name).description(description).identifier(identifier).serviceType(type).
                versions(versionList).ok().withMimeType("text/html");
    }

    /**
     * Action which configure a CSW service.
     *
     * @param name
     * @param description
     * @param identifier
     * @param type           service type
     * @param versionList    service version available
     * @return a {@link juzu.Response} to create view
     */
    @Action
    @Route("csw/configservice")
    public Response configService(final String name, final String description, final String identifier, final String type, final List<String> versionList) {
        final Automatic automatic = new Automatic();
        automatic.setFormat("internal");
        automatic.setProfile("discovery");

        try {
            servicesManager.setInstanceConfiguration(identifier, ServiceDef.Specification.CSW, automatic);
            servicesManager.startService(identifier, ServiceDef.Specification.CSW);
        }  catch (IOException ex) {
            return Response.error(ex.getLocalizedMessage());
        }

        return Controller_.succeeded(name, description, identifier, type, versionList, "true");
    }

    /**
     * Returns the view for CSW dashboard.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type. Should be csw.
     * @return the view {@link juzu.Response}
     * @throws IOException on communication error with Constellation server
     */
    @View
    @Route("editcsw/{serviceType}/{serviceId}")
    public Response dashboard(final String serviceId, final String serviceType) throws IOException {
        final Service metadata = servicesManager.getMetadata(serviceId, ServiceDef.Specification.fromShortName(serviceType));
        final Instance instance   = servicesManager.getInstance(serviceId, ServiceDef.Specification.fromShortName(serviceType));

        // Build service capabilities URL.
        String capabilitiesUrl = cstl.getUrl().toString();
        if (!capabilitiesUrl.endsWith("/")) {
            capabilitiesUrl += "/";
        }
        capabilitiesUrl += "WS/" + serviceType + "/" + serviceId + "?REQUEST=GetCapabilities&SERVICE=" + serviceType;
        if (metadata.getVersions()!=null && metadata.getVersions().size() == 1) {
            capabilitiesUrl += "&VERSION=" + metadata.getVersions().get(0);
        }

        //use parameter map (not type safe technique) because we aren't on juzu project => gtmpl aren't build.
        final Map<String, Object> parameters = new HashMap<>(0);
        parameters.put("service",         metadata);
        parameters.put("instance",        instance);
        parameters.put("startIndex",      0);
        parameters.put("nbPerPage",       10);
        parameters.put("capabilitiesUrl", capabilitiesUrl);
        parameters.put("selected",        null);
        return serviceDescription.ok(parameters).withMimeType("text/html");
    }
}
