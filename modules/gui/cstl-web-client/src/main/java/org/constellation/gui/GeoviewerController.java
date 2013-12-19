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

import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.template.Template;

import javax.inject.Inject;

/**
 * Viewer page controller
 *
 * @author bgarcia
 * @version 0.9
 * @since 0.9
 */
public class GeoviewerController {


    @Inject
    @Path("geoviewer.gtmpl")
    Template geoviewer;

    @View
    @Route("/viewer")
    public Response geoviewer() {
        return geoviewer.ok().withMimeType("text/html");
    }
}
