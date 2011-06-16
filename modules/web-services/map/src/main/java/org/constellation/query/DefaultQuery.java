/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

package org.constellation.query;

import net.jcip.annotations.Immutable;
import org.geotoolkit.util.Version;

/**
 * Default Implementation of a Query.
 * @author Johann Sorel (Geomatys)
 */
@Immutable
public final class DefaultQuery implements Query{

    private final QueryRequest request;
    private final String service;
    private final Version version;
    private final String exception;

    public DefaultQuery(QueryRequest request, String service, Version version, String exception) {
        this.request = request;
        this.service = service;
        this.version = version;
        this.exception = exception;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public QueryRequest getRequest() {
        return request;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getService() {
        return service;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Version getVersion() {
        return version;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getExceptionFormat() {
        return exception;
    }

}
