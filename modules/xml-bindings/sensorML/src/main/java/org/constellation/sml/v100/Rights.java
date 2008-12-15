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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         &lt;element ref="{http://www.opengis.net/sensorML/1.0}documentation"/>
 *       &lt;/sequence>
 *       &lt;attribute name="copyRights" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="intellectualPropertyRights" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="privacyAct" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute ref="{http://www.opengis.net/gml/3.2}id"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "documentation"
})
@XmlRootElement(name = "Rights")
public class Rights {

    @XmlElement(required = true)
    private Documentation documentation;
    @XmlAttribute
    private Boolean copyRights;
    @XmlAttribute
    private Boolean intellectualPropertyRights;
    @XmlAttribute
    private Boolean privacyAct;
    @XmlAttribute(namespace = "http://www.opengis.net/gml/3.2")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    private String id;

    /**
     * Gets the value of the documentation property.
     */
    public Documentation getDocumentation() {
        return documentation;
    }

    /**
     * Sets the value of the documentation property.
     */
    public void setDocumentation(Documentation value) {
        this.documentation = value;
    }

    /**
     * Gets the value of the copyRights property.
     */
    public Boolean isCopyRights() {
        return copyRights;
    }

    /**
     * Sets the value of the copyRights property.
     */
    public void setCopyRights(Boolean value) {
        this.copyRights = value;
    }

    /**
     * Gets the value of the intellectualPropertyRights property.
     */
    public Boolean isIntellectualPropertyRights() {
        return intellectualPropertyRights;
    }

    /**
     * Sets the value of the intellectualPropertyRights property.
     */
    public void setIntellectualPropertyRights(Boolean value) {
        this.intellectualPropertyRights = value;
    }

    /**
     * Gets the value of the privacyAct property.
     */
    public Boolean isPrivacyAct() {
        return privacyAct;
    }

    /**
     * Sets the value of the privacyAct property.
     */
    public void setPrivacyAct(Boolean value) {
        this.privacyAct = value;
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

}
