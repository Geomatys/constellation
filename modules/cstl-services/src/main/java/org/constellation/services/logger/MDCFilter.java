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
package org.constellation.services.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * Filter that exposes OGC service ids in log {@link MDC} <br />
 * By default it exposes /WS/{serviceType}/{serviceId}/...
 * 
 * @author olivier.nouguier@geomatys.com
 * 
 */
public class MDCFilter implements Filter {

    static final String OGC = "ogc";

    private static final String ogcServiceLogKey = "ogcServiceLog";
    
    private static final String servicePathKey = "ogcServicePath";

    
    private static final String DEFAULT_SERVLET_MAPPING = "/WS/";

    private static final String WS_MAPPING = "WSMapping";

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String servletPath;

    /**
     * Removal of jul log handlers.
     */
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    /**
     * Init filter configuration to match the proper URL (/WS/* by default).
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        String servletMapping = filterConfig.getInitParameter(WS_MAPPING);
        if (servletMapping == null) {
            servletMapping = DEFAULT_SERVLET_MAPPING;
        } else if (!servletMapping.startsWith("/")) {
            LOGGER.warn("WSMapping should start with \"/\"!");
            servletMapping = "/" + servletMapping;
        }

        servletPath = filterConfig.getServletContext().getContextPath() + servletMapping;

    }

    /**
     * Put ogc service type (wms, wmts ...) and service id in {@link MDC}. <br >
     * This information can be used afterward in logger configuration.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            String requestURI = httpServletRequest.getRequestURI();
            if (requestURI.startsWith(servletPath)) {
                String path = requestURI.substring(servletPath.length());
                //Remove jsessionid
                int jsessionid = path.indexOf(";jsessionid=");
                if(jsessionid!=-1) {
                    path = path.substring(0, jsessionid);
                }
                String[] split = path.split("/");
                if (split.length > 1) {
                    try {
                        MDC.put(OGC, "true");
                        String serviceType = split[0];
                        String serviceName = split[1];
                       
                        String log = serviceType;
                        if("admin".equals(serviceName)) {
                            //This is not a service call, but a admin console
                            serviceName = httpServletRequest.getParameter("id");
                            final String command = httpServletRequest.getParameter("request");
                            log += " " + serviceName + " (" + command + ")";
                            path = serviceType + "/" + serviceName;
                        }else {
                            log += " " + serviceName;
                        }
                        
                        MDC.put(ogcServiceLogKey, log);
                        MDC.put(servicePathKey, path);
                        chain.doFilter(request, response);
                        return;
                    } finally {
                        MDC.remove(OGC);
                        MDC.remove(ogcServiceLogKey);
                        
                        MDC.remove(servicePathKey);
                    }
                }

            } else {
                LOGGER.warn("MDCFilter is misconfigured in regard with WS mapping: " + requestURI);
            }

        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
