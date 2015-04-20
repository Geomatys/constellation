package org.constellation.model.metadata;

import java.io.Serializable;

/**
 * Pojo that represents a group with stats to serve UI in metadata manager page.
 * @author Mehdi Sidhoum (Geomatys).
 */
public class GroupStatBrief implements Serializable {
    private int id;
    private String name;

    private int toValidate;
    private int toPublish;
    private int published;

    public GroupStatBrief() {}

    public GroupStatBrief(final int id, final String name,final int toValidate, final int toPublish, final int published) {
        this.id = id;
        this.name = name;
        this.toValidate = toValidate;
        this.toPublish = toPublish;
        this.published = published;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getToValidate() {
        return toValidate;
    }

    public void setToValidate(int toValidate) {
        this.toValidate = toValidate;
    }

    public int getToPublish() {
        return toPublish;
    }

    public void setToPublish(int toPublish) {
        this.toPublish = toPublish;
    }

    public int getPublished() {
        return published;
    }

    public void setPublished(int published) {
        this.published = published;
    }
}
