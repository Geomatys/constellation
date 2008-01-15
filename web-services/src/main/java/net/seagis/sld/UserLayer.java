/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */


package net.seagis.sld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.se.DescriptionType;


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
 *         &lt;element ref="{http://www.opengis.net/se}Name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Description" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/sld}RemoteOWS"/>
 *         &lt;/choice>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/sld}LayerFeatureConstraints"/>
 *           &lt;element ref="{http://www.opengis.net/sld}LayerCoverageConstraints"/>
 *         &lt;/choice>
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
    "description",
    "remoteOWS",
    "layerFeatureConstraints",
    "layerCoverageConstraints",
    "userStyle"
})
@XmlRootElement(name = "UserLayer")
public class UserLayer {

    @XmlElement(name = "Name", namespace = "http://www.opengis.net/se")
    private String name;
    @XmlElement(name = "Description", namespace = "http://www.opengis.net/se")
    private DescriptionType description;
    @XmlElement(name = "RemoteOWS")
    private RemoteOWS remoteOWS;
    @XmlElement(name = "LayerCoverageConstraints")
    private LayerCoverageConstraints layerCoverageConstraints;
    @XmlElement(name = "UserStyle", required = true)
    private List<UserStyle> userStyle;

    /**
     * Empty Constructor used by JAXB.
     */
    UserLayer() {
        
    }
    
    /**
     * Build a new User Layer.
     */
    public UserLayer(String name, DescriptionType description, RemoteOWS remoteOWS,
            LayerCoverageConstraints layerCoverageConstraints, List<UserStyle> userStyle) {
        
        this.layerCoverageConstraints = layerCoverageConstraints;
        this.name                     = name;
        this.description              = description;
        this.remoteOWS                = remoteOWS;
        this.userStyle                = userStyle;
        
    }
    
    /**
     * Gets the value of the name property.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the description property.
     */
    public DescriptionType getDescription() {
        return description;
    }

    /**
     * Gets the value of the remoteOWS property.
     */
    public RemoteOWS getRemoteOWS() {
        return remoteOWS;
    }

    /**
     * Gets the value of the layerCoverageConstraints property.
     */
    public LayerCoverageConstraints getLayerCoverageConstraints() {
        return layerCoverageConstraints;
    }

    /**
     * Gets the value of the userStyle property.
     * (unmodifiable)
     */
    public List<UserStyle> getUserStyle() {
        if (userStyle == null) {
            userStyle = new ArrayList<UserStyle>();
        }
        return Collections.unmodifiableList(userStyle);
    }

}
