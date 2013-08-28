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
import juzu.impl.request.Request;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerList;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.DataInformation;
import org.constellation.dto.Service;
import org.constellation.dto.StyleListBean;
import org.constellation.gui.service.InstanceSummary;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.service.ServicesManager;
import org.constellation.gui.service.WMSManager;
import org.constellation.gui.service.bean.LayerData;
import org.constellation.gui.templates.add_data_listing;
import org.constellation.gui.templates.webservices;
import org.constellation.gui.util.LayerComparator;
import org.constellation.gui.util.LayerDataComparator;

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
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    protected WMSManager wmsManager;

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

    @Inject
    @Path("add_data_listing.gtmpl")
    add_data_listing  dataListing;


    @Inject
    @Path("add_data.gtmpl")
    protected Template addData;

    /**
     * {@link java.util.ResourceBundle} used on this application
     */
    @Inject
    protected ResourceBundle bundle;

    /**
     * Generate homepage
     * @return a {@link juzu.Response} with right mime type
     */
    @View
    @Route("/")
    public Response index() {
        return index.ok().withMimeType("text/html");
    }

    /**
     * Generate webservice main page
     * @return a {@link juzu.Response} with right mime type
     */
    @View
    @Route("/webservices")
    public Response webservices() {
        List<InstanceSummary> services = servicesManager.getServiceList();
        return webServices.with().services(services).ok().withMimeType("text/html");
    }

    /**
     * Action to create a WMS Service.
     *
     * @param createdService main {@link org.constellation.dto.Service} generated by web form
     * @param serviceContact service contact which set after on {@link  org.constellation.dto.Service}
     * @param serviceConstraint service constraint which set after on {@link org.constellation.dto.Service}
     * @param v111 <code>null</code> if service will not have this version
     * @param v130 <code>null</code> if service will not have this version
     * @param keywords service keyword list
     * @return a {@link juzu.Response} to create view
     * @throws IOException on communication error with Constellation server
     */
    @Action
    @Route("/wms/success")
    public Response createWMSService(Service createdService, Contact serviceContact, AccessConstraint serviceConstraint,
                                     String v111, String v130, String keywords) throws IOException {

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
        boolean created = servicesManager.createServices(createdService, Specification.WMS);

        //return generated view
        return Controller_.succeded(createdService, "WMS", versionList, created + "");
    }

    /**
     * View after service creation
     *
     * @param createdService {@link org.constellation.dto.Service} asked creation
     * @param type service type
     * @param versionList service version available
     * @param created {@link String} {@link boolean} mirror to no if service is created
     *
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
     * @param serviceId Service where we want to see data
     * @param startElement First element list counter
     * @param counter Element number by page
     * @param orderBy String to order by this attribute
     * @param filter String to filter list
     * @throws IOException on communication error with Constellation server
     */
    @Ajax
    @Resource
    @Route("/datalist")
    public void generateDataList(String serviceId, String startElement, String counter, String orderBy, String direction, String filter) throws IOException {
        LayerList layers = wmsManager.getLayers(serviceId);
        Map<String, Object> parameters = new HashMap<String, Object>(0);
        int nbByPage =  Integer.parseInt(counter);

        //show filtered element if list is higher than element number by page
        int start =  Integer.parseInt(startElement);
        int boundary = start+nbByPage;

        //define higher bound on list
        if(boundary>layers.getLayer().size()){
            boundary = layers.getLayer().size();
        }

        // create layer list
        List<Layer> layerList = new ArrayList<Layer>(nbByPage);
        for (int i = start; i < boundary; i++) {
            final Layer layer = layers.getLayer().get(i);
            if (StringUtils.isBlank(filter) || StringUtils.containsIgnoreCase(layer.getName().getLocalPart(), filter)) {
                layerList.add(layer);
            }
        }

        // sort layers if necessary
        if (!StringUtils.isBlank(orderBy) && !StringUtils.isBlank(direction)) {
            Collections.sort(layerList, new LayerComparator(orderBy, direction));
        }

        layers.getLayer().clear();
        layers.getLayer().addAll(layerList);

        parameters.put("layers", layers);
        dataElement.with(parameters).render();
    }

    @Ajax
    @Resource
    @Route("/availabledata")
    public void getAvailableData(String startElement, String counter, String orderBy, String direction, String filter){
        Map<String, Object> parameters = new HashMap<>(0);
        Locale userLocale = Request.getCurrent().getUserContext().getLocale();
        List<LayerData> layerDatas = providerManager.getDataListing(userLocale);

        int nbByPage = Integer.parseInt(counter);

        //show filtered element if list is higher than element number by page
        int start =  Integer.parseInt(startElement);
        int boundary = start+nbByPage;

        //define higher bound on list
        if(boundary>layerDatas.size()){
            boundary = layerDatas.size();
        }
        // create layer list
        List<LayerData> layerList = new ArrayList(nbByPage);
        for (int i = start; i < boundary; i++) {
            final LayerData layerData = layerDatas.get(i);
            if (StringUtils.isBlank(filter) || StringUtils.containsIgnoreCase(layerData.getName(), filter)) {
                layerList.add(layerData);
            }
        }

        // sort layers if necessary
        if (!StringUtils.isBlank(orderBy) && !StringUtils.isBlank(direction)) {
            Collections.sort(layerDatas, new LayerDataComparator(orderBy, direction));
        }

        parameters.put("totalProvider", layerDatas.size());
        parameters.put("providers", layerList);
        dataListing.with(parameters).render();
    }

    /**
     * juzu Upload utilisation. Save file on temp directory before create a thread to send it on server.
     * @param file file set by client for constellation server
     * @return a {@link juzu.Response} to redirect on another page
     */
    @Resource
    @Route("/upload")
    public Response upload(final FileItem file, final String name, final String dataType, final String returnURL) {
        if (file != null) {
            DataInformation di = null;
            try {
                //open stream on file
                final InputStream stream = file.getInputStream();

                // Create file on temporary folder
                String tempDir= System.getProperty("java.io.tmpdir");
                final File newFile = new File(tempDir +"/"+ file.getName());

                // write on file
                final FileOutputStream fos = new FileOutputStream(newFile);
                int intVal = stream.read();
                while (intVal != -1) {
                    fos.write(intVal);
                    intVal = stream.read();
                }

                di = servicesManager.uploadToServer(newFile, name, dataType);

            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "error when saving file on server", e);
                return Response.error("error when saving file on server");
            }

            di.setName(name);
            informationContainer.setInformation(di);
            return RasterController_.showRaster(returnURL);

        }else{
            return Response.error("error when saving file on server");
        }
    }

    @Ajax
    @Resource
    @Route("style/list")
    public void getStyleList(){
        Map<String, Object> parameters = new HashMap<>(0);
        StyleListBean aList = servicesManager.getStyleList();
        parameters.put("styleList", aList);
        styleListing.with(parameters).render();
    }
}
