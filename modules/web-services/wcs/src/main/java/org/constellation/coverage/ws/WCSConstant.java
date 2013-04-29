/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.coverage.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.geotoolkit.ows.xml.v110.AllowedValues;
import org.geotoolkit.ows.xml.v110.DCP;
import org.geotoolkit.ows.xml.v110.DomainType;
import org.geotoolkit.ows.xml.v110.HTTP;
import org.geotoolkit.ows.xml.v110.Operation;
import org.geotoolkit.ows.xml.v110.OperationsMetadata;
import org.geotoolkit.ows.xml.v110.RequestMethodType;
import org.geotoolkit.wcs.xml.v100.DCPTypeType;
import org.geotoolkit.wcs.xml.v100.DCPTypeType.HTTP.Get;
import org.geotoolkit.wcs.xml.v100.DCPTypeType.HTTP.Post;
import org.geotoolkit.wcs.xml.v100.OnlineResourceType;
import org.geotoolkit.wcs.xml.v100.Request;
import org.geotoolkit.wcs.xml.v100.WCSCapabilityType;

/**
 *  WCS Constants
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class WCSConstant {

    private WCSConstant() {}

    /**
     * WCS Query service
     */
    public static final String WCS_SERVICE = "WCS";

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
    /** Specific Geomatys parameter used in getCoverage */
    public static final String KEY_CATEGORIES = "CATEGORIES";

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
    public static final String ASCII_GRID = "ASCII-GRID";
    /** Format value used in getCoverage */
    public static final String GEOTIFF    = "GEOTIFF";
    /** Format value used in getCoverage */
    public static final String NETCDF     = "NETCDF";
    /** Format value used in getCoverage */
    public static final String PNG        = "PNG";
    /** Format value used in getCoverage */
    public static final String GIF        = "GIF";
    /** Format value used in getCoverage */
    public static final String JPG        = "JPG";
    /** Format value used in getCoverage */
    public static final String JPEG       = "JPEG";
    /** Format value used in getCoverage */
    public static final String BMP        = "BMP";
    /** Format value used in getCoverage */
    public static final String TIF        = "TIF";
    /** Format value used in getCoverage */
    public static final String TIFF       = "TIFF";

    
    public static final WCSCapabilityType OPERATIONS_METADATA_100;
    static {
        final Get get         = new DCPTypeType.HTTP.Get(new OnlineResourceType("someurl"));
        final Post post       = new DCPTypeType.HTTP.Post(new OnlineResourceType("someurl"));
        final DCPTypeType dcp = new DCPTypeType(new DCPTypeType.HTTP(get, post));
        final Request REQUEST_100 = new Request();
        final Request.DescribeCoverage describeCoverage = new Request.DescribeCoverage(Arrays.asList(dcp));
        REQUEST_100.setDescribeCoverage(describeCoverage);
        final Request.GetCapabilities getCapabilities = new Request.GetCapabilities(Arrays.asList(dcp));
        REQUEST_100.setGetCapabilities(getCapabilities);
        final Request.GetCoverage getCoverage = new Request.GetCoverage(Arrays.asList(dcp));
        REQUEST_100.setGetCoverage(getCoverage);
        final WCSCapabilityType.Exception ex = new WCSCapabilityType.Exception(Arrays.asList("application/vnd.ogc.se_xml", "text/xml"));
        OPERATIONS_METADATA_100 = new WCSCapabilityType(REQUEST_100, ex);
    }

    public static final OperationsMetadata OPERATIONS_METADATA_111;
    static {
        final List<DCP> dcps = new ArrayList<DCP>();
        dcps.add(new DCP(new HTTP(new RequestMethodType("somURL"), new RequestMethodType("someURL"))));

        final List<DCP> dcps2 = new ArrayList<DCP>();
        dcps2.add(new DCP(new HTTP(null, new RequestMethodType("someURL"))));

        final List<Operation> operations = new ArrayList<Operation>();

        final List<DomainType> gcParameters = new ArrayList<DomainType>();
        gcParameters.add(new DomainType("AcceptVersions", new AllowedValues(Arrays.asList("1.0.0","1.1.1"))));
        gcParameters.add(new DomainType("AcceptFormats", new AllowedValues(Arrays.asList("text/xml","application/vnd.ogc.wcs_xml"))));
        gcParameters.add(new DomainType("Service", new AllowedValues(Arrays.asList("WCS"))));
        gcParameters.add(new DomainType("Sections", new AllowedValues(Arrays.asList("ServiceIdentification","ServiceProvider","OperationsMetadata","Contents"))));
        Operation getCapabilities = new Operation(dcps, gcParameters, null, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<DomainType> gcoParameters = new ArrayList<DomainType>();
        gcoParameters.add(new DomainType("Version", new AllowedValues(Arrays.asList("1.0.0","1.1.1"))));
        gcoParameters.add(new DomainType("Service", new AllowedValues(Arrays.asList("WCS"))));
        gcoParameters.add(new DomainType("Format", new AllowedValues(Arrays.asList("image/gif","image/png","image/jpeg","matrix"))));
        gcoParameters.add(new DomainType("Store", new AllowedValues(Arrays.asList("false"))));
        Operation getCoverage = new Operation(dcps, gcoParameters, null, null, "GetCoverage");
        operations.add(getCoverage);

        final List<DomainType> dcParameters = new ArrayList<DomainType>();
        dcParameters.add(new DomainType("Version", new AllowedValues(Arrays.asList("1.0.0","1.1.1"))));
        dcParameters.add(new DomainType("Service", new AllowedValues(Arrays.asList("WCS"))));
        dcParameters.add(new DomainType("Format", new AllowedValues(Arrays.asList("text/xml"))));
        Operation describeCoverage = new Operation(dcps, dcParameters, null, null, "DescribeCoverage");
        operations.add(describeCoverage);

        final List<DomainType> constraints = new ArrayList<DomainType>();
        constraints.add(new DomainType("PostEncoding", new AllowedValues(Arrays.asList("XML"))));
        
        OPERATIONS_METADATA_111 = new OperationsMetadata(operations, null, null, null);
    }

}
