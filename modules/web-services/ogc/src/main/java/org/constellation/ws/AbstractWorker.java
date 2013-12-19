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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.constellation.ServiceDef;
import org.constellation.ServiceDef.Specification;
import org.constellation.ws.security.SimplePDP;
import org.geotoolkit.ows.xml.AbstractCapabilitiesCore;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.util.StringUtilities;
import org.apache.sis.util.Version;
import org.apache.sis.internal.util.UnmodifiableArrayList;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.dto.Service;

import org.constellation.security.SecurityManagerHolder;

import org.opengis.util.CodeList;
import org.xml.sax.SAXException;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

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
     * The log level off al the informations log.
     */
    protected Level logLevel = Level.INFO;

    /**
     * A map containing the Capabilities Object already loaded from file.
     */
    private final Map<String, Service> capabilities = Collections.synchronizedMap(new HashMap<String,Service>());

    /**
     * Output responses of a GetCapabilities request.
     */
    private static final Map<String,AbstractCapabilitiesCore> CAPS_RESPONSE = new HashMap<>();

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
    
    /**
     * Security manager.
     */
    

    public AbstractWorker(final String id, final Specification specification) {
        this.id = id;
        this.specification = specification;
    }

    protected String getUserLogin() {
        final String userLogin;
        if (shiroAccessible) {
            userLogin = SecurityManagerHolder.getInstance().getCurrentUserLogin();
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
    public void checkVersionSupported(final String versionNumber, final boolean getCapabilities) throws CstlServiceException {
        if (getVersionFromNumber(versionNumber) == null) {
            final StringBuilder messageb = new StringBuilder("The parameter ");
            for (ServiceDef vers : supportedVersions) {
                messageb.append("VERSION=").append(vers.version.toString()).append(" OR ");
            }
            messageb.delete(messageb.length()-4, messageb.length()-1);
            messageb.append(" must be specified");
            final CodeList code;
            if (getCapabilities) {
                code = VERSION_NEGOTIATION_FAILED;
            } else {
                code = INVALID_PARAMETER_VALUE;
            }
            throw new CstlServiceException(messageb.toString(), code, "version");
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
        if (serviceUrl.endsWith("/")) {
            this.serviceUrl = serviceUrl + specification.toString().toLowerCase() + '/' + id + '?';
        } else {
            this.serviceUrl = serviceUrl + '/' + specification.toString().toLowerCase() + '/' + id + '?';
        }
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
     * @param language The language of the metadata.
     *
     * @return The capabilities Object, or {@code null} if none.
     *
     * @throws CstlServiceException if an error occurs during the unmarshall of the document.
     */
    protected Service getStaticCapabilitiesObject(final String service, final String language) throws CstlServiceException {
        final String key;
        if (language == null) {
            key = getId() + service;
        } else {
            key = getId() + service + "-" + language;
        }
        Service metadata = capabilities.get(key);
        if (metadata == null) {
            try {
                metadata = ConfigurationEngine.readServiceMetadata(getId(), service, language);
                capabilities.put(key, metadata);
            } catch (JAXBException | IOException ex) {
                LOGGER.log(Level.WARNING, "An error occurred when trying to read the service metadata. Returning default capabilities.", ex);
            }
        }
        return metadata;
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
            schemas = new ArrayList<>();
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
        final List<String> toClear = new ArrayList<>();
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

    @Override
    public Object getConfiguration() {
        return null;
    }
}
