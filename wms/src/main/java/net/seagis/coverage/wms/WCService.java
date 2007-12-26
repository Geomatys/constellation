package net.seagis.coverage.wms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.StringWriter;


// JAXB xml binding dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// geotools dependencies
import org.geotools.util.Version;

// jersey dependencies
import com.sun.ws.rest.spi.resource.Singleton;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;

// seagis dependencies
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.web.WebServiceException;
/*import net.seagis.wcs.Capabilities;
import net.seagis.wcs.CoverageDescriptions;
import net.seagis.wcs.CoveragesType;
import net.seagis.wcs.DescribeCoverage;
import net.seagis.wcs.GetCapabilities;
import net.seagis.wcs.GetCoverage;
import net.seagis.wcs.RequestBaseType;*/
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
@UriTemplate("wms")
@Singleton
public class WCService extends WebService {

    /**
     * The http context containing the request parameter
     */
    @HttpContext
    private UriInfo context;
    
    private final Logger logger = Logger.getLogger("net.seagis.wms");
     
    /**
     * The file where to store configuration parameters.
     */
    private static final String CAPABILITIES_FILENAME = "WCSCapabilities.xml";
    
    /** 
     * Build a new instance of the webService and initialise the JAXB marshaller. 
     */
    public WCService() throws JAXBException, IOException {
        super("1.1.0");
       // JAXBContext jbcontext = JAXBContext.newInstance("net.seagis.wcs");
       // unmarshaller = jbcontext.createUnmarshaller();
        
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
    public Capabilities getCapabilities() throws JAXBException, WebServiceException {
        logger.info("getCapabilities request received");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        Capabilities response = (Capabilities)unmarshaller.unmarshal(getCapabilitiesFile(false, getCurrentVersion()));
        Contents contents = new Contents();
        
        //we get the list of layers
        
        for (Layer inputLayer: webServiceWorker.getLayers()) {
            CoverageSummaryType cs = new CoverageSummaryType();
            
            contents.getCoverageSummary().add(cs);
        }
        response.setContents(contents);
        return response;
        
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
     * @todo this method is duplicate from the database class. it must be fix.
     *   
     * Returns the file where to read or write user configuration. If no such file is found,
     * then this method returns {@code null}. This method is allowed to create the destination
     * directory if and only if {@code create} is {@code true}.
     * <p>
     * Subclasses may override this method in order to search for an other file than the default one.
     *
     * @param  create {@code true} if this method is allowed to create the destination directory.
     * @return The configuration file, or {@code null} if none.
     */
    File getCapabilitiesFile(final boolean create, Version version) {
       String path = System.getenv().get("CATALINA_HOME") + "/webapps" + context.getBase().getPath() + "WEB-INF/";
       
        String fileName = null;
        
            
        if (version.toString().equals("1.1.1")){
            fileName = CAPABILITIES_FILENAME;
        }
        
        if (fileName == null) {
            return null;
        } else {
            return new File(path + fileName);
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

