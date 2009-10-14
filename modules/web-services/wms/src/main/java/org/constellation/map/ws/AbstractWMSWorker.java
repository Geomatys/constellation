/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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
package org.constellation.map.ws;

//J2SE dependencies
import com.sun.jersey.api.core.HttpContext;
import javax.servlet.ServletContext;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * Abstract definition of a {@code Web Map Service} worker called by a facade
 * to perform the logic for a particular WMS instance.
 *
 * @version $Id$
 * 
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractWMSWorker implements WMSWorker {

    /**
     * Contains information about the HTTP exchange of the request, for instance, 
     * the HTTP headers.
     */
    private HttpContext httpContext = null;
    /**
     * Contains authentication information related to the requesting principal.
     */
    private SecurityContext securityContext = null;
    /**
     * Defines a set of methods that a servlet uses to communicate with its servlet container,
     * for example, to get the MIME type of a file, dispatch requests, or write to a log file.
     */
    private ServletContext servletContext = null;
    /**
     * Contains the request URI and therefore any  KVP parameters it may contain.
     */
    private UriInfo uriContext = null;
    
    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void initUriContext(final UriInfo uriInfo){
        uriContext = uriInfo;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void initHTTPContext(final HttpContext httpCtxt){
        httpContext = httpCtxt;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void initServletContext(final ServletContext servCtxt){
        servletContext = servCtxt;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void initSecurityContext(final SecurityContext secCtxt){
        securityContext = secCtxt;
    }

    protected synchronized HttpContext getHttpContext(){
        return httpContext;
    }

    protected synchronized SecurityContext getSecurityContext(){
        return securityContext;
    }

    protected synchronized ServletContext getServletContext(){
        return servletContext;
    }

    protected synchronized UriInfo getUriContext(){
        return uriContext;
    }

}
