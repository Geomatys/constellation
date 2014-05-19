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
package org.constellation.engine.register.jpa;

import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.DomainAccess;
import org.constellation.engine.register.User;

@Entity
@Table(schema="`admin`", name = "`user_x_domain`")
public class DomainAccessEntity implements DomainAccess {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="`domain_id`")
    private int domain_id;

    @Id
    @Column(name="`login`")
    private String login;

   

    @ManyToOne(targetEntity = DomainEntity.class, fetch=FetchType.EAGER)
    @JoinColumn(name = "`domain_id`")
    private Domain domain;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "`admin`", name = "`user_x_domain_x_domainrole`", joinColumns = {
            @JoinColumn(name = "`login`", referencedColumnName = "`login`"),
            @JoinColumn(name = "`domain_id`", referencedColumnName = "`domain_id`") })
    @Column(name = "`domainrole`")
    private Set<String> roles;

  

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + domain_id;
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        return result;
    }
    
    
    @Override
    public String toString() {
         return "roles=" + roles.toString() + " in domain " + domain.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DomainAccessEntity other = (DomainAccessEntity) obj;
        if (domain_id != other.domain_id)
            return false;
        if (login == null) {
            if (other.login != null)
                return false;
        } else if (!login.equals(other.login))
            return false;
        return true;
    }
    
    

}
