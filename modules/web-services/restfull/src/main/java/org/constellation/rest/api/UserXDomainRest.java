package org.constellation.rest.api;

import org.constellation.engine.register.repository.DomainRepository;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/1/userXdomain")
public class UserXDomainRest {

    @Inject
    private DomainRepository domainRepository;

    @POST
    @Path("/{domainId}/user/{userId}")
    public Response post(@PathParam("userId") int userId,@PathParam("domainId") int domainId, Set<Integer> roles) {
        domainRepository.addUserToDomain(userId, domainId, roles);
        return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
    }
    
    @PUT
    @Path("/{domainId}/user/{userId}")
    public Response put(@PathParam("userId") int userId,@PathParam("domainId") int domainId, Set<Integer> roles) {
        domainRepository.updateUserInDomain(userId, domainId, roles);
        return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @DELETE
    @Path("/{domainId}/user/{userId}")
    public Response delete(@PathParam("userId") int userId, @PathParam("domainId") int domainId) {
        domainRepository.removeUserFromDomain(userId, domainId);
        return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

}
