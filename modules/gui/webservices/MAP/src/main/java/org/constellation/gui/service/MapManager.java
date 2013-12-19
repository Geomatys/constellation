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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manager for WMS service operations.
 *
 * @author Benjamin Garcia (Geomatys).
 * @author Bernard Fabien (Geomatys).
 * @since 0.9
 */
public class MapManager {

    private static final Logger LOGGER = Logger.getLogger(MapManager.class.getName());
    /**
     * Constellation manager used to communicate with the Constellation server.
     */
    @Inject
    private ConstellationService cstl;

    /**
     * Loads a service layer list.
     *
     * @param serviceId   the service identifier
     * @return the {@link org.constellation.configuration.LayerList} instance
     * @throws java.io.IOException on communication error with Constellation server
     */
    public LayerList getLayers(final String serviceId) throws IOException {
        return cstl.openClient().services.getLayers(Specification.WMS, serviceId);
    }

    public void removeLayer(final String layerName, final String layerNamespace, final String serviceId, final String spec) {
        try {
            cstl.openClient().services.deleteLayer(serviceId, layerName, layerNamespace, spec);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when call web service to remove layer", e);
        }
    }
}
