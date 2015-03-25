/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.rest.api;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Domain;
import org.constellation.engine.register.jooq.tables.pojos.Domainrole;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("/1/domain/")
@RolesAllowed("cstl-admin")
public class DomainRest {

    public static class DomainWithUsers extends Domain {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public DomainWithUsers() {
        }

        public DomainWithUsers(Domain domain, List<CstlUser> users) {
            super(domain.getId(), domain.getName(), domain.getDescription(), domain.getSystem());
            this.users = users;
        }

        List<CstlUser> users;

        public List<CstlUser> getUsers() {
            return users;
        }

        public void setUsers(List<CstlUser> users) {
            this.users = users;
        }

    }
    
    public static class UserWithDomainRoles  {
        private CstlUser user;
        private List<Domainrole> domainRoles;

        public UserWithDomainRoles(CstlUser user, List<Domainrole> domainRoles) {
            this.user = user;
            this.domainRoles = domainRoles; 
        }

        public List<Domainrole> getDomainRoles() {
            return domainRoles;
        }

        public void setDomainRoles(List<Domainrole> domainRoles) {
            this.domainRoles = domainRoles;
        }

        public Integer getId() {
            return user.getId();
        }

        public void setId(Integer id) {
            user.setId(id);
        }

        public String getLogin() {
            return user.getLogin();
        }

        public void setLogin(String login) {
            user.setLogin(login);
        }

        public String getPassword() {
            return user.getPassword();
        }

        public void setPassword(String password) {
            user.setPassword(password);
        }

        public String getLastname() {
            return user.getLastname();
        }

        public void setLastname(String lastname) {
            user.setLastname(lastname);
        }

        public String getFirstname() {
            return user.getFirstname();
        }

        public int hashCode() {
            return user.hashCode();
        }

        public void setFirstname(String firstname) {
            user.setFirstname(firstname);
        }

        public String getEmail() {
            return user.getEmail();
        }

        public void setEmail(String email) {
            user.setEmail(email);
        }

        public String toString() {
            return user.toString();
        }

        public boolean equals(Object obj) {
            return user.equals(obj);
        }
        
        
        
        
    }
        

    @Inject
    private DomainRepository domainRepository;
    
    @Inject
    private UserRepository userRepository;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response all(@QueryParam("withMembers") boolean withMembers,
            @DefaultValue("0") @QueryParam("userId") int userId) {
        List<Domain> domains;
        if (userId == 0)
            domains = domainRepository.findAll();
        else
            domains = domainRepository.findAllByUserId(userId);
        if (withMembers) {
            List<DomainWithUsers> result = new ArrayList<DomainWithUsers>();
            for (Domain domain : domains) {
                List<CstlUser> users = userRepository.findUsersByDomainId(domain.getId());
                result.add(new DomainWithUsers(domain, users));
            }
            return Response.ok(result).build();
        }
        return Response.ok(domains).build();
    }

    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response get(@PathParam("id") int id) {
        Domain domain = domainRepository.findOne(id);
        if (domain == null)
            return Response.status(404).build();
        return Response.ok(domain).build();

    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public Response insert(Domain domain) {
        Domain saved = domainRepository.save(domain);
        return Response.ok(saved).build();
    }

    @PUT
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public Response update(@PathParam("id") int id, Domain domain) {
        domain.setId(id);
        Domain saved = domainRepository.update(domain);
        return Response.ok(saved).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") int id) {
        try {
            domainRepository.delete(id);
        } catch (DataIntegrityViolationException e) {
            Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
            LOGGER.warn(e.getMessage());
            return Response.serverError().entity("Domain is in use.").build();
        }
        return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("members/{id}")
    public Response members(@PathParam("id") int domainId) {
        Map<CstlUser, List<Domainrole>> findUsersWithDomainRoles = userRepository.findUsersWithDomainRoles(domainId);
        
        List<UserWithDomainRoles> result = new ArrayList<UserWithDomainRoles>();
        
        for (Map.Entry<CstlUser, List<Domainrole>> e : findUsersWithDomainRoles.entrySet()) {
            result.add(new UserWithDomainRoles(e.getKey(), e.getValue()));
        }
        
        return Response.ok(result).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("nonmembers/{id}")
    public Response notMembers(@PathParam("id") int domainId) {
        return Response.ok(userRepository.findUsersNotInDomain(domainId)).build();
    }

}
