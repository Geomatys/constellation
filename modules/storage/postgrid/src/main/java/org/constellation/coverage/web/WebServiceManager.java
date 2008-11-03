/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.coverage.web;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.lang.management.ManagementFactory;
import javax.imageio.ImageIO;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.geotools.util.logging.Logging;


/**
 * Manages a collection of {@link WebServiceWorker}.
 *
 * @version $Id$
 * @author Guilhem Legal
 * @author Martin Desruisseaux
 */
final class WebServiceManager implements WebServiceManagerMBean {
    /**
     * A logger for {@link WebServiceManager} instances.
     */
    private static final Logger LOGGER = ImageProducer.LOGGER;

    /**
     * The list of all running web service workers.
     */
    private final List<ImageProducer> workers;

    /**
     * Uniquely identify the MBeans to be registered in the platform server.
     * May be {@code null} if the name can't be created.
     */
    private final ObjectName name;

    /**
     * The platform server on which this MBean is deployed.
     * May be {@code null} if the MBeans can't be registered.
     */
    private final MBeanServer server;

    /**
     * Creates an initially empty {@code WebServiceManager}.
     *
     * @param jmx {@code true} for enabling JMX management, or {@code false} otherwise.
     */
    public WebServiceManager(final boolean jmx) {
        workers = new ArrayList<ImageProducer>();
        if (jmx) {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName name;
            try {
               
               name = new ObjectName("WebServiceManager:name=WebServiceManager");
               if(server.isRegistered(name)) {
                   server.unregisterMBean(name);
               }
               server.registerMBean(this, name);
            } catch (JMException exception) {
                Logging.unexpectedException(LOGGER, WebServiceManager.class, "<init>", exception);
                name = null;
                server = null;
            }
            this.name = name;
            this.server = server;
        } else {
            name = null;
            server = null;
        }
    }

    /**
     * Adds a new worker to the list.
     */
    public void addWorker(final ImageProducer worker){
        workers.add(worker);
    }

    /**
     * Removes a worker from the list.
     */
    public void removeWorker(final ImageProducer worker){
        workers.remove(worker);
    }

    /**
     * Returns the current number of {@link WebServiceWorker}s.
     */
    public int workerCount() {
        return workers.size();
    }

    /**
     * Flushs the cache on every running workers.
     */
    public void flush() {
        LOGGER.info("Flush all WebServiceWorkers");
        for (ImageProducer worker: workers){
            try {
                worker.flush();
            } catch (WebServiceException exception) {
                Logging.unexpectedException(LOGGER, WebServiceManager.class, "flush", exception);
            }
        }
    }
    
    /**
     * Scan the plugin used by the jvm and display the current wcs formats accepted.
     */
    public void scanPlugin() {
        ImageIO.scanForPlugins();
    }

    public String acceptedFormats() {
        return java.util.Arrays.toString(ImageIO.getReaderFormatNames());
    }
    
    /**
     * Disposes every workers and unregister The MBean from the platform server.
     */
    public void dispose() {
        for (final Iterator<ImageProducer> it = workers.iterator(); it.hasNext();) {
            final ImageProducer worker = it.next();
            try {
                worker.dispose();
            } catch (WebServiceException exception) {
                Logging.unexpectedException(LOGGER, WebServiceManager.class, "dispose", exception);
                continue;
            }
            // Remove from the list only if the disposal succeed.
            it.remove();
        }
        if (server != null && server.isRegistered(name)) try {
            server.unregisterMBean(name);
        } catch (JMException exception) {
            Logging.unexpectedException(LOGGER, WebServiceManager.class, "dispose", exception);
        }
    }
}
