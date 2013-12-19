/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.admin;

import org.constellation.security.IncorrectCredentialsException;
import org.constellation.security.SecurityManager;
import org.constellation.security.UnknownAccountException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
 class DummySecurityManager implements SecurityManager {

    @Override
    public String getCurrentUserLogin() {
        return "admin";
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public boolean isAllowed(String action) {
        return true;
    }

    @Override
    public boolean hasRole(String role) {
        return true;
    }

    @Override
    public void login(String login, String pass) throws UnknownAccountException, IncorrectCredentialsException {
        //do nothing
    }

    @Override
    public void logout() {
        //do nothing
    }

}
