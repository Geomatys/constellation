/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.catalog.CatalogException;
import org.constellation.coverage.catalog.Series;
import org.constellation.gml.v311.CodeListType;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.TimePositionType;
import org.constellation.ows.AbstractDCP;
import org.constellation.ows.AbstractOnlineResourceType;
import org.constellation.ows.AbstractOperation;
import org.constellation.ows.v110.AcceptFormatsType;
import org.constellation.ows.v110.BoundingBoxType;
import org.constellation.ows.v110.KeywordsType;
import org.constellation.ows.v110.LanguageStringType;
import org.constellation.ows.v110.OperationsMetadata;
import org.constellation.ows.v110.SectionsType;
import org.constellation.ows.v110.ServiceIdentification;
import org.constellation.ows.v110.ServiceProvider;
import org.constellation.ows.v110.WGS84BoundingBoxType;
import org.constellation.portrayal.Portrayal;
import org.constellation.provider.LayerDetails;
import org.constellation.register.RegisterException;
import org.constellation.util.StringUtilities;
import org.constellation.util.Util;
import org.constellation.wcs.DescribeCoverage;
import org.constellation.wcs.DescribeCoverageResponse;
import org.constellation.wcs.GetCoverage;
import org.constellation.wcs.GetCapabilities;
import org.constellation.wcs.GetCapabilitiesResponse;
import org.constellation.wcs.v100.ContentMetadata;
import org.constellation.wcs.v100.CoverageDescription;
import org.constellation.wcs.v100.CoverageOfferingBriefType;
import org.constellation.wcs.v100.CoverageOfferingType;
import org.constellation.wcs.v100.DCPTypeType;
import org.constellation.wcs.v100.DCPTypeType.HTTP.Get;
import org.constellation.wcs.v100.DCPTypeType.HTTP.Post;
import org.constellation.wcs.v100.DomainSetType;
import org.constellation.wcs.v100.Keywords;
import org.constellation.wcs.v100.LonLatEnvelopeType;
import org.constellation.wcs.v100.RangeSet;
import org.constellation.wcs.v100.RangeSetType;
import org.constellation.wcs.v100.SupportedCRSsType;
import org.constellation.wcs.v100.SupportedFormatsType;
import org.constellation.wcs.v100.SupportedInterpolationsType;
import org.constellation.wcs.v100.WCSCapabilitiesType;
import org.constellation.wcs.v100.WCSCapabilityType.Request;
import org.constellation.wcs.v111.Capabilities;
import org.constellation.wcs.v111.Contents;
import org.constellation.wcs.v111.CoverageDescriptionType;
import org.constellation.wcs.v111.CoverageDescriptions;
import org.constellation.wcs.v111.CoverageDomainType;
import org.constellation.wcs.v111.CoverageSummaryType;
import org.constellation.wcs.v111.FieldType;
import org.constellation.wcs.v111.InterpolationMethodType;
import org.constellation.wcs.v111.InterpolationMethods;
import org.constellation.wcs.v111.RangeType;
import org.constellation.ws.CstlServiceException;

// Geotools dependencies
import org.constellation.ws.rs.WebService;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.display.exception.PortrayalException;
import org.geotools.geometry.jts.ReferencedEnvelope;

// GeoAPI dependencies
import org.geotools.referencing.CRS;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;

import static org.constellation.query.Query.APP_XML;
import static org.constellation.query.Query.TEXT_XML;
import static org.constellation.query.wcs.WCSQuery.GEOTIFF;
import static org.constellation.query.wcs.WCSQuery.MATRIX;
import static org.constellation.query.wcs.WCSQuery.NETCDF;
import static org.constellation.ws.ExceptionCode.INVALID_PARAMETER_VALUE;
import static org.constellation.ws.ExceptionCode.LAYER_NOT_DEFINED;
import static org.constellation.ws.ExceptionCode.MISSING_PARAMETER_VALUE;
import static org.constellation.ws.ExceptionCode.NO_APPLICABLE_CODE;
import static org.constellation.ws.ExceptionCode.VERSION_NEGOTIATION_FAILED;
import org.opengis.referencing.FactoryException;


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
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public final class WCSWorker {

    /**
     * The default debugging logger for the WCS service.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.coverage.ws");

    /*
     * Set to true for CITE tests.
     */
    private final static boolean CITE_TESTING = false;

    /**
     * The web service marshaller, which will use the web service name space.
     */
    private final LinkedBlockingQueue<Marshaller> marshallers;

    /**
     * The web service unmarshaller, which will use the web service name space.
     */
    private final LinkedBlockingQueue<Unmarshaller> unmarshallers;

    /**
     * Defines a set of methods that a servlet uses to communicate with its servlet container,
     * for example, to get the MIME type of a file, dispatch requests, or write to a log file.
     */
    private ServletContext servletContext = null;
    /**
     * Contains the request URI and therefore any  KVP parameters it may contain.
     */
    private UriInfo uriContext = null;

    /**
     * A map containing the Capabilities Object already loaded from file.
     */
    private Map<String,Object> capabilities = new HashMap<String,Object>();

    public WCSWorker(final LinkedBlockingQueue<Marshaller> marshallers,
                     final LinkedBlockingQueue<Unmarshaller> unmarshallers)
    {
        this.marshallers   = marshallers;
        this.unmarshallers = unmarshallers;
    }

    /**
     * The DescribeCoverage operation returns an XML file, containing the 
     * complete description of the specific coverages requested.
     * <p>
     * This method extends the definition of each coverage given in the 
     * Capabilities document with supplementary information.
     * </p>
     *
     * @param abstractRequest A {@linkplain AbstractDescribeCoverage request}
     *                        with the parameters of the user message.
     * @return An XML document giving the full description of the requested 
     *           coverages.
     * @throws JAXBException
     * @throws CstlServiceException
     */
    public DescribeCoverageResponse describeCoverage(final DescribeCoverage abstractRequest)
                                                  throws JAXBException, CstlServiceException
    {
        LOGGER.info("describeCoverage request processing");
        //this.actingVersion = new ServiceVersion(ServiceType.WCS, abstractRequest.getVersion());
        //we begin by extracting the base attribute
        final String version = abstractRequest.getVersion();
        if (version == null) {
            throw new CstlServiceException("The parameter SERVICE must be specified.",
                           MISSING_PARAMETER_VALUE, "version");
        }

        if (version.equals("1.0.0")) {
            return describeCoverage100((org.constellation.wcs.v100.DescribeCoverageType) abstractRequest);
        } else if (version.equals("1.1.1")) {
            return describeCoverage111((org.constellation.wcs.v111.DescribeCoverageType) abstractRequest);
        } else {
            throw new CstlServiceException("The version number specified for this GetCoverage request " +
                    "is not handled.", NO_APPLICABLE_CODE, "version");
        }
    }

    /**
     * Returns the description of the coverage requested in version 1.0.0 of WCS standard.
     *
     * @param request a {@linkplain org.constellation.wcs.v100.DescribeCoverage describe coverage}
     *                request done by the user.
     * @return an XML document giving the full description of a coverage, in version 1.0.0.
     *
     * @throws JAXBException
     * @throws CstlServiceException
     */
    private DescribeCoverageResponse describeCoverage100(
            final org.constellation.wcs.v100.DescribeCoverageType request)
                            throws JAXBException, CstlServiceException
    {
        if (request.getCoverage().size() == 0) {
            throw new CstlServiceException("The parameter COVERAGE must be specified.",
                    MISSING_PARAMETER_VALUE, "coverage");
        }

        //TODO: we should loop over the list
        final LayerDetails layerRef = getLayerReference(request.getCoverage().get(0), "1.0.0");

        final List<CoverageOfferingType> coverages = new ArrayList<CoverageOfferingType>();
        final Set<Series> series = layerRef.getSeries();
        if (series == null || series.isEmpty()) {
            throw new CstlServiceException("The coverage " + layerRef.getName() + " is not defined.",
                    LAYER_NOT_DEFINED);
        }
        final GeographicBoundingBox inputGeoBox;
        try {
            inputGeoBox = layerRef.getGeographicBoundingBox();
        } catch (CatalogException ex) {
            throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
        }
        final String srsName = "urn:ogc:def:crs:OGC:1.3:CRS84";
        final LonLatEnvelopeType llenvelope;
        if (inputGeoBox != null) {
            final SortedSet<Number> elevations;
            try {
                elevations = layerRef.getAvailableElevations();
            } catch (CatalogException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
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
            llenvelope = new LonLatEnvelopeType(pos, srsName);
        } else {
            throw new CstlServiceException("The geographic bbox for the layer is null !",
                                           NO_APPLICABLE_CODE);
        }
        final Keywords keywords = new Keywords("WCS", layerRef.getName(),
                Util.cleanSpecialCharacter(layerRef.getThematic()));

        //Spatial metadata
        final org.constellation.wcs.v100.SpatialDomainType spatialDomain =
                new org.constellation.wcs.v100.SpatialDomainType(llenvelope);

        // temporal metadata
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        final List<Object> times = new ArrayList<Object>();
        final SortedSet<Date> dates;
        try {
            dates = layerRef.getAvailableTimes();
        } catch (CatalogException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }
        for (Date d : dates) {
            times.add(new TimePositionType(df.format(d)));
        }
        final org.constellation.wcs.v100.TimeSequenceType temporalDomain =
                new org.constellation.wcs.v100.TimeSequenceType(times);
        final DomainSetType domainSet = new DomainSetType(spatialDomain, temporalDomain);
        //TODO complete
        final RangeSetType rangeSetT = new RangeSetType(null, layerRef.getName(),
                layerRef.getName(), null, null, null,null);
        final RangeSet rangeSet = new RangeSet(rangeSetT);
        //supported CRS
        final SupportedCRSsType supCRS = new SupportedCRSsType(new CodeListType("EPSG:4326"));

        // supported formats
        final Set<CodeListType> formats = new LinkedHashSet<CodeListType>();
        formats.add(new CodeListType("matrix"));
        formats.add(new CodeListType("jpeg"));
        formats.add(new CodeListType("png"));
        formats.add(new CodeListType("gif"));
        formats.add(new CodeListType("bmp"));
        String nativeFormat = "unknow";
        Iterator<Series> it = layerRef.getSeries().iterator();
        if (it.hasNext()) {
            Series s = it.next();
            nativeFormat = s.getFormat().getImageFormat();
        }
        final SupportedFormatsType supForm = new SupportedFormatsType(
                              nativeFormat, new ArrayList<CodeListType>(formats));

        //supported interpolations
        final List<org.constellation.wcs.v100.InterpolationMethod> interpolations =
                new ArrayList<org.constellation.wcs.v100.InterpolationMethod>();
        interpolations.add(org.constellation.wcs.v100.InterpolationMethod.BILINEAR);
        interpolations.add(org.constellation.wcs.v100.InterpolationMethod.BICUBIC);
        interpolations.add(org.constellation.wcs.v100.InterpolationMethod.NEAREST_NEIGHBOR);
        final SupportedInterpolationsType supInt = new SupportedInterpolationsType(
                org.constellation.wcs.v100.InterpolationMethod.NEAREST_NEIGHBOR, interpolations);

        //we build the coverage offering for this layer/coverage
        final CoverageOfferingType coverage = new CoverageOfferingType(null, layerRef.getName(),
                layerRef.getName(), Util.cleanSpecialCharacter(layerRef.getRemarks()), llenvelope,
                keywords, domainSet, rangeSet, supCRS, supForm, supInt);
        coverages.add(coverage);
        return new CoverageDescription(coverages, "1.0.0");
    }

    /**
     * Returns the description of the coverage requested in version 1.1.1 of WCS standard.
     *
     * @param request a {@linkplain org.constellation.wcs.v111.DescribeCoverage describe coverage}
     *                request done by the user.
     * @return an XML document giving the full description of a coverage, in version 1.1.1.
     *
     * @throws JAXBException
     * @throws CstlServiceException
     */
    private DescribeCoverageResponse describeCoverage111(
            final org.constellation.wcs.v111.DescribeCoverageType request)
                            throws JAXBException, CstlServiceException
    {
        if (request.getIdentifier().size() == 0) {
            throw new CstlServiceException("The parameter IDENTIFIER must be specified",
                    MISSING_PARAMETER_VALUE, "identifier");
        }

        //TODO: we should loop over the list
        final LayerDetails layer = getLayerReference(request.getIdentifier().get(0), "1.1.1");

        final org.constellation.ows.v110.ObjectFactory owsFactory =
                new org.constellation.ows.v110.ObjectFactory();
        final List<CoverageDescriptionType> coverages = new ArrayList<CoverageDescriptionType>();
        if (layer.getSeries().size() == 0) {
            throw new CstlServiceException("the coverage " + layer.getName() +
                    " is not defined", LAYER_NOT_DEFINED);
        }
        final GeographicBoundingBox inputGeoBox;
        try {
            inputGeoBox = layer.getGeographicBoundingBox();
        } catch (CatalogException ex) {
            throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
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

            final String crs = "EPSG:4326";
            final BoundingBoxType outputBBox2 = new BoundingBoxType(crs,
                    inputGeoBox.getWestBoundLongitude(),
                    inputGeoBox.getSouthBoundLatitude(),
                    inputGeoBox.getEastBoundLongitude(),
                    inputGeoBox.getNorthBoundLatitude());

            bboxs.add(owsFactory.createBoundingBox(outputBBox2));
        }

        //general metadata
        final List<LanguageStringType> title = new ArrayList<LanguageStringType>();
        title.add(new LanguageStringType(layer.getName()));
        final List<LanguageStringType> _abstract = new ArrayList<LanguageStringType>();
        _abstract.add(new LanguageStringType(Util.cleanSpecialCharacter(layer.getRemarks())));
        final List<KeywordsType> keywords = new ArrayList<KeywordsType>();
        keywords.add(new KeywordsType(new LanguageStringType("WCS"),
                new LanguageStringType(layer.getName())));

        // spatial metadata
        final org.constellation.wcs.v111.SpatialDomainType spatial =
                new org.constellation.wcs.v111.SpatialDomainType(bboxs);

        // temporal metadata
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        final List<Object> times = new ArrayList<Object>();
        final SortedSet<Date> dates;
        try {
            dates = layer.getAvailableTimes();
        } catch (CatalogException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }
        for (Date d : dates) {
            times.add(new TimePositionType(df.format(d)));
        }
        final org.constellation.wcs.v111.TimeSequenceType temporalDomain =
                new org.constellation.wcs.v111.TimeSequenceType(times);

        final CoverageDomainType domain = new CoverageDomainType(spatial, temporalDomain);

        //supported interpolations
        final List<InterpolationMethodType> intList = new ArrayList<InterpolationMethodType>();
        intList.add(new InterpolationMethodType(
                org.constellation.wcs.v111.InterpolationMethod.BILINEAR.value(), null));
        intList.add(new InterpolationMethodType(
                org.constellation.wcs.v111.InterpolationMethod.BICUBIC.value(), null));
        intList.add(new InterpolationMethodType(
                org.constellation.wcs.v111.InterpolationMethod.NEAREST_NEIGHBOR.value(), null));
        final InterpolationMethods interpolations = new InterpolationMethods(
                intList, org.constellation.wcs.v111.InterpolationMethod.NEAREST_NEIGHBOR.value());
        final RangeType range = new RangeType(new FieldType(Util.cleanSpecialCharacter(layer.getThematic()),
                null, new org.constellation.ows.v110.CodeType("0.0"), interpolations));

        //supported CRS
        final List<String> supportedCRS = new ArrayList<String>();
        supportedCRS.add("EPSG:4326");

        //supported formats
        final List<String> supportedFormat = new ArrayList<String>();
        supportedFormat.add("application/matrix");
        supportedFormat.add("image/png");
        supportedFormat.add("image/jpeg");
        supportedFormat.add("image/bmp");
        supportedFormat.add("image/gif");

        final CoverageDescriptionType coverage = new CoverageDescriptionType(title, _abstract,
                keywords, layer.getName(), domain, range, supportedCRS, supportedFormat);
        coverages.add(coverage);
        return new CoverageDescriptions(coverages);
    }

    /**
     * Describe the capabilities and the layers available for the WCS service.
     *
     * @param abstractRequest The request done by the user.
     * @return a WCSCapabilities XML document describing the capabilities of this service.
     *
     * @throws CstlServiceException
     * @throws JAXBException when unmarshalling the default GetCapabilities file.
     */
    public GetCapabilitiesResponse getCapabilities(GetCapabilities abstractRequest)
                                  throws JAXBException, CstlServiceException
    {
        //we begin by extract the base attribute
        String version = abstractRequest.getVersion();
        if (version == null) {
            version = "1.1.1";
        }

        //this.actingVersion = new ServiceVersion(ServiceType.WCS, version);
        final String format;

        if (version.equals("1.0.0")) {
            return getCapabilities100((org.constellation.wcs.v100.GetCapabilitiesType) abstractRequest);
        } else if (version.equals("1.1.1")) {
            // if the user have specified one format accepted (only one for now != spec)
            final AcceptFormatsType formats =
                    ((org.constellation.wcs.v111.GetCapabilitiesType)abstractRequest).getAcceptFormats();
            if (formats == null || formats.getOutputFormat().size() == 0) {
                format = TEXT_XML;
            } else {
                format = formats.getOutputFormat().get(0);
                if (!format.equals(TEXT_XML) && !format.equals(APP_XML)) {
                    throw new CstlServiceException("This format " + format + " is not allowed",
                            INVALID_PARAMETER_VALUE, "format");
                }
            }

            return getCapabilities111((org.constellation.wcs.v111.GetCapabilitiesType) abstractRequest);
        } else {
            throw new CstlServiceException("The version number specified for this request " +
                    "is not handled.", NO_APPLICABLE_CODE, "version");
        }
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
    private GetCapabilitiesResponse getCapabilities100(
            final org.constellation.wcs.v100.GetCapabilitiesType request)
                           throws CstlServiceException, JAXBException
    {
        /*
         * In WCS 1.0.0 the user can request only one section
         * ( or all by omitting the parameter section)
         */
        final String section = request.getSection();
        String requestedSection = null;
        boolean contentMeta = false;
        if (section != null) {
            if (SectionsType.getExistingSections("1.0.0").contains(section)) {
                requestedSection = section;
            } else {
                throw new CstlServiceException("The section " + section + " does not exist",
                        INVALID_PARAMETER_VALUE);
            }
            contentMeta = requestedSection.equals("/WCS_Capabilities/ContentMetadata");
        }

        // We unmarshall the static capabilities document.
        final WCSCapabilitiesType staticCapabilities;
        try {
            staticCapabilities = (WCSCapabilitiesType) ((JAXBElement<?>) getStaticCapabilitiesObject(
                    servletContext.getRealPath("WEB-INF"), "1.0.0")).getValue();
        } catch (IOException e) {
            throw new CstlServiceException("IO exception while getting Services Metadata: " + e.getMessage(),
                    NO_APPLICABLE_CODE);
        }
        if (requestedSection == null || requestedSection.equals("/WCS_Capabilities/Capability") ||
                                        requestedSection.equals("/"))
        {
            //we update the url in the static part.
            final Request req = staticCapabilities.getCapability().getRequest();
            updateURL(req.getGetCapabilities().getDCPType());
            updateURL(req.getDescribeCoverage().getDCPType());
            updateURL(req.getGetCoverage().getDCPType());
        }

        final WCSCapabilitiesType responsev100;
        if (requestedSection == null || contentMeta || requestedSection.equals("/")) {
            responsev100 = staticCapabilities;
        } else {
            if (requestedSection.equals("/WCS_Capabilities/Capability")) {
                return new WCSCapabilitiesType(staticCapabilities.getCapability());
            } else if (requestedSection.equals("/WCS_Capabilities/Service")) {
                return new WCSCapabilitiesType(staticCapabilities.getService());
            } else {
                throw new CstlServiceException("Not a valid section requested: "+ requestedSection,
                        NO_APPLICABLE_CODE);
            }
        }

        final ContentMetadata contentMetadata;
        final List<CoverageOfferingBriefType> offBrief = new ArrayList<CoverageOfferingBriefType>();
        final org.constellation.wcs.v100.ObjectFactory wcs100Factory =
                new org.constellation.wcs.v100.ObjectFactory();

        //NOTE: ADRIAN HACKED HERE
        final List<LayerDetails> layerRefs = getAllLayerReferences("1.0.0");
        try {
            for (LayerDetails layer : layerRefs) {
                final CoverageOfferingBriefType co = new CoverageOfferingBriefType();
                co.addRest(wcs100Factory.createName(layer.getName()));
                co.addRest(wcs100Factory.createLabel(layer.getName()));

                final GeographicBoundingBox inputGeoBox = layer.getGeographicBoundingBox();
                if (inputGeoBox != null) {
                    final String srsName = "urn:ogc:def:crs:OGC:1.3:CRS84";

                    final SortedSet<Number> elevations = layer.getAvailableElevations();
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
                    final LonLatEnvelopeType outputBBox = new LonLatEnvelopeType(pos, srsName);
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
                        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        df.setTimeZone(TimeZone.getTimeZone("UTC"));
                        outputBBox.getTimePosition().add(new TimePositionType(df.format(firstDate)));
                        outputBBox.getTimePosition().add(new TimePositionType(df.format(lastDate)));
                    }
                    co.setLonLatEnvelope(outputBBox);
                }

                offBrief.add(co);
            }
            contentMetadata = new ContentMetadata("1.0.0", offBrief);
        } catch (CatalogException exception) {
            throw new CstlServiceException(exception, NO_APPLICABLE_CODE);
        }

        // The ContentMetadata has finally been filled, we can now return the response.
        if (contentMeta) {
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
     * @throws JAXBException when unmarshalling the default GetCapabilities file.
     */
    private Capabilities getCapabilities111(final org.constellation.wcs.v111.GetCapabilitiesType request)
                                                           throws CstlServiceException, JAXBException
    {
        // First we try to extract only the requested section.
        List<String> requestedSections = SectionsType.getExistingSections("1.1.1");

        if (request.getSections() != null && request.getSections().getSection().size() > 0) {
            requestedSections = request.getSections().getSection();
            for (String sec : requestedSections) {
                if (!SectionsType.getExistingSections("1.1.1").contains(sec)) {
                    throw new CstlServiceException("This sections " + sec + " is not allowed",
                            INVALID_PARAMETER_VALUE);
                }
            }
        }

        // We unmarshall the static capabilities document.
        final Capabilities staticCapabilities;
        try {
            staticCapabilities = (Capabilities) getStaticCapabilitiesObject(
                    servletContext.getRealPath("WEB-INF"), "1.1.1");
        } catch (IOException e) {
            throw new CstlServiceException("IO exception while getting Services Metadata: " + e.getMessage(),
                    NO_APPLICABLE_CODE);

        }

        ServiceIdentification si = null;
        ServiceProvider sp = null;
        OperationsMetadata om = null;
        //we add the static sections if the are included in the requested sections
        if (requestedSections.contains("ServiceProvider") || requestedSections.contains("All")) {
            sp = staticCapabilities.getServiceProvider();
        }
        if (requestedSections.contains("ServiceIdentification") || requestedSections.contains("All")) {
            si = staticCapabilities.getServiceIdentification();
        }
        if (requestedSections.contains("OperationsMetadata") || requestedSections.contains("All")) {
            om = staticCapabilities.getOperationsMetadata();
            //we update the url in the static part.
            updateOWSURL(om.getOperation(), uriContext.getBaseUri().toString(), "WCS");
        }
        final Capabilities responsev111 = new Capabilities(si, sp, om, "1.1.1", null, null);

        // if the user does not request the contents section we can return the result.
        if (!requestedSections.contains("Contents") && !requestedSections.contains("All")) {
            return responsev111;
        }

        // Generate the Contents part of the GetCapabilities.
        final Contents contents;
        List<CoverageSummaryType>        summary = new ArrayList<CoverageSummaryType>();
        org.constellation.wcs.v111.ObjectFactory wcs111Factory = new org.constellation.wcs.v111.ObjectFactory();
        org.constellation.ows.v110.ObjectFactory owsFactory = new org.constellation.ows.v110.ObjectFactory();

        //NOTE: ADRIAN HACKED HERE
        final List<LayerDetails> layerRefs = getAllLayerReferences("1.1.1");
        try {
            for (LayerDetails layer : layerRefs) {
                final List<LanguageStringType> title = new ArrayList<LanguageStringType>();
                title.add(new LanguageStringType(layer.getName()));
                final List<LanguageStringType> remark = new ArrayList<LanguageStringType>();
                remark.add(new LanguageStringType(Util.cleanSpecialCharacter(layer.getRemarks())));

                final CoverageSummaryType cs = new CoverageSummaryType(title, remark);

                final GeographicBoundingBox inputGeoBox = layer.getGeographicBoundingBox();

                if (inputGeoBox != null) {
                    //final String srsName = "urn:ogc:def:crs:OGC:1.3:CRS84";
                    final WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(
                            inputGeoBox.getWestBoundLongitude(),
                            inputGeoBox.getSouthBoundLatitude(),
                            inputGeoBox.getEastBoundLongitude(),
                            inputGeoBox.getNorthBoundLatitude());
                    cs.addRest(owsFactory.createWGS84BoundingBox(outputBBox));
                }
                cs.addRest(wcs111Factory.createIdentifier(layer.getName()));
                summary.add(cs);
            }

            /**
             * FOR CITE TEST we put the first data mars because of ifremer overlapping data
             * TODO delete when overlapping problem is solved
             */
            if (CITE_TESTING) {
                CoverageSummaryType temp = summary.get(10);
                summary.remove(10);
                summary.add(0, temp);
            }

            contents = new Contents(summary, null, null, null);
        } catch (CatalogException exception) {
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
     * @param abstractRequest The request done by the user.
     * @return An {@linkplain RenderedImage image}, or a data representation.
     *
     * @throws JAXBException
     * @throws CstlServiceException
     */
    public RenderedImage getCoverage(final GetCoverage abstractRequest) throws JAXBException,
                                                                         CstlServiceException
    {
        final String inputVersion = abstractRequest.getVersion();
        if(inputVersion == null) {
            throw new CstlServiceException("The parameter version must be specified",
                           MISSING_PARAMETER_VALUE, "version");
        }
        //this.actingVersion = new ServiceVersion(ServiceType.WCS, inputVersion);

        Date date = null;
        try {
            date = StringUtilities.toDate(abstractRequest.getTime());
        } catch (ParseException ex) {
            LOGGER.log(Level.INFO, "Parsing of the date failed. Please verify that the specified" +
                    " date is compliant with the ISO-8601 standard.", ex);
        }

        /*
         * Generating the response.
         * It can be a text one (format MATRIX) or an image one (image/png, image/gif ...).
         */
        if ( abstractRequest.getFormat().equalsIgnoreCase(MATRIX) ) {

            //NOTE ADRIAN HACKED HERE
            final LayerDetails layerRef = getLayerReference(abstractRequest.getCoverage(), inputVersion);

            final Envelope envelope;
            try {
                envelope = abstractRequest.getEnvelope();
            } catch (FactoryException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
            }
            final Double elevation = (envelope.getDimension() > 2) ? envelope.getMedian(2) : null;
            final RenderedImage image;
            try {
                final GridCoverage2D gridCov = layerRef.getCoverage(abstractRequest.getEnvelope(),
                        abstractRequest.getSize(), elevation, date);
                image = gridCov.getRenderedImage();
            } catch (IOException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            } catch (CatalogException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            } catch (FactoryException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }

            return image;

        } else if( abstractRequest.getFormat().equalsIgnoreCase(NETCDF) ){

            throw new CstlServiceException(new IllegalArgumentException(
                                               "Constellation does not support netcdf writing."),
                                           NO_APPLICABLE_CODE);

        } else if( abstractRequest.getFormat().equalsIgnoreCase(GEOTIFF) ){

            throw new CstlServiceException(new IllegalArgumentException(
                                               "Constellation does not support geotiff writing."),
                                           NO_APPLICABLE_CODE);

        } else {
            // We are in the case of an image format requested.
        	// TODO: This should be the fall through, add formats.

            //NOTE: ADRIAN HACKED HERE

            // SCENE
            final LayerDetails layerRef = getLayerReference(abstractRequest.getCoverage(), inputVersion);
            final Map<String, Object> renderParameters = new HashMap<String, Object>();
            final Envelope envelope;
            try {
                envelope = abstractRequest.getEnvelope();
            } catch (FactoryException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
            }
            final Double elevation = (envelope.getDimension() > 2) ? envelope.getMedian(2) : null;
            renderParameters.put("TIME", date);
            renderParameters.put("ELEVATION", elevation);
            final Portrayal.SceneDef sdef = new Portrayal.SceneDef(layerRef, null, renderParameters);

            // VIEW
            final ReferencedEnvelope refEnvel;
            try {
                if (envelope.getDimension() > 2) {
                    refEnvel = new ReferencedEnvelope(
                            envelope.getMinimum(0), envelope.getMaximum(0),
                            envelope.getMinimum(1), envelope.getMaximum(1),
                            CRS.getHorizontalCRS(abstractRequest.getCRS()));
                } else {
                    refEnvel = new ReferencedEnvelope(envelope);
                }
            } catch (FactoryException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
            }
            final Double azimuth =  0.0; //HARD CODED SINCE PROTOCOL DOES NOT ALLOW
            final Portrayal.ViewDef vdef = new Portrayal.ViewDef(refEnvel, azimuth);

            // CANVAS
            final Portrayal.CanvasDef cdef = new Portrayal.CanvasDef(abstractRequest.getSize(), null);

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
     * Returns the file where to read the capabilities document for each serviceType.
     * If no such file is found, then this method returns {@code null}.
     *
     * @param home The home directory, where to search for configuration files.
     * @param version The version of the GetCapabilities.
     * @return The capabilities Object, or {@code null} if none.
     */
    private Object getStaticCapabilitiesObject(final String home, final String version) throws JAXBException, IOException {
       final String fileName = "WCSCapabilities" + version + ".xml";
       final File changeFile = getFile("change.properties", home);
       final Properties p = new Properties();

       // if the flag file is present we load the properties
       if (changeFile != null && changeFile.exists()) {
           FileInputStream in    = new FileInputStream(changeFile);
           p.load(in);
           in.close();
       } else {
           p.put("update", "false");
       }

       //we get the capabilities file and unmarshalls it
        //we look if we have already put it in cache
        Object response = capabilities.get(fileName);
        final boolean update = p.getProperty("update").equals("true");

        if (response == null || update) {
            if (update) {
                LOGGER.info("updating metadata");
            }

            final File f = getFile(fileName, home);
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = unmarshallers.take();
                response = unmarshaller.unmarshal(f);
                capabilities.put(fileName, response);
            } catch (InterruptedException ex) {
                LOGGER.severe("Interrupted exception in getSaticCapabiltiesObject:" + ex.getMessage());
            } finally {
                if (unmarshaller != null) {
                    unmarshallers.add(unmarshaller);
                }
            }
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

    //TODO: handle the null value in the exception.
    //TODO: harmonize with the method getLayerReference().
    private List<LayerDetails> getAllLayerReferences(final String version) throws CstlServiceException {

    	List<LayerDetails> layerRefs = new ArrayList<LayerDetails>();
    	try { // WE catch the exception from either service version
	        if ( version.equals("1.0.0") ) {
	        	layerRefs = Cstl.getRegister().getAllLayerReferences(ServiceDef.WCS_1_0_0 );
	        } else if ( version.equals("1.1.0") ) {
	        	layerRefs = Cstl.getRegister().getAllLayerReferences(ServiceDef.WCS_1_1_0 );
	        } else if ( version.equals("1.1.1") ) {
	        	layerRefs = Cstl.getRegister().getAllLayerReferences(ServiceDef.WCS_1_1_1 );
	        } else if ( version.equals("1.1.2") ) {
	        	layerRefs = Cstl.getRegister().getAllLayerReferences(ServiceDef.WCS_1_1_2 );
	        } else {
	        	throw new CstlServiceException("WCS acting according to no known version.",
                        VERSION_NEGOTIATION_FAILED);
	        }
        } catch (RegisterException regex ){
        	throw new CstlServiceException(regex, INVALID_PARAMETER_VALUE);
        }
        return layerRefs;
    }

    //TODO: handle the null value in the exception.
    //TODO: harmonize with the method getAllLayerReferences().
    //TODO: distinguish exceptions: layer doesn't exist and layer could not be obtained.
    private LayerDetails getLayerReference(final String layerName, final String version)
                                                             throws CstlServiceException
    {

    	LayerDetails layerRef;
    	try { // WE catch the exception from either service version
        	if ( version.equals("1.0.0") ){
        		layerRef = Cstl.getRegister().getLayerReference(ServiceDef.WCS_1_0_0, layerName);
        	} else if ( version.equals("1.1.1") ) {
        		layerRef = Cstl.getRegister().getLayerReference(ServiceDef.WCS_1_1_1, layerName);
        	} else if ( version.equals("1.1.2") ) {
        		layerRef = Cstl.getRegister().getLayerReference(ServiceDef.WCS_1_1_2, layerName);
        	} else {
        		throw new CstlServiceException("WCS acting according to no known version.",
                        VERSION_NEGOTIATION_FAILED);
        	}
        } catch (RegisterException regex ){
        	throw new CstlServiceException(regex, INVALID_PARAMETER_VALUE);
        }
        return layerRef;
    }

    /**
     * Update all the url in a OWS capabilities document.
     *
     * @param operations A list of OWS operation.
     * @param url The url of the web application.
     * @param serviceType the initials of the web serviceType (WMS, SOS, WCS, CSW, ...).
     *        This string is the resource name in lower case.
     */
    private void updateOWSURL(List<? extends AbstractOperation> operations, String url, String service) {
        for (AbstractOperation op:operations) {
            for (AbstractDCP dcp: op.getDCP()) {
                for (AbstractOnlineResourceType method:dcp.getHTTP().getGetOrPost())
                    method.setHref(url + service.toLowerCase() + "?");
            }
       }
    }

    /**
     * update The URL in capabilities document with the service actual URL.
     */
    private void updateURL(List<DCPTypeType> dcpList) {
        for(DCPTypeType dcp: dcpList) {
           for (Object obj: dcp.getHTTP().getGetOrPost()){
               if (obj instanceof Get){
                   final Get getMethod = (Get)obj;
                   getMethod.getOnlineResource().setHref(uriContext.getBaseUri().toString() + "wcs?SERVICE=WCS&");
               } else if (obj instanceof Post){
                   final Post postMethod = (Post)obj;
                   postMethod.getOnlineResource().setHref(uriContext.getBaseUri().toString() + "wcs?SERVICE=WCS&");
               }
           }
        }
    }
    
    /**
     * This method should be considered private.
     */
    public void internal_initServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * This method should be considered private.
     */
    public void internal_initUriContext(final UriInfo uriContext) {
        this.uriContext = uriContext;
    }

}
