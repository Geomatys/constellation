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

package org.constellation.configuration;

import org.apache.commons.lang3.StringUtils;
import org.mdweb.model.auth.MDwebRole;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Administration database record for {@code User} table.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class UserRecord implements Serializable {

    private String login;
    private String password;
    private String name;
    private List<String> roles;
    private List<String> permissions;

    UserRecord() {
    }

    public UserRecord(final ResultSet rs) throws SQLException {
        this.login       = rs.getString(1);
        this.password    = rs.getString(2);
        this.name        = rs.getString(3);
        this.roles       = new ArrayList<>();
        this.permissions = new ArrayList<>();

        final String[] roleArray = StringUtils.split(rs.getString(4), ',');
        if (roleArray != null) {
            this.roles.addAll(Arrays.asList(roleArray));
        }
        for (final String role : roles) {
            this.permissions.addAll(MDwebRole.getPermissionListFromRole(role));
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(final List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(final List<String> permissions) {
        this.permissions = permissions;
    }
}
