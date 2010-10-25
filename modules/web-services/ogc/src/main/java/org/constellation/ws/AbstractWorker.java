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

import javax.ws.rs.core.UriInfo;

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
     * Contains the request URI and therefore any  KVP parameters it may contain.
     */
    private UriInfo uriContext = null;

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
    public synchronized void initUriContext(final UriInfo uriInfo){
        uriContext = uriInfo;
    }

    protected synchronized UriInfo getUriContext(){
        return uriContext;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    protected Object getStaticCapabilitiesObject(final String version, final String service) throws JAXBException {
        return getStaticCapabilitiesObject(version, service, null);
    }
    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @param home    The home directory, where to search for configuration files.
     * @param version The version of the GetCapabilities.
     * @return The capabilities Object, or {@code null} if none.
     *
     * @throws JAXBException
     * @throws IOException
     */
    protected Object getStaticCapabilitiesObject(final String version, final String service, final String language) throws JAXBException {
        final String fileName;
        if (language == null) {
            fileName = service + "Capabilities" + version + ".xml";
        } else {
            fileName = service + "Capabilities" + version + '-' + language + ".xml";
        }
        final String home = null;
       
        final boolean update  = WebServiceUtilities.getUpdateCapabilitiesFlag(home);

        //Look if the template capabilities is already in cache.
        Object response = capabilities.get(fileName);
        if (response == null || update) {
            if (update) {
                LOGGER.log(logLevel, "updating metadata");
            }

            final File f = WebServiceUtilities.getFile(fileName, home);
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = getMarshallerPool().acquireUnmarshaller();
                // If the file is not present in the configuration directory, take the one in resource.
                if (!f.exists()) {
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
                LOGGER.warning("Unable to close the skeleton capabilities input stream.");
            } finally {
                if (unmarshaller != null) {
                    getMarshallerPool().release(unmarshaller);
                }
            }

            WebServiceUtilities.storeUpdateCapabilitiesFlag(home);
        }
        return response;
    }

    protected abstract MarshallerPool getMarshallerPool();


    /**
     * Look for the service configuration directory.
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
