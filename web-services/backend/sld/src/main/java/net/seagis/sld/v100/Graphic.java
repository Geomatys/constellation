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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
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
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/sld}ExternalGraphic"/>
 *           &lt;element ref="{http://www.opengis.net/sld}Mark"/>
 *         &lt;/choice>
 *         &lt;sequence>
 *           &lt;element ref="{http://www.opengis.net/sld}Opacity" minOccurs="0"/>
 *           &lt;element ref="{http://www.opengis.net/sld}Size" minOccurs="0"/>
 *           &lt;element ref="{http://www.opengis.net/sld}Rotation" minOccurs="0"/>
 *         &lt;/sequence>
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
    "externalGraphicOrMark",
    "opacity",
    "size",
    "rotation"
})
@XmlRootElement(name = "Graphic")
public class Graphic {

    @XmlElements({
        @XmlElement(name = "ExternalGraphic", type = ExternalGraphic.class),
        @XmlElement(name = "Mark", type = Mark.class)
    })
    private List<Object> externalGraphicOrMark;
    @XmlElement(name = "Opacity")
    private ParameterValueType opacity;
    @XmlElement(name = "Size")
    private ParameterValueType size;
    @XmlElement(name = "Rotation")
    private ParameterValueType rotation;

    /**
     * Gets the value of the externalGraphicOrMark property.
     * 
     */
    public List<Object> getExternalGraphicOrMark() {
        if (externalGraphicOrMark == null) {
            externalGraphicOrMark = new ArrayList<Object>();
        }
        return this.externalGraphicOrMark;
    }

    /**
     * Gets the value of the opacity property.
     * 
     */
    public ParameterValueType getOpacity() {
        return opacity;
    }

    /**
     * Sets the value of the opacity property.
     * 
     */
    public void setOpacity(ParameterValueType value) {
        this.opacity = value;
    }

    /**
     * Gets the value of the size property.
     * 
     */
    public ParameterValueType getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     */
    public void setSize(ParameterValueType value) {
        this.size = value;
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
