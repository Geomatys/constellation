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
package org.constellation.admin.service;

import org.constellation.configuration.BriefNodeList;
import org.w3c.dom.Node;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * @author Cédric Briançon (Geomatys)
 */
public final class CswAPI {
    /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     * Creates a {@link ServicesAPI} instance.
     *
     * @param client the client to use
     */
    CswAPI(final ConstellationClient client) {
        this.client = client;
    }

    public BriefNodeList getMetadataList(final String identifier, final int count, final int startIndex) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);

        final String path = "CSW/" + identifier + "/records/" + count + "-" + startIndex;
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(BriefNodeList.class);
    }

    public Node getMetadata(final String identifier, final String metaID) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);
        ensureNonNull("metaID",  metaID);

        final String path = "CSW/" + identifier + "/record/" + metaID;
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(Node.class);
    }

    public void deleteMetadata(final String identifier, final String metaID) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);
        ensureNonNull("metaID",  metaID);

        final String path = "CSW/" + identifier + "/record/" + metaID;
        client.delete(path, MediaType.APPLICATION_XML_TYPE).ensure2xxStatus();
    }

}
