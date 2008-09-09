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
import org.constellation.ogc.v100.FilterType;


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
 *         &lt;element ref="{http://www.opengis.net/sld}FeatureTypeName" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}Filter" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}Extent" maxOccurs="unbounded" minOccurs="0"/>
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
    "featureTypeName",
    "filter",
    "extent"
})
@XmlRootElement(name = "FeatureTypeConstraint")
public class FeatureTypeConstraint {

    @XmlElement(name = "FeatureTypeName")
    private String featureTypeName;
    @XmlElement(name = "Filter", namespace = "http://www.opengis.net/ogc")
    private FilterType filter;
    @XmlElement(name = "Extent")
    private List<Extent> extent;

    /**
     * Gets the value of the featureTypeName property.
     * 
     */
    public String getFeatureTypeName() {
        return featureTypeName;
    }

    /**
     * Sets the value of the featureTypeName property.
     * 
     */
    public void setFeatureTypeName(String value) {
        this.featureTypeName = value;
    }

    /**
     * Gets the value of the filter property.
     * 
     */
    public FilterType getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     */
    public void setFilter(FilterType value) {
        this.filter = value;
    }

    /**
     * Gets the value of the extent property.
     * 
     */
    public List<Extent> getExtent() {
        if (extent == null) {
            extent = new ArrayList<Extent>();
        }
        return this.extent;
    }

}
