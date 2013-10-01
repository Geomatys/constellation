package org.constellation.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
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
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskRecord implements Serializable {

    public static enum TaskState {
        PENDING,
        SUCCEED,
        FAILED
    }

    private String identifier;
    private String type;
    private TaskState state;
    private String description;
    private long start;
    private Long end;
    private String owner;

    TaskRecord() {
    }

    public TaskRecord(final ResultSet rs) throws SQLException {
        this.identifier  = rs.getString(1);
        this.type        = rs.getString(2);
        this.state       = TaskState.valueOf(rs.getString(3));
        this.description = rs.getString(4);
        this.start       = rs.getLong(5);
        this.end         = rs.getLong(6);
        this.owner       = rs.getString(7);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(final TaskState state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public long getStart() {
        return start;
    }

    public void setStart(final long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(final Long end) {
        this.end = end;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }
}
