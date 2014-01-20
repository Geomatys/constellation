/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
