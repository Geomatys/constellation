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
import org.constellation.gui.templates.raster_description;

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
 * @author Benjamin Garcia (Geomatys).
 * @author Fabien Bernard (Geomatys).
 */
public class RasterController {

    private static final Logger LOGGER = Logging.getLogger(RasterController.class);
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
    public Response showRaster(final String returnURL, final String metadataUploaded) throws IOException {
        final Locale userLocale = Request.getCurrent().getUserContext().getLocale();
        final MetadataLists codeLists = providerManager.getMetadataCodeLists(userLocale.toString());

        return rasterDescription.with().datainformation(informationContainer.getInformation())
                .returnURL(returnURL)
                .dateTypes(codeLists.getDateTypes())
                .locales(codeLists.getLocales())
                .roles(codeLists.getRoles())
                .topics(codeLists.getCategories())
                .userLocale(userLocale.getLanguage())
                .metadataUploaded(metadataUploaded)
                .ok().withMimeType("text/html");
    }

    @Action
    @Route("/raster/create")
    public Response createProvider(String returnURL, DataMetadata metadataToSave, String date, String keywords, String metadataUploaded) {

        DataInformation information = informationContainer.getInformation();
        String path = information.getPath();
        int lastPointIndex = path.lastIndexOf('.');
        String extension = path.substring(lastPointIndex+1, path.length());

        boolean metadataUpload = Boolean.parseBoolean(metadataUploaded);
        if(!metadataUpload){
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


            metadataToSave.setDataPath(information.getPath());
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

        //if it's netCDF, we don't pyramid data
        if("nc".equalsIgnoreCase(extension)){
            providerManager.createProvider("coverage-file", information.getName(), information.getPath(), information.getDataType(), null);
        }else{
            providerManager.pyramidData(information.getName(), information.getPath());
            final String pyramidPath = providerManager.getPyramidPath(information.getName());
            providerManager.createProvider("coverage-store", information.getName(), pyramidPath, information.getDataType(), null);
        }

        return Response.redirect(returnURL);
    }
}
