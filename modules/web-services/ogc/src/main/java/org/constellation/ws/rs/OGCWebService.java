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
import org.constellation.ws.CstlServiceException;
import org.constellation.ows.AbstractDCP;
import org.constellation.ows.AbstractOnlineResourceType;
import org.constellation.ows.AbstractOperation;
import org.constellation.ows.OWSExceptionCode;

// Geotools dependencies
import org.constellation.ows.v110.ExceptionReport;
import org.geotools.util.Version;

import static org.constellation.ws.ExceptionCode.*;


/**
 * Abstract parent REST facade for all OGC web services in Constellation.
 * <p>
 * This class
 * </p>
 * <p>
 * The Open Geospatial Consortium (OGC) has defined a number of web services for 
 * geospatial data such as:
 * <ul>
 *   <li><b>CSW</b> -- Catalog Service for the Web</li>
 *   <li><b>WMS</b> -- Web Map Service</li>
 *   <li><b>WCS</b> -- Web Coverage Service</li>
 *   <li><b>SOS</b> -- Sensor Observation Service</li>
 * </ul>
 * Many of these Web Services have been defined to work with REST based HTTP 
 * message exchange; this class provides base functionality for those services.
 * </p>
 *
 * @version $Id$
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
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
     * parameter to the Date constructor).
     */
    private long lastUpdateTime;
    
    
    /**
     * Initialize the basic attributes of a web serviceType.
     *
     * @param serviceType The initials of the web serviceType (CSW, WMS, WCS, SOS, ...)
     * @param supportedVersions A list of the supported version of this serviceType.
     *                          The first version specified <strong>MUST</strong> be the highest
     *                          one, the best one.
     */
    public OGCWebService(String service, ServiceVersion... supportedVersions) {
        super();
        this.serviceType = service;

        for (final ServiceVersion element : supportedVersions) {
            this.supportedVersions.add(element);
        }
        if (this.supportedVersions.size() == 0) {
            throw new IllegalArgumentException("It is compulsory for a web service to have " +
                    "at least one version specified.");
        }
        // We set that the current version is probably the highest one, the best one, which should
        // be the first in the list of supported version.
        this.actingVersion = this.supportedVersions.get(0);
        unmarshaller = null;//TODO: explain this. We are freeing the un-needed resource?
    }

    /**
     * Verify the base parameter or each request.
     *
     * @param sld case 0: no sld.
     *            case 1: VERSION parameter for WMS version and SLD_VERSION for sld version.
     *            case 2: VERSION parameter for sld version.
     *
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    protected void verifyBaseParameter(int sld) throws CstlServiceException {
        if (sld == 2) {
            if (!getParameter("VERSION", true).equals(sldVersion.toString())) {
                throw new CstlServiceException("The parameter VERSION=" + sldVersion + " must be specified",
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
            throw new CstlServiceException(message, VERSION_NEGOTIATION_FAILED);
        } else {
            setActingVersion(inputVersion);
        }
        if (sld == 1) {
            if (!getParameter("SLD_VERSION", true).equals(sldVersion.toString())) {
                throw new CstlServiceException("The parameter SLD_VERSION=" + sldVersion + " must be specified",
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
    protected void isVersionSupported(String versionNumber) throws CstlServiceException {
        if (getVersionFromNumber(versionNumber) == null) {
            String message = "The parameter ";
            for (ServiceVersion vers : supportedVersions) {
                message += "VERSION=" + vers.toString() + " OR ";
            }
            message = message.substring(0, message.length()-3);
            message += " must be specified";
            throw new CstlServiceException(message, VERSION_NEGOTIATION_FAILED);
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
     * The shared method to build a service ExceptionReport.
     *
     * @param message
     * @param codeName
     * @return
     */
    @Override
    protected Object launchException(final String message, final String codeName, final String locator) {
        if (getActingVersion().isOWS()) {
            final OWSExceptionCode code = OWSExceptionCode.valueOf(codeName);
            final ExceptionReport report = new ExceptionReport(message, code.name(), locator, getActingVersion().toString());
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
    public Object getStaticCapabilitiesObject() throws JAXBException {
        return getStaticCapabilitiesObject(getActingVersion());
    }

    /**
     * Returns the file where to read the capabilities document for each serviceType.
     * If no such file is found, then this method returns {@code null}.
     *
     * @return The capabilities Object, or {@code null} if none.
     */
    public Object getStaticCapabilitiesObject(final Version version) throws JAXBException {
        String fileName = this.serviceType + "Capabilities" + version.toString() + ".xml";
        File changeFile = getFile("change.properties");
        Properties p    = new Properties();

        // if the flag file is present we load the properties
        if (changeFile != null && changeFile.exists()) {
            try {
                FileInputStream in = new FileInputStream(changeFile);
                p.load(in);
                in.close();
            } catch (IOException ex) {
                LOGGER.warning("Unable to read the change.properties file");
            }
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
                   try {
                   FileOutputStream out = new FileOutputStream(changeFile);
                   p.store(out, "updated from WebService");
                   out.close();
                   } catch (IOException ex) {
                        LOGGER.warning("Unable to write the change.properties file");
                   }
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
     * @param number A version number, which will be compared to the ones specified.
     *               Can be {@code null}, in this case the best version specified is just returned.
     * @return The best version (the highest one) specified for this web service.
     */
    protected ServiceVersion getBestVersion(final String number) {
        for (ServiceVersion v : supportedVersions) {
            if (v.toString().equals(number)){
                return v;
            }
        }
        final ServiceVersion firstSpecifiedVersion = supportedVersions.get(0);
        if (number == null || number.equals("")) {
            return firstSpecifiedVersion;
        }
        final ServiceVersion wrongVersion = new ServiceVersion(null, number);
        if (wrongVersion.compareTo(firstSpecifiedVersion) < 0) {
            return firstSpecifiedVersion;
        } else {
            if (wrongVersion.compareTo(supportedVersions.get(supportedVersions.size() - 1)) > 0) {
                return supportedVersions.get(supportedVersions.size() - 1);
            }
        }
        return firstSpecifiedVersion;
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
