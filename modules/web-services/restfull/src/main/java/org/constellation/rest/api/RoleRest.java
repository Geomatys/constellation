package org.constellation.rest.api;

import org.constellation.engine.register.repository.RoleRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created with IntelliJ IDEA.
 * User: laurent
 * Date: 07/05/15
 * Time: 14:28
 * Geomatys
 */
@Component
@Named
@Path("/1/role/")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class RoleRest {

    @Inject
    private RoleRepository roleRepository;

    @GET
    public Response getAll(){
        return Response.ok(roleRepository.findAll()).build();
    }
}
