package org.constellation.rest.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.DomainRole;
import org.constellation.engine.register.Permission;
import org.constellation.engine.register.repository.DomainRoleRepository;
import org.constellation.engine.register.repository.ServiceRepository;

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

        public LinkedDomain(Domain key, Boolean value) {
            super(key.getId(), key.getName(), key.getDescription());
            this.linked = value;
        }

        public boolean isLinked() {
            return linked;
        }

        public void setLinked(boolean linked) {
            this.linked = linked;
        }
    }

    @Inject
    private DomainRoleRepository domainRoleRepository;

    @Inject
    private ServiceRepository serviceRepository;

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
    public List<LinkedDomain> linkedDomains(@PathParam("userId") int userId, @PathParam("serviceId") int serviceId) {

        List<LinkedDomain> result = new ArrayList<>();
        for (Entry<Domain, Boolean> e : serviceRepository.getLinkedDomains(serviceId).entrySet()) {
            result.add(new LinkedDomain(e.getKey(), e.getValue()));
        }
        return result;
    }

}
