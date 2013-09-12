package org.constellation.gui;

import juzu.Action;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.plugin.ajax.Ajax;
import org.constellation.dto.DataInformation;
import org.constellation.dto.FileBean;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.templates.folder_listing;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

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
    private ProviderManager providerManager;

    @Ajax
    @Resource
    @Route("/dataFolderList")
    public void getDataFolders(final String path) {
        List<FileBean> folders = providerManager.getDataFolder(path);
        folderListing.with().folders(folders).render();
    }

    @Action
    @Route("/loadingFileData")
    public Response loadingFileData(final String filePath, final String returnURL, final String name, final String dataType){
        DataInformation di = providerManager.loadData(filePath, name, dataType);
        DataInformationContainer.setInformation(di);
        Response aResponse = Response.error("response not initialized");
        switch(dataType){
            case "raster":
                aResponse =  RasterController_.showRaster(returnURL);
                break;
            case "vector":
                aResponse = VectorController_.showVector(returnURL);
        }
        return aResponse;
    }
}
