package org.constellation.rest.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.constellation.engine.register.repository.ServiceRepository;
import org.springframework.stereotype.Component;

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
    private ServiceRepository serviceRepository;



}
