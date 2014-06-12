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

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.DomainRole;
import org.constellation.engine.register.DomainUser;
import org.constellation.engine.register.Permission;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.DomainRoleRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.engine.register.repository.UserRepository;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

@Path("1/servicepermission")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class ServicePermissionRest {

    static class DomainRoleWithServiceAcccess {

        private String domainRole;

        private List<String> access;

        public DomainRoleWithServiceAcccess() {
        }

        public String getDomainRole() {
            return domainRole;
        }

        public void setDomainRole(String domainRole) {
            this.domainRole = domainRole;
        }

        public DomainRoleWithServiceAcccess(String domainRole, List<String> access) {
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

    public static class LinkedDomain extends Domain {
        private boolean linked;
        private boolean canPublish;

        public LinkedDomain(Domain key, boolean linked, boolean canPublish) {
            super(key.getId(), key.getName(), key.getDescription(), key.isSystem());
            this.linked = linked;
            this.setCanPublish(canPublish);
        }

        public boolean isLinked() {
            return linked;
        }

        public void setLinked(boolean linked) {
            this.linked = linked;
        }

        public boolean isCanPublish() {
            return canPublish;
        }

        public void setCanPublish(boolean canPublish) {
            this.canPublish = canPublish;
        }
    }

    @Inject
    private DomainRoleRepository domainRoleRepository;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private DomainRepository domainRepository;

    @GET
    @Path("/access")
    public Collection<DomainRoleWithServiceAcccess> domainsrolesWithServiceAccess() {

        Map<DomainRole, List<Integer>> domainRolesWithPermissions = domainRoleRepository.findAllWithPermissions(
                Permission.SERVICE_READ_ACCESS_PERMISSION_ID, Permission.SERVICE_WRITE_ACCESS_PERMISSION_ID);

        return Maps.transformEntries(domainRolesWithPermissions,
                new EntryTransformer<DomainRole, List<Integer>, DomainRoleWithServiceAcccess>() {

                    public DomainRoleWithServiceAcccess transformEntry(DomainRole domainRole,
                            List<Integer> permissionIds) {
                        List<String> access = new ArrayList<>();
                        if (permissionIds.contains(Permission.SERVICE_READ_ACCESS_PERMISSION_ID)) {
                            access.add("READ");
                        }
                        if (permissionIds.contains(Permission.SERVICE_WRITE_ACCESS_PERMISSION_ID)) {
                            access.add("WRITE");
                        }
                        return new DomainRoleWithServiceAcccess(domainRole.getName(), access);
                    }

                }).values();

    }

    @GET
    @Path("/user/{userId}/service/{serviceId}")
    public Response linkedDomains(@PathParam("userId") int userId, @PathParam("serviceId") int serviceId) {

        Set<Integer> domainsById = domainRepository
                .findUserDomainIdsWithPermission(userId, Permission.SERVICE_CREATION);
        List<LinkedDomain> result = new ArrayList<>();
        for (Entry<Domain, Boolean> e : serviceRepository.getLinkedDomains(serviceId).entrySet()) {
            Domain domain = e.getKey();
            result.add(new LinkedDomain(domain, e.getValue(), domainsById.contains(domain.getId())));
        }
        return Response.ok(result).build();
    }

}
