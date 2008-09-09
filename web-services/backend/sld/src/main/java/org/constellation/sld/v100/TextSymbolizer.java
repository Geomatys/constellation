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
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/sld}SymbolizerType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/sld}Geometry" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}Label" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}Font" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}LabelPlacement" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}Halo" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}Fill" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "geometry",
    "label",
    "font",
    "labelPlacement",
    "halo",
    "fill"
})
public class TextSymbolizer extends SymbolizerType {

    @XmlElement(name = "Geometry")
    private Geometry geometry;
    @XmlElement(name = "Label")
    private ParameterValueType label;
    @XmlElement(name = "Font")
    private Font font;
    @XmlElement(name = "LabelPlacement")
    private LabelPlacement labelPlacement;
    @XmlElement(name = "Halo")
    private Halo halo;
    @XmlElement(name = "Fill")
    private Fill fill;

    /**
     * Gets the value of the geometry property.
     * 
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Sets the value of the geometry property.
     * 
     */
    public void setGeometry(Geometry value) {
        this.geometry = value;
    }

    /**
     * Gets the value of the label property.
     * 
     */
    public ParameterValueType getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     */
    public void setLabel(ParameterValueType value) {
        this.label = value;
    }

    /**
     * Gets the value of the font property.
     * 
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the value of the font property.
     * 
     */
    public void setFont(Font value) {
        this.font = value;
    }

    /**
     * Gets the value of the labelPlacement property.
     * 
     */
    public LabelPlacement getLabelPlacement() {
        return labelPlacement;
    }

    /**
     * Sets the value of the labelPlacement property.
     * 
     */
    public void setLabelPlacement(LabelPlacement value) {
        this.labelPlacement = value;
    }

    /**
     * Gets the value of the halo property.
     * 
     */
    public Halo getHalo() {
        return halo;
    }

    /**
     * Sets the value of the halo property.
     * 
     */
    public void setHalo(Halo value) {
        this.halo = value;
    }

    /**
     * Gets the value of the fill property.
     * 
     */
    public Fill getFill() {
        return fill;
    }

    /**
     * Sets the value of the fill property.
     * 
     */
    public void setFill(Fill value) {
        this.fill = value;
    }

}
