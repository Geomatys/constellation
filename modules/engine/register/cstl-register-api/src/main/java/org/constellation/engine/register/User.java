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

import java.util.HashSet;
import java.util.Set;

public class User {

    private String lastname;
    
    private String firstname;
    
    private String email;
    
    private String login;

    private String password;

    private Set<String> roles = new HashSet<String>();
    
    private Set<Integer> domains = new HashSet<Integer>();
    
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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


    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    
    public boolean addRole(String role) {
        return roles.add(role);
    }
    

    public boolean removeRole(String role) {
        return roles.remove(role);
    }
    
    
    public Set<Integer> getDomains() {
        return domains;
    }
    
    public void setDomains(Set<Integer> domains) {
        this.domains = domains;
    }

    public void addDomain(int domainId) {
        domains.add(domainId);
    }

    @Override
    public String toString() {
        return "User [lastname=" + lastname + ", firstname=" + firstname + ", email=" + email + ", login=" + login
                + ", password=" + password + ", roles=" + roles + ", domains=" + domains + "]";
    }
    
  

    

}
