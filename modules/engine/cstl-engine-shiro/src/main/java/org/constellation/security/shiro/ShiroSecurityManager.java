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
package org.constellation.security.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.constellation.security.NoSecurityManagerException;
import org.constellation.security.SecurityManager;

/**
 * 
 * @author Guilhem Legal (Geomatys)
 * @author Olivier NOUGUIER (Geomatys)
 */
public class ShiroSecurityManager implements SecurityManager {

    public String getCurrentUserLogin() throws NoSecurityManagerException {
        try{
            final Subject currentUser = SecurityUtils.getSubject();
            return (String) currentUser.getPrincipal();
        } catch (UnavailableSecurityManagerException ex){
            throw new NoSecurityManagerException(ex.getMessage(), ex);
        }
    }

    public boolean isAuthenticated() {
        try {
            final Subject currentUser = SecurityUtils.getSubject();
            return currentUser.isAuthenticated();
        } catch (UnavailableSecurityManagerException ex) {
            return false;
        }
    }

    public boolean isAllowed(final String action) {
        final Subject currentUser = SecurityUtils.getSubject();
        return currentUser.isPermitted(action);
    }

    public boolean hasRole(final String role) {
        final Subject currentUser = SecurityUtils.getSubject();
        return currentUser.hasRole(role);
    }

    public void login(final String login, final String pass)
            throws UnknownAccountException, IncorrectCredentialsException {
        final UsernamePasswordToken token = new UsernamePasswordToken(login,
                pass);
        SecurityUtils.getSubject().login(token);
    }

    public void logout() {
        SecurityUtils.getSubject().logout();
    }

	@Override
	public void runAs(String login) {
		
		throw new RuntimeException("Not implemented");
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
}
