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
package org.constellation.sld.v100;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/sld}GraphicFill"/>
 *           &lt;element ref="{http://www.opengis.net/sld}GraphicStroke"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/sld}CssParameter" maxOccurs="unbounded" minOccurs="0"/>
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
    "graphicFill",
    "graphicStroke",
    "cssParameter"
})
@XmlRootElement(name = "Stroke")
public class Stroke {

    @XmlElement(name = "GraphicFill")
    private GraphicFill graphicFill;
    @XmlElement(name = "GraphicStroke")
    private GraphicStroke graphicStroke;
    @XmlElement(name = "CssParameter")
    private List<CssParameter> cssParameter;

    /**
     * Gets the value of the graphicFill property.
     */
    public GraphicFill getGraphicFill() {
        return graphicFill;
    }

    /**
     * Sets the value of the graphicFill property.
     * 
     */
    public void setGraphicFill(GraphicFill value) {
        this.graphicFill = value;
    }

    /**
     * Gets the value of the graphicStroke property.
     * 
     */
    public GraphicStroke getGraphicStroke() {
        return graphicStroke;
    }

    /**
     * Sets the value of the graphicStroke property.
     * 
     */
    public void setGraphicStroke(GraphicStroke value) {
        this.graphicStroke = value;
    }

    /**
     * Gets the value of the cssParameter property.
     * 
     */
    public List<CssParameter> getCssParameter() {
        if (cssParameter == null) {
            cssParameter = new ArrayList<CssParameter>();
        }
        return this.cssParameter;
    }

}
