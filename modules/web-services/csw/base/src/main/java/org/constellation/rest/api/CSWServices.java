/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.rest.api;

import java.io.File;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.BriefNode;
import org.constellation.configuration.BriefNodeList;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.configuration.ServiceConfigurer;
import org.constellation.dto.ParameterValues;
import org.constellation.metadata.configuration.CSWConfigurer;
import org.constellation.dto.SimpleValue;
import org.w3c.dom.Node;

import static org.constellation.utils.RESTfulUtilities.ok;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
@Path("/1/CSW")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class CSWServices {

    @POST
    @Path("{id}/index/refresh")
    public Response refreshIndex(final @PathParam("id") String id, final ParameterValues values) throws Exception {
        final boolean asynchrone = values.getAsBoolean("ASYNCHRONE");
        final boolean forced     = values.getAsBoolean("FORCED");
        final CSWConfigurer conf = getConfigurer();
        final AcknowlegementType ack = conf.refreshIndex(id, asynchrone, forced);
        if (asynchrone && ack.getStatus().equals("Sucess")) {
            conf.restartInstance(id, false);
        }
        return ok(ack);
   }

    @PUT
    @Path("{id}/index/{metaID}")
    public Response AddToIndex(final @PathParam("id") String id, final @PathParam("metaID") String metaID) throws Exception {
        return ok(getConfigurer().addToIndex(id, metaID));
    }

    @DELETE
    @Path("{id}/index/{metaID}")
    public Response removeFromIndex(final @PathParam("id") String id, final @PathParam("metaID") String metaID) throws Exception {
        return ok(getConfigurer().removeFromIndex(id, metaID));
    }

    @POST
    @Path("{id}/index/stop")
    public Response stopIndexation(final @PathParam("id") String id) throws Exception {
        return ok(getConfigurer().stopIndexation(id));
    }

    /**
     * TODO change fileName into dataType parameter
     */
    @PUT
    @Path("{id}/records/{fileName}")
    public Response importRecord(final @PathParam("id") String id, final @PathParam("fileName") String fileName, final File record) throws Exception {
        return ok(getConfigurer().importRecords(id, record, fileName));
    }

    @GET
    @Path("{id}/records/{count: \\w+}-{startIndex: \\w+}")
    public Response getMetadataList(final @PathParam("id") String id, final @PathParam("count") int count, final @PathParam("startIndex") int startIndex) throws Exception {
        final List<BriefNode> nodes = getConfigurer().getMetadataList(id, count, startIndex);
        return ok(new BriefNodeList(nodes));
    }

    @DELETE
    @Path("{id}/record/{metaID}")
    public Response removeMetadata(final @PathParam("id") String id, final @PathParam("metaID") String metaID) throws Exception {
        return ok(getConfigurer().removeRecords(id, metaID));
    }

    @GET
    @Path("{id}/record/{metaID}")
    public Response getMetadata(final @PathParam("id") String id, final @PathParam("metaID") String metaID) throws Exception {
        return ok(getConfigurer().getMetadata(id, metaID));
    }

    @GET
    @Path("{id}/record/download/{metaID}")
    @Produces("application/xml")
    public Response downloadMetadata(final @PathParam("id") String id, final @PathParam("metaID") String metaID) throws Exception {
        final Node md = getConfigurer().getMetadata(id, metaID);
        return Response.ok(md, MediaType.APPLICATION_XML_TYPE).header("Content-Disposition", "attachment; filename=\"" + metaID + ".xml\"").build();
    }

    @GET
    @Path("{id}/records/count")
    public Response getMetadataCount(final @PathParam("id") String id) throws Exception {
        return ok(new SimpleValue(getConfigurer().getMetadataCount(id)));
    }

    @GET
    @Path("types")
    public Response getCSWDatasourceType() throws Exception {
        return ok(getConfigurer().getAvailableCSWDataSourceType());
    }

    private static CSWConfigurer getConfigurer() throws NotRunningServiceException {
        return (CSWConfigurer) ServiceConfigurer.newInstance(Specification.CSW);
    }
}
