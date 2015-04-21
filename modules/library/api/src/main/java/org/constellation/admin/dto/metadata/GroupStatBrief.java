package org.constellation.admin.dto.metadata;

import java.io.Serializable;

/**
 * Pojo that represents a group with stats to serve UI in metadata manager page.
 * @author Mehdi Sidhoum (Geomatys).
 */
public class GroupStatBrief implements Serializable {
    private GroupBrief group;

    private int toValidate;
    private int toPublish;
    private int published;

    public GroupStatBrief() {}

    public GroupStatBrief(final GroupBrief group,final int toValidate, final int toPublish, final int published) {
        this.group = group;
        this.toValidate = toValidate;
        this.toPublish = toPublish;
        this.published = published;
    }

    public GroupBrief getGroup() {
        return group;
    }

    public void setGroup(GroupBrief group) {
        this.group = group;
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
