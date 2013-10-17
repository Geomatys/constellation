/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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
package org.constellation.api;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author guilhem Legal (Geomatys)
 * @since 0.9
 */
public class CommonConstants {
 
    /*
     * Default declareded CRS codes for each layer in the getCapabilities
     */
    public static final List<String> DEFAULT_CRS = new ArrayList<>();
    static {
        DEFAULT_CRS.add("EPSG:4326");
        DEFAULT_CRS.add("CRS:84");
        DEFAULT_CRS.add("EPSG:3395");
        DEFAULT_CRS.add("EPSG:3857");
        DEFAULT_CRS.add("EPSG:27571");
        DEFAULT_CRS.add("EPSG:27572");
        DEFAULT_CRS.add("EPSG:27573");
        DEFAULT_CRS.add("EPSG:27574");
    }

    public static final List<String> WXS = new ArrayList<>();
    static {
        WXS.add("WMS");
        WXS.add("WCS");
        WXS.add("WFS");
        WXS.add("WMTS");
    }

    public static final String SUCCESS = "Success";

    public static final String SERVICE = "Service";
}
