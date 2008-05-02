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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

//geotools dependencies

// jersey dependencies
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

// JAXB xml binding dependencies
import javax.ws.rs.core.Context;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;


// seagis dependencies
import net.seagis.coverage.web.Service;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.ows.v110.OWSWebServiceException;
import net.seagis.coverage.web.WMSWebServiceException;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.ows.AbstractDCP;
import net.seagis.ows.AbstractOnlineResourceType;
import net.seagis.ows.AbstractOperation;
import net.seagis.wcs.AbstractRequest;
import net.seagis.ows.OWSExceptionCode;
/**
 *
 * @author legal
 */
public abstract class WebService {
    
    protected static final Logger logger = Logger.getLogger("net.seagis.wms");
    
    /**
     * The user directory where to store the configuration file on Unix platforms.
     */
    private static final String UNIX_DIRECTORY = ".sicade";

    /**
     * The user directory where to store the configuration file on Windows platforms.
     */
    private static final String WINDOWS_DIRECTORY = "Application Data\\Sicade";

    /**
     * The supported versions supportd by this web service.
     */
    private final List<ServiceVersion> versions = new ArrayList<ServiceVersion>();
    
    /**
     * The current version used (since the last request)
     */
    private ServiceVersion currentVersion;
    
     /**
     * The version of the SLD profile for the WMS web service. fixed a 1.1.0 for now.
     */
    private final ServiceVersion sldVersion = new ServiceVersion(Service.WMS, "1.1.0");
    
     /**
     * A JAXB unmarshaller used to create java object from XML file.
     */
    private Unmarshaller unmarshaller;
    
    /**
     * A JAXB marshaller used to transform the java object in XML String.
     */
    protected Marshaller marshaller;
    
    /**
     * The name of the service (WMS, WCS,...)
     */
    private final String service;
    
    /**
     * A map containing the Capabilities Object already load from file.
     */
    private Map<String,Object> capabilities = new HashMap<String,Object>();
    
     /**
     * the service URL (used in getCapabilities document).
     */
    private String serviceURL;
    
     /**
     * The http context containing the request parameter
     */
    @Context
    private UriInfo context;
    
    /**
     * The last update sequence
     */
    private long lastUpdateSequence;
    
    
    /**
     * Initialize the basic attribute of a web service.
     * 
     * @param versions A list of the supported version of this service.
     */
    public WebService(String service, ServiceVersion... versions) {
        this.service = service;
       
        for (final ServiceVersion element : versions) {
            this.versions.add(element);
        }
        if (this.versions.size() == 0)
             throw new IllegalArgumentException("A web service must have at least one version");
        else
            this.currentVersion = this.versions.get(0);
        unmarshaller = null;
        serviceURL   = null;
        ImageIO.scanForPlugins();
    }
    
    /**
     * Initialize the JAXB context and build the unmarshaller/marshaller
     * 
     * @param packagesName A list of package containing JAXB annoted classes.
     * @param rootNamespace The main namespace for all the document.
     */
    protected void setXMLContext(String packagesName, String rootNamespace) throws JAXBException {
        logger.finer("SETTING XML CONTEXT: class " + this.getClass().getSimpleName() + '\n' +
                    " packages: " + packagesName);
        
        JAXBContext jbcontext = JAXBContext.newInstance(packagesName);
        unmarshaller = jbcontext.createUnmarshaller();
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        setPrefixMapper(rootNamespace);
    }
    
    /**
     * Initialize the JAXB context and build the unmarshaller/marshaller
     * 
     * @param classesName A list of JAXB annoted classes.
     * @param rootNamespace The main namespace for all the document.
     */
    protected void setXMLContext(String rootNamespace, Class... classes) throws JAXBException {
        logger.finer("SETTING XML CONTEXT: classes version");
        
        JAXBContext jbcontext = JAXBContext.newInstance(classes);
        unmarshaller = jbcontext.createUnmarshaller();
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        setPrefixMapper(rootNamespace);
    }
    
    /**
     * 
     * Extract The parameter named parameterName from the query.
     * If the parameter is mandatory and if it is null it throw an exception.
     * else it return null.
     * 
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional. 
      * 
     * @return the parameter or null if not specified
     * @throw WebServiceException
     */
    protected String getParameter(String parameterName, boolean mandatory) throws WebServiceException {
        
        MultivaluedMap parameters = context.getQueryParameters();
        
        Set<String> keySet = parameters.keySet();
        boolean notFound = true;
        Iterator<String> it = keySet.iterator();
        String s = null;
        while (notFound && it.hasNext()) {
            s = it.next();
            if (parameterName.equalsIgnoreCase(s)) {
                notFound = false;
            }
        }
        if (notFound) {
            if (mandatory) {
                ServiceVersion v;
                if (parameterName.equalsIgnoreCase("version")) {
                    v = null;
                } else {
                    v = currentVersion;
                }
                throwException("The parameter " + parameterName + " must be specified",
                               "MISSING_PARAMETER_VALUE", parameterName);
                //never reach;
                return null;
            } else {
                return null;
            }
        } else {
             LinkedList<String> list = (LinkedList) parameters.get(s);
             return list.get(0);
        }
    }
    
    /**
     * Extract all The parameters from the query and write it in the console.
     * It is a debug method.
     * 
     */
    protected void writeParameters() throws WebServiceException {
        MultivaluedMap parameters = context.getQueryParameters();
        logger.info(parameters.toString());
    }
    
    /**
     * Verify the base parameter or each request.
     * 
     * @param sld case 0: no sld.
     *            case 1: VERSION parameter for WMS version and SLD_VERSION for sld version.
     *            case 2: VERSION parameter for sld version.
     * 
     * @throws net.seagis.coverage.web.WebServiceException
     */
    protected void verifyBaseParameter(int sld) throws WebServiceException {  
        if (sld == 2) {
            if (!getParameter("VERSION", true).equals(sldVersion.toString())) {
                throwException("The parameter VERSION=" + sldVersion + " must be specified",
                               "MISSING_PARAMETER_VALUE", "version");
            } else {
                return;
            }
        }
        // if the version is not accepted we send an exception 
        String inputVersion = getParameter("VERSION", true);
        if (getVersionFromNumber(inputVersion) == null) {
            
            String message = "The parameter ";
            for (ServiceVersion vers : versions) {
                message += "VERSION=" + vers.toString() + " OR ";
            }
            message = message.substring(0, message.length()-3);
            message += " must be specified";
            throwException(message, "VERSION_NEGOTIATION_FAILED", null);
        
        } else {
            setCurrentVersion(inputVersion);
        }
        if (sld == 1) {
            if (!getParameter("SLD_VERSION", true).equals(sldVersion.toString())) {
                throwException("The parameter SLD_VERSION=" + sldVersion + " must be specified",
                               "VERSION_NEGOTIATION_FAILED", null);
            }
        }
    } 
   
    /**
     * Verify if the version is supported by the service.
     * if the version is not accepted we send an exception
     */
    protected void isSupportedVersion(String versionNumber) throws WebServiceException {
        
        if (getVersionFromNumber(versionNumber) == null) {
            
            String message = "The parameter ";
            for (ServiceVersion vers : versions) {
                message += "VERSION=" + vers.toString() + " OR ";
            }
            message = message.substring(0, message.length()-3);
            message += " must be specified";
            throwException(message, "VERSION_NEGOTIATION_FAILED", null);
        
        } 
    }
    
    /**
     * Extract The complex parameter encoded in XML from the query.
     * If the parameter is mandatory and if it is null it throw an exception.
     * else it return null.
     * 
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional. 
     * 
     * @return the parameter or null if not specified
     * @throw WebServiceException
     */
    protected Object getComplexParameter(String parameterName, boolean mandatory) throws WebServiceException {
        
        try {
            MultivaluedMap parameters = context.getQueryParameters();
            LinkedList<String> list = (LinkedList) parameters.get(parameterName);
            if (list == null) {
                list = (LinkedList) parameters.get(parameterName.toLowerCase());
                if (list == null) {
                    if (!mandatory) {
                        return null;
                    } else {
                        throwException("The parameter " + parameterName + " must be specified", 
                                       "MISSING_PARAMETER_VALUE", parameterName);
                        //never reach
                        return null;
                    }
                }
            }
            StringReader sr = new StringReader(list.get(0));
            Object result = unmarshaller.unmarshal(sr);
            return result;
        } catch (JAXBException ex) {
             throwException("the xml object for parameter" + parameterName + " is not well formed:" + '\n' +
                            ex, "INVALID_PARAMETER_VALUE", null);
             //never reach
             return null;
        }
    }
   
    
    /**
     * Return the current version of the Web Service.
     */
    protected ServiceVersion getCurrentVersion() {
        return this.currentVersion;
    }
    
    /**
     * Return the current version of the Web Service.
     */
    protected void setCurrentVersion(String versionNumber) {
        logger.finer("set current version to: " + versionNumber);
        currentVersion = getVersionFromNumber(versionNumber);
    }
    
    /**
     * Return the SLD version.
     */
    protected ServiceVersion getSldVersion() {
        return this.sldVersion;
    }
    
    /**
     * Treat the incomming GET request.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @GET
    public Response doGET() throws JAXBException  {
        return treatIncommingRequest(null);
    }
    
    /**
     * Treat the incomming POST request encoded in kvp.
     * for each parameters in the request it fill the httpContext.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @POST
    @ConsumeMime("application/x-www-form-urlencoded")
    public Response doPOSTKvp(String request) throws JAXBException  {
        final StringTokenizer tokens = new StringTokenizer(request, "&");
        String log = "";
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            String paramName  = token.substring(0, token.indexOf('='));
            String paramValue = token.substring(token.indexOf('=')+ 1);
            log += "put: " + paramName + "=" + paramValue + '\n';
            context.getQueryParameters().add(paramName, paramValue);
        }
        logger.info("request POST kvp: " + request + '\n' + log);
        
        return treatIncommingRequest(null);
    }
    
    /**
     * Treat the incomming POST request encoded in xml.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @POST
    @ConsumeMime("*/xml")
    public Response doPOSTXml(InputStream is) throws JAXBException  {
        logger.info("request POST xml: ");
        Object request = null;
        try {
            request = unmarshaller.unmarshal(is);
        
        } catch (UnmarshalException e) {
            logger.severe("UNMARSHALL EXCEPTION: " + e.getMessage());
            StringWriter sw = new StringWriter(); 
            if (getCurrentVersion().isOWS()) {
                OWSWebServiceException wse = new OWSWebServiceException("The XML request is not valid",
                                                                        OWSExceptionCode.INVALID_REQUEST, 
                                                                        null,
                                                                        getCurrentVersion());
                marshaller.marshal(wse.getExceptionReport(), sw);
            } else {
                WMSWebServiceException wse = new WMSWebServiceException("The XML request is not valid",
                                                                        WMSExceptionCode.INVALID_PARAMETER_VALUE,
                                                                        getCurrentVersion());
                marshaller.marshal(wse.getServiceExceptionReport(), sw);
            }
            
            return Response.ok(sw.toString(), "text/xml").build();
        }
        
        if (request != null && request instanceof AbstractRequest) {
            AbstractRequest ar = (AbstractRequest) request;
            context.getQueryParameters().add("VERSION", ar.getVersion());
        }
        return treatIncommingRequest(request);
    }
    
    @POST
    @ConsumeMime("text/plain")
    public Response doPOSTPlain(InputStream is) throws JAXBException  {
        logger.severe("request POST plain sending Exception");
        StringWriter sw = new StringWriter(); 
        if (getCurrentVersion().isOWS()) {
            OWSWebServiceException wse = new OWSWebServiceException("This content type is not allowed try text/xml or application/x-www-form-urlencoded",
                                                                    OWSExceptionCode.INVALID_REQUEST, 
                                                                    null,
                                                                    getCurrentVersion());
            marshaller.marshal(wse.getExceptionReport(), sw);
        } else {
            WMSWebServiceException wse = new WMSWebServiceException("This content type is not allowed try text/xml or application/x-www-form-urlencoded",
                                                                    WMSExceptionCode.INVALID_PARAMETER_VALUE,
                                                                    getCurrentVersion());
            marshaller.marshal(wse.getServiceExceptionReport(), sw);
        }
            
        return Response.ok(sw.toString(), "text/xml").build();
    }
    
    /**
     * Treat the incomming request and call the right function.
     * 
     * @param objectRequest if the server receive a POST request in XML,
     *        this object contain the request. Else for a GET or a POST kvp
     *        request this param is {@code null}
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    public abstract Response treatIncommingRequest(Object objectRequest) throws JAXBException;

   
    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @return The capabilities Object, or {@code null} if none.
     */
    public Object getCapabilitiesObject() throws JAXBException, FileNotFoundException, IOException {
       String fileName = this.service + "Capabilities" + getCurrentVersion().toString() + ".xml";
       File changeFile = getFile("change.properties");
       Properties p = new Properties();
       FileInputStream in    = new FileInputStream(changeFile);
       p.load(in);
       in.close();
       
       if (fileName == null) {
           return null;
       } else {
           Object response = capabilities.get(fileName);
           
           if (response == null || p.getProperty("update").equals("true")) {
               logger.info("updating metadata");
               File f = getFile(fileName);
               response = unmarshaller.unmarshal(f);
               capabilities.put(fileName, response);
               this.setLastUpdateSequence(System.currentTimeMillis());
               FileOutputStream out = new FileOutputStream(changeFile);
               p.put("update", "false");
               p.store(out, "updated from WebService");
               out.close();
           }
           
           return response;
        }
    }
    
    public File getFile(String fileName) {
         File path;
         String appName = context.getBaseUri().getPath();
         //we delete the /WS
         appName = appName.substring(0, appName.length()-3);
         String home = System.getenv().get("CATALINA_HOME") + "/webapps" + appName + "WEB-INF/";
         if (home == null || !(path = new File(home)).isDirectory()) {
            home = System.getProperty("user.home");
            if (System.getProperty("os.name", "").startsWith("Windows")) {
                path = new File(home, WINDOWS_DIRECTORY);
            } else {
                path = new File(home, UNIX_DIRECTORY);
            }
         } 
         return new File(path, fileName);
    }
    /**
     * Return the service url obtain by the first request made.
     * 
     * @return the service url.
     */
    protected String getServiceURL() {
        if (serviceURL == null) {
            serviceURL = context.getBaseUri().toString();
        }
        return serviceURL; 
    }
    
    /**
     * A utility method whitch replace the special character.
     * 
     * @param s the string to clean.
     * @return a String without special character.
     */
    protected String cleanSpecialCharacter(String s) {
        if (s != null) {
            s = s.replace('é', 'e');
            s = s.replace('è', 'e');
            s = s.replace('à', 'a');
            s = s.replace('É', 'E');
        }
        return s;
    }
    
    /**
     * 
     * @param rootNamespace
     */
    protected void setPrefixMapper(String rootNamespace) throws PropertyException {
        NamespacePrefixMapperImpl prefixMapper = new NamespacePrefixMapperImpl(rootNamespace);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);
    }
    
    /**
     *  Throw a WebserviceException. 
     *  If the service and version applies to OWS specification it throw an OWSException.
     */
    protected void throwException(final String message, String code, String locator) throws WebServiceException {
        if (getCurrentVersion().isOWS()) {
            code = transformCodeName(code);
            throw new OWSWebServiceException(message, OWSExceptionCode.valueOf(code), locator, getCurrentVersion());
        } else {
            throw new WMSWebServiceException(message, WMSExceptionCode.valueOf(code), getCurrentVersion());
        }
        
    }
    
    /**
     * Transform a exception code into the ows specification.
     * example : MISSING_PARAMETER_VALUE become MissingParameterValue.
     * 
     * @param code
     * @return
     */
    private String transformCodeName(String code) {
        String result = "";
        while (code.indexOf('_') != -1) {
            String prefix = code.charAt(0) + "";
            String tmp = code.substring(0, code.indexOf('_')).toLowerCase();
            result += tmp.replace(tmp.charAt(0), prefix.charAt(0));
            code = code.substring(code.indexOf('_') + 1, code.length());
            System.out.println(code);
        }
        String prefix = code.charAt(0) + "";
        code = code.toLowerCase();
        result += code.replace(code.charAt(0), prefix.charAt(0));
        
        return result;
    }
    
    /**
     * Return a Version Object from the version number.
     * if the version number is not correct return the default version.
     * 
     * @param number the version number.
     * @return
     */
    protected ServiceVersion getVersionFromNumber(String number) {
        for (ServiceVersion v : versions) {
            if (v.toString().equals(number)){
                return v;
            }
        }
        return null;
    }
    
    /**
     * 
     */
    protected ServiceVersion getBestVersion(String number) {
        for (ServiceVersion v : versions) {
            if (v.toString().equals(number)){
                return v;
            }
        }
        ServiceVersion wrongVersion = new ServiceVersion(null, number);
        if (wrongVersion.compareTo(versions.get(0)) > 0) {
            return this.versions.get(0);
        } else {
            if (wrongVersion.compareTo(versions.get(versions.size() - 1)) < 0) {
                return versions.get(versions.size() - 1);
            }
        }
        return versions.get(0);
    }
     
    /**
     * Update all the url in a OWS capabilities document.
     * 
     * @param operations A list of OWS operation.
     */
    public static void updateOWSURL(List<? extends AbstractOperation> operations, String url, String service) {
        for (AbstractOperation op:operations) {
            for (AbstractDCP dcp: op.getDCP()) {
                for (AbstractOnlineResourceType method:dcp.getHTTP().getGetOrPost())
                    method.setHref(url + service.toLowerCase() + "?");
            }
       }
    }

    public long getLastUpdateSequence() {
        return lastUpdateSequence;
    }

    public void setLastUpdateSequence(long lastUpdateSequence) {
        this.lastUpdateSequence = lastUpdateSequence;
    }
}