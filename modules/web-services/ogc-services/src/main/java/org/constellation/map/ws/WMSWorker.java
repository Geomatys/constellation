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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.logging.Logger;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.constellation.catalog.CatalogException;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedLayerDP;
import org.constellation.query.wms.GetCapabilities;
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
import org.constellation.ws.Service;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.WebServiceException;
import org.geotools.util.MeasurementRange;
import org.opengis.metadata.extent.GeographicBoundingBox;

import static org.constellation.ws.ExceptionCode.*;
import static org.constellation.query.wms.WMSQuery.*;

/**
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 */
public class WMSWorker {
    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.map.ws");

    /**
     * Describe the capabilities and the layers available of this service.
     *
     * @param getCapab The {@linkplain GetCapabilities get capabilities} request.
     * @return a WMSCapabilities XML document describing the capabilities of the service.
     *
     * @throws WebServiceException
     * @throws JAXBException when unmarshalling the default GetCapabilities file.
     */
    public AbstractWMSCapabilities getCapabilities(final GetCapabilities getCapab, final String url,
            AbstractWMSCapabilities inCapabilities) throws WebServiceException
    {
        final ServiceVersion queryVersion = getCapab.getVersion();

        //we build the list of accepted crs
        final List<String> crs = new ArrayList<String>();
        crs.add("EPSG:4326");     crs.add("CRS:84");  crs.add("EPSG:3395");
        crs.add("EPSG:27571"); crs.add("EPSG:27572"); crs.add("EPSG:27573"); crs.add("EPSG:27574");
        //we update the url in the static part.
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
            final int queryable = (layer.isQueryable(Service.GETINFO) == true) ? 1 : 0;
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
