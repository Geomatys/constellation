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

package org.constellation.wfs.ws.rs;

import org.geotoolkit.wfs.xml.WFSResponse;

/**
 *
 * @author Guilhem Legal
 */
public class WFSResponseWrapper implements WFSResponse {

    private final Object response;

    public WFSResponseWrapper(final Object response) {
        this.response = response;
    }

    /**
     * @return the response
     */
    public Object getResponse() {
        return response;
    }
}
