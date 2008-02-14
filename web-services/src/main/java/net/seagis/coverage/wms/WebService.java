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
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

//geotools dependencies
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.HttpContext;

// jersey dependencies
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

// JAXB xml binding dependencies
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;


// seagis dependencies
import javax.xml.bind.UnmarshalException;
import net.seagis.catalog.Database;
import net.seagis.coverage.web.Version;
import net.seagis.ows.OWSWebServiceException;
import net.seagis.coverage.web.WMSWebServiceException;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;
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
    private final List<Version> versions = new ArrayList<Version>();
    
    /**
     * The current version used (since the last request)
     */
    private Version currentVersion;
    
     /**
     * The version of the SLD profile for the WMS web service. fixed a 1.1.0 for now.
     */
    private final Version sldVersion = new Version("1.1.0", false);
    
     /**
     * A JAXB unmarshaller used to create java object from XML file.
     */
    protected Unmarshaller unmarshaller;
    
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
    @HttpContext
    private UriInfo context;
    
    /**
     * The object whitch made all the operation on the postgrid database
     */
    protected static ThreadLocal<WebServiceWorker> webServiceWorker;
    static {
        try {
            /* only for ifremer configuration
            String path = System.getenv().get("CATALINA_HOME") + "/webapps/ifremerWS/WEB-INF/config.xml";
            File configFile = new File(path);
            final WebServiceWorker initialValue = new WebServiceWorker(new Database(configFile));*/
        
            final WebServiceWorker initialValue = new WebServiceWorker(new Database(), true);
            webServiceWorker = new ThreadLocal<WebServiceWorker>() {
                @Override
                protected WebServiceWorker initialValue() {
                    return new WebServiceWorker(initialValue);
                }
            };
            
       }catch (IOException e) {
            logger.severe("IOException a l'initialisation du webServiceWorker:" + e);
       }
        
    }
    
    /**
     * Initialize the basic attribute of a web service.
     * 
     * @param versions A list of the supported version of this service.
     */
    public WebService(String service, Version... versions) {
        this.service = service;
       
        for (final Version element : versions) {
            this.versions.add(element);
        }
        if (this.versions.size() == 0)
             throw new IllegalArgumentException("A web service must have at least one version");
        else
            this.currentVersion = this.versions.get(0);
        
        unmarshaller = null;
        serviceURL   = null;
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
                Version v;
                if (parameterName.equalsIgnoreCase("version")) {
                    v = null;
                } else {
                    v = currentVersion;
                }
                throwException("The parameter " + parameterName + " must be specify",
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
                throwException("The parameter VERSION=" + sldVersion + " must be specify",
                               "MISSING_PARAMETER_VALUE", "version");
            } else {
                return;
            }
        }
        // if the version is not accepted we send an exception 
        String inputVersion = getParameter("VERSION", true);
        if (getVersionFromNumber(inputVersion) == null) {
            
            String message = "The parameter ";
            for (Version vers:versions){
                message += "VERSION=" + vers.getVersionNumber() + " OR ";
            }
            message = message.substring(0, message.length()-3);
            message += " must be specify";
            throwException(message, "VERSION_NEGOTIATION_FAILED", null);
        
        } else {
            setCurrentVersion(inputVersion);
        }
        if (sld == 1) {
            if (!getParameter("SLD_VERSION", true).equals(sldVersion.toString())) {
                throwException("The parameter SLD_VERSION=" + sldVersion + " must be specify",
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
            for (Version vers:versions){
                message += "VERSION=" + vers.getVersionNumber() + " OR ";
            }
            message = message.substring(0, message.length()-3);
            message += " must be specify";
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
                        throwException("The parameter " + parameterName + " must be specify", 
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
    protected Version getCurrentVersion() {
        return this.currentVersion;
    }
    
    /**
     * Return the current version of the Web Service.
     */
    protected void setCurrentVersion(String versionNumber) {
        currentVersion = getVersionFromNumber(versionNumber);
    }
    
    /**
     * Return the SLD version.
     */
    protected Version getSldVersion() {
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
    @ConsumeMime("text/xml")
    public Response doPOSTXml(InputStream is) throws JAXBException  {
        logger.info("request POST xml: ");
        Object request = null;
        try {
            request = unmarshaller.unmarshal(is);
        
        } catch (UnmarshalException e) {
            logger.severe(e.getMessage());
            StringWriter sw = new StringWriter(); 
            if (getCurrentVersion().isOWS()) {
                OWSWebServiceException wse = new OWSWebServiceException("The XML request is not valid",
                                                                        OWSExceptionCode.INVALID_PARAMETER_VALUE, 
                                                                        null,
                                                                        getCurrentVersion().getVersionNumber());
                marshaller.marshal(wse.getExceptionReport(), sw);
            } else {
                WMSWebServiceException wse = new WMSWebServiceException("The XML request is not valid",
                                                                        WMSExceptionCode.INVALID_PARAMETER_VALUE,
                                                                        getCurrentVersion().getVersionNumber());
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
     * @param  version the version of the service.
     * @return The capabilities Object, or {@code null} if none.
     */
    protected Object getCapabilitiesObject(Version version) throws JAXBException {
       String fileName = this.service + "Capabilities" + version.toString() + ".xml";
       
       if (fileName == null) {
           return null;
       } else {
           Object response = capabilities.get(fileName);
           if (response == null) {
           
               File path;
               String appName = context.getBaseUri().getPath();
               //we delete the /WS
               appName = appName.substring(0, appName.length()-3);
               String home = System.getenv().get("CATALINA_HOME") + "/webapps" + appName + "WEB-INF/";
               if (home == null || !(path=new File(home)).isDirectory()) {
                    home = System.getProperty("user.home");
                    if (System.getProperty("os.name", "").startsWith("Windows")) {
                        path = new File(home, WINDOWS_DIRECTORY);
                    } else {
                        path = new File(home, UNIX_DIRECTORY);
                    }
                } 
            
               File f = new File(path, fileName);
               response = unmarshaller.unmarshal(f);
               capabilities.put(fileName, response);
           }
           
           return response;
        }
    }
    
    /**
     * Return the service url obtain by the first request made.
     * 
     * @return the service url.
     */
    public String getServiceURL() {
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
            throw new OWSWebServiceException(message, OWSExceptionCode.valueOf(code), locator, getCurrentVersion().getVersionNumber());
        } else {
            throw new WMSWebServiceException(message, WMSExceptionCode.valueOf(code), getCurrentVersion().getVersionNumber());
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
     * 
     * @param number the version number.
     * @return
     */
    private Version getVersionFromNumber(String number) {
        for (Version v: this.versions) {
            if (v.getVersionNumber().equals(number)){
                return v;
            }
        }
        return null;
    }
}