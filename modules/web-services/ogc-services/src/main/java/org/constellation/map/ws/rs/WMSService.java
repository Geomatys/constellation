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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.logging.Level;
import javax.measure.unit.Unit;
import javax.naming.NamingException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

//Constellation dependencies
import org.constellation.catalog.CatalogException;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.Service;
import org.constellation.ws.ServiceExceptionReport;
import org.constellation.ws.ServiceExceptionType;
import org.constellation.ws.WebServiceException;
import org.constellation.ws.ServiceVersion;
import org.constellation.portrayal.CSTLPortrayalService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedLayerDP;
import org.constellation.query.QueryAdapter;
import org.constellation.query.wms.GetMap;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.query.wms.WMSQueryVersion;
import org.constellation.util.PeriodUtilities;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.wms.AbstractDCP;
import org.constellation.wms.AbstractDimension;
import org.constellation.wms.AbstractHTTP;
import org.constellation.wms.AbstractLayer;
import org.constellation.wms.AbstractRequest;
import org.constellation.wms.AbstractOperation;
import org.constellation.wms.AbstractProtocol;
import org.constellation.wms.v111.LatLonBoundingBox;
import org.constellation.wms.v130.OperationType;
import org.constellation.wms.v130.EXGeographicBoundingBox;
import org.constellation.ws.rs.OGCWebService;

// Geotools dependencies
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.display.exception.PortrayalException;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.ImmutableEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.sld.MutableStyledLayerDescriptor;
import org.geotools.style.sld.XMLUtilities;
import org.geotools.util.MeasurementRange;

//Geoapi dependencies
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
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
     * Build a new instance of the webService and initialise the JAXB marshaller.
     */
    public WMSService() throws JAXBException, SQLException, IOException, NamingException {
        super("WMS", new ServiceVersion(Service.WMS, WMSQueryVersion.WMS_1_3_0.toString()),
                     new ServiceVersion(Service.WMS, WMSQueryVersion.WMS_1_1_1.toString()));

        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext("org.constellation.ws:org.constellation.wms.v111:" +
                "org.constellation.wms.v130:org.constellation.sld.v110:org.constellation.gml.v311",
                "http://www.opengis.net/wms");

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

            if (GETMAP.equalsIgnoreCase(request)) {
                return getMap(adaptGetMap(true));
            }
            if (GETFEATUREINFO.equalsIgnoreCase(request)) {
                return getFeatureInfo(adaptGetFeatureInfo());
            }
            if (GETCAPABILITIES.equalsIgnoreCase(request)) {
                return getCapabilities(adaptGetCapabilities());
            }
            if (GETLEGENDGRAPHIC.equalsIgnoreCase(request)) {
                return getLegendGraphic(adaptGetLegendGraphic());
            }
            final String version = (String) getParameter(KEY_VERSION, false);
            final WMSQueryVersion queryVersion;
            if (version == null) {
                queryVersion = WMSQueryVersion.WMS_1_1_1;
            } else {
                queryVersion = (version.equalsIgnoreCase(WMSQueryVersion.WMS_1_1_1.toString())) ?
                    WMSQueryVersion.WMS_1_1_1 : WMSQueryVersion.WMS_1_3_0;
            }
            throw new WebServiceException("The operation " + request +
                    " is not supported by the service", OPERATION_NOT_SUPPORTED, queryVersion, "request");

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
            return Response.ok(cleanSpecialCharacter(sw.toString()), APP_XML).build();
        } catch (NumberFormatException n) {
            final ServiceExceptionReport report = new ServiceExceptionReport(getCurrentVersion(),
                    new ServiceExceptionType(n.getMessage(), INVALID_PARAMETER_VALUE));
            LOGGER.log(Level.INFO, n.getLocalizedMessage(), n);
            StringWriter sw = new StringWriter();
            marshaller.marshal(report, sw);
            return Response.ok(cleanSpecialCharacter(sw.toString()), APP_XML).build();
        }
    }

    /**
     * Describe the capabilities and the layers available of this service.
     *
     * @param getCapab The {@linkplain GetCapabilities get capabilities} request.
     * @return a WMSCapabilities XML document describing the capabilities of the service.
     *
     * @throws WebServiceException
     * @throws JAXBException when unmarshalling the default GetCapabilities file.
     */
    private Response getCapabilities(final GetCapabilities getCapab) throws WebServiceException,
                                                                  JAXBException
    {
        final WMSQueryVersion queryVersion = getCapab.getVersion();
        String format = getParameter(KEY_FORMAT, false);
        if (format == null || !(format.equalsIgnoreCase(TEXT_XML) ||
                format.equalsIgnoreCase(APP_WMS_XML) || format.equalsIgnoreCase(APP_XML)))
        {
            format = TEXT_XML;
        }

        final AbstractWMSCapabilities response;
        // String updateSequence = getParameter("UPDATESEQUENCE", false);

        // the service shall return WMSCapabilities marshalled
        try {
            response = (AbstractWMSCapabilities)getCapabilitiesObject(queryVersion);
        } catch(IOException e)   {
            throw new WebServiceException("IO exception while getting Services Metadata:" +
                    e.getMessage(), INVALID_PARAMETER_VALUE, queryVersion);
        }

        //we build the list of accepted crs
        final List<String> crs = new ArrayList<String>();
        crs.add("EPSG:4326");     crs.add("CRS:84");  crs.add("EPSG:3395");
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
                throw new WebServiceException(exception, NO_APPLICABLE_CODE, queryVersion);
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
                    new org.constellation.wms.v111.Dimension("dim_range", unit, defaut,
                                                           minRange + "," + maxRange) :
                    new org.constellation.wms.v130.Dimension("dim_range", unit, defaut,
                                                           minRange + "," + maxRange);
                dimensions.add(dim);
            }

            // LegendUrl generation
            final String layerName = layer.getName();
            final String beginLegendUrl = getServiceURL() + "wms?REQUEST=GetLegendGraphic&" +
                    "VERSION=1.1.0&FORMAT=";
            final String legendUrlGif = beginLegendUrl + IMAGE_GIF + "&LAYER=" + layerName;
            final String legendUrlPng = beginLegendUrl + IMAGE_PNG + "&LAYER=" + layerName;
            final int queryable = (layer.isQueryable(Service.GETINFO) == true) ? 1 : 0;
            final AbstractLayer outputLayer;
            if (queryVersion.equals(WMSQueryVersion.WMS_1_1_1)) {
                /*
                 * TODO
                 * Envelope inputBox = inputLayer.getCoverage().getEnvelope();
                 */
                final org.constellation.wms.v111.BoundingBox outputBBox = (inputGeoBox != null) ?
                    new org.constellation.wms.v111.BoundingBox("EPSG:4326",
                            inputGeoBox.getWestBoundLongitude(),
                            inputGeoBox.getSouthBoundLatitude(), inputGeoBox.getEastBoundLongitude(),
                            inputGeoBox.getNorthBoundLatitude(), 0.0, 0.0, queryVersion.toString()) :
                    null;

                // we build The Style part
                org.constellation.wms.v111.OnlineResource or =
                        new org.constellation.wms.v111.OnlineResource(legendUrlPng);
                org.constellation.wms.v111.LegendURL legendURL1 =
                        new org.constellation.wms.v111.LegendURL(IMAGE_PNG, or);

                or = new org.constellation.wms.v111.OnlineResource(legendUrlGif);
                org.constellation.wms.v111.LegendURL legendURL2 =
                        new org.constellation.wms.v111.LegendURL(IMAGE_GIF, or);

                List<String> stylesName = layer.getFavoriteStyles();
                List<org.constellation.wms.v111.Style> styles = new ArrayList<org.constellation.wms.v111.Style>();
                if (stylesName != null && stylesName.size() != 0) {
                    for (String styleName : stylesName) {
                        org.constellation.wms.v111.Style style = new org.constellation.wms.v111.Style(
                                styleName, styleName, null, null, null, legendURL1, legendURL2);
                        styles.add(style);
                    }
                } else {
                    org.constellation.wms.v111.Style style = new org.constellation.wms.v111.Style(
                                "Style1", "defaultStyle", null, null, null, legendURL1, legendURL2);
                    styles.add(style);
                }
                
                //we build the complete layer object
                outputLayer = new org.constellation.wms.v111.Layer(layerName,
                        cleanSpecialCharacter(layer.getRemarks()),
                        cleanSpecialCharacter(layer.getThematic()), crs,
                        new LatLonBoundingBox(inputGeoBox.getWestBoundLongitude(),
                                              inputGeoBox.getSouthBoundLatitude(),
                                              inputGeoBox.getEastBoundLongitude(),
                                              inputGeoBox.getNorthBoundLatitude()),
                        outputBBox, queryable, dimensions, styles);
            } else {
                /*
                 * TODO
                 * Envelope inputBox = inputLayer.getCoverage().getEnvelope();
                 */
                final org.constellation.wms.v130.BoundingBox outputBBox = (inputGeoBox != null) ?
                    new org.constellation.wms.v130.BoundingBox("EPSG:4326",
                            inputGeoBox.getWestBoundLongitude(),
                            inputGeoBox.getSouthBoundLatitude(),
                            inputGeoBox.getEastBoundLongitude(),
                            inputGeoBox.getNorthBoundLatitude(), 0.0, 0.0,
                            queryVersion.toString()) :
                    null;

                // we build a Style Object
                org.constellation.wms.v130.OnlineResource or =
                        new org.constellation.wms.v130.OnlineResource(legendUrlPng);
                org.constellation.wms.v130.LegendURL legendURL1 =
                        new org.constellation.wms.v130.LegendURL(IMAGE_PNG, or);

                or = new org.constellation.wms.v130.OnlineResource(legendUrlGif);
                org.constellation.wms.v130.LegendURL legendURL2 =
                        new org.constellation.wms.v130.LegendURL(IMAGE_GIF, or);
                
                List<String> stylesName = layer.getFavoriteStyles();
                List<org.constellation.wms.v130.Style> styles = new ArrayList<org.constellation.wms.v130.Style>();
                if (stylesName != null && stylesName.size() != 0) {
                    for (String styleName : stylesName) {
                        org.constellation.wms.v130.Style style = new org.constellation.wms.v130.Style(
                        styleName, styleName, null, null, null, legendURL1, legendURL2);
                        styles.add(style);
                    }
                } else {
                    org.constellation.wms.v130.Style style = new org.constellation.wms.v130.Style(
                        "Style1", "default Style", null, null, null, legendURL1, legendURL2);
                    styles.add(style);
                }

                outputLayer = new org.constellation.wms.v130.Layer(layerName,
                        cleanSpecialCharacter(layer.getRemarks()),
                        cleanSpecialCharacter(layer.getThematic()), crs,
                        new EXGeographicBoundingBox(inputGeoBox.getWestBoundLongitude(),
                                                    inputGeoBox.getSouthBoundLatitude(),
                                                    inputGeoBox.getEastBoundLongitude(),
                                                    inputGeoBox.getNorthBoundLatitude()),
                        outputBBox, queryable, dimensions, styles);
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
        marshaller.setProperty("com.sun.xml.bind.xmlHeaders",
                (queryVersion.equals(WMSQueryVersion.WMS_1_1_1)) ?
            "<!DOCTYPE WMT_MS_Capabilities SYSTEM \"http://schemas.opengis.net/wms/1.1.1/" +
            "WMS_MS_Capabilities.dtd\">\n" : "");
        marshaller.marshal(response, sw);

        return Response.ok(sw.toString(), format).build();
    }

    /**
     * Return the value of a point in a map.
     *
     * @param gfi The {@linkplain GetFeatureInfo get feature info} request.
     * @return text, HTML , XML or GML code.
     *
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private Response getFeatureInfo(final GetFeatureInfo gfi)
                          throws WebServiceException, JAXBException
    {
        final WMSQueryVersion queryVersion = gfi.getVersion();

        String infoFormat = gfi.getInfoFormat();
        if (infoFormat != null) {
            if(!(infoFormat.equalsIgnoreCase(TEXT_PLAIN) || infoFormat.equalsIgnoreCase(TEXT_HTML) ||
                 infoFormat.equalsIgnoreCase(APP_GML) || infoFormat.equalsIgnoreCase(TEXT_XML) ||
                 infoFormat.equalsIgnoreCase(APP_XML) || infoFormat.equalsIgnoreCase(XML) ||
                 infoFormat.equalsIgnoreCase(GML)))
            {
                throw new WebServiceException("This MIME type " + infoFormat +
                        " is not accepted by the service", INVALID_PARAMETER_VALUE, queryVersion, "info_format");
            }
        } else {
            infoFormat = TEXT_PLAIN;
        }
        final NamedLayerDP dp = NamedLayerDP.getInstance();
        final List<String> layers = gfi.getQueryLayers();
        final int size = layers.size();
        /* Now proceed to the calculation of the values, and use the toString method to store them.
         * This map will store couples of <layerName, List<values>> obtained by the getInformationAt() method.
         */

        final Map<String, List<String>> results;
        try {
            results = CSTLPortrayalService.getInstance().hit(gfi);
        } catch (PortrayalException ex) {
            throw new WebServiceException(ex, NO_APPLICABLE_CODE, queryVersion);
        }


        // We now build the response, according to the format chosen.
        final StringBuilder response = new StringBuilder();
        // TEXT / PLAIN
        if (infoFormat.equalsIgnoreCase(TEXT_PLAIN)) {
            for (String layer : layers) {
                final List<String> values = results.get(layer);
                response.append((values.size() < 2) ? "Result for " : "Results for ")
                        .append(layer);
                response.append((values.size() < 2) ? " is :" : " are : ");
                for (String value : values) {
                    response.append(value).append("\n");
                }
            }
            return Response.ok(response.toString(), infoFormat).build();
        }

        // TEXT / HTML
        if (infoFormat.equalsIgnoreCase(TEXT_HTML)) {
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
        if (infoFormat.equalsIgnoreCase(APP_GML) || infoFormat.equalsIgnoreCase(TEXT_XML) ||
                infoFormat.equalsIgnoreCase(APP_XML) || infoFormat.equalsIgnoreCase(XML) ||
                infoFormat.equalsIgnoreCase(GML))
        {
            final StringBuilder builder = new StringBuilder();
            builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n")
                   .append("<msGMLOutput xmlns:gml=\"http://www.opengis.net/gml\" ")
                   .append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" ")
                   .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
                   .append("\n");
            final Envelope objEnv = gfi.getEnvelope();
            final Date time = gfi.getTime();
            final Double elevation = gfi.getElevation();
            for (String layer : layers) {
                final String layerNameCorrected = layer.replaceAll("\\W", "");
                builder.append("\t<").append(layerNameCorrected).append("_layer").append(">\n")
                       .append("\t\t<").append(layerNameCorrected).append("_feature").append(">\n");
                       
                final LayerDetails layerPostgrid = dp.get(layer);
                final CoordinateReferenceSystem crs = objEnv.getCoordinateReferenceSystem();
                builder.append("\t\t\t<gml:boundedBy>").append("\n");
                String crsName;
                try {
                    crsName = CRS.lookupIdentifier(Citations.EPSG, crs, true);
                    if (!crsName.startsWith("EPSG:")) {
                        crsName = "ESPG:" + crsName;
                    }
                } catch (FactoryException ex) {
                    crsName = crs.getName().getCode();
                }
                builder.append("\t\t\t\t<gml:Box srsName=\"").append(crsName).append("\">\n");
                builder.append("\t\t\t\t\t<gml:coordinates>");
                final GeneralDirectPosition pos = layerPostgrid.getPixelCoordinates(gfi);
                builder.append(pos.getOrdinate(0)).append(",").append(pos.getOrdinate(1)).append(" ")
                       .append(pos.getOrdinate(0)).append(",").append(pos.getOrdinate(1));
                builder.append("</gml:coordinates>").append("\n");
                builder.append("\t\t\t\t</gml:Box>").append("\n");
                builder.append("\t\t\t</gml:boundedBy>").append("\n");
                builder.append("\t\t\t<x>").append(pos.getOrdinate(0)).append("</x>").append("\n")
                       .append("\t\t\t<y>").append(pos.getOrdinate(1)).append("</y>").append("\n");
                if (time != null) {
                    builder.append("\t\t\t<time>").append(time).append("</time>")
                           .append("\n");
                } else {
                    SortedSet<Date> dates = null;
                    try {
                        dates = layerPostgrid.getAvailableTimes();
                    } catch (CatalogException ex) {
                        dates = null;
                    }
                    if (dates != null && !(dates.isEmpty())) {
                        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        df.setTimeZone(TimeZone.getTimeZone("UTC"));
                        builder.append("\t\t\t<time>").append(df.format(dates.last()))
                               .append("</time>").append("\n");
                    }
                }
                if (elevation != null) {
                    builder.append("\t\t\t<elevation>").append(elevation)
                           .append("</elevation>").append("\n");
                } else {
                    SortedSet<Number> elevs = null;
                    try {
                        elevs = layerPostgrid.getAvailableElevations();
                    } catch (CatalogException ex) {
                        elevs = null;
                    }
                    if (elevs != null && !(elevs.isEmpty())) {
                        builder.append("\t\t\t<elevation>").append(elevs.first().toString())
                               .append("</elevation>").append("\n");
                    }
                }
                final GridCoverage2D coverage;
                try {
                    coverage = layerPostgrid.getCoverage(objEnv, new Dimension(gfi.getSize()), elevation, time);
                } catch (CatalogException cat) {
                    throw new WebServiceException(cat, NO_APPLICABLE_CODE, queryVersion);
                } catch (IOException io) {
                    throw new WebServiceException(io, NO_APPLICABLE_CODE, queryVersion);
                }
                if (coverage != null) {
                    builder.append("\t\t\t<variable>")
                           .append(coverage.getSampleDimension(0).getDescription())
                           .append("</variable>").append("\n");
                }
                final MeasurementRange[] ranges = layerPostgrid.getSampleValueRanges();
                if (ranges != null && ranges.length > 0 && !ranges[0].toString().equals("")) {
                    builder.append("\t\t\t<unit>").append(ranges[0].getUnits().toString())
                           .append("</unit>").append("\n");
                }
                builder.append("\t\t\t<value>").append(results.get(layer).get(0))
                       .append("</value>").append("\n")
                       .append("\t\t</").append(layerNameCorrected).append("_feature").append(">\n")
                       .append("\t</").append(layerNameCorrected).append("_layer").append(">\n");
            }
            builder.append("</msGMLOutput>");
            return Response.ok(builder.toString(), APP_GML).build();
        }

        // Info format not handled.
        throw new WebServiceException("Unsupported info format chosen",
                INVALID_FORMAT, queryVersion, "info_format");
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
    private Response getLegendGraphic(final GetLegendGraphic getLegend) throws WebServiceException,
                                                                            JAXBException
    {
        final WMSQueryVersion version = getLegend.getVersion();
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
        final WMSQueryVersion queryVersion = getMap.getVersion();
        final String errorType = getMap.getExceptionFormat();
        final boolean errorInImage = EXCEPTIONS_INIMAGE.equalsIgnoreCase(errorType);
        final String format = getMap.getFormat();
        
        BufferedImage image = null;
        try {
            image = CSTLPortrayalService.getInstance().portray(getMap);
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
    private void updateExtendedOperationURL(final AbstractRequest request,
                                            final WMSQueryVersion version)
    {

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
     * Converts a GetCapabilities request composed of string values, to a container
     * of real java objects.
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
        final WMSQueryVersion wmsVersion = (bestVersion.toString().equals(
                    WMSQueryVersion.WMS_1_1_1.toString())) ?
                    WMSQueryVersion.WMS_1_1_1 : WMSQueryVersion.WMS_1_3_0;
        final String service = getParameter(KEY_SERVICE, true);
        if (!Service.WMS.toString().equalsIgnoreCase(service)) {
            throw new WebServiceException("Invalid service specified. Should be WMS.",
                    INVALID_PARAMETER_VALUE, wmsVersion, "service");
        }
        return new GetCapabilities(wmsVersion);
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
                    WMSQueryVersion.WMS_GETLEGENDGRAPHIC_1_1_0);
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
                        WMSQueryVersion.WMS_GETLEGENDGRAPHIC_1_1_0);
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
        final WMSQueryVersion wmsVersion = (version.equals(WMSQueryVersion.WMS_1_1_1.toString())) ?
                    WMSQueryVersion.WMS_1_1_1 : WMSQueryVersion.WMS_1_3_0;
        final String strFormat       = getParameter(KEY_FORMAT,    fromGetMap);
        final String strCRS          = getParameter((version.equals(
                WMSQueryVersion.WMS_1_3_0.toString())) ? KEY_CRS_v130 : KEY_CRS_v110, true);
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
                && (wmsVersion.equals(WMSQueryVersion.WMS_1_1_1))) ? false : fromGetMap);

        final CoordinateReferenceSystem crs;
        try {
            crs = QueryAdapter.toCRS(strCRS);
        } catch (FactoryException ex) {
            throw new WebServiceException(ex, INVALID_CRS, wmsVersion);
        }
        final ImmutableEnvelope env;
        try {
            env = (ImmutableEnvelope) QueryAdapter.toEnvelope(strBBox, crs);
            //TODO change to this method when renderer will support 4D BBox
//            env = QueryAdapter.toEnvelope(strBBox, crs, strElevation, strTime,wmsVersion);
        } catch (IllegalArgumentException i) {
            throw new WebServiceException(i, INVALID_PARAMETER_VALUE, wmsVersion);
        }
        final String format;
        try {
            format = QueryAdapter.toFormat(strFormat);
        } catch (IllegalArgumentException i) {
            throw new WebServiceException(i, INVALID_FORMAT, wmsVersion);
        }
        final List<String> layers  = QueryAdapter.toStringList(strLayers);
        final List<String> styles = QueryAdapter.toStringList(strStyles);
        MutableStyledLayerDescriptor sld = null;
        final Double elevation;
        try {
            elevation = (strElevation != null) ? QueryAdapter.toDouble(strElevation) : null;
        } catch (NumberFormatException n) {
            throw new WebServiceException(n, INVALID_PARAMETER_VALUE, wmsVersion);
        }
        final MeasurementRange dimRange = QueryAdapter.toMeasurementRange(strDimRange);
        final Date date;
        try {
            date = QueryAdapter.toDate(strTime);
        } catch (ParseException ex) {
            throw new WebServiceException(ex, INVALID_PARAMETER_VALUE, wmsVersion);
        }
        final int width;
        final int height;
        try {
            width  = QueryAdapter.toInt(strWidth);
            height = QueryAdapter.toInt(strHeight);
        } catch (NumberFormatException n) {
            throw new WebServiceException(n, INVALID_PARAMETER_VALUE, wmsVersion);
        }
        final Dimension size = new Dimension(width, height);
        final Color background = QueryAdapter.toColor(strBGColor);
        final boolean transparent = QueryAdapter.toBoolean(strTransparent);

        if (strRemoteOwsUrl != null) {
            InputStream in = null;
            try {
                in = new FileInputStream(new File(strRemoteOwsUrl));
            } catch (FileNotFoundException ex) {
                throw new WebServiceException(ex, STYLE_NOT_DEFINED, wmsVersion);
            }
            final XMLUtilities sldparser = new XMLUtilities();
            try {
                sld = sldparser.readSLD(in,
                        org.geotools.style.sld.Specification.StyledLayerDescriptor.V_1_0_0);
            } catch (JAXBException ex) {
                throw new WebServiceException(ex, STYLE_NOT_DEFINED, wmsVersion);
            }
            if (sld == null) {
                try {
                    sld = sldparser.readSLD(in,
                            org.geotools.style.sld.Specification.StyledLayerDescriptor.V_1_1_0);
                } catch (JAXBException ex) {
                    throw new WebServiceException(ex, STYLE_NOT_DEFINED, wmsVersion);
                }
            }
        } else {
            try {
                sld = QueryAdapter.toSLD(urlSLD);
            } catch (MalformedURLException ex) {
                throw new WebServiceException(ex, STYLE_NOT_DEFINED, wmsVersion);
            }
        }

        double azimuth = 0;
        if(strAzimuth != null){
            try{
                azimuth = QueryAdapter.toDouble(strAzimuth);
            }catch(NumberFormatException ex){
                throw new WebServiceException(ex, INVALID_PARAMETER_VALUE, wmsVersion);
            }
        }

        // Builds the request.
        return new GetMap(env, wmsVersion, format, layers, styles, sld, elevation,
                    date, dimRange, size, background, transparent, azimuth, strExceptions);
    }

    /**
     * Creates a temporary file which will be deleted at ending-time of the webservice.
     */
    private static File createTempFile(final String prefix, final String type) throws IOException {
        //TODO, I dont know if using a temp file is correct or if it should be
        //somewhere else.

        final String ending;
        if (IMAGE_JPEG.equalsIgnoreCase(type)) {
            ending = ".jpeg";
        } else if (IMAGE_GIF.equalsIgnoreCase(type)) {
            ending = ".gif";
        } else {
            ending = ".png";
        }

        final File f = File.createTempFile(prefix, ending);
        f.deleteOnExit();

        return f;
    }

    /**
     * TODO.
     */
    @Override
    protected void destroy() {
        LOGGER.info("destroying WMS service");
    }
}
