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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author bgarcia
 */
public class CorsFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(CorsFilter.class.getName());

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
       // do nothing
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
    	((HttpServletResponse) response).addHeader("Access-Control-Allow-Origin", "*");
    	((HttpServletResponse) response).addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    	((HttpServletResponse) response).addHeader("Access-Control-Allow-Headers", "Content-Type");
    	
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
