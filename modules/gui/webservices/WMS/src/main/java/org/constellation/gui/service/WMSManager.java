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

import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.LayerList;
import org.constellation.dto.Service;
import org.constellation.ws.rs.ServiceType;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Manager for WMS service operations.
 *
 * @author Benjamin Garcia (Geomatys).
 * @author Bernard Fabien (Geomatys).
 * @since 0.9
 */
public class WMSManager {

    /**
     * Constellation manager used to communicate with the Constellation server.
     */
    @Inject
    private ConstellationService cstl;

    /**
     * Loads a service metadata.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return the {@link Service} instance
     */
    public Service getServiceMetadata(final String serviceId, final ServiceType serviceType) throws IOException {
        return cstl.openClient().services.getMetadata(serviceType, serviceId);
    }

    /**
     * Configures an existing service metadata.
     *
     * @param metadata    the service metadata
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return {@code true} on success, otherwise {@code false}
     */
    public boolean setServiceMetadata(final Service metadata, final ServiceType serviceType) throws IOException {
        final AcknowlegementType response = cstl.openClient().services.setMetadata(serviceType, metadata);
        return "success".equalsIgnoreCase(response.getStatus());
    }

    /**
     * Loads a service layer list.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return the {@link LayerList} instance
     */
    public LayerList getLayers(final String serviceId, final String serviceType) {
        return cstl.openServer().services.getLayers(serviceType, serviceId);
    }

    /**
     * Restarts a service.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return {@code true} on success, otherwise {@code false}
     */
    public boolean restartService(final String serviceId, final String serviceType) {
        return cstl.openServer(true).services.restartInstance(serviceType, serviceId);
    }

    /**
     * Stops a service.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return {@code true} on success, otherwise {@code false}
     */
    public boolean stopService(final String serviceId, final String serviceType) {
        return cstl.openServer(true).services.stopInstance(serviceType, serviceId);
    }

    /**
     * Starts a service.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return {@code true} on success, otherwise {@code false}
     */
    public boolean startService(final String serviceId, final String serviceType) {
        return cstl.openServer(true).services.startInstance(serviceType, serviceId);
    }
}
