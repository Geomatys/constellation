/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.service.logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.bridge.SLF4JBridgeHandler;

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
     * Init filter configuratin to match the proper URL (/WS/* by default).
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
     * This information can be used afterward in logger.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            String requestURI = httpServletRequest.getRequestURI();
            if (requestURI.startsWith(servletPath)) {
                String path = requestURI.substring(servletPath.length());
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
