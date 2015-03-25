package org.constellation.rest.api;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Domain;
import org.constellation.engine.register.jooq.tables.pojos.Domainrole;
import org.constellation.engine.register.jooq.tables.pojos.Permission;
import org.constellation.engine.register.repository.DomainroleRepository;
import org.constellation.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;

@Component
@Path("/1/domainrole/")
@RolesAllowed("cstl-admin")
public class DomainRoleRest {

    
    public static class DomainroleWithPermissions extends Domainrole {
        
        private List<Permission> permissions;
        
        public DomainroleWithPermissions() {
        }
        
        public DomainroleWithPermissions(Domainrole domainRole) {
        	  Util.copy(domainRole, this);
        }

        public void setPermissions(List<Permission> permissions) {
            this.permissions = permissions;
        }
        
        public List<Permission> getPermissions() {
            return permissions;
        }
        
        
    }
    
    public static class DomainroleWithMembers extends Domainrole {
        
        private String memberList;

        public DomainroleWithMembers() {
        }
        
        public DomainroleWithMembers(Domainrole domainrole) {
            Util.copy(domainrole, this);
        }

        public String getMemberList() {
            return memberList;
        }

        public void setMemberList(String memberList) {
            this.memberList = memberList;
        }

            }

    @Inject
    private DomainroleRepository domainRoleRepository;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response all(@QueryParam("withMembers") boolean withMembers) {
        if (withMembers) {
            List<DomainroleWithMembers> result = new ArrayList<DomainroleWithMembers>();
            Map<Domainrole, List<Pair<CstlUser, List<Domain>>>> findAllWithMembers = domainRoleRepository
                    .findAllWithMembers();

            for (Entry<Domainrole, List<Pair<CstlUser, List<Domain>>>> domainEntry : findAllWithMembers.entrySet()) {
                DomainroleWithMembers domainRoleWithMember = new DomainroleWithMembers(domainEntry.getKey());
                List<Pair<CstlUser, List<Domain>>> value = domainEntry.getValue();
                StringBuilder builder = new StringBuilder();
                boolean afterFirstUser = false;
                for (Pair<CstlUser, List<Domain>> userDomainsPair : value) {
                    if (afterFirstUser)
                        builder.append(", ");
                    else
                        afterFirstUser = true;
                    builder.append(userDomainsPair.getLeft().getLogin());
                    builder.append(" (");
                    boolean afterFistDomain = false;
                    List<Domain> right = userDomainsPair.getRight();
                    for (Domain domain : right) {
                        if (afterFistDomain)
                            builder.append(", ");
                        else
                            afterFistDomain = true;
                        builder.append(domain.getName());
                    }
                    builder.append(')');
                }
                domainRoleWithMember.setMemberList(builder.toString());
                result.add(domainRoleWithMember);

            }

            return Response.ok(result).build();
        }
        return Response.ok(domainRoleRepository.findAll()).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") int id) {
        Optional<Pair<Domainrole, List<Permission>>> opt = domainRoleRepository.findOneWithPermission(id);
        if(opt.isPresent()) {
            DomainroleWithPermissions domainroleWithPermissions = new DomainroleWithPermissions(opt.get().getKey());          
            domainroleWithPermissions.setPermissions(opt.get().getValue());
            return Response.ok(domainroleWithPermissions).build();
        }
        return Response.status(404).build();
        
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public Response insert(DomainroleWithPermissions domainRole) {
        Domainrole saved = domainRoleRepository.createWithPermissions(domainRole, domainRole.getPermissions());
        return Response.ok(saved).build();
    }

    @Path("{id}")
    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public Response update(@PathParam("id") int id, DomainroleWithPermissions domainRole) {
        Domainrole saved = domainRoleRepository.updateWithPermissions(domainRole, domainRole.getPermissions());
        return Response.ok(saved).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") int id) {
        try {
            if(domainRoleRepository.delete(id) == 0) {
                return Response.serverError().entity("admin.domainrole.delete.failed.").build();
            }
        } catch (DataIntegrityViolationException e) {
            Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
            LOGGER.warn(e.getMessage());
            return Response.serverError().entity("Domain is in use.").build();
        }
        return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

}
