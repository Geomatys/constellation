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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * A Subscription for specified Events in an ebXML V3+ registry.
 * 
 * <p>Java class for SubscriptionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SubscriptionType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0}RegistryObjectType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0}Action" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="selector" use="required" type="{urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0}referenceURI" />
 *       &lt;attribute name="startTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="endTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="notificationInterval" type="{http://www.w3.org/2001/XMLSchema}duration" default="P1D" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubscriptionType", propOrder = {
    "action"
})
public class SubscriptionType extends RegistryObjectType {

    @XmlElementRef(name = "Action", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", type = JAXBElement.class)
    private List<JAXBElement<? extends ActionType>> action;
    @XmlAttribute(required = true)
    private String selector;
    @XmlAttribute
    private XMLGregorianCalendar startTime;
    @XmlAttribute
    private XMLGregorianCalendar endTime;
    @XmlAttribute
    private Duration notificationInterval;

    /**
     * Gets the value of the action property.
     */
    public List<JAXBElement<? extends ActionType>> getAction() {
        if (action == null) {
            action = new ArrayList<JAXBElement<? extends ActionType>>();
        }
        return this.action;
    }

    /**
     * Gets the value of the selector property.
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Sets the value of the selector property.
     */
    public void setSelector(String value) {
        this.selector = value;
    }

    /**
     * Gets the value of the startTime property.
     */
    public XMLGregorianCalendar getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     *     
     */
    public void setStartTime(XMLGregorianCalendar value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the endTime property.
     */
    public XMLGregorianCalendar getEndTime() {
        return endTime;
    }

    /**
     * Sets the value of the endTime property.
     */
    public void setEndTime(XMLGregorianCalendar value) {
        this.endTime = value;
    }

    /**
     * Gets the value of the notificationInterval property.
     */
    public Duration getNotificationInterval() {
        return notificationInterval;
    }

    /**
     * Sets the value of the notificationInterval property.
     */
    public void setNotificationInterval(Duration value) {
        this.notificationInterval = value;
    }

}
