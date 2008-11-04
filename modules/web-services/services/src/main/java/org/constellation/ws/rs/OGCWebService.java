/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.ws.rs;

// J2SE dependencies
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import org.constellation.coverage.web.ExceptionCode;
import org.constellation.coverage.web.Service;
import org.constellation.coverage.web.ServiceExceptionReport;
import org.constellation.coverage.web.ServiceExceptionType;
import org.constellation.coverage.web.ServiceVersion;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.ows.AbstractDCP;
import org.constellation.ows.AbstractOnlineResourceType;
import org.constellation.ows.AbstractOperation;
import org.constellation.ows.OWSExceptionCode;
import org.constellation.ows.v110.OWSWebServiceException;

// Geotools dependencies
import org.geotools.util.Version;

import static org.constellation.coverage.web.ExceptionCode.*;


/**
 * Main class for all web services.
 *
 * @version $Id$
 *
 * @author Guilhem Legal
 * @author Cédric Briançon
 */
public abstract class OGCWebService extends WebService {
    
    /**
     * The supported versions supported by this web service.
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
     * The last update sequence
     */
    private long lastUpdateSequence;

    /**
     * Initialize the basic attribute of a web service.
     *
     * @param service The initials of the web service (CSW, WMS, WCS, SOS, ...)
     * @param versions A list of the supported version of this service.
     */
    public OGCWebService(String service, ServiceVersion... versions) {
        super(service);
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
    }
   
    /**
     * Verify the base parameter or each request.
     *
     * @param sld case 0: no sld.
     *            case 1: VERSION parameter for WMS version and SLD_VERSION for sld version.
     *            case 2: VERSION parameter for sld version.
     *
     * @throws org.constellation.coverage.web.WebServiceException
     */
    protected void verifyBaseParameter(int sld) throws WebServiceException {
        if (sld == 2) {
            if (!getParameter("VERSION", true).equals(sldVersion.toString())) {
                throw new WebServiceException("The parameter VERSION=" + sldVersion + " must be specified",
                               MISSING_PARAMETER_VALUE, null);
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
            throw new WebServiceException(message, VERSION_NEGOTIATION_FAILED, null);
        } else {
            setCurrentVersion(inputVersion);
        }
        if (sld == 1) {
            if (!getParameter("SLD_VERSION", true).equals(sldVersion.toString())) {
                throw new WebServiceException("The parameter SLD_VERSION=" + sldVersion + " must be specified",
                               VERSION_NEGOTIATION_FAILED, null);
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
            throw new WebServiceException(message, VERSION_NEGOTIATION_FAILED, null);
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
        currentVersion = getVersionFromNumber(versionNumber);
    }

    /**
     * Return the SLD version.
     */
    protected ServiceVersion getSldVersion() {
        return this.sldVersion;
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
    public abstract Response treatIncomingRequest(Object objectRequest) throws JAXBException;

    /**
     * build an service Exception and marshall it into a StringWriter
     *
     * @param message
     * @param codeName
     * @return
     */
    protected Object launchException(final String message, final String codeName, final String locator) {
        if (getCurrentVersion().isOWS()) {
            final OWSExceptionCode code = OWSExceptionCode.valueOf(codeName);
            final OWSWebServiceException wse = new OWSWebServiceException(message,
                    code, locator, getCurrentVersion());
            return wse.getExceptionReport();
        } else {
            final ExceptionCode code = ExceptionCode.valueOf(codeName);
            return new ServiceExceptionReport(getCurrentVersion(), new ServiceExceptionType(message, code));
        }
    }

    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @return The capabilities Object, or {@code null} if none.
     *
     * @deprecated
     */
    public Object getCapabilitiesObject() throws JAXBException, FileNotFoundException, IOException {
        return getCapabilitiesObject(getCurrentVersion());
    }

    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @return The capabilities Object, or {@code null} if none.
     */
    public Object getCapabilitiesObject(final Version version) throws JAXBException, FileNotFoundException, IOException {
       String fileName = this.service + "Capabilities" + version.toString() + ".xml";
       File changeFile = getFile("change.properties");
       Properties p = new Properties();

       // if the flag file is present we load the properties
       if (changeFile != null && changeFile.exists()) {
           FileInputStream in    = new FileInputStream(changeFile);
           p.load(in);
           in.close();
       } else {
           p.put("update", "false");
       }

       //we recup the capabilities file and unmarshall it
       if (fileName == null) {
           return null;

       } else {

           //we look if we have already put it in cache
           Object response = capabilities.get(fileName);
           boolean update  = p.getProperty("update").equals("true");

           if (response == null || update) {
               if (update)
                    LOGGER.info("updating metadata");

               File f = getFile(fileName);
               response = unmarshaller.unmarshal(f);
               capabilities.put(fileName, response);
               this.setLastUpdateSequence(System.currentTimeMillis());
               p.put("update", "false");

               // if the flag file is present we store the properties
               if (changeFile != null && changeFile.exists()) {
                   FileOutputStream out = new FileOutputStream(changeFile);
                   p.store(out, "updated from WebService");
                   out.close();
               }
           }

           return response;
        }
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
     * Transform an exception code into the OWS specification.
     * Example : MISSING_PARAMETER_VALUE become MissingParameterValue.
     *
     * @param code
     * @return
     */
    protected String transformCodeName(String code) {
        String result = "";
        final String prefix = code.charAt(0) + "";
        while (code.indexOf('_') != -1) {
            final String tmp = code.substring(0, code.indexOf('_')).toLowerCase();
            result += tmp.replace(tmp.charAt(0), prefix.charAt(0));
            code = code.substring(code.indexOf('_') + 1, code.length());
            //System.out.println(code);
        }
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
     * If the requested version number is not available we choose the best version to return.
     *
     * @param A service version number.
     */
    protected ServiceVersion getBestVersion(String number) {
        for (ServiceVersion v : versions) {
            if (v.toString().equals(number)){
                return v;
            }
        }
        ServiceVersion wrongVersion = new ServiceVersion(null, number);
        if (wrongVersion.compareTo(versions.get(0)) < 0) {
            return this.versions.get(0);
        } else {
            if (wrongVersion.compareTo(versions.get(versions.size() - 1)) > 0) {
                return versions.get(versions.size() - 1);
            }
        }
        return versions.get(0);
    }

    /**
     * Update all the url in a OWS capabilities document.
     *
     * @param operations A list of OWS operation.
     * @param url The url of the web application.
     * @param service the initials of the web service (WMS, SOS, WCS, CSW, ...). This string correspound to the resource name in lower case.
     */
    public static void updateOWSURL(List<? extends AbstractOperation> operations, String url, String service) {
        for (AbstractOperation op:operations) {
            for (AbstractDCP dcp: op.getDCP()) {
                for (AbstractOnlineResourceType method:dcp.getHTTP().getGetOrPost())
                    method.setHref(url + service.toLowerCase() + "?");
            }
       }
    }

    /**
     * return the last time that the capabilities have been updated (not yet really used)
     */
    public long getLastUpdateSequence() {
        return lastUpdateSequence;
    }

    /**
     * set the last time that the capabilities have been updated (not yet really used)
     *
     * @param lastUpdateSequence A Date.
     */
    public void setLastUpdateSequence(long lastUpdateSequence) {
        this.lastUpdateSequence = lastUpdateSequence;
    }
}
