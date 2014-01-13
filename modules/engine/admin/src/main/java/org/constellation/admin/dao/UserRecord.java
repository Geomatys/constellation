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

package org.constellation.admin.dao;

import org.apache.commons.lang3.StringUtils;
import org.constellation.admin.EmbeddedDatabase;
import org.geotoolkit.util.StringUtilities;
import org.mdweb.model.auth.MDwebRole;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class UserRecord extends Record {

    private Session session;

    private final String login;
    private String password;
    private String name;
    private List<String> roles;
    private final List<String> permissions;

    UserRecord(final Session session, final String login, final String password, final String name, final List<String> roles) {
        this.session     = session;
        this.login       = login;
        this.password    = password;
        this.name        = name;
        this.roles       = roles;
        this.permissions = new ArrayList<>();

        // Set permissions from role list.
        for (final String role : roles) {
            permissions.addAll(MDwebRole.getPermissionListFromRole(role));
        }
    }

    public UserRecord(final Session s, final ResultSet rs) throws SQLException {
        this(s, rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                Arrays.asList(StringUtils.split(rs.getString(4), ',')));
    }

    /**
     * {@inheritDoc}
     */
    public void ensureConnectionNotClosed() throws SQLException {
        if (session.isClosed()) {
            session = EmbeddedDatabase.createSession();
        }
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) throws SQLException {
        this.password = StringUtilities.MD5encode(password);
        ensureConnectionNotClosed();
        session.updateUser(login, this.password, name, roles);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) throws SQLException {
        this.name = name;
        ensureConnectionNotClosed();
        session.updateUser(login, password, name, roles);
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(final List<String> roles) throws SQLException {
        this.roles = roles;
        ensureConnectionNotClosed();
        session.updateUser(login, password, name, roles);
    }

    public List<String> getPermissions() {
        return permissions;
    }
}
