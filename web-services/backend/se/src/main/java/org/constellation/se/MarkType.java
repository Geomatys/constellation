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
package org.constellation.se;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MarkType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MarkType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/se}WellKnownName"/>
 *           &lt;sequence>
 *             &lt;choice>
 *               &lt;element ref="{http://www.opengis.net/se}OnlineResource"/>
 *               &lt;element ref="{http://www.opengis.net/se}InlineContent"/>
 *             &lt;/choice>
 *             &lt;element ref="{http://www.opengis.net/se}Format"/>
 *             &lt;element ref="{http://www.opengis.net/se}MarkIndex" minOccurs="0"/>
 *           &lt;/sequence>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/se}Fill" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Stroke" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MarkType", propOrder = {
    "wellKnownName",
    "onlineResource",
    "inlineContent",
    "format",
    "markIndex",
    "fill",
    "stroke"
})
public class MarkType {

    @XmlElement(name = "WellKnownName")
    private String wellKnownName;
    @XmlElement(name = "OnlineResource")
    private OnlineResourceType onlineResource;
    @XmlElement(name = "InlineContent")
    private InlineContentType inlineContent;
    @XmlElement(name = "Format")
    private String format;
    @XmlElement(name = "MarkIndex")
    private BigInteger markIndex;
    @XmlElement(name = "Fill")
    private FillType fill;
    @XmlElement(name = "Stroke")
    private StrokeType stroke;

    /**
     * Empty Constructor used by JAXB.
     */
    MarkType() {
        
    }
    
    /**
     * Build a new Mark.
     */
    public MarkType(String wellKnownName, OnlineResourceType onlineResource, InlineContentType inlineContent,
            String format, BigInteger markIndex, FillType fill, StrokeType stroke) {
        this.wellKnownName  = wellKnownName;
        this.onlineResource = onlineResource;
        this.inlineContent  = inlineContent;
        this.format         = format;
        this.fill           = fill;
        this.stroke         = stroke;
        this.markIndex      = markIndex;
    }
    
    /**
     * Gets the value of the wellKnownName property.
     */
    public String getWellKnownName() {
        return wellKnownName;
    }

    /**
     * Gets the value of the onlineResource property.
     * 
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
     * Gets the value of the markIndex property.
     */
    public BigInteger getMarkIndex() {
        return markIndex;
    }

    /**
     * Gets the value of the fill property.
     */
    public FillType getFill() {
        return fill;
    }

    /**
     * Gets the value of the stroke property.
     */
    public StrokeType getStroke() {
        return stroke;
    }
}
