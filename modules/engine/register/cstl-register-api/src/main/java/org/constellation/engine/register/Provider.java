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

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class Provider implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int id;

    private String identifier;

    private String metadata;

    private String owner;

    private String config;

    private String impl;

    private String type;

    private String parent;

    private String metadataId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getImpl() {
        return impl;
    }

    public void setImpl(String impl) {
        this.impl = impl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public boolean hasParent() {
        return StringUtils.isNotBlank(parent);
    }

    public void setMetadataId(String metadataId) {
        this.metadataId = metadataId;
    }
    
    public String getMetadataId() {
        return metadataId;
    }
    @Override
    public String toString() {
        return "Provider [id=" + id + ", identifier=" + identifier + ", metadata=" + metadata + ", owner=" + owner
                + ", config=" + config + ", impl=" + impl + ", type=" + type + ", parent=" + parent + ", metadataId="
                + metadataId + "]";
    }


    
    

}