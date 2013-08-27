package org.constellation.gui;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import org.constellation.utils.SimplyMetadataTreeNode;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.templates.raster_description;
import org.constellation.dto.DataInformation;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Benjamin Garcia (Geomatys)
 */
public class RasterController {

    private static final Logger LOGGER = Logger.getLogger(RasterController.class.getName());

    /**
     * Manager used to call constellation server side.
     */
    @Inject
    protected ProviderManager providerManager;

    @Inject
    @Path("raster_description.gtmpl")
    raster_description rasterDescription;

//    @Inject
    public DataInformationContainer informationContainer;


    @View
    @Route("/raster/description")
    public Response showRaster(final String returnURL) throws IOException {
        List<SimplyMetadataTreeNode> metadataList= informationContainer.getInformation().getFileMetadata();
        return rasterDescription.with().datainformation(informationContainer.getInformation()).metadataMap(metadataList).returnURL(returnURL).ok().withMimeType("text/html");
    }



    @Action
    @Route("/raster/create")
    public Response createProvider(final String returnURL){
        DataInformation information = informationContainer.getInformation();
        //create provider
        providerManager.createProvider("coverage-file", information.getName(), information.getPath());
        return StyleController_.edition(information.getName(), information.getName(), null, null, returnURL);
    }
}
