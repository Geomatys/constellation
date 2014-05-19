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
package org.constellation.engine.register;

import java.io.Serializable;
import java.util.Set;

public class DomainAccess implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private int domainId;
    
    private Set<String> roles;
    
    public int getDomain() {
        return domainId;
    }
    
    public void setDomainDTO(int domainId) {
        this.domainId = domainId;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;        
    }

}
