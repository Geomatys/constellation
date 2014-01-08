/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.dto;

import juzu.Mapped;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Service part on getCapabilities.
 * It's a DTO used from Juzu to constellation server side.
 * It contains all service description which will use on GetCapabilities call for all choose version.
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
@Mapped
@XmlRootElement
public class Service {

    private String name;

    private String identifier;

    private List<String> keywords;

    private String description;

    private List<String> versions;

    private Contact serviceContact;

    private AccessConstraint serviceConstraints;

    private boolean transactional;

    public Service() {
    }

    public Service(final String name, final String identifier, final List<String> keywords,
                   final String description, final List<String> versions, final Contact serviceContact,
                   final AccessConstraint serviceConstraints, final boolean transactionnal) {
        this.description = description;
        this.identifier  = identifier;
        this.keywords    = keywords;
        this.name        = name;
        this.serviceConstraints = serviceConstraints;
        this.serviceContact = serviceContact;
        this.versions = versions;
        this.transactional = transactionnal;
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

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(final boolean transactional) {
        this.transactional = transactional;
    }
}
