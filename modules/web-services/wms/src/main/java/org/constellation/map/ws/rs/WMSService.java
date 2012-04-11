/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.map.ws.rs;

import com.sun.jersey.spi.resource.Singleton;

//J2SE dependencies
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

//Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.map.ws.DefaultWMSWorker;
import org.constellation.map.ws.QueryContext;
import org.constellation.map.ws.WMSWorker;
import org.constellation.portrayal.internal.PortrayalResponse;
import org.constellation.query.QueryAdapter;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.rs.GridWebService;
import org.constellation.ws.rs.provider.SchemaLocatedExceptionResponse;

//GeotoolKit dependencies
import org.geotoolkit.client.util.RequestsUtilities;
import org.geotoolkit.display2d.service.DefaultPortrayalService;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.ogc.xml.exception.ServiceExceptionReport;
import org.geotoolkit.ogc.xml.exception.ServiceExceptionType;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.sld.xml.GetLegendGraphic;
import org.geotoolkit.sld.xml.v110.DescribeLayerResponseType;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.TimeParser;
import org.geotoolkit.util.Version;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.WMSMarshallerPool;
import org.geotoolkit.wms.xml.GetCapabilities;
import org.geotoolkit.wms.xml.GetMap;
import org.geotoolkit.wms.xml.GetFeatureInfo;
import org.geotoolkit.wms.xml.DescribeLayer;

//Geoapi dependencies
import org.opengis.feature.type.Name;
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
@Path("wms/{serviceId}")
@Singleton
public class WMSService extends GridWebService<WMSWorker> {
    
    public static boolean writeDTD = true;
    
    /**
     * Build a new instance of the webService and initialize the JAXB context.
     */
    public WMSService() {
        super(ServiceDef.WMS_1_3_0_SLD, ServiceDef.WMS_1_1_1_SLD);

        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext(WMSMarshallerPool.getInstance());

        setFullRequestLog(true);
        LOGGER.log(Level.INFO, "WMS REST service running ({0} instances)\n", getWorkerMapSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WMSWorker createWorker(File instanceDirectory) {
        return new DefaultWMSWorker(instanceDirectory.getName(), instanceDirectory);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Response treatIncomingRequest(Object objectRequest, WMSWorker worker) {
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
                version = getVersionFromNumber(requestMap.getVersion());
                final PortrayalResponse map = worker.getMap(requestMap);
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
                version = getVersionFromNumber(requestFeatureInfo.getVersion());
                final String result = worker.getFeatureInfo(requestFeatureInfo);
                //Need to reset the GML mime format to XML for browsers
                String infoFormat = requestFeatureInfo.getInfoFormat();
                if (infoFormat.equals(GML) || infoFormat.equals(GML3)) {
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
                    version = getVersionFromNumber(requestCapab.getVersion());
                }
                worker.setServiceUrl(getServiceURL());
                final AbstractWMSCapabilities capabilities = worker.getCapabilities(requestCapab);
                
                return Response.ok(capabilities, requestCapab.getFormat()).build();
            }
            if (GETLEGENDGRAPHIC.equalsIgnoreCase(request)) {
                final GetLegendGraphic requestLegend = adaptGetLegendGraphic();
                version = getVersionFromNumber(requestLegend.getVersion());
                final PortrayalResponse legend = worker.getLegendGraphic(requestLegend);
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
                version = getVersionFromNumber(describeLayer.getVersion());
                worker.setServiceUrl(getServiceURL());
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
    private Response processExceptionResponse(final QueryContext queryContext, final CstlServiceException ex, ServiceDef serviceDef) {
        logException(ex);
        
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
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef) {

        if (serviceDef == null) {
            serviceDef = getBestVersion(null);
        }
        final Version version = serviceDef.exceptionVersion;
        final String locator = ex.getLocator();
        final ServiceExceptionReport report = new ServiceExceptionReport(version,
                (locator == null) ? new ServiceExceptionType(ex.getMessage(), ex.getExceptionCode()) :
                                    new ServiceExceptionType(ex.getMessage(), ex.getExceptionCode(), locator));
        

        final String schemaLocation;
        if (serviceDef.equals(ServiceDef.WMS_1_1_1_SLD) || serviceDef.equals(ServiceDef.WMS_1_1_1)) {
            schemaLocation = "http://schemas.opengis.net/wms/1.1.1/exception_1_1_1.dtd";
        } else {
            schemaLocation = "http://www.opengis.net/ogc http://schemas.opengis.net/wms/1.3.0/exceptions_1_3_0.xsd";
        }
        final SchemaLocatedExceptionResponse response = new SchemaLocatedExceptionResponse(report, schemaLocation);
        final String mimeException = (serviceDef.version.equals(ServiceDef.WMS_1_1_1_SLD.version)) ? MimeType.APP_SE_XML : MimeType.TEXT_XML;
        return Response.ok(response, mimeException).build();
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
        final String language = getParameter(KEY_LANGUAGE, false);
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
            return new GetCapabilities(capsService.version, format, language);
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
        return new GetCapabilities(bestVersion.version, format, language);
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
        // Verify that the format is known, otherwise returns an exception.
        final String format;
        try {
            format = RequestsUtilities.toFormat(strFormat);
        } catch (IllegalArgumentException i) {
            throw new CstlServiceException(i, INVALID_FORMAT);
        }

        final String strWidth  = getParameter(KEY_WIDTH,  false);
        final String strHeight = getParameter(KEY_HEIGHT, false);
        final Integer width;
        final Integer height;
        if (strWidth == null || strHeight == null) {
            width  = null;
            height = null;
        } else {
            try {
                width  = RequestsUtilities.toInt(strWidth);
            } catch (NumberFormatException n) {
                throw new CstlServiceException(n, INVALID_PARAMETER_VALUE, KEY_WIDTH.toLowerCase());
            }
            try {
                height = RequestsUtilities.toInt(strHeight);
            } catch (NumberFormatException n) {
                throw new CstlServiceException(n, INVALID_PARAMETER_VALUE, KEY_HEIGHT.toLowerCase());
            }
        }

        final String strStyle   = getParameter(KEY_STYLE,       false);
        final String strSld     = getParameter(KEY_SLD,         false);
        final String strSldVers = getParameter(KEY_SLD_VERSION, (strSld != null) ? true : false);
        final String strRule    = getParameter(KEY_RULE,        false);
        final StyledLayerDescriptor sldVersion;
        if (strSldVers == null) {
            sldVersion = null;
        } else if (strSldVers.equalsIgnoreCase("1.0.0")) {
            sldVersion = StyledLayerDescriptor.V_1_0_0;
        } else if (strSldVers.equalsIgnoreCase("1.1.0")) {
            sldVersion = StyledLayerDescriptor.V_1_1_0;
        } else {
            throw new CstlServiceException("The given sld version number "+ strSldVers +" is not known.",
                    INVALID_PARAMETER_VALUE, KEY_SLD_VERSION.toLowerCase());
        }
        final String strScale   = getParameter(KEY_SCALE,       false);
        final Double scale = RequestsUtilities.toDouble(strScale);
        return new GetLegendGraphic(strLayer, format, width, height, strStyle, strSld, sldVersion, strRule, scale, ServiceDef.WMS_1_1_1_SLD.version);
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
        /*
         * we verify that the exception format is an allowed value
         */
        if ("1.3.0".equals(version)) {
            if (strExceptions != null && 
                (!"XML".equals(strExceptions) && !"INIMAGE".equals(strExceptions) && !"BLANK".equals(strExceptions))) {
                throw new CstlServiceException("exception format:" + strExceptions + " is not allowed. Use XML, INIMAGE or BLANK", INVALID_PARAMETER_VALUE);
            }
        } else {
            if (strExceptions != null &&
                (!"application/vnd.ogc.se_xml".equals(strExceptions) && !"application/vnd.ogc.se_inimage".equals(strExceptions)
              && !"application/vnd.ogc.se_blank".equals(strExceptions))) {
                throw new CstlServiceException("exception format:" + strExceptions + " is not allowed. Use application/vnd.ogc.se_xml, application/vnd.ogc.se_inimage or application/vnd.ogc.se_blank", INVALID_PARAMETER_VALUE);
            }
        }
        if (strExceptions != null && (strExceptions.equalsIgnoreCase(MimeType.APP_INIMAGE) || strExceptions.equalsIgnoreCase("INIMAGE"))) {
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
        final String strSldVersion   = getParameter(KEY_SLD_VERSION, (urlSLD != null) ? true : false);
        final String strAzimuth      = getParameter(KEY_AZIMUTH,        false);
        final String strStyles       = getParameter(KEY_STYLES, ((urlSLD != null)) ? false : fromGetMap);

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
                // If a JAXBException occurs it can be because it is not parsed in the
                // good version. Let's just continue with the other version.
                LOGGER.finest(ex.getLocalizedMessage());
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
                final StyledLayerDescriptor sldVersion;
                if (strSldVersion == null) {
                    sldVersion = null;
                } else if (strSldVersion.equalsIgnoreCase("1.1.0")) {
                    sldVersion = StyledLayerDescriptor.V_1_1_0;
                } else if (strSldVersion.equalsIgnoreCase("1.0.0")) {
                    sldVersion = StyledLayerDescriptor.V_1_0_0;
                } else {
                    throw new CstlServiceException("The given sld version "+ strSldVersion +" is not known.",
                            INVALID_PARAMETER_VALUE, KEY_SLD_VERSION.toLowerCase());
                }
                sld = QueryAdapter.toSLD(urlSLD, sldVersion);
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
}
