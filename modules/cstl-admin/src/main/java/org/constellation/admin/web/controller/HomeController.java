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
package org.constellation.admin.web.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String cstl(HttpServletRequest request, HttpServletResponse response, @RequestParam("cstlSessionId") String csltSessionId) {
    	Cookie cookie = new Cookie("cstlSessionId", csltSessionId);
    	cookie.setPath(request.getContextPath());
		response.addCookie(cookie);
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
