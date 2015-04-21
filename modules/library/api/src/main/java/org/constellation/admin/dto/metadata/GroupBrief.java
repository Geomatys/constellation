package org.constellation.admin.dto.metadata;

import java.io.Serializable;

/**
 * Pojo class that represents a group of users
 *
 * @author Mehdi Sidhoum (Geomatys).
 */
public class GroupBrief implements Serializable{
    private int id;
    private String name;

    public GroupBrief() {
    }

    public GroupBrief(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
