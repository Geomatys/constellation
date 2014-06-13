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



public class Layer {
    
    private int id;

    private String name;

    private String namespace;

    private String alias;

    private int serviceId;

    private int dataId;

    private long date;

    private int title;

    private int description;

    private String config;

    private String owner;

    public Layer() {
        
    }
    
    public Layer(final String name, final String namespace, final String alias, final int serviceId,
            final int dataId, final long date, final int title, final int description, final String config, final String owner) {
        this.name        = name;
        this.namespace   = namespace;
        this.alias       = alias;
        this.serviceId   = serviceId;
        this.dataId      = dataId;
        this.date        = date;
        this.title       = title;
        this.description = description;
        this.config      = config;
        this.owner       = owner;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getService() {
        return serviceId;
    }

    public void setService(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getData() {
        return dataId;
    }

    public void setData(int dataId) {
        this.dataId = dataId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getDescription() {
        return description;
    }

    public void setDescription(int description) {
        this.description = description;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Layer [id=" + id + ", name=" + name + ", namespace=" + namespace + ", alias=" + alias + ", serviceId="
                + serviceId + ", dataId=" + dataId + ", date=" + date + ", title=" + title + ", description="
                + description + ", config=" + config + ", owner=" + owner + "]";
    }
    
    
}
