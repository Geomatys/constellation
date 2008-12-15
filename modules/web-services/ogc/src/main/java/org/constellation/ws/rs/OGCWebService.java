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
import javax.xml.bind.JAXBException;

// Constellation dependencies
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.ServiceType;
import org.constellation.ws.ServiceExceptionReport;
import org.constellation.ws.ServiceExceptionType;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.WebServiceException;
import org.constellation.ows.AbstractDCP;
import org.constellation.ows.AbstractOnlineResourceType;
import org.constellation.ows.AbstractOperation;
import org.constellation.ows.OWSExceptionCode;

// Geotools dependencies
import org.constellation.ows.v110.ExceptionReport;
import org.geotools.util.Version;

import static org.constellation.ws.ExceptionCode.*;


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
     * The supported supportedVersions supported by this web serviceType.
     */
    private final List<ServiceVersion> supportedVersions = new ArrayList<ServiceVersion>();

    /**
     * The version of the WMS specification for this request.
     */
    private ServiceVersion actingVersion;

    /**
     * The version of the SLD profile for the WMS web serviceType. fixed a 1.1.0 for now.
     */
    private final ServiceVersion sldVersion = new ServiceVersion(ServiceType.WMS, "1.1.0");

    /**
     * The name of the serviceType (WMS, WCS,...)
     */
    private final String serviceType;//TODO: use the ServiceType[type] enum.

    /**
     * A map containing the Capabilities Object already load from file.
     */
    private Map<String,Object> capabilities = new HashMap<String,Object>();

    /**
     * The time of the last update of the cached capabilities map, represented
     * as the number of milliseconds since the Unix Epoch (i.e. useful as a
     * paramter to the Date constructor).
     */
    private long lastUpdateTime;

    /**
     * Initialize the basic attribute of a web serviceType.
     *
     * @param serviceType The initials of the web serviceType (CSW, WMS, WCS, SOS, ...)
     * @param supportedVersions A list of the supported version of this serviceType.
     */
    public OGCWebService(String service, ServiceVersion... supportedVersions) {
        super(service);
        this.serviceType = service;

        for (final ServiceVersion element : supportedVersions) {
            this.supportedVersions.add(element);
        }
        if (this.supportedVersions.size() == 0)
             throw new IllegalArgumentException("A web service must have at least one version");
        else
            this.actingVersion = this.supportedVersions.get(0);
        unmarshaller = null;
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
                               MISSING_PARAMETER_VALUE);
            } else {
                return;
            }
        }
        // if the version is not accepted we send an exception
        String inputVersion = getParameter("VERSION", true);
        if (getVersionFromNumber(inputVersion) == null) {

            String message = "The parameter ";
            for (ServiceVersion vers : supportedVersions) {
                message += "VERSION=" + vers.toString() + " OR ";
            }
            message = message.substring(0, message.length()-3);
            message += " must be specified";
            throw new WebServiceException(message, VERSION_NEGOTIATION_FAILED);
        } else {
            setActingVersion(inputVersion);
        }
        if (sld == 1) {
            if (!getParameter("SLD_VERSION", true).equals(sldVersion.toString())) {
                throw new WebServiceException("The parameter SLD_VERSION=" + sldVersion + " must be specified",
                               VERSION_NEGOTIATION_FAILED);
            }
        }
    }

    /**
     * Verify if the version is supported by this serviceType.
     * <p>
     * If the version is not accepted we send an exception.
     * </p>
     */
    protected void isVersionSupported(String versionNumber) throws WebServiceException {
        if (getVersionFromNumber(versionNumber) == null) {
            String message = "The parameter ";
            for (ServiceVersion vers : supportedVersions) {
                message += "VERSION=" + vers.toString() + " OR ";
            }
            message = message.substring(0, message.length()-3);
            message += " must be specified";
            throw new WebServiceException(message, VERSION_NEGOTIATION_FAILED);
        }
    }

    /**
     * Return the current version of the Web ServiceType.
     */
    protected ServiceVersion getActingVersion() {
        return this.actingVersion;
    }

    /**
     * Return the current version of the Web ServiceType.
     */
    protected void setActingVersion(String versionNumber) {
        actingVersion = getVersionFromNumber(versionNumber);
    }

    /**
     * Return the SLD version.
     */
    protected ServiceVersion getSldVersion() {
        return this.sldVersion;
    }

    /**
     * build an serviceType Exception and marshall it into a StringWriter
     *
     * @param message
     * @param codeName
     * @return
     */
    @Override
    protected Object launchException(final String message, final String codeName, final String locator) {
        if (getActingVersion().isOWS()) {
            final OWSExceptionCode code = OWSExceptionCode.valueOf(codeName);
            final ExceptionReport report = new ExceptionReport(message, code.name(), locator, getActingVersion());
            return report;
        } else {
            final ExceptionCode code = ExceptionCode.valueOf(codeName);
            return new ServiceExceptionReport(getActingVersion(), new ServiceExceptionType(message, code));
        }
    }

    /**
     * Returns the file where to read the capabilities document for each serviceType.
     * If no such file is found, then this method returns {@code null}.
     *
     * @return The capabilities Object, or {@code null} if none.
     */
    public Object getStaticCapabilitiesObject() throws JAXBException, FileNotFoundException, IOException {
        return getStaticCapabilitiesObject(getActingVersion());
    }

    /**
     * Returns the file where to read the capabilities document for each serviceType.
     * If no such file is found, then this method returns {@code null}.
     *
     * @return The capabilities Object, or {@code null} if none.
     */
    public Object getStaticCapabilitiesObject(final Version version) throws JAXBException, FileNotFoundException, IOException {
       String fileName = this.serviceType + "Capabilities" + version.toString() + ".xml";
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
               this.setLastUpdateTIme(System.currentTimeMillis());
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
     * Return a Version Object from the version number.
     * if the version number is not correct return the default version.
     *
     * @param number the version number.
     * @return
     */
    protected ServiceVersion getVersionFromNumber(String number) {
        for (ServiceVersion v : supportedVersions) {
            if (v.toString().equals(number)){
                return v;
            }
        }
        return null;
    }

    /**
     * If the requested version number is not available we choose the best version to return.
     *
     * @param A serviceType version number.
     */
    protected ServiceVersion getBestVersion(String number) {
        for (ServiceVersion v : supportedVersions) {
            if (v.toString().equals(number)){
                return v;
            }
        }
        ServiceVersion wrongVersion = new ServiceVersion(null, number);
        if (wrongVersion.compareTo(supportedVersions.get(0)) < 0) {
            return this.supportedVersions.get(0);
        } else {
            if (wrongVersion.compareTo(supportedVersions.get(supportedVersions.size() - 1)) > 0) {
                return supportedVersions.get(supportedVersions.size() - 1);
            }
        }
        return supportedVersions.get(0);
    }

    /**
     * Update all the url in a OWS capabilities document.
     *
     * @param operations A list of OWS operation.
     * @param url The url of the web application.
     * @param serviceType the initials of the web serviceType (WMS, SOS, WCS, CSW, ...). This string correspound to the resource name in lower case.
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
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * set the last time that the capabilities have been updated (not yet really used)
     *
     * @param lastUpdateSequence A Date.
     */
    public void setLastUpdateTIme(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
