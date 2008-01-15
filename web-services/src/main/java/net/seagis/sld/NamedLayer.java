/*
 * Sicade - SystÃ¨mes intÃ©grÃ©s de connaissances pour l'aide Ã  la dÃ©cision en environnement
 * (C) 2005, Institut de Recherche pour le DÃ©veloppement
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
import javax.xml.bind.annotation.XmlElements;
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
 *         &lt;element ref="{http://www.opengis.net/se}Name"/>
 *         &lt;element ref="{http://www.opengis.net/se}Description" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}LayerFeatureConstraints" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/sld}NamedStyle"/>
 *           &lt;element ref="{http://www.opengis.net/sld}UserStyle"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 *  @author Guilhem Legal
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "description",
    //"layerFeatureConstraints",
    "namedStyleOrUserStyle"
})
@XmlRootElement(name = "NamedLayer")
public class NamedLayer {

    @XmlElement(name = "Name", namespace = "http://www.opengis.net/se", required = true)
    private String name;
    @XmlElement(name = "Description", namespace = "http://www.opengis.net/se")
    private DescriptionType description;
    //@XmlElement(name = "LayerFeatureConstraints")
    // private LayerFeatureConstraints layerFeatureConstraints;
    @XmlElements({
        @XmlElement(name = "UserStyle", type = UserStyle.class),
        @XmlElement(name = "NamedStyle", type = NamedStyle.class)
    })
    private List<Object> namedStyleOrUserStyle;

    
    /**
     * build a new named layer.
     */
    public NamedLayer(String name, DescriptionType description, List<Object> namedStyleOrUserStyle) {
        this.name                  = name;
        this.description           = description;
        this.namedStyleOrUserStyle = namedStyleOrUserStyle;
        
    }
    
    /**
     * Empty Constructor used by JAXB.
     */
    NamedLayer() {
        
    }
    
    /**
     * Gets the value of the name property.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the description property.
     * 
     */
    public DescriptionType getDescription() {
        return description;
    }

    /**
     * Gets the value of the namedStyleOrUserStyle property.
     * (unmodifiable)
     */
    public List<Object> getNamedStyleOrUserStyle() {
        if (namedStyleOrUserStyle == null) {
            namedStyleOrUserStyle = new ArrayList<Object>();
        }
        return Collections.unmodifiableList(namedStyleOrUserStyle);
    }


    /**
     * Gets the value of the layerFeatureConstraints property.
     * 
     * @return
     *     possible object is
     *     {@link LayerFeatureConstraints }
     *     
     
   public LayerFeatureConstraints getLayerFeatureConstraints() {
        return layerFeatureConstraints;
    }

    /**
     * Sets the value of the layerFeatureConstraints property.
     * 
     * @param value
     *     allowed object is
     *     {@link LayerFeatureConstraints }
     *     
     
    public void setLayerFeatureConstraints(LayerFeatureConstraints value) {
        this.layerFeatureConstraints = value;
    }*/
}
