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

package org.constellation.test.utils;

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
        return URI.create("http://something.com");
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

    @Override
    public URI resolve(URI uri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URI relativize(URI uri) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

}
