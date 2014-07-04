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
package org.constellation.admin.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Home Controller
 */
@Controller
public class HomeController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        return "index";
    }
    
    @RequestMapping(value = "/cstl", method = RequestMethod.GET)
    public String cstl(HttpServletRequest request, HttpServletResponse response, @RequestParam("cstlSessionId") String csltSessionId, @RequestParam("cstlActiveDomainId") int cstlActiveDomainId, @RequestParam("cstlUserId") int cstlUserId) {
    	Cookie cookie = new Cookie("cstlSessionId", csltSessionId);
    	cookie.setPath(request.getContextPath());
		response.addCookie(cookie);
		
		Cookie activeDomainId = new Cookie("cstlActiveDomainId", String.valueOf(cstlActiveDomainId));
		activeDomainId.setPath(request.getContextPath());
        response.addCookie(activeDomainId);
        
        Cookie userId = new Cookie("cstlUserId", String.valueOf(cstlUserId));
        userId.setPath(request.getContextPath());
        response.addCookie(userId);
        
		
        return "redirect:/admin.html";
    }
    
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
    	request.getSession().invalidate();
    	Cookie cookie = new Cookie("cstlSessionId", "");
    	cookie.setPath("/cstl-admin");
		response.addCookie(cookie);
        return "redirect:/";
    }
    
}
