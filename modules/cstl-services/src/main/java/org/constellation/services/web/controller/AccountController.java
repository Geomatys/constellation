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

import javax.servlet.http.HttpServletResponse;

import org.constellation.rest.api.SessionData;
import org.constellation.security.SecurityManagerHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private SessionData sessionData;
    
	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody
	SessionData test(HttpServletResponse httpServletResponse) {
		if(SecurityManagerHolder.getInstance().isAuthenticated())
			return sessionData;
		httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		return null;
	}
	
	

}
