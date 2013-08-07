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

    @View
    @Route("/raster/description")
    public Response showRaster() throws IOException {
        return rasterDescription.with().datainformation(DataInformationContainer.getInformation()).ok().withMimeType("text/html");
    }

    @Action
    @Route("/raster/create")
    public Response createProvider(){
        DataInformation information = DataInformationContainer.getInformation();

        String providerId = information.getPath().substring(information.getPath().lastIndexOf('/') + 1);

        // boucle tant que l'écran de sélection de couche n'est pas fait => on prend la première couche.
        Set<String> keys = information.getCoveragesMetadata().keySet();
        String dataName = "";
        for (String key : keys) {
            dataName = key;
            break;
        }

        //TODO create provider
        providerManager.createProvider("coverage-file", providerId, information.getPath());

        return StyleController_.edition(providerId, dataName);
    }
}
