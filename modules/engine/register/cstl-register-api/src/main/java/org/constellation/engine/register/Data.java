/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.engine.register;

import java.util.List;

public class Data {

    private int id;
    private String name;
    private String namespace;
    private int providerId;
    private String type;
    private long date;
    private int title;
    private int description;
    private String owner;
    private List<Integer> styleIds;
    private String metadata;

    
    public int getId() {
        return id;
    }

    
    public void setId(int id) {
        this.id = id;        
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public int getProviderId() {
        return providerId;
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
        this.providerId = provider;
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


    @Override
    public String toString() {
        return "Data [id=" + id + ", name=" + name + ", namespace=" + namespace + ", providerId=" + providerId
                + ", type=" + type + ", date=" + date + ", title=" + title + ", description=" + description
                + ", owner=" + owner + ", styleIds=" + styleIds + ", metadata=" + metadata + "]";
    }

    
    
}
