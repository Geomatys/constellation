/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.wps.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.util.collection.UnmodifiableArrayList;


/**
 *  WPS Constants
 *
 * @author Quentin Boileau (Geomatys)
 */
public final class WPSConstant {

    private WPSConstant() {}

    /**
     * WPS Query service
     */
    public static final String WPS_SERVICE = "WPS";

    /**
     * Version
     */
     public static final String WPS_1_0_0 = "1.0.0";

    /**
     * Lang
     */
     public static final String WPS_LANG = "en-EN";

    /**
     * Request parameters.
     */
    public static final String GETCAPABILITIES = "GetCapabilities";
    public static final String DESCRIBEPROCESS = "DescribeProcess";
    public static final String EXECUTE = "Execute";
    
    
    public static final String IDENTIFER_PARAMETER = "IDENTIFIER";
    public static final String LANGUAGE_PARAMETER = "LANGUAGE";
    public static final String ACCEPT_VERSIONS_PARAMETER = "ACCEPTVERSIONS";

    /* Maximum size in megabytes for a complex input */
    public static final int MAX_MB_INPUT_COMPLEX = 100;
    
    
    /* Default Coordinate Reference System  */
    public static final String DEFAULT_CRS = "EPSG:4326";
    
    /** 
     * Process identifier prefix to uniquely identifies process using OGC URN code.
     */
    public static final String PROCESS_PREFIX = "urn:geomatys:wps:";
}
