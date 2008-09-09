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
 *         &lt;element ref="{http://www.opengis.net/sld}DisplacementX"/>
 *         &lt;element ref="{http://www.opengis.net/sld}DisplacementY"/>
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
    "displacementX",
    "displacementY"
})
@XmlRootElement(name = "Displacement")
public class Displacement {

    @XmlElement(name = "DisplacementX", required = true)
    private ParameterValueType displacementX;
    @XmlElement(name = "DisplacementY", required = true)
    private ParameterValueType displacementY;

    /**
     * Gets the value of the displacementX property.
     * 
     */
    public ParameterValueType getDisplacementX() {
        return displacementX;
    }

    /**
     * Sets the value of the displacementX property.
     * 
     */
    public void setDisplacementX(ParameterValueType value) {
        this.displacementX = value;
    }

    /**
     * Gets the value of the displacementY property.
     * 
     */
    public ParameterValueType getDisplacementY() {
        return displacementY;
    }

    /**
     * Sets the value of the displacementY property.
     * 
     */
    public void setDisplacementY(ParameterValueType value) {
        this.displacementY = value;
    }

}
