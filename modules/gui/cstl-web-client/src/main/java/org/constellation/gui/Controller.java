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

import juzu.Action;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerList;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.AddLayer;
import org.constellation.dto.Contact;
import org.constellation.dto.DataInformation;
import org.constellation.dto.DataMetadata;
import org.constellation.dto.Service;
import org.constellation.dto.StyleListBean;
import org.constellation.gui.service.InstanceSummary;
import org.constellation.gui.service.MapManager;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.service.ServicesManager;
import org.constellation.gui.service.bean.LayerData;
import org.constellation.gui.templates.webservices;
import org.constellation.gui.util.LayerComparator;
import org.constellation.gui.util.LayerDataComparator;
import org.constellation.utils.GeotoolkitFileExtensionAvailable;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Constellation web client main Juzu controller. Manage linkage with other controller and homepages
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class Controller {


    private static final Logger LOGGER = Logger.getLogger(Controller.class.toString());
    /**
     * Manager used to call constellation server side.
     */
    @Inject
    protected ServicesManager servicesManager;
    @Inject
    protected MapManager mapManager;
    @Inject
    protected ProviderManager providerManager;
    //    @Inject
    protected DataInformationContainer informationContainer;
    /**
     * Home page juzu template.
     */
    @Inject
    @Path("index.gtmpl")
    protected Template index;
    @Inject
    @Path("data_dashboard.gtmpl")
    protected Template dataDashboard;
    @Inject
    @Path("data_listing.gtmpl")
    protected Template dataListing;
    @Inject
    @Path("add_data.gtmpl")
    protected Template addData;
    @Inject
    @Path("local_file_modal.gtmpl")
    protected Template localFileModal;
    @Inject
    @Path("server_modal.gtmpl")
    protected Template serverModal;
    /**
     * {@link java.util.ResourceBundle} used on this application
     */
    @Inject
    protected ResourceBundle bundle;
    /**
     * Webservice main page juzu template.
     */
    @Inject
    @Path("webservices.gtmpl")
    webservices webServices;
    /**
     * End service creation page juzu tempate
     */
    @Inject
    @Path("success.gtmpl")
    org.constellation.gui.templates.success success;
    @Inject
    @Path("add_data_alias.gtmpl")
    Template addDataAlias;
    @Inject
    @Path("add_data_style.gtmpl")
    Template addDataStyle;
    @Inject
    @Path("add_data_style_listing.gtmpl")
    Template styleListing;
    @Inject
    @Path("layer.gtmpl")
    Template dataElement;

    /**
     * Generate homepage
     *
     * @return a {@link juzu.Response} with right mime type
     */
    @View
    @Route("/")
    public Response index() {
        return index.ok().withMimeType("text/html");
    }

    /**
     * Generate webservice main page
     *
     * @return a {@link juzu.Response} with right mime type
     */
    @View
    @Route("/webservices")
    public Response webservices() {
        List<InstanceSummary> services = servicesManager.getServiceList();
        return webServices.with().services(services).ok().withMimeType("text/html");
    }

    @View
    @Route("/data")
    public Response dataDashboard() {
        final List<LayerData> list = providerManager.getDataListing(Arrays.asList("vector"));
        final int nbResults = list.size();

        // Truncate the list.
        final List<LayerData> providers;
        if (!list.isEmpty()) {
            final int endIndex = Math.min(list.size(), 10);
            providers = list.subList(0, endIndex);
        } else {
            providers = new ArrayList<>(0);
        }

        final Map<String, Object> parameters = new HashMap<>(0);
        parameters.put("providers", providers);
        parameters.put("nbResults", nbResults);
        parameters.put("startIndex", 0);
        parameters.put("nbPerPage", 10);
        return dataDashboard.ok(parameters).withMimeType("text/html");
    }

    /**
     * Action to create a WMS Service.
     *
     * @param createdService    main {@link org.constellation.dto.Service} generated by web form
     * @param serviceContact    service contact which set after on {@link  org.constellation.dto.Service}
     * @param serviceConstraint service constraint which set after on {@link org.constellation.dto.Service}
     * @param v111              <code>null</code> if service will not have this version
     * @param v130              <code>null</code> if service will not have this version
     * @param keywords          service keyword list
     * @return a {@link juzu.Response} to create view
     */
    @Action
    @Route("/{serviceType}/success")
    public Response createService(Service createdService, Contact serviceContact, AccessConstraint serviceConstraint,
                                  String v111, String v130, String keywords, String serviceType) throws IOException {

        //create version list to set on createdService
        List<String> versionList = new ArrayList<String>(0);
        if (v111 != null) {
            versionList.add(v111);
        }
        if (v130 != null) {
            versionList.add(v130);
        }
        createdService.setVersions(versionList);

        //build keyword list
        String[] keywordsArray = keywords.split(" ");
        List<String> keywordsList = Arrays.asList(keywordsArray);
        createdService.setKeywords(keywordsList);

        //set other object on service
        createdService.setServiceConstraints(serviceConstraint);
        createdService.setServiceContact(serviceContact);

        //call service
        try {
            servicesManager.createServices(createdService, Specification.WMS);
            return Controller_.succeded(createdService, "WMS", versionList, "true");
        } catch (IOException ex) {
            return Controller_.succeded(createdService, "WMS", versionList, "false");
        }
    }

    /**
     * View after service creation
     *
     * @param createdService {@link org.constellation.dto.Service} asked creation
     * @param type           service type
     * @param versionList    service version available
     * @param created        {@link String} {@link boolean} mirror to no if service is created
     * @return a {@link juzu.Response} to create view
     */
    @View
    @Route("/succeded")
    public Response succeded(Service createdService, String type, List<String> versionList, String created) {
        Boolean create = Boolean.parseBoolean(created);
        return success.with().service(createdService).type(type).versions(versionList).created(create).ok().withMimeType("text/html");
    }

    /**
     * Generate datalist to show it in ajax
     *
     * @param serviceId Service where we want to see data
     * @param start     First element list counter
     * @param count     Element number by page
     * @param orderBy   String to order by this attribute
     * @param filter    String to filter list
     * @throws java.io.IOException on communication error with Constellation server
     */
    @Ajax
    @Resource
    @Route("/dataList")
    public Response generateDataList(final String serviceId, final String start, final String count, final String orderBy,
                                     final String direction, final String filter) throws IOException {
        final LayerList listBean = mapManager.getLayers(serviceId);

        // Search layers by name.
        if (!isBlank(filter)) {
            final List<Layer> toRemove = new ArrayList<>();
            for (final Layer bean : listBean.getLayer()) {
                if (!containsIgnoreCase(bean.getName().getLocalPart(), filter)) {
                    toRemove.add(bean);
                }
            }
            listBean.getLayer().removeAll(toRemove);
        }
        final int nbResults = listBean.getLayer().size();

        // Sort layers by criteria.
        if (!StringUtils.isBlank(orderBy) && !StringUtils.isBlank(direction)) {
            Collections.sort(listBean.getLayer(), new LayerComparator(orderBy, direction));
        }

        // Truncate the list.
        final List<Layer> layers;
        final int intStart = Integer.parseInt(start);
        final int intCount = Integer.parseInt(count);
        if (!listBean.getLayer().isEmpty() && intStart < listBean.getLayer().size()) {
            final int endIndex = Math.min(listBean.getLayer().size(), intStart + intCount);
            layers = listBean.getLayer().subList(intStart, endIndex);
        } else {
            layers = new ArrayList<>(0);
        }

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("layers", layers);
        parameters.put("nbResults", nbResults);
        parameters.put("startIndex", intStart);
        parameters.put("nbPerPage", intCount);
        return dataElement.with(parameters).ok();
    }

    @Ajax
    @Resource
    @Route("/providerList")
    public Response getAvailableData(final List<String> dataTypes, final String start, final String count, final String orderBy,
                                     final String direction, final String filter) {
        final List<LayerData> list = providerManager.getDataListing(dataTypes);

        // Search layers by name.
        if (!isBlank(filter)) {
            final List<LayerData> toRemove = new ArrayList<>();
            for (final LayerData bean : list) {
                if (!containsIgnoreCase(bean.getName(), filter)) {
                    toRemove.add(bean);
                }
            }
            list.removeAll(toRemove);
        }
        final int nbResults = list.size();

        // Sort layers by criteria.
        if (!StringUtils.isBlank(orderBy) && !StringUtils.isBlank(direction)) {
            Collections.sort(list, new LayerDataComparator(orderBy, direction));
        }

        // Truncate the list.
        final List<LayerData> providers;

        final int intStart = (start == null) ? 0 : Integer.parseInt(start);
        final int intCount = (count == null) ? 10 : Integer.parseInt(count);
        if (!list.isEmpty() && intStart < list.size()) {
            final int endIndex = Math.min(list.size(), intStart + intCount);
            providers = list.subList(intStart, endIndex);
        } else {
            providers = new ArrayList<>(0);
        }

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("providers", providers);
        parameters.put("nbResults", nbResults);
        parameters.put("startIndex", intStart);
        parameters.put("nbPerPage", intCount);
        return dataListing.with(parameters).ok();
    }

    /**
     * juzu Upload utilisation. Save file on temp directory before create a thread to send it on server.
     *
     * @param file file set by client for constellation server
     * @return a {@link juzu.Response} to redirect on another page
     */
    @Resource
    @Route("/upload")
    public Response upload(final FileItem file, final String dataType, final String returnURL) {
        if (file != null) {
            DataInformation di;
            // Create file on temporary folder
            String tempDir = System.getProperty("java.io.tmpdir");
            final File newFile = new File(tempDir + "/" + file.getName());

            try {
                //open stream on file
                final InputStream stream = file.getInputStream();


                // write on file
                final FileOutputStream fos = new FileOutputStream(newFile);
                int intVal = stream.read();
                while (intVal != -1) {
                    fos.write(intVal);
                    intVal = stream.read();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "error when saving file on server", e);
                return Response.error("error when saving file on server");
            }

            di = servicesManager.uploadToServer(newFile, dataType);
            informationContainer.setInformation(di);
            Response aResponse = Response.error("response not initialized");
            switch (dataType) {
                case "raster":
                    aResponse = RasterController_.showRaster(returnURL);
                    break;
                case "vector":
                    aResponse = VectorController_.showVector(returnURL);
            }
            return aResponse;
        } else {
            return Response.error("error when saving file on server");
        }
    }

    @Action
    @Route("layer/add")
    public Response addLayer(final String layerAlias, final String dataName, final String dataProvider, final String styleName, final String styleProvider, final String serviceId) {
        AddLayer toAddLayer = new AddLayer(layerAlias, "WMS", serviceId, dataProvider, dataName, styleProvider, styleName);
        providerManager.addLayer(toAddLayer);
        return MapController_.editMapService(serviceId, "wms");
    }

    /**
     * Reloads the WMS service with the specified identifier.
     *
     * @param serviceId the service identifier
     * @return a status {@link juzu.Response}
     */
    @Ajax
    @Resource
    @Route("service/reload")
    public Response restartService(final String serviceType, final String serviceId) {
        try {
            servicesManager.restartService(serviceId, Specification.fromShortName(serviceType));
            return Response.ok();
        } catch (IOException ex) {
            return Response.error(ex.getLocalizedMessage());
        }
    }

    /**
     * Stops the WMS service with the specified identifier.
     *
     * @param serviceId the service identifier
     * @return a status {@link juzu.Response}
     */
    @Ajax
    @Resource
    @Route("service/stop")
    public Response stopService(final String serviceType, final String serviceId) {
        try {
            servicesManager.stopService(serviceId, Specification.fromShortName(serviceType));
            return Response.ok();
        } catch (IOException ex) {
            return Response.error(ex.getLocalizedMessage());
        }
    }

    /**
     * Deletes the WMS service with the specified identifier.
     *
     * @param serviceId the service identifier
     * @return a status {@link juzu.Response}
     */
    @Ajax
    @Resource
    @Route("service/delete")
    public Response deleteService(final String serviceType, final String serviceId) {
        try {
            servicesManager.deleteService(serviceId, Specification.fromShortName(serviceType));
            return Response.ok();
        } catch (IOException ex) {
            return Response.error(ex.getLocalizedMessage());
        }
    }

    /**
     * Stars the WMS service with the specified identifier.
     *
     * @param serviceId the service identifier
     * @return a status {@link juzu.Response}
     */
    @Ajax
    @Resource
    @Route("service/start")
    public Response startService(final String serviceType, final String serviceId) {
        try {
            servicesManager.startService(serviceId, Specification.fromShortName(serviceType));
            return Response.ok();
        } catch (IOException ex) {
            return Response.error(ex.getLocalizedMessage());
        }
    }

    /**
     * Updates the WMS service general description.
     *
     * @param identifier  the service identifier
     * @param name        the (new) service name
     * @param keywords    the (new) service keywords
     * @param description the (new) service description
     * @param v111        the (new) service v111 state
     * @param v130        the (new) service v130 state
     * @return a status {@link juzu.Response}
     * @throws java.io.IOException on communication error with Constellation server
     */
    @Ajax
    @Resource
    @Route("service/description")
    public Response setServiceDescription(final String serviceType, final String identifier, final String name,
                                          final String keywords, final String description, final String v111,
                                          final String v130) throws IOException {
        final Service metadata = servicesManager.getMetadata(identifier, Specification.WMS);
        metadata.setName(name);
        metadata.setKeywords(!isBlank(keywords) ? Arrays.asList(keywords.split(",")) : null);
        metadata.setDescription(description);
        metadata.setVersions(new ArrayList<String>());
        if (v111 != null) metadata.getVersions().add(v111);
        if (v130 != null) metadata.getVersions().add(v130);

        try {
            servicesManager.setMetadata(metadata, Specification.fromShortName(serviceType));
            return Response.ok();
        } catch (IOException ex) {
            return Response.error(ex.getLocalizedMessage());
        }
    }

    /**
     * Update the WMS service constraint and contact.
     *
     * @param identifier the service identifier
     * @param contact    the (new) service contact
     * @param constraint the (new) service constraint
     * @return a status {@link juzu.Response}
     * @throws java.io.IOException on communication error with Constellation server
     */
    @Ajax
    @Resource
    @Route("service/metadata")
    public Response setServiceMetadata(final String serviceType, final String identifier, final Contact contact,
                                       final AccessConstraint constraint) throws IOException {
        final Service metadata = servicesManager.getMetadata(identifier, Specification.WMS);
        metadata.setServiceContact(contact);
        metadata.setServiceConstraints(constraint);

        try {
            servicesManager.setMetadata(metadata, Specification.fromShortName(serviceType));
            return Response.ok();
        } catch (IOException ex) {
            return Response.error(ex.getLocalizedMessage());
        }
    }
}
