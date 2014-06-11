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

import org.apache.commons.lang3.StringUtils;

public class Service {

    private int id;
    private String identifier;
    private String type;
    private long date;
    private int title;
    private int description;
    private String config;
    private String owner;
    private String metadata;
    private String metadataId;
    private String status;
    private String versions;

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

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDate() {
        return this.date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getTitle() {
        return this.title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getDescription() {
        return this.description;
    }

    public void setDescription(int description) {
        this.description = description;
    }

    public String getConfig() {
        return this.config;
    }

    public void setConfig(String config) {
        this.config = config;

    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    
    public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
	
	public String getMetadataId() {
		return metadataId;
	}

	public void setMetadataId(String metadataId) {
		this.metadataId = metadataId;
	}

    public boolean hasIsoMetadata() {
        return StringUtils.isNotBlank(metadata);
    }

	public String getStatus() {
	    return status;
    }

	public void setStatus(String status) {
	    this.status = status;
    }

	

    public String getVersions() {
	    return versions;
    }

	public void setVersions(String versions) {
	    this.versions = versions;
    }

	@Override
    public String toString() {
        return "ServiceDTO [id=" + id + ", identifier=" + identifier + ", type=" + type + ", date=" + date + ", title="
                + title + ", description=" + description + ", config=" + config + ", owner=" + owner + "]";
    }



	
}
