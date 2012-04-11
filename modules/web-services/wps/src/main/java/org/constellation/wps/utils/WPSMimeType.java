/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.wps.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * MimeType list based on OGC 12-029 paper.
 * 
 * @author Quentin Boileau (Geomatys)
 */
public enum WPSMimeType {

    NONE(null),
    
    IMG_JPEG("image/jpeg"),
    IMG_TIFF("image/tiff"),
    IMG_GEOTIFF("image/tiff;subtype=geotiff"),
    IMG_BMP("image/bmp"),
    IMG_SVG("image/svg+xml"),
    
    APP_OCTET("application/octet-stream"),
    APP_JSON("application/json"),
    APP_GEOJSON("application/geojson"),
    APP_GML("application/gml+xml"),
    APP_SHP("application/x-zipped-shp"),
    
    OGC_WFS("application/x‐ogc-wfs"),
    OGC_WMS("application/x-ogc-wms"),
    
    //not recommended in paper
    TEXT_XML("text/xml"),
    TEXT_GML("text/gml");
    
    private String mime;
    private WPSMimeType(final String mime) {
        this.mime = mime;
    }

    public String getValue() {
        return mime;
    }
}
