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

package org.constellation.dto;

//import juzu.Mapped;

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
//@Mapped
@XmlRootElement
public class Details {

    private String name;

    private String identifier;

    private List<String> keywords;

    private String description;

    private List<String> versions;

    private String lang;

    private Contact serviceContact;

    private AccessConstraint serviceConstraints;

    private boolean transactional;

    public Details() {
    }



    public Details(final String name, final String identifier, final List<String> keywords,
                   final String description, final List<String> versions, final Contact serviceContact,
                   final AccessConstraint serviceConstraints, final boolean transactionnal, final String lang) {
        this.description = description;
        this.identifier  = identifier;
        this.keywords    = keywords;
        this.name        = name;
        this.serviceConstraints = serviceConstraints;
        this.serviceContact = serviceContact;
        this.versions = versions;
        this.transactional = transactionnal;
        this.lang = lang;
        this.versions = versions;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
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
