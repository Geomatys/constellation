package org.constellation.gui;

import juzu.Path;
import juzu.Resource;
import juzu.Route;
import juzu.plugin.ajax.Ajax;
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
}
