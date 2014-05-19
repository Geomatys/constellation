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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.DomainAccess;
import org.constellation.engine.register.DomainAccessDTO;
import org.constellation.engine.register.DomainRole;
import org.constellation.engine.register.User;
import org.hibernate.annotations.Target;

@Entity
@Table(schema = "`admin`", name = "`user`")
public class UserEntity implements User {

    @Id
    @Column(name = "`login`")
    private String login;

    @Column(name = "`password`", updatable=false)
    private String password;

    @Column(name = "`lastname`")
    private String lastname;

    @Column(name = "`firstname`")
    private String firstname;

    @Column(name = "`email`")
    private String email;

    
    @OneToMany(targetEntity=DomainAccessEntity.class, cascade= {CascadeType.ALL})
    @JoinColumn(name="`login`")
    private Set<DomainAccess> domains;

    
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(schema="`admin`", name = "`user_x_role`", joinColumns = { @JoinColumn(name = "`login`", referencedColumnName = "`login`")})
    @Column(name="`role`")
    private Set<String> roles;

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

   
   
    
    @Override
    public String toString() {
        return "UserEntity [login=" + login + ", password=" + password + ", lastname=" + lastname + ", firstname="
                + firstname + ", email=" + email + "]";
    }

    @Override
    public void setDomainAccesses(List<DomainAccessDTO> domainsAccesses) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<DomainAccessDTO> getDomainAccesses() {
        // TODO Auto-generated method stub
        return null;
    }

   

   
    
    
    

}
