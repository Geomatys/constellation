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

package org.constellation.swe.v100;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.constellation.gml.v311.AbstractGMLEntry;
import org.constellation.swe.AbstractDataComponent;


/**
 * Base type for all data components. 
 * 			This is implemented as an XML Schema complexType because it includes both element and attribute content.
 * 
 * <p>Java class for AbstractDataComponentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractDataComponentType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}AbstractGMLType">
 *       &lt;attribute name="fixed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="definition" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractDataComponentType")
@XmlSeeAlso({
    AbstractDataArrayType.class,
    AbstractDataRecordType.class,
    Category.class,
    Text.class,
    BooleanType.class,
    QuantityType.class,
    TimeType.class,
    Count.class,
    ObservableProperty.class,
    TimeRange.class,
    QuantityRange.class,
    CountRange.class
})
public abstract class AbstractDataComponentType extends AbstractGMLEntry implements AbstractDataComponent {

    @XmlAttribute
    private java.lang.Boolean fixed;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String definition;

    public AbstractDataComponentType() {

    }

    public AbstractDataComponentType(String definition) {
        this.definition = definition;
    }

    /**
     * Gets the value of the fixed property.
     */
    public boolean isFixed() {
        if (fixed == null) {
            return false;
        } else {
            return fixed;
        }
    }

    /**
     * Sets the value of the fixed property.
     */
    public void setFixed(java.lang.Boolean value) {
        this.fixed = value;
    }

    /**
     * Gets the value of the definition property.
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * Sets the value of the definition property.
      */
    public void setDefinition(String value) {
        this.definition = value;
    }

}
