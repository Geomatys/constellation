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
 *         &lt;element ref="{http://www.opengis.net/sld}Opacity" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}ChannelSelection" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}OverlapBehavior" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}ColorMap" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}ContrastEnhancement" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}ShadedRelief" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}ImageOutline" minOccurs="0"/>
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
    "opacity",
    "channelSelection",
    "overlapBehavior",
    "colorMap",
    "contrastEnhancement",
    "shadedRelief",
    "imageOutline"
})
public class RasterSymbolizer extends SymbolizerType {

    @XmlElement(name = "Geometry")
    private Geometry geometry;
    @XmlElement(name = "Opacity")
    private ParameterValueType opacity;
    @XmlElement(name = "ChannelSelection")
    private ChannelSelection channelSelection;
    @XmlElement(name = "OverlapBehavior")
    private OverlapBehavior overlapBehavior;
    @XmlElement(name = "ColorMap")
    private ColorMap colorMap;
    @XmlElement(name = "ContrastEnhancement")
    private ContrastEnhancement contrastEnhancement;
    @XmlElement(name = "ShadedRelief")
    private ShadedRelief shadedRelief;
    @XmlElement(name = "ImageOutline")
    private ImageOutline imageOutline;

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
     * Gets the value of the channelSelection property.
     * 
     */
    public ChannelSelection getChannelSelection() {
        return channelSelection;
    }

    /**
     * Sets the value of the channelSelection property.
     * 
     */
    public void setChannelSelection(ChannelSelection value) {
        this.channelSelection = value;
    }

    /**
     * Gets the value of the overlapBehavior property.
     * 
     */
    public OverlapBehavior getOverlapBehavior() {
        return overlapBehavior;
    }

    /**
     * Sets the value of the overlapBehavior property.
     * 
     */
    public void setOverlapBehavior(OverlapBehavior value) {
        this.overlapBehavior = value;
    }

    /**
     * Gets the value of the colorMap property.
     * 
     */
    public ColorMap getColorMap() {
        return colorMap;
    }

    /**
     * Sets the value of the colorMap property.
     * 
     */
    public void setColorMap(ColorMap value) {
        this.colorMap = value;
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

    /**
     * Gets the value of the shadedRelief property.
     * 
     */
    public ShadedRelief getShadedRelief() {
        return shadedRelief;
    }

    /**
     * Sets the value of the shadedRelief property.
     * 
     */
    public void setShadedRelief(ShadedRelief value) {
        this.shadedRelief = value;
    }

    /**
     * Gets the value of the imageOutline property.
     * 
     */
    public ImageOutline getImageOutline() {
        return imageOutline;
    }

    /**
     * Sets the value of the imageOutline property.
     */
    public void setImageOutline(ImageOutline value) {
        this.imageOutline = value;
    }

}
