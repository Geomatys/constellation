package org.constellation.rest.api;

import org.constellation.engine.register.Permission;
import org.constellation.engine.register.repository.DomainroleRepository;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.List;

@Component
@Path("/1/permission/")
@RolesAllowed("cstl-admin")
public class PermissionRest {
    
    @Inject
    private DomainroleRepository domainRoleRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Permission> all(){
        return domainRoleRepository.allPermission();
    }
    
}
