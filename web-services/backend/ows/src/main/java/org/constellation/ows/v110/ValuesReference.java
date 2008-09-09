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
package org.constellation.ows.v110;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import org.geotools.resources.Utilities;


/**
 * Human-readable name of the list of values provided by the referenced document. Can be empty string when this list has no name. 
 * 
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute ref="{http://www.opengis.net/ows/1.1}reference use="required""/>
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "ValuesReference")
public class ValuesReference {

    @XmlValue
    private String value;
    @XmlAttribute(namespace = "http://www.opengis.net/ows/1.1", required = true)
    @XmlSchemaType(name = "anyURI")
    private String reference;

    /**
     * Empty constructor used by JAXB.
     */
    ValuesReference(){
    }
    
    /**
     * Build a new Values reference.
     */
    public ValuesReference(String value, String reference){
        this.value     = value;
        this.reference = reference;
    }
    
    /**
     * Gets the value of the value property.
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the value of the reference property.
     */
    public String getReference() {
        return reference;
    }

    /**
     * Verify that this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final  ValuesReference that = ( ValuesReference) object;

        return Utilities.equals(this.reference,   that.reference)   &&
               Utilities.equals(this.value,       that.value);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 17 * hash + (this.reference != null ? this.reference.hashCode() : 0);
        return hash;
    }

}
