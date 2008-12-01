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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;
import org.geotools.util.Utilities;


/**
 * <p>Java class for ElementSetNameType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ElementSetNameType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.opengis.net/cat/csw/2.0.2>ElementSetType">
 *       &lt;attribute name="typeNames" type="{http://www.opengis.net/cat/csw/2.0.2}TypeNameListType" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ElementSetNameType", propOrder = {
    "value"
})
public class ElementSetNameType {

    @XmlValue
    private ElementSetType value;
    @XmlAttribute
    private List<QName> typeNames;
    
    /**
     * An empty constructor used by JAXB
     */
    public ElementSetNameType(){
        
    }
    
    /**
     * Build a elementSetName with only the elementSet value (no typeNames).
     */
    public ElementSetNameType(ElementSetType value){
        this.value = value;
    }

    /**
     * Named subsets of catalogue object properties; these
     * views are mapped to a specific information model and
     * are defined in an application profile.
     * 
     */
    public ElementSetType getValue() {
        return value;
    }
    
    /**
     * Named subsets of catalogue object properties; these
     * views are mapped to a specific information model and
     * are defined in an application profile.
     * 
     */
    public void setValue(ElementSetType value) {
        this.value = value;
    }

    /**
     * Gets the value of the typeNames property.
     * (unmodifiable)
     */
    public List<QName> getTypeNames() {
        if (typeNames == null) {
            typeNames = new ArrayList<QName>();
        }
        return Collections.unmodifiableList(typeNames);
    }
    
    /**
     * sets the value of the typeNames property.
     */
    public void setTypeNames(QName typeName) {
        if (this.typeNames == null) {
            this.typeNames = new ArrayList<QName>();
        }
        this.typeNames.add(typeName);
    }
    
    /**
     * sets the value of the typeNames property.
     */
    public void setTypeNames(List<QName> typeNames) {
        this.typeNames = typeNames;
    }
    
    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ElementSetNameType) {
            final ElementSetNameType that = (ElementSetNameType) object;
            return Utilities.equals(this.typeNames,  that.typeNames)   &&
                   Utilities.equals(this.value,  that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 43 * hash + (this.typeNames != null ? this.typeNames.hashCode() : 0);
        return hash;
    }

}
