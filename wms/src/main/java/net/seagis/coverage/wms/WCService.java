package net.seagis.coverage.wms;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;


// JAXB xml binding dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.geotools.util.Version;

// jersey dependencies
import com.sun.ws.rest.spi.resource.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;

// seagis dependencies
import javax.xml.bind.Marshaller;
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;
import net.seagis.wcs.Capabilities;
import net.seagis.wcs.Contents;
import net.seagis.wcs.CoverageDescriptionType;
import net.seagis.wcs.CoverageDescriptions;
import net.seagis.wcs.CoverageSummaryType;
import net.seagis.wcs.CoveragesType;

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
        super("WCS", "1.1.1");
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
                if (request.equals("DescribeCoverage")) {
                    
                    return Response.Builder.representation(describeCoverage(), "text/xml").build();
                    
                } else if (request.equals("GetCapabilities")) {
                    
                    return Response.Builder.representation(getCapabilities(), "text/xml").build();
                    
                } else if (request.equals("GetCoverage")) {
                    
                    return Response.Builder.representation(getCoverage(), "text/xml").build();
                    
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
        
        //we begin by extract the mandatory attribute
        if (!getParameter("SERVICE", true).equalsIgnoreCase("WCS")) {
            throw new WebServiceException("The parameters SERVICE=WCS must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, getCurrentVersion());
        }
        
        //and the the optional attribute
        String inputVersion = getParameter("VERSION", false);
        setCurrentVersion("1.1.1");
        
        
        Capabilities response = (Capabilities)unmarshaller.unmarshal(getCapabilitiesFile(false, getCurrentVersion()));
        Contents contents = new Contents();
        
        //we get the list of layers
        
        for (Layer inputLayer: webServiceWorker.getLayers()) {
            CoverageSummaryType cs = new CoverageSummaryType();
            
            contents.getCoverageSummary().add(cs);
        }
        response.setContents(contents);
        StringWriter sw = new StringWriter();    
        marshaller.marshal(response, sw);
        return sw.toString();
        
    }
    
    
    /**
     * Web service operation
     */
    public String getCoverage() throws JAXBException, WebServiceException {
        CoveragesType response = new CoveragesType();
        verifyBaseParameter(0);
        String identifiers  = getParameter("Identifier", true);
        String domainSubSet = getParameter("DomainSubset", true);
        String rangeSubset  = getParameter("RangeSubset", false);
        String outputFormat = getParameter("Output", true);
        
        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();    
        marshaller.marshal(response, sw);
        return sw.toString();
    }
    
    
    /**
     * Web service operation
     */
    public String describeCoverage() throws JAXBException, WebServiceException {
        CoverageDescriptions response = new CoverageDescriptions();
        verifyBaseParameter(0);
        String identifiers = getParameter("IDENTIFIER", true);
        
        
        CoverageDescriptionType coverage = new CoverageDescriptionType();
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

