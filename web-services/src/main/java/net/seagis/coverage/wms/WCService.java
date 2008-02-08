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
import java.util.List;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;

// JAXB xml binding dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

// jersey dependencies
import com.sun.ws.rest.spi.resource.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.Path;

// seagis dependencies
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.Series;
import net.seagis.coverage.web.WMSWebServiceException;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;
import net.seagis.gml.CodeListType;
import net.seagis.gml.DirectPositionType;
import net.seagis.gml.EnvelopeEntry;
import net.seagis.gml.GridEnvelopeType;
import net.seagis.gml.GridLimitsType;
import net.seagis.gml.GridType;
import net.seagis.gml.RectifiedGridType;
import net.seagis.gml.TimePositionType;
import net.seagis.ows.AcceptFormatsType;
import net.seagis.ows.BoundingBoxType;
import net.seagis.ows.KeywordsType;
import net.seagis.ows.LanguageStringType;
import net.seagis.ows.Operation;
import net.seagis.ows.WGS84BoundingBoxType;
import net.seagis.ows.CodeType;
import net.seagis.ows.OperationsMetadata;
import net.seagis.ows.SectionsType;
import net.seagis.ows.ServiceIdentification;
import net.seagis.ows.ServiceIdentification;
import net.seagis.ows.ServiceProvider;
import net.seagis.wcs.RangeType;
import net.seagis.wcs.Capabilities;
import net.seagis.wcs.ContentMetadata;
import net.seagis.wcs.Contents;
import net.seagis.wcs.CoverageDescriptionType;
import net.seagis.wcs.CoverageDescriptions;
import net.seagis.wcs.CoverageDescription;
import net.seagis.wcs.CoverageDomainType;
import net.seagis.wcs.CoverageOfferingBriefType;
import net.seagis.wcs.CoverageOfferingType;
import net.seagis.wcs.CoverageSummaryType;
import net.seagis.wcs.WCSCapabilityType.Request;
import net.seagis.wcs.DCPTypeType;
import net.seagis.wcs.DCPTypeType.HTTP.Get;
import net.seagis.wcs.DCPTypeType.HTTP.Post;
import net.seagis.wcs.DescribeCoverage;
import net.seagis.wcs.DescribeCoverage;
import net.seagis.wcs.DescribeCoverage;
import net.seagis.wcs.DomainSetType;
import net.seagis.wcs.DomainSubsetType;
import net.seagis.wcs.FieldType;
import net.seagis.wcs.GetCapabilities;
import net.seagis.wcs.GetCoverage;
import net.seagis.wcs.InterpolationMethod;
import net.seagis.wcs.InterpolationMethodType;
import net.seagis.wcs.InterpolationMethods;
import net.seagis.wcs.Keywords;
import net.seagis.wcs.LonLatEnvelopeType;
import net.seagis.wcs.OutputType;
import net.seagis.wcs.RangeSet;
import net.seagis.wcs.RangeSetType;
import net.seagis.wcs.RangeSubsetType;
import net.seagis.wcs.SupportedCRSsType;
import net.seagis.wcs.SpatialDomainType;
import net.seagis.wcs.SpatialSubsetType;
import net.seagis.wcs.SupportedFormatsType;
import net.seagis.wcs.SupportedInterpolationsType;
import net.seagis.wcs.TimeSequenceType;
import net.seagis.wcs.WCSCapabilitiesType;
import static net.seagis.coverage.wms.WMSExceptionCode.*;

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
        super("WCS", false, "1.1.1", "1.0.0");
        //TODO true for 1.1.1
        JAXBContext jbcontext = JAXBContext.newInstance("net.seagis.coverage.web:net.seagis.wcs");
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl("http://www.opengis.net/wcs"));
        unmarshaller = jbcontext.createUnmarshaller();
        
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        webServiceWorker.setService("WCS", getCurrentVersion().toString());
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
            
            if (request.equalsIgnoreCase("DescribeCoverage") || (objectRequest instanceof DescribeCoverage)) {
                
                DescribeCoverage dc = (DescribeCoverage)objectRequest;
                verifyBaseParameter(0);
                
                //this wcs does not implement "store" mechanism
                String store = getParameter("STORE", false);
                if (store!= null && store.equals("true")) {
                    throw new WMSWebServiceException("The service does not implement the store mechanism", 
                                                  NO_APPLICABLE_CODE, getCurrentVersion());
                }
                
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */ 
                if (dc == null) {
                    String identifiers;
                    if (getCurrentVersion().toString().equals("1.0.0")) {
                        identifiers = getParameter("COVERAGE", true);
                    } else {
                        identifiers = getParameter("IDENTIFIER", true);
                    }
                    dc = new DescribeCoverage(getCurrentVersion().toString(), identifiers);
                }
                
                return Response.ok(describeCoverage(dc), "text/xml").build();
                    
            } else if (request.equalsIgnoreCase("GetCapabilities") || (objectRequest instanceof GetCapabilities)) {
                
                GetCapabilities gc = (GetCapabilities)objectRequest;
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (gc == null) {
                    if (!getParameter("SERVICE", true).equalsIgnoreCase("WCS")) {
                        throw new WMSWebServiceException("The parameters SERVICE=WCS must be specify",
                                         MISSING_PARAMETER_VALUE, getCurrentVersion());
                    }
                    if (getCurrentVersion().toString().equals("1.0.0")){
                        gc = new GetCapabilities(getParameter("VERSION", false),
                                                 getParameter("SECTION", false),
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
                                    throw new WMSWebServiceException("The section " + token + " does not exist",
                                                                INVALID_PARAMETER_VALUE, getCurrentVersion());
                                }   
                            }
                        } else {
                            //if there is no requested Sections we add all the sections
                            requestedSections = SectionsType.getExistingSections("1.1.1");
                        }
                        SectionsType sections     = new SectionsType(requestedSections);
                        gc = new GetCapabilities(getParameter("VERSION", false),
                                                 null,
                                                 sections,
                                                 formats,
                                                 null);
                    }
                }
                return getCapabilities(gc);
                    
            } else if (request.equalsIgnoreCase("GetCoverage") || (objectRequest instanceof GetCoverage)) {
                
                GetCoverage gc = (GetCoverage)objectRequest;
                verifyBaseParameter(0);
                
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (gc == null) {
                    
                    // temporal subset
                    TimeSequenceType temporal = null;
                    String timeParameter = getParameter("time", false);
                    if (timeParameter != null) {
                        TimePositionType time     = new TimePositionType(timeParameter);
                        temporal = new TimeSequenceType(time); 
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
                            throw new WMSWebServiceException("The parameters WIDTH and HEIGHT or RESX and RESY have to be specified" , 
                                                          INVALID_PARAMETER_VALUE, getCurrentVersion());
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
                    SpatialSubsetType spatial = new SpatialSubsetType(envelope, grid);
                    
                    //domain subset
                    DomainSubsetType domain   = new DomainSubsetType(temporal, spatial);
                    
                    //range subset (not yet used)
                    RangeSubsetType  range    = null;
                    
                    //interpolation method
                    InterpolationMethodType interpolation = new InterpolationMethodType(getParameter("interpolation", false), null);
                    
                    //output
                    OutputType output         = new OutputType(getParameter("format", true),
                                                               getParameter("response_crs", false));
                    
                    gc = new GetCoverage(getParameter("coverage", true),
                                         domain,
                                         range,
                                         interpolation,
                                         output,
                                         getCurrentVersion().toString());
                }
                return Response.ok(getCoverage(gc), webServiceWorker.getMimeType()).build();
                     
            } else {
                throw new WMSWebServiceException("The operation " + request + " is not supported by the service",
                                              OPERATION_NOT_SUPPORTED, getCurrentVersion());
            }
        } catch (WebServiceException ex) {
            /* We don't print the stack trace:
             * - if the user have forget a mandatory parameter.
             * - if the version number is wrong.
             */
            if (ex instanceof WMSWebServiceException) {
                WMSWebServiceException wmsex = (WMSWebServiceException)ex;
                if (!wmsex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
                    !wmsex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED)) {
                    wmsex.printStackTrace();
                }
                StringWriter sw = new StringWriter();    
                marshaller.marshal(wmsex.getServiceExceptionReport(), sw);
                return Response.ok(cleanSpecialCharacter(sw.toString()), webServiceWorker.getExceptionFormat()).build();
            } else {
                throw new IllegalArgumentException("this service can't return OWS Exception");
            }
        }
     }
    
    /**
     * GetCapabilities operation. 
     */ 
    public Response getCapabilities(GetCapabilities request) throws JAXBException, WebServiceException {
        logger.info("getCapabilities request received");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        //we begin by extract the base attribute
        String inputVersion = request.getVersion();       
        if(inputVersion != null && inputVersion.equals("1.1.1")) {
            setCurrentVersion("1.1.1");
        } else {
            setCurrentVersion("1.0.0");
        } 
        webServiceWorker.setService("WCS", getCurrentVersion().toString());
        
        Capabilities        responsev111 = null;
        WCSCapabilitiesType responsev100 = null;
        boolean contentMeta              = false;
        String format                    = "text/xml";
        
        if (inputVersion.equals("1.1.1")) {
            
            // if the user have specified one format accepted (only one for now != spec)
            AcceptFormatsType formats = request.getAcceptFormats();
            if (formats == null || formats.getOutputFormat().size() > 0) {
                format = "text/xml";
            } else {
                format = formats.getOutputFormat().get(0);
                if (!format.equals("text/xml") && !format.equals("application/vnd.ogc.se_xml")){
                    throw new WMSWebServiceException("This format " + format + " is not allowed",
                                       INVALID_PARAMETER_VALUE, getCurrentVersion());
                }
            }
            
            //if the user have requested only some sections
            List<String> requestedSections;
            if (request.geSections() != null && request.geSections().getSection().size() > 0) {
                requestedSections = request.geSections().getSection();
            } else {
                requestedSections = SectionsType.getExistingSections("1.1.1");
            }
            
            // we unmarshall the static capabilities docuement
            Capabilities staticCapabilities = (Capabilities)getCapabilitiesObject(getCurrentVersion());
            ServiceIdentification si = null;
            ServiceProvider       sp = null;
            OperationsMetadata    om = null;
        
            //we add the static sections if the are included in the requested sections
            if (requestedSections.contains("ServiceProvider")) 
                sp = staticCapabilities.getServiceProvider();
            if (requestedSections.contains("ServiceIdentification")) 
                si = staticCapabilities.getServiceIdentification();
            if (requestedSections.contains("OperationsMetadata")) { 
                om = staticCapabilities.getOperationsMetadata();
                //we update the url in the static part.
                for (Operation op:om.getOperation()) {
                    op.getDCP().get(0).getHTTP().getGetOrPost().get(0).getValue().setHref(getServiceURL() + "wcs?REQUEST=" + op.getName());
                    op.getDCP().get(1).getHTTP().getGetOrPost().get(0).getValue().setHref(getServiceURL() + "wcs?REQUEST=" + op.getName());
                }
            }
            responsev111 = new Capabilities(si, sp, om, "1.1.1", null, null);
            
            // if the user does not request the contents section we can return the result.
            if (!requestedSections.contains("Contents")) {
                StringWriter sw = new StringWriter();
                marshaller.marshal(responsev111, sw);
                return Response.ok(sw.toString(), format).build();
            }
                   
        } else {
            
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
                    throw new WMSWebServiceException("The section " + section + " does not exist",
                                          INVALID_PARAMETER_VALUE, getCurrentVersion());
               }
               contentMeta = requestedSection.equals("/WCS_Capabilities/ContentMetadata"); 
            }
            WCSCapabilitiesType staticCapabilities = (WCSCapabilitiesType)((JAXBElement)getCapabilitiesObject(getCurrentVersion())).getValue();
            
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
        Contents contents;
        ContentMetadata contentMetadata;
        
        //we get the list of layers
        List<CoverageSummaryType>        summary = new ArrayList<CoverageSummaryType>();
        List<CoverageOfferingBriefType> offBrief = new ArrayList<CoverageOfferingBriefType>();
        
        net.seagis.wcs.ObjectFactory wcsFactory = new net.seagis.wcs.ObjectFactory();
        net.seagis.ows.ObjectFactory owsFactory = new net.seagis.ows.ObjectFactory();
        try {
            for (Layer inputLayer: webServiceWorker.getLayers()) {
                CoverageSummaryType       cs = new CoverageSummaryType();
                CoverageOfferingBriefType co = new CoverageOfferingBriefType();
                
                cs.addRest(wcsFactory.createIdentifier(inputLayer.getName()));
                co.addRest(wcsFactory.createName(inputLayer.getName()));
                co.addRest(wcsFactory.createLabel(inputLayer.getName()));
                
                GeographicBoundingBox inputGeoBox = inputLayer.getGeographicBoundingBox();
               
                if(inputGeoBox != null) {
                     String crs = "WGS84(DD)";
                    if (inputVersion.equals("1.1.1")){
                        WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(crs, 
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
           
                summary.add(cs);
                offBrief.add(co);
            }
            contents        = new Contents(summary, null, null, null);    
            contentMetadata = new ContentMetadata("1.0.0", offBrief); 
        } catch (CatalogException exception) {
            throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, getCurrentVersion());
        }
            
        
        StringWriter sw = new StringWriter();
        if (inputVersion.equals("1.1.1")) {
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
    public File getCoverage(GetCoverage request) throws JAXBException, WebServiceException {
        logger.info("getCoverage recu");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        webServiceWorker.setService("WCS", getCurrentVersion().toString());
        String format, coverage, crs, bbox = null, time = null , interpolation = null, exceptions;
        String width = null, height = null, depth = null;
        String resx  = null, resy   = null, resz  = null;
        String gridType, gridOrigin = null, gridOffsets = null, gridCS, gridBaseCrs;
        String responseCRS = null;
        
       if (getCurrentVersion().toString().equals("1.1.1")){
            
            coverage = getParameter("identifier", true);
            
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
            bbox = getParameter("BoundingBox", true);
            if (bbox.indexOf(',') != -1) {
                crs  = bbox.substring(bbox.lastIndexOf(',') + 1, bbox.length());
                bbox = bbox.substring(0, bbox.lastIndexOf(','));
            } else {
                throw new WMSWebServiceException("The correct pattern for BoundingBox parameter are minX,minY,maxX,maxY,CRS" , 
                            INVALID_PARAMETER_VALUE, getCurrentVersion());
            } 
            time = getParameter("timeSequence", false);
            
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
                    throw new WMSWebServiceException("The field " + fieldId + " is not present in this coverage" , 
                            INVALID_PARAMETER_VALUE, getCurrentVersion());
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

            format = getParameter("format", true);
            
            gridBaseCrs = getParameter("GridBaseCRS", false);
            gridOffsets = getParameter("GridOffsets", false);
            
            gridType = getParameter("GridType", false);
            if (gridType == null) {
                gridType = "urn:ogc:def:method:WCS:1.1:2dSimpleGrid";
            }
            gridOrigin = getParameter("GridOrigin", false);
            if (gridType == null) {
                gridType = "0.0,0.0";
            }
            
            gridCS = getParameter("GridCS", false);
            if (gridType == null) {
                gridType = "urn:ogc:def:cs:OGC:0.0:Grid2dSquareCS";
            }
            
            getParameter("store", false);
            exceptions    = getParameter("exceptions", false);
            
        } else {
            
            // parameter for 1.0.0 version
            if (request.getOutput().getFormat()!= null) {
                format    = request.getOutput().getFormat().getValue();
            } else {
                throw new WMSWebServiceException("The parameters Format have to be specified",
                                              MISSING_PARAMETER_VALUE, getCurrentVersion());
            }
            
            coverage      = request.getSourceCoverage();
            if (coverage == null) {
                throw new WMSWebServiceException("The parameters sourceCoverage have to be specified",
                                              MISSING_PARAMETER_VALUE, getCurrentVersion());
            }
            if (request.getInterpolationMethod() != null) {
                interpolation = request.getInterpolationMethod().getValue();
            }
            exceptions    = getParameter("exceptions", false);
            if (request.getOutput().getCrs() != null){
                responseCRS   = request.getOutput().getCrs().getValue();
            }
            
            //for now we only handle one time parameter with timePosition type
            TimeSequenceType temporalSubset = request.getDomainSubset().getTemporalSubSet(); 
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
                        throw new WMSWebServiceException("The parameters BBOX or TIME have to be specified" , 
                                                      MISSING_PARAMETER_VALUE, getCurrentVersion());
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
                     throw new WMSWebServiceException("you must specify grid size or resolution" , 
                                                   MISSING_PARAMETER_VALUE, getCurrentVersion());
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
    public String describeCoverage(DescribeCoverage request) throws JAXBException, WebServiceException {
        logger.info("describeCoverage recu");
        try {
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        //we prepare the response object to return
        Object response;
        
        if (getCurrentVersion().toString().equals("1.0.0")) {
        
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
                SpatialDomainType spatialDomain = new SpatialDomainType(llenvelope);
                
                // temporal metadata
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                List<Object> times = new ArrayList<Object>();
                SortedSet<Date> dates = layer.getAvailableTimes();
                for (Date d:dates){
                        times.add(new TimePositionType(df.format(d))); 
                }
                TimeSequenceType temporalDomain = new TimeSequenceType(times);
                
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
                List<CodeListType> formats = new ArrayList<CodeListType>();
                formats.add(new CodeListType("matrix"));
                formats.add(new CodeListType("jpeg"));
                formats.add(new CodeListType("png"));
                formats.add(new CodeListType("gif"));
                formats.add(new CodeListType("bmp"));
                String nativeFormat = "unknow";
                Iterator<Series> it = layer.getSeries().iterator();
                if (it.hasNext()) {
                    Series s = it.next();
                    nativeFormat = s.getFormat().getMimeType();
                }
                SupportedFormatsType supForm = new SupportedFormatsType(nativeFormat, formats); 
                
                //supported interpolations
                List<InterpolationMethod> interpolations = new ArrayList<InterpolationMethod>();
                interpolations.add(InterpolationMethod.BILINEAR);
                interpolations.add(InterpolationMethod.BICUBIC);
                interpolations.add(InterpolationMethod.NEAREST_NEIGHBOR);
                SupportedInterpolationsType supInt = new SupportedInterpolationsType(InterpolationMethod.BILINEAR, interpolations);
                
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
        
            List<Layer> layers = webServiceWorker.getLayers(request.getIdentifier());
        
            net.seagis.ows.ObjectFactory owsFactory = new net.seagis.ows.ObjectFactory();
            List<CoverageDescriptionType> coverages = new ArrayList<CoverageDescriptionType>();
            for (Layer layer: layers){
                GeographicBoundingBox inputGeoBox = layer.getGeographicBoundingBox();
                JAXBElement<? extends BoundingBoxType> bbox = null;
                if(inputGeoBox != null) {
                    String crs = "WGS84(DD)";
                
                    WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(crs, 
                                                         inputGeoBox.getWestBoundLongitude(),
                                                         inputGeoBox.getSouthBoundLatitude(),
                                                         inputGeoBox.getEastBoundLongitude(),
                                                         inputGeoBox.getNorthBoundLatitude());
                    bbox = owsFactory.createWGS84BoundingBox(outputBBox);
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
                SpatialDomainType spatial = new SpatialDomainType(bbox);
                
                // temporal metadata
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                List<Object> times = new ArrayList<Object>();
                SortedSet<Date> dates = layer.getAvailableTimes();
                for (Date d:dates){
                        times.add(new TimePositionType(df.format(d))); 
                }
                TimeSequenceType temporalDomain = new TimeSequenceType(times);
                
                CoverageDomainType domain       = new CoverageDomainType(spatial, temporalDomain);
                
                //supported interpolations
                List<InterpolationMethodType> intList = new ArrayList<InterpolationMethodType>();
                intList.add(new InterpolationMethodType(InterpolationMethod.BILINEAR.value(), null));
                intList.add(new InterpolationMethodType(InterpolationMethod.BICUBIC.value(), null));
                intList.add(new InterpolationMethodType(InterpolationMethod.NEAREST_NEIGHBOR.value(), null));
                InterpolationMethods interpolations = new InterpolationMethods(intList, InterpolationMethod.BILINEAR.value());  
                RangeType range = new RangeType(new FieldType(layer.getThematic(), null, new CodeType("0.0"), interpolations));
               
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
            throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, getCurrentVersion());
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
            throw new WMSWebServiceException(Errors.format(ErrorKeys.NOT_A_NUMBER_$1, value),
                    exception, INVALID_PARAMETER_VALUE, getCurrentVersion());
        }
    }
}

