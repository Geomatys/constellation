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
package org.constellation.security.spring;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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