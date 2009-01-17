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

package org.constellation.sml.v100;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.constellation.gml.v311.EngineeringCRSType;


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
 *         &lt;element ref="{http://www.opengis.net/gml}EngineeringCRS"/>
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
    "engineeringCRS"
})
@XmlRootElement(name = "spatialReferenceFrame")
public class SpatialReferenceFrame {

    @XmlElement(name = "EngineeringCRS", namespace = "http://www.opengis.net/gml", required = true)
    private EngineeringCRSType engineeringCRS;

    /**
     * Gets the value of the engineeringCRS property.
     * 
     * @return
     *     possible object is
     *     {@link EngineeringCRSType }
     *     
     */
    public EngineeringCRSType getEngineeringCRS() {
        return engineeringCRS;
    }

    /**
     * Sets the value of the engineeringCRS property.
     * 
     * @param value
     *     allowed object is
     *     {@link EngineeringCRSType }
     *     
     */
    public void setEngineeringCRS(EngineeringCRSType value) {
        this.engineeringCRS = value;
    }

}
