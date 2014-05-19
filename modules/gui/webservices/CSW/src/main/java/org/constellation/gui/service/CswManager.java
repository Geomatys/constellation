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

import org.constellation.configuration.BriefNodeList;
import org.w3c.dom.Node;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class CswManager {
    /**
     * Constellation manager used to communicate with the Constellation server.
     */
    @Inject
    private ConstellationService cstl;

    /**
     * Get metadata list for the given CSW instance.
     *
     * @param serviceId the service identifier
     * @param count number of items to keep
     * @param startIndex index for first item in the results
     * @return the {@link List} of metadata
     * @throws IOException on communication error with Constellation server
     */
    public BriefNodeList getMetadataList(final String serviceId, final int count, final int startIndex) throws IOException {
        return cstl.openClient().csw.getMetadataList(serviceId, count, startIndex);
    }

    /**
     * Get the metadata in the given CSW instance for the specified identifier.
     *
     * @param serviceId the service identifier
     * @param metaID the metadata identifier
     * @return the metadata as {@link Node}
     * @throws IOException on communication error with Constellation server
     */
    public Node getMetadata(final String serviceId, final String metaID) throws IOException {
        return cstl.openClient().csw.getMetadata(serviceId, metaID);
    }

    /**
     * Delete a metadata in the given CSW instance.
     *
     * @param serviceId the service identifier
     * @param metaID the metadata identifier
     * @throws IOException on communication error with Constellation server
     */
    public void deleteMetadata(final String serviceId, final String metaID) throws IOException {
        cstl.openClient().csw.deleteMetadata(serviceId, metaID);
    }
}
