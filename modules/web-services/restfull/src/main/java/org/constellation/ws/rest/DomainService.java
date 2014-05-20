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
package org.constellation.ws.rest;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.User;
import org.constellation.engine.register.repository.DomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

@Path("/1/domain")
public class DomainService {

    
    public static class DomainUserWrapper extends Domain {
        
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public DomainUserWrapper() {
        }
        
        public DomainUserWrapper(Domain domain, List<User> users) {
            setId(domain.getId());
            setName(domain.getName());
            setDescription(domain.getDescription());
            this.users = users;
        }

        List<User> users;

        public List<User> getUsers() {
            return users;
        }
        public void setUsers(List<User> users) {
            this.users = users;
        }
        
    }
    
    @Inject
    private DomainRepository domainRepository;
    
    
    @Path("/")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response  all(@QueryParam("withUsers") boolean withUsers){;
        if(withUsers) {
            List<DomainUserWrapper> result = new ArrayList<DomainService.DomainUserWrapper>();            
            List<Domain> domains = domainRepository.findAll();
            for (Domain domain : domains) {
                List<User> users = domainRepository.findUsers(domain.getId());
                result.add(new DomainUserWrapper(domain, users));
            }
            return  Response.ok(result).build();
        }
        return  Response.ok(domainRepository.findAll()).build();
    }
    

    
    @Path("/")
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response insert(Domain domain) {
        Domain saved = domainRepository.save(domain);
        return Response.ok(saved).build();
    }
    
    @Path("/{id}")
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response update(@PathParam("id") int id, Domain domain) {
        domain.setId(id);
        Domain saved = domainRepository.update(domain);
        return Response.ok(saved).build();
    }
    
    @Path("/{id}")
    @DELETE
    public Response delete(@PathParam("id") int id) {
        try {
            domainRepository.delete(id);
        }catch(DataIntegrityViolationException e) {
            Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
            LOGGER.warn(e.getMessage());
            return Response.serverError().entity("Domain is in use.").build();
        }
        return Response.noContent().build();
    }
    
    
    
}
