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


/**
 * Manages {@link WebServiceWorker} on remote JMX.
 *
 * @version $Id$
 * @author Guilhem Legal
 */
public interface WebServiceManagerMBean {
    /**
     * Returns the number of workers.
     */
    int workerCount();

    /**
     * Flush the cache content.
     */
    void flush();
    
    /**
     * Scan the plugin used by the jvm.
     */
    void scanPlugin();
    
    /**
     *  Display the current wcs formats accepted.
     */
    String acceptedFormats();
}
