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
package org.constellation.map.ws;

//J2SE dependencies
import static org.constellation.api.CommonConstants.DEFAULT_CRS;
import static org.constellation.map.ws.WMSConstant.EXCEPTION_111_BLANK;
import static org.constellation.map.ws.WMSConstant.EXCEPTION_111_INIMAGE;
import static org.constellation.map.ws.WMSConstant.EXCEPTION_130_BLANK;
import static org.constellation.map.ws.WMSConstant.EXCEPTION_130_INIMAGE;
import static org.constellation.query.wms.WMSQuery.KEY_BBOX;
import static org.constellation.query.wms.WMSQuery.KEY_INFO_FORMAT;
import static org.constellation.query.wms.WMSQuery.KEY_LAYER;
import static org.constellation.query.wms.WMSQuery.KEY_LAYERS;
import static org.geotoolkit.ows.xml.OWSExceptionCode.CURRENT_UPDATE_SEQUENCE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_FORMAT;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_POINT;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_UPDATE_SEQUENCE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.LAYER_NOT_DEFINED;
import static org.geotoolkit.ows.xml.OWSExceptionCode.LAYER_NOT_QUERYABLE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.STYLE_NOT_DEFINED;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createBoundingBox;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createDimension;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createGeographicBoundingBox;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createLayer;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createLegendURL;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createLogoURL;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createOnlineResource;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createStyle;
import groovy.transform.TimedInterrupt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;
import javax.xml.bind.JAXBException;

import org.apache.sis.internal.util.UnmodifiableArrayList;
import org.apache.sis.measure.MeasurementRange;
import org.apache.sis.measure.Range;
import org.apache.sis.referencing.crs.DefaultEngineeringCRS;
import org.apache.sis.referencing.cs.AbstractCS;
import org.apache.sis.referencing.datum.AbstractDatum;
import org.apache.sis.referencing.datum.DefaultEngineeringDatum;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.xml.MarshallerPool;
//Constellation dependencies
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.*;
import org.constellation.converter.DataReferenceConverter;
import org.constellation.dto.Service;
import org.constellation.map.featureinfo.FeatureInfoFormat;
import org.constellation.map.featureinfo.FeatureInfoUtilities;
import org.constellation.portrayal.PortrayalUtil;
import org.constellation.portrayal.internal.PortrayalResponse;
import org.constellation.provider.CoverageData;
import org.constellation.provider.Data;
import org.constellation.query.wms.WMSQuery;
import org.constellation.util.DataReference;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.LayerWorker;
import org.constellation.ws.MimeType;
//Geotoolkit dependencies
import org.geotoolkit.cql.CQL;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.inspire.xml.vs.ExtendedCapabilitiesType;
import org.geotoolkit.inspire.xml.vs.LanguageType;
import org.geotoolkit.inspire.xml.vs.LanguagesType;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.referencing.ReferencingUtilities;
import org.geotoolkit.referencing.crs.DefaultTemporalCRS;
import org.geotoolkit.referencing.crs.DefaultVerticalCRS;
import org.geotoolkit.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotoolkit.referencing.cs.DiscreteCoordinateSystemAxis;
import org.geotoolkit.se.xml.v110.OnlineResourceType;
import org.geotoolkit.sld.MutableLayer;
import org.geotoolkit.sld.MutableLayerStyle;
import org.geotoolkit.sld.MutableNamedLayer;
import org.geotoolkit.sld.MutableNamedStyle;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.xml.GetLegendGraphic;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.sld.xml.v110.DescribeLayerResponseType;
import org.geotoolkit.sld.xml.v110.LayerDescriptionType;
import org.geotoolkit.sld.xml.v110.TypeNameType;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.StyleUtilities;
import org.geotoolkit.util.PeriodUtilities;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.wms.xml.AbstractBoundingBox;
import org.geotoolkit.wms.xml.AbstractDimension;
import org.geotoolkit.wms.xml.AbstractGeographicBoundingBox;
import org.geotoolkit.wms.xml.AbstractLayer;
import org.geotoolkit.wms.xml.AbstractLegendURL;
import org.geotoolkit.wms.xml.AbstractLogoURL;
import org.geotoolkit.wms.xml.AbstractOnlineResource;
import org.geotoolkit.wms.xml.AbstractRequest;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.DescribeLayer;
import org.geotoolkit.wms.xml.GetCapabilities;
import org.geotoolkit.wms.xml.GetFeatureInfo;
import org.geotoolkit.wms.xml.GetMap;
import org.geotoolkit.wms.xml.WMSMarshallerPool;
import org.geotoolkit.wms.xml.v111.LatLonBoundingBox;
import org.geotoolkit.wms.xml.v130.Capability;
//Geoapi dependencies
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.referencing.operation.TransformException;
import org.opengis.sld.StyledLayerDescriptor;
import org.opengis.style.Style;
import org.opengis.util.FactoryException;
import org.springframework.context.annotation.Scope;

import com.codahale.metrics.annotation.Timed;

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

@Named
@Scope("prototype")
public class DefaultWMSWorker extends LayerWorker implements WMSWorker {


    /**
     * AxisDirection name for Lat/Long, Elevation, temporal dimensions.
     */
    private static final List<String> COMMONS_DIM = UnmodifiableArrayList.wrap(new String[] {
            "NORTH", "EAST", "SOUTH", "WEST",
            "UP", "DOWN",
            "FUTURE", "PAST"});

    /**
     * Only Elevation dimension.
     */
    private static final List<String> VERTICAL_DIM = UnmodifiableArrayList.wrap(new String[] {"UP", "DOWN"});

    /**
     * List of FeatureInfo mimeTypes
     */
    private final List<String> GFI_MIME_TYPES = new ArrayList<String>();

    private WMSPortrayal mapPortrayal;
    
    public static class Factory {
        public static DefaultWMSWorker create(String id) {
            return new DefaultWMSWorker(id);
        }
    }

    public DefaultWMSWorker(final String id) {
        super(id, ServiceDef.Specification.WMS);

        //get all supported GetFeatureInfo mimetypes
        try {
            GFI_MIME_TYPES.clear();
            final LayerContext config = (LayerContext)getConfiguration();
            GFI_MIME_TYPES.addAll(FeatureInfoUtilities.allSupportedMimeTypes(config));
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        mapPortrayal = new WMSPortrayal();
        try {
            mapPortrayal = (WMSPortrayal) ConfigurationEngine.getConfiguration("WMS", id, "WMSPortrayal.xml");
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (FileNotFoundException ex) {
            // the file can be absent
        }

        if (isStarted) {
            LOGGER.log(Level.INFO, "WMS worker {0} running", id);
        }
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
     * @return a description of layers specified in the user's request.
     *
     * @throws CstlServiceException
     */
    @Override
    public DescribeLayerResponseType describeLayer(final DescribeLayer descLayer) throws CstlServiceException {
        final OnlineResourceType or = new OnlineResourceType(getServiceUrl());

        final List<LayerDescriptionType> layerDescriptions = new ArrayList<>();
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
    @Timed
    public AbstractWMSCapabilities getCapabilities(final GetCapabilities getCapab) throws CstlServiceException {
        isWorking();
        final String queryVersion      = getCapab.getVersion().toString();
        final String requestedLanguage = getCapab.getLanguage();
        final String userLogin         = getUserLogin();
        // we get the request language, if its not set we get the default "eng"
        final String currentLanguage;
        if (requestedLanguage != null && supportedLanguages.contains(requestedLanguage) && !requestedLanguage.equals(defaultLanguage)) {
            currentLanguage = requestedLanguage;
        } else {
            currentLanguage = null;
        }

        final Object cachedCapabilities = getCapabilitiesFromCache(queryVersion, currentLanguage);
        if (cachedCapabilities != null) {
            return (AbstractWMSCapabilities) cachedCapabilities;
        }

        final Service skeleton = getStaticCapabilitiesObject("WMS", requestedLanguage);
        final AbstractWMSCapabilities inCapabilities = WMSConstant.createCapabilities(queryVersion, skeleton);

        final AbstractRequest request;
        final List<String> exceptionFormats;
        if (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) {
            request          = WMSConstant.createRequest111(GFI_MIME_TYPES).clone();
            exceptionFormats = WMSConstant.EXCEPTION_111;
        } else {
            request          = WMSConstant.createRequest130(GFI_MIME_TYPES).clone();
            exceptionFormats = WMSConstant.EXCEPTION_130;
        }
        request.updateURL(getServiceUrl());
        inCapabilities.getCapability().setRequest(request);
        inCapabilities.getCapability().setExceptionFormats(exceptionFormats);

        //set the current updateSequence parameter
        final boolean returnUS = returnUpdateSequenceDocument(getCapab.getUpdateSequence());
        if (returnUS) {
            throw new CstlServiceException("the update sequence parameter is equal to the current", CURRENT_UPDATE_SEQUENCE, "updateSequence");
        }

        //Build the list of layers
        final List<AbstractLayer> outputLayers = new ArrayList<>();
        final List<Layer> layers = getConfigurationLayers(userLogin);

       for (Layer configLayer : layers) {
            final Data layer = getLayerReference(userLogin, configLayer.getName());

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
            final List<AbstractDimension> dimensions = new ArrayList<>();

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
                dim = createDimension(queryVersion, "time", "ISO8601", defaut, null);
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
                dim = createDimension(queryVersion, "elevation", "EPSG:5030", defaut, null);
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
                final double minRange = firstRange.getMinDouble();
                final double maxRange = firstRange.getMaxDouble();
                final String defaut = minRange + "," + maxRange;
                final Unit<?> u = firstRange.unit();
                final String unit = (u != null) ? u.toString() : null;
                String unitSymbol;
                try {
                    unitSymbol = UnitFormat.getInstance().format(u);
                } catch (IllegalArgumentException e) {
                    // Workaround for one more bug in javax.measure...
                    unitSymbol = unit;
                }
                dim = createDimension(queryVersion, minRange + "," + maxRange, "dim_range", unit,unitSymbol, defaut, null, null, null);
                dimensions.add(dim);
            }

            /*
             * Create dimensions using CRS of the layer native envelope
             */
            Envelope nativeEnv;
            try {
                nativeEnv = layer.getEnvelope();
            } catch (DataStoreException exception) {
                throw new CstlServiceException(exception, NO_APPLICABLE_CODE);
            }

            if (nativeEnv != null && nativeEnv.getCoordinateReferenceSystem() != null) {
                final CoordinateReferenceSystem crs = nativeEnv.getCoordinateReferenceSystem();
                final CoordinateSystem cs = crs.getCoordinateSystem();

                final int nbDim = cs.getDimension();

                for (int i = 0; i < nbDim; i++) {
                    final CoordinateSystemAxis axis = cs.getAxis(i);
                    final AxisDirection direction = axis.getDirection();
                    final Unit axisUnit = axis.getUnit();

                    final String directionName = direction.name();

                    boolean addDimension = false;

                    //valid axis if a common dimensions
                    if (!COMMONS_DIM.contains(directionName)) {
                        addDimension = true;

                        //or a vertical direction without axis length unit.
                    } else if (VERTICAL_DIM.contains(directionName) && axisUnit != null && !axisUnit.isCompatible(SI.METRE)) {
                        addDimension = true;
                    }

                    if (addDimension) {
                        final org.opengis.metadata.Identifier axisName = axis.getName();

                        final String unit = (axisUnit != null) ? axisUnit.toString() : null;
                        String unitSymbol;
                        try {
                            unitSymbol = UnitFormat.getInstance().format(axisUnit);
                        } catch (IllegalArgumentException e) {
                            // Workaround for one more bug in javax.measure...
                            unitSymbol = unit;
                        }

                        final LinkedList<String> valuesList = new LinkedList<>();
                        if (axis instanceof DiscreteCoordinateSystemAxis) {
                            final DiscreteCoordinateSystemAxis direcretAxis = (DiscreteCoordinateSystemAxis) axis;
                            final int nbOrdiante = direcretAxis.length();
                            for (int j = 0; j < nbOrdiante; j++) {
                                valuesList.add(direcretAxis.getOrdinateAt(j).toString());
                            }
                        }

                        final StringBuilder values = new StringBuilder();
                        int index = 0;
                        for (final String val : valuesList) {
                            values.append(val);
                            if (index++ < valuesList.size()-1) {
                                values.append(",");
                            }
                        }

                        final String defaut = !valuesList.isEmpty() ? valuesList.getFirst() : null;
                        final boolean multipleValues = (valuesList.size() > 1);

                        dim = createDimension(queryVersion, values.toString(), axisName.getCode(), unit,
                                unitSymbol, defaut, multipleValues, null, null);

                        dimensions.add(dim);
                    }
                }
            }

            // Verify extra dimensions
            if(!configLayer.getDimensions().isEmpty()){
                try {
                    final MapItem mi = layer.getMapLayer(null, null);
                    applyLayerFiltersAndDims(mi, userLogin);

                    if (mi instanceof MapContext) {
                        final MapContext mc = (MapContext)mi;
                        final List<AbstractDimension> dimensionsToAdd = new ArrayList<>();
                        for (final MapLayer candidateLayer : mc.layers()) {
                            if (candidateLayer instanceof FeatureMapLayer) {
                                final FeatureMapLayer fml = (FeatureMapLayer)candidateLayer;
                                final List<AbstractDimension> extraDimsToAdd = getExtraDimensions(fml, queryVersion);
                                for (AbstractDimension newExtraDim : extraDimsToAdd) {
                                    boolean exist = false;
                                    for (AbstractDimension oldExtraDim : dimensionsToAdd) {
                                        if (oldExtraDim.getName().equalsIgnoreCase(newExtraDim.getName())) {
                                            mergeValues(oldExtraDim, newExtraDim);
                                            exist = true;
                                            break;
                                        }
                                    }
                                    if (!exist) {
                                        dimensionsToAdd.add(newExtraDim);
                                    }
                                }
                            }
                        }

                        if (!dimensionsToAdd.isEmpty()) {
                            dimensions.addAll(dimensionsToAdd);
                        }
                    }

                    if(mi instanceof FeatureMapLayer){
                        final FeatureMapLayer fml = (FeatureMapLayer) mi;
                        dimensions.addAll(getExtraDimensions(fml, queryVersion));
                    }

                } catch (PortrayalException | DataStoreException ex) {
                    Logger.getLogger(DefaultWMSWorker.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                    break;
                }
            }

            /*
             * LegendUrl generation
             * TODO: Use a StringBuilder or two
             */
            final Name fullLayerName = layer.getName();
            final String layerName;

            //Use layer alias from config if exist.
            if (configLayer.getAlias() != null && !configLayer.getAlias().isEmpty()) {
                layerName = configLayer.getAlias();
            } else {

                if (fullLayerName.getNamespaceURI() != null && !fullLayerName.getNamespaceURI().isEmpty()) {
                    layerName = fullLayerName.getNamespaceURI() + ':' + fullLayerName.getLocalPart();
                } else {
                    layerName = fullLayerName.getLocalPart();
                }
            }
            final String beginLegendUrl = getServiceUrl() + "REQUEST=GetLegendGraphic&VERSION=1.1.1&FORMAT=";
            final String legendUrlGif   = beginLegendUrl + MimeType.IMAGE_GIF + "&LAYER=" + layerName;
            final String legendUrlPng   = beginLegendUrl + MimeType.IMAGE_PNG + "&LAYER=" + layerName;
            final String queryable      = (layer.isQueryable(ServiceDef.Query.WMS_GETINFO)) ? "1" : "0";
            final String _abstract;
            final String keyword;
            if (layer instanceof CoverageData) {
                final CoverageData coverageLayer = (CoverageData)layer;
                _abstract = StringUtilities.cleanSpecialCharacter(coverageLayer.getRemarks());
                keyword   = StringUtilities.cleanSpecialCharacter(coverageLayer.getThematic());
            } else {
                _abstract = "Vector data";
                keyword   = "Vector data";
            }

            final AbstractBoundingBox outputBBox;
            if (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) {
                /*
                 * TODO
                 * Envelope inputBox = inputLayer.getCoverage().getEnvelope();
                 *
                 *
                 * do we have to use the same order as WMS 1.3.0 (SOUTH WEST NORTH EAST) ???
                 */
                outputBBox = createBoundingBox(queryVersion,
                            "EPSG:4326",
                            inputGeoBox.getWestBoundLongitude(),
                            inputGeoBox.getSouthBoundLatitude(),
                            inputGeoBox.getEastBoundLongitude(),
                            inputGeoBox.getNorthBoundLatitude(), 0.0, 0.0);
            } else {
                /*
                 * TODO
                 * Envelope inputBox = inputLayer.getCoverage().getEnvelope();
                 */
                outputBBox = createBoundingBox(queryVersion,
                            "EPSG:4326",
                            inputGeoBox.getSouthBoundLatitude(),
                            inputGeoBox.getWestBoundLongitude(),
                            inputGeoBox.getNorthBoundLatitude(),
                            inputGeoBox.getEastBoundLongitude(), 0.0, 0.0);
            }
            // we build a Style Object
            final List<DataReference> stylesName = configLayer.getStyles();
            final List<org.geotoolkit.wms.xml.Style> styles = new ArrayList<>();
            if (stylesName != null && !stylesName.isEmpty()) {
                // For each styles defined for the layer, get the dimension of the getLegendGraphic response.
                for (DataReference styleName : stylesName) {
                    final MutableStyle ms = getStyle(styleName);
                    final org.geotoolkit.wms.xml.Style style = convertMutableStyleToWmsStyle(queryVersion, ms, layer, legendUrlPng, legendUrlGif);
                    styles.add(style);
                }
            }
            final AbstractGeographicBoundingBox bbox = createGeographicBoundingBox(queryVersion, inputGeoBox);
            final AbstractLayer outputLayerO = createLayer(queryVersion, layerName,_abstract,keyword, DEFAULT_CRS,
                            bbox, outputBBox, queryable, dimensions, styles);

            final AbstractLayer outputLayer = customizeLayer(queryVersion, outputLayerO, configLayer, layer, legendUrlPng, legendUrlGif);
            outputLayers.add(outputLayer);
        }

        //we build the general layer and add it to the document
        final AbstractLayer mainLayer = customizeLayer(queryVersion, createLayer(queryVersion, "Constellation Web Map Layer",
                    "description of the service(need to be fill)", DEFAULT_CRS,
                    createGeographicBoundingBox(queryVersion, -180.0, -90.0, 180.0, 90.0), outputLayers), getMainLayer(), null, null, null);

        inCapabilities.getCapability().setLayer(mainLayer);


        /*
         * INSPIRE PART
         */
        if (queryVersion.equals(ServiceDef.WMS_1_3_0.version.toString()) || queryVersion.equals(ServiceDef.WMS_1_3_0_SLD.version.toString()) ) {

            final Capability capa = (Capability) inCapabilities.getCapability();
            final ExtendedCapabilitiesType inspireExtension =  capa.getInspireExtendedCapabilities();

            if (inspireExtension != null) {
                inspireExtension.setMetadataDate(new Date(System.currentTimeMillis()));

                List<LanguageType> languageList = new ArrayList<>();
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
        putCapabilitiesInCache(queryVersion, currentLanguage, inCapabilities);
        return inCapabilities;
    }

    private String sortValues(final String... vals) {
        final List<String> finalVals = new ArrayList<>();
        for (final String s : vals) {
            finalVals.add(s);
        }

        boolean isDoubleValues = false;
        List<Double> finalValsDouble = null;
        try {
            Double.valueOf(finalVals.get(0));
            // It is a double!
            isDoubleValues = true;
            finalValsDouble = new ArrayList<>();
            for (String s : finalVals) {
                finalValsDouble.add(Double.valueOf(s));
            }
        } catch (NumberFormatException ex) {
        }

        if (isDoubleValues) {
            Collections.sort(finalValsDouble);
            finalVals.clear();
            for (Double d : finalValsDouble) {
                finalVals.add(String.valueOf(d));
            }
        } else {
            Collections.sort(finalVals);
        }

        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String val : finalVals) {
            if (!first) {
                sb.append(",");
            }
            sb.append(val);
            first = false;
        }

        return sb.toString();
    }

    /**
     * Merge old and new values in the old dimension. Try to sort its values.
     *
     * @param oldExtraDim
     * @param newExtraDim
     */
    private void mergeValues(final AbstractDimension oldExtraDim, final AbstractDimension newExtraDim) {
        final Set<String> valsSet = new HashSet<>();
        final String oldVals = oldExtraDim.getValue();
        final String[] oldValsSplit = oldVals.split(",");
        for (final String o : oldValsSplit) {
            valsSet.add(o);
        }

        final String newVals = newExtraDim.getValue();
        final String[] newValsSplit = newVals.split(",");
        for (final String n : newValsSplit) {
            valsSet.add(n);
        }

        final List<String> finalVals = new ArrayList<>();
        finalVals.addAll(valsSet);

        if (finalVals.isEmpty()) {
            return;
        }

        final String finalValSorted = sortValues(finalVals.toArray(new String[0]));
        oldExtraDim.setValue(finalValSorted);
    }

    /**
     * Get extra dimensions from a {@link FeatureMapLayer}.
     *
     * @param fml {@link FeatureMapLayer}
     * @param queryVersion Version of the request.
     * @return A list of extra dimensions, never {@code null}
     * @throws DataStoreException
     */
    private List<AbstractDimension> getExtraDimensions(final FeatureMapLayer fml, final String queryVersion) throws DataStoreException {
        final List<AbstractDimension> dimensions = new ArrayList<>();
        for(FeatureMapLayer.DimensionDef ddef : fml.getExtraDimensions()){
            final Collection<Range> collRefs = fml.getDimensionRange(ddef);
            // Transform it to a set in order to filter same values
            final Set<Range> refs = new HashSet<>();
            for (Range ref : collRefs) {
                refs.add(ref);
            }

            if (refs.isEmpty()) {
                // Dimension applied on a layer which has no values: just skip this dimension
                continue;
            }

            final StringBuilder values = new StringBuilder();
            int index = 0;
            for (final Range val : refs) {
                values.append(val.getMinValue());
                if(val.getMinValue().compareTo(val.getMaxValue()) != 0){
                    values.append('-');
                    values.append(val.getMaxValue());
                }
                if (index++ < refs.size()-1) {
                    values.append(",");
                }
            }

            final String sortedValues = sortValues(values.toString().split(","));
            final boolean multipleValues = (refs.size() > 1);
            final String unitSymbol = ddef.getCrs().getCoordinateSystem().getAxis(0).getUnit().toString();
            final String unit = unitSymbol;
            final String axisName = ddef.getCrs().getCoordinateSystem().getAxis(0).getName().getCode();
            final String defaut = "";

            final AbstractDimension dim = (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) ?
                new org.geotoolkit.wms.xml.v111.Dimension(sortedValues, axisName, unit,
                    unitSymbol, defaut, multipleValues, null, null) :
                new org.geotoolkit.wms.xml.v130.Dimension(sortedValues, axisName, unit,
                    unitSymbol, defaut, multipleValues, null, null);

            dimensions.add(dim);
        }
        return dimensions;
    }

    /**
     * Apply the layer customization extracted from the configuration.
     *
     * @param version
     * @param outputLayer
     * @param configLayer
     * @param layerDetails
     * @param legendUrlPng
     * @param legendUrlGif
     * @return
     * @throws CstlServiceException
     */
    private AbstractLayer customizeLayer(final String version, final AbstractLayer outputLayer, final Layer configLayer,
            final Data layerDetails, final String legendUrlPng, final String legendUrlGif) throws CstlServiceException
    {
        if (configLayer == null) {
            return outputLayer;
        }
        if (configLayer.getStyles() != null && !configLayer.getStyles().isEmpty()) {
            // @TODO: convert the data reference string to a mutable style
            // ${providerStyleType|providerStyleId|styleName}
            final List<org.geotoolkit.wms.xml.Style> styles = new ArrayList<>();
            for (DataReference styl : configLayer.getStyles()) {
                final MutableStyle ms;
                Style style = null;
                try {
                    style = DataReferenceConverter.convertDataReferenceToStyle(styl);
                } catch (NonconvertibleObjectException e) {
                    // The given style reference was invalid, we can't get a style from that
                    LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
                }
                ms = StyleUtilities.copy(style);
                if (ms != null) {
                    styles.add(convertMutableStyleToWmsStyle(version, ms, layerDetails, legendUrlPng, legendUrlGif));
                }
            }
            if (!styles.isEmpty()) {
                outputLayer.updateStyle(styles);
            }
        }
        if (configLayer.getTitle() != null) {
            outputLayer.setTitle(configLayer.getTitle());
        }
        if (configLayer.getAbstrac() != null) {
            outputLayer.setAbstract(configLayer.getAbstrac());
        }
        if (configLayer.getKeywords() != null && !configLayer.getKeywords().isEmpty()) {
            outputLayer.setKeywordList(configLayer.getKeywords());
        }
        if (configLayer.getMetadataURL() != null) {
            final FormatURL metadataURL = configLayer.getMetadataURL();
            outputLayer.setMetadataURL(metadataURL.getFormat(),
                                          metadataURL.getOnlineResource().getHref(),
                                          metadataURL.getType());
        }
        if (configLayer.getDataURL() != null) {
            final FormatURL dataURL = configLayer.getDataURL();
            outputLayer.setDataURL(dataURL.getFormat(),
                                      dataURL.getOnlineResource().getHref());
        }
        if (configLayer.getAuthorityURL() != null) {
            final FormatURL authorityURL = configLayer.getAuthorityURL();
            outputLayer.setAuthorityURL(authorityURL.getName(),
                                           authorityURL.getOnlineResource().getHref());
        }
        if (configLayer.getIdentifier() != null) {
            final Reference identifier = configLayer.getIdentifier();
            outputLayer.setIdentifier(identifier.getAuthority(), identifier.getValue());
        }
        if (configLayer.getAttribution() != null) {
            final AttributionType attribution = configLayer.getAttribution();
            final FormatURL fUrl = attribution.getLogoURL();
            final AbstractLogoURL logoUrl;
            if (fUrl != null) {
                logoUrl = createLogoURL(version, fUrl.getFormat(), fUrl.getOnlineResource().getHref(), fUrl.getWidth(), fUrl.getHeight());
            } else {
                logoUrl = null;
            }
            outputLayer.setAttribution(attribution.getTitle(),
                                          attribution.getOnlineResource().getHref(),
                                          logoUrl);
        }
        if (configLayer.getOpaque() != null) {
            int opaque = 0;
            if (configLayer.getOpaque()) {
                opaque = 1;
            }
            outputLayer.setOpaque(opaque);
        }
        if (!configLayer.getCrs().isEmpty()) {
            outputLayer.setCrs(configLayer.getCrs());
        }
        return outputLayer;
    }


    /**
     *
     * @param currentVersion
     * @param ms
     * @param layerDetails
     * @param legendUrlPng
     * @param legendUrlGif
     * @return
     */
    private org.geotoolkit.wms.xml.Style convertMutableStyleToWmsStyle(final String currentVersion, final MutableStyle ms, final Data layerDetails,
            final String legendUrlPng, final String legendUrlGif)
    {
        if (layerDetails == null) {
            return null;
        }
        AbstractOnlineResource or = createOnlineResource(currentVersion, legendUrlPng);
        final LegendTemplate lt = mapPortrayal.getDefaultLegendTemplate();
        final Dimension dimension;
        try {
            dimension = layerDetails.getPreferredLegendSize(lt, ms);
        } catch (PortrayalException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            return null;
        }

        final AbstractLegendURL legendURL1 = createLegendURL(currentVersion, MimeType.IMAGE_PNG, or, dimension.width, dimension.height);

        or = createOnlineResource(currentVersion, legendUrlGif);
        final AbstractLegendURL legendURL2 = createLegendURL(currentVersion, MimeType.IMAGE_GIF, or, dimension.width, dimension.height);

        String styleName = ms.getName();
        if (styleName != null && !styleName.isEmpty() && styleName.startsWith("${")) {
            final DataReference dataRef = new DataReference(styleName);
            styleName = dataRef.getLayerId().getLocalPart();
        }
        return createStyle(currentVersion, styleName, styleName, null, legendURL1, legendURL2);
    }

    /**
     * Return the value of a point in a map.
     *
     * @param getFI The {@linkplain GetFeatureInfo get feature info} request.
     * @return text, HTML , XML or GML code.
     *
     * @throws CstlServiceException
     */
    @Override
    public Map.Entry<String, Object> getFeatureInfo(final GetFeatureInfo getFI) throws CstlServiceException {
        isWorking();
        //
        // Note this is almost the same logic as in getMap
        //
        // 1. SCENE
        //       -- get the List of layer references
        final String userLogin             = getUserLogin();
        final List<Name> layerNames        = getFI.getQueryLayers();
        final List<Data> layerRefs;
        final List<Layer> layerConfig;
        try{
            layerRefs = getLayerReferences(userLogin, layerNames);
            layerConfig = getConfigurationLayers(userLogin, layerNames);
        } catch (CstlServiceException ex) {
            throw new CstlServiceException(ex, LAYER_NOT_DEFINED, KEY_LAYERS.toLowerCase());
        }

        for (Data layer : layerRefs) {
            if (!layer.isQueryable(ServiceDef.Query.WMS_GETINFO)) {
                throw new CstlServiceException("You are not allowed to request the layer \""+
                        layer.getName() +"\".", LAYER_NOT_QUERYABLE, KEY_LAYERS.toLowerCase());
            }
        }
        //       -- build an equivalent style List
        //TODO: clean up the SLD vs. style logic
        final List<String> styleNames   = getFI.getStyles();
        final StyledLayerDescriptor sld = getFI.getSld();

        final List<MutableStyle> styles        = getStyles(layerConfig, sld, styleNames, userLogin);
        //       -- create the rendering parameter Map
        final Double elevation                 = getFI.getElevation();
        final List<Date> time                  = getFI.getTime();
        final Map<String, Object> params       = new HashMap<>();
        params.put(WMSQuery.KEY_ELEVATION, elevation);
        params.put(WMSQuery.KEY_TIME, time);
        params.put(WMSQuery.KEY_EXTRA_PARAMETERS, getFI.getParameters());
        final SceneDef sdef = new SceneDef();

        try {
            final MapContext context = PortrayalUtil.createContext(layerRefs, styles, params);
            sdef.setContext(context);
            //apply layercontext filters
            applyLayerFiltersAndDims(context, userLogin);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        // 2. VIEW
        Envelope refEnv;
        try {
            final Date[] dates = new Date[2];
            if (time != null && !time.isEmpty()) {
                dates[0] = time.get(0);
                dates[1] = time.get(time.size()-1);
            }
            refEnv = ReferencingUtilities.combine(getFI.getEnvelope2D(), dates, new Double[]{getFI.getElevation(), getFI.getElevation()});
        } catch (TransformException ex) {
            throw new CstlServiceException(ex);
        }
        final double azimuth = getFI.getAzimuth();
        final ViewDef vdef   = new ViewDef(refEnv,azimuth);
        try {
            //force longitude first
            vdef.setLongitudeFirst();
        } catch (TransformException | FactoryException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

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

        //search custom FeatureInfoFormat
        Layer config = null;
        if (layerRefs.size() == 1) {
            config = layerConfig.get(0);
        }

        FeatureInfoFormat featureInfo = null;
        try {
            featureInfo = FeatureInfoUtilities.getFeatureInfoFormat(getConfiguration(), config, infoFormat);
        } catch (ClassNotFoundException | ConfigurationException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        if (featureInfo == null) {
            throw new CstlServiceException("INFO_FORMAT="+infoFormat+" not supported for layers : "+layerNames, NO_APPLICABLE_CODE);
        }

        try {
            //give the layerRef list used by some FIF
            featureInfo.setLayersDetails(layerRefs);
            final Object result = featureInfo.getFeatureInfo(sdef, vdef, cdef, selectionArea, getFI);
            return new AbstractMap.SimpleEntry<>(infoFormat, result);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }
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
        final String userLogin   = getUserLogin();
        final Data layer = getLayerReference(userLogin, getLegend.getLayer());
        final Layer layerConf = getConfigurationLayer(getLegend.getLayer(), userLogin);
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
                final StyleXmlIO utils = new StyleXmlIO();
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

                final List<MutableLayer> emptyNameMutableLayers = new ArrayList<>();
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

                final List<DataReference> defaultStyleRefs = layerConf.getStyles();
                if (defaultStyleRefs != null && !defaultStyleRefs.isEmpty()) {
                    final DataReference styleRef = defaultStyleRefs.get(0);
                    ms = (styleRef.getLayerId() == null) ? null : getStyle(styleRef);
                } else {
                    ms = null;
                }
            }
            image = layer.getLegendGraphic(dims, mapPortrayal.getDefaultLegendTemplate(), ms, rule, scale);
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
    @Timed
    public PortrayalResponse getMap(final GetMap getMap) throws CstlServiceException {
        isWorking();
        final String queryVersion = getMap.getVersion().toString();
        final String userLogin    = getUserLogin();
    	//
    	// Note this is almost the same logic as in getFeatureInfo
    	//
        // TODO support BLANK exception format for WMS1.1.1 and WMS1.3.0
        final String errorType = getMap.getExceptionFormat();
        final boolean errorInImage;
        final boolean errorBlank;
        if (queryVersion.equals(ServiceDef.WMS_1_3_0.version.toString())) {
            errorInImage = EXCEPTION_130_INIMAGE.equalsIgnoreCase(errorType);
            errorBlank = EXCEPTION_130_BLANK.equalsIgnoreCase(errorType);
        } else {
            errorInImage = EXCEPTION_111_INIMAGE.equalsIgnoreCase(errorType);
            errorBlank = EXCEPTION_111_BLANK.equalsIgnoreCase(errorType);
        }


        // 1. SCENE
        //       -- get the List of layer references
        final List<Name> layerNames = getMap.getLayers();
        final List<Data> layerRefs;
        final List<Layer> layerConfig;
        try{
            layerRefs = getLayerReferences(userLogin, layerNames);
            layerConfig = getConfigurationLayers(userLogin, layerNames);
        } catch (CstlServiceException ex) {
            return handleExceptions(getMap, errorInImage, errorBlank, ex, LAYER_NOT_DEFINED,  KEY_LAYERS.toLowerCase());
        }
        for (Data layer : layerRefs) {
            if (!layer.isQueryable(ServiceDef.Query.WMS_ALL)) {
                throw new CstlServiceException("You are not allowed to request the layer \""+
                        layer.getName() +"\".", LAYER_NOT_QUERYABLE, KEY_LAYERS.toLowerCase());
            }
        }
        //       -- build an equivalent style List
        //TODO: clean up the SLD vs. style logic
        final List<String> styleNames = getMap.getStyles();
        final StyledLayerDescriptor sld = getMap.getSld();

        List<MutableStyle> styles;
        try {
            styles = getStyles(layerConfig, sld, styleNames, userLogin);
        } catch (CstlServiceException ex) {
            return handleExceptions(getMap, errorInImage, errorBlank, ex, STYLE_NOT_DEFINED, null);
        }
        //       -- create the rendering parameter Map
        final Map<String, Object> params = new HashMap<>();
        params.put(WMSQuery.KEY_EXTRA_PARAMETERS, getMap.getParameters());
        final SceneDef sdef = new SceneDef();
        sdef.extensions().add(mapPortrayal.getExtension());
        final Hints hints = mapPortrayal.getHints();
        if (hints != null) {
            /*
             * HACK we set anti-aliasing to false for gif
             */
            if ("image/gif".equals(getMap.getFormat())) {
                hints.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            sdef.getHints().putAll(hints);
        }

        try {
            final MapContext context = PortrayalUtil.createContext(layerRefs, styles, params);
            //apply layercontext filters
            applyLayerFiltersAndDims(context, userLogin);

            sdef.setContext(context);
        } catch (PortrayalException ex) {
            return handleExceptions(getMap, errorInImage, errorBlank, ex, NO_APPLICABLE_CODE, null);
        }


        // 2. VIEW
        final Envelope refEnv;
        try {
            if (getMap.getEnvelope2D().getLowerCorner().getOrdinate(0) > getMap.getEnvelope2D().getUpperCorner().getOrdinate(0) ||
                getMap.getEnvelope2D().getLowerCorner().getOrdinate(1) > getMap.getEnvelope2D().getUpperCorner().getOrdinate(1)) {
                throw new CstlServiceException("BBOX parameter minimum is greater than the maximum", INVALID_PARAMETER_VALUE, KEY_BBOX.toLowerCase());
            }
            final List<Date> times = getMap.getTime();
            final Date[] dates = new Date[2];
            if (times != null && !times.isEmpty()) {
                dates[0] = times.get(0);
                dates[1] = times.get(times.size()-1);
            }
            refEnv = ReferencingUtilities.combine(getMap.getEnvelope2D(), dates, new Double[]{getMap.getElevation(), getMap.getElevation()});
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
        final OutputDef odef = mapPortrayal.getOutputDef(mime);

        try {
            //force longitude first
            vdef.setLongitudeFirst();
        } catch (TransformException | FactoryException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        final PortrayalResponse response = new PortrayalResponse(cdef, sdef, vdef, odef);
        if(!mapPortrayal.isCoverageWriter()){
            try {
                response.prepareNow();
            } catch (PortrayalException ex) {
                return handleExceptions(getMap, errorInImage, errorBlank, ex, NO_APPLICABLE_CODE, null);
            }
        }

        return response;
    }

    private PortrayalResponse handleExceptions(GetMap getMap, boolean errorInImage, boolean errorBlank,
                                               Exception ex, OWSExceptionCode expCode, String locator) throws CstlServiceException {
        if (errorInImage) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return new PortrayalResponse(Cstl.getPortrayalService().writeInImage(ex, getMap.getSize()));
        } else if (errorBlank) {
            Color exColor = getMap.getBackground() != null ? getMap.getBackground() : Color.WHITE;
            if (getMap.getTransparent()) {
                exColor = new Color(0x00FFFFFF & exColor.getRGB(), true); //mark alpha bit as 0 to make color transparent
            }
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return new PortrayalResponse(Cstl.getPortrayalService().writeBlankImage(exColor, getMap.getSize()));
        } else {
            if (locator != null) {
                throw new CstlServiceException(ex, expCode, locator);
            } else {
                throw new CstlServiceException(ex, expCode);
            }
        }
    }

    private static MutableStyle extractStyle(final Name layerName, final Layer configLayer, final StyledLayerDescriptor sld) throws CstlServiceException{
        if(sld == null){
            throw new IllegalArgumentException("SLD should not be null");
        }
        
        final List<MutableNamedLayer> emptyNameSLDLayers = new ArrayList<>();
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
                        final DataReference styleRef = configLayer.getStyle(namedStyle);
                        return getStyle(styleRef);
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

    private List<MutableStyle> getStyles(final List<Layer> layerConfig, final StyledLayerDescriptor sld,
                                         final List<String> styleNames, final String userLogin) throws CstlServiceException {
        final List<MutableStyle> styles = new ArrayList<>();
        for (int i=0; i<layerConfig.size(); i++) {
            final Layer config = layerConfig.get(i);
            if (config != null) {
                final Name layerName = new DefaultName(config.getName());

                final MutableStyle style;
                if (sld != null) {
                    //try to use the provided SLD
                    style = extractStyle(layerName, config, sld);
                } else if (styleNames != null && styleNames.size() > i && styleNames.get(i) != null && !styleNames.get(i).isEmpty()) {
                    //try to grab the style if provided
                    //a style has been given for this layer, try to use it
                    final String namedStyle = styleNames.get(i);
                    style = getLayerStyle(namedStyle);
                    if (style == null) {
                        throw new CstlServiceException("Style provided not found.", STYLE_NOT_DEFINED);
                    }
                } else {
                    //no defined styles, use the favorite one, let the layer get it himself.

                    final List<DataReference> defaultStyleRefs = config.getStyles();
                    if (defaultStyleRefs != null && !defaultStyleRefs.isEmpty()) {
                        final DataReference styleRef = defaultStyleRefs.get(0);
                        style = (styleRef.getLayerId() == null) ? null : getStyle(styleRef);
                    } else {
                        style = null;
                    }
                }
                styles.add(style);
            }
        }
        return styles;
    }

    private void applyLayerFiltersAndDims(final MapItem item, final String userLogin){

        if(item instanceof FeatureMapLayer){
            final FeatureMapLayer fml = (FeatureMapLayer)item;
            final Layer layerContext = getConfigurationLayer(fml.getCollection().getFeatureType().getName(), userLogin);
            if (layerContext.getFilter() != null) {
                final StyleXmlIO xmlUtil = new StyleXmlIO();
                Filter filterGt = Filter.INCLUDE;
                try {
                    filterGt = xmlUtil.getTransformer110().visitFilter(layerContext.getFilter());
                } catch (FactoryException e) {
                    LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
                }
                fml.setQuery(QueryBuilder.filtered(fml.getCollection().getFeatureType().getName(), filterGt));
            }

            for(DimensionDefinition ddef : layerContext.getDimensions()){

                try {
                    final String crsname = ddef.getCrs();
                    final Expression lower = CQL.parseExpression(ddef.getLower());
                    final Expression upper = CQL.parseExpression(ddef.getUpper());
                    final CoordinateReferenceSystem crs;

                    if("elevation".equalsIgnoreCase(crsname)){
                        crs = DefaultVerticalCRS.ELLIPSOIDAL_HEIGHT;
                    }else if("temporal".equalsIgnoreCase(crsname)){
                        crs = DefaultTemporalCRS.JAVA;
                    }else{
                        final EngineeringDatum customDatum = new DefaultEngineeringDatum(Collections.singletonMap("name", crsname));
                        final CoordinateSystemAxis csAxis = new DefaultCoordinateSystemAxis(crsname, "u", AxisDirection.valueOf(crsname), Unit.ONE);
                        final AbstractCS customCs = new AbstractCS(Collections.singletonMap("name", crsname), csAxis);
                        crs = new DefaultEngineeringCRS(Collections.singletonMap("name", crsname), customDatum, customCs);
                    }

                    final FeatureMapLayer.DimensionDef fdef = new FeatureMapLayer.DimensionDef(crs, lower, upper);
                    fml.getExtraDimensions().add(fdef);

                } catch (CQLException ex) {
                    Logger.getLogger(DefaultWMSWorker.class.getName()).log(Level.WARNING, null, ex);
                }
            }

        }

        for(MapItem layer : item.items()){
            applyLayerFiltersAndDims(layer, userLogin);
        }

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
}
