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
 * limitations under the License.
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
