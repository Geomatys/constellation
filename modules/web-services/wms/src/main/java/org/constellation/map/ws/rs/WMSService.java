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
package org.constellation.map.ws.rs;

import org.constellation.util.Util;
import java.util.ArrayList;
import org.opengis.feature.type.Name;
import com.sun.jersey.spi.resource.Singleton;

//J2SE dependencies
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

//Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.map.ws.DefaultWMSWorker;
import org.constellation.map.ws.QueryContext;
import org.constellation.map.ws.WMSWorker;
import org.constellation.query.QueryAdapter;
import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetMap;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.util.TimeParser;
import org.constellation.writer.ExceptionFilterWriter;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.rs.GridWebService;

//GeotoolKit dependencies
import org.geotoolkit.client.util.RequestsUtilities;
import org.geotoolkit.display2d.service.DefaultPortrayalService;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.sld.xml.v110.DescribeLayerResponseType;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.Version;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.ogc.xml.exception.ServiceExceptionReport;
import org.geotoolkit.ogc.xml.exception.ServiceExceptionType;
import org.geotoolkit.wms.xml.WMSMarshallerPool;
import org.geotoolkit.xml.MarshallerPool;

//Geoapi dependencies
import org.opengis.geometry.Envelope;
import org.opengis.util.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.constellation.query.wms.WMSQuery.*;


/**
 * The REST facade to an OGC Web Map Service, implementing versions 1.1.1 and
 * 1.3.0.
 *
 * @version $Id$
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @since 0.1
 */
@Path("wms")
@Singleton
public class WMSService extends GridWebService {
    /**
     * The worker which will perform the core logic for this service.
     */
    private final WMSWorker worker;

    /**
     * Build a new instance of the webService and initialize the JAXB marshaller.
     */
    public WMSService() {
        super(ServiceDef.WMS_1_3_0_SLD, ServiceDef.WMS_1_1_1_SLD);

        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext(WMSMarshallerPool.getInstance());

        worker = new DefaultWMSWorker();
        setFullRequestLog(true);
        LOGGER.info("WMS service running");
    }

    /**
     * Treat the incoming request and call the right function in the worker.
     * <p>
     * The parent class will have processed the request sufficiently to ensure
     * all the relevant information is either in the {@code uriContext} field or
     * in the {@code Object} passed in as a parameter. Here we proceed a step
     * further to ensure the request is encapsulated in a Java object which we
     * then pass to the worker when calling the appropriate method.
     * </p>
     *
     * @param  objectRequest  an object encapsulating the request or {@code null}
     *                          if the request parameters are all in the
     *                          {@code uriContext} field.
     * @return a Response, either an image or an XML document depending on the
     *           user's request.
     * @throw JAXBException
     */
    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        final UriInfo uriContext = getUriContext();
        final QueryContext queryContext = new QueryContext();

        ServiceDef version = null;
        try {
            final String request = (String) getParameter(KEY_REQUEST, true);
            logParameters();

            //Handle user's requests.
            if (GETMAP.equalsIgnoreCase(request) || MAP.equalsIgnoreCase(request)) {
                String versionSt = getParameter(KEY_VERSION, false);
                if (versionSt == null) {
                    versionSt = getParameter(KEY_WMTVER, false);
                }
                if (versionSt == null) {
                    throw new CstlServiceException("The parameter version must be specified",
                        MISSING_PARAMETER_VALUE, "version");
                }
                isVersionSupported(versionSt);
                version = ServiceDef.getServiceDefinition(ServiceDef.Specification.WMS.toString(), versionSt);
                final GetMap requestMap = adaptGetMap(versionSt, true, queryContext);
                version = getVersionFromNumber(requestMap.getVersion().toString());
                final BufferedImage map = worker.getMap(requestMap);
                return Response.ok(map, requestMap.getFormat()).build();
            }
            if (GETFEATUREINFO.equalsIgnoreCase(request)) {
                String versionSt = getParameter(KEY_VERSION, false);
                if (versionSt == null) {
                    versionSt = getParameter(KEY_WMTVER, false);
                }
                if (versionSt == null) {
                    throw new CstlServiceException("The parameter version must be specified",
                        MISSING_PARAMETER_VALUE, "version");
                }
                isVersionSupported(versionSt);
                version = ServiceDef.getServiceDefinition(ServiceDef.Specification.WMS.toString(), versionSt);
                final GetFeatureInfo requestFeatureInfo = adaptGetFeatureInfo(versionSt, queryContext);
                version = getVersionFromNumber(requestFeatureInfo.getVersion().toString());
                final String result = worker.getFeatureInfo(requestFeatureInfo);
                //Need to reset the GML mime format to XML for browsers
                String infoFormat = requestFeatureInfo.getInfoFormat();
                if (infoFormat.equals(GML)) {
                    infoFormat = MimeType.APP_XML;
                }
                return Response.ok(result, infoFormat).build();
            }
            // For backward compatibility between WMS 1.1.1 and WMS 1.0.0, we handle the "Capabilities" request
            // as "GetCapabilities" request in version 1.1.1.
            if (GETCAPABILITIES.equalsIgnoreCase(request) || CAPABILITIES.equalsIgnoreCase(request)) {
                /*
                 * If the request is "Capabilities" then we set the version to 1.1.1, since it is
                 * the one which tries to stay compatible with the 1.0.0.
                 */
                String versionSt;
                if (CAPABILITIES.equalsIgnoreCase(request)) {
                    version = ServiceDef.WMS_1_1_1_SLD;
                    versionSt = version.version.toString();
                } else {
                    versionSt = getParameter(KEY_VERSION, false);
                    if (versionSt == null) {
                        // For backward compatibility with WMS 1.0.0, we try to find the version number
                        // from the WMTVER parameter too.
                        versionSt = getParameter(KEY_WMTVER, false);
                    }
                }
                final GetCapabilities requestCapab = adaptGetCapabilities(versionSt);
                if (version == null) {
                    version = getVersionFromNumber(requestCapab.getVersion().toString());
                }
                worker.initServletContext(getServletContext());
                worker.initUriContext(uriContext);
                final AbstractWMSCapabilities capabilities = worker.getCapabilities(requestCapab);
                
                return Response.ok(capabilities, requestCapab.getFormat()).build();
            }
            if (GETLEGENDGRAPHIC.equalsIgnoreCase(request)) {
                final GetLegendGraphic requestLegend = adaptGetLegendGraphic();
                version = getVersionFromNumber(requestLegend.getVersion().toString());
                final BufferedImage legend = worker.getLegendGraphic(requestLegend);
                return Response.ok(legend, requestLegend.getFormat()).build();
            }
            if (DESCRIBELAYER.equalsIgnoreCase(request)) {
                String versionSt = getParameter(KEY_VERSION, false);
                if (versionSt == null) {
                    versionSt = getParameter(KEY_WMTVER, false);
                }
                if (versionSt == null) {
                    throw new CstlServiceException("The parameter version must be specified",
                        MISSING_PARAMETER_VALUE, "version");
                }
                isVersionSupported(versionSt);
                version = ServiceDef.getServiceDefinition(ServiceDef.Specification.WMS.toString(), versionSt);
                final DescribeLayer describeLayer = adaptDescribeLayer(versionSt);
                version = getVersionFromNumber(describeLayer.getVersion().toString());
                worker.initUriContext(uriContext);
                final DescribeLayerResponseType response = worker.describeLayer(describeLayer);
                return Response.ok(response, MimeType.TEXT_XML).build();
            }
            throw new CstlServiceException("The operation " + request + " is not supported by the service",
                                           OPERATION_NOT_SUPPORTED, KEY_REQUEST.toLowerCase());
        } catch (CstlServiceException ex) {
            return processExceptionResponse(queryContext, ex, version);
        } 
    }

    /**
     * Generate an error response in image if query asks it.
     * Otherwise this call will fallback on normal xml error.
     */
    private Response processExceptionResponse(final QueryContext queryContext, final CstlServiceException ex, ServiceDef serviceDef) throws JAXBException {
        // Log the exception, even if the request specify that the exception should be in image.
        // In that precise case, printing the exception in the logs would allow administrator to
        // follow the user's error.
        if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE)    && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.MISSING_PARAMETER_VALUE)   &&
            !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.VERSION_NEGOTIATION_FAILED) &&
            !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE)   && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.INVALID_PARAMETER_VALUE)    &&
            !ex.getExceptionCode().equals(LAYER_NOT_DEFINED)   && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.LAYER_NOT_DEFINED)    &&
            !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED) && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.OPERATION_NOT_SUPPORTED))
        {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
        } else {
            LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getLocalizedMessage() + '\n');
        }
        // Now handle in image response or exception report.
        if (queryContext.isErrorInimage()) {
            final BufferedImage image = DefaultPortrayalService.writeException(ex, new Dimension(600, 400), queryContext.isOpaque());
            return Response.ok(image, queryContext.getExceptionImageFormat()).build();
        } else {
            return processExceptionResponse(ex, serviceDef);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef) throws JAXBException{

        if (serviceDef == null) {
            serviceDef = getBestVersion(null);
        }
        final Version version = serviceDef.exceptionVersion;
        final String locator = ex.getLocator();
        final ServiceExceptionReport report = new ServiceExceptionReport(version,
                (locator == null) ? new ServiceExceptionType(ex.getMessage(), ex.getExceptionCode()) :
                                    new ServiceExceptionType(ex.getMessage(), ex.getExceptionCode(), locator));
        final StringWriter sw = new StringWriter();
        /*
         * For WMS 1.1.1, we need to define another marshalling pool, with just the service exception
         * packages. Actually that package does not contain any reference to namespace, consequently
         * the service exception marshalled file will not contain namespaces definitions.
         * This is what we want since the service exception report already owns a DTD.
         */
        final MarshallerPool poolException = new MarshallerPool(
                Collections.singletonMap(MarshallerPool.ROOT_NAMESPACE_KEY, "http://www.opengis.net/ogc"),
                "org.geotoolkit.ogc.xml.exception");
        final Marshaller marsh = poolException.acquireMarshaller();
        try {
            if (serviceDef.equals(ServiceDef.WMS_1_1_1_SLD) || serviceDef.equals(ServiceDef.WMS_1_1_1)) {
                final ExceptionFilterWriter swException = new ExceptionFilterWriter(sw);
                try {
                    swException.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
                    swException.write("<!DOCTYPE ServiceExceptionReport SYSTEM \"http://schemas.opengis.net/wms/1.1.1/exception_1_1_1.dtd\">\n");
                } catch (IOException io) {
                    throw new JAXBException(io);
                }
                marsh.setProperty(Marshaller.JAXB_FRAGMENT, true);
                marsh.marshal(report, swException);
            } else {
                marsh.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
                        "http://www.opengis.net/ogc http://schemas.opengis.net/wms/1.3.0/exceptions_1_3_0.xsd");
                marsh.marshal(report, sw);
            }
        } finally {
            if (marsh != null) {
                poolException.release(marsh);
            }
        }
        final String mimeException = (serviceDef.version.equals(ServiceDef.WMS_1_1_1_SLD.version)) ?
                                                                MimeType.APP_SE_XML : MimeType.TEXT_XML;
        return Response.ok(StringUtilities.cleanSpecialCharacter(sw.toString()), mimeException).build();
    }

    /**
     * Converts a DescribeLayer request composed of string values, to a container
     * of real java objects.
     *
     * @param version The version of the web service detected.
     * @return The DescribeLayer request.
     * @throws CstlServiceException
     */
    private DescribeLayer adaptDescribeLayer(final String version) throws CstlServiceException {
        ServiceDef serviceDef = getVersionFromNumber(version);
        if (serviceDef == null) {
            serviceDef = getBestVersion(null);
        }
        isVersionSupported(version);
        final String strLayer  = getParameter(KEY_LAYERS,  true);
        final List<String> layers = StringUtilities.toStringList(strLayer);
        return new DescribeLayer(layers, serviceDef.version);
    }

    /**
     * Converts a GetCapabilities request composed of string values, to a container
     * of real java objects.
     *
     * @param version The version of the web service detected.
     * @return A GetCapabilities request.
     * @throws CstlServiceException
     */
    private GetCapabilities adaptGetCapabilities(final String version) throws CstlServiceException {
        final String service = getParameter(KEY_SERVICE, true);
        if (!ServiceDef.Specification.WMS.toString().equalsIgnoreCase(service)) {
            throw new CstlServiceException("Invalid service specified. Should be WMS.",
                    INVALID_PARAMETER_VALUE, KEY_SERVICE.toLowerCase());
        }
        if (version == null) {
            final ServiceDef capsService = getBestVersion(null);
            String format = getParameter(KEY_FORMAT, false);
            // Verify that the format is not null, and is not something totally different from the known
            // output formats. If it is the case, choose the default output format according to the version.
            if (format == null || format.isEmpty() ||
                    (!format.equalsIgnoreCase(MimeType.APP_XML) && !format.equalsIgnoreCase(MimeType.APPLICATION_XML)
                  && !format.equalsIgnoreCase(MimeType.TEXT_XML) && !format.equalsIgnoreCase(MimeType.APP_WMS_XML)))
            {
                format = (ServiceDef.WMS_1_1_1_SLD.version.equals(capsService.version)) ?
                    MimeType.APP_WMS_XML : MimeType.TEXT_XML;
            }
            return new GetCapabilities(capsService.version, format);
        }
        final ServiceDef bestVersion = getBestVersion(version);
        String format = getParameter(KEY_FORMAT, false);
        // Verify that the format is not null, and is not something totally different from the known
        // output formats. If it is the case, choose the default output format according to the version.
        if (format == null || format.isEmpty() ||
                (!format.equalsIgnoreCase(MimeType.APP_XML) && !format.equalsIgnoreCase(MimeType.APPLICATION_XML)
              && !format.equalsIgnoreCase(MimeType.TEXT_XML) && !format.equalsIgnoreCase(MimeType.APP_WMS_XML)))
        {
            format = (ServiceDef.WMS_1_1_1_SLD.version.equals(bestVersion.version)) ?
                     MimeType.APP_WMS_XML : MimeType.TEXT_XML;
        }
        return new GetCapabilities(bestVersion.version, format);
    }

    /**
     * Converts a GetFeatureInfo request composed of string values, to a container
     * of real java objects.
     *
     * @param version The version of the web service detected.
     * @return A GetFeatureInfo request.
     * @throws CstlServiceException
     */
    private GetFeatureInfo adaptGetFeatureInfo(final String version, final QueryContext queryContext) throws CstlServiceException, NumberFormatException {
        final GetMap getMap  = adaptGetMap(version, false, queryContext);
        isVersionSupported(version);
        final String strX    = getParameter(version.equals(ServiceDef.WMS_1_1_1_SLD.version.toString()) ? KEY_I_V111 : KEY_I_V130, true);
        final String strY    = getParameter(version.equals(ServiceDef.WMS_1_1_1_SLD.version.toString()) ? KEY_J_V111 : KEY_J_V130, true);
        final String strQueryLayers = getParameter(KEY_QUERY_LAYERS, true);
              String infoFormat  = getParameter(KEY_INFO_FORMAT, false);
        final String strFeatureCount = getParameter(KEY_FEATURE_COUNT, false);
        final List<String> queryLayers = StringUtilities.toStringList(strQueryLayers);
        final List<String> queryableLayers = QueryAdapter.areQueryableLayers(queryLayers, null);
        final List<Name> namedQueryableLayers = parseNamespaceLayerList(queryableLayers);
        if (infoFormat == null) {
            infoFormat = MimeType.TEXT_XML;
        }
        final int x, y;
        try {
            x = RequestsUtilities.toInt(strX);
        } catch (NumberFormatException ex) {
            throw new CstlServiceException("Integer value waited. " + ex.getMessage(), ex, INVALID_POINT,
                    version.equals(ServiceDef.WMS_1_1_1_SLD.version.toString()) ? KEY_I_V111 : KEY_I_V130);
        }
        try {
            y = RequestsUtilities.toInt(strY);
        } catch (NumberFormatException ex) {
            throw new CstlServiceException("Integer value waited. " + ex.getMessage(), ex, INVALID_POINT,
                    version.equals(ServiceDef.WMS_1_1_1_SLD.version.toString()) ? KEY_J_V111 : KEY_J_V130);
        }
        final Integer featureCount;
        if (strFeatureCount == null || strFeatureCount.isEmpty()) {
            featureCount = 1;
        } else {
            featureCount = RequestsUtilities.toInt(strFeatureCount);
        }
        return new GetFeatureInfo(getMap, x, y, namedQueryableLayers, infoFormat, featureCount);
    }

    /**
     * Return a List of named Layer (namespace : name) from a string list.
     *
     * @param layerNames
     * @return
     */
    private List<Name> parseNamespaceLayerList(List<String> layerNames) {
        final List<Name> result = new ArrayList<Name>();
        for (String layerName : layerNames) {
            result.add(Util.parseLayerName(layerName));
        }
        return result;
    }

    /**
     * Converts a GetLegendGraphic request composed of string values, to a container
     * of real java objects.
     *
     * @return The GetLegendGraphic request.
     * @throws CstlServiceException
     */
    private GetLegendGraphic adaptGetLegendGraphic() throws CstlServiceException {
        final Name strLayer  = Util.parseLayerName(getParameter(KEY_LAYER,  true));
        final String strFormat = getParameter(KEY_FORMAT, true );
        final String strWidth  = getParameter(KEY_WIDTH,  false);
        final String strHeight = getParameter(KEY_HEIGHT, false);
        final String format;
        try {
            format = RequestsUtilities.toFormat(strFormat);
        } catch (IllegalArgumentException i) {
            throw new CstlServiceException(i, INVALID_FORMAT);
        }
        if (strWidth == null || strHeight == null) {
            return new GetLegendGraphic(strLayer, strFormat, null, null);
        } else {
            final int width;
            final int height;
            try {
                width  = RequestsUtilities.toInt(strWidth);
                height = RequestsUtilities.toInt(strHeight);
            } catch (NumberFormatException n) {
                throw new CstlServiceException(n, INVALID_PARAMETER_VALUE);
            }
            return new GetLegendGraphic(strLayer, format, width, height);
        }
    }

    private boolean isV111orUnder(String version) {
        return version.equals(ServiceDef.WMS_1_0_0.version.toString())     ||
               version.equals(ServiceDef.WMS_1_0_0_SLD.version.toString()) ||
               version.equals(ServiceDef.WMS_1_1_1.version.toString())     ||
               version.equals(ServiceDef.WMS_1_1_1_SLD.version.toString());
    }

    /**
     * Converts a GetMap request composed of string values, to a container of real
     * java objects.
     *
     * @param version    The version of the web service detected.
     * @param fromGetMap {@code true} if the request is done for a GetMap, {@code false}
     *                   otherwise (in the case of a GetFeatureInfo for example).
     * @return The GetMap request.
     * @throws CstlServiceException
     */
    private GetMap adaptGetMap(final String version, final boolean fromGetMap, final QueryContext queryContext) throws CstlServiceException {
        final String strExceptions   = getParameter(KEY_EXCEPTIONS,     false);
        if (strExceptions != null && strExceptions.equalsIgnoreCase(MimeType.APP_INIMAGE)) {
            queryContext.setErrorInimage(true);
        }
        final String strFormat       = getParameter(KEY_FORMAT,    fromGetMap);
        if (strFormat != null && !strFormat.isEmpty()) {
            // Ensures that the format specified is known, to use it as the format of the
            // image which will contain the exception.
            if (strFormat.equalsIgnoreCase(MimeType.IMAGE_BMP) ||
                strFormat.equalsIgnoreCase(MimeType.IMAGE_GIF) ||
                strFormat.equalsIgnoreCase(MimeType.IMAGE_JPEG)||
                strFormat.equalsIgnoreCase(MimeType.IMAGE_PNG) ||
                strFormat.equalsIgnoreCase(MimeType.IMAGE_TIFF))
            {
                queryContext.setExceptionImageFormat(strFormat);
            }
        }

        final String strCRS          = getParameter((version.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) ?
                                            KEY_CRS_V111 : KEY_CRS_V130, true);
        final String strBBox         = getParameter(KEY_BBOX,            true);
        final String strLayers       = getParameter(KEY_LAYERS,          true);
        final String strWidth        = getParameter(KEY_WIDTH,           true);
        final String strHeight       = getParameter(KEY_HEIGHT,          true);
        final String strElevation    = getParameter(KEY_ELEVATION,      false);
        final String strTime         = getParameter(KEY_TIME,           false);
        final String strBGColor      = getParameter(KEY_BGCOLOR,        false);
        final String strTransparent  = getParameter(KEY_TRANSPARENT,    false);
        //final String strRemoteOwsType = getParameter(KEY_REMOTE_OWS_TYPE, false);
        final String strRemoteOwsUrl = getParameter(KEY_REMOTE_OWS_URL, false);
        final String urlSLD          = getParameter(KEY_SLD,            false);
        final String strAzimuth      = getParameter(KEY_AZIMUTH,        false);
        final String strStyles       = getParameter(KEY_STYLES, ((urlSLD != null)
                && (version.equals(ServiceDef.WMS_1_1_1_SLD.version.toString()))) ? false : fromGetMap);

        final CoordinateReferenceSystem crs;
        boolean forceLongitudeFirst = false;
        try {
            if (isV111orUnder(version)) {
                /*
                 * If we are in version older than WMS 1.3.0, then the bounding box is
                 * expressed with the longitude in first, even if the CRS has the latitude as
                 * first axis. Consequently we have to force the longitude in first for the
                 * CRS decoding.
                 */
                forceLongitudeFirst = true;
            }
            crs = CRS.decode(strCRS, forceLongitudeFirst);
        } catch (FactoryException ex) {
            if (isV111orUnder(version)) {
                throw new CstlServiceException(ex, org.constellation.ws.ExceptionCode.INVALID_SRS);
            } else {
                throw new CstlServiceException(ex, INVALID_CRS);
            }
        }
        final Envelope env;
        try {
            env = RequestsUtilities.toEnvelope(strBBox, crs);
        } catch (IllegalArgumentException i) {
            throw new CstlServiceException(i, INVALID_PARAMETER_VALUE);
        }
        final String format;
        try {
            format = RequestsUtilities.toFormat(strFormat);
        } catch (IllegalArgumentException i) {
            throw new CstlServiceException(i, INVALID_FORMAT, KEY_FORMAT.toLowerCase());
        }
        final List<String> layers  = StringUtilities.toStringList(strLayers);
        final List<Name> namedLayers  = parseNamespaceLayerList(layers);
        final List<String> styles = StringUtilities.toStringList(strStyles);
        MutableStyledLayerDescriptor sld = null;
        final Double elevation;
        try {
            elevation = (strElevation != null) ? RequestsUtilities.toDouble(strElevation) : null;
        } catch (NumberFormatException n) {
            throw new CstlServiceException(n, INVALID_PARAMETER_VALUE, KEY_ELEVATION.toLowerCase());
        }
        final Date date;
        try {
            date = TimeParser.toDate(strTime);
        } catch (ParseException ex) {
            throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, KEY_TIME.toLowerCase());
        }
        final int width;
        final int height;
        try {
            width  = RequestsUtilities.toInt(strWidth);
            height = RequestsUtilities.toInt(strHeight);
        } catch (NumberFormatException n) {
            throw new CstlServiceException(n, INVALID_DIMENSION_VALUE);
        }
        final Dimension size = new Dimension(width, height);
        final Color background = RequestsUtilities.toColor(strBGColor);
        final boolean transparent = RequestsUtilities.toBoolean(strTransparent);
        queryContext.setOpaque(!transparent);

        if (strRemoteOwsUrl != null) {
            InputStream in = null;
            try {
                in = new FileInputStream(new File(strRemoteOwsUrl));
            } catch (FileNotFoundException ex) {
                throw new CstlServiceException(ex, STYLE_NOT_DEFINED);
            }
            final XMLUtilities sldparser = new XMLUtilities();
            try {
                sld = sldparser.readSLD(in,
                        StyledLayerDescriptor.V_1_0_0);
            } catch (JAXBException ex) {
                throw new CstlServiceException(ex, STYLE_NOT_DEFINED);
            } catch (FactoryException ex) {
                    throw new CstlServiceException(ex, STYLE_NOT_DEFINED);
                }
            if (sld == null) {
                try {
                    sld = sldparser.readSLD(in,
                            StyledLayerDescriptor.V_1_1_0);
                } catch (JAXBException ex) {
                    throw new CstlServiceException(ex, STYLE_NOT_DEFINED);
                } catch (FactoryException ex) {
                    throw new CstlServiceException(ex, STYLE_NOT_DEFINED);
                }
            }
        } else {
            try {
                sld = QueryAdapter.toSLD(urlSLD);
            } catch (MalformedURLException ex) {
                throw new CstlServiceException(ex, STYLE_NOT_DEFINED);
            }
        }

        final double azimuth;
        try {
            azimuth = (strAzimuth == null) ? 0.0 : RequestsUtilities.toDouble(strAzimuth);
        } catch(NumberFormatException ex) {
            throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, KEY_AZIMUTH.toLowerCase());
        }

        // Builds the request.
        return new GetMap(env, new Version(version), format, namedLayers, styles, sld, elevation,
                    date, size, background, transparent, azimuth, strExceptions, getParameters());
    }

    
    /**
     * Logs the destruction of the service
     */
    @PreDestroy
    @Override
    public void destroy() {
        LOGGER.info("Shutting down the REST WMS service facade.");
        worker.destroy();
    }
}

