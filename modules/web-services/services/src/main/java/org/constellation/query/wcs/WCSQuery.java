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

import org.constellation.query.Query;
import org.constellation.query.QueryService;


/**
 * WCS Query in java objects
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public abstract class WCSQuery extends Query {
    /**
     * Request parameters.
     */
    public static final String DESCRIBECOVERAGE = "DescribeCoverage";
    public static final String GETCOVERAGE = "GetCoverage";
    public static final String GETCAPABILITIES = "GetCapabilities";

    /** Parameter used in getMap, getLegendGraphic, getCapabilities */
    public static final String KEY_FORMAT = "FORMAT";
    /** Parameter used in getMap, describeLayer */
    public static final String KEY_LAYERS = "LAYERS";
    /** Parameter used in getCoverage 1.1.1 */ 
    public static final String KEY_IDENTIFIER = "IDENTIFIER";
    /** Parameter used in getCoverage 1.0.0 */ 
    public static final String KEY_COVERAGE = "COVERAGE";

    /** BBOX for getCoverage in version 1.1.1 */ 
    public static final String KEY_BOUNDINGBOX = "BoundingBox";
    /** BBOX for getCoverage in version 1.1.1 */
    public static final String KEY_STORE = "STORE";
    /** Parameter used in getCapabilities 1.0.0 */ 
    public static final String KEY_SECTION = "SECTION";
    /** Parameter used in getCoverage 1.0.0 */ 
    public static final String KEY_TIME = "TIME";
    /** Parameter used in getCoverage 1.0.0 */ 
    public static final String KEY_BBOX = "BBOX";
    /** Parameter used in getCoverage 1.0.0 and 1.1.1 */ 
    public static final String KEY_CRS = "CRS";
    /** Parameter used in getCoverage 1.0.0 */ 
    public static final String KEY_RESPONSE_CRS = "RESPONSE_CRS";
    /** Parameter used in getCoverage */
    public static final String KEY_WIDTH = "WIDTH";
    /** Parameter used in getCoverage */
    public static final String KEY_HEIGHT = "HEIGHT";
    /** Parameter used in getCoverage */
    public static final String KEY_DEPTH = "DEPTH";
    /** Parameter used in getCoverage */
    public static final String KEY_RESX = "RESX";
    /** Parameter used in getCoverage */
    public static final String KEY_RESY = "RESY";
    /** Parameter used in getCoverage */
    public static final String KEY_RESZ = "RESZ";
    /** Parameter used in getCoverage */
    public static final String KEY_INTERPOLATION = "INTERPOLATION";
    /** Parameter used in getCoverage */
    public static final String KEY_AZIMUTH = "AZIMUTH";

    /** Parameter used in getCoverage */
    public static final String MATRIX = "MATRIX";

    /**
     * {@inheritDoc}
     */
    public final QueryService getService() {
        return new QueryService.WCS();
    }
}
