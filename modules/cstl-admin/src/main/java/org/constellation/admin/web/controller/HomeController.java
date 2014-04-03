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
