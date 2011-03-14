/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
package org.constellation.ws.soap;

// J2SE dependencies
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;

// Constellation dependencies
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.ConfigDirectory;

// Geotoolkit dependencies
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;
import org.geotoolkit.util.logging.Logging;


// GeoAPI dependencies

/**
 * Abstract parent SOAP facade for all OGC web services in Constellation.
 * <p>
 * This class
 * </p>
 * <p>
 * The Open Geospatial Consortium (OGC) has defined a number of web services for
 * geospatial data such as:
 * <ul>
 *   <li><b>CSW</b> -- Catalog Service for the Web</li>
 *   <li><b>WCS</b> -- Web Coverage Service</li>
 *   <li><b>SOS</b> -- Sensor Observation Service</li>
 * </ul>
 * Many of these Web Services have been defined to work with SOAP based HTTP
 * message exchange; this class provides base functionality for those services.
 * </p>
 *
 * @version $Id$
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.7
 */
public abstract class OGCWebService<W extends Worker> {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.ws.soap");

    /**
     * A map of service worker.
     * TODO this attribute must be set to private when will fix the WFS service
     */
    protected final Map<String, W> workersMap;


    private final Specification specification;

    @Resource
    private volatile WebServiceContext context;

    /**
     * Initialize the basic attributes of a web serviceType.
     *
     * @param supportedVersions A list of the supported version of this serviceType.
     *                          The first version specified <strong>MUST</strong> be the highest
     *                          one, the best one.
     */
    public OGCWebService(final Specification spec) {

        LOGGER.log(Level.INFO, "Starting the SOAP {0} service facade.\n", spec.name());

        this.specification = spec;
        
        /*
         * build the map of Workers, by scanning the sub-directories of its service directory.
         */
        workersMap = new HashMap<String, W>();
        buildWorkerMap();
    }

    /**
     * Initialize the basic attributes of a web serviceType.
     * the worker Map here is fill by the subClasse, this is not the best behavior.
     * This constructor is here to keep compatibility with old version.
     *
     * @param supportedVersions A list of the supported version of this serviceType.
     *                          The first version specified <strong>MUST</strong> be the highest
     *                          one, the best one.
     * @param workers A map of worker id / worker.
     */
    public OGCWebService(final Map<String, W> workers, final Specification spec) {
        super();

        this.specification = spec;
        this.workersMap    = workers;
    }

    private File getServiceDirectory() {
        final File configDirectory   = ConfigDirectory.getConfigDirectory();
        if (configDirectory != null && configDirectory.exists() && configDirectory.isDirectory()) {
            final File serviceDirectory = new File(configDirectory, specification.name());
            if (serviceDirectory.exists() && serviceDirectory.isDirectory()) {
                return serviceDirectory;
            } else {
                LOGGER.log(Level.SEVERE, "The service configuration directory: {0} does not exist or is not a directory.", serviceDirectory.getPath());
            }
        } else {
            if (configDirectory == null) {
                LOGGER.severe("The service was unable to find a config directory.");
            } else {
                LOGGER.log(Level.SEVERE, "The configuration directory: {0} does not exist or is not a directory.", configDirectory.getPath());
            }
        }
        return null;
    }

    /**
     * Scan the configuration directory to instantiate Web service workers.
     */
    private void buildWorkerMap() {
        final File serviceDirectory = getServiceDirectory();
        if (serviceDirectory != null) {
            for (File instanceDirectory : serviceDirectory.listFiles()) {
                /*
                 * For each sub-directory we build a new Worker.
                 */
                if (instanceDirectory.isDirectory() && !instanceDirectory.getName().startsWith(".")) {
                    final W newWorker = createWorker(instanceDirectory);
                    workersMap.put(instanceDirectory.getName(), newWorker);
                }
            }
        }
    }

    /**
     * Build a new instance of Web service worker with the specified configuration directory
     *
     * @param instanceDirectory The configuration directory of the instance.
     * @return
     */
    protected abstract W createWorker(final File instanceDirectory);

    /**
     * extract the service URL (before serviceName/serviceID?)
     * @return
     */
    protected String getServiceURL() {
        final HttpServletRequest request =   (HttpServletRequest) context.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        String url = request.getRequestURL().toString();
        url = url.substring(0, url.lastIndexOf('/'));
        url = url.substring(0, url.lastIndexOf('/') + 1);
        return url;
    }

    /**
     * Extract the instance ID from the URL.
     * 
     * @return
     */
    private String extractWorkerID() {
        final HttpServletRequest request = (HttpServletRequest) context.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        final String url = request.getRequestURL().toString();
        return url.substring(url.lastIndexOf('/') + 1);
    }

    /**
     * Return the current worker specified by the URL.
     *
     * @return
     * @throws CstlServiceException
     */
    protected W getCurrentWorker() throws CstlServiceException {
        final String serviceID = extractWorkerID();
        if (serviceID == null || !workersMap.containsKey(serviceID)) {
            LOGGER.log(Level.WARNING, "Received request on undefined instance identifier:{0}", serviceID);
            final String msg;
            if (serviceID == null) {
                msg = "You must specify an instance id.\n available instance:" + workersMap.keySet();
            } else {
                msg = "Undefined instance id.\n available instance:" + workersMap.keySet();
            }
            throw new CstlServiceException(msg);
            // TODO return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return workersMap.get(serviceID);
        }
    }


    @PreDestroy
    public void destroy() {
        LOGGER.log(Level.INFO, "Shutting down the SOAP {0} service facade.", specification.name());
        for (final Worker worker : workersMap.values()) {
            worker.destroy();
        }
        workersMap.clear();
    }

}
