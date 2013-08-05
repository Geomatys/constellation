package org.constellation.gui;

import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.template.Template;

import javax.inject.Inject;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Benjamin Garcia (Geomatys)
 */
public class RasterController {

    private static final Logger LOGGER = Logger.getLogger(RasterController.class.getName());

    @Inject
    @Path("raster_description.gtmpl")
    Template rasterDescription;

    @View
    @Route("/raster/description")
    public Response showRaster() throws IOException {
        return rasterDescription.ok().withMimeType("text/html");
    }
}
