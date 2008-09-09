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
package org.constellation.se;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
//import net.opengis.ogc.BinaryOperatorType;
//import net.opengis.ogc.ExpressionType;
//import net.opengis.ogc.LiteralType;
//import net.opengis.ogc.PropertyNameType;


/**
 * 
 *         The "ParameterValueType" uses WFS-Filter expressions to give
 *         values for SE graphic parameters.  A "mixed" element-content
 *         model is used with textual substitution for values.
 *       
 * 
 * <p>Java class for ParameterValueType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ParameterValueType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://www.opengis.net/ogc}expression"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterValueType", propOrder = {
    "content"
})
@XmlSeeAlso({
    SvgParameterType.class
})
public class ParameterValueType {

    @XmlElementRef(name = "expression", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
    @XmlMixed
    private List<Serializable> content;

    /**
     * Empty Constructor used by JAXB.
     */
    ParameterValueType() {
        
    }
    
    /**
     * Build a new Parameter value.
     */
    public ParameterValueType(List<Serializable> content) {
        this.content = content;
    }
    
    /**
     * 
     * The "ParameterValueType" uses WFS-Filter expressions to give
     * values for SE graphic parameters.  A "mixed" element-content
     * model is used with textual substitution for values.
     * Gets the value of the content property.
     * (unmodifiable);
     * 
     */
    public List<Serializable> getContent() {
        if (content == null) {
            content = new ArrayList<Serializable>();
        }
        return this.content;
    }

}
