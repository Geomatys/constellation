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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.configuration.AcknowlegementType;
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
public class UserService implements IUserService {


    @Inject
    private UserRepository userRepository;
    
   
    /*
     * (non-Javadoc)
     * 
     * @see org.constellation.ws.rest.IUserService#findOne(java.lang.String)
     */
    @Override
    @GET
    @Path("/{id}")
    public Response findOne(@PathParam("id") String login) {
        return Response.ok(userRepository.all()).build();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.constellation.ws.rest.IUserService#findAll(java.lang.String)
     */
    @Override
    @GET
    @Path("/")
    public Response findAll(@PathParam("id") String login) {
        return Response.ok(userRepository.all()).build();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.constellation.ws.rest.IUserService#delete(java.lang.String)
     */
    @Override
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        userRepository.delete(id);
        return Response.noContent().build();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.constellation.ws.rest.IUserService#post(org.constellation.engine.
     * register.UserDTO)
     */
    @Override
    @POST
    @Path("/")
    @Transactional
    public Response post(User userDTO) {
        if (StringUtils.hasText(userDTO.getPassword()))
            userDTO.setPassword(StringUtilities.MD5encode(userDTO.getPassword()));

        userRepository.insert(userDTO);

        return Response.ok(userDTO).build();
    }
    
    
    @Override
    @PUT
    @Path("/")
    @Transactional
    public Response put(User userDTO) {
        if (StringUtils.hasText(userDTO.getPassword()))
            userDTO.setPassword(StringUtilities.MD5encode(userDTO.getPassword()));

        userRepository.update(userDTO);

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
