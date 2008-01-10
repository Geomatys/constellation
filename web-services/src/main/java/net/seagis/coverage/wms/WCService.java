/*
 * Sicade - SystÃ¨mes intÃ©grÃ©s de connaissances pour l'aide Ã  la dÃ©cision en environnement
 * (C) 2005, Institut de Recherche pour le DÃ©veloppement
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
import java.util.StringTokenizer;
import javax.ws.rs.core.Response;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;

// seagis dependencies
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;
import net.seagis.gml.DirectPositionType;
import net.seagis.ows.BoundingBoxType;
import net.seagis.ows.Operation;
import net.seagis.wcs.RangeType;
import net.seagis.ows.WGS84BoundingBoxType;
import net.seagis.wcs.Capabilities;
import net.seagis.wcs.ContentMetadata;
import net.seagis.wcs.Contents;
import net.seagis.wcs.CoverageDescriptionType;
import net.seagis.wcs.CoverageDescriptions;
import net.seagis.wcs.CoverageDomainType;
import net.seagis.wcs.CoverageOfferingBriefType;
import net.seagis.wcs.CoverageSummaryType;
import net.seagis.wcs.DCPTypeType.HTTP.Get;
import net.seagis.wcs.DCPTypeType.HTTP.Post;
import net.seagis.wcs.LonLatEnvelopeType;
import net.seagis.wcs.SpatialDomainType;
import net.seagis.wcs.ServiceType;

// geoAPI dependencies
import net.seagis.wcs.WCSCapabilitiesType;
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
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl("http://www.opengis.net/wcs/1.1.1"));
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
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            String paramName  = token.substring(0, token.indexOf('='));
            String paramValue = token.substring(token.indexOf('=')+ 1);
            logger.info("put: " + paramName + "=" + paramValue);
            context.getQueryParameters().add(paramName, paramValue);
        }
        
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

        try {
            String request = (String) getParameter("REQUEST", true);
            if (request.equalsIgnoreCase("DescribeCoverage")) {
                    
                return Response.Builder.representation(describeCoverage(), "text/xml").build();
                    
            } else if (request.equalsIgnoreCase("GetCapabilities")) {
                    
                return Response.Builder.representation(getCapabilities(), "text/xml").build();
                    
            } else if (request.equalsIgnoreCase("GetCoverage")) {
                    
                return Response.Builder.representation(getCoverage(), webServiceWorker.getMimeType()).build();
                     
            } else {
                throw new WebServiceException("The operation " + request + " is not supported by the service",
                                              WMSExceptionCode.OPERATION_NOT_SUPPORTED, getCurrentVersion());
            }
        } catch (WebServiceException ex) {
            ex.printStackTrace();
            StringWriter sw = new StringWriter();    
            marshaller.marshal(ex.getServiceExceptionReport(), sw);
            return Response.Builder.representation(sw.toString(), "text/xml").build();
        }
     }
    
    /**
     * Web service operation
     */ 
    public String getCapabilities() throws JAXBException, WebServiceException {
        logger.info("getCapabilities request received");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        //we begin by extract the base attribute
        if (!getParameter("SERVICE", true).equalsIgnoreCase("WCS")) {
            throw new WebServiceException("The parameters SERVICE=WCS must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, getCurrentVersion());
        }
        
        String inputVersion = getParameter("VERSION", false);
        setCurrentVersion(inputVersion);
        webServiceWorker.setService("WCS", getCurrentVersion().toString());
        
        Capabilities        responsev111 = null;
        WCSCapabilitiesType responsev100 = null;
        
        if (inputVersion.equals("1.1.1")) {
            responsev111 = (Capabilities)getCapabilitiesObject(getCurrentVersion());
        
            //we update the url in the static part.
            for (Operation op:responsev111.getOperationsMetadata().getOperation()) {
                op.getDCP().get(0).getHTTP().getGetOrPost().get(0).getValue().setHref(getServiceURL() + "wcs?REQUEST=" + op.getName());
                op.getDCP().get(1).getHTTP().getGetOrPost().get(0).getValue().setHref(getServiceURL() + "wcs?REQUEST=" + op.getName());
            }
        } else {
            responsev100 = (WCSCapabilitiesType)((JAXBElement)getCapabilitiesObject(getCurrentVersion())).getValue();
            
            //we update the url in the static part.
            ((Get) responsev100.getCapability().getRequest().getGetCapabilities().getDCPType().get(0).getHTTP().getGetOrPost().get(0)).getOnlineResource().setHref(getServiceURL() + "wcs?REQUEST=GetCapabilities");
            ((Post)responsev100.getCapability().getRequest().getGetCapabilities().getDCPType().get(1).getHTTP().getGetOrPost().get(0)).getOnlineResource().setHref(getServiceURL() + "wcs?REQUEST=GetCapabilities");
            
            ((Get)responsev100.getCapability().getRequest().getDescribeCoverage().getDCPType().get(0).getHTTP().getGetOrPost().get(0)).getOnlineResource().setHref(getServiceURL() + "wcs?REQUEST=DescribeCoverage");
            ((Post)responsev100.getCapability().getRequest().getDescribeCoverage().getDCPType().get(1).getHTTP().getGetOrPost().get(0)).getOnlineResource().setHref(getServiceURL() + "wcs?REQUEST=DescribeCoverage");
            
            ((Get)responsev100.getCapability().getRequest().getGetCoverage().getDCPType().get(0).getHTTP().getGetOrPost().get(0)).getOnlineResource().setHref(getServiceURL() + "wcs?REQUEST=GetCoverage");
            ((Post)responsev100.getCapability().getRequest().getGetCoverage().getDCPType().get(1).getHTTP().getGetOrPost().get(0)).getOnlineResource().setHref(getServiceURL() + "wcs?REQUEST=GetCoverage");
            
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
                
                GeographicBoundingBox inputGeoBox = inputLayer.getGeographicBoundingBox();
               
                if(inputGeoBox != null) {
                     String crs = "WGS84(DD)";
                    if (inputVersion.equals("1.1.1")){
                        WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(crs, 
                                                     inputGeoBox.getWestBoundLongitude(),
                                                     inputGeoBox.getEastBoundLongitude(),
                                                     inputGeoBox.getSouthBoundLatitude(),
                                                     inputGeoBox.getNorthBoundLatitude());
                
                        cs.addRest(owsFactory.createWGS84BoundingBox(outputBBox));
                    } else {
                        List<Double> pos1 = new ArrayList<Double>();
                        pos1.add(inputGeoBox.getWestBoundLongitude());
                        pos1.add(inputGeoBox.getEastBoundLongitude());
                        
                        List<Double> pos2 = new ArrayList<Double>();
                        pos2.add(inputGeoBox.getNorthBoundLatitude());
                        pos2.add(inputGeoBox.getSouthBoundLatitude());
                        
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
            responsev100.setContentMetadata(contentMetadata);
            marshaller.marshal(responsev100, sw);
        }
        
        return sw.toString();
        
    }
    
    
    /**
     * Web service operation
     */
    public File getCoverage() throws JAXBException, WebServiceException {
        logger.info("getCoverage recu");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        verifyBaseParameter(0);
        webServiceWorker.setService("WCS", getCurrentVersion().toString());
        String format, coverage, crs, bbox, time, interpolation;
        String width = null, height = null; 
        String gridType, gridOrigin, gridOffsets, gridCS;
        
       if (getCurrentVersion().toString().equals("1.1.1")){
            
            coverage = getParameter("identifier", true);
            
            //Domain subset
            bbox = getParameter("BoundingBox", true);
            time = getParameter("timeSequence", false);
            
            /**
             * Range subSet not yet used.
             * contain the sub fields : fieldSubset
             * FieldSubset: - identifier
             *              - interpolationMethodType
             *              - axisSubset
             * 
             * AxisSubset:  - identifier
             *              - key 
             */
            getParameter("rangeSubset", false);
            //output
            format = getParameter("format", true);
            
            /**
             * grid crs
             */
            crs = getParameter("GridBaseCRS", true);
            gridOffsets = getParameter("GridOffsets", true);
            
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
            
        } else {
            
            // parameter for 1.0.0 version
            format        = getParameter("format", true);
            coverage      = getParameter("coverage", true);
            crs           = getParameter("CRS", true);
            bbox          = getParameter("bbox", false);
            time          = getParameter("time", false);
            width         = getParameter("width", true);
            height        = getParameter("height", true);
            interpolation = getParameter("interpolation", false);
        }
        
        webServiceWorker.setFormat(format);
        webServiceWorker.setLayer(coverage);
        webServiceWorker.setCoordinateReferenceSystem(crs);
        webServiceWorker.setBoundingBox(bbox);
        webServiceWorker.setTime(time);
        webServiceWorker.setDimension(width, height);
            
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
        String identifiers = getParameter("IDENTIFIER", true);
        webServiceWorker.setLayer(identifiers);
        
        Layer layer = webServiceWorker.getLayer();
        
        // TODO
        net.seagis.ows.ObjectFactory owsFactory = new net.seagis.ows.ObjectFactory();
        GeographicBoundingBox inputGeoBox = layer.getGeographicBoundingBox();
        JAXBElement<? extends BoundingBoxType> bbox = null;
        if(inputGeoBox != null) {
            String crs = "WGS84(DD)";
            WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(crs, 
                                                 inputGeoBox.getWestBoundLongitude(),
                                                 inputGeoBox.getEastBoundLongitude(),
                                                 inputGeoBox.getSouthBoundLatitude(),
                                                 inputGeoBox.getNorthBoundLatitude());
            bbox = owsFactory.createWGS84BoundingBox(outputBBox);
        }
        SpatialDomainType spatial = new SpatialDomainType(bbox);
        CoverageDomainType domain = new CoverageDomainType(spatial, null);
        RangeType range = null;
        List<String> supportedCRS = new ArrayList<String>();
        supportedCRS.add("4326");
        List<String> supportedFormat = new ArrayList<String>();
        supportedCRS.add("??");
        CoverageDescriptionType coverage = new CoverageDescriptionType(layer.getName(),
                                                                       domain,
                                                                       range,
                                                                       supportedCRS,
                                                                       supportedFormat);
        List<CoverageDescriptionType> coverages = new ArrayList<CoverageDescriptionType>();
        coverages.add(coverage);
        
        CoverageDescriptions response = new CoverageDescriptions(coverages);
               
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

