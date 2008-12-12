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


/**
 * Stores the different possibilities of service type available for this webservice.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public abstract class QueryService {
    /**
     * Key which represents the service name.
     */
    public final String key;

    protected QueryService(final String key) {
        this.key = key;
    }
    
    /**
     * WMS string for the parameter {@code service} in a request.
     *
     * @author Cédric Briançon (Geomatys)
     */
    public static final class WMS extends QueryService {
        public WMS() {
            super("WMS");
        }
    }

    /**
     * WCS string for the parameter {@code service} in a request.
     *
     * @author Cédric Briançon (Geomatys)
     */
    public static final class WCS extends QueryService {
        public WCS() {
            super("WCS");
        }
    }
}
