/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.coverage.ws.rs;

// J2SE dependencies
import com.sun.jersey.spi.resource.Singleton;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.naming.NamingException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import org.constellation.catalog.CatalogException;
import org.constellation.coverage.web.ExceptionCode;
import org.constellation.coverage.web.Service;
import org.constellation.coverage.web.ServiceExceptionReport;
import org.constellation.coverage.web.ServiceExceptionType;
import org.constellation.coverage.web.ServiceVersion;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.gml.v311.CodeType;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.EnvelopeEntry;
import org.constellation.gml.v311.GridEnvelopeType;
import org.constellation.gml.v311.GridLimitsType;
import org.constellation.gml.v311.GridType;
import org.constellation.gml.v311.RectifiedGridType;
import org.constellation.gml.v311.TimePositionType;
import org.constellation.ows.AbstractGetCapabilities;
import org.constellation.ows.v110.AcceptFormatsType;
import org.constellation.ows.v110.AcceptVersionsType;
import org.constellation.ows.v110.BoundingBoxType;
import org.constellation.ows.v110.LanguageStringType;
import org.constellation.ows.v100.ExceptionReport;
import org.constellation.ows.v110.WGS84BoundingBoxType;
import org.constellation.ows.v110.OperationsMetadata;
import org.constellation.ows.v110.SectionsType;
import org.constellation.ows.v110.ServiceIdentification;
import org.constellation.ows.v110.ServiceProvider;
import org.constellation.portrayal.CSTLPortrayalService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedLayerDP;
import org.constellation.wcs.AbstractDescribeCoverage;
import org.constellation.wcs.AbstractGetCoverage;
import org.constellation.wcs.v111.Capabilities;
import org.constellation.wcs.v100.ContentMetadata;
import org.constellation.wcs.v111.Contents;
import org.constellation.wcs.v100.CoverageOfferingBriefType;
import org.constellation.wcs.v111.CoverageSummaryType;
import org.constellation.wcs.v100.WCSCapabilityType.Request;
import org.constellation.wcs.v100.DCPTypeType;
import org.constellation.wcs.v100.DCPTypeType.HTTP.Get;
import org.constellation.wcs.v100.DCPTypeType.HTTP.Post;
import org.constellation.wcs.v100.LonLatEnvelopeType;
import org.constellation.wcs.v100.SpatialSubsetType;
import org.constellation.wcs.v100.WCSCapabilitiesType;
import org.constellation.wcs.v111.GridCrsType;
import org.constellation.wcs.v111.RangeSubsetType.FieldSubset;
import org.constellation.ws.rs.OGCWebService;

// GeoAPI dependencies
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.display.exception.PortrayalException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.metadata.extent.GeographicBoundingBox;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import static org.constellation.coverage.web.ExceptionCode.*;
import static org.constellation.query.wcs.WCSQuery.*;


/**
 * WCS 1.1.1 / 1.0.0
 * web service implementing the operation getCoverage, describeCoverage and getCapabilities.
 *
 * @version $Id$
 * @author Guilhem Legal
 * @author Cédric Briançon
 */
@Path("wcs")
@Singleton
public class WCSService extends OGCWebService {
    /**
     * Build a new instance of the webService and initialise the JAXB marshaller.
     */
    public WCSService() throws JAXBException, SQLException, IOException, NamingException {
        super("WCS", new ServiceVersion(Service.WCS, "1.1.1"), new ServiceVersion(Service.WCS, "1.0.0"));

        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext("org.constellation.coverage.web:org.constellation.wcs.v100:org.constellation.wcs.v111",
                      "http://www.opengis.net/wcs");

        LOGGER.info("WCS service running");
    }

    /**
     * Treat the incoming request and call the right function.
     *
     * @return an image or xml response.
     * @throw JAXBException
     */
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        try {
            final String request = (String) getParameter(KEY_REQUEST, true);
            LOGGER.info("New request: " + request);
            writeParameters();

            if (DESCRIBECOVERAGE.equalsIgnoreCase(request) ||
                    (objectRequest instanceof AbstractDescribeCoverage))
            {
                AbstractDescribeCoverage dc = (AbstractDescribeCoverage)objectRequest;
                verifyBaseParameter(0);

                //this wcs does not implement "store" mechanism
                String store = getParameter(KEY_STORE, false);
                if (store!= null && store.trim().equalsIgnoreCase("true")) {
                    throw new WebServiceException("The service does not implement the store mechanism",
                                   NO_APPLICABLE_CODE, getCurrentVersion(), "store");
                }
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (dc == null) {
                    dc = createNewDescribeCoverageRequest();
                }
                return Response.ok(describeCoverage(dc), TEXT_XML).build();
            }
            if (GETCAPABILITIES.equalsIgnoreCase(request) || 
                    (objectRequest instanceof AbstractGetCapabilities))
            {
                AbstractGetCapabilities gc = (AbstractGetCapabilities)objectRequest;
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (gc == null) {
                    gc = createNewGetCapabilitiesRequest();
                }
                return getCapabilities(gc);
            }
            if (GETCOVERAGE.equalsIgnoreCase(request) || 
                    (objectRequest instanceof AbstractGetCoverage))
            {
                AbstractGetCoverage gc = (AbstractGetCoverage)objectRequest;
                verifyBaseParameter(0);
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (gc == null) {

                    gc = createNewGetCoverageRequest();

                }
                return getCoverage(gc);
            }
            throw new WebServiceException("The operation " + request + " is not supported by the service",
                    OPERATION_NOT_SUPPORTED, getCurrentVersion(), "request");
        } catch (WebServiceException ex) {
            final Object report;
            if (getCurrentVersion().isOWS()) {
                final String code = transformCodeName(ex.getExceptionCode().name());
                report = new ExceptionReport(ex.getMessage(), code, ex.getLocator(), getCurrentVersion());
            } else {
                report = new ServiceExceptionReport(getCurrentVersion(),
                        new ServiceExceptionType(ex.getMessage(), (ExceptionCode) ex.getExceptionCode()));
            }
            
            if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE)   &&
                !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED)&& 
                !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE)&& 
                !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED))
            {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            } else {
                LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getLocalizedMessage() + '\n');
            }
            StringWriter sw = new StringWriter();
            marshaller.marshal(report, sw);
            return Response.ok(cleanSpecialCharacter(sw.toString()), TEXT_XML).build();
        }
    }

    /**
     * Build a new GetCapabilities request from a kvp request
     */
    private AbstractGetCapabilities createNewGetCapabilitiesRequest() throws WebServiceException {

        if (!getParameter(KEY_SERVICE, true).equalsIgnoreCase("WCS")) {
            throw new WebServiceException("The parameters SERVICE=WCS must be specified",
                    MISSING_PARAMETER_VALUE, getCurrentVersion(), "service");
        }
        String inputVersion = getParameter(KEY_VERSION, false);
        if (inputVersion == null) {
            inputVersion = getParameter("acceptversions", false);
            if (inputVersion == null) {
                inputVersion = "1.1.1";
            } else {
                //we verify that the version id supported
                isSupportedVersion(inputVersion);
            }
        }

        this.setCurrentVersion(getBestVersion(inputVersion).toString());

        if (getCurrentVersion().toString().equals("1.0.0")) {
            return new org.constellation.wcs.v100.GetCapabilities(getParameter(KEY_SECTION, false),
                                                           null);
        } else {
            AcceptFormatsType formats = new AcceptFormatsType(getParameter("AcceptFormats", false));

            //We transform the String of sections in a list.
            //In the same time we verify that the requested sections are valid.
            String section = getParameter("Sections", false);
            List<String> requestedSections = new ArrayList<String>();
            if (section != null) {
                final StringTokenizer tokens = new StringTokenizer(section, ",;");
                while (tokens.hasMoreTokens()) {
                    final String token = tokens.nextToken().trim();
                    if (SectionsType.getExistingSections("1.1.1").contains(token)) {
                        requestedSections.add(token);
                    } else {
                        throw new WebServiceException("The section " + token + " does not exist",
                                INVALID_PARAMETER_VALUE, getCurrentVersion());
                    }
                }
            } else {
                //if there is no requested Sections we add all the sections
                requestedSections = SectionsType.getExistingSections("1.1.1");
            }
            SectionsType sections = new SectionsType(requestedSections);
            AcceptVersionsType versions = new AcceptVersionsType("1.1.1");
            return new org.constellation.wcs.v111.GetCapabilities(versions,
                                                           sections,
                                                           formats,
                                                           null);
        }
    }

    /**
     * Build a new DescribeCoverage request from a kvp request
     */
    private AbstractDescribeCoverage createNewDescribeCoverageRequest() throws WebServiceException {
        if (getCurrentVersion().toString().equals("1.0.0")) {
            return new org.constellation.wcs.v100.DescribeCoverage(getParameter(KEY_COVERAGE, true));
        } else {
            return new org.constellation.wcs.v111.DescribeCoverage(getParameter(KEY_IDENTIFIER, true));
        }
    }

    /**
     * Build a new DescribeCoverage request from a kvp request
     */
    private AbstractGetCoverage createNewGetCoverageRequest() throws WebServiceException {
        String width  = getParameter(KEY_WIDTH,  false);
        String height = getParameter(KEY_HEIGHT, false);
        String depth  = getParameter(KEY_DEPTH,  false);

        String resx = getParameter(KEY_RESX, false);
        String resy = getParameter(KEY_RESY, false);
        String resz = getParameter(KEY_RESZ, false);

        if (getCurrentVersion().toString().equals("1.0.0")) {
            // temporal subset
            org.constellation.wcs.v100.TimeSequenceType temporal = null;
            final String timeParameter = getParameter(KEY_TIME, false);
            if (timeParameter != null) {
                final TimePositionType time = new TimePositionType(timeParameter);
                temporal = new org.constellation.wcs.v100.TimeSequenceType(time);
            }

            /*
             * spatial subset
             */
            // the boundingBox/envelope
            final List<DirectPositionType> pos = new ArrayList<DirectPositionType>();
            final String bbox = getParameter(KEY_BBOX, true);
            if (bbox != null) {
                final StringTokenizer tokens = new StringTokenizer(bbox, ",;");
                final Double[] coordinates = new Double[tokens.countTokens()];
                int i = 0;
                    while (tokens.hasMoreTokens()) {
                        coordinates[i] = parseDouble(tokens.nextToken());
                        i++;
                    }
                    pos.add(new DirectPositionType(coordinates[0], coordinates[2]));
                    pos.add(new DirectPositionType(coordinates[1], coordinates[3]));
            }
            final EnvelopeEntry envelope = new EnvelopeEntry(pos, getParameter(KEY_CRS, true));

            if ((width == null || height == null) && (resx == null || resy == null)) {
                    throw new WebServiceException("The parameters WIDTH and HEIGHT or RESX and RESY have to be specified" ,
                                   INVALID_PARAMETER_VALUE, getCurrentVersion());
            }

            final List<String> axis = new ArrayList<String>();
            axis.add("width");
            axis.add("height");
            final List<BigInteger> low = new ArrayList<BigInteger>();
            low.add(new BigInteger("0"));
            low.add(new BigInteger("0"));
            final List<BigInteger> high = new ArrayList<BigInteger>();
            high.add(new BigInteger(width));
            high.add(new BigInteger(height));
            if (depth != null) {
                axis.add("depth");
                low.add(new BigInteger("0"));
                high.add(new BigInteger(depth));
            }
            final GridLimitsType limits = new GridLimitsType(low, high);
            final GridType grid = new GridType(limits, axis);

            org.constellation.wcs.v100.SpatialSubsetType spatial = new org.constellation.wcs.v100.SpatialSubsetType(envelope, grid);

            //domain subset
            org.constellation.wcs.v100.DomainSubsetType domain   = new org.constellation.wcs.v100.DomainSubsetType(temporal, spatial);

            //range subset (not yet used)
            org.constellation.wcs.v100.RangeSubsetType  range    = null;

            //interpolation method
            org.constellation.wcs.v100.InterpolationMethod interpolation =
                    org.constellation.wcs.v100.InterpolationMethod.fromValue(getParameter(KEY_INTERPOLATION, false));

            //output
            org.constellation.wcs.v100.OutputType output         = new org.constellation.wcs.v100.OutputType(getParameter(KEY_FORMAT, true),
                                                                                                     getParameter(KEY_RESPONSE_CRS, false));

            return new org.constellation.wcs.v100.GetCoverage(getParameter(KEY_COVERAGE, true),
                                                       domain,
                                                       range,
                                                       interpolation,
                                                       output);
         } else {

            // temporal subset
            org.constellation.wcs.v111.TimeSequenceType temporal = null;
            String timeParameter = getParameter(KEY_TIMESEQUENCE, false);
            if (timeParameter != null) {
                if (timeParameter.indexOf('/') == -1) {
                    temporal = new org.constellation.wcs.v111.TimeSequenceType(new TimePositionType(timeParameter));
                } else {
                    throw new WebServiceException("The service does not handle TimePeriod" ,
                                   INVALID_PARAMETER_VALUE, getCurrentVersion());
                }
            }

            /*
             * spatial subset
             */
             // the boundingBox/envelope
             String bbox          = getParameter(KEY_BOUNDINGBOX, true);
             final String crs;
             if (bbox.indexOf(',') != -1) {
                crs  = bbox.substring(bbox.lastIndexOf(',') + 1, bbox.length());
                bbox = bbox.substring(0, bbox.lastIndexOf(','));
             } else {
                throw new WebServiceException("The correct pattern for BoundingBox parameter are crs,minX,minY,maxX,maxY,CRS",
                                INVALID_PARAMETER_VALUE, getCurrentVersion(), "boundingbox");
             }
             BoundingBoxType envelope = null;

             if (bbox != null) {
                final StringTokenizer tokens = new StringTokenizer(bbox, ",;");
                final Double[] coordinates   = new Double[tokens.countTokens()];
                int i = 0;
                while (tokens.hasMoreTokens()) {
                    coordinates[i] = parseDouble(tokens.nextToken());
                    i++;
                }
                 if (i < 4) {
                     throw new WebServiceException("The correct pattern for BoundingBox parameter are crs,minX,minY,maxX,maxY,CRS",
                             INVALID_PARAMETER_VALUE, getCurrentVersion(), "boundingbox");
                 }
                envelope = new BoundingBoxType(crs, coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
             }

             //domain subset
             org.constellation.wcs.v111.DomainSubsetType domain   = new org.constellation.wcs.v111.DomainSubsetType(temporal, envelope);

             //range subset.
             org.constellation.wcs.v111.RangeSubsetType  range = null;
             String rangeSubset = getParameter(KEY_RANGESUBSET, false);
             if (rangeSubset != null) {
                //for now we don't handle Axis Identifiers
                if (rangeSubset.indexOf('[') != -1 || rangeSubset.indexOf(']') != -1) {
                    throw new WebServiceException("The service does not handle axis identifiers",
                            INVALID_PARAMETER_VALUE, getCurrentVersion(), "axis");
                }

                final StringTokenizer tokens = new StringTokenizer(rangeSubset, ";");
                final List<FieldSubset> fields = new ArrayList<FieldSubset>(tokens.countTokens());
                while (tokens.hasMoreTokens()) {
                    final String value = tokens.nextToken();
                    String interpolation = null;
                    String rangeIdentifier = null;
                    if (value.indexOf(':') != -1) {
                        rangeIdentifier = value.substring(0, rangeSubset.indexOf(':'));
                        interpolation = value.substring(rangeSubset.indexOf(':') + 1);
                    } else {
                        rangeIdentifier = value;
                    }
                    fields.add(new FieldSubset(rangeIdentifier, interpolation));
                }

                range = new org.constellation.wcs.v111.RangeSubsetType(fields);
            }


            String gridType = getParameter(KEY_GRIDTYPE, false);
            if (gridType == null) {
                gridType = "urn:ogc:def:method:WCS:1.1:2dSimpleGrid";
            }
            String gridOrigin = getParameter(KEY_GRIDORIGIN, false);
            if (gridOrigin == null) {
                gridOrigin = "0.0,0.0";
            }
            
            StringTokenizer tokens = new StringTokenizer(gridOrigin, ",;");
            final List<Double> origin = new ArrayList<Double>(tokens.countTokens());
            while (tokens.hasMoreTokens()) {
                Double value = parseDouble(tokens.nextToken());
                origin.add(value);
            }

            String gridOffsets = getParameter(KEY_GRIDOFFSETS, false);
            List<Double> offset = new ArrayList<Double>();
            if (gridOffsets != null) {
                tokens = new StringTokenizer(gridOffsets, ",;");
                while (tokens.hasMoreTokens()) {
                    Double value = parseDouble(tokens.nextToken());
                    offset.add(value);
                }
            }
            String gridCS = getParameter(KEY_GRIDCS, false);
            if (gridCS == null) {
                gridCS = "urn:ogc:def:cs:OGC:0.0:Grid2dSquareCS";
            }

            //output
            final CodeType codeCRS = new CodeType(crs);
            final GridCrsType grid = new GridCrsType(codeCRS, getParameter(KEY_GRIDBASECRS, false), gridType,
                    origin, offset, gridCS, "");
            org.constellation.wcs.v111.OutputType output = new org.constellation.wcs.v111.OutputType(grid, getParameter(KEY_FORMAT, true));

            return new org.constellation.wcs.v111.GetCoverage(new org.constellation.ows.v110.CodeType(getParameter(KEY_IDENTIFIER, true)),
                    domain, range, output);
        }
    }

    /**
     * Describe the capabilities and the layers available for the WMS service.
     *
     * @param abstractRequest The request done by the user.
     * @return a WCSCapabilities XML document describing the capabilities of this service.
     *
     * @throws WebServiceException
     * @throws JAXBException when unmarshalling the default GetCapabilities file.
     */
    public Response getCapabilities(AbstractGetCapabilities abstractRequest) throws JAXBException, WebServiceException {
        //we begin by extract the base attribute
        String inputVersion = abstractRequest.getVersion();
        if (inputVersion == null) {
            setCurrentVersion("1.1.1");
        } else {
           isSupportedVersion(inputVersion);
           setCurrentVersion(inputVersion);
        }
        Capabilities        responsev111 = null;
        WCSCapabilitiesType responsev100 = null;
        boolean contentMeta              = false;
        String format                    = TEXT_XML;
        if (getCurrentVersion().toString().equals("1.1.1")) {

            org.constellation.wcs.v111.GetCapabilities request = (org.constellation.wcs.v111.GetCapabilities) abstractRequest;

            // if the user have specified one format accepted (only one for now != spec)
            AcceptFormatsType formats = request.getAcceptFormats();
            if (formats == null || formats.getOutputFormat().size() == 0) {
                format = TEXT_XML;
            } else {
                format = formats.getOutputFormat().get(0);
                if (!format.equals(TEXT_XML) && !format.equals(APP_XML)){
                    throw new WebServiceException("This format " + format + " is not allowed",
                                   INVALID_PARAMETER_VALUE, getCurrentVersion(), "format");
                }
            }

            //if the user have requested only some sections
            List<String> requestedSections = SectionsType.getExistingSections("1.1.1");

            if (request.getSections() != null && request.getSections().getSection().size() > 0) {
                requestedSections = request.getSections().getSection();
                for (String sec:requestedSections) {
                    if (!SectionsType.getExistingSections("1.1.1").contains(sec)){
                       throw new WebServiceException("This sections " + sec + " is not allowed",
                                       INVALID_PARAMETER_VALUE, getCurrentVersion());
                    }
                }
            }

            // we unmarshall the static capabilities document
            Capabilities staticCapabilities = null;
            try {
                staticCapabilities = (Capabilities)getCapabilitiesObject();
            } catch(IOException e)   {
                throw new WebServiceException("IO exception while getting Services Metadata: " + e.getMessage(),
                               INVALID_PARAMETER_VALUE, getCurrentVersion());

            }
            ServiceIdentification si = null;
            ServiceProvider       sp = null;
            OperationsMetadata    om = null;

            //we add the static sections if the are included in the requested sections
            if (requestedSections.contains("ServiceProvider") || requestedSections.contains("All"))
                sp = staticCapabilities.getServiceProvider();
            if (requestedSections.contains("ServiceIdentification") || requestedSections.contains("All"))
                si = staticCapabilities.getServiceIdentification();
            if (requestedSections.contains("OperationsMetadata") || requestedSections.contains("All")) {
                om = staticCapabilities.getOperationsMetadata();
                //we update the url in the static part.
                updateOWSURL(om.getOperation(), getServiceURL(), "WCS");
            }
            responsev111 = new Capabilities(si, sp, om, "1.1.1", null, null);

            // if the user does not request the contents section we can return the result.
            if (!requestedSections.contains("Contents") && !requestedSections.contains("All")) {
                StringWriter sw = new StringWriter();
                marshaller.marshal(responsev111, sw);
                return Response.ok(sw.toString(), format).build();
            }

        } else {

            org.constellation.wcs.v100.GetCapabilities request = (org.constellation.wcs.v100.GetCapabilities) abstractRequest;

            /*
             * In WCS 1.0.0 the user can request only one section
             * ( or all by ommiting the parameter section)
             */
            String section = request.getSection();
            String requestedSection = null;
            if (section != null) {
                if (SectionsType.getExistingSections("1.0.0").contains(section)){
                    requestedSection = section;
                } else {
                    throw new WebServiceException("The section " + section + " does not exist",
                                   INVALID_PARAMETER_VALUE, getCurrentVersion());
               }
               contentMeta = requestedSection.equals("/WCS_Capabilities/ContentMetadata");
            }
            WCSCapabilitiesType staticCapabilities = null;
            try {
                staticCapabilities = (WCSCapabilitiesType)((JAXBElement)getCapabilitiesObject()).getValue();
            } catch(IOException e)   {
                throw new WebServiceException("IO exception while getting Services Metadata: " + e.getMessage(),
                               INVALID_PARAMETER_VALUE, getCurrentVersion());

            }
            if (requestedSection == null || requestedSection.equals("/WCS_Capabilities/Capability") || requestedSection.equals("/")) {
                //we update the url in the static part.
                Request req = staticCapabilities.getCapability().getRequest();
                updateURL(req.getGetCapabilities().getDCPType());
                updateURL(req.getDescribeCoverage().getDCPType());
                updateURL(req.getGetCoverage().getDCPType());
            }

            if (requestedSection == null || contentMeta  || requestedSection.equals("/")) {
                responsev100 = staticCapabilities;
            } else {
                if (requestedSection.equals("/WCS_Capabilities/Capability")) {
                    responsev100 = new WCSCapabilitiesType(staticCapabilities.getCapability());
                } else if (requestedSection.equals("/WCS_Capabilities/Service")) {
                    responsev100 = new WCSCapabilitiesType(staticCapabilities.getService());
                }

                StringWriter sw = new StringWriter();
                marshaller.marshal(responsev100, sw);
                return Response.ok(sw.toString(), format).build();
            }
        }
        Contents contents = null;
        ContentMetadata contentMetadata = null;

        //we get the list of layers
        List<CoverageSummaryType>        summary = new ArrayList<CoverageSummaryType>();
        List<CoverageOfferingBriefType> offBrief = new ArrayList<CoverageOfferingBriefType>();

        org.constellation.wcs.v111.ObjectFactory wcs111Factory = new org.constellation.wcs.v111.ObjectFactory();
        org.constellation.wcs.v100.ObjectFactory wcs100Factory = new org.constellation.wcs.v100.ObjectFactory();
        org.constellation.ows.v110.ObjectFactory owsFactory = new org.constellation.ows.v110.ObjectFactory();
        try {
            final NamedLayerDP dp = NamedLayerDP.getInstance();
            final Set<String> keys = dp.getKeys();
            for (String key : keys) {
                final LayerDetails layer = dp.get(key);
                if (layer == null) {
                    LOGGER.warning("Missing layer : " + key);
                    continue;
                }
                if (!layer.isQueryable(Service.WCS)) {
                    LOGGER.info("layer" + layer.getName() + " not queryable by WCS");
                    continue;
                }
                List<LanguageStringType> title = new ArrayList<LanguageStringType>();
                title.add(new LanguageStringType(layer.getName()));
                List<LanguageStringType> remark = new ArrayList<LanguageStringType>();
                remark.add(new LanguageStringType(cleanSpecialCharacter(layer.getRemarks())));

                CoverageSummaryType       cs = new CoverageSummaryType(title, remark);
                CoverageOfferingBriefType co = new CoverageOfferingBriefType();

                co.addRest(wcs100Factory.createName(layer.getName()));
                co.addRest(wcs100Factory.createLabel(layer.getName()));

                GeographicBoundingBox inputGeoBox = layer.getGeographicBoundingBox();

                if(inputGeoBox != null) {
                     String crs = "WGS84(DD)";
                    if (getCurrentVersion().toString().equals("1.1.1")){
                        WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(
                                                     inputGeoBox.getWestBoundLongitude(),
                                                     inputGeoBox.getSouthBoundLatitude(),
                                                     inputGeoBox.getEastBoundLongitude(),
                                                     inputGeoBox.getNorthBoundLatitude());

                        cs.addRest(owsFactory.createWGS84BoundingBox(outputBBox));
                    } else {
                        List<Double> pos1 = new ArrayList<Double>();
                        pos1.add(inputGeoBox.getWestBoundLongitude());
                        pos1.add(inputGeoBox.getSouthBoundLatitude());

                        List<Double> pos2 = new ArrayList<Double>();
                        pos2.add(inputGeoBox.getEastBoundLongitude());
                        pos2.add(inputGeoBox.getNorthBoundLatitude());

                        List<DirectPositionType> pos = new ArrayList<DirectPositionType>();
                        pos.add(new DirectPositionType(pos1));
                        pos.add(new DirectPositionType(pos2));
                        LonLatEnvelopeType outputBBox = new LonLatEnvelopeType(pos, crs);
                        co.setLonLatEnvelope(outputBBox);
                    }

                }
                cs.addRest(wcs111Factory.createIdentifier(layer.getName()));
                summary.add(cs);
                offBrief.add(co);
            }

            /**
             * FOR CITE TEST we put the first data mars because of ifremer overlapping data
             * TODO delete when overlapping problem is solved
             */
            CoverageSummaryType temp = summary.get(10);
            summary.remove(10);
            summary.add(0, temp);

            contents        = new Contents(summary, null, null, null);
            contentMetadata = new ContentMetadata("1.0.0", offBrief);
        } catch (CatalogException exception) {
            throw new WebServiceException(exception, NO_APPLICABLE_CODE, getCurrentVersion());
        }


        StringWriter sw = new StringWriter();
        if (getCurrentVersion().toString().equals("1.1.1")) {
            responsev111.setContents(contents);
            marshaller.marshal(responsev111, sw);
        } else {
            if (contentMeta) {
                responsev100 = new WCSCapabilitiesType(contentMetadata);
            } else {
                responsev100.setContentMetadata(contentMetadata);
            }
            marshaller.marshal(responsev100, sw);
        }
        return Response.ok(sw.toString(), format).build();

    }


    /**
     * Web service operation
     */
    public Response getCoverage(AbstractGetCoverage abstractRequest) throws JAXBException, WebServiceException {
        String inputVersion = abstractRequest.getVersion();
        if(inputVersion == null) {
            throw new WebServiceException("The parameter version must be specified",
                           MISSING_PARAMETER_VALUE, getCurrentVersion(), "version");
        } else {
           isSupportedVersion(inputVersion);
           setCurrentVersion(inputVersion);
        }

        final String format, coverage;
        final GeneralEnvelope objEnv;
        final CoordinateReferenceSystem crs;
        final int width, height;
        
        String time = null , interpolation = null, exceptions;
        String depth = null;
        String resx  = null, resy   = null, resz  = null;
        String gridType, gridOrigin = "", gridOffsets = "", gridCS, gridBaseCrs;
        String responseCRS = null;

       if (getCurrentVersion().toString().equals("1.1.1")) {
            final org.constellation.wcs.v111.GetCoverage request = (org.constellation.wcs.v111.GetCoverage)abstractRequest;

            if (request.getIdentifier() != null) {
                coverage = request.getIdentifier().getValue();
            } else {
                throw new WebServiceException("The parameter identifier must be specified",
                               MISSING_PARAMETER_VALUE, getCurrentVersion(), "identifier");
            }

            /*
             * Domain subset: - spatial subSet
             *                - temporal subset
             *
             * spatial subset: - BoundingBox
             * here the boundingBox parameter contain the crs.
             * we must extract it before calling webServiceWorker.setBoundingBox(...)
             *
             * temporal subSet: - timeSequence
             *
             */
            org.constellation.wcs.v111.DomainSubsetType domain = request.getDomainSubset();
            if (domain == null) {
                throw new WebServiceException("The DomainSubset must be specify",
                               MISSING_PARAMETER_VALUE, getCurrentVersion());
            }
            BoundingBoxType boundingBox = null;
            if (domain.getBoundingBox() != null) {
                boundingBox = domain.getBoundingBox().getValue();
            }
            if (boundingBox != null && boundingBox.getLowerCorner() != null &&
                boundingBox.getUpperCorner() != null     &&
                boundingBox.getLowerCorner().size() >= 2 &&
                boundingBox.getUpperCorner().size() >= 2)
            {
                final String crsName = boundingBox.getCrs();
                try {
                    crs  = CRS.decode((crsName.startsWith("EPSG:")) ? crsName : "EPSG:" + crsName);
                } catch (FactoryException ex) {
                    throw new WebServiceException(ex, INVALID_CRS, getCurrentVersion());
                }
                objEnv = new GeneralEnvelope(crs);
                objEnv.setRange(0, boundingBox.getLowerCorner().get(0), boundingBox.getUpperCorner().get(0));
                objEnv.setRange(1, boundingBox.getLowerCorner().get(1), boundingBox.getUpperCorner().get(1));
            } else {
                throw new WebServiceException("The BoundingBox is not well-formed",
                               INVALID_PARAMETER_VALUE, getCurrentVersion(), "boundingbox");
            }

            if (domain.getTemporalSubset() != null) {
                List<Object> timeSeq = domain.getTemporalSubset().getTimePositionOrTimePeriod();
                for (Object obj:timeSeq) {
                    if (obj instanceof TimePositionType)
                        time = ((TimePositionType)obj).getValue();
                    else if (obj instanceof org.constellation.wcs.v111.TimePeriodType) {
                        throw new WebServiceException("The service does not handle time Period type",
                                       INVALID_PARAMETER_VALUE, getCurrentVersion());
                    }
                }
            }
            /*
             * Range subSet.
             * contain the sub fields : fieldSubset
             * for now we handle only one field to change the interpolation method.
             *
             * FieldSubset: - identifier
             *              - interpolationMethodType
             *              - axisSubset (not yet used)
             *
             * AxisSubset:  - identifier
             *              - key
             */
            org.constellation.wcs.v111.RangeSubsetType rangeSubset = request.getRangeSubset();
            if (rangeSubset != null) {
                List<String> requestedField = new ArrayList<String>();
                final NamedLayerDP dp = NamedLayerDP.getInstance();
                for(org.constellation.wcs.v111.RangeSubsetType.FieldSubset field: rangeSubset.getFieldSubset()) {
                    final LayerDetails currentLayer = dp.get(coverage);
                    if (currentLayer == null) {
                        throw new WebServiceException("The coverage requested is not found.",
                                INVALID_PARAMETER_VALUE, getCurrentVersion());
                    }
                    if (field.getIdentifier().equalsIgnoreCase(currentLayer.getThematic())){
                        interpolation = field.getInterpolationType();

                        //we look that the same field is not requested two times
                        if (!requestedField.contains(field.getIdentifier())) {
                            requestedField.add(field.getIdentifier());
                        } else {
                            throw new WebServiceException("The field " + field.getIdentifier() + " is already present in the request",
                                       INVALID_PARAMETER_VALUE, getCurrentVersion());
                        }

                        //if there is some AxisSubset we send an exception
                        if (field.getAxisSubset().size() != 0) {
                            throw new WebServiceException("The service does not handle AxisSubset",
                                       INVALID_PARAMETER_VALUE, getCurrentVersion());
                        }
                    } else {
                        throw new WebServiceException("The field " + field.getIdentifier() + " is not present in this coverage",
                                       INVALID_PARAMETER_VALUE, getCurrentVersion());
                    }
                }

            } else {
                interpolation = null;
            }

            /*
             * output subSet:  - format
             *                 - GridCRS
             *
             * Grid CRS: - GridBaseCRS (not yet used)
             *           - GridOffsets
             *           - GridType (not yet used)
             *           - GridOrigin
             *           - GridCS (not yet used)
             *
             */

            org.constellation.wcs.v111.OutputType output = request.getOutput();
            if (output == null) {
                throw new WebServiceException("The OUTPUT must be specify" ,
                               MISSING_PARAMETER_VALUE, getCurrentVersion(), "output");
            }
            format = output.getFormat();
            if (format == null) {
                throw new WebServiceException("The FORMAT must be specify" ,
                               MISSING_PARAMETER_VALUE, getCurrentVersion(), "format");
            }

            final GridCrsType grid = output.getGridCRS();
            if (grid != null) {
                gridBaseCrs = grid.getGridBaseCRS();
                gridType = grid.getGridType();
                gridCS = grid.getGridCS();

                for (Double d: grid.getGridOffsets()) {
                    gridOffsets += d.toString() + ',';
                }
                if (gridOffsets.length() > 0) {
                    gridOffsets = gridOffsets.substring(0, gridOffsets.length() - 1);
                } else {
                    gridOffsets = null;
                }

                for (Double d: grid.getGridOrigin()) {
                    gridOrigin += d.toString() + ',';
                }
                if (gridOrigin.length() > 0) {
                    gridOrigin = gridOrigin.substring(0, gridOrigin.length() - 1);
                }
            } else {
                // TODO the default value for gridOffsets is temporary until we get the rigth treatment
                gridOffsets = "1.0,0.0,0.0,1.0"; // = null;
                gridOrigin  = "0.0,0.0";
            }
            /* TODO: get the width and height parameter from the calculation using the grid origin, the size
             * of the envelope and the grid offsets.
             */
            width = Integer. parseInt(getParameter(KEY_WIDTH, false));
            height = Integer.parseInt(getParameter(KEY_HEIGHT, false));
            exceptions = getParameter(KEY_EXCEPTIONS, false);

        } else {

            // parameter for 1.0.0 version
            org.constellation.wcs.v100.GetCoverage request = (org.constellation.wcs.v100.GetCoverage)abstractRequest;
            if (request.getOutput().getFormat()!= null) {
                format = request.getOutput().getFormat().getValue();
            } else {
                throw new WebServiceException("The parameters FORMAT have to be specified",
                                                 MISSING_PARAMETER_VALUE, getCurrentVersion(), "format");
            }

            coverage = request.getSourceCoverage();
            if (coverage == null) {
                throw new WebServiceException("The parameters SOURCECOVERAGE have to be specified",
                                                 MISSING_PARAMETER_VALUE, getCurrentVersion(), "sourceCoverage");
            }
            if (request.getInterpolationMethod() != null) {
                interpolation = request.getInterpolationMethod().value();
            }
            exceptions = getParameter(KEY_EXCEPTIONS, false);
            if (request.getOutput().getCrs() != null){
                responseCRS   = request.getOutput().getCrs().getValue();
            }

            //for now we only handle one time parameter with timePosition type
            org.constellation.wcs.v100.TimeSequenceType temporalSubset = request.getDomainSubset().getTemporalSubSet();
            if (temporalSubset != null) {
                for (Object timeObj:temporalSubset.getTimePositionOrTimePeriod()){
                    if (timeObj instanceof TimePositionType) {
                        time  = ((TimePositionType)timeObj).getValue();
                    }
                }
            }
            final SpatialSubsetType spatial = request.getDomainSubset().getSpatialSubSet();
            final EnvelopeEntry env = spatial.getEnvelope();
            final String crsName = env.getSrsName();
            try {
                crs = CRS.decode((crsName.startsWith("EPSG:")) ? crsName : "EPSG:" + crsName);
            } catch (FactoryException ex) {
                throw new WebServiceException(ex, INVALID_CRS, getCurrentVersion());
            }
            objEnv = new GeneralEnvelope(crs);
            objEnv.setRange(0, env.getPos().get(0).getValue().get(0), env.getPos().get(0).getValue().get(1));
            objEnv.setRange(1, env.getPos().get(1).getValue().get(0), env.getPos().get(1).getValue().get(1));

            if (temporalSubset == null && env.getPos().size() == 0) {
                        throw new WebServiceException("The parameters BBOX or TIME have to be specified",
                                       MISSING_PARAMETER_VALUE, getCurrentVersion());
            }
            /* here the parameter width and height (and depth for 3D matrix)
             *  have to be fill. If not they can be replace by resx and resy
             * (resz for 3D grid)
             */
            final GridType grid = spatial.getGrid();
            if (grid instanceof RectifiedGridType){
                resx = getParameter(KEY_RESX,  false);
                resy = getParameter(KEY_RESY,  false);
                resz = getParameter(KEY_RESZ,  false);

                width = Integer.parseInt(resx);
                height = Integer.parseInt(resy);
            } else {
                GridEnvelopeType gridEnv = grid.getLimits().getGridEnvelope();
                if (gridEnv.getHigh().size() > 0) {
                    width         = gridEnv.getHigh().get(0).intValue();
                    height        = gridEnv.getHigh().get(1).intValue();
                    
                    if (gridEnv.getHigh().size() == 3) {
                        depth     = gridEnv.getHigh().get(2).toString();
                    }
                } else {
                     throw new WebServiceException("you must specify grid size or resolution",
                                                      MISSING_PARAMETER_VALUE, getCurrentVersion());
                }
            }
        }

        /*
         * Generating the response.
         * It can be a text one (format MATRIX) or an image one (image/png, image/gif ...).
         */

        if (format.equalsIgnoreCase(MATRIX) || format.equalsIgnoreCase(NETCDF) ||
            format.equalsIgnoreCase(GEOTIFF))
        {
            final NamedLayerDP dp = NamedLayerDP.getInstance();
            final LayerDetails layer = dp.get(coverage);
            final ImageOutputStream outputStream;
            try {
                final GridCoverage2D gridCov = layer.getCoverage(objEnv, new Dimension(), null, null);
                outputStream = ImageIO.createImageOutputStream(null);
                writeImage(gridCov.getRenderedImage(), format, outputStream);
            } catch (IOException ex) {
                throw new WebServiceException(ex, NO_APPLICABLE_CODE, getCurrentVersion());
            } catch (CatalogException ex) {
                throw new WebServiceException(ex, NO_APPLICABLE_CODE, getCurrentVersion());
            }
            return Response.ok(outputStream, format).build();
        } else {
            // We are in the case of an image format requested.
            BufferedImage image = null;
            try {
                image = CSTLPortrayalService.getInstance().portray(abstractRequest);
            } catch (PortrayalException ex) {
                if (exceptions != null && exceptions.equalsIgnoreCase(EXCEPTIONS_INIMAGE)) {
                    image = CSTLPortrayalService.getInstance().writeInImage(ex, width, height);
                } else {
                    throw new WebServiceException(ex, NO_APPLICABLE_CODE, getCurrentVersion());
                }
            } catch (WebServiceException ex) {
                if (exceptions != null && exceptions.equalsIgnoreCase(EXCEPTIONS_INIMAGE)) {
                    image = CSTLPortrayalService.getInstance().writeInImage(ex, width, height);
                } else {
                    throw new WebServiceException(ex, LAYER_NOT_DEFINED, getCurrentVersion());
                }
            }
            return Response.ok(image, format).build();
        }
    }


    /**
     * Web service operation
     */
    public String describeCoverage(AbstractDescribeCoverage abstractRequest) throws JAXBException, WebServiceException {
        throw new UnsupportedOperationException();
        // TODO: fix it
        /*LOGGER.info("describeCoverage request processing");

        //we begin by extract the base attribute
        String inputVersion = abstractRequest.getVersion();
        if(inputVersion == null) {
            throw new WebServiceException("The parameter SERVICE must be specified.",
                           MISSING_PARAMETER_VALUE, getCurrentVersion(), "version");
        } else {
           isSupportedVersion(inputVersion);
           setCurrentVersion(inputVersion);
        }
        //we prepare the response object to return
        Object response;

        if (getCurrentVersion().toString().equals("1.0.0")) {
            org.constellation.wcs.v100.DescribeCoverage request = (org.constellation.wcs.v100.DescribeCoverage) abstractRequest;
            if (request.getCoverage().size() == 0) {
                throw new WebServiceException("The parameter COVERAGE must be specified.",
                        MISSING_PARAMETER_VALUE, getCurrentVersion(), "coverage");
            }
            List<Layer> layers = webServiceWorker.getLayers(request.getCoverage());

            List<CoverageOfferingType> coverages = new ArrayList<CoverageOfferingType>();
            for (Layer layer: layers) {
                if (layer.getSeries().size() == 0) {
                    throw new WebServiceException("The coverage " + layer.getName() + " is not defined.",
                            LAYER_NOT_DEFINED, getCurrentVersion());
                }

                GeographicBoundingBox inputGeoBox = layer.getGeographicBoundingBox();
                LonLatEnvelopeType               llenvelope = null;
                if(inputGeoBox != null) {
                    String crs = "WGS84(DD)";
                    List<Double> pos1 = new ArrayList<Double>();
                    pos1.add(inputGeoBox.getWestBoundLongitude());
                    pos1.add(inputGeoBox.getSouthBoundLatitude());

                    List<Double> pos2 = new ArrayList<Double>();
                    pos2.add(inputGeoBox.getEastBoundLongitude());
                    pos2.add(inputGeoBox.getNorthBoundLatitude());

                    List<DirectPositionType> pos = new ArrayList<DirectPositionType>();
                    pos.add(new DirectPositionType(pos1));
                    pos.add(new DirectPositionType(pos2));
                    llenvelope = new LonLatEnvelopeType(pos, crs);
                }
                Keywords keywords = new Keywords("WCS", layer.getName(), cleanSpecialCharacter(layer.getThematic()));

                //Spatial metadata
                org.constellation.wcs.v100.SpatialDomainType spatialDomain = new org.constellation.wcs.v100.SpatialDomainType(llenvelope);

                // temporal metadata
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                List<Object> times = new ArrayList<Object>();
                SortedSet<Date> dates = layer.getAvailableTimes();
                for (Date d:dates){
                        times.add(new TimePositionType(df.format(d)));
                }
                org.constellation.wcs.v100.TimeSequenceType temporalDomain = new org.constellation.wcs.v100.TimeSequenceType(times);

                DomainSetType domainSet = new DomainSetType(spatialDomain, temporalDomain);

                //TODO complete
                RangeSetType  rangeSetT  = new RangeSetType(null,
                                                           layer.getName(),
                                                           layer.getName(),
                                                           null,
                                                           null,
                                                           null,
                                                           null);
                RangeSet rangeSet        = new RangeSet(rangeSetT);
                //supported CRS
                SupportedCRSsType supCRS = new SupportedCRSsType(new CodeListType("EPSG:4326"));

                // supported formats
                Set<CodeListType> formats = new LinkedHashSet<CodeListType>();
                formats.add(new CodeListType("matrix"));
                formats.add(new CodeListType("jpeg"));
                formats.add(new CodeListType("png"));
                formats.add(new CodeListType("gif"));
                formats.add(new CodeListType("bmp"));
                String nativeFormat = "unknow";
                Iterator<Series> it = layer.getSeries().iterator();
                if (it.hasNext()) {
                    Series s = it.next();
                    nativeFormat = s.getFormat().getImageFormat();
                }
                SupportedFormatsType supForm = new SupportedFormatsType(nativeFormat, new ArrayList<CodeListType>(formats));

                //supported interpolations
                List<org.constellation.wcs.v100.InterpolationMethod> interpolations = new ArrayList<org.constellation.wcs.v100.InterpolationMethod>();
                interpolations.add(org.constellation.wcs.v100.InterpolationMethod.BILINEAR);
                interpolations.add(org.constellation.wcs.v100.InterpolationMethod.BICUBIC);
                interpolations.add(org.constellation.wcs.v100.InterpolationMethod.NEAREST_NEIGHBOR);
                SupportedInterpolationsType supInt = new SupportedInterpolationsType(org.constellation.wcs.v100.InterpolationMethod.NEAREST_NEIGHBOR, interpolations);

                //we build the coverage offering for this layer/coverage
                CoverageOfferingType coverage = new CoverageOfferingType(null,
                                                                         layer.getName(),
                                                                         layer.getName(),
                                                                         cleanSpecialCharacter(layer.getRemarks()),
                                                                         llenvelope,
                                                                         keywords,
                                                                         domainSet,
                                                                         rangeSet,
                                                                         supCRS,
                                                                         supForm,
                                                                         supInt);

                coverages.add(coverage);
            }
            response = new CoverageDescription(coverages, "1.0.0");

        // describeCoverage version 1.1.1
        } else {
            org.constellation.wcs.v111.DescribeCoverage request = (org.constellation.wcs.v111.DescribeCoverage) abstractRequest;
            if (request.getIdentifier().size() == 0) {
                throw new WebServiceException("The parameter IDENTIFIER must be specified",
                        MISSING_PARAMETER_VALUE, getCurrentVersion(), "identifier");
            }
            List<Layer> layers = webServiceWorker.getLayers(request.getIdentifier());

            org.constellation.ows.v110.ObjectFactory owsFactory = new org.constellation.ows.v110.ObjectFactory();
            List<CoverageDescriptionType> coverages = new ArrayList<CoverageDescriptionType>();
            for (Layer layer: layers) {
                if (layer.getSeries().size() == 0) {
                    throw new WebServiceException("the coverage " + layer.getName() +
                            " is not defined", LAYER_NOT_DEFINED, getCurrentVersion());
                }
                GeographicBoundingBox inputGeoBox = layer.getGeographicBoundingBox();
                List<JAXBElement<? extends BoundingBoxType>> bboxs = new ArrayList<JAXBElement<? extends BoundingBoxType>>();
                if(inputGeoBox != null) {
                    WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(
                                                         inputGeoBox.getWestBoundLongitude(),
                                                         inputGeoBox.getSouthBoundLatitude(),
                                                         inputGeoBox.getEastBoundLongitude(),
                                                         inputGeoBox.getNorthBoundLatitude());
                    bboxs.add(owsFactory.createWGS84BoundingBox(outputBBox));

                    String crs = "EPSG:4326";
                    BoundingBoxType outputBBox2 = new BoundingBoxType(crs,
                                                         inputGeoBox.getWestBoundLongitude(),
                                                         inputGeoBox.getSouthBoundLatitude(),
                                                         inputGeoBox.getEastBoundLongitude(),
                                                         inputGeoBox.getNorthBoundLatitude());

                    bboxs.add(owsFactory.createBoundingBox(outputBBox2));
                }

                //general metadata
                List<LanguageStringType> title   = new ArrayList<LanguageStringType>();
                title.add(new LanguageStringType(layer.getName()));
                List<LanguageStringType> _abstract   = new ArrayList<LanguageStringType>();
                _abstract.add(new LanguageStringType(cleanSpecialCharacter(layer.getRemarks())));
                List<KeywordsType> keywords = new ArrayList<KeywordsType>();
                keywords.add(new KeywordsType(new LanguageStringType("WCS"),
                                              new LanguageStringType(layer.getName())
                                              ));

                // spatial metadata
                org.constellation.wcs.v111.SpatialDomainType spatial = new org.constellation.wcs.v111.SpatialDomainType(bboxs);

                // temporal metadata
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                List<Object> times = new ArrayList<Object>();
                SortedSet<Date> dates = layer.getAvailableTimes();
                for (Date d:dates){
                        times.add(new TimePositionType(df.format(d)));
                }
                org.constellation.wcs.v111.TimeSequenceType temporalDomain = new org.constellation.wcs.v111.TimeSequenceType(times);

                CoverageDomainType domain       = new CoverageDomainType(spatial, temporalDomain);

                //supported interpolations
                List<InterpolationMethodType> intList = new ArrayList<InterpolationMethodType>();
                intList.add(new InterpolationMethodType(org.constellation.wcs.v111.InterpolationMethod.BILINEAR.value(), null));
                intList.add(new InterpolationMethodType(org.constellation.wcs.v111.InterpolationMethod.BICUBIC.value(), null));
                intList.add(new InterpolationMethodType(org.constellation.wcs.v111.InterpolationMethod.NEAREST_NEIGHBOR.value(), null));
                InterpolationMethods interpolations = new InterpolationMethods(intList, org.constellation.wcs.v111.InterpolationMethod.NEAREST_NEIGHBOR.value());
                RangeType range = new RangeType(new FieldType(cleanSpecialCharacter(layer.getThematic()),
                                                              null,
                                                              new org.constellation.ows.v110.CodeType("0.0"),
                                                              interpolations));

                //supported CRS
                List<String> supportedCRS = new ArrayList<String>();
                supportedCRS.add("EPSG:4326");

                //supported formats
                List<String> supportedFormat = new ArrayList<String>();
                supportedFormat.add("application/matrix");
                supportedFormat.add("image/png");
                supportedFormat.add("image/jpeg");
                supportedFormat.add("image/bmp");
                supportedFormat.add("image/gif");
                CoverageDescriptionType coverage = new CoverageDescriptionType(title,
                                                                               _abstract,
                                                                               keywords,
                                                                               layer.getName(),
                                                                               domain,
                                                                               range,
                                                                               supportedCRS,
                                                                               supportedFormat);

                coverages.add(coverage);
            }
            response = new CoverageDescriptions(coverages);
        }

        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();
        marshaller.marshal(response, sw);
        return sw.toString();
        } catch (CatalogException exception) {
            throw new WebServiceException(exception.getMessage(), NO_APPLICABLE_CODE, getCurrentVersion());
        }*/
    }

    /**
     * update The URL in capabilities document with the service actual URL.
     */
    private void updateURL(List<DCPTypeType> dcpList) {
        for(DCPTypeType dcp: dcpList) {
           for (Object obj: dcp.getHTTP().getGetOrPost()){
               if (obj instanceof Get){
                   Get getMethod = (Get)obj;
                   getMethod.getOnlineResource().setHref(getServiceURL() + "wcs?SERVICE=WCS&");
               } else if (obj instanceof Post){
                   Post postMethod = (Post)obj;
                   postMethod.getOnlineResource().setHref(getServiceURL() + "wcs?SERVICE=WCS&");
               }
           }
        }
    }


    /**
     * Parses a value as a floating point.
     *
     * @throws WebServiceException if the value can't be parsed.
     */
    private double parseDouble(String value) throws WebServiceException {
        value = value.trim();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new WebServiceException(Errors.format(ErrorKeys.NOT_A_NUMBER_$1, value) + "cause:" +
                           exception.getMessage(), INVALID_PARAMETER_VALUE, getCurrentVersion());
        }
    }
}
