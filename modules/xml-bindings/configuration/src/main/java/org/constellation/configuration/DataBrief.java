/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Bernard Fabien (Geomatys)
 * @author Garcia Benjamin (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class DataBrief implements Serializable {

    @XmlElement(name="Name")
    private String name;

    @XmlElement(name="Namespace")
    private String namespace;

    @XmlElement(name="Provider")
    private String provider;

    @XmlElement(name="Parent")
    private String parent;

    @XmlElement(name="Title")
    private String title;

    @XmlElement(name="Date")
    private Date date;

    @XmlElement(name="Type")
    private String type;

    @XmlElement(name="Sensorable")
    private boolean sensorable;

    @XmlElement(name="UsedAsSensor")
    private boolean usedAsSensor;

    @XmlElement(name="Owner")
    private String owner;

    @XmlElement(name="TargetStyle")
    private List<StyleBrief> targetStyle = new ArrayList<>(0);

    @XmlElement(name="TargetService")
    private List<ServiceProtocol> targetService = new ArrayList<>(0);

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(final String provider) {
        this.provider = provider;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public boolean isSensorable() {
        return sensorable;
    }

    public void setSensorable(boolean sensorable) {
        this.sensorable = sensorable;
    }

    public boolean isUsedAsSensor() {
        return usedAsSensor;
    }

    public void setUsedAsSensor(boolean usedAsSensor) {
        this.usedAsSensor = usedAsSensor;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public List<StyleBrief> getTargetStyle() {
        return targetStyle;
    }

    public void setTargetStyle(final List<StyleBrief> targetStyle) {
        this.targetStyle = targetStyle;
    }

    public List<ServiceProtocol> getTargetService() {
        return targetService;
    }

    public void setTargetService(final List<ServiceProtocol> targetService) {
        this.targetService = targetService;
    }

    @Override
    public String toString() {
        return "DataBrief{" +
                "name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                ", provider='" + provider + '\'' +
                ", parent='" + parent + '\'' +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", sensorable='" + sensorable + '\'' +
                ", usedAsSensor='" + usedAsSensor + '\'' +
                ", owner='" + owner + '\'' +
                ", targetStyle=" + targetStyle +
                '}';
    }

}
