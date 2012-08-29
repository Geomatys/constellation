/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.logging.Level;
import javax.imageio.spi.ServiceRegistry;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;
import javax.xml.bind.JAXBException;

//Constellation dependencies
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.configuration.AttributionType;
import org.constellation.configuration.Layer;
import org.constellation.configuration.FormatURL;
import org.constellation.configuration.Reference;
import org.constellation.converter.DataReferenceConverter;
import org.constellation.map.visitor.GetFeatureInfoVisitor;
import org.constellation.map.visitor.WMSVisitorFactory;
import org.constellation.portrayal.PortrayalUtil;
import org.constellation.portrayal.internal.PortrayalResponse;
import org.constellation.provider.CoverageLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.query.wms.WMSQuery;
import org.constellation.util.DataReference;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.LayerWorker;
import org.constellation.ws.MimeType;
import static org.constellation.api.CommonConstants.*;
import static org.constellation.query.wms.WMSQuery.*;

//Geotoolkit dependencies
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.display2d.service.VisitDef;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.inspire.xml.vs.ExtendedCapabilitiesType;
import org.geotoolkit.inspire.xml.vs.LanguageType;
import org.geotoolkit.inspire.xml.vs.LanguagesType;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.se.xml.v110.OnlineResourceType;
import org.geotoolkit.sld.MutableLayer;
import org.geotoolkit.sld.MutableLayerStyle;
import org.geotoolkit.sld.MutableNamedLayer;
import org.geotoolkit.sld.MutableNamedStyle;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.sld.xml.v110.DescribeLayerResponseType;
import org.geotoolkit.sld.xml.v110.LayerDescriptionType;
import org.geotoolkit.sld.xml.v110.TypeNameType;
import org.geotoolkit.sld.xml.GetLegendGraphic;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.StyleUtilities;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.util.PeriodUtilities;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.wms.xml.v111.Request;
import org.geotoolkit.wms.xml.v130.Keyword;
import org.geotoolkit.wms.xml.v130.DataURL;
import org.geotoolkit.wms.xml.v130.Capability;
import org.geotoolkit.wms.xml.v130.MetadataURL;
import org.geotoolkit.wms.xml.v130.Identifier;
import org.geotoolkit.wms.xml.v130.KeywordList;
import org.geotoolkit.wms.xml.v130.LogoURL;
import org.geotoolkit.wms.xml.AbstractDimension;
import org.geotoolkit.wms.xml.AbstractLayer;
import org.geotoolkit.wms.xml.AbstractRequest;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.WMSMarshallerPool;
import org.geotoolkit.wms.xml.GetCapabilities;
import org.geotoolkit.wms.xml.GetMap;
import org.geotoolkit.wms.xml.GetFeatureInfo;
import org.geotoolkit.wms.xml.DescribeLayer;
import org.geotoolkit.wms.xml.v111.LatLonBoundingBox;
import org.geotoolkit.wms.xml.v130.Attribution;
import org.geotoolkit.wms.xml.v130.AuthorityURL;
import org.geotoolkit.wms.xml.v130.EXGeographicBoundingBox;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

//Geoapi dependencies
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.TransformException;
import org.opengis.sld.StyledLayerDescriptor;
import org.opengis.style.Style;
import org.opengis.util.FactoryException;


/**
 * A WMS worker for a local WMS service which handles requests from either REST
 * or SOAP facades and issues appropriate responses.
 * <p>
 * The classes implementing the REST or SOAP facades to this service will have
 * processed the requests sufficiently to ensure that all the information
 * conveyed by the HTTP request is either in the method call parameters or is
 * in one of the fields of the parent class which holds instances of the
 * injectable interface {@code Context} objects created by the JEE container.
 * </p>
 *
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @author Guilhem Legall (Geomatys)
 * @since 0.3
 */
public class DefaultWMSWorker extends LayerWorker implements WMSWorker {

    private static final WMSVisitorFactory[] VISITOR_FACTORIES;
    private static final List<String> GFI_MIME_TYPES = new ArrayList<String>();

    static {
        final List<WMSVisitorFactory> factories = new ArrayList<WMSVisitorFactory>();

        final Iterator<WMSVisitorFactory> ite = ServiceRegistry.lookupProviders(WMSVisitorFactory.class);
        while(ite.hasNext()){
            final WMSVisitorFactory f = ite.next();
            factories.add(f);
            GFI_MIME_TYPES.addAll(Arrays.asList(f.getSupportedMimeTypes()));
        }

        VISITOR_FACTORIES = factories.toArray(new WMSVisitorFactory[factories.size()]);
    }


    /**
     * Output responses of a GetCapabilities request.
     */
    private final Map<String,AbstractWMSCapabilities> CAPS_RESPONSE =
            Collections.synchronizedMap(new HashMap<String,AbstractWMSCapabilities>());

    private final WMSMapDecoration mapDecoration;

    public DefaultWMSWorker(String id, File configurationDirectory) {
        super(id, configurationDirectory, ServiceDef.Specification.WMS);
        mapDecoration = new WMSMapDecoration(configurationDirectory);
        if (isStarted) {
            LOGGER.log(Level.INFO, "WMS worker {0} running", id);
        }

        //listen to changes on the providers to clear the getcapabilities cache
        LayerProviderProxy.getInstance().addPropertyListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                refreshUpdateSequence();
                CAPS_RESPONSE.clear();
            }
        });

    }

    @Override
    protected MarshallerPool getMarshallerPool() {
        return WMSMarshallerPool.getInstance();
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
        final OnlineResourceType or = new OnlineResourceType(getServiceUrl());

        final List<LayerDescriptionType> layerDescriptions = new ArrayList<LayerDescriptionType>();
        final List<String> layerNames = descLayer.getLayers();
        for (String layerName : layerNames) {
            final TypeNameType t = new TypeNameType(layerName.trim());
            final LayerDescriptionType outputLayer = new LayerDescriptionType(or, t);
            layerDescriptions.add(outputLayer);
        }
        return new DescribeLayerResponseType("1.1.1", layerDescriptions);
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
        isWorking();
        final String queryVersion = getCapab.getVersion().toString();

        final String requestedLanguage = getCapab.getLanguage();

        // we get the request language, if its not set we get the default "eng"
        final String currentLanguage;
        if (requestedLanguage != null && supportedLanguages.contains(requestedLanguage) && !requestedLanguage.equals(defaultLanguage)) {
            currentLanguage = requestedLanguage;
        } else {
            currentLanguage = null;
        }

        final String keyCache = getId() + queryVersion + '-' + currentLanguage;
        if (CAPS_RESPONSE.containsKey(keyCache)) {
            return CAPS_RESPONSE.get(keyCache);
        }

        final AbstractWMSCapabilities inCapabilities = (AbstractWMSCapabilities) getStaticCapabilitiesObject(queryVersion, "WMS", currentLanguage);

        final AbstractRequest request;
        final List<String> exceptionFormats;
        if (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) {
            request          = new Request(WMSConstant.createRequest111(GFI_MIME_TYPES));
            exceptionFormats = WMSConstant.EXCEPTION_111;
        } else {
            request          = new org.geotoolkit.wms.xml.v130.Request(WMSConstant.createRequest130(GFI_MIME_TYPES));
            exceptionFormats = WMSConstant.EXCEPTION_130;
        }
        request.updateURL(getServiceUrl());
        inCapabilities.getCapability().setRequest(request);
        inCapabilities.getCapability().setExceptionFormats(exceptionFormats);

        //set the current updateSequence parameter
        final boolean returnUS = returnUpdateSequenceDocument(getCapab.getUpdateSequence());
        if (returnUS) {
            throw new CstlServiceException("th update sequence paramter is equal to the current", CURRENT_UPDATE_SEQUENCE, "updateSequence");
        }

        //Build the list of layers
        final List<AbstractLayer> outputLayers = new ArrayList<AbstractLayer>();
        final LayerProviderProxy namedProxy    = LayerProviderProxy.getInstance();
        final Map<Name,Layer> layers = getLayers();

        for (Name name : layers.keySet()) {
            final LayerDetails layer = namedProxy.get(name);
            final Layer configLayer  = layers.get(name);

            if (!layer.isQueryable(ServiceDef.Query.WMS_ALL)) {
                continue;
            }
            /*
             *  TODO
             * code = CRS.lookupEpsgCode(inputLayer.getCoverageReference().getCoordinateReferenceSystem(), false);
             */
            GeographicBoundingBox inputGeoBox;
            try {
                inputGeoBox = layer.getGeographicBoundingBox();
            } catch (DataStoreException exception) {
                throw new CstlServiceException(exception, NO_APPLICABLE_CODE);
            }

            if (inputGeoBox == null) {
                // The layer does not contain geometric information, we do not want this layer
                // in the capabilities response.
                continue;
            }

            // We ensure that the data envelope is not empty. It can occurs with vector data, on a single point.
            final double width  = inputGeoBox.getEastBoundLongitude() - inputGeoBox.getWestBoundLongitude();
            final double height = inputGeoBox.getNorthBoundLatitude() - inputGeoBox.getSouthBoundLatitude();
            if (width == 0 && height == 0) {
                final double diffWidth = Math.nextUp(inputGeoBox.getEastBoundLongitude()) - inputGeoBox.getEastBoundLongitude();
                final double diffHeight = Math.nextUp(inputGeoBox.getNorthBoundLatitude()) - inputGeoBox.getNorthBoundLatitude();
                inputGeoBox = new LatLonBoundingBox(inputGeoBox.getWestBoundLongitude() - diffWidth,
                                                    inputGeoBox.getSouthBoundLatitude() - diffHeight,
                                                    Math.nextUp(inputGeoBox.getEastBoundLongitude()),
                                                    Math.nextUp(inputGeoBox.getNorthBoundLatitude()));
            }
            if (width == 0) {
                final double diffWidth = Math.nextUp(inputGeoBox.getEastBoundLongitude()) - inputGeoBox.getEastBoundLongitude();
                inputGeoBox = new LatLonBoundingBox(inputGeoBox.getWestBoundLongitude() - diffWidth, inputGeoBox.getSouthBoundLatitude(),
                        Math.nextUp(inputGeoBox.getEastBoundLongitude()), inputGeoBox.getNorthBoundLatitude());
            }
            if (height == 0) {
                final double diffHeight = Math.nextUp(inputGeoBox.getNorthBoundLatitude()) - inputGeoBox.getNorthBoundLatitude();
                inputGeoBox = new LatLonBoundingBox(inputGeoBox.getWestBoundLongitude(), inputGeoBox.getSouthBoundLatitude() - diffHeight,
                        inputGeoBox.getEastBoundLongitude(), Math.nextUp(inputGeoBox.getNorthBoundLatitude()));
            }
            // List of elevations, times and dim_range values.
            final List<AbstractDimension> dimensions = new ArrayList<AbstractDimension>();

            /*
             * Dimension: the available date
             */
            AbstractDimension dim;
            SortedSet<Date> dates;
            try {
                dates = layer.getAvailableTimes();
            } catch (DataStoreException ex) {
                LOGGER.log(Level.INFO, "Error retrieving dates values for the layer :"+ layer.getName(), ex);
                dates = null;
            }
            if (dates != null && !(dates.isEmpty())) {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                final PeriodUtilities periodFormatter = new PeriodUtilities(df);
                final String defaut = df.format(dates.last());
                dim = (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) ?
                    new org.geotoolkit.wms.xml.v111.Dimension("time", "ISO8601", defaut, null) :
                    new org.geotoolkit.wms.xml.v130.Dimension("time", "ISO8601", defaut, null);
                dim.setValue(periodFormatter.getDatesRespresentation(dates));
                dimensions.add(dim);
            }

            /*
             * Dimension: the available elevation
             */
            SortedSet<Number> elevations;
            try {
                elevations = layer.getAvailableElevations();
            } catch (DataStoreException ex) {
                LOGGER.log(Level.INFO, "Error retrieving elevation values for the layer :"+ layer.getName(), ex);
                elevations = null;
            }
            if (elevations != null && !(elevations.isEmpty())) {
                final String defaut = elevations.first().toString();
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

            /*
             * Dimension: the dimension range
             */
            final MeasurementRange<?>[] ranges = layer.getSampleValueRanges();
            /* If the layer has only one sample dimension, then we can apply the dim_range
             * parameter. Otherwise it can be a multiple sample dimensions layer, and we
             * don't apply the dim_range.
             */
            if (ranges != null && ranges.length == 1 && ranges[0] != null) {
                final MeasurementRange<?> firstRange = ranges[0];
                final double minRange = firstRange.getMinimum();
                final double maxRange = firstRange.getMaximum();
                final String defaut = minRange + "," + maxRange;
                final Unit<?> u = firstRange.getUnits();
                final String unit = (u != null) ? u.toString() : null;
                String unitSymbol;
                try {
                    unitSymbol = UnitFormat.getInstance().format(u);
                } catch (IllegalArgumentException e) {
                    // Workaround for one more bug in javax.measure...
                    unitSymbol = unit;
                }
                dim = (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) ?
                    new org.geotoolkit.wms.xml.v111.Dimension(minRange + "," + maxRange, "dim_range", unit,
                                                              unitSymbol, defaut, null, null, null) :
                    new org.geotoolkit.wms.xml.v130.Dimension(minRange + "," + maxRange, "dim_range", unit,
                                                              unitSymbol, defaut, null, null, null);
                dimensions.add(dim);
            }

            /*
             * LegendUrl generation
             * TODO: Use a StringBuilder or two
             */
            final Name fullLayerName = layer.getName();
            final String layerName;

            //Use layer alias from config if exist.
            if (configLayer.getAlias() != null && !configLayer.getAlias().isEmpty()) {
                layerName = configLayer.getAlias().trim().replaceAll(" ", "_");
            } else {

                if (fullLayerName.getNamespaceURI() != null && !fullLayerName.getNamespaceURI().isEmpty()) {
                    layerName = fullLayerName.getNamespaceURI() + ':' + fullLayerName.getLocalPart();
                } else {
                    layerName = fullLayerName.getLocalPart();
                }
            }
            final String beginLegendUrl = getServiceUrl() + "REQUEST=GetLegendGraphic&" +
                                                    "VERSION=1.1.1&" +
                                                    "FORMAT=";
            final String legendUrlGif = beginLegendUrl + MimeType.IMAGE_GIF + "&LAYER=" + layerName;
            final String legendUrlPng = beginLegendUrl + MimeType.IMAGE_PNG + "&LAYER=" + layerName;
            final String queryable    = (layer.isQueryable(ServiceDef.Query.WMS_GETINFO)) ? "1" : "0";

            final AbstractLayer outputLayer;
            if (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) {
                /*
                 * TODO
                 * Envelope inputBox = inputLayer.getCoverage().getEnvelope();
                 */
                final org.geotoolkit.wms.xml.v111.BoundingBox outputBBox =
                    new org.geotoolkit.wms.xml.v111.BoundingBox("EPSG:4326",
                            inputGeoBox.getWestBoundLongitude(),
                            inputGeoBox.getSouthBoundLatitude(), inputGeoBox.getEastBoundLongitude(),
                            inputGeoBox.getNorthBoundLatitude(), 0.0, 0.0, queryVersion);

                // we build The Style part
                org.geotoolkit.wms.xml.v111.OnlineResource or =
                        new org.geotoolkit.wms.xml.v111.OnlineResource(legendUrlPng);

                final List<String> stylesName = layer.getFavoriteStyles();
                final List<org.geotoolkit.wms.xml.v111.Style> styles = new ArrayList<org.geotoolkit.wms.xml.v111.Style>();
                if (stylesName != null && !stylesName.isEmpty()) {
                    // For each styles defined for the layer, get the dimension of the getLegendGraphic response.
                    for (String styleName : stylesName) {
                        final MutableStyle ms = StyleProviderProxy.getInstance().get(styleName);
                        final org.geotoolkit.wms.xml.v111.Style style = convertMutableStyleToWmsStyle111(ms, layer, legendUrlPng, legendUrlGif);
                        styles.add(style);
                    }
                }

                final LatLonBoundingBox bbox = new LatLonBoundingBox(inputGeoBox);
                final org.geotoolkit.wms.xml.v111.Layer outputLayer111;
                if (layer instanceof CoverageLayerDetails) {
                    final CoverageLayerDetails coverageLayer = (CoverageLayerDetails)layer;
                    outputLayer111 = new org.geotoolkit.wms.xml.v111.Layer(layerName,
                            StringUtilities.cleanSpecialCharacter(coverageLayer.getRemarks()),
                            StringUtilities.cleanSpecialCharacter(coverageLayer.getThematic()), DEFAULT_CRS,
                            bbox, outputBBox, queryable, dimensions, styles);
                } else {
                    outputLayer111 = new org.geotoolkit.wms.xml.v111.Layer(layerName,
                            "Vector data", "Vector data", DEFAULT_CRS, bbox,
                            outputBBox, queryable, dimensions, styles);
                }

                outputLayer = customizeLayer111(outputLayer111, configLayer, layer, legendUrlPng, legendUrlGif);
            } else {
                /*
                 * TODO
                 * Envelope inputBox = inputLayer.getCoverage().getEnvelope();
                 */
                final org.geotoolkit.wms.xml.v130.BoundingBox outputBBox =
                    new org.geotoolkit.wms.xml.v130.BoundingBox("EPSG:4326",
                            inputGeoBox.getWestBoundLongitude(),
                            inputGeoBox.getSouthBoundLatitude(),
                            inputGeoBox.getEastBoundLongitude(),
                            inputGeoBox.getNorthBoundLatitude(), 0.0, 0.0,
                            queryVersion);

                // we build a Style Object
                org.geotoolkit.wms.xml.v130.OnlineResource or =
                        new org.geotoolkit.wms.xml.v130.OnlineResource(legendUrlPng);

                final List<String> stylesName = layer.getFavoriteStyles();
                final List<org.geotoolkit.wms.xml.v130.Style> styles = new ArrayList<org.geotoolkit.wms.xml.v130.Style>();
                if (stylesName != null && !stylesName.isEmpty()) {
                    // For each styles defined for the layer, get the dimension of the getLegendGraphic response.
                    for (String styleName : stylesName) {
                        final MutableStyle ms = StyleProviderProxy.getInstance().get(styleName);
                        final org.geotoolkit.wms.xml.v130.Style style = convertMutableStyleToWmsStyle130(ms, layer, legendUrlPng, legendUrlGif);
                        styles.add(style);
                    }
                }

                final EXGeographicBoundingBox bbox = new EXGeographicBoundingBox(inputGeoBox);
                final org.geotoolkit.wms.xml.v130.Layer outputLayer130;
                if (layer instanceof CoverageLayerDetails) {
                    final CoverageLayerDetails coverageLayer = (CoverageLayerDetails)layer;
                    outputLayer130 = new org.geotoolkit.wms.xml.v130.Layer(layerName,
                            StringUtilities.cleanSpecialCharacter(coverageLayer.getRemarks()),
                            StringUtilities.cleanSpecialCharacter(coverageLayer.getThematic()), DEFAULT_CRS,
                            bbox, outputBBox, queryable, dimensions, styles);
                } else {
                    outputLayer130 = new org.geotoolkit.wms.xml.v130.Layer(layerName,
                            "Vector data", "Vector data", DEFAULT_CRS, bbox,
                            outputBBox, queryable, dimensions, styles);
                }
                outputLayer = customizeLayer130(outputLayer130, configLayer, layer, legendUrlPng, legendUrlGif);
            }
            outputLayers.add(outputLayer);
        }

        //we build the general layer and add it to the document
        final AbstractLayer mainLayer = (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) ?
            customizeLayer111(new org.geotoolkit.wms.xml.v111.Layer("Constellation Web Map Layer",
                    "description of the service(need to be fill)", DEFAULT_CRS,
                    new LatLonBoundingBox(-180.0, -90.0, 180.0, 90.0), outputLayers), getMainLayer(), null, null, null):
            customizeLayer130(new org.geotoolkit.wms.xml.v130.Layer("Constellation Web Map Layer",
                    "description of the service(need to be fill)", DEFAULT_CRS,
                    new EXGeographicBoundingBox(-180.0, -90.0, 180.0, 90.0), outputLayers), getMainLayer(), null, null, null);

        inCapabilities.getCapability().setLayer(mainLayer);


        /*
         * INSPIRE PART
         */
        if (queryVersion.equals(ServiceDef.WMS_1_3_0.version.toString()) || queryVersion.equals(ServiceDef.WMS_1_3_0_SLD.version.toString()) ) {

            Capability capa = (Capability) inCapabilities.getCapability();
            ExtendedCapabilitiesType inspireExtension =  capa.getInspireExtendedCapabilities();

            if (inspireExtension != null) {
                inspireExtension.setMetadataDate(new Date(System.currentTimeMillis()));

                List<LanguageType> languageList = new ArrayList<LanguageType>();
                for (String language : supportedLanguages) {
                    boolean isDefault = language.equals(defaultLanguage);
                    languageList.add(new LanguageType(language, isDefault));
                }
                LanguagesType languages = new LanguagesType(languageList);
                inspireExtension.setLanguages(languages);
                if (currentLanguage == null) {
                    inspireExtension.setCurrentLanguage(defaultLanguage);
                } else {
                    inspireExtension.setCurrentLanguage(currentLanguage);
                }
            }

        }
        CAPS_RESPONSE.put(keyCache, inCapabilities);
        return inCapabilities;
    }

    /**
     * Apply the layer customization extracted from the configuration.
     *
     * @param outputLayer111
     * @param configLayer
     * @return
     */
    private org.geotoolkit.wms.xml.v111.Layer customizeLayer111(final org.geotoolkit.wms.xml.v111.Layer outputLayer111, final Layer configLayer,
            final LayerDetails layerDetails, final String legendUrlPng, final String legendUrlGif)
    {
        if (configLayer == null) {
            return outputLayer111;
        }
        if (configLayer.getStyles() != null && !configLayer.getStyles().isEmpty()) {
            // @TODO: convert the data reference string to a mutable style
            // ${providerStyleType|providerStyleId|styleName}
            final List<org.geotoolkit.wms.xml.v111.Style> styles = new ArrayList<org.geotoolkit.wms.xml.v111.Style>();
            for (String styl : configLayer.getStyles()) {
                final MutableStyle ms;
                if (styl.startsWith("${")) {
                    final DataReference dr = new DataReference(styl);
                    Style style = null;
                    try {
                        style = DataReferenceConverter.convertDataReferenceToStyle(dr);
                    } catch (NonconvertibleObjectException e) {
                        // The given style reference was invalid, we can't get a style from that
                        LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
                    }
                    ms = StyleUtilities.copy(style);
                } else {
                    ms = StyleProviderProxy.getInstance().getByIdentifier(styl);
                }

                final org.geotoolkit.wms.xml.v111.Style wmsStyle = convertMutableStyleToWmsStyle111(ms, layerDetails, legendUrlPng, legendUrlGif);
                styles.add(wmsStyle);
            }
            if (!styles.isEmpty()) {
                outputLayer111.setStyle(styles);
            }
        }
        if (configLayer.getTitle() != null) {
            outputLayer111.setTitle(configLayer.getTitle());
        }
        if (configLayer.getAbstrac() != null) {
            outputLayer111.setAbstract(configLayer.getAbstrac());
        }
        if (configLayer.getKeywords() != null && !configLayer.getKeywords().isEmpty()) {
            final List<org.geotoolkit.wms.xml.v111.Keyword> keywords = new ArrayList<org.geotoolkit.wms.xml.v111.Keyword>();
            for (String kw : configLayer.getKeywords()) {
                keywords.add(new org.geotoolkit.wms.xml.v111.Keyword(kw));
            }
            outputLayer111.setKeywordList(new org.geotoolkit.wms.xml.v111.KeywordList(keywords));
        }
        if (configLayer.getMetadataURL() != null) {
            final FormatURL metadataURL = configLayer.getMetadataURL();
            outputLayer111.setMetadataURL(Arrays.asList(new org.geotoolkit.wms.xml.v111.MetadataURL(metadataURL.getFormat(),
                    metadataURL.getOnlineResource().getHref(),
                    metadataURL.getType())));
        }
        if (configLayer.getDataURL() != null) {
            final FormatURL dataURL = configLayer.getDataURL();
            outputLayer111.setDataURL(Arrays.asList(new org.geotoolkit.wms.xml.v111.DataURL(dataURL.getFormat(),
                    dataURL.getOnlineResource().getHref())));
        }
        if (configLayer.getAuthorityURL() != null) {
            final FormatURL authorityURL = configLayer.getAuthorityURL();
            outputLayer111.setAuthorityURL(Arrays.asList(new org.geotoolkit.wms.xml.v111.AuthorityURL(authorityURL.getName(),
                    authorityURL.getOnlineResource().getHref())));
        }
        if (configLayer.getIdentifier() != null) {
            final Reference identifier = configLayer.getIdentifier();
            outputLayer111.setIdentifier(Arrays.asList(new org.geotoolkit.wms.xml.v111.Identifier(identifier.getValue(), identifier.getAuthority())));
        }
        if (configLayer.getAttribution() != null) {
            final AttributionType attribution = configLayer.getAttribution();
            final FormatURL fUrl = attribution.getLogoURL();
            final org.geotoolkit.wms.xml.v111.LogoURL logoUrl;
            if (fUrl != null) {
                logoUrl = new org.geotoolkit.wms.xml.v111.LogoURL(fUrl.getFormat(), fUrl.getOnlineResource().getHref(), fUrl.getWidth(), fUrl.getHeight());
            } else {
                logoUrl = null;
            }
            outputLayer111.setAttribution(new org.geotoolkit.wms.xml.v111.Attribution(attribution.getTitle(),
                    attribution.getOnlineResource().getHref(),
                    logoUrl));
        }
        if (configLayer.getOpaque() != null) {
            int opaque = 0;
            if (configLayer.getOpaque()) {
                opaque = 1;
            }
            outputLayer111.setOpaque(opaque);
        }
        if (!configLayer.getCrs().isEmpty()) {
            outputLayer111.setSrs(configLayer.getCrs());
        }
        return outputLayer111;
    }

    private org.geotoolkit.wms.xml.v111.Style convertMutableStyleToWmsStyle111(final MutableStyle ms, final LayerDetails layerDetails,
            final String legendUrlPng, final String legendUrlGif)
    {
        if (layerDetails == null) {
            return null;
        }
        org.geotoolkit.wms.xml.v111.OnlineResource or = new org.geotoolkit.wms.xml.v111.OnlineResource(legendUrlPng);
        final LegendTemplate lt = mapDecoration.getDefaultLegendTemplate();
        final Dimension dimension;
        try {
            dimension = layerDetails.getPreferredLegendSize(lt, ms);
        } catch (PortrayalException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            return null;
        }

        final org.geotoolkit.wms.xml.v111.LegendURL legendURL1 =
                new org.geotoolkit.wms.xml.v111.LegendURL(MimeType.IMAGE_PNG, or,
                dimension.width, dimension.height);

        or = new org.geotoolkit.wms.xml.v111.OnlineResource(legendUrlGif);
        final org.geotoolkit.wms.xml.v111.LegendURL legendURL2 =
                new org.geotoolkit.wms.xml.v111.LegendURL(MimeType.IMAGE_GIF, or,
                dimension.width, dimension.height);

        String styleName = ms.getName();
        if (!styleName.isEmpty() && styleName.startsWith("${")) {
            final DataReference dataRef = new DataReference(styleName);
            styleName = dataRef.getLayerId().getLocalPart();
        }
        return new org.geotoolkit.wms.xml.v111.Style(
                styleName, styleName, null, null, null, legendURL1, legendURL2);
    }

    /**
     * Apply the layer customization extracted from the configuration.
     *
     * @param outputLayer130
     * @param configLayer
     * @return
     */
    private org.geotoolkit.wms.xml.v130.Layer customizeLayer130(final org.geotoolkit.wms.xml.v130.Layer outputLayer130, final Layer configLayer,
            final LayerDetails layerDetails, final String legendUrlPng, final String legendUrlGif)
    {
        if (configLayer == null) {
            return outputLayer130;
        }
        if (configLayer.getStyles() != null && !configLayer.getStyles().isEmpty()) {
            // @TODO: convert the data reference string to a mutable style
            // ${providerStyleType|providerStyleId|styleName}
            final List<org.geotoolkit.wms.xml.v130.Style> styles = new ArrayList<org.geotoolkit.wms.xml.v130.Style>();
            for (String styl : configLayer.getStyles()) {
                final MutableStyle ms;
                if (styl.startsWith("${")) {
                    final DataReference dr = new DataReference(styl);
                    Style style = null;
                    try {
                        style = DataReferenceConverter.convertDataReferenceToStyle(dr);
                    } catch (NonconvertibleObjectException e) {
                        // The given style reference was invalid, we can't get a style from that
                        LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
                    }
                    ms = StyleUtilities.copy(style);
                } else {
                    ms = StyleProviderProxy.getInstance().getByIdentifier(styl);
                }

                final org.geotoolkit.wms.xml.v130.Style wmsStyle = convertMutableStyleToWmsStyle130(ms, layerDetails, legendUrlPng, legendUrlGif);
                styles.add(wmsStyle);
            }
            if (!styles.isEmpty()) {
                outputLayer130.setStyle(styles);
            }
        }
        if (configLayer.getTitle() != null) {
            outputLayer130.setTitle(configLayer.getTitle());
        }
        if (configLayer.getAbstrac() != null) {
            outputLayer130.setAbstract(configLayer.getAbstrac());
        }
        if (configLayer.getKeywords() != null && !configLayer.getKeywords().isEmpty()) {
            final List<Keyword> keywords = new ArrayList<Keyword>();
            for (String kw : configLayer.getKeywords()) {
                keywords.add(new Keyword(kw));
            }
            outputLayer130.setKeywordList(new KeywordList(keywords));
        }
        if (configLayer.getMetadataURL() != null) {
            final FormatURL metadataURL = configLayer.getMetadataURL();
            outputLayer130.setMetadataURL(Arrays.asList(new MetadataURL(metadataURL.getFormat(),
                    metadataURL.getOnlineResource().getHref(),
                    metadataURL.getType())));
        }
        if (configLayer.getDataURL() != null) {
            final FormatURL dataURL = configLayer.getDataURL();
            outputLayer130.setDataURL(Arrays.asList(new DataURL(dataURL.getFormat(),
                    dataURL.getOnlineResource().getHref())));
        }
        if (configLayer.getAuthorityURL() != null) {
            final FormatURL authorityURL = configLayer.getAuthorityURL();
            outputLayer130.setAuthorityURL(Arrays.asList(new AuthorityURL(authorityURL.getName(),
                    authorityURL.getOnlineResource().getHref())));
        }
        if (configLayer.getIdentifier() != null) {
            final Reference identifier = configLayer.getIdentifier();
            outputLayer130.setIdentifier(Arrays.asList(new Identifier(identifier.getValue(), identifier.getAuthority())));
        }
        if (configLayer.getAttribution() != null) {
            final AttributionType attribution = configLayer.getAttribution();
            final FormatURL fUrl = attribution.getLogoURL();
            final LogoURL logoUrl;
            if (fUrl != null) {
                logoUrl = new LogoURL(fUrl.getFormat(), fUrl.getOnlineResource().getHref(), fUrl.getWidth(), fUrl.getHeight());
            } else {
                logoUrl = null;
            }
            outputLayer130.setAttribution(new Attribution(attribution.getTitle(),
                    attribution.getOnlineResource().getHref(),
                    logoUrl));
        }
        if (configLayer.getOpaque() != null) {
            int opaque = 0;
            if (configLayer.getOpaque()) {
                opaque = 1;
            }
            outputLayer130.setOpaque(opaque);
        }
        if (!configLayer.getCrs().isEmpty()) {
            outputLayer130.setCrs(configLayer.getCrs());
        }
        return outputLayer130;
    }

    private org.geotoolkit.wms.xml.v130.Style convertMutableStyleToWmsStyle130(final MutableStyle ms, final LayerDetails layerDetails,
            final String legendUrlPng, final String legendUrlGif)
    {
        if (layerDetails == null) {
            return null;
        }
        org.geotoolkit.wms.xml.v130.OnlineResource or = new org.geotoolkit.wms.xml.v130.OnlineResource(legendUrlPng);
        final LegendTemplate lt = mapDecoration.getDefaultLegendTemplate();
        final Dimension dimension;
        try {
            dimension = layerDetails.getPreferredLegendSize(lt, ms);
        } catch (PortrayalException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            return null;
        }

        final org.geotoolkit.wms.xml.v130.LegendURL legendURL1 =
                new org.geotoolkit.wms.xml.v130.LegendURL(MimeType.IMAGE_PNG, or,
                dimension.width, dimension.height);

        or = new org.geotoolkit.wms.xml.v130.OnlineResource(legendUrlGif);
        final org.geotoolkit.wms.xml.v130.LegendURL legendURL2 =
                new org.geotoolkit.wms.xml.v130.LegendURL(MimeType.IMAGE_GIF, or,
                dimension.width, dimension.height);

        String styleName = ms.getName();
        if (!styleName.isEmpty() && styleName.startsWith("${")) {
            final DataReference dataRef = new DataReference(styleName);
            styleName = dataRef.getLayerId().getLocalPart();
        }
        return new org.geotoolkit.wms.xml.v130.Style(
                styleName, styleName, null, null, null, legendURL1, legendURL2);
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
    public GetFeatureInfoVisitor getFeatureInfo(final GetFeatureInfo getFI) throws CstlServiceException {
        isWorking();
    	//
    	// Note this is almost the same logic as in getMap
    	//
        // 1. SCENE
        //       -- get the List of layer references
        final List<Name> layerNames = getFI.getQueryLayers();
        final List<LayerDetails> layerRefs = getLayerReferences(layerNames);

        for (LayerDetails layer : layerRefs) {
            if (!layer.isQueryable(ServiceDef.Query.WMS_GETINFO)) {
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
        params.put(WMSQuery.KEY_EXTRA_PARAMETERS, getFI.getParameters());
        final SceneDef sdef = new SceneDef();

        try {
            final MapContext context = PortrayalUtil.createContext(layerRefs, styles, params);
            sdef.setContext(context);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        // 2. VIEW
        Envelope refEnv;
        try {
            refEnv = GO2Utilities.combine(getFI.getEnvelope2D(), new Date[]{getFI.getTime(), getFI.getTime()}, new Double[]{getFI.getElevation(), getFI.getElevation()});
        } catch (TransformException ex) {
            throw new CstlServiceException(ex);
        }
        final double azimuth = getFI.getAzimuth();
        final ViewDef vdef   = new ViewDef(refEnv,azimuth);


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

        GetFeatureInfoVisitor visitor = null;
        for(final WMSVisitorFactory vf : VISITOR_FACTORIES){
            visitor = vf.createVisitor(getFI, layerRefs, infoFormat);
            if(visitor != null) break;
        }

        if(visitor == null) {
            throw new CstlServiceException("MIME type " + infoFormat + " is not accepted by the service.\n" +
                    "You have to choose between: "+ MimeType.TEXT_PLAIN +", "+ MimeType.TEXT_HTML +", "+ MimeType.APP_GML +", "+ GML +
                    ", "+ MimeType.APP_XML +", "+ XML+", "+ MimeType.TEXT_XML,
                    INVALID_FORMAT, KEY_INFO_FORMAT.toLowerCase());
        }

        final VisitDef visitDef = new VisitDef();
        visitDef.setArea(selectionArea);
        visitDef.setVisitor(visitor);

        try {
            //force longitude first
            vdef.setLongitudeFirst();
        } catch (TransformException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        } catch (FactoryException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }


        // We now build the response, according to the format chosen.
        try {
            Cstl.getPortrayalService().visit(sdef,vdef,cdef,visitDef);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        return visitor;
    }

    /**
     * Return the legend graphic for the current layer.
     * <p>If no width or height have been specified, a default output
     * size is adopted, the size will depend on the symbolizer used.</p>
     *
     * @param getLegend The {@linkplain GetLegendGraphic get legend graphic} request.
     * @return a file containing the legend graphic image.
     *
     * @throws CstlServiceException if the layer does not support GetLegendGraphic requests.
     */
    @Override
    public PortrayalResponse getLegendGraphic(final GetLegendGraphic getLegend) throws CstlServiceException {
        isWorking();
        final LayerDetails layer = getLayerReference(getLegend.getLayer());
        final String layerName = layer.getName().toString();
        if (!layer.isQueryable(ServiceDef.Query.WMS_ALL)) {
            throw new CstlServiceException("You are not allowed to request the layer \""+
                    layerName +"\".", LAYER_NOT_QUERYABLE, KEY_LAYER.toLowerCase());
        }
        final Integer width  = getLegend.getWidth();
        final Integer height = getLegend.getHeight();

        final Dimension dims;
        if (width != null && height != null) {
            dims = new Dimension(width, height);
        } else {
            //layers will calculate the best size
            dims = null;
        }
        final BufferedImage image;
        final String rule = getLegend.getRule();
        final Double scale = getLegend.getScale();
        final String sld = getLegend.getSld();
        try {
            MutableStyle ms = null;
            // If a sld file is given, extracts the style from it.
            if (sld != null && !sld.isEmpty()) {
                final XMLUtilities utils = new XMLUtilities();
                final MutableStyledLayerDescriptor mutableSLD;

                try {
                    mutableSLD = utils.readSLD(new URL(sld), getLegend.getSldVersion());
                } catch (JAXBException ex) {
                    final String message;
                    if (ex.getLinkedException() instanceof FileNotFoundException) {
                        message = "The given url \""+ sld +"\" points to an non-existing file.";
                    } else {
                        message = ex.getLocalizedMessage();
                    }
                    throw new PortrayalException(message, ex);
                } catch (FactoryException ex) {
                    throw new PortrayalException(ex);
                } catch (MalformedURLException ex) {
                    throw new PortrayalException("The given SLD url \""+ sld +"\" is not a valid url", ex);
                }

                final List<MutableLayer> emptyNameMutableLayers = new ArrayList<MutableLayer>();
                for (final MutableLayer mutableLayer : mutableSLD.layers()) {
                    final String mutableLayerName = mutableLayer.getName();
                    if (mutableLayerName == null || mutableLayerName.isEmpty()) {
                        emptyNameMutableLayers.add(mutableLayer);
                        continue;
                    }
                    if (layerName.equals(mutableLayerName)) {
                        ms = (MutableStyle) mutableLayer.styles().get(0);
                        break;
                    }
                }
                if (ms == null) {
                    LOGGER.log(Level.INFO, "No layer {0} found for the given SLD. Continue with the first style found.", layerName);
                    ms = (MutableStyle) emptyNameMutableLayers.get(0).styles().get(0);
                }
            } else {
                // No sld given, we use the style.
                final Map<Name,Layer> layers = getLayers();
                final Layer layerRef = layers.get(layer.getName());

                final List<String> defaultStyleRefs = layerRef.getStyles();
                if (defaultStyleRefs != null && !defaultStyleRefs.isEmpty()) {
                    final String styleId = defaultStyleRefs.get(0);
                    if (styleId.startsWith("${")) {
                        final DataReference styleRef = new DataReference(styleId);
                        ms = (styleRef == null || styleRef.getLayerId() == null) ? null :
                                StyleProviderProxy.getInstance().get(styleRef.getLayerId().getLocalPart());
                    } else {
                        ms = StyleProviderProxy.getInstance().getByIdentifier(styleId);
                    }
                } else {
                    ms = null;
                }
            }
            image = layer.getLegendGraphic(dims, mapDecoration.getDefaultLegendTemplate(), ms, rule, scale);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex);
        }
        if (image == null) {
            throw new CstlServiceException("The requested layer \""+ layerName +"\" does not support "
                    + "GetLegendGraphic request", NO_APPLICABLE_CODE, KEY_LAYER.toLowerCase());
        }
        return new PortrayalResponse(image);
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
    public PortrayalResponse getMap(final GetMap getMap) throws CstlServiceException {
        isWorking();
    	//
    	// Note this is almost the same logic as in getFeatureInfo
    	//
        final String errorType = getMap.getExceptionFormat();
        final boolean errorInImage = EXCEPTIONS_INIMAGE.equalsIgnoreCase(errorType);


        // 1. SCENE
        //       -- get the List of layer references
        final List<Name> layerNames = getMap.getLayers();
        final List<LayerDetails> layerRefs;
        try{
            layerRefs = getLayerReferences(layerNames);
        } catch (CstlServiceException ex) {
        	//TODO: distinguish
            if (errorInImage) {
                return new PortrayalResponse(Cstl.getPortrayalService().writeInImage(ex, getMap.getSize()));
            } else {
                throw new CstlServiceException(ex, LAYER_NOT_DEFINED, KEY_LAYERS.toLowerCase());
            }
        }
        for (LayerDetails layer : layerRefs) {
            if (!layer.isQueryable(ServiceDef.Query.WMS_ALL)) {
                throw new CstlServiceException("You are not allowed to request the layer \""+
                        layer.getName() +"\".", LAYER_NOT_QUERYABLE, KEY_LAYERS.toLowerCase());
            }
        }
        //       -- build an equivalent style List
        //TODO: clean up the SLD vs. style logic
        final List<String> styleNames = getMap.getStyles();
        final StyledLayerDescriptor sld = getMap.getSld();

        final List<MutableStyle> styles = getStyles(layerRefs, sld, styleNames);
        //       -- create the rendering parameter Map
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(WMSQuery.KEY_EXTRA_PARAMETERS, getMap.getParameters());
        final SceneDef sdef = new SceneDef();
        sdef.extensions().add(mapDecoration.getExtension());
        final Hints hints = mapDecoration.getHints();
        if (hints != null) {
            sdef.getHints().putAll(hints);
        }

        try {
            final MapContext context = MapBuilder.createContext();
            final Map<Name,Layer> layersContext = getLayers();

            for (int i = 0; i < layerRefs.size(); i++) {
                final LayerDetails layerRef = layerRefs.get(i);
                final MutableStyle style = styles.get(i);

                assert (null != layerRef);
                //style can be null

                final MapItem mapLayer = layerRef.getMapLayer(style, params);
                if (mapLayer == null) {
                    throw new PortrayalException("Could not create a mapLayer for layer: " + layerRef.getName());
                }
                if(mapLayer instanceof MapLayer){
                    ((MapLayer)mapLayer).setSelectable(true);
                }
                if (mapLayer instanceof FeatureMapLayer) {
                    final FeatureMapLayer fml = (FeatureMapLayer)mapLayer;
                    if (fml != null) {
                        final Layer layerContext = layersContext.get(layerRef.getName());
                        if (layerContext.getFilter() != null) {
                            final XMLUtilities xmlUtil = new XMLUtilities();
                            Filter filterGt = Filter.INCLUDE;
                            try {
                                filterGt = xmlUtil.getTransformer110().visitFilter(layerContext.getFilter());
                            } catch (FactoryException e) {
                                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
                            }

                            fml.setQuery(QueryBuilder.filtered(fml.getCollection().getFeatureType().getName(), filterGt));
                        }
                    }
                }
                mapLayer.setVisible(true);
                context.items().add(mapLayer);
            }

            sdef.setContext(context);
        } catch (PortrayalException ex) {
            if (errorInImage) {
                return new PortrayalResponse(Cstl.getPortrayalService().writeInImage(ex, getMap.getSize()));
            } else {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
        }


        // 2. VIEW
        final Envelope refEnv;
        try {
            if (getMap.getEnvelope2D().getLowerCorner().getOrdinate(0) > getMap.getEnvelope2D().getUpperCorner().getOrdinate(0) ||
                getMap.getEnvelope2D().getLowerCorner().getOrdinate(1) > getMap.getEnvelope2D().getUpperCorner().getOrdinate(1)) {
                throw new CstlServiceException("BBOX parameter minimum is greater than the maximum", INVALID_PARAMETER_VALUE, KEY_BBOX.toLowerCase());
            }
            refEnv = GO2Utilities.combine(getMap.getEnvelope2D(), new Date[]{getMap.getTime(), getMap.getTime()}, new Double[]{getMap.getElevation(), getMap.getElevation()});
        } catch (TransformException ex) {
            throw new CstlServiceException(ex);
        }


        final double azimuth = getMap.getAzimuth();
        final ViewDef vdef = new ViewDef(refEnv,azimuth);


        // 3. CANVAS
        final Dimension canvasDimension = getMap.getSize();
        final Color background;
        if (getMap.getTransparent() && !MimeType.IMAGE_JPEG.equalsIgnoreCase(getMap.getFormat())) {
            background = null;
        } else {
            final Color color = getMap.getBackground();
            background = (color == null) ? Color.WHITE : color;
        }
        final CanvasDef cdef = new CanvasDef(canvasDimension,background);

        // 4. IMAGE
        final String mime = getMap.getFormat();
        final OutputDef odef = mapDecoration.getOutputDef(mime);

        try {
            //force longitude first
            vdef.setLongitudeFirst();
        } catch (TransformException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        } catch (FactoryException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        final PortrayalResponse response = new PortrayalResponse(cdef, sdef, vdef, odef);

        if(!mapDecoration.writeInStream()){
            try {
                response.prepareNow();
            } catch (PortrayalException ex) {
                if (errorInImage) {
                    return new PortrayalResponse(Cstl.getPortrayalService().writeInImage(ex, getMap.getSize()));
                } else {
                    throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                }
            }
        }

        return response;
    }

    private static MutableStyle extractStyle(final Name layerName, final StyledLayerDescriptor sld){
        if(sld == null){
            throw new IllegalArgumentException("SLD should not be null");
        }

        final List<MutableNamedLayer> emptyNameSLDLayers = new ArrayList<MutableNamedLayer>();
        for(final org.opengis.sld.Layer sldLayer : sld.layers()){
            // We can't do anything if it is not a MutableNamedLayer.
            if (!(sldLayer instanceof MutableNamedLayer)) {
                continue;
            }
            final MutableNamedLayer mnl = (MutableNamedLayer) sldLayer;
            final String sldLayerName = mnl.getName();
            // We store this sld layer, for the case all styles defined in the sld would
            // be associated to no layer.
            if (sldLayerName == null || sldLayerName.isEmpty()) {
                emptyNameSLDLayers.add(mnl);
                continue;
            }
            // If it matches, then we return it.
            if (layerName.getLocalPart().equals(sldLayerName)) {
                for (final MutableLayerStyle mls : mnl.styles()) {
                    if (mls instanceof MutableNamedStyle) {
                        final MutableNamedStyle mns = (MutableNamedStyle) mls;
                        final String namedStyle = mns.getName();
                        return StyleProviderProxy.getInstance().get(namedStyle);
                    } else if (mls instanceof MutableStyle) {
                        return (MutableStyle) mls;
                    }

                }
            }
        }

        //no valid style found, returns the first one that do not specify a layer on which to apply.
        LOGGER.log(Level.INFO, "No layer {0} found for the styles defined in the given SLD file.", layerName);
        if (!emptyNameSLDLayers.isEmpty()) {
            LOGGER.info("Continue with the first style read in the SLD, that do not specify any layer on which to apply.");
            return (MutableStyle) ((MutableNamedLayer)sld.layers().get(0)).styles().get(0);
        }
        return null;
    }

    private List<MutableStyle> getStyles(final List<LayerDetails> layerRefs, final StyledLayerDescriptor sld,
                                         final List<String> styleNames) throws CstlServiceException {
        final List<MutableStyle> styles = new ArrayList<MutableStyle>();
        for (int i=0; i<layerRefs.size(); i++) {

            final MutableStyle style;
            if (sld != null) {
                //try to use the provided SLD
                style = extractStyle(layerRefs.get(i).getName(), sld);
            } else if (styleNames != null && styleNames.size() > i &&
                       styleNames.get(i) != null && !styleNames.get(i).isEmpty()) {
                //try to grab the style if provided
                //a style has been given for this layer, try to use it
                final String namedStyle = styleNames.get(i);
                style = StyleProviderProxy.getInstance().get(namedStyle);
                if (style == null) {
                    throw new CstlServiceException("Style provided not found.", STYLE_NOT_DEFINED);
                }
            } else {
                //no defined styles, use the favorite one, let the layer get it himself.
                final Map<Name,Layer> layers = getLayers();
                final Layer layer = layers.get(layerRefs.get(i).getName());

                final List<String> defaultStyleRefs = layer.getStyles();
                if (defaultStyleRefs != null && !defaultStyleRefs.isEmpty()) {
                    final String styleId = defaultStyleRefs.get(0);
                    if (styleId.startsWith("${")) {
                        final DataReference styleRef = new DataReference(styleId);
                        style = (styleRef == null || styleRef.getLayerId() == null) ? null :
                                StyleProviderProxy.getInstance().get(styleRef.getLayerId().getLocalPart());
                    } else {
                        style = StyleProviderProxy.getInstance().getByIdentifier(styleId);
                    }
                } else {
                    style = null;
                }
            }
            styles.add(style);
        }
        return styles;
    }

    /**
     * Overriden from AbstractWorker because the behaviour is different when the request updateSequence
     * is equal to the current.
     *
     * @param updateSequence
     * @return
     * @throws CstlServiceException
     */
    @Override
    protected boolean returnUpdateSequenceDocument(final String updateSequence) throws CstlServiceException {
        if (updateSequence == null) {
            return false;
        }
        try {
            final long sequenceNumber = Long.parseLong(updateSequence);
            final long currentUpdateSequence = Long.parseLong(getCurrentUpdateSequence());
            if (sequenceNumber == currentUpdateSequence) {
                throw new CstlServiceException("The update sequence parameter is equal to the current", CURRENT_UPDATE_SEQUENCE, "updateSequence");
            } else if (sequenceNumber > currentUpdateSequence) {
                throw new CstlServiceException("The update sequence parameter is invalid (higher value than the current)", INVALID_UPDATE_SEQUENCE, "updateSequence");
            }
            return false;
        } catch(NumberFormatException ex) {
            throw new CstlServiceException("The update sequence must be an integer", ex, INVALID_PARAMETER_VALUE, "updateSequence");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        if (!CAPS_RESPONSE.isEmpty()) {
            CAPS_RESPONSE.clear();
        }
    }
}
