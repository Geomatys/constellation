/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

import org.constellation.coverage.web.ServiceVersion;


/**
 * Interface for web queries.
 * Thoses are containers for real java objects, that means this should hold only
 * functionnal objects, and the minimum strings possible.
 *
 * @version $Id$
 * @author Johann Sorel (Geomayts)
 * @author Cédric Briançon (Geomatys)
 */
public abstract class Query {
    /**
     * Returns the request type specified for this query.
     */
    public abstract QueryRequest getRequest();

    /**
     * Returns the service name. Never {@code null}.
     */
    public abstract QueryService getService();

    /**
     * Returns the version of the service chosen, or {@code null} if not specified.
     */
    public abstract ServiceVersion getVersion();
    
    /**
     * Returns the exception format. Never {@code null}.
     */
    public abstract String getExceptionFormat();
}
