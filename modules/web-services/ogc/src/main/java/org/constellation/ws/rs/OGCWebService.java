/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2009, Geomatys
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.ServiceDef;
import org.constellation.ws.ServiceType;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.CstlServiceException;

// Geotools dependencies
import org.geotoolkit.internal.CodeLists;
import org.geotoolkit.ows.xml.AbstractDCP;
import org.geotoolkit.ows.xml.AbstractOnlineResourceType;
import org.geotoolkit.ows.xml.AbstractOperation;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.util.Version;

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
    private final List<ServiceDef> supportedVersions = new ArrayList<ServiceDef>();
    /**
     * The version of the WMS specification for this request.
     */
    @Deprecated
    private ServiceDef actingVersion;
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
    public OGCWebService(ServiceDef... supportedVersions) {
        super();

        for (final ServiceDef element : supportedVersions) {
            this.supportedVersions.add(element);
        }
        if (this.supportedVersions.size() == 0) {
            throw new IllegalArgumentException("It is compulsory for a web service to have " +
                    "at least one version specified.");
        }
        final ServiceDef firstDef = this.supportedVersions.get(0);
        this.serviceType = firstDef.specification.toString();
        // We set that the current version is probably the highest one, the best one, which should
        // be the first in the list of supported version.
        this.actingVersion = firstDef;
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
//    protected void verifyBaseParameter(int sld) throws CstlServiceException {
//        if (sld == 2) {
//            if (!getParameter("VERSION", true).equals(sldVersion.toString())) {
//                throw new CstlServiceException("The parameter VERSION=" + sldVersion + " must be specified",
//                               MISSING_PARAMETER_VALUE);
//            } else {
//                return;
//            }
//        }
//        // if the version is not accepted we send an exception
//        String inputVersion = getParameter("VERSION", true);
//        if (getVersionFromNumber(inputVersion) == null) {
//
//            String message = "The parameter ";
//            for (ServiceDef vers : supportedVersions) {
//                message += "VERSION=" + vers.version.toString() + " OR ";
//            }
//            message = message.substring(0, message.length()-3);
//            message += " must be specified";
//            throw new CstlServiceException(message, VERSION_NEGOTIATION_FAILED);
//        } else {
//            setActingVersion(inputVersion);
//        }
//        if (sld == 1) {
//            if (!getParameter("SLD_VERSION", true).equals(sldVersion.toString())) {
//                throw new CstlServiceException("The parameter SLD_VERSION=" + sldVersion + " must be specified",
//                               VERSION_NEGOTIATION_FAILED);
//            }
//        }
//    }

    /**
     * Verify if the version is supported by this serviceType.
     * <p>
     * If the version is not accepted we send an exception.
     * </p>
     */
    protected void isVersionSupported(String versionNumber) throws CstlServiceException {
        if (getVersionFromNumber(versionNumber) == null) {
            final StringBuilder messageb = new StringBuilder("The parameter ");
            for (ServiceDef vers : supportedVersions) {
                messageb.append("VERSION=").append(vers.version.toString()).append(" OR ");
            }
            String message = messageb.substring(0, messageb.length()-3);
            message += " must be specified";
            throw new CstlServiceException(message, VERSION_NEGOTIATION_FAILED);
        }
    }

    /**
     * Return the current version of the Web ServiceType.
     */
    @Deprecated
    protected ServiceDef getActingVersion() {
        return this.actingVersion;
    }

    /**
     * Return the current version of the Web ServiceType.
     */
    @Deprecated
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
     * Handle all exceptions returned by a web service operation in two ways:
     * <ul>
     *   <li>if the exception code indicates a mistake done by the user, just display a single
     *       line message in logs.</li>
     *   <li>otherwise logs the full stack trace in logs, because it is something interesting for
     *       a developper</li>
     * </ul>
     * In both ways, the exception is then marshalled and returned to the client.
     *
     * @param ex         The exception that has been generated during the webservice operation requested.
     * @param marshaller The marshaller to use for the exception report.
     * @param serviceDef The service definition, from which the version number of exception report will
     *                   be extracted.
     * @return An XML representing the exception.
     *
     * @throws JAXBException if an error occurs during the marshalling of the exception.
     */
    protected abstract Response processExceptionResponse(final CstlServiceException ex, final Marshaller marshaller,
                                                         final ServiceDef serviceDef) throws JAXBException;

    /**
     * The shared method to build a service ExceptionReport.
     *
     * @param message
     * @param codeName
     * @return
     */
    @Override
    protected Response launchException(final String message, String codeName, final String locator) throws JAXBException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();

            if (isOWS(actingVersion)) {
                codeName = codeName.replace("_", "");
                codeName = codeName.toLowerCase();
            }
            final OWSExceptionCode code   = CodeLists.valueOf(OWSExceptionCode.class, codeName);
            final CstlServiceException ex = new CstlServiceException(message, code, locator);
            return processExceptionResponse(ex, marshaller, supportedVersions.get(0));
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
    }

    /**
     * Returns the file where to read the capabilities document for each serviceType.
     * If no such file is found, then this method returns {@code null}.
     *
     * @return The capabilities Object, or {@code null} if none.
     */
    public Object getStaticCapabilitiesObject() throws JAXBException {
        return getStaticCapabilitiesObject(getActingVersion().version);
    }

    /**
     * Returns the file where to read the capabilities document for each serviceType.
     * If no such file is found, then this method returns {@code null}.
     *
     * @return The capabilities Object, or {@code null} if none.
     */
    public Object getStaticCapabilitiesObject(final Version version) throws JAXBException {
        final String fileName = this.serviceType + "Capabilities" + version.toString() + ".xml";
        final File changeFile = getFile("change.properties");
        final Properties p    = new Properties();

        // if the flag file is present we load the properties
        if (changeFile != null && changeFile.exists()) {
            try {
                final FileInputStream in = new FileInputStream(changeFile);
                p.load(in);
                in.close();
            } catch (IOException ex) {
                LOGGER.warning("Unable to read the change.properties file");
            }
        } else {
            p.put("update", "false");
        }

       //we recup the capabilities file and unmarshall it
       
       //we look if we have already put it in cache
       Object response = capabilities.get(fileName);
       final boolean update  = p.getProperty("update").equals("true");

       if (response == null || update) {
           if (update) {
               LOGGER.info("updating metadata");
           }

           final File f = getFile(fileName);
           Unmarshaller unmarshaller = null;
           try {
               unmarshaller = marshallerPool.acquireUnmarshaller();
               if (f == null || !f.exists()) {
                   final InputStream in = getClass().getResourceAsStream(fileName);
                   response = unmarshaller.unmarshal(in);
                   in.close();
               } else {
                   response = unmarshaller.unmarshal(f);
               }
           } catch (IOException ex) {
               LOGGER.info("Unable to close the skeleton capabilities input stream.");
           } finally {
               if (unmarshaller != null) {
                   marshallerPool.release(unmarshaller);
               }
           }
           if (response != null) {
               capabilities.put(fileName, response);
               this.setLastUpdateTIme(System.currentTimeMillis());
               p.put("update", "false");
           }

           // if the flag file is present we store the properties
           if (changeFile != null && changeFile.exists()) {
               try {
                   final FileOutputStream out = new FileOutputStream(changeFile);
                   p.store(out, "updated from WebService");
                   out.close();
               } catch (IOException ex) {
                   LOGGER.warning("Unable to write the change.properties file");
               }
           }
       }
       return response;
        
    }

    /**
     * Return a Version Object from the version number.
     * if the version number is not correct return the default version.
     *
     * @param number the version number.
     * @return
     */
    protected ServiceDef getVersionFromNumber(String number) {
        for (ServiceDef v : supportedVersions) {
            if (v.version.toString().equals(number)){
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
    protected ServiceDef getBestVersion(final String number) {
        for (ServiceDef v : supportedVersions) {
            if (v.version.toString().equals(number)){
                return v;
            }
        }
        final ServiceDef firstSpecifiedVersion = supportedVersions.get(0);
        if (number == null || number.equals("")) {
            return firstSpecifiedVersion;
        }
        final ServiceDef.Version wrongVersion = new ServiceDef.Version(number);
        if (wrongVersion.compareTo(firstSpecifiedVersion.version) > 0) {
            return firstSpecifiedVersion;
        } else {
            if (wrongVersion.compareTo(supportedVersions.get(supportedVersions.size() - 1).version) < 0) {
                return supportedVersions.get(supportedVersions.size() - 1);
            }
        }
        return firstSpecifiedVersion;
    }

    /**
     * Returns whether the service is OWS, or not.
     *
     * @param def The service definition to consider.
     * @return True if the service is OWS, false otherwise.
     */
    protected boolean isOWS(final ServiceDef def) {
        if (def == null) {
            throw new IllegalArgumentException("Unable to know if the service is OWS because it is not defined.");
        }
        return  def.equals(ServiceDef.CSW_2_0_2) || def.equals(ServiceDef.PDP)       ||
                def.equals(ServiceDef.PEP)       || def.equals(ServiceDef.SOS_1_0_0) ||
                def.equals(ServiceDef.WCS_1_1_1) || def.equals(ServiceDef.WCS_1_1_0) ||
                def.equals(ServiceDef.WCS_1_1_2) || def.equals(ServiceDef.WMTS_1_0_0);
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
                for (AbstractOnlineResourceType method:dcp.getHTTP().getGetOrPost()) {
                    method.setHref(url + service.toLowerCase() + "?");
                }
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
