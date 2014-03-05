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
package org.constellation.security;



/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface SecurityManager {


    /**
     * Access to current user login
     * @return login as {@link String}
     * @throws NoSecurityManagerException if it don't exist SecurityManager (it's a {@link java.lang.RuntimeException})
     */
    String getCurrentUserLogin() throws NoSecurityManagerException;

    boolean isAuthenticated();
    
    boolean isAllowed(final String action);

    boolean hasRole(final String role);

    void login(final String login, final String pass) throws UnknownAccountException, IncorrectCredentialsException;

    void logout();
    
    void runAs(String login);
    
    void reset();
}
    
