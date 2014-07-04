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
