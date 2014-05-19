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
		storeCstlAdmin(request, response);

		return "redirect:../../login.html";
	}

	@RequestMapping("/loggedin")
	public String loggedin(HttpServletRequest httpServletRequest) {
		String sessionId = httpServletRequest.getSession().getId();
		String adminUrl = retrieveCstlAdmin(httpServletRequest);
		if(adminUrl==null)
			return "redirect:/";
		return "redirect:" + adminUrl + "app/cstl?cstlSessionId=" + sessionId;
	}

	@RequestMapping("/loggedout")
	public String loggedout(HttpServletRequest httpServletRequest) {

		String adminUrl = retrieveCstlAdmin(httpServletRequest);

		return "redirect:" + adminUrl;
	}

	private void storeCstlAdmin(HttpServletRequest request,
			HttpServletResponse response) {
		String adminUrl = request.getHeader("REFERER");
		if(adminUrl!=null) {
		    int i = adminUrl.indexOf("index.html");
		    if(i>0) {
		        adminUrl = adminUrl.substring(0, i);
		    }
		}
		
		Cookie cookie = new Cookie("cstlAdmin", adminUrl);
		cookie.setPath("/constellation/spring/auth/");
		response.addCookie(cookie);
	}

	private String retrieveCstlAdmin(HttpServletRequest httpServletRequest) {
		Cookie[] cookies = httpServletRequest.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if ("cstlAdmin".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

}
