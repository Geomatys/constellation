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
package org.constellation.ws.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SecurityManager {

    public static String getCurrentUserLogin() {
        final Subject currentUser = SecurityUtils.getSubject();
        return (String) currentUser.getPrincipal();
    }

    public static boolean isAuthenticated() {
        final Subject currentUser = SecurityUtils.getSubject();
        return currentUser.isAuthenticated();
    }

    public static boolean isAllowed(final String action) {
        final Subject currentUser = SecurityUtils.getSubject();
        return currentUser.isPermitted(action);
    }

    public static boolean hasRole(final String role) {
        final Subject currentUser = SecurityUtils.getSubject();
        return currentUser.hasRole(role);
    }

    public static void login(final String login, final String pass) throws UnknownAccountException, IncorrectCredentialsException {
        final UsernamePasswordToken token = new UsernamePasswordToken(login, pass);
        SecurityUtils.getSubject().login(token);
    }

    public static void logout() {
        SecurityUtils.getSubject().logout();
    }
}
