/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2008, Geomatys
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


package net.seagis.sld.v100;

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
 *         &lt;element ref="{http://www.opengis.net/sld}AnchorPoint" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}Displacement" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}Rotation" minOccurs="0"/>
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
    "anchorPoint",
    "displacement",
    "rotation"
})
@XmlRootElement(name = "PointPlacement")
public class PointPlacement {

    @XmlElement(name = "AnchorPoint")
    private AnchorPoint anchorPoint;
    @XmlElement(name = "Displacement")
    private Displacement displacement;
    @XmlElement(name = "Rotation")
    private ParameterValueType rotation;

    /**
     * Gets the value of the anchorPoint property.
     * 
     */
    public AnchorPoint getAnchorPoint() {
        return anchorPoint;
    }

    /**
     * Sets the value of the anchorPoint property.
     * 
     */
    public void setAnchorPoint(AnchorPoint value) {
        this.anchorPoint = value;
    }

    /**
     * Gets the value of the displacement property.
     * 
     */
    public Displacement getDisplacement() {
        return displacement;
    }

    /**
     * Sets the value of the displacement property.
     * 
     */
    public void setDisplacement(Displacement value) {
        this.displacement = value;
    }

    /**
     * Gets the value of the rotation property.
     * 
     */
    public ParameterValueType getRotation() {
        return rotation;
    }

    /**
     * Sets the value of the rotation property.
     * 
     */
    public void setRotation(ParameterValueType value) {
        this.rotation = value;
    }

}
