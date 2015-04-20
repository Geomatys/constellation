package org.constellation.model.metadata;

import java.io.Serializable;

/**
 * Pojo that represents a metadata's owner with stats to serve the UI for managing metadata page.
 *
 * @author Mehdi Sidhoum (Geomatys).
 */
public class OwnerStatBrief implements Serializable {

    private User contributor;

    private int toValidate;
    private int toPublish;
    private int published;

    public OwnerStatBrief(){}

    public OwnerStatBrief(final User contributor, final int toValidate, final int toPublish, final int published) {
        this.contributor = contributor;
        this.toValidate = toValidate;
        this.toPublish = toPublish;
        this.published = published;
    }

    public User getContributor() {
        return contributor;
    }

    public void setContributor(User contributor) {
        this.contributor = contributor;
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
