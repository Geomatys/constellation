package org.constellation.ws.rest;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.inject.Inject;
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

import org.constellation.engine.register.DomainRole;
import org.constellation.engine.register.Permission;
import org.constellation.engine.register.repository.DomainRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

@Path("/1/domainrole")
public class DomainRoleService {

    public static class DomainRolePermissionDTO {
        private DomainRole domainRole;

        private List<Permission> permissions;

        public DomainRole getDomainRole() {
            return domainRole;
        }

        public void setDomainRole(DomainRole domainRole) {
            this.domainRole = domainRole;
        }

        public List<Permission> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<Permission> permissions) {
            this.permissions = permissions;
        }
    }

    @Inject
    private DomainRoleRepository groupRepository;

    @Path("/")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response all() {
        return Response.ok(groupRepository.findAll()).build();
    }
    
    
    @Path("/{name}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response select(@PathParam("name") String name) {
        DomainRole domainRole = groupRepository.findOneWithPermission(name);
        return Response.ok(groupRepository.findAll()).build();
    }

    @Path("/")
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response insert(DomainRole domainRole) {
        DomainRole saved = groupRepository.save(domainRole);
        return Response.ok(saved).build();
    }

    @Path("/{name}")
    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response update(@PathParam("name") String name, DomainRole domainRole) {
        domainRole.setName(name);
        DomainRole saved = groupRepository.update(domainRole);
        return Response.ok(saved).build();
    }

    @Path("/{name}")
    @DELETE
    public Response delete(@PathParam("name") String name) {
        try {
            groupRepository.delete(name);
        } catch (DataIntegrityViolationException e) {
            Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
            LOGGER.warn(e.getMessage());
            return Response.serverError().entity("Domain is in use.").build();
        }
        return Response.noContent().build();
    }

}
