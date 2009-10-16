/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2009, Geomatys
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
package org.constellation.coverage.ws.rs;

// Jersey dependencies
import com.sun.jersey.spi.resource.Singleton;

// J2SE dependencies
import java.awt.image.RenderedImage;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.coverage.ws.WCSWorker;
import org.constellation.util.StringUtilities;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.ServiceExceptionReport;
import org.constellation.ws.ServiceExceptionType;
import org.constellation.ws.rs.OGCWebService;
import static org.constellation.query.wcs.WCSQuery.*;

// Geotoolkit dependencies
import org.geotoolkit.gml.xml.v311.CodeType;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311.GridLimitsType;
import org.geotoolkit.gml.xml.v311.GridType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.ows.xml.v110.AcceptFormatsType;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.resources.Errors;
import org.geotoolkit.wcs.xml.DescribeCoverage;
import org.geotoolkit.wcs.xml.DescribeCoverageResponse;
import org.geotoolkit.wcs.xml.GetCapabilities;
import org.geotoolkit.wcs.xml.GetCapabilitiesResponse;
import org.geotoolkit.wcs.xml.GetCoverage;
import org.geotoolkit.wcs.xml.v111.GridCrsType;
import org.geotoolkit.wcs.xml.v111.RangeSubsetType.FieldSubset;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;


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
@Path("wcs")
@Singleton
public class WCSService extends OGCWebService {
    /**
     * The worker which will perform the core logic for this service.
     */
    private final WCSWorker worker;

    /**
     * Build a new instance of the webService and initialize the JAXB marshaller.
     *
     * @throws JAXBException if the initialization of the {@link JAXBContext} fails.
     */
    public WCSService() throws JAXBException {
        super(ServiceDef.WCS_1_1_1, ServiceDef.WCS_1_0_0);

        setFullRequestLog(true);
        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext("org.constellation.ws:" +
                      "org.geotoolkit.ows.xml.v100:" +
                      "org.geotoolkit.wcs.xml.v100:" +
                      "org.geotoolkit.wcs.xml.v111:" +
                      "org.geotoolkit.gml.xml.v311",
                      "");

        worker = new WCSWorker(getMarshallerPool());
        LOGGER.info("WCS service running");
    }

    /**
     * Treat the incoming request, contained either in the {@link UriInfo} 
     * injected context variable or in the parameter, then call the right 
     * function in the worker.
     *
     * @param objectRequest An object holding the request received, if this is 
     *                        not in the {@code UriInfo} variable.
     * @return The response to the request, either an image or an XML response.
     * @throws JAXBException
     */
    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        final UriInfo uriContext = getUriContext();

        Marshaller marshaller = null;
        ServiceDef serviceDef = null;
        try {

            marshaller = getMarshallerPool().acquireMarshaller();
            // Handle an empty request by sending a basic web page.
            if ((null == objectRequest) && (0 == uriContext.getQueryParameters().size())) {
                return Response.ok(getIndexPage(), MimeType.TEXT_HTML).build();
            }

            String request = "";
            if (objectRequest instanceof JAXBElement) {
                objectRequest = ((JAXBElement) objectRequest).getValue();
            }

            // if the request is not an xml request we fill the request parameter.
            if (objectRequest == null) {
                request = getParameter(KEY_REQUEST, true);
            }

            //TODO: fix logging of request, which may be in the objectRequest 
            //      and not in the parameter.
            logParameters();
            
            if ( GETCAPABILITIES.equalsIgnoreCase(request) || (objectRequest instanceof GetCapabilities) )
            {
                GetCapabilities getcaps = (GetCapabilities)objectRequest;
                if (getcaps == null) {
                    getcaps = adaptKvpGetCapabilitiesRequest();
                }
                serviceDef = getVersionFromNumber(getcaps.getVersion().toString());
                //TODO: is this necessary?
                worker.internalInitServletContext(getServletContext());
                worker.internalInitUriContext(uriContext);
                
                final GetCapabilitiesResponse capsResponse = worker.getCapabilities(getcaps);
                final StringWriter sw = new StringWriter();
                marshaller.marshal(capsResponse, sw);
                return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
            }
            
            if ( DESCRIBECOVERAGE.equalsIgnoreCase(request) || (objectRequest instanceof DescribeCoverage) )
            {
                DescribeCoverage desccov = (DescribeCoverage)objectRequest;
                
                //TODO: move me into the worker.
                //verifyBaseParameter(0);
                //TODO: move me into the worker.
                //The Constellation WCS does not currently implement the "store" mechanism.
                /*String store = getParameter(KEY_STORE, false);
                if (  store != null  &&  store.trim().equalsIgnoreCase("true")  ) {
                    throw new CstlServiceException("The service does not implement the store mechanism.",
                                   NO_APPLICABLE_CODE, "store");
                }*/
                
                if (desccov == null) {
                    desccov = adaptKvpDescribeCoverageRequest();
                }
                serviceDef = getVersionFromNumber(desccov.getVersion().toString());
                final DescribeCoverageResponse describeResponse = worker.describeCoverage(desccov);
                //we marshall the response and return the XML String
                final StringWriter sw = new StringWriter();
                marshaller.marshal(describeResponse, sw);
                return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
            }
            
            if ( GETCOVERAGE.equalsIgnoreCase(request) || (objectRequest instanceof GetCoverage) )
            {
                GetCoverage getcov = (GetCoverage)objectRequest;
                //TODO: move me into the worker.
                //verifyBaseParameter(0);
                
                if (getcov == null) {
                    getcov = adaptKvpGetCoverageRequest();
                }
                serviceDef = getVersionFromNumber(getcov.getVersion().toString());
                String format = getcov.getFormat();
                if (!format.equalsIgnoreCase(MimeType.IMAGE_BMP)  && !format.equalsIgnoreCase(BMP)  &&
                    !format.equalsIgnoreCase(MimeType.IMAGE_GIF)  && !format.equalsIgnoreCase(GIF)  &&
                    !format.equalsIgnoreCase(MimeType.IMAGE_JPEG) && !format.equalsIgnoreCase(JPEG) &&
                    !format.equalsIgnoreCase(JPG)                 && !format.equalsIgnoreCase(TIF)  &&
                    !format.equalsIgnoreCase(MimeType.IMAGE_TIFF) && !format.equalsIgnoreCase(TIFF) &&
                    !format.equalsIgnoreCase(MimeType.IMAGE_PNG)  && !format.equalsIgnoreCase(PNG)  &&
                    !format.equalsIgnoreCase(GEOTIFF)             && !format.equalsIgnoreCase(NETCDF))
                {
                    throw new CstlServiceException("The format specified is not recognized. Please choose a known format " +
                        "for your coverage, defined in a DescribeCoverage response on the coverage.", INVALID_FORMAT, format);
                }
                final RenderedImage rendered = worker.getCoverage(getcov);
                if (format.equalsIgnoreCase(MATRIX)) {
                    format = "application/matrix";
                }
                // Convert the supported image type into known mime-type.
                if (format.equalsIgnoreCase(PNG)) {
                    format = MimeType.IMAGE_PNG;
                }
                if (format.equalsIgnoreCase(GIF)) {
                    format = MimeType.IMAGE_GIF;
                }
                if (format.equalsIgnoreCase(BMP)) {
                    format = MimeType.IMAGE_BMP;
                }
                if (format.equalsIgnoreCase(JPEG) || format.equalsIgnoreCase(JPG)) {
                    format = MimeType.IMAGE_JPEG;
                }
                if (format.equalsIgnoreCase(TIF) || format.equalsIgnoreCase(TIFF)) {
                    format = MimeType.IMAGE_TIFF;
                }
                return Response.ok(rendered, format).build();
            }

            throw new CstlServiceException("This service can not handle the requested operation: " + request + ".",
                                           OPERATION_NOT_SUPPORTED, "request");
            
        } catch (CstlServiceException ex) {
        	/* 
        	 * This block handles all the exceptions which have been generated 
        	 * anywhere in the service and transforms them to a response message 
        	 * for the protocol stream which JAXB, in this case, will then 
        	 * marshall and serialize into an XML message HTTP response.
        	 */
        	return processExceptionResponse(ex, marshaller, serviceDef);
            
        } finally {
            if (marshaller != null) {
                getMarshallerPool().release(marshaller);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, Marshaller marshaller,
                                                ServiceDef serviceDef) throws JAXBException
    {
        // LOG THE EXCEPTION
        // We do not want to log the full stack trace if this is an error
        // which seems to have been caused by the user.
        if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE)    &&
            !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) &&
            !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE)    &&
            !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED))
        {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
        } else {
            LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getLocalizedMessage() + '\n');
        }

        // SEND THE HTTP RESPONSE
        final Object report;
        if (serviceDef == null) {
            // TODO: Get the best version for WCS. For the moment, just 1.0.0.
            serviceDef = ServiceDef.WCS_1_0_0;
            //serviceDef = getBestVersion(null);
        }
        final String locator = ex.getLocator();
        if (isOWS(serviceDef)) {
            final String code = Util.transformCodeName(ex.getExceptionCode().name());
            report = new ExceptionReport(ex.getMessage(), code, locator, serviceDef.exceptionVersion.toString());
        } else {
            report = new ServiceExceptionReport(serviceDef.exceptionVersion,
                         (locator == null) ? new ServiceExceptionType(ex.getMessage(), ex.getExceptionCode()) :
                                             new ServiceExceptionType(ex.getMessage(), ex.getExceptionCode(), locator));
        }
        final StringWriter sw = new StringWriter();
        marshaller.marshal(report, sw);

        return Response.ok(Util.cleanSpecialCharacter(sw.toString()), MimeType.APP_SE_XML).build();
    }

    /**
     * Build a new {@linkplain AbstractGetCapabilities GetCapabilities} request from
     * from a request formulated as a Key-Value Pair either in the URL or as a 
     * plain text message body.
     *
     * @return a marshallable GetCapabilities request.
     * @throws CstlServiceException
     */
    private GetCapabilities adaptKvpGetCapabilitiesRequest() throws CstlServiceException {

        if (!getParameter(KEY_SERVICE, true).equalsIgnoreCase("WCS")) {
            throw new CstlServiceException("The parameter SERVICE must be specified as WCS",
                    MISSING_PARAMETER_VALUE, "service");
        }

        // TODO: find the best version when the WCS 1.1.1 will be fully implemented.
        //       For the moment, the version chosen is always the 1.0.0.

//        String inputVersion = getParameter(KEY_VERSION, false);
//        if (inputVersion == null) {
//            inputVersion = getParameter("acceptversions", false);
//            if (inputVersion == null) {
//                inputVersion = getBestVersion(null).version.toString();
//            } else {
//                //we verify that the version is supported
//                isVersionSupported(inputVersion);
//            }
//        }
//        final ServiceDef finalVersion = getBestVersion(inputVersion);

        final ServiceDef finalVersion = ServiceDef.WCS_1_0_0;
        if (finalVersion.equals(ServiceDef.WCS_1_0_0)) {
            return new org.geotoolkit.wcs.xml.v100.GetCapabilitiesType(getParameter(KEY_SECTION, false), null);
        } else if (finalVersion.equals(ServiceDef.WCS_1_1_1)) {
            final AcceptFormatsType formats = new AcceptFormatsType(getParameter("AcceptFormats", false));

            //We transform the String of sections in a list.
            //In the same time we verify that the requested sections are valid.
            final String section = getParameter("Sections", false);
            final List<String> requestedSections;
            if (section != null) {
                requestedSections = new ArrayList<String>();
                final StringTokenizer tokens = new StringTokenizer(section, ",;");
                while (tokens.hasMoreTokens()) {
                    final String token = tokens.nextToken().trim();
                    if (SectionsType.getExistingSections(ServiceDef.WCS_1_1_1.version.toString()).contains(token)) {
                        requestedSections.add(token);
                    } else {
                        throw new CstlServiceException("The section " + token + " does not exist",
                                INVALID_PARAMETER_VALUE, "sections");
                    }
                }
            } else {
                //if there is no requested Sections we add all the sections
                requestedSections = SectionsType.getExistingSections(ServiceDef.WCS_1_1_1.version.toString());
            }
            final SectionsType sections = new SectionsType(requestedSections);
            final AcceptVersionsType versions = new AcceptVersionsType(ServiceDef.WCS_1_1_1.version.toString());
            return (GetCapabilities) new org.geotoolkit.wcs.xml.v111.GetCapabilitiesType(versions, sections, formats, null);
        } else {
            throw new CstlServiceException("The version number specified for this request " +
                    "is not handled.", VERSION_NEGOTIATION_FAILED, "version");
        }
    }

    /**
     * Build a new {@linkplain AbstractDescribeCoverage DescribeCoverage} 
     * request from a Key-Value Pair request.
     *
     * @return a marshallable DescribeCoverage request.
     * @throws CstlServiceException
     */
    private DescribeCoverage adaptKvpDescribeCoverageRequest() throws CstlServiceException {
        final String strVersion = getParameter(KEY_VERSION, true);
        isVersionSupported(strVersion);
        final ServiceDef serviceDef = getVersionFromNumber(strVersion);

        if (serviceDef.equals(ServiceDef.WCS_1_0_0)) {
            return new org.geotoolkit.wcs.xml.v100.DescribeCoverageType(getParameter(KEY_COVERAGE, true));
        } else if (serviceDef.equals(ServiceDef.WCS_1_1_1)) {
            return new org.geotoolkit.wcs.xml.v111.DescribeCoverageType(getParameter(KEY_IDENTIFIER, true));
        } else {
            throw new CstlServiceException("The version number specified for this request " +
                    "is not handled.", VERSION_NEGOTIATION_FAILED, "version");
        }
    }
    

    /**
     * Build a new {@linkplain AbstractGetCoverage GetCoverage} request from a 
     * Key-Value Pair request.
     *
     * @return a marshallable GetCoverage request.
     * @throws CstlServiceException
     */
    private GetCoverage adaptKvpGetCoverageRequest() throws CstlServiceException {
        final String strVersion = getParameter(KEY_VERSION, true);
        isVersionSupported(strVersion);
        final ServiceDef serviceDef = getVersionFromNumber(strVersion);
        if (serviceDef.equals(ServiceDef.WCS_1_0_0)) {
            return adaptKvpGetCoverageRequest100();
         } else if (serviceDef.equals(ServiceDef.WCS_1_1_1)) {
            return adaptKvpGetCoverageRequest111();
         } else {
            throw new CstlServiceException("The version number specified for this request " +
                    "is not handled.", VERSION_NEGOTIATION_FAILED, "version");
         }
    }
    

    /**
     * Generate a marshallable {@linkplain org.geotoolkit.wcs.xml.v100.GetCoverage GetCoverage}
     * request in version 1.0.0, from what the user specified.
     *
     * @return The GetCoverage request in version 1.0.0
     * @throws CstlServiceException
     */
    private org.geotoolkit.wcs.xml.v100.GetCoverageType adaptKvpGetCoverageRequest100()
                                                    throws CstlServiceException
    {
        final String width  = getParameter(KEY_WIDTH,  false);
        final String height = getParameter(KEY_HEIGHT, false);
        final String depth  = getParameter(KEY_DEPTH,  false);

        final String resx   = getParameter(KEY_RESX,   false);
        final String resy   = getParameter(KEY_RESY,   false);
        final String resz   = getParameter(KEY_RESZ,   false);

        // temporal subset
        org.geotoolkit.wcs.xml.v100.TimeSequenceType temporal = null;
        final String timeParameter = getParameter(KEY_TIME, false);
        if (timeParameter != null) {
            final TimePositionType time = new TimePositionType(timeParameter);
            temporal = new org.geotoolkit.wcs.xml.v100.TimeSequenceType(time);
        }

        /*
         * spatial subset
         */
        // the boundingBox/envelope
        final List<DirectPositionType> pos = new ArrayList<DirectPositionType>();
        final String bbox = getParameter(KEY_BBOX, true);
        if (bbox != null) {
            final List<String> bboxValues = StringUtilities.toStringList(bbox);
            final double minimumLon = StringUtilities.toDouble(bboxValues.get(0));
            final double maximumLon = StringUtilities.toDouble(bboxValues.get(2));
            try {
                if (minimumLon > maximumLon) {
                    throw new IllegalArgumentException(
                            Errors.format(Errors.Keys.BAD_RANGE_$2, minimumLon, maximumLon));
                }
                final double minimumLat = StringUtilities.toDouble(bboxValues.get(1));
                final double maximumLat = StringUtilities.toDouble(bboxValues.get(3));
                if (minimumLat > maximumLat) {
                    throw new IllegalArgumentException(
                            Errors.format(Errors.Keys.BAD_RANGE_$2, minimumLat, maximumLat));
                }
                pos.add(new DirectPositionType(minimumLon, maximumLon));
                pos.add(new DirectPositionType(minimumLat, maximumLat));
                if (bboxValues.size() > 4) {
                    final double minimumDepth = StringUtilities.toDouble(bboxValues.get(4));
                    final double maximumDepth = StringUtilities.toDouble(bboxValues.get(5));
                    if (minimumLat > maximumLat) {
                        throw new IllegalArgumentException(
                                Errors.format(Errors.Keys.BAD_RANGE_$2, minimumDepth, maximumDepth));
                    }
                    pos.add(new DirectPositionType(minimumDepth, maximumDepth));
                }
            } catch (IllegalArgumentException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
            }
        }
        final EnvelopeEntry envelope = new EnvelopeEntry(pos, getParameter(KEY_CRS, true));

        if (width == null && height == null && resx == null && resy == null) {
            throw new CstlServiceException("You should specify either width/height or resx/resy.",
                    INVALID_PARAMETER_VALUE);
        }

        final List<String> axis = new ArrayList<String>();
        axis.add("width");
        axis.add("height");
        List<BigInteger> low = null;
        List<BigInteger> high = null;
        if (width != null && height != null) {
            low = new ArrayList<BigInteger>();
            high = new ArrayList<BigInteger>();
            low.add(BigInteger.ZERO);
            low.add(BigInteger.ZERO);
            high.add(new BigInteger(width));
            high.add(new BigInteger(height));
            if (depth != null) {
                axis.add("depth");
                low.add(BigInteger.ZERO);
                high.add(new BigInteger(depth));
            }
        }
        final GridLimitsType limits = new GridLimitsType(low, high);
        final GridType grid = new GridType(limits, axis);

        //spatial subset
        final org.geotoolkit.wcs.xml.v100.SpatialSubsetType spatial =
                new org.geotoolkit.wcs.xml.v100.SpatialSubsetType(envelope, grid);

        //domain subset
        final org.geotoolkit.wcs.xml.v100.DomainSubsetType domain =
                new org.geotoolkit.wcs.xml.v100.DomainSubsetType(temporal, spatial);

        //range subset (not yet used)
        final org.geotoolkit.wcs.xml.v100.RangeSubsetType range = null;

        //interpolation method
        final org.geotoolkit.wcs.xml.v100.InterpolationMethod interpolation =
                org.geotoolkit.wcs.xml.v100.InterpolationMethod.fromValue(getParameter(KEY_INTERPOLATION, false));

        //output
        final List<Double> resolutions;
        if (resx != null && resy != null) {
            resolutions = new ArrayList<Double>();
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

        return new org.geotoolkit.wcs.xml.v100.GetCoverageType(
                getParameter(KEY_COVERAGE, true), domain, range, interpolation, output);
    }
    

    /**
     * Generate a marshallable {@linkplain org.geotoolkit.wcs.xml.v111.GetCoverage GetCoverage}
     * request in version 1.1.1, from what the user specified.
     *
     * @return The GetCoverage request in version 1.1.1
     * @throws CstlServiceException
     */
    private org.geotoolkit.wcs.xml.v111.GetCoverageType adaptKvpGetCoverageRequest111()
                                                    throws CstlServiceException
    {
        // temporal subset
        org.geotoolkit.wcs.xml.v111.TimeSequenceType temporal = null;
        final String timeParameter = getParameter(KEY_TIMESEQUENCE, false);
        if (timeParameter != null) {
            if (timeParameter.indexOf('/') == -1) {
                temporal = new org.geotoolkit.wcs.xml.v111.TimeSequenceType(new TimePositionType(timeParameter));
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
                                           INVALID_PARAMETER_VALUE, "boundingbox");
        }
        BoundingBoxType envelope = null;

        if (bbox != null) {
            final StringTokenizer tokens = new StringTokenizer(bbox, ",;");
            final Double[] coordinates = new Double[tokens.countTokens()];
            int i = 0;
            while (tokens.hasMoreTokens()) {
                coordinates[i] = StringUtilities.toDouble(tokens.nextToken());
                i++;
            }
            if (i < 4) {
                throw new CstlServiceException("The correct pattern for BoundingBox parameter are" +
                                               " crs,minX,minY,maxX,maxY,CRS",
                                               INVALID_PARAMETER_VALUE, "boundingbox");
            }
            envelope = new BoundingBoxType(crs, coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
        }

        //domain subset
        final org.geotoolkit.wcs.xml.v111.DomainSubsetType domain =
                new org.geotoolkit.wcs.xml.v111.DomainSubsetType(temporal, envelope);

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
            final List<FieldSubset> fields = new ArrayList<FieldSubset>(tokens.countTokens());
            while (tokens.hasMoreTokens()) {
                final String value = tokens.nextToken();
                String interpolation = null;
                String rangeIdentifier = null;
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
        final List<Double> origin = new ArrayList<Double>(tokens.countTokens());
        while (tokens.hasMoreTokens()) {
            origin.add(StringUtilities.toDouble(tokens.nextToken()));
        }

        final String gridOffsets = getParameter(KEY_GRIDOFFSETS, false);
        final List<Double> offset = new ArrayList<Double>();
        if (gridOffsets != null) {
            tokens = new StringTokenizer(gridOffsets, ",;");
            while (tokens.hasMoreTokens()) {
                offset.add(StringUtilities.toDouble(tokens.nextToken()));
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

        return new org.geotoolkit.wcs.xml.v111.GetCoverageType(
                new org.geotoolkit.ows.xml.v110.CodeType(getParameter(KEY_IDENTIFIER, true)),
                domain, range, output);
    }

    /**
     * Get an html page for the root resource.
     */
    private String getIndexPage(){
    	return  "<html>\n" +
    		"  <title>Constellation WCS</title>\n" +
    		"  <body>\n" +
    		"    <h1><i>Constellation:</i></h1>\n" +
    		"    <h1>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Web Coverage Service</h1>\n" +
    		"    <p>\n" +
    		"      In order to access this service, you must form a valid request.\n" +
    		"    </p\n" + 
    		"    <p>\n" +
    		"      Try using a <a href=\"" + getUriContext().getBaseUri() + "wcs"
    		                             + "?service=WCS&version=1.0.0&request=GetCapabilities&version=1.0.0\""
    		                             + ">Get Capabilities</a> request to obtain the 'Capabilities'<br>\n" +
    		"      document which describes the resources available on this server.\n" +
    		"    </p>\n" + 
    		"  </body>\n" +
    		"</html>\n";
    }


    @PreDestroy
    @Override
    public void destroy() {
        LOGGER.info("Shutting down the REST WCS service facade");
    }
}
