/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.query.wcs;

import org.constellation.query.DefaultQueryService;
import org.constellation.query.Query;
import org.constellation.query.QueryService;


/**
 * Handle the service type of a WCS query.
 * Contains constants for WCS requests in version 1.0.0 and 1.1.1.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public interface WCSQuery extends Query {

    /**
     * WCS Query service
     */
    QueryService WCS_SERVICE = new DefaultQueryService("WCS");

    /**
     * Request parameters.
     */
    String DESCRIBECOVERAGE = "DescribeCoverage";
    String GETCOVERAGE = "GetCoverage";
    String GETCAPABILITIES = "GetCapabilities";

    /** Parameter used in getCoverage 1.1.1 */ 
    String KEY_IDENTIFIER = "IDENTIFIER";
    /** Parameter used in getCoverage 1.0.0 */ 
    String KEY_COVERAGE   = "COVERAGE";

    /** BBOX for getCoverage in version 1.1.1 */ 
    String KEY_BOUNDINGBOX = "BOUNDINGBOX";
    /** BBOX for getCoverage in version 1.1.1 */
    String KEY_STORE = "STORE";
    /** Parameter used in getCapabilities 1.0.0 */ 
    String KEY_SECTION = "SECTION";
    /** Parameter used in getCoverage 1.0.0 */ 
    String KEY_TIME = "TIME";
    /** Parameter used in getCoverage 1.1.1 */ 
    String KEY_TIMESEQUENCE = "TIMESEQUENCE";
    /** Parameter used in getCoverage 1.0.0 */ 
    String KEY_BBOX = "BBOX";
    /** Parameter used in getCoverage 1.0.0 and 1.1.1 */ 
    String KEY_CRS = "CRS";
    /** Parameter used in getCoverage 1.0.0 */ 
    String KEY_RESPONSE_CRS = "RESPONSE_CRS";
    /** Parameter used in getCoverage */
    String KEY_WIDTH  = "WIDTH";
    /** Parameter used in getCoverage */
    String KEY_HEIGHT = "HEIGHT";
    /** Parameter used in getCoverage */
    String KEY_DEPTH  = "DEPTH";
    /** Parameter used in getCoverage */
    String KEY_RESX   = "RESX";
    /** Parameter used in getCoverage */
    String KEY_RESY   = "RESY";
    /** Parameter used in getCoverage */
    String KEY_RESZ   = "RESZ";
    /** Parameter used in getCoverage */
    String KEY_INTERPOLATION = "INTERPOLATION";
    /** Specific Geomatys parameter used in getCoverage */
    String KEY_CATEGORIES = "CATEGORIES";

    /** Parameter used in getCoverage 1.1.1 */
    String KEY_GRIDCS      = "GRIDCS";
    /** Parameter used in getCoverage 1.1.1 */
    String KEY_GRIDOFFSETS = "GRIDOFFSETS";
    /** Parameter used in getCoverage 1.1.1 */
    String KEY_GRIDORIGIN  = "GRIDORIGIN";
    /** Parameter used in getCoverage 1.1.1 */
    String KEY_GRIDTYPE    = "GRIDTYPE";
    /** Parameter used in getCoverage 1.1.1 */
    String KEY_GRIDBASECRS = "GRIDBASECRS";
    /** Parameter used in getCoverage 1.1.1 */
    String KEY_RANGESUBSET = "RANGESUBSET";

    /** Parameter used in getCoverage */
    String KEY_FORMAT = "FORMAT";
    /** Format value used in getCoverage */
    String MATRIX     = "MATRIX";
    /** Format value used in getCoverage */
    String ASCII_GRID = "ASCII-GRID";
    /** Format value used in getCoverage */
    String GEOTIFF    = "GEOTIFF";
    /** Format value used in getCoverage */
    String NETCDF     = "NETCDF";
    /** Format value used in getCoverage */
    String PNG        = "PNG";
    /** Format value used in getCoverage */
    String GIF        = "GIF";
    /** Format value used in getCoverage */
    String JPG        = "JPG";
    /** Format value used in getCoverage */
    String JPEG       = "JPEG";
    /** Format value used in getCoverage */
    String BMP        = "BMP";
    /** Format value used in getCoverage */
    String TIF        = "TIF";
    /** Format value used in getCoverage */
    String TIFF       = "TIFF";

}
