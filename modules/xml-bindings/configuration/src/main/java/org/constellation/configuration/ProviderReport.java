/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.configuration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
@XmlRootElement(name = "ProviderReport")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProviderReport {

    @XmlAttribute
    private String id;
    @XmlAttribute
    private String type;
    @XmlElement(name = "item")
    private List<String> items;

    public ProviderReport() {
    }

    public ProviderReport(final String id, final String type, final List<String> items) {
        this.id = id;
        this.type = type;
        this.items = items;
    }

    /**
     * @return id of the provider
     */
    public String getId() {
        return id;
    }

    /**
     * @param id : provider id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return id of the provider
     */
    public String getType() {
        return type;
    }

    /**
     * @param id : provider id
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the provider items (styles or layers)
     */
    public List<String> getItems() {
        if (items == null) {
            items = new ArrayList<String>();
        }
        return items;
    }

    /**
     * @param items the provider items to set
     */
    public void setItems(List<String> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ProviderReport) {
            final ProviderReport that = (ProviderReport) other;
            return Objects.equals(this.id, that.id) &&
                   Objects.equals(this.type, that.type) &&
                   Objects.equals(this.items, that.items);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 89 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 89 * hash + (this.items != null ? this.items.hashCode() : 0);
        return hash;
    }
}
