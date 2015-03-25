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
package org.constellation.wmts.ws;

import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.IdentifiedObjects;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Layer;
import org.constellation.dto.Details;
import org.constellation.map.featureinfo.FeatureInfoFormat;
import org.constellation.map.featureinfo.FeatureInfoUtilities;
import org.constellation.portrayal.PortrayalUtil;
import org.constellation.provider.Data;
import org.constellation.util.DataReference;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.LayerWorker;
import org.constellation.ws.MimeType;
import org.geotoolkit.coverage.CoverageUtilities;
import org.geotoolkit.coverage.GridMosaic;
import org.geotoolkit.coverage.Pyramid;
import org.geotoolkit.coverage.PyramidSet;
import org.geotoolkit.coverage.PyramidalCoverageReference;
import org.geotoolkit.coverage.TileReference;
import org.geotoolkit.coverage.finder.StrictlyCoverageFinder;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.ows.xml.AbstractCapabilitiesCore;
import org.geotoolkit.ows.xml.v110.AcceptFormatsType;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.ows.xml.v110.OperationsMetadata;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.ows.xml.v110.ServiceIdentification;
import org.geotoolkit.ows.xml.v110.ServiceProvider;
import org.geotoolkit.ows.xml.v110.WGS84BoundingBoxType;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.ReferencingUtilities;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.TimeParser;
import org.geotoolkit.wmts.WMTSUtilities;
import org.geotoolkit.wmts.xml.WMTSMarshallerPool;
import org.geotoolkit.wmts.xml.v100.Capabilities;
import org.geotoolkit.wmts.xml.v100.ContentsType;
import org.geotoolkit.wmts.xml.v100.Dimension;
import org.geotoolkit.wmts.xml.v100.DimensionNameValue;
import org.geotoolkit.wmts.xml.v100.GetCapabilities;
import org.geotoolkit.wmts.xml.v100.GetFeatureInfo;
import org.geotoolkit.wmts.xml.v100.GetTile;
import org.geotoolkit.wmts.xml.v100.LayerType;
import org.geotoolkit.wmts.xml.v100.Themes;
import org.geotoolkit.wmts.xml.v100.TileMatrix;
import org.geotoolkit.wmts.xml.v100.TileMatrixSet;
import org.geotoolkit.wmts.xml.v100.TileMatrixSetLink;
import org.geotoolkit.wmts.xml.v100.URLTemplateType;
import org.opengis.coverage.Coverage;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.inject.Named;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 * Working part of the WMTS service.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 * @since 0.3
 */
@Named("WTMSWorker")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DefaultWMTSWorker extends LayerWorker implements WMTSWorker {

    public static final String TIME_NAME = "time";
    public static final String TIME_UNIT = "ISO-8601";

    public static final String ELEVATION_NAME = "elevation";
    public static final String CURRENT_VALUE = "current";

    public static final double RESOLUTION_EPSILON = 1E-9;

    /** Default temporal CRS, used for comparison purposes. */
    public static final TemporalCRS JAVA_TIME = CommonCRS.Temporal.JAVA.crs();

    public static final SimpleDateFormat ISO_8601_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
        ISO_8601_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * A list of supported MIME type
     */
    private static final List<String> ACCEPTED_OUTPUT_FORMATS;
    static {
        ACCEPTED_OUTPUT_FORMATS = Arrays.asList(MimeType.TEXT_XML,
                                                MimeType.APP_XML,
                                                MimeType.TEXT_PLAIN);
    }

    /**
     * A map which contains the binding between capabilities tile matrix set identifiers and input
     * {@link org.geotoolkit.coverage.Pyramid} ids. It's used only if we've got multiple pyramids with the same ID but
     * different matrix structure. Otherwise, we directly use pyramid ids as tile matrix set name.
     */
    private final HashMap<String, HashSet<String>> tmsIdBinding = new HashMap<>();
    private final ReentrantReadWriteLock tmsBindingLock = new ReentrantReadWriteLock();

    /**
     * Instanciates the working class for a SOAP client, that do request on a SOAP PEP service.
     */
    public DefaultWMTSWorker(final String id) {
        super(id, ServiceDef.Specification.WMTS);
        if (isStarted) {
            LOGGER.log(Level.INFO, "WMTS worker {0} running", id);
        }
    }

    @Override
    protected MarshallerPool getMarshallerPool() {
        return WMTSMarshallerPool.getInstance();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities getCapabilities(GetCapabilities requestCapabilities) throws CstlServiceException {
        LOGGER.log(logLevel, "getCapabilities request processing\n");
        final long start = System.currentTimeMillis();
        final String userLogin  = getUserLogin();

        //we verify the base request attribute
        if (requestCapabilities.getService() != null) {
            if (!requestCapabilities.getService().equalsIgnoreCase("WMTS")) {
                throw new CstlServiceException("service must be \"WMTS\"!",
                                                 INVALID_PARAMETER_VALUE, "service");
            }
        } else {
            throw new CstlServiceException("Service must be specified!",
                                             MISSING_PARAMETER_VALUE, "service");
        }
        final AcceptVersionsType versions = requestCapabilities.getAcceptVersions();
        if (versions != null) {
            if (!versions.getVersion().contains("1.0.0")){
                 throw new CstlServiceException("version available : 1.0.0",
                                             VERSION_NEGOTIATION_FAILED, "acceptVersion");
            }
        }

        final AcceptFormatsType formats = requestCapabilities.getAcceptFormats();
        if (formats != null && formats.getOutputFormat().size() > 0 ) {
            boolean found = false;
            for (String form: formats.getOutputFormat()) {
                if (ACCEPTED_OUTPUT_FORMATS.contains(form)) {
                    found = true;
                }
            }
            if (!found) {
                throw new CstlServiceException("accepted format : text/xml, application/xml",
                                                 INVALID_PARAMETER_VALUE, "acceptFormats");
            }
        }

        SectionsType sections = requestCapabilities.getSections();
        if (sections == null) {
            sections = new SectionsType(SectionsType.getExistingSections("1.1.1"));
        }

        //set the current updateSequence parameter
        final boolean returnUS = returnUpdateSequenceDocument(requestCapabilities.getUpdateSequence());
        if (returnUS) {
            return new Capabilities("1.0.0", getCurrentUpdateSequence());
        }

        // If the getCapabilities response is in cache, we just return it.
        AbstractCapabilitiesCore cachedCapabilities = getCapabilitiesFromCache("1.0.0", null);
        if (cachedCapabilities != null) {
            return (Capabilities) cachedCapabilities.applySections(sections);
        }

        /* We synchronize Computing, because every thread will compute the same thing, its useless to waste CPU. One will
         * do the job, the others will wait for it. More over, it's EXTREMELY important for the integrity of the binding
         * between pyramids and Tile matrix set ids that we synchronize the get Capa using our binding lock.
         */
        tmsBindingLock.writeLock().lock();
        try {

            cachedCapabilities = getCapabilitiesFromCache("1.0.0", null);
            if (cachedCapabilities != null) {
                return (Capabilities) cachedCapabilities.applySections(sections);
            }
            /*
             * BUILD NEW CAPABILITIES DOCUMENT
             */
            tmsIdBinding.clear();

            // we load the skeleton capabilities
            final Details skeleton = getStaticCapabilitiesObject("wmts", null);
            final Capabilities skeletonCapabilities = (Capabilities) WMTSConstant.createCapabilities("1.0.0", skeleton);

            //we prepare the response document
            final ServiceIdentification si = skeletonCapabilities.getServiceIdentification();
            final ServiceProvider sp = skeletonCapabilities.getServiceProvider();
            final OperationsMetadata om = (OperationsMetadata) WMTSConstant.OPERATIONS_METADATA.clone();
            // TODO
            final List<Themes> themes = new ArrayList<>();

            //we update the URL
            om.updateURL(getServiceUrl());

            // Build the list of layers
            final List<LayerType> outputLayers = new ArrayList<>();
            // and the list of matrix set
            final HashMap<String, TileMatrixSet> tileSets = new HashMap<>();

            final List<Layer> declaredLayers = getConfigurationLayers(userLogin);

            for (final Layer configLayer : declaredLayers) {
                final Data details = getLayerReference(configLayer);
                if (details == null) {
                    LOGGER.log(Level.WARNING, "No data can be found for name : "+configLayer.getName());
                    continue;
                }
                final String name;
                if (configLayer.getAlias() != null && !configLayer.getAlias().isEmpty()) {
                    name = configLayer.getAlias().trim().replaceAll(" ", "_");
                } else {
                    name = configLayer.getName().getLocalPart();
                }

                final Object origin = details.getOrigin();
                if (!(origin instanceof PyramidalCoverageReference)) {
                    //WMTS only handle PyramidalModel
                    LOGGER.log(Level.WARNING, "Layer {0} has not a PyramidalModel origin. It will not be included in capabilities", name);
                    continue;
                }

                try {
                    final PyramidalCoverageReference pmodel = (PyramidalCoverageReference) origin;
                    final PyramidSet set = pmodel.getPyramidSet();

                    final Envelope pyramidSetEnv = set.getEnvelope();
                    if (pyramidSetEnv == null) {
                        throw new CstlServiceException("No valid extent for layer " + name);
                    }

                    final CoordinateReferenceSystem pyramidSetEnvCRS = pyramidSetEnv.getCoordinateReferenceSystem();
                    final int xAxis = Math.max(0, CoverageUtilities.getMinOrdinate(pyramidSetEnvCRS));
                    final int yAxis = xAxis + 1;

                    /* We get pyramid set CRS components to identify additional dimensions. We remove horizontal component
                     * from the list to ease further operations, and prepare WMTS dimension descriptors. Dimension allowed
                     * values will be filled when we'll browse mosaics to build tile matrix capabilities.
                     */
                    final HashMap<Integer, Dimension> dims = new HashMap<>();
                    final Map<Integer, CoordinateReferenceSystem> splittedCRS =
                            ReferencingUtilities.indexedDecompose(pyramidSetEnvCRS);
                    final Iterator<Map.Entry<Integer, CoordinateReferenceSystem>> iterator = splittedCRS.entrySet().iterator();
                    while (iterator.hasNext()) {
                        final Map.Entry<Integer, CoordinateReferenceSystem> entry = iterator.next();
                        final CoordinateReferenceSystem tmpCRS = entry.getValue();
                        // If it's not a single dimension, It's not an additional dimension.
                        if (tmpCRS.getCoordinateSystem().getDimension() > 1) {
                            iterator.remove();
                        } else {
                            // TODO : we have no check for multiple temporal dimensions (is it possible to have more than one ?)
                            final Dimension dimension;
                            if (tmpCRS instanceof TemporalCRS) {
                                // current value is a special wmts case.
                                dimension = new Dimension(TIME_NAME, TIME_UNIT, "current");
                            } else {
                                final String dimName;
                                final CoordinateSystemAxis axis = tmpCRS.getCoordinateSystem().getAxis(0);
                                // vertical dimension name is fixed by 1.0.0 standard.
                                if (tmpCRS instanceof VerticalCRS) {
                                    dimName = ELEVATION_NAME;
                                } else {
                                    dimName = axis.getName().getCode();
                                }
                                dimension = new Dimension(dimName, axis.getUnit().toString(), "current");
                            }
                            dims.put(entry.getKey(), dimension);
                        }
                    }

                    final Collection<Pyramid> pyramids = set.getPyramids();
                    final List<BoundingBoxType> bboxList = new ArrayList<>();
                    for (Pyramid pyramid : pyramids) {
                        final GeneralEnvelope pyramidEnv = CoverageUtilities.getPyramidEnvelope(pyramid);
                        final int envXAxis = Math.max(0, CoverageUtilities.getMinOrdinate(pyramid.getCoordinateReferenceSystem()));
                        final int envYAxis = xAxis + 1;
                        final BoundingBoxType bbox = new WGS84BoundingBoxType(
                                getCRSCode(pyramid.getCoordinateReferenceSystem()),
                                pyramidEnv.getMinimum(envXAxis),
                                pyramidEnv.getMinimum(envYAxis),
                                pyramidEnv.getMaximum(envXAxis),
                                pyramidEnv.getMaximum(envYAxis));
                        bboxList.add(bbox);
                    }

                    final LayerType outputLayer = new LayerType(
                            name,
                            name,
                            name,
                            bboxList,
                            WMTSConstant.DEFAULT_STYLES,
                            new ArrayList<>(dims.values()));

                try {
                    final Envelope crs84Env = CRS.transform(pyramidSetEnv, CommonCRS.defaultGeographic());
                    outputLayer.getWGS84BoundingBox().add(new WGS84BoundingBoxType("CRS:84",
                            crs84Env.getMinimum(xAxis),
                            crs84Env.getMinimum(yAxis),
                            crs84Env.getMaximum(xAxis),
                            crs84Env.getMaximum(yAxis)));
                } catch (Exception e) {
                    // Optional parameter, we don't let exception make capabilities fail.
                    LOGGER.log(Level.FINE, "Input envelope cannot be reprojected in CRS:84.");
                }

                    final List<String> pformats = set.getFormats();
                    outputLayer.setFormat(pformats);

                    final List<URLTemplateType> resources = new ArrayList<>();
                    for (String pformat : pformats) {
                        String url = getServiceUrl();
                        url = url.substring(0, url.length() - 1) + "/" + name + "/{tileMatrixSet}/{tileMatrix}/{tileRow}/{tileCol}.{format}";
                        final URLTemplateType tileURL = new URLTemplateType(pformat, "tile", url);
                        resources.add(tileURL);
                    }
                    outputLayer.setResourceURL(resources);

                    for (Pyramid pr : pyramids) {
                        final TileMatrixSet tms = new TileMatrixSet();
                        tms.setIdentifier(new CodeType(pr.getId()));
                        tms.setSupportedCRS(getCRSCode(pr.getCoordinateReferenceSystem()));

                        final List<TileMatrix> tm = new ArrayList<>();
                        final double[] scales = pr.getScales();
                        for (int i = 0; i < scales.length; i++) {
                            final Iterator<GridMosaic> mosaicIt = pr.getMosaics(i).iterator();
                            if (!mosaicIt.hasNext()) {
                                continue;
                            }
                            final GridMosaic mosaic = mosaicIt.next();
                            DirectPosition upperLeft = mosaic.getUpperLeftCorner();
                            double scale = mosaic.getScale();
                            //convert scale in the strange WMTS scale denominator
                            scale = WMTSUtilities.toScaleDenominator(pr.getCoordinateReferenceSystem(), scale);
                            final TileMatrix matrix = new TileMatrix();
                            matrix.setIdentifier(new CodeType(mosaic.getId()));
                            matrix.setScaleDenominator(scale);
                            matrix.setMatrixDimension(mosaic.getGridSize());
                            matrix.setTileDimension(mosaic.getTileSize());
                            matrix.getTopLeftCorner().add(upperLeft.getOrdinate(xAxis));
                            matrix.getTopLeftCorner().add(upperLeft.getOrdinate(yAxis));
                            tm.add(matrix);

                            // Fill dimensions. We iterate over all mosaics of the current scale to find all slices.
                            int timeIndex = -1;
                            MathTransform toJavaTime = null;
                            final SimpleDateFormat dateFormatter = (SimpleDateFormat) ISO_8601_FORMATTER.clone();
                            for (Map.Entry<Integer, CoordinateReferenceSystem> entry : splittedCRS.entrySet()) {
                                String strValue;
                                // For temporal values, we convert it into timestamp, then to an ISO 8601 date.
                                final List<String> currentDimValues = dims.get(entry.getKey()).getValue();
                                if (entry.getValue() instanceof TemporalCRS) {
                                    timeIndex = entry.getKey();
                                    double value = upperLeft.getOrdinate(entry.getKey());
                                    if (!CRS.equalsApproximatively(JAVA_TIME, entry.getValue())) {
                                        final double[] tmpArray = new double[]{value};
                                        toJavaTime = CRS.findMathTransform(entry.getValue(), JAVA_TIME);
                                        toJavaTime.transform(tmpArray, 0, tmpArray, 0, 1);
                                        value = tmpArray[0];
                                    }

                                    strValue = dateFormatter.format(new Date((long) value));

                                } else {
                                    strValue = String.valueOf(upperLeft.getOrdinate(entry.getKey()));
                                }

                                if (strValue != null && !currentDimValues.contains(strValue)) {
                                    currentDimValues.add(strValue);
                                }
                            }

                            while (mosaicIt.hasNext()) {
                                upperLeft = mosaicIt.next().getUpperLeftCorner();
                                for (Map.Entry<Integer, CoordinateReferenceSystem> entry : splittedCRS.entrySet()) {
                                    String strValue = null;
                                    // For temporal values, we convert it into timestamp, then to an ISO 8601 date.
                                    final List<String> currentDimValues = dims.get(entry.getKey()).getValue();
                                    if (timeIndex == entry.getKey()) {
                                        double value = upperLeft.getOrdinate(entry.getKey());
                                        if (toJavaTime != null) {
                                            final double[] tmpArray = new double[]{value};
                                            toJavaTime.transform(tmpArray, 0, tmpArray, 0, 1);
                                            value = tmpArray[0];
                                        }

                                        strValue = dateFormatter.format(new Date((long) value));

                                    } else {
                                        strValue = String.valueOf(upperLeft.getOrdinate(entry.getKey()));
                                    }

                                    if (strValue != null && !currentDimValues.contains(strValue)) {
                                        currentDimValues.add(strValue);
                                    }
                                }
                            }
                        }
                        tms.setTileMatrix(tm);

                    /*
                     * Once our tile matrix set is defined, we must check if we've got one which is equal to the newly
                     * computed set.
                     * - If no matrix set is equal to the new one, and no matrix set with the same name exists, we add our
                     * matrix set to the service capabilities.
                     * - If we already have a tile matrix set equal to the current one, we just make a link to the old
                     * one.
                     * - If we've got two sets with the same name, but they're different, we rename the new matrix set
                     * to avoid mistakes. 
                     * 
                     * In all cases, we store a binding between the TMS identifier and the pyramid one to be able to
                     * retrieve pyramid at getTile request.
                     */
                        TileMatrixSet previousDefined = tileSets.get(pr.getId());
                        boolean equalSets = false;
                        if (previousDefined == null || !(equalSets = areEqual(tms, previousDefined))) {
                            for (final TileMatrixSet tmpSet : tileSets.values()) {
                                if (areEqual(tms, tmpSet)) {
                                    equalSets = true;
                                    previousDefined = tmpSet;
                                    break;
                                }
                            }
                        }

                        if (previousDefined == null) {
                            tileSets.put(pr.getId(), tms);

                        } else if (equalSets) {
                            tms.setIdentifier(previousDefined.getIdentifier());

                        } else {
                            // Two different matrix sets with same identifier. We'll change the name of the new one.
                            final String tmsUUID = UUID.randomUUID().toString();
                            tms.setIdentifier(new CodeType(tmsUUID));
                            tileSets.put(tmsUUID, tms);
                        }
                        
                        addTileMatrixSetBinding(tms.getIdentifier().getValue(), pr.getId());
                        

                        final TileMatrixSetLink tmsl = new TileMatrixSetLink(tms.getIdentifier().getValue());
                        outputLayer.addTileMatrixSetLink(tmsl);
                    }

                    outputLayers.add(outputLayer);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Cannot build matrix list of the following layer : " + name, ex);
                }
            }
            final ContentsType cont = new ContentsType(outputLayers, new ArrayList<>(tileSets.values()));

            // put full capabilities in cache
            final Capabilities c = new Capabilities(si, sp, om, "1.0.0", null, cont, themes);
            putCapabilitiesInCache("1.0.0", null, c);
            LOGGER.log(logLevel, "getCapabilities processed in {0}ms.\n", (System.currentTimeMillis() - start));
            return (Capabilities) c.applySections(sections);
        
        } finally {
            tmsBindingLock.writeLock().unlock();
        }
    }

    /**
     * Add a binding between a tile matrix set defined in service GetCapabilities,
     * and a pyramid set Id. The aim is to factorize the possible tile matrixes.
     * @param tmsId The ID of the {@link TileMatrixSet} to expose via GetCapabilities.
     * @param pyramidId The pyramid ID to add as valid pyramid for input TMS.
     * @return True if we successfully added the binding, false otherwise (in could already exist).
     */
    private boolean addTileMatrixSetBinding(final String tmsId, final String pyramidId) {
        tmsBindingLock.writeLock().lock();
        try {
            HashSet<String> bindings = tmsIdBinding.get(tmsId);
            if (bindings == null) {
                bindings = new HashSet<>();
                tmsIdBinding.put(tmsId, bindings);
            }
            return bindings.add(pyramidId);
        } finally {
            tmsBindingLock.writeLock().unlock();
        }
    }
    
    /**
     * Return CRS code name. As WMTS define only 2D CRS (additional dimensions are stored beside), we will extract
     * horizontal CRS, and search for a standard EPSG identifier. If we cannot find it, we will just keep CRS initial
     * code.
     * @param candidate The system to analyse.
     * @return An identifier for the horizontal part of input crs.
     */
    private String getCRSCode(CoordinateReferenceSystem candidate) {
        final SingleCRS horizontal = org.apache.sis.referencing.CRS.getHorizontalComponent(candidate);
        // Workaround to normalize WGS84 that return "EPSG:WGS 84"
        // for IdentifiedObjects.getIdentifierOrName() call
        if (CRS.equalsIgnoreMetadata(CommonCRS.WGS84.normalizedGeographic(), horizontal)) {
            return "CRS:84";
        } else {
            try {
                final Integer identifier = org.geotoolkit.referencing.IdentifiedObjects.lookupEpsgCode(
                        horizontal, true);
                if (identifier != null) {
                    return "EPSG:"+identifier;
                }
            } catch (FactoryException e) {
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            }
        }
        return IdentifiedObjects.getIdentifierOrName(candidate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map.Entry<String, Object> getFeatureInfo(GetFeatureInfo request) throws CstlServiceException {

        //       -- get the List of layer references
        final GetTile getTile       = request.getGetTile();
        final String userLogin      = getUserLogin();
        final Name layerName        = Util.parseLayerName(getTile.getLayer());
        final Data layerRef = getLayerReference(userLogin, layerName);
        final Layer configLayer     = getConfigurationLayer(layerName, userLogin);

        // build an equivalent style List
        final String styleName       = getTile.getStyle();
        final DataReference styleRef = configLayer.getStyle(styleName);
        final MutableStyle style     = getStyle(styleRef);


        Coverage c = null;
        //       -- create the rendering parameter Map
        Double elevation =  null;
        Date time        = null;
        final List<DimensionNameValue> dimensions = getTile.getDimensionNameValue();
        for (DimensionNameValue dimension : dimensions) {
            if (dimension.getName().equalsIgnoreCase("elevation")) {
                try {
                    elevation = Double.parseDouble(dimension.getValue());
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException("Unable to parse the elevation value", INVALID_PARAMETER_VALUE, "elevation");
                }
            }
            if (dimension.getName().equalsIgnoreCase("time")) {
                try {
                    time = TimeParser.toDate(dimension.getValue());
                } catch (ParseException ex) {
                    throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, "time");
                }
            }
        }
        final Map<String, Object> params = new HashMap<>();
        params.put("ELEVATION", elevation);
        params.put("TIME", time);
        final SceneDef sdef = new SceneDef();

        try {
            final MapContext context = PortrayalUtil.createContext(layerRef, style, params);
            sdef.setContext(context);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        // 2. VIEW
        final JTSEnvelope2D refEnv = new JTSEnvelope2D(c.getEnvelope());
        final double azimuth       = 0;//request.getAzimuth();
        final ViewDef vdef         = new ViewDef(refEnv,azimuth);


        // 3. CANVAS
        final java.awt.Dimension canvasDimension = null;//request.getSize();
        final Color background = null;
        final CanvasDef cdef = new CanvasDef(canvasDimension,background);

        // 4. SHAPE
        //     a
        final int pixelTolerance = 3;
        final int i = request.getI();
        final int j = request.getJ();
        if (i < 0 || i > canvasDimension.width) {
            throw new CstlServiceException("The requested point has an invalid X coordinate.", INVALID_POINT);
        }
        if (j < 0 || j > canvasDimension.height) {
            throw new CstlServiceException("The requested point has an invalid Y coordinate.", INVALID_POINT);
        }
        final Rectangle selectionArea = new Rectangle( request.getI()-pixelTolerance,
        		                               request.getJ()-pixelTolerance,
        		                               pixelTolerance*2,
        		                               pixelTolerance*2);

        // 5. VISITOR
        String infoFormat = request.getInfoFormat();
        if (infoFormat == null) {
            //Should not happen since the info format parameter is mandatory for the GetFeatureInfo request.
            infoFormat = MimeType.TEXT_PLAIN;
        }

        FeatureInfoFormat featureInfo = null;
        try {
            featureInfo = FeatureInfoUtilities.getFeatureInfoFormat( getConfiguration(), configLayer, infoFormat);
        } catch (ClassNotFoundException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        } catch (ConfigurationException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        if (featureInfo == null) {
            throw new CstlServiceException("INFO_FORMAT="+infoFormat+" not supported for layers : "+layerName, NO_APPLICABLE_CODE);
        }

        try {
            final Object result = featureInfo.getFeatureInfo(sdef, vdef, cdef, selectionArea, request);
            return new AbstractMap.SimpleEntry<>(infoFormat, result);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TileReference getTile(final GetTile request) throws CstlServiceException {

        //1 LAYER NOT USED FOR NOW
        final Name layerName = Util.parseLayerName(request.getLayer());
        final String userLogin  = getUserLogin();
//        final Layer configLayer = getConfigurationLayer(layerName, userLogin);

        // 2. STYLE NOT USED FOR NOW
//        final String styleName    = request.getStyle();
//        final DataReference styleRef = configLayer.getStyle(styleName);
//        final MutableStyle style  = getStyle(styleRef);

        // 3. Get and check parameters
        final int columnIndex         = request.getTileCol();
        final int rowIndex            = request.getTileRow();
        final String level            = request.getTileMatrix();
        
        String matrixSetName   = request.getTileMatrixSet();
        final HashSet<String> validPyramidNames;
        tmsBindingLock.readLock().lock();
        try {
            final HashSet<String> idBinding = tmsIdBinding.get(matrixSetName);
            if (idBinding != null && !idBinding.isEmpty()) {
                validPyramidNames = idBinding;
            } else {
                validPyramidNames = new HashSet<>(1);
                validPyramidNames.add(matrixSetName);
            }
        } finally {
            tmsBindingLock.readLock().unlock();
        }

        if (columnIndex < 0 || rowIndex < 0) {
            throw new CstlServiceException("Operation request contains an invalid parameter value, " +
                    "TileCol and TileRow must be positive integers. Received position : " +
                    new Point(columnIndex, rowIndex), INVALID_PARAMETER_VALUE, "TileCol or TileRow");
        }

        try {
            final Data details = getLayerReference(userLogin, layerName);
            if (details == null) {
                throw new CstlServiceException("Operation request contains an invalid parameter value, "
                        + "No layer for name : " + layerName,
                        INVALID_PARAMETER_VALUE, "layerName");
            }

            final Object origin = details.getOrigin();
            if (!(origin instanceof PyramidalCoverageReference)) {
                //WMTS only handle PyramidalCoverageReference
                throw new CstlServiceException("Operation request contains an invalid parameter value, "
                        + "invalid layer : " + layerName + " , layer is not a pyramid model " + layerName,
                        INVALID_PARAMETER_VALUE, "layerName");
            }

            final PyramidSet set = ((PyramidalCoverageReference) origin).getPyramidSet();
            Pyramid pyramid = null;
            for (Pyramid pr : set.getPyramids()) {
                if (validPyramidNames.contains(pr.getId())) {
                    pyramid = pr;
                    break;
                }
            }
            if (pyramid == null) {
                throw new CstlServiceException("Operation request contains an invalid parameter value,"
                        + " undefined matrixSet: " + matrixSetName + " for layer: " + layerName,
                        INVALID_PARAMETER_VALUE, "tilematrixset");
            }

            GridMosaic mosaic = null;
            for (GridMosaic gm : pyramid.getMosaics()) {
                if (gm.getId().equals(level)) {
                    mosaic = gm;
                    break;
                }
            }

            // 4. If we found a base mosaic and user specified additional dimensions, we try to switch on the right slice.
            final List<DimensionNameValue> dimensions = request.getDimensionNameValue();
            if (mosaic != null && dimensions != null && !dimensions.isEmpty()) {
                final GeneralEnvelope envelope = envelopeFromDimensions(mosaic.getEnvelope(), dimensions);
                // We use a strict finder, because default one (as methods in coverage utilities) return arbitrary data
                // if it don't find any fitting mosaic...
                StrictlyCoverageFinder finder = new StrictlyCoverageFinder();
                mosaic = finder.findMosaic(pyramid, mosaic.getScale(), RESOLUTION_EPSILON, envelope, -1);
            }

            if (mosaic == null) {
                throw new CstlServiceException("Operation request contains an invalid parameter value," +
                        " undefined matrix: " + level + " for matrixSet: " + matrixSetName,
                        INVALID_PARAMETER_VALUE, "tilematrix");
            }

            if (columnIndex >= mosaic.getGridSize().width) {
                throw new CstlServiceException("TileCol out of range, expected value < "+mosaic.getGridSize().width+" but got " + columnIndex,
                        TILE_OUT_OF_RANGE, "tilecol");
            }
            if (rowIndex >= mosaic.getGridSize().height) {
                throw new CstlServiceException("TileRow out of range, expected value < " + mosaic.getGridSize().height + " but got "+rowIndex,
                        TILE_OUT_OF_RANGE, "tilerow");
            }

            if (mosaic.isMissing(columnIndex, rowIndex)) {
                return emptyTile(mosaic, columnIndex, rowIndex);
            } else {
                return mosaic.getTile(columnIndex, rowIndex, null);
            }

        } catch(CstlServiceException ex) {
            throw ex;
        } catch(Exception ex) {
            throw new CstlServiceException("Unexpected error for operation GetTile  : "+ layerName, ex , NO_APPLICABLE_CODE);
        }
    }

    /**
     * Create empty TileReference with black image as input.
     * @param mosaic
     * @param columnIndex
     * @param rowIndex
     * @return TileReference
     */
    private TileReference emptyTile(final GridMosaic mosaic, final int columnIndex, final int rowIndex) {
        return  new TileReference() {
            @Override
            public ImageReader getImageReader() throws IOException {
                return null;
            }

            @Override
            public ImageReaderSpi getImageReaderSpi() {
                return null;
            }

            @Override
            public Object getInput() {
                //TODO cache empty image
                Color color = new Color(0x00FFFFFF, true);
                return Cstl.getPortrayalService().writeBlankImage(color,mosaic.getTileSize());
            }

            @Override
            public int getImageIndex() {
                return 0;
            }

            @Override
            public Point getPosition() {
                return new Point(columnIndex, rowIndex);
            }
        };
    }

    /**
     * Change range values of input envelope for all dimensions specified in given dimension list.
     * @param envelope The envelope containing base values for dimensions to change. Not modified, a copy is performed.
     * @param dimensions Dimensions to override.
     * @return An envelope containing same values as input, except for ranges specified in dimensions parameter.
     * @throws ParseException
     * @throws FactoryException
     * @throws TransformException
     */
    private static GeneralEnvelope envelopeFromDimensions(final Envelope envelope, List<DimensionNameValue> dimensions) throws ParseException, FactoryException, TransformException {
        final GeneralEnvelope result = new GeneralEnvelope(envelope);
        final CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();
        final Map<Integer, CoordinateReferenceSystem> systems = ReferencingUtilities.indexedDecompose(crs);
        CoordinateReferenceSystem currentCRS;
        for (final Map.Entry<Integer, CoordinateReferenceSystem> entry : systems.entrySet()) {
            currentCRS = entry.getValue();
            // Not an additional dimension, it must be horizontal CRS.
            if (currentCRS.getCoordinateSystem().getDimension() > 1) {
                continue;
            }

            if (currentCRS instanceof TemporalCRS) {
                for (final DimensionNameValue dim : dimensions) {
                    if (dim.getName().equalsIgnoreCase(TIME_NAME)) {
                        if (dim.getValue().equalsIgnoreCase(CURRENT_VALUE)) break;
                        final long timestamp = TimeParser.toDate(dim.getValue()).getTime();
                        // We don't know what is the CRS of our envelope, but WMTS exposes times as ISO 8601, so a
                        // conversion may be needed.
                        if (CRS.equalsApproximatively(currentCRS, JAVA_TIME)) {
                            // put a minimal epsilon.
                            result.setRange(entry.getKey(), timestamp - 1, timestamp + 1);
                        } else {
                            final double[] time = new double[1];
                            CRS.findMathTransform(JAVA_TIME, currentCRS, true).transform(time, 0, time, 0, 1);
                            result.setRange(entry.getKey(), time[0], time[0]);
                        }
                        break;
                    }
                }

            } else {
                final String axisName;
                if (currentCRS instanceof VerticalCRS) {
                    axisName = ELEVATION_NAME; // Fixed in WMTS standard.
                } else {
                    axisName = currentCRS.getCoordinateSystem().getAxis(0).getName().getCode();
                }
                for (final DimensionNameValue dim : dimensions) {
                    if (dim.getName().equalsIgnoreCase(axisName)) {
                        if (dim.getValue().equalsIgnoreCase(CURRENT_VALUE)) break;
                        final double value = Double.parseDouble(dim.getValue());
                        // put a minimal epsilon.
                        result.setRange(entry.getKey(), value, value);
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Test the equality of 2 {@link org.geotoolkit.wmts.xml.v100.TileMatrixSet}, ignoring their name.
     *
     * We check their bounding box, CRS and list of tile matrixes.
     * @param tms1
     * @param tms2
     * @return
     */
    private static boolean areEqual(final TileMatrixSet tms1, TileMatrixSet tms2) {
        if (!tms1.getSupportedCRS().equals(tms2.getSupportedCRS())) return false;

        final BoundingBoxType bbox1 = (tms1.getBoundingBox() == null)? null : tms1.getBoundingBox().getValue();
        final BoundingBoxType bbox2 = (tms2.getBoundingBox() == null)? null : tms2.getBoundingBox().getValue();
        if (bbox1 != null? !bbox1.equals(bbox2) : bbox2 != null) return false;

        final List<TileMatrix> sourceMatrixes = tms1.getTileMatrix();
        final List<TileMatrix> targetMatrixes = tms2.getTileMatrix();
        return (targetMatrixes == null ? sourceMatrixes == null : targetMatrixes.equals(sourceMatrixes));
    }
}
