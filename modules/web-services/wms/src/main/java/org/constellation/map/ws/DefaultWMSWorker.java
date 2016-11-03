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

import com.codahale.metrics.annotation.Timed;
import org.apache.sis.internal.util.UnmodifiableArrayList;
import org.apache.sis.measure.MeasurementRange;
import org.apache.sis.measure.Range;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.crs.DefaultEngineeringCRS;
import org.apache.sis.referencing.cs.AbstractCS;
import org.apache.sis.referencing.datum.DefaultEngineeringDatum;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.AttributionType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DimensionDefinition;
import org.constellation.configuration.FormatURL;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Reference;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.configuration.WMSPortrayal;
import org.constellation.dto.Details;
import org.constellation.map.featureinfo.FeatureInfoFormat;
import org.constellation.map.featureinfo.FeatureInfoUtilities;
import org.constellation.portrayal.PortrayalUtil;
import org.constellation.portrayal.internal.CstlPortrayalService;
import org.constellation.portrayal.internal.PortrayalResponse;
import org.constellation.provider.CoverageData;
import org.constellation.provider.Data;
import org.constellation.util.DataReference;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.LayerWorker;
import org.constellation.ws.MimeType;
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
import org.geotoolkit.inspire.xml.vs.ExtendedCapabilitiesType;
import org.geotoolkit.inspire.xml.vs.LanguageType;
import org.geotoolkit.inspire.xml.vs.LanguagesType;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.referencing.ReferencingUtilities;
import org.apache.sis.referencing.cs.DefaultCoordinateSystemAxis;
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
import org.geotoolkit.temporal.util.PeriodUtilities;
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
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.referencing.operation.TransformException;
import org.opengis.sld.StyledLayerDescriptor;
import org.opengis.util.FactoryException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Level;
import org.apache.sis.measure.NumberRange;

import org.apache.sis.util.CharSequences;
import static org.constellation.api.CommonConstants.DEFAULT_CRS;
import static org.constellation.map.ws.WMSConstant.EXCEPTION_111_BLANK;
import static org.constellation.map.ws.WMSConstant.EXCEPTION_111_INIMAGE;
import static org.constellation.map.ws.WMSConstant.EXCEPTION_130_BLANK;
import static org.constellation.map.ws.WMSConstant.EXCEPTION_130_INIMAGE;
import static org.constellation.map.ws.WMSConstant.KEY_BBOX;
import static org.constellation.map.ws.WMSConstant.KEY_ELEVATION;
import static org.constellation.map.ws.WMSConstant.KEY_EXTRA_PARAMETERS;
import static org.constellation.map.ws.WMSConstant.KEY_LAYER;
import static org.constellation.map.ws.WMSConstant.KEY_LAYERS;
import static org.constellation.map.ws.WMSConstant.KEY_TIME;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.combineIterator.GridCombineIterator;
import org.geotoolkit.util.NamesExt;
import static org.geotoolkit.ows.xml.OWSExceptionCode.CURRENT_UPDATE_SEQUENCE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_POINT;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_FORMAT;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_UPDATE_SEQUENCE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.LAYER_NOT_DEFINED;
import static org.geotoolkit.ows.xml.OWSExceptionCode.LAYER_NOT_QUERYABLE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.STYLE_NOT_DEFINED;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.geotoolkit.storage.coverage.CoverageReference;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createBoundingBox;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createDimension;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createGeographicBoundingBox;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createLayer;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createLegendURL;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createLogoURL;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createOnlineResource;
import static org.geotoolkit.wms.xml.WmsXmlFactory.createStyle;
import org.opengis.util.GenericName;
import org.apache.sis.util.logging.Logging;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;

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
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DefaultWMSWorker extends LayerWorker implements WMSWorker {

    /**
     * Temporal formatting for layer with TemporalCRS.
     */
    private static final DateFormat ISO8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
        ISO8601_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

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
    private final List<String> GFI_MIME_TYPES = new ArrayList<>();

    private WMSPortrayal mapPortrayal;
    public DefaultWMSWorker(final String id) {
        super(id, ServiceDef.Specification.WMS);

        //get all supported GetFeatureInfo mimetypes
        try {
            GFI_MIME_TYPES.clear();
            final LayerContext config = (LayerContext)getConfiguration();
            GFI_MIME_TYPES.addAll(FeatureInfoUtilities.allSupportedMimeTypes(config));
        } catch (ConfigurationException | ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        mapPortrayal = new WMSPortrayal();
        try {
            WMSPortrayal candidate = (WMSPortrayal) serviceBusiness.getExtraConfiguration("WMS", id, "WMSPortrayal.xml");
            if (candidate != null) {
                mapPortrayal = candidate;
            }
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, null, ex);
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
        if (requestedLanguage != null && supportedLanguages.contains(requestedLanguage)) {
            currentLanguage = requestedLanguage;
        } else if (requestedLanguage == null && defaultLanguage != null) {
            currentLanguage = defaultLanguage;
        } else {
            currentLanguage = null;
        }

        final Object cachedCapabilities = getCapabilitiesFromCache(queryVersion, currentLanguage);
        if (cachedCapabilities != null) {
            return (AbstractWMSCapabilities) cachedCapabilities;
        }

        final Details skeleton = getStaticCapabilitiesObject("wms", currentLanguage);
        final AbstractWMSCapabilities inCapabilities = WMSConstant.createCapabilities(queryVersion, skeleton);

        // temporary sort in order to fix cite test
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
            final Data layer = getLayerReference(configLayer);

            if (!layer.isQueryable(ServiceDef.Query.WMS_ALL)) {
                continue;
            }

            // Get default CRS for the layer supported crs.
            Envelope layerNativeEnv = null;
            String nativeCrs = null;
            try {
                layerNativeEnv = layer.getEnvelope();
                if(layerNativeEnv!=null){
                    CoordinateReferenceSystem crs = layerNativeEnv.getCoordinateReferenceSystem();
                    if(crs!=null){
                        final Integer epsgCode = IdentifiedObjects.lookupEpsgCode(crs, true);
                        if(epsgCode!=null){
                            nativeCrs = "EPSG:"+epsgCode;
                        }
                    }
                }
            } catch (DataStoreException | FactoryException ex) {
                LOGGER.log(Level.INFO, "Error retrieving data crs for the layer :"+ layer.getName(), ex);
            }

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
                final PeriodUtilities periodFormatter = new PeriodUtilities(ISO8601_FORMAT);
                final String defaut = ISO8601_FORMAT.format(dates.last());
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

            final Object origin = layer.getOrigin();

            //-- execute only if it is a CoverageReference
            if (origin != null && origin instanceof CoverageReference) {
                final CoverageReference covRef = (CoverageReference)origin;
                GeneralGridGeometry gridGeom = null;

                //-- try to open coverage
                try {
                    final GridCoverageReader covReader = covRef.acquireReader();
                    gridGeom = new GeneralGridGeometry(covReader.getGridGeometry(covRef.getImageIndex()));
                    covRef.recycle(covReader);
                } catch (DataStoreException ex) {
                    throw new CstlServiceException(ex);
                }

                final CoordinateReferenceSystem crsLayer                       = gridGeom.getCoordinateReferenceSystem();
                final Map<Integer, CoordinateReferenceSystem> indexedDecompose = ReferencingUtilities.indexedDecompose(crsLayer);

                //-- for each CRS part if crs is not 2D part or Temporal or elevation add value
                for (Integer key : indexedDecompose.keySet()) {
                    final CoordinateReferenceSystem currentCrs = indexedDecompose.get(key);

                    //-- in this case we add value only if crs is one dimensional -> 1 dimension -> getAxis(0).
                    final CoordinateSystemAxis axis = currentCrs.getCoordinateSystem().getAxis(0);

                    if (!COMMONS_DIM.contains(axis.getDirection().name())) {
                        //we want values at center, not at corner
                        final MathTransform gridToCRS = gridGeom.getGridToCRS(PixelInCell.CELL_CENTER);
                        final NumberRange[] numberRanges = GridCombineIterator.extractAxisRanges(gridGeom.getExtent(), gridToCRS, key);

                        final StringBuilder values = new StringBuilder();
                        for (int i = 0; i < numberRanges.length; i++) {
                            final NumberRange numberRange = numberRanges[i];
                            values.append(numberRange.getMinDouble());
                            if (i != numberRanges.length - 1) values.append(',');
                        }
                        final String unitStr = (axis.getUnit() != null) ? axis.getUnit().toString() : null;
                        final String defaut = (!(numberRanges.length != 0)) ? ""+numberRanges[0].getMinDouble() : null;
                        String unitSymbol;
                        try {
                            unitSymbol = UnitFormat.getInstance().format(axis.getUnit());
                        } catch (IllegalArgumentException e) {
                            // Workaround for one more bug in javax.measure...
                            unitSymbol = unitStr;
                        }
                        dim = createDimension(queryVersion, values.toString(), axis.getName().getCode(), unitStr,
                                unitSymbol, defaut, null, null, null);
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
                    Logging.getLogger("org.constellation.map.ws").log(Level.INFO, ex.getMessage(), ex);
                    break;
                }
            }

            /*
             * LegendUrl generation
             * TODO: Use a StringBuilder or two
             */
            final GenericName fullLayerName = layer.getName();
            final String layerName;

            //Use layer alias from config if exist.
            if (configLayer.getAlias() != null && !configLayer.getAlias().isEmpty()) {
                layerName = configLayer.getAlias();
            } else {
                final String ns = NamesExt.getNamespace(fullLayerName);
                if (ns != null && !ns.isEmpty()) {
                    layerName = ns + ':' + fullLayerName.tip().toString();
                } else {
                    layerName = fullLayerName.tip().toString();
                }
            }
            final String beginLegendUrl = getServiceUrl() + "REQUEST=GetLegendGraphic&VERSION=1.1.1&FORMAT=";
            final String legendUrlGif   = beginLegendUrl + MimeType.IMAGE_GIF + "&LAYER=" + layerName;
            final String legendUrlPng   = beginLegendUrl + MimeType.IMAGE_PNG + "&LAYER=" + layerName;
            final String queryable      = (layer.isQueryable(ServiceDef.Query.WMS_GETINFO)) ? "1" : "0";
            final CharSequence _abstract;
            final CharSequence keyword;
            if (layer instanceof CoverageData) {
                final CoverageData coverageLayer = (CoverageData)layer;
                _abstract = CharSequences.toASCII(coverageLayer.getRemarks());
                keyword   = CharSequences.toASCII(coverageLayer.getThematic());
            } else {
                _abstract = "Vector data";
                keyword   = "Vector data";
            }

            final AbstractBoundingBox outputBBox;
            AbstractBoundingBox nativeBBox = null;
            if (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) {
                /*
                 * TODO
                 * do we have to use the same order as WMS 1.3.0 (SOUTH WEST NORTH EAST) ???
                 */
                outputBBox = createBoundingBox(queryVersion,
                        "EPSG:4326",
                        inputGeoBox.getWestBoundLongitude(),
                        inputGeoBox.getSouthBoundLatitude(),
                        inputGeoBox.getEastBoundLongitude(),
                        inputGeoBox.getNorthBoundLatitude(), 0.0, 0.0);

                if(nativeCrs!=null){
                    try {
                        layerNativeEnv = CRS.transform(layerNativeEnv, CRS.decode(nativeCrs, true));
                        nativeBBox = createBoundingBox(queryVersion,
                            nativeCrs,
                            layerNativeEnv.getMinimum(0),
                            layerNativeEnv.getMinimum(1),
                            layerNativeEnv.getMaximum(0),
                            layerNativeEnv.getMaximum(1), 0.0, 0.0);
                    } catch (FactoryException | TransformException ex) {
                        LOGGER.log(Level.INFO, "Error retrieving data crs for the layer :"+ layer.getName(), ex);
                    }
                }

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

                if(nativeCrs!=null){
                    nativeBBox = createBoundingBox(queryVersion,
                        nativeCrs,
                        layerNativeEnv.getMinimum(0),
                        layerNativeEnv.getMinimum(1),
                        layerNativeEnv.getMaximum(0),
                        layerNativeEnv.getMaximum(1), 0.0, 0.0);
                }

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

            //list supported crs
            final List<String> supportedCrs;
            if(nativeCrs!=null && DEFAULT_CRS.indexOf(nativeCrs)!=0){
                //we add or move to first position the native crs
                supportedCrs = new ArrayList<>(DEFAULT_CRS);
                supportedCrs.remove(nativeCrs);
                supportedCrs.add(0, nativeCrs);
            }else{
                supportedCrs = DEFAULT_CRS;
            }

            final AbstractGeographicBoundingBox bbox = createGeographicBoundingBox(queryVersion, inputGeoBox);
            final AbstractLayer outputLayerO = createLayer(queryVersion, layerName,
                    (_abstract != null) ? _abstract.toString() : null,
                    ( keyword  != null) ?  keyword .toString() : null,
                    supportedCrs, bbox, outputBBox, queryable, dimensions, styles);
            if(nativeBBox!=null && !nativeBBox.getCRSCode().equals(outputBBox.getCRSCode())){
                ((List)outputLayerO.getBoundingBox()).add(0, nativeBBox);
            }

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
            final String unitSymbol = ddef.getCrs().getCoordinateSystem().getAxis(0).getUnit().toString();
            final String unit = unitSymbol;
            final String axisName = ddef.getCrs().getCoordinateSystem().getAxis(0).getName().getCode();
            final String defaut = "";

            final AbstractDimension dim = (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) ?
                new org.geotoolkit.wms.xml.v111.Dimension(sortedValues, axisName, unit,
                    unitSymbol, defaut, null, null, null) :
                new org.geotoolkit.wms.xml.v130.Dimension(sortedValues, axisName, unit,
                    unitSymbol, defaut, null, null, null);

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
            for (DataReference styleRef : configLayer.getStyles()) {
                MutableStyle ms = null;
                try {
                    final MutableStyle style = styleBusiness.getStyle(styleRef.getProviderId(), styleRef.getLayerId().tip().toString());
                    if (style != null) {
                        ms = style;
                    } else {
                        throw new ConstellationException(new IllegalArgumentException("The given style reference was invalid"));
                    }
                } catch (ConstellationException | TargetNotFoundException e) {
                    // The given style reference was invalid, we can't get a style from that
                    LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
                }

//                Style style = null;
//                try {
//                    style = DataReferenceConverter.convertDataReferenceToStyle(styleRef);
//                } catch (NonconvertibleObjectException e) {
//                    // The given style reference was invalid, we can't get a style from that
//                    LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
//                }
//                ms = StyleUtilities.copy(style);
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
            styleName = dataRef.getLayerId().tip().toString();
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
        final List<GenericName> layerNames        = getFI.getQueryLayers();
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
        params.put(KEY_ELEVATION, elevation);
        params.put(KEY_TIME, time);
        params.put(KEY_EXTRA_PARAMETERS, getFI.getParameters());
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
        final Envelope refEnv = buildRequestedViewEnvelope(getFI, layerRefs);
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
            throw new CstlServiceException("INFO_FORMAT="+infoFormat+" not supported for layers : "+layerNames, INVALID_FORMAT);
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
        final List<GenericName> layerNames = getMap.getLayers();

        //check layer limit
        final Details skeleton = getStaticCapabilitiesObject("wms", "eng");
        if (skeleton.getServiceConstraints()!=null) {
            final int layerLimit = skeleton.getServiceConstraints().getLayerLimit();
            if(layerLimit>0 && layerLimit<layerNames.size()) {
                throw new CstlServiceException("Too many layers requested, limit is "+layerLimit);
            }
        }
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
        params.put(KEY_EXTRA_PARAMETERS, getMap.getParameters());
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
        final Envelope refEnv = buildRequestedViewEnvelope(getMap, layerRefs);
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

    /**
     * Build request view envelope from request parameters and requested layers.
     * Limitation : generate an envelope only with TIME and ELEVATION dimensions, all layers default values
     * ar merge in one range instead of request each layers with his default value.
     *
     * TODO support cases defined in WMS spec. Annexe C and D. See CSTL-1245.
     *
     * @param request GetMap based request (GetMap and GetFeatureInfo)
     * @param layers all layers requested
     * @return view Envelope 2D, 3D or 4D depending of dimensions of layers and request.
     * @throws CstlServiceException
     */
    public Envelope buildRequestedViewEnvelope(GetMap request, List<Data> layers) throws CstlServiceException {
        final Envelope refEnv;
        try {
            //check envelope has positive span only if not a GetFeatureInfo request.
            if (!(request instanceof GetFeatureInfo)) {
                if (request.getEnvelope2D().getLowerCorner().getOrdinate(0) > request.getEnvelope2D().getUpperCorner().getOrdinate(0) ||
                        request.getEnvelope2D().getLowerCorner().getOrdinate(1) > request.getEnvelope2D().getUpperCorner().getOrdinate(1)) {
                    throw new CstlServiceException("BBOX parameter minimum is greater than the maximum", INVALID_PARAMETER_VALUE, KEY_BBOX.toLowerCase());
                }
            }

            final Date[] time = new Date[2];
            final List<Date> times = request.getTime();
            if (times != null && !times.isEmpty()) {
                time[0] = times.get(0);
                time[1] = times.get(times.size()-1);
            } else {

                /*
                    No time specified on request, find all defaultTime (last)
                    of all layers and use defaults times to create request range.

                    This behavior is not defined in WMS spec.
                    It's an arbitrary behavior until getMap/getFeatureInfo request are refactored
                    in CSTL-1245.
                 */
                final SortedSet<Date> defaultTimes = new TreeSet<>();
                for (Data layer : layers) {
                    try {
                        ArrayList<Date> layerTimes = new ArrayList<>(layer.getAvailableTimes());
                        if (layerTimes != null && !layerTimes.isEmpty()) {

                            //get the last and previous date
                            defaultTimes.add(layerTimes.get(StrictMath.max(0, layerTimes.size()-2)));
                        }
                    } catch (DataStoreException e) {
                        // no time found for layer, continue to next one
                        LOGGER.log(Level.FINE, "Enable to extract layer available times for " + layer.getName(), e);
                    }
                }

                if (!defaultTimes.isEmpty()) {
                    // first and last layer defaults times
                    time[0] = defaultTimes.first();
                    time[1] = defaultTimes.last();
                }
            }

            final Double[] vertical = new Double[2];
            Double requestElevation = request.getElevation();
            if (requestElevation != null) {
                vertical[0] = vertical[1] = requestElevation;
            } else {

                //No time specified on request, find all defaultElevations (first)
                //of all layers and use defaults elevations to create request range.
                final SortedSet<Double> defaultElevations = new TreeSet<>();
                for (Data layer : layers) {
                    try {
                        SortedSet<Number> layerElevations = layer.getAvailableElevations();
                        if (layerElevations != null && !layerElevations.isEmpty()) {
                            defaultElevations.add(layerElevations.first().doubleValue());
                        }
                    } catch (DataStoreException e) {
                        // no time found for layer, continue to next one
                        LOGGER.log(Level.FINE, "Enable to extract layer available times for " + layer.getName(), e);
                    }
                }

                if (!defaultElevations.isEmpty()) {
                    // first and last layer defaults times
                    vertical[0] = defaultElevations.first();
                    vertical[1] = defaultElevations.last();
                }
            }

            // generate view envelope with 2D, time and vertical values.
            // TODO add other dimensions (see CSTL-1245).
            refEnv = ReferencingUtilities.combine(request.getEnvelope2D(), time, vertical);
        } catch (TransformException ex) {
            throw new CstlServiceException(ex);
        }
        return refEnv;
    }

    private PortrayalResponse handleExceptions(GetMap getMap, boolean errorInImage, boolean errorBlank,
                                               Exception ex, OWSExceptionCode expCode, String locator) throws CstlServiceException {
        if (errorInImage) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            BufferedImage img = CstlPortrayalService.getInstance().writeInImage(ex, getMap.getSize());
            Boolean trs = getMap.getTransparent();
            if (Boolean.FALSE.equals(trs)) {
                //force background
                final BufferedImage buffer = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
                final Color exColor = getMap.getBackground() != null ? getMap.getBackground() : Color.WHITE;
                final Graphics2D g = buffer.createGraphics();
                g.setColor(exColor);
                g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
                g.drawImage(img, 0, 0, null);
                img = buffer;
            }
            return new PortrayalResponse(img);

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

    private MutableStyle extractStyle(final GenericName layerName, final Layer configLayer, final StyledLayerDescriptor sld) throws CstlServiceException{
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
            if (layerName.tip().toString().equals(sldLayerName)) {
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
                final GenericName layerName = NamesExt.create(config.getName());

                final MutableStyle style;
                if (sld != null) {
                    //try to use the provided SLD
                    style = extractStyle(layerName, config, sld);
                } else if (styleNames != null && styleNames.size() > i && styleNames.get(i) != null && !styleNames.get(i).isEmpty()) {
                    //try to grab the style if provided
                    //a style has been given for this layer, try to use it
                    final String namedStyle = styleNames.get(i);
                    final DataReference styleRef = config.getStyle(namedStyle);
                    if (styleRef == null) {
                        style = null;
                    } else {
                        style = (styleRef.getLayerId() == null) ? null : getStyle(styleRef);
                    }
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

    /**
     * Apply and transform recursively configuration {@link org.opengis.filter.Filter} and
     * {@link org.constellation.configuration.DimensionDefinition} to all {@link org.geotoolkit.map.FeatureMapLayer} in
     * input {@link org.geotoolkit.map.MapItem}.
     * This method will only work on Feature layers.
     *
     * @param item root mapItem
     * @param userLogin login used to get configuration.
     */
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
                        crs = CommonCRS.Vertical.ELLIPSOIDAL.crs();
                    }else if("temporal".equalsIgnoreCase(crsname)){
                        crs = CommonCRS.Temporal.JAVA.crs();
                    }else{
                        final EngineeringDatum customDatum = new DefaultEngineeringDatum(Collections.singletonMap("name", crsname));
                        final CoordinateSystemAxis csAxis = new DefaultCoordinateSystemAxis(Collections.singletonMap("name", crsname), "u", AxisDirection.valueOf(crsname), Unit.ONE);
                        final AbstractCS customCs = new AbstractCS(Collections.singletonMap("name", crsname), csAxis);
                        crs = new DefaultEngineeringCRS(Collections.singletonMap("name", crsname), customDatum, customCs);
                    }

                    final FeatureMapLayer.DimensionDef fdef = new FeatureMapLayer.DimensionDef(crs, lower, upper);
                    fml.getExtraDimensions().add(fdef);

                } catch (CQLException ex) {
                    Logging.getLogger("org.constellation.map.ws").log(Level.WARNING, null, ex);
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
