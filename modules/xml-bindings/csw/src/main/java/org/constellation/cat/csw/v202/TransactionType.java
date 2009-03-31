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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.constellation.cat.csw.Transaction;


/**
 * Users may insert, update, or delete catalogue entries. 
 * If the verboseResponse attribute has the value "true", then one or more
 * csw:InsertResult elements must be included in the response.
 *          
 * 
 * <p>Java class for TransactionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TransactionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}RequestBaseType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element name="Insert" type="{http://www.opengis.net/cat/csw/2.0.2}InsertType"/>
 *           &lt;element name="Update" type="{http://www.opengis.net/cat/csw/2.0.2}UpdateType"/>
 *           &lt;element name="Delete" type="{http://www.opengis.net/cat/csw/2.0.2}DeleteType"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="verboseResponse" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="requestId" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransactionType", propOrder = {
    "insertOrUpdateOrDelete"
})
@XmlRootElement(name = "Transaction")
public class TransactionType extends RequestBaseType implements Transaction {

    @XmlElements({
        @XmlElement(name = "Insert", type = InsertType.class),
        @XmlElement(name = "Update", type = UpdateType.class),
        @XmlElement(name = "Delete", type = DeleteType.class)
    })
    private List<Object> insertOrUpdateOrDelete;
    @XmlAttribute
    private Boolean verboseResponse;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String requestId;

    /**
     * An empty constructor used by JAXB. 
     */
    public TransactionType() {}
    
    /**
     * Build a new transaction request to insert a list of object
     */
    public TransactionType(String service, String version, InsertType... inserts) {
        super(service, version);
        insertOrUpdateOrDelete = new ArrayList<Object>();
        for (InsertType insert: inserts) {
            insertOrUpdateOrDelete.add(insert);
        }
        verboseResponse = false;
    }

    /**
     * Build a new transaction request to insert a list of object
     */
    public TransactionType(String service, String version, UpdateType... updates) {
        super(service, version);
        insertOrUpdateOrDelete = new ArrayList<Object>();
        for (UpdateType update: updates) {
            insertOrUpdateOrDelete.add(update);
        }
        verboseResponse = false;
    }

    /**
     * Build a new transaction request to delete a list of object
     */
    public TransactionType(String service, String version, DeleteType delete) {
        super(service, version);
        insertOrUpdateOrDelete = new ArrayList<Object>();
        if (delete != null)
            insertOrUpdateOrDelete.add(delete);

        verboseResponse = false;
    }
    
    /**
     * Gets the value of the insertOrUpdateOrDelete property.
     * (unmodifiable)
     */
    public List<Object> getInsertOrUpdateOrDelete() {
        if (insertOrUpdateOrDelete == null) {
            insertOrUpdateOrDelete = new ArrayList<Object>();
        }
        return Collections.unmodifiableList(insertOrUpdateOrDelete);
    }

    /**
     * Gets the value of the verboseResponse property.
     */
    public Boolean isVerboseResponse() {
        if (verboseResponse == null) {
            return false;
        } else {
            return verboseResponse;
        }
    }

    /**
     * Gets the value of the requestId property.
     */
    public String getRequestId() {
        return requestId;
    }

    public String getOutputFormat() {
        return "application/xml";
    }

    public void setOutputFormat(String value) {}
}
