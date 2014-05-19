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
package org.constellation.configuration;

import java.util.ArrayList;
import java.util.Date;
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

    /**
     * type define by org.constellation.provider.ProviderType
     */
    @XmlAttribute
    private String abstractType;

    @XmlAttribute
    private Date date;

    @XmlElement(name = "item")
    private List<DataBrief> items;

    public ProviderReport() {
    }

    public ProviderReport(final String id, final String type, final List<DataBrief> items, final Date date, final String abstractType) {
        this.id = id;
        this.type = type;
        this.items = items;
        this.date = date;
        this.abstractType = abstractType;
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
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the provider items (styles or layers)
     */
    public List<DataBrief> getItems() {
        if (items == null) {
            items = new ArrayList<DataBrief>(0);
        }
        return items;
    }

    /**
     * @param items the provider items to set
     */
    public void setItems(List<DataBrief> items) {
        this.items = items;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getAbstractType() {
        return abstractType;
    }

    public void setAbstractType(final String abstractType) {
        this.abstractType = abstractType;
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
