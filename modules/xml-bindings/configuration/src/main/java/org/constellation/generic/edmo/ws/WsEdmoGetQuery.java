/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le DÃƒÂ©veloppement
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

package org.constellation.generic.edmo.ws;

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
 *         &lt;element name="short_list" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="free_search" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="first_character" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="country_number" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="upper_left_lat" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="upper_left_long" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lower_right_lat" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lower_right_long" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="last_update" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "shortList",
    "freeSearch",
    "firstCharacter",
    "countryNumber",
    "upperLeftLat",
    "upperLeftLong",
    "lowerRightLat",
    "lowerRightLong",
    "lastUpdate"
})
@XmlRootElement(name = "ws_edmo_get_query")
public class WsEdmoGetQuery {

    @XmlElement(name = "short_list")
    private String shortList;
    @XmlElement(name = "free_search")
    private String freeSearch;
    @XmlElement(name = "first_character")
    private String firstCharacter;
    @XmlElement(name = "country_number")
    private String countryNumber;
    @XmlElement(name = "upper_left_lat")
    private String upperLeftLat;
    @XmlElement(name = "upper_left_long")
    private String upperLeftLong;
    @XmlElement(name = "lower_right_lat")
    private String lowerRightLat;
    @XmlElement(name = "lower_right_long")
    private String lowerRightLong;
    @XmlElement(name = "last_update")
    private String lastUpdate;

    /**
     * Gets the value of the shortList property.
     */
    public String getShortList() {
        return shortList;
    }

    /**
     * Sets the value of the shortList property.
     */
    public void setShortList(String value) {
        this.shortList = value;
    }

    /**
     * Gets the value of the freeSearch property.
     */
    public String getFreeSearch() {
        return freeSearch;
    }

    /**
     * Sets the value of the freeSearch property.
     */
    public void setFreeSearch(String value) {
        this.freeSearch = value;
    }

    /**
     * Gets the value of the firstCharacter property.
     */
    public String getFirstCharacter() {
        return firstCharacter;
    }

    /**
     * Sets the value of the firstCharacter property.
     */
    public void setFirstCharacter(String value) {
        this.firstCharacter = value;
    }

    /**
     * Gets the value of the countryNumber property.
     */
    public String getCountryNumber() {
        return countryNumber;
    }

    /**
     * Sets the value of the countryNumber property.
     */
    public void setCountryNumber(String value) {
        this.countryNumber = value;
    }

    /**
     * Gets the value of the upperLeftLat property.
     */
    public String getUpperLeftLat() {
        return upperLeftLat;
    }

    /**
     * Sets the value of the upperLeftLat property.
     */
    public void setUpperLeftLat(String value) {
        this.upperLeftLat = value;
    }

    /**
     * Gets the value of the upperLeftLong property.
     */
    public String getUpperLeftLong() {
        return upperLeftLong;
    }

    /**
     * Sets the value of the upperLeftLong property.
     */
    public void setUpperLeftLong(String value) {
        this.upperLeftLong = value;
    }

    /**
     * Gets the value of the lowerRightLat property.
     */
    public String getLowerRightLat() {
        return lowerRightLat;
    }

    /**
     * Sets the value of the lowerRightLat property.
     */
    public void setLowerRightLat(String value) {
        this.lowerRightLat = value;
    }

    /**
     * Gets the value of the lowerRightLong property.
     */
    public String getLowerRightLong() {
        return lowerRightLong;
    }

    /**
     * Sets the value of the lowerRightLong property.
     */
    public void setLowerRightLong(String value) {
        this.lowerRightLong = value;
    }

    /**
     * Gets the value of the lastUpdate property.
     */
    public String getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Sets the value of the lastUpdate property.
     */
    public void setLastUpdate(String value) {
        this.lastUpdate = value;
    }

}
