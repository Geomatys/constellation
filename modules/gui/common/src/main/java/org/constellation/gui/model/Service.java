package org.constellation.gui.model;

import juzu.Mapped;

/**
 * @author bgarcia
 * @since 28/05/13
 */
@Mapped
public class Service {

    private String name;

    private String identifier;

    private String keywords;

    private String description;

    private String versions;

    private Contact serviceContact;

    private AccessConstraint serviceConstraints;


    public Service() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }

    public Contact getServiceContact() {
        return serviceContact;
    }

    public void setServiceContact(Contact serviceContact) {
        this.serviceContact = serviceContact;
    }

    public AccessConstraint getServiceConstraints() {
        return serviceConstraints;
    }

    public void setServiceConstraints(AccessConstraint serviceConstraints) {
        this.serviceConstraints = serviceConstraints;
    }
}
