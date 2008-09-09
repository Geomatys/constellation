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
package net.seagis.gml.v311;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Represents a coordinate tuple in one, two, or three dimensions. 
 * Deprecated with GML 3.0 and replaced by DirectPositionType.
 * 
 * <p>Java class for CoordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CoordType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="X" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="Y" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="Z" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoordType", propOrder = {
    "x",
    "y",
    "z"
})
public class CoordType {

    @XmlElement(name = "X", required = true)
    private BigDecimal x;
    @XmlElement(name = "Y")
    private BigDecimal y;
    @XmlElement(name = "Z")
    private BigDecimal z;

    /**
     * Gets the value of the x property.
     * 
     */
    public BigDecimal getX() {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     */
    public void setX(BigDecimal value) {
        this.x = value;
    }

    /**
     * Gets the value of the y property.
     * 
     */
    public BigDecimal getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     */
    public void setY(BigDecimal value) {
        this.y = value;
    }

    /**
     * Gets the value of the z property.
     * 
     */
    public BigDecimal getZ() {
        return z;
    }

    /**
     * Sets the value of the z property.
     * 
     */
    public void setZ(BigDecimal value) {
        this.z = value;
    }

}
