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

import java.util.HashSet;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.sis.util.logging.Logging;
import org.constellation.engine.register.User;
import org.constellation.engine.register.repository.UserRepository;
import org.mdweb.model.auth.MDwebRole;

/**
 * 
 * @author Guilhem Legal (Geomatys)
 * @since 0.8
 */
public final class DefaultCstlRealm extends AuthorizingRealm {

    /**
     * Logger used for debugging and event notification.
     */
    private static final Logger LOGGER = Logging.getLogger(DefaultCstlRealm.class);

    @Inject
    private UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token)
            throws UnknownAccountException {
        final String username = ((UsernamePasswordToken) token).getUsername();
        checkNotNull(username, "Null username are not allowed by this realm.");

        // Acquire user record.
        User user = userRepository.findOneWithRolesAndDomains(username);
        if (user == null)
            throw new UnknownAccountException();

        // Build and return authentication info.
        return new SimpleAuthenticationInfo(username, user.getPassword(), getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
        checkNotNull(principals, "Null principals are not allowed by this realm.");
        final String username = (String) principals.getPrimaryPrincipal();

        // Acquire user record.
        final User user = userRepository.findOneWithRolesAndDomains(username);
        final HashSet<String> roles = new HashSet<>();
        final HashSet<String> permissions = new HashSet<>();
        for (String role : user.getRoles()) {
            roles.add(role);
            permissions.addAll(MDwebRole.getPermissionListFromRole(role));

        }
        // Build and return authorization info.
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setRoles(roles);
        info.setStringPermissions(permissions);
        return info;
    }

    private static void checkNotNull(final Object reference, final String message) {
        if (reference == null) {
            throw new AuthenticationException(message);
        }
    }
}
