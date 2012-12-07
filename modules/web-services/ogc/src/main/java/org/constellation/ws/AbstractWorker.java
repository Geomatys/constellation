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

import org.constellation.ServiceDef.Specification;
import org.constellation.ws.security.SimplePDP;
import org.geotoolkit.ows.xml.AbstractCapabilitiesCore;

import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.xml.MarshallerPool;


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

    /**
     * A flag indicating if the transaction methods of the worker are securized.
     */
    protected boolean transactionSecurized = true;

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

    /**
     * A Policy Decision Point (PDP) if some security constraints have been defined.
     */
    protected SimplePDP pdp = null;

    private long currentUpdateSequence = System.currentTimeMillis();

    public AbstractWorker(final String id, final File configurationDirectory, final Specification specification) {
        this.id = id;
        this.configurationDirectory = configurationDirectory;
        this.specification = specification;
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
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = getMarshallerPool().acquireUnmarshaller();
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

                if (response instanceof JAXBElement) {
                    response = ((JAXBElement)response).getValue();
                }

                capabilities.put(fileName, response);
            } catch (IOException ex) {
                throw new CstlServiceException("Unable to close the skeleton capabilities input stream.", ex, OWSExceptionCode.NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("JAXB exception while unmarshaling static capabilities file", ex, OWSExceptionCode.NO_APPLICABLE_CODE);
            } finally {
                if (unmarshaller != null) {
                    getMarshallerPool().release(unmarshaller);
                }
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
        final String keyCache = specification.name() + '-' + id + '-' + version + '-' + language;
        CAPS_RESPONSE.put(keyCache, capabilities);
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
