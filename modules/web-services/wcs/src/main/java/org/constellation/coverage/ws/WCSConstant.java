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
package org.constellation.coverage.ws;

import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Details;
import org.constellation.ws.MimeType;
import org.geotoolkit.gml.xml.v311.CodeListType;
import org.geotoolkit.ows.xml.AbstractContact;
import org.geotoolkit.ows.xml.AbstractDCP;
import org.geotoolkit.ows.xml.AbstractDomain;
import org.geotoolkit.ows.xml.AbstractOnlineResourceType;
import org.geotoolkit.ows.xml.AbstractOperation;
import org.geotoolkit.ows.xml.AbstractOperationsMetadata;
import org.geotoolkit.ows.xml.AbstractResponsiblePartySubset;
import org.geotoolkit.ows.xml.AbstractServiceIdentification;
import org.geotoolkit.ows.xml.AbstractServiceProvider;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.wcs.xml.GetCapabilitiesResponse;
import org.geotoolkit.wcs.xml.WCSXmlFactory;
import org.geotoolkit.wcs.xml.v100.DCPTypeType;
import org.geotoolkit.wcs.xml.v100.DCPTypeType.HTTP.Get;
import org.geotoolkit.wcs.xml.v100.DCPTypeType.HTTP.Post;
import org.geotoolkit.wcs.xml.v100.OnlineResourceType;
import org.geotoolkit.wcs.xml.v100.Request;
import org.geotoolkit.wcs.xml.v100.WCSCapabilityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

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

    /*
     * A list supported formats
     *
     */
     public static final List<CodeListType> SUPPORTED_FORMATS_100 = new ArrayList<>();
     static {
        SUPPORTED_FORMATS_100.add(new CodeListType("png"));
        SUPPORTED_FORMATS_100.add(new CodeListType("gif"));
        SUPPORTED_FORMATS_100.add(new CodeListType("jpeg"));
        SUPPORTED_FORMATS_100.add(new CodeListType("bmp"));
        SUPPORTED_FORMATS_100.add(new CodeListType("tiff"));
        SUPPORTED_FORMATS_100.add(new CodeListType("geotiff"));
        SUPPORTED_FORMATS_100.add(new CodeListType("matrix"));
        SUPPORTED_FORMATS_100.add(new CodeListType("ascii-grid"));
    }

     public static final List<String> SUPPORTED_FORMATS_111 = new ArrayList<>();
     static {
         SUPPORTED_FORMATS_111.add(MimeType.IMAGE_PNG);
         SUPPORTED_FORMATS_111.add(MimeType.IMAGE_GIF);
         SUPPORTED_FORMATS_111.add(MimeType.IMAGE_JPEG);
         SUPPORTED_FORMATS_111.add(MimeType.IMAGE_BMP);
         SUPPORTED_FORMATS_111.add("matrix");
         SUPPORTED_FORMATS_111.add("ascii-grid");
    }

    /**
     * A list of supported interpolation
     */
    public static final List<org.geotoolkit.wcs.xml.v100.InterpolationMethod> SUPPORTED_INTERPOLATIONS_V100 =
            new ArrayList<>();
    static {
            SUPPORTED_INTERPOLATIONS_V100.add(org.geotoolkit.wcs.xml.v100.InterpolationMethod.BILINEAR);
            SUPPORTED_INTERPOLATIONS_V100.add(org.geotoolkit.wcs.xml.v100.InterpolationMethod.BICUBIC);
            SUPPORTED_INTERPOLATIONS_V100.add(org.geotoolkit.wcs.xml.v100.InterpolationMethod.NEAREST_NEIGHBOR);
    }
    public static final org.geotoolkit.wcs.xml.v100.SupportedInterpolationsType INTERPOLATION_V100 = new org.geotoolkit.wcs.xml.v100.SupportedInterpolationsType(
                    org.geotoolkit.wcs.xml.v100.InterpolationMethod.NEAREST_NEIGHBOR, SUPPORTED_INTERPOLATIONS_V100);

    /**
     * A list of supported interpolation
     */
    public static final List<org.geotoolkit.wcs.xml.v111.InterpolationMethod> SUPPORTED_INTERPOLATIONS_V111 =
            new ArrayList<>();
    static {
            SUPPORTED_INTERPOLATIONS_V111.add(org.geotoolkit.wcs.xml.v111.InterpolationMethod.BILINEAR);
            SUPPORTED_INTERPOLATIONS_V111.add(org.geotoolkit.wcs.xml.v111.InterpolationMethod.BICUBIC);
            SUPPORTED_INTERPOLATIONS_V111.add(org.geotoolkit.wcs.xml.v111.InterpolationMethod.NEAREST_NEIGHBOR);
    }
    public static final org.geotoolkit.wcs.xml.v111.InterpolationMethods INTERPOLATION_V111 =
            new org.geotoolkit.wcs.xml.v111.InterpolationMethods(SUPPORTED_INTERPOLATIONS_V111 , org.geotoolkit.wcs.xml.v111.InterpolationMethod.NEAREST_NEIGHBOR.value());

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

    public static final AbstractOperationsMetadata OPERATIONS_METADATA_111;
    static {
        final List<AbstractDCP> dcps = new ArrayList<>();
        dcps.add(WCSXmlFactory.buildDCP("1.1.1", "someURL", "someURL"));

        final List<AbstractDCP> dcps2 = new ArrayList<>();
        dcps2.add(WCSXmlFactory.buildDCP("1.1.1", null, "someURL"));

        final List<AbstractOperation> operations = new ArrayList<>();

        final List<AbstractDomain> gcParameters = new ArrayList<>();
        gcParameters.add(WCSXmlFactory.buildDomain("1.1.1", "AcceptVersions", Arrays.asList("1.0.0","1.1.1")));
        gcParameters.add(WCSXmlFactory.buildDomain("1.1.1", "AcceptFormats",  Arrays.asList("text/xml","application/vnd.ogc.wcs_xml")));
        gcParameters.add(WCSXmlFactory.buildDomain("1.1.1", "Service",        Arrays.asList("WCS")));
        gcParameters.add(WCSXmlFactory.buildDomain("1.1.1", "Sections",       Arrays.asList("ServiceIdentification","ServiceProvider","OperationsMetadata","Contents")));
        AbstractOperation getCapabilities = WCSXmlFactory.buildOperation("1.1.1", dcps, gcParameters, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<AbstractDomain> gcoParameters = new ArrayList<>();
        gcoParameters.add(WCSXmlFactory.buildDomain("1.1.1", "Version", Arrays.asList("1.0.0","1.1.1")));
        gcoParameters.add(WCSXmlFactory.buildDomain("1.1.1", "Service", Arrays.asList("WCS")));
        gcoParameters.add(WCSXmlFactory.buildDomain("1.1.1", "Format",  Arrays.asList("image/gif","image/png","image/jpeg","matrix")));
        gcoParameters.add(WCSXmlFactory.buildDomain("1.1.1", "Store",   Arrays.asList("false")));
        AbstractOperation getCoverage = WCSXmlFactory.buildOperation("1.1.1", dcps, gcoParameters, null, "GetCoverage");
        operations.add(getCoverage);

        final List<AbstractDomain> dcParameters = new ArrayList<>();
        dcParameters.add(WCSXmlFactory.buildDomain("1.1.1", "Version", Arrays.asList("1.0.0","1.1.1")));
        dcParameters.add(WCSXmlFactory.buildDomain("1.1.1", "Service", Arrays.asList("WCS")));
        dcParameters.add(WCSXmlFactory.buildDomain("1.1.1", "Format",  Arrays.asList("text/xml")));
        AbstractOperation describeCoverage = WCSXmlFactory.buildOperation("1.1.1", dcps, dcParameters, null, "DescribeCoverage");
        operations.add(describeCoverage);

        final List<AbstractDomain> constraints = new ArrayList<>();
        constraints.add(WCSXmlFactory.buildDomain("1.1.1", "PostEncoding", Arrays.asList("XML")));

        OPERATIONS_METADATA_111 = OWSXmlFactory.buildOperationsMetadata("1.1.0", operations, null, constraints, null);
    }

    public static AbstractOperationsMetadata getOperationMetadata(final String version) {
        if (version.equals("1.0.0")) {
            return OPERATIONS_METADATA_100.clone();
        } else if (version.equals("1.1.1")){
            return OPERATIONS_METADATA_111.clone();
        } else {
            throw new IllegalArgumentException("unexpected version:" + version);
        }
    }

    /**
     * Generates the base capabilities for a WMS from the service metadata.
     *
     * @param metadata the service metadata
     * @return the service base capabilities
     */
    public static GetCapabilitiesResponse createCapabilities(final String version, final Details metadata) {
        ensureNonNull("metadata", metadata);
        ensureNonNull("version",  version);

        final Contact currentContact = metadata.getServiceContact();
        final AccessConstraint constraint = metadata.getServiceConstraints();

        final AbstractServiceIdentification servIdent;
        if (constraint != null) {
            servIdent = WCSXmlFactory.createServiceIdentification(version, metadata.getName(), metadata.getDescription(),
                    metadata.getKeywords(), "WCS", metadata.getVersions(),
                    constraint.getFees(), Arrays.asList(constraint.getAccessConstraint()));
        } else {
            servIdent = WCSXmlFactory.createServiceIdentification(version, metadata.getName(), metadata.getDescription(),
                    metadata.getKeywords(), "WCS", metadata.getVersions(),
                    null, new ArrayList<String>());
        }

        // Create provider part.
        final AbstractServiceProvider servProv;
        if (currentContact != null) {
            final AbstractContact contact = WCSXmlFactory.buildContact(version, currentContact.getPhone(), currentContact.getFax(),
                    currentContact.getEmail(), currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                    currentContact.getZipCode(), currentContact.getCountry(), currentContact.getHoursOfService(), currentContact.getContactInstructions());

            final AbstractResponsiblePartySubset responsible = WCSXmlFactory.buildResponsiblePartySubset(version, currentContact.getFullname(), currentContact.getPosition(), contact, null);

            // url
            AbstractOnlineResourceType orgUrl = null;
            if (currentContact.getUrl() != null) {
                orgUrl = WCSXmlFactory.buildOnlineResource(version, currentContact.getUrl());
            }
            servProv = WCSXmlFactory.buildServiceProvider(version, currentContact.getOrganisation(), orgUrl, responsible);
        } else {
            servProv = WCSXmlFactory.buildServiceProvider(version, "", null, null);
        }

        // Create capabilities base.
        return WCSXmlFactory.createCapabilitiesResponse(version, servIdent, servProv, null, null, null);
    }
}
