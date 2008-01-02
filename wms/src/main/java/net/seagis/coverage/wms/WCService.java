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

import java.io.File;
import java.io.StringWriter;


// JAXB xml binding dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

// jersey dependencies
import com.sun.ws.rest.spi.resource.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;

// seagis dependencies
import javax.xml.bind.Marshaller;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;
import net.seagis.ows.WGS84BoundingBoxType;
import net.seagis.wcs.Capabilities;
import net.seagis.wcs.Contents;
import net.seagis.wcs.CoverageDescriptionType;
import net.seagis.wcs.CoverageDescriptions;
import net.seagis.wcs.CoverageSummaryType;
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
     * Treat the incomming GET request and call the right function.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @HttpMethod("GET")
    public Response treatGETrequest() throws JAXBException  {
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
        
        Capabilities response = (Capabilities)getCapabilitiesObject(getCurrentVersion());
        Contents contents;
       
        //we get the list of layers
        List<CoverageSummaryType> summary = new ArrayList<CoverageSummaryType>();
        net.seagis.wcs.ObjectFactory wcsFactory = new net.seagis.wcs.ObjectFactory();
        net.seagis.ows.ObjectFactory owsFactory = new net.seagis.ows.ObjectFactory();
        try {
            for (Layer inputLayer: webServiceWorker.getLayers()) {
                CoverageSummaryType cs = new CoverageSummaryType();
           
                cs.addRest(wcsFactory.createIdentifier(inputLayer.getName()));
            
                GeographicBoundingBox inputGeoBox = inputLayer.getGeographicBoundingBox();
               
                if(inputGeoBox != null) {
                    String crs = "WGS84(DD)";
                    WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(crs, 
                                                 inputGeoBox.getWestBoundLongitude(),
                                                 inputGeoBox.getEastBoundLongitude(),
                                                 inputGeoBox.getSouthBoundLatitude(),
                                                 inputGeoBox.getNorthBoundLatitude());
                
                    cs.addRest(owsFactory.createWGS84BoundingBox(outputBBox));
                }
           
                summary.add(cs);
            }
            contents = new Contents(summary, null, null, null);    
        
        } catch (CatalogException exception) {
            throw new WebServiceException(exception, WMSExceptionCode.NO_APPLICABLE_CODE, getCurrentVersion());
        }
            
        response.setContents(contents);
        StringWriter sw = new StringWriter();    
        marshaller.marshal(response, sw);
        return sw.toString();
        
    }
    
    
    /**
     * Web service operation
     */
    public File getCoverage() throws JAXBException, WebServiceException {
        logger.info("getCoverage re�u");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        verifyBaseParameter(0);
        webServiceWorker.setService("WCS", getCurrentVersion().toString());
        if (getCurrentVersion().toString().equals("1.0.0")) {
            webServiceWorker.setFormat(getParameter("format", true));
            webServiceWorker.setLayer(getParameter("coverage", true));
            webServiceWorker.setCoordinateReferenceSystem(getParameter("CRS", true));
            webServiceWorker.setBoundingBox(getParameter("bbox", false));
            webServiceWorker.setTime(getParameter("time", false));
            webServiceWorker.setDimension(getParameter("width", true), getParameter("height", true));      
        }
        return webServiceWorker.getImageFile();
    }
    
    
    /**
     * Web service operation
     */
    public String describeCoverage() throws JAXBException, WebServiceException {
        CoverageDescriptions response = null;// new CoverageDescriptions();
        verifyBaseParameter(0);
        String identifiers = getParameter("IDENTIFIER", true);
        
        
        CoverageDescriptionType coverage = null;//new CoverageDescriptionType();
        //coverage.
        response.getCoverageDescription().add(coverage);
               
        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();    
        marshaller.marshal(response, sw);
        return sw.toString();
    }

    /**
     * Return the current Http context. 
     */
    @Override
    protected UriInfo getContext() {
        return this.context;
    }
}

