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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * id may be empty. If specified it may be in urn:uuid format or be in some
 * arbitrary format. If id is empty registry must generate globally unique id.
 * 
 * If id is provided and in proper UUID syntax (starts with urn:uuid:) 
 * registry will honour it.
 * 
 * If id is provided and is not in proper UUID syntax then it is used for
 * linkage within document and is ignored by the registry. In this case the
 * registry generates a UUID for id attribute.
 * 
 * id must not be null when object is being retrieved from the registry.
 * 
 * 			
 * 
 * <p>Java class for RegistryObjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegistryObjectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5}Name" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5}Description" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5}Slot" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5}Classification" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5}ExternalIdentifier" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="home" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="objectType" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="status">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName">
 *             &lt;enumeration value="Submitted"/>
 *             &lt;enumeration value="Approved"/>
 *             &lt;enumeration value="Deprecated"/>
 *             &lt;enumeration value="Withdrawn"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegistryObjectType", propOrder = {
    "name",
    "description",
    "slot",
    "classification",
    "externalIdentifier"
})
@XmlSeeAlso({
    UserType.class,
    ClassificationNodeType.class,
    AssociationType.class,
    OrganizationType.class,
    ClassificationType.class,
    RegistryEntryType.class,
    ServiceBindingType.class,
    SpecificationLinkType.class,
    ExternalLinkType.class,
    AuditableEventType.class,
    ExternalIdentifierType.class,
    AdhocQueryType.class,
    Subscription.class
})
public class RegistryObjectType {

    @XmlElement(name = "Name")
    private InternationalStringType name;
    @XmlElement(name = "Description")
    private InternationalStringType description;
    @XmlElement(name = "Slot")
    private List<SlotType> slot;
    @XmlElement(name = "Classification")
    private List<ClassificationType> classification;
    @XmlElement(name = "ExternalIdentifier")
    private List<ExternalIdentifierType> externalIdentifier;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String id;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String home;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String objectType;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String status;

    /**
     * Gets the value of the name property.
     * 
     */
    public InternationalStringType getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     */
    public void setName(InternationalStringType value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     *     
     */
    public InternationalStringType getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     */
    public void setDescription(InternationalStringType value) {
        this.description = value;
    }

    /**
     * Gets the value of the slot property.
     * 
     */
    public List<SlotType> getSlot() {
        if (slot == null) {
            slot = new ArrayList<SlotType>();
        }
        return this.slot;
    }

    /**
     * Gets the value of the classification property.
     */
    public List<ClassificationType> getClassification() {
        if (classification == null) {
            classification = new ArrayList<ClassificationType>();
        }
        return this.classification;
    }

    /**
     * Gets the value of the externalIdentifier property.
     */
    public List<ExternalIdentifierType> getExternalIdentifier() {
        if (externalIdentifier == null) {
            externalIdentifier = new ArrayList<ExternalIdentifierType>();
        }
        return this.externalIdentifier;
    }

    /**
     * Gets the value of the id property.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the home property.
     */
    public String getHome() {
        return home;
    }

    /**
     * Sets the value of the home property.
     */
    public void setHome(String value) {
        this.home = value;
    }

    /**
     * Gets the value of the objectType property.
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Sets the value of the objectType property.
     */
    public void setObjectType(String value) {
        this.objectType = value;
    }

    /**
     * Gets the value of the status property.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     */
    public void setStatus(String value) {
        this.status = value;
    }

}
