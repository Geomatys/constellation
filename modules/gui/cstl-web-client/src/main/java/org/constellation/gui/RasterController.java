package org.constellation.gui;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.request.Request;
import org.constellation.dto.DataInformation;
import org.constellation.dto.MetadataLists;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.templates.raster_description;
import org.constellation.gui.service.ProviderManager;
import org.constellation.dto.DataInformation;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Benjamin Garcia (Geomatys).
 * @author Fabien Bernard (Geomatys).
 */
public class RasterController {

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
        final Locale userLocale = Request.getCurrent().getUserContext().getLocale();
        final MetadataLists codeLists = providerManager.getMetadataCodeLists(userLocale.toString());

        return rasterDescription.with().datainformation(informationContainer.getInformation())
                .returnURL(returnURL)
                .dateTypes(codeLists.getDateTypes())
                .locales(codeLists.getLocales())
                .roles(codeLists.getRoles())
                .topics(codeLists.getCategories())
                .ok().withMimeType("text/html");
    }

    @Action
    @Route("/raster/create")
    public Response createProvider(final String returnURL){
        DataInformation information = informationContainer.getInformation();
        //create provider
        providerManager.createProvider("coverage-file", information.getName(), information.getPath(), information.getDataType(), null);
        return Response.redirect(returnURL);
    }
}
