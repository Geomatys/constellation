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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ServiceBindingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceBindingType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5}RegistryObjectType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5}SpecificationLink" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="service" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="accessURI" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="targetBinding" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceBindingType", propOrder = {
    "specificationLink"
})
public class ServiceBindingType extends RegistryObjectType {

    @XmlElement(name = "SpecificationLink")
    private List<SpecificationLinkType> specificationLink;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String service;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String accessURI;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String targetBinding;

    /**
     * Gets the value of the specificationLink property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the specificationLink property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpecificationLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SpecificationLinkType }
     * 
     * 
     */
    public List<SpecificationLinkType> getSpecificationLink() {
        if (specificationLink == null) {
            specificationLink = new ArrayList<SpecificationLinkType>();
        }
        return this.specificationLink;
    }

    /**
     * Gets the value of the service property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getService() {
        return service;
    }

    /**
     * Sets the value of the service property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setService(String value) {
        this.service = value;
    }

    /**
     * Gets the value of the accessURI property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccessURI() {
        return accessURI;
    }

    /**
     * Sets the value of the accessURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccessURI(String value) {
        this.accessURI = value;
    }

    /**
     * Gets the value of the targetBinding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetBinding() {
        return targetBinding;
    }

    /**
     * Sets the value of the targetBinding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetBinding(String value) {
        this.targetBinding = value;
    }

}
