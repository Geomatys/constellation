/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.constellation.configuration.DataBrief;
import org.constellation.dto.DataInformation;
import org.constellation.dto.FileBean;
import org.constellation.gui.service.ConstellationService;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.templates.folder_listing;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * Main data controller. For common action without data type
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class DataController {

    @Inject
    @Path("folder_listing.gtmpl")
    folder_listing folderListing;

    @Inject
    @Path("data_selected.gtmpl")
    Template dataSelected;

    @Inject
    private ProviderManager providerManager;
    @Inject
    private ConstellationService cstl;

    @Ajax
    @Resource
    @Route("/dataFolderList")
    public Response getDataFolders(final String path) {
        final List<FileBean> folders = providerManager.getDataFolder(path);
        return folderListing.with().folders(folders).ok().withMimeType("text/html");
    }

    @Action
    @Route("/loadingFileData")
    public Response loadingFileData(final String filePath, final String returnURL, final String dataType){
        DataInformation di = providerManager.loadData(filePath, "", dataType);
        Response aResponse = Response.error("response not initialized");
        if(di.getErrorInformation() == null){
            DataInformationContainer.setInformation(di);
            switch(dataType){
                case "raster":
                    aResponse =  RasterController_.showRaster(returnURL, "false", "true");
                    break;
                case "vector":
                    aResponse = VectorController_.showVector(returnURL, "false", "true");
            }
        }
        else{
            aResponse =  Controller_.dataDashboard(di.getErrorInformation());
        }
        return aResponse;
    }

    @Ajax
    @Resource
    @Route("/selectData")
    public Response selectData(final String name, final String namespace, final String providerId) {
        final Map<String, Object> parameters = new HashMap<>(0);
        final DataBrief db = providerManager.getDataSummary(name, namespace, providerId);
        String url = cstl.getUrlWithEndSlash() + "api/1/portrayal/portray";
        parameters.put("selected", db);
        parameters.put("portrayUrl", url);
        return dataSelected.with(parameters).ok().withMimeType("text/html");
    }

    @Action
    @Route("/metadata")
    public Response getMetadata(final String providerId, final String dataId, final String dataType, final String returnUrl){
        final DataInformation di = providerManager.getMetadata(providerId, dataId, dataType);
        DataInformationContainer.setInformation(di);
        switch (dataType){
            case "VECTOR" :
                return VectorController_.showVector(returnUrl, "false", "false");
            case "COVERAGE" :
                return RasterController_.showRaster(returnUrl, "false", "false");
            default :
                return Response.error("response not initialized");
        }
    }
}
