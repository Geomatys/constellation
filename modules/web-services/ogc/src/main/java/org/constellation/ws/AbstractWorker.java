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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.constellation.provider.configuration.ConfigDirectory;

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
    private final Map<String,Object> capabilities = new HashMap<String,Object>();

    /**
     * {@inheritDoc }
     */
    @Override
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * return the current service URL.
     * @return
     */
    protected synchronized String getServiceUrl(){
        return serviceUrl;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setLogLevel(Level logLevel) {
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
    protected Object getStaticCapabilitiesObject(final String version, final String service) throws JAXBException {
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
    protected Object getStaticCapabilitiesObject(final String version, final String service, final String language) throws JAXBException {
        final String fileName;
        if (language == null) {
            fileName = service + "Capabilities" + version + ".xml";
        } else {
            fileName = service + "Capabilities" + version + '-' + language + ".xml";
        }
        final boolean update  = WebServiceUtilities.getUpdateCapabilitiesFlag();

        //Look if the template capabilities is already in cache.
        Object response = capabilities.get(fileName);
        if (response == null || update) {
            if (update) {
                LOGGER.log(logLevel, "updating metadata");
            }

            final File configDirectory = getConfigurationDirectory(service);
            final File f;
            if (configDirectory != null && configDirectory.exists()) {
                f = new File(configDirectory, fileName);
            } else {
                f = null;
            }
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = getMarshallerPool().acquireUnmarshaller();
                // If the file is not present in the configuration directory, take the one in resource.
                if (f == null || !f.exists()) {
                    final InputStream in = getClass().getResourceAsStream(fileName);
                    response = unmarshaller.unmarshal(in);
                    in.close();
                } else {
                    response = unmarshaller.unmarshal(f);
                }

                if(response instanceof JAXBElement){
                    response = ((JAXBElement)response).getValue();
                }

                capabilities.put(fileName, response);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Unable to close the skeleton capabilities input stream.", ex);
            } finally {
                if (unmarshaller != null) {
                    getMarshallerPool().release(unmarshaller);
                }
            }

            WebServiceUtilities.storeUpdateCapabilitiesFlag();
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
    * Look for the service configuration directory.
    *
    * @param service The service type identifier. example "WMS"
    *
    * @return The configuration directory of the service or {@code null}
    * if there is no configuration directory for this service.
    */
    protected File getConfigurationDirectory(String service) {
        final File configDir = ConfigDirectory.getConfigDirectory();
        if (configDir != null && configDir.exists()) {
            final File sosDir = new File(configDir, service);
            if (sosDir != null && sosDir.exists()) {
                LOGGER.log(Level.INFO, "taking configuration for service {0} from directory: {1}", new Object[]{service, configDir.getPath()});
            } else {
                LOGGER.log(Level.WARNING, "Unable to find a {0} configuration directory", service);
            }
            return sosDir;
        }
        return null;
    }
}
