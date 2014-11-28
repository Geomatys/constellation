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
package org.constellation.coverage.ws;

// J2SE dependencies

import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.api.QueryConstants;
import org.constellation.configuration.Layer;
import org.constellation.dto.Details;
import org.constellation.portrayal.PortrayalUtil;
import org.constellation.provider.CoverageData;
import org.constellation.provider.Data;
import org.constellation.util.DataReference;
import org.constellation.util.WCSUtils;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.LayerWorker;
import org.constellation.ws.MimeType;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.gml.xml.v311.GridType;
import org.geotoolkit.gml.xml.v311.RectifiedGridType;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.ows.xml.AbstractCapabilitiesCore;
import org.geotoolkit.ows.xml.AbstractOperationsMetadata;
import org.geotoolkit.ows.xml.AbstractServiceIdentification;
import org.geotoolkit.ows.xml.AbstractServiceProvider;
import org.geotoolkit.ows.xml.AcceptFormats;
import org.geotoolkit.ows.xml.Sections;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.ows.xml.v110.WGS84BoundingBoxType;
import org.geotoolkit.referencing.CRS;
import org.apache.sis.referencing.CommonCRS;
import org.geotoolkit.resources.Errors;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.TimeParser;
import org.geotoolkit.wcs.xml.Content;
import org.geotoolkit.wcs.xml.CoverageInfo;
import org.geotoolkit.wcs.xml.DescribeCoverage;
import org.geotoolkit.wcs.xml.DescribeCoverageResponse;
import org.geotoolkit.wcs.xml.GetCapabilities;
import org.geotoolkit.wcs.xml.GetCapabilitiesResponse;
import org.geotoolkit.wcs.xml.GetCoverage;
import org.geotoolkit.wcs.xml.WCSMarshallerPool;
import org.geotoolkit.wcs.xml.WCSXmlFactory;
import org.geotoolkit.wcs.xml.v100.CoverageOfferingType;
import org.geotoolkit.wcs.xml.v100.DomainSetType;
import org.geotoolkit.wcs.xml.v100.InterpolationMethod;
import org.geotoolkit.wcs.xml.v100.LonLatEnvelopeType;
import org.geotoolkit.wcs.xml.v100.RangeSetType;
import org.geotoolkit.wcs.xml.v100.SupportedCRSsType;
import org.geotoolkit.wcs.xml.v100.SupportedFormatsType;
import org.geotoolkit.wcs.xml.v100.SupportedInterpolationsType;
import org.geotoolkit.wcs.xml.v111.CoverageDescriptionType;
import org.geotoolkit.wcs.xml.v111.CoverageDomainType;
import org.geotoolkit.wcs.xml.v111.FieldType;
import org.geotoolkit.wcs.xml.v111.GridCrsType;
import org.geotoolkit.wcs.xml.v111.InterpolationMethods;
import org.geotoolkit.wcs.xml.v111.RangeType;
import org.opengis.coverage.grid.RectifiedGrid;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.text.ParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Level;

import org.apache.sis.util.CharSequences;
import static org.constellation.coverage.ws.WCSConstant.ASCII_GRID;
import static org.constellation.coverage.ws.WCSConstant.GEOTIFF;
import static org.constellation.coverage.ws.WCSConstant.INTERPOLATION_V100;
import static org.constellation.coverage.ws.WCSConstant.INTERPOLATION_V111;
import static org.constellation.coverage.ws.WCSConstant.KEY_BBOX;
import static org.constellation.coverage.ws.WCSConstant.KEY_COVERAGE;
import static org.constellation.coverage.ws.WCSConstant.KEY_CRS;
import static org.constellation.coverage.ws.WCSConstant.KEY_FORMAT;
import static org.constellation.coverage.ws.WCSConstant.KEY_IDENTIFIER;
import static org.constellation.coverage.ws.WCSConstant.KEY_INTERPOLATION;
import static org.constellation.coverage.ws.WCSConstant.KEY_RESPONSE_CRS;
import static org.constellation.coverage.ws.WCSConstant.KEY_SECTION;
import static org.constellation.coverage.ws.WCSConstant.KEY_TIME;
import static org.constellation.coverage.ws.WCSConstant.MATRIX;
import static org.constellation.coverage.ws.WCSConstant.NETCDF;
import static org.constellation.coverage.ws.WCSConstant.SUPPORTED_FORMATS_100;
import static org.constellation.coverage.ws.WCSConstant.SUPPORTED_FORMATS_111;
import static org.constellation.coverage.ws.WCSConstant.SUPPORTED_INTERPOLATIONS_V100;
import static org.constellation.coverage.ws.WCSConstant.getOperationMetadata;
import static org.geotoolkit.ows.xml.OWSExceptionCode.CURRENT_UPDATE_SEQUENCE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_CRS;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_DIMENSION_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_FORMAT;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_UPDATE_SEQUENCE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.LAYER_NOT_DEFINED;
import static org.geotoolkit.ows.xml.OWSExceptionCode.LAYER_NOT_QUERYABLE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.VERSION_NEGOTIATION_FAILED;

// GeoAPI dependencies


/**
 * Worker for the WCS services in Constellation which services both the REST and
 * SOAP facades by issuing appropriate responses.
 * <p>
 * The classes implementing the REST or SOAP facades to this service will have
 * processed the requests sufficiently to ensure that all the information
 * conveyed by the HTTP request is in one of the fields of the object passed
 * to the worker methods as a parameter.
 * </p>
 *
 * @version 0.9
 *
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 * @since 0.3
 */
public final class DefaultWCSWorker extends LayerWorker implements WCSWorker {


    public DefaultWCSWorker(final String id) {
        super(id, ServiceDef.Specification.WCS);
        if (isStarted) {
            LOGGER.log(Level.INFO, "WCS worker {0} running", id);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected MarshallerPool getMarshallerPool() {
        return WCSMarshallerPool.getInstance();
    }

    /**
     * The DescribeCoverage operation returns an XML file, containing the
     * complete description of the specific coverages requested.
     * <p>
     * This method extends the definition of each coverage given in the
     * Capabilities document with supplementary information.
     * </p>
     *
     * @param request A {@linkplain AbstractDescribeCoverage request} with the
     *                parameters of the user message.
     * @return An XML document giving the full description of the requested coverages.
     * @throws CstlServiceException
     */
    @Override
    public DescribeCoverageResponse describeCoverage(final DescribeCoverage request) throws CstlServiceException {
        isWorking();
        final String version   = request.getVersion().toString();
        final String userLogin = getUserLogin();
        if (version.isEmpty()) {
            throw new CstlServiceException("The parameter VERSION must be specified.",
                           MISSING_PARAMETER_VALUE, QueryConstants.VERSION_PARAMETER.toLowerCase());
        }

        if (request.getIdentifier().isEmpty()) {
            throw new CstlServiceException("The parameter IDENTIFIER must be specified",
                    MISSING_PARAMETER_VALUE, KEY_IDENTIFIER.toLowerCase());
        }

        final List<CoverageInfo> coverageOfferings = new ArrayList<>();
        for (String coverage : request.getIdentifier()) {

            final Name tmpName = parseCoverageName(coverage);
            final Data layerRef = getLayerReference(userLogin, tmpName);
            if (layerRef.getType().equals(Data.TYPE.FEATURE)) {
                throw new CstlServiceException("The requested layer is vectorial. WCS is not able to handle it.",
                        LAYER_NOT_DEFINED, KEY_COVERAGE.toLowerCase());
            }
            if (!(layerRef instanceof CoverageData)) {
                // Should not occurs, since we have previously verified the type of layer.
                throw new CstlServiceException("The requested layer is not a coverage. WCS is not able to handle it.",
                        LAYER_NOT_DEFINED, KEY_COVERAGE.toLowerCase());
            }

            final CoverageData coverageRef = (CoverageData) layerRef;
            if (!coverageRef.isQueryable(ServiceDef.Query.WCS_ALL)) {
                throw new CstlServiceException("You are not allowed to request the layer \"" +
                        coverage + "\".", LAYER_NOT_QUERYABLE, KEY_COVERAGE.toLowerCase());
            }

            final Layer configLayer = getConfigurationLayer(layerRef.getName(), userLogin);
            final Name fullCoverageName = coverageRef.getName();
            final String coverageName;
            if (configLayer.getAlias() != null && !configLayer.getAlias().isEmpty()) {
                coverageName = configLayer.getAlias().trim().replaceAll(" ", "_");
            } else {
                if (fullCoverageName.getNamespaceURI() != null && !fullCoverageName.getNamespaceURI().isEmpty()) {
                    coverageName = fullCoverageName.getNamespaceURI() + ':' + fullCoverageName.getLocalPart();
                } else {
                    coverageName = fullCoverageName.getLocalPart();
                }
            }
            if (version.equals("1.0.0")) {
                coverageOfferings.add(describeCoverage100(coverageName, coverageRef));
            } else if (version.equals("1.1.1")) {
                coverageOfferings.add(describeCoverage111(coverageName, coverageRef));
            } else {
                throw new CstlServiceException("The version number specified for this GetCoverage request " +
                        "is not handled.", NO_APPLICABLE_CODE, QueryConstants.VERSION_PARAMETER.toLowerCase());
            }
        }
        return WCSXmlFactory.createDescribeCoverageResponse(version, coverageOfferings);
    }

    /**
     * Returns the description of the coverage requested in version 1.0.0 of WCS standard.
     *
     * @param request a {@linkplain org.geotoolkit.wcs.xml.v100.DescribeCoverage describe coverage}
     *                request done by the user.
     * @return an XML document giving the full description of a coverage, in version 1.0.0.
     *
     * @throws CstlServiceException
     */
    private  CoverageInfo describeCoverage100(final String coverageName, final CoverageData coverageRef) throws CstlServiceException {

        try {
            final GeographicBoundingBox inputGeoBox = coverageRef.getGeographicBoundingBox();

            final LonLatEnvelopeType llenvelope;
            final EnvelopeType envelope;
            if (inputGeoBox != null) {
                final SortedSet<Number> elevations = coverageRef.getAvailableElevations();
                final List<DirectPositionType> pos = WCSUtils.buildPositions(inputGeoBox, elevations);
                llenvelope = new LonLatEnvelopeType(pos, "urn:ogc:def:crs:OGC:1.3:CRS84");
                envelope   = new EnvelopeType(pos, "EPSG:4326");
            } else {
                throw new CstlServiceException("The geographic bbox for the layer is null !",
                        NO_APPLICABLE_CODE);
            }
            final List<String> keywords = Arrays.asList("WCS", coverageName);

            /*
             * Spatial metadata
             */
            final EnvelopeType nativeEnvelope = new EnvelopeType(coverageRef.getEnvelope());

            GridType grid = null;
            try {
                SpatialMetadata meta = coverageRef.getSpatialMetadata();
                if (meta != null) {
                    RectifiedGrid brutGrid =  meta.getInstanceForType(RectifiedGrid.class);
                    if (brutGrid != null) {
                        grid = new RectifiedGridType(brutGrid);
                        /*
                         * UGLY PATCH : remove it when geotk will fill this data
                         */
                        if (grid.getDimension() == 0) {
                            int dimension = brutGrid.getOffsetVectors().size();
                            grid.setDimension(dimension);
                        }
                        if (grid.getAxisName().isEmpty()) {
                            if (grid.getDimension() == 2) {
                                grid.setAxisName(Arrays.asList("x", "y"));
                            } else if (grid.getDimension() == 3) {
                                grid.setAxisName(Arrays.asList("x", "y", "z"));
                            }
                        }
                    }
                }
            } catch (DataStoreException ex) {
                LOGGER.log(Level.WARNING, "Unable to get coverage spatial metadata", ex);
            }

            final org.geotoolkit.wcs.xml.v100.SpatialDomainType spatialDomain =
                new org.geotoolkit.wcs.xml.v100.SpatialDomainType(Arrays.asList(envelope, nativeEnvelope), Arrays.asList(grid));

            // temporal metadata
            final List<Object> times = WCSUtils.formatDateList(coverageRef.getAvailableTimes());
            final DomainSetType domainSet = new DomainSetType(spatialDomain, times);
            //TODO complete
            final RangeSetType rangeSet   = new RangeSetType(null, coverageName, coverageName, null, null, null, null);
            //supported CRS
            final SupportedCRSsType supCRS = new SupportedCRSsType("EPSG:4326");
            supCRS.addNativeCRSs(nativeEnvelope.getSrsName());

            // supported formats
            String nativeFormat = coverageRef.getImageFormat();
            if (nativeFormat == null || nativeFormat.isEmpty()) {
                nativeFormat = "unknown";
            }
            final SupportedFormatsType supForm = new SupportedFormatsType(nativeFormat, SUPPORTED_FORMATS_100);

            //supported interpolations
            final SupportedInterpolationsType supInt = INTERPOLATION_V100;

            //we build the coverage offering for this layer/coverage
            CharSequence remarks = CharSequences.toASCII(coverageRef.getRemarks());
            return new CoverageOfferingType(null, coverageName,
                    coverageName, (remarks != null) ? remarks.toString() : null, llenvelope,
                    keywords, domainSet, rangeSet, supCRS, supForm, supInt);
        } catch (DataStoreException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }
    }


    /**
     * Returns the description of the coverage requested in version 1.1.1 of WCS standard.
     *
     * @param request a {@linkplain org.geotoolkit.wcs.xml.v111.DescribeCoverage describe coverage}
     *                request done by the user.
     * @return an XML document giving the full description of a coverage, in version 1.1.1.
     *
     * @throws CstlServiceException
     */
    private  CoverageInfo describeCoverage111(final String coverageName, final CoverageData coverageRef) throws CstlServiceException {
        try {
            final GeographicBoundingBox inputGeoBox = coverageRef.getGeographicBoundingBox();

            WGS84BoundingBoxType outputBBox = null;
            if (inputGeoBox != null) {
                outputBBox = new WGS84BoundingBoxType(inputGeoBox);
            }
            /*
             * Spatial metadata
             */
            final BoundingBoxType nativeEnvelope = new BoundingBoxType(coverageRef.getEnvelope());

            GridCrsType grid = null;
            try {
                SpatialMetadata meta = coverageRef.getSpatialMetadata();
                if (meta != null) {
                    RectifiedGrid brutGrid =  meta.getInstanceForType(RectifiedGrid.class);
                    if (brutGrid != null) {
                        grid = new GridCrsType(brutGrid);
                    }
                }
            } catch (DataStoreException ex) {
                LOGGER.log(Level.WARNING, "Unable to get coverage spatial metadata", ex);
            }

            // spatial metadata
            final org.geotoolkit.wcs.xml.v111.SpatialDomainType spatial =
                    new org.geotoolkit.wcs.xml.v111.SpatialDomainType(outputBBox,nativeEnvelope, grid, null, null, null);

            //general metadata
            final String title     = coverageName;
            final CharSequence abstractt = CharSequences.toASCII(coverageRef.getRemarks());
            final List<String> keywords = Arrays.asList("WCS", coverageName);

            // temporal metadata
            final List<Object> times = WCSUtils.formatDateList(coverageRef.getAvailableTimes());
            final CoverageDomainType domain = new CoverageDomainType(spatial, times);

            //supported interpolations
            final InterpolationMethods interpolations = INTERPOLATION_V111;

            final CharSequence thematic = CharSequences.toASCII(coverageRef.getThematic());
            final RangeType range = new RangeType(new FieldType((thematic != null) ? thematic.toString() : null,
                    null, new org.geotoolkit.ows.xml.v110.CodeType("0.0"), interpolations));

            //supported CRS
            final List<String> supportedCRS = Arrays.asList("EPSG:4326");

            return new CoverageDescriptionType(title, (abstractt != null) ? abstractt.toString() : null,
                    keywords, coverageName, domain, range, supportedCRS, SUPPORTED_FORMATS_111);
        } catch (DataStoreException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * Describe the capabilities and the layers available for the WCS service.
     *
     * @param request The request done by the user.
     * @return a WCSCapabilities XML document describing the capabilities of this service.
     *
     * @throws CstlServiceException
     */
    @Override
    public GetCapabilitiesResponse getCapabilities(final GetCapabilities request) throws CstlServiceException {
        isWorking();
        //we begin by extract the base attribute
        String version         = request.getVersion().toString();
        final String userLogin = getUserLogin();
        if (version.isEmpty()) {
            // For the moment the only version that we really support is this one.
            version = "1.0.0";
        }

        //set the current updateSequence parameter
        final boolean returnUS = returnUpdateSequenceDocument(request.getUpdateSequence(), version);
        if (returnUS) {
            return WCSXmlFactory.createCapabilitiesResponse(version, getCurrentUpdateSequence());
        }

        /*
         * In WCS 1.0.0 the user can request only one section
         * ( or all by omitting the parameter section)
         */
        final Sections sections = request.getSections();
        if (sections != null && !sections.getSection().isEmpty()) {
            for (String sec : sections.getSection()) {
                if (!SectionsType.getExistingSections(version).contains(sec)) {
                    throw new CstlServiceException("This sections " + sec + " is not allowed", INVALID_PARAMETER_VALUE, KEY_SECTION.toLowerCase());
                }
            }
        }

        // if the user have specified one format accepted (only one for now != spec)
        final String format;
        if (version.equals("1.1.1")) {
            final AcceptFormats formats = request.getAcceptFormats();
            if (formats == null || formats.getOutputFormat().isEmpty()) {
                format = MimeType.TEXT_XML;
            } else {
                format = formats.getOutputFormat().get(0);
                if (!format.equals(MimeType.TEXT_XML) && !format.equals(MimeType.APP_XML)) {
                    throw new CstlServiceException("This format " + format + " is not allowed",
                            INVALID_FORMAT, KEY_FORMAT.toLowerCase());
                }
            }
        }

        // If the getCapabilities response is in cache, we just return it.
        final AbstractCapabilitiesCore cachedCapabilities = getCapabilitiesFromCache(version, null);
        if (cachedCapabilities != null) {
            return (GetCapabilitiesResponse) cachedCapabilities.applySections(sections);
        }

        // We unmarshall the static capabilities document.
        final Details skeleton = getStaticCapabilitiesObject("WCS", null);
        final GetCapabilitiesResponse staticCapabilities = WCSConstant.createCapabilities(version, skeleton);
        final AbstractServiceIdentification si  = staticCapabilities.getServiceIdentification();
        final AbstractServiceProvider sp        = staticCapabilities.getServiceProvider();
        final AbstractOperationsMetadata om     = getOperationMetadata(version);
        om.updateURL(getServiceUrl());


        final List<CoverageInfo> offBrief = new ArrayList<>();
        final List<Layer> layers = getConfigurationLayers(userLogin);
        try {
            for (Layer configLayer : layers) {
                final Data layer = getLayerReference(userLogin, configLayer.getName());

                if (layer.getType().equals(Data.TYPE.FEATURE)) {
                    continue;
                }
                if (!layer.isQueryable(ServiceDef.Query.WCS_ALL)) {
                    continue;
                }
                if (layer.getGeographicBoundingBox() == null) {
                    // The coverage does not contain geometric information, we do not want this coverage
                    // in the capabilities response.
                    continue;
                }

                final CoverageInfo co;
                if (version.equals("1.0.0")) {
                    co = getCoverageInfo100(layer, configLayer);
                } else if (version.equals("1.1.1")) {
                    co = getCoverageInfo111(layer, configLayer);
                } else {
                    throw new CstlServiceException("The version number specified for this request " +
                            "is not handled.", VERSION_NEGOTIATION_FAILED, QueryConstants.VERSION_PARAMETER.toLowerCase());
                }
                /*
                * coverage brief customisation
                */
                if (configLayer.getTitle() != null) {
                   co.setTitle(configLayer.getTitle());
                }
                if (configLayer.getAbstrac() != null) {
                   co.setAbstract(configLayer.getAbstrac());
                }
                if (configLayer.getKeywords() != null && !configLayer.getKeywords().isEmpty()) {
                   co.setKeywordValues(configLayer.getKeywords());
                }
                if (configLayer.getMetadataURL() != null && configLayer.getMetadataURL().getOnlineResource() != null) {
                    co.setMetadata(configLayer.getMetadataURL().getOnlineResource().getValue());
                }
                offBrief.add(co);
            }
        }   catch (DataStoreException exception) {
            throw new CstlServiceException(exception, NO_APPLICABLE_CODE);
        }
        final Content contents = WCSXmlFactory.createContent("1.0.0", offBrief);
        final GetCapabilitiesResponse response = WCSXmlFactory.createCapabilitiesResponse(version, si, sp, om, contents, getCurrentUpdateSequence());
        putCapabilitiesInCache(version, null, response);
        return (GetCapabilitiesResponse) response.applySections(sections);
    }

    /**
     * Returns the {@linkplain GetCapabilitiesResponse GetCapabilities} response of the request
     * given by parameter, in version 1.0.0 of WCS.
     *
     * @param request The request done by the user, in version 1.0.0.
     * @return a WCSCapabilities XML document describing the capabilities of this service.
     *
     * @throws CstlServiceException
     * @throws JAXBException when unmarshalling the default GetCapabilities file.
     */
    private CoverageInfo getCoverageInfo100(final Data layer, final Layer configLayer) throws DataStoreException {

        final Name fullLayerName = layer.getName();
        final String layerName;
        if (configLayer.getAlias() != null && !configLayer.getAlias().isEmpty()) {
            layerName = configLayer.getAlias().trim().replaceAll(" ", "_");
        } else {
            if (fullLayerName.getNamespaceURI() != null && !fullLayerName.getNamespaceURI().isEmpty()) {
                layerName = fullLayerName.getNamespaceURI() + ':' + fullLayerName.getLocalPart();
            } else {
                layerName = fullLayerName.getLocalPart();
            }
        }
        final GeographicBoundingBox inputGeoBox = layer.getGeographicBoundingBox();
        final List<DirectPositionType> pos      = WCSUtils.buildPositions(inputGeoBox, layer.getAvailableElevations());
        final LonLatEnvelopeType outputBBox     = new LonLatEnvelopeType(pos, "urn:ogc:def:crs:OGC:1.3:CRS84");

        final SortedSet<Date> dates = layer.getAvailableTimes();
        if (dates != null && dates.size() >= 2) {
            /*
             * Adds the first and last date available, since in the WCS GetCapabilities,
             * it is a brief description of the capabilities.
             * To get the whole available values, the describeCoverage request has to be
             * done on a specific coverage.
             */
            final Date firstDate = dates.first();
            final Date lastDate = dates.last();
            synchronized(WCSUtils.FORMATTER) {
                outputBBox.addTimePosition(WCSUtils.FORMATTER.format(firstDate), WCSUtils.FORMATTER.format(lastDate));
            }
        }

        return WCSXmlFactory.createCoverageInfo("1.0.0", layerName, layerName, null, outputBBox);
    }

    /**
     * Returns the {@linkplain GetCapabilitiesResponse GetCapabilities} response of the request given
     * by parameter, in version 1.1.1 of WCS.
     *
     * @param request The request done by the user, in version 1.1.1.
     * @return a WCSCapabilities XML document describing the capabilities of this service.
     *
     * @throws CstlServiceException
     */
    private CoverageInfo getCoverageInfo111(final Data layer, final Layer configLayer) throws DataStoreException {

        final CoverageData coverageLayer = (CoverageData)layer;
        final String identifier;
        if (configLayer.getAlias() != null && !configLayer.getAlias().isEmpty()) {
            identifier = configLayer.getAlias().trim().replaceAll(" ", "_");
        } else {
            identifier = coverageLayer.getName().getLocalPart();
        }

        final String title      = coverageLayer.getName().getLocalPart();
        final CharSequence remark = CharSequences.toASCII(coverageLayer.getRemarks());

        final GeographicBoundingBox inputGeoBox = coverageLayer.getGeographicBoundingBox();
        final WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(inputGeoBox);

        return WCSXmlFactory.createCoverageInfo("1.1.1", identifier, title,
                (remark != null) ? remark.toString() : null, outputBBox);
    }


    /**
     * Get the coverage values for a specific coverage specified.
     * According to the output format chosen, the response could be an
     * {@linkplain RenderedImage image} or data representation.
     *
     * @param request The request done by the user.
     * @return An {@linkplain RenderedImage image}, or a data representation.
     *
     * @throws CstlServiceException
     */
    @Override
    public Object getCoverage(final GetCoverage request) throws CstlServiceException {
        isWorking();
        final String inputVersion = request.getVersion().toString();
        final String userLogin    = getUserLogin();
        if (inputVersion == null) {
            throw new CstlServiceException("The parameter version must be specified",
                           MISSING_PARAMETER_VALUE, QueryConstants.VERSION_PARAMETER.toLowerCase());
        } else if (!"1.0.0".equals(inputVersion) && !"1.1.1".equals(inputVersion)) {
            throw new CstlServiceException("The version number specified for this request " + inputVersion +
                    " is not handled.", VERSION_NEGOTIATION_FAILED, QueryConstants.VERSION_PARAMETER.toLowerCase());
        }

        Date date = null;
        try {
            date = TimeParser.toDate(request.getTime());
        } catch (ParseException ex) {
            throw new CstlServiceException("Parsing of the date failed. Please verify that the specified" +
                    " date is compliant with the ISO-8601 standard.", ex, INVALID_PARAMETER_VALUE,
                    KEY_TIME.toLowerCase());
        }

        final String coverageName = request.getCoverage();
        if (coverageName == null) {
            throw new CstlServiceException("You must specify the parameter: COVERAGE" , INVALID_PARAMETER_VALUE,
                    KEY_COVERAGE.toLowerCase());
        }
        final Name tmpName = parseCoverageName(request.getCoverage());
        final Data tmplayerRef = getLayerReference(userLogin, tmpName);
        if (!tmplayerRef.isQueryable(ServiceDef.Query.WCS_ALL) || tmplayerRef.getType().equals(Data.TYPE.FEATURE)) {
            throw new CstlServiceException("You are not allowed to request the layer \"" +
                    tmplayerRef.getName() + "\".", INVALID_PARAMETER_VALUE, KEY_COVERAGE.toLowerCase());
        }
        if (!(tmplayerRef instanceof CoverageData)) {
                // Should not occurs, since we have previously verified the type of layer.
                throw new CstlServiceException("The requested layer is not a coverage. WCS is not able to handle it.",
                        LAYER_NOT_DEFINED, KEY_COVERAGE.toLowerCase());
        }
        final CoverageData layerRef = (CoverageData) tmplayerRef;
        final Layer configLayer = getConfigurationLayer(tmpName, userLogin);

        // we verify the interpolation method even if we don't use it
        try {
            if (request.getInterpolationMethod() != null) {
                final InterpolationMethod interpolation = (InterpolationMethod)request.getInterpolationMethod();
                if (!SUPPORTED_INTERPOLATIONS_V100.contains(interpolation)) {
                    throw new CstlServiceException("Unsupported interpolation: " + request.getInterpolationMethod(), INVALID_PARAMETER_VALUE, KEY_INTERPOLATION.toLowerCase());
                }
            }
        } catch (IllegalArgumentException ex) {
            throw new CstlServiceException(ex.getMessage(), INVALID_PARAMETER_VALUE, KEY_INTERPOLATION.toLowerCase());
        }


        Envelope envelope;
        try {
            envelope = request.getEnvelope();
        } catch (FactoryException ex) {
            throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, KEY_BBOX.toLowerCase());
        }
        /*
         * Here the envelope can be null, if we have specified a TIME parameter. In this case we
         * do not have to test whether the bbox parameter are into the CRS axes definition.
         */
        if (envelope != null) {
            // Ensures the bbox specified is inside the range of the CRS.
            final CoordinateReferenceSystem objectiveCrs;
            try {
                objectiveCrs = request.getCRS();
            } catch (FactoryException ex) {
                throw new CstlServiceException(ex, INVALID_CRS, KEY_CRS.toLowerCase());
            }
            for (int i = 0; i < objectiveCrs.getCoordinateSystem().getDimension(); i++) {
                final CoordinateSystemAxis axis = objectiveCrs.getCoordinateSystem().getAxis(i);
                if (envelope.getMaximum(i) < axis.getMinimumValue() ||
                    envelope.getMinimum(i) > axis.getMaximumValue())
                {
                    throw new CstlServiceException(Errors.format(Errors.Keys.ILLEGAL_RANGE_2,
                            envelope.getMinimum(i), envelope.getMaximum(i)),
                            INVALID_DIMENSION_VALUE, KEY_BBOX.toLowerCase());
                }
            }
            // Ensures the requested envelope has, at least, a part that intersects the valid envelope
            // for the coverage.
            try {
                final GeographicBoundingBox geoBbox = layerRef.getGeographicBoundingBox();
                if (geoBbox == null) {
                    throw new CstlServiceException("The request coverage \""+ layerRef.getName() +"\" has" +
                                                   " no geometric information.", NO_APPLICABLE_CODE);
                }
                final GeneralEnvelope validGeoEnv = new GeneralEnvelope(geoBbox);
                Envelope requestGeoEnv = envelope;
                // We have to transform the objective envelope into an envelope that uses a geographic CRS,
                // in order to be able to verify the intersection between those two envelopes.
                if (!CRS.equalsIgnoreMetadata(envelope.getCoordinateReferenceSystem(), CommonCRS.WGS84.normalizedGeographic())) {
                    try {
                        requestGeoEnv = CRS.transform(envelope, CommonCRS.WGS84.normalizedGeographic());
                    } catch (TransformException ex) {
                        throw new CstlServiceException(ex, NO_APPLICABLE_CODE, KEY_BBOX.toLowerCase());
                    }
                }
                if (!(validGeoEnv.intersects(requestGeoEnv, false))) {
                    throw new CstlServiceException("The requested bbox is outside the domain of validity " +
                            "for this coverage", NO_APPLICABLE_CODE, KEY_BBOX.toLowerCase());
                }
            } catch (DataStoreException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE, KEY_BBOX.toLowerCase());
            }
        } else if (date == null) {

            throw new CstlServiceException("One of Time or Envelope has to be specified", MISSING_PARAMETER_VALUE);

        } else {
            // We take the envelope from the data provider. That envelope can be a little bit imprecise.
            try {
                final GeographicBoundingBox geoBbox = layerRef.getGeographicBoundingBox();
                if (geoBbox == null) {
                    throw new CstlServiceException("The request coverage \""+ layerRef.getName() +"\" has" +
                                                   " no geometric information.", NO_APPLICABLE_CODE);
                }
                envelope = new JTSEnvelope2D(geoBbox.getWestBoundLongitude(), geoBbox.getEastBoundLongitude(),
                                             geoBbox.getSouthBoundLatitude(), geoBbox.getNorthBoundLatitude(),
                                             CommonCRS.WGS84.normalizedGeographic());
            } catch (DataStoreException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE, KEY_BBOX.toLowerCase());
            }
        }
        final JTSEnvelope2D refEnvel;
        try {
            final CoordinateReferenceSystem responseCRS = request.getResponseCRS();
            if (responseCRS != null && !CRS.equalsIgnoreMetadata(responseCRS, envelope.getCoordinateReferenceSystem())) {
                final Envelope responseEnv = CRS.transform(envelope, responseCRS);
                refEnvel = new JTSEnvelope2D(responseEnv);
            } else {
                refEnvel = new JTSEnvelope2D(envelope);
            }
        } catch (FactoryException ex) {
            throw new CstlServiceException(ex, INVALID_CRS, KEY_CRS.toLowerCase());
        } catch (TransformException ex) {
            throw new CstlServiceException(ex, INVALID_CRS, KEY_RESPONSE_CRS.toLowerCase());
        }

        Dimension size = request.getSize();
        if (size == null) {
            // Try with resx/resy, those parameters should be filled.
            final List<Double> resolutions = request.getResolutions();
            if (resolutions == null || resolutions.isEmpty()) {
                // Should not occurs since it is already tested
                throw new CstlServiceException("If width/height are not specified, you have to give resx/resy");
            }
            final double resx = resolutions.get(0);
            final double resy = resolutions.get(1);
            final double envWidth = refEnvel.getSpan(0);
            final double envHeight = refEnvel.getSpan(1);
            // Assume that the resolution is in unit per px -> unit / (unit/pixel) -> px
            // For example to obtain an image whose width is 1024 pixels, representing 360 degrees,
            // the resolution on the x axis is 360 / 1024 = 0,3515625 degrees/pixels.
            // In our case, we want to know the image width using the size of the envelope and the
            // given resolution on that axis, so: image_width = envelope_width / resx
            final int newWidth  = (int) Math.round(envWidth  / resx);
            final int newHeight = (int) Math.round(envHeight / resy);
            size = new Dimension(newWidth, newHeight);
        }

        final Double elevation = (envelope.getDimension() > 2) ? envelope.getMedian(2) : null;

        /*
         * Generating the response.
         * It can be a text one (format MATRIX) or an image one (png, gif ...).
         */
        final String format = request.getFormat();
        if ( format.equalsIgnoreCase(MATRIX) || format.equalsIgnoreCase(ASCII_GRID)) {

            //NOTE ADRIAN HACKED HERE
            final RenderedImage image;
            try {
                final GridCoverage2D gridCov = layerRef.getCoverage(refEnvel, size, elevation, date);
                image = gridCov.getRenderedImage();
            } catch (IOException | DataStoreException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }

            return image;

        } else if( format.equalsIgnoreCase(NETCDF) ){

            throw new CstlServiceException(new IllegalArgumentException(
                                               "Constellation does not support netcdf writing."),
                                           INVALID_FORMAT, KEY_FORMAT.toLowerCase());

        } else if( format.equalsIgnoreCase(GEOTIFF) ){
            try {
                final SpatialMetadata metadata = layerRef.getSpatialMetadata();
                final GridCoverage2D coverage  = layerRef.getCoverage(refEnvel, size, elevation, date);
                return new SimpleEntry(coverage, metadata);
            } catch (IOException | DataStoreException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }

        } else {
            // We are in the case of an image format requested.
            //NOTE: ADRIAN HACKED HERE

            // SCENE
            final Map<String, Object> renderParameters = new HashMap<>();

            renderParameters.put(KEY_TIME, date);
            renderParameters.put("ELEVATION", elevation);
            final SceneDef sdef = new SceneDef();

            final List<DataReference> styles = configLayer.getStyles();
            final MutableStyle style;
            if (!styles.isEmpty()) {
                final DataReference styleName = styles.get(0);
                final MutableStyle incomingStyle = getStyle(styleName);
                style = WCSUtils.filterStyle(incomingStyle, request.getRangeSubset());
            } else {
                style = null;
            }
            try {
                final MapContext context = PortrayalUtil.createContext(layerRef, style, renderParameters);
                sdef.setContext(context);
            } catch (PortrayalException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }

            // VIEW
            final Double azimuth =  0.0; //HARD CODED SINCE PROTOCOL DOES NOT ALLOW
            final ViewDef vdef = new ViewDef(refEnvel, azimuth);

            // CANVAS
            Color background = null;
            if (MimeType.IMAGE_JPEG.equalsIgnoreCase(format)) {
                background = Color.WHITE;
            }
            final CanvasDef cdef = new CanvasDef(size, background);

            // IMAGE
            final BufferedImage img;
            try {
                img = Cstl.getPortrayalService().portray(sdef, vdef, cdef);
            } catch (PortrayalException ex) {
                /*
                 * TODO: the binding xml for WCS and GML do not support the exceptions format,
                 * consequently we can't extract the exception output mime-type information from
                 * the request. Maybe a more recent version of the GML 3 spec has fixed this bug ...
                 */
                //if (exceptions != null && exceptions.equalsIgnoreCase(EXCEPTIONS_INIMAGE)) {
                //    img = Cstl.Portrayal.writeInImage(ex, abstractRequest.getSize());
                //} else {
                    throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                //}
            }

            return img;
        }
    }

    /**
     * Overriden from AbstractWorker because in version 1.0.0 the behaviour is different when the request updateSequence
     * is equal to the current.
     *
     * @param updateSequence
     * @param version
     * @return
     * @throws CstlServiceException
     */
    private boolean returnUpdateSequenceDocument(final String updateSequence, final String version) throws CstlServiceException {
        if (updateSequence == null) {
            return false;
        }
        if ("1.0.0".equals(version)) {
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
        } else {
            return returnUpdateSequenceDocument(updateSequence);
        }
    }
}
