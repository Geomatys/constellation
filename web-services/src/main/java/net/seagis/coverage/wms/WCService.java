/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.coverage.wms;

//jdk dependencies
import java.io.File;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;

// JAXB xml binding dependencies
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;

// jersey dependencies
import com.sun.ws.rest.spi.resource.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.Path;

// seagis dependencies
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.Series;
import net.seagis.coverage.web.Version;
import net.seagis.coverage.web.WMSWebServiceException;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;
import net.seagis.gml.CodeListType;
import net.seagis.gml.CodeType;
import net.seagis.gml.DirectPositionType;
import net.seagis.gml.EnvelopeEntry;
import net.seagis.gml.GridEnvelopeType;
import net.seagis.gml.GridLimitsType;
import net.seagis.gml.GridType;
import net.seagis.gml.RectifiedGridType;
import net.seagis.gml.TimePositionType;
import net.seagis.ows.AcceptFormatsType;
import net.seagis.ows.AcceptVersionsType;
import net.seagis.ows.BoundingBoxType;
import net.seagis.ows.KeywordsType;
import net.seagis.ows.LanguageStringType;
import net.seagis.ows.OWSExceptionCode;
import net.seagis.ows.OWSWebServiceException;
import net.seagis.ows.WGS84BoundingBoxType;
import net.seagis.ows.OperationsMetadata;
import net.seagis.ows.SectionsType;
import net.seagis.ows.ServiceIdentification;
import net.seagis.ows.ServiceIdentification;
import net.seagis.ows.ServiceProvider;
import net.seagis.wcs.AbstractDescribeCoverage;
import net.seagis.wcs.AbstractGetCapabilities;
import net.seagis.wcs.AbstractGetCoverage;
import net.seagis.wcs.v111.RangeType;
import net.seagis.wcs.v111.Capabilities;
import net.seagis.wcs.v100.ContentMetadata;
import net.seagis.wcs.v111.Contents;
import net.seagis.wcs.v111.CoverageDescriptionType;
import net.seagis.wcs.v111.CoverageDescriptions;
import net.seagis.wcs.v100.CoverageDescription;
import net.seagis.wcs.v111.CoverageDomainType;
import net.seagis.wcs.v100.CoverageOfferingBriefType;
import net.seagis.wcs.v100.CoverageOfferingType;
import net.seagis.wcs.v111.CoverageSummaryType;
import net.seagis.wcs.v100.WCSCapabilityType.Request;
import net.seagis.wcs.v100.DCPTypeType;
import net.seagis.wcs.v100.DCPTypeType.HTTP.Get;
import net.seagis.wcs.v100.DCPTypeType.HTTP.Post;
import net.seagis.wcs.v100.DomainSetType;
import net.seagis.wcs.v111.FieldType;
import net.seagis.wcs.v111.InterpolationMethodType;
import net.seagis.wcs.v111.InterpolationMethods;
import net.seagis.wcs.v100.Keywords;
import net.seagis.wcs.v100.LonLatEnvelopeType;
import net.seagis.wcs.v100.RangeSet;
import net.seagis.wcs.v100.RangeSetType;
import net.seagis.wcs.v100.SpatialSubsetType;
import net.seagis.wcs.v100.SupportedCRSsType;
import net.seagis.wcs.v100.SupportedFormatsType;
import net.seagis.wcs.v100.SupportedInterpolationsType;
import net.seagis.wcs.v100.WCSCapabilitiesType;
import net.seagis.wcs.v111.GridCrsType;

// geoAPI dependencies
import org.opengis.metadata.extent.GeographicBoundingBox;

// geoTools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;

/**
 *
 * @author Guilhem Legal
 */
@Path("wcs")
@Singleton
public class WCService extends WebService {

    /** 
     * Build a new instance of the webService and initialise the JAXB marshaller. 
     */
    public WCService() throws JAXBException, WebServiceException {
        super("WCS", new Version("1.1.1", true), new Version("1.0.0", false));
        
        setXMLContext("net.seagis.coverage.web:net.seagis.wcs.v100:net.seagis.wcs.v111",
                      "http://www.opengis.net/wcs");
        
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        webServiceWorker.setService("WCS", getCurrentVersion());
    }
    
    /**
     * Treat the incomming request and call the right function.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @Override
    public Response treatIncommingRequest(Object objectRequest) throws JAXBException {
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        try {
            writeParameters();
            String request = "";
            if (objectRequest == null)
                request = (String) getParameter("REQUEST", true);
            
            if (request.equalsIgnoreCase("DescribeCoverage") || (objectRequest instanceof AbstractDescribeCoverage)) {
                
                AbstractDescribeCoverage dc = (AbstractDescribeCoverage)objectRequest;
                verifyBaseParameter(0);
                
                //this wcs does not implement "store" mechanism
                String store = getParameter("STORE", false);
                if (store!= null && store.equals("true")) {
                    throwException("The service does not implement the store mechanism", 
                                   "NO_APPLICABLE_CODE", null);
                }
                
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */ 
                if (dc == null) {
                    if (getCurrentVersion().toString().equals("1.0.0")) {
                        dc = new net.seagis.wcs.v100.DescribeCoverage(getParameter("COVERAGE", true));
                    } else {
                        dc = new net.seagis.wcs.v111.DescribeCoverage(getParameter("IDENTIFIERS", true));
                    }
                }
                
                return Response.ok(describeCoverage(dc), "text/xml").build();
                    
            } else if (request.equalsIgnoreCase("GetCapabilities") || (objectRequest instanceof AbstractGetCapabilities)) {
                
                AbstractGetCapabilities gc = (AbstractGetCapabilities)objectRequest;
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (gc == null) {
                    if (!getParameter("SERVICE", true).equalsIgnoreCase("WCS")) {
                        throwException("The parameters SERVICE=WCS must be specify",
                                       "MISSING_PARAMETER_VALUE", "service");
                    }
                    String inputVersion = getParameter("VERSION", false);
                    if (inputVersion == null) {
                        inputVersion = getParameter("acceptversions", false);
                        if (inputVersion == null) {
                            inputVersion = "1.1.1";
                        } else {
                            //we verify that the version id supported
                            isSupportedVersion(inputVersion);
                        }
                    }
                    
                    this.setCurrentVersion(getBestVersion(inputVersion).getVersionNumber());
                    
                    if (getCurrentVersion().toString().equals("1.0.0")){
                        gc = new net.seagis.wcs.v100.GetCapabilities(getParameter("SECTION", false),
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
                                if (SectionsType.getExistingSections("1.1.1").contains(token)){
                                    requestedSections.add(token);
                                } else {
                                    throwException("The section " + token + " does not exist",
                                                   "INVALID_PARAMETER_VALUE", "section");
                                }   
                            }
                        } else {
                            //if there is no requested Sections we add all the sections
                            requestedSections = SectionsType.getExistingSections("1.1.1");
                        }
                        SectionsType sections       = new SectionsType(requestedSections);
                        AcceptVersionsType versions = new AcceptVersionsType("1.1.1"); 
                        gc = new net.seagis.wcs.v111.GetCapabilities(versions,
                                                                     sections,
                                                                     formats,
                                                                     null);
                    }
                } 
                return getCapabilities(gc);
                    
            } else if (request.equalsIgnoreCase("GetCoverage") || (objectRequest instanceof AbstractGetCoverage)) {
                
                AbstractGetCoverage gc = (AbstractGetCoverage)objectRequest;
                verifyBaseParameter(0);
                
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (gc == null) {
                    
                    if (getCurrentVersion().toString().equals("1.0.0")) {
                        // temporal subset
                        net.seagis.wcs.v100.TimeSequenceType temporal = null;
                        String timeParameter = getParameter("time", false);
                        if (timeParameter != null) {
                            TimePositionType time     = new TimePositionType(timeParameter);
                            temporal = new net.seagis.wcs.v100.TimeSequenceType(time); 
                        }
                    
                        /*
                        * spatial subset
                        */
                        // the boundingBox/envelope
                        List<DirectPositionType> pos = new ArrayList<DirectPositionType>();
                        String bbox          = getParameter("bbox", false);
                        if (bbox != null) {
                            StringTokenizer tokens = new StringTokenizer(bbox, ",;");
                            Double[] coordinates = new Double[tokens.countTokens()];
                            int i = 0;
                            while (tokens.hasMoreTokens()) {
                                Double value = parseDouble(tokens.nextToken());
                                coordinates[i] = value;
                                i++;
                            }
                    
                            pos.add(new DirectPositionType(coordinates[0], coordinates[2]));
                            pos.add(new DirectPositionType(coordinates[1], coordinates[3]));
                        }
                        EnvelopeEntry envelope    = new EnvelopeEntry(pos, getParameter("CRS", true));
                    
                        // the grid dimensions.
                        GridType grid = null;
                    
                        String width  = getParameter("width",  false);
                        String height = getParameter("height", false);
                        String depth  = getParameter("depth", false);
                        if (width == null || height == null) {
                            //TODO
                            grid = new RectifiedGridType();
                            String resx = getParameter("resx",  false);
                            String resy = getParameter("resy",  false);
                            String resz = getParameter("resz",  false);
                
                            if (resx == null || resy == null) {
                                throwException("The parameters WIDTH and HEIGHT or RESX and RESY have to be specified" , 
                                               "INVALID_PARAMETER_VALUE", null);
                            }
                        } else {
                            List<String> axis         = new ArrayList<String>();
                            axis.add("width");
                            axis.add("height");
                            List<BigInteger> low = new ArrayList<BigInteger>();
                            low.add(new BigInteger("0"));
                            low.add(new BigInteger("0"));
                            List<BigInteger> high = new ArrayList<BigInteger>();
                            high.add(new BigInteger(width));
                            high.add(new BigInteger(height));
                            if (depth != null) {
                                axis.add("depth");
                                low.add(new BigInteger("0"));
                                high.add(new BigInteger(depth));
                            }
                            GridLimitsType limits     = new GridLimitsType(low, high);
                            grid        = new GridType(limits, axis);
                        }
                        net.seagis.wcs.v100.SpatialSubsetType spatial = new net.seagis.wcs.v100.SpatialSubsetType(envelope, grid);
                    
                        //domain subset
                        net.seagis.wcs.v100.DomainSubsetType domain   = new net.seagis.wcs.v100.DomainSubsetType(temporal, spatial);
                    
                        //range subset (not yet used)
                        net.seagis.wcs.v100.RangeSubsetType  range    = null;
                    
                        //interpolation method
                        net.seagis.wcs.v100.InterpolationMethod interpolation = net.seagis.wcs.v100.InterpolationMethod.fromValue(getParameter("interpolation", false));
                    
                        //output
                        net.seagis.wcs.v100.OutputType output         = new net.seagis.wcs.v100.OutputType(getParameter("format", true),
                                                                                                           getParameter("response_crs", false));
                    
                        gc = new net.seagis.wcs.v100.GetCoverage(getParameter("coverage", true),
                                                                 domain,
                                                                 range,
                                                                 interpolation,
                                                                 output);
                    } else {
                       
                        // temporal subset
                        net.seagis.wcs.v111.TimeSequenceType temporal = null;
                        String timeParameter = getParameter("timeSequence", false);
                        if (timeParameter != null) {
                            TimePositionType time     = new TimePositionType(timeParameter);
                            temporal = new net.seagis.wcs.v111.TimeSequenceType(time); 
                        }
                    
                        /*
                        * spatial subset
                        */
                        // the boundingBox/envelope
                        String bbox          = getParameter("BoundingBox", true);
                        String crs           = null;
                        if (bbox.indexOf(',') != -1) {
                            crs  = bbox.substring(bbox.lastIndexOf(',') + 1, bbox.length());
                            bbox = bbox.substring(0, bbox.lastIndexOf(','));
                        } else {
                            throwException("The correct pattern for BoundingBox parameter are crs,minX,minY,maxX,maxY,CRS" , 
                                           "INVALID_PARAMETER_VALUE", "BoundingBox");
                        } 
                        BoundingBoxType envelope = null;
                        
                        if (bbox != null) {
                            StringTokenizer tokens = new StringTokenizer(bbox, ",;");
                            Double[] coordinates   = new Double[tokens.countTokens()];
                            int i = 0;
                            while (tokens.hasMoreTokens()) {
                                Double value = parseDouble(tokens.nextToken());
                                coordinates[i] = value;
                                i++;
                            }
                            if (i < 4){
                                throwException("The correct pattern for BoundingBox parameter are crs,minX,minY,maxX,maxY,CRS" , 
                                           "INVALID_PARAMETER_VALUE", "BoundingBox");
                            }
                            envelope = new BoundingBoxType(crs,coordinates[0], coordinates[2], coordinates[1], coordinates[3]);
                        }
                        
                        //domain subset
                        net.seagis.wcs.v111.DomainSubsetType domain   = new net.seagis.wcs.v111.DomainSubsetType(temporal, envelope);
                    
                        //range subset (not yet used) TODO for interpolation
                        //interpolation method
                        InterpolationMethodType interpolation = new InterpolationMethodType(getParameter("interpolation", false), null);
                        net.seagis.wcs.v111.RangeSubsetType  range    = null;
                        
                        
                        String gridType = getParameter("GridType", false);
                        if (gridType == null) {
                            gridType = "urn:ogc:def:method:WCS:1.1:2dSimpleGrid";
                        }
                        String gridOrigin = getParameter("GridOrigin", false);
                        if (gridOrigin == null) {
                            gridOrigin = "0.0,0.0";
                        }
                        StringTokenizer tokens = new StringTokenizer(gridOrigin, ",;");
                        List<Double> origin   = new ArrayList<Double>(tokens.countTokens());
                        while (tokens.hasMoreTokens()) {
                            Double value = parseDouble(tokens.nextToken());
                            origin.add(value);
                        }
                        
                        String gridOffsets = getParameter("GridOffsets", false);
                        List<Double> offset = null;
                        if (gridOffsets != null) {
                            tokens = new StringTokenizer(gridOffsets, ",;");
                            offset   = new ArrayList<Double>(tokens.countTokens());
                            while (tokens.hasMoreTokens()) {
                                Double value = parseDouble(tokens.nextToken());
                                offset.add(value);
                            }
                        }
                        
                        String gridCS = getParameter("GridCS", false);
                        if (gridCS == null) {
                            gridCS = "urn:ogc:def:cs:OGC:0.0:Grid2dSquareCS";
                        }
            
                        //output
                        CodeType codeCRS = new CodeType(crs);
                        GridCrsType grid = new GridCrsType(codeCRS,
                                                           getParameter("GridBaseCRS", false),
                                                           gridType,
                                                           origin,
                                                           offset,
                                                           gridCS,
                                                           "");
                        net.seagis.wcs.v111.OutputType output = new net.seagis.wcs.v111.OutputType(grid, getParameter("format", true));
                    
                        gc = new net.seagis.wcs.v111.GetCoverage(new net.seagis.ows.CodeType(getParameter("identifier", true)),
                                                                 domain,
                                                                 range,
                                                                 output); 
                    }   
                }
                return Response.ok(getCoverage(gc), webServiceWorker.getMimeType()).build();
                     
            } else {
                throwException("The operation " + request + " is not supported by the service",
                               "OPERATION_NOT_SUPPORTED", "request");
                //never reach
                return null;
            }
        } catch (WebServiceException ex) {
            /* We don't print the stack trace:
             * - if the user have forget a mandatory parameter.
             * - if the version number is wrong.
             */
            if (ex instanceof WMSWebServiceException) {
                WMSWebServiceException wmsex = (WMSWebServiceException)ex;
                if (!wmsex.getExceptionCode().equals(WMSExceptionCode.MISSING_PARAMETER_VALUE) &&
                    !wmsex.getExceptionCode().equals(WMSExceptionCode.VERSION_NEGOTIATION_FAILED)&&
                    !wmsex.getExceptionCode().equals(WMSExceptionCode.OPERATION_NOT_SUPPORTED)) {
                    wmsex.printStackTrace();
                }
                StringWriter sw = new StringWriter();    
                marshaller.marshal(wmsex.getServiceExceptionReport(), sw);
                return Response.ok(cleanSpecialCharacter(sw.toString()), webServiceWorker.getExceptionFormat()).build();
            } else if (ex instanceof OWSWebServiceException) {
              
                OWSWebServiceException owsex = (OWSWebServiceException)ex;
                if (!owsex.getExceptionCode().equals(OWSExceptionCode.MISSING_PARAMETER_VALUE)   &&
                    !owsex.getExceptionCode().equals(OWSExceptionCode.VERSION_NEGOTIATION_FAILED)&& 
                    !owsex.getExceptionCode().equals(OWSExceptionCode.OPERATION_NOT_SUPPORTED)) {
                    owsex.printStackTrace();
                } else {
                    logger.info(owsex.getMessage());
                }
                StringWriter sw = new StringWriter();    
                marshaller.marshal(owsex.getExceptionReport(), sw);
                return Response.ok(cleanSpecialCharacter(sw.toString()), "text/xml").build();
            } else {
                throw new IllegalArgumentException("this service can't return this kind of Exception");
            }
        }
     }
    
    /**
     * GetCapabilities operation. 
     * 
     * TODO refaire tte la fonction en separant proprement les versions.
     */ 
    public Response getCapabilities(AbstractGetCapabilities abstractRequest) throws JAXBException, WebServiceException {
        logger.info("getCapabilities request processing");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        //we begin by extract the base attribute
        String inputVersion = abstractRequest.getVersion();       
        if(inputVersion == null) {
            setCurrentVersion("1.1.1");
        } else {
           isSupportedVersion(inputVersion);
           setCurrentVersion(inputVersion);
        }
        webServiceWorker.setService("WCS", getCurrentVersion());
        
        Capabilities        responsev111 = null;
        WCSCapabilitiesType responsev100 = null;
        boolean contentMeta              = false;
        String format                    = "text/xml";
        
        if (getCurrentVersion().toString().equals("1.1.1")) {
            
            net.seagis.wcs.v111.GetCapabilities request = (net.seagis.wcs.v111.GetCapabilities) abstractRequest;
            
            // if the user have specified one format accepted (only one for now != spec)
            AcceptFormatsType formats = request.getAcceptFormats();
            if (formats == null || formats.getOutputFormat().size() == 0) {
                format = "text/xml";
            } else {
                format = formats.getOutputFormat().get(0);
                if (!format.equals("text/xml") && !format.equals("application/vnd.ogc.se_xml")){
                    throwException("This format " + format + " is not allowed",
                                   "INVALID_PARAMETER_VALUE", "format");
                }
            }
            
            //if the user have requested only some sections
            List<String> requestedSections = SectionsType.getExistingSections("1.1.1");
            
            if (request.getSections() != null && request.getSections().getSection().size() > 0) {
                requestedSections = request.getSections().getSection();
                for (String sec:requestedSections) {
                    if (!SectionsType.getExistingSections("1.1.1").contains(sec)){
                       throwException("This sections " + sec + " is not allowed",
                                       "INVALID_PARAMETER_VALUE", "sections"); 
                    }
                }
            } 
            
            // we unmarshall the static capabilities docuement
            Capabilities staticCapabilities = (Capabilities)getCapabilitiesObject();
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
                WebService.updateOWSURL(om.getOperation(), getServiceURL(), "WCS");
            }
            responsev111 = new Capabilities(si, sp, om, "1.1.1", null, null);
            
            // if the user does not request the contents section we can return the result.
            if (!requestedSections.contains("Contents") && !requestedSections.contains("All")) {
                StringWriter sw = new StringWriter();
                marshaller.marshal(responsev111, sw);
                return Response.ok(sw.toString(), format).build();
            }
                   
        } else {
            
            net.seagis.wcs.v100.GetCapabilities request = (net.seagis.wcs.v100.GetCapabilities) abstractRequest;
            
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
                    throwException("The section " + section + " does not exist",
                                   "INVALID_PARAMETER_VALUE", "section");
               }
               contentMeta = requestedSection.equals("/WCS_Capabilities/ContentMetadata"); 
            }
            WCSCapabilitiesType staticCapabilities = (WCSCapabilitiesType)((JAXBElement)getCapabilitiesObject()).getValue();
            
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
        
        net.seagis.wcs.v111.ObjectFactory wcs111Factory = new net.seagis.wcs.v111.ObjectFactory();
        net.seagis.wcs.v100.ObjectFactory wcs100Factory = new net.seagis.wcs.v100.ObjectFactory();
        net.seagis.ows.ObjectFactory owsFactory = new net.seagis.ows.ObjectFactory();
        try {
            for (Layer inputLayer: webServiceWorker.getLayers()) {
                List<LanguageStringType> title = new ArrayList<LanguageStringType>();
                title.add(new LanguageStringType(inputLayer.getName()));
                List<LanguageStringType> remark = new ArrayList<LanguageStringType>();
                remark.add(new LanguageStringType(cleanSpecialCharacter(inputLayer.getRemarks())));
                
                CoverageSummaryType       cs = new CoverageSummaryType(title, remark);
                CoverageOfferingBriefType co = new CoverageOfferingBriefType();
                
                co.addRest(wcs100Factory.createName(inputLayer.getName()));
                co.addRest(wcs100Factory.createLabel(inputLayer.getName()));
                
                GeographicBoundingBox inputGeoBox = inputLayer.getGeographicBoundingBox();
               
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
                cs.addRest(wcs111Factory.createIdentifier(inputLayer.getName()));
                summary.add(cs);
                offBrief.add(co);
            }
            contents        = new Contents(summary, null, null, null);    
            contentMetadata = new ContentMetadata("1.0.0", offBrief); 
        } catch (CatalogException exception) {
            throwException(exception.getMessage(), "NO_APPLICABLE_CODE", null);
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
    public File getCoverage(AbstractGetCoverage AbstractRequest) throws JAXBException, WebServiceException {
        logger.info("getCoverage request processing");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        webServiceWorker.setService("WCS", getCurrentVersion());
        String format = null, coverage = null, crs = null, bbox = null, time = null , interpolation = null, exceptions;
        String width = null, height = null, depth = null;
        String resx  = null, resy   = null, resz  = null;
        String gridType, gridOrigin = "", gridOffsets = "", gridCS, gridBaseCrs;
        String responseCRS = null;
        
       if (getCurrentVersion().toString().equals("1.1.1")) {
            net.seagis.wcs.v111.GetCoverage request = (net.seagis.wcs.v111.GetCoverage)AbstractRequest;
            
            if (request.getIdentifier() != null) {
                coverage = request.getIdentifier().getValue();
            } else {
                throwException("The parameter identifiers must be specify" , 
                               "MISSING_PARAMETER_VALUE", "identifier");
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
            net.seagis.wcs.v111.DomainSubsetType domain = request.getDomainSubset();
            
            bbox = getParameter("BoundingBox", true);
            if (bbox.indexOf(',') != -1) {
                crs  = bbox.substring(bbox.lastIndexOf(',') + 1, bbox.length());
                bbox = bbox.substring(0, bbox.lastIndexOf(','));
            } else {
                throwException("The correct pattern for BoundingBox parameter are minX,minY,maxX,maxY,CRS" , 
                               "INVALID_PARAMETER_VALUE", "BoundingBox");
            } 
            
            if (domain.getTemporalSubset() != null) {
                List<Object> timeSeq = domain.getTemporalSubset().getTimePositionOrTimePeriod();
                for (Object obj:timeSeq) {
                    if (obj instanceof TimePositionType)
                        time = ((TimePositionType)obj).getValue();
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
            String rangeSubset = getParameter("rangeSubset", false);
            if (rangeSubset != null && rangeSubset.indexOf(':') != -1) {
                String fieldId     = rangeSubset.substring(0, rangeSubset.indexOf(':'));
                Layer currentLayer =  webServiceWorker.getLayers(coverage).get(0);
                if (fieldId.equalsIgnoreCase(currentLayer.getThematic())){
                    interpolation = rangeSubset.substring(rangeSubset.indexOf(':')+ 1, rangeSubset.length());
                } else {
                    throwException("The field " + fieldId + " is not present in this coverage" , 
                                   "INVALID_PARAMETER_VALUE", "RangeSubset");
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

            net.seagis.wcs.v111.OutputType output = request.getOutput();
            format = output.getFormat();
            GridCrsType grid = output.getGridCRS();
            
            gridBaseCrs = grid.getGridBaseCRS();
            gridType = grid.getGridType();
            gridCS = grid.getGridCS();
            
            for (Double d: grid.getGridOffsets()) {
                gridOffsets += d.toString() + ',';
            }
            if (gridOffsets.length() > 0) {
                gridOffsets = gridOffsets.substring(0, gridOffsets.length() - 1);
            }
            
            for (Double d: grid.getGridOrigin()) {
                gridOrigin += d.toString() + ',';
            }
            if (gridOrigin.length() > 0) {
                gridOrigin = gridOrigin.substring(0, gridOrigin.length() - 1);
            }
            
            exceptions    = getParameter("exceptions", false);
            
        } else {
            
            // parameter for 1.0.0 version
            net.seagis.wcs.v100.GetCoverage request = (net.seagis.wcs.v100.GetCoverage)AbstractRequest;
            if (request.getOutput().getFormat()!= null) {
                format    = request.getOutput().getFormat().getValue();
            } else {
                throwException("The parameters Format have to be specified",
                                                 "MISSING_PARAMETER_VALUE", "Format");
            }
            
            coverage      = request.getSourceCoverage();
            if (coverage == null) {
                throwException("The parameters sourceCoverage have to be specified",
                                                 "MISSING_PARAMETER_VALUE", "sourceCoverage");
            }
            if (request.getInterpolationMethod() != null) {
                interpolation = request.getInterpolationMethod().value();
            }
            exceptions    = getParameter("exceptions", false);
            if (request.getOutput().getCrs() != null){
                responseCRS   = request.getOutput().getCrs().getValue();
            }
            
            //for now we only handle one time parameter with timePosition type
            net.seagis.wcs.v100.TimeSequenceType temporalSubset = request.getDomainSubset().getTemporalSubSet(); 
            if (temporalSubset != null) {
                for (Object timeObj:temporalSubset.getTimePositionOrTimePeriod()){
                    if (timeObj instanceof TimePositionType) {
                        time  = ((TimePositionType)timeObj).getValue();
                    }
                }
            }
            SpatialSubsetType spatial = request.getDomainSubset().getSpatialSubSet();
            EnvelopeEntry env = spatial.getEnvelope();
            crs               = env.getSrsName();
            //TODO remplacer les param dans webServiceWorker
            if (env.getPos().size() > 1) { 
                bbox  = env.getPos().get(0).getValue().get(0).toString() + ',';
                bbox += env.getPos().get(1).getValue().get(0).toString() + ',';
                bbox += env.getPos().get(0).getValue().get(1).toString() + ',';
                bbox += env.getPos().get(1).getValue().get(1).toString();
            }
            
            if (temporalSubset == null && env.getPos().size() == 0) {
                        throwException("The parameters BBOX or TIME have to be specified" , 
                                       "MISSING_PARAMETER_VALUE", null);
            }
            /* here the parameter width and height (and depth for 3D matrix)
             *  have to be fill. If not they can be replace by resx and resy 
             * (resz for 3D grid)
             */
            GridType grid = spatial.getGrid();
            if (grid instanceof RectifiedGridType){
                resx = getParameter("resx",  false);
                resy = getParameter("resy",  false);
                resz = getParameter("resz",  false);
           
            } else {
                GridEnvelopeType gridEnv = grid.getLimits().getGridEnvelope();
                if (gridEnv.getHigh().size() > 0) {
                    width         = gridEnv.getHigh().get(0).toString();
                    height        = gridEnv.getHigh().get(1).toString();
                    if (gridEnv.getHigh().size() == 3) {
                        depth     = gridEnv.getHigh().get(2).toString();
                    }
                } else {
                     throwException("you must specify grid size or resolution" , 
                                                      "MISSING_PARAMETER_VALUE", null);
                }
            }
        }
        
        webServiceWorker.setExceptionFormat(exceptions);
        webServiceWorker.setFormat(format);
        webServiceWorker.setLayer(coverage);
        webServiceWorker.setCoordinateReferenceSystem(crs);
        webServiceWorker.setBoundingBox(bbox);
        webServiceWorker.setTime(time);
        webServiceWorker.setInterpolation(interpolation);
        if (getCurrentVersion().toString().equals("1.1.1")) {
            webServiceWorker.setGridCRS(gridOrigin, gridOffsets);
        } else {
            if (width != null && height != null) {
                webServiceWorker.setDimension(width, height, depth);
            } else {
                webServiceWorker.setResolution(resx, resy, resz);
            }
        }
        webServiceWorker.setResponseCRS(responseCRS);
            
        return webServiceWorker.getImageFile();
    }
    
    
    /**
     * Web service operation
     */
    public String describeCoverage(AbstractDescribeCoverage abstractRequest) throws JAXBException, WebServiceException {
        logger.info("describeCoverage request processing");
        try {
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        //we begin by extract the base attribute
        String inputVersion = abstractRequest.getVersion();       
        if(inputVersion == null) {
            throwException("you must specify the service version" , 
                           "MISSING_PARAMETER_VALUE", "version");
        } else {
           isSupportedVersion(inputVersion);
           setCurrentVersion(inputVersion);
        }
        
        //we prepare the response object to return
        Object response;
        
        if (getCurrentVersion().toString().equals("1.0.0")) {
            net.seagis.wcs.v100.DescribeCoverage request = (net.seagis.wcs.v100.DescribeCoverage) abstractRequest;
            if (request.getCoverage().size() == 0) {
                throwException("the parameter COVERAGE must be specified", "MISSING_PARAMETER_VALUE", "coverage");
            }
            List<Layer> layers = webServiceWorker.getLayers(request.getCoverage());
        
            List<CoverageOfferingType> coverages = new ArrayList<CoverageOfferingType>();
            for (Layer layer: layers){
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
                net.seagis.wcs.v100.SpatialDomainType spatialDomain = new net.seagis.wcs.v100.SpatialDomainType(llenvelope);
                
                // temporal metadata
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                List<Object> times = new ArrayList<Object>();
                SortedSet<Date> dates = layer.getAvailableTimes();
                for (Date d:dates){
                        times.add(new TimePositionType(df.format(d))); 
                }
                net.seagis.wcs.v100.TimeSequenceType temporalDomain = new net.seagis.wcs.v100.TimeSequenceType(times);
                
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
                List<net.seagis.wcs.v100.InterpolationMethod> interpolations = new ArrayList<net.seagis.wcs.v100.InterpolationMethod>();
                interpolations.add(net.seagis.wcs.v100.InterpolationMethod.BILINEAR);
                interpolations.add(net.seagis.wcs.v100.InterpolationMethod.BICUBIC);
                interpolations.add(net.seagis.wcs.v100.InterpolationMethod.NEAREST_NEIGHBOR);
                SupportedInterpolationsType supInt = new SupportedInterpolationsType(net.seagis.wcs.v100.InterpolationMethod.NEAREST_NEIGHBOR, interpolations);
                
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
            net.seagis.wcs.v111.DescribeCoverage request = (net.seagis.wcs.v111.DescribeCoverage) abstractRequest;
            if (request.getIdentifier().size() == 0) {
                throwException("the parameter IDENTIFIER must be specified", "MISSING_PARAMETER_VALUE", "identifier");
            }
            List<Layer> layers = webServiceWorker.getLayers(request.getIdentifier());
        
            net.seagis.ows.ObjectFactory owsFactory = new net.seagis.ows.ObjectFactory();
            List<CoverageDescriptionType> coverages = new ArrayList<CoverageDescriptionType>();
            for (Layer layer: layers){
                GeographicBoundingBox inputGeoBox = layer.getGeographicBoundingBox();
                List<JAXBElement<? extends BoundingBoxType>> bboxs = new ArrayList<JAXBElement<? extends BoundingBoxType>>();
                if(inputGeoBox != null) {
                    String crs = "urn:ogc:def:crs:OGC:1.3:CRS84";
                    BoundingBoxType outputBBox = new BoundingBoxType(crs,
                                                         inputGeoBox.getWestBoundLongitude(),
                                                         inputGeoBox.getSouthBoundLatitude(),
                                                         inputGeoBox.getEastBoundLongitude(),
                                                         inputGeoBox.getNorthBoundLatitude());
                    
                    bboxs.add(owsFactory.createBoundingBox(outputBBox));        
                    /* this second version of bounding box is valid
                     WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(
                                                         inputGeoBox.getWestBoundLongitude(),
                                                         inputGeoBox.getSouthBoundLatitude(),
                                                         inputGeoBox.getEastBoundLongitude(),
                                                         inputGeoBox.getNorthBoundLatitude());
                    bbox = owsFactory.createWGS84BoundingBox(outputBBox);
                    */
                    
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
                net.seagis.wcs.v111.SpatialDomainType spatial = new net.seagis.wcs.v111.SpatialDomainType(bboxs);
                
                // temporal metadata
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                List<Object> times = new ArrayList<Object>();
                SortedSet<Date> dates = layer.getAvailableTimes();
                for (Date d:dates){
                        times.add(new TimePositionType(df.format(d))); 
                }
                net.seagis.wcs.v111.TimeSequenceType temporalDomain = new net.seagis.wcs.v111.TimeSequenceType(times);
                
                CoverageDomainType domain       = new CoverageDomainType(spatial, temporalDomain);
                
                //supported interpolations
                List<InterpolationMethodType> intList = new ArrayList<InterpolationMethodType>();
                intList.add(new InterpolationMethodType(net.seagis.wcs.v111.InterpolationMethod.BILINEAR.value(), null));
                intList.add(new InterpolationMethodType(net.seagis.wcs.v111.InterpolationMethod.BICUBIC.value(), null));
                intList.add(new InterpolationMethodType(net.seagis.wcs.v111.InterpolationMethod.NEAREST_NEIGHBOR.value(), null));
                InterpolationMethods interpolations = new InterpolationMethods(intList, net.seagis.wcs.v111.InterpolationMethod.NEAREST_NEIGHBOR.value());  
                RangeType range = new RangeType(new FieldType(cleanSpecialCharacter(layer.getThematic()), 
                                                              null, 
                                                              new net.seagis.ows.CodeType("0.0"), 
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
            throwException(exception.getMessage(), "NO_APPLICABLE_CODE", null);
            //never reach
            return null;
        }
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
            throwException(Errors.format(ErrorKeys.NOT_A_NUMBER_$1, value) + "cause:" +
                           exception.getMessage(), "INVALID_PARAMETER_VALUE", null);
            //never reach
            return 0.0;
        }
    }
}

