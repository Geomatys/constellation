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

import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.LayerList;

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
     * Loads a service layer list.
     *
     * @param serviceId   the service identifier
     * @return the {@link LayerList} instance
     * @throws IOException on communication error with Constellation server
     */
    public LayerList getLayers(final String serviceId) throws IOException {
        return cstl.openClient().services.getLayers(Specification.WMS, serviceId);
    }
}
