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

import com.google.common.base.Optional;
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
import org.constellation.engine.register.DomainUser;
import org.constellation.engine.register.User;
import org.constellation.engine.register.repository.UserRepository;
import org.mdweb.model.auth.MDwebRole;

import javax.inject.Inject;
import java.util.HashSet;
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
        Optional<DomainUser> user = userRepository.findOneWithRolesAndDomains(username);
        if (user.isPresent())
            return new SimpleAuthenticationInfo(username, user.get().getPassword(), getName());
        throw new UnknownAccountException();
        // Build and return authentication info.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
        checkNotNull(principals, "Null principals are not allowed by this realm.");
        final String username = (String) principals.getPrimaryPrincipal();

        // Acquire user record.
        final User user = userRepository.findOne(username);
        final HashSet<String> roles = new HashSet<>();
        final HashSet<String> permissions = new HashSet<>();
        for (String role : userRepository.getRoles(user.getId())) {
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
