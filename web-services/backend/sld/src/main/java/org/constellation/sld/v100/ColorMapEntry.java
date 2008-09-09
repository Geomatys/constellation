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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;attribute name="color" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="label" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="opacity" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="quantity" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "ColorMapEntry")
public class ColorMapEntry {

    @XmlAttribute(required = true)
    private String color;
    @XmlAttribute
    private String label;
    @XmlAttribute
    private Double opacity;
    @XmlAttribute
    private Double quantity;

    /**
     * Gets the value of the color property.
     * 
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     */
    public void setColor(String value) {
        this.color = value;
    }

    /**
     * Gets the value of the label property.
     * 
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the opacity property.
     * 
     */
    public Double getOpacity() {
        return opacity;
    }

    /**
     * Sets the value of the opacity property.
     * 
     */
    public void setOpacity(Double value) {
        this.opacity = value;
    }

    /**
     * Gets the value of the quantity property.
     * 
     */
    public Double getQuantity() {
        return quantity;
    }

    /**
     * Sets the value of the quantity property.
     * 
     */
    public void setQuantity(Double value) {
        this.quantity = value;
    }

}
