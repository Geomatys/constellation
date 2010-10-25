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
import javax.ws.rs.core.UriInfo;

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
     * Initialize the {@see #uriContext} information.
     */
    void initUriContext(final UriInfo uriInfo);

    /**
     * Set The Logging level for all the info message in the worker
     * 
     * @param logLevel
     */
    void setLogLevel(Level logLevel);
}
