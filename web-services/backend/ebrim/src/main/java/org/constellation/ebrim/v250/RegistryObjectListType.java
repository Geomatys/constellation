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

package org.constellation.ebrim.v250;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegistryObjectListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegistryObjectListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5}RegistryObject" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5}ObjectRef" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegistryObjectListType", propOrder = {
    "registryObject",
    "objectRef"
})
public class RegistryObjectListType {

    @XmlElement(name = "RegistryObject")
    private List<RegistryObjectType> registryObject;
    @XmlElement(name = "ObjectRef")
    private List<ObjectRefType> objectRef;

    /**
     * Gets the value of the registryObject property.
     */
    public List<RegistryObjectType> getRegistryObject() {
        if (registryObject == null) {
            registryObject = new ArrayList<RegistryObjectType>();
        }
        return this.registryObject;
    }

    /**
     * Gets the value of the objectRef property.
     * 
     */
    public List<ObjectRefType> getObjectRef() {
        if (objectRef == null) {
            objectRef = new ArrayList<ObjectRefType>();
        }
        return this.objectRef;
    }

}
