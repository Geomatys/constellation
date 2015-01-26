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
package org.constellation.admin.web.controller;

import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.constellation.admin.security.CstlAdminLoginConfigurationService;
import org.constellation.token.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/conf")
public class ConfigController {
	
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigController.class);
    
    private static long TOKEN_LIFE = TokenUtils.getTokenLife();

    @Inject
    private CstlAdminLoginConfigurationService cstlAdminLoginConfigurationService;
    
    public ConfigController() {
         LOGGER.info("***** ConfigController contruct *****");
    }
    
    
	@Inject
	private Environment env;
	/**
	 * Resolve the Constellation service webapp context.
	 * It will return:
	 * <ul>
	 *   <li>-Dcstl.url</li>
	 *   <li>/constellation</li>
	 * </ul>
	 * Current webapp context if running the same webapp (cstl-uberwar) 
	 * @param request
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET)
	public @ResponseBody
	Map<Object, Object> get(HttpServletRequest request) {
		Properties properties = new Properties();
		String context;
		if("true".equals(request.getServletContext().getInitParameter("cstl-uberwar"))) {
		    //If run in a single war, handle the renaming of this war
		    context = request.getContextPath();
		}else {
		    context = env.getProperty("cstl.url", "/constellation");
		}
		if(!context.endsWith("/")) {
		    context += "/";
		}
		properties.put("cstl", context);
		properties.put("token.life", TOKEN_LIFE);
		properties.put("cstlLoginURL", env.getProperty("cstlLoginURL", cstlAdminLoginConfigurationService.getCstlLoginURL()));
		return properties;
	}

}
