package org.constellation.gui;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.templates.vector_description;
import org.constellation.dto.DataInformation;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Benjamin Garcia (Geomatys)
 */
public class VectorController {

    /**
     * Manager used to call constellation server side.
     */
    @Inject
    protected ProviderManager providerManager;

    @Inject
    @Path("vector_description.gtmpl")
    vector_description vectorDescription;

//    @Inject
    public DataInformationContainer informationContainer;


    @View
    @Route("/vector/description")
    public Response showVector(final String returnUrl) throws IOException {
        final DataInformation di = DataInformationContainer.getInformation();
        return vectorDescription.with().crs(di.getCrs()).returnURL(returnUrl).name(di.getName()).ok().withMimeType("text/html");
    }

    @Action
    @Route("/vector/create")
    public Response createProvider(final String returnURL, final String crsSelected){
        DataInformation information = informationContainer.getInformation();
        //create provider
        providerManager.createProvider("feature-store", information.getName(), information.getPath(), information.getDataType(), null);
        return StyleController_.edition(information.getName(), information.getName(), null, null, returnURL);
    }
}
