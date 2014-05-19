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
 * limitations under the License..
 */

package org.constellation.gui;

import juzu.Action;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.constellation.ServiceDef;
import org.constellation.configuration.BriefNode;
import org.constellation.configuration.BriefNodeList;
import org.constellation.configuration.Instance;
import org.constellation.dto.Service;
import org.constellation.generic.database.Automatic;
import org.constellation.gui.service.ConstellationService;
import org.constellation.gui.service.CswManager;
import org.constellation.gui.service.ServicesManager;
import org.w3c.dom.Node;

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

    /**
     * Manager used to call constellation server side.
     */
    @Inject
    protected CswManager cswManager;

    @Inject
    @Path("csw_service.gtmpl")
    Template serviceDescription;

    @Inject
    @Path("md.gtmpl")
    Template list;

    @Inject
    @Path("md_selected.gtmpl")
    Template selected;

    @Inject
    @Path("required_iso19115.gtmpl")
    Template iso19115;

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
        final Instance instance = servicesManager.getInstance(serviceId, ServiceDef.Specification.fromShortName(serviceType));
        final BriefNodeList mdList = cswManager.getMetadataList(serviceId, 10, 0);

        // Build service capabilities URL.
        String capabilitiesUrl = cstl.getUrl().toString();
        if (!capabilitiesUrl.endsWith("/")) {
            capabilitiesUrl += "/";
        }
        capabilitiesUrl += "WS/" + serviceType + "/" + serviceId + "?REQUEST=GetCapabilities&SERVICE=" + serviceType.toUpperCase();
        if (metadata.getVersions()!=null && metadata.getVersions().size() == 1) {
            capabilitiesUrl += "&VERSION=" + metadata.getVersions().get(0);
        }

        //use parameter map (not type safe technique) because we aren't on juzu project => gtmpl aren't build.
        final Map<String, Object> parameters = new HashMap<>(0);
        parameters.put("service",         metadata);
        parameters.put("instance",        instance);
        parameters.put("mdList",          mdList);
        parameters.put("nbResults",       mdList.size());
        parameters.put("startIndex",      0);
        parameters.put("nbPerPage",       10);
        parameters.put("capabilitiesUrl", capabilitiesUrl);
        parameters.put("selected",        null);
        return serviceDescription.ok(parameters).withMimeType("text/html");
    }

    @Ajax
    @Resource
    @Route("metadata/select")
    public Response selectMetadata(final String serviceId, final String metadataId) throws IOException {
        final Node mdNode = cswManager.getMetadata(serviceId, metadataId);

        // Go to view with appropriate parameters.
        final Map<String, Object> parameters = new HashMap<>(0);
        parameters.put("selected", new BriefNode(mdNode));
        return selected.ok(parameters).withMimeType("text/html");
    }

    @Ajax
    @Resource
    @Route("metadata/filter")
    public Response metadataList(final String serviceId, final String start, final String count, final String filter, final String orderBy, final String direction, final String dataTypes) throws IOException {
        final int intStart = Integer.parseInt(start);
        final int intCount = Integer.parseInt(count);

        final BriefNodeList mdList = cswManager.getMetadataList(serviceId, intCount, intStart);
        final Map<String, Object> parameters = new HashMap<>(0);
        parameters.put("mdList",     mdList);
        parameters.put("nbResults",  mdList.size());
        parameters.put("startIndex", intStart);
        parameters.put("nbPerPage",  intCount);
        return list.ok(parameters).withMimeType("text/html");
    }

    @Action
    @Route("delete/metadata")
    public Response deleteMetadata(final String serviceId, final String metadataId) throws IOException {
        cswManager.deleteMetadata(serviceId, metadataId);
        return CswController_.dashboard(serviceId, metadataId);
    }

    @Action
    @Route("edit/metadata")
    public Response editMetadata(final String serviceId, final String metadataId) throws IOException {
        final Map<String, Object> parameters = new HashMap<>(0);
        parameters.put("serviceId", serviceId);
        parameters.put("metadataId", metadataId);
        return iso19115.ok(parameters).withMimeType("text/html");
    }
}
