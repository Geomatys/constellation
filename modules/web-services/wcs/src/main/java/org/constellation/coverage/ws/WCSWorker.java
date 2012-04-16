/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2010, Geomatys
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
package org.constellation.coverage.ws;

// J2SE dependencies
import java.util.logging.Level;
import java.util.Arrays;
import java.io.File;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.portrayal.PortrayalUtil;
import org.constellation.provider.CoverageLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.util.StyleUtils;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.configuration.Layer;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.ws.LayerWorker;
import static org.constellation.query.Query.*;
import static org.constellation.coverage.ws.WCSConstant.*;

// Geotoolkit dependencies
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.gml.xml.v311.CodeListType;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.ows.xml.v110.AcceptFormatsType;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.ows.xml.v110.KeywordsType;
import org.geotoolkit.ows.xml.v110.LanguageStringType;
import org.geotoolkit.ows.xml.v110.OperationsMetadata;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.ows.xml.v110.ServiceIdentification;
import org.geotoolkit.ows.xml.v110.ServiceProvider;
import org.geotoolkit.ows.xml.v110.WGS84BoundingBoxType;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.resources.Errors;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.TimeParser;
import org.geotoolkit.wcs.xml.WCSMarshallerPool;
import org.geotoolkit.wcs.xml.DescribeCoverage;
import org.geotoolkit.wcs.xml.DescribeCoverageResponse;
import org.geotoolkit.wcs.xml.GetCoverage;
import org.geotoolkit.wcs.xml.GetCapabilities;
import org.geotoolkit.wcs.xml.GetCapabilitiesResponse;
import org.geotoolkit.wcs.xml.v100.ContentMetadata;
import org.geotoolkit.wcs.xml.v100.CoverageDescription;
import org.geotoolkit.wcs.xml.v100.CoverageOfferingBriefType;
import org.geotoolkit.wcs.xml.v100.CoverageOfferingType;
import org.geotoolkit.wcs.xml.v100.DomainSetType;
import org.geotoolkit.wcs.xml.v100.GetCoverageType;
import org.geotoolkit.wcs.xml.v100.Keywords;
import org.geotoolkit.wcs.xml.v100.LonLatEnvelopeType;
import org.geotoolkit.wcs.xml.v100.RangeSet;
import org.geotoolkit.wcs.xml.v100.RangeSetType;
import org.geotoolkit.wcs.xml.v100.SupportedCRSsType;
import org.geotoolkit.wcs.xml.v100.SupportedFormatsType;
import org.geotoolkit.wcs.xml.v100.SupportedInterpolationsType;
import org.geotoolkit.wcs.xml.v100.WCSCapabilitiesType;
import org.geotoolkit.wcs.xml.v100.WCSCapabilityType;
import org.geotoolkit.wcs.xml.v100.WCSCapabilityType.Request;
import org.geotoolkit.wcs.xml.v111.Capabilities;
import org.geotoolkit.wcs.xml.v111.Contents;
import org.geotoolkit.wcs.xml.v111.CoverageDescriptionType;
import org.geotoolkit.wcs.xml.v111.CoverageDescriptions;
import org.geotoolkit.wcs.xml.v111.CoverageDomainType;
import org.geotoolkit.wcs.xml.v111.CoverageSummaryType;
import org.geotoolkit.wcs.xml.v111.FieldType;
import org.geotoolkit.wcs.xml.v111.InterpolationMethodType;
import org.geotoolkit.wcs.xml.v111.InterpolationMethods;
import org.geotoolkit.wcs.xml.v111.RangeType;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.wcs.xml.v100.InterpolationMethod;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.gml.xml.v311.RectifiedGridType;
import org.geotoolkit.gml.xml.v311.GridType;
import org.geotoolkit.ows.xml.v110.MetadataType;
import org.geotoolkit.wcs.xml.v100.MetadataLinkType;
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.geotoolkit.wcs.xml.v111.GridCrsType;
import org.opengis.geometry.Envelope;
import org.opengis.feature.type.Name;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.util.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.TransformException;
import org.opengis.coverage.grid.RectifiedGrid;


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
 * @version 0.5
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public final class WCSWorker extends LayerWorker {
    /**
     * The date format to match.
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * A list of supported interpolation
     */
    private static final List<org.geotoolkit.wcs.xml.v100.InterpolationMethod> SUPPORTED_INTERPOLATIONS_V100 =
            new ArrayList<org.geotoolkit.wcs.xml.v100.InterpolationMethod>();
    static {
            SUPPORTED_INTERPOLATIONS_V100.add(org.geotoolkit.wcs.xml.v100.InterpolationMethod.BILINEAR);
            SUPPORTED_INTERPOLATIONS_V100.add(org.geotoolkit.wcs.xml.v100.InterpolationMethod.BICUBIC);
            SUPPORTED_INTERPOLATIONS_V100.add(org.geotoolkit.wcs.xml.v100.InterpolationMethod.NEAREST_NEIGHBOR);
    }

    /**
     * A list of supported interpolation
     */
    private static final List<org.geotoolkit.wcs.xml.v111.InterpolationMethod> SUPPORTED_INTERPOLATIONS_V111 =
            new ArrayList<org.geotoolkit.wcs.xml.v111.InterpolationMethod>();
    static {
            SUPPORTED_INTERPOLATIONS_V111.add(org.geotoolkit.wcs.xml.v111.InterpolationMethod.BILINEAR);
            SUPPORTED_INTERPOLATIONS_V111.add(org.geotoolkit.wcs.xml.v111.InterpolationMethod.BICUBIC);
            SUPPORTED_INTERPOLATIONS_V111.add(org.geotoolkit.wcs.xml.v111.InterpolationMethod.NEAREST_NEIGHBOR);
    }

    /*
     * A list supported formats
     *
     */
     private static final List<CodeListType> SUPPORTED_FORMATS_100 = new ArrayList<CodeListType>();
     static {
            SUPPORTED_FORMATS_100.add(new CodeListType("png"));
            SUPPORTED_FORMATS_100.add(new CodeListType("gif"));
            SUPPORTED_FORMATS_100.add(new CodeListType("jpeg"));
            SUPPORTED_FORMATS_100.add(new CodeListType("bmp"));
            SUPPORTED_FORMATS_100.add(new CodeListType("tiff"));
            SUPPORTED_FORMATS_100.add(new CodeListType("geotiff"));
            SUPPORTED_FORMATS_100.add(new CodeListType("matrix"));
            SUPPORTED_FORMATS_100.add(new CodeListType("ascii-grid"));
     }

    /**
     * Output responses of a GetCapabilities request.
     */
    private static final Map<String,GetCapabilitiesResponse> CAPS_RESPONSE =
            new HashMap<String,GetCapabilitiesResponse>();

    public WCSWorker(final String id, final File configurationDirectory) {
        super(id, configurationDirectory, ServiceDef.Specification.WCS);
        if (isStarted) {
            LOGGER.log(Level.INFO, "WCS worker {0} running", id);
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
    public DescribeCoverageResponse describeCoverage(final DescribeCoverage request) throws CstlServiceException {
        isWorking();
        final String version = request.getVersion().toString();
        if (version.isEmpty()) {
            throw new CstlServiceException("The parameter VERSION must be specified.",
                           MISSING_PARAMETER_VALUE, KEY_VERSION.toLowerCase());
        }

        if (version.equals(ServiceDef.WCS_1_0_0.version.toString()) &&
            request instanceof org.geotoolkit.wcs.xml.v100.DescribeCoverageType)
        {
            return describeCoverage100((org.geotoolkit.wcs.xml.v100.DescribeCoverageType) request);
        } else if (version.equals(ServiceDef.WCS_1_1_1.version.toString()) &&
                   request instanceof org.geotoolkit.wcs.xml.v111.DescribeCoverageType)
        {
            return describeCoverage111((org.geotoolkit.wcs.xml.v111.DescribeCoverageType) request);
        } else {
            throw new CstlServiceException("The version number specified for this GetCoverage request " +
                    "is not handled.", NO_APPLICABLE_CODE, KEY_VERSION.toLowerCase());
        }
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
    private DescribeCoverageResponse describeCoverage100(final org.geotoolkit.wcs.xml.v100.DescribeCoverageType request)
            throws CstlServiceException {
        
        if (request.getCoverage().isEmpty()) {
            throw new CstlServiceException("The parameter COVERAGE must be specified.",
                    MISSING_PARAMETER_VALUE, KEY_COVERAGE.toLowerCase());
        }

        final List<CoverageOfferingType> coverageOfferings = new ArrayList<CoverageOfferingType>();
        for (String coverage : request.getCoverage()) {
            final Name tmpName = parseCoverageName(coverage);
            final LayerDetails layerRef = getLayerReference(tmpName);
            if (layerRef.getType().equals(LayerDetails.TYPE.FEATURE)) {
                throw new CstlServiceException("The requested layer is vectorial. WCS is not able to handle it.",
                        LAYER_NOT_DEFINED, KEY_COVERAGE.toLowerCase());
            }
            if (!(layerRef instanceof CoverageLayerDetails)) {
                // Should not occurs, since we have previously verified the type of layer.
                throw new CstlServiceException("The requested layer is not a coverage. WCS is not able to handle it.",
                        LAYER_NOT_DEFINED, KEY_COVERAGE.toLowerCase());
            }
            final CoverageLayerDetails coverageRef = (CoverageLayerDetails) layerRef;
            final Name fullCoverageName = coverageRef.getName();
            final String coverageName;
            if (fullCoverageName.getNamespaceURI() != null) {
                coverageName = fullCoverageName.getNamespaceURI() + ':' + fullCoverageName.getLocalPart();
            } else {
                coverageName = fullCoverageName.getLocalPart();
            }
            if (!coverageRef.isQueryable(ServiceDef.Query.WCS_ALL)) {
                throw new CstlServiceException("You are not allowed to request the layer \"" +
                        coverageName + "\".", LAYER_NOT_QUERYABLE, KEY_COVERAGE.toLowerCase());
            }

            final GeographicBoundingBox inputGeoBox;
            try {
                inputGeoBox = coverageRef.getGeographicBoundingBox();
            } catch (DataStoreException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
            }
            final LonLatEnvelopeType llenvelope;
            final EnvelopeType envelope;
            if (inputGeoBox != null) {
                final SortedSet<Number> elevations;
                try {
                    elevations = coverageRef.getAvailableElevations();
                } catch (DataStoreException ex) {
                    throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                }

                final List<DirectPositionType> pos = buildPositions(inputGeoBox, elevations);
                llenvelope = new LonLatEnvelopeType(pos, "urn:ogc:def:crs:OGC:1.3:CRS84");
                envelope   = new EnvelopeType(pos, "EPSG:4326");
            } else {
                throw new CstlServiceException("The geographic bbox for the layer is null !",
                        NO_APPLICABLE_CODE);
            }
            final Keywords keywords = new Keywords(ServiceDef.Specification.WCS.toString(), coverageName);

            /*
             * Spatial metadata
             */
            final EnvelopeType nativeEnvelope;
            try {
                nativeEnvelope = new EnvelopeType(coverageRef.getEnvelope());
            } catch (DataStoreException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
            }
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
                LOGGER.log(Level.SEVERE, null, ex);
            }

            final org.geotoolkit.wcs.xml.v100.SpatialDomainType spatialDomain =
                    new org.geotoolkit.wcs.xml.v100.SpatialDomainType(Arrays.asList(envelope, nativeEnvelope), Arrays.asList(grid));

            // temporal metadata
            final SortedSet<Date> dates;
            try {
                dates = coverageRef.getAvailableTimes();
            } catch (DataStoreException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
            final org.geotoolkit.wcs.xml.v100.TimeSequenceType temporalDomain;
            if (dates == null || dates.isEmpty()) {
                temporalDomain = null;
            } else {
                final List<Object> times = new ArrayList<Object>();
                final DateFormat df = new SimpleDateFormat(DATE_FORMAT);
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                for (Date d : dates) {
                    times.add(new TimePositionType(df.format(d)));
                }
                temporalDomain = new org.geotoolkit.wcs.xml.v100.TimeSequenceType(times);
            }
            final DomainSetType domainSet = new DomainSetType(spatialDomain, temporalDomain);
            //TODO complete
            final RangeSetType rangeSetT = new RangeSetType(null, coverageName,
                    coverageName, null, null, null, null);
            final RangeSet rangeSet = new RangeSet(rangeSetT);
            //supported CRS
            final SupportedCRSsType supCRS = new SupportedCRSsType(new CodeListType("EPSG:4326"));
            supCRS.addNativeCRSs(new CodeListType(nativeEnvelope.getSrsName()));

            // supported formats
            String nativeFormat = coverageRef.getImageFormat();
            if (nativeFormat == null || nativeFormat.isEmpty()) {
                nativeFormat = "unknown";
            }
            final SupportedFormatsType supForm = new SupportedFormatsType(nativeFormat, SUPPORTED_FORMATS_100);

            //supported interpolations
            final SupportedInterpolationsType supInt = new SupportedInterpolationsType(
                    org.geotoolkit.wcs.xml.v100.InterpolationMethod.NEAREST_NEIGHBOR, SUPPORTED_INTERPOLATIONS_V100);

            //we build the coverage offering for this layer/coverage
            final CoverageOfferingType coverageOffering = new CoverageOfferingType(null, coverageName,
                    coverageName, StringUtilities.cleanSpecialCharacter(coverageRef.getRemarks()), llenvelope,
                    keywords, domainSet, rangeSet, supCRS, supForm, supInt);
            coverageOfferings.add(coverageOffering);
        }

        return new CoverageDescription(coverageOfferings, ServiceDef.WCS_1_0_0.version.toString());
    }


    /**
     * Transform a geographicBoundingBox into a list of direct positions.
     *
     * @param inputGeoBox
     * @param elevations
     * @return
     */
    private List<DirectPositionType> buildPositions(final GeographicBoundingBox inputGeoBox, final SortedSet<Number> elevations) {
        final List<Double> pos1 = new ArrayList<Double>();
        pos1.add(inputGeoBox.getWestBoundLongitude());
        pos1.add(inputGeoBox.getSouthBoundLatitude());
        final List<Double> pos2 = new ArrayList<Double>();
        pos2.add(inputGeoBox.getEastBoundLongitude());
        pos2.add(inputGeoBox.getNorthBoundLatitude());
        if (elevations != null && elevations.size() >= 2) {
            pos1.add(elevations.first().doubleValue());
            pos2.add(elevations.last().doubleValue());
        }
        final List<DirectPositionType> pos = new ArrayList<DirectPositionType>();
        pos.add(new DirectPositionType(pos1));
        pos.add(new DirectPositionType(pos2));
        return pos;
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
    private DescribeCoverageResponse describeCoverage111(
            final org.geotoolkit.wcs.xml.v111.DescribeCoverageType request)
                            throws CstlServiceException
    {
        if (request.getIdentifier().isEmpty()) {
            throw new CstlServiceException("The parameter IDENTIFIER must be specified",
                    MISSING_PARAMETER_VALUE, KEY_IDENTIFIER.toLowerCase());
        }

        final List<CoverageDescriptionType> coverageDescriptions = new ArrayList<CoverageDescriptionType>();
        for (String coverage : request.getIdentifier()) {
            final Name tmpName = parseCoverageName(coverage);
            final LayerDetails layerRef = getLayerReference(tmpName);
            if (layerRef.getType().equals(LayerDetails.TYPE.FEATURE)) {
                throw new CstlServiceException("The requested layer is vectorial. WCS is not able to handle it.",
                        LAYER_NOT_DEFINED, KEY_IDENTIFIER.toLowerCase());
            }
            if (!(layerRef instanceof CoverageLayerDetails)) {
                // Should not occurs, since we have previously verified the type of layer.
                throw new CstlServiceException("The requested layer is not a coverage. WCS is not able to handle it.",
                        LAYER_NOT_DEFINED, KEY_IDENTIFIER.toLowerCase());
            }
            final CoverageLayerDetails coverageRef = (CoverageLayerDetails) layerRef;
            final Name fullCoverageName = coverageRef.getName();
            final String coverageName;
            if (fullCoverageName.getNamespaceURI() != null) {
                coverageName = fullCoverageName.getNamespaceURI() + ':' + fullCoverageName.getLocalPart();
            } else {
                coverageName = fullCoverageName.getLocalPart();
            }
            if (!coverageRef.isQueryable(ServiceDef.Query.WCS_ALL)) {
                throw new CstlServiceException("You are not allowed to request the layer \"" +
                        coverageName + "\".", INVALID_PARAMETER_VALUE, KEY_IDENTIFIER.toLowerCase());
            }
            final org.geotoolkit.ows.xml.v110.ObjectFactory owsFactory =
                    new org.geotoolkit.ows.xml.v110.ObjectFactory();
            final GeographicBoundingBox inputGeoBox;
            try {
                inputGeoBox = coverageRef.getGeographicBoundingBox();
            } catch (DataStoreException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, KEY_BOUNDINGBOX.toLowerCase());
            }
            final List<JAXBElement<? extends BoundingBoxType>> bboxs =
                    new ArrayList<JAXBElement<? extends BoundingBoxType>>();
            if (inputGeoBox != null) {
                final WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(
                        inputGeoBox.getWestBoundLongitude(),
                        inputGeoBox.getSouthBoundLatitude(),
                        inputGeoBox.getEastBoundLongitude(),
                        inputGeoBox.getNorthBoundLatitude());
                bboxs.add(owsFactory.createWGS84BoundingBox(outputBBox));
            }
            /*
             * Spatial metadata
             */
            final BoundingBoxType nativeEnvelope;
            try {
                nativeEnvelope = new BoundingBoxType(coverageRef.getEnvelope());
                bboxs.add(owsFactory.createBoundingBox(nativeEnvelope));
            } catch (DataStoreException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
            }
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
                LOGGER.log(Level.SEVERE, null, ex);
            }
                
            // spatial metadata
            final org.geotoolkit.wcs.xml.v111.SpatialDomainType spatial =
                    new org.geotoolkit.wcs.xml.v111.SpatialDomainType(bboxs, grid, null, null, null);
            

            //general metadata
            final List<LanguageStringType> title = new ArrayList<LanguageStringType>();
            title.add(new LanguageStringType(coverageName));
            final List<LanguageStringType> abstractt = new ArrayList<LanguageStringType>();
            abstractt.add(new LanguageStringType(StringUtilities.cleanSpecialCharacter(coverageRef.getRemarks())));
            final List<KeywordsType> keywords = new ArrayList<KeywordsType>();
            keywords.add(new KeywordsType(new LanguageStringType(ServiceDef.Specification.WCS.toString()),
                    new LanguageStringType(coverageName)));

            // temporal metadata
            final List<Object> times = new ArrayList<Object>();
            final SortedSet<Date> dates;
            try {
                dates = coverageRef.getAvailableTimes();
            } catch (DataStoreException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
            final DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            for (Date d : dates) {
                times.add(new TimePositionType(df.format(d)));
            }
            final org.geotoolkit.wcs.xml.v111.TimeSequenceType temporalDomain =
                    new org.geotoolkit.wcs.xml.v111.TimeSequenceType(times);

            final CoverageDomainType domain = new CoverageDomainType(spatial, temporalDomain);

            //supported interpolations
            final List<InterpolationMethodType> intList = new ArrayList<InterpolationMethodType>();
            for (org.geotoolkit.wcs.xml.v111.InterpolationMethod inte : SUPPORTED_INTERPOLATIONS_V111) {
                intList.add(new InterpolationMethodType(inte.value(), null));
            }
            final InterpolationMethods interpolations = new InterpolationMethods(intList
                    , org.geotoolkit.wcs.xml.v111.InterpolationMethod.NEAREST_NEIGHBOR.value());
            final RangeType range = new RangeType(new FieldType(StringUtilities.cleanSpecialCharacter(coverageRef.getThematic()),
                    null, new org.geotoolkit.ows.xml.v110.CodeType("0.0"), interpolations));

            //supported CRS
            final List<String> supportedCRS = new ArrayList<String>();
            supportedCRS.add("EPSG:4326");

            //supported formats
            final List<String> supportedFormats = new ArrayList<String>();
            supportedFormats.add(MimeType.IMAGE_PNG);
            supportedFormats.add(MimeType.IMAGE_GIF);
            supportedFormats.add(MimeType.IMAGE_JPEG);
            supportedFormats.add(MimeType.IMAGE_BMP);
            supportedFormats.add("matrix");
            supportedFormats.add("ascii-grid");

            final CoverageDescriptionType coverageDescription = new CoverageDescriptionType(title, abstractt,
                    keywords, coverageName, domain, range, supportedCRS, supportedFormats);
            coverageDescriptions.add(coverageDescription);
        }

        return new CoverageDescriptions(coverageDescriptions);
    }

    /**
     * Describe the capabilities and the layers available for the WCS service.
     *
     * @param request The request done by the user.
     * @return a WCSCapabilities XML document describing the capabilities of this service.
     *
     * @throws CstlServiceException
     * @throws JAXBException when unmarshalling the default GetCapabilities file.
     */
    public GetCapabilitiesResponse getCapabilities(final GetCapabilities request) throws CstlServiceException {
        isWorking();
        //we begin by extract the base attribute
        String version = request.getVersion().toString();
        if (version.isEmpty()) {
            // For the moment the only version that we really support is this one.
            version = "1.0.0";
        }

        // If the getCapabilities response is in cache, we just return it.
        if (CAPS_RESPONSE.containsKey(version)) {
            return CAPS_RESPONSE.get(version);
        }

        final String format;
        final GetCapabilitiesResponse response;
        if (version.equals(ServiceDef.WCS_1_0_0.version.toString()) &&
            request instanceof org.geotoolkit.wcs.xml.v100.GetCapabilitiesType)
        {
            response = getCapabilities100((org.geotoolkit.wcs.xml.v100.GetCapabilitiesType) request);
        } else if (version.equals(ServiceDef.WCS_1_1_1.version.toString()) &&
                   request instanceof org.geotoolkit.wcs.xml.v111.GetCapabilitiesType)
        {
            // if the user have specified one format accepted (only one for now != spec)
            final AcceptFormatsType formats =
                    ((org.geotoolkit.wcs.xml.v111.GetCapabilitiesType)request).getAcceptFormats();
            if (formats == null || formats.getOutputFormat().isEmpty()) {
                format = MimeType.TEXT_XML;
            } else {
                format = formats.getOutputFormat().get(0);
                if (!format.equals(MimeType.TEXT_XML) && !format.equals(MimeType.APP_XML)) {
                    throw new CstlServiceException("This format " + format + " is not allowed",
                            INVALID_FORMAT, KEY_FORMAT.toLowerCase());
                }
            }

            response = getCapabilities111((org.geotoolkit.wcs.xml.v111.GetCapabilitiesType) request);
        } else {
            throw new CstlServiceException("The version number specified for this request " +
                    "is not handled.", VERSION_NEGOTIATION_FAILED, KEY_VERSION.toLowerCase());
        }

        CAPS_RESPONSE.put(version, response);
        return response;
    }

    /**
     * Returns the {@linkplain WCSCapabilitiesType GetCapabilities} response of the request
     * given by parameter, in version 1.0.0 of WCS.
     *
     * @param request The request done by the user, in version 1.0.0.
     * @return a WCSCapabilities XML document describing the capabilities of this service.
     *
     * @throws CstlServiceException
     * @throws JAXBException when unmarshalling the default GetCapabilities file.
     */
    private GetCapabilitiesResponse getCapabilities100(final org.geotoolkit.wcs.xml.v100.GetCapabilitiesType request)
            throws CstlServiceException {
        /*
         * In WCS 1.0.0 the user can request only one section
         * ( or all by omitting the parameter section)
         */
        final String section = request.getSection();
        String requestedSection = null;
        boolean contentMeta = false;
        if (section != null) {
            if (SectionsType.getExistingSections(ServiceDef.WCS_1_0_0.version.toString()).contains(section)) {
                requestedSection = section;
            } else {
                throw new CstlServiceException("The section " + section + " does not exist",
                        INVALID_PARAMETER_VALUE, KEY_SECTION.toLowerCase());
            }
            contentMeta = "/WCS_Capabilities/ContentMetadata".equals(requestedSection);
        }

        // We unmarshall the static capabilities document.
        final WCSCapabilitiesType staticCapabilities;
        try {
            staticCapabilities = (WCSCapabilitiesType) getStaticCapabilitiesObject(ServiceDef.WCS_1_0_0.version.toString(), ServiceDef.Specification.WCS.toString());
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex);
        }
        
        if (requestedSection == null || "/WCS_Capabilities/Capability".equals(requestedSection) || "/".equals(requestedSection))
        {
            //we update the url in the static part.
            final Request req = WCSConstant.REQUEST_100;
            final String url  = getServiceUrl() + "SERVICE=WCS&";
            req.updateURL(url);
            staticCapabilities.getCapability().setRequest(req);
        }

        final WCSCapabilitiesType responsev100;
        if (requestedSection == null || contentMeta || "/".equals(requestedSection)) {
            responsev100 = staticCapabilities;
        } else {
            if ("/WCS_Capabilities/Capability".equals(requestedSection)) {
                final WCSCapabilityType getStaticCapa = staticCapabilities.getCapability();
                getStaticCapa.setVersion(ServiceDef.WCS_1_0_0.version.toString());
                return new WCSCapabilitiesType(getStaticCapa);
            } else if ("/WCS_Capabilities/Service".equals(requestedSection)) {
                final org.geotoolkit.wcs.xml.v100.ServiceType getStaticService = staticCapabilities.getService();
                getStaticService.setVersion(ServiceDef.WCS_1_0_0.version.toString());
                return new WCSCapabilitiesType(getStaticService);
            } else {
                throw new CstlServiceException("Not a valid section requested: " + requestedSection,
                        INVALID_PARAMETER_VALUE, KEY_SECTION.toLowerCase());
            }
        }

        final ContentMetadata contentMetadata;
        final List<CoverageOfferingBriefType> offBrief = new ArrayList<CoverageOfferingBriefType>();
        final org.geotoolkit.wcs.xml.v100.ObjectFactory wcs100Factory = new org.geotoolkit.wcs.xml.v100.ObjectFactory();
        final LayerProviderProxy namedProxy    = LayerProviderProxy.getInstance();
        final Map<Name,Layer> layers = getLayers();
        try {
            for (Name name : layers.keySet()) {
                final LayerDetails layer = namedProxy.get(name);
                final Layer configLayer  = layers.get(name);

                if (layer.getType().equals(LayerDetails.TYPE.FEATURE)) {
                    continue;
                }
                if (!layer.isQueryable(ServiceDef.Query.WCS_ALL)) {
                    continue;
                }
                final CoverageOfferingBriefType co = new CoverageOfferingBriefType();
                final Name fullLayerName = layer.getName();
                final String layerName;
                if (fullLayerName.getNamespaceURI() != null) {
                    layerName = fullLayerName.getNamespaceURI() + ':' + fullLayerName.getLocalPart();
                } else {
                    layerName = fullLayerName.getLocalPart();
                }

                co.addRest(wcs100Factory.createName(layerName));
                co.addRest(wcs100Factory.createLabel(layerName));

                final GeographicBoundingBox inputGeoBox = layer.getGeographicBoundingBox();
                if (inputGeoBox == null) {
                    // The coverage does not contain geometric information, we do not want this coverage
                    // in the capabilities response.
                    continue;
                }
                final List<DirectPositionType> pos = buildPositions(inputGeoBox, layer.getAvailableElevations());
                final LonLatEnvelopeType outputBBox = new LonLatEnvelopeType(pos, "urn:ogc:def:crs:OGC:1.3:CRS84");

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
                    final DateFormat df = new SimpleDateFormat(DATE_FORMAT);
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    outputBBox.getTimePosition().add(new TimePositionType(df.format(firstDate)));
                    outputBBox.getTimePosition().add(new TimePositionType(df.format(lastDate)));
                }
                co.setLonLatEnvelope(outputBBox);

                /*
                 * coverage brief customisation
                 */
                 if (configLayer.getTitle() != null) {
                    co.setLabel(configLayer.getTitle());
                 }
                 if (configLayer.getAbstrac() != null) {
                    co.setDescription(configLayer.getAbstrac());
                 }
                 if (configLayer.getKeywords() != null && !configLayer.getKeywords().isEmpty()) {
                    co.setKeywords(new Keywords(configLayer.getKeywords()));
                 }
                 if (configLayer.getMetadataURL() != null && configLayer.getMetadataURL().getOnlineResource() != null) {
                     co.setMetadataLink(Arrays.asList(new MetadataLinkType(configLayer.getMetadataURL().getOnlineResource().getValue())));
                 }
                offBrief.add(co);
            }
            contentMetadata = new ContentMetadata(offBrief);
        } catch (DataStoreException exception) {
            throw new CstlServiceException(exception, NO_APPLICABLE_CODE);
        }

        // The ContentMetadata has finally been filled, we can now return the response.
        if (contentMeta) {
            contentMetadata.setVersion(ServiceDef.WCS_1_0_0.version.toString());
            return new WCSCapabilitiesType(contentMetadata);
        } else {
            responsev100.setContentMetadata(contentMetadata);
            return responsev100;
        }
    }

    /**
     * Returns the {@linkplain Capabilities GetCapabilities} response of the request given
     * by parameter, in version 1.1.1 of WCS.
     *
     * @param request The request done by the user, in version 1.1.1.
     * @return a WCSCapabilities XML document describing the capabilities of this service.
     *
     * @throws CstlServiceException
     */
    private Capabilities getCapabilities111(final org.geotoolkit.wcs.xml.v111.GetCapabilitiesType request)
                                throws CstlServiceException {
        // First we try to extract only the requested section.
        List<String> requestedSections =
                SectionsType.getExistingSections(ServiceDef.WCS_1_1_1.version.toString());

        if (request.getSections() != null && request.getSections().getSection().size() > 0) {
            requestedSections = request.getSections().getSection();
            for (String sec : requestedSections) {
                if (!SectionsType.getExistingSections(ServiceDef.WCS_1_1_1.version.toString()).contains(sec)) {
                    throw new CstlServiceException("This sections " + sec + " is not allowed",
                            INVALID_PARAMETER_VALUE, KEY_SECTION.toLowerCase());
                }
            }
        }

        // We unmarshall the static capabilities document.
        final Capabilities staticCapabilities;
        try {
            staticCapabilities = (Capabilities) getStaticCapabilitiesObject(ServiceDef.WCS_1_1_1.version.toString(), ServiceDef.Specification.WCS.toString());
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex);
        }
        
        ServiceIdentification si = null;
        ServiceProvider sp       = null;
        OperationsMetadata om    = null;
        final String all         = "All";
        //we add the static sections if the are included in the requested sections
        if (requestedSections.contains("ServiceProvider") || requestedSections.contains(all)) {
            sp = staticCapabilities.getServiceProvider();
        }
        if (requestedSections.contains("ServiceIdentification") || requestedSections.contains(all)) {
            si = staticCapabilities.getServiceIdentification();
        }
        if (requestedSections.contains("OperationsMetadata") || requestedSections.contains(all)) {
            om = WCSConstant.OPERATIONS_METADATA_111;
            //we update the url in the static part. TODO
            om.updateURL(getServiceUrl());
        }
        final Capabilities responsev111 = new Capabilities(si, sp, om, ServiceDef.WCS_1_1_1.version.toString(), null, null);

        // if the user does not request the contents section we can return the result.
        if (!requestedSections.contains("Contents") && !requestedSections.contains(all)) {
            return responsev111;
        }

        // Generate the Contents part of the GetCapabilities.
        final Contents contents;
        final List<CoverageSummaryType>        summary                = new ArrayList<CoverageSummaryType>();
        final org.geotoolkit.wcs.xml.v111.ObjectFactory wcs111Factory = new org.geotoolkit.wcs.xml.v111.ObjectFactory();
        final org.geotoolkit.ows.xml.v110.ObjectFactory owsFactory    = new org.geotoolkit.ows.xml.v110.ObjectFactory();

        final LayerProviderProxy namedProxy    = LayerProviderProxy.getInstance();
        final Map<Name,Layer> layers = getLayers();
        try {
            for (Name name : layers.keySet()) {
                final LayerDetails layer = namedProxy.get(name);
                final Layer configLayer  = layers.get(name);
                
                if (layer.getType().equals(LayerDetails.TYPE.FEATURE)) {
                    continue;
                }
                if (!layer.isQueryable(ServiceDef.Query.WCS_ALL)) {
                    continue;
                }
                final CoverageLayerDetails coverageLayer = (CoverageLayerDetails)layer;
                final List<LanguageStringType> title = new ArrayList<LanguageStringType>();
                title.add(new LanguageStringType(coverageLayer.getName().getLocalPart()));
                final List<LanguageStringType> remark = new ArrayList<LanguageStringType>();
                remark.add(new LanguageStringType(StringUtilities.cleanSpecialCharacter(coverageLayer.getRemarks())));

                final CoverageSummaryType cs = new CoverageSummaryType(title, remark);

                final GeographicBoundingBox inputGeoBox = coverageLayer.getGeographicBoundingBox();
                if (inputGeoBox == null) {
                    // The coverage does not contain geometric information, we do not want this coverage
                    // in the capabilities response.
                    continue;
                }
                //final String srsName = "urn:ogc:def:crs:OGC:1.3:CRS84";
                final WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(
                        inputGeoBox.getWestBoundLongitude(),
                        inputGeoBox.getSouthBoundLatitude(),
                        inputGeoBox.getEastBoundLongitude(),
                        inputGeoBox.getNorthBoundLatitude());
                cs.addRest(owsFactory.createWGS84BoundingBox(outputBBox));
                cs.addRest(wcs111Factory.createIdentifier(coverageLayer.getName().getLocalPart()));

                /*
                 * coverage brief customisation
                 */
                 if (configLayer.getTitle() != null) {
                    cs.setTitle(configLayer.getTitle());
                 }
                 if (configLayer.getAbstrac() != null) {
                    cs.setAbstract(configLayer.getAbstrac());
                 }
                 if (configLayer.getKeywords() != null && !configLayer.getKeywords().isEmpty()) {
                    cs.setKeywords(Arrays.asList(new KeywordsType(configLayer.getKeywords())));
                 }
                 if (configLayer.getMetadataURL() != null && configLayer.getMetadataURL().getOnlineResource() != null) {
                    cs.setMetadata(new MetadataType(configLayer.getMetadataURL().getOnlineResource().getValue()));
                 }
                summary.add(cs);
            }

            contents = new Contents(summary, null, null, null);
        } catch (DataStoreException exception) {
            throw new CstlServiceException(exception, NO_APPLICABLE_CODE);
        }

        // Finally set the contents and return the full response.
        responsev111.setContents(contents);
        return responsev111;
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
    public Object getCoverage(final GetCoverage request) throws CstlServiceException {
        isWorking();
        final String inputVersion = request.getVersion().toString();
        if (inputVersion == null) {
            throw new CstlServiceException("The parameter version must be specified",
                           MISSING_PARAMETER_VALUE, KEY_VERSION.toLowerCase());
        } else if (!"1.0.0".equals(inputVersion) && !"1.1.1".equals(inputVersion)) {
            throw new CstlServiceException("The version number specified for this request " + inputVersion +
                    " is not handled.", VERSION_NEGOTIATION_FAILED, KEY_VERSION.toLowerCase());
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
        final LayerDetails tmplayerRef = getLayerReference(tmpName);
        if (!tmplayerRef.isQueryable(ServiceDef.Query.WCS_ALL) || tmplayerRef.getType().equals(LayerDetails.TYPE.FEATURE)) {
            throw new CstlServiceException("You are not allowed to request the layer \"" +
                    tmplayerRef.getName() + "\".", INVALID_PARAMETER_VALUE, KEY_COVERAGE.toLowerCase());
        }
        if (!(tmplayerRef instanceof CoverageLayerDetails)) {
                // Should not occurs, since we have previously verified the type of layer.
                throw new CstlServiceException("The requested layer is not a coverage. WCS is not able to handle it.",
                        LAYER_NOT_DEFINED, KEY_COVERAGE.toLowerCase());
        }
        CoverageLayerDetails layerRef = (CoverageLayerDetails) tmplayerRef;

        // we verify the interpolation method even if we don't use it
        if (request instanceof GetCoverageType) {
            final String interpolationSt = ((GetCoverageType)request).getInterpolationMethod();
            if (interpolationSt != null) {
                final InterpolationMethod interpolation = InterpolationMethod.fromValue(interpolationSt);
                if (interpolation == null || !SUPPORTED_INTERPOLATIONS_V100.contains(interpolation)) {
                    throw new CstlServiceException("Unsupported interpolation: " + interpolationSt, INVALID_PARAMETER_VALUE, KEY_INTERPOLATION.toLowerCase());
                }
            }
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
                    throw new CstlServiceException(Errors.format(Errors.Keys.ILLEGAL_RANGE_$2,
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
                if (!CRS.equalsIgnoreMetadata(envelope.getCoordinateReferenceSystem(), DefaultGeographicCRS.WGS84)) {
                    try {
                        requestGeoEnv = CRS.transform(envelope, DefaultGeographicCRS.WGS84);
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
                                             DefaultGeographicCRS.WGS84);
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
            } catch (IOException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            } catch (DataStoreException ex) {
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
            } catch (IOException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            } catch (DataStoreException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }

        } else {
            // We are in the case of an image format requested.
            //NOTE: ADRIAN HACKED HERE

            // SCENE
            final Map<String, Object> renderParameters = new HashMap<String, Object>();
            
            renderParameters.put(KEY_TIME, date);
            renderParameters.put("ELEVATION", elevation);
            final SceneDef sdef = new SceneDef();

            final List<String> styleNames = layerRef.getFavoriteStyles();
            final MutableStyle style;
            if (!styleNames.isEmpty()) {
                final String styleName = styleNames.get(0);
                final MutableStyle incomingStyle = StyleProviderProxy.getInstance().get(styleName);
                style = StyleUtils.filterStyle(incomingStyle, request.getRangeSubset());
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
     * Parse a Name from a string.
     * @param layerName
     * @return
     */
    private Name parseCoverageName(final String layerName) {
        final Name namedLayerName;
        if (layerName != null && layerName.lastIndexOf(':') != -1) {
            final String namespace = layerName.substring(0, layerName.lastIndexOf(':'));
            final String localPart = layerName.substring(layerName.lastIndexOf(':') + 1);
            namedLayerName = new DefaultName(namespace, localPart);
        } else {
            namedLayerName = new DefaultName(layerName);
        }
        return namedLayerName;
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
