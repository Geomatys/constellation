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

import java.security.Principal;
import java.util.Enumeration;

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

import org.constellation.database.api.UserWithRole;
import org.constellation.database.api.jooq.tables.pojos.CstlUser;
import org.constellation.database.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import org.constellation.token.TokenUtils;

/**
 * RestFull user configuration service
 *
 * @author Olivier NOUGUIER (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Named
@Path("/1/session")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class SessionRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionRest.class);

    @Inject
    private UserRepository userRepository;

    @GET
    @Path("/logout")
    public Response findOne(@PathParam("id") String login, @Context HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null)
            session.invalidate();
        return Response.ok("OK").build();
    }

    @GET
    @Path("/account")
    public Response account(@Context HttpServletRequest req) {
        String token = TokenUtils.extractAccessToken(req);
        
        String username = null;
        if (token != null) {
            username = TokenUtils.getUserNameFromToken(token);
        } 
        
        if (username == null || username.isEmpty()) {
            Principal userPrincipal = req.getUserPrincipal();

            if (userPrincipal == null) {
                LOGGER.warn("No token in request");

                StringBuilder builder = new StringBuilder();
                for (Enumeration<String> headerNames = req.getHeaderNames(); headerNames.hasMoreElements(); /* NO-OPS */) {
                    String header = headerNames.nextElement();
                    builder.append(header).append(':').append(req.getHeader(header));
                }
                LOGGER.warn(builder.toString());
                return Response.status(401).build();
            }
        }
        return userRepository.findOneWithRole(username)
                .transform(new Function<UserWithRole, Response>() {
                    @Override
                    public Response apply(UserWithRole domainUser) {
                    	domainUser.setPassword("*******");
                        return Response.ok(domainUser).build();
                    }
                }).or(Response.status(404).build());
    }


}
