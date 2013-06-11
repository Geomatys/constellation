package org.constellation.dto;

import juzu.Mapped;

import java.util.List;

/**
 * @author bgarcia
 * @since 28/05/13
 */
@Mapped
public class Service {

    private String name;

    private String identifier;

    private List<String> keywords;

    private String description;

    private List<String> versions;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }
}
