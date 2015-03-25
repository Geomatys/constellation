package org.constellation.rest.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.engine.register.PermissionConstants;
import org.constellation.engine.register.jooq.tables.pojos.Domain;
import org.constellation.engine.register.jooq.tables.pojos.Domainrole;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.DomainroleRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

@Component
@Path("/1/servicepermission")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class ServicePermissionRest {

    static class DomainroleWithServiceAcccess {

        private String domainRole;

        private List<String> access;

        public DomainroleWithServiceAcccess() {
        }

        public String getDomainRole() {
            return domainRole;
        }

        public void setDomainRole(String domainRole) {
            this.domainRole = domainRole;
        }

        public DomainroleWithServiceAcccess(String domainRole, List<String> access) {
            super();
            this.domainRole = domainRole;
            this.access = access;
        }

        public List<String> getAccess() {
            return access;
        }

        public void setAccess(List<String> access) {
            this.access = access;
        }

    }

    @Inject
    private DomainroleRepository domainRoleRepository;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private DomainRepository domainRepository;

    @GET
    @Path("/access")
    public Collection<DomainroleWithServiceAcccess> domainsrolesWithServiceAccess() {

        Map<Domainrole, List<Integer>> domainRolesWithPermissions = domainRoleRepository.findAllWithPermissions(
                PermissionConstants.SERVICE_READ_ACCESS_PERMISSION_ID, PermissionConstants.SERVICE_WRITE_ACCESS_PERMISSION_ID);

        return Maps.transformEntries(domainRolesWithPermissions,
                new EntryTransformer<Domainrole, List<Integer>, DomainroleWithServiceAcccess>() {

                    public DomainroleWithServiceAcccess transformEntry(Domainrole domainRole,
                            List<Integer> permissionIds) {
                        List<String> access = new ArrayList<>();
                        if (permissionIds.contains(PermissionConstants.SERVICE_READ_ACCESS_PERMISSION_ID)) {
                            access.add("READ");
                        }
                        if (permissionIds.contains(PermissionConstants.SERVICE_WRITE_ACCESS_PERMISSION_ID)) {
                            access.add("WRITE");
                        }
                        return new DomainroleWithServiceAcccess(domainRole.getName(), access);
                    }

                }).values();

    }

    @GET
    @Path("/user/{userId}/service/{serviceId}")
    public Response linkedDomains(@PathParam("userId") int userId, @PathParam("serviceId") int serviceId) {

        Set<Integer> domainsById = domainRepository
                .findUserDomainIdsWithPermission(userId, PermissionConstants.SERVICE_CREATION);
        List<LinkedDomain> result = new ArrayList<>();
        for (Entry<Domain, Boolean> e : serviceRepository.getLinkedDomains(serviceId).entrySet()) {
            Domain domain = e.getKey();
            result.add(new LinkedDomain(domain, e.getValue(), domainsById.contains(domain.getId())));
        }
        return Response.ok(result).build();
    }

}
