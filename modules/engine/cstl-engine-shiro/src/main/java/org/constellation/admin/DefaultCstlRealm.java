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
import org.constellation.configuration.UserRecord;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    /**
     * Authentication exception message when database connection cannot be established.
     */
    private static final String NO_DB_MSG = "Unable to contact authentication database. Refusing the access to anyone.";


    /**
     * {@inheritDoc}
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws UnknownAccountException {
        final String username = ((UsernamePasswordToken) token).getUsername();
        checkNotNull(username, "Null username are not allowed by this realm.");

        // Acquire user record.
        final UserRecord user = getUser(username);

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
        final UserRecord user = getUser(username);

        // Build and return authorization info.
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        final HashSet<String> roles = new HashSet<>();
        roles.addAll(user.getRoles());
        info.setRoles(roles);
        final HashSet<String> permissions = new HashSet<>();
        permissions.addAll(user.getPermissions());
        info.setStringPermissions(permissions);
        return info;
    }

    /**
     * Retrieves a {@link UserRecord} instance using {@link AdminDatabase#getCachedUser(String)}
     * or query it from the administration database.
     *
     * @param login the user login
     * @return a {@link UserRecord} instance
     * @throws UnknownAccountException if the user does not exist
     */
    private static UserRecord getUser(final String login) throws UnknownAccountException {
        UserRecord user = AdminDatabase.getCachedUser(login);
        if (user == null) {
            AdminSession session = null;
            try {
                session = AdminDatabase.createSession();
                user = session.readUser(login);
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, NO_DB_MSG, ex);
                throw new UnknownAccountException(NO_DB_MSG);
            } finally {
                if (session != null) session.close();
            }
        }
        if (user != null) {
            return user;
        } else {
            throw new UnknownAccountException("There is no account for login: " + login);
        }
    }

    private static void checkNotNull(final Object reference, final String message) {
        if (reference == null) {
            throw new AuthenticationException(message);
        }
    }
}
