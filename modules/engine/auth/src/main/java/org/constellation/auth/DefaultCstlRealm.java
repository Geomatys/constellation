/*
 * MDweb - Open Source tool for cataloging and locating environmental resources
 *         http://mdweb.codehaus.org
 *
 *   Copyright (c) 2010, Institut de Recherche pour le Développement (IRD)
 *   Copyright (c) 2010, Geomatys
 *
 * This file is part of MDweb.
 *
 * MDweb is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation;
 *   version 3.0 of the License.
 *
 * MDweb is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *   Lesser General Public License for more details:
 *         http://www.gnu.org/licenses/lgpl-3.0.html
 *
 */
package org.constellation.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.constellation.configuration.ConfigDirectory;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.logging.Logging;
import org.mdweb.io.auth.AuthenticationReader;
import org.mdweb.io.auth.sql.v24.DataSourceAuthenticationReader;
import org.mdweb.model.auth.UserAuthnInfo;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.8
 */
public class DefaultCstlRealm extends AuthorizingRealm {

    private static final String NO_DB_MSG = "Unable to contact authentication database. refusing the access to anyone";

    private AuthenticationReader authReader;

    private static final Logger LOGGER = Logging.getLogger(DefaultCstlRealm.class);

    public DefaultCstlRealm() {
        final File authProperties = ConfigDirectory.getAuthConfigFile();
        final Properties prop = new Properties();
        try {
            final FileInputStream fis = new FileInputStream(authProperties);
            prop.load(fis);
            final String url = prop.getProperty("cstl_authdb_host");
            final DefaultDataSource ds = new DefaultDataSource(url.replace('\\', '/') + ";");
            authReader = new DataSourceAuthenticationReader(ds);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "unable to find cstl auth properties file.\nAuthentication is not working.");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IOException while loading cstl auth properties file", ex);
        }
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws UnknownAccountException {
        if (authReader == null) {
            LOGGER.warning(NO_DB_MSG);
            throw new UnknownAccountException(NO_DB_MSG);
        }
        final UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        final String username               = upToken.getUsername();
        checkNotNull(username, "Null usernames are not allowed by this realm.");
        final String password;
        try {

            if (!authReader.userExist(username)) {
                throw new UnknownAccountException("There is no account for login:" + username);
            } else {
                password = authReader.getPassword(username);
            }
        } catch ( org.mdweb.model.auth.AuthenticationException ex) {
            LOGGER.log(Level.WARNING, NO_DB_MSG, ex);
            throw new UnknownAccountException(NO_DB_MSG);
        }

        return new SimpleAuthenticationInfo(username, password, getName());
    }

    private void checkNotNull(final Object reference, final String message) {
        if (reference == null) {
            throw new AuthenticationException(message);
        }
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
        checkNotNull(principals, "PrincipalCollection method argument cannot be null.");
        if (authReader == null) {
            LOGGER.warning(NO_DB_MSG);
            throw new UnknownAccountException(NO_DB_MSG);
        }

        final String username              = (String) principals.getPrimaryPrincipal();
        final UserAuthnInfo user           = authReader.getUser(username);
        final Set<String> roles            = new HashSet<String>();
        roles.addAll(user.getRoles());
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roles);
        final Set<String> permissions      = new HashSet<String>();
        permissions.addAll(user.getpermissions());
        info.setStringPermissions(permissions);
        return info;
    }
}
