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
        this.type = layer.getProviderType();
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
