package org.constellation.services.web.controller.admin;

import javax.inject.Inject;

import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("adminSessionController")
@RequestMapping("/admin/session")
public class SessionController {

	@Inject
	private SessionRegistry registry;
	
	@RequestMapping(method=RequestMethod.GET)
	public @ResponseBody int countPrincipals(){
		
		return registry.getAllPrincipals().size();
	}
	
	
	@RequestMapping(value="/test", method=RequestMethod.GET)
	public @ResponseBody int countSessions(){
		return registry.getAllPrincipals().size();
	}
	
	
	
	
}
