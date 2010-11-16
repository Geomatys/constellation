/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.map.ws.rs;

import java.net.URI;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Testing implementation of UriInfo, 
 * only queryParameters and pathParameters are set.
 *
 * @author Johann Sorel (Geomatys)
 */
public class BasicUriInfo implements UriInfo{

    private MultivaluedMap<String,String> queryParameters;
    private MultivaluedMap<String,String> pathParameters;

    public BasicUriInfo(MultivaluedMap<String, String> queryParameters,
                          MultivaluedMap<String, String> pathParameters) {
        this.queryParameters = queryParameters;
        this.pathParameters = pathParameters;
    }

    public void setQueryParameters(MultivaluedMap<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public void setPathParameters(MultivaluedMap<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }
    
    @Override
    public String getPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPath(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<PathSegment> getPathSegments() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<PathSegment> getPathSegments(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URI getRequestUri() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URI getAbsolutePath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URI getBaseUri() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        return pathParameters;
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean bln) {
        return pathParameters;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return queryParameters;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean bln) {
        return queryParameters;
    }

    @Override
    public List<String> getMatchedURIs() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getMatchedURIs(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Object> getMatchedResources() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
