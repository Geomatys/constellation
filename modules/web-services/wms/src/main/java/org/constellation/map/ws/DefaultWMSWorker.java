/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.map.ws;

//J2SE dependencies
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

//Constellation dependencies
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.catalog.CatalogException;
import org.constellation.map.visitor.CSVGraphicVisitor;
import org.constellation.map.visitor.GMLGraphicVisitor;
import org.constellation.map.visitor.HTMLGraphicVisitor;
import org.constellation.map.visitor.TextGraphicVisitor;
import org.constellation.portrayal.PortrayalUtil;
import org.constellation.provider.CoverageLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.query.wms.GetMap;
import org.constellation.query.wms.WMSQuery;
import org.constellation.register.RegisterException;
import org.constellation.util.PeriodUtilities;
import org.constellation.util.Util;
import org.constellation.ws.AbstractWorker;
import org.constellation.ws.ServiceType;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.rs.WebService;

//Geotoolkit dependencies
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.display2d.service.VisitDef;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.se.xml.v110.OnlineResourceType;
import org.geotoolkit.sld.MutableLayerStyle;
import org.geotoolkit.sld.MutableNamedLayer;
import org.geotoolkit.sld.MutableNamedStyle;
import org.geotoolkit.sld.xml.v110.DescribeLayerResponseType;
import org.geotoolkit.sld.xml.v110.LayerDescriptionType;
import org.geotoolkit.sld.xml.v110.TypeNameType;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.util.Version;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.wms.xml.AbstractDCP;
import org.geotoolkit.wms.xml.AbstractDimension;
import org.geotoolkit.wms.xml.AbstractHTTP;
import org.geotoolkit.wms.xml.AbstractLayer;
import org.geotoolkit.wms.xml.AbstractOperation;
import org.geotoolkit.wms.xml.AbstractProtocol;
import org.geotoolkit.wms.xml.AbstractRequest;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.v111.LatLonBoundingBox;
import org.geotoolkit.wms.xml.v130.EXGeographicBoundingBox;
import org.geotoolkit.wms.xml.v130.OperationType;

//Geoapi dependencies
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.sld.Layer;
import org.opengis.sld.StyledLayerDescriptor;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
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
 * @author Johann Sorel (Geomatys)
 * @since 0.3
 */
public class DefaultWMSWorker extends AbstractWorker implements WMSWorker {
    /**
     * The default debugging logger for the WMS service.
     */
    private static final Logger LOGGER = Logging.getLogger(DefaultWMSWorker.class);

    /**
     * A map containing the Capabilities Object already loaded from file.
     */
    private final Map<String,Object> capabilities = new HashMap<String,Object>();

    /**
     * The web service unmarshaller, which will use the web service name space.
     */
    private final MarshallerPool marshallerPool;

    /**
     * Default size of the legend graphic.
     */
    private static final int LEGEND_WIDTH = 140;
    private static final int LEGEND_HEIGHT = 15;

    /**
     * Initializes the marshaller pool for the WMS.
     */
    public DefaultWMSWorker(final MarshallerPool marshallerPool) {
        this.marshallerPool = marshallerPool;
    }

    /**
     * Return a description of layers specified in the user's request.
     *
     * TODO: Does this actually do anything? why does this never access LayerDetails?
     * TODO: Is this broken?
     *
     * @param descLayer The {@linkplain DescribeLayer describe layer} request.
     *
     * @throws CstlServiceException
     */
    @Override
    public DescribeLayerResponseType describeLayer(final DescribeLayer descLayer) throws CstlServiceException {
        final OnlineResourceType or = new OnlineResourceType();
        or.setHref(getUriContext().getBaseUri().toString() + "wcs?");

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
     * @throws CstlServiceException
     */
    @Override
    public AbstractWMSCapabilities getCapabilities(final GetCapabilities getCapab) throws CstlServiceException {

        final String queryVersion = getCapab.getVersion().toString();

        //Add accepted CRS codes
        final List<String> crs = new ArrayList<String>();
        crs.add("EPSG:4326");
        crs.add("CRS:84");
        crs.add("EPSG:3395");
        crs.add("EPSG:27571");
        crs.add("EPSG:27572");
        crs.add("EPSG:27573");
        crs.add("EPSG:27574");


        //Generate the correct URL in the static part. ?TODO: clarify this.
        final AbstractWMSCapabilities inCapabilities;
        try {
            inCapabilities = (AbstractWMSCapabilities) getStaticCapabilitiesObject(
                    getServletContext().getRealPath("WEB-INF"), queryVersion);
        } catch (IOException e) {
            throw new CstlServiceException(e, NO_APPLICABLE_CODE);
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }
        final String url = getUriContext().getBaseUri().toString();
        inCapabilities.getService().getOnlineResource().setHref(url + "wms");
        final AbstractRequest request = inCapabilities.getCapability().getRequest();

        updateURL(request.getGetCapabilities().getDCPType(), url);
        updateURL(request.getGetFeatureInfo().getDCPType(), url);
        updateURL(request.getGetMap().getDCPType(), url);
        updateExtendedOperationURL(request, getCapab.getVersion(), url);



//        /* ****************************************************************** *
//         *   TODO: make this call Cstl.*
//         * ****************************************************************** */
////        final List<LayerDetails> layerRefs = Cstl.REGISTER.getLayerReferencesForWMS();
//        final NamedLayerDP dp = NamedLayerDP.getInstance();
//        final Set<String> keys = dp.getKeys();
//        final List<LayerDetails> layerRefs = new ArrayList<LayerDetails>();
//        for (String key : keys) {
//            final LayerDetails layer = dp.get(key);
//            if (layer == null) {
//                LOGGER.warning("Missing layer : " + key);
//                continue;
//            }
//            if (!layer.isQueryable(ServiceType.WMS)) {
//                LOGGER.info("layer" + layer.getName() + " not queryable by WMS");
//                continue;
//            }
//            layerRefs.add(layer);
//        }
//        /* ****************************************************************** *
//         *   TODO: make this call Cstl.                                       *
//         * ****************************************************************** */
        final List<LayerDetails> layerRefs = getAllLayerReferences(queryVersion);

        //Build the list of layers
        final List<AbstractLayer> layers = new ArrayList<AbstractLayer>();
        for (LayerDetails layer : layerRefs){
            if (!layer.isQueryable(ServiceType.WMS)) {
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
                throw new CstlServiceException(exception, NO_APPLICABLE_CODE);
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
                LOGGER.log(Level.INFO, "Error retrieving dates values for the layer :"+ layer.getName(), ex);
                dates = null;
            }
            if (dates != null && !(dates.isEmpty())) {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                final PeriodUtilities periodFormatter = new PeriodUtilities(df);
                defaut = df.format(dates.last());
                dim = (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) ?
                    new org.geotoolkit.wms.xml.v111.Dimension("time", "ISO8601", defaut, null) :
                    new org.geotoolkit.wms.xml.v130.Dimension("time", "ISO8601", defaut, null);
                dim.setValue(periodFormatter.getDatesRespresentation(dates));
                dimensions.add(dim);
            }

            //the available elevation
            defaut = null;
            SortedSet<Number> elevations = null;
            try {
                elevations = layer.getAvailableElevations();
            } catch (CatalogException ex) {
                LOGGER.log(Level.INFO, "Error retrieving elevation values for the layer :"+ layer.getName(), ex);
                elevations = null;
            }
            if (elevations != null && !(elevations.isEmpty())) {
                defaut = elevations.first().toString();
                dim = (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) ?
                    new org.geotoolkit.wms.xml.v111.Dimension("elevation", "EPSG:5030", defaut, null) :
                    new org.geotoolkit.wms.xml.v130.Dimension("elevation", "EPSG:5030", defaut, null);
                final StringBuilder elevs = new StringBuilder();
                for (final Iterator<Number> it = elevations.iterator(); it.hasNext();) {
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
            final MeasurementRange<?>[] ranges = layer.getSampleValueRanges();
            /* If the layer has only one sample dimension, then we can apply the dim_range
             * parameter. Otherwise it can be a multiple sample dimensions layer, and we
             * don't apply the dim_range.
             */
            if (ranges != null && ranges.length == 1 && ranges[0] != null) {
                final MeasurementRange<?> firstRange = ranges[0];
                final double minRange = firstRange.getMinimum();
                final double maxRange = firstRange.getMaximum();
                defaut = minRange + "," + maxRange;
                final Unit<?> u = firstRange.getUnits();
                final String unit = (u != null) ? u.toString() : null;
                dim = (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) ?
                    new org.geotoolkit.wms.xml.v111.Dimension("dim_range", unit, defaut,
                                                           minRange + "," + maxRange) :
                    new org.geotoolkit.wms.xml.v130.Dimension("dim_range", unit, defaut,
                                                           minRange + "," + maxRange);
                dimensions.add(dim);
            }

            // LegendUrl generation
            //TODO: Use a StringBuilder or two
            final String layerName = layer.getName();
            final String beginLegendUrl = url + "wms?REQUEST=GetLegendGraphic&" +
                                                    "VERSION=1.1.0&" +
                                                    "FORMAT=";
            final String legendUrlGif = beginLegendUrl + MimeType.IMAGE_GIF + "&LAYER=" + layerName;
            final String legendUrlPng = beginLegendUrl + MimeType.IMAGE_PNG + "&LAYER=" + layerName;
            final int queryable = (layer.isQueryable(ServiceType.GETINFO)) ? 1 : 0;
            final AbstractLayer outputLayer;
            if (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) {
                /*
                 * TODO
                 * Envelope inputBox = inputLayer.getCoverage().getEnvelope();
                 */
                final org.geotoolkit.wms.xml.v111.BoundingBox outputBBox = (inputGeoBox != null) ?
                    new org.geotoolkit.wms.xml.v111.BoundingBox("EPSG:4326",
                            inputGeoBox.getWestBoundLongitude(),
                            inputGeoBox.getSouthBoundLatitude(), inputGeoBox.getEastBoundLongitude(),
                            inputGeoBox.getNorthBoundLatitude(), 0.0, 0.0, queryVersion) :
                    null;

                // we build The Style part
                org.geotoolkit.wms.xml.v111.OnlineResource or =
                        new org.geotoolkit.wms.xml.v111.OnlineResource(legendUrlPng);
                final org.geotoolkit.wms.xml.v111.LegendURL legendURL1 =
                        new org.geotoolkit.wms.xml.v111.LegendURL(MimeType.IMAGE_PNG, or,
                        BigInteger.valueOf(LEGEND_WIDTH), BigInteger.valueOf(LEGEND_HEIGHT));

                or = new org.geotoolkit.wms.xml.v111.OnlineResource(legendUrlGif);
                final org.geotoolkit.wms.xml.v111.LegendURL legendURL2 =
                        new org.geotoolkit.wms.xml.v111.LegendURL(MimeType.IMAGE_GIF, or,
                        BigInteger.valueOf(LEGEND_WIDTH), BigInteger.valueOf(LEGEND_HEIGHT));

                final List<String> stylesName = layer.getFavoriteStyles();
                final List<org.geotoolkit.wms.xml.v111.Style> styles = new ArrayList<org.geotoolkit.wms.xml.v111.Style>();
                if (stylesName != null && stylesName.size() != 0) {
                    for (String styleName : stylesName) {
                        final org.geotoolkit.wms.xml.v111.Style style = new org.geotoolkit.wms.xml.v111.Style(
                                styleName, styleName, null, null, null, legendURL1, legendURL2);
                        styles.add(style);
                    }
                }

                final LatLonBoundingBox bbox = new LatLonBoundingBox(
                        inputGeoBox.getWestBoundLongitude(),
                        inputGeoBox.getSouthBoundLatitude(),
                        inputGeoBox.getEastBoundLongitude(),
                        inputGeoBox.getNorthBoundLatitude());
                if (layer instanceof CoverageLayerDetails) {
                    final CoverageLayerDetails coverageLayer = (CoverageLayerDetails)layer;
                    outputLayer = new org.geotoolkit.wms.xml.v111.Layer(layerName,
                            Util.cleanSpecialCharacter(coverageLayer.getRemarks()),
                            Util.cleanSpecialCharacter(coverageLayer.getThematic()), crs,
                            bbox, outputBBox, queryable, dimensions, styles);
                } else {
                    outputLayer = new org.geotoolkit.wms.xml.v111.Layer(layerName,
                            Util.cleanSpecialCharacter("Vector data"),
                            Util.cleanSpecialCharacter("Vector data"), crs, bbox,
                            outputBBox, queryable, dimensions, styles);
                }
            } else {
                /*
                 * TODO
                 * Envelope inputBox = inputLayer.getCoverage().getEnvelope();
                 */
                final org.geotoolkit.wms.xml.v130.BoundingBox outputBBox = (inputGeoBox != null) ?
                    new org.geotoolkit.wms.xml.v130.BoundingBox("EPSG:4326",
                            inputGeoBox.getWestBoundLongitude(),
                            inputGeoBox.getSouthBoundLatitude(),
                            inputGeoBox.getEastBoundLongitude(),
                            inputGeoBox.getNorthBoundLatitude(), 0.0, 0.0,
                            queryVersion) :
                    null;

                // we build a Style Object
                org.geotoolkit.wms.xml.v130.OnlineResource or =
                        new org.geotoolkit.wms.xml.v130.OnlineResource(legendUrlPng);
                final org.geotoolkit.wms.xml.v130.LegendURL legendURL1 =
                        new org.geotoolkit.wms.xml.v130.LegendURL(MimeType.IMAGE_PNG, or,
                        BigInteger.valueOf(LEGEND_WIDTH), BigInteger.valueOf(LEGEND_HEIGHT));

                or = new org.geotoolkit.wms.xml.v130.OnlineResource(legendUrlGif);
                final org.geotoolkit.wms.xml.v130.LegendURL legendURL2 =
                        new org.geotoolkit.wms.xml.v130.LegendURL(MimeType.IMAGE_GIF, or,
                        BigInteger.valueOf(LEGEND_WIDTH), BigInteger.valueOf(LEGEND_HEIGHT));

                final List<String> stylesName = layer.getFavoriteStyles();
                final List<org.geotoolkit.wms.xml.v130.Style> styles = new ArrayList<org.geotoolkit.wms.xml.v130.Style>();
                if (stylesName != null && stylesName.size() != 0) {
                    for (String styleName : stylesName) {
                        final org.geotoolkit.wms.xml.v130.Style style = new org.geotoolkit.wms.xml.v130.Style(
                        styleName, styleName, null, null, null, legendURL1, legendURL2);
                        styles.add(style);
                    }
                }

                final EXGeographicBoundingBox bbox = new EXGeographicBoundingBox(
                        inputGeoBox.getWestBoundLongitude(),
                        inputGeoBox.getSouthBoundLatitude(),
                        inputGeoBox.getEastBoundLongitude(),
                        inputGeoBox.getNorthBoundLatitude());
                if (layer instanceof CoverageLayerDetails) {
                    final CoverageLayerDetails coverageLayer = (CoverageLayerDetails)layer;
                    outputLayer = new org.geotoolkit.wms.xml.v130.Layer(layerName,
                            Util.cleanSpecialCharacter(coverageLayer.getRemarks()),
                            Util.cleanSpecialCharacter(coverageLayer.getThematic()), crs,
                            bbox, outputBBox, queryable, dimensions, styles);
                } else {
                    outputLayer = new org.geotoolkit.wms.xml.v130.Layer(layerName,
                            Util.cleanSpecialCharacter("Vector data"),
                            Util.cleanSpecialCharacter("Vector data"), crs, bbox,
                            outputBBox, queryable, dimensions, styles);
                }
            }
            layers.add(outputLayer);
        }

        //we build the general layer and add it to the document
        final AbstractLayer mainLayer = (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) ?
            new org.geotoolkit.wms.xml.v111.Layer("Constellation Web Map Layer",
                    "description of the service(need to be fill)", crs,
                    new LatLonBoundingBox(-180.0, -90.0, 180.0, 90.0), layers) :
            new org.geotoolkit.wms.xml.v130.Layer("Constellation Web Map Layer",
                    "description of the service(need to be fill)", crs,
                    new EXGeographicBoundingBox(-180.0, -90.0, 180.0, 90.0), layers);

        inCapabilities.getCapability().setLayer(mainLayer);
        return inCapabilities;
    }

    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @param home    The home directory, where to search for configuration files.
     * @param version The version of the GetCapabilities.
     * @return The capabilities Object, or {@code null} if none.
     *
     * @throws JAXBException
     * @throws IOException
     */
    private Object getStaticCapabilitiesObject(final String home, final String version) throws JAXBException, IOException {
        final String fileName = "WMSCapabilities" + version + ".xml";
        final File changeFile = getFile("change.properties", home);
        final Properties p = new Properties();

        // if the flag file is present we load the properties
        if (changeFile != null && changeFile.exists()) {
            final FileInputStream in = new FileInputStream(changeFile);
            p.load(in);
            in.close();
        } else {
            p.put("update", "false");
        }

        //Look if the template capabilities is already in cache.
        Object response = capabilities.get(fileName);
        final boolean update = p.getProperty("update").equals("true");

        if (response == null || update) {
            if (update) {
                LOGGER.info("updating metadata");
            }

            final File f = getFile(fileName, home);
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                // If the file is not present in the configuration directory, take the one in resource.
                if (!f.exists()) {
                    final InputStream in = getClass().getResourceAsStream(fileName);
                    response = unmarshaller.unmarshal(in);
                    in.close();
                } else {
                    response = unmarshaller.unmarshal(f);
                }
                capabilities.put(fileName, response);

            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }

            //this.setLastUpdateSequence(System.currentTimeMillis());
            p.put("update", "false");

            // if the flag file is present we store the properties
            if (changeFile != null && changeFile.exists()) {
                final FileOutputStream out = new FileOutputStream(changeFile);
                p.store(out, "updated from WebService");
                out.close();
            }
        }

        return response;
    }

    /**
     * Return a file located in the home directory. In this implementation, it should be
     * the WEB-INF directory of the deployed service.
     *
     * @param fileName The name of the file requested.
     * @return The specified file.
     */
    private File getFile(final String fileName, final String home) {
         File path;
         if (home == null || !(path = new File(home)).isDirectory()) {
            path = WebService.getSicadeDirectory();
         }
         if (fileName != null)
            return new File(path, fileName);
         else return path;
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
    private void updateExtendedOperationURL(final AbstractRequest request, final Version version,
                                                                           final String url){

        if (version.toString().equals(ServiceDef.WMS_1_3_0_SLD.version.toString())) {
            final org.geotoolkit.wms.xml.v130.Request r = (org.geotoolkit.wms.xml.v130.Request) request;
            final List<JAXBElement<OperationType>> extendedOperations = r.getExtendedOperation();
            for(JAXBElement<OperationType> extOp: extendedOperations) {
                updateURL(extOp.getValue().getDCPType(), url);
            }

        // version 1.1.1
        } else {
           final org.geotoolkit.wms.xml.v111.Request r = (org.geotoolkit.wms.xml.v111.Request) request;
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

    /**
     * Return the value of a point in a map.
     *
     * @param gfi The {@linkplain GetFeatureInfo get feature info} request.
     * @return text, HTML , XML or GML code.
     *
     * @throws CstlServiceException
     */
    @Override
    public synchronized String getFeatureInfo(final GetFeatureInfo getFI) throws CstlServiceException {

    	//
    	// Note this is almost the same logic as in getMap
    	//
        // 1. SCENE
        //       -- get the List of layer references
        final List<String> layerNames = getFI.getQueryLayers();
        final List<LayerDetails> layerRefs = getLayerReferences(layerNames, getFI.getVersion().toString());

        for (LayerDetails layer : layerRefs) {
            if (!layer.isQueryable(ServiceType.GETINFO)) {
                throw new CstlServiceException("You are not allowed to request the layer \""+
                        layer.getName() +"\".", LAYER_NOT_QUERYABLE, KEY_LAYERS.toLowerCase());
            }
        }
        //       -- build an equivalent style List
        //TODO: clean up the SLD vs. style logic
        final List<String> styleNames          = getFI.getStyles();
        final StyledLayerDescriptor sld = getFI.getSld();

        final List<MutableStyle> styles        = getStyles(layerRefs, sld, styleNames);
        //       -- create the rendering parameter Map
        final Double elevation                 = getFI.getElevation();
        final Date time                        = getFI.getTime();
        final Map<String, Object> params       = new HashMap<String, Object>();
        params.put(WMSQuery.KEY_ELEVATION, elevation);
        params.put(WMSQuery.KEY_TIME, time);
        final SceneDef sdef = new SceneDef();

        try {
            final MapContext context = PortrayalUtil.createContext(layerRefs, styles, params);
            sdef.setContext(context);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        // 2. VIEW
        final JTSEnvelope2D refEnv             = new JTSEnvelope2D(getFI.getEnvelope());
        final double azimuth                   = getFI.getAzimuth();
        final ViewDef vdef = new ViewDef(refEnv,azimuth);


        // 3. CANVAS
        final Dimension canvasDimension = getFI.getSize();
        final Color background;
        if (getFI.getTransparent()) {
            background = null;
        } else {
            final Color color = getFI.getBackground();
            background = (color == null) ? Color.WHITE : color;
        }
        final CanvasDef cdef = new CanvasDef(canvasDimension,background);

        // 4. SHAPE
        //     a
        final int pixelTolerance = 3;
        final int x = getFI.getX();
        final int y = getFI.getY();
        if (x < 0 || x > canvasDimension.width) {
            throw new CstlServiceException("The requested point has an invalid X coordinate.", INVALID_POINT);
        }
        if (y < 0 || y > canvasDimension.height) {
            throw new CstlServiceException("The requested point has an invalid Y coordinate.", INVALID_POINT);
        }
        final Rectangle selectionArea = new Rectangle( getFI.getX()-pixelTolerance,
        		                               getFI.getY()-pixelTolerance,
        		                               pixelTolerance*2,
        		                               pixelTolerance*2);

        // 5. VISITOR
        String infoFormat = getFI.getInfoFormat();
        if (infoFormat == null) {
            //Should not happen since the info format parameter is mandatory for the GetFeatureInfo request.
            infoFormat = MimeType.TEXT_PLAIN;
        }
        final TextGraphicVisitor visitor;
        if (infoFormat.equalsIgnoreCase(MimeType.TEXT_PLAIN)) {
            // TEXT / PLAIN
            visitor = new CSVGraphicVisitor(getFI);
        } else if (infoFormat.equalsIgnoreCase(MimeType.TEXT_HTML)) {
            // TEXT / HTML
            visitor = new HTMLGraphicVisitor(getFI);
        } else if (infoFormat.equalsIgnoreCase(MimeType.APP_GML) || infoFormat.equalsIgnoreCase(MimeType.TEXT_XML) ||
                   infoFormat.equalsIgnoreCase(MimeType.APP_XML) || infoFormat.equalsIgnoreCase(XML) ||
                   infoFormat.equalsIgnoreCase(GML))
        {
            // GML
            visitor = new GMLGraphicVisitor(getFI);
        } else {
            throw new CstlServiceException("MIME type " + infoFormat + " is not accepted by the service.\n" +
                    "You have to choose between: "+ MimeType.TEXT_PLAIN +", "+ MimeType.TEXT_HTML +", "+ MimeType.APP_GML +", "+ GML +
                    ", "+ MimeType.APP_XML +", "+ XML+", "+ MimeType.TEXT_XML,
                    INVALID_FORMAT, KEY_INFO_FORMAT.toLowerCase());
        }

        final VisitDef visitDef = new VisitDef();
        visitDef.setArea(selectionArea);
        visitDef.setVisitor(visitor);


        // We now build the response, according to the format chosen.
        try {
        	Cstl.getPortrayalService().visit(sdef,vdef,cdef,visitDef);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        return visitor.getResult();
    }

    /**
     * Return the legend graphic for the current layer.
     * <p>If no width or height have been specified, a default output
     * size is adopted (140x15 pixels).</p>
     *
     * @param getLegend The {@linkplain GetLegendGraphic get legend graphic} request.
     * @return a file containing the legend graphic image.
     *
     * @throws CstlServiceException
     */
    @Override
    public BufferedImage getLegendGraphic(final GetLegendGraphic getLegend) throws CstlServiceException {
        final LayerDetails layer = getLayerReference(getLegend.getLayer(), getLegend.getVersion().toString());
        if (!layer.isQueryable(ServiceType.WMS)) {
            throw new CstlServiceException("You are not allowed to request the layer \""+
                    layer.getName() +"\".", LAYER_NOT_QUERYABLE, KEY_LAYER.toLowerCase());
        }
        final Integer width  = getLegend.getWidth();
        final Integer height = getLegend.getHeight();
        final Dimension dims = new Dimension((width == null) ? LEGEND_WIDTH : width,
                                             (height == null) ? LEGEND_HEIGHT : height);
        return layer.getLegendGraphic(dims);
    }

    /**
     * Return a map for the specified parameters in the query.
     *
     * @param getMap The {@linkplain GetMap get map} request.
     * @return The map requested, or an error.
     *
     * @throws CstlServiceException
     */
    @Override
    public BufferedImage getMap(final GetMap getMap) throws CstlServiceException {

    	//
    	// Note this is almost the same logic as in getFeatureInfo
    	//
        final String errorType = getMap.getExceptionFormat();
        final boolean errorInImage = EXCEPTIONS_INIMAGE.equalsIgnoreCase(errorType);


        // 1. SCENE
        //       -- get the List of layer references
        final List<String> layerNames = getMap.getLayers();
        final List<LayerDetails> layerRefs;
        try{
        	layerRefs = getLayerReferences(layerNames, getMap.getVersion().toString());
        } catch (CstlServiceException ex) {
        	//TODO: distinguish
            if (errorInImage) {
                return Cstl.getPortrayalService().writeInImage(ex, getMap.getSize());
            } else {
                throw new CstlServiceException(ex, LAYER_NOT_DEFINED, KEY_LAYERS.toLowerCase());
            }
        }
        for (LayerDetails layer : layerRefs) {
            if (!layer.isQueryable(ServiceType.WMS)) {
                throw new CstlServiceException("You are not allowed to request the layer \""+
                        layer.getName() +"\".", LAYER_NOT_QUERYABLE, KEY_LAYERS.toLowerCase());
            }
        }
        //       -- build an equivalent style List
        //TODO: clean up the SLD vs. style logic
        final List<String> styleNames          = getMap.getStyles();
        final StyledLayerDescriptor sld = getMap.getSld();

        final List<MutableStyle> styles = getStyles(layerRefs, sld, styleNames);
        //       -- create the rendering parameter Map
        final Double elevation                 = getMap.getElevation();
        final Date time                        = getMap.getTime();
        final MeasurementRange<?> dimRange     = getMap.getDimRange();
        final Map<String, Object> params       = new HashMap<String, Object>();
        params.put(WMSQuery.KEY_ELEVATION, elevation);
        params.put(WMSQuery.KEY_DIM_RANGE, dimRange);
        params.put(WMSQuery.KEY_TIME, time);
        final SceneDef sdef = new SceneDef();
        sdef.extensions().add(WMSMapDecoration.getExtension());

        try {
            final MapContext context = PortrayalUtil.createContext(layerRefs, styles, params);
            sdef.setContext(context);
        } catch (PortrayalException ex) {
            if (errorInImage) {
                return Cstl.getPortrayalService().writeInImage(ex, getMap.getSize() );
            } else {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
        }


        // 2. VIEW
        final JTSEnvelope2D refEnv             = new JTSEnvelope2D(getMap.getEnvelope());
        final double azimuth                   = getMap.getAzimuth();
        final ViewDef vdef = new ViewDef(refEnv,azimuth);


        // 3. CANVAS
        final Dimension canvasDimension        = getMap.getSize();
        final Color background;
        if (getMap.getTransparent()) {
            background = null;
        } else {
            final Color color = getMap.getBackground();
            background = (color == null) ? Color.WHITE : color;
        }
        final CanvasDef cdef = new CanvasDef(canvasDimension,background);

        // 4. IMAGE
        BufferedImage image;
        try {
            image = Cstl.getPortrayalService().portray(sdef, vdef, cdef);
        } catch (PortrayalException ex) {
            if (errorInImage) {
                return Cstl.getPortrayalService().writeInImage(ex, getMap.getSize() );
            } else {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
        }

        return image;
    }



    //TODO: handle the null value in the exception.
    //TODO: harmonize with the method getLayerReference().
    private static List<LayerDetails> getAllLayerReferences(final String version) throws CstlServiceException {

        List<LayerDetails> layerRefs;
        try { // WE catch the exception from either service version
            if (version.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) {
                layerRefs = Cstl.getRegister().getAllLayerReferences(ServiceDef.WMS_1_1_1_SLD);
            } else if (version.equals(ServiceDef.WMS_1_3_0_SLD.version.toString())) {
                layerRefs = Cstl.getRegister().getAllLayerReferences(ServiceDef.WMS_1_3_0_SLD);
            } else {
                throw new CstlServiceException("WMS acting according to no known version.",
                        VERSION_NEGOTIATION_FAILED, KEY_VERSION.toLowerCase());
            }
        } catch (RegisterException regex) {
            throw new CstlServiceException(regex, LAYER_NOT_DEFINED);
        }
        return layerRefs;
    }

    //TODO: handle the null value in the exception.
    //TODO: harmonize with the method getLayerReference().
    private static List<LayerDetails> getLayerReferences(final List<String> layerNames, final String version)
            throws CstlServiceException {
        List<LayerDetails> layerRefs;
        try { // WE catch the exception from either service version
            if (version.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) {
                layerRefs = Cstl.getRegister().getLayerReferences(ServiceDef.WMS_1_1_1_SLD, layerNames);
            } else if (version.equals(ServiceDef.WMS_1_3_0_SLD.version.toString())) {
                layerRefs = Cstl.getRegister().getLayerReferences(ServiceDef.WMS_1_3_0_SLD, layerNames);
            } else {
                throw new CstlServiceException("WMS acting according to no known version.",
                        VERSION_NEGOTIATION_FAILED, KEY_VERSION.toLowerCase());
            }
        } catch (RegisterException regex) {
            throw new CstlServiceException(regex, LAYER_NOT_DEFINED);
        }
        return layerRefs;
    }

    //TODO: handle the null value in the exception.
    //TODO: harmonize with the method getLayerReference().
    public static LayerDetails getLayerReference(final String layerName, final String version)
            throws CstlServiceException {

        LayerDetails layerRef;
        try { // WE catch the exception from either service version
            if (version.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) {
                layerRef = Cstl.getRegister().getLayerReference(ServiceDef.WMS_1_1_1_SLD, layerName);
            } else if (version.equals(ServiceDef.WMS_1_3_0_SLD.version.toString())) {
                layerRef = Cstl.getRegister().getLayerReference(ServiceDef.WMS_1_3_0_SLD, layerName);
            } else {
                throw new CstlServiceException("WMS acting according to no known version.",
                        VERSION_NEGOTIATION_FAILED, KEY_VERSION.toLowerCase());
            }
        } catch (RegisterException regex) {
            throw new CstlServiceException(regex, LAYER_NOT_DEFINED);
        }
        return layerRef;
    }



    private static MutableStyle extractStyle(final String layerName, final StyledLayerDescriptor sld){
        if(sld == null){
            throw new NullPointerException("SLD should not be null");
        }

        for(final Layer layer : sld.layers()){

            if(layer instanceof MutableNamedLayer && layerName.equals(layer.getName()) ){
                //we can only extract style from a NamedLayer that has the same name
                final MutableNamedLayer mnl = (MutableNamedLayer) layer;

                for(final MutableLayerStyle mls : mnl.styles()){
                    if(mls instanceof MutableNamedStyle){
                        final MutableNamedStyle mns = (MutableNamedStyle) mls;
                        final String namedStyle = mns.getName();
                        return StyleProviderProxy.getInstance().get(namedStyle);
                    }else if(mls instanceof MutableStyle){
                        return (MutableStyle)mls;
                    }

                }
            }
        }

        //no valid style found
        return null;
    }

    private static List<MutableStyle> getStyles(final List<LayerDetails> layerRefs, final StyledLayerDescriptor sld,
                                         final List<String> styleNames) throws CstlServiceException {
        final List<MutableStyle> styles = new ArrayList<MutableStyle>();
        for (int i=0; i<layerRefs.size(); i++) {

            final MutableStyle style;
            if (sld != null) {
                //try to use the provided SLD
                style = extractStyle(layerRefs.get(i).getName(), sld);
            } else if (styleNames != null && styleNames.size() > i) {
                //try to grab the style if provided
                //a style has been given for this layer, try to use it
                final String namedStyle = styleNames.get(i);
                style = StyleProviderProxy.getInstance().get(namedStyle);
                if (style == null) {
                    throw new CstlServiceException("Style provided not found.", STYLE_NOT_DEFINED);
                }
            } else {
                //no defined styles, use the favorite one, let the layer get it himself.
                style = null;
            }
            styles.add(style);
        }
        return styles;
    }
}
