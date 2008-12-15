/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.map.ws;

//J2SE dependencies
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

//Constellation dependencies
import org.constellation.catalog.CatalogException;
import org.constellation.map.ws.rs.CSVGraphicVisitor;
import org.constellation.map.ws.rs.GMLGraphicVisitor;
import org.constellation.map.ws.rs.HTMLGraphicVisitor;
import org.constellation.map.ws.rs.WMSPortrayalAdapter;
import org.constellation.portrayal.AbstractGraphicVisitor;
import org.constellation.portrayal.CSTLPortrayalService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedLayerDP;
import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.query.wms.GetMap;
import org.constellation.util.PeriodUtilities;
import org.constellation.util.Utils;
import org.constellation.wms.AbstractDCP;
import org.constellation.wms.AbstractDimension;
import org.constellation.wms.AbstractHTTP;
import org.constellation.wms.AbstractLayer;
import org.constellation.wms.AbstractOperation;
import org.constellation.wms.AbstractProtocol;
import org.constellation.wms.AbstractRequest;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.wms.v111.LatLonBoundingBox;
import org.constellation.wms.v130.EXGeographicBoundingBox;
import org.constellation.wms.v130.OperationType;
import org.constellation.ws.ServiceType;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.WebServiceException;
import org.constellation.ws.rs.WebService;

//Geotools dependencies
import org.geotools.display.exception.PortrayalException;
import org.geotools.internal.jaxb.v110.se.OnlineResourceType;
import org.geotools.internal.jaxb.v110.sld.DescribeLayerResponseType;
import org.geotools.internal.jaxb.v110.sld.LayerDescriptionType;
import org.geotools.internal.jaxb.v110.sld.TypeNameType;
import org.geotools.util.MeasurementRange;
import org.geotools.util.Version;

//Geoapi dependencies
import org.opengis.metadata.extent.GeographicBoundingBox;

import static org.constellation.ws.ExceptionCode.*;
import static org.constellation.query.wms.WMSQuery.*;


/**
 * A WMS worker for a local WMS service which handles requests from either REST 
 * or SOAP facades and issues appropriate responses.
 * <p>
 * The classes implementing the REST or SOAP facades to this service will have 
 * processed the requests sufficiently to ensure that all the information 
 * conveyed by the HTTP request is either in the method call parameters or is 
 * in one of the fields of the parent class which holds instances of the 
 * injectible interface {@code Context} objects created by the JEE container.
 * </p>
 *
 * @version $Id$
 * 
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public class WMSWorker extends AbstractWMSWorker {
	
    /**
     * The default debugging logger for the WMS service.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.map.ws");

    /**
     * A map containing the Capabilities Object already loaded from file.
     */
    private Map<String,Object> capabilities = new HashMap<String,Object>();

    /**
     * The web service marshaller, which will use the web service name space.
     */
    @SuppressWarnings("unused")
	private final Marshaller marshaller;

    /**
     * The web service unmarshaller, which will use the web service name space.
     */
    private final Unmarshaller unmarshaller;

    public WMSWorker(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        this.marshaller   = marshaller;
        this.unmarshaller = unmarshaller;
    }

    /**
     * Return a description of layers specified in the user's request.
     *
     * @param descLayer  The {@linkplain DescribeLayer describe layer} request.
     * @param url        The service url.
     * @param sldVersion The version of the sld specified.
     */
    @Override
    public DescribeLayerResponseType describeLayer(final DescribeLayer descLayer) throws WebServiceException {
        final OnlineResourceType or = new OnlineResourceType();
        or.setHref(uriContext.getBaseUri().toString() + "wcs?");

        final List<LayerDescriptionType> layerDescriptions = new ArrayList<LayerDescriptionType>();
        final List<String> layers = descLayer.getLayers();
        for (String layer : layers) {
            final TypeNameType t = new TypeNameType(layer.trim());
            final LayerDescriptionType outputLayer = new LayerDescriptionType(or, t);
            layerDescriptions.add(outputLayer);
        }
        return new DescribeLayerResponseType("1.1.0", layerDescriptions);
    }

    /**
     * Describe the capabilities and the layers available of this service.
     *
     * @param getCapab       The {@linkplain GetCapabilities get capabilities} request.
     * @return a WMSCapabilities XML document describing the capabilities of the service.
     *
     * @throws JAXBException
     * @throws WebServiceException
     */
    @Override
    public AbstractWMSCapabilities getCapabilities(final GetCapabilities getCapab) throws WebServiceException {
        final ServiceVersion queryVersion = getCapab.getVersion();

        //we build the list of accepted crs
        final List<String> crs = new ArrayList<String>();
        crs.add("EPSG:4326");     crs.add("CRS:84");  crs.add("EPSG:3395");
        crs.add("EPSG:27571"); crs.add("EPSG:27572"); crs.add("EPSG:27573"); crs.add("EPSG:27574");
        //we update the url in the static part.
        final AbstractWMSCapabilities inCapabilities;
        try {
            inCapabilities = (AbstractWMSCapabilities) getCapabilitiesObject(getCapab.getVersion(),
                    servletContext.getRealPath("WEB-INF"));
        } catch (IOException e) {
            throw new WebServiceException("IO exception while getting Services Metadata:" +
                    e.getMessage(), INVALID_PARAMETER_VALUE, getCapab.getVersion());
        } catch (JAXBException ex) {
            throw new WebServiceException("IO exception while getting Services Metadata:" +
                    ex.getMessage(), INVALID_PARAMETER_VALUE, getCapab.getVersion());
        }
        final String url = uriContext.getBaseUri().toString();
        inCapabilities.getService().getOnlineResource().setHref(url + "wms");
        final AbstractRequest request = inCapabilities.getCapability().getRequest();

        updateURL(request.getGetCapabilities().getDCPType(), url);
        updateURL(request.getGetFeatureInfo().getDCPType(), url);
        updateURL(request.getGetMap().getDCPType(), url);
        updateExtendedOperationURL(request, queryVersion, url);

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
            if (!layer.isQueryable(ServiceType.WMS)) {
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
                LOGGER.log(Level.INFO, "Error retrieving dates values for the layer :"+ key, ex);
                dates = null;
            }
            if (dates != null && !(dates.isEmpty())) {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                final PeriodUtilities periodFormatter = new PeriodUtilities(df);
                defaut = df.format(dates.last());
                dim = (queryVersion.toString().equals("1.1.1")) ?
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
                LOGGER.log(Level.INFO, "Error retrieving elevation values for the layer :"+ key, ex);
                elevations = null;
            }
            if (elevations != null && !(elevations.isEmpty())) {
                defaut = elevations.first().toString();
                dim = (queryVersion.toString().equals("1.1.1")) ?
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
                dim = (queryVersion.toString().equals("1.1.1")) ?
                    new org.constellation.wms.v111.Dimension("dim_range", unit, defaut,
                                                           minRange + "," + maxRange) :
                    new org.constellation.wms.v130.Dimension("dim_range", unit, defaut,
                                                           minRange + "," + maxRange);
                dimensions.add(dim);
            }

            // LegendUrl generation
            final String layerName = layer.getName();
            final String beginLegendUrl = url + "wms?REQUEST=GetLegendGraphic&" +
                    "VERSION=1.1.0&FORMAT=";
            final String legendUrlGif = beginLegendUrl + IMAGE_GIF + "&LAYER=" + layerName;
            final String legendUrlPng = beginLegendUrl + IMAGE_PNG + "&LAYER=" + layerName;
            final int queryable = (layer.isQueryable(ServiceType.GETINFO) == true) ? 1 : 0;
            final AbstractLayer outputLayer;
            if (queryVersion.toString().equals("1.1.1")) {
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
                        Utils.cleanSpecialCharacter(layer.getRemarks()),
                        Utils.cleanSpecialCharacter(layer.getThematic()), crs,
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
                        Utils.cleanSpecialCharacter(layer.getRemarks()),
                        Utils.cleanSpecialCharacter(layer.getThematic()), crs,
                        new EXGeographicBoundingBox(inputGeoBox.getWestBoundLongitude(),
                                                    inputGeoBox.getSouthBoundLatitude(),
                                                    inputGeoBox.getEastBoundLongitude(),
                                                    inputGeoBox.getNorthBoundLatitude()),
                        outputBBox, queryable, dimensions, styles);
            }
            layers.add(outputLayer);
        }

        //we build the general layer and add it to the document
        final AbstractLayer mainLayer = (queryVersion.toString().equals("1.1.1")) ?
            new org.constellation.wms.v111.Layer("Constellation Web Map Layer",
                    "description of the service(need to be fill)", crs, null, layers) :
            new org.constellation.wms.v130.Layer("Constellation Web Map Layer",
                    "description of the service(need to be fill)", crs, null, layers);

        inCapabilities.getCapability().setLayer(mainLayer);
        return inCapabilities;
    }

    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @return The capabilities Object, or {@code null} if none.
     */
    private Object getCapabilitiesObject(final Version version, final String home) throws JAXBException, IOException {
        final String fileName = "WMSCapabilities" + version.toString() + ".xml";
        final File changeFile = getFile("change.properties", home);
        Properties p = new Properties();

        // if the flag file is present we load the properties
        if (changeFile != null && changeFile.exists()) {
            FileInputStream in = new FileInputStream(changeFile);
            p.load(in);
            in.close();
        } else {
            p.put("update", "false");
        }

        //Look if the template capabilities is already in cache.
        Object response = capabilities.get(fileName);
        boolean update = p.getProperty("update").equals("true");

        if (response == null || update) {
            if (update) {
                LOGGER.info("updating metadata");
            }

            File f = getFile(fileName, home);
            response = unmarshaller.unmarshal(f);
            capabilities.put(fileName, response);
            //this.setLastUpdateSequence(System.currentTimeMillis());
            p.put("update", "false");

            // if the flag file is present we store the properties
            if (changeFile != null && changeFile.exists()) {
                FileOutputStream out = new FileOutputStream(changeFile);
                p.store(out, "updated from WebService");
                out.close();
            }
        }

        return response;
    }

    /**
     * Return the value of a point in a map.
     *
     * @param gfi The {@linkplain GetFeatureInfo get feature info} request.
     * @return text, HTML , XML or GML code.
     *
     * @throws org.constellation.coverage.web.WebServiceException
     */
    @Override
    public synchronized String getFeatureInfo(final GetFeatureInfo gfi) throws WebServiceException {

        String infoFormat = gfi.getInfoFormat();
        if (infoFormat == null) {
            //Should not happen since the info format parameter is mandatory for the GetFeatureInfo request.
            infoFormat = TEXT_PLAIN;
        }

        final AbstractGraphicVisitor visitor;

        if (infoFormat.equalsIgnoreCase(TEXT_PLAIN)) {
            // TEXT / PLAIN
            visitor = new CSVGraphicVisitor();
        } else if (infoFormat.equalsIgnoreCase(TEXT_HTML)) {
            // TEXT / HTML
            visitor = new HTMLGraphicVisitor();
        } else if (infoFormat.equalsIgnoreCase(APP_GML) || infoFormat.equalsIgnoreCase(TEXT_XML) ||
                   infoFormat.equalsIgnoreCase(APP_XML) || infoFormat.equalsIgnoreCase(XML) ||
                   infoFormat.equalsIgnoreCase(GML))
        {
            // GML
            visitor = new GMLGraphicVisitor(gfi);
        } else {
            throw new WebServiceException("MIME type " + infoFormat + " is not accepted by the service.\n" +
                    "You have to choose between: "+ TEXT_PLAIN +", "+ TEXT_HTML +", "+ APP_GML +", "+ GML +
                    ", "+ APP_XML +", "+ XML+", "+ TEXT_XML,
                    INVALID_PARAMETER_VALUE, gfi.getVersion(), "info_format");
        }

        // We now build the response, according to the format chosen.
        try {
            WMSPortrayalAdapter.hit(gfi, visitor);
        } catch (PortrayalException ex) {
            throw new WebServiceException(ex, NO_APPLICABLE_CODE, gfi.getVersion());
        }

        return visitor.getResult();
    }

    /**
     * Return a file located in WEB-INF deployed directory.
     *
     * @param fileName The name of the file requested.
     * @return The specified file.
     */
    private File getFile(String fileName, String home) {
         File path;
         if (home == null || !(path = new File(home)).isDirectory()) {
            path = WebService.getSicadeDirectory();
         }
         if (fileName != null)
            return new File(path, fileName);
         else return path;
    }

    /**
     * Return the legend graphic for the current layer.
     *
     * @param getLegend The {@linkplain GetLegendGraphic get legend graphic} request.
     * @return a file containing the legend graphic image.
     *
     * @throws org.constellation.coverage.web.WebServiceException
     */
    @Override
    public BufferedImage getLegendGraphic(final GetLegendGraphic getLegend) throws WebServiceException {
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
        return layer.getLegendGraphic(dims);
    }

    /**
     * Return a map for the specified parameters in the query.
     *
     * @param getMap The {@linkplain GetMap get map} request.
     * @return The map requested, or an error.
     *
     * @throws WebServiceException
     */
    @Override
    public synchronized BufferedImage getMap(final GetMap getMap) throws WebServiceException {
        final ServiceVersion queryVersion = getMap.getVersion();
        final String errorType = getMap.getExceptionFormat();
        final boolean errorInImage = EXCEPTIONS_INIMAGE.equalsIgnoreCase(errorType);

        BufferedImage image = null;
        try {
            image = WMSPortrayalAdapter.portray(getMap);
        } catch (PortrayalException ex) {
            if (errorInImage) {
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

        return image;
    }

    /**
     * update The URL in capabilities document with the service actual URL.
     */
    private void updateURL(final List<? extends AbstractDCP> dcpList, final String url) {
        for(AbstractDCP dcp: dcpList) {
            final AbstractHTTP http = dcp.getHTTP();
            final AbstractProtocol getMethod = http.getGet();
            if (getMethod != null) {
                getMethod.getOnlineResource().setHref(url + "wms?SERVICE=WMS&");
            }
            final AbstractProtocol postMethod = http.getPost();
            if (postMethod != null) {
                postMethod.getOnlineResource().setHref(url + "wms?SERVICE=WMS&");
            }
        }
    }

    /**
     * update The URL in capabilities document for the extended operation.
     */
    private void updateExtendedOperationURL(final AbstractRequest request, final ServiceVersion version,
                                                                           final String url)
    {

        if (version.toString().equals("1.3.0")) {
            org.constellation.wms.v130.Request r = (org.constellation.wms.v130.Request) request;
            List<JAXBElement<OperationType>> extendedOperations = r.getExtendedOperation();
            for(JAXBElement<OperationType> extOp: extendedOperations) {
                updateURL(extOp.getValue().getDCPType(), url);
            }

        // version 1.1.1
        } else {
           org.constellation.wms.v111.Request r = (org.constellation.wms.v111.Request) request;
           AbstractOperation op = r.getDescribeLayer();
           if (op != null)
                updateURL(op.getDCPType(), url);
           op = r.getGetLegendGraphic();
           if (op != null)
                updateURL(op.getDCPType(), url);
           op = r.getGetStyles();
           if (op != null)
                updateURL(op.getDCPType(), url);
           op = r.getPutStyles();
           if (op != null)
                updateURL(op.getDCPType(), url);
        }
    }

}
