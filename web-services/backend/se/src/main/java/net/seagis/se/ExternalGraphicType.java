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
import java.util.List;
import java.util.Collections;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ExternalGraphicType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExternalGraphicType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{http://www.opengis.net/se}OnlineResource"/>
 *           &lt;element ref="{http://www.opengis.net/se}InlineContent"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/se}Format"/>
 *         &lt;element ref="{http://www.opengis.net/se}ColorReplacement" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExternalGraphicType", propOrder = {
    "onlineResource",
    "inlineContent",
    "format",
    "colorReplacement"
})
public class ExternalGraphicType {

    @XmlElement(name = "OnlineResource")
    private OnlineResourceType onlineResource;
    @XmlElement(name = "InlineContent")
    private InlineContentType inlineContent;
    @XmlElement(name = "Format", required = true)
    private String format;
    @XmlElement(name = "ColorReplacement")
    private List<ColorReplacementType> colorReplacement;

    /**
     * Empty Constructor used by JAXB.
     */
    ExternalGraphicType() {
        
    }
    
    /**
     * Build a new External graphic.
     */
    public ExternalGraphicType(OnlineResourceType onlineResource, InlineContentType inlineContent,
            String format, List<ColorReplacementType> colorReplacement) {
        this.colorReplacement = colorReplacement;
        this.onlineResource   = onlineResource;
        this.format           = format;
        this.inlineContent    = inlineContent;
    }
    
    /**
     * Gets the value of the onlineResource property.
     */
    public OnlineResourceType getOnlineResource() {
        return onlineResource;
    }

    /**
     * Gets the value of the inlineContent property.
     */
    public InlineContentType getInlineContent() {
        return inlineContent;
    }

    /**
     * Gets the value of the format property.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Gets the value of the colorReplacement property.
     * (unmodifiable)
     */
    public List<ColorReplacementType> getColorReplacement() {
        if (colorReplacement == null) {
            colorReplacement = new ArrayList<ColorReplacementType>();
        }
        return Collections.unmodifiableList(colorReplacement);
    }

}
