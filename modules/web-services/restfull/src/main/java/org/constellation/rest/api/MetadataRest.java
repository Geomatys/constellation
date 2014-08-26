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

package org.constellation.rest.api;

import org.apache.sis.util.logging.Logging;
import org.constellation.admin.MetadataBusiness;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static org.constellation.utils.RESTfulUtilities.ok;

/**
 * RESTful API for metadata.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Path("/1/domain/{domainId}/metadata/")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class MetadataRest {

    private static final Logger LOGGER = Logging.getLogger(MetadataRest.class);

    @Inject
    private MetadataBusiness metadataBusiness;

    @GET
    @Path("all/{includeService}")
    public Response getAllMetadata(final @PathParam("includeService") boolean includeService) throws Exception {
        return ok(metadataBusiness.getAllMetadata(includeService));
    }

}
