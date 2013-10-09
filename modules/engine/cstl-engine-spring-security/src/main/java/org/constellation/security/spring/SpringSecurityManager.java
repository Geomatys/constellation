/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.security.spring;

import java.util.Collection;

import org.constellation.security.IncorrectCredentialsException;
import org.constellation.security.SecurityManager;
import org.constellation.security.UnknownAccountException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 
 * @author Guilhem Legal (Geomatys)
 * @author Olivier NOUGUIER (Geomatys)
 */
public class SpringSecurityManager implements SecurityManager {

    public String getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    public boolean isAllowed(final String action) {
        throw new RuntimeException("Not implemented yet");
    }

    public boolean hasRole(final String role) {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            if(grantedAuthority.getAuthority().equals(role)) 
                return true;
        }
        return false;
    }

    public void login(final String login, final String pass)
            throws UnknownAccountException, IncorrectCredentialsException {
        
        
        
        
    }

    public void logout() {
        
    }
}
