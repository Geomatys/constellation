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
package net.seagis.ogc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;


/**
 * <p>Java class for SortPropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SortPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ogc}PropertyName"/>
 *         &lt;element name="SortOrder" type="{http://www.opengis.net/ogc}SortOrderType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SortPropertyType", propOrder = {
    "propertyName",
    "sortOrder"
})
public class SortPropertyType implements SortBy {

    @XmlElement(name = "PropertyName", required = true)
    private PropertyNameType propertyName;
    @XmlElement(name = "SortOrder")
    private SortOrderType sortOrder;
    
    /**
     * Empty constructor used by JAXB
     */
    SortPropertyType(){
        
    }
    
    /**
     * build a new SOrt property object.
     */
    public SortPropertyType(String propertyName, SortOrder sortOrder) {
        this.propertyName = new PropertyNameType(propertyName);
        if (sortOrder != null && sortOrder.equals(SortOrder.ASCENDING))
            this.sortOrder = SortOrderType.ASC;
        else if (sortOrder != null && sortOrder.equals(SortOrder.DESCENDING))
            this.sortOrder = SortOrderType.DESC;
    }
    
    /**
     * build a new SOrt property object.
     */
    public SortPropertyType(String propertyName, SortOrderType sortOrder) {
        this.propertyName = new PropertyNameType(propertyName);
        this.sortOrder    = sortOrder;
    }

    /**
     * Gets the value of the propertyName property.
     */
    public PropertyNameType getPropertyName() {
        return propertyName;
    }

    /**
     * Gets the value of the sortOrder property.
     */
    public SortOrder getSortOrder() {
        if (sortOrder != null && sortOrder.equals(SortOrderType.ASC))
            return SortOrder.ASCENDING;
        else if (sortOrder != null && sortOrder.equals(SortOrderType.DESC))
            return SortOrder.DESCENDING;
        else
            return SortOrder.ASCENDING;
    }
}
