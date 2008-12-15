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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AbstractDataArrayType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractDataArrayType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/swe/1.0}AbstractDataComponentType">
 *       &lt;sequence>
 *         &lt;element name="elementCount">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence minOccurs="0">
 *                   &lt;element ref="{http://www.opengis.net/swe/1.0}Count"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="ref" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractDataArrayType", propOrder = {
    "elementCount"
})
@XmlSeeAlso({
    CurveType.class,
    DataArrayType.class,
    AbstractMatrixType.class
})
public abstract class AbstractDataArrayType extends AbstractDataComponentType {

    @XmlElement(required = true)
    private AbstractDataArrayType.ElementCount elementCount;

    /**
     * Gets the value of the elementCount property.
     */
    public AbstractDataArrayType.ElementCount getElementCount() {
        return elementCount;
    }

    /**
     * Sets the value of the elementCount property.
     */
    public void setElementCount(AbstractDataArrayType.ElementCount value) {
        this.elementCount = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence minOccurs="0">
     *         &lt;element ref="{http://www.opengis.net/swe/1.0}Count"/>
     *       &lt;/sequence>
     *       &lt;attribute name="ref" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "count"
    })
    public static class ElementCount {

        @XmlElement(name = "Count")
        private Count count;
        @XmlAttribute
        @XmlIDREF
        @XmlSchemaType(name = "IDREF")
        private Object ref;

        /**
         * Gets the value of the count property.
         */
        public Count getCount() {
            return count;
        }

        /**
         * Sets the value of the count property.
         */
        public void setCount(Count value) {
            this.count = value;
        }

        /**
         * Gets the value of the ref property.
         */
        public Object getRef() {
            return ref;
        }

        /**
         * Sets the value of the ref property.
         */
        public void setRef(Object value) {
            this.ref = value;
        }

    }

}
