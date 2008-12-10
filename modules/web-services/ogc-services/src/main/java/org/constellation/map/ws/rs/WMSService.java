/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.map.ws.rs;

import com.sun.jersey.spi.resource.Singleton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PreDestroy;
import javax.naming.NamingException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

//Constellation dependencies
import org.constellation.map.ws.WMSWorker;
import org.constellation.portrayal.AbstractGraphicVisitor;
import org.constellation.portrayal.CSTLPortrayalService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedLayerDP;
import org.constellation.query.QueryAdapter;
import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetMap;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.util.Utils;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.Service;
import org.constellation.ws.ServiceExceptionReport;
import org.constellation.ws.ServiceExceptionType;
import org.constellation.ws.WebServiceException;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.rs.OGCWebService;

// Geotools dependencies
import org.geotools.display.exception.PortrayalException;
import org.geotools.geometry.ImmutableEnvelope;
import org.geotools.internal.jaxb.v110.se.OnlineResourceType;
import org.geotools.internal.jaxb.v110.sld.DescribeLayerResponseType;
import org.geotools.internal.jaxb.v110.sld.LayerDescriptionType;
import org.geotools.internal.jaxb.v110.sld.TypeNameType;
import org.geotools.sld.MutableStyledLayerDescriptor;
import org.geotools.style.sld.XMLUtilities;
import org.geotools.util.MeasurementRange;

//Geoapi dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.constellation.ws.ExceptionCode.*;
import static org.constellation.query.wms.WMSQuery.*;


/**
 * WMS 1.3.0 / 1.1.1
 * web service implementing the operation getMap, getFeatureInfo and getCapabilities.
 *
 * @version $Id$
 * @author Guilhem Legal
 * @author Cédric Briançon
 */
@Path("wms")
@Singleton
public class WMSService extends OGCWebService {
    /**
     * The worker, designed to generate the output stream matching with the request
     * done by the user.
     */
    private final WMSWorker worker;

    /**
     * Build a new instance of the webService and initialise the JAXB marshaller.
     */
    public WMSService() throws JAXBException, SQLException, IOException, NamingException {
        super("WMS", new ServiceVersion(Service.WMS, "1.3.0"), new ServiceVersion(Service.WMS, "1.1.1"));

        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext("org.constellation.ws:org.constellation.wms.v111:" +
                "org.constellation.wms.v130:org.geotools.internal.jaxb.v110.sld",
                "http://www.opengis.net/wms");

        worker = new WMSWorker();
        LOGGER.info("WMS service running");
    }

    /**
     * Treat the incoming request and call the right function.
     *
     * @return an image or xml response.
     * @throw JAXBException
     */
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        try {
            final String request = (String) getParameter(KEY_REQUEST, true);
            LOGGER.info("New request: " + request);
            writeParameters();

            String version = (String) getParameter(KEY_VERSION, false);
            if (version != null) {
                setCurrentVersion(version);
            }

            if (GETMAP.equalsIgnoreCase(request)) {
                return getMap(adaptGetMap(true));
            }
            if (GETFEATUREINFO.equalsIgnoreCase(request)) {
                return getFeatureInfo(adaptGetFeatureInfo());
            }
            if (GETCAPABILITIES.equalsIgnoreCase(request)) {
                final GetCapabilities requestCapab = adaptGetCapabilities();
                final AbstractWMSCapabilities capab;
                try {
                    capab = (AbstractWMSCapabilities) getCapabilitiesObject(requestCapab.getVersion());
                } catch (IOException e) {
                    throw new WebServiceException("IO exception while getting Services Metadata:" +
                            e.getMessage(), INVALID_PARAMETER_VALUE, requestCapab.getVersion());
                }
                final AbstractWMSCapabilities capabilities = worker.getCapabilities(requestCapab, getServiceURL(), capab);
                //we marshall the response and return the XML String
                final StringWriter sw = new StringWriter();
                marshaller.setProperty("com.sun.xml.bind.xmlHeaders", (requestCapab.getVersion().toString().equals("1.1.1")) ?
                    "<!DOCTYPE WMT_MS_Capabilities SYSTEM \"http://schemas.opengis.net/wms/1.1.1/WMS_MS_Capabilities.dtd\">\n" : "");
                marshaller.marshal(capabilities, sw);
                return Response.ok(sw.toString(), requestCapab.getFormat()).build();
            }
            if (GETLEGENDGRAPHIC.equalsIgnoreCase(request)) {
                return getLegendGraphic(adaptGetLegendGraphic());
            }
            if (DESCRIBELAYER.equalsIgnoreCase(request)) {
                return describeLayer(adaptDescribeLayer());
            }
            throw new WebServiceException("The operation " + request +
                    " is not supported by the service", OPERATION_NOT_SUPPORTED, "request");

        } catch (WebServiceException ex) {
            final ServiceExceptionReport report = new ServiceExceptionReport(getCurrentVersion(),
                    new ServiceExceptionType(ex.getMessage(), (ExceptionCode) ex.getExceptionCode()));
            if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE)   &&
                !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED)&& 
                !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE)&& 
                !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED))
            {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            } else {
                LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getLocalizedMessage() + '\n');
            }
            StringWriter sw = new StringWriter();
            marshaller.marshal(report, sw);
            return Response.ok(Utils.cleanSpecialCharacter(sw.toString()), APP_XML).build();
        } catch (NumberFormatException n) {
            final ServiceExceptionReport report = new ServiceExceptionReport(getCurrentVersion(),
                    new ServiceExceptionType(n.getMessage(), INVALID_PARAMETER_VALUE));
            LOGGER.log(Level.INFO, n.getLocalizedMessage(), n);
            StringWriter sw = new StringWriter();
            marshaller.marshal(report, sw);
            return Response.ok(Utils.cleanSpecialCharacter(sw.toString()), APP_XML).build();
        }
    }

    /**
     * Return the value of a point in a map.
     *
     * @param gfi The {@linkplain GetFeatureInfo get feature info} request.
     * @return text, HTML , XML or GML code.
     *
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private synchronized Response getFeatureInfo(final GetFeatureInfo gfi) throws WebServiceException {

        String infoFormat = gfi.getInfoFormat();
        if(infoFormat == null) infoFormat = TEXT_PLAIN;

        final AbstractGraphicVisitor visitor;

        if (infoFormat.equalsIgnoreCase(TEXT_PLAIN)) {
            // TEXT / PLAIN
            visitor = new CSVGraphicVisitor();
        }else if (infoFormat.equalsIgnoreCase(TEXT_HTML)) {
            // TEXT / HTML
            visitor = new HTMLGraphicVisitor();
        }else if (infoFormat.equalsIgnoreCase(APP_GML) || infoFormat.equalsIgnoreCase(TEXT_XML) ||
                  infoFormat.equalsIgnoreCase(APP_XML) || infoFormat.equalsIgnoreCase(XML) ||
                  infoFormat.equalsIgnoreCase(GML))  {
            // GML
            visitor = new GMLGraphicVisitor(gfi);
        }else{
            throw new WebServiceException("This MIME type " + infoFormat + " is not accepted by the service",
                    INVALID_PARAMETER_VALUE, gfi.getVersion(), "info_format");
        }

        // We now build the response, according to the format chosen.
        try {
            WMSPortrayalAdapter.hit(gfi, visitor);
        } catch (PortrayalException ex) {
            throw new WebServiceException(ex, NO_APPLICABLE_CODE, gfi.getVersion());
        }

        return Response.ok(visitor.getResult(), infoFormat).build();
    }

    /**
     * Return a description of specified layers.
     *
     * @param descLayer The {@linkplain DescribeLayer describe layer} request.
     *
     * @throws JAXBException
     */
    private Response describeLayer(final DescribeLayer descLayer) throws JAXBException {
        OnlineResourceType or = new OnlineResourceType();
        or.setHref(getServiceURL() + "wcs?");

        List<LayerDescriptionType> layersDescriptions = new ArrayList<LayerDescriptionType>();
        final List<String> layers = descLayer.getLayers();
        for (String layer : layers) {
            final TypeNameType t = new TypeNameType(layer.trim());
            final LayerDescriptionType outputLayer = new LayerDescriptionType(or, t);
            layersDescriptions.add(outputLayer);
        }
        final DescribeLayerResponseType response = new DescribeLayerResponseType(getSldVersion().toString(),
                layersDescriptions);

        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();
        marshaller.marshal(response, sw);
        return Response.ok(sw.toString(), TEXT_XML).build();
    }

    /**
     * Return the legend graphic for the current layer.
     *
     * @param getLegend The {@linkplain GetLegendGraphic get legend graphic} request.
     * @return a file containing the legend graphic image.
     *
     * @throws org.constellation.coverage.web.WebServiceException
     * @throws javax.xml.bind.JAXBException
     */
    private Response getLegendGraphic(final GetLegendGraphic getLegend) throws WebServiceException {
        final ServiceVersion version = getLegend.getVersion();
        final NamedLayerDP dp = NamedLayerDP.getInstance();
        final LayerDetails layer = dp.get(getLegend.getLayer());
        if (layer == null) {
            throw new WebServiceException("Layer requested not found.", INVALID_PARAMETER_VALUE,
                    version, "layer");
        }
        final int width  = getLegend.getWidth();
        final int height = getLegend.getHeight();
        final Dimension dims = new Dimension(width, height);
        final BufferedImage image = layer.getLegendGraphic(dims);
        final String mimeType = getLegend.getFormat();
        
        return Response.ok(image, mimeType).build();
    }

    /**
     * Return a map for the specified parameters in the query: works with
     * the new GO2 Renderer.
     *
     * @param getMap The {@linkplain GetMap get map} request.
     * @return The map requested, or an error.
     * @throws WebServiceException
     */
    private synchronized Response getMap(final GetMap getMap) throws WebServiceException {
        final ServiceVersion queryVersion = getMap.getVersion();
        final String errorType = getMap.getExceptionFormat();
        final boolean errorInImage = EXCEPTIONS_INIMAGE.equalsIgnoreCase(errorType);
        final String format = getMap.getFormat();
        
        BufferedImage image = null;
        try {
            image = WMSPortrayalAdapter.portray(getMap);
        } catch (PortrayalException ex) {
            if(errorInImage) {
                final Dimension dim = getMap.getSize();
                image = CSTLPortrayalService.getInstance().writeInImage(ex, dim.width, dim.height);
            } else {
                throw new WebServiceException(ex, NO_APPLICABLE_CODE, queryVersion);
            }
        } catch (WebServiceException ex) {
            if (errorInImage) {
                final Dimension dim = getMap.getSize();
                image = CSTLPortrayalService.getInstance().writeInImage(ex, dim.width, dim.height);
            } else {
                throw new WebServiceException(ex, LAYER_NOT_DEFINED, queryVersion);
            }
        }

        return Response.ok(image, format).build();
    }

    /**
     * Converts a DescribeLayer request composed of string values, to a container
     * of real java objects.
     *
     * @return The DescribeLayer request.
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private DescribeLayer adaptDescribeLayer() throws WebServiceException {
        final String strLayer  = getParameter(KEY_LAYERS,  true );
        final String strVersion = getParameter(KEY_VERSION, false);
        final List<String> layers = QueryAdapter.toStringList(strLayer);
        setCurrentVersion(strVersion);
        return new DescribeLayer(layers, getCurrentVersion());
   }

    /**
     * Converts a GetCapabilities request composed of string values, to a container
     * of real java objects.
     *
     * @return A GetCapabilities request.
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private GetCapabilities adaptGetCapabilities() throws WebServiceException {
        final String version = getParameter(KEY_VERSION, false);
        if (version == null) {
            setCurrentVersion("1.1.1");
            return new GetCapabilities(getCurrentVersion());
        }
        final ServiceVersion bestVersion = getBestVersion(version);
        final String service = getParameter(KEY_SERVICE, true);
        if (!Service.WMS.toString().equalsIgnoreCase(service)) {
            throw new WebServiceException("Invalid service specified. Should be WMS.",
                    INVALID_PARAMETER_VALUE, bestVersion, "service");
        }
        String format = getParameter(KEY_FORMAT, false);
        if (format == null || !(format.equalsIgnoreCase(TEXT_XML) ||
                format.equalsIgnoreCase(APP_WMS_XML) || format.equalsIgnoreCase(APP_XML)))
        {
            format = TEXT_XML;
        }
        return new GetCapabilities(bestVersion, format);
    }

    /**
     * Converts a GetFeatureInfo request composed of string values, to a container
     * of real java objects.
     *
     * @return A GetFeatureInfo request.
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private GetFeatureInfo adaptGetFeatureInfo() throws WebServiceException, NumberFormatException {
        final GetMap getMap  = adaptGetMap(false);
        final String version = getParameter(KEY_VERSION, true);
        setCurrentVersion(version);
        final String strX    = getParameter(version.equals("1.1.1") ? KEY_I_v110 : KEY_I_v130, true);
        final String strY    = getParameter(version.equals("1.1.1") ? KEY_J_v110 : KEY_J_v130, true);
        final String strQueryLayers = getParameter(KEY_QUERY_LAYERS, true);
        final String infoFormat  = getParameter(KEY_INFO_FORMAT, true);
        final String strFeatureCount = getParameter(KEY_FEATURE_COUNT, false);
        final List<String> queryLayers = QueryAdapter.toStringList(strQueryLayers);
        final List<String> queryableLayers = QueryAdapter.areQueryableLayers(queryLayers, getCurrentVersion());
        final int x = QueryAdapter.toInt(strX);
        final int y = QueryAdapter.toInt(strY);
        final int featureCount = QueryAdapter.toFeatureCount(strFeatureCount);
        return new GetFeatureInfo(getMap, x, y, queryableLayers, infoFormat, featureCount);
    }

    /**
     * Converts a GetLegendGraphic request composed of string values, to a container
     * of real java objects.
     *
     * @return The GetLegendGraphic request.
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private GetLegendGraphic adaptGetLegendGraphic() throws WebServiceException {
        final String strLayer  = getParameter(KEY_LAYER,  true );
        final String strFormat = getParameter(KEY_FORMAT, true );
        final String strWidth  = getParameter(KEY_WIDTH,  false);
        final String strHeight = getParameter(KEY_HEIGHT, false);
        final String format;
        try {
            format = QueryAdapter.toFormat(strFormat);
        } catch (IllegalArgumentException i) {
            throw new WebServiceException(i, INVALID_FORMAT,
                    new ServiceVersion(Service.WMS, "1.1.0"));
        }
        if (strWidth == null || strHeight == null) {
            return new GetLegendGraphic(strLayer, strFormat);
        } else {
            final int width;
            final int height;
            try {
                width  = QueryAdapter.toInt(strWidth);
                height = QueryAdapter.toInt(strHeight);
            } catch (NumberFormatException n) {
                throw new WebServiceException(n, INVALID_PARAMETER_VALUE,
                        new ServiceVersion(Service.WMS, "1.1.0"));
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
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private GetMap adaptGetMap(final boolean fromGetMap) throws WebServiceException {
        final String version         = getParameter(KEY_VERSION,         true);
        setCurrentVersion(version);
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
        final String strAzimuth      = getParameter(KEY_AZIMUTH,    false);
        final String strStyles       = getParameter(KEY_STYLES, ((urlSLD != null) 
                && (version.equals("1.1.1"))) ? false : fromGetMap);

        final CoordinateReferenceSystem crs;
        try {
            crs = QueryAdapter.toCRS(strCRS);
        } catch (FactoryException ex) {
            throw new WebServiceException(ex, INVALID_CRS, getCurrentVersion());
        }
        final ImmutableEnvelope env;
        try {
            env = (ImmutableEnvelope) QueryAdapter.toEnvelope(strBBox, crs);
            //TODO change to this method when renderer will support 4D BBox
//            env = QueryAdapter.toEnvelope(strBBox, crs, strElevation, strTime,wmsVersion);
        } catch (IllegalArgumentException i) {
            throw new WebServiceException(i, INVALID_PARAMETER_VALUE, getCurrentVersion());
        }
        final String format;
        try {
            format = QueryAdapter.toFormat(strFormat);
        } catch (IllegalArgumentException i) {
            throw new WebServiceException(i, INVALID_FORMAT, getCurrentVersion());
        }
        final List<String> layers  = QueryAdapter.toStringList(strLayers);
        final List<String> styles = QueryAdapter.toStringList(strStyles);
        MutableStyledLayerDescriptor sld = null;
        final Double elevation;
        try {
            elevation = (strElevation != null) ? QueryAdapter.toDouble(strElevation) : null;
        } catch (NumberFormatException n) {
            throw new WebServiceException(n, INVALID_PARAMETER_VALUE, getCurrentVersion());
        }
        final MeasurementRange dimRange = QueryAdapter.toMeasurementRange(strDimRange);
        final Date date;
        try {
            date = QueryAdapter.toDate(strTime);
        } catch (ParseException ex) {
            throw new WebServiceException(ex, INVALID_PARAMETER_VALUE, getCurrentVersion());
        }
        final int width;
        final int height;
        try {
            width  = QueryAdapter.toInt(strWidth);
            height = QueryAdapter.toInt(strHeight);
        } catch (NumberFormatException n) {
            throw new WebServiceException(n, INVALID_PARAMETER_VALUE, getCurrentVersion());
        }
        final Dimension size = new Dimension(width, height);
        final Color background = QueryAdapter.toColor(strBGColor);
        final boolean transparent = QueryAdapter.toBoolean(strTransparent);

        if (strRemoteOwsUrl != null) {
            InputStream in = null;
            try {
                in = new FileInputStream(new File(strRemoteOwsUrl));
            } catch (FileNotFoundException ex) {
                throw new WebServiceException(ex, STYLE_NOT_DEFINED, getCurrentVersion());
            }
            final XMLUtilities sldparser = new XMLUtilities();
            try {
                sld = sldparser.readSLD(in,
                        org.geotools.style.sld.Specification.StyledLayerDescriptor.V_1_0_0);
            } catch (JAXBException ex) {
                throw new WebServiceException(ex, STYLE_NOT_DEFINED, getCurrentVersion());
            }
            if (sld == null) {
                try {
                    sld = sldparser.readSLD(in,
                            org.geotools.style.sld.Specification.StyledLayerDescriptor.V_1_1_0);
                } catch (JAXBException ex) {
                    throw new WebServiceException(ex, STYLE_NOT_DEFINED, getCurrentVersion());
                }
            }
        } else {
            try {
                sld = QueryAdapter.toSLD(urlSLD);
            } catch (MalformedURLException ex) {
                throw new WebServiceException(ex, STYLE_NOT_DEFINED, getCurrentVersion());
            }
        }

        double azimuth = 0;
        if(strAzimuth != null){
            try{
                azimuth = QueryAdapter.toDouble(strAzimuth);
            }catch(NumberFormatException ex){
                throw new WebServiceException(ex, INVALID_PARAMETER_VALUE, getCurrentVersion());
            }
        }

        // Builds the request.
        return new GetMap(env, getCurrentVersion(), format, layers, styles, sld, elevation,
                    date, dimRange, size, background, transparent, azimuth, strExceptions);
    }

    /**
     * TODO.
     */
    @PreDestroy
    public void destroy() {
        LOGGER.info("destroying WMS service");
    }
}
