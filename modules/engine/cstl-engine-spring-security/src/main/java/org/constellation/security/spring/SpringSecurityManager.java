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
package org.constellation.security.spring;

import org.constellation.security.IncorrectCredentialsException;
import org.constellation.security.NoSecurityManagerException;
import org.constellation.security.SecurityManager;
import org.constellation.security.UnknownAccountException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Olivier NOUGUIER (Geomatys)
 */
public class SpringSecurityManager implements SecurityManager {

    @Override
    public String getCurrentUserLogin() throws NoSecurityManagerException {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null) {
            return false;
        }
        if (!authentication.isAuthenticated()) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            if (!grantedAuthority.getAuthority().equals("ROLE_ANONYMOUS")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAllowed(final String action) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public boolean hasRole(final String role) {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void login(final String login, final String pass)
            throws UnknownAccountException, IncorrectCredentialsException {

    }

    @Override
    public void runAs(String login) {
        //FIXME add role from DB.
        Collection<SimpleGrantedAuthority> auths = new ArrayList<>();
        auths.add(new SimpleGrantedAuthority("cstl-admin"));
        Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "admin", auths);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    public void logout() {

    }

    @Override
    public void reset() {
        SecurityContextHolder.getContext().setAuthentication(null);

    }
}
