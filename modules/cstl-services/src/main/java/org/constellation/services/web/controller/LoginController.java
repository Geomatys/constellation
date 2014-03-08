package org.constellation.services.web.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/auth")
public class LoginController {

	@RequestMapping(value="/form", method = RequestMethod.GET)
	public String get(HttpServletRequest request, HttpServletResponse response) {
		String adminUrl = request.getHeader("REFERER");
		
		Cookie cookie = new Cookie("cstlAdmin", adminUrl);
    	cookie.setPath("/constellation/spring/auth/");
		response.addCookie(cookie);

		return "redirect:../../login.html";
	}
	

	@RequestMapping("/loggedin")
	public String loggedin(HttpServletRequest httpServletRequest) {
		String sessionId = httpServletRequest.getSession().getId();
		String adminUrl = (String) httpServletRequest.getSession()
				.getAttribute("adminUrl");
		if (adminUrl == null) {
			adminUrl = "http://localhost:8080/cstl-admin/";
		}
		return "redirect:" + adminUrl + "app/cstl?cstlSessionId=" + sessionId;
	}

	@RequestMapping("/loggedout")
	public String loggedout(HttpServletRequest httpServletRequest) {

		Cookie[] cookies = httpServletRequest.getCookies();
		String adminUrl = null;
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if ("cstlAdmin".equals(cookie.getName())) {
					adminUrl = cookie.getValue();
				}
			}
		}
		if (adminUrl == null)
			adminUrl = "http://localhost:8080/cstl-admin/";

		return "redirect:" + adminUrl;
	}

}
