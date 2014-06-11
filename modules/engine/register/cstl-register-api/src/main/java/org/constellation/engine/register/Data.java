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
package org.constellation.engine.register;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Data {

    private Integer id;
    private String name;
    private String namespace;
    private int provider;
    private String type;
    private String subtype;
    private long date;
    private int title;
    private int description;
    private String owner;
    private List<Integer> styleIds;
    private String metadata;
    private String metadataId;
    private String isoMetaData;
    private boolean sensorable;

    
    public Integer getId() {
        return id;
    }

    
    public void setId(Integer id) {
        this.id = id;        
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public int getProvider() {
        return provider;
    }


    public String getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public int getTitle() {
        return title;
    }

    public int getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }


    public List<Integer> getStyles() {
        return styleIds;
    }


    public String getMetadata() {
        return metadata;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setProviderId(int provider) {
        this.provider = provider;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public void setDescription(int description) {
        this.description = description;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setStyleIds(List<Integer> styleIds) {
        this.styleIds = styleIds;
    }

    public void setMetadata(String metaData) {
        this.metadata = metaData;
    }

    
    public String getIsoMetadata() {
        return isoMetaData;
    }

   public void setIsoMetaData(String isoMetaData) {
    this.isoMetaData = isoMetaData;
}

    @Override
    public String toString() {
        return "Data [id=" + id + ", name=" + name + ", namespace=" + namespace + ", provider=" + provider
                + ", type=" + type + ", date=" + date + ", title=" + title + ", description=" + description
                + ", owner=" + owner + ", styleIds=" + styleIds + ", metadata=" + metadata + ", isoMetaData="
                + isoMetaData + "]";
    }


    public boolean isSensorable() {
        return sensorable;
    }


    public void setSensorable(boolean sensorable) {
        this.sensorable = sensorable;
    }


    public boolean hasIsoMetadata() {
        return StringUtils.isNotBlank(isoMetaData);
    }



    public String getMetadataId() {
        return metadataId;
    }


    public void setMetadataId(String metadataId) {
        this.metadataId = metadataId;
    }


    public String getSubtype() {
        return subtype;
    }


    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
    
}
