/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.services;

import org.constellation.ws.rest.SessionData;

public class SessionDataImpl implements SessionData {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Active domainId, this domain will be linked to new resources (data,
     * layer, provider or service).
     */
    private int activeDomain = 1;

    @Override
    public int getActiveDomainId() {
        return activeDomain;
    }

    @Override
    public void setActiveDomain(int activeDomain) {
        this.activeDomain = activeDomain;
    }

}
