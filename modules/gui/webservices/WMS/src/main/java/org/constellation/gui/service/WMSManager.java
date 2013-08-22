/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.gui.service;

import org.constellation.configuration.LayerList;
import org.constellation.dto.Service;

import javax.inject.Inject;

/**
 * Manager for WMS service operations.
 *
 * @author Benjamin Garcia (Geomatys).
 * @author Bernard Fabien (Geomatys).
 * @since 0.9
 */
public final class WMSManager {

    /**
     * Constellation manager used to communicate with the Constellation server.
     */
    @Inject
    private ConstellationService cstl;

    /**
     * Loads a service metadata.
     *
     * @param serviceName the service name
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return the {@link Service} instance
     */
    public Service getServiceMetadata(final String serviceName, final String serviceType) {
        return cstl.openServer().services.getMetadata(serviceType, serviceName);
    }

    /**
     * Loads a service layer list.
     *
     * @param serviceName the service name
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return the {@link LayerList} instance
     */
    public LayerList getLayers(final String serviceName, final String serviceType) {
        return cstl.openServer().services.getLayers(serviceType, serviceName);
    }

    /**
     * Restarts a service.
     *
     * @param serviceName the service name
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return {@code true} on success, otherwise {@code false}
     */
    public boolean restartService(final String serviceName, final String serviceType) {
        return cstl.openServer(true).services.restartInstance(serviceType, serviceName);
    }

    /**
     * Stops a service.
     *
     * @param serviceName the service name
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return {@code true} on success, otherwise {@code false}
     */
    public boolean stopService(final String serviceName, final String serviceType) {
        return cstl.openServer(true).services.stopInstance(serviceType, serviceName);
    }

    /**
     * Starts a service.
     *
     * @param serviceName the service name
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return {@code true} on success, otherwise {@code false}
     */
    public boolean startService(final String serviceName, final String serviceType) {
        return cstl.openServer(true).services.startInstance(serviceType, serviceName);
    }
}
