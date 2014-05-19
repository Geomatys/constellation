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
package org.constellation.gui.service.bean;


import juzu.Mapped;

import java.util.Date;

/**
 * @author Benjamin Garcia (Geomatys)
 */
@Mapped
public class LayerData {

    private String idProvider;

    private String type;

    private String name;

    private String namespace;

    private Date date;

    private String owner;

    public LayerData() {
    }

    public LayerData(final String idProvider, final String type, final String name, final Date date, final String owner, final String namespace) {
        this.idProvider = idProvider;
        this.type = type;
        this.name = name;
        this.date = date;
        this.owner = owner;
        this.namespace = namespace;
    }

    public String getIdProvider() {
        return idProvider;
    }

    public void setIdProvider(final String idProvider) {
        this.idProvider = idProvider;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }
}
