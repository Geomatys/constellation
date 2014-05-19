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
package org.constellation.map.ws.rs;

import static org.constellation.api.QueryConstants.REQUEST_PARAMETER;
import static org.constellation.api.QueryConstants.SERVICE_PARAMETER;
import static org.constellation.api.QueryConstants.UPDATESEQUENCE_PARAMETER;
import static org.constellation.api.QueryConstants.VERSION_PARAMETER;
import static org.constellation.query.Query.*;
import static org.constellation.query.wms.WMSQuery.CAPABILITIES;
import static org.constellation.query.wms.WMSQuery.DESCRIBELAYER;
import static org.constellation.query.wms.WMSQuery.GETCAPABILITIES;
import static org.constellation.query.wms.WMSQuery.GETFEATUREINFO;
import static org.constellation.query.wms.WMSQuery.GETLEGENDGRAPHIC;
import static org.constellation.query.wms.WMSQuery.GETMAP;
import static org.constellation.query.wms.WMSQuery.KEY_AZIMUTH;
import static org.constellation.query.wms.WMSQuery.KEY_BBOX;
import static org.constellation.query.wms.WMSQuery.KEY_BGCOLOR;
import static org.constellation.query.wms.WMSQuery.KEY_CRS_V111;
import static org.constellation.query.wms.WMSQuery.KEY_CRS_V130;
import static org.constellation.query.wms.WMSQuery.KEY_ELEVATION;
import static org.constellation.query.wms.WMSQuery.KEY_FEATURE_COUNT;
import static org.constellation.query.wms.WMSQuery.KEY_FORMAT;
import static org.constellation.query.wms.WMSQuery.KEY_HEIGHT;
import static org.constellation.query.wms.WMSQuery.KEY_INFO_FORMAT;
import static org.constellation.query.wms.WMSQuery.KEY_I_V111;
import static org.constellation.query.wms.WMSQuery.KEY_I_V130;
import static org.constellation.query.wms.WMSQuery.KEY_J_V111;
import static org.constellation.query.wms.WMSQuery.KEY_J_V130;
import static org.constellation.query.wms.WMSQuery.KEY_LANGUAGE;
import static org.constellation.query.wms.WMSQuery.KEY_LAYER;
import static org.constellation.query.wms.WMSQuery.KEY_LAYERS;
import static org.constellation.query.wms.WMSQuery.KEY_QUERY_LAYERS;
import static org.constellation.query.wms.WMSQuery.KEY_REMOTE_OWS_URL;
import static org.constellation.query.wms.WMSQuery.KEY_RULE;
import static org.constellation.query.wms.WMSQuery.KEY_SCALE;
import static org.constellation.query.wms.WMSQuery.KEY_SLD;
import static org.constellation.query.wms.WMSQuery.KEY_SLD_BODY;
import static org.constellation.query.wms.WMSQuery.KEY_SLD_VERSION;
import static org.constellation.query.wms.WMSQuery.KEY_STYLE;
import static org.constellation.query.wms.WMSQuery.KEY_STYLES;
import static org.constellation.query.wms.WMSQuery.KEY_TIME;
import static org.constellation.query.wms.WMSQuery.KEY_TRANSPARENT;
import static org.constellation.query.wms.WMSQuery.KEY_WIDTH;
import static org.constellation.query.wms.WMSQuery.KEY_WMTVER;
import static org.constellation.query.wms.WMSQuery.MAP;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_CRS;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_DIMENSION_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_FORMAT;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_POINT;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;
import static org.geotoolkit.ows.xml.OWSExceptionCode.STYLE_NOT_DEFINED;


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
import java.util.Map;
import java.util.logging.Level;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.sis.util.Version;
//Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.ServiceDef.Specification;
import org.constellation.map.configuration.MapConfigurer;
import org.constellation.map.ws.DefaultWMSWorker;
import org.constellation.map.ws.QueryContext;
import org.constellation.map.ws.WMSConstant;
import org.constellation.map.ws.WMSWorker;
import org.constellation.portrayal.internal.PortrayalResponse;
import org.constellation.query.QueryAdapter;
import org.constellation.util.Util;
import org.constellation.wms.configuration.WMSConfigurer;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.Worker;
import org.constellation.ws.rs.GridWebService;
import org.constellation.ws.rs.provider.SchemaLocatedExceptionResponse;
//GeotoolKit dependencies
import org.geotoolkit.client.RequestsUtilities;
import org.geotoolkit.display2d.service.DefaultPortrayalService;
import org.geotoolkit.ogc.xml.exception.ServiceExceptionReport;
import org.geotoolkit.ogc.xml.exception.ServiceExceptionType;
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.xml.GetLegendGraphic;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.sld.xml.v110.DescribeLayerResponseType;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.TimeParser;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.DescribeLayer;
import org.geotoolkit.wms.xml.GetCapabilities;
import org.geotoolkit.wms.xml.GetFeatureInfo;
import org.geotoolkit.wms.xml.GetMap;
import org.geotoolkit.wms.xml.WMSMarshallerPool;
//Geoapi dependencies
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

/**
 * The REST facade to an OGC Web Map Service, implementing versions 1.1.1 and
 * 1.3.0.
 *
 * @version 0.9
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Benjain Garcia (Geomatys)
 * @since 0.1
 */
//@Named
@Path("wms/{serviceId}")
@Singleton
public class WMSService extends GridWebService<WMSWorker> {

    public static boolean writeDTD = true;

    /**
     * Build a new instance of the webService and initialize the JAXB context.
     */
    public WMSService() {
        super(Specification.WMS);
        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext(WMSMarshallerPool.getInstance());

        setFullRequestLog(true);
        LOGGER.log(Level.INFO, "WMS REST service running ({0} instances)\n", getWorkerMapSize());
    }


    @Override
    protected Class getWorkerClass() {
        return DefaultWMSWorker.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends MapConfigurer> getConfigurerClass() {
        return WMSConfigurer.class;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Response treatIncomingRequest(final Object objectRequest, final WMSWorker worker) {
        final QueryContext queryContext = new QueryContext();

        ServiceDef version = null;
        try {

            final RequestBase request;
            if (objectRequest == null) {
                version = worker.getVersionFromNumber(getParameter(VERSION_PARAMETER, false)); // needed if exception is launch before request build
                request = adaptQuery(getParameter(REQUEST_PARAMETER, true), worker, queryContext);
            } else if (objectRequest instanceof RequestBase) {
                request = (RequestBase) objectRequest;
            } else {
                throw new CstlServiceException("The operation " + objectRequest.getClass().getName() + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
            }
            version = worker.getVersionFromNumber(request.getVersion());

            //Handle user's requests.
            if (request instanceof GetFeatureInfo) {
                final GetFeatureInfo requestFeatureInfo = (GetFeatureInfo) request;
                final Map.Entry<String, Object> result  = worker.getFeatureInfo(requestFeatureInfo);

                if (result != null) {
                    final String infoFormat = result.getKey();
                    return Response.ok(result.getValue(), infoFormat).build();
                }

                //throw an exception if result of GetFeatureInfo visitor is null
                throw new CstlServiceException("An error occurred during GetFeatureInfo response building.");
            }
            if (request instanceof GetMap) {
                final GetMap requestMap     = (GetMap)request;
                final PortrayalResponse map = worker.getMap(requestMap);
                return Response.ok(map, requestMap.getFormat()).build();
            }
            if (request instanceof GetCapabilities) {

                final GetCapabilities requestCapab         = (GetCapabilities) request;
                final AbstractWMSCapabilities capabilities = worker.getCapabilities(requestCapab);

                return Response.ok(capabilities, requestCapab.getFormat()).build();
            }
            if (request instanceof GetLegendGraphic) {
                final GetLegendGraphic requestLegend = (GetLegendGraphic)request;
                final PortrayalResponse legend       = worker.getLegendGraphic(requestLegend);
                return Response.ok(legend, requestLegend.getFormat()).build();
            }
            if (request instanceof DescribeLayer) {
                final DescribeLayer describeLayer        = (DescribeLayer)request;
                final DescribeLayerResponseType response = worker.describeLayer(describeLayer);
                return Response.ok(response, MimeType.TEXT_XML).build();
            }
            throw new CstlServiceException("The operation " + request + " is not supported by the service",
                                           OPERATION_NOT_SUPPORTED, KEY_REQUEST.toLowerCase());
        } catch (CstlServiceException ex) {
            return processExceptionResponse(queryContext, ex, version, worker);
        }
    }

    /**
     * Build request object from KVP parameters.
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private RequestBase adaptQuery(final String request, final Worker worker, final QueryContext queryContext) throws CstlServiceException {
         if (GETMAP.equalsIgnoreCase(request) || MAP.equalsIgnoreCase(request)) {
             return  adaptGetMap(true, queryContext, worker);

         } else if (GETFEATUREINFO.equalsIgnoreCase(request)) {
             return adaptGetFeatureInfo(queryContext, worker);

         // For backward compatibility between WMS 1.1.1 and WMS 1.0.0, we handle the "Capabilities" request
         // as "GetCapabilities" request in version 1.1.1.
         } else if (GETCAPABILITIES.equalsIgnoreCase(request) || CAPABILITIES.equalsIgnoreCase(request)) {
             return adaptGetCapabilities(request, worker);

         } else  if (GETLEGENDGRAPHIC.equalsIgnoreCase(request)) {
             return adaptGetLegendGraphic();
         } else if (DESCRIBELAYER.equalsIgnoreCase(request)) {

             return adaptDescribeLayer(worker);
         }
         throw new CstlServiceException("The operation " + request + " is not supported by the service", INVALID_PARAMETER_VALUE, "request");
    }

    /**
     * Generate an error response in image if query asks it.
     * Otherwise this call will fallback on normal xml error.
     */
    private Response processExceptionResponse(final QueryContext queryContext, final CstlServiceException ex, ServiceDef serviceDef, final Worker w) {
        logException(ex);

        // Now handle in image response or exception report.
        if (queryContext.isErrorInimage()) {
            final BufferedImage image = DefaultPortrayalService.writeException(ex, new Dimension(600, 400), queryContext.isOpaque());
            return Response.ok(image, queryContext.getExceptionImageFormat()).build();
        } else {
            return processExceptionResponse(ex, serviceDef, w);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef, final Worker w) {

        if (serviceDef == null) {
            serviceDef = w.getBestVersion(null);
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
     * @return The DescribeLayer request.
     * @throws CstlServiceException
     */
    private DescribeLayer adaptDescribeLayer(final Worker worker) throws CstlServiceException {
        String version = getParameter(VERSION_PARAMETER, false);
        if (version == null) {
            version = getParameter(KEY_WMTVER, false);
        }
        if (version == null) {
            throw new CstlServiceException("The parameter version must be specified",
                MISSING_PARAMETER_VALUE, "version");
        }
        ServiceDef serviceDef = worker.getVersionFromNumber(version);
        if (serviceDef == null) {
            serviceDef = worker.getBestVersion(null);
        }
        worker.checkVersionSupported(version, false);
        final String strLayer  = getParameter(KEY_LAYERS,  true);
        final List<String> layers = StringUtilities.toStringList(strLayer);
        return new DescribeLayer(layers, serviceDef.version);
    }

    /**
     * Converts a GetCapabilities request composed of string values, to a container
     * of real java objects.
     *
     * @return A GetCapabilities request.
     * @throws CstlServiceException
     */
    private GetCapabilities adaptGetCapabilities(final String request, final Worker worker) throws CstlServiceException {
        String version;
        if (CAPABILITIES.equalsIgnoreCase(request)) {
            version =  ServiceDef.WMS_1_1_1_SLD.version.toString();
        } else {
            version = getParameter(VERSION_PARAMETER, false);
            if (version == null) {
                // For backward compatibility with WMS 1.0.0, we try to find the version number
                // from the WMTVER parameter too.
                version = getParameter(KEY_WMTVER, false);
            }
        }
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (!ServiceDef.Specification.WMS.toString().equalsIgnoreCase(service)) {
            throw new CstlServiceException("Invalid service specified. Should be WMS.",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }
        final String language = getParameter(KEY_LANGUAGE, false);
        if (version == null) {
            final ServiceDef capsService = worker.getBestVersion(null);
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
        final ServiceDef bestVersion = worker.getBestVersion(version);
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
        final String updateSequence = getParameter(UPDATESEQUENCE_PARAMETER, false);

        return new GetCapabilities(bestVersion.version, format, language, updateSequence);
    }

    /**
     * Converts a GetFeatureInfo request composed of string values, to a container
     * of real java objects.
     *
     * @return A GetFeatureInfo request.
     * @throws CstlServiceException
     */
    private GetFeatureInfo adaptGetFeatureInfo(final QueryContext queryContext, final Worker worker) throws CstlServiceException, NumberFormatException {
        final GetMap getMap  = adaptGetMap(false, queryContext, worker);

        String version = getParameter(VERSION_PARAMETER, false);
        if (version == null) {
            version = getParameter(KEY_WMTVER, false);
        }
        if (version == null) {
            throw new CstlServiceException("The parameter version must be specified",
                MISSING_PARAMETER_VALUE, "version");
        }
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
     * @param fromGetMap {@code true} if the request is done for a GetMap, {@code false}
     *                   otherwise (in the case of a GetFeatureInfo for example).
     * @return The GetMap request.
     * @throws CstlServiceException
     */
    private GetMap adaptGetMap(final boolean fromGetMap, final QueryContext queryContext, final Worker w) throws CstlServiceException {
        String version = getParameter(VERSION_PARAMETER, false);
        if (version == null) {
            version = getParameter(KEY_WMTVER, false);
        }
        if (version == null) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        w.checkVersionSupported(version, false);

        final String strExceptions   = getParameter(KEY_EXCEPTIONS,     false);
        /*
         * we verify that the exception format is an allowed value
         */
        if (ServiceDef.WMS_1_3_0_SLD.version.toString().equals(version)) {
            if (strExceptions != null && !WMSConstant.EXCEPTION_130.contains(strExceptions)) {
                throw new CstlServiceException("exception format:" + strExceptions + " is not allowed. Use XML, INIMAGE or BLANK", INVALID_PARAMETER_VALUE);
            }
        } else {
            if (strExceptions != null && !WMSConstant.EXCEPTION_111.contains(strExceptions)) {
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
        final String bodySLD         = getParameter(KEY_SLD_BODY,       false);
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
        final ArrayList<Date> dates = new ArrayList<>();
        try {
            TimeParser.parse(strTime, 0l, dates);
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
            final StyleXmlIO sldparser = new StyleXmlIO();
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
                sld = QueryAdapter.toSLD(bodySLD, urlSLD, sldVersion);
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
                    dates, size, background, transparent, azimuth, strExceptions, getParameters());
    }
}
