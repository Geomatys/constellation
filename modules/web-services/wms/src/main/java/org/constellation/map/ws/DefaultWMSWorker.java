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
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.spi.ServiceRegistry;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

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
import org.constellation.query.wms.WMSQuery;
import org.constellation.util.DataReference;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.LayerWorker;
import org.constellation.ws.MimeType;
import static org.constellation.api.CommonConstants.*;
import org.constellation.configuration.DimensionDefinition;
import org.constellation.configuration.WMSPortrayal;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import static org.constellation.query.wms.WMSQuery.*;
import static org.constellation.map.ws.WMSConstant.*;

//Geotoolkit dependencies
import org.geotoolkit.cql.CQL;
import org.geotoolkit.cql.CQL;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display.exception.PortrayalException;
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
import org.geotoolkit.sld.xml.StyleXmlIO;
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
import org.geotoolkit.wms.xml.AbstractLegendURL;
import org.geotoolkit.wms.xml.AbstractOnlineResource;
import org.geotoolkit.wms.xml.v130.Capability;
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
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.referencing.ReferencingUtilities;
import org.geotoolkit.referencing.crs.AbstractSingleCRS;
import org.geotoolkit.referencing.crs.DefaultTemporalCRS;
import org.geotoolkit.referencing.crs.DefaultVerticalCRS;
import org.geotoolkit.referencing.cs.AbstractCS;
import org.geotoolkit.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotoolkit.referencing.cs.DiscreteCoordinateSystemAxis;
import org.geotoolkit.referencing.datum.AbstractDatum;
import org.geotoolkit.util.Range;
import org.geotoolkit.util.collection.UnmodifiableArrayList;

import static org.geotoolkit.wms.xml.WmsXmlFactory.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.wms.xml.AbstractBoundingBox;
import org.geotoolkit.wms.xml.AbstractGeographicBoundingBox;
import org.geotoolkit.wms.xml.AbstractLogoURL;

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
     * AxisDirection name for Lat/Long, Elevation, temporal dimensions.
     */
    private static final List<String> COMMONS_DIM = UnmodifiableArrayList.wrap(
            "NORTH", "EAST", "SOUTH", "WEST",
            "UP", "DOWN",
            "FUTURE", "PAST");

    private WMSPortrayal mapPortrayal;

    public DefaultWMSWorker(String id, File configurationDirectory) {
        super(id, configurationDirectory, ServiceDef.Specification.WMS);
        setSupportedVersion(ServiceDef.WMS_1_3_0, ServiceDef.WMS_1_0_0);

        mapPortrayal = new WMSPortrayal();

        final File portrayalFile = new File(configurationDirectory, "WMSPortrayal.xml");
        if (portrayalFile.exists()) {
            final MarshallerPool marshallerPool = GenericDatabaseMarshallerPool.getInstance();
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                mapPortrayal = (WMSPortrayal) unmarshaller.unmarshal(portrayalFile);
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }
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

        final Object cachedCapabilities = getCapabilitiesFromCache(queryVersion, currentLanguage);
        if (cachedCapabilities != null) {
            return (AbstractWMSCapabilities) cachedCapabilities;
        }

        final AbstractWMSCapabilities inCapabilities = (AbstractWMSCapabilities) getStaticCapabilitiesObject(queryVersion, "WMS", currentLanguage);

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
            throw new CstlServiceException("the update sequence paramter is equal to the current", CURRENT_UPDATE_SEQUENCE, "updateSequence");
        }

        //Build the list of layers
        final List<AbstractLayer> outputLayers = new ArrayList<AbstractLayer>();
        final Map<Name,Layer> layers = getLayers();

        for (Name name : layers.keySet()) {
            final LayerDetails layer = getLayerReference(name);
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
             * Create dimentions using CRS of the layer native envelope
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

                    final String directionName = direction.name();
                    if (!COMMONS_DIM.contains(directionName)) {
                        final org.opengis.metadata.Identifier axisName = axis.getName();

                        final Unit<?> u = axis.getUnit();
                        final String unit = (u != null) ? u.toString() : null;
                        String unitSymbol;
                        try {
                            unitSymbol = UnitFormat.getInstance().format(u);
                        } catch (IllegalArgumentException e) {
                            // Workaround for one more bug in javax.measure...
                            unitSymbol = unit;
                        }

                        final LinkedList<String> valuesList = new LinkedList<String>();
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

                        dim = (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) ?
                            new org.geotoolkit.wms.xml.v111.Dimension(values.toString(), axisName.getCode(), unit,
                                unitSymbol, defaut, multipleValues, null, null) :
                            new org.geotoolkit.wms.xml.v130.Dimension(values.toString(), axisName.getCode(), unit,
                                unitSymbol, defaut, multipleValues, null, null);

                        dimensions.add(dim);
                    }
                }
            }

            // Verify extra dimensions
            if(!configLayer.getDimensions().isEmpty()){
                try {
                    final MapItem mi = layer.getMapLayer(null, null);
                    applyLayerFiltersAndDims(mi);

                    if(mi instanceof FeatureMapLayer){
                        final FeatureMapLayer fml = (FeatureMapLayer) mi;
                        for(FeatureMapLayer.DimensionDef ddef : fml.getExtraDimensions()){
                            final Collection<Range> collRefs = fml.getDimensionRange(ddef);
                            // Transform it to a set in order to filter same values
                            final Set<Range> refs = new HashSet<Range>();
                            for (Range ref : collRefs) {
                                refs.add(ref);
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

                            final boolean multipleValues = (refs.size() > 1);
                            final String unitSymbol = ddef.getCrs().getCoordinateSystem().getAxis(0).getUnit().toString();
                            final String unit = unitSymbol;
                            final String axisName = ddef.getCrs().getCoordinateSystem().getAxis(0).getName().getCode();
                            final String defaut = "";

                            dim = (queryVersion.equals(ServiceDef.WMS_1_1_1_SLD.version.toString())) ?
                                new org.geotoolkit.wms.xml.v111.Dimension(values.toString(), axisName, unit,
                                    unitSymbol, defaut, multipleValues, null, null) :
                                new org.geotoolkit.wms.xml.v130.Dimension(values.toString(), axisName, unit,
                                    unitSymbol, defaut, multipleValues, null, null);

                            dimensions.add(dim);
                        }
                    }

                } catch (PortrayalException ex) {
                    Logger.getLogger(DefaultWMSWorker.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                    break;
                } catch (DataStoreException ex) {
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
                layerName = configLayer.getAlias().trim().replaceAll(" ", "_");
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
            if (layer instanceof CoverageLayerDetails) {
                final CoverageLayerDetails coverageLayer = (CoverageLayerDetails)layer;
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
            final List<String> stylesName = layer.getFavoriteStyles();
            final List<org.geotoolkit.wms.xml.Style> styles = new ArrayList<org.geotoolkit.wms.xml.Style>();
            if (stylesName != null && !stylesName.isEmpty()) {
                // For each styles defined for the layer, get the dimension of the getLegendGraphic response.
                for (String styleName : stylesName) {
                    final MutableStyle ms = getStyle(styleName);
                    final org.geotoolkit.wms.xml.Style style = convertMutableStyleToWmsStyle("1.3.0", ms, layer, legendUrlPng, legendUrlGif);
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
        putCapabilitiesInCache(queryVersion, currentLanguage, inCapabilities);
        return inCapabilities;
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
            final LayerDetails layerDetails, final String legendUrlPng, final String legendUrlGif) throws CstlServiceException
    {
        if (configLayer == null) {
            return outputLayer;
        }
        if (configLayer.getStyles() != null && !configLayer.getStyles().isEmpty()) {
            // @TODO: convert the data reference string to a mutable style
            // ${providerStyleType|providerStyleId|styleName}
            final List<org.geotoolkit.wms.xml.Style> styles = new ArrayList<org.geotoolkit.wms.xml.Style>();
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
                    ms = getStyleByIdentifier(styl);
                }
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
    private org.geotoolkit.wms.xml.Style convertMutableStyleToWmsStyle(final String currentVersion, final MutableStyle ms, final LayerDetails layerDetails,
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
            //apply layercontext filters
            applyLayerFiltersAndDims(context);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        // 2. VIEW
        Envelope refEnv;
        try {
            refEnv = ReferencingUtilities.combine(getFI.getEnvelope2D(), new Date[]{getFI.getTime(), getFI.getTime()}, new Double[]{getFI.getElevation(), getFI.getElevation()});
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
            if(visitor != null) {break;}
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
                        ms = (styleRef == null || styleRef.getLayerId() == null) ? null : getStyle(styleRef.getLayerId().getLocalPart());
                    } else {
                        ms = getStyleByIdentifier(styleId);
                    }
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
    public PortrayalResponse getMap(final GetMap getMap) throws CstlServiceException {
        isWorking();
        final String queryVersion = getMap.getVersion().toString();

    	//
    	// Note this is almost the same logic as in getFeatureInfo
    	//
        // TODO support BLANK exception format for WMS1.1.1 and WMS1.3.0
        final String errorType = getMap.getExceptionFormat();
        boolean errorInImage = false;
        if (queryVersion.equals(ServiceDef.WMS_1_3_0.version.toString())) {
            errorInImage = EXCEPTION_130_INIMAGE.equalsIgnoreCase(errorType);
        } else {
            errorInImage = EXCEPTION_111_INIMAGE.equalsIgnoreCase(errorType);
        }


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
            final MapContext context = MapBuilder.createContext();

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
                mapLayer.setVisible(true);
                context.items().add(mapLayer);
            }
            //apply layercontext filters
            applyLayerFiltersAndDims(context);

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
            refEnv = ReferencingUtilities.combine(getMap.getEnvelope2D(), new Date[]{getMap.getTime(), getMap.getTime()}, new Double[]{getMap.getElevation(), getMap.getElevation()});
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
        } catch (TransformException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        } catch (FactoryException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        final PortrayalResponse response = new PortrayalResponse(cdef, sdef, vdef, odef);

        if(!mapPortrayal.isCoverageWriter()){
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

    private static MutableStyle extractStyle(final Name layerName, final StyledLayerDescriptor sld) throws CstlServiceException{
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
                        return getStyle(namedStyle);
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
                style = getStyle(namedStyle);
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
                        style = (styleRef == null || styleRef.getLayerId() == null) ? null : getStyle(styleRef.getLayerId().getLocalPart());
                    } else {
                        style = getStyleByIdentifier(styleId);
                    }
                } else {
                    style = null;
                }
            }
            styles.add(style);
        }
        return styles;
    }

    private void applyLayerFiltersAndDims(final MapItem item){
        final Map<Name,Layer> layersContext = getLayers();

        if(item instanceof FeatureMapLayer){
            final FeatureMapLayer fml = (FeatureMapLayer)item;
            final Layer layerContext = layersContext.get(fml.getCollection().getFeatureType().getName());
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
                        final AbstractDatum customDatum = new AbstractDatum(Collections.singletonMap("name", crsname));
                        final CoordinateSystemAxis csAxis = new DefaultCoordinateSystemAxis(crsname, "u", AxisDirection.valueOf(crsname), Unit.ONE);
                        final AbstractCS customCs = new AbstractCS(Collections.singletonMap("name", crsname), csAxis);
                        crs = new AbstractSingleCRS(Collections.singletonMap("name", crsname), customDatum, customCs);
                    }

                    final FeatureMapLayer.DimensionDef fdef = new FeatureMapLayer.DimensionDef(crs, lower, upper);
                    fml.getExtraDimensions().add(fdef);

                } catch (CQLException ex) {
                    Logger.getLogger(DefaultWMSWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        for(MapItem layer : item.items()){
            applyLayerFiltersAndDims(layer);
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
