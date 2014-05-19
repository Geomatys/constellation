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
import java.util.Date;
import java.util.List;

/**
 * Just pojo for metadata information write by user
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
//@Mapped
@XmlRootElement
public class DataMetadata {

    private String          dataName;
    private String          title;
    private String          anAbstract;
    private List<String>    keywords;
    private String          username;
    private String          organisationName;
    private String          role;
    private String          localeMetadata;
    private String          topicCategory;
    private Date            date;
    private String          dateType;
    private String          localeData;
    private String          type;

    public String getDataName() {
        return dataName;
    }

    public void setDataName(final String dataName) {
        this.dataName = dataName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getAnAbstract() {
        return anAbstract;
    }

    public void setAnAbstract(final String anAbstract) {
        this.anAbstract = anAbstract;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(final List<String> keywords) {
        this.keywords = keywords;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(final String organisationName) {
        this.organisationName = organisationName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public String getLocaleMetadata() {
        return localeMetadata;
    }

    public void setLocaleMetadata(final String localeMetadata) {
        this.localeMetadata = localeMetadata;
    }

    public String getTopicCategory() {
        return topicCategory;
    }

    public void setTopicCategory(final String topicCategory) {
        this.topicCategory = topicCategory;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(final String dateType) {
        this.dateType = dateType;
    }

    public String getLocaleData() {
        return localeData;
    }

    public void setLocaleData(final String localeData) {
        this.localeData = localeData;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
