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

import org.geotoolkit.lang.Immutable;

/**
 * Default Implementation of a QueryRequest.
 * @author Johann Sorel (Geomatys)
 */
@Immutable
public final class DefaultQueryRequest implements QueryRequest{

    private final String name;

    public DefaultQueryRequest(String name) {
        if(name == null) throw new IllegalArgumentException("Request name can not be null");
        this.name = name;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName() {
        return name;
    }

}
