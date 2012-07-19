/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

import java.util.logging.Level;

/**
 * Generic definition of a worker.
 *
 * @author Johann Sorel (Geomatys)
 */
public interface Worker {
    /**
     * Destroy the worker and free the resources.
     */
    void destroy();

    /**
     * Initialize the serviceUrl of the service.
     */
    void setServiceUrl(final String serviceUrl);

    /**
     * Set The Logging level for all the info message in the worker
     *
     * @param logLevel
     */
    void setLogLevel(Level logLevel);

    /**
     * Return the identifier of the worker.
     * @return
     */
    String getId();

    /**
     * Returns {@code true} if the worker is secured, {@code false} otherwise.
     */
    boolean isSecured();

    /**
     * Returns {@code true} if the worker is working, {@code false} otherwise.
     */
    boolean isStarted();

    /**
     * Verifies if the ip or referrer is authorized to request the service or not.
     * Should delegate the verification to the Policy Desicion Point (PDP).
     *
     * @param ip The IP address of the requestor.
     * @param referer The referrer identifier of the requestor.
     * @return {@code True} if the requestor is authorized, {@code false} otherwise.
     */
    boolean isAuthorized(String ip, String referer);

    /**
     * Return the service URL.
     * @return String
     */
    String getServiceUrl();
}
