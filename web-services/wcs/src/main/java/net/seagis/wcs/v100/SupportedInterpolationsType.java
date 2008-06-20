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


package net.seagis.wcs.v100;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Unordered list of interpolation methods supported. 
 * 
 * <p>Java class for SupportedInterpolationsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SupportedInterpolationsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/wcs}interpolationMethod" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="default" type="{http://www.opengis.net/wcs}InterpolationMethodType" default="nearest neighbor" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SupportedInterpolationsType", propOrder = {
    "interpolationMethod"
})
public class SupportedInterpolationsType {

    @XmlElement(required = true)
    private List<InterpolationMethod> interpolationMethod;
    @XmlAttribute(name = "default")
    private InterpolationMethod _default;
    
    /**
     * Empty constructor used by JAXB
     */
    SupportedInterpolationsType(){
        
    }
    
    /**
     * Build a new List of supported interpolation.
     */
    public SupportedInterpolationsType(InterpolationMethod _default, List<InterpolationMethod> interpolationMethod){
        this._default            = _default;
        this.interpolationMethod = interpolationMethod;
    }

    /**
     * Gets the value of the interpolationMethod property (unModifiable).
     */
    public List<InterpolationMethod> getInterpolationMethod() {
        return this.interpolationMethod;
    }

    /**
     * Gets the value of the default property.
     * 
     */
    public InterpolationMethod getDefault() {
        if (_default == null) {
            return InterpolationMethod.NEAREST_NEIGHBOR;
        } else {
            return _default;
        }
    }
}
