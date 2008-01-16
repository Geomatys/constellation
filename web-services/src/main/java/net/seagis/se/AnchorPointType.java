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
 * <p>Java class for AnchorPointType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AnchorPointType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}AnchorPointX"/>
 *         &lt;element ref="{http://www.opengis.net/se}AnchorPointY"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnchorPointType", propOrder = {
    "anchorPointX",
    "anchorPointY"
})
public class AnchorPointType {

    @XmlElement(name = "AnchorPointX", required = true)
    private ParameterValueType anchorPointX;
    @XmlElement(name = "AnchorPointY", required = true)
    private ParameterValueType anchorPointY;

    /**
     * Empty Constructor used by JAXB.
     */
    AnchorPointType() {
        
    }
    
    /**
     * Build a new Anchor point.
     */
    public AnchorPointType(ParameterValueType anchorPointX, ParameterValueType anchorPointY) {
        this.anchorPointX = anchorPointX;
        this.anchorPointY = anchorPointY;
    }
    
    /**
     * Gets the value of the anchorPointX property.
     */
    public ParameterValueType getAnchorPointX() {
        return anchorPointX;
    }

    /**
     * Gets the value of the anchorPointY property.
     */
    public ParameterValueType getAnchorPointY() {
        return anchorPointY;
    }
}
