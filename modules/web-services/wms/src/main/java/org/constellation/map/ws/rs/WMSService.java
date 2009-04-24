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

import com.sun.jersey.spi.resource.Singleton;

//J2SE dependencies
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

//Constellation dependencies
import javax.xml.bind.Marshaller;
import org.constellation.ServiceDef;
import org.constellation.map.ws.AbstractWMSWorker;
import org.constellation.map.ws.WMSWorker;
import org.constellation.query.QueryAdapter;
import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetMap;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.util.Util;
import org.constellation.util.StringUtilities;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.ServiceType;
import org.constellation.ws.ServiceExceptionReport;
import org.constellation.ws.ServiceExceptionType;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.OGCWebService;

//Geotools dependencies
import org.geotoolkit.internal.jaxb.v110.sld.DescribeLayerResponseType;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.util.MeasurementRange;

//Geoapi dependencies
import org.geotoolkit.util.Version;
import org.geotoolkit.style.xml.XMLUtilities;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.constellation.ws.ExceptionCode.*;
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
public class WMSService extends OGCWebService {

    /**
     * The worker which will perform the core logic for this service.
     */
    protected AbstractWMSWorker worker;


    /**
     * Build a new instance of the webService and initialize the JAXB marshaller.
     */
    public WMSService() throws JAXBException {
        super(ServiceDef.WMS_1_3_0_SLD, ServiceDef.WMS_1_1_1_SLD);

        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext("org.constellation.ws:" +
        		      "org.constellation.wms.v111:" +
                      "org.constellation.wms.v130:" +
                      "org.geotoolkit.internal.jaxb.v110.sld",
                      "http://www.opengis.net/wms");

        worker = new WMSWorker(marshallerPool);
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
        Marshaller marshaller = null;
        ServiceDef version = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            final String request = (String) getParameter(KEY_REQUEST, true);
            logParameters();

            //Handle user's requests.
            if (GETMAP.equalsIgnoreCase(request)) {
                final GetMap requestMap = adaptGetMap(true);
                version = getVersionFromNumber(requestMap.getVersion().toString());
                final BufferedImage map = worker.getMap(requestMap);
                return Response.ok(map, requestMap.getFormat()).build();
            }
            if (GETFEATUREINFO.equalsIgnoreCase(request)) {
                final GetFeatureInfo requestFeatureInfo = adaptGetFeatureInfo();
                version = getVersionFromNumber(requestFeatureInfo.getVersion().toString());
                final String result = worker.getFeatureInfo(requestFeatureInfo);
                //Need to reset the GML mime format to XML for browsers
                String infoFormat = requestFeatureInfo.getInfoFormat();
                if (infoFormat.equals(GML)) {
                    infoFormat = APP_XML;
                }
                return Response.ok(result, infoFormat).build();
            }
            if (GETCAPABILITIES.equalsIgnoreCase(request)) {
                final GetCapabilities requestCapab = adaptGetCapabilities();
                version = getVersionFromNumber(requestCapab.getVersion().toString());
                worker.initServletContext(servletContext);
                worker.initUriContext(uriContext);
                final AbstractWMSCapabilities capabilities = worker.getCapabilities(requestCapab);
                //workaround because 1.1.1 is defined with a DTD rather than an XSD
                //we marshall the response and return the XML String
                final StringWriter sw = new StringWriter();
                marshaller.setProperty("com.sun.xml.bind.xmlHeaders",
                           (requestCapab.getVersion().toString().equals("1.1.1")) ?
                           "<!DOCTYPE WMT_MS_Capabilities SYSTEM \"http://schemas.opengis.net/wms/1.1.1/WMS_MS_Capabilities.dtd\">\n" :
                           "");
                marshaller.marshal(capabilities, sw);
                return Response.ok(sw.toString(), requestCapab.getFormat()).build();
            }
            if (GETLEGENDGRAPHIC.equalsIgnoreCase(request)) {
                final GetLegendGraphic requestLegend = adaptGetLegendGraphic();
                version = getVersionFromNumber(requestLegend.getVersion().toString());
                final BufferedImage legend = worker.getLegendGraphic(requestLegend);
                return Response.ok(legend, requestLegend.getFormat()).build();
            }
            if (DESCRIBELAYER.equalsIgnoreCase(request)) {
                final DescribeLayer describeLayer = adaptDescribeLayer();
                version = getVersionFromNumber(describeLayer.getVersion().toString());
                worker.initUriContext(uriContext);
                final DescribeLayerResponseType response = worker.describeLayer(describeLayer);
                //We need to marshall the string to XML
                final StringWriter sw = new StringWriter();
                marshaller.marshal(response, sw);
                return Response.ok(sw.toString(), TEXT_XML).build();
            }
            throw new CstlServiceException("The operation " + request + " is not supported by the service",
                                           OPERATION_NOT_SUPPORTED, "request");
        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, marshaller, version);
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, final Marshaller marshaller,
                                                ServiceDef serviceDef) throws JAXBException
    {
        if (serviceDef == null) {
            serviceDef = getBestVersion(null);
        }
        final Version version = serviceDef.exceptionVersion;
        final ServiceExceptionReport report = new ServiceExceptionReport(version,
                new ServiceExceptionType(ex.getMessage(), (ExceptionCode) ex.getExceptionCode()));
        if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
                !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) &&
                !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE) &&
                !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
        } else {
            LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getLocalizedMessage() + '\n');
        }
        StringWriter sw = new StringWriter();
        marshaller.marshal(report, sw);
        return Response.ok(Util.cleanSpecialCharacter(sw.toString()), APP_XML).build();
    }

    /**
     * Converts a DescribeLayer request composed of string values, to a container
     * of real java objects.
     *
     * @return The DescribeLayer request.
     * @throws CstlServiceException
     */
    private DescribeLayer adaptDescribeLayer() throws CstlServiceException {
        final String version = getParameter(KEY_VERSION, true);
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
     * @return A GetCapabilities request.
     * @throws CstlServiceException
     */
    private GetCapabilities adaptGetCapabilities() throws CstlServiceException {
        final String version = getParameter(KEY_VERSION, false);
        if (version == null) {
            final ServiceDef capsService = getBestVersion(null);
            return new GetCapabilities(capsService.version);
        }
        final ServiceDef bestVersion = getBestVersion(version);
        final String service = getParameter(KEY_SERVICE, true);
        if (!ServiceType.WMS.toString().equalsIgnoreCase(service)) {
            throw new CstlServiceException("Invalid service specified. Should be WMS.",
                    INVALID_PARAMETER_VALUE, "service");
        }
        String format = getParameter(KEY_FORMAT, false);
        if (format == null || !(format.equalsIgnoreCase(TEXT_XML) ||
                format.equalsIgnoreCase(APP_WMS_XML) || format.equalsIgnoreCase(APP_XML)))
        {
            format = TEXT_XML;
        }
        return new GetCapabilities(bestVersion.version, format);
    }

    /**
     * Converts a GetFeatureInfo request composed of string values, to a container
     * of real java objects.
     *
     * @return A GetFeatureInfo request.
     * @throws CstlServiceException
     */
    private GetFeatureInfo adaptGetFeatureInfo() throws CstlServiceException, NumberFormatException {
        final GetMap getMap  = adaptGetMap(false);
        final String version = getParameter(KEY_VERSION, true);
        ServiceDef serviceDef = getVersionFromNumber(version);
        if (serviceDef == null) {
            serviceDef = getBestVersion(null);
        }
        isVersionSupported(version);
        final String strX    = getParameter(version.equals("1.1.1") ? KEY_I_v111 : KEY_I_v130, true);
        final String strY    = getParameter(version.equals("1.1.1") ? KEY_J_v111 : KEY_J_v130, true);
        final String strQueryLayers = getParameter(KEY_QUERY_LAYERS, true);
        final String infoFormat  = getParameter(KEY_INFO_FORMAT, true);
        final String strFeatureCount = getParameter(KEY_FEATURE_COUNT, false);
        final List<String> queryLayers = StringUtilities.toStringList(strQueryLayers);
        final List<String> queryableLayers = QueryAdapter.areQueryableLayers(queryLayers, null);
        final int x = StringUtilities.toInt(strX);
        final int y = StringUtilities.toInt(strY);
        final Integer featureCount;
        if (strFeatureCount == null || strFeatureCount.equals("")) {
            featureCount = null;
        } else {
            featureCount = StringUtilities.toInt(strFeatureCount);
        }
        return new GetFeatureInfo(getMap, x, y, queryableLayers, infoFormat, featureCount);
    }

    /**
     * Converts a GetLegendGraphic request composed of string values, to a container
     * of real java objects.
     *
     * @return The GetLegendGraphic request.
     * @throws CstlServiceException
     */
    private GetLegendGraphic adaptGetLegendGraphic() throws CstlServiceException {
        final String strLayer  = getParameter(KEY_LAYER,  true );
        final String strFormat = getParameter(KEY_FORMAT, true );
        final String strWidth  = getParameter(KEY_WIDTH,  false);
        final String strHeight = getParameter(KEY_HEIGHT, false);
        final String format;
        try {
            format = StringUtilities.toFormat(strFormat);
        } catch (IllegalArgumentException i) {
            throw new CstlServiceException(i, INVALID_FORMAT);
        }
        if (strWidth == null || strHeight == null) {
            return new GetLegendGraphic(strLayer, strFormat, null, null);
        } else {
            final int width;
            final int height;
            try {
                width  = StringUtilities.toInt(strWidth);
                height = StringUtilities.toInt(strHeight);
            } catch (NumberFormatException n) {
                throw new CstlServiceException(n, INVALID_PARAMETER_VALUE);
            }
            return new GetLegendGraphic(strLayer, format, width, height);
        }
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
    private GetMap adaptGetMap(final boolean fromGetMap) throws CstlServiceException {
        final String version         = getParameter(KEY_VERSION,         true);
        isVersionSupported(version);
        final String strFormat       = getParameter(KEY_FORMAT,    fromGetMap);
        final String strCRS          = getParameter((version.equals("1.1.1")) ?
                                            KEY_CRS_v111 : KEY_CRS_v130, true);
        final String strBBox         = getParameter(KEY_BBOX,            true);
        final String strLayers       = getParameter(KEY_LAYERS,          true);
        final String strWidth        = getParameter(KEY_WIDTH,           true);
        final String strHeight       = getParameter(KEY_HEIGHT,          true);
        final String strElevation    = getParameter(KEY_ELEVATION,      false);
        final String strTime         = getParameter(KEY_TIME,           false);
        final String strDimRange     = getParameter(KEY_DIM_RANGE,      false);
        final String strBGColor      = getParameter(KEY_BGCOLOR,        false);
        final String strTransparent  = getParameter(KEY_TRANSPARENT,    false);
        //final String strRemoteOwsType = getParameter(KEY_REMOTE_OWS_TYPE, false);
        final String strRemoteOwsUrl = getParameter(KEY_REMOTE_OWS_URL, false);
        final String strExceptions   = getParameter(KEY_EXCEPTIONS,     false);
        final String urlSLD          = getParameter(KEY_SLD,            false);
        final String strAzimuth      = getParameter(KEY_AZIMUTH,        false);
        final String strStyles       = getParameter(KEY_STYLES, ((urlSLD != null)
                && (version.equals("1.1.1"))) ? false : fromGetMap);

        final CoordinateReferenceSystem crs;
        try {
            crs = StringUtilities.toCRS(strCRS);
        } catch (FactoryException ex) {
            throw new CstlServiceException(ex, INVALID_CRS);
        }
        final Envelope env;
        try {
            env = StringUtilities.toEnvelope(strBBox, crs);
        } catch (IllegalArgumentException i) {
            throw new CstlServiceException(i, INVALID_PARAMETER_VALUE);
        }
        final String format;
        try {
            format = StringUtilities.toFormat(strFormat);
        } catch (IllegalArgumentException i) {
            throw new CstlServiceException(i, INVALID_FORMAT);
        }
        final List<String> layers  = StringUtilities.toStringList(strLayers);
        final List<String> styles = StringUtilities.toStringList(strStyles);
        MutableStyledLayerDescriptor sld = null;
        final Double elevation;
        try {
            elevation = (strElevation != null) ? StringUtilities.toDouble(strElevation) : null;
        } catch (NumberFormatException n) {
            throw new CstlServiceException(n, INVALID_PARAMETER_VALUE);
        }
        final MeasurementRange dimRange = QueryAdapter.toMeasurementRange(strDimRange);
        final Date date;
        try {
            date = StringUtilities.toDate(strTime);
        } catch (ParseException ex) {
            throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
        }
        final int width;
        final int height;
        try {
            width  = StringUtilities.toInt(strWidth);
            height = StringUtilities.toInt(strHeight);
        } catch (NumberFormatException n) {
            throw new CstlServiceException(n, INVALID_PARAMETER_VALUE);
        }
        final Dimension size = new Dimension(width, height);
        final Color background = StringUtilities.toColor(strBGColor);
        final boolean transparent = StringUtilities.toBoolean(strTransparent);

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
                        org.geotoolkit.style.xml.Specification.StyledLayerDescriptor.V_1_0_0);
            } catch (JAXBException ex) {
                throw new CstlServiceException(ex, STYLE_NOT_DEFINED);
            }
            if (sld == null) {
                try {
                    sld = sldparser.readSLD(in,
                            org.geotoolkit.style.xml.Specification.StyledLayerDescriptor.V_1_1_0);
                } catch (JAXBException ex) {
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
            azimuth = (strAzimuth == null) ? 0.0 : StringUtilities.toDouble(strAzimuth);
        } catch(NumberFormatException ex) {
            throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
        }

        // Builds the request.
        return new GetMap(env, new Version(version), format, layers, styles, sld, elevation,
                    date, dimRange, size, background, transparent, azimuth, strExceptions);
    }

    /**
     * Logs the destruction of the service
     */
    @PreDestroy
    @Override
    public void destroy() {
        LOGGER.info("Shutting down the REST WMS service facade.");
    }
}
