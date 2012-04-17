/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.constellation.api;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author guilhem Legal (Geomatys)
 * @since 0.9 */
public class CommonConstants {
 
    /*
     * Default declareded CRS codes for each layer in the getCapabilities
     */
    public static final List<String> DEFAULT_CRS = new ArrayList<String>();
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
}
