/*
 * Constellation - An open source and standard compliant SDI
 *      http://www.constellation-sdi.org
 *   (C) 2009-2013, Geomatys
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details..
 */

package org.constellation.filter;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author bgarcia
 * @author Olivier NOUGUIER
 */
public class CorsFilter implements Filter {

    private Pattern EXCUSION_PATTERN;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        String exclude = filterConfig.getInitParameter("exclude");
        if (exclude != null)
            EXCUSION_PATTERN = Pattern.compile(filterConfig.getServletContext().getContextPath() +  exclude);
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if (EXCUSION_PATTERN == null || !EXCUSION_PATTERN.matcher(httpServletRequest.getRequestURI()).matches())
            httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");

        httpServletResponse.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpServletResponse.addHeader("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equals(httpServletRequest.getMethod()))
            httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
        else
            chain.doFilter(request, response);

    }

    @Override
    public void destroy() {
        // do nothing
    }
}
