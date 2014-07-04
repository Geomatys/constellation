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

package org.constellation.admin.dao;

import org.constellation.admin.EmbeddedDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class TaskRecord extends Record {

    public static enum TaskState {
        PENDING,
        SUCCEED,
        FAILED
    }

    private Session session;

    private final String identifier;
    private TaskState state;
    private final String type;
    private final Date start;
    private final Date end;
    private final String owner;

    TaskRecord(final Session session, final String identifier, final TaskState state, final String type, final Date start, final Date end, final String owner) {
        this.session     = session;
        this.identifier  = identifier;
        this.state       = state;
        this.type        = type;
        this.start       = start;
        this.end         = end;
        this.owner       = owner;
    }

    public TaskRecord(final Session s, final ResultSet rs) throws SQLException {
        this(s, rs.getString(1),
                TaskState.valueOf(rs.getString(2)),
                rs.getString(3),
                new Date(rs.getLong(4)),
                rs.getString(5) != null ? new Date(rs.getLong(5)) : null,
                rs.getString(6));
    }

    /**
     * {@inheritDoc}
     */
    public void ensureConnectionNotClosed() throws SQLException {
        if (session.isClosed()) {
            session = EmbeddedDatabase.createSession();
        }
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
        ensureConnectionNotClosed();
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


}
