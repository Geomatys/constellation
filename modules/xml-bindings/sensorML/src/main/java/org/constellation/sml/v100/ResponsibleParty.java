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

package org.constellation.sml.v100;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.geotoolkit.util.Utilities;


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
 *         &lt;element name="individualName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="organizationName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="positionName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sensorML/1.0}contactInfo" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://www.opengis.net/gml}id"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "individualName",
    "organizationName",
    "positionName",
    "contactInfo"
})
@XmlRootElement(name = "ResponsibleParty")
public class ResponsibleParty {

    private String individualName;
    private String organizationName;
    private String positionName;
    private ContactInfo contactInfo;
    @XmlAttribute(namespace = "http://www.opengis.net/gml")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    private String id;

    public ResponsibleParty() {

    }

    public ResponsibleParty(String organizationName) {
        this.organizationName = organizationName;
    }

    public ResponsibleParty(String individualName, String organizationName, String positionName, ContactInfo contactInfo) {
        this.organizationName = organizationName;
        this.contactInfo      = contactInfo;
        this.individualName   = individualName;
        this.positionName     = positionName;
    }

    /**
     * Gets the value of the individualName property.
     */
    public String getIndividualName() {
        return individualName;
    }

    /**
     * Sets the value of the individualName property.
     *     
     */
    public void setIndividualName(String value) {
        this.individualName = value;
    }

    /**
     * Gets the value of the organizationName property.
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * Sets the value of the organizationName property.
     */
    public void setOrganizationName(String value) {
        this.organizationName = value;
    }

    /**
     * Gets the value of the positionName property.
     */
    public String getPositionName() {
        return positionName;
    }

    /**
     * Sets the value of the positionName property.
     */
    public void setPositionName(String value) {
        this.positionName = value;
    }

    /**
     * Gets the value of the contactInfo property.
     */
    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    /**
     * Sets the value of the contactInfo property.
     */
    public void setContactInfo(ContactInfo value) {
        this.contactInfo = value;
    }

    /**
     * Gets the value of the id property.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     */
    public void setId(String value) {
        this.id = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ResponsibleParty]").append("\n");
        if (individualName != null) {
            sb.append("individualName: ").append(individualName).append('\n');
        }
        if (organizationName != null) {
            sb.append("organizationName: ").append(organizationName).append('\n');
        }
        if (positionName != null) {
            sb.append("positionName: ").append(positionName).append('\n');
        }
        if (contactInfo != null) {
            sb.append("contactInfo: ").append(contactInfo).append('\n');
        }
        if (id != null) {
            sb.append("id: ").append(id).append('\n');
        }
        return sb.toString();
    }

    /**
     * Verify if this entry is identical to specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (object instanceof ResponsibleParty) {
            final ResponsibleParty that = (ResponsibleParty) object;
            return Utilities.equals(this.contactInfo,      that.contactInfo)      &&
                   Utilities.equals(this.id,               that.id)               &&
                   Utilities.equals(this.individualName,   that.individualName)   &&
                   Utilities.equals(this.organizationName, that.organizationName) &&
                   Utilities.equals(this.positionName,     that.positionName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.individualName != null ? this.individualName.hashCode() : 0);
        hash = 73 * hash + (this.organizationName != null ? this.organizationName.hashCode() : 0);
        hash = 73 * hash + (this.positionName != null ? this.positionName.hashCode() : 0);
        hash = 73 * hash + (this.contactInfo != null ? this.contactInfo.hashCode() : 0);
        hash = 73 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}
