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
import java.util.ArrayList;
import java.util.List;

// JAXB xml binding dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

// jersey dependencies
import com.sun.ws.rest.spi.resource.Singleton;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;
import javax.ws.rs.core.Response;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;

// seagis dependencies
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.Series;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;
import net.seagis.gml.CodeListType;
import net.seagis.gml.DirectPositionType;
import net.seagis.gml.TimePositionType;
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
import net.seagis.wcs.DCPTypeType.HTTP.Get;
import net.seagis.wcs.DCPTypeType.HTTP.Post;
import net.seagis.wcs.DomainSetType;
import net.seagis.wcs.FieldType;
import net.seagis.wcs.InterpolationMethod;
import net.seagis.wcs.InterpolationMethodType;
import net.seagis.wcs.InterpolationMethods;
import net.seagis.wcs.Keywords;
import net.seagis.wcs.LonLatEnvelopeType;
import net.seagis.wcs.RangeSet;
import net.seagis.wcs.RangeSetType;
import net.seagis.wcs.SupportedCRSsType;
import net.seagis.wcs.SpatialDomainType;
import net.seagis.wcs.SupportedFormatsType;
import net.seagis.wcs.SupportedInterpolationsType;
import net.seagis.wcs.TimeSequenceType;
import net.seagis.wcs.WCSCapabilitiesType;

// geoAPI dependencies
import org.opengis.metadata.extent.GeographicBoundingBox;

/**
 *
 * @author Guilhem Legal
 */
@UriTemplate("wcs")
@Singleton
public class WCService extends WebService {

    /**
     * The http context containing the request parameter
     */
    @HttpContext
    private UriInfo context;
    
    /** 
     * Build a new instance of the webService and initialise the JAXB marshaller. 
     */
    public WCService() throws JAXBException, WebServiceException {
        super("WCS", "1.1.1", "1.0.0");
        JAXBContext jbcontext = JAXBContext.newInstance("net.seagis.ogc:net.seagis.wcs");
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl("http://www.opengis.net/wcs"));
        unmarshaller = jbcontext.createUnmarshaller();
        
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        webServiceWorker.setService("WCS", getCurrentVersion().toString());
    }
    
    /**
     * Treat the incomming GET request.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @HttpMethod("GET")
    public Response doGET() throws JAXBException  {

        return treatIncommingRequest();
    }
    
     /**
     * Treat the incomming POST request.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @HttpMethod("POST")
    public Response doPOST(String request) throws JAXBException  {
        logger.info("request: " + request);
        final StringTokenizer tokens = new StringTokenizer(request, "&");
        String log = "";
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            String paramName  = token.substring(0, token.indexOf('='));
            String paramValue = token.substring(token.indexOf('=')+ 1);
            log += "put: " + paramName + "=" + paramValue + '\n';
            context.getQueryParameters().add(paramName, paramValue);
        }
        logger.info(log);
        return treatIncommingRequest();
    }
    
    /**
     * Treat the incomming request and call the right function.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @Override
    public Response treatIncommingRequest() throws JAXBException {
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        writeParameters();
        try {
            String request = (String) getParameter("REQUEST", true);
            if (request.equalsIgnoreCase("DescribeCoverage")) {
                    
                return Response.Builder.representation(describeCoverage(), "text/xml").build();
                    
            } else if (request.equalsIgnoreCase("GetCapabilities")) {
                    
                return getCapabilities();
                    
            } else if (request.equalsIgnoreCase("GetCoverage")) {
                    
                return Response.Builder.representation(getCoverage(), webServiceWorker.getMimeType()).build();
                     
            } else {
                throw new WebServiceException("The operation " + request + " is not supported by the service",
                                              WMSExceptionCode.OPERATION_NOT_SUPPORTED, getCurrentVersion());
            }
        } catch (WebServiceException ex) {
            
            //we don't print the stack trace if the user have forget a mandatory parameter.
            if (ex.getServiceExceptionReport().getServiceExceptions().get(0).getCode().equals(WMSExceptionCode.MISSING_PARAMETER_VALUE) {
                ex.printStackTrace();
            }
            StringWriter sw = new StringWriter();    
            marshaller.marshal(ex.getServiceExceptionReport(), sw);
            return Response.Builder.representation(cleanSpecialCharacter(sw.toString()), webServiceWorker.getExceptionFormat()).build();
        }
     }
    
    /**
     * Web service operation
     */ 
    public Response getCapabilities() throws JAXBException, WebServiceException {
        logger.info("getCapabilities request received");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        //we begin by extract the base attribute
        if (!getParameter("SERVICE", true).equalsIgnoreCase("WCS")) {
            throw new WebServiceException("The parameters SERVICE=WCS must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, getCurrentVersion());
        }
        
        String inputVersion = getParameter("VERSION", false);
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
            format = getParameter("AcceptFormats", false);
            if (format == null) {
                format = "text/xml";
            } else {
                if (!format.equals("text/xml") && !format.equals("application/vnd.ogc.se_xml")){
                    throw new WebServiceException("This format " + format + " is not allowed",
                                       WMSExceptionCode.INVALID_PARAMETER_VALUE, getCurrentVersion());
                }
            }
            
            //if the user have requested only some sections
            String sections = getParameter("Sections", false);
            List<String> requestedSections = new ArrayList<String>();
            if (sections != null) {
                final StringTokenizer tokens = new StringTokenizer(sections, ",;");
                while (tokens.hasMoreTokens()) {
                    final String token = tokens.nextToken().trim();
                    if (SectionsType.getExistingSections("1.1.1").contains(token)){
                        requestedSections.add(token);
                    } else {
                        throw new WebServiceException("The section " + token + " does not exist",
                                                 WMSExceptionCode.INVALID_PARAMETER_VALUE, getCurrentVersion());
                    }
                }
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
                return Response.Builder.representation(sw.toString(), format).build();
            }
                   
        } else {
            
            /*
             * In WCS 1.0.0 the user can request only one section 
             * ( or all by ommiting the parameter section)
             */ 
            String section = getParameter("SECTION", false);
            String requestedSection = null;
            if (section != null) {
                if (SectionsType.getExistingSections("1.0.0").contains(section)){
                    requestedSection = section;
                } else {
                    throw new WebServiceException("The section " + section + " does not exist",
                                          WMSExceptionCode.INVALID_PARAMETER_VALUE, getCurrentVersion());
               }
               contentMeta = requestedSection.equals("/WCS_Capabilities/ContentMetadata"); 
            }
            WCSCapabilitiesType staticCapabilities = (WCSCapabilitiesType)((JAXBElement)getCapabilitiesObject(getCurrentVersion())).getValue();
            
            if (requestedSection == null || requestedSection.equals("/WCS_Capabilities/Capability") || requestedSection.equals("/")) {
                //we update the url in the static part.
                ((Get) staticCapabilities.getCapability().getRequest().getGetCapabilities().getDCPType().get(0).getHTTP().getGetOrPost().get(0)).getOnlineResource().setHref(getServiceURL() + "wcs?REQUEST=GetCapabilities&");
                ((Post)staticCapabilities.getCapability().getRequest().getGetCapabilities().getDCPType().get(1).getHTTP().getGetOrPost().get(0)).getOnlineResource().setHref(getServiceURL() + "wcs?REQUEST=GetCapabilities&");
            
                ((Get) staticCapabilities.getCapability().getRequest().getDescribeCoverage().getDCPType().get(0).getHTTP().getGetOrPost().get(0)).getOnlineResource().setHref(getServiceURL() + "wcs?REQUEST=DescribeCoverage&");
                ((Post)staticCapabilities.getCapability().getRequest().getDescribeCoverage().getDCPType().get(1).getHTTP().getGetOrPost().get(0)).getOnlineResource().setHref(getServiceURL() + "wcs?REQUEST=DescribeCoverage&");
            
                ((Get) staticCapabilities.getCapability().getRequest().getGetCoverage().getDCPType().get(0).getHTTP().getGetOrPost().get(0)).getOnlineResource().setHref(getServiceURL() + "wcs?REQUEST=GetCoverage&");
                ((Post)staticCapabilities.getCapability().getRequest().getGetCoverage().getDCPType().get(1).getHTTP().getGetOrPost().get(0)).getOnlineResource().setHref(getServiceURL() + "wcs?REQUEST=GetCoverage&");
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
                return Response.Builder.representation(sw.toString(), format).build();
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
            throw new WebServiceException(exception, WMSExceptionCode.NO_APPLICABLE_CODE, getCurrentVersion());
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
        
        return Response.Builder.representation(sw.toString(), format).build();
        
    }
    
    
    /**
     * Web service operation
     */
    public File getCoverage() throws JAXBException, WebServiceException {
        logger.info("getCoverage recu");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        verifyBaseParameter(0);
        webServiceWorker.setService("WCS", getCurrentVersion().toString());
        String format, coverage, crs, bbox, time, interpolation, exceptions;
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
                throw new WebServiceException("The correct pattern for BoundingBox parameter are minX,minY,maxX,maxY,CRS" , 
                            WMSExceptionCode.INVALID_PARAMETER_VALUE, getCurrentVersion());
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
                    throw new WebServiceException("The field " + fieldId + " is not present in this coverage" , 
                            WMSExceptionCode.INVALID_PARAMETER_VALUE, getCurrentVersion());
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
            format        = getParameter("format", true);
            coverage      = getParameter("coverage", true);
            crs           = getParameter("CRS", true);
            bbox          = getParameter("bbox", false);
            time          = getParameter("time", false);
            interpolation = getParameter("interpolation", false);
            exceptions    = getParameter("exceptions", false);
            responseCRS   = getParameter("response_crs", false);
            
            /* here the parameter width and height (and depth for 3D matrix)
             *  have to be fill. If not they can be replace by resx and resy 
             * (resz for 3D grid)
             */
            width         = getParameter("width",  false);
            height        = getParameter("height", false);
            depth         = getParameter("depth", false);
            if (width == null || height == null) {
                resx = getParameter("resx",  false);
                resy = getParameter("resy",  false);
                resz = getParameter("resz",  false);
                
                if (resx == null || resy == null) {
                    throw new WebServiceException("The parameters WIDTH and HEIGHT or RESX and RESY have to be specified" , 
                              WMSExceptionCode.INVALID_PARAMETER_VALUE, getCurrentVersion());
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
            if (width == null || height == null) {
                webServiceWorker.setDimension(width, height, depth);
            } else {
                webServiceWorker.setResolution(resx, resy, resz);
            }
        }
        webServiceWorker.setResponseCoordinateReferenceSystem(responseCRS);
            
        return webServiceWorker.getImageFile();
    }
    
    
    /**
     * Web service operation
     */
    public String describeCoverage() throws JAXBException, WebServiceException {
        logger.info("describeCoverage recu");
        try {
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        verifyBaseParameter(0);
        String identifiers;
        if (getCurrentVersion().toString().equals("1.0.0")) {
            identifiers = getParameter("COVERAGE", true);
        } else {
            identifiers = getParameter("IDENTIFIER", true);
        }
        List<Layer> layers = webServiceWorker.getLayers(identifiers);
        
        //this wcs does not implement "store" mechanism
        String store = getParameter("STORE", false);
        if (store!= null && store.equals("true")) {
             throw new WebServiceException("The service does not implement the store mechanism", 
                     WMSExceptionCode.NO_APPLICABLE_CODE, getCurrentVersion());
        }
        
        //we prepare the response object to return
        Object response;
        
        if (getCurrentVersion().toString().equals("1.0.0")){
            
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
                String nativeFormat = "??";
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
                _abstract.add(new LanguageStringType(layer.getRemarks()));
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
                supportedFormat.add("matrix");
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
            throw new WebServiceException(exception, WMSExceptionCode.NO_APPLICABLE_CODE, getCurrentVersion());
        }
    }

    /**
     * Return the current Http context. 
     */
    @Override
    protected UriInfo getContext() {
        return this.context;
    }
}

