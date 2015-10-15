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
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerList;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.AddLayer;
import org.constellation.dto.Contact;
import org.constellation.dto.DataInformation;
import org.constellation.dto.Service;
import org.constellation.gui.service.ConstellationService;
import org.constellation.gui.service.InstanceSummary;
import org.constellation.gui.service.MapManager;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.service.ServicesManager;
import org.constellation.gui.service.bean.LayerData;
import org.constellation.gui.templates.webservices;
import org.constellation.gui.util.LayerComparator;
import org.constellation.gui.util.LayerDataComparator;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;

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


    private static final Logger LOGGER = Logging.getLogger("org.constellation.gui");
    /**
     * Manager used to call constellation server side.
     */
    @Inject
    protected ServicesManager servicesManager;
    @Inject
    protected MapManager mapManager;
    @Inject
    protected ProviderManager providerManager;
    @Inject
    private ConstellationService cstl;
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
    public Response dataDashboard(final String errorInformation) {
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
        parameters.put("selected", null);
        parameters.put("errorInformation", errorInformation);
        String url = cstl.getUrlWithEndSlash() + "api/1/portrayal/portray";
        parameters.put("portrayalUrl", url);
        return dataDashboard.ok(parameters).withMimeType("text/html");
    }

    /**
     * Action to create a WMS Service.
     *
     * @param createdService    main {@link org.constellation.dto.Service} generated by web form
     * @param serviceContact    service contact which set after on {@link  org.constellation.dto.Service}
     * @param serviceConstraint service constraint which set after on {@link org.constellation.dto.Service}
     * @param versions          service versions list
     * @param keywords          service keyword list
     * @param transactional     {@code true} if in transactional mode.
     * @param serviceType       service type (WMS, etc...)
     * @return a {@link juzu.Response} to create view
     */
    @Action
    @Route("/{serviceType}/success")
    public Response createService(Service createdService, Contact serviceContact, AccessConstraint serviceConstraint,
                                  String versions, String keywords, String transactional, String serviceType) throws IOException {

        //build versions list
        final List<String> versionsList;
        if (versions.contains(",")) {
            final String[] versionsArray = versions.split(",");
            versionsList = Arrays.asList(versionsArray);
        } else {
            versionsList = new ArrayList<>();
            versionsList.add(versions);
        }
        createdService.setVersions(versionsList);

        //build keywords list
        final List<String> keywordsList;
        if (keywords.contains(",")) {
            final String[] keywordsArray = keywords.split(",");
            keywordsList = Arrays.asList(keywordsArray);
        } else {
            keywordsList = new ArrayList<>();
            keywordsList.add(keywords);
        }
        createdService.setKeywords(keywordsList);

        //set other object on service
        createdService.setServiceConstraints(serviceConstraint);
        serviceContact.setFullname();
        createdService.setServiceContact(serviceContact);
        if(transactional!=null && !transactional.isEmpty()){
            createdService.setTransactional(true);
        }

        //call service
        try {
            servicesManager.createServices(createdService, Specification.fromShortName(serviceType));
            if (serviceType.equalsIgnoreCase("csw")) {
                return CswController_.chooseSource(createdService.getName(), createdService.getDescription(), createdService.getIdentifier(), serviceType, versionsList);
            } else {
                return Controller_.succeeded(createdService.getName(), createdService.getDescription(), createdService.getIdentifier(), serviceType, versionsList, "true");
            }
        } catch (IOException ex) {
            return Controller_.succeeded(createdService.getName(), createdService.getDescription(), createdService.getIdentifier(), serviceType, versionsList, "false");
        }
    }

    /**
     * View after service creation
     *
     * @param name
     * @param description
     * @param identifier
     * @param type           service type
     * @param versionList    service version available
     * @param created        {@link String} {@link boolean} mirror to no if service is created
     * @return a {@link juzu.Response} to create view
     */
    @View
    @Route("/succeeded")
    public Response succeeded(final String name, final String description, final String identifier, final String type, final List<String> versionList, final String created) {
        Boolean create = Boolean.parseBoolean(created);
        InstanceSummary is = new InstanceSummary();
        is.setIdentifier(identifier);
        is.setName(name);
        is.set_abstract(description);
        is.setType(type);
        servicesManager.buildServiceUrl(type, identifier, versionList, is);
        return success.with().service(is).versions(versionList).created(create).ok().withMimeType("text/html");
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
    public Response generateDataList(final String serviceId, final String serviceType, final String start, final String count, final String orderBy,
                                     final String direction, final String filter) throws IOException {
        final LayerList listBean = mapManager.getLayers(serviceId, Specification.fromShortName(serviceType));

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
    public Response getAvailableData(List<String> dataTypes, final String start, final String count, final String orderBy,
                                     final String direction, final String filter) {

        for (int i = 0; i < dataTypes.size(); i++) {
            String current = dataTypes.get(i);
            if(current.equalsIgnoreCase("coverage")){
                dataTypes.set(i, "raster");
            }
        }
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
    public Response upload(final FileItem file, final FileItem metadataFile, final String dataType, final String returnURL) {
        boolean metadataUploaded = false;
        if (!metadataFile.getName().isEmpty()) {
            metadataUploaded = true;
        }


        if (file != null) {
            DataInformation di;
            try {
                File dataDirectory = ConfigDirectory.getDataDirectory();
                final File newFile = new File(dataDirectory, file.getName());
                Files.copy(file.getInputStream(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                if (metadataUploaded) {
                    File newMetadataFile = new File(dataDirectory.getAbsolutePath() + "/metadata/" + metadataFile.getName());
                    Files.copy(metadataFile.getInputStream(), newMetadataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                di = providerManager.loadData(file.getName(), metadataFile.getName(), dataType);
                informationContainer.setInformation(di);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }

            Response aResponse = Response.error("response not initialized");
            switch (dataType) {
                case "raster":
                    aResponse = RasterController_.showRaster(returnURL, metadataUploaded + "", "true");
                    break;
                case "vector":
                    aResponse = VectorController_.showVector(returnURL, metadataUploaded + "", "true");
            }
            return aResponse;
        } else {
            return Response.error("error when saving file on server");
        }
    }

    @Action
    @Route("layer/add")
    public Response addLayer(final String layerAlias, final String dataName, final String dataProvider, final String serviceId, final String serviceType) {
        AddLayer toAddLayer = new AddLayer(layerAlias, serviceType, serviceId, dataProvider, dataName);
        providerManager.addLayer(toAddLayer);
        return MapController_.dashboard(serviceId, serviceType);
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
