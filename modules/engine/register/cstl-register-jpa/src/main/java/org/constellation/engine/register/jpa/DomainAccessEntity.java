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
package org.constellation.engine.register.jpa;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.DomainAccess;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Set;

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
