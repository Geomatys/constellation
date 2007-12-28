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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

//geotools dependencies
import java.util.Map;
import java.util.logging.Logger;
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
     * The object whitch made all the operation on the postgrid database
     */
    protected static ThreadLocal<WebServiceWorker> webServiceWorker;
    static {
        try {
            // only for ifremer configuration
            String path = System.getenv().get("CATALINA_HOME") + "/webapps/ifremerWS/WEB-INF/config.xml";
            File configFile = new File(path);
            final WebServiceWorker initialValue = new WebServiceWorker(new Database(configFile));
        
            //final WebServiceWorker initialValue = new WebServiceWorker(new Database());
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
        
       
    }
    
     /**
     * Extract The parameter named parameterName from the query.
     * If the parameter is mandatory and if it is null it throw an exception.
     * else it return null.
     * 
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional. 
     * @param context The url context of the query
      * 
     * @return the parameter or null if not specified
     * @throw WebServiceException
     */
    protected String getParameter(String parameterName, boolean mandatory) throws WebServiceException {
        
        UriInfo context = getContext();
        
        MultivaluedMap parameters = context.getQueryParameters();
        //we try with the parameter in Upper case.
        LinkedList<String> list = (LinkedList) parameters.get(parameterName);
        if (list == null) {
            //else with the parameter in lower case.
            list = (LinkedList) parameters.get(parameterName.toLowerCase());
            if (list == null) {
                //and finally with the first character in uppercase
                String s = parameterName.toLowerCase();
                s = s.substring(1);
                s = parameterName.charAt(0) + s;
                list = (LinkedList) parameters.get(s);
                if (list == null) {
                    if (!mandatory) {
                        return null;
                    } else {
                        throw new WebServiceException("The parameter " + parameterName + " must be specify",
                                                      WMSExceptionCode.MISSING_PARAMETER_VALUE, currentVersion);
                    }
                }
            } 
        } 
        
        return list.get(0);
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
    public abstract Response treatGETrequest() throws JAXBException;

   
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
    protected Object getCapabilitiesObject(Version version) throws JAXBException {
       String path = System.getenv().get("CATALINA_HOME") + "/webapps" + getContext().getBase().getPath() + "WEB-INF/";
       
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
}
