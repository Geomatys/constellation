package org.constellation.engine.register;

import java.util.ArrayList;
import java.util.List;

public class DomainUser extends User {

    private List<Integer> domainIds = new ArrayList<Integer>();
    
    private List<String> roles = new ArrayList<String>();

    public List<Integer> getDomainIds() {
        return domainIds;
    }

    public void setDomainIds(List<Integer> domainIds) {
        this.domainIds = domainIds;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void addRole(String role) {
        roles.add(role);
        
    }

    public void addDomain(Integer domainId) {
        domainIds.add(domainId);
        
    }
    
    
    
}
