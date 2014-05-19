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
 * limitations under the License..
 */

package org.constellation.gui;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.request.Request;
import org.apache.sis.util.logging.Logging;
import org.constellation.dto.DataInformation;
import org.constellation.dto.DataMetadata;
import org.constellation.dto.MetadataLists;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.templates.vector_description;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Benjamin Garcia (Geomatys)
 */
public class VectorController {

    private final Logger LOGGER = Logging.getLogger(VectorController.class);

    /**
     * Manager used to call constellation server side.
     */
    @Inject
    protected ProviderManager providerManager;

    @Inject
    @Path("vector_description.gtmpl")
    vector_description vectorDescription;

    public DataInformationContainer informationContainer;


    @View
    @Route("/vector/description")
    public Response showVector(final String returnUrl, final String metadataUploaded, final String creationMode) throws IOException {
        final Locale userLocale = Request.getCurrent().getUserContext().getLocale();
        final MetadataLists codeLists = providerManager.getMetadataCodeLists(userLocale.toString());
        final DataInformation di = DataInformationContainer.getInformation();

        return vectorDescription.with()
                .crs(di.getCrs())
                .returnURL(returnUrl)
                .name(di.getName())
                .datainformation(di)
                .metadataUploaded(metadataUploaded)
                .dateTypes(codeLists.getDateTypes())
                .locales(codeLists.getLocales())
                .roles(codeLists.getRoles())
                .topics(codeLists.getCategories())
                .userLocale(userLocale.getLanguage())
                .creationMode(creationMode)
                .ok().withMimeType("text/html");
    }

    @Action
    @Route("/vector/create")
    public Response createProvider(final String returnURL, final String crsSelected, DataMetadata metadataToSave, String date, String keywords, String metadataUploaded) {
        DataInformation information = informationContainer.getInformation();
        //create provider
        providerManager.createProvider("feature-store", information.getName(), information.getPath(), information.getDataType(), null, null);

        boolean metadataUpload = Boolean.parseBoolean(metadataUploaded);
        if (!metadataUpload) {
            saveEditedMetadata(metadataToSave, date, keywords, information);
        }

        return Response.redirect(returnURL);
    }

    /**
     * Save Metadata if it was edited (not given by user)
     *
     * @param metadataToSave {@link org.constellation.dto.DataMetadata} which define global metadata except date and keywords
     * @param date           data date
     * @param keywords       data keywords
     * @param information    information receive from server.
     */
    private void saveEditedMetadata(final DataMetadata metadataToSave, final String date, final String keywords, final DataInformation information) {
        final Locale userLocale = Request.getCurrent().getUserContext().getLocale();
        DateFormat formatDate = DateFormat.getDateInstance(DateFormat.SHORT, userLocale);
        Date metadataDate = null;
        try {
            metadataDate = formatDate.parse(date);
        } catch (ParseException e) {
            LOGGER.log(Level.WARNING, "can't parse data", e);
        }
        metadataToSave.setLocaleMetadata(userLocale.toString());
        metadataToSave.setDate(metadataDate);


        metadataToSave.setDataName(information.getName());
        metadataToSave.setType(information.getDataType());

        //split keywords
        String[] keywordArray = keywords.split(",");
        List<String> keywordList = new ArrayList<>(0);
        for (int i = 0; i < keywordArray.length; i++) {
            String keyword = keywordArray[i];
            keywordList.add(keyword);
        }
        metadataToSave.setKeywords(keywordList);

        //create pyramid, provider and metadata
        providerManager.saveISO19115Metadata(metadataToSave);
    }
}
