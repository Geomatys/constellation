package org.constellation.engine.register;

import java.util.ArrayList;
import java.util.List;

import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Domain;

public class DomainUser extends CstlUser {

    private List<Domain> domains = new ArrayList<Domain>();
    
    private List<String> roles = new ArrayList<String>();

   
   
    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void addRole(String role) {
        roles.add(role);
        
    }

    public List<Domain> getDomains() {
        return domains;
    }

    public void setDomains(List<Domain> domains) {
        this.domains = domains;
    }

    public void addDomain(Domain domain) {
        domains.add(domain);
        
    }

   
    
    
    
}
