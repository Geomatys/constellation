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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GraphicType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GraphicType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/se}ExternalGraphic"/>
 *           &lt;element ref="{http://www.opengis.net/se}Mark"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/se}Opacity" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Size" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Rotation" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}AnchorPoint" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Displacement" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GraphicType", propOrder = {
    "externalGraphicOrMark",
    "opacity",
    "size",
    "rotation",
    "anchorPoint",
    "displacement"
})
public class GraphicType {

    @XmlElements({
        @XmlElement(name = "Mark", type = MarkType.class),
        @XmlElement(name = "ExternalGraphic", type = ExternalGraphicType.class)
    })
    private List<Object> externalGraphicOrMark;
    @XmlElement(name = "Opacity")
    private ParameterValueType opacity;
    @XmlElement(name = "Size")
    private ParameterValueType size;
    @XmlElement(name = "Rotation")
    private ParameterValueType rotation;
    @XmlElement(name = "AnchorPoint")
    private AnchorPointType anchorPoint;
    @XmlElement(name = "Displacement")
    private DisplacementType displacement;

    
    /**
     * Empty Constructor used by JAXB.
     */
    GraphicType() {
        
    }
    
    /**
     * Build a new graphic.
     */
    public GraphicType(List<Object> externalGraphicOrMark, ParameterValueType opacity, ParameterValueType size,
            ParameterValueType rotation, AnchorPointType anchorPoint, DisplacementType displacement) {
        this.externalGraphicOrMark = externalGraphicOrMark;
        this.opacity               = opacity;
        this.size                  = size;
        this.rotation              = rotation;
        this.anchorPoint           = anchorPoint;
        this.displacement          = displacement;
        
    }
    
    /**
     * Gets the value of the externalGraphicOrMark property.
     * (unmodifiable)
     */
    public List<Object> getExternalGraphicOrMark() {
        if (externalGraphicOrMark == null) {
            externalGraphicOrMark = new ArrayList<Object>();
        }
        return Collections.unmodifiableList(externalGraphicOrMark);
    }

    /**
     * Gets the value of the opacity property.
     */
    public ParameterValueType getOpacity() {
        return opacity;
    }

    /**
     * Gets the value of the size property.
     */
    public ParameterValueType getSize() {
        return size;
    }

    /**
     * Gets the value of the rotation property.
     */
    public ParameterValueType getRotation() {
        return rotation;
    }

    /**
     * Gets the value of the anchorPoint property.
     */
    public AnchorPointType getAnchorPoint() {
        return anchorPoint;
    }

    /**
     * Gets the value of the displacement property.
     */
    public DisplacementType getDisplacement() {
        return displacement;
    }
}
