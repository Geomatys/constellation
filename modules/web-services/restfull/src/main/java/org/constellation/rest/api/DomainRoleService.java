package org.constellation.rest.api;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.DomainRole;
import org.constellation.engine.register.User;
import org.constellation.engine.register.repository.DomainRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

@Path("/1/domainrole")
public class DomainRoleService {

    public static class DomainRoleWithMembers extends DomainRole {
        
        
        
        private String memberList;

        public String getMemberList() {
            return memberList;
        }

        public void setMemberList(String memberList) {
            this.memberList = memberList;
        }

            }

    @Inject
    private DomainRoleRepository domainRoleRepository;

    @Path("/")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response all(@QueryParam("withMembers") boolean withMembers) {
        if (withMembers) {
            List<DomainRoleWithMembers> result = new ArrayList<DomainRoleService.DomainRoleWithMembers>();
            Map<DomainRole, List<Pair<User, List<Domain>>>> findAllWithMembers = domainRoleRepository
                    .findAllWithMembers();

            for (Entry<DomainRole, List<Pair<User, List<Domain>>>> domainEntry : findAllWithMembers.entrySet()) {
                DomainRoleWithMembers domainRoleWithMember = new DomainRoleWithMembers();
                domainRoleWithMember.setSystem(domainEntry.getKey().isSystem());
                domainRoleWithMember.setId(domainEntry.getKey().getId());
                domainRoleWithMember.setName(domainEntry.getKey().getName());
                domainRoleWithMember.setDescription(domainEntry.getKey().getDescription());
                List<Pair<User, List<Domain>>> value = domainEntry.getValue();
                StringBuilder builder = new StringBuilder();
                boolean afterFirstUser = false;
                for (Pair<User, List<Domain>> userDomainsPair : value) {
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
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") int id) {
        DomainRole domainRole = domainRoleRepository.findOneWithPermission(id);
        if (domainRole == null)
            return Response.status(404).build();
        return Response.ok(domainRole).build();
    }

    @Path("/")
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response insert(DomainRole domainRole) {
        DomainRole saved = domainRoleRepository.save(domainRole);
        return Response.ok(saved).build();
    }

    @Path("/{id}")
    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response update(@PathParam("id") int id, DomainRole domainRole) {
        DomainRole saved = domainRoleRepository.update(domainRole);
        return Response.ok(saved).build();
    }

    @DELETE
    @Path("/{id}")
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
        return Response.noContent().build();
    }

}
