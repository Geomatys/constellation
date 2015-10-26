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

import org.constellation.admin.security.CstlAdminLoginConfigurationService;
import org.constellation.configuration.AppProperty;
import org.constellation.configuration.Application;
import org.constellation.token.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Properties;

@Controller
public class ConfigController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigController.class);

    private static long TOKEN_LIFE = TokenUtils.getTokenLife();

    @Inject
    private CstlAdminLoginConfigurationService cstlAdminLoginConfigurationService;

    public ConfigController() {
        LOGGER.debug("ConfigController construct");
    }

    @Inject
    @Named("build")
    private Properties buildProperties;

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
     * @param request {@code HttpServletRequest}
     * @return Map
     */
    @RequestMapping(value = "/conf", method=RequestMethod.GET)
    public @ResponseBody
    Map<Object, Object> get(final HttpServletRequest request) {
        final ServletContext servletCtxt = request.getServletContext();
        Properties properties = new Properties();
        String context;
        final String cstlConfUrl = Application.getProperty(AppProperty.CSTL_URL);
        //first check against variable if defined to override cstl url
        if (cstlConfUrl != null) {
            context = cstlConfUrl;
        } else if ("true".equals(servletCtxt.getInitParameter("cstl-uberwar"))) {
            //If run in a single war, handle the renaming of this war
            final String requestUrl = request.getRequestURL().toString();
            final String contextPath = request.getContextPath();
            context = requestUrl.substring(0, requestUrl.indexOf(contextPath) + (contextPath.length()));
        } else {
            //only in case of using both war services and admin without variable cstl.url defined.
            //the variable must be defined when using both war for deployment.
            context = "/constellation";
        }
        if (!context.endsWith("/")) {
            context += "/";
        }
        properties.put("cstl", context);
        properties.put("token.life", TOKEN_LIFE);
        properties.put("cstl.import.empty", "true".equals(servletCtxt.getInitParameter("cstl.import.empty")));
        properties.put("cstl.import.custom", "true".equals(servletCtxt.getInitParameter("cstl.import.custom")));
        properties.put("cstlLoginURL", env.getProperty("cstlLoginURL", cstlAdminLoginConfigurationService.getCstlLoginURL()));

        final String logoutURL = cstlAdminLoginConfigurationService.getCstlLogoutURL();
        if (logoutURL != null) {
            properties.put("cstlLogoutURL", env.getProperty("cstlLogoutURL", logoutURL));
        }

        final String refreshURL = cstlAdminLoginConfigurationService.getCstlRefreshURL();
        if (refreshURL != null) {
            properties.put("cstlRefreshURL", env.getProperty("cstlRefreshURL", refreshURL));
        }

        final String profileURL = cstlAdminLoginConfigurationService.getCstlProfileURL();
        if (profileURL != null) {
            properties.put("cstlProfileURL", env.getProperty("cstlProfileURL", profileURL));
        }
        return properties;
    }

    @RequestMapping(value = "/build", method=RequestMethod.GET)
    public @ResponseBody
    Properties getBuildInfo(final HttpServletRequest request) {
        return buildProperties;
    }

}
