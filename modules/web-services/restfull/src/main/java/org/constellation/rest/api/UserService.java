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

import javax.inject.Inject;
import javax.inject.Named;
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

import org.constellation.configuration.AcknowlegementType;
import org.constellation.engine.register.DomainUser;
import org.constellation.engine.register.User;
import org.constellation.engine.register.repository.UserRepository;
import org.geotoolkit.util.StringUtilities;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * RestFull user configuration service
 * 
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Named
@Path("/1/user")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class UserService  {


    @Inject
    private UserRepository userRepository;
    
    @GET
    @Path("/")
    public Response findAll(@QueryParam("withDomainAndRoles") boolean withDomainAndRole) {
        if(withDomainAndRole) {
            return Response.ok(userRepository.findAllWithDomainAndRole()).build();
        }
        return Response.ok(userRepository.findAll()).build();
    }

    @GET
    @Path("/{id}")
    public Response findOne(@PathParam("login") String login) {
        return Response.ok(userRepository.findOneWithRolesAndDomains(login)).build();
    }


    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        if(userRepository.isLastAdmin(id))
            return Response.serverError().entity("admin.user.last.admin").build();
        userRepository.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/")
    @Transactional
    public Response post(DomainUser userDTO) {
        if (StringUtils.hasText(userDTO.getPassword()))
            userDTO.setPassword(StringUtilities.MD5encode(userDTO.getPassword()));

        userRepository.insert(userDTO, userDTO.getRoles());

        return Response.ok(userDTO).build();
    }
    
    
    @PUT
    @Path("/")
    @Transactional
    public Response put(DomainUser userDTO) {
        userRepository.update(userDTO, userDTO.getRoles());

        return Response.ok(userDTO).build();
    }

    /**
     * Called on login. To know if login is granted to access to server
     * 
     * @return an {@link AcknowlegementType} on {@link Response} to know
     *         operation state
     */
    @GET
    @Path("/access")
    public Response access() {
        final AcknowlegementType response = new AcknowlegementType("Success",
                "You have access to the configuration service");
        return Response.ok(response).build();
    }

}
