/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.ws;

//J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.constellation.ServiceDef;

import org.constellation.ServiceDef.Specification;
import org.constellation.ws.security.SimplePDP;
import org.geotoolkit.ows.xml.AbstractCapabilitiesCore;

import org.geotoolkit.ows.xml.OWSExceptionCode;
import static org.geotoolkit.ows.xml.OWSExceptionCode.VERSION_NEGOTIATION_FAILED;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.Version;
import org.geotoolkit.util.collection.UnmodifiableArrayList;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.xml.MarshallerPool;
import org.xml.sax.SAXException;


/**
 * Abstract definition of a {@code Web Map Service} worker called by a facade
 * to perform the logic for a particular WMS instance.
 *
 * @version $Id: AbstractWMSWorker.java 1889 2009-10-14 16:05:52Z eclesia $
 *
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractWorker implements Worker {

    /**
     * The default logger.
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.ws");

    /**
     * A flag indicating if the worker is correctly started.
     */
    protected boolean isStarted;

    protected boolean multipleVersionActivated = true;
    
    /**
     * A message keeping the reason of the start error of the service
     */
    protected String startError;

    /**
     * Contains the service url used in capabilities document.
     */
    private String serviceUrl = null;

    /**
     * The direcory containing the configuration for this service. TODO make it private => SOSWorker.recordMapping
     */
    protected final File configurationDirectory;

    /**
     * The log level off al the informations log.
     */
    protected Level logLevel = Level.INFO;

    /**
     * A map containing the Capabilities Object already loaded from file.
     */
    private final Map<String,Object> capabilities = Collections.synchronizedMap(new HashMap<String,Object>());

    /**
     * Output responses of a GetCapabilities request.
     */
    private static final Map<String,AbstractCapabilitiesCore> CAPS_RESPONSE = new HashMap<String, AbstractCapabilitiesCore>();
    
    /**
     * The identifier of the worker.
     */
    private final String id;

    /**
     * The specification for this worker.
     */
    private final Specification specification;
    
    protected UnmodifiableArrayList<ServiceDef> supportedVersions;

    /**
     * use this flag to pass the shiro security when using the worker in a non web context.
     */
    protected boolean shiroAccessible = true;
    
    /**
     * use this flag to pass the shiro security when using the worker in a non web context.
     */
    protected boolean cacheCapabilities = true;
    
    /**
     * A Policy Decision Point (PDP) if some security constraints have been defined.
     */
    protected SimplePDP pdp = null;
    
    private List<Schema> schemas = null;

    private long currentUpdateSequence = System.currentTimeMillis();

    public AbstractWorker(final String id, final File configurationDirectory, final Specification specification) {
        this.id = id;
        this.configurationDirectory = configurationDirectory;
        this.specification = specification;
    }
    
    protected String getUserLogin() {
        final String userLogin;
        if (shiroAccessible) {
            userLogin = org.constellation.ws.security.SecurityManager.getCurrentUserLogin();
        } else {
            userLogin = null;
        }
        return userLogin;
    }

    protected void setSupportedVersion(final ServiceDef... supportedVersions) {
         this.supportedVersions = UnmodifiableArrayList.wrap(supportedVersions.clone());
    }
    
    protected boolean isSupportedVersion(final String version) {
        final ServiceDef.Version vv = new ServiceDef.Version(version);
        for (ServiceDef sd : supportedVersions) {
            if (sd.version.equals(vv)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verify if the version is supported by this serviceType.
     * <p>
     * If the version is not accepted we send an exception.
     * </p>
     */
    @Override
    public void checkVersionSupported(final String versionNumber) throws CstlServiceException {
        if (getVersionFromNumber(versionNumber) == null) {
            final StringBuilder messageb = new StringBuilder("The parameter ");
            for (ServiceDef vers : supportedVersions) {
                messageb.append("VERSION=").append(vers.version.toString()).append(" OR ");
            }
            messageb.delete(messageb.length()-4, messageb.length()-1);
            messageb.append(" must be specified");
            throw new CstlServiceException(messageb.toString(), VERSION_NEGOTIATION_FAILED, "version");
        }
    }
    
    /**
     * Return a Version Object from the version number.
     * if the version number is not correct return the default version.
     *
     * @param number the version number.
     * @return
     */
    @Override
    public ServiceDef getVersionFromNumber(final Version number) {
        if (number != null) {
            for (ServiceDef v : supportedVersions) {
                if (v.version.toString().equals(number.toString())){
                    return v;
                }
            }
        }
        return null;
    }
    
    /**
     * Return a Version Object from the version number.
     * if the version number is not correct return the default version.
     *
     * @param number the version number.
     * @return
     * 
     */
    @Override
    public ServiceDef getVersionFromNumber(final String number) {
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
     * 
     */
    @Override
    public ServiceDef getBestVersion(final String number) {
        for (ServiceDef v : supportedVersions) {
            if (v.version.toString().equals(number)){
                return v;
            }
        }
        final ServiceDef firstSpecifiedVersion = supportedVersions.get(0);
        if (number == null || number.isEmpty()) {
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
     * {@inheritDoc }
     */
    @Override
    public void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl + specification.toString().toLowerCase() + '/' + id + '?';
    }

    /**
     * return the current service URL.
     * @return
     */
    @Override
    public synchronized String getServiceUrl(){
        return serviceUrl;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setLogLevel(final Level logLevel) {
        this.logLevel = logLevel;
    }
    
    @Override
    public void setShiroAccessible(final boolean shiroAccessible) {
        this.shiroAccessible = shiroAccessible;
    }

    protected abstract String getProperty(final String propertyName);
    
    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     * This method has a cache system, the object will be read from the file system only one time.
     *
     * @param service The service type identifier. example "WMS"
     * @param version The version of the GetCapabilities.
     *
     * @return The capabilities Object, or {@code null} if none.
     *
     * @throws JAXBException if an error occurs during the unmarshall of the document.
     */
    protected Object getStaticCapabilitiesObject(final String version, final String service) throws CstlServiceException {
        return getStaticCapabilitiesObject(version, service, null);
    }

    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     * This method has a cache system, the object will be read from the file system only one time.
     *
     * @param service The service type identifier. example "WMS"
     * @param version The version of the GetCapabilities.
     * @param language The language of the capabilities skeleton.
     *
     * @return The capabilities Object, or {@code null} if none.
     *
     * @throws JAXBException if an error occurs during the unmarshall of the document.
     */
    protected Object getStaticCapabilitiesObject(final String version, final String service, final String language) throws CstlServiceException {
        final String fileName;
        if (language == null) {
            fileName = service + "Capabilities" + version + ".xml";
        } else {
            fileName = service + "Capabilities" + version + '-' + language + ".xml";
        }

        //Look if the template capabilities is already in cache.
        Object response = capabilities.get(fileName);
        if (response == null) {
            final File f;
            if (configurationDirectory != null && configurationDirectory.exists()) {
                f = new File(configurationDirectory, fileName);
            } else {
                f = null;
            }
            try {
                final Unmarshaller unmarshaller = getMarshallerPool().acquireUnmarshaller();
                // If the file is not present in the configuration directory, take the one in resource.
                if (f == null || !f.exists()) {
                    final InputStream in = getClass().getResourceAsStream(fileName);
                    if (in != null) {
                        response = unmarshaller.unmarshal(in);
                        in.close();
                    } else {
                        throw new CstlServiceException("Unable to find the capabilities skeleton from resource:" + fileName, OWSExceptionCode.NO_APPLICABLE_CODE);
                    }
                } else {
                    response = unmarshaller.unmarshal(f);
                }
                getMarshallerPool().release(unmarshaller);
                
                if (response instanceof JAXBElement) {
                    response = ((JAXBElement)response).getValue();
                }

                capabilities.put(fileName, response);
            } catch (IOException ex) {
                throw new CstlServiceException("Unable to close the skeleton capabilities input stream.", ex, OWSExceptionCode.NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("JAXB exception while unmarshaling static capabilities file", ex, OWSExceptionCode.NO_APPLICABLE_CODE);
            }
        }
        return response;
    }

    /**
     * Return the marshaller pool used to unmarshaller the capabilities documents of the service.
     *
     * @return the marshaller pool used to unmarshaller the capabilities documents of the service.
     */
    protected abstract MarshallerPool getMarshallerPool();

    /**
     * Throw and exception if the service is not working
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    protected void isWorking() throws CstlServiceException {
        if (!isStarted) {
            throw new CstlServiceException("The service is not running!\nCause:" + startError, OWSExceptionCode.NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return isStarted;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPostRequestLog() {
        final String value = getProperty("postRequestLog");
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrintRequestParameter() {
        final String value = getProperty("printRequestParameter");
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return true;
    }
    
    /**
     * A flag indicating if the transaction methods of the worker are securized.
     */
    protected boolean isTransactionSecurized() {
        final String value = getProperty("transactionSecurized");
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return true;
    }

    @Override
    public boolean isRequestValidationActivated() {
        final String value = getProperty("requestValidationActivated");
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return false;
    }
    
    @Override
    public List<Schema> getRequestValidationSchema() {
        if (schemas == null) {
            final String value = getProperty("requestValidationSchema");
            schemas = new ArrayList<Schema>();
            if (value != null) {
                final List<String> schemaPaths = StringUtilities.toStringList(value);
                LOGGER.info("Reading schemas. This may take some times ...");
                final SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
                for (String schemaPath : schemaPaths) {
                    LOGGER.log(Level.INFO, "Reading {0}", schemaPath);
                    try {
                        schemas.add(sf.newSchema(new URL(schemaPath)));
                    } catch (SAXException ex) {
                        LOGGER.warning("SAX exception while adding the Validator to the JAXB unmarshaller");
                    } catch (MalformedURLException ex) {
                        LOGGER.warning("MalformedURL exception while adding the Validator to the JAXB unmarshaller");
                    }
                }
            }
        }
        return schemas;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorized(final String ip, final String referer) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSecured() {
        return false;
    }

    /**
     * @return the currentUpdateSequence
     */
    protected String getCurrentUpdateSequence() {
        return Long.toString(currentUpdateSequence);
    }

    /**
     * Set the current date to the updateSequence parameter
     */
    protected void refreshUpdateSequence() {
        currentUpdateSequence = System.currentTimeMillis();
    }

    protected boolean returnUpdateSequenceDocument(final String updateSequence) throws CstlServiceException {
        if (updateSequence == null) {
            return false;
        }
        try {
            final long sequenceNumber = Long.parseLong(updateSequence);
            if (sequenceNumber == currentUpdateSequence) {
                return true;
            } else if (sequenceNumber > currentUpdateSequence) {
                throw new CstlServiceException("The update sequence parameter is invalid (higher value than the current)", OWSExceptionCode.INVALID_UPDATE_SEQUENCE, "updateSequence");
            }
            return false;
        } catch(NumberFormatException ex) {
            throw new CstlServiceException("The update sequence must be an integer", ex, OWSExceptionCode.INVALID_PARAMETER_VALUE, "updateSequence");
        }
    }
    
    /**
     * Return a cached capabilities response.
     * 
     * @param version
     * @return r
     */
    protected AbstractCapabilitiesCore getCapabilitiesFromCache(final String version, final String language) {
        final String keyCache = specification.name() + '-' + id + '-' + version + '-' + language;
        return CAPS_RESPONSE.get(keyCache);
    }
    
    /**
     * Add the capabilities object to the cache.
     * 
     * @param version
     * @param language
     * @param capabilities 
     */
    protected void putCapabilitiesInCache(final String version, final String language, final AbstractCapabilitiesCore capabilities) {
        if (cacheCapabilities) {
            final String keyCache = specification.name() + '-' + id + '-' + version + '-' + language;
            CAPS_RESPONSE.put(keyCache, capabilities);
        }
    }
    
    protected void clearCapabilitiesCache() {
        final List<String> toClear = new ArrayList<String>();
        for (String key: CAPS_RESPONSE.keySet()) {
            if (key.startsWith(specification.name() + '-')) {
                toClear.add(key);
            }
        }
        for (String key : toClear) {
            CAPS_RESPONSE.remove(key);
        }
    }
    
    @Override
    public void destroy() {
        clearCapabilitiesCache();
    }
}
