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
package org.constellation.security.spring;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NOPAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {

//        if (jsonWanted(request.getHeader("Accept"))) {
//            response.setContentType("application/json");
//            Map<String, String> map = new HashMap<String, String>();
//            map.put("type", "error");
//            map.put("message", exception.getMessage());
//            com.fasterxml.jackson.databind.ObjectMapper mapper = new ObjectMapper();
//            mapper.writeValue(response.getWriter(), map);
//
//        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    }

    private boolean jsonWanted(String header) {
        return "application/json".equals(header);
    }

}