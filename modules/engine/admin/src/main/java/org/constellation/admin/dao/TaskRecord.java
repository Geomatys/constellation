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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class TaskRecord implements Record {

    public static enum TaskState {
        PENDING,
        SUCCEED,
        FAILED
    }

    private final Session session;

    private final String identifier;
    private TaskState state;
    private final String type;
    private final int title;
    private final int description;
    private final Date start;
    private final Date end;
    private final String owner;

    TaskRecord(final Session session, final String identifier, final TaskState state, final String type, final int title,
               final int description, final Date start, final Date end, final String owner) {
        this.session     = session;
        this.identifier  = identifier;
        this.state       = state;
        this.type        = type;
        this.title       = title;
        this.description = description;
        this.start       = start;
        this.end         = end;
        this.owner       = owner;
    }

    public TaskRecord(final Session s, final ResultSet rs) throws SQLException {
        this(s, rs.getString(1),
                TaskState.valueOf(rs.getString(2)),
                rs.getString(3),
                rs.getInt(4), rs.getInt(5),
                new Date(rs.getLong(6)),
                rs.getString(7) != null ? new Date(rs.getLong(7)) : null,
                rs.getString(8));
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getType() {
        return type;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(final TaskState state) throws SQLException {
        this.state = state;
        session.updateTask(identifier, state);
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public String getOwnerLogin() {
        return owner;
    }

    public UserRecord getOwner() throws SQLException {
        return session.readUser(owner);
    }

    public String getTitle(final Locale locale) throws SQLException {
        return session.readI18n(title, locale);
    }

    public void setTitle(final Locale locale, final String value) throws SQLException {
        session.updateI18n(title, locale, value);
    }

    public String getDescription(final Locale locale) throws SQLException {
        return session.readI18n(description, locale);
    }

    public void setDescription(final Locale locale, final String value) throws SQLException {
        session.updateI18n(description, locale, value);
    }
}
