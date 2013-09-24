package org.constellation.admin;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Administration database record for {@code User} table.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class TaskRecord implements Serializable {

    public static enum State {
        PENDING,
        SUCCEED,
        FAILED
    }

    final String identifier;
    final String type;
    final State state;
    final String description;
    final long start;
    final Long end;
    final String owner;

    public TaskRecord(final ResultSet rs) throws SQLException {
        this.identifier  = rs.getString(1);
        this.type        = rs.getString(2);
        this.state       = State.valueOf(rs.getString(3));
        this.description = rs.getString(4);
        this.start       = rs.getLong(5);
        this.end         = rs.getLong(6);
        this.owner       = rs.getString(7);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getType() {
        return type;
    }

    public State getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }

    public long getStart() {
        return start;
    }

    public Long getEnd() {
        return end;
    }

    public String getOwner() {
        return owner;
    }
}
