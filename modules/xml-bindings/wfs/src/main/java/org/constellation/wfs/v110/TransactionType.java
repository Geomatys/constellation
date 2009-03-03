/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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


package org.constellation.wfs.v110;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * The TransactionType defines the Transaction operation.  
 * A Transaction element contains one or more Insert, Update Delete and Native elements that allow a client application
 * to create, modify or remove feature instances from the feature repository that a Web Feature Service controls.
 *          
 * 
 * <p>Java class for TransactionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TransactionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/wfs}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/wfs}LockId" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/wfs}Insert"/>
 *           &lt;element ref="{http://www.opengis.net/wfs}Update"/>
 *           &lt;element ref="{http://www.opengis.net/wfs}Delete"/>
 *           &lt;element ref="{http://www.opengis.net/wfs}Native"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="releaseAction" type="{http://www.opengis.net/wfs}AllSomeType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransactionType", propOrder = {
    "lockId",
    "insertOrUpdateOrDelete"
})
public class TransactionType
    extends BaseRequestType
{

    @XmlElement(name = "LockId")
    private String lockId;
    @XmlElements({
        @XmlElement(name = "Native", type = NativeType.class),
        @XmlElement(name = "Update", type = UpdateElementType.class),
        @XmlElement(name = "Delete", type = DeleteElementType.class),
        @XmlElement(name = "Insert", type = InsertElementType.class)
    })
    private List<Object> insertOrUpdateOrDelete;
    @XmlAttribute
    private AllSomeType releaseAction;

    /**
     * 
     * In order for a client application to operate upon locked feature instances,
     * the Transaction request must include the LockId element.
     * The content of this element must be the lock identifier the client application obtained from a previous
     * GetFeatureWithLock or LockFeature operation.
     *
     * If the correct lock identifier is specified the Web Feature Service knows that the client application may
     * operate upon the locked feature instances.
     * 
     * No LockId element needs to be specified to operate upon unlocked features.
     *                      
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLockId() {
        return lockId;
    }

    /**
     * 
     * In order for a client application to operate upon locked feature instances,
     * the Transaction request must include the LockId element.
     * The content of this element must be the lock identifier the client application obtained from a previous
     * GetFeatureWithLock or LockFeature operation.
     *
     * If the correct lock identifier is specified the Web Feature Service knows that the client application may
     * operate upon the locked feature instances.
     *
     * No LockId element needs to be specified to operate upon unlocked features.
     *                      
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLockId(String value) {
        this.lockId = value;
    }

    /**
     * Gets the value of the insertOrUpdateOrDelete property.
     * 
     */
    public List<Object> getInsertOrUpdateOrDelete() {
        if (insertOrUpdateOrDelete == null) {
            insertOrUpdateOrDelete = new ArrayList<Object>();
        }
        return this.insertOrUpdateOrDelete;
    }

    /**
     * Gets the value of the releaseAction property.
     * 
     * @return
     *     possible object is
     *     {@link AllSomeType }
     *     
     */
    public AllSomeType getReleaseAction() {
        return releaseAction;
    }

    /**
     * Sets the value of the releaseAction property.
     * 
     * @param value
     *     allowed object is
     *     {@link AllSomeType }
     *     
     */
    public void setReleaseAction(AllSomeType value) {
        this.releaseAction = value;
    }

}
