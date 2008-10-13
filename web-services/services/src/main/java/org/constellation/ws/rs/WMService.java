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
package org.constellation.ws.rs;

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
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.measure.unit.Unit;
import javax.naming.NamingException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

//Constellation dependencies
import org.constellation.catalog.CatalogException;
import org.constellation.coverage.web.Service;
import org.constellation.coverage.web.WMSWebServiceException;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.coverage.web.ServiceVersion;
import org.constellation.util.PeriodUtilities;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.wms.AbstractDCP;
import org.constellation.wms.AbstractDimension;
import org.constellation.wms.AbstractLayer;
import org.constellation.wms.AbstractRequest;
import org.constellation.wms.AbstractOperation;
import org.constellation.wms.AbstractProtocol;
import org.constellation.wms.v111.LatLonBoundingBox;
import org.constellation.wms.v130.OperationType;
import org.constellation.wms.v130.EXGeographicBoundingBox;
import org.constellation.portrayal.CSTLPortrayalService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedLayerDP;
import org.constellation.query.QueryAdapter;
import org.constellation.query.wms.GetMap;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.query.wms.WMSQuery;
import org.constellation.query.wms.WMSQueryVersion;
import org.constellation.wms.AbstractHTTP;

// Geotools dependencies
import org.geotools.display.exception.PortrayalException;
import org.geotools.geometry.ImmutableEnvelope;
import org.geotools.sld.MutableStyledLayerDescriptor;
import org.geotools.style.sld.XMLUtilities;
import org.geotools.util.MeasurementRange;

//Geoapi dependencies
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeType;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.constellation.coverage.wms.WMSExceptionCode.*;
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
public class WMService extends WebService {
    /**
     * Build a new instance of the webService and initialise the JAXB marshaller.
     */
    public WMService() throws JAXBException, SQLException, IOException, NamingException {
        super("WMS", new ServiceVersion(Service.WMS, WMSQueryVersion.WMS_1_3_0.toString()),
                     new ServiceVersion(Service.WMS, WMSQueryVersion.WMS_1_1_1.toString()));

        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext("org.constellation.coverage.web:org.constellation.wms.v111:org.constellation.wms.v130:" +
                "org.constellation.sld.v110:org.constellation.gml.v311", "http://www.opengis.net/wms");

        LOGGER.info("WMS service running");
    }

    /**
     * Treat the incoming request and call the right function.
     *
     * @return an image or xml response.
     * @throw JAXBException
     */
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        WMSQuery query = null;
        try {
            final String request = (String) getParameter(KEY_REQUEST, true);
            LOGGER.info("New request: " + request);
            writeParameters();

            if (GETMAP.equalsIgnoreCase(request)) {
                query = adaptGetMap();
                return getMap(query);
            }
            if (GETFEATUREINFO.equalsIgnoreCase(request)) {
                query = adaptGetFeatureInfo();
                return getFeatureInfo(query);
            }
            if (GETCAPABILITIES.equalsIgnoreCase(request)) {
                query = adaptGetCapabilities();
                return getCapabilities(query);
            }
            if (GETLEGENDGRAPHIC.equalsIgnoreCase(request)) {
                query = adaptGetLegendGraphic();
                final String mimeType = getParameter(KEY_FORMAT, true);
                return Response.ok(getLegendGraphic(query), mimeType).build();
            }
            final String version = (String) getParameter(KEY_VERSION, false);
            final WMSQueryVersion queryVersion;
            if (version == null) {
                queryVersion = WMSQueryVersion.WMS_1_1_1;
            } else {
                queryVersion = (version.equalsIgnoreCase(WMSQueryVersion.WMS_1_1_1.toString())) ?
                    WMSQueryVersion.WMS_1_1_1 : WMSQueryVersion.WMS_1_3_0;
            }
            throw new WMSWebServiceException("The operation " + request + " is not supported by the service",
                    OPERATION_NOT_SUPPORTED, queryVersion);

        } catch (WebServiceException ex) {
            /* We don't print the stack trace:
             * - if the user have forget a mandatory parameter.
             * - if the version number is wrong.
             */
            if (ex instanceof WMSWebServiceException) {
                WMSWebServiceException wmsex = (WMSWebServiceException)ex;
                if (!wmsex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
                    !wmsex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) &&
                    !wmsex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED))
                {
                    wmsex.printStackTrace();
                }
                StringWriter sw = new StringWriter();
                marshaller.marshal(wmsex.getExceptionReport(), sw);
                return Response.ok(cleanSpecialCharacter(sw.toString()),
                                   (query == null) ? "application/vnd.ogc.se_xml" : query.getExceptionFormat()).build();
            } else {
                throw new IllegalArgumentException("this service can't return OWS Exception");
            }
        } catch (NumberFormatException n) {
            String version;
            try {
                version = (String) getParameter(KEY_VERSION, false);
            } catch (WebServiceException ex) {
                version = WMSQueryVersion.WMS_1_1_1.toString();
            }
            final WMSQueryVersion queryVersion;
            if (version == null) {
                queryVersion = WMSQueryVersion.WMS_1_1_1;
            } else {
                queryVersion = (version.equalsIgnoreCase(WMSQueryVersion.WMS_1_1_1.toString())) ?
                    WMSQueryVersion.WMS_1_1_1 : WMSQueryVersion.WMS_1_3_0;
            }
            final WMSWebServiceException wmsEx = new WMSWebServiceException(n, INVALID_PARAMETER_VALUE, queryVersion);
            StringWriter sw = new StringWriter();
            marshaller.marshal(wmsEx.getExceptionReport(), sw);
            return Response.ok(cleanSpecialCharacter(sw.toString()),
                                   (query == null) ? "application/vnd.ogc.se_xml" : query.getExceptionFormat()).build();
        }
    }

    /**
     * Describe the capabilities and the layers available of this service.
     *
     * @param query The {@linkplain WMSQuery wms query}.
     * @return a WMSCapabilities XML document describing the capabilities of the service.
     *
     * @throws org.constellation.coverage.web.WebServiceException
     * @throws javax.xml.bind.JAXBException
     */
    private Response getCapabilities(final WMSQuery query) throws WebServiceException, JAXBException {
        //we begin by extracting the mandatory attribute
        if (!(query instanceof GetCapabilities)) {
            throw new WMSWebServiceException("Invalid request found, should be GetCapabilities.",
                    INVALID_REQUEST, query.getVersion());
        }
        final GetCapabilities capabRequest = (GetCapabilities) query;
        //and the the optional attribute
        final WMSQueryVersion queryVersion = capabRequest.getVersion();
        String format = getParameter(KEY_FORMAT, false);
        if (format == null || !(format.equals("text/xml") || format.equals("application/vnd.ogc.wms_xml"))) {
            format = "text/xml";
        }

        final AbstractWMSCapabilities response;
        // String updateSequence = getParameter("UPDATESEQUENCE", false);

        // the service shall return WMSCapabilities marshalled
        try {
            response = (AbstractWMSCapabilities)getCapabilitiesObject(queryVersion);
        } catch(IOException e)   {
            throw new WMSWebServiceException("IO exception while getting Services Metadata:" + e.getMessage(),
                      INVALID_PARAMETER_VALUE, queryVersion);
        }

        //we build the list of accepted crs
        final List<String> crs = new ArrayList<String>();
        crs.add("CRS:84");     crs.add("EPSG:4326");  crs.add("EPSG:3395");
        crs.add("EPSG:27571"); crs.add("EPSG:27572"); crs.add("EPSG:27573"); crs.add("EPSG:27574");
        //we update the url in the static part.
        response.getService().getOnlineResource().setHref(getServiceURL() + "wms");
        final AbstractRequest request = response.getCapability().getRequest();

        updateURL(request.getGetCapabilities().getDCPType());
        updateURL(request.getGetFeatureInfo().getDCPType());
        updateURL(request.getGetMap().getDCPType());
        updateExtendedOperationURL(request, queryVersion);

        //we get the list of layers
        final List<AbstractLayer> layers = new ArrayList<AbstractLayer>();

        final NamedLayerDP dp = NamedLayerDP.getInstance();
        final Set<String> keys = dp.getKeys();
        for (String key : keys) {
            final LayerDetails layer = dp.get(key);
            if (layer == null) {
                LOGGER.warning("Missing layer : " + key);
                continue;
            }
            if (!layer.isQueryable(Service.WMS)) {
                LOGGER.info("layer" + layer.getName() + " not queryable by WMS");
                continue;
            }
            /*
             *  TODO
             * code = CRS.lookupEpsgCode(inputLayer.getCoverageReference().getCoordinateReferenceSystem(), false);
             */
            final GeographicBoundingBox inputGeoBox;
            try {
                inputGeoBox = layer.getGeographicBoundingBox();
            } catch (CatalogException exception) {
                throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, queryVersion);
            }

            // List of elevations, times and dim_range values.
            final List<AbstractDimension> dimensions = new ArrayList<AbstractDimension>();

            //the available date
            String defaut = null;
            AbstractDimension dim;
            SortedSet<Date> dates = null;
            try {
                dates = layer.getAvailableTimes();
            } catch (CatalogException ex) {
                dates = null;
            }
            if (dates != null && !(dates.isEmpty())) {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                final PeriodUtilities periodFormatter = new PeriodUtilities(df);
                defaut = df.format(dates.last());
                dim = (queryVersion.equals(WMSQueryVersion.WMS_1_1_1)) ?
                    new org.constellation.wms.v111.Dimension("time", "ISO8601", defaut, null) :
                    new org.constellation.wms.v130.Dimension("time", "ISO8601", defaut, null);
                dim.setValue(periodFormatter.getDatesRespresentation(dates));
                dimensions.add(dim);
            }

            //the available elevation
            defaut = null;
            SortedSet<Number> elevations = null;
            try {
                elevations = layer.getAvailableElevations();
            } catch (CatalogException ex) {
                elevations = null;
            }
            if (elevations != null && !(elevations.isEmpty())) {
                defaut = elevations.first().toString();
                dim = (queryVersion.equals(WMSQueryVersion.WMS_1_1_1)) ?
                    new org.constellation.wms.v111.Dimension("elevation", "EPSG:5030", defaut, null) :
                    new org.constellation.wms.v130.Dimension("elevation", "EPSG:5030", defaut, null);
                final StringBuilder elevs = new StringBuilder();
                for (Iterator<Number> it = elevations.iterator(); it.hasNext();) {
                    final Number n = it.next();
                    elevs.append(n.toString());
                    if (it.hasNext()) {
                        elevs.append(',');
                    }
                }
                dim.setValue(elevs.toString());
                dimensions.add(dim);
            }

            //the dimension range
            defaut = null;
            final MeasurementRange[] ranges = layer.getSampleValueRanges();
            /* If the layer has only one sample dimension, then we can apply the dim_range
             * parameter. Otherwise it can be a multiple sample dimensions layer, and we
             * don't apply the dim_range.
             */
            if (ranges != null && ranges.length == 1 && ranges[0] != null) {
                final MeasurementRange firstRange = ranges[0];
                final double minRange = firstRange.getMinimum();
                final double maxRange = firstRange.getMaximum();
                defaut = minRange + "," + maxRange;
                final Unit<?> u = firstRange.getUnits();
                final String unit = (u != null) ? u.toString() : null;
                dim = (queryVersion.equals(WMSQueryVersion.WMS_1_1_1)) ?
                    new org.constellation.wms.v111.Dimension("dim_range", unit, defaut, minRange + "," + maxRange) :
                    new org.constellation.wms.v130.Dimension("dim_range", unit, defaut, minRange + "," + maxRange);
                dimensions.add(dim);
            }

            // LegendUrl generation
            final String layerName = layer.getName();
            final String beginLegendUrl = getServiceURL() + "wms?REQUEST=GetLegendGraphic&VERSION=1.1.0&FORMAT=";
            final String formatPng = "image/png";
            final String formatGif = "image/gif";
            final String legendUrlGif = beginLegendUrl + formatGif + "&LAYER=" + layerName;
            final String legendUrlPng = beginLegendUrl + formatPng + "&LAYER=" + layerName;
            final int queryable = (layer.isQueryable(Service.GETINFO) == true) ? 1 : 0;
            final AbstractLayer outputLayer;
            if (queryVersion.equals(WMSQueryVersion.WMS_1_1_1)) {
                /*
                 * TODO
                 * Envelope inputBox = inputLayer.getCoverage().getEnvelope();
                 */
                final org.constellation.wms.v111.BoundingBox outputBBox = (inputGeoBox != null) ?
                    new org.constellation.wms.v111.BoundingBox("EPSG:4326", inputGeoBox.getWestBoundLongitude(),
                            inputGeoBox.getSouthBoundLatitude(), inputGeoBox.getEastBoundLongitude(),
                            inputGeoBox.getNorthBoundLatitude(), 0.0, 0.0, queryVersion.toString()) :
                    null;

                org.constellation.wms.v111.OnlineResource or = new org.constellation.wms.v111.OnlineResource(legendUrlPng);
                org.constellation.wms.v111.LegendURL legendURL1 = new org.constellation.wms.v111.LegendURL(formatPng, or);

                or = new org.constellation.wms.v111.OnlineResource(legendUrlGif);
                org.constellation.wms.v111.LegendURL legendURL2 = new org.constellation.wms.v111.LegendURL(formatGif, or);
                org.constellation.wms.v111.Style style = new org.constellation.wms.v111.Style(
                        "Style1", "default Style", null, null, null, legendURL1, legendURL2);

                outputLayer = new org.constellation.wms.v111.Layer(layerName, cleanSpecialCharacter(layer.getRemarks()),
                        cleanSpecialCharacter(layer.getThematic()), crs,
                        new LatLonBoundingBox(inputGeoBox.getWestBoundLongitude(), inputGeoBox.getSouthBoundLatitude(),
                                              inputGeoBox.getEastBoundLongitude(), inputGeoBox.getNorthBoundLatitude()),
                        outputBBox, queryable, dimensions, style);
            } else {
                /*
                 * TODO
                 * Envelope inputBox = inputLayer.getCoverage().getEnvelope();
                 */
                final org.constellation.wms.v130.BoundingBox outputBBox = (inputGeoBox != null) ?
                    new org.constellation.wms.v130.BoundingBox("EPSG:4326", inputGeoBox.getWestBoundLongitude(),
                            inputGeoBox.getSouthBoundLatitude(), inputGeoBox.getEastBoundLongitude(),
                            inputGeoBox.getNorthBoundLatitude(), 0.0, 0.0, queryVersion.toString()) :
                    null;

                // we build a Style Object
                org.constellation.wms.v130.OnlineResource or = new org.constellation.wms.v130.OnlineResource(legendUrlPng);
                org.constellation.wms.v130.LegendURL legendURL1 = new org.constellation.wms.v130.LegendURL(formatPng, or);

                or = new org.constellation.wms.v130.OnlineResource(legendUrlGif);
                org.constellation.wms.v130.LegendURL legendURL2 = new org.constellation.wms.v130.LegendURL(formatGif, or);
                org.constellation.wms.v130.Style style = new org.constellation.wms.v130.Style(
                        "Style1", "default Style", null, null, null, legendURL1, legendURL2);

                outputLayer = new org.constellation.wms.v130.Layer(layerName, cleanSpecialCharacter(layer.getRemarks()),
                        cleanSpecialCharacter(layer.getThematic()), crs,
                        new EXGeographicBoundingBox(inputGeoBox.getWestBoundLongitude(), inputGeoBox.getSouthBoundLatitude(),
                        inputGeoBox.getEastBoundLongitude(), inputGeoBox.getNorthBoundLatitude()),
                        outputBBox, queryable, dimensions, style);
            }
            layers.add(outputLayer);
        }

        //we build the general layer and add it to the document
        final AbstractLayer mainLayer = (queryVersion.equals(WMSQueryVersion.WMS_1_1_1)) ?
            new org.constellation.wms.v111.Layer("Constellation Web Map Layer",
                    "description of the service(need to be fill)", crs, null, layers) :
            new org.constellation.wms.v130.Layer("Constellation Web Map Layer",
                    "description of the service(need to be fill)", crs, null, layers);

        response.getCapability().setLayer(mainLayer);

        //we marshall the response and return the XML String
        final StringWriter sw = new StringWriter();
        marshaller.setProperty("com.sun.xml.bind.xmlHeaders", (queryVersion.equals(WMSQueryVersion.WMS_1_1_1)) ?
            "<!DOCTYPE WMT_MS_Capabilities SYSTEM \"http://schemas.opengis.net/wms/1.1.1/WMS_MS_Capabilities.dtd\">\n" : "");
        marshaller.marshal(response, sw);

        return Response.ok(sw.toString(), format).build();
    }

    /**
     * Return the value of a point in a map.
     *
     * @return text, HTML , XML or GML code.
     *
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private synchronized Response getFeatureInfo(final WMSQuery query) throws WMSWebServiceException {
        if (!(query instanceof GetFeatureInfo)) {
            throw new WMSWebServiceException("Invalid request found, should be GetFeatureInfo.",
                    INVALID_REQUEST, query.getVersion());
        }
        final GetFeatureInfo info = (GetFeatureInfo) query;
        final WMSQueryVersion queryVersion = info.getVersion();

        String infoFormat = info.getInfoFormat();
        if (infoFormat != null) {
            if(!(infoFormat.equalsIgnoreCase("text/plain") || infoFormat.equalsIgnoreCase("text/html") ||
                 infoFormat.equalsIgnoreCase("application/vnd.ogc.gml") || infoFormat.equalsIgnoreCase("text/xml") ||
                 infoFormat.equalsIgnoreCase("application/vnd.ogc.xml") || infoFormat.equalsIgnoreCase("xml") ||
                 infoFormat.equalsIgnoreCase("gml")))
            {
                throw new WMSWebServiceException("This MIME type " + infoFormat +
                        " is not accepted by the service", INVALID_PARAMETER_VALUE, queryVersion);
            }
        } else {
            infoFormat = "text/plain";
        }
        final NamedLayerDP dp = NamedLayerDP.getInstance();
        final List<String> layers = info.getQueryLayers();
        final int size = layers.size();
        /* Now proceed to the calculation of the values, and use the toString method to store them.
         * This map will store couples of <layerName, List<values>> obtained by the getInformationAt() method.
         */
        final Map<String, List<String>> results = new HashMap<String, List<String>>(size);
        for (final String key : layers) {
            final LayerDetails layer = dp.get(key);
            if (!layer.isQueryable(Service.GETINFO)) {
                throw new WMSWebServiceException("The requested layer \""+ key +"\" for a GetFeatureInfo" +
                        " is not queryable.", OPERATION_NOT_SUPPORTED, queryVersion);
            }
            final Object currentValue;
            try {
                currentValue = layer.getInformationAt(info);
            } catch (CatalogException cat) {
                throw new WMSWebServiceException(cat, NO_APPLICABLE_CODE, queryVersion);
            } catch (IOException io) {
                throw new WMSWebServiceException(io, NO_APPLICABLE_CODE, queryVersion);
            }
            final List<String> values = new ArrayList<String>();
            if (currentValue instanceof Double) {
                values.add(Float.toString(((Double) currentValue).floatValue()));
            } else {
                final List<SimpleFeature> features = (List<SimpleFeature>) currentValue;
                // Defines how many features we will take in the list of results.
                final int featuresSize = (info.getFeatureCount() > features.size()) ?
                                          features.size() : info.getFeatureCount();
                // Fill the list of values with features found.
                for (int i=0; i<featuresSize; i++) {
                    final SimpleFeature feature = features.get(i);
                    final List<Object> attrs = feature.getAttributes();
                    final List<AttributeType> attrTypes = feature.getFeatureType().getTypes();
                    final StringBuilder value = new StringBuilder();
                    for (int j = 0; j < attrs.size(); j++) {
                        value.append(attrTypes.get(j).getName().toString() + "[" +
                                attrs.get(j).toString() + "] ");
                    }
                    values.add(value.toString());
                }
            }
            results.put(key, values);
        }

        // We now build the response, according to the format chosen.
        final StringBuilder response = new StringBuilder();
        // TEXT / PLAIN
        if (infoFormat.equalsIgnoreCase("text/plain")) {
            for (String layer : layers) {
                final List<String> values = results.get(layer);
                response.append((values.size() < 2) ? "Result for " : "Results for ").append(layer);
                response.append((values.size() < 2) ? " is :" : " are : ");
                for (String value : values) {
                    response.append(value).append("\n");
                }
            }
            return Response.ok(response.toString(), infoFormat).build();
        }

        // TEXT / HTML
        if (infoFormat.equalsIgnoreCase("text/html")) {
            response.append("<html>\n")
                    .append("    <head>\n")
                    .append("        <title>GetFeatureInfo output</title>\n")
                    .append("    </head>\n")
                    .append("    <body>\n")
                    .append("    <table>\n");
            for (String layer : layers) {
                response.append("       <tr>")
                        .append("           <th>").append(layer).append("</th>")
                        .append("       </tr>");
                final List<String> values = results.get(layer);
                for (String value : values) {
                    response.append("       <tr>")
                            .append("           <th>")
                            .append(value)
                            .append("           </th>")
                            .append("       </tr>");
                }
            }
            response.append("    </table>\n")
                    .append("    </body>\n")
                    .append("</html>");
            return Response.ok(response.toString(), infoFormat).build();
        }

        // GML
        if (infoFormat.equalsIgnoreCase("application/vnd.ogc.gml") || infoFormat.equalsIgnoreCase("text/xml") ||
            infoFormat.equalsIgnoreCase("application/vnd.ogc.xml") || infoFormat.equalsIgnoreCase("xml") ||
            infoFormat.equalsIgnoreCase("gml"))
        {
            // todo: returns gml information of features
            throw new WMSWebServiceException("Unsupported info format chosen", INVALID_FORMAT, queryVersion);
        }

        // Info format not handled.
        throw new WMSWebServiceException("Unsupported info format chosen", INVALID_FORMAT, queryVersion);
    }

    /**
     * Return the legend graphic for the current layer.
     *
     * @param query The {@linkplain WMSQuery wms query}.
     * @return a file containing the legend graphic image.
     *
     * @throws org.constellation.coverage.web.WebServiceException
     * @throws javax.xml.bind.JAXBException
     */
    private synchronized File getLegendGraphic(final WMSQuery query) throws WMSWebServiceException,
                                                                            JAXBException
    {
        if (!(query instanceof GetLegendGraphic)) {
            throw new WMSWebServiceException("Invalid request found, should be GetLegendGraphic.",
                    INVALID_REQUEST, query.getVersion());
        }
        final WMSQueryVersion version = query.getVersion();
        final GetLegendGraphic legendRequest = (GetLegendGraphic) query;
        final NamedLayerDP dp = NamedLayerDP.getInstance();
        final LayerDetails layer = dp.get(legendRequest.getLayer());
        if (layer == null) {
            throw new WMSWebServiceException("Layer requested not found.", INVALID_PARAMETER_VALUE,
                    version);
        }
        final int width  = legendRequest.getWidth();
        final int height = legendRequest.getHeight();
        final Dimension dims = new Dimension(width, height);
        final BufferedImage image = layer.getLegendGraphic(dims);
        final String mimeType = legendRequest.getFormat();
        try {
            final File legendFile = createTempFile("legend", mimeType);
            legendFile.deleteOnExit();
            writeImage(image, mimeType, legendFile);
            return legendFile;
        } catch (IOException ex) {
            throw new WMSWebServiceException(ex, NO_APPLICABLE_CODE, version);
        }
    }

    /**
     * Return a map for the specified parameters in the query: works with
     * the new GO2 Renderer.
     *
     * @param query The {@linkplain WMSQuery wms query}.
     * @return The map requested, or an error.
     * @throws WebServiceException
     */
    private synchronized Response getMap(final WMSQuery query) throws WMSWebServiceException {
        //verifyBaseParameter(0);
        if (!(query instanceof GetMap)) {
            throw new WMSWebServiceException("Invalid request found, should be GetMap.",
                    INVALID_REQUEST, query.getVersion());
        }
        final GetMap getMap = (GetMap) query;
        final WMSQueryVersion queryVersion = getMap.getVersion();
        final String errorType = getMap.getExceptionFormat();
        final boolean errorInImage = EXCEPTIONS_INIMAGE.equalsIgnoreCase(errorType);
        final String format = getMap.getFormat();
        final File imageFile;
        try {
            imageFile = createTempFile("map", format);
        } catch (IOException io) {
            throw new WMSWebServiceException(io, NO_APPLICABLE_CODE, queryVersion);
        }

        File errorFile = null;
        try {
            CSTLPortrayalService.getInstance().portray(getMap, imageFile);
        } catch (PortrayalException ex) {
            if(errorInImage) {
                try {
                    errorFile = createTempFile("map", format);
                    final Dimension dim = getMap.getSize();
                    CSTLPortrayalService.writeInImage(ex, dim.width, dim.height, errorFile, format);
                } catch (IOException io) {
                    throw new WMSWebServiceException(io, NO_APPLICABLE_CODE, queryVersion);
                }
            } else {
                throw new WMSWebServiceException("The requested map could not be renderered correctly :" +
                        ex.getMessage(), NO_APPLICABLE_CODE, queryVersion);
            }
        } catch (WebServiceException ex) {
            if (errorInImage) {
                try {
                    errorFile = createTempFile("map", format);
                    final Dimension dim = getMap.getSize();
                    CSTLPortrayalService.writeInImage(ex, dim.width, dim.height, errorFile, format);
                } catch (IOException io) {
                    throw new WMSWebServiceException(io, NO_APPLICABLE_CODE, queryVersion);
                }
            } else {
                throw new WMSWebServiceException(ex, LAYER_NOT_DEFINED, queryVersion);
            }
        }

        final File result = (errorFile != null) ? errorFile : imageFile;
        return Response.ok(result, getMap.getFormat()).build();
    }

    /**
     * Check if the provided object is an instance of one of the given classes.
     */
    private static synchronized boolean isValidType(final Class<?>[] validTypes, final Object type) {
        for (final Class<?> t : validTypes) {
            if (t.isInstance(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * update The URL in capabilities document with the service actual URL.
     */
    private void updateURL(final List<? extends AbstractDCP> dcpList) {
        for(AbstractDCP dcp: dcpList) {
            final AbstractHTTP http = dcp.getHTTP();
            final AbstractProtocol getMethod = http.getGet();
            if (getMethod != null) {
                getMethod.getOnlineResource().setHref(getServiceURL() + "wms?SERVICE=WMS&");
            }
            final AbstractProtocol postMethod = http.getPost();
            if (postMethod != null) {
                postMethod.getOnlineResource().setHref(getServiceURL() + "wms?SERVICE=WMS&");
            }
        }
    }

    /**
     * update The URL in capabilities document for the extended operation.
     */
    private void updateExtendedOperationURL(final AbstractRequest request, final WMSQueryVersion version) {

        if (version.equals(WMSQueryVersion.WMS_1_3_0)) {
            org.constellation.wms.v130.Request r = (org.constellation.wms.v130.Request) request;
            List<JAXBElement<OperationType>> extendedOperations = r.getExtendedOperation();
            for(JAXBElement<OperationType> extOp: extendedOperations) {
                updateURL(extOp.getValue().getDCPType());
            }

        // version 1.1.1
        } else {
           org.constellation.wms.v111.Request r = (org.constellation.wms.v111.Request) request;
           AbstractOperation op = r.getDescribeLayer();
           if (op != null)
                updateURL(op.getDCPType());
           op = r.getGetLegendGraphic();
           if (op != null)
                updateURL(op.getDCPType());
           op = r.getGetStyles();
           if (op != null)
                updateURL(op.getDCPType());
           op = r.getPutStyles();
           if (op != null)
                updateURL(op.getDCPType());
        }
    }

    /**
     * Converts a GetCapabilities request composed of string values, to a container of real java objects.
     *
     * @return A GetCapabilities request.
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private GetCapabilities adaptGetCapabilities() throws WebServiceException {
        final String version = getParameter(KEY_VERSION, false);
        if (version == null) {
            return new GetCapabilities(WMSQueryVersion.WMS_1_1_1);
        }
        final ServiceVersion bestVersion = getBestVersion(version);
        final WMSQueryVersion wmsVersion = (bestVersion.toString().equals(WMSQueryVersion.WMS_1_1_1.toString())) ?
                    WMSQueryVersion.WMS_1_1_1 : WMSQueryVersion.WMS_1_3_0;
        final String service = getParameter(KEY_SERVICE, true);
        if (!Service.WMS.toString().equalsIgnoreCase(service)) {
            throw new WMSWebServiceException("Invalid service specified. Should be WMS.",
                    INVALID_PARAMETER_VALUE, wmsVersion);
        }
        return new GetCapabilities(wmsVersion);
    }

    /**
     * Converts a GetFeatureInfo request composed of string values, to a container of real java objects.
     *
     * @return A GetFeatureInfo request.
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private GetFeatureInfo adaptGetFeatureInfo() throws WebServiceException, NumberFormatException {
        final GetMap getMap  = adaptGetMap();
        final String version = getParameter(KEY_VERSION, true);
        final WMSQueryVersion wmsVersion = (version.equals(WMSQueryVersion.WMS_1_1_1.toString())) ?
                    WMSQueryVersion.WMS_1_1_1 : WMSQueryVersion.WMS_1_3_0;
        final String strX    = getParameter((version.equals(WMSQueryVersion.WMS_1_1_1.toString())) ?
                                                            KEY_I_v110 : KEY_I_v130, true);
        final String strY    = getParameter((version.equals(WMSQueryVersion.WMS_1_1_1.toString())) ?
                                                            KEY_J_v110 : KEY_J_v130, true);
        final String strQueryLayers = getParameter(KEY_QUERY_LAYERS, true);
        final String infoFormat  = getParameter(KEY_INFO_FORMAT, true);
        final String strFeatureCount = getParameter(KEY_FEATURE_COUNT, false);
        final List<String> queryLayers = QueryAdapter.toStringList(strQueryLayers);
        final List<String> queryableLayers = QueryAdapter.areQueryableLayers(queryLayers, wmsVersion);
        final int x = QueryAdapter.toInt(strX);
        final int y = QueryAdapter.toInt(strY);
        final int featureCount = QueryAdapter.toFeatureCount(strFeatureCount);
        return new GetFeatureInfo(getMap, x, y, queryableLayers, infoFormat, featureCount);
    }

    /**
     * Converts a GetLegendGraphic request composed of string values, to a container of real java objects.
     *
     * @return The GetLegendGraphic request.
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private GetLegendGraphic adaptGetLegendGraphic() throws WebServiceException {
        final String strLayer  = getParameter( KEY_LAYER,  true );
        final String strFormat = getParameter( KEY_FORMAT, true );
        final String strWidth  = getParameter( KEY_WIDTH, false );
        final String strHeight = getParameter( KEY_HEIGHT, false);
        final String format;
        try {
            format = QueryAdapter.toFormat(strFormat);
        } catch (IllegalArgumentException i) {
            throw new WMSWebServiceException(i, INVALID_FORMAT, WMSQueryVersion.WMS_GETLEGENDGRAPHIC_1_1_0);
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
                throw new WMSWebServiceException(n, INVALID_PARAMETER_VALUE, WMSQueryVersion.WMS_GETLEGENDGRAPHIC_1_1_0);
            }
            return new GetLegendGraphic(strLayer, format, width, height);
        }
    }

    /**
     * Converts a GetMap request composed of string values, to a container of real java objects.
     *
     * @return The GetMap request.
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private GetMap adaptGetMap() throws WebServiceException {
        final String version         = getParameter(KEY_VERSION,         true);
        final WMSQueryVersion wmsVersion = (version.equals(WMSQueryVersion.WMS_1_1_1.toString())) ?
                    WMSQueryVersion.WMS_1_1_1 : WMSQueryVersion.WMS_1_3_0;
        final String strFormat       = getParameter(KEY_FORMAT,          true);
        final String strCRS          = getParameter((version.equals(WMSQueryVersion.WMS_1_3_0.toString())) ?
                                                     KEY_CRS_v130 : KEY_CRS_v110, true );
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
        final String strSLD          = getParameter(KEY_SLD,            false);
        final String strStyles       = getParameter(KEY_STYLES, 
                ((strSLD != null) && (wmsVersion.equals(WMSQueryVersion.WMS_1_1_1))) ? false : true);

        final CoordinateReferenceSystem crs;
        try {
            crs = QueryAdapter.toCRS(strCRS);
        } catch (FactoryException ex) {
            throw new WMSWebServiceException(ex, INVALID_CRS, wmsVersion);
        }
        final ImmutableEnvelope env;
        try {
            env = (ImmutableEnvelope) QueryAdapter.toEnvelope(strBBox, crs);
        } catch (IllegalArgumentException i) {
            throw new WMSWebServiceException(i, INVALID_PARAMETER_VALUE, wmsVersion);
        }
        final String format;
        try {
            format = QueryAdapter.toFormat(strFormat);
        } catch (IllegalArgumentException i) {
            throw new WMSWebServiceException(i, INVALID_FORMAT, wmsVersion);
        }
        final List<String> layers  = QueryAdapter.toStringList(strLayers);
        final List<String> styles = QueryAdapter.toStringList(strStyles);
        MutableStyledLayerDescriptor sld = null;
        final Double elevation;
        try {
            elevation = (strElevation != null) ? QueryAdapter.toDouble(strElevation) : null;
        } catch (NumberFormatException n) {
            throw new WMSWebServiceException(n, INVALID_PARAMETER_VALUE, wmsVersion);
        }
        final MeasurementRange dimRange = QueryAdapter.toMeasurementRange(strDimRange);
        final Date date;
        try {
            date = QueryAdapter.toDate(strTime);
        } catch (ParseException ex) {
            throw new WMSWebServiceException(ex, INVALID_PARAMETER_VALUE, wmsVersion);
        }
        final int width;
        final int height;
        try {
            width  = QueryAdapter.toInt(strWidth);
            height = QueryAdapter.toInt(strHeight);
        } catch (NumberFormatException n) {
            throw new WMSWebServiceException(n, INVALID_PARAMETER_VALUE, wmsVersion);
        }
        final Dimension size = new Dimension(width, height);
        final Color background = QueryAdapter.toColor(strBGColor);
        final boolean transparent = QueryAdapter.toBoolean(strTransparent);

        if (strRemoteOwsUrl != null) {
            InputStream in = null;
            try {
                in = new FileInputStream(new File(strRemoteOwsUrl));
            } catch (FileNotFoundException ex) {
                throw new WMSWebServiceException(ex, STYLE_NOT_DEFINED, wmsVersion);
            }
            final XMLUtilities sldparser = new XMLUtilities();
            try {
                sld = sldparser.readSLD(in, org.geotools.style.sld.Specification.StyledLayerDescriptor.V_1_0_0);
            } catch (JAXBException ex) {
                throw new WMSWebServiceException(ex, STYLE_NOT_DEFINED, wmsVersion);
            }
            if (sld == null) {
                try {
                    sld = sldparser.readSLD(in, org.geotools.style.sld.Specification.StyledLayerDescriptor.V_1_1_0);
                } catch (JAXBException ex) {
                    throw new WMSWebServiceException(ex, STYLE_NOT_DEFINED, wmsVersion);
                }
            }
        } else {
            sld = QueryAdapter.toSLD(strSLD);
        }

        // Builds the request.
        return new GetMap(env, wmsVersion, format, layers, styles, sld, elevation,
                    date, dimRange, size, background, transparent, strExceptions);
    }

    /**
     * Creates a temporary file which will be deleted at ending-time of the webservice.
     */
    private static File createTempFile(final String prefix, final String type) throws IOException {
        //TODO, I dont know if using a temp file is correct or if it should be
        //somewhere else.

        final String ending;
        if ("image/jpeg".equalsIgnoreCase(type)) {
            ending = ".jpeg";
        } else if ("image/gif".equalsIgnoreCase(type)) {
            ending = ".gif";
        } else {
            ending = ".png";
        }

        final File f = File.createTempFile(prefix, ending);
        f.deleteOnExit();

        return f;
    }

    /**
     * Write an {@linkplain BufferedImage image} into an output stream, using the mime
     * type specified.
     *
     * @param image The image to write into an output stream.
     * @param mime Mime-type of the output
     * @param output Output stream containing the image.
     * @throws java.io.IOException if a writing error occurs.
     */
    private static synchronized void writeImage(final BufferedImage image,
            final String mime, Object output) throws IOException
    {
        if(image == null) throw new NullPointerException("Image can not be null");
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(mime);
        while (writers.hasNext()) {
            final ImageWriter writer = writers.next();
            final ImageWriterSpi spi = writer.getOriginatingProvider();
            if (spi.canEncodeImage(image)) {
                ImageOutputStream stream = null;
                if (!isValidType(spi.getOutputTypes(), output)) {
                    stream = ImageIO.createImageOutputStream(output);
                    output = stream;
                }
                writer.setOutput(output);
                writer.write(image);
                writer.dispose();
                if (stream != null) {
                    stream.close();
                }
                return;
            }
        }
        throw new IOException("Unknowed image type");
    }
}
