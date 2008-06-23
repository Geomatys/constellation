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


package net.seagis.se;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DisplacementType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DisplacementType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}DisplacementX"/>
 *         &lt;element ref="{http://www.opengis.net/se}DisplacementY"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DisplacementType", propOrder = {
    "displacementX",
    "displacementY"
})
public class DisplacementType {

    @XmlElement(name = "DisplacementX", required = true)
    private ParameterValueType displacementX;
    @XmlElement(name = "DisplacementY", required = true)
    private ParameterValueType displacementY;

    /**
     * Empty Constructor used by JAXB.
     */
    DisplacementType() {
        
    }
    
    /**
     * Build a new Coverage extent with the specified range axis.
     */
    public DisplacementType(ParameterValueType displacementX, ParameterValueType displacementY) {
        this.displacementX = displacementX;
        this.displacementY = displacementY;
    }
    
    /**
     * Gets the value of the displacementX property.
     */
    public ParameterValueType getDisplacementX() {
        return displacementX;
    }

    /**
     * Gets the value of the displacementY property.
     */
    public ParameterValueType getDisplacementY() {
        return displacementY;
    }
}
