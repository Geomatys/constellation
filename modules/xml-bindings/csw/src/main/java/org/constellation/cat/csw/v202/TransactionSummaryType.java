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
package org.constellation.cat.csw.v202;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *          Reports the total number of catalogue items modified by a transaction 
 *          request (i.e, inserted, updated, deleted). If the client did not 
 *          specify a requestId, the server may assign one (a URI value).
 *          
 * 
 * <p>Java class for TransactionSummaryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TransactionSummaryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="totalInserted" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *         &lt;element name="totalUpdated" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *         &lt;element name="totalDeleted" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="requestId" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransactionSummaryType", propOrder = {
    "totalInserted",
    "totalUpdated",
    "totalDeleted"
})
public class TransactionSummaryType {

    @XmlSchemaType(name = "nonNegativeInteger")
    private int totalInserted;
    @XmlSchemaType(name = "nonNegativeInteger")
    private int totalUpdated;
    @XmlSchemaType(name = "nonNegativeInteger")
    private int totalDeleted;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String requestId;

    /**
     * An empty constructor used by JAXB
     */
    TransactionSummaryType() {
        
    }
    
    /**
     * Build a new Transation summary.
     */
    public TransactionSummaryType(int totalInserted, int totalUpdated, int totalDeleted, String requestId) {
        this.requestId     = requestId;
        this.totalDeleted  = totalDeleted;
        this.totalInserted = totalInserted;
        this.totalUpdated  = totalUpdated;
    }
    
    /**
     * Gets the value of the totalInserted property.
     */
    public int getTotalInserted() {
        return totalInserted;
    }

    /**
     * Gets the value of the totalUpdated property.
     */
    public int getTotalUpdated() {
        return totalUpdated;
    }

    /**
     * Gets the value of the totalDeleted property.
     */
    public int getTotalDeleted() {
        return totalDeleted;
    }

    /**
     * Gets the value of the requestId property.
     */
    public String getRequestId() {
        return requestId;
    }
}
