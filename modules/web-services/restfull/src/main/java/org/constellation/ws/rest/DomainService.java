/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.ws.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.User;
import org.constellation.engine.register.repository.DomainRepository;

@Path("/1/domain")
public class DomainService {

    
    public static class DomainUserWrapper implements Serializable {
        
        public DomainUserWrapper() {
            // TODO Auto-generated constructor stub
        }
        
        public DomainUserWrapper(Domain domain, List<User> users) {
            this.domain = domain;
            this.users = users;
        }
        Domain domain;
        List<User> users;
        public Domain getDomain() {
            return domain;
        }
        public void setDomain(Domain domain) {
            this.domain = domain;
        }
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
    public Response  all(){
        List<DomainUserWrapper> result = new ArrayList<DomainService.DomainUserWrapper>();
        List<Domain> domains = domainRepository.findAll();
        for (Domain domain : domains) {
            List<User> users = domainRepository.findUsers(domain.getId());
            result.add(new DomainUserWrapper(domain, users));
        }
        return  Response.ok(result).build();

    }
    
    
    
}
