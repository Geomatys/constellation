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
import org.geotoolkit.util.Version;


/**
 * Handle the service type of a WCS query.
 * Contains constants for WCS requests in version 1.0.0 and 1.1.1.
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

    /** Parameter used in getCoverage 1.1.1 */ 
    public static final String KEY_IDENTIFIER = "IDENTIFIER";
    /** Parameter used in getCoverage 1.0.0 */ 
    public static final String KEY_COVERAGE   = "COVERAGE";

    /** BBOX for getCoverage in version 1.1.1 */ 
    public static final String KEY_BOUNDINGBOX = "BOUNDINGBOX";
    /** BBOX for getCoverage in version 1.1.1 */
    public static final String KEY_STORE = "STORE";
    /** Parameter used in getCapabilities 1.0.0 */ 
    public static final String KEY_SECTION = "SECTION";
    /** Parameter used in getCoverage 1.0.0 */ 
    public static final String KEY_TIME = "TIME";
    /** Parameter used in getCoverage 1.1.1 */ 
    public static final String KEY_TIMESEQUENCE = "TIMESEQUENCE";
    /** Parameter used in getCoverage 1.0.0 */ 
    public static final String KEY_BBOX = "BBOX";
    /** Parameter used in getCoverage 1.0.0 and 1.1.1 */ 
    public static final String KEY_CRS = "CRS";
    /** Parameter used in getCoverage 1.0.0 */ 
    public static final String KEY_RESPONSE_CRS = "RESPONSE_CRS";
    /** Parameter used in getCoverage */
    public static final String KEY_WIDTH  = "WIDTH";
    /** Parameter used in getCoverage */
    public static final String KEY_HEIGHT = "HEIGHT";
    /** Parameter used in getCoverage */
    public static final String KEY_DEPTH  = "DEPTH";
    /** Parameter used in getCoverage */
    public static final String KEY_RESX   = "RESX";
    /** Parameter used in getCoverage */
    public static final String KEY_RESY   = "RESY";
    /** Parameter used in getCoverage */
    public static final String KEY_RESZ   = "RESZ";
    /** Parameter used in getCoverage */
    public static final String KEY_INTERPOLATION = "INTERPOLATION";

    /** Parameter used in getCoverage 1.1.1 */
    public static final String KEY_GRIDCS      = "GRIDCS";
    /** Parameter used in getCoverage 1.1.1 */
    public static final String KEY_GRIDOFFSETS = "GRIDOFFSETS";
    /** Parameter used in getCoverage 1.1.1 */
    public static final String KEY_GRIDORIGIN  = "GRIDORIGIN";
    /** Parameter used in getCoverage 1.1.1 */
    public static final String KEY_GRIDTYPE    = "GRIDTYPE";
    /** Parameter used in getCoverage 1.1.1 */
    public static final String KEY_GRIDBASECRS = "GRIDBASECRS";
    /** Parameter used in getCoverage 1.1.1 */
    public static final String KEY_RANGESUBSET = "RANGESUBSET";

    /** Parameter used in getCoverage */
    public static final String KEY_FORMAT = "FORMAT";
    /** Format value used in getCoverage */
    public static final String MATRIX     = "MATRIX";
    /** Format value used in getCoverage */
    public static final String GEOTIFF    = "GEOTIFF";
    /** Format value used in getCoverage */
    public static final String NETCDF     = "NETCDF";
    /** Format value used in getCoverage */
    public static final String PNG        = "PNG";
    /** Format value used in getCoverage */
    public static final String GIF        = "GIF";
    /** Format value used in getCoverage */
    public static final String JPEG       = "JPEG";
    /** Format value used in getCoverage */
    public static final String BMP        = "BMP";

    protected final Version version;

    protected WCSQuery(final Version version) {
        if (version == null) {
            throw new NullPointerException("Version should not be null !");
        }
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final QueryService getService() {
        return new QueryService.WCS();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Version getVersion() {
        return version;
    }
}
