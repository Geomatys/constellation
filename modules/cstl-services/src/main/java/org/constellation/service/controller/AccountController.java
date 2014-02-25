package org.constellation.service.controller;

import org.constellation.engine.register.User;
import org.constellation.engine.register.UserDTO;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.security.SecurityManagerHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping("/account")
public class AccountController {

	@Autowired
	private UserRepository userRepository;

	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody
	String test() {
		if(SecurityManagerHolder.getInstance().isAuthenticated())
			return "OK";
		return "NOK";
	}

}
