/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.sld.v100;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/sld}Name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}RemoteOWS" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}LayerFeatureConstraints"/>
 *         &lt;element ref="{http://www.opengis.net/sld}UserStyle" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "remoteOWS",
    "layerFeatureConstraints",
    "userStyle"
})
@XmlRootElement(name = "UserLayer")
public class UserLayer {

    @XmlElement(name = "Name")
    private String name;
    @XmlElement(name = "RemoteOWS")
    private RemoteOWS remoteOWS;
    @XmlElement(name = "LayerFeatureConstraints", required = true)
    private LayerFeatureConstraints layerFeatureConstraints;
    @XmlElement(name = "UserStyle", required = true)
    private List<UserStyle> userStyle;

    /**
     * Gets the value of the name property.
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the remoteOWS property.
     * 
     */
    public RemoteOWS getRemoteOWS() {
        return remoteOWS;
    }

    /**
     * Sets the value of the remoteOWS property.
     * 
     */
    public void setRemoteOWS(RemoteOWS value) {
        this.remoteOWS = value;
    }

    /**
     * Gets the value of the layerFeatureConstraints property.
     * 
     */
    public LayerFeatureConstraints getLayerFeatureConstraints() {
        return layerFeatureConstraints;
    }

    /**
     * Sets the value of the layerFeatureConstraints property.
     * 
     */
    public void setLayerFeatureConstraints(LayerFeatureConstraints value) {
        this.layerFeatureConstraints = value;
    }

    /**
     * Gets the value of the userStyle property.
     * 
     */
    public List<UserStyle> getUserStyle() {
        if (userStyle == null) {
            userStyle = new ArrayList<UserStyle>();
        }
        return this.userStyle;
    }

}
