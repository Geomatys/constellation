package org.constellation.ws.rest;

import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ws.rs.ConfigurationUtilities;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * RestFull user configuration service
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Path("/1/user")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class User {

    /**
     * @return a {@link Response} which contains requester user name
     */
    @GET
    @Path("/")
    public Response user() {
        return Response.ok(ConfigurationUtilities.getUserName()).build();
    }

    /**
     * Called on login. To know if login is granted to access to server
     *
     * @return an {@link AcknowlegementType} on {@link Response} to know operation state
     */
    @GET
    @Path("/access")
    public Response access() {
        final AcknowlegementType response = new AcknowlegementType("Success", "You have access to the configuration service");
        return Response.ok(response).build();
    }

}
