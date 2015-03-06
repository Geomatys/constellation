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
package org.constellation.coverage.ws.rs;

// Jersey dependencies

import org.constellation.ServiceDef;
import org.constellation.ServiceDef.Specification;
import org.constellation.api.QueryConstants;
import org.constellation.coverage.ws.DefaultWCSWorker;
import org.constellation.coverage.ws.WCSWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.MimeType;
import org.constellation.ws.Worker;
import org.constellation.ws.rs.GridWebService;
import org.constellation.ws.rs.provider.SchemaLocatedExceptionResponse;
import org.geotoolkit.client.RequestsUtilities;
import org.geotoolkit.gml.xml.v311.CodeType;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.gml.xml.v311.GridLimitsType;
import org.geotoolkit.gml.xml.v311.GridType;
import org.geotoolkit.ogc.xml.exception.ServiceExceptionReport;
import org.geotoolkit.ogc.xml.exception.ServiceExceptionType;
import org.geotoolkit.ows.xml.AcceptFormats;
import org.geotoolkit.ows.xml.AcceptVersions;
import org.geotoolkit.ows.xml.ExceptionResponse;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.ows.xml.Sections;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.resources.Errors;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.wcs.xml.DescribeCoverage;
import org.geotoolkit.wcs.xml.DescribeCoverageResponse;
import org.geotoolkit.wcs.xml.DomainSubset;
import org.geotoolkit.wcs.xml.GetCapabilities;
import org.geotoolkit.wcs.xml.GetCapabilitiesResponse;
import org.geotoolkit.wcs.xml.GetCoverage;
import org.geotoolkit.wcs.xml.TimeSequence;
import org.geotoolkit.wcs.xml.WCSMarshallerPool;
import org.geotoolkit.wcs.xml.WCSXmlFactory;
import org.geotoolkit.wcs.xml.v111.GridCrsType;
import org.geotoolkit.wcs.xml.v111.RangeSubsetType.FieldSubset;

import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.logging.Level;

import static org.constellation.api.QueryConstants.ACCEPT_FORMATS_PARAMETER;
import static org.constellation.api.QueryConstants.REQUEST_PARAMETER;
import static org.constellation.api.QueryConstants.SECTIONS_PARAMETER;
import static org.constellation.api.QueryConstants.UPDATESEQUENCE_PARAMETER;
import static org.constellation.api.QueryConstants.VERSION_PARAMETER;
import static org.constellation.coverage.ws.WCSConstant.ASCII_GRID;
import static org.constellation.coverage.ws.WCSConstant.BMP;
import static org.constellation.coverage.ws.WCSConstant.DESCRIBECOVERAGE;
import static org.constellation.coverage.ws.WCSConstant.GEOTIFF;
import static org.constellation.coverage.ws.WCSConstant.GETCAPABILITIES;
import static org.constellation.coverage.ws.WCSConstant.GETCOVERAGE;
import static org.constellation.coverage.ws.WCSConstant.GIF;
import static org.constellation.coverage.ws.WCSConstant.JPEG;
import static org.constellation.coverage.ws.WCSConstant.JPG;
import static org.constellation.coverage.ws.WCSConstant.KEY_BBOX;
import static org.constellation.coverage.ws.WCSConstant.KEY_BOUNDINGBOX;
import static org.constellation.coverage.ws.WCSConstant.KEY_CATEGORIES;
import static org.constellation.coverage.ws.WCSConstant.KEY_COVERAGE;
import static org.constellation.coverage.ws.WCSConstant.KEY_CRS;
import static org.constellation.coverage.ws.WCSConstant.KEY_DEPTH;
import static org.constellation.coverage.ws.WCSConstant.KEY_FORMAT;
import static org.constellation.coverage.ws.WCSConstant.KEY_GRIDBASECRS;
import static org.constellation.coverage.ws.WCSConstant.KEY_GRIDCS;
import static org.constellation.coverage.ws.WCSConstant.KEY_GRIDOFFSETS;
import static org.constellation.coverage.ws.WCSConstant.KEY_GRIDORIGIN;
import static org.constellation.coverage.ws.WCSConstant.KEY_GRIDTYPE;
import static org.constellation.coverage.ws.WCSConstant.KEY_HEIGHT;
import static org.constellation.coverage.ws.WCSConstant.KEY_IDENTIFIER;
import static org.constellation.coverage.ws.WCSConstant.KEY_COVERAGE_ID;
import static org.constellation.coverage.ws.WCSConstant.KEY_INTERPOLATION;
import static org.constellation.coverage.ws.WCSConstant.KEY_MEDIA_TYPE;
import static org.constellation.coverage.ws.WCSConstant.KEY_RANGESUBSET;
import static org.constellation.coverage.ws.WCSConstant.KEY_RESPONSE_CRS;
import static org.constellation.coverage.ws.WCSConstant.KEY_RESX;
import static org.constellation.coverage.ws.WCSConstant.KEY_RESY;
import static org.constellation.coverage.ws.WCSConstant.KEY_RESZ;
import static org.constellation.coverage.ws.WCSConstant.KEY_SECTION;
import static org.constellation.coverage.ws.WCSConstant.KEY_TIME;
import static org.constellation.coverage.ws.WCSConstant.KEY_TIMESEQUENCE;
import static org.constellation.coverage.ws.WCSConstant.KEY_WIDTH;
import static org.constellation.coverage.ws.WCSConstant.MATRIX;
import static org.constellation.coverage.ws.WCSConstant.NETCDF;
import static org.constellation.coverage.ws.WCSConstant.PNG;
import static org.constellation.coverage.ws.WCSConstant.TIF;
import static org.constellation.coverage.ws.WCSConstant.TIFF;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_DIMENSION_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_FORMAT;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;
import static org.geotoolkit.ows.xml.OWSExceptionCode.VERSION_NEGOTIATION_FAILED;
import org.geotoolkit.wcs.xml.v200.GetCoverageType;

// J2SE dependencies
// Constellation dependencies
// Geotoolkit dependencies



/**
 * The Web Coverage Service (WCS) REST facade for Constellation.
 * <p>
 * This service implements the following methods:
 * <ul>
 *   <li>{@code GetCoverage(.)}</li>
 *   <li>{@code DescribeCoverage(.)}</li>
 *   <li>{@code GetCapabilities(.)}</li>
 * </ul>
 * of the Open Geospatial Consortium (OGC) WCS specifications. As of
 * Constellation version 0.3, this Web Coverage Service complies with the
 * specification version 1.0.0 (OGC document 03-065r6) and mostly complies with
 * specification version 1.1.1 (OGC document 06-083r8).
 * </p>
 *
 * @version $Id$
 * @author Guilhem Legal
 * @author Cédric Briançon
 * @since 0.3
 */
@Path("wcs/{serviceId}")
@Singleton
public class WCSService extends GridWebService<WCSWorker> {

    /**
     * Build a new instance of the webService and initialize the JAXB context.
     */
    public WCSService() {
        super(Specification.WCS);
        setFullRequestLog(true);
        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext(WCSMarshallerPool.getInstance());

        LOGGER.log(Level.INFO, "WCS REST service running ({0} instances)\n", getWorkerMapSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getWorkerClass() {
        return DefaultWCSWorker.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response treatIncomingRequest(Object objectRequest, final WCSWorker worker) {
        ServiceDef serviceDef = null;

        try {

            String request = "";
            // if the request is not an xml request we fill the request parameter.
            if (objectRequest == null) {
                request = getParameter(REQUEST_PARAMETER, true);
                objectRequest = adaptQuery(request, worker);
            }

            if (objectRequest instanceof GetCapabilities){
                final GetCapabilities getcaps = (GetCapabilities)objectRequest;
                serviceDef              = worker.getVersionFromNumber(getcaps.getVersion());

                final GetCapabilitiesResponse capsResponse = worker.getCapabilities(getcaps);
                return Response.ok(capsResponse, MimeType.TEXT_XML).build();
            }

            if (objectRequest instanceof DescribeCoverage) {
                final DescribeCoverage desccov = (DescribeCoverage)objectRequest;

                if (desccov.getVersion() == null) {
                    throw new CstlServiceException("The parameter version must be specified",
                        MISSING_PARAMETER_VALUE, "version");
                }
                serviceDef = worker.getVersionFromNumber(desccov.getVersion());
                final DescribeCoverageResponse describeResponse = worker.describeCoverage(desccov);
                return Response.ok(describeResponse, MimeType.TEXT_XML).build();
            }

            if (objectRequest instanceof GetCoverage) {
                final GetCoverage getcov = (GetCoverage)objectRequest;

                if (getcov.getVersion() == null) {
                    throw new CstlServiceException("The parameter version must be specified",
                        MISSING_PARAMETER_VALUE, "version");
                } else if (getcov.getFormat() == null) {
                    throw new CstlServiceException("The parameter format must be specified",
                        MISSING_PARAMETER_VALUE, "format");
                }
                serviceDef = worker.getVersionFromNumber(getcov.getVersion());
                String format = getcov.getFormat();
                if (!isSupportedFormat(getcov.getVersion().toString(), format)){
                    throw new CstlServiceException("The format specified is not recognized. Please choose a known format " +
                        "for your coverage, defined in a DescribeCoverage response on the coverage.", INVALID_FORMAT,
                        KEY_FORMAT.toLowerCase());
                }

                format = getOutputFormat(format, getcov.getMediaType());
                return Response.ok(worker.getCoverage(getcov), format).build();
            }

            throw new CstlServiceException("This service can not handle the requested operation: " + request + ".",
                                           OPERATION_NOT_SUPPORTED, QueryConstants.REQUEST_PARAMETER.toLowerCase());

        } catch (CstlServiceException ex) {
            /*
             * This block handles all the exceptions which have been generated
             * anywhere in the service and transforms them to a response message
             * for the protocol stream which JAXB, in this case, will then
             * marshall and serialize into an XML message HTTP response.
             */
            return processExceptionResponse(ex, serviceDef, worker);

        }
    }

    private String getOutputFormat(String format, String mediaType) {
        if (mediaType != null) {
            return mediaType;
        }
        
        if (format.equalsIgnoreCase(MATRIX)) {
            format = MimeType.MATRIX;
        } else if (format.equalsIgnoreCase(ASCII_GRID)) {
            format = MimeType.ASCII_GRID;
        } else if (format.equalsIgnoreCase(NETCDF)) {
            format = MimeType.NETCDF;
        } else if (format.equalsIgnoreCase(PNG)) {
            format = MimeType.IMAGE_PNG;
        } else if (format.equalsIgnoreCase(GIF)) {
            format = MimeType.IMAGE_GIF;
        } else if (format.equalsIgnoreCase(BMP)) {
            format = MimeType.IMAGE_BMP;
        } else if (format.equalsIgnoreCase(JPEG) || format.equalsIgnoreCase(JPG)) {
            format = MimeType.IMAGE_JPEG;
        } else if (format.equalsIgnoreCase(TIF) || format.equalsIgnoreCase(TIFF)) {
            format = MimeType.IMAGE_TIFF;
        } else if (format.equalsIgnoreCase(GEOTIFF)) {
            format = "image/geotiff";
        }
        return format;
    }

    private boolean isSupportedFormat(final String version, final String format) {
        if (version.equals("2.0.0")) {
            return format.equalsIgnoreCase(MimeType.IMAGE_TIFF) || format.equalsIgnoreCase(MimeType.NETCDF);
        } else {
            return format.equalsIgnoreCase(MimeType.IMAGE_BMP)  ||format.equalsIgnoreCase(BMP)  ||
                   format.equalsIgnoreCase(MimeType.IMAGE_GIF)   ||format.equalsIgnoreCase(GIF)  ||
                   format.equalsIgnoreCase(MimeType.IMAGE_JPEG)  ||format.equalsIgnoreCase(JPEG) ||
                   format.equalsIgnoreCase(JPG)                  ||format.equalsIgnoreCase(TIF)  ||
                   format.equalsIgnoreCase(MimeType.IMAGE_TIFF)  ||format.equalsIgnoreCase(TIFF) ||
                   format.equalsIgnoreCase(MimeType.IMAGE_PNG)  ||format.equalsIgnoreCase(PNG)  ||
                   format.equalsIgnoreCase(GEOTIFF)              ||format.equalsIgnoreCase(NETCDF) ||
                   format.equalsIgnoreCase(MATRIX)               ||format.equalsIgnoreCase(ASCII_GRID);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef, final Worker w) {
        logException(ex);

        // SEND THE HTTP RESPONSE
        final ExceptionResponse report;
        if (serviceDef == null) {
            // TODO: Get the best version for WCS. For the moment, just 1.0.0.
            serviceDef = ServiceDef.WCS_1_0_0;
            //serviceDef = getBestVersion(null);
        }
        final String locator = ex.getLocator();
        final String code;
        if (ex.getExceptionCode() instanceof ExceptionCode) {
            code = StringUtilities.transformCodeName(ex.getExceptionCode().name());
        } else {
            code = ex.getExceptionCode().name();
        }
        if (serviceDef.owsCompliant) {
            report = new ExceptionReport(ex.getMessage(), code, locator, serviceDef.exceptionVersion.toString());
        } else {
            final ServiceExceptionReport exReport = new ServiceExceptionReport(serviceDef.exceptionVersion,
                         (locator == null) ? new ServiceExceptionType(ex.getMessage(), code) : new ServiceExceptionType(ex.getMessage(), code, locator));

            report = new SchemaLocatedExceptionResponse(exReport, "http://www.opengis.net/ogc http://schemas.opengis.net/wcs/1.0.0/OGC-exception.xsd");
        }
        return Response.ok(report, MimeType.APP_SE_XML).build();
    }

    public RequestBase adaptQuery(final String request, final Worker w) throws CstlServiceException {
        if (GETCAPABILITIES.equalsIgnoreCase(request)) {
            return adaptKvpGetCapabilitiesRequest(w);
        } else if (GETCOVERAGE.equalsIgnoreCase(request)) {
            return adaptKvpGetCoverageRequest(w);
        } else if (DESCRIBECOVERAGE.equalsIgnoreCase(request)) {
            return adaptKvpDescribeCoverageRequest(w);
        }
        throw new CstlServiceException("The operation " + request + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
    }

    /**
     * Build a new {@linkplain AbstractGetCapabilities GetCapabilities} request from
     * from a request formulated as a Key-Value Pair either in the URL or as a
     * plain text message body.
     *
     * @return a marshallable GetCapabilities request.
     * @throws CstlServiceException
     */
    private GetCapabilities adaptKvpGetCapabilitiesRequest(final Worker w) throws CstlServiceException {
        final String service = getParameter(QueryConstants.SERVICE_PARAMETER, true);
        if (!service.equalsIgnoreCase("WCS")) {
            throw new CstlServiceException("The parameter SERVICE must be specified as WCS",
                    MISSING_PARAMETER_VALUE, QueryConstants.SERVICE_PARAMETER.toLowerCase());
        }

        // TODO: find the best version when the WCS 1.1.1 will be fully implemented.
        //       For the moment, the version chosen is always the 1.0.0.

        String inputVersion = getParameter(QueryConstants.VERSION_PARAMETER, false);
        if (inputVersion == null) {
            inputVersion = getParameter("acceptversions", false);
            if (inputVersion == null) {
                inputVersion = w.getBestVersion(null).version.toString();
            } else {
                //we verify that the version is supported
                w.checkVersionSupported(inputVersion, true);
            }
        }
        final String finalVersion = w.getBestVersion(inputVersion).version.toString();

        final String updateSequence = getParameter(UPDATESEQUENCE_PARAMETER, false);

        
        if (finalVersion.equals("1.0.0")) {
            final String section = getParameter(KEY_SECTION, false);
            final Sections sections = OWSXmlFactory.buildSections(finalVersion, Arrays.asList(section));
            return WCSXmlFactory.createGetCapabilities(finalVersion, null, sections, null, updateSequence, service);
            
        } else if (finalVersion.equals("1.1.1") || finalVersion.equals("2.0.0")) {
            final String acceptformat = getParameter(ACCEPT_FORMATS_PARAMETER, false);
            final AcceptFormats formats = OWSXmlFactory.buildAcceptFormat(finalVersion, Arrays.asList(acceptformat));

            //We transform the String of sections in a list.
            //In the same time we verify that the requested sections are valid.
            final String section = getParameter(SECTIONS_PARAMETER, false);
            final List<String> requestedSections;
            if (section != null) {
                requestedSections = new ArrayList<>();
                final StringTokenizer tokens = new StringTokenizer(section, ",;");
                while (tokens.hasMoreTokens()) {
                    final String token = tokens.nextToken().trim();
                    if (SectionsType.getExistingSections(ServiceDef.WCS_1_1_1.version.toString()).contains(token)) {
                        requestedSections.add(token);
                    } else {
                        throw new CstlServiceException("The section " + token + " does not exist",
                                INVALID_PARAMETER_VALUE, KEY_SECTION.toLowerCase());
                    }
                }
            } else {
                //if there is no requested Sections we add all the sections
                requestedSections = SectionsType.getExistingSections(ServiceDef.WCS_1_1_1.version.toString());
            }
            final Sections sections = OWSXmlFactory.buildSections(finalVersion, requestedSections);
            final AcceptVersions versions = OWSXmlFactory.buildAcceptVersion(finalVersion, Arrays.asList(finalVersion));
            return WCSXmlFactory.createGetCapabilities(finalVersion, versions, sections, formats, updateSequence, service);
        } else {
            throw new CstlServiceException("The version number specified for this request " +
                    "is not handled.", VERSION_NEGOTIATION_FAILED, QueryConstants.VERSION_PARAMETER.toLowerCase());
        }
    }

    /**
     * Build a new {@linkplain AbstractDescribeCoverage DescribeCoverage}
     * request from a Key-Value Pair request.
     *
     * @return a marshallable DescribeCoverage request.
     * @throws CstlServiceException
     */
    private DescribeCoverage adaptKvpDescribeCoverageRequest(final Worker w) throws CstlServiceException {
        final String strVersion = getParameter(QueryConstants.VERSION_PARAMETER, true);
        w.checkVersionSupported(strVersion, false);
        final String coverage;
        if ("1.0.0".equals(strVersion)) {
            coverage = getParameter(KEY_COVERAGE, true);
        } else if ("1.1.1".equals(strVersion)) {
            coverage = getParameter(KEY_IDENTIFIER, true);
        } else if ("2.0.0".equals(strVersion)) {
            coverage = getParameter(KEY_COVERAGE_ID, true);
        } else {
            throw new CstlServiceException("The version number specified for this request " +
                    "is not handled.", VERSION_NEGOTIATION_FAILED, QueryConstants.VERSION_PARAMETER.toLowerCase());
        }
        return WCSXmlFactory.createDescribeCoverage(strVersion, Arrays.asList(coverage));
    }

    /**
     * Build a new {@linkplain AbstractGetCoverage GetCoverage} request from a
     * Key-Value Pair request.
     *
     * @return a marshallable GetCoverage request.
     * @throws CstlServiceException
     */
    private GetCoverage adaptKvpGetCoverageRequest(final Worker w) throws CstlServiceException {
        final String strVersion = getParameter(VERSION_PARAMETER, true);
        w.checkVersionSupported(strVersion, false);
        if (strVersion.equals("1.0.0")) {
            return adaptKvpGetCoverageRequest100();
         } else if (strVersion.equals("1.1.1")) {
            return adaptKvpGetCoverageRequest111();
         } else if (strVersion.equals("2.0.0")) {
            return adaptKvpGetCoverageRequest200();
         } else {
            throw new CstlServiceException("The version number specified for this request " +
                    "is not handled.", VERSION_NEGOTIATION_FAILED, QueryConstants.VERSION_PARAMETER.toLowerCase());
         }
    }

    
    /**
     * Generate a marshallable {@linkplain org.geotoolkit.wcs.xml.v200.GetCoverage GetCoverage}
     * request in version 1.0.0, from what the user specified.
     *
     * @return The GetCoverage request in version 2.0.0
     * @throws CstlServiceException
     */
    private GetCoverage adaptKvpGetCoverageRequest200() throws CstlServiceException {
        final String coverageID  = getParameter(KEY_COVERAGE_ID,  true);
        final String format      = getParameter(KEY_FORMAT,  false);
        final String mediaType   = getParameter(KEY_MEDIA_TYPE,  false);
         return new GetCoverageType(coverageID, format, mediaType);
    }
    
    /**
     * Generate a marshallable {@linkplain org.geotoolkit.wcs.xml.v100.GetCoverage GetCoverage}
     * request in version 1.0.0, from what the user specified.
     *
     * @return The GetCoverage request in version 1.0.0
     * @throws CstlServiceException
     */
    private GetCoverage adaptKvpGetCoverageRequest100() throws CstlServiceException {
        final String width  = getParameter(KEY_WIDTH,  false);
        final String height = getParameter(KEY_HEIGHT, false);
        final String depth  = getParameter(KEY_DEPTH,  false);

        final String resx   = getParameter(KEY_RESX,   false);
        final String resy   = getParameter(KEY_RESY,   false);
        final String resz   = getParameter(KEY_RESZ,   false);

        // temporal subset
        TimeSequence temporal = null;
        final String time = getParameter(KEY_TIME, false);
        if (time != null) {
            temporal = WCSXmlFactory.createTimeSequence("1.0.0", time);
        }

        /*
         * spatial subset
         */
        // the boundingBox/envelope
        final String bbox = getParameter(KEY_BBOX, false);
        if (bbox == null && time == null) {
            throw new CstlServiceException("Either BBOX or TIME parameter must be specified",
                                           MISSING_PARAMETER_VALUE);
        }
        List<DirectPositionType> pos = null;
        if (bbox != null) {
            pos = new ArrayList<>();
            final List<String> bboxValues = StringUtilities.toStringList(bbox);
            final double minimumLon = RequestsUtilities.toDouble(bboxValues.get(0));
            final double maximumLon = RequestsUtilities.toDouble(bboxValues.get(2));
            try {
                if (minimumLon > maximumLon) {
                    throw new IllegalArgumentException(
                            Errors.format(Errors.Keys.ILLEGAL_RANGE_2, minimumLon, maximumLon));
                }
                final double minimumLat = RequestsUtilities.toDouble(bboxValues.get(1));
                final double maximumLat = RequestsUtilities.toDouble(bboxValues.get(3));
                if (minimumLat > maximumLat) {
                    throw new IllegalArgumentException(
                            Errors.format(Errors.Keys.ILLEGAL_RANGE_2, minimumLat, maximumLat));
                }
                if (bboxValues.size() > 4) {
                    final double minimumDepth = RequestsUtilities.toDouble(bboxValues.get(4));
                    final double maximumDepth = RequestsUtilities.toDouble(bboxValues.get(5));
                    if (minimumLat > maximumLat) {
                        throw new IllegalArgumentException(
                                Errors.format(Errors.Keys.ILLEGAL_RANGE_2, minimumDepth, maximumDepth));
                    }
                    pos.add(new DirectPositionType(minimumLon, minimumLat, minimumDepth));
                    pos.add(new DirectPositionType(maximumLon, maximumLat, maximumDepth));
                } else {
                    pos.add(new DirectPositionType(minimumLon, minimumLat));
                    pos.add(new DirectPositionType(maximumLon, maximumLat));
                }
            } catch (IllegalArgumentException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
            }
        }
        final EnvelopeType envelope = new EnvelopeType(pos, getParameter(KEY_CRS, true));

        if ((width == null || height == null) && (resx == null || resy == null)) {
            throw new CstlServiceException("You should specify either width/height or resx/resy.",
                    INVALID_DIMENSION_VALUE);
        }

        final List<String> axis = new ArrayList<>();
        axis.add("width");
        axis.add("height");
        int[] low = null;
        int[] high = null;
        if (width != null && height != null) {
            if (depth != null) {
                low  = new int[3];
                high = new int[3];
                axis.add("depth");
                low[2]  = 0;
                high[2] = Integer.valueOf(depth);
            } else {
                low  = new int[2];
                high = new int[2];
            }
            low[0] = 0;
            low[1] = 0;
            high[0] = Integer.valueOf(width);
            high[1] = Integer.valueOf(height);

        }
        final GridLimitsType limits = new GridLimitsType(low, high);
        final GridType grid = new GridType(limits, axis);

        //spatial subset
        final org.geotoolkit.wcs.xml.v100.SpatialSubsetType spatial =
                new org.geotoolkit.wcs.xml.v100.SpatialSubsetType(envelope, grid);

        //domain subset
        final DomainSubset domain = WCSXmlFactory.createDomainSubset("1.0.0", temporal, spatial);

        //range subset
        final org.geotoolkit.wcs.xml.v100.RangeSubsetType rangeSubset;
        final String categories = getParameter(KEY_CATEGORIES, false);
        if (categories != null) {
            final List<Double[]> ranges = RequestsUtilities.toCategoriesRange(categories);
            final List<Object> objects = new ArrayList<>();
            for (Double[] range : ranges) {
                if (Objects.equals(range[0], range[1])) {
                    objects.add(new org.geotoolkit.wcs.xml.v100.TypedLiteralType(String.valueOf(range[0]), "xs:double"));
                } else {
                    objects.add(new org.geotoolkit.wcs.xml.v100.IntervalType(
                                    new org.geotoolkit.wcs.xml.v100.TypedLiteralType(String.valueOf(range[0]), "xs:double"),
                                    new org.geotoolkit.wcs.xml.v100.TypedLiteralType(String.valueOf(range[1]), "xs:double")));
                }
            }

            final org.geotoolkit.wcs.xml.v100.RangeSubsetType.AxisSubset axisSubset =
                    new org.geotoolkit.wcs.xml.v100.RangeSubsetType.AxisSubset(KEY_CATEGORIES, objects);
            final List<org.geotoolkit.wcs.xml.v100.RangeSubsetType.AxisSubset> axisSubsets = Collections.singletonList(axisSubset);
            rangeSubset = new org.geotoolkit.wcs.xml.v100.RangeSubsetType(axisSubsets);
        } else {
            rangeSubset = null;
        }

        //interpolation method
        final String interpolation = getParameter(KEY_INTERPOLATION, false);

        //output
        final List<Double> resolutions;
        if (resx != null && resy != null) {
            resolutions = new ArrayList<>();
            resolutions.add(Double.valueOf(resx));
            resolutions.add(Double.valueOf(resy));
            if (resz != null) {
                resolutions.add(Double.valueOf(resz));
            }
        } else {
            resolutions = null;
        }
        final org.geotoolkit.wcs.xml.v100.OutputType output =
                new org.geotoolkit.wcs.xml.v100.OutputType(getParameter(KEY_FORMAT, true),
                                                           getParameter(KEY_RESPONSE_CRS, false),
                                                           resolutions);

        return WCSXmlFactory.createGetCoverage("1.0.0", getParameter(KEY_COVERAGE, true), domain, rangeSubset, interpolation, output);
    }

    /**
     * Generate a marshallable {@linkplain org.geotoolkit.wcs.xml.v111.GetCoverage GetCoverage}
     * request in version 1.1.1, from what the user specified.
     *
     * @return The GetCoverage request in version 1.1.1
     * @throws CstlServiceException
     */
    private GetCoverage adaptKvpGetCoverageRequest111() throws CstlServiceException {
        // temporal subset
        TimeSequence temporal = null;
        final String timeParameter = getParameter(KEY_TIMESEQUENCE, false);
        if (timeParameter != null) {
            if (timeParameter.indexOf('/') == -1) {
                temporal = WCSXmlFactory.createTimeSequence("1.1.1", timeParameter);
            } else {
                throw new CstlServiceException("The service does not handle TimePeriod",
                        INVALID_PARAMETER_VALUE);
            }
        }

        /*
         * spatial subset
         */
        // the boundingBox/envelope
        String bbox = getParameter(KEY_BOUNDINGBOX, true);
        final String crs;
        if (bbox.indexOf(',') != -1) {
            crs = bbox.substring(bbox.lastIndexOf(',') + 1, bbox.length());
            bbox = bbox.substring(0, bbox.lastIndexOf(','));
        } else {
            throw new CstlServiceException("The correct pattern for BoundingBox parameter are" +
                                           " crs,minX,minY,maxX,maxY,CRS",
                                           INVALID_PARAMETER_VALUE, KEY_BOUNDINGBOX.toLowerCase());
        }
        BoundingBoxType envelope = null;

        if (bbox != null) {
            final StringTokenizer tokens = new StringTokenizer(bbox, ",;");
            final Double[] coordinates = new Double[tokens.countTokens()];
            int i = 0;
            while (tokens.hasMoreTokens()) {
                coordinates[i] = RequestsUtilities.toDouble(tokens.nextToken());
                i++;
            }
            if (i < 4) {
                throw new CstlServiceException("The correct pattern for BoundingBox parameter are" +
                                               " crs,minX,minY,maxX,maxY,CRS",
                                               INVALID_PARAMETER_VALUE, KEY_BOUNDINGBOX.toLowerCase());
            }
            envelope = new BoundingBoxType(crs, coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
        }

        //domain subset
        final DomainSubset domain = WCSXmlFactory.createDomainSubset("1.1.1", temporal, envelope);

        //range subset.
        org.geotoolkit.wcs.xml.v111.RangeSubsetType range = null;
        final String rangeSubset = getParameter(KEY_RANGESUBSET, false);
        if (rangeSubset != null) {
            //for now we don't handle Axis Identifiers
            if (rangeSubset.indexOf('[') != -1 || rangeSubset.indexOf(']') != -1) {
                throw new CstlServiceException("The service does not handle axis identifiers",
                        INVALID_PARAMETER_VALUE, "axis");
            }

            final StringTokenizer tokens = new StringTokenizer(rangeSubset, ";");
            final List<FieldSubset> fields = new ArrayList<>(tokens.countTokens());
            while (tokens.hasMoreTokens()) {
                final String value = tokens.nextToken();
                String interpolation = null;
                String rangeIdentifier;
                if (value.indexOf(':') != -1) {
                    rangeIdentifier = value.substring(0, rangeSubset.indexOf(':'));
                    interpolation = value.substring(rangeSubset.indexOf(':') + 1);
                } else {
                    rangeIdentifier = value;
                }
                fields.add(new FieldSubset(rangeIdentifier, interpolation));
            }

            range = new org.geotoolkit.wcs.xml.v111.RangeSubsetType(fields);
        }


        String gridType = getParameter(KEY_GRIDTYPE, false);
        if (gridType == null) {
            gridType = "urn:ogc:def:method:WCS:1.1:2dSimpleGrid";
        }
        String gridOrigin = getParameter(KEY_GRIDORIGIN, false);
        if (gridOrigin == null) {
            gridOrigin = "0.0,0.0";
        }

        StringTokenizer tokens = new StringTokenizer(gridOrigin, ",;");
        final List<Double> origin = new ArrayList<>(tokens.countTokens());
        while (tokens.hasMoreTokens()) {
            origin.add(RequestsUtilities.toDouble(tokens.nextToken()));
        }

        final String gridOffsets = getParameter(KEY_GRIDOFFSETS, false);
        final List<Double> offset = new ArrayList<>();
        if (gridOffsets != null) {
            tokens = new StringTokenizer(gridOffsets, ",;");
            while (tokens.hasMoreTokens()) {
                offset.add(RequestsUtilities.toDouble(tokens.nextToken()));
            }
        }
        String gridCS = getParameter(KEY_GRIDCS, false);
        if (gridCS == null) {
            gridCS = "urn:ogc:def:cs:OGC:0.0:Grid2dSquareCS";
        }

        //output
        final CodeType codeCRS = new CodeType(crs);
        final GridCrsType grid = new GridCrsType(codeCRS, getParameter(KEY_GRIDBASECRS, false), gridType,
                origin, offset, gridCS, "");
        final org.geotoolkit.wcs.xml.v111.OutputType output =
                new org.geotoolkit.wcs.xml.v111.OutputType(grid, getParameter(KEY_FORMAT, true));

        return WCSXmlFactory.createGetCoverage("1.1.1", getParameter(KEY_IDENTIFIER, true), domain, range, null, output);
    }
}
