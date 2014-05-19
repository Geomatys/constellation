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
    
