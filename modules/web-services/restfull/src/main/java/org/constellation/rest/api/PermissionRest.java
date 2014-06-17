package org.constellation.rest.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.constellation.engine.register.Permission;
import org.constellation.engine.register.repository.DomainRoleRepository;

@Path("1/permission")
public class PermissionRest {
    
    @Inject
    private DomainRoleRepository domainRoleRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public List<Permission> all(){
        return domainRoleRepository.allPermission();
    }
    
}
