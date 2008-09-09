/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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


package org.constellation.ebrim.v300;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SlotType1 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SlotType1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0}ValueList"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0}LongName" />
 *       &lt;attribute name="slotType" type="{urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0}referenceURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SlotType1", propOrder = {
    "valueList"
})
public class SlotType1 {

    @XmlElementRef(name = "ValueList", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", type = JAXBElement.class)
    private JAXBElement<? extends ValueListType> valueList;
    @XmlAttribute(required = true)
    private String name;
    @XmlAttribute
    private String slotType;

    /**
     * Gets the value of the valueList property.
     */
    public JAXBElement<? extends ValueListType> getValueList() {
        return valueList;
    }

    /**
     * Sets the value of the valueList property.
     */
    public void setValueList(JAXBElement<? extends ValueListType> value) {
        this.valueList = ((JAXBElement<? extends ValueListType> ) value);
    }

    /**
     * Gets the value of the name property.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the slotType property.
     */
    public String getSlotType() {
        return slotType;
    }

    /**
     * Sets the value of the slotType property.
     */
    public void setSlotType(String value) {
        this.slotType = value;
    }

}
