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
 * @author Cédric Briançon (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class DataBrief implements Serializable {

    @XmlElement(name="Id")
    private Integer id;
    
    @XmlElement(name="Name")
    private String name;

    @XmlElement(name="Namespace")
    private String namespace;

    @XmlElement(name="Provider")
    private String provider;

    @XmlElement(name="DatasetId")
    private Integer datasetId;

    @XmlElement(name="Parent")
    private String parent;

    @XmlElement(name="Title")
    private String title;

    @XmlElement(name="Date")
    private Date date;

    @XmlElement(name="Type")
    private String type;

    @XmlElement(name="Subtype")
    private String subtype;

    @XmlElement(name="Sensorable")
    private boolean sensorable;

    @XmlElement(name="Rendered")
    private Boolean rendered;

    @XmlElement(name="Owner")
    private String owner;

    @XmlElement(name="TargetStyle")
    private List<StyleBrief> targetStyle = new ArrayList<>(0);

    @XmlElement(name="TargetService")
    private List<ServiceProtocol> targetService = new ArrayList<>(0);

    @XmlElement(name="TargetSensor")
    private List<String> targetSensor = new ArrayList<>(0);

    @XmlElement(name="Stats")
    private String stats;

    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
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

    public Integer getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
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

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(final String subtype) {
        this.subtype = subtype;
    }

    public boolean isSensorable() {
        return sensorable;
    }

    public void setSensorable(boolean sensorable) {
        this.sensorable = sensorable;
    }

    public Boolean getRendered() {
        return rendered;
    }

    public void setRendered(Boolean rendered) {
        this.rendered = rendered;
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

    public List<String> getTargetSensor() {
        return targetSensor;
    }

    public void setTargetSensor(final List<String> targetSensor) {
        this.targetSensor = targetSensor;
    }

    public String getStats() {
        return stats;
    }

    public void setStats(String stats) {
        this.stats = stats;
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
                ", subtype='" + subtype + '\'' +
                ", sensorable='" + sensorable + '\'' +
                ", rendered='" + rendered + '\'' +
                ", owner='" + owner + '\'' +
                ", targetStyle=" + targetStyle +
                ", targetService=" + targetService +
                ", targetSensor=" + targetSensor +
                ", stats=" + stats +
                '}';
    }

}
