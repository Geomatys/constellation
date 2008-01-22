/*
 * Sicade - SystÃƒÂ¨mes intÃƒÂ©grÃƒÂ©s de connaissances pour l'aide ÃƒÂ  la dÃƒÂ©cision en environnement
 * (C) 2005, Institut de Recherche pour le DÃƒÂ©veloppement
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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

//geotools dependencies
import org.geotools.util.Version;

// jersey dependencies
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

// JAXB xml binding dependencies
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;


// seagis dependencies
import net.seagis.catalog.Database;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;

/**
 *
 * @author legal
 */
public abstract class WebService {
    
    protected static final Logger logger = Logger.getLogger("net.seagis.wms");
    
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
    private final Version sldVersion = new Version("1.1.0");
    
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
    private final String serviceURL;
    
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
        
            final WebServiceWorker initialValue = new WebServiceWorker(new Database());
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
     * Initialiise the basic attribute of a web service.
     * 
     * @param versions A list of the supported version of this service.
     */
    public WebService(String service, String... versions) {
        this.service = service;
       
        for (final String element : versions) {
            this.versions.add(new Version(element));
        }
        if (this.versions.size() == 0)
             throw new IllegalArgumentException("A web service must have at least one version");
        else
            this.currentVersion = this.versions.get(0);
        
        unmarshaller = null;
        serviceURL   = null;
        
       
    }
    
    /**
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
        
        UriInfo context = getContext();
        
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
                throw new WebServiceException("The parameter " + parameterName + " must be specify",
                                              WMSExceptionCode.MISSING_PARAMETER_VALUE, v);
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
        
        UriInfo context = getContext();
        
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
                throw new WebServiceException("The parameter VERSION=" + sldVersion + "must be specify",
                                              WMSExceptionCode.MISSING_PARAMETER_VALUE, sldVersion);
            } else {
                return;
            }
        }
        // if the version is not accepted we send an exception 
        String inputVersion = getParameter("VERSION", true);
        if (!versions.contains(new Version(inputVersion))) {
            
            String message = "The parameter ";
            for (Version vers:versions){
                message += "VERSION=" + vers + "OR ";
            }
            message = message.substring(0, message.length()-3);
            message += "must be specify";
            throw new WebServiceException(message,
                                         WMSExceptionCode.VERSION_NEGOTIATION_FAILED, getCurrentVersion());
        
        } else {
            setCurrentVersion(inputVersion);
        }
        if (sld == 1) {
            if (!getParameter("SLD_VERSION", true).equals(sldVersion.toString())) {
                throw new WebServiceException("The parameter SLD_VERSION=" + sldVersion + "must be specify",
                                              WMSExceptionCode.VERSION_NEGOTIATION_FAILED, versions.get(0));
            }
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
            MultivaluedMap parameters = getContext().getQueryParameters();
            LinkedList<String> list = (LinkedList) parameters.get(parameterName);
            if (list == null) {
                list = (LinkedList) parameters.get(parameterName.toLowerCase());
                if (list == null) {
                    if (!mandatory) {
                        return null;
                    } else {
                        throw new WebServiceException("The parameter " + parameterName + " must be specify", WMSExceptionCode.MISSING_PARAMETER_VALUE, getCurrentVersion());
                    }
                }
            }
            StringReader sr = new StringReader(list.get(0));
            Object result = unmarshaller.unmarshal(sr);
            return result;
        } catch (JAXBException ex) {
             throw new WebServiceException("the xml object for parameter" + parameterName + " is not well formed:" + '\n' +
                                           ex,
                                           WMSExceptionCode.INVALID_PARAMETER_VALUE, getCurrentVersion());
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
    protected void setCurrentVersion(String version) {
        currentVersion = new Version(version);
    }
    
    /**
     * Return the SLD version.
     */
    protected Version getSldVersion() {
        return this.sldVersion;
    }
    
    /**
     * return The current httpContext.
     */
    protected abstract UriInfo getContext();
    
    /**
     * Treat the incomming GET request and call the right function.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    public abstract Response treatIncommingRequest() throws JAXBException;

   
    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @param  version the version of the service.
     * @return The capabilities Object, or {@code null} if none.
     */
    protected Object getCapabilitiesObject(Version version) throws JAXBException {
       String appName = getContext().getBase().getPath();
       //we delete the /WS
       appName = appName.substring(0, appName.length()-3);
       String path = System.getenv().get("CATALINA_HOME") + "/webapps" + appName + "WEB-INF/";
       
       String fileName = this.service + "Capabilities" + version.toString() + ".xml";
        
       if (fileName == null) {
           return null;
       } else {
           
           Object response = capabilities.get(fileName);
           if (response == null) {
               response = unmarshaller.unmarshal(new File(path + fileName));
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
    protected String getServiceURL() {
        return getContext().getBase().toString();
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
        }
        return s;
    }
    
    
}