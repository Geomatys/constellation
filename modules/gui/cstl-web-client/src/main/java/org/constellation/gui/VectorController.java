/*
 * Constellation - An open source and standard compliant SDI
 *      http://www.constellation-sdi.org
 *   (C) 2009-2013, Geomatys
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details..
 */

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
    public Response showVector(final String returnUrl, final String metadataUploaded) throws IOException {
        final DataInformation di = DataInformationContainer.getInformation();
        return vectorDescription.with()
                .crs(di.getCrs())
                .returnURL(returnUrl)
                .name(di.getName())
                .datainformation(di)
                .ok().withMimeType("text/html");
    }

    @Action
    @Route("/vector/create")
    public Response createProvider(final String returnURL, final String crsSelected){
        DataInformation information = informationContainer.getInformation();
        //create provider
        providerManager.createProvider("feature-store", information.getName(), information.getPath(), information.getDataType(), null, null);
        return Response.redirect(returnURL);
    }
}
