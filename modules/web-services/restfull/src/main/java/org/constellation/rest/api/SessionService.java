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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.engine.register.DomainUser;
import org.constellation.engine.register.repository.UserRepository;

import com.google.common.base.Function;

/**
 * RestFull user configuration service
 * 
 * @author Olivier NOUGUIER (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Named
@Path("/1/session")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class SessionService {

    @Inject
    private UserRepository userRepository;
    
	@GET
	@Path("/logout")
	public Response findOne(@PathParam("id") String login, @Context HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if(session!=null)
			session.invalidate();
		return Response.ok("OK").build();
	}
	
	 @GET
	    @Path("/account")
	    public Response account(@Context HttpServletRequest req) {
	        return userRepository.findOneWithRolesAndDomains(req.getUserPrincipal().getName())
	                .transform(new Function<DomainUser, Response>() {
	                    @Override
	                    public Response apply(DomainUser domainUser) {
	                        return Response.ok(domainUser).build();
	                    }
	                }).or(Response.status(404).build());
	    }


}
