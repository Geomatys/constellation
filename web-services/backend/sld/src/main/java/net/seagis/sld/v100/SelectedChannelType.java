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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SelectedChannelType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SelectedChannelType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/sld}SourceChannelName"/>
 *         &lt;element ref="{http://www.opengis.net/sld}ContrastEnhancement" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SelectedChannelType", propOrder = {
    "sourceChannelName",
    "contrastEnhancement"
})
public class SelectedChannelType {

    @XmlElement(name = "SourceChannelName", required = true)
    private String sourceChannelName;
    @XmlElement(name = "ContrastEnhancement")
    private ContrastEnhancement contrastEnhancement;

    /**
     * Gets the value of the sourceChannelName property.
     * 
     */
    public String getSourceChannelName() {
        return sourceChannelName;
    }

    /**
     * Sets the value of the sourceChannelName property.
     * 
     */
    public void setSourceChannelName(String value) {
        this.sourceChannelName = value;
    }

    /**
     * Gets the value of the contrastEnhancement property.
     * 
     */
    public ContrastEnhancement getContrastEnhancement() {
        return contrastEnhancement;
    }

    /**
     * Sets the value of the contrastEnhancement property.
     * 
     */
    public void setContrastEnhancement(ContrastEnhancement value) {
        this.contrastEnhancement = value;
    }

}
