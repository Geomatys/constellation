/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.gui.service;

import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.LayerList;
import org.apache.sis.util.logging.Logging;

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

    private static final Logger LOGGER = Logging.getLogger("org.constellation.gui.service");
    /**
     * Constellation manager used to communicate with the Constellation server.
     */
    @Inject
    private ConstellationService cstl;

    /**
     * Loads a service layer list.
     *
     *
     * @param serviceId   the service identifier
     * @param specification
     * @return the {@link org.constellation.configuration.LayerList} instance
     * @throws java.io.IOException on communication error with Constellation server
     */
    public LayerList getLayers(final String serviceId, final Specification specification) throws IOException {
        return cstl.openClient().services.getLayers(specification, serviceId);
    }

    public void removeLayer(final String layerName, final String layerNamespace, final String serviceId, final String spec) {
        try {
            cstl.openClient().services.deleteLayer(serviceId, layerName, layerNamespace, spec);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when call web service to remove layer", e);
        }
    }
}
