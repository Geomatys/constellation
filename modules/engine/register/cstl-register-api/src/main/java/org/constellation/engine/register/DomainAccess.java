package org.constellation.engine.register;

import java.util.Set;

public class DomainAccess {

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
