package org.constellation.gui;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.template.Template;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.service.ServicesManager;
import org.constellation.gui.templates.raster_description;
import org.constellation.ws.rest.post.DataInformation;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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

    @Inject
    DataInformationContainer informationContainer;

    @View
    @Route("/raster/description")
    public Response showRaster() throws IOException {
        return rasterDescription.with().datainformation(informationContainer.getInformation()).ok().withMimeType("text/html");
    }

    @Action
    @Route("/raster/create")
    public Response createProvider(){
        DataInformation information = informationContainer.getInformation();

        int indexfileName = information.getPath().lastIndexOf('/')+1;
        int extention = information.getPath().lastIndexOf('.');
        String dataName = information.getPath().substring(indexfileName, extention);


        //TODO create provider
        providerManager.createProvider("coverage-file", dataName, information.getPath());

        return StyleController_.edition(dataName, dataName);
    }
}
