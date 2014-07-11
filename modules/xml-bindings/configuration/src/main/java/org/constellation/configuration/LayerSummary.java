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

import org.constellation.util.DataReference;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Cédric Briançon (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class LayerSummary {
    @XmlElement(name="Id")
    private Integer id;
    @XmlElement(name = "Name")
    private String name;
    @XmlElement(name = "Namespace")
    private String namespace;
    @XmlElement(name = "Alias")
    private String alias;
    @XmlElement(name = "Type")
    private String type;
    @XmlElement(name = "Subtype")
    private String subtype;
    @XmlElement(name = "Date")
    private Date date;
    @XmlElement(name = "Owner")
    private String owner;
    @XmlElement(name = "Provider")
    private String provider;
    @XmlElement(name = "TargetStyle")
    private List<StyleBrief> targetStyle;

    public LayerSummary() {}

    public LayerSummary(final Layer layer, final DataBrief db) {
        this.id = layer.getId();
        this.name = layer.getName().getLocalPart();
        this.namespace = layer.getName().getNamespaceURI();
        this.alias = layer.getAlias();
        this.type = db.getType();
        this.subtype = db.getSubtype();
        this.date = layer.getDate();
        this.owner = layer.getOwner();
        this.provider = db.getProvider();
        this.targetStyle = convertIntoStylesBrief(layer.getStyles());
    }

    private final List<StyleBrief> convertIntoStylesBrief(final List<DataReference> refs) {
        final List<StyleBrief> briefs = new ArrayList<>();
        if (refs != null) {
            for (final DataReference ref: refs) {
                final StyleBrief styleToAdd = new StyleBrief();
                styleToAdd.setProvider(ref.getProviderId());
                final String styleName = ref.getLayerId().getLocalPart();
                styleToAdd.setName(styleName);
                styleToAdd.setTitle(styleName);
                briefs.add(styleToAdd);
            }
        }
        return briefs;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace the namespace to set
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LayerSummary that = (LayerSummary) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (alias != null ? !alias.equals(that.alias) : that.alias != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
        if (provider != null ? !provider.equals(that.provider) : that.provider != null) return false;
        if (subtype != null ? !subtype.equals(that.subtype) : that.subtype != null) return false;
        if (targetStyle != null ? !targetStyle.equals(that.targetStyle) : that.targetStyle != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (subtype != null ? subtype.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        result = 31 * result + (targetStyle != null ? targetStyle.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LayerSummary{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                ", alias='" + alias + '\'' +
                ", type='" + type + '\'' +
                ", subtype='" + subtype + '\'' +
                ", date=" + date +
                ", owner='" + owner + '\'' +
                ", provider='" + provider + '\'' +
                ", targetStyle=" + targetStyle +
                '}';
    }
}
