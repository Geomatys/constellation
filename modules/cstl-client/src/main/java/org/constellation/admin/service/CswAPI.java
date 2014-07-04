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
package org.constellation.admin.service;

import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.BriefNodeList;
import org.constellation.configuration.StringList;
import org.constellation.dto.ParameterValues;
import org.w3c.dom.Node;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

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

    public boolean deleteMetadata(final String identifier, final String metaID) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);
        ensureNonNull("metaID",  metaID);

        final String path = "CSW/" + identifier + "/record/" + metaID;
        final AcknowlegementType ack = client.delete(path, MediaType.APPLICATION_XML_TYPE).getEntity(AcknowlegementType.class);
        return ack.getStatus().equals("Success");
    }
    
    public void deleteAllMetadata(final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);

        final String path = "CSW/" + identifier + "/records";
        client.delete(path, MediaType.APPLICATION_XML_TYPE).ensure2xxStatus();
    }
    
    public boolean metadataExist(final String identifier, final String metaID) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);
        ensureNonNull("metaID",  metaID);

        final String path = "CSW/" + identifier + "/record/exist/" + metaID;
        final AcknowlegementType ack = client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(AcknowlegementType.class);
        return "Exist".equals(ack.getStatus());
    }
    
    public Collection<String> getAvailableDataSourceType() throws HttpResponseException, IOException {

        final String path = "CSW/types";
        final StringList sl = client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StringList.class);
        return sl.getList();
    }


    public boolean refreshIndex(final String identifier, final boolean asynchrone, final boolean forced) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);

        final String path = "CSW/" + identifier + "/index/refresh";
        final ParameterValues values = new ParameterValues();
        values.getValues().put("ASYNCHRONE", Boolean.toString(asynchrone));
        values.getValues().put("FORCED",     Boolean.toString(forced));
        final AcknowlegementType ack = client.post(path, MediaType.APPLICATION_XML_TYPE, values).getEntity(AcknowlegementType.class);
        return ack.getStatus().equals("Success");
    }
    
    public boolean importMetadata(final String identifier, final File metaFile) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);

        final String path = "CSW/" + identifier + "/records/" + metaFile.getName();
        final AcknowlegementType ack = client.post(path, MediaType.APPLICATION_XML_TYPE, metaFile).getEntity(AcknowlegementType.class);
        return ack.getStatus().equals("Success");
    }
}
