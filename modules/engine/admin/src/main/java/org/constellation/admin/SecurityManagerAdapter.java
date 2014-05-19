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
package org.constellation.admin;

import org.constellation.security.IncorrectCredentialsException;
import org.constellation.security.SecurityManager;
import org.constellation.security.UnknownAccountException;
/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class SecurityManagerAdapter implements SecurityManager {

    @Override
    public String getCurrentUserLogin() {
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public boolean isAllowed(final String action) {
        return false;
    }

    @Override
    public boolean hasRole(final String role) {
        return false;
    }

    @Override
    public void login(final String login, final String pass) throws UnknownAccountException, IncorrectCredentialsException {
        //do nothing
    }

    @Override
    public void logout() {
        //do nothing
    }
    
    @Override
    public void runAs(String login) {
    	
    }
    @Override
    public void reset() {
    	
    }

}
