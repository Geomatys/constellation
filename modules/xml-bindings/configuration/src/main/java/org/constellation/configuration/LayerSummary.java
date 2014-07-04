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
}
