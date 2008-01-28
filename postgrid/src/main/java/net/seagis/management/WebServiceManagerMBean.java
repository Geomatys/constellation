/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.management;


/**
 * An interface declaring the exposed methods on remote JMX.
 *
 * @author Guilhem Legal
 */
public interface WebServiceManagerMBean {
    /**
     * Flush the cache content.
     */
    public void flush();

    /**
     * Returns the number of workers.
     */
    public int workerCount();

    /**
     * Returns the time in seconds required for creating the last image.
     */
    public String lastImageCreationDelay();
}
