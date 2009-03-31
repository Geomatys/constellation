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
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Update statements may replace an entire record or only update part of a record:
 *
 * 1) To replace an existing record, include a new instance of the record;
 *
 * 2) To update selected properties of an existing record, include a set of RecordProperty elements.
 * The scope of the update statement is determined by the Constraint element.
 * 
 * The 'handle' is a local identifier for the action.
 *          
 * 
 * <p>Java class for UpdateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpdateType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;any/>
 *           &lt;sequence>
 *             &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}RecordProperty" maxOccurs="unbounded"/>
 *             &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}Constraint"/>
 *           &lt;/sequence>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="handle" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateType", propOrder = {
    "any",
    "recordProperty",
    "constraint"
})
public class UpdateType {

    @XmlAnyElement(lax = true)
    private Object any;
    @XmlElement(name = "RecordProperty")
    private List<RecordPropertyType> recordProperty;
    @XmlElement(name = "Constraint")
    private QueryConstraintType constraint;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    private String handle;

    public UpdateType() {

    }

    public UpdateType(Object any, QueryConstraintType query) {
        this.any        = any;
        this.constraint = query;
    }

    public UpdateType(List<RecordPropertyType> recordProperty, QueryConstraintType query) {
        this.recordProperty = recordProperty;
        this.constraint     = query;
    }
    /**
     * Gets the value of the any property.
     */
    public Object getAny() {
        return any;
    }

    /**
     * Gets the value of the recordProperty property.
     * (unmodifiable)
     */
    public List<RecordPropertyType> getRecordProperty() {
        if (recordProperty == null) {
            recordProperty = new ArrayList<RecordPropertyType>();
        }
        return Collections.unmodifiableList(recordProperty);
    }

    /**
     * Gets the value of the constraint property.
     */
    public QueryConstraintType getConstraint() {
        return constraint;
    }

    /**
     * Gets the value of the handle property.
     */
    public String getHandle() {
        return handle;
    }
}
