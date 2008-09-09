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
 *         &lt;element ref="{http://www.opengis.net/sld}Name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}Title" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}Abstract" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}IsDefault" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}FeatureTypeStyle" maxOccurs="unbounded"/>
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
    "name",
    "title",
    "_abstract",
    "isDefault",
    "featureTypeStyle"
})
@XmlRootElement(name = "UserStyle")
public class UserStyle {

    @XmlElement(name = "Name")
    private String name;
    @XmlElement(name = "Title")
    private String title;
    @XmlElement(name = "Abstract")
    private String _abstract;
    @XmlElement(name = "IsDefault")
    private Boolean isDefault;
    @XmlElement(name = "FeatureTypeStyle", required = true)
    private List<FeatureTypeStyle> featureTypeStyle;

    /**
     * Gets the value of the name property.
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the title property.
     * 
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the abstract property.
     * 
     */
    public String getAbstract() {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     */
    public void setAbstract(String value) {
        this._abstract = value;
    }

    /**
     * Gets the value of the isDefault property.
     * 
     */
    public Boolean isIsDefault() {
        return isDefault;
    }

    /**
     * Sets the value of the isDefault property.
     * 
     */
    public void setIsDefault(Boolean value) {
        this.isDefault = value;
    }

    /**
     * Gets the value of the featureTypeStyle property.
     * 
     */
    public List<FeatureTypeStyle> getFeatureTypeStyle() {
        if (featureTypeStyle == null) {
            featureTypeStyle = new ArrayList<FeatureTypeStyle>();
        }
        return this.featureTypeStyle;
    }

}
