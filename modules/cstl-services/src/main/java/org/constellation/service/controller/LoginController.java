package org.constellation.service.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.constellation.engine.register.repository.UserRepository;
import org.constellation.security.SecurityManagerHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    
    @RequestMapping(value="/status", method = RequestMethod.GET)
    public
    void form(HttpServletResponse response) {
    	if(SecurityManagerHolder.getInstance().isAuthenticated())
    	   response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    	else
    		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    
    @RequestMapping("/loggedin")
	public String admin(HttpServletRequest httpServletRequest) {
    	String sessionId = httpServletRequest.getSession().getId();
		return "redirect:http://localhost:8080/cstl-admin/app/cstl?cstlSessionId="+sessionId;
	}
}
