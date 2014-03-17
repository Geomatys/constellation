package org.constellation.services.web.controller;

import org.constellation.security.SecurityManagerHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping("/account")
public class AccountController {

	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody
	String test() {
		if(SecurityManagerHolder.getInstance().isAuthenticated())
			return "OK";
		return "NOK";
	}
	
	

}
