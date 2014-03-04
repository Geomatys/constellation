package org.constellation.services.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.constellation.engine.register.repository.UserRepository;
import org.constellation.security.SecurityManagerHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/session")
public class SessionController {

	@Autowired
	private UserRepository userRepository;

	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public void form(HttpServletResponse response) {
		if (SecurityManagerHolder.getInstance().isAuthenticated())
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		else
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}

	@RequestMapping("/loggedin")
	public String admin(HttpServletRequest httpServletRequest) {
		String sessionId = httpServletRequest.getSession().getId();
		String adminUrl = (String) httpServletRequest.getSession()
				.getAttribute("adminUrl");
		if(adminUrl==null) {
			return "redirect:/";
		}
		return "redirect:" + adminUrl + "app/cstl?cstlSessionId=" + sessionId;
	}

	@RequestMapping("/logout")
	public @ResponseBody
	String logout(HttpServletRequest httpServletRequest) {
		HttpSession session = httpServletRequest.getSession(false);
		if (session == null)
			return "NOK";
		session.invalidate();
		return "OK";
	}

}
